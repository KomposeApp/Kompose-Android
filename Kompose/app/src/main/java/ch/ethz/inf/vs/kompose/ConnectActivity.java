package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.service.SessionService;


public class ConnectActivity extends BaseServiceActivity {

    private static final String LOG_TAG = "## Connect Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_placeholder);

        bindBaseService(SessionService.class);
    }

    public void connect(View v) {
        //TODO: resolve pressed session and set client name
        getSessionService().joinSession(null, "clientName");

        Log.d(LOG_TAG, "Connect button pressed");
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }


}
