package ch.ethz.inf.vs.kompose;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ch.ethz.inf.vs.kompose.service.StateSingleton;
import ch.ethz.inf.vs.kompose.service.preferences.PreferenceUtility;

public class SettingsActivity extends AppCompatActivity {

    private SettingsFragment settingsFragment;

    @Override
    public void onResume() {
        super.onResume();
        if (settingsFragment != null) {
            settingsFragment.registerSharedPrefListener();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (settingsFragment != null) {
            settingsFragment.unregisterSharedPrefListener();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // setup toolbar
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);

        // setup preference fragment
        settingsFragment = new SettingsFragment();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment {

        SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                setDefaults();
                preferenceUtility.setChanged();
            }
        };

        PreferenceUtility preferenceUtility = StateSingleton.getInstance().getPreferenceUtility();

        public void registerSharedPrefListener() {
            getPreferenceManager()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listener);
        }

        public void unregisterSharedPrefListener() {
            getPreferenceManager()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listener);
        }

        public void setDefaults() {
            PreferenceUtility preferenceUtility = StateSingleton.getInstance().getPreferenceUtility();

            Preference clientPortPref = findPreference(PreferenceUtility.KEY_CPORT);
            clientPortPref.setSummary("Current: " + preferenceUtility.getClientPort());

            Preference hostPortPref = findPreference(PreferenceUtility.KEY_HPORT);
            hostPortPref.setSummary("Current: " + preferenceUtility.getHostPort());

            Preference preloadPref = findPreference(PreferenceUtility.KEY_PRELOAD);
            preloadPref.setSummary("Current: " + preferenceUtility.getPreload());

            Preference cachePref = findPreference(PreferenceUtility.KEY_MAXDLSIZE);
            cachePref.setSummary("Current: " + preferenceUtility.getCurrentMaxDLSize());
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.fragment_preference);
            setDefaults();
        }
    }
}
