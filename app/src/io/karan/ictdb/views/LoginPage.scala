package io.karan.ictdb.views

import io.karan.ictdb.views.htmx.Attributes.boost
import scalatags.Text.all.*
import scalatags.Text.tags2.section

object LoginPage:
  def apply() =
    section(
      cls := "section",
      form(
        action := "/login/form",
        method := "GET",
        div(
          cls   := "field",
          label(`for` := "username", cls := "label", "Username or Email"),
          div(cls     := "control", input(id := "username", cls := "input", `type` := "text", name := "username"))
        ),
        div(
          cls   := "field",
          label(`for` := "password", cls := "label", "Password"),
          div(cls     := "control", input(id := "password", cls := "input", `type` := "password", name := "password"))
        ),
        div(cls := "field", div(cls := "control", button(cls := "button is-link", `type` := "submit", "Submit")))
      ),
      div(cls := "container", a(cls := "mx-auto button is-secondary", href := "/login/google", boost(false), role := "button", "Login with Google"))
    )
