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

import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter;
import gov.nih.nlm.malaria_screener.custom.CustomAdapterBold;
import gov.nih.nlm.malaria_screener.custom.RowItem;
import gov.nih.nlm.malaria_screener.uploadFunction.UploadSessionManager;

public abstract class SummarySheetBaseActivity extends AppCompatActivity{

    private static final String TAG = "MyDebug";

    ListView listView_patient;
    ListView listView_slide;
    ListView listView_more;

    public String[] patient_item;
    String[] patient_txt;

    public String[] slide_item;
    String[] slide_txt;

    public String[] more_item;
    String[] more_txt;

    List<RowItem> rowItems_p = new ArrayList<>();
    List<RowItem> rowItems_s = new ArrayList<>();
    List<RowItem> rowItems_m = new ArrayList<>();

    public String patientIDStr;
    public String initialStr;
    public String genderStr;
    public String ageStr;
    public String countStr_1;
    public String countStr_2;
    public String slideIDStr;
    public String dateStr;
    public String timeStr;
    public String siteStr;
    public String preparatorStr;
    public String operatorStr;
    public String stainingStr;
    public String hctStr;
    public String ParasitaemiaStr;

    public String imageName[];
    public String count_1[];
    public String count_2[];
    public String countGT_1[];
    public String countGT_2[];

    public boolean newPatient = false;
    public boolean newSlide = false;

    public ArrayList<String > imageNameList = new ArrayList<>();
    public ArrayList<String> folderNameList = new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_summary_sheet);

        setup_toolbar();

    }

    public void setup_toolbar(){

        Toolbar toolbar = (Toolbar) findViewById(gov.nih.nlm.malaria_screener.R.id.navigate_bar_summary);
        toolbar.setTitle(gov.nih.nlm.malaria_screener.R.string.title_summary);
        toolbar.setTitleTextColor(getResources().getColor(gov.nih.nlm.malaria_screener.R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    // this method will vary depends on the smear type
    public void setStringResources(){
        countStr_1 = "";
        countStr_2 = "";
        ParasitaemiaStr = "";
    }

    public void setup_listview(Bundle extras){

        patientIDStr = extras.getString("patientID");
        initialStr = extras.getString("initial");
        genderStr = extras.getString("gender");
        ageStr = extras.getString("age");

        slideIDStr = extras.getString("slideID");
        dateStr = extras.getString("date");
        timeStr = extras.getString("time");
        siteStr = extras.getString("site");
        preparatorStr = extras.getString("preparator");
        operatorStr = extras.getString("operator");
        stainingStr = extras.getString("staining");
        hctStr = extras.getString("hct");

        newPatient = Boolean.valueOf(extras.getString("newPatient"));
        newSlide = Boolean.valueOf(extras.getString("newSlide"));

        //patient
        patient_txt = new String[4];
        patient_txt[0] = patientIDStr;
        patient_txt[1] = initialStr;
        if (genderStr.equals("male")) {
            String string = getResources().getString(R.string.male);
            patient_txt[2] = string;
        } else {
            String string = getResources().getString(R.string.female);
            patient_txt[2] = string;
        }
        patient_txt[3] = ageStr;

        for (int i = 0; i < patient_item.length; i++) {
            RowItem item = new RowItem(patient_item[i], patient_txt[i]);
            rowItems_p.add(item);
        }

        listView_patient = (ListView) findViewById(gov.nih.nlm.malaria_screener.R.id.listView_patient);
        CustomAdapter adapter_p = new CustomAdapter(this, rowItems_p);
        listView_patient.setAdapter(adapter_p);

        // slide
        slide_txt = new String[4];
        slide_txt[0] = extras.getString("slide_result");
        slide_txt[1] = countStr_1;
        slide_txt[2] = countStr_2;
        slide_txt[3] = ParasitaemiaStr;

        for (int i = 0; i < slide_item.length; i++) {
            RowItem item = new RowItem(slide_item[i], slide_txt[i]);
            rowItems_s.add(item);
        }

        listView_slide = (ListView) findViewById(gov.nih.nlm.malaria_screener.R.id.listView_slide);
        CustomAdapterBold adapter_s = new CustomAdapterBold(this, rowItems_s);
        listView_slide.setAdapter(adapter_s);

        //more
        more_txt = new String[8];
        more_txt[0] = slideIDStr;
        more_txt[1] = dateStr;
        more_txt[2] = timeStr;
        more_txt[3] = siteStr;
        more_txt[4] = preparatorStr;
        more_txt[5] = operatorStr;
        more_txt[6] = stainingStr;
        more_txt[7] = hctStr;

        for (int i = 0; i < more_item.length; i++) {
            RowItem item = new RowItem(more_item[i], more_txt[i]);
            rowItems_m.add(item);
        }

        listView_more = (ListView) findViewById(gov.nih.nlm.malaria_screener.R.id.listView_more);
        CustomAdapter adapter_m = new CustomAdapter(this, rowItems_m);
        listView_more.setAdapter(adapter_m);
    }

    public void deleteImagesInSlide(String PID, String SID) {

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener");

        File[] folderList = file.listFiles(); // list all folder with "PID_"

        if (folderList != null) {
            int length = folderList.length;

            // delete files
            for (int i = 0; i < length; i++) {

                if (folderList[i].getAbsolutePath().toString().contains(PID + "_" + SID)) {

                    // delete files
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

    public void prepare_and_upload(String[] imageName, String patientIDStr, String slideIDStr){

        for (int i=0;i<imageName.length;i++) {
            imageNameList.add(imageName[i]);
            folderNameList.add(patientIDStr + "_" + slideIDStr);
        }

        UploadSessionManager uploadSessionManager = new UploadSessionManager();
        uploadSessionManager.authenticate(getApplicationContext(), imageNameList, folderNameList);
    }






}
