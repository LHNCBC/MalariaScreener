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
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import gov.nih.nlm.malaria_screener.R;

public class UserOnBoardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_on_board);

        Button registerButton = findViewById(R.id.button_register);
        registerButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent disclaimerIntent = new Intent(getApplicationContext(), DisclaimerActivity.class);
                        disclaimerIntent.putExtra("register_now", true);
                        startActivity(disclaimerIntent);

                        finish();
                    }
                }
        );

        Button skipButton = findViewById(R.id.button_skip);
        skipButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        Intent disclaimerIntent = new Intent(getApplicationContext(), DisclaimerActivity.class);
                        disclaimerIntent.putExtra("register_now", false);
                        startActivity(disclaimerIntent);

                        finish();
                    }
                }
        );

        CheckBox checkBox_doNotShowAgain = findViewById(R.id.checkBox_doNotShowAgain);
        checkBox_doNotShowAgain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (isChecked) {
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean("do_not_show_again_register", true).apply();
                }
            }
        });

    }
}
