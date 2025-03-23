package io.karan.ictdb.auth

import cats.effect.IO
import cats.effect.kernel.Clock
import cats.syntax.all.*
import com.nimbusds.jwt.JWT
import com.nimbusds.oauth2.sdk.*
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic
import com.nimbusds.oauth2.sdk.http.HTTPRequestSender
import com.nimbusds.oauth2.sdk.id.*
import com.nimbusds.oauth2.sdk.pkce.{CodeChallengeMethod, CodeVerifier}
import com.nimbusds.oauth2.sdk.token.AccessToken
import com.nimbusds.openid.connect.sdk.*
import com.nimbusds.openid.connect.sdk.claims.UserInfo
import com.nimbusds.openid.connect.sdk.token.OIDCTokens
import io.karan.ictdb.config.GoogleConfig
import org.http4s.Uri

import java.net.URI
import java.time.temporal.ChronoUnit
import java.util.concurrent.Executor

trait GoogleAuthService:
  def getRedirectionComponents: IO[Option[RedirectComponents]]
  def parseCodeResponse(uri: Uri, state: State): IO[(AuthorizationCode, State)]
  def exchangeCodeForTokens(code: AuthorizationCode, state: State, verifier: CodeVerifier): IO[OIDCTokens]
  def getUserInfo(token: AccessToken): IO[UserInfo]
  def calculateSecondsToExpiration(idToken: JWT): IO[Long]

object GoogleAuthService:
  def make(config: GoogleConfig, sender: HTTPRequestSender, es: Executor): GoogleAuthService =
    new GoogleAuthService:
      private val provider =
        AuthProvider.GoogleOIDC("http://localhost:8080/callback", config.clientId, config.clientSecret.value)
      private val scope    = Scope("openid", "email", "profile")

      override def getRedirectionComponents: IO[Option[RedirectComponents]] =
        val nonce    = Nonce()
        val state    = State()
        val verifier = CodeVerifier()

        val req = new AuthenticationRequest.Builder(ResponseType.CODE, scope, provider.clientId, provider.callbackUri)
          .endpointURI(provider.codeEndpoint)
          .state(state)
          .nonce(nonce)
          .codeChallenge(verifier, CodeChallengeMethod.S256)
          .build()

        IO.pure:
          Uri
            .fromString(req.toURI.toString)
            .toOption
            .map(uri => RedirectComponents(state, verifier, uri))

      override def parseCodeResponse(uri: Uri, state: State): IO[(AuthorizationCode, State)] =
        val response = AuthenticationResponseParser.parse(URI(uri.toString))

        IO.raiseWhen(!state.equals(response.getState))(Exception("Invalid state for OIDC Code response"))
          .flatMap: _ =>
            IO.raiseWhen(!response.indicatesSuccess)(Exception("Unsuccessful OIDC code request"))
          .map: _ =>
            (response.toSuccessResponse.getAuthorizationCode, response.getState)

      override def exchangeCodeForTokens(
        code: AuthorizationCode,
        state: State,
        verifier: CodeVerifier
      ): IO[OIDCTokens] =
        val codeGrant  =
          AuthorizationCodeGrant(code, provider.callbackUri, verifier)
        val clientAuth =
          ClientSecretBasic(provider.clientId, provider.clientSecret)

        val request =
          TokenRequest(provider.tokenEndpoint, clientAuth, codeGrant, scope)

        IO.blocking(request.toHTTPRequest.send(sender))
          .evalOnExecutor(es)
          .map(OIDCTokenResponseParser.parse)
          .flatMap(tokenResp =>
            IO.raiseWhen(!tokenResp.indicatesSuccess)(Exception("Unsuccessful OIDC token request"))
              .as(
                tokenResp.toSuccessResponse
                  .asInstanceOf[OIDCTokenResponse]
                  .getOIDCTokens
              )
          )

      override def getUserInfo(token: AccessToken): IO[UserInfo] =
        val request = UserInfoRequest(provider.userInfoEndpoint, token)
        IO.blocking(request.toHTTPRequest.send(sender))
          .evalOnExecutor(es)
          .map(UserInfoResponse.parse)
          .flatMap(userInfoResp =>
            IO.raiseWhen(!userInfoResp.indicatesSuccess)(Exception("Unsuccessful OIDC UserInfo request"))
              .as(userInfoResp.toSuccessResponse.getUserInfo)
          )

      override def calculateSecondsToExpiration(idToken: JWT): IO[Long] =
        Clock[IO].realTimeInstant.map: current =>
          ChronoUnit.SECONDS
            .between(current, idToken.getJWTClaimsSet.getExpirationTime.toInstant)
end GoogleAuthService
