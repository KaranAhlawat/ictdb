package io.karan.ictdb.configuration

import cats.effect.IO
import cats.syntax.all.*
import ciris.*
import com.comcast.ip4s.*
import org.http4s.ResponseCookie
import org.pac4j.http4s.SessionConfig

import scala.concurrent.duration.*

case class ServerConfig(host: Host, port: Port, frontendPort: Port)

case class DBConfig(
    host: Host,
    username: String,
    password: Secret[String],
    dbName: String,
    maxConnections: Int
)

case class AppConfig(serverConfig: ServerConfig, dbConfig: DBConfig, sessionConfig: SessionConfig)

object AppConfig:
    def make: IO[AppConfig] =
        (loadServerConf, loadDbConf, loadCookieConfig).parMapN(AppConfig.apply).load

    private def loadServerConf =
        (
            env("HTTP_HOST").as[Host].default(ipv4"0.0.0.0"),
            env("HTTP_PORT").as[Port].default(port"8080"),
            env("HTTP_FRONTEND_PORT").as[Port].default(port"3000")
        ).parMapN(ServerConfig.apply)

    private def loadDbConf =
        (
            env("DB_HOST").as[Host],
            env("DB_USERNAME").as[String],
            env("DB_PASSWORD").as[String].secret,
            env("DB_DATABASE").as[String],
            env("DB_MAX_CONN").as[Int]
        ).parMapN(DBConfig.apply)

    private def loadCookieConfig =
        (
            env("SESSION_COOKIE_NAME").as[String],
            env("SESSION_COOKIE_SECRET").as[String].secret,
            env("SESSION_MAX_AGE").as[Int]
        ).parMapN((name, secret, duration) =>
            SessionConfig(
                cookieName = name,
                secret = secret.value.getBytes().toList,
                mkCookie = (name, content) =>
                    println(name)
                    ResponseCookie(name, content, path = Some("/"))
                ,
                maxAge = duration.minutes
            )
        )

    private given ConfigDecoder[String, Host] =
        ConfigDecoder[String, String].mapOption("Host")(Host.fromString)

    private given ConfigDecoder[String, Port] =
        ConfigDecoder[String, Int].mapOption("Port")(Port.fromInt)
end AppConfig
