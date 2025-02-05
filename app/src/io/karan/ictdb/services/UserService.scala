package io.karan.ictdb.services

import cats.effect.IO
import io.karan.ictdb.domain.User
import io.karan.ictdb.persistence.UserRepo
import io.scalaland.chimney.dsl.*
import io.karan.ictdb.persistence.models.UserModel

trait UserService:
  def registerUser(user: User): IO[Unit]
  def loginUser(usernameOrEmail: String, password: String): IO[Either[String, User]]

class UserServiceLive private[services] (userRepo: UserRepo) extends UserService:
  override def registerUser(user: User) =
    userRepo.delay(userRepo.insert(user.transformInto[UserModel]))

  override def loginUser(usernameOrEmail: String, password: String): IO[Either[String, User]] =
    val user = userRepo.delay(userRepo.findByUsernameOrEmail(usernameOrEmail))
    user.map: userOpt =>
      userOpt.fold(Left("Invalid credentials provided"))(user =>
        if user.userPassword.contains(password) then Right(user.transformInto[User])
        else Left("Invalid credentials provided")
      )

object UserService:
  def live(userRepo: UserRepo) = UserServiceLive(userRepo)
