package gov.nih.nlm.malaria_screener.userOnboard;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
