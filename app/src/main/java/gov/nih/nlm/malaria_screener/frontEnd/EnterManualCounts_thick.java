package gov.nih.nlm.malaria_screener.frontEnd;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter_ManualCounts_thick;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

public class EnterManualCounts_thick extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    MyDBHandler dbHandler;
    String[] wbc_eachImageGT;
    String[] parasite_eachImageGT;

    CustomAdapter_ManualCounts_thick customAdapter_manualCounts;

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
        customAdapter_manualCounts = new CustomAdapter_ManualCounts_thick(this, imageNum, wbc_eachImageGT, parasite_eachImageGT);
        recyclerView.setAdapter(customAdapter_manualCounts);

    }

    private void readManualCountsfromDB() {

        wbc_eachImageGT = new String[imageNameList.size()];
        parasite_eachImageGT = new String[imageNameList.size()];

        for (int i = 0; i < imageNameList.size(); i++) {

            Cursor cursor = dbHandler.returnSlideImage_thick(patientStr, slideStr, imageNameList.get(i));

            wbc_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("wbc_count_gt"));
            parasite_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("parasite_count_gt"));
        }

    }

    public void onBackPressed() {
        saveManualCounts2DB();

        finish();

        return;
    }

    public void saveManualCounts2DB() {

        for (int i = 0; i < imageNum; i++) {
            dbHandler.updateImageManulCounts_thick(patientStr, slideStr, imageNameList.get(i), customAdapter_manualCounts.slide_txt[2 * i], customAdapter_manualCounts.slide_txt[2 * i + 1]);
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
