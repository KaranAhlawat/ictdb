package io.karan.ictdb
import calico.*
import calico.html.io.{*, given}
import calico.syntax.*
import calico.unsafe.given
import cats.effect.IO
import cats.effect.Resource
import fs2.dom.*
import fs2.dom.Window
import io.karan.ictdb.gen.domain.user.UserEmail

import scala.scalajs.js.annotation.JSExportTopLevel

object Main extends IOWebApp:
    override def render: Resource[IO, HtmlElement[IO]] =
        val a = UserEmail("a@h.com")
        div(s"The email is ${a}")

    @JSExportTopLevel("main")
    override def main(args: Array[String]): Unit =
        val rootElement = window.document.getElementById(rootElementId).map(_.get)
        rootElement.flatMap(render.renderInto(_).useForever).unsafeRunAndForget()
