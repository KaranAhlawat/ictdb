package io.karan.ictdb.http.middleware

import cats.data.OptionT
import cats.effect.IO
import org.http4s.server.middleware.*
import org.http4s.{HttpApp, HttpRoutes}

import scala.concurrent.duration.*

type Middleware = HttpRoutes[IO] => HttpRoutes[IO]
object Http4sMiddleware:
    def apply(httpRoutes: HttpRoutes[IO]): HttpApp[IO] =
        val logging: Middleware =
            Logger.httpRoutes[IO](logBody = true, logHeaders = false)

        val errorLogging: Middleware = inRoutes =>
            ErrorHandling.Recover.total(
                ErrorAction.log(
                    inRoutes,
                    messageFailureLogAction = errorHandler,
                    serviceErrorLogAction = errorHandler
                )
            )

        val autoSlash: Middleware = AutoSlash(_)
        val timeout: Middleware   = Timeout(60.seconds)

        val middleware = logging.andThen(errorLogging).andThen(autoSlash).andThen(timeout)

        middleware(httpRoutes).orNotFound

    private def errorHandler(t: Throwable, msg: => String) =
        OptionT.liftF(IO.println(msg) >> IO(t.printStackTrace()))
