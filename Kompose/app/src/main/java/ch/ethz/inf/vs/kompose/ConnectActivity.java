package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;


public class ConnectActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Connect Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_placeholder);
    }

    public void connect(View v) {
        //TODO: Add connection logic before starting the next activity
        Log.d(LOG_TAG, "Connect button pressed");
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }


}
