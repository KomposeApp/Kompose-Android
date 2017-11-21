# Architecture

this file describes the overall set up of the application. 

## Description by folder
`/`: all activities sit in the root of the src folder  
`converter`: enables the conversion between `data` and `model` objects and vice versa  
`data`: all objects which are meant to be serialized to json. The json is then persisted on storage, sent over the network etc  
`enums`: all enumerations (like `MessageType`, `SongStatus`, ...)  
`model`: all objects which are displayed in the view, and are used by the algorithms  
`patterns`: contains interfaces and implementations of patterns used throughout the application   
`service`: services are abstraction from the network, the storage, youtube, etc. They interact with each other, and are used in the activities to send commands and get objects to display.

## what can be done
 - [x] implement all models (template see `SongModel`)
 - [ ] implement the services (template see `YoutubeResolveService`)
 - [ ] connect the activities with the respective services (and then use data binding wherever possible)
 
 example usercase: "put song into queue"  
 - acitivity displays textbox to user
 - user clicks on OK, activity creates `YoutubeResolveService` and passes the link
 - activity waits for callback from `YoutubeResolveService`
 - activity processes callback by passing the received `Song` to the `SongService.requestNewSong`
 - the `SongService` creates a `SongModel` and puts it into the corresponding list, causing it to be displayed to the user
 - the `SongService` makes sure the `Song` is broadcast to the network