package io.karan.ictdb.http

import cats.effect.IO
import cats.syntax.all.*
import io.karan.ictdb.auth.{Crypto, GoogleAuthService}
import io.karan.ictdb.config.ServerConfig
import org.http4s.ember.server.EmberServerBuilder

object Server:
  def make(config: ServerConfig, crypto: Crypto, gs: GoogleAuthService) =
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(routes(crypto, gs).orNotFound)
      .build

  private def routes(crypto: Crypto, gs: GoogleAuthService) =
    val rootRoutes = RootController.routes(crypto)
    val authRoutes = AuthController.make(crypto, gs).routes
    rootRoutes <+> authRoutes
