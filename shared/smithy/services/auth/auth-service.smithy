$version: "2"

namespace io.karan.ictdb.gen.services.auth

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
