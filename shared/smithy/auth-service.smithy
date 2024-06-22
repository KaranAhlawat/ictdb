$version: "2"

namespace io.karan.ictdb.gen.services.auth

use io.karan.ictdb.gen.domain.user#SerializableUser
use io.karan.ictdb.gen.domain.user#UserEmail
use io.karan.ictdb.gen.domain.user#UserPassword
use io.karan.ictdb.gen.domain.user#Username

@error("client")
@httpError(401)
structure UsernameTakenException {
    @required
    message: String = "Provided username is already taken"
}

@error("client")
@httpError(401)
structure EmailTakenException {
    @required
    message: String = "Provided email is already registered"
}

@error("client")
@httpError(401)
structure InvalidCredentialsException {
    @required
    message: String = "Invalid credentials provided"
}

service AuthService {
    operations: [RegisterUser]
}

operation RegisterUser {
    input: RegisterUserInput
    output: RegisterUserOutput
    errors: [UsernameTakenException, EmailTakenException]
}

@input
structure RegisterUserInput {
    @required
    username: Username
    @required
    email: UserEmail
    @required
    password: UserPassword
}

@output
structure RegisterUserOutput {
    @required
    user: SerializableUser
}
