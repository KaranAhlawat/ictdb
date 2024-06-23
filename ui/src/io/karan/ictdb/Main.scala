package io.karan.ictdb
import calico.*
import calico.html.io.{*, given}
import calico.syntax.*
import calico.unsafe.given
import cats.effect.{IO, Resource}
import fs2.dom.*

object Main extends IOWebApp:
    override def render: Resource[IO, HtmlElement[IO]] =
        div(s"The email is not or is it!")
