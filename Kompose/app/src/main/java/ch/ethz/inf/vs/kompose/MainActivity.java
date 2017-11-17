package ch.ethz.inf.vs.kompose;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.model.AndroidServerService;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "### Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_placeholder);
    }

    public void joinParty(View view) {
    }

    public void createParty(View view) {
        Log.d(LOG_TAG, "Create party button pressed");
        AndroidServerService androidServerService = new AndroidServerService(this);
        androidServerService.startService(null);
    }

    public void viewHistoryFromTitle(View view){

    }
}
