package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import ch.ethz.inf.vs.kompose.service.SessionService;

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

        EditText editText = findViewById(R.id.entry_creation);
        String partyName = editText.getText().toString();
        getSessionService().startSession(partyName, partyName);

        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }
}
