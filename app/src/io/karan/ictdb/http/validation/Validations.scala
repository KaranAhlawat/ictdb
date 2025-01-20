package io.karan.ictdb.http.validation

import cats.data.Validated
import cats.data.ValidatedNec
import cats.syntax.all.*
import io.karan.ictdb.http.dto.RegisterUserRequest
import org.http4s.UrlForm
import io.karan.ictdb.http.validation.ValidationError.*
import io.karan.ictdb.http.dto.LoginUserRequest

object Validations:
  def parseRegisterUser(form: UrlForm): ValidatedNec[ValidationError, RegisterUserRequest] =
    val username = form.getFirst("username").flatMap(s => if s.isBlank() then none else s.some)
    val email    = form.getFirst("email").flatMap(s => if s.isBlank() then none else s.some)
    val password = form.getFirst("password").flatMap(s => if s.isBlank() then none else s.some)

    (
      Validated.fromOption(username, MissingFields("username")).toValidatedNec,
      Validated.fromOption(email, MissingFields("email")).toValidatedNec,
      Validated.fromOption(password, MissingFields("password")).toValidatedNec
    ).mapN: (u, e, p) =>
      RegisterUserRequest(u, e, p)

  def parseLoginUser(form: UrlForm): ValidatedNec[ValidationError, LoginUserRequest] =
    val username = form.getFirst("username").flatMap(s => if s.isBlank() then none else s.some)
    val password = form.getFirst("password").flatMap(s => if s.isBlank() then none else s.some)

    (
      Validated.fromOption(username, MissingFields("username")).toValidatedNec,
      Validated.fromOption(password, MissingFields("password")).toValidatedNec
    ).mapN: (u, p) =>
      LoginUserRequest(u, p)
