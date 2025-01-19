package io.karan.ictdb.http

import cats.effect.IO
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import io.karan.ictdb.auth.*
import io.karan.ictdb.domain.User
import io.karan.ictdb.domain.UserOrigin.{Form, Google}
import io.karan.ictdb.http.auth.{Cookies, checkAuthentication}
import io.karan.ictdb.views.{Layout, LoginPage}
import org.http4s.dsl.io.*
import org.http4s.headers.{Location, `WWW-Authenticate`}
import org.http4s.implicits.uri
import org.http4s.scalatags.*
import org.http4s.server.Router
import org.http4s.{Challenge, HttpRoutes, Response, Status, Uri}

case class AuthController private (crypto: Crypto, gs: GoogleAuthService):
  private val HOME_REDIRECT = Found(Location(uri"http://localhost:8080/"))

  private val loginRoutes = HttpRoutes.of[IO]:
    case req @ GET -> Root =>
      req.checkAuthentication(_ => HOME_REDIRECT) {
        req.cookies.find(_.name == Cookies.LOGIN_TYPE) match
          case None            => Ok(Layout(false, LoginPage()))
          case Some(loginType) =>
            crypto
              .decrypt(loginType.content)
              .flatMap: login =>
                Uri.fromString(s"http://localhost:8080/login/$login") match
                  case Left(_)         => Ok(Layout(false, LoginPage()))
                  case Right(location) => Found(Location(location))
      }

    case req @ GET -> Root / "google" =>
      req.checkAuthentication(_ => HOME_REDIRECT) {
        gs.getRedirectionComponents.flatMap: opt =>
          opt.fold(IO.pure(Response(Status.InternalServerError)))(comp =>
            val googleStateCookie = Cookies.createStateCookie(Google, comp.state)
            val googleCodeCookie  = Cookies.createCodeCookie(Google, comp.verifier)

            Found(Location(comp.uri)).map(_.addCookie(googleStateCookie).addCookie(googleCodeCookie))
          )
      }

    case req @ GET -> Root / "form" =>
      req.checkAuthentication(_ => HOME_REDIRECT) {
        // Check is credentials are correct
        val dummyUser = User(
          id = 1L,
          providerId = "formID",
          username = "korven",
          userEmail = "ahlawatkaran12@gmail.com",
          userPassword = Some("pass"),
          provider = Form
        )
        (crypto.encrypt(dummyUser.toString), crypto.encrypt("form")).parTupled
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
      req.checkAuthentication(_ => HOME_REDIRECT.map(_.removeCookie(Cookies.AUTH).removeCookie(Cookies.LOGIN_TYPE)))(
        Unauthorized(`WWW-Authenticate`(Challenge("username_password", "localhost")))
      )

    case req @ POST -> Root / "register" =>
      Ok("Register form goes here")

  def routes = Router("/login" -> loginRoutes, "/" -> baseRoutes)
end AuthController

object AuthController:
  def make(crypto: Crypto, gs: GoogleAuthService) = AuthController(crypto, gs)
