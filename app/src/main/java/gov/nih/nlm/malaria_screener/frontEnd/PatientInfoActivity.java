/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

Licensed under GNU General Public License v3.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package gov.nih.nlm.malaria_screener.frontEnd;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;

import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import gov.nih.nlm.malaria_screener.MainActivity;
import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsData;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatientInfoActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "MyDebug";

    private TextInputLayout pIDLayout;
    private TextInputLayout initialLayout;
    private TextInputLayout ageLayout;

    private TextView genderTextView;

    private EditText patientID;
    private EditText initials;
    private EditText age;
    private CheckBox checkBox_male;
    private CheckBox checkBox_female;
    private String genderStr = "";
    //private Button continueButton;
    MyDBHandler dbHandler;
    //private boolean fromDB = false;

    Bundle bundle;

    SearchView searchView;

    String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        Intent in = getIntent();
        bundle = in.getExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar);
        toolbar.setTitle(R.string.title_patient_info);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        // set home button
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pIDLayout = (TextInputLayout) findViewById(R.id.layout_patientID);
        initialLayout = (TextInputLayout) findViewById(R.id.layout_initial);
        ageLayout = (TextInputLayout) findViewById(R.id.layout_age);
        genderTextView = (TextView) findViewById(R.id.textView_genderString);

        dbHandler = new MyDBHandler(this, null, null, 1);

        checkBox_male = (CheckBox) findViewById(R.id.checkBox_male);

        checkBox_male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (isChecked) {
                    genderTextView.setText(R.string.gender1);
                    checkBox_female.setChecked(false);
                    genderStr = "male";
                } else {
                    genderStr = "";
                }
            }
        });

        checkBox_female = (CheckBox) findViewById(R.id.checkBox_female);
        checkBox_female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (isChecked) {
                    genderTextView.setText(R.string.gender1);
                    checkBox_male.setChecked(false);
                    genderStr = "female";
                } else {
                    genderStr = "";
                }
            }
        });

        patientID = (EditText) findViewById(R.id.editText_patientID);
        initials = (EditText) findViewById(R.id.editText_initials);
        age = (EditText) findViewById(R.id.editText_age);

        patientID.addTextChangedListener(watcher);
        initials.addTextChangedListener(initialWatcher);
        age.addTextChangedListener(watcher);

        //cursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.list_item_search, null, new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1}, new int[] {R.id.textView_search}, 0);

        //listView.setAdapter(cursorAdapter);

    }

    private void nextpageclicked() {

        /*Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
        Matcher ms = ps.matcher(initials.getText().toString());
        boolean bs = ms.matches();*/

        //if (patientID.getText().toString().isEmpty() || initials.getText().toString().isEmpty() || age.getText().toString().isEmpty() || genderStr.isEmpty() || bs == false) {
        if (patientID.getText().toString().isEmpty()){          // only patient ID is required  03/12/2019

            //if (patientID.getText().toString().isEmpty()) {
                pIDLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.patient_id_empty);
                pIDLayout.setError(string);
            //}
            /*if (initials.getText().toString().isEmpty()) {
                initialLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.initial_empty);
                initialLayout.setError(string);
            }
            if (age.getText().toString().isEmpty()) {
                ageLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.age_empty);
                ageLayout.setError(string);
            }
            if (genderStr.isEmpty()) {
                genderTextView.setText(R.string.gender);
            }*/

        } else {

            String initialStr;
            String ageStr;

            if (initials.getText().toString().isEmpty()) {
                initialStr = "N/A";
            } else {
                initialStr = initials.getText().toString().toUpperCase();
            }

            if (age.getText().toString().isEmpty()) {
                ageStr = "N/A";
            } else {
                ageStr = age.getText().toString();
            }

            if (genderStr.isEmpty()) {
                genderStr = "N/A";
            }

            // check patient ID to see if this patient is already in the database
            if (!dbHandler.checkExist_Patient(patientID.getText().toString())) { // not exists in the database
                // add patient info into database
                //String pIDStr = patientID.getText().toString();
                //String initialStr = initials.getText().toString();
                //String ageStr = age.getText().toString();

                //Patients patients = new Patients(pIDStr, genderStr, initialStr, ageStr);
                //dbHandler.addPatient(patients);

                Intent intent = new Intent(getApplicationContext(), SlideInfoActivity.class);
                bundle.putString("patientID", patientID.getText().toString());
                bundle.putString("initial", initialStr);
                bundle.putString("gender", genderStr);
                bundle.putString("age", ageStr);
                bundle.putString("newPatient", "true");
                intent.putExtras(bundle);
                startActivity(intent);
            } else { // exists in the database

                Log.d(TAG, "patientID: " + patientID.getText().toString());
                Log.d(TAG, "genderStr: " + genderStr);
                Log.d(TAG, "initials: " + initials.getText().toString());
                Log.d(TAG, "age: " + age.getText().toString());

                boolean ifSame = dbHandler.checkIfSame_Patient(patientID.getText().toString(), genderStr, initials.getText().toString(), age.getText().toString());

                if (ifSame) {
                    Intent intent = new Intent(getApplicationContext(), SlideInfoActivity.class);
                    bundle.putString("patientID", patientID.getText().toString());
                    bundle.putString("initial", initialStr);
                    bundle.putString("gender", genderStr);
                    bundle.putString("age", ageStr);
                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    String string = getResources().getString(R.string.pid_exist);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
                }
            }


        }

    }

    private TextWatcher initialWatcher = new TextWatcher() {

        boolean bs;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int length, int count) {
            Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
            Matcher ms = ps.matcher(initials.getText().toString());
            bs = ms.matches();

            if (bs == false && initials.getText().toString().isEmpty() == false && !initials.getText().toString().equals("N/A")) {
                initialLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.initial_are_letters);
                initialLayout.setError(string);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

            if ((!initials.getText().toString().isEmpty() && bs) || initials.getText().toString().isEmpty()) {
                //initialLayout.setErrorEnabled(false);
                initialLayout.setError(null);
            }

        }
    };

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            if (!patientID.getText().toString().isEmpty()) {
                //pIDLayout.setErrorEnabled(false);
                pIDLayout.setError(null);
            }
            if (!age.getText().toString().isEmpty()) {
                //ageLayout.setErrorEnabled(false);
                ageLayout.setError(null);
            }

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.nextPage:
                nextpageclicked();
                return true;

            /*case R.id.skip:
                onSkipPressed();
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.requestFocus();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        Cursor cursor = dbHandler.findPatient(s);

        if (cursor.moveToFirst()) {

            patientID.setText(cursor.getString(1));
            initials.setText(cursor.getString(3));
            age.setText(cursor.getString(4));
            genderStr = cursor.getString(2);


            if (genderStr.equals("male")) {
                checkBox_male.setChecked(true);
                checkBox_female.setChecked(false);
            } else if (genderStr.equals("female")){
                checkBox_male.setChecked(false);
                checkBox_female.setChecked(true);
            }

        } else {
            String string = getResources().getString(R.string.no_resultDB);
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {


        return false;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    public void onBackPressed() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.abort);

        alertDialog.setIcon(R.drawable.warning);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.quit_session_message);

        // Setting Positive "Yes" Button
        String string = getResources().getString(R.string.yes);
        alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // kill all the other activities on top of the old MainActivity.class activity
                startActivity(intent);

                reset_utils_data();

                finish();

                // Write your code here to invoke YES event
                String string = getResources().getString(R.string.quit_session_aborted);
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        });

        // Setting Negative "NO" Button
        String string1 = getResources().getString(R.string.no);
        alertDialog.setNegativeButton(string1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                String string = getResources().getString(R.string.click_no);
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();

        return;
    }

    private void reset_utils_data() {

        UtilsData.resetImageNames();

        // thin
        UtilsData.resetCurrentCounts();
        UtilsData.resetTotalCounts();
        UtilsData.resetCountLists();
        UtilsData.resetCountLists_GT();

        //thick
        UtilsData.resetCurrentCounts_thick();
        UtilsData.resetTotalCounts_thick();
        UtilsData.resetCountLists_thick();
        UtilsData.resetCountLists_GT_thick();

    }

}
