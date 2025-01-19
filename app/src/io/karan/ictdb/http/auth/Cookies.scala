package io.karan.ictdb.http.auth

import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import io.karan.ictdb.domain.UserOrigin
import org.http4s.ResponseCookie
import org.http4s.SameSite.Lax

object Cookies:
  val AUTH                            = ".http4s.cookie"
  val LOGIN_TYPE                      = ".http4s.login"

  def createStateCookie(provider: UserOrigin, state: State) =
    ResponseCookie(
      name = STATE_FOR_(provider.toString.toLowerCase),
      content = state.toString,
      path = Some("/"),
      httpOnly = true,
      secure = true,
      maxAge = Some(60 * 10),
      sameSite = Some(Lax)
    )

  def STATE_FOR_(provider: String)    = s"${provider}_oauth_state"

  def createCodeCookie(provider: UserOrigin, verifier: CodeVerifier) =
    ResponseCookie(
      name = VERIFIER_FOR_(provider.toString.toLowerCase),
      content = verifier.getValue,
      path = Some("/"),
      httpOnly = true,
      secure = true,
      maxAge = Some(60 * 10),
      sameSite = Some(Lax)
    )

  def VERIFIER_FOR_(provider: String) = s"${provider}_code_verifier"

  def createAuthCookie(content: String, maxAge: Long) =
    ResponseCookie(
      name = Cookies.AUTH,
      content = content,
      domain = Some("localhost"),
      maxAge = Some(maxAge),
      secure = true,
      httpOnly = true,
      path = Some("/")
    )
    
  def createLoginCookie(content: String) =
    ResponseCookie(
      name = Cookies.LOGIN_TYPE,
      content = content,
      domain = Some("localhost"),
      maxAge = Some(31_536_000L), // 1 Year
      secure = true,
      httpOnly = true,
      path = Some("/")
    )
end Cookies
