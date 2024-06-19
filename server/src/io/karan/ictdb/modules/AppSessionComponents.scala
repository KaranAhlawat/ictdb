package io.karan.ictdb.modules

import cats.effect.IO
import io.karan.ictdb.http.security.FormAuthenticator
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.http4s.*

case class AppSessionComponents(
    store: Http4sGenericSessionStore[IO],
    ctxBuilder: Http4sContextBuilder[IO],
    authenticator: Authenticator
)

object AppSessionComponents:
    def make(resources: AppResources, repositories: AppRepositories): AppSessionComponents =
        AppSessionComponents(
            Http4sGenericSessionStore(CacheSessionRepository[IO](), resources.dispatcher)(),
            Http4sWebContext.withDispatcherInstance(resources.dispatcher),
            FormAuthenticator(repositories.userRepo, resources.dispatcher)
        )
