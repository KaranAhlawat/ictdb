package io.karan.ictdb.domain

import alloy.common.EmailFormat
import smithy4s.{Refinement, RefinementProvider}

case class Email(value: String)

object Email:
    given RefinementProvider[EmailFormat, String, Email] =
        Refinement.drivenBy[EmailFormat](Email.apply, _.value)

    def apply(value: String): Either[String, Email] =
        if isValidEmail(value) then Right(new Email(value))
        else Left("Email is not valid")

    private def isValidEmail(value: String): Boolean = value.contains('@')
