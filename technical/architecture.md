# Architecture

this file describes the overall set up of the application. 

## General architecture

## Description by folder
`/`: all activities sit in the root of the src folder
`converter`: enables the conversion between `data` and `model` objects and vice versa
`data`: all objects which are meant to be serialized to json. The json is then persisted on storage, sent over the network etc
`enums`: all enumerations (like `MessageType`, `SongStatus`, ...)
`model`: all objects which are displayed in the view, and are used by the algorithms
`patterns`: contains interfaces and implementations of patterns used throughout the application
`repository`: contains repository classes. They have `model` classes as in/output and perform specific actions on them
`service`: services are abstraction from the network, the storage, youtube, etc. They are consumed by the repositories.

## what can be done
[ ] implement all models (template see `SongModel`)
[ ] implement the services (template see `YoutubeResolveService`)
[ ] implement the repositories (template see `SongRepository.requestNewSong`)
[ ] connect the activities with the repositories (use data binding wherever possible)

## repository vs service
`repository`
 - Convert `data` to `model` using the `converter`s
 - Serialize / deserialize
 - Query services
 - never throw exceptions

`service`
 - abstract storage, network, youtube, permissions, settings, ...
 - work with raw strings or `data` objects
 - never consume repositories
 
 example usercase: "put song into queue"  
 - acitivity displays textbox to user
 - user clicks on OK, activity creates `YoutubeResolveService` and passes the link
 - activity waits for callback from `YoutubeResolveService`
 - activity processes callback by passing the received `SongModel` to the `SongRepository`
 - the `SongRepository` puts the `SongModel` into the corresponding list, causing it to be displayed to the user
 - the `SongRepository` sends the request over the network