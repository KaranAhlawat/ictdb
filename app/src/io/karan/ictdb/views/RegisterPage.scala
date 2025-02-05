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
            cls := "alert alert-error alert-soft",
            strong("Missing fields: "),
            s"${missing.map(_.capitalize).mkString(", ")}"
          )
        ),
        form(
          method := "POST",
          fieldset(
            cls := "fieldset",
            FormField("username", "Username", "text"),
            FormField("email", "Email", "email"),
            FormField("password", "Password", "password"),
            button(cls := "btn btn-primary", `type` := "submit", "Submit")
          )
        )
      )
    }
