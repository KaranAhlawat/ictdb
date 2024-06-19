package io.karan.ictdb

import cats.effect.{IO, IOApp}
import cats.syntax.all.*
import io.karan.ictdb.configuration.AppConfig
import io.karan.ictdb.http.middleware.Http4sMiddleware
import io.karan.ictdb.modules.*
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger as AppLogger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import java.time.Clock

object Main extends IOApp.Simple:

    given AppLogger[IO] = Slf4jLogger.getLogger[IO]

    given Clock = Clock.systemUTC

    override def run: IO[Unit] =
        val server =
            for
                conf                              <- AppConfig.make.toResource
                resources                         <- AppResources.make(conf)
                repos                              = AppRepositories.make(resources)
                sessionComponents                  = AppSessionComponents.make(resources, repos)
                security                           = AppSecurity.make(conf, sessionComponents)
                services                           = AppServices.make(resources, repos, sessionComponents, security)
                AppRoutes(authRoutes, userRoutes) <-
                    AppRoutes.make(conf, resources, security, sessionComponents, services)

                app = Http4sMiddleware(userRoutes <+> authRoutes)

                server <- EmberServerBuilder
                              .default[IO]
                              .withHost(conf.serverConfig.host)
                              .withPort(conf.serverConfig.port)
                              .withHttpApp(app)
                              .build
            yield server

        server.useForever
