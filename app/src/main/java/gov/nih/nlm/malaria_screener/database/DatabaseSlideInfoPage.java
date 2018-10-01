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

public class DatabaseSlideInfoPage extends AppCompatActivity {

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

        //allManualCounts = readTxtFile4ManualCounts();

//        if (patientStr.equals("test")) { // added for test folder images
//            feedListView_test(slideStr);
//        } else {
        feedListView(patientStr, slideStr);
        //}

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
        int cellTotal = 0;
        int infectedTotal = 0;
        do {
            cellTotal = cellTotal + Integer.valueOf(cursorImages2.getString(cursorImages2.getColumnIndex("cell_count")));
            infectedTotal = infectedTotal + Integer.valueOf(cursorImages2.getString(cursorImages2.getColumnIndex("infected_count")));

        } while (cursorImages2.moveToNext());

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
        slide_txt[8] = String.valueOf(cellTotal);
        slide_txt[9] = String.valueOf(infectedTotal);
        slide_txt[10] = cursor.getString(cursor.getColumnIndex("parasitemia"));
        slide_txt[11] = cellCountGTStr;
        slide_txt[12] = infectedCountGTStr;

        if ((cellCountGT == 0 && infectedCountGT == 0) || patientStr.equals("test")) {
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

    /*private void feedListView_test(String slideStr) {

        String cellCountStr;
        String infectedCountStr;
        int Parasitaemia;
        int cellCount = 0;
        int infectedCount = 0;
        String ParasitaemiaStr;

        for (int i=0;i<allManualCounts.length/2;i++){
            Log.d(TAG, "allManualCounts: " + allManualCounts[i]);
            if (allManualCounts[2*i].equals("N/A") || allManualCounts[2*i+1].equals("N/A")){
                cellCount = 0;
                infectedCount = 0;
                break;
            } else{
                cellCount = cellCount + Integer.valueOf(allManualCounts[2*i]);
                infectedCount = infectedCount + Integer.valueOf(allManualCounts[2*i+1]);
            }

        }

        if (cellCount == 0 && infectedCount == 0){
            cellCountStr = "N/A";
            infectedCountStr = "N/A";

        } else {
            cellCountStr = String.valueOf(cellCount);
            infectedCountStr = String.valueOf(infectedCount);
        }

        slide_txt = new String[14];
        slide_txt[0] = slideStr;
        slide_txt[1] = "N/A";
        slide_txt[2] = "N/A";
        slide_txt[3] = "N/A";
        slide_txt[4] = "N/A";
        slide_txt[5] = "N/A";
        slide_txt[6] = "N/A";
        slide_txt[7] = "N/A";
        slide_txt[8] = "N/A";
        slide_txt[9] = "N/A";
        slide_txt[10] = "N/A";
        slide_txt[11] = "N/A";
        slide_txt[12] = "N/A";
        slide_txt[13] = "N/A";

        for (int i = 0; i < slide_item.length; i++) {
            RowItem item = new RowItem(slide_item[i], slide_txt[i]);
            rowItems_slide.add(item);
        }

        CustomAdapter adapter_slide = new CustomAdapter(this, rowItems_slide);
        listView_slide.setAdapter(adapter_slide);

    }*/

    /*private String[] readTxtFile4ManualCounts() {

        // Get image path
        File slideDir = null;
        if (patientStr.equals("test")) { // added for test folder images
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/Test/" + slideStr);
        } else {
            slideDir = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/" + patientStr + "_" + slideStr);
        }

        File[] allImageListing = slideDir.listFiles(); // list all images in this slide directory

        int imageNum = 0;
        for (int i = 0; i < allImageListing.length; i++) {

            String imagePath = allImageListing[i].getAbsolutePath();

            if ((imagePath.indexOf("result") == -1) && (imagePath.indexOf("mask") == -1) && (imagePath.contains("png"))) { // pick out the original image by checking the image name
                imageNum++;
            }

        }

        String[] allCounts = new String[imageNum*2];
        Arrays.fill(allCounts, "N/A");

        if (slideDir.exists()) {

            // get all original image file names
            String[] imageName = new String[imageNum];
            int index = 0;
            for (int i = 0; i < allImageListing.length; i++) {
                String imagePath = allImageListing[i].getAbsolutePath();

                if ((imagePath.indexOf("result") == -1) && (imagePath.indexOf("mask") == -1) && (imagePath.indexOf("png") != -1)) {
                    imageName[index] = imagePath.substring(imagePath.lastIndexOf("/")+1, imagePath.lastIndexOf("."));

                    index++;
                }

            }

            for (int i=0; i<imageName.length; i++){

                for (int j = 0; j < allImageListing.length; j++) {
                    String imagePath = allImageListing[j].getAbsolutePath();
                    String fileName = imagePath.substring(imagePath.lastIndexOf("/")+1);
                    String imageNameTemp = imagePath.substring(imagePath.lastIndexOf("/")+1, imagePath.lastIndexOf("."));
                    if (imageNameTemp.equals(imageName[i]) && fileName.contains("txt")){
                        File file = new File(imagePath);
                        try {
                            BufferedReader br = new BufferedReader(new FileReader(file));
                            String line = br.readLine();
                            String[] eachItem = line.split(" ");
                            allCounts[2*i] = eachItem[1];
                            allCounts[2*i+1] = eachItem[2];

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
