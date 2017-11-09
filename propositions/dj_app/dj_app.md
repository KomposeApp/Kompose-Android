## Distributed DJ App / Distributed playlist

### Motivation

We want to create an application that allows many users to share a playlist
with their Android smartphone during a playback session. During such a session,
any user can request a song to be added with a Youtube link or a local media
file. A host device (connected to a audio system) will download/cache songs
in the queue and play them.

### Implementation

It will be session based. Sessions will be recorded, so that playlists
from past session can still be viewed later.

A *host device* will initiate a session. This device will act as the central
hub for communication. It will announce a network service on the local WiFi
network (NSD, [1]), which the clients then can automatically discover and
connect to.

Songs can be downloaded from Youtube by extracting the URL for the audio track.
There already exists a library for Android that does this [2].

### Feature list

- Communication over local WiFi
- Automatic service discovery, no manual IP address settings
- Ability to vote to skip a song, and if a majority is reached, the song will be skipped
- The host will download and cache songs in the play queue ahead of time
- User settings like a username
- Playlist items will be tagged with the user that added it
- A host can be migrated to a client, so a different device can be connected to the (audio system). Useful if the host runs out battery.


[1] https://developer.android.com/training/connect-devices-wirelessly/nsd.html
[2] https://github.com/HaarigerHarald/android-youtubeExtractor
