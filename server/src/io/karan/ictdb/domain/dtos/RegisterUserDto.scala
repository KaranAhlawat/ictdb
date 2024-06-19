package io.karan.ictdb.domain.dtos

import io.circe.Decoder

case class RegisterUserDto(username: String, email: String, password: String) derives Decoder
