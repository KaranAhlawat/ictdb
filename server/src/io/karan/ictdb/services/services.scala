package io.karan.ictdb.services

import cats.data.NonEmptyList
import cats.effect.{IO, IOLocal}
import org.pac4j.core.profile.CommonProfile

private def getProfiles(local: IOLocal[List[CommonProfile]]): IO[NonEmptyList[CommonProfile]] =
    local.get.flatMap {
        case head :: next => IO.pure(NonEmptyList.of(head, next*))
        case Nil          =>
            IO.raiseError(
                IllegalAccessException(
                    "Tried to access the value outside of the lifecycle of an authenticated request"
                )
            )
    }
