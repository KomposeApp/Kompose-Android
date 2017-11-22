package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class MainActivity extends BaseServiceActivity {

    private static final String LOG_TAG = "## Main Activity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main_placeholder);
    }

    /**
     * Navigation to the ConnectActivity, where clients join existing parties
     */
    public void callConnectActivity(View view) {
        Log.d(LOG_TAG, "Join party button pressed");
        Intent connectIntent = new Intent(this, ConnectActivity.class);
        startActivity(connectIntent);
    }

    /**
     * Navigation to PartyCreationActivity, where hosts create new parties
     */
    public void callCreationActivity(View view) {
        Log.d(LOG_TAG, "Create party button pressed");
        Intent partyIntent = new Intent(this, PartyCreationActivity.class);
        startActivity(partyIntent);
    }

    /**
     * Navigation to the History Activity
     */
    public void viewHistoryFromTitle(View view) {
        Log.d(LOG_TAG, "History button pressed");
        Intent historyIntent = new Intent(this, HistoryOverviewActivity.class);
        startActivity(historyIntent);
    }

    /**
     * Navigation to the Design Activity
     */
    public void viewDesignFromTitle(View view) {
        Log.d(LOG_TAG, "Design button pressed");
        Intent desingIntent = new Intent(this, DesignActivity.class);
        startActivity(desingIntent);
    }

    /**
     * Create an inflatable options menu in the top right corner.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu m) {
        getMenuInflater().inflate(R.menu.menu_main, m);
        return true;
    }

    /**
     * Make the Settings tab call the Settings screen
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Log.d(LOG_TAG, "Settings selected");

                Intent settings = new Intent(this, SettingsActivity.class);
                this.startActivity(settings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
