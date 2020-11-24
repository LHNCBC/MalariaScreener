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

package gov.nih.nlm.malaria_screener.settings;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.Register;

public class DisclaimerActivity extends AppCompatActivity {

    Boolean register_now;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        Bundle extras = getIntent().getExtras();
        register_now = extras.getBoolean("register_now");

        Button acceptButton = findViewById(R.id.button_accept_disclaimer);
        acceptButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent registerIntent = new Intent(getApplicationContext(), Register.class);

                        if (register_now) {
                            registerIntent.putExtra("from_disclaimer", true);
                            startActivity(registerIntent);
                        } else {
                            finish();
                        }

                    }
                }
        );
    }
}
