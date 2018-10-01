package gov.nih.nlm.malaria_screener.frontEnd;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.UtilsCustom;

/**
 * Created by yuh5 on 11/8/2016.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    CharSequence[] cs_entry, clssifier_entry = {"Deep Learning", "SVM"};
    public static final String KEY_PREF_WB = "whitebalance";
    public static final String KEY_PREF_CLASSIFIER = "classifier";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());


        cs_entry = getArguments().getCharSequenceArray("WB_list");

        ListPreference listPreference = (ListPreference) findPreference("whitebalance");

        if (listPreference!=null){
            CharSequence[] entryValues = new String[cs_entry.length];
            for (int i=0;i<cs_entry.length;i++){
                entryValues[i] = Integer.toString(i);
            }
            listPreference.setEntries(cs_entry);
            listPreference.setEntryValues(entryValues);
            listPreference.setSummary(cs_entry[Integer.valueOf(sharedPreferences.getString("whitebalance", "0"))]);
        }

        ListPreference listPreference_classifier = (ListPreference) findPreference("classifier");
        if (listPreference_classifier!=null){
            CharSequence[] entryValues = new String[clssifier_entry.length];
            for (int i=0;i<clssifier_entry.length;i++){
                entryValues[i] = Integer.toString(i);
            }
            listPreference_classifier.setEntries(clssifier_entry);
            listPreference_classifier.setEntryValues(entryValues);
            listPreference_classifier.setSummary(clssifier_entry[Integer.valueOf(sharedPreferences.getString("classifier", "0"))]);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(KEY_PREF_WB)) {
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            int index = Integer.valueOf(sharedPreferences.getString(key, "0"));
            connectionPref.setSummary(cs_entry[index]);

        } else if (key.equals(KEY_PREF_CLASSIFIER)){
            Preference connectionPref = findPreference(key);
            // Set summary to be the user-description for the selected value
            int index = Integer.valueOf(sharedPreferences.getString(key, "0"));
            connectionPref.setSummary(clssifier_entry[index]);
        }
    }
}
