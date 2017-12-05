package ch.ethz.inf.vs.kompose;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;

import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;
import ch.ethz.inf.vs.kompose.service.StateSingleton;


public class SettingsActivity extends AppCompatActivity {

    private static int MAXPRELOAD = 10;
    private static int MAXCACHE = 20;

    // View elements that matter to us
    private TextView error_display;

    private EditText username_input;
    private EditText sessionname_input;
    private EditText preload_input;
    private EditText cachesize_input;
    private EditText hostport_input;
    private EditText clientport_input;

    // Preference Utility from StateSingleton
    private PreferenceUtility util;

    //TODO: Optimize everything here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Retrieve error text display
        error_display = findViewById(R.id.textview_settings_error);

        //Retrieve input fields
        username_input = findViewById(R.id.edittext_setting_username);
        sessionname_input = findViewById(R.id.edittext_setting_sessionname);
        clientport_input = findViewById(R.id.edittext_setting_clientport);
        hostport_input = findViewById(R.id.edittext_setting_hostport);
        preload_input = findViewById(R.id.edittext_setting_preload);
        cachesize_input = findViewById(R.id.edittext_setting_cachesize);

        // Display current values
        util = StateSingleton.getInstance().getPreferenceUtility();
        cachesize_input.setText(String.valueOf(util.getCurrentCacheSize()));
        username_input.setText(util.getUsername());
        sessionname_input.setText(util.getSessionName());
        hostport_input.setText(String.valueOf(util.getHostPort()));
        clientport_input.setText(String.valueOf(util.getClientPort()));
        preload_input.setText(String.valueOf(util.getPreload()));
    }

    /**
     * Executed when we want to confirm our settings and exit the activity.
     * Is not performed if we simply push the back button.
     * TODO: Optimize everything here
     */
    public void confirmSettings(View v) {

        boolean commitChanges = true;

        String preload_text = preload_input.getText().toString();
        String cachesize_text = cachesize_input.getText().toString();
        String username_text = username_input.getText().toString();
        String sessionname_text = sessionname_input.getText().toString();
        String hostport_text = hostport_input.getText().toString();
        String clientport_text = clientport_input.getText().toString();

        // Remove trailing whitespace from username.
        username_text = username_text.trim();
        sessionname_text = sessionname_text.trim();

        // Revert color changes if there have been previously
        username_input.setTextColor(getResources().getColor(R.color.colorBlack));
        sessionname_input.setTextColor(getResources().getColor(R.color.colorBlack));
        preload_input.setTextColor(getResources().getColor(R.color.colorBlack));
        cachesize_input.setTextColor(getResources().getColor(R.color.colorBlack));
        hostport_input.setTextColor(getResources().getColor(R.color.colorBlack));
        clientport_input.setTextColor(getResources().getColor(R.color.colorBlack));

        /* Here we check for invalid inputs */
        boolean valid_port;
        String error_text = "";
        if (username_text.isEmpty()){
            username_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_username) + "\n";
            commitChanges = false;
        }

        if (sessionname_text.isEmpty()){
            username_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_sessionname) + "\n";
            commitChanges = false;
        }

        if (Integer.valueOf(preload_text) > MAXPRELOAD) {
            preload_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_preload) + ": " + MAXPRELOAD + "\n";
            commitChanges = false;
        }

        if (Integer.valueOf(cachesize_text) > MAXCACHE) {
            cachesize_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_cache) + ": "+ MAXCACHE + "\n";
            commitChanges = false;
        }

        valid_port = checkPortValidity(hostport_text);
        if (!valid_port){
            hostport_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_hostport) + "\n";
            commitChanges = false;
        }

        valid_port = checkPortValidity(clientport_text);
        if (!valid_port){
            clientport_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_clientport) + "\n";
            commitChanges = false;
        }

        error_display.setText(error_text);

        /* END Invalid input check */

        if (commitChanges) {
            int preload = Integer.valueOf(preload_text);
            int cachesize = Integer.valueOf(cachesize_text);

            util.setUsername(username_text);
            util.setSessionName(sessionname_text);
            util.setHostPort(Integer.valueOf(hostport_text));
            util.setClientPort(Integer.valueOf(clientport_text));
            util.setPreload(preload);
            util.setCurrentCacheSize(cachesize);
            util.applyChanges();

            //Reset Song Cache after making changes
            StateSingleton.getInstance().initializeSongCache(preload,cachesize);
            finish();
        }
    }

    /**
     * Checks whether the given port is not reserved or already in use.
     * Note that 0 tells the app to use a random open port.
     * @param port_text String representation of the port to check
     * @return true iff port is usable
     */
    private boolean checkPortValidity(String port_text){
        int port = Integer.valueOf(port_text);
        boolean goahead = (0 == port) || ((1024 < port) && (port < 65535));
        if (goahead){
            try{
                new ServerSocket(port).close();
                return true;
            }catch(IOException io){
                return false;
            }
        }
        return false;
    }

}
