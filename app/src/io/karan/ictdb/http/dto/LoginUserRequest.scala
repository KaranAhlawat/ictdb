package io.karan.ictdb.http.dto

final case class LoginUserRequest(usernameOrEmail: String, password: String)
