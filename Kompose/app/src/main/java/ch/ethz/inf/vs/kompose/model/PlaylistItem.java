package ch.ethz.inf.vs.kompose.model;

import java.io.File;

import ch.ethz.inf.vs.kompose.data.SongDetails;

public class PlaylistItem {

    public boolean isCached = false;
    public int order;

    private SongDetails songDetails;
    private File storedFile;
}
