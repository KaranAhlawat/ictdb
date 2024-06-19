package io.karan.ictdb.extensions

import io.karan.ictdb.gen.domain.user.*

import java.util.UUID

extension (user: User.type)
    def fromTuple(t: (String, String, Option[String], Option[String])): User =
        new User(
            id = UserID(t._1),
            username = Username(t._2),
            email = t._3.map(UserEmail.apply),
            password = t._4.map(UserPassword.apply)
        )
