package io.karan.ictdb.modules

import io.karan.ictdb.http.middleware.SecurityMiddlewares
import io.karan.ictdb.http.security.SecurityConfig
import org.pac4j.core.config.Config

case class AppSecurity(
    securityConfig: SecurityConfig,
    config: Config,
    middlewares: SecurityMiddlewares
)

object AppSecurity:
    def make(conf: AppConfig, components: AppSessionComponents): AppSecurity =
        val securityConfig =
            SecurityConfig.make(conf.serverConfig, components.store, components.authenticator)
        val config         = securityConfig.build()
        AppSecurity(securityConfig, config, SecurityMiddlewares.make(config, components.ctxBuilder))
