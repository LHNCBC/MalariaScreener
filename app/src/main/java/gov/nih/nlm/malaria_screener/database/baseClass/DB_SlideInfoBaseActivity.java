package gov.nih.nlm.malaria_screener.database.baseClass;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter;
import gov.nih.nlm.malaria_screener.custom.RowItem;
import gov.nih.nlm.malaria_screener.database.ImageGallery;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

public abstract class DB_SlideInfoBaseActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    public MyDBHandler dbHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_database_slide_page);

        setup_toolbar();

        dbHandler = new MyDBHandler(this, null, null, 1);

    }

    public void setup_toolbar(){

        Toolbar toolbar = findViewById(R.id.navigate_bar_slideDB);
        toolbar.setTitle(R.string.title_slide_infoDB);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    public void feedListView(String patientStr, String slideStr){

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


