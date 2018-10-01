package gov.nih.nlm.malaria_screener.tutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import gov.nih.nlm.malaria_screener.R;

public class Diagram extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagram);

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_diagram);
        toolbar.setTitle(R.string.action_diagram);
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
