package io.karan.ictdb.persistence

import com.augustnagro.magnum.magcats.*

case class UserCreator(
  providerId: String,
  username: String,
  userEmail: String,
  userPassword: Option[String],
  provider: DbUserOrigin
)

@SqlName("user_origin")
@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
enum DbUserOrigin derives DbCodec:
  case Form
  case Github

@SqlName("users")
@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
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
