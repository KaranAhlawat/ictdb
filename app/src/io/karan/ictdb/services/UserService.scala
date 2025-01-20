package io.karan.ictdb.services

import cats.effect.IO
import com.augustnagro.magnum.magcats.Transactor
import io.karan.ictdb.domain.User
import io.karan.ictdb.persistence.{UserCreator, UserRepo}
import io.scalaland.chimney.dsl.*

trait UserService:
  def registerUser(user: User): IO[Unit]
  def loginUser(usernameOrEmail: String, password: String): IO[Either[String, User]]

class UserServiceLive private[services] (xa: Transactor[IO], userRepo: UserRepo) extends UserService:
  override def registerUser(user: User) =
    xa.connect:
      userRepo.insert(user.transformInto[UserCreator])

  override def loginUser(usernameOrEmail: String, password: String): IO[Either[String, User]] =
    val user = xa.connect(userRepo.findByUsernameOrEmail(usernameOrEmail))
    user.map: userOpt =>
      userOpt.fold(Left("Invalid credentials provided"))(user =>
        if user.userPassword.contains(password) then Right(user.transformInto[User])
        else Left("Invalid credentials provided")
      )

object UserService:
  def live(xa: Transactor[IO], userRepo: UserRepo) = UserServiceLive(xa, userRepo)
