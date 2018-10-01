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

    /*private String[] readTxtFile4ManualCounts() {

        File slideDir;
        if (patientStr.equals("test")) { // added for test folder images
            // Get image path
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/Test/" + slideStr);
        } else {
            // Get image path
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/" + patientStr + "_" + slideStr);
        }

        String[] allCounts = new String[imageNum * 2];
        Arrays.fill(allCounts, "N/A");

        if (slideDir.exists()) {

            File[] allImageListing = slideDir.listFiles(); // list all images in this slide directory

            // get all original image file names
            String[] imageName = new String[imageNum];
            int index = 0;
            for (int i = 0; i < allImageListing.length; i++) {
                String imagePath = allImageListing[i].getAbsolutePath();

                if ((imagePath.indexOf("result") == -1) && (imagePath.indexOf("mask") == -1) && (imagePath.indexOf("png") != -1)) {
                    imageName[index] = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf("."));
                    //Log.d(TAG, "ImageName:" + imageName[index] + " " +index);
                    index++;
                }

            }

            for (int i = 0; i < imageName.length; i++) {

                for (int j = 0; j < allImageListing.length; j++) {
                    String imagePath = allImageListing[j].getAbsolutePath();
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
                    String imageNameTemp = imagePath.substring(imagePath.lastIndexOf("/") + 1, imagePath.lastIndexOf("."));
                    if (imageNameTemp.equals(imageName[i]) && fileName.contains("txt")) {
                        File file = new File(imagePath);
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line = br.readLine();
                            String[] eachItem = line.split(" ");
                            allCounts[2 * i] = eachItem[1];
                            allCounts[2 * i + 1] = eachItem[2];

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            return allCounts;

        }

        return allCounts;
    }*/

    /*public void saveManualCounts2TextFile() {

        File slideDir;
        // Get image path
        if (patientStr.equals("test")) { // added for test folder images
            // Get image path
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/Test/" + slideStr);
        } else {
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/" + patientStr + "_" + slideStr);
        }

        ArrayList<String> originalImageName = new ArrayList<>();

        if (slideDir.exists()) {

            File[] allImageListing = slideDir.listFiles(); // list all images in this slide directory

            for (int i = 0; i < allImageListing.length; i++) {

                String imagePath = allImageListing[i].getAbsolutePath();

                if ((imagePath.indexOf("result") == -1) && (imagePath.indexOf("mask") == -1) && (imagePath.contains("png"))) { // pick out the original image by checking the image name
                    int startIndex = imagePath.lastIndexOf("/") + 1;
                    int endIndex = imagePath.lastIndexOf(".");
                    String imageNameStr = imagePath.substring(startIndex, endIndex);
                    originalImageName.add(imageNameStr);
                }

            }

        }

        for (int i = 0; i < originalImageName.size(); i++) {

            File textFile = null;

            try {

                textFile = createTextFile(originalImageName.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (textFile != null) {
                FileOutputStream outText = null;

                try {

                    if (textFile.length() == 0) {

                        outText = new FileOutputStream(textFile, true);

                        outText.write((originalImageName.get(i) + ".png " + customAdapter_manualCounts.slide_txt[2 * i] + " " + customAdapter_manualCounts.slide_txt[2 * i + 1]).getBytes());
                    } else {

                        outText = new FileOutputStream(textFile, false);

                        outText.write((originalImageName.get(i) + ".png " + customAdapter_manualCounts.slide_txt[2 * i] + " " + customAdapter_manualCounts.slide_txt[2 * i + 1]).getBytes());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outText != null) {
                            outText.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    private File createTextFile(String imagename) throws IOException {

        File Dir;
        // Get image path
        if (patientStr.equals("test")) { // added for test folder images
            // Get image path
            Dir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/Test/" + slideStr);
        } else {
            Dir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/" + patientStr + "_" + slideStr);
        }

        File imgFile = new File(Dir, imagename + ".txt");

        return imgFile;
    }*/

    public void onBackPressed() {
        saveManualCounts2DB();

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
