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

    private SharedPreferences sPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_placeholder);

        preload_input = findViewById(R.id.edittext_setting_preload);
        username_input = findViewById(R.id.edittext_setting_username);

        sPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int preload = sPrefs.getInt(BasePreferencesService.KEY_PRELOAD, 1);
        String username = sPrefs.getString(BasePreferencesService.KEY_USERNAME, "");

        preload_input.setText(String.valueOf(preload));
        username_input.setText(username);
    }

    public void confirmSettings(View v) {

        String preload_text = preload_input.getText().toString();
        String username_text = username_input.getText().toString();

        if (Integer.valueOf(preload_text) > 50) {
            preload_input.setText("50");
            return;
        }

        SharedPreferences.Editor editor = sPrefs.edit();
        editor.putInt(BasePreferencesService.KEY_PRELOAD, Integer.valueOf(preload_text));
        editor.putString(BasePreferencesService.KEY_USERNAME, username_text);

        editor.apply();
        finish();
    }
}
