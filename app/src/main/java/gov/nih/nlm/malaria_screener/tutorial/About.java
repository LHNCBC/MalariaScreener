package gov.nih.nlm.malaria_screener.tutorial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

/**
 * Created by yuh5 on 12/2/2016.
 */
public class About extends AppCompatActivity{

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(gov.nih.nlm.malaria_screener.R.id.navigate_bar_about);
        toolbar.setTitle(gov.nih.nlm.malaria_screener.R.string.title_about);
        toolbar.setTitleTextColor(getResources().getColor(gov.nih.nlm.malaria_screener.R.color.toolbar_title));
        setSupportActionBar(toolbar);
        // set home button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
