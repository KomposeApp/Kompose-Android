package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class PartyCreationActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Party Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_creation_placeholder);
    }

    public void confirmParty(View v){
        //TODO: Add party creation process before starting next activity
        Log.d(LOG_TAG, "Confirmation button pressed");
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }
}
