package io.karan.ictdb.auth

import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID

import java.net.URI

trait AuthProvider(callback: String, id: String, secret: String):
  val codeEndpoint: URI
  val tokenEndpoint: URI
  val userInfoEndpoint: URI
  val callbackUri: URI     = URI(callback)
  val clientId: ClientID   = ClientID(id)
  val clientSecret: Secret = Secret(secret)

object AuthProvider:
  case class GoogleOIDC(callback: String, id: String, secret: String) extends AuthProvider(callback, id, secret):
    override val codeEndpoint: URI     = URI("https://accounts.google.com/o/oauth2/v2/auth")
    override val tokenEndpoint: URI    = URI("https://oauth2.googleapis.com/token")
    override val userInfoEndpoint: URI = URI("https://openidconnect.googleapis.com/v1/userinfo")
