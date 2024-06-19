$version: "2.0"

namespace io.karan.ictdb.gen

@error("client")
@httpError(400)
structure CredentialsException {
    @required
    message: String = "Invalid credentials"
}

@error("client")
@httpError(401)
structure UnauthenticatedException {
    @required
    message: String = "Unauthenticated"
}

@error("client")
@httpError(403)
structure ForbiddenException {}

@error("server")
@httpError(500)
structure GeneralServerException {}
