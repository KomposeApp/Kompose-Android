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
- KEEP_ALIVE,
- ERROR

JSON fields:

- `type`: message type (string)
- `username`: sender username (string)
- `sender_uuid`: a standard uuid (string)
- `session`: table that can be deserialized to a session object (JSON object)
    - `session_name`: Name of the session
    - `host_username`: Name of the host
    - `host_uuid`: UUID of the host
    - `clients`: JSON Object that maps UUIDs to user names
    - `playlist`: JSON array with `PlaylistItem` objects
- `song_details`: serialized `PlaylistItem` object

`PlaylistItem` objects:

- `order`: sorted order of the item in the play queue
- `item_uuid`: UUID of the item
- `downvotes`: JSON Array with all UUIDs that downvoted the song
- `title`: Song title
- `proposed_by`: UUID of the client that originally requested the item
- `download_url`: URL to download the audio file from
- `source_url`: Original link, i.e. the youtube.com URL

Examples:

```
{
    type: "REQUEST_INFORMATION",
    sender_uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
}
```

```
{
    type: "REGISTER_CLIENT",
    username: "Mario Huana",
    sender_uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
}
```

```
{
    type: "UNREGISTER_CLIENT",
    sender_uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
}
```

```
{
    type: "SESSION_UPDATE",
    username: "Mario Huana",
    sender_uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    session: {
        session_name: "great party",
        host_username: "Big Shaq",
        host_uuid: "24aa4a92-c9e2-11e7-86b4-f68673f17803",
        clients: {
            "019f9150-cac4-11e7-8bb4-8474e3e86aae": "Jeb Bush",
            "0d56458e-cac4-11e7-9ffc-23156f95d8bf": "Ted Cruz"
        },
        playlist: [
            {
                order: 1,
                item_uuid: "...",
                downvotes: [
                    "019f9150-cac4-11e7-8bb4-8474e3e86aae",
                    "0d56458e-cac4-11e7-9ffc-23156f95d8bf"
                ],
                title: "Shooting Stars",
                download_url: "...",
                source_url: "https://www.youtube.com/watch?v=feA64wXhbjo"
            },
            {
                order: 2,
                item_uuid: "...",
                downvotes: [
                    "019f9150-cac4-11e7-8bb4-8474e3e86aae",
                    "0d56458e-cac4-11e7-9ffc-23156f95d8bf"
                ],
                title: "Ghostbusters",
                download_url: "...",
                source_url: "https://www.youtube.com/watch?v=m9We2XsVZfc"
            },
            ...
        ]
    },
}
```

```
{
    type: "REQUEST_SONG",
    username: "Mario Huana",
    sender_uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    song_details: {
        order: 0,
        item_uuid: "...",
        downvotes: [
            "019f9150-cac4-11e7-8bb4-8474e3e86aae",
            "0d56458e-cac4-11e7-9ffc-23156f95d8bf"
        ],
        title: "Ghostbusters",
        download_url: "...",
        source_url: "https://www.youtube.com/watch?v=m9We2XsVZfc"
    }
}
```

```
{
    type: "VOTE_SKIP_SONG",
    username: "Mario Huana",
    sender_uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    song_details: {
        item_uuid: "0d56458e-cac4-11e7-9ffc-23156f95d8bf",
    }
}
```

```
{
    type: "ERROR",
    username: "Mario Huana",
    sender_uuid: "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    error_message: "Playing Rick Astley is not supported",
}
```

