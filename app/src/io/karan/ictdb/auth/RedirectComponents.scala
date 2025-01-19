package io.karan.ictdb.auth

import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier
import org.http4s.Uri

case class RedirectComponents(state: State, verifier: CodeVerifier, uri: Uri)
