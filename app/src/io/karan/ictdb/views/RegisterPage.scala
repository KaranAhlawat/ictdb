package io.karan.ictdb.views

import io.karan.ictdb.views.common.FormField
import scalatags.Text.all.*
import scalatags.Text.tags2.section

object RegisterPage:
  def apply() =
    section(
      cls := "section container",
      maxWidth := 500,
      form(
        action := "/register",
        method := "POST",
        FormField("username", "Username", "text"),
        FormField("email", "Email", "email"),
        FormField("password", "Password", "password"),
        div(cls := "field", div(cls := "control", button(cls := "button is-link is-fullwidth", `type` := "submit", "Submit")))
      )
    )
