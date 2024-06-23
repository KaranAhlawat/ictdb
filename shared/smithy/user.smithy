$version: "2.0"

namespace io.karan.ictdb.gen.domain.user

use alloy.common#emailFormat
use smithy4s.meta#refinement

apply emailFormat @refinement(
    targetType: "io.karan.ictdb.domain.Email"
)

@length(min: 21, max: 21)
string UserID

@length(min: 3, max: 50)
string Username

@emailFormat
string UserEmail

string UserPassword

@mixin
structure UserMixin {
    @required
    id: UserID
    @required
    username: Username
    email: UserEmail
}

structure User with [UserMixin] {
    password: UserPassword
}

structure SerializableUser with [UserMixin] {}
