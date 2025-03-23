package io.karan.ictdb.views.common

import scalatags.Text.all.*

object FormField:
  def apply(field: String, labelText: String, typ: String) =
    Vector(
      label(`for` := field, cls := "fieldset-label", labelText),
      input(id    := field, cls := "input", `type` := typ, name := field)
    )
