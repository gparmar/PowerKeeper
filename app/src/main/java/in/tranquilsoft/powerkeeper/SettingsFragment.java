package in.tranquilsoft.powerkeeper;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by gparmar on 24/05/17.
 */

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference emailPref = getPreferenceScreen().findPreference("email");
        emailPref.setSummary(emailPref.getSharedPreferences().getString("email", ""));

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
//        SharedPreferences prefs = getPreferenceManager().getSharedPreferences();

    }

    @Override
    public void onDestroy() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("email")) {
            Log.d("Settings","email");
            Preference emailPref = getPreferenceScreen().findPreference("email");
            emailPref.setSummary(emailPref.getSharedPreferences().getString("email", ""));
        }
    }
}
