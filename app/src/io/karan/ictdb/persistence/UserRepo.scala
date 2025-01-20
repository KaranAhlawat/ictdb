package io.karan.ictdb.persistence

import com.augustnagro.magnum.magcats.*

case class UserCreator(
  providerId: String,
  username: String,
  userEmail: String,
  userPassword: Option[String],
  provider: DbUserOrigin
)

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("user_origin")
enum DbUserOrigin derives DbCodec:
  case Form
  case Google

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
@SqlName("users")
case class UserReader(
  @Id id: Long,
  providerId: String,
  username: String,
  userEmail: String,
  userPassword: Option[String],
  provider: DbUserOrigin
) derives DbCodec

trait UserRepo extends Repo[UserCreator, UserReader, Long]

class UserRepoLive extends UserRepo

object UserRepo:
  def live = UserRepoLive()
