package ch.ethz.inf.vs.kompose.service;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import ch.ethz.inf.vs.kompose.R;
import ch.ethz.inf.vs.kompose.converter.SongConverter;
import ch.ethz.inf.vs.kompose.data.json.Song;
import ch.ethz.inf.vs.kompose.model.ClientModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

/**
 * Created by git@famoser.ch on 20/11/2017.
 */

public class SongService extends BaseService {

    private static final String LOG_TAG = "## Song Service";

    public void onCreate() {

        Intent gattServiceIntent = new Intent(this, NetworkService.class);
        boolean isBound = bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        Log.d(LOG_TAG, "finished creation, bound service: " + isBound);
    }


    NetworkService networkService;

    //service connection
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            networkService = (NetworkService) ((BaseService.LocalBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            networkService = null;
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }

    //the receiver
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
        }
    };


    /**
     * sends the requested song to the server and puts it into the playlist
     *
     * @param song the new song which should be included in the playlist
     */
    public void requestNewSong(Song song) {
        //todo: connect with session service, send song
        SongConverter songConverter = new SongConverter(new ClientModel[0]);
        SongModel songModel = songConverter.convert(song);
        networkService.sendRequestSong(song);
    }

    /**
     * down votes this song, possibly removing it from the play queue
     *
     * @param songModel the song which is disliked
     */
    public void castSkipVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        Song song = songConverter.convert(songModel);
        networkService.sendCastSkipSongVote(song);
    }

    /**
     * Remove the downvote for a song
     *
     * @param songModel Song for which the downvote is revoked
     */
    public void removeSkipVote(SongModel songModel) {
        SongConverter songConverter = new SongConverter(songModel.getPartOfSession().getClients());
        Song song = songConverter.convert(songModel);
        networkService.sendRemoveSkipSongVote(song);
    }

}
