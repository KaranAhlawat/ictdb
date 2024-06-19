import $ivy.`com.disneystreaming.smithy4s::smithy4s-mill-codegen-plugin::0.18.22`
import $ivy.`io.github.davidgregory084::mill-tpolecat::0.3.5`
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalalib._
import mill.scalalib.scalafmt._
import smithy4s.codegen.mill._

object Versions {
    val cats        = "3.5.3"
    val ciris       = "3.6.0"
    val http4s      = "0.23.27"
    val alloy       = "0.3.8"
    val skunk       = "0.6.4"
    val bcrypt      = "0.10.2"
    val jwt         = "10.0.1"
    val logback     = "1.5.6"
    val pac4j       = "6.0.3"
    val pac4jHttp4s = "5.0.0"
    val jsoniter = "0.1.2"
}

object shared extends ScalaModule with Smithy4sModule with ScalafmtModule {
    override def scalaVersion = "3.4.2"
    override def ivyDeps      = Agg(
        ivy"com.disneystreaming.smithy4s::smithy4s-core:${smithy4sVersion()}",
        ivy"com.disneystreaming.alloy:alloy-core:${Versions.alloy}"
    )
}

object server extends ScalaModule with TpolecatModule with ScalafmtModule {
    override def scalaVersion = "3.4.2"
    override def moduleDeps   = Seq(shared)
    override def ivyDeps      = Agg(
        ivy"org.typelevel::cats-effect:${Versions.cats}",
        ivy"is.cir::ciris:${Versions.ciris}",
        ivy"org.tpolecat::skunk-core:${Versions.skunk}",
        ivy"com.disneystreaming.smithy4s::smithy4s-http4s:${shared.smithy4sVersion()}",
        ivy"com.disneystreaming.smithy4s::smithy4s-json:${shared.smithy4sVersion()}",
        ivy"org.http4s::http4s-ember-server:${Versions.http4s}",
        ivy"com.github.cornerman::http4s-jsoniter:${Versions.jsoniter}",
        ivy"org.pac4j::http4s-pac4j:${Versions.pac4jHttp4s}",
        ivy"org.pac4j:pac4j-oidc:${Versions.pac4j}",
        ivy"org.pac4j:pac4j-http:${Versions.pac4j}",
        ivy"at.favre.lib:bcrypt:${Versions.bcrypt}",
        ivy"ch.qos.logback:logback-classic:${Versions.logback}"
    )
}
