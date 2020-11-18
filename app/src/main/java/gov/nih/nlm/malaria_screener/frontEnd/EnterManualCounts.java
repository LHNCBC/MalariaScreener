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

import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter_ManualCounts;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsMethods;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

public class EnterManualCounts extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    MyDBHandler dbHandler;
    String[] cell_eachImageGT;
    String[] infected_eachImageGT;

    CustomAdapter_ManualCounts customAdapter_manualCounts;

    String patientStr;
    String slideStr;
    int imageNum;
    ArrayList<String> imageNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_manual_counts);

        Intent intent = getIntent();
        imageNum = intent.getIntExtra("imageNum", 0);
        imageNameList = intent.getStringArrayListExtra("imageNameList");
        patientStr = intent.getStringExtra("itemPID");
        slideStr = intent.getStringExtra("itemSID");

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_manualcounts);
        toolbar.setTitle(R.string.title_manual_counts);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview_manualcounts);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);

        dbHandler = new MyDBHandler(this, null, null, 1);

        readManualCountsfromDB();
        customAdapter_manualCounts = new CustomAdapter_ManualCounts(this, imageNum, cell_eachImageGT, infected_eachImageGT);
        recyclerView.setAdapter(customAdapter_manualCounts);

    }

    private void readManualCountsfromDB() {

        cell_eachImageGT = new String[imageNameList.size()];
        infected_eachImageGT = new String[imageNameList.size()];

        for (int i = 0; i < imageNameList.size(); i++) {

            Cursor cursor = dbHandler.returnSlideImage(patientStr, slideStr, imageNameList.get(i));

            cell_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("cell_count_gt"));
            infected_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("infected_count_gt"));
        }

    }

    public void onBackPressed() {
        saveManualCounts2DB();

        // export and update database file to include info from current slide
        UtilsMethods.exportDB(getApplicationContext());

        finish();

        return;
    }

    public void saveManualCounts2DB() {

        for (int i = 0; i < imageNum; i++) {
            dbHandler.updateImageManulCounts(patientStr, slideStr, imageNameList.get(i), customAdapter_manualCounts.slide_txt[2 * i], customAdapter_manualCounts.slide_txt[2 * i + 1]);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //saveManualCounts2TextFile();

                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
