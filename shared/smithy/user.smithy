$version: "2.0"

namespace io.karan.ictdb.gen.domain.user

@length(min: 21, max: 21)
string UserID

@length(min: 3, max: 50)
string Username

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
