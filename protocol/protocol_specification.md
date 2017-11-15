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
    - `body`: message body, content depends on `type`. (string)
    - `session`: table that can be deserialized to a session object (JSON table)

Examples:

```
{
    type: "REQUEST_INFORMATION",
    username: "Mario Huana",
    UUID: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: ""
    session: {}
}
```

```
{
    type: "REGISTER_CLIENT",
    username: "Mario Huana",
    UUID: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: ""
    session: {}
}
```

```
{
    type: "UNREGISTER_CLIENT",
    username: "Mario Huana",
    UUID: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: ""
    session: {}
}
```

```
{
    type: "SESSION_UPDATE",
    username: "Mario Huana",
    UUID: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: ""
    session: {
        ...
    }
}
```

```
{
    type: "REQUEST_SONG",
    username: "Mario Huana",
    UUID: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "https://www.youtube.com/watch?v=dQw4w9WgXcQ"
    session: {}
}
```

```
{
    type: "VOTE_SKIP_SONG",
    username: "Mario Huana",
    UUID: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "420"
    session: {}
}
```

```
{
    type: "ERROR",
    username: "Mario Huana",
    UUID: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    body: "Playing Rick Astley is not supported"
    session: {}
}
```

## Session table

`sesion` looks like this:

{
    session_name: "great party",
    host_username: "Big Shaq",
    host_UUID: "24aa4a92-c9e2-11e7-86b4-f68673f17803",
    playlist: [
        {
            id: 420,
            title: "Shooting Stars",
            num_downvotes: 0,
            youtube_url: "https://www.youtube.com/watch?v=feA64wXhbjo"
        },
        {
            id: 421,
            title: "Ghostbusters",
            num_downvotes: 12,
            youtube_url: "https://www.youtube.com/watch?v=m9We2XsVZfc"
        },
        ...
    ]
}


