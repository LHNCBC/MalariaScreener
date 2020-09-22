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

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter_SlideDB;
import gov.nih.nlm.malaria_screener.custom.RowItem_Slide;
import gov.nih.nlm.malaria_screener.frontEnd.PatientGraph;

import java.util.ArrayList;
import java.util.List;

public class DatabaseSlideLogPage extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    ListView listView_allSlides;

    ListView listView_allSlides_thick;

    MyDBHandler dbHandler;

    List<RowItem_Slide> rowItem_slidesLog;

    List<RowItem_Slide> rowItem_slidesLog_thick;

    String itemPIDStr; // patient ID for query

    Button graphButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_database_slide_log_page);

        Bundle extras = getIntent().getExtras();
        itemPIDStr = extras.getString("itemPID");

        dbHandler = new MyDBHandler(this, null, null, 1);

        rowItem_slidesLog = new ArrayList<RowItem_Slide>();

        rowItem_slidesLog_thick = new ArrayList<RowItem_Slide>();

        Toolbar toolbar = (Toolbar) findViewById(gov.nih.nlm.malaria_screener.R.id.navigate_bar_slide_logDB);
        toolbar.setTitle(gov.nih.nlm.malaria_screener.R.string.title_slide_logDB);
        toolbar.setTitleTextColor(getResources().getColor(gov.nih.nlm.malaria_screener.R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        listView_allSlides = (ListView) findViewById(gov.nih.nlm.malaria_screener.R.id.listView_slideLog);

        listView_allSlides_thick = (ListView) findViewById(gov.nih.nlm.malaria_screener.R.id.listView_slideLog_thick);

        //if (itemPIDStr.equals("test")) { // added for test folder images
        //feedListView_test();
//        } else {
        feedListView_thin(itemPIDStr);
//        }
        feedListView_thick(itemPIDStr);

        listView_allSlides.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

//                        if (itemPIDStr.equals("test")) { // added for test folder images
//
//                            // Get image path
//                            File slideDir = new File(Environment.getExternalStorageDirectory(
//                            ), "NLM_Malaria_Screener/Test");
//
//                            if (slideDir.exists()) {
//                                File[] folderListing = slideDir.listFiles(); // list all sub folders in Test
//                                String testSlideIDStr = folderListing[position].getAbsolutePath().substring(folderListing[position].getAbsolutePath().lastIndexOf("/") + 1);
//
//                                Intent intentSlideInfo = new Intent(getApplicationContext(), DB_SlideInfoActivity.class);
//                                Bundle bundle = new Bundle();
//                                bundle.putString("itemPID", itemPIDStr);
//                                bundle.putString("itemSID", testSlideIDStr);
//
//                                intentSlideInfo.putExtras(bundle);
//                                startActivity(intentSlideInfo);
//                            }
//                        } else {

                            // get slide ID of the tapped item in listview
                            RowItem_Slide rowItem_slide = (RowItem_Slide) adapterView.getItemAtPosition(position);
                            String item_sIDStr = rowItem_slide.getSlideID();

                            Intent intentSlideInfo = new Intent(getApplicationContext(), DB_SlideInfoActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putString("itemPID", itemPIDStr);
                            bundle.putString("itemSID", item_sIDStr);

                            intentSlideInfo.putExtras(bundle);
                            startActivity(intentSlideInfo);
                        //}
                    }
                }
        );

        listView_allSlides_thick.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                        // get slide ID of the tapped item in listview
                        RowItem_Slide rowItem_slide = (RowItem_Slide) adapterView.getItemAtPosition(position);
                        String item_sIDStr = rowItem_slide.getSlideID();

                        Intent intentSlideInfo =  new Intent(getApplicationContext(), DB_SlideInfoActivity_thick.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("itemPID", itemPIDStr);
                        bundle.putString("itemSID", item_sIDStr);

                        intentSlideInfo.putExtras(bundle);
                        startActivity(intentSlideInfo);
                    }
                }
        );

        graphButton = (Button) findViewById(gov.nih.nlm.malaria_screener.R.id.buttonGraph);

        graphButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {

                        if (itemPIDStr.equals("test")) { // added for test folder images
                            Toast.makeText(getBaseContext(), R.string.no_graph,
                                    Toast.LENGTH_LONG).show();
                        } else {

                            Cursor cursor = dbHandler.returnPatientSlides(itemPIDStr);
                            cursor.moveToFirst();

                            ArrayList<String> parasitemiaList = new ArrayList<String>();

                            do {

                                String itemStr = cursor.getString(cursor.getColumnIndex("parasitemia_thin"));
                                if (!itemStr.equals("")) {
                                    parasitemiaList.add(itemStr);
                                }

                            } while (cursor.moveToNext());

                            Intent intentGraph = new Intent(getApplicationContext(), PatientGraph.class);

                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("parasitemiaList", parasitemiaList);
                            intentGraph.putExtras(bundle);

                            startActivity(intentGraph);
                        }
                    }
                }
        );

    }

    public void feedListView_thin(String PID) {

        Cursor cursor = dbHandler.returnPatientSlides(PID);

        cursor.moveToFirst();

        if (dbHandler.returnPatientSlides(PID).getCount() != 0) { // only feed the list when there are slides for current patient

            do {
                String p_thin = cursor.getString(cursor.getColumnIndex("parasitemia_thin"));

                if (!p_thin.equals("")) {
                    String slideIDStr = cursor.getString(cursor.getColumnIndex("slideID"));
                    String pIDStr = cursor.getString(cursor.getColumnIndex("patient_id"));
                    String timeStr = cursor.getString(cursor.getColumnIndex("time"));
                    String dateStr = cursor.getString(cursor.getColumnIndex("date"));

                    RowItem_Slide item = new RowItem_Slide(slideIDStr, pIDStr, timeStr, dateStr);
                    rowItem_slidesLog.add(item);
                }

            } while (cursor.moveToNext());
        }

        CustomAdapter_SlideDB adapter_slideDB = new CustomAdapter_SlideDB(this, rowItem_slidesLog);
        listView_allSlides.setAdapter(adapter_slideDB);

    }

    public void feedListView_thick(String PID) {

        Cursor cursor = dbHandler.returnPatientSlides(PID);

        cursor.moveToFirst();

        if (dbHandler.returnPatientSlides(PID).getCount() != 0) { // only feed the list when there are slides for current patient

            do {
                String p_thick = cursor.getString(cursor.getColumnIndex("parasitemia_thick"));

                if (!p_thick.equals("")) {
                    String slideIDStr = cursor.getString(cursor.getColumnIndex("slideID"));
                    String pIDStr = cursor.getString(cursor.getColumnIndex("patient_id"));
                    String timeStr = cursor.getString(cursor.getColumnIndex("time"));
                    String dateStr = cursor.getString(cursor.getColumnIndex("date"));

                    RowItem_Slide item = new RowItem_Slide(slideIDStr, pIDStr, timeStr, dateStr);
                    rowItem_slidesLog_thick.add(item);
                }

            } while (cursor.moveToNext());
        }

        CustomAdapter_SlideDB adapter_slideDB = new CustomAdapter_SlideDB(this, rowItem_slidesLog_thick);
        listView_allSlides_thick.setAdapter(adapter_slideDB);

    }

    /*public void feedListView_test() {

        // Get image path
        File slideDir = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/Test");

        if (slideDir.exists()) {
            File[] folderListing = slideDir.listFiles(); // list all sub folders in Test

            for (int i = 0; i < folderListing.length; i++) {
                String testSlideIDStr = folderListing[i].getAbsolutePath().substring(folderListing[i].getAbsolutePath().lastIndexOf("/") + 1);
                String slideIDStr = testSlideIDStr;
                String pIDStr = "";
                String timeStr = "N/A";
                String dateStr = "N/A";

                RowItem_Slide item = new RowItem_Slide(slideIDStr, pIDStr, timeStr, dateStr);
                rowItem_slidesLog.add(item);
            }
        }

        CustomAdapter_SlideDB adapter_slideDB = new CustomAdapter_SlideDB(this, rowItem_slidesLog);
        listView_allSlides.setAdapter(adapter_slideDB);

    }*/

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
