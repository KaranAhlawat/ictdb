package io.karan.ictdb

import cats.effect.{IO, IOApp}
import com.augustnagro.magnum.magcats.Transactor
import io.karan.ictdb.config.AppConfig
import io.karan.ictdb.http.Server
import io.karan.ictdb.persistence.{DataSource, UserRepo}

object Application extends IOApp.Simple:
  val runF =
    for
      config  <- AppConfig.make.toResource
      ds      <- DataSource.make(config.db)
      xa      <- config.db.maxConn.fold(Transactor[IO](ds))(max => Transactor[IO](ds, max)).toResource
      userRepo = UserRepo.live
      server  <- Server.make(config.server)
    yield server

  override def run = runF.useForever
