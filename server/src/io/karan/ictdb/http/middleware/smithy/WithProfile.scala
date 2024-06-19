package io.karan.ictdb.http.middleware.smithy

import cats.data.OptionT
import cats.effect.{IO, IOLocal}
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.pac4j.core.config.Config
import org.pac4j.core.profile.{CommonProfile, ProfileManager}
import org.pac4j.http4s.Http4sContextBuilder

import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.ListHasAsScala

object WithProfile:
    def apply(
        local: IOLocal[List[CommonProfile]],
        config: Config,
        ctxBuilder: Http4sContextBuilder[IO]
    ): HttpRoutes[IO] => HttpRoutes[IO] =
        routes =>
            HttpRoutes[IO] { request =>
                val context = ctxBuilder(request)
                val manager =
                    ProfileManager(context, config.getSessionStoreFactory.newSessionStore(null))
                val profile = manager.getProfiles.asScala.map(_.asInstanceOf[CommonProfile]).toList
                OptionT.liftF(local.set(profile)) *> routes(request)
            }
