package ch.ethz.inf.vs.kompose.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.util.Log;

import ch.ethz.inf.vs.kompose.R;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    /** LogCat Tag **/
    private static final String SETTINGS_TAG="## Settings:";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Include XML layout
        addPreferencesFromResource(R.xml.preferences);

        // Register for Change Listener in order to do dynamic changes to the menu
        // TODO: If at the end of the project we never used this, remove any trace of it.
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    /**
     * ChangeListener to dynamically update the measurement threshold option.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
