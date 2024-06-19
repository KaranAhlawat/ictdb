package io.karan.ictdb.http.middleware

import cats.effect.IO
import org.http4s.server.AuthMiddleware
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http4s.{Http4sContextBuilder, SecurityFilterMiddleware}

class SecurityMiddlewares private (config: Config, builder: Http4sContextBuilder[IO]):
    def formClientMiddleware: AuthMiddleware[IO, List[CommonProfile]] =
        SecurityFilterMiddleware.securityFilter(config, builder, Some("FormClient"))

    def oidcClientMiddleware: AuthMiddleware[IO, List[CommonProfile]] =
        SecurityFilterMiddleware.securityFilter(config, builder, Some("OidcClient"))

    def protectedMiddleware: AuthMiddleware[IO, List[CommonProfile]] =
        SecurityFilterMiddleware.securityFilter(config, builder)

object SecurityMiddlewares:
    def make(config: Config, builder: Http4sContextBuilder[IO]): SecurityMiddlewares =
        SecurityMiddlewares(config, builder)
