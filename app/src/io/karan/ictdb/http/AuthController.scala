package io.karan.ictdb.http

import cats.data.Validated.{Invalid, Valid}
import cats.effect.IO
import cats.syntax.all.*
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import io.circe.syntax.*
import io.karan.ictdb.auth.*
import io.karan.ictdb.domain.*
import io.karan.ictdb.http.auth.{Cookies, checkAuthn}
import io.karan.ictdb.http.validation.ValidationError.*
import io.karan.ictdb.http.validation.Validations
import io.karan.ictdb.services.UserService
import io.karan.ictdb.views.{Root as _, *}
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.*
import org.http4s.implicits.uri
import org.http4s.scalatags.*
import org.http4s.server.*

import java.time.Instant

object MissingFieldsOptionalMatcher extends OptionalQueryParamDecoderMatcher[String]("missing")
object ErrOptionalMatcher           extends OptionalQueryParamDecoderMatcher[String]("err")

case class AuthController private (crypto: Crypto, gs: GoogleAuthService, userService: UserService):
  private val HOME_REDIRECT = Found(Location(uri"http://localhost:8080/"))

  private val loginRoutes = HttpRoutes.of[IO]:
    case req @ GET -> Root :? MissingFieldsOptionalMatcher(missing) +& ErrOptionalMatcher(err) =>
      val missingList = missing.map(m => m.split(" ").toList)
      req.checkAuthn(_ => HOME_REDIRECT) {
        req.cookies.find(_.name == Cookies.LOGIN_TYPE) match
          case None            => Ok(LoginPage(missingList, err))
          case Some(loginType) =>
            crypto
              .decrypt(loginType.content)
              .flatMap: login =>
                val location =
                  if login == "form" then uri"http://localhost:8080/login" else uri"http://localhost:8080/login/google"
                Found(Location(location))
      }

    case req @ GET -> Root / "google" =>
      req.checkAuthn(_ => HOME_REDIRECT) {
        gs.getRedirectionComponents.flatMap: opt =>
          opt.fold(InternalServerError())(comp =>
            val googleStateCookie = Cookies.createStateCookie(UserOrigin.Google, comp.state)
            val googleCodeCookie  = Cookies.createCodeCookie(UserOrigin.Google, comp.verifier)

            Found(Location(comp.uri)).map(_.addCookie(googleStateCookie).addCookie(googleCodeCookie))
          )
      }

    case req @ POST -> Root =>
      req.checkAuthn(_ => HOME_REDIRECT) {
        req
          .as[UrlForm]
          .map(Validations.parseLoginUser)
          .flatMap: login =>
            login match
              case Invalid(e)   =>
                val missing = e
                  .map {
                    case MissingFields(fieldName) => fieldName
                    case PasswordMismatch         => ""
                  }
                  .filterNot(_.isBlank)
                UnprocessableEntity(LoginPage(missing = missing.toList.some))
              case Valid(creds) =>
                val user = userService.loginUser(creds.usernameOrEmail, creds.password)
                user.flatMap: userE =>
                  userE match
                    case Left(msg)   => UnprocessableEntity(LoginPage(err = msg.some))
                    case Right(user) =>
                      (crypto.encrypt(user.asJson.toString), crypto.encrypt("form")).parTupled
                        .map: (content, client) =>
                          (Cookies.createAuthCookie(content, 604800L), Cookies.createLoginCookie(client))
                        .flatMap: (auth, login) =>
                          HOME_REDIRECT.map: resp =>
                            resp.addCookie(auth).addCookie(login)
      }

  private val baseRoutes = HttpRoutes.of[IO]:
    case req @ GET -> Root / "callback" =>
      val storedState  = IO.fromOption(req.cookies.find(_.name == Cookies.STATE_FOR_("google")))(
        Exception("google_oauth_state cookie not found")
      )
      val codeVerifier = IO.fromOption(req.cookies.find(_.name == Cookies.VERIFIER_FOR_("google")))(
        Exception("google_code_verifier cookie not found")
      )
      (storedState, codeVerifier).parTupled.flatMap: (state, verifier) =>
        gs
          .parseCodeResponse(req.uri, State(state.content))
          .flatMap((code, state) => gs.exchangeCodeForTokens(code, state, CodeVerifier(verifier.content)))
          .flatMap: tokens =>
            gs
              .getUserInfo(tokens.getBearerAccessToken)
              .map(info => (tokens.getIDToken, info))
          .flatMap: (idToken, userInfo) =>
            gs
              .calculateSecondsToExpiration(idToken)
              .flatMap: maxAge =>
                userService
                  .registerUser(
                    User(
                      0L,
                      userInfo.getSubject.getValue,
                      userInfo.getGivenName,
                      userInfo.getEmailAddress,
                      None,
                      UserOrigin.Google
                    )
                  )
                  .voidError >>
                  (crypto.encrypt(userInfo.toJSONString), crypto.encrypt("google")).parTupled
                    .flatMap: (content, client) =>
                      // Set cookie
                      val authCookie      = Cookies.createAuthCookie(content, maxAge)
                      val loginTypeCookie = Cookies.createLoginCookie(client)

                      HOME_REDIRECT.map: resp =>
                        resp
                          .addCookie(authCookie)
                          .addCookie(loginTypeCookie)
                          .removeCookie(Cookies.STATE_FOR_("google"))
                          .removeCookie(Cookies.VERIFIER_FOR_("google"))

    case req @ GET -> Root / "logout" =>
      req.checkAuthn(_ => HOME_REDIRECT.map(_.removeCookie(Cookies.AUTH).removeCookie(Cookies.LOGIN_TYPE)))(
        Unauthorized(`WWW-Authenticate`(Challenge("username_password", "localhost")))
      )

    case GET -> Root / "register" :? MissingFieldsOptionalMatcher(missing) =>
      val missingList = missing.map(m => m.split(" ").toList)
      Ok(RegisterPage(missingList))

    case req @ POST -> Root / "register" =>
      val respIO =
        for
          form <- req.as[UrlForm]
          rur  <- Validations
                    .parseRegisterUser(form)
                    .fold(
                      nec =>
                        val missing = nec
                          .map {
                            case MissingFields(fieldName) => fieldName
                            case PasswordMismatch         => ""
                          }
                          .filterNot(_.isBlank)
                        IO.raiseError(MissingFields(missing.mkString_(" ")))
                      ,
                      data => IO.pure(data)
                    )
          _    <-
            userService.registerUser(
              User(
                0L,
                Instant.now.getEpochSecond.toString,
                rur.username,
                rur.email,
                Some(rur.password),
                UserOrigin.Form
              )
            )
          resp <- Found(Location(uri"http://localhost:8080/login"))
        yield resp

      respIO.handleErrorWith { case MissingFields(fieldName) =>
        Found(
          Location(
            Uri
              .fromString(s"http://localhost:8080/register?missing=$fieldName")
              .getOrElse(uri"http://localhost:8080/register")
          )
        )
      }

  def routes = Router("/login" -> loginRoutes, "/" -> baseRoutes)
end AuthController

object AuthController:
  def make(crypto: Crypto, gs: GoogleAuthService, userService: UserService) = AuthController(crypto, gs, userService)
