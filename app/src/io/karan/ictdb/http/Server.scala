package io.karan.ictdb.http

import cats.effect.IO
import io.karan.ictdb.config.ServerConfig
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router

object Server:
  private val routes = Router("/" -> RootController.routes)

  def make(config: ServerConfig) =
    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(routes.orNotFound)
      .build
