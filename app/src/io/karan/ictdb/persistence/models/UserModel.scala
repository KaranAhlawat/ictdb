package io.karan.ictdb.persistence.models

import com.augustnagro.magnum.magcats.*
import com.augustnagro.magnum.pg.enums.PgEnumDbCodec
import java.sql.PreparedStatement
import java.sql.ResultSet

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

case class UserCreator(
  providerId: String,
  username: String,
  userEmail: String,
  userPassword: Option[String],
  provider: UserOriginType
) derives DbCodec
