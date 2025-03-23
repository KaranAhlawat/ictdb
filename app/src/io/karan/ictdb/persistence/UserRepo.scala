package io.karan.ictdb.persistence

import com.augustnagro.magnum.magcats.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import cats.effect.IO
import io.karan.ictdb.persistence.models.{UserModel, UserCreator}

trait UserRepo extends Repo[UserCreator, UserModel, Long]:
  def delay[A](f: DbCon ?=> A): IO[A]
  def delayTx[A](f: DbTx ?=> A): IO[A]
  def findByUsernameOrEmail(usernameOrEmail: String)(using DbCon): Option[UserModel]

class UserRepoLive private[persistence] (xa: Transactor[IO]) extends UserRepo:
  override def delay[A](f: (DbCon) ?=> A): IO[A]  = xa.connect(f)
  override def delayTx[A](f: (DbTx) ?=> A): IO[A] = xa.transact(f)

  override def findByUsernameOrEmail(usernameOrEmail: String)(using DbCon): Option[UserModel] =
    sql"SELECT * FROM users WHERE (username = $usernameOrEmail OR user_email = $usernameOrEmail) AND provider = 'form' LIMIT 1"
      .query[UserModel]
      .run()
      .headOption

object UserRepo:
  def live(xa: Transactor[IO]) = UserRepoLive(xa)
