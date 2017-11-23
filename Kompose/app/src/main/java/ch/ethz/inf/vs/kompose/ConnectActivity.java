package ch.ethz.inf.vs.kompose;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import ch.ethz.inf.vs.kompose.service.SessionService;
import ch.ethz.inf.vs.kompose.preferences.BasePreferencesService;
import ch.ethz.inf.vs.kompose.service.base.BaseService;


public class ConnectActivity extends BaseServiceActivity {

    private static final String LOG_TAG = "## Connect Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_placeholder);

        bindBaseService(SessionService.class);
    }

    @Override
    protected void serviceBoundCallback(BaseService boundService) {
        if (boundService instanceof SessionService) {
            //todo: bind this to view
            getSessionService().getActiveSessions();
        }
    }

    public void connect(View v) {

        //TODO: resolve pressed session and set client name
        String username = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(BasePreferencesService.KEY_USERNAME, BasePreferencesService.DEFAULT_USERNAME);
        getSessionService().joinSession(null, username);

        Log.d(LOG_TAG, "Connect button pressed");
        Intent playlistIntent = new Intent(this, PlaylistActivity.class);
        startActivity(playlistIntent);
        this.finish();
    }


}
