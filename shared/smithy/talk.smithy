$version: "2.0"

namespace io.karan.ictdb.gen.domain.talk

use io.karan.ictdb.gen.domain.user#UserID

@length(min: 21, max: 21)
string TalkID

@length(min: 5, max: 150)
string TalkTitle

@length(min: 5)
string TalkLink

string TalkDescription

string TalkOrganizer

string Speaker

string Tag

list TagList {
    member: Tag
}

structure Talk {
    @required
    id: TalkID
    @required
    userId: UserID
    @required
    name: TalkTitle
    @required
    link: TalkLink
    @required
    tags: TagList
    description: TalkDescription
    speaker: Speaker
    organizer: TalkOrganizer
}

list TalkList {
    member: Talk
}

