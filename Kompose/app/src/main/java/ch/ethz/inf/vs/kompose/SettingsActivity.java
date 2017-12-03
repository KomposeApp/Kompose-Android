package ch.ethz.inf.vs.kompose;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import ch.ethz.inf.vs.kompose.preferences.PreferenceUtility;


public class SettingsActivity extends AppCompatActivity {

    // View elements that matter to us
    private TextView error_display;

    private EditText preload_input;
    private EditText username_input;
    private EditText port_input;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Retrieve error text display
        error_display = findViewById(R.id.textview_settings_error);

        //Retrieve input fields
        username_input = findViewById(R.id.edittext_setting_username);
        port_input = findViewById(R.id.edittext_setting_port);
        preload_input = findViewById(R.id.edittext_setting_preload);

        // Display current values
        username_input.setText(PreferenceUtility.getCurrentUsername(this));
        port_input.setText(String.valueOf(PreferenceUtility.getCurrentPort(this)));
        preload_input.setText(String.valueOf(PreferenceUtility.getCurrentPreload(this)));

    }

    /**
     * Executed when we want to confirm our settings and exit the activity.
     * Is not performed if we simply push the back button.
     */
    public void confirmSettings(View v) {

        boolean commitChanges = true;

        String preload_text = preload_input.getText().toString();
        String username_text = username_input.getText().toString();
        String port_text = port_input.getText().toString();

        // Remove trailing whitespace from username.
        username_text = username_text.trim();

        // Revert color changes if there have been previously
        username_input.setTextColor(getResources().getColor(R.color.colorBlack));
        preload_input.setTextColor(getResources().getColor(R.color.colorBlack));
        port_input.setTextColor(getResources().getColor(R.color.colorBlack));

        /* Here we check for invalid inputs */

        String error_text = "";
        if (username_text.isEmpty()){
            username_input.setTextColor(getResources().getColor(R.color.colorRedFlat));
            error_text += getString(R.string.setting_error_username) + "\n";
            commitChanges = false;
        }

        if (Integer.valueOf(preload_text) > 10) {
            preload_input.setTextColor(getResources().getColor(R.color.colorRedFlat));
            error_text += getString(R.string.setting_error_preload) + "\n";
            commitChanges = false;
        }

        boolean valid_port = checkPortValidity(port_text);
        if (!valid_port){
            port_input.setTextColor(getResources().getColor(R.color.colorRedFlat));
            error_text += getString(R.string.setting_error_port) + "\n";
            commitChanges = false;
        }

        error_display.setText(error_text);

        /* END Invalid input check */

        if (commitChanges) {
            PreferenceUtility.setCurrentUsername(this, username_text);
            PreferenceUtility.setCurrentPort(this, Integer.valueOf(port_text));
            PreferenceUtility.setCurrentPreload(this, Integer.valueOf(preload_text));
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
