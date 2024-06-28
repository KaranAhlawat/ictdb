package io.karan.ictdb.services.auth
import at.favre.lib.crypto.bcrypt.BCrypt
import cats.effect.IO
import io.karan.ictdb.gen.domain.user.*
import io.karan.ictdb.gen.services.auth.*
import io.karan.ictdb.persistence.user.UserRepository

class AuthServiceLive private (userRepo: UserRepository) extends AuthService[IO]:
    private val PLACEHOLDER_ID = List.fill(21)("0").mkString("")

    override def registerUser(
        username: Username,
        email: UserEmail,
        password: UserPassword
    ): IO[RegisterUserOutput] =
        val hashedPassword =
            BCrypt.withDefaults().hashToString(12, password.value.toCharArray);

        val user = User(
            id = UserID(PLACEHOLDER_ID),
            username = username,
            email = Some(email),
            password = Some(UserPassword(hashedPassword))
        )

        userRepo
            .insertUser(user)
            .flatMap {
                case Right(saved) =>
                    IO.pure(
                        RegisterUserOutput(
                            SerializableUser(
                                id = saved.id,
                                username = saved.username,
                                email = saved.email
                            )
                        )
                    )
                case Left(error)  =>
                    IO.raiseError(error)
            }
    end registerUser
end AuthServiceLive

object AuthServiceLive:
    def make(userRepo: UserRepository): AuthServiceLive =
        AuthServiceLive(userRepo)
