package io.karan.ictdb.modules

import cats.effect.IO
import io.karan.ictdb.gen.services.user.UserService
import io.karan.ictdb.services.auth.{AuthService, AuthServiceLive}
import io.karan.ictdb.services.user.UserServiceLive
import org.pac4j.http4s.CallbackService

case class AppServices(
    userService: UserService[IO],
    authService: AuthService,
    callbackService: CallbackService[IO]
)

object AppServices:
    def make(
        resources: AppResources,
        repos: AppRepositories,
        components: AppSessionComponents,
        security: AppSecurity
    ): AppServices =
        AppServices(
            UserServiceLive.make(repos.userRepo, resources.local),
            AuthServiceLive.make(repos.userRepo),
            CallbackService[IO](
                security.config,
                components.ctxBuilder,
                defaultUrl = Some("/profiles")
            )
        )
