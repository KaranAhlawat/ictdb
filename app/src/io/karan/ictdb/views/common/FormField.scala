package io.karan.ictdb.views.common

import scalatags.Text.all.*

object FormField:
  def apply(field: String, labelText: String, typ: String) =
    div(
      cls := "field",
      label(`for` := field, cls := "label", labelText),
      div(cls     := "control", input(id := field, cls := "input", `type` := typ, name := field))
    )
