package io.karan.ictdb.views

import scalatags.Text.all.*
import scalatags.Text.tags2.nav

object Layout:
  def apply[A](content: Seq[Modifier]) =
    html(
      head(
        title := "ICTdb",
        link(rel := "stylesheet", href := "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css")
      ),
      body(header(nav(p("ICTdb"))), content, footer(p("Made with ❤\uFE0F with Scala + HTMX")))
    )
