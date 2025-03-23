package io.karan.ictdb.services

import cats.effect.IO
import com.password4j.Password
import io.karan.ictdb.domain.User
import io.karan.ictdb.persistence.UserRepo
import io.scalaland.chimney.dsl.*
import io.karan.ictdb.persistence.models.UserModel
import io.karan.ictdb.persistence.models.UserCreator

trait UserService:
  def registerUser(user: User): IO[Unit]
  def loginUser(usernameOrEmail: String, password: String): IO[Either[String, User]]

class UserServiceLive private[services] (userRepo: UserRepo) extends UserService:
  override def registerUser(user: User) =
    val hashedPw    = user.userPassword.map(pw => Password.hash(pw).addRandomSalt(12).withScrypt.getResult)
    val updatedUser = user.copy(userPassword = hashedPw)
    userRepo.delay(userRepo.insert(updatedUser.transformInto[UserCreator]))

  override def loginUser(usernameOrEmail: String, password: String): IO[Either[String, User]] =
    val user = userRepo.delay(userRepo.findByUsernameOrEmail(usernameOrEmail))
    user.map: userOpt =>
      userOpt.fold(Left("Invalid credentials provided"))(user =>
        val passwordMatches = user.userPassword.map(hash => Password.check(password, hash).withScrypt).contains(true)

        if passwordMatches then Right(user.transformInto[User])
        else Left("Invalid credentials provided")
      )

object UserService:
  def live(userRepo: UserRepo) = UserServiceLive(userRepo)
