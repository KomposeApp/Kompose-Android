package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class HistoryOverviewActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## History Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_overview_placeholder);
    }

    public void examineHistoryItem() {
        //TODO: Select the correct item to examine out of the listview
        Log.d(LOG_TAG, "Item selected");
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
    }
}
