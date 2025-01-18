package io.karan.ictdb.config

import cats.effect.IO
import cats.syntax.all.*
import ciris.*
import com.comcast.ip4s.*

case class GoogleConfig(clientId: String, clientSecret: Secret[String])
case class ServerConfig(host: Host, port: Port)
case class DbConfig(host: Host, username: String, password: Secret[String], database: String, maxConn: Option[Int])
case class AppConfig(server: ServerConfig, db: DbConfig, google: GoogleConfig)

object AppConfig:
  def make: IO[AppConfig] =
    (loadServer, loadDb, loadGoogle).parMapN(AppConfig.apply).load

  private def loadServer =
    (env("HTTP_HOST").as[Host].default(host"localhost"), env("HTTP_PORT").as[Port].default(port"8080"))
      .parMapN(ServerConfig.apply)

  private def loadDb =
    (
      env("DB_HOST").as[Host],
      env("DB_USERNAME").as[String],
      env("DB_PASSWORD").as[String].secret,
      env("DB_DATABASE").as[String],
      env("DB_MAX_CONN").as[Int].option
    ).parMapN(DbConfig.apply)

  private def loadGoogle =
    (env("GOOGLE_CLIENT_ID").as[String], env("GOOGLE_CLIENT_SECRET").as[String].secret).parMapN(GoogleConfig.apply)

  private given ConfigDecoder[String, Host] =
    ConfigDecoder[String, String].mapOption("Host")(Hostname.fromString)

  private given ConfigDecoder[String, Port] =
    ConfigDecoder[String, Int].mapOption("Port")(Port.fromInt)
