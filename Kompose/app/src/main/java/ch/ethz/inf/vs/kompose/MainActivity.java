package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_placeholder);
    }

    public void joinParty(View view) {
        Log.d(LOG_TAG, "Join party button pressed");
        Intent connectIntent = new Intent(this, ConnectActivity.class);
        startActivity(connectIntent);
    }

    public void createParty(View view) {
        Log.d(LOG_TAG, "Create party button pressed");
        Intent partyIntent = new Intent(this, PartyCreationActivity.class);
        startActivity(partyIntent);
    }

    public void viewHistoryFromTitle(View view) {
        Log.d(LOG_TAG, "History button pressed");
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }
}
