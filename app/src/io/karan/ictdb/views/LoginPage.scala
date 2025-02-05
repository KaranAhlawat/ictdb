package io.karan.ictdb.views

import io.karan.ictdb.views.common.{FormField, GoogleIcon}
import io.karan.ictdb.views.htmx.Attributes.boost
import scalatags.Text.all.*
import scalatags.Text.tags2.section

object LoginPage:
  def apply(missing: Option[List[String]] = None, err: Option[String] = None) =
    Layout(false) {
      section(
        maxWidth := 500,
        missing.fold(p())(missing =>
          div(
            cls := "alert alert-error alert-soft",
            strong("Missing fields: "),
            s"${missing.map(_.capitalize).mkString(", ")}"
          )
        ),
        err.fold(p())(err => div(cls := "alert alert-error alert-soft", strong(err))),
        div(
          cls := "card bg-base-100 w-full max-w-sm shrink-0 shadow-2xl",
          form(
            method := "POST",
            cls    := "card-body",
            fieldset(
              cls := "fieldset",
              FormField("username", "Username Or Email", "text"),
              FormField("password", "Password", "password"),
              button(cls := "btn btn-primary", `type` := "submit", "Submit")
            )
          )
        ),
        div(
          cls := "container is-flex is-justify-content-center is-flex-direction-column",
          p(cls := "has-text-centered has-text-weight-semibold pb-2", "Or login with"),
          a(cls := "mx-auto", href := "/login/google", boost(false), role := "button", GoogleIcon())
        )
      )
    }
end LoginPage
