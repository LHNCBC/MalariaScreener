package gov.nih.nlm.malaria_screener.frontEnd;


import android.app.DatePickerDialog;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class SlideInfoActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    Bundle bundle;
    private EditText slideID;
    private EditText date;
    private EditText time;
    private AutoCompleteTextView site;
    private AutoCompleteTextView preparator;
    private AutoCompleteTextView operator;
    private EditText HCT;

    //private Button nextButton;
    //private Button finishButton;
    private Spinner stainingSpinner;
    private String spinnerStr;
    //private String[] stainingM = new String[]{"Giemsa", "Field's"};

    private TextInputLayout sIDLayout;
    private TextInputLayout dateLayout;
    private TextInputLayout timeLayout;
    private TextInputLayout siteLayout;

    private TextInputLayout preparatorLayout;
    private TextInputLayout operatorLayout;
    private TextInputLayout hctLayout;

    MyDBHandler dbHandler;

    private String patientIDStr;

    public static final String PREFS_NAME = "inputPrefs";
    public static final String PREFS_SEARCH_SITE = "site";
    public static final String PREFS_SEARCH_PREPARATOR = "preparator";
    public static final String PREFS_SEARCH_OPERATOR = "operator";
    private SharedPreferences sharedPreferences;
    private Set<String> site_history;
    private Set<String> preparator_history;
    private Set<String> operator_history;

    private boolean firstPageDone = false;

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TimePickerDialog.OnTimeSetListener mTimeSetListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_slide_info);

        Intent in = getIntent();
        bundle = in.getExtras();
        patientIDStr = bundle.getString("patientID");

        final Toolbar toolbar = (Toolbar) findViewById(gov.nih.nlm.malaria_screener.R.id.navigate_bar_slide);
        toolbar.setTitle(gov.nih.nlm.malaria_screener.R.string.title_slide_info);
        toolbar.setTitleTextColor(getResources().getColor(gov.nih.nlm.malaria_screener.R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        sIDLayout = (TextInputLayout) findViewById(gov.nih.nlm.malaria_screener.R.id.layout_slideID);
        dateLayout = (TextInputLayout) findViewById(gov.nih.nlm.malaria_screener.R.id.layout_date);
        timeLayout = (TextInputLayout) findViewById(gov.nih.nlm.malaria_screener.R.id.layout_time);
        siteLayout = (TextInputLayout) findViewById(gov.nih.nlm.malaria_screener.R.id.layout_site);

        dbHandler = new MyDBHandler(this, null, null, 1);

        slideID = (EditText) findViewById(gov.nih.nlm.malaria_screener.R.id.editText_slideID);

        date = (EditText) findViewById(gov.nih.nlm.malaria_screener.R.id.editText_date);
        date.setFocusable(false);
        date.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(SlideInfoActivity.this, android.R.style.ThemeOverlay_Material_Dialog, mDateSetListener, year, month, day);
                        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();

                    }

                }
        );

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                month = month + 1;
                date.setText(month + "/" + dayOfMonth + "/" + year);
            }
        };

        time = (EditText) findViewById(gov.nih.nlm.malaria_screener.R.id.editText_time);
        time.setFocusable(false);
        time.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Calendar calendar = Calendar.getInstance();
                        int hour = calendar.get(Calendar.HOUR_OF_DAY);
                        int minute = calendar.get(Calendar.MINUTE);

                        TimePickerDialog dialog = new TimePickerDialog(SlideInfoActivity.this, android.R.style.ThemeOverlay_Material_Dialog, mTimeSetListener, hour, minute, true);
                        //dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                    }

                }
        );

        mTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                if (minute < 10) {
                    time.setText(hourOfDay + ":0" + minute);
                } else {
                    time.setText(hourOfDay + ":" + minute);

                }
            }
        };

        site = (AutoCompleteTextView) findViewById(gov.nih.nlm.malaria_screener.R.id.editText_site);

        sharedPreferences = getSharedPreferences(PREFS_NAME, 0);
        site_history = sharedPreferences.getStringSet(PREFS_SEARCH_SITE, new HashSet<String>());
        setAutoCompleteSource();

        slideID.addTextChangedListener(watcher);
        date.addTextChangedListener(watcher);
        time.addTextChangedListener(watcher);
        site.addTextChangedListener(watcher);

    }

    public void nextPageClicked1() {

        //if (slideID.getText().toString().isEmpty() || date.getText().toString().isEmpty() || time.getText().toString().isEmpty() || site.getText().toString().isEmpty()) {
        if (slideID.getText().toString().isEmpty()) {  // only slide ID is required  03/12/2019

            //if (slideID.getText().toString().isEmpty()) {
            String string = getResources().getString(R.string.slide_id_empty);
            sIDLayout.setError(string);
            sIDLayout.setErrorEnabled(true);
            //}

            /*if (date.getText().toString().isEmpty()) {
                String string = getResources().getString(R.string.date_empty);
                dateLayout.setError(string);
                dateLayout.setErrorEnabled(true);
            }

            if (time.getText().toString().isEmpty()) {
                String string = getResources().getString(R.string.time_empty);
                timeLayout.setError(string);
                timeLayout.setErrorEnabled(true);
            }

            if (site.getText().toString().isEmpty()) {
                String string = getResources().getString(R.string.site_empty);
                siteLayout.setError(string);
                siteLayout.setErrorEnabled(true);
            }*/

        } else {

            if (!dbHandler.checkExist_Slide(patientIDStr, slideID.getText().toString())) {

                firstPageDone = true;

                View view = this.getCurrentFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                // add new sites to preference list
                addSearchInput(site.getText().toString());

                setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_slide_info1);

                Toolbar toolbar = (Toolbar) findViewById(gov.nih.nlm.malaria_screener.R.id.navigate_bar_slide1);
                toolbar.setTitle(gov.nih.nlm.malaria_screener.R.string.title_slide_info);
                toolbar.setTitleTextColor(getResources().getColor(gov.nih.nlm.malaria_screener.R.color.toolbar_title));
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);

                stainingSpinner = (Spinner) findViewById(gov.nih.nlm.malaria_screener.R.id.spinner_staining);

                preparator = (AutoCompleteTextView) findViewById(gov.nih.nlm.malaria_screener.R.id.editText_preparator);
                operator = (AutoCompleteTextView) findViewById(gov.nih.nlm.malaria_screener.R.id.editText_operator);
                HCT = (EditText) findViewById(gov.nih.nlm.malaria_screener.R.id.editText_HCT);

                preparatorLayout = (TextInputLayout) findViewById(gov.nih.nlm.malaria_screener.R.id.layout_preparator);
                operatorLayout = (TextInputLayout) findViewById(gov.nih.nlm.malaria_screener.R.id.layout_operator);
                hctLayout = (TextInputLayout) findViewById(gov.nih.nlm.malaria_screener.R.id.layout_hct);

                final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
                HCT.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (hasFocus) {

                        }
                    }
                });

                preparator.addTextChangedListener(secondWatcher);
                operator.addTextChangedListener(secondWatcher);
                HCT.addTextChangedListener(secondWatcher);

                preparator_history = sharedPreferences.getStringSet(PREFS_SEARCH_PREPARATOR, new HashSet<String>());
                operator_history = sharedPreferences.getStringSet(PREFS_SEARCH_OPERATOR, new HashSet<String>());
                setAutoCompleteSource1();

            } else {
                String string = getResources().getString(R.string.slide_id_exist);
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void nextPageClicked2() {

        /*if (preparator.getText().toString().isEmpty() || operator.getText().toString().isEmpty() || HCT.getText().toString().isEmpty()) {

            if (preparator.getText().toString().isEmpty()) {
                String string = getResources().getString(R.string.slide_pre_empty);
                preparatorLayout.setError(string);
                preparatorLayout.setErrorEnabled(true);
            }

            if (operator.getText().toString().isEmpty()) {
                String string = getResources().getString(R.string.microscopist_empty);
                operatorLayout.setError(string);
                operatorLayout.setErrorEnabled(true);
            }

            if (HCT.getText().toString().isEmpty()) {
                String string = getResources().getString(R.string.hct_empty);
                hctLayout.setError(string);
                hctLayout.setErrorEnabled(true);
            }

        } else {*/

        String dateStr;
        String timeStr;
        String siteStr;
        String preparatorStr;
        String operatorStr;
        String hctStr;

        if (date.getText().toString().isEmpty()){
            dateStr = "N/A";
        } else {
            dateStr = date.getText().toString();
        }

        if (time.getText().toString().isEmpty()){
            timeStr = "N/A";
        } else {
            timeStr = time.getText().toString();
        }

        if (site.getText().toString().isEmpty()){
            siteStr = "N/A";
        } else {
            siteStr = site.getText().toString();
        }

        if (preparator.getText().toString().isEmpty()){
            preparatorStr = "N/A";
        } else {
            preparatorStr = preparator.getText().toString();
        }

        if (operator.getText().toString().isEmpty()){
            operatorStr = "N/A";
        } else {
            operatorStr = operator.getText().toString();
        }

        if (HCT.getText().toString().isEmpty()){
            hctStr = "0";
        } else {
            hctStr = HCT.getText().toString();
        }

        addSearchInput_preparator(preparator.getText().toString());
        addSearchInput_operator(operator.getText().toString());

        // add slide into database
//                                                String slideIDStr = slideID.getText().toString();
//                                                String patientIDStr = bundle.getString("patientID");
//                                                String dateStr = date.getText().toString();
//                                                String timeStr = time.getText().toString();
//                                                String siteStr = site.getText().toString();
//                                                String preparatorStr = preparator.getText().toString();
//                                                String operatorStr = operator.getText().toString();
//                                                String stainingStr = stainingSpinner.getSelectedItem().toString();
//                                                String hctStr = HCT.getText().toString();

        //Slides slides = new Slides(slideIDStr, patientIDStr, dateStr, timeStr, siteStr, preparatorStr, operatorStr, stainingStr, hctStr, "", "", "");
        //dbHandler.addSlide(slides);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // smear type
        String smearType = sharedPreferences.getString("smeartype", "Thin");

        Intent intent = null;

        if (smearType.equals("Thin")) {
            intent = new Intent(getApplicationContext(), SummarySheetActivity.class);
        } else if (smearType.equals("Thick")) {
            intent = new Intent(getApplicationContext(), SummarySheetActivity_thick.class);
        }

        spinnerStr = stainingSpinner.getSelectedItem().toString();

        bundle.putString("patientID", patientIDStr);
        bundle.putString("slideID", slideID.getText().toString());
        bundle.putString("date", dateStr);
        bundle.putString("time", timeStr);
        bundle.putString("site", siteStr);

        bundle.putString("preparator", preparatorStr);
        bundle.putString("operator", operatorStr);
        bundle.putString("staining", spinnerStr);
        bundle.putString("hct", hctStr);
        bundle.putString("newSlide", "true");

        intent.putExtras(bundle);
        startActivity(intent);

        //}
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!slideID.getText().toString().isEmpty()) {
                //sIDLayout.setErrorEnabled(false);
                sIDLayout.setError(null);
            }
            if (!date.getText().toString().isEmpty()) {
                //dateLayout.setErrorEnabled(false);
                dateLayout.setError(null);
            }
            if (!time.getText().toString().isEmpty()) {
                //timeLayout.setErrorEnabled(false);
                timeLayout.setError(null);
            }
            if (!site.getText().toString().isEmpty()) {
                //siteLayout.setErrorEnabled(false);
                siteLayout.setError(null);
            }
        }
    };

    TextWatcher secondWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (!preparator.getText().toString().isEmpty()) {
                //preparatorLayout.setErrorEnabled(false);
                preparatorLayout.setError(null);
            }
            if (!operator.getText().toString().isEmpty()) {
                //operatorLayout.setErrorEnabled(false);
                operatorLayout.setError(null);
            }
            if (!HCT.getText().toString().isEmpty()) {
                //hctLayout.setErrorEnabled(false);
                hctLayout.setError(null);
            }


        }
    };

    private void setAutoCompleteSource() {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, site_history.toArray(new String[site_history.size()]));

        site.setAdapter(adapter);

    }

    private void addSearchInput(String input) {

        if (!site_history.contains(input)) {
            site_history.add(input);
            setAutoCompleteSource();
        }
    }

    private void setAutoCompleteSource1() {

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, preparator_history.toArray(new String[preparator_history.size()]));

        preparator.setAdapter(adapter);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
                this, android.R.layout.simple_list_item_1, operator_history.toArray(new String[operator_history.size()]));

        operator.setAdapter(adapter1);

    }

    private void addSearchInput_preparator(String input) {
        if (!preparator_history.contains(input)) {
            preparator_history.add(input);
            setAutoCompleteSource1();

        }
    }

    private void addSearchInput_operator(String input) {
        if (!operator_history.contains(input)) {
            operator_history.add(input);
            setAutoCompleteSource1();

        }
    }

    private void savePrefs() {

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putStringSet(PREFS_SEARCH_SITE, site_history);
        editor.putStringSet(PREFS_SEARCH_PREPARATOR, preparator_history);
        editor.putStringSet(PREFS_SEARCH_OPERATOR, operator_history);

        editor.commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_slide, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.nextPage:
                if (!firstPageDone) {
                    nextPageClicked1();
                } else {
                    nextPageClicked2();
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

        savePrefs();
    }

}
