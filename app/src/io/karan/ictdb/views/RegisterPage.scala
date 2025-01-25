package io.karan.ictdb.views

import io.karan.ictdb.views.common.FormField
import scalatags.Text.all.*
import scalatags.Text.tags2.section

object RegisterPage:
  def apply(missing: Option[List[String]]) =
    Layout(false) {
      section(
        cls      := "section container",
        maxWidth := 500,
        missing.fold(p())(missing =>
          div(
            cls := "notification is-danger",
            button(cls := "delete"),
            strong("Missing fields: "),
            s"${missing.map(_.capitalize).mkString(", ")}"
          )
        ),
        form(
          method := "POST",
          FormField("username", "Username", "text"),
          FormField("email", "Email", "email"),
          FormField("password", "Password", "password"),
          div(
            cls := "field",
            div(cls := "control", button(cls := "button is-link is-fullwidth", `type` := "submit", "Submit"))
          )
        )
      )
    }
