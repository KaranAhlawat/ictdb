package io.karan.ictdb.dto

import io.circe.Decoder

case class RegisterUserDto(username: String, email: String, password: String) derives Decoder
