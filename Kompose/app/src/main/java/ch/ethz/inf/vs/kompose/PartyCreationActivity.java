package ch.ethz.inf.vs.kompose;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import ch.ethz.inf.vs.kompose.service.SessionService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;

public class PartyCreationActivity extends BaseServiceActivity {

    private static final String LOG_TAG = "## Party Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_creation_placeholder);

        bindBaseService(SessionService.class);
    }

    public void confirmParty(View v) {
        //TODO: Add party creation process before starting next activity
        Log.d(LOG_TAG, "Confirmation button pressed");


        EditText editText = findViewById(R.id.party_name_text_entry);
        String partyName = editText.getText().toString();
        getSessionService().startSession(partyName, partyName);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }
}
