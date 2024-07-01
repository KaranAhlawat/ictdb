package io.karan.ictdb.persistence.user

import cats.effect.IO
import io.karan.ictdb.gen.domain.talk.Talk
import io.karan.ictdb.gen.domain.user.User
import io.karan.ictdb.gen.services.auth.EmailTakenException
import io.karan.ictdb.gen.services.auth.UsernameTakenException

trait UserRepository:
    def findUserById(id: String): IO[Option[User]]
    def findUserByUsernameOrEmail(username: String, email: String): IO[Option[User]]
    def findUserFavorites(id: String): IO[List[Talk]]
    def insertUser(user: User): IO[Either[UsernameTakenException | EmailTakenException, User]]
    def addToUserFavorites(userID: String, talkID: String): IO[Unit]
    def removeFromUserFavorites(userID: String, talkID: String): IO[Unit]
