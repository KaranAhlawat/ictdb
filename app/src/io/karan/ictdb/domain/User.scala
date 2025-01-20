package io.karan.ictdb.domain

import io.circe.Encoder

enum UserOrigin:
  case Form
  case Google

object UserOrigin:
  given Encoder[UserOrigin] = Encoder.encodeString.contramap(origin => origin.toString.toLowerCase)

case class User(
  id: Long,
  providerId: String,
  username: String,
  userEmail: String,
  userPassword: Option[String],
  provider: UserOrigin
) derives Encoder
