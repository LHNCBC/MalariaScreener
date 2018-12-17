package gov.nih.nlm.malaria_screener.frontEnd;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import gov.nih.nlm.malaria_screener.R;

/**
 * Created by yuh5 on 11/8/2016.
 */
public class SettingsActivity extends AppCompatActivity{

    public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";

    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        //CharSequence[] cs = bundle.getCharSequenceArray("WB_list");

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_setting);
        toolbar.setTitle(R.string.title_setting);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // smear type
        String smearType = sharedPreferences.getString("smeartype", "Thin");

        // Display the fragment as the main content.
        if (smearType.equals("Thin")) {
            SettingsFragment fragment = new SettingsFragment();
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.container, fragment);
            transaction.commit();
        } else if (smearType.equals("Thick")){
            SettingsFragment_thick fragment = new SettingsFragment_thick();
            fragment.setArguments(bundle);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.container, fragment);
            transaction.commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }



}
