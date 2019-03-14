package gov.nih.nlm.malaria_screener.database;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter;
import gov.nih.nlm.malaria_screener.custom.RowItem;
import gov.nih.nlm.malaria_screener.database.baseClass.DB_SlideInfoBaseActivity;

public class DB_SlideInfoActivity_thick extends DB_SlideInfoBaseActivity {

    private static final String TAG = "MyDebug";

    private Button imageGalleryButton;

    private String[] slide_item;
    private String[] slide_txt;

    private ListView listView_slide;

    private String patientStr;
    private String slideStr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listView_slide = findViewById(R.id.listView_slide);

        Bundle extras = getIntent().getExtras();
        patientStr = extras.getString("itemPID");
        slideStr = extras.getString("itemSID");

        imageGalleryButton = findViewById(R.id.button_imagegallery);
        imageGalleryButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        Intent ImageViewIntent = new Intent(getApplicationContext(), ImageGalleryActivity_thick.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("itemPID", patientStr);
                        bundle.putString("itemSID", slideStr);

                        ImageViewIntent.putExtras(bundle);
                        startActivity(ImageViewIntent);

                    }
                }
        );

        feedListView(patientStr, slideStr);

    }

    @Override
    public void feedListView(String patientStr, String slideStr) {
        super.feedListView(patientStr, slideStr);

        // get manual counts from database of each image and then add up together
        Cursor cursorImages1 = dbHandler.returnAllSlideImages_thick(patientStr, slideStr);

        String WBCCountGTStr;
        String parasiteCountGTStr;

        int wbcCountGT = 0;
        int parasiteCountGT = 0;
        int ParasitaemiaGT;
        String ParasitaemiaGTStr;

        do {
            String wbcCountGTTemp, parasiteCountGTTemp;
            wbcCountGTTemp = cursorImages1.getString(cursorImages1.getColumnIndex("wbc_count_gt"));
            parasiteCountGTTemp = cursorImages1.getString(cursorImages1.getColumnIndex("parasite_count_gt"));

            Log.d(TAG, "wbcCountGTTemp:" + wbcCountGTTemp);
            Log.d(TAG, "parasiteCountGTTemp:" + parasiteCountGTTemp);

            if (wbcCountGTTemp.equals("N/A") || parasiteCountGTTemp.equals("N/A")) {
                wbcCountGT = 0;
                parasiteCountGT = 0;
                break;
            } else {
                wbcCountGT = wbcCountGT + Integer.valueOf(wbcCountGTTemp);
                parasiteCountGT = parasiteCountGT + Integer.valueOf(parasiteCountGTTemp);
            }

        } while (cursorImages1.moveToNext());

        if (wbcCountGT == 0 && parasiteCountGT == 0) {
            WBCCountGTStr = "N/A";
            parasiteCountGTStr = "N/A";

        } else {
            WBCCountGTStr = String.valueOf(wbcCountGT);
            parasiteCountGTStr = String.valueOf(parasiteCountGT);
        }

        // get count of each image and then add up together
        Cursor cursorImages2 = dbHandler.returnAllSlideImages_thick(patientStr, slideStr);

        String WBCCountStr;
        String parasiteCountStr;

        int wbcTotal = 0;
        int parasiteTotal = 0;
        String ParasitaemiaStr;

        do {
            String wbcCountTemp, parasiteCountTemp;
            wbcCountTemp = cursorImages2.getString(cursorImages2.getColumnIndex("wbc_count"));
            parasiteCountTemp = cursorImages2.getString(cursorImages2.getColumnIndex("parasite_count"));

            if (wbcCountTemp.equals("N/A") || parasiteCountTemp.equals("N/A")){
                wbcTotal = 0;
                parasiteTotal = 0;
                break;
            }

            wbcTotal = wbcTotal + Integer.valueOf(wbcCountTemp);
            parasiteTotal = parasiteTotal + Integer.valueOf(parasiteCountTemp);

        } while (cursorImages2.moveToNext());

        if (wbcTotal == 0 && parasiteTotal == 0) {
            WBCCountStr = "N/A";
            parasiteCountStr = "N/A";

        } else {
            WBCCountStr = String.valueOf(wbcTotal);
            parasiteCountStr = String.valueOf(parasiteTotal);
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
        slide_txt[8] = WBCCountStr;
        slide_txt[9] = parasiteCountStr;
        //slide_txt[10] = cursor.getString(cursor.getColumnIndex("parasitemia_thick"));
        slide_txt[11] = WBCCountGTStr;
        slide_txt[12] = parasiteCountGTStr;

        if (wbcTotal == 0 && parasiteTotal == 0){
            ParasitaemiaStr = "N/A";
        } else {
            ParasitaemiaStr = cursor.getString(cursor.getColumnIndex("parasitemia_thick"));
        }
        slide_txt[10] = ParasitaemiaStr;

        if ((wbcCountGT == 0 && parasiteCountGT == 0) || patientStr.equals("test")) {
            ParasitaemiaGTStr = "N/A";
        } else {
            ParasitaemiaGT = (int) (parasiteCountGT * 40);
            ParasitaemiaGTStr = String.valueOf(ParasitaemiaGT) + " Parasites/" + Html.fromHtml("&#956") + "L";
        }
        slide_txt[13] = ParasitaemiaGTStr;

        List<RowItem> rowItems_slide = new ArrayList<RowItem>();

        slide_item = getResources().getStringArray(R.array.slide_item_db_thick);

        for (int i = 0; i < slide_item.length; i++) {
            RowItem item = new RowItem(slide_item[i], slide_txt[i]);
            rowItems_slide.add(item);
        }

        CustomAdapter adapter_slide = new CustomAdapter(this, rowItems_slide);
        listView_slide.setAdapter(adapter_slide);

    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");

        feedListView(patientStr, slideStr);
    }

}
