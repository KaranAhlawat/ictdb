package io.karan.ictdb.views

import io.karan.ictdb.views.htmx.Attributes.*
import scalatags.Text.all.{footer, content as contentAttr, *}
import scalatags.Text.tags2.{main, nav}

val htmxConfig = """
{
        "responseHandling":[
            {"code":"204", "swap": false},
            {"code":"[23]..", "swap": true},
            {"code":"422", "swap": true},
            {"code":"[45]..", "swap": false, "error":true},
            {"code":"...", "swap": true}
       ]
}
""".stripMargin

val rawContent = attr("content", raw = true)

object Root:
  def apply(content: Modifier*) =
    doctype("html")(
      html(
        head(
          meta(charset := "utf-8"),
          meta(name    := "viewport", contentAttr   := "width=device-width, initial-scale=1"),
          meta(name    := "htmx-config", rawContent := htmxConfig),
          title := "ICTdb",
          link(rel     := "stylesheet", href        := "https://cdn.jsdelivr.net/npm/bulma@1.0.2/css/bulma.min.css"),
          script(src   := "https://unpkg.com/htmx.org@2.0.4")
        ),
        body(boost(), content, script(defer, src := "/bulma.js"))
      )
    )

object Layout:
  def apply(loggedIn: Boolean)(content: Modifier*) =
    Root(appHeader(loggedIn), main(cls := "container", content), appFooter)

  private def appHeader(loggedIn: Boolean) =
    val loginButton  =
      div(
        cls := "navbar-end",
        div(cls := "navbar-item", a(cls := "button is-primary", href := "/register", "Sign Up")),
        div(cls := "navbar-item", a(href := "/login", "Log In"))
      )
    val logoutButton =
      div(cls := "navbar-end", div(cls := "navbar-item", a(cls := "button is-primary", href := "/logout", "Log Out")))
    header(
      cls := "px-6 pt-4",
      nav(
        cls := "navbar",
        ul(cls := "navbar-brand", li(get("/"), strong(cls := "has-text-weight-bold is-size-4", "ICTdb"))),
        if loggedIn then logoutButton else loginButton
      )
    )

  private def appFooter =
    footer(
      cls    := "footer",
      position.fixed,
      bottom := 0,
      width  := "100%",
      div(cls := "content has-text-centered", p("Made with  Scala ➕ HTMX ➕ ❤\uFE0F"))
    )
end Layout
