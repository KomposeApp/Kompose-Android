package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.net.URI;
import java.util.UUID;

import ch.ethz.inf.vs.kompose.base.BaseActivity;
import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.model.SongModel;
import ch.ethz.inf.vs.kompose.service.SongRequestListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;
import ch.ethz.inf.vs.kompose.service.handler.SongResolveHandler;

public class ShareActivity extends BaseActivity {

    private final String LOG_TAG = "## ShareActivity";
    private final int TIME_UNTIL_DEATH = 3000; // Time until process is killed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "ShareActivity started, attempting to send given URL to host...");
        Intent intent = getIntent();

        // Check whether this Activity was correctly called through sharing a link.
        String action = intent.getAction();
        if (!Intent.ACTION_SEND.equals(action)) {
            throw new IllegalStateException("ShareActivity was called from an illegal source");
        }

        // Check whether there is a currently active session. If not, stop and display an error.
        // Additionally, kill the process if Kompose hasn't been started beforehand.
        SessionModel activeSession = StateSingleton.getInstance().getActiveSession();
        if (activeSession == null || !StateSingleton.getInstance().getPlaylistIsActive()) {
            showError("Kompose is not connected to a host!");
            Log.d(LOG_TAG, "Failed to send given URL. Reason: Not connected to host.");

            //Prepare thread to clean up the process in case it is necessary.
            final int pid = android.os.Process.myPid();
            Runnable selfDestructionProcess = new Runnable() {
                @Override
                public void run() {
                    try {
                        // Wait some time to display the error toast to the user
                        Thread.sleep(TIME_UNTIL_DEATH);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // If Kompose has not been started properly, kill the process.
                    if (!StateSingleton.getInstance().isStartedFromMainActivity()) {
                        Log.d(LOG_TAG, "Kompose has dekomposed. *badum tish*");
                        android.os.Process.killProcess(pid);
                    } else {
                        Log.d(LOG_TAG, "Kompose gets to live another day.");
                    }
                }
            };

            // Start cleanup thread.
            new Thread(selfDestructionProcess).start();
            finish();
            return;
        }

        // Retrieve shared text
        String requestURL = (String) intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
        Log.d(LOG_TAG, "requesting URL: " + requestURL);

        if (!SongResolveHandler.resolveAndRequestSong(this, requestURL)){
            showError("Invalid URL");
        }

        //Make sure everything is cleaned up. Don't remove this or it will cause an exception.
        finish();
    }
}
