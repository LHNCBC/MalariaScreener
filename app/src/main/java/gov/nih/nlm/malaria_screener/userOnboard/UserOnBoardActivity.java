package gov.nih.nlm.malaria_screener.userOnboard;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.Register;

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
