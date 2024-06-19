package io.karan.ictdb.http.middleware.smithy

import cats.effect.IO
import io.karan.ictdb.http.middleware.smithy
import org.http4s.HttpApp
import org.http4s.server.AuthMiddleware
import smithy4s.Hints
import smithy4s.http4s.ServerEndpointMiddleware

class Protected[T](authMiddleware: AuthMiddleware[IO, T])
    extends ServerEndpointMiddleware.Simple[IO]:
    private val middleware = authWrapper(authMiddleware)
    override def prepareWithHints(
        serviceHints: Hints,
        endpointHints: Hints
    ): HttpApp[IO] => HttpApp[IO] =
        serviceHints.get[_root_.smithy.api.Auth] match
        case Some(outerAuth) if outerAuth.value.isEmpty =>
            endpointHints.get[_root_.smithy.api.Auth] match
            case Some(innerAuth) if innerAuth.value.isEmpty => identity
            case Some(_)                                    => middleware
            case None                                       => identity
        case Some(outerAuth)                            =>
            endpointHints.get[_root_.smithy.api.Auth] match
            case Some(innerAuth) if innerAuth.value.isEmpty => identity
            case _                                          => middleware
        case None                                       => identity
