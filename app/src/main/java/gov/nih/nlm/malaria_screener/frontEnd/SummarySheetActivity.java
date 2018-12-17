package gov.nih.nlm.malaria_screener.frontEnd;

import android.content.Intent;
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

import gov.nih.nlm.malaria_screener.MainActivity;
import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter;
import gov.nih.nlm.malaria_screener.custom.CustomAdapterBold;
import gov.nih.nlm.malaria_screener.custom.RowItem;
import gov.nih.nlm.malaria_screener.database.Images;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;
import gov.nih.nlm.malaria_screener.database.Patients;
import gov.nih.nlm.malaria_screener.database.Slides;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SummarySheetActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    ListView listView_patient;
    ListView listView_slide;
    ListView listView_more;

    String[] patient_item;
    String[] patient_txt;

    String[] slide_item;
    String[] slide_txt;

    String[] more_item;
    String[] more_txt;

    List<RowItem> rowItems_p;
    List<RowItem> rowItems_s;
    List<RowItem> rowItems_m;

    private Button finishButton;

    MyDBHandler dbHandler;

    String patientIDStr;
    String initialStr;
    String genderStr;
    String ageStr;
    String cellCountStr;
    String infectedNumStr;
    String slideIDStr;
    String dateStr;
    String timeStr;
    String siteStr;
    String preparatorStr;
    String operatorStr;
    String stainingStr;

    String hctStr;
    String ParasitaemiaStr = null;
    String name_eachImageStr;
    String cell_eachImgStr;
    String infected_eachImgStr;
    String cell_eachImgGTStr;
    String infected_eachImgGTStr;

    boolean newPatient = false;
    boolean newSlide = false;

    String imageName[];
    String cellCount[];
    String infectedCount[];
    String cellCountGT[];
    String infectedCountGT[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_summary_sheet);

        Toolbar toolbar = (Toolbar) findViewById(gov.nih.nlm.malaria_screener.R.id.navigate_bar_summary);
        toolbar.setTitle(gov.nih.nlm.malaria_screener.R.string.title_summary);
        toolbar.setTitleTextColor(getResources().getColor(gov.nih.nlm.malaria_screener.R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbHandler = new MyDBHandler(this, null, null, 1);

        patient_item = getResources().getStringArray(R.array.patient_item);
        slide_item = getResources().getStringArray(R.array.slide_item);
        more_item = getResources().getStringArray(R.array.more_item);

        rowItems_p = new ArrayList<RowItem>();
        rowItems_s = new ArrayList<RowItem>();
        rowItems_m = new ArrayList<RowItem>();

        final Bundle extras = getIntent().getExtras();
        patientIDStr = extras.getString("patientID");
        initialStr = extras.getString("initial");
        genderStr = extras.getString("gender");
        ageStr = extras.getString("age");
        name_eachImageStr = extras.getString("nameStringEachImage");
        cell_eachImgStr = extras.getString("cellCountEachImage");
        infected_eachImgStr = extras.getString("infectedCountEachImage");
        cell_eachImgGTStr = extras.getString("cellCountEachImageGT");
        infected_eachImgGTStr = extras.getString("infectedCountEachImageGT");
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

        imageName = name_eachImageStr.split(",");
        cellCount = cell_eachImgStr.split(",");
        infectedCount = infected_eachImgStr.split(",");
        cellCountGT = cell_eachImgGTStr.split(",");
        infectedCountGT = infected_eachImgGTStr.split(",");

        int cellTotal = 0;
        int infectedTotal = 0;
        for (int i = 0; i < imageName.length; i++) {

            cellTotal = cellTotal + Integer.valueOf(cellCount[i]);
            infectedTotal = infectedTotal + Integer.valueOf(infectedCount[i]);
        }

        cellCountStr = String.valueOf(cellTotal);
        infectedNumStr = String.valueOf(infectedTotal);

        double infectedNum = Double.valueOf(infectedNumStr);

        double Hct = Double.valueOf(hctStr);
        ParasitaemiaStr = String.valueOf((int) (infectedNum * Hct * 125.6)) + " Parasites/" + Html.fromHtml("&#956") + "L";


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
        slide_txt = new String[3];
        slide_txt[0] = cellCountStr;
        slide_txt[1] = infectedNumStr;
        slide_txt[2] = ParasitaemiaStr;

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

        finishButton = (Button) findViewById(gov.nih.nlm.malaria_screener.R.id.button_finish);
        finishButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {

                        //dbHandler.addSlideResults(slideIDStr, cellCountStr, infectedNumStr, ParasitaemiaStr);

                        if (newPatient) {
                            Patients patients = new Patients(patientIDStr, genderStr, initialStr, ageStr);
                            dbHandler.addPatient(patients);
                        }

                        if (newSlide) {
                            Slides slides = new Slides(patientIDStr, slideIDStr, dateStr, timeStr, siteStr, preparatorStr, operatorStr, stainingStr, hctStr, ParasitaemiaStr, "");
                            dbHandler.addSlide(slides);
                        }

                        // add images to image table
                        for (int i=0;i<imageName.length;i++) {
                            Images images = new Images(patientIDStr, slideIDStr, imageName[i], cellCount[i], infectedCount[i], cellCountGT[i], infectedCountGT[i]);
                            dbHandler.addImage(images);
                        }

                        // change directory name
                        File oldFile = new File(Environment.getExternalStorageDirectory(
                        ), "NLM_Malaria_Screener/New");

                        File newFile = new File(Environment.getExternalStorageDirectory(
                        ), "NLM_Malaria_Screener/" + patientIDStr + "_" + slideIDStr);

                        // delete if there is already a folder with the same name there
                        deleteImagesInSlide(patientIDStr, slideIDStr);

                        if (oldFile.exists()) {
                            boolean success = oldFile.renameTo(newFile);
                        }

                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // kill all the other activities on top of the old MainActivity.class activity
                        startActivity(intent);
                        finish();
                    }
                }
        );

    }

    private void deleteImagesInSlide(String PID, String SID) {

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
}
