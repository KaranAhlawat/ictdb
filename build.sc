import $ivy.`com.disneystreaming.smithy4s::smithy4s-mill-codegen-plugin::0.18.22`
import $ivy.`io.github.davidgregory084::mill-tpolecat::0.3.5`
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalajslib._
import mill.scalajslib.api._
import mill.scalalib._
import mill.scalalib.scalafmt._
import smithy4s.codegen.mill._

object Versions {
    val cats        = "3.5.4"
    val ciris       = "3.6.0"
    val http4s      = "0.23.27"
    val alloy       = "0.3.8"
    val skunk       = "0.6.4"
    val bcrypt      = "0.10.2"
    val logback     = "1.5.6"
    val pac4j       = "6.0.3"
    val pac4jHttp4s = "5.0.0"
    val jsoniter    = "0.1.2"
    val calico      = "0.2.2"
    val munitCats   = "2.0.0"
}

trait AppScalaModule extends ScalaModule with TpolecatModule with ScalafmtModule {
    override def scalaVersion  = "3.4.2"
    override def scalacOptions = T {
        super.scalacOptions().filterNot(Set("-explain", "-explaintypes", "-explain-types"))
    }
}

trait AppScalaJSModule extends AppScalaModule with ScalaJSModule {
    override def scalaJSVersion = "1.16.0"
}

object shared extends Module {
    trait SharedModule extends AppScalaModule with PlatformScalaModule with Smithy4sModule {
        def smithySources    = T.source(millSourcePath / "smithy")
        override def sources = super.sources() :+ smithySources()
        override def ivyDeps =
            Agg(ivy"com.disneystreaming.alloy:alloy-core:${Versions.alloy}")
    }

    object jvm extends SharedModule                       {
        override def ivyDeps = super.ivyDeps() ++ Agg(
            ivy"com.disneystreaming.smithy4s::smithy4s-core:${smithy4sVersion()}"
        )
    }
    object js  extends SharedModule with AppScalaJSModule {
        override def ivyDeps = super.ivyDeps() ++ Agg(
            ivy"com.disneystreaming.smithy4s::smithy4s-core::${smithy4sVersion()}"
        )
    }
}

object server extends AppScalaModule {
    override def moduleDeps = Seq(shared.jvm)
    override def ivyDeps    = Agg(
        ivy"org.typelevel::cats-effect:${Versions.cats}",
        ivy"is.cir::ciris:${Versions.ciris}",
        ivy"org.tpolecat::skunk-core:${Versions.skunk}",
        ivy"com.disneystreaming.smithy4s::smithy4s-http4s:${shared.jvm.smithy4sVersion()}",
        ivy"com.disneystreaming.smithy4s::smithy4s-json:${shared.jvm.smithy4sVersion()}",
        ivy"org.http4s::http4s-ember-server:${Versions.http4s}",
        ivy"com.github.cornerman::http4s-jsoniter:${Versions.jsoniter}",
        ivy"org.pac4j::http4s-pac4j:${Versions.pac4jHttp4s}",
        ivy"org.pac4j:pac4j-oidc:${Versions.pac4j}",
        ivy"org.pac4j:pac4j-http:${Versions.pac4j}",
        ivy"at.favre.lib:bcrypt:${Versions.bcrypt}",
        ivy"ch.qos.logback:logback-classic:${Versions.logback}"
    )

    object test extends ScalaTests with TestModule.Munit {
        override def ivyDeps = Agg(ivy"org.typelevel::munit-cats-effect:${Versions.munitCats}")
    }
}

object ui extends AppScalaJSModule {
    override def moduleDeps       = Seq(shared.js)
    override def ivyDeps          = Agg(ivy"com.armanbilge::calico::${Versions.calico}")
    override def moduleSplitStyle = ModuleSplitStyle.SmallModulesFor(List("io.karan.ictdb"))
    override def moduleKind       = ModuleKind.ESModule

    object test extends ScalaJSTests with TestModule.Munit {
        override def ivyDeps = Agg(ivy"org.typelevel::munit-cats-effect::${Versions.munitCats}")
    }

    def publicDev = T {
        public(fastLinkJS)()
    }

    def publicProd = T {
        public(fullLinkJS)()
    }
}

def public(jsTask: Task[Report]): Task[Map[String, os.Path]] = T.task {
    Map("@public" -> jsTask().dest.path)
}
