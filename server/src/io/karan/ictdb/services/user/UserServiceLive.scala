package io.karan.ictdb.services.user

import cats.data.NonEmptyList
import cats.effect.{IO, IOLocal}
import cats.syntax.all.*
import io.karan.ictdb.gen.ForbiddenError
import io.karan.ictdb.gen.domain.talk.TalkID
import io.karan.ictdb.gen.domain.user.{SerializableUser, UserID}
import io.karan.ictdb.gen.services.user.*
import io.karan.ictdb.persistence.user.UserRepository
import io.karan.ictdb.services.getProfiles
import org.pac4j.core.profile.CommonProfile

class UserServiceLive private (userRepo: UserRepository, profile: IO[NonEmptyList[CommonProfile]])
    extends UserService[IO]:

    override def getDetails(userId: UserID): IO[GetDetailsOutput] =
        profile
            .map(_.head.getId === userId.value)
            .ifM(
                ifTrue = userRepo
                    .findUserById(userId.value)
                    .flatMap(IO.fromOption(_)(UserNotFoundError()))
                    .map(user =>
                        GetDetailsOutput(SerializableUser(user.id, user.username, user.email))
                    ),
                ifFalse = IO.raiseError(ForbiddenError())
            )

    override def getFavoriteTalks(userId: UserID): IO[GetFavoriteTalksOutput] =
        userRepo
            .findUserFavorites(userId.value)
            .map(GetFavoriteTalksOutput.apply)

    override def addTalkToFavorites(userId: UserID, talkId: TalkID): IO[Unit] =
        userRepo
            .addToUserFavorites(userId.value, talkId.value)

    override def removeTalkFromFavorites(userId: UserID, talkId: TalkID): IO[Unit] =
        userRepo
            .removeFromUserFavorites(userId.value, talkId.value)

object UserServiceLive:
    def make(userRepo: UserRepository, local: IOLocal[List[CommonProfile]]): UserServiceLive =
        UserServiceLive(userRepo, getProfiles(local))
