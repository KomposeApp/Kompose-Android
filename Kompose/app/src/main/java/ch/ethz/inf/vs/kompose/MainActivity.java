package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import ch.ethz.inf.vs.kompose.databinding.ActivityMainBinding;


public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Main Activity";

    //Some static variables here because I didn't know where else to put them
    public static final String KEY_CNETWORKSERVICE = String.valueOf("ClientNetworkService".hashCode());

    public static final boolean DESIGN_MODE = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setTitle("create a shared playlist with friends");
        binding.setText1("someone is already komposing music");
        binding.setSubText1("... and I want to join!");
        binding.setText2("no, others should join my party");
        binding.setSubText2("... because I am connected to the music station");
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
