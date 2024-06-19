package io.karan.ictdb.services.auth
import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO
import io.karan.ictdb.domain.dtos.RegisterUserDto
import io.karan.ictdb.gen.domain.user.User
import io.karan.ictdb.persistence.user.UserRepository

class AuthServiceLive private (userRepo: UserRepository) extends AuthService:
    override def registerUser(registerInfo: RegisterUserDto): IO[User] =
        val hashedPassword =
            BCrypt.withDefaults().hashToString(12, registerInfo.password.toCharArray);
        userRepo.insertUser(registerInfo.username, Some(registerInfo.email), Some(hashedPassword))

object AuthServiceLive:
    def make(userRepo: UserRepository): AuthServiceLive =
        AuthServiceLive(userRepo)
