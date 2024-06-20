$version: "2"

namespace io.karan.ictdb.gen.services.auth

use io.karan.ictdb.gen.domain.user#SerializableUser
use io.karan.ictdb.gen.domain.user#UserEmail
use io.karan.ictdb.gen.domain.user#UserPassword
use io.karan.ictdb.gen.domain.user#Username

@error("client")
@httpError(401)
structure UsernameTakenError {
    @required
    message: String = "Provided username is already taken"
}

@error("client")
@httpError(401)
structure EmailTakenError {
    @required
    message: String = "Provided email is already registered"
}

@error("client")
@httpError(401)
structure InvalidCredentialsError {
    @required
    message: String = "Invalid credentials provided"
}

@authDefinition
@trait
structure sessionAuth {}

service AuthService {
    operations: [
        RegisterUser
    ]
}

operation RegisterUser {
    input: RegisterUserInput
    output: RegisterUserOutput
    errors: [UsernameTakenError, EmailTakenError]
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
