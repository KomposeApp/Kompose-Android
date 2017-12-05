# The FINAL distributed project

## Deadlines

17.11.2017 Deadline project proposal  
15.12.2017 Deadline presentation slides, project logo  
17.12.2017 Deadline code  
18.12.2017 Presentations and demo session

## Team

Dino Bolliger (bdino)  
Florian Moser (moserfl)  
Lino Lendi (llendi)  11-714-383
Lukas Tobler (lutobler)  
Mark Arnold (arnomark)  15-917-701
Tobias Brodmann (brotobia) 15-934-565

## Meetings

- Monday 27.11.17, 11:00 (CHN green floor)

## Notes

- YouTube file formats: 
    - https://github.com/HaarigerHarald/android-youtubeExtractor/blob/master/youtubeExtractor/src/main/java/at/huber/youtubeExtractor/YouTubeExtractor.java
    - https://github.com/rg3/youtube-dl/blob/f2332f18e66fc5255d11a2762bfaff02f8221251/youtube_dl/extractor/youtube.py

## TODO

### Known bugs

- ~~Pressing 'ADD SONG' with an empty text field crashes the app~~
- 'ADD SONG' does not parse alternative URLs correctly, like m.youtube or youtu.be or even links originating from Youtube playlists. 
  Idea: Parse out the Video ID (?v=...) and handle everything else from there. (SongModels now support the ID as a field)
- Sometimes app crashes with `Illegal character in query` when calling `URI.create` when the URL from the YouTubeExtractor contains a `"` (double quote) (??) (YouTubeExtractor issue?)
- PlaylistActivity sometimes calls onCreate more than once, which leads to the following exception:
java.lang.NullPointerException: Attempt to invoke virtual method 'ch.ethz.inf.vs.kompose.model.list.ObservableUniqueSortedList ch.ethz.inf.vs.kompose.model.SessionModel.getPlayQueue()' on a null object reference
- Currently playing song can't be downvoted, skipped or removed
- PlaylistActivity rebinds the Audio Service every single time we bring it into focus
- Video Downloading is sometimes called more than 10 times in a row for whatever reason. When this happens, playback and caching fails completely.

