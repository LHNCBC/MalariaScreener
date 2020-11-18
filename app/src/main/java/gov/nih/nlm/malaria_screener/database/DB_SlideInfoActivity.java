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
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter;
import gov.nih.nlm.malaria_screener.custom.RowItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DB_SlideInfoActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    ListView listView_slide;

    MyDBHandler dbHandler;

    String[] slide_item;
    String[] slide_txt;

    Button imageGalleryButton;

    String patientStr;
    String slideStr;

    String[] allManualCounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_slide_page);

        Bundle extras = getIntent().getExtras();
        patientStr = extras.getString("itemPID");
        slideStr = extras.getString("itemSID");

        dbHandler = new MyDBHandler(this, null, null, 1);

        slide_item = getResources().getStringArray(R.array.slide_item_db);

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_slideDB);
        toolbar.setTitle(R.string.title_slide_infoDB);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        listView_slide = (ListView) findViewById(R.id.listView_slide);

        feedListView(patientStr, slideStr);

        imageGalleryButton = (Button) findViewById(R.id.button_imagegallery);
        imageGalleryButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        Intent ImageViewIntent = new Intent(getApplicationContext(), ImageGallery.class);
                        Bundle bundle = new Bundle();
                        bundle.putString("itemPID", patientStr);
                        bundle.putString("itemSID", slideStr);

                        ImageViewIntent.putExtras(bundle);
                        startActivity(ImageViewIntent);

                    }
                }
        );

    }

    private void feedListView(String patientStr, String slideStr) {

        //String cellCountStr;
        //String infectedCountStr;
        //int Parasitaemia;
        //int cellCount = 0;
        //int infectedCount = 0;
        //String ParasitaemiaStr;

        Cursor cursorImages1 = dbHandler.returnAllSlideImages(patientStr, slideStr);

        // get manual counts from database of each image and then add up together
        String cellCountGTStr;
        String infectedCountGTStr;

        int cellCountGT = 0;
        int infectedCountGT = 0;
        int ParasitaemiaGT;
        String ParasitaemiaGTStr;

        do {
            String cellCountGTTemp, infectedCountGTTemp;
            cellCountGTTemp = cursorImages1.getString(cursorImages1.getColumnIndex("cell_count_gt"));
            infectedCountGTTemp = cursorImages1.getString(cursorImages1.getColumnIndex("infected_count_gt"));

            if (cellCountGTTemp.equals("N/A") || infectedCountGTTemp.equals("N/A")) {
                cellCountGT = 0;
                infectedCountGT = 0;
                break;
            } else {
                cellCountGT = cellCountGT + Integer.valueOf(cellCountGTTemp);
                infectedCountGT = infectedCountGT + Integer.valueOf(infectedCountGTTemp);
            }

        } while (cursorImages1.moveToNext());

        if (cellCountGT == 0 && infectedCountGT == 0) {
            cellCountGTStr = "N/A";
            infectedCountGTStr = "N/A";

        } else {
            cellCountGTStr = String.valueOf(cellCountGT);
            infectedCountGTStr = String.valueOf(infectedCountGT);
        }

//        for (int i=0;i<allManualCounts.length/2;i++){
//
//            if (allManualCounts[2*i].equals("N/A") || allManualCounts[2*i+1].equals("N/A")){
//                cellCount = 0;
//                infectedCount = 0;
//                break;
//            } else{
//                cellCount = cellCount + Integer.valueOf(allManualCounts[2*i]);
//                infectedCount = infectedCount + Integer.valueOf(allManualCounts[2*i+1]);
//            }
//
//        }
//
//        if (cellCount == 0 && infectedCount == 0){
//            cellCountStr = "N/A";
//            infectedCountStr = "N/A";
//
//        } else {
//            cellCountStr = String.valueOf(cellCount);
//            infectedCountStr = String.valueOf(infectedCount);
//        }


        // get count of each image and then add up together
        Cursor cursorImages2 = dbHandler.returnAllSlideImages(patientStr, slideStr);

        String cellCountStr;
        String infectedCountStr;

        int cellTotal = 0;
        int infectedTotal = 0;
        String ParasitaemiaStr;

        do {
            String cellCountTemp, infectedCountTemp;
            cellCountTemp = cursorImages2.getString(cursorImages2.getColumnIndex("cell_count"));
            infectedCountTemp = cursorImages2.getString(cursorImages2.getColumnIndex("infected_count"));

            if (cellCountTemp.equals("N/A") || infectedCountTemp.equals("N/A")){
                cellTotal = 0;
                infectedTotal = 0;
                break;
            }

            cellTotal = cellTotal + Integer.valueOf(cellCountTemp);
            infectedTotal = infectedTotal + Integer.valueOf(infectedCountTemp);

        } while (cursorImages2.moveToNext());

        if (cellTotal == 0 && infectedTotal == 0) {
            cellCountStr = "N/A";
            infectedCountStr = "N/A";

        } else {
            cellCountStr = String.valueOf(cellTotal);
            infectedCountStr = String.valueOf(infectedTotal);
        }

        Cursor cursor = dbHandler.returnSlideCursor(patientStr, slideStr);
        slide_txt = new String[14];
        slide_txt[0] = cursor.getString(cursor.getColumnIndex("slideID"));
        slide_txt[1] = cursor.getString(cursor.getColumnIndex("date"));
        slide_txt[2] = cursor.getString(cursor.getColumnIndex("time"));
        slide_txt[3] = cursor.getString(cursor.getColumnIndex("site"));
        slide_txt[4] = cursor.getString(cursor.getColumnIndex("preparator"));
        slide_txt[5] = cursor.getString(cursor.getColumnIndex("operator"));
        slide_txt[6] = cursor.getString(cursor.getColumnIndex("stainingMethod"));
        slide_txt[7] = cursor.getString(cursor.getColumnIndex("hct"));
        slide_txt[8] = cellCountStr;
        slide_txt[9] = infectedCountStr;
        //slide_txt[10] = cursor.getString(cursor.getColumnIndex("parasitemia_thin"));
        slide_txt[11] = cellCountGTStr;
        slide_txt[12] = infectedCountGTStr;

        if (cellTotal == 0 && infectedTotal == 0){
            ParasitaemiaStr = "N/A";
        } else {
            ParasitaemiaStr = cursor.getString(cursor.getColumnIndex("parasitemia_thin"));
        }
        slide_txt[10] = ParasitaemiaStr;

        if ((cellCountGT == 0 && infectedCountGT == 0)) {
            ParasitaemiaGTStr = "N/A";
        } else {
            ParasitaemiaGT = (int) (infectedCountGT * Integer.valueOf(slide_txt[7]) * 125.6);
            ParasitaemiaGTStr = String.valueOf(ParasitaemiaGT) + " Parasites/" + Html.fromHtml("&#956") + "L";
        }
        slide_txt[13] = ParasitaemiaGTStr;

        List<RowItem> rowItems_slide = new ArrayList<RowItem>();

        for (int i = 0; i < slide_item.length; i++) {
            RowItem item = new RowItem(slide_item[i], slide_txt[i]);
            rowItems_slide.add(item);
        }

        CustomAdapter adapter_slide = new CustomAdapter(this, rowItems_slide);
        listView_slide.setAdapter(adapter_slide);

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

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");

        feedListView(patientStr, slideStr);
    }
}
