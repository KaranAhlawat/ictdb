package io.karan.ictdb.persistence

import cats.effect
import cats.effect.IO
import cats.effect.kernel.Resource
import com.comcast.ip4s.Host
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.karan.ictdb.config.DbConfig

import javax.sql.DataSource

object DataSource:
  def make(config: DbConfig): Resource[IO, DataSource] =
    Resource.fromAutoCloseable:
      IO.pure:
        val hikariConf = new HikariConfig()
        hikariConf.setJdbcUrl(mkUrl(config.host, config.database))
        hikariConf.setUsername(config.username)
        hikariConf.setPassword(config.password.value)
        hikariConf.setDriverClassName("org.postgresql.Driver")
        if config.maxConn.isDefined then hikariConf.setMaximumPoolSize(config.maxConn.get)
        hikariConf
      .map: hikariConf =>
          new HikariDataSource(hikariConf)

  private def mkUrl(host: Host, db: String) = s"jdbc:postgresql://$host/$db"
