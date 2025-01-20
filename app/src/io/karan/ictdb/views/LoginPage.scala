package io.karan.ictdb.views

import io.karan.ictdb.views.common.{FormField, GoogleIcon}
import io.karan.ictdb.views.htmx.Attributes.boost
import scalatags.Text.all.*
import scalatags.Text.tags2.section

object LoginPage:
  def apply(missing: Option[List[String]] = None, err: Option[String] = None) =
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
      err.fold(p())(err =>
        div(
          cls := "notification is-danger",
          button(cls := "delete"),
          strong(err)
        )
      ),
      form(
        method := "POST",
        FormField("username", "Username Or Email", "text"),
        FormField("password", "Password", "password"),
        div(
          cls := "field",
          div(cls := "control", button(cls := "button is-link is-fullwidth", `type` := "submit", "Submit"))
        )
      ),
      div(
        cls    := "container is-flex is-justify-content-center is-flex-direction-column",
        p(cls := "has-text-centered has-text-weight-semibold pb-2", "Or login with"),
        a(cls := "mx-auto", href := "/login/google", boost(false), role := "button", GoogleIcon())
      )
    )
