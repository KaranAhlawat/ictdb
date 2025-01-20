package io.karan.ictdb.http.dto

import io.circe.Codec

case class RegisterUserRequest(username: String, email: String, password: String) derives Codec
