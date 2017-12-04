package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import ch.ethz.inf.vs.kompose.enums.SessionStatus;
import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.SongRequestListener;
import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.YoutubeDownloadUtility;

public class ShareActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## ShareActivity";
    //Time until process is killed
    private static final int TIME_UNTIL_DEATH = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "ShareActivity started, attempting to send given URL to host...");
        Intent intent = getIntent();

        // Check whether this Activity was correctly called through sharing a link.
        String action = intent.getAction();
        if (!Intent.ACTION_SEND.equals(action)){
            throw new IllegalStateException("ShareActivity was called from an illegal source");
        }

        // Check whether there is a currently active session. If not, immediately stop and display an error.
        // Additionally, kill the process if Kompose hasn't been started beforehand.
        SessionModel activeSession = StateSingleton.getInstance().getActiveSession();
        if (activeSession == null) {
            Toast.makeText(this, "Kompose is not connected to a host!", Toast.LENGTH_SHORT).show();
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
                    // Intentional data race here
                    // If Kompose has not been started properly, kill the process.
                    if(!StateSingleton.getInstance().isStartedFromMainActivity()){
                        Log.d(LOG_TAG, "Kompose has dekomposed. *badum tish*");
                        android.os.Process.killProcess(pid);
                    } else{
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

        //set session to active if host
        if (activeSession.getIsHost() && activeSession.getSessionStatus().equals(SessionStatus.WAITING)) {
            activeSession.setSessionStatus(SessionStatus.ACTIVE);
        }

        //TODO: Please check this for correctness, I copied it from PlaylistActivity.
        YoutubeDownloadUtility youtubeService = new YoutubeDownloadUtility(this);
        youtubeService.resolveSong(requestURL, activeSession,
                StateSingleton.getInstance().getActiveClient(), new SongRequestListener(this));

        //Make sure everything is cleaned up. Don't remove this or it will cause an exception.
        finish();
    }
}
