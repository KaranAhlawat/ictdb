package io.karan.ictdb.persistence.user

import cats.effect.*
import cats.syntax.all.*
import io.karan.ictdb.gen.domain.talk.Talk
import io.karan.ictdb.gen.domain.user.*
import io.karan.ictdb.gen.services.auth.*
import skunk.*
import skunk.codec.all.*
import skunk.exception.PostgresErrorException
import skunk.implicits.*
import io.karan.ictdb.domain.Email

class UserRepositoryLive private (pool: Resource[IO, Session[IO]]) extends UserRepository:
    override def findUserById(id: String): IO[Option[User]] =
        val query =
            sql"""
                 | SELECT users.id, users.username, users.user_email, users.user_password
                 | FROM users
                 | WHERE id = ${bpchar(21)}
                 |""".stripMargin
                .query(UserRepositoryLive.userCodec)

        pool.use: s =>
            s.prepare(query)
                .flatMap: ps =>
                    ps.option(id)

    override def findUserByUsernameOrEmail(username: String, email: String): IO[Option[User]] =
        val query =
            sql"""
                 | SELECT id, username, user_email, user_password
                 | FROM users
                 | WHERE (username = ${varchar(50)}
                 | OR user_email = $text)
                 | AND user_password IS NOT NULL
               """.stripMargin
                .query(UserRepositoryLive.userCodec)

        pool.use: s =>
            s.prepare(query)
                .flatMap: ps =>
                    ps.option((username, email))

    override def findUserFavorites(id: String): IO[List[Talk]] = ???

    override def addToUserFavorites(userID: String, talkID: String): IO[Unit] = ???

    override def removeFromUserFavorites(userID: String, talkID: String): IO[Unit] = ???

    override def insertUser(
        user: User
    ): IO[Either[UsernameTakenException | EmailTakenException, User]] =
        val query =
            sql"""
                 | INSERT INTO users (username, user_email, user_password)
                 | VALUES (${varchar(50)}, ${text.opt}, ${text.opt})
                 | RETURNING id, username, user_email, user_password
               """.stripMargin
                .query(UserRepositoryLive.userCodec)

        pool.use: s =>
            s.prepare(query)
                .flatMap: ps =>
                    ps.unique(
                        (
                            user.username.value,
                            user.email.map(_.value.email),
                            user.password.map(_.value)
                        )
                    )
        .attempt
            .map(_.leftMap {
                case e: PostgresErrorException if e.constraintName.exists(_.contains("username")) =>
                    UsernameTakenException()
                case e: PostgresErrorException
                    if e.constraintName.exists(_.contains("user_email")) =>
                    EmailTakenException()
            })
end UserRepositoryLive

object UserRepositoryLive:
    private val userCodec: Codec[User] = (bpchar(21) *: varchar(50) *: text.opt *: text.opt).imap {
        (id, name, email, password) =>
            User(
                id = UserID(id),
                username = Username(name),
                email = email.flatMap(m => Email(m).toOption).map(UserEmail.apply),
                password = password.map(UserPassword.apply)
            )
    } { case User(id, name, email, password) =>
        (id.value, name.value, email.map(_.value.email), password.map(_.value))
    }

    def make(pool: Resource[IO, Session[IO]]): UserRepositoryLive =
        UserRepositoryLive(pool)
