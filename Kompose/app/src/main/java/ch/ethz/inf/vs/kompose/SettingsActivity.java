package ch.ethz.inf.vs.kompose;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import ch.ethz.inf.vs.kompose.service.base.BasePreferencesService;

public class SettingsActivity extends AppCompatActivity {

    private EditText preload_input;
    private EditText username_input;
    private EditText port_input;

    private SharedPreferences sPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_placeholder);

        preload_input = findViewById(R.id.edittext_setting_preload);
        username_input = findViewById(R.id.edittext_setting_username);
        port_input = findViewById(R.id.edittext_setting_port);

        sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int preload = sPrefs.getInt(BasePreferencesService.KEY_PRELOAD, 1);
        String username = sPrefs.getString(BasePreferencesService.KEY_USERNAME, "");
        int port = sPrefs.getInt(BasePreferencesService.KEY_PORT, 0);

        preload_input.setText(String.valueOf(preload));
        username_input.setText(username);
        port_input.setText(String.valueOf(port));
    }

    public void confirmSettings(View v) {

        boolean commitChanges = true;

        String preload_text = preload_input.getText().toString();
        String username_text = username_input.getText().toString();
        String port_text = port_input.getText().toString();

        if (Integer.valueOf(preload_text) > 50) {
            preload_input.setText("50");
            commitChanges = false;
        }

        if (Integer.valueOf(port_text) > 65535){
            port_input.setText("65535");
            commitChanges = false;
        }

        if (commitChanges) {
            SharedPreferences.Editor editor = sPrefs.edit();
            editor.putInt(BasePreferencesService.KEY_PRELOAD, Integer.valueOf(preload_text));
            editor.putString(BasePreferencesService.KEY_USERNAME, username_text);
            editor.putInt(BasePreferencesService.KEY_PORT, Integer.valueOf(port_text));

            editor.apply();
            finish();
        }
    }
}
