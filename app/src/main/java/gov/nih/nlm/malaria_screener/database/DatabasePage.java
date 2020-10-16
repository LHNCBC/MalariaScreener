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

package gov.nih.nlm.malaria_screener.database;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter_PatientDB;
import gov.nih.nlm.malaria_screener.custom.RowItem_Patient;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsMethods;
import gov.nih.nlm.malaria_screener.uploadFunction.UploadActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class DatabasePage extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    MyDBHandler dbHandler;
    private Button exportButton;
    private Button deleteDBButton, dropboxButton;

    static final int REQUEST_DROPBOX = 1;
    static final int REQUEST_REGISTER = 5;

    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String DROPBOX_REGISTER = "registered";

    ListView listView_allPatients;
    ListView listView_testPatients;

    Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_manage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_database);
        toolbar.setTitle(R.string.title_database);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbHandler = new MyDBHandler(this, null, null, 1);
        exportButton = (Button) findViewById(R.id.button_export);
        deleteDBButton = (Button) findViewById(R.id.button_delete);
        dropboxButton = (Button) findViewById(R.id.button_dropbox);
        //deleteImagesButton = (Button) findViewById(R.id.button_delete_all_images);

        listView_allPatients = (ListView) findViewById(R.id.listView_allPatient);
        listView_testPatients = (ListView) findViewById(R.id.listView_testPatient);

        printDatabase();

        exportButton.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // export database to csv file
                        UtilsMethods.exportDB(getApplicationContext());

                        Toast.makeText(getApplicationContext(), "Database file has been exported.", Toast.LENGTH_SHORT).show();

                    }
                }
        );

        dropboxButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        SharedPreferences sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);

                        if (!sharedPreferences.getBoolean(DROPBOX_REGISTER, false)) { // if haven't register

                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DatabasePage.this);

                            // Setting Dialog Title
                            alertDialog.setTitle(R.string.register);

                            // Setting Dialog Message
                            alertDialog.setMessage(R.string.register_message);

                            // Setting Positive "Yes" Button
                            String string = getResources().getString(R.string.yes);
                            alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent registerIntent = new Intent(getApplicationContext(), Register.class);
                                    registerIntent.putExtra("from_disclaimer", false);
                                    startActivityForResult(registerIntent, REQUEST_REGISTER);
                                }
                            });

                            // Setting Negative "NO" Button
                            String string1 = getResources().getString(R.string.no);
                            alertDialog.setNegativeButton(string1, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // Write your code here to invoke NO event
                                    String string = getResources().getString(R.string.register_cancel);
                                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                                    dialog.cancel();
                                }
                            });

                            alertDialog.show();

                        } else {
                            /*Intent dropboxIntent = new Intent(v.getContext(), Dropbox.class);
                            startActivityForResult(dropboxIntent, REQUEST_DROPBOX);*/
                            Intent uploadIntent = new Intent(v.getContext(), UploadActivity.class);
                            startActivity(uploadIntent);

                        }
                    }
                }
        );

        deleteDBButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        // First alert
                        AlertDialog.Builder alertDialog_pre = new AlertDialog.Builder(v.getContext());

                        alertDialog_pre.setIcon(R.drawable.warning);

                        alertDialog_pre.setTitle(R.string.warning);

                        alertDialog_pre.setMessage(R.string.deleteDB_message);

                        // Second alert
                        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(v.getContext());

                        // Setting Dialog Title
                        alertDialog.setTitle(R.string.delete);

                        // Setting Dialog Message
                        alertDialog.setMessage(R.string.deleteDB_message1);

                        // Setting Positive "Yes" Button
                        String string = getResources().getString(R.string.yes);
                        alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dbHandler.deleteTable();
                                deleteAllImages();
                                printDatabase();

                                // Write your code here to invoke YES event
                                String string = getResources().getString(R.string.db_empty);
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

                        // Setting Positive "YES" Button
                        String string2 = getResources().getString(R.string.proceed);
                        alertDialog_pre.setPositiveButton(string2, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                dialog.dismiss();

                                // Showing Alert Message
                                alertDialog.show();

                            }
                        });

                        // Setting Negative "NO" Button
                        String string3 = getResources().getString(R.string.cancel);
                        alertDialog_pre.setNegativeButton(string3, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
                                String string = getResources().getString(R.string.click_no);
                                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        });

                        alertDialog_pre.show();

                    }
                }
        );

        listView_allPatients.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                        // get patient ID of the tapped item in listview
                        RowItem_Patient rowItem_patient = (RowItem_Patient) adapterView.getItemAtPosition(position);
                        String item_pIDStr = rowItem_patient.getID();

                        Intent intentSlideLog = new Intent(getApplicationContext(), DatabaseSlideLogPage.class);
                        bundle = new Bundle();
                        bundle.putString("itemPID", item_pIDStr); // the ID for retrieve patient records from database

                        intentSlideLog.putExtras(bundle);
                        startActivity(intentSlideLog);

                    }
                }
        );

        listView_testPatients.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                        Intent intentSlideLog = new Intent(getApplicationContext(), DatabaseSlideLogPage.class);
                        bundle = new Bundle();
                        bundle.putString("itemPID", "test"); // the ID for retrieve patient records from database

                        intentSlideLog.putExtras(bundle);
                        startActivity(intentSlideLog);

                    }
                }
        );


    }

    public void printDatabase() {

        List<RowItem_Patient> rowItems_allpatients = new ArrayList<RowItem_Patient>();
        List<RowItem_Patient> rowItems_testpatients = new ArrayList<RowItem_Patient>();
        ;

        String dbString[][] = dbHandler.databaseToString();

        int dbLength = dbHandler.getTableSize();

        for (int i = 0; i < dbLength; i++) {
            if (!dbString[i][0].equals("test")) { // don't print out test patient here
                if (dbString[i][2].equals("male")) {
                    String string = getResources().getString(R.string.male);
                    dbString[i][2] = string;
                } else if (dbString[i][2].equals("female")){
                    String string = getResources().getString(R.string.female);
                    dbString[i][2] = string;
                }

                RowItem_Patient item = new RowItem_Patient(dbString[i][0], dbString[i][1], dbString[i][2], dbString[i][3]);
                rowItems_allpatients.add(item);
            }
        }

        CustomAdapter_PatientDB adapter_patientDB = new CustomAdapter_PatientDB(this, rowItems_allpatients, false);
        listView_allPatients.setAdapter(adapter_patientDB);

        //test patient
        /*if (dbHandler.checkExist_Patient("test")) {
            RowItem_Patient item_testPatient = new RowItem_Patient("Test", "", "", "");
            rowItems_testpatients.add(item_testPatient);
        }

        CustomAdapter_PatientDB adapter_patientDB_test = new CustomAdapter_PatientDB(this, rowItems_testpatients, true);
        listView_testPatients.setAdapter(adapter_patientDB_test);*/

    }

    private void deleteAllImages() {

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener");

        File[] folderList = file.listFiles();

        if (folderList != null) {
            int length = folderList.length;

            // delete files
            for (int i = 0; i < length; i++) {

                File[] imageList = folderList[i].listFiles();

                if (imageList != null) {
                    int length1 = imageList.length;

                    if (length1 != 0) {
                        for (int j = 0; j < length1; j++) {
                            imageList[j].delete();
                        }

                        folderList[i].delete();
                    }
                }
            }

        }

        final File fileTest = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/Test");


        File[] folderListTest = fileTest.listFiles();

        if (folderListTest != null) {
            int length = folderListTest.length;

            // delete files
            for (int i = 0; i < length; i++) {

                File[] imageList = folderListTest[i].listFiles();

                if (imageList != null) {
                    int length1 = imageList.length;

                    if (length1 != 0) {
                        for (int j = 0; j < length1; j++) {
                            imageList[j].delete();
                        }

                        folderListTest[i].delete();
                    }
                }
            }

            fileTest.delete();
        }

        // Write your code here to invoke NO event
        String string = getResources().getString(R.string.image_all_deleted);
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
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
