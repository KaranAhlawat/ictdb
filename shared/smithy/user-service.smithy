$version: "2"

namespace io.karan.ictdb.gen.services.user

use alloy#simpleRestJson
use io.karan.ictdb.gen#ForbiddenException
use io.karan.ictdb.gen#UnauthenticatedException
use io.karan.ictdb.gen.domain.talk#TalkID
use io.karan.ictdb.gen.domain.talk#TalkList
use io.karan.ictdb.gen.domain.user#SerializableUser
use io.karan.ictdb.gen.domain.user#UserID

@error("client")
@httpError(400)
structure UserNotFoundException {
    @required
    message: String = "User not found"
}

@authDefinition
@trait
structure sessionAuth {}

@simpleRestJson
@sessionAuth
@auth([sessionAuth])
service UserService {
    operations: [
        GetDetails,
        GetFavoriteTalks,
        AddTalkToFavorites,
        RemoveTalkFromFavorites
    ]
}

@http(method: "GET", uri: "/users/{userId}", code: 200)
@readonly
operation GetDetails {
    input: GetDetailsInput
    output: GetDetailsOutput
    errors: [UnauthenticatedException, ForbiddenException, UserNotFoundException]
}

@input
structure GetDetailsInput {
    @httpLabel
    @required
    userId: UserID
}

@output
structure GetDetailsOutput {
    @required
    user: SerializableUser
}

@http(method: "GET", uri: "/users/{userId}/favorites", code: 200)
@readonly
operation GetFavoriteTalks {
    input: GetFavoriteTalksInput
    output: GetFavoriteTalksOutput
    errors: [UnauthenticatedException, ForbiddenException, UserNotFoundException]
}

@input
structure GetFavoriteTalksInput {
    @httpLabel
    @required
    userId: UserID
}

@output
structure GetFavoriteTalksOutput {
    @required
    talks: TalkList
}

@http(method: "POST", uri: "/users/{userId}/favorites/{talkId}", code: 201)
operation AddTalkToFavorites {
    input: AddTalkToFavoritesInput
    errors: [UnauthenticatedException, ForbiddenException, UserNotFoundException]
}

@input
structure AddTalkToFavoritesInput {
    @httpLabel
    @required
    userId: UserID
    @httpLabel
    @required
    talkId: TalkID
}

@http(method: "DELETE", uri: "/users/{userId}/favorites/{talkId}", code: 200)
@idempotent
operation RemoveTalkFromFavorites {
    input: RemoveTalkFromFavoritesInput
    errors: [UnauthenticatedException, ForbiddenException, UserNotFoundException]
}

@input
structure RemoveTalkFromFavoritesInput {
    @httpLabel
    @required
    userId: UserID
    @httpLabel
    @required
    talkId: TalkID
}
