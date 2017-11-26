package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.util.UUID;

import ch.ethz.inf.vs.kompose.model.SessionModel;
import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

public class PartyCreationActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Party Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_creation_placeholder);
    }

    public void confirmParty(View v) {
        Log.d(LOG_TAG, "Confirmation button pressed");

        EditText editText = findViewById(R.id.entry_creation);
        String partyName = editText.getText().toString();

        // create a new session
        SessionModel newSession = new SessionModel(UUID.randomUUID(),
                StateSingleton.getInstance().deviceUUID);
        newSession.setSessionName(partyName);
        StateSingleton.getInstance().activeSession = newSession;

        // start the server service
        Intent serverIntent = new Intent(this, AndroidServerService.class);
        startService(serverIntent);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }

    /**
     * creates a new session and register the host service on the network
     */
//    public SessionModel startSession(String sessionName, String clientName) {
//        isHost = true;
//
//        activeSessionModel = new SessionModel(UUID.randomUUID(), getDeviceUUID());
//        activeSessionModel.setSessionName(sessionName);
//        joinActiveSession(clientName);
//
//        Intent serverIntent = new Intent(this, AndroidServerService.class);
//        startService(serverIntent);
//
//        return activeSessionModel;
//    }
}
