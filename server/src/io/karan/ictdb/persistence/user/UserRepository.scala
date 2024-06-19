package io.karan.ictdb.persistence.user

import cats.effect.IO
import io.karan.ictdb.gen.domain.talk.Talk
import io.karan.ictdb.gen.domain.user.User

trait UserRepository:
    def findUserById(id: String): IO[Option[User]]
    def findUserByUsernameOrEmail(username: String, email: String): IO[Option[User]]
    def findUserFavorites(id: String): IO[List[Talk]]
    def insertUser(username: String, email: Option[String], password: Option[String]): IO[User]
    def addToUserFavorites(userID: String, talkID: String): IO[Unit]
    def removeFromUserFavorites(userID: String, talkID: String): IO[Unit]
