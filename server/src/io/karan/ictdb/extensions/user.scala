package io.karan.ictdb.extensions

import io.karan.ictdb.domain.Email
import io.karan.ictdb.gen.domain.user.*

extension (user: User.type)
    def fromTuple(t: (String, String, Option[String], Option[String])): User =
        User(
            id = UserID(t._1),
            username = Username(t._2),
            email = t._3.flatMap(Email.apply(_).toOption).map(UserEmail.apply),
            password = t._4.map(UserPassword.apply)
        )
