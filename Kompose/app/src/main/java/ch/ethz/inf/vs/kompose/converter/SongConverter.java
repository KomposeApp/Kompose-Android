package ch.ethz.inf.vs.kompose.converter;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.data.Song;
import ch.ethz.inf.vs.kompose.model.SongModel;

public class SongConverter {

    public static SongModel convert(Song song) {
        SongModel songModel = new SongModel(UUID.fromString(song.getUuid()));
        //todo parse properties
        return songModel;
    }

    public static Song convert(SongModel songModel) {
        Song song = new Song();
        //todo parse properties
        return song;
    }
}
