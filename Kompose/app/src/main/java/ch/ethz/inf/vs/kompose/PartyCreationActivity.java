package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import ch.ethz.inf.vs.kompose.repository.SessionRepository;
import ch.ethz.inf.vs.kompose.service.NetworkService;

public class PartyCreationActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Party Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_creation_placeholder);
    }

    public void confirmParty(View v) {
        //TODO: Add party creation process before starting next activity
        Log.d(LOG_TAG, "Confirmation button pressed");

/*
        // start a session as host
        EditText editText = (EditText) findViewById(R.id.party_name_text_entry);
        String partyName = editText.getText().toString();
        repository.startSession(partyName, partyName);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
*/
    }
}
