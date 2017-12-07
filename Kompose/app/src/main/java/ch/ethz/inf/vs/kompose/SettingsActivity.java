package ch.ethz.inf.vs.kompose;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;

import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;
import ch.ethz.inf.vs.kompose.service.StateSingleton;

public class SettingsActivity extends AppCompatActivity {

    private final int MAXPRELOAD = 10;
    private final int MAXCACHE = 20;

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

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Confirm the settings if no errors are present.
     */
    public void confirmSettings(View v) {

        boolean commitChanges = true;

        // Define a local variable for each of the text inputs
        // We refrain from using an array since most of these are treated differently
        String username_text = username_input.getText().toString().trim();
        String sessionname_text = sessionname_input.getText().toString().trim();
        String preload_text = preload_input.getText().toString();
        String cachesize_text = cachesize_input.getText().toString();
        String hostport_text = hostport_input.getText().toString();
        String clientport_text = clientport_input.getText().toString();

        // Revert color changes
        username_input.setTextColor(getResources().getColor(R.color.colorBlack));
        sessionname_input.setTextColor(getResources().getColor(R.color.colorBlack));
        preload_input.setTextColor(getResources().getColor(R.color.colorBlack));
        cachesize_input.setTextColor(getResources().getColor(R.color.colorBlack));
        hostport_input.setTextColor(getResources().getColor(R.color.colorBlack));
        clientport_input.setTextColor(getResources().getColor(R.color.colorBlack));

        boolean valid_port;
        String error_text = "";
        // Display an error if the username is empty (or all whitespace)
        if (username_text.isEmpty()){
            username_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_username) + "\n";
            commitChanges = false;
        }

        // Same for the session
        if (sessionname_text.isEmpty()){
            sessionname_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_sessionname) + "\n";
            commitChanges = false;
        }

        // Display an error if the preload value exceeds its maximum value
        if (Integer.valueOf(preload_text) > MAXPRELOAD) {
            preload_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_preload) + ": " + MAXPRELOAD + "\n";
            commitChanges = false;
        }

        // Display an error if the cache size exceeds the maximum value
        if (Integer.valueOf(cachesize_text) > MAXCACHE) {
            cachesize_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_cache) + ": "+ MAXCACHE + "\n";
            commitChanges = false;
        }

        // Check the validity of the host port
        valid_port = checkPortValidity(hostport_text);
        if (!valid_port){
            hostport_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_hostport) + "\n";
            commitChanges = false;
        }

        // Check the validity of the client port
        valid_port = checkPortValidity(clientport_text);
        if (!valid_port){
            clientport_input.setTextColor(getResources().getColor(R.color.colorAccent));
            error_text += getString(R.string.setting_error_clientport) + "\n";
            commitChanges = false;
        }

        // Display any errors in the activity
        error_display.setText(error_text);

        // If no errors occurred, finish and reset the cache.
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
