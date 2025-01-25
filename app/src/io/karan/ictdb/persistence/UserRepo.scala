package io.karan.ictdb.persistence

import com.augustnagro.magnum.magcats.*
import java.sql.PreparedStatement
import java.sql.ResultSet
import cats.effect.IO

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("user_origin")
enum UserOriginType:
  case Form
  case Google

object DbUserOrigin:
  given DbCodec[UserOriginType] =
    val derived = DbCodec.derived[UserOriginType]
    new DbCodec[UserOriginType]:

      override def queryRepr: String = "?::user_origin"

      override def cols: IArray[Int] = derived.cols

      override def readSingle(resultSet: ResultSet, pos: Int): UserOriginType = derived.readSingle(resultSet, pos)

      override def readSingleOption(resultSet: ResultSet, pos: Int): Option[UserOriginType] =
        derived.readSingleOption(resultSet, pos)

      override def writeSingle(entity: UserOriginType, ps: PreparedStatement, pos: Int): Unit =
        derived.writeSingle(entity, ps, pos)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("users")
case class UserModel(
  @Id id: Long,
  providerId: String,
  username: String,
  userEmail: String,
  userPassword: Option[String],
  provider: UserOriginType
) derives DbCodec
trait UserRepo extends Repo[UserModel, UserModel, Long]:
  def delay[A](f: DbCon ?=> A): IO[A]
  def delayTx[A](f: DbTx ?=> A): IO[A]
  def findByUsernameOrEmail(usernameOrEmail: String)(using DbCon): Option[UserModel]

class UserRepoLive private[persistence] (xa: Transactor[IO]) extends UserRepo:

  override def delay[A](f: (DbCon) ?=> A): IO[A] = xa.connect(f)
  override def delayTx[A](f: (DbTx) ?=> A): IO[A] = xa.transact(f)

  override def findByUsernameOrEmail(usernameOrEmail: String)(using DbCon): Option[UserModel] =
    sql"SELECT * FROM users WHERE (username = $usernameOrEmail OR user_email = $usernameOrEmail) AND provider = 'form' LIMIT 1"
      .query[UserModel]
      .run()
      .headOption

object UserRepo:
  def live(xa: Transactor[IO]) = UserRepoLive(xa)
