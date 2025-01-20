package io.karan.ictdb.services

import cats.effect.IO
import com.augustnagro.magnum.magcats.Transactor
import io.karan.ictdb.domain.User
import io.karan.ictdb.persistence.{UserCreator, UserRepo}
import io.scalaland.chimney.dsl.*

trait UserService:
  def registerUser(user: User): IO[Unit]

class UserServiceLive private[services] (xa: Transactor[IO], userRepo: UserRepo) extends UserService:
  override def registerUser(user: User) =
    xa.connect:
      userRepo.insert(user.transformInto[UserCreator])

object UserService:
  def live(xa: Transactor[IO], userRepo: UserRepo) = UserServiceLive(xa, userRepo)
