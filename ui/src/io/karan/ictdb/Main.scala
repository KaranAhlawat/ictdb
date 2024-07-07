package io.karan.ictdb

import calico.*
import calico.html.io.{*, given}
import calico.syntax.*
import calico.unsafe.given
import cats.effect.IO
import cats.effect.Resource
import fs2.dom.*

object Main extends IOWebApp:
    override def render: Resource[IO, HtmlElement[IO]] =
        div(
            cls := "py-4 max-w-4xl mx-auto",
            p(cls := "text-white text-2xl", "The email is not here")
        )
