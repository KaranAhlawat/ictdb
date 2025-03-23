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
          link(rel     := "stylesheet", href        := "/style.css"),
          script(src   := "https://unpkg.com/htmx.org@2.0.4"),
          script(src   := "https://unpkg.com/hyperscript.org@0.9.14")
        ),
        body(boost(), content)
      )
    )

object Layout:
  def apply(loggedIn: Boolean)(content: Modifier*) =
    Root(appHeader(loggedIn), main(cls := "px-8", content), appFooter)

  private def appHeader(loggedIn: Boolean) =
    val loginButton  =
      div(
        cls := "flex-none flex gap-5 items-center",
        div(cls := "navbar-item", a(cls := "btn btn-primary", href := "/register", "Sign Up")),
        div(cls := "navbar-item", a(href := "/login", "Log In"))
      )
    val logoutButton =
      div(cls := "flex-none", div(cls := "navbar-item", a(cls := "btn btn-primary", href := "/logout", "Log Out")))
    header(
      cls := "px-6 pt-4",
      nav(
        cls := "navbar",
        ul(cls := "flex-1", li(get("/"), strong(cls := "font-bold text-2xl", "ICTdb"))),
        if loggedIn then logoutButton else loginButton
      )
    )

  private def appFooter =
    footer(
      cls    := "footer footer-center p-10 bg-base-300",
      position.fixed,
      bottom := 0,
      width  := "100%",
      div(cls := "content has-text-centered", p("Made with  Scala ➕ HTMX ➕ ❤\uFE0F"))
    )
end Layout
