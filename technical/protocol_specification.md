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

`type` is one of the following (or more): 

- REQUEST_INFORMATION,
- REGISTER_CLIENT,
- UNREGISTER_CLIENT,
- SESSION_UPDATE,
- REQUEST_SONG,
- VOTE_SKIP_SONG,
- KEEP_ALIVE,
- ERROR

`status` is one of the following (or more): 

- REQUESTED
- IN_QUEUE,
- EXCLUDED_BY_POPULAR_VOTE,
- DOWNLOAD_FAILED,
- OTHER_ERROR

`Message` object contains:

- `type`: message type (string)
- `error_message`: the error message that occurred if message type is ERROR
- `sender_uuid`: sender uuid (string)
- `sender_username`: sender username (string)
- `session`: the current session object which saves all clients and songs
    - `uuid`: the ID of the session (string)
    - `host_uuid`: UUID of the host (string)
    - `session_name`: Name of the session (string)
    - `clients`: list of `Client` objects (array)
    - `playlist`: list of `Song` objects (array)
- `song_details`: a `Song` (object)

`Song` object contains:

- `order`: sorted order of the item in the play queue (int)
- `uuid`: UUID of the item (string)
- `downvotes`: JSON Array with all UUIDs that downvoted the song (JSON object)
- `title`: Song title (string)
- `proposed_by`: UUID of the client that originally requested the item (string)
- `thumbnail_url`: URL to download the audio file from (string)
- `download_url`: URL to download the audio file from (string)
- `source_url`: Original link, i.e. the youtube.com URL (string)

`Client` object contains:

- `uuid`: UUID of the client (string)
- `name`: the name of the client (string)
- `is_active`: boolean indicating whether the client is active or not (string)

`Downvote` object contains:

- `client_uuid`: UUID of the client which cast the downvote (string)
- `cast_time`: ISO time when the downvote was cast (string)

Full JSON example:  

```
{
    "type": "MESSAGE_TYPE",
    "error_message": "Playing Rick Astley is not supported",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    "sender_username": "Mario Huana",
    "session": {
        "uuid": "24aa4a92-c9e2-11e7-86b4-f68673f17803",
        "host_uuid": "24aa4a92-c9e2-11e7-86b4-f68673f17803",
		"session_name": "great party",
        "clients": [ 
			{
				"uuid": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
				"name": "Jeb Bush",
				"is_active": true
			}
		],
        "playlist": [
            {
                "uuid": "019f9150-cac4-11e7-8bb4-8474e3e86a2e",
                "order": 1,
                "title": "Ghostbusters",
				"thumbnail_url": "https://www.youtube.com/thumbnail?v=m9We2XsVZfc",
                "download_url": "https://www.youtube.com/download?v=m9We2XsVZfc",
                "source_url": "https://www.youtube.com/watch?v=m9We2XsVZfc",
				"status": "SUBMITTED",
                "proposed_by": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
				"downvotes": [
					{
						"client_uuid": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
						"cast_time": "2005-08-15T15:52:01+0000"
					}
                ]
            }
        ]
    },
	"song_details": {
        "item_uuid": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
        "title": "Ghostbusters",
        "thumbnail_url": "https://www.youtube.com/thumbnail?v=m9We2XsVZfc",
        "download_url": "https://www.youtube.com/download?v=m9We2XsVZfc",
        "source_url": "https://www.youtube.com/watch?v=m9We2XsVZfc"
    }
}
```

Example for each request "type":

```
{
    "type": "REQUEST_INFORMATION",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
}
```

```
{
    "type": "REGISTER_CLIENT",
    "sender_username": "Mario Huana",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
}
```

```
{
    "type": "UNREGISTER_CLIENT",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
}
```

```
{
    "type": "SESSION_UPDATE",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    "session": {
        "uuid": "24aa4a92-c9e2-11e7-86b4-f68673f17803",
        "host_uuid": "24aa4a92-c9e2-11e7-86b4-f68673f17803",
		"session_name": "great party",
        "clients": [ 
			{
				"uuid": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
				"name": "Jeb Bush",
				"is_active": true
			}
		],
        "playlist": [
            {
                "uuid": "019f9150-cac4-11e7-8bb4-8474e3e86a2e",
                "order": 1,
                "title": "Ghostbusters",
				"thumbnail_url": "https://www.youtube.com/thumbnail?v=m9We2XsVZfc",
                "download_url": "https://www.youtube.com/download?v=m9We2XsVZfc",
                "source_url": "https://www.youtube.com/watch?v=m9We2XsVZfc",
				"status": "SUBMITTED",
                "proposed_by": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
				"downvotes": [
					{
						"uuid": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
						"cast_time": "2005-08-15T15:52:01+0000"
					},
					{
						"uuid": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
						"cast_time": "2005-08-15T15:52:01+0000"
					}
                ]
            },
            {
                "uuid": "019e9150-cac4-11e7-8bb4-8474e3e86a2e",
                "order": 2,
                "title": "Ghostbusters",
				"thumbnail_url": "https://www.youtube.com/thumbnail?v=m9We2XsVZfc",
                "download_url": "https://www.youtube.com/download?v=m9We2XsVZfc",
                "source_url": "https://www.youtube.com/watch?v=m9We2XsVZfc",
				"status": "SUBMITTED",
                "proposed_by": "019f9150-cac4-11e7-8bb4-8474e3e86aae",
				"downvotes": []
            }
        ]
    }
}
```

```
{
    "type": "REQUEST_SONG",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    "song_details": {
		"uuid": "019e9150-cac4-11e7-8bb4-8474e3e86a2e",
		"title": "Ghostbusters",
		"thumbnail_url": "https://www.youtube.com/thumbnail?v=m9We2XsVZfc",
		"download_url": "https://www.youtube.com/download?v=m9We2XsVZfc",
		"source_url": "https://www.youtube.com/watch?v=m9We2XsVZfc",
		"status": "REQUESTED"
    }
}
```

```
{
    "type": "CAST_SKIP_SONG_VOTE",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    "song_details": {
        "uuid": "0d56458e-cac4-11e7-9ffc-23156f95d8bf",
    }
}
```

```
{
    "type": "REMOVE_SKIP_SONG_VOTE",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    "song_details": {
        "uuid": "0d56458e-cac4-11e7-9ffc-23156f95d8bf",
    }
}
```

```
{
    "type": "KEEP_ALIVE",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33"
}
```

```
{
    "type": "ERROR",
    "sender_uuid": "c4d435c6-c92b-11e7-9e80-d1034c1b7b33",
    "error_message": "Playing Rick Astley is not supported",
}
```

