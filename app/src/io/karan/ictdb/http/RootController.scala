package io.karan.ictdb.http

import cats.effect.IO
import io.karan.ictdb.views.Layout
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.scalatags.*

object RootController:
  val routes = HttpRoutes.of[IO]:
    case GET -> Root => Ok(Layout(Seq.empty))
