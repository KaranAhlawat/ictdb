package io.karan.ictdb.persistence

import cats.effect.{IO, Resource}
import fs2.io.net.Network
import io.karan.ictdb.modules.DBConfig
import natchez.Trace.Implicits.given
import skunk.Session

object PostgresSession:
    def make(config: DBConfig): Resource[IO, Resource[IO, Session[IO]]] =
        Session.pooled[IO](
            host = config.host.toString,
            user = config.username,
            database = config.dbName,
            password = Some(config.password.value),
            max = config.maxConnections
        )
