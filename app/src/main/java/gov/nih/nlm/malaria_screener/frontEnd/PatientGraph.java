/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

Licensed under GNU General Public License v3.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package gov.nih.nlm.malaria_screener.frontEnd;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
