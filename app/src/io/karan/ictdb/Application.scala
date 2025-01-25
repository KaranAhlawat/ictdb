package io.karan.ictdb

import cats.effect.{IO, IOApp}
import com.augustnagro.magnum.magcats.Transactor
import com.google.crypto.tink.*
import com.google.crypto.tink.aead.AeadConfig
import fs2.io.file.{Files, Path}
import fs2.text
import io.karan.ictdb.auth.{Crypto, GoogleAuthService}
import io.karan.ictdb.config.AppConfig
import io.karan.ictdb.http.Server
import io.karan.ictdb.http.auth.Http4sRequestSender
import io.karan.ictdb.persistence.{DataSource, UserRepo}
import io.karan.ictdb.services.UserService
import org.http4s.ember.client.EmberClientBuilder

import java.util.concurrent.Executors

object Application extends IOApp.Simple:
  val runF =
    for
      config               <- AppConfig.make.toResource
      ds                   <- DataSource.make(config.db)
      xa                   <- config.db.maxConn.fold(Transactor[IO](ds))(max => Transactor[IO](ds, max)).toResource
      userRepo              = UserRepo.live(xa)
      userService           = UserService.live(userRepo)
      client               <- EmberClientBuilder.default[IO].build
      sender               <- Http4sRequestSender.make(client)
      virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor
      gs                    = GoogleAuthService.make(config.google, sender, virtualThreadExecutor)
      aead                 <- setupAead(config.server.keysetPath).toResource
      crypto                = Crypto.make(aead)
      server               <- Server.make(config.server, crypto, gs, userService)
    yield server

  override def run = runF.useForever

  private def setupAead(path: Path): IO[Aead] = IO
    .delay(AeadConfig.register())
    .flatMap: _ =>
      Files[IO]
        .readAll(path)
        .through(text.utf8.decode)
        .compile
        .string
        .map: json =>
          TinkJsonProtoKeysetFormat
            .parseKeyset(json, InsecureSecretKeyAccess.get)
        .map(_.getPrimitive(RegistryConfiguration.get(), classOf[Aead]))
end Application
