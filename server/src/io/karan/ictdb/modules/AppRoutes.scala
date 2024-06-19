package io.karan.ictdb.modules

import cats.effect.{IO, Resource}
import io.karan.ictdb.http.routes.{AuthRoutes, UserRoutes}
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger

case class AppRoutes(authRoutes: HttpRoutes[IO], userRoutes: HttpRoutes[IO])

object AppRoutes:
    def make(
        config: AppConfig,
        resources: AppResources,
        security: AppSecurity,
        components: AppSessionComponents,
        services: AppServices
    )(using Logger[IO]): Resource[IO, AppRoutes] =
        val authRoutes =
            AuthRoutes
                .make(
                    config.sessionConfig,
                    security.middlewares,
                    services.authService,
                    services.callbackService
                )
                .routes
        val userRoutes = UserRoutes.make(
            services.userService,
            security.middlewares.protectedMiddleware,
            resources.local,
            security.config,
            components.ctxBuilder
        )

        userRoutes.routes.map: ur =>
            AppRoutes(authRoutes, ur)
