package io.karan.ictdb.http.routes

import cats.effect.{IO, IOLocal}
import io.karan.ictdb.gen
import io.karan.ictdb.gen.GeneralServerException
import io.karan.ictdb.http.middleware.smithy.{Protected, WithProfile}
import org.http4s.server.AuthMiddleware
import org.pac4j.core.config.Config
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.http4s.Http4sContextBuilder
import org.typelevel.log4cats.Logger
import smithy4s.http4s.SimpleRestJsonBuilder
import smithy4s.{Service, kinds}

private def makeSmithyRoutes[Alg[_[_, _, _, _, _]]: Service, T, P](
    impl: kinds.FunctorAlgebra[Alg, IO],
    optionalMiddleware: Option[AuthMiddleware[IO, T]],
    local: IOLocal[List[CommonProfile]],
    config: Config,
    ctxBuilder: Http4sContextBuilder[IO]
)(using Logger[IO]) =
    val routes = SimpleRestJsonBuilder
        .routes(impl)
        .flatMapErrors(e =>
            val transformed =
                e match
                case _: IllegalAccessException => GeneralServerException()
                case _: CredentialsException   => gen.CredentialsException()
                case _                         => e

            Logger[IO].warn(e)("Error at top-level").as(transformed)
        )

    optionalMiddleware
        .fold(routes.resource)(authMw =>
            routes
                .middleware(Protected(authMw))
                .resource
                .map(WithProfile(local, config, ctxBuilder))
        )
