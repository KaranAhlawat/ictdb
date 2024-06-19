package io.karan.ictdb.http.routes

import cats.effect.{IO, IOLocal, Resource}
import io.karan.ictdb.gen.services.user.UserService
import org.http4s.HttpRoutes
import org.http4s.server.AuthMiddleware
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http4s.Http4sContextBuilder
import org.typelevel.log4cats.Logger

class UserRoutes[T] private (
    userService: UserService[IO],
    mw: AuthMiddleware[IO, T],
    local: IOLocal[List[CommonProfile]],
    config: Config,
    ctxBuilder: Http4sContextBuilder[IO]
)(using Logger[IO]):
    def routes: Resource[IO, HttpRoutes[IO]] =
        makeSmithyRoutes(userService, Some(mw), local, config, ctxBuilder)

object UserRoutes:
    def make[T](
        userService: UserService[IO],
        mw: AuthMiddleware[IO, T],
        local: IOLocal[List[CommonProfile]],
        config: Config,
        ctxBuilder: Http4sContextBuilder[IO]
    )(using Logger[IO]): UserRoutes[T] =
        UserRoutes(userService, mw, local, config, ctxBuilder)
