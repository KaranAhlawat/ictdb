package io.karan.ictdb.http.routes

import cats.data.OptionT
import cats.effect.IO
import cats.syntax.all.*
import http4sJsoniter.ArrayEntityCodec.*
import io.karan.ictdb.gen.services.auth.{AuthService, EmailTakenError, UsernameTakenError}
import io.karan.ictdb.gen.services.user.RegisterUserInput
import io.karan.ictdb.http.JsonCodecs.given
import io.karan.ictdb.http.middleware.SecurityMiddlewares
import org.http4s.*
import org.http4s.circe.CirceEntityEncoder.*
import org.http4s.dsl.io.*
import org.http4s.headers.Location
import org.http4s.server.Router
import org.http4s.syntax.all.*
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http4s.*

class AuthRoutes private (
    cookieConfig: SessionConfig,
    smw: SecurityMiddlewares,
    authService: AuthService[IO],
    callbackService: CallbackService[IO]
):
    private val authedTrivial   =
        AuthedRoutes.of[List[CommonProfile], IO] { case req @ GET -> Root as _ =>
            Found(Location(uri"/profiles"))
        }
    private val loginRoutes     =
        Router(
            "form" -> smw.formClientMiddleware(authedTrivial),
            "oidc" -> smw.oidcClientMiddleware(authedTrivial)
        )
    private val protectedRoutes =
        val routes = AuthedRoutes.of[List[CommonProfile], IO] {
            case GET -> Root / "profiles" as profiles =>
                Ok(
                    profiles.map(profile =>
                        s"${profile.getId}: ${profile.getUsername} (${profile.getEmail})"
                    )
                )
        }
        smw.protectedMiddleware(routes)
    private val anonymousRoutes =
        HttpRoutes.of[IO] {
            case req @ POST -> Root / "register" =>
                for
                    body <- req.as[RegisterUserInput]
                    resp <- authService
                                .registerUser(body.username, body.email, body.password)
                                .flatMap(Ok(_))
                                .handleErrorWith {
                                    case e: UsernameTakenError => BadRequest(e)
                                    case e: EmailTakenError    => BadRequest(e)
                                }
                yield resp
            case req @ GET -> Root / "callback"  =>
                callbackService.callback(req)
            case req @ POST -> Root / "callback" =>
                callbackService.callback(req)
            case GET -> Root / "loginForm"       => Ok("Hit the callback manually for now")
        }

    def routes: HttpRoutes[IO] =
        val managedSession = Session.sessionManagement[IO](cookieConfig)

        Router(
            "/login" -> managedSession(loginRoutes),
            "/"      -> (managedSession(anonymousRoutes) <+> managedSession(protectedRoutes))
        )
end AuthRoutes

object AuthRoutes:
    def make(
        cookieConfig: SessionConfig,
        smw: SecurityMiddlewares,
        authService: AuthService[IO],
        callbackService: CallbackService[IO]
    ): AuthRoutes =
        AuthRoutes(cookieConfig, smw, authService, callbackService)
