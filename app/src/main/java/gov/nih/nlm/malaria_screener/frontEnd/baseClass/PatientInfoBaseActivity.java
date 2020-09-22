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

package gov.nih.nlm.malaria_screener.frontEnd.baseClass;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;
import gov.nih.nlm.malaria_screener.frontEnd.SlideInfoActivity;

public class PatientInfoBaseActivity extends AppCompatActivity {

    Bundle bundle;

    private TextInputLayout pIDLayout;
    private TextInputLayout initialLayout;
    private TextInputLayout ageLayout;

    private EditText patientID;
    private EditText initials;
    private EditText age;
    private CheckBox checkBox_male;
    private CheckBox checkBox_female;
    private String genderStr = "";
    private TextView genderTextView;

    MyDBHandler dbHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

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
                //onSkipPressed();
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    private void nextpageclicked() {

        Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
        Matcher ms = ps.matcher(initials.getText().toString());
        boolean bs = ms.matches();

        if (patientID.getText().toString().isEmpty() || initials.getText().toString().isEmpty() || age.getText().toString().isEmpty() || genderStr.isEmpty() || bs == false) {

            if (patientID.getText().toString().isEmpty()) {
                pIDLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.patient_id_empty);
                pIDLayout.setError(string);
            }
            if (initials.getText().toString().isEmpty()) {
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
            }

        } else {

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
                bundle.putString("initial", initials.getText().toString().toUpperCase());
                bundle.putString("gender", genderStr);
                bundle.putString("age", age.getText().toString());
                bundle.putString("newPatient", "true");
                intent.putExtras(bundle);
                startActivity(intent);
            } else { // exists in the database

                boolean ifSame = dbHandler.checkIfSame_Patient(patientID.getText().toString(), genderStr, initials.getText().toString(), age.getText().toString());

                if (ifSame) {
                    Intent intent = new Intent(getApplicationContext(), SlideInfoActivity.class);
                    bundle.putString("patientID", patientID.getText().toString());
                    bundle.putString("initial", initials.getText().toString().toUpperCase());
                    bundle.putString("gender", genderStr);
                    bundle.putString("age", age.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    String string = getResources().getString(R.string.pid_exist);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
                }
            }


        }

    }

}
