package io.karan.ictdb.modules

import cats.effect.std.Dispatcher
import cats.effect.{IO, IOLocal, Resource}
import cats.syntax.all.*
import io.karan.ictdb.persistence.PostgresSession
import org.pac4j.core.profile.CommonProfile
import skunk.Session

case class AppResources(
    dispatcher: Dispatcher[IO],
    dbPool: Resource[IO, Session[IO]],
    local: IOLocal[List[CommonProfile]]
)

object AppResources:
    def make(config: AppConfig): Resource[IO, AppResources] =
        (
            Dispatcher.parallel[IO],
            PostgresSession.make(config.dbConfig),
            IOLocal(List.empty[CommonProfile]).toResource
        ).mapN(AppResources.apply)
