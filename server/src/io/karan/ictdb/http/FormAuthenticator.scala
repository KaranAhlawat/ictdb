package io.karan.ictdb.http

import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO
import cats.effect.std.Dispatcher
import io.karan.ictdb.persistence.user.UserRepository
import org.pac4j.core.context.CallContext
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.credentials.{Credentials, UsernamePasswordCredentials}
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.{CommonProfile, UserProfile}
import org.pac4j.core.util.Pac4jConstants

import java.util.Optional

class FormAuthenticator(userRepo: UserRepository, dispatcher: Dispatcher[IO]) extends Authenticator:
    override def validate(ctx: CallContext, credentials: Credentials): Optional[Credentials] =
        if credentials == null then throw CredentialsException("No credentials")

        val creds    = credentials.asInstanceOf[UsernamePasswordCredentials]
        val username = creds.getUsername
        val password = creds.getPassword

        // Validate username and password
        val userIO = userRepo
            .findUserByUsernameOrEmail(username, username)
            .flatMap {
                case Some(user) =>
                    user.password match
                    case None                 => IO.raiseError(CredentialsException("Invalid credentials provided"))
                    case Some(hashedPassword) =>
                        val passwordMatched = BCrypt
                            .verifyer()
                            .verify(password.toCharArray, hashedPassword.value)
                            .verified
                        if passwordMatched
                        then IO.pure(user)
                        else IO.raiseError(CredentialsException("Invalid credentials provided"))
                case None       => IO.raiseError(CredentialsException("Invalid credentials provided"))
            }

        val user = dispatcher.unsafeRunSync(userIO)

        val profile: UserProfile = CommonProfile()
        profile.setId(user.id.value)
        profile.addAttribute(Pac4jConstants.USERNAME, user.username.value)
        user.email.fold(())(email => profile.addAttribute("email", email))
        creds.setUserProfile(profile)

        Optional.of(creds)
    end validate
end FormAuthenticator
