package io.karan.ictdb.http.middleware.smithy

import cats.data.OptionT
import cats.effect.IO
import io.karan.ictdb.gen.UnauthenticatedException
import org.http4s.server.AuthMiddleware
import org.http4s.{AuthedRoutes, HttpApp}

private def authWrapper[T](authMw: AuthMiddleware[IO, T]): HttpApp[IO] => HttpApp[IO] =
    inputApp =>
        val dummyAuthRoutes = AuthedRoutes[T, IO](authReq => OptionT.liftF(inputApp(authReq.req)))
        val httpRoutes      = authMw(dummyAuthRoutes)
        HttpApp { req =>
            val resp = httpRoutes(req)
            // TODO: When is this triggered?
            resp.getOrRaise(UnauthenticatedException(message = "Please sign up or sign in."))
        }
