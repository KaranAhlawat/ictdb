package io.karan.ictdb.services.auth

import cats.effect.IO
import io.karan.ictdb.domain.dtos.RegisterUserDto
import io.karan.ictdb.gen.domain.user.User
import org.pac4j.core.credentials.authenticator.Authenticator

trait AuthService:
    def registerUser(registerInfo: RegisterUserDto): IO[User]
