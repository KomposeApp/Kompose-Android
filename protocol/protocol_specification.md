# Kompose protocol specification

This specifies the JSON protocol used for the `Kompose` application.

## Features

The following operations are supported:

- Request session information
- Registering a client
- Unregistering a client
- Playlist state update
- Request song
- Downvote a song
- Error message

## Implementation

`Message.java` will be the Java implementation of such a message. It stores
all necessary information and fields for this protocol. `Session.java` holds
information about the current session, such as its name, the play queue, etc.

## JSON

`type` is one of following: 

- REQUEST_INFORMATION,
- REGISTER_CLIENT,
- UNREGISTER_CLIENT,
- SESSION_UPDATE,
- REQUEST_SONG,
- VOTE_SKIP_SONG,
- ERROR

An message for registering a client looks like this:

JSON fields:
    - `type`: message type (string)
    - `username`: sender username (string)
    - `uuid`: a standard uuid (string)
    - `body`: message body, content depends on `type`. (string)
    - `session`: table that can be deserialized to a session object (JSON object)
    - `song_details`: song request details, such as title and download url

`session.playlist` items and `song_details` are both serialized `PlaylistItem`
objects.

Examples:

```
{
    type: "REQUEST_INFORMATION",
    username: "Mario Huana",
    uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "",
    session: {},
    song_details: {}
}
```

```
{
    type: "REGISTER_CLIENT",
    username: "Mario Huana",
    uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "",
    session: {},
    song_details: {}
}
```

```
{
    type: "UNREGISTER_CLIENT",
    username: "Mario Huana",
    uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "",
    session: {},
    song_details: {}
}
```

```
{
    type: "SESSION_UPDATE",
    username: "Mario Huana",
    uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: ""
    session: {
        session_name: "great party",
        host_username: "Big Shaq",
        host_uuid: "24aa4a92-c9e2-11e7-86b4-f68673f17803",
        playlist: [
            {
                id: 420,
                num_downvotes: 0,
                title: "Shooting Stars",
                download_url: "...",
                youtube_url: "https://www.youtube.com/watch?v=feA64wXhbjo"
            },
            {
                id: 421,
                num_downvotes: 12,
                title: "Ghostbusters",
                download_url: "...",
                youtube_url: "https://www.youtube.com/watch?v=m9We2XsVZfc"
            },
            ...
        ]
    },
    song_details: {}
}
```

```
{
    type: "REQUEST_SONG",
    username: "Mario Huana",
    uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
    session: {},
    song_details: {
        id: -1,
        num_downvotes: -1,
        title: "Ghostbusters",
        download_url: "...",
        youtube_url: "https://www.youtube.com/watch?v=m9We2XsVZfc"
    }
}
```

```
{
    type: "VOTE_SKIP_SONG",
    username: "Mario Huana",
    uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "420",
    session: {},
    song_details: {}
}
```

```
{
    type: "ERROR",
    username: "Mario Huana",
    uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "Playing Rick Astley is not supported",
    session: {},
    song_details: {}
}
```

