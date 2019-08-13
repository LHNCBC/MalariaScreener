package gov.nih.nlm.malaria_screener.frontEnd;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;

import gov.nih.nlm.malaria_screener.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.custom.VerticalTextView;

public class PatientGraph extends AppCompatActivity {

    LineGraphSeries<DataPoint> series;

    GraphView graphView;
    VerticalTextView verticalTextView;

    ArrayList<String> parasitemiaList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_graph);

        Bundle extras = getIntent().getExtras();

        parasitemiaList = extras.getStringArrayList("parasitemiaList");

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_graph);
        toolbar.setTitle(R.string.title_patient_graph);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        verticalTextView = (VerticalTextView) findViewById(R.id.textView_YAxisTitle);
        verticalTextView.setText("Parasitemia( per " + Html.fromHtml("&#956") + "L )");
        graphView = (GraphView) findViewById(R.id.graphView);

        // draw graph
        series = new LineGraphSeries<>();
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(7);
        series.setThickness(5);

        double x, y;

        for (int i = 0; i < parasitemiaList.size(); i++) {

            x = i + 1;
            String digits = parasitemiaList.get(i).replaceAll("[^0-9]", "");
            y = Double.parseDouble(digits);
            series.appendData(new DataPoint(x, y), true, parasitemiaList.size());

        }

        graphView.addSeries(series);

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
