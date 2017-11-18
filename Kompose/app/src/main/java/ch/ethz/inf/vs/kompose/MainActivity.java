package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.service.AndroidServerService;
import ch.ethz.inf.vs.kompose.service.StorageService;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "## Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_placeholder);
    }

    public void joinParty(View view) {
    }

    public void createParty(View view) {
        Log.d(LOG_TAG, "Create party button pressed");

        // start server service
        Intent serviceIntent = new Intent(this, AndroidServerService.class);
        startService(serviceIntent);
    }

    public void viewHistoryFromTitle(View view) {

    }
}
