package io.karan.ictdb.http

import cats.effect.IO
import cats.effect.std.Dispatcher
import io.karan.ictdb.configuration.ServerConfig
import io.karan.ictdb.persistence.user.UserRepository
import io.karan.ictdb.services.auth.AuthService
import org.http4s.Uri
import org.pac4j.core.authorization.authorizer.{Authorizer, DefaultAuthorizers}
import org.pac4j.core.client.Clients
import org.pac4j.core.config.{Config, ConfigFactory}
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.context.{CallContext, WebContext}
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.{Credentials, UsernamePasswordCredentials}
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.matching.matcher.{DefaultMatchers, Matcher}
import org.pac4j.core.profile.UserProfile
import org.pac4j.http.client.indirect.FormClient
import org.pac4j.http4s.{DefaultHttpActionAdapter, Http4sGenericSessionStore}

import java.util
import java.util.Optional

class SecurityConfig private (
    serverConf: ServerConfig,
    store: Http4sGenericSessionStore[IO],
    authenticator: Authenticator
) extends ConfigFactory:
    override def build(parameters: Any*): Config =
        val callbackUri =
            Uri.unsafeFromString(s"http://${serverConf.host}:${serverConf.port}/callback")
        val clients     = Clients(callbackUri.renderString, formClient)
        val config      = Config(clients)

        config.setHttpActionAdapter(DefaultHttpActionAdapter[IO]())
        config.setSessionStoreFactory(_ => store)
        // Disable CSRF generation and checks for now
        config.addMatcher(DefaultMatchers.CSRF_TOKEN, (ctx: CallContext) => true)
        config.addAuthorizer(
            DefaultAuthorizers.CSRF_CHECK,
            (context: WebContext, sessionStore: SessionStore, profiles: util.List[UserProfile]) =>
                true
        )
        config

    private def formClient: FormClient =
        FormClient(s"http://${serverConf.host}:${serverConf.port}/loginForm", authenticator)

object SecurityConfig:
    def make(
        serverConf: ServerConfig,
        store: Http4sGenericSessionStore[IO],
        authenticator: Authenticator
    ): SecurityConfig =
        SecurityConfig(serverConf, store, authenticator)
