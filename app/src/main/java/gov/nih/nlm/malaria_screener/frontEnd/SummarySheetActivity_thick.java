package gov.nih.nlm.malaria_screener.frontEnd;

import android.content.Intent;
import android.os.Environment;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;

import gov.nih.nlm.malaria_screener.MainActivity;
import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsData;
import gov.nih.nlm.malaria_screener.database.Images_thick;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;
import gov.nih.nlm.malaria_screener.database.Patients;
import gov.nih.nlm.malaria_screener.database.Slides;
import gov.nih.nlm.malaria_screener.frontEnd.baseClass.SummarySheetBaseActivity;

public class SummarySheetActivity_thick extends SummarySheetBaseActivity {

    private static final String TAG = "MyDebug";

    private Button finishButton;

    MyDBHandler dbHandler;

    Bundle extras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHandler = new MyDBHandler(this, null, null, 1);

        extras = getIntent().getExtras();

        setStringResources();
        super.setup_listview(extras);

        fillCountArrays();

        finishButton = findViewById(gov.nih.nlm.malaria_screener.R.id.button_finish);
        finishButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {

                        if (newPatient) {
                            Patients patients = new Patients(patientIDStr, genderStr, initialStr, ageStr);
                            dbHandler.addPatient(patients);
                        }

                        if (newSlide) {
                            Slides slides = new Slides(patientIDStr, slideIDStr, dateStr, timeStr, siteStr, preparatorStr, operatorStr, stainingStr, hctStr, "", ParasitaemiaStr);
                            dbHandler.addSlide(slides);
                        }

                        // add images to image table
                        for (int i=0;i<imageName.length;i++) {
                            Images_thick images = new Images_thick(patientIDStr, slideIDStr, imageName[i], count_1[i], count_2[i], countGT_1[i], countGT_2[i]);
                            dbHandler.addImage_thick(images);
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

                        reset_utils_data(); // reset data in UtilsData

                        Intent intent = new Intent(view.getContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // kill all the other activities on top of the old MainActivity.class activity
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    @Override
    public void setStringResources() {
        super.setStringResources();

        patient_item = getResources().getStringArray(R.array.patient_item);
        slide_item = getResources().getStringArray(R.array.slide_item_thick);
        more_item = getResources().getStringArray(R.array.more_item);

        countStr_1 = String.valueOf(UtilsData.WBCTotal);
        countStr_2 = String.valueOf(UtilsData.parasiteTotal);

        ParasitaemiaStr = String.valueOf(UtilsData.parasiteTotal * 40) + " Parasites/" + Html.fromHtml("&#956") + "L";

    }

    private void fillCountArrays() {

        int size = UtilsData.imageNames.size();

        imageName = new String[size];
        count_1 = new String[size];
        count_2 = new String[size];
        countGT_1 = new String[size];
        countGT_2 = new String[size];

        for (int i=0;i<UtilsData.imageNames.size();i++){
            imageName[i] = UtilsData.imageNames.get(i);
            count_1[i] = UtilsData.parasiteCountList.get(i);
            count_2[i] = UtilsData.WBCCountList.get(i);
            countGT_1[i] = UtilsData.parasiteCountList_GT.get(i);
            countGT_2[i] = UtilsData.WBCCountList_GT.get(i);
        }

    }

    private void reset_utils_data(){

        UtilsData.resetImageNames();
        UtilsData.resetCurrentCounts();
        UtilsData.resetTotalCounts();
        UtilsData.resetCountLists();
        UtilsData.resetCountLists_GT();

    }




}
