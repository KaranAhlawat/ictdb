package io.karan.ictdb.http

import cats.data.OptionT
import cats.effect.IO
import cats.syntax.all.*
import io.karan.ictdb.auth.{Crypto, GoogleAuthService}
import io.karan.ictdb.config.ServerConfig
import io.karan.ictdb.services.UserService
import org.http4s.HttpRoutes
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.{ErrorAction, ErrorHandling}
import org.http4s.server.middleware.RequestLogger
import org.http4s.server.middleware.ResponseLogger

type Middleware = HttpRoutes[IO] => HttpRoutes[IO]

object Server:
  def make(config: ServerConfig, crypto: Crypto, gs: GoogleAuthService, userService: UserService) =
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(routes(crypto, gs, userService).orNotFound)
      .build

  private def routes(crypto: Crypto, gs: GoogleAuthService, userService: UserService) =
    val rootRoutes = RootController.routes(crypto)
    val authRoutes = AuthController.make(crypto, gs, userService).routes

    val app                        = rootRoutes <+> authRoutes
    val requestLogger: Middleware  = RequestLogger.httpRoutes(logBody = true, logHeaders = true)
    val responseLogger: Middleware = ResponseLogger.httpRoutes(logBody = false, logHeaders = true)

    val errorLogger: Middleware = inRoutes =>
      ErrorHandling.Recover.total(
        ErrorAction.log(inRoutes, messageFailureLogAction = errorHandler, serviceErrorLogAction = errorHandler)
      )

    val middleware = requestLogger.andThen(errorLogger).andThen(responseLogger)
    middleware(app)

  private def errorHandler(t: Throwable, msg: => String) =
    OptionT.liftF(IO.println(msg) >> IO.delay(t.printStackTrace()))
