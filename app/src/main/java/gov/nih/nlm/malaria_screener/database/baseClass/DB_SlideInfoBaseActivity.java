package gov.nih.nlm.malaria_screener.database.baseClass;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;

import gov.nih.nlm.malaria_screener.R;
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


