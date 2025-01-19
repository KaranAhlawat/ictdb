package io.karan.ictdb.domain

case class User(
  id: Long,
  providerId: String,
  username: String,
  userEmail: String,
  userPassword: Option[String],
  provider: UserOrigin
)

enum UserOrigin:
  case Form
  case Google
