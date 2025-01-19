package io.karan.ictdb.views

import io.karan.ictdb.views.htmx.Attributes.boost
import scalatags.Text.all.{footer, *}
import scalatags.Text.tags2.{main, nav}

object Layout:
  def apply(loggedIn: Boolean, content: Modifier*) =
    html(
      head(
        title := "ICTdb",
        script(src := "https://unpkg.com/htmx.org@2.0.4")
      ),
      body(boost(), appHeader(loggedIn), main(cls := "container", content), appFooter)
    )

  private def appHeader(loggedIn: Boolean) =
    val loginButton  =
      div(cls := "navbar-end", div(cls := "navbar-item", a(cls := "button is-primary", href := "/login", "Sign In")))
    val logoutButton =
      div(cls := "navbar-end", div(cls := "navbar-item", a(cls := "button is-primary", href := "/logout", "Log Out")))
    header(
      cls := "px-6 pt-4",
      nav(
        cls := "navbar",
        ul(cls := "navbar-brand", li(strong("ICTdb"))),
        if loggedIn then logoutButton else loginButton
      )
    )

  private def appFooter =
    footer(cls := "px-6", p("Made with ❤\uFE0F with Scala + HTMX"))
