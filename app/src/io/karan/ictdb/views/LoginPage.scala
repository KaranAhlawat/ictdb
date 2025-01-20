package io.karan.ictdb.views

import io.karan.ictdb.views.common.FormField
import io.karan.ictdb.views.htmx.Attributes.boost
import scalatags.Text.all.*
import scalatags.Text.tags2.section

object LoginPage:
  def apply() =
    section(
      cls := "section container",
      maxWidth := 500,
      form(
        action := "/login/form",
        method := "GET",
        FormField("username", "Username Or Email", "text"),
        FormField("password", "Password", "password"),
        div(cls := "field", div(cls := "control", button(cls := "button is-link is-fullwidth", `type` := "submit", "Submit")))
      ),
      div(
        cls    := "container is-flex is-justify-content-center is-flex-direction-column",
        p(cls := "has-text-centered has-text-weight-semibold pb-2", "Or login with"),
        a(cls := "button is-light", href := "/login/google", boost(false), role := "button", "Google")
      )
    )
