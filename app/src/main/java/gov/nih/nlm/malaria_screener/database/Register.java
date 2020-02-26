package gov.nih.nlm.malaria_screener.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Patterns;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import gov.nih.nlm.malaria_screener.MainActivity;
import gov.nih.nlm.malaria_screener.R;

public class Register extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    static final int REQUEST_DROPBOX = 1;
    private final static String DROPBOX_FILE_DIR = "/NLM_Malaria_Screener/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String DROPBOX_REGISTERED = "registered";
    private final static String DROPBOX_DIR = "dropbox_dir";



    private EditText et_firstName, et_lastName, et_email, et_organization, et_department, et_address1, et_address2, et_city, et_zip, et_country;
    private String firstName, lastName, email, organization, department, address1, address2, city, zip, country;

    Button registerButton;

    Boolean fromDisclaimer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_register);
        toolbar.setTitle(R.string.title_register);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        et_firstName = (EditText) findViewById(R.id.editText_firstName);
        et_lastName = (EditText) findViewById(R.id.editText_lastName);
        et_email = (EditText) findViewById(R.id.editText_email);
        et_organization = (EditText) findViewById(R.id.editText_organization);
        et_department = (EditText) findViewById(R.id.editText_department);
        et_address1 = (EditText) findViewById(R.id.editText_address1);
        et_address2 = (EditText) findViewById(R.id.editText_address2);
        et_city = (EditText) findViewById(R.id.editText_city);
        et_zip = (EditText) findViewById(R.id.editText_zip);
        et_country = (EditText) findViewById(R.id.editText_country);


        Bundle extras = getIntent().getExtras();
        fromDisclaimer = extras.getBoolean("from_disclaimer");

        registerButton = (Button) findViewById(R.id.button_register);
        registerButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        register();
                    }
                }
        );

    }

    public void register() {
        initialize();

        if (!validate()) {
            String string = getResources().getString(R.string.register_error);
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show();
        } else { // if all items are correctly filled

            // make sure names' first letters are capital
            firstName = firstName.substring(0, 1).toUpperCase() + firstName.substring(1);
            lastName = lastName.substring(0, 1).toUpperCase() + lastName.substring(1);

            // put a record that the user is registered & the folder name
            SharedPreferences sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(DROPBOX_REGISTERED, true);
            editor.putString(DROPBOX_DIR, firstName + lastName + "_" + email);
            editor.apply();

            OutputTextFile();

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("do_not_show_again_register", true).apply();

            /*Intent dropboxIntent = new Intent(getApplicationContext(), Dropbox.class);
            startActivityForResult(dropboxIntent, REQUEST_DROPBOX);*/

            if (fromDisclaimer){
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // kill all the other activities on top of the old MainActivity.class activity
                startActivity(intent);
            } else {
                finish();
            }
        }
    }

    public boolean validate() {
        boolean valid = true;

        if (firstName.isEmpty() || firstName.length() > 32) {
            String string = getResources().getString(R.string.first_name);
            et_firstName.setError(string);
            valid = false;
        }

        if (lastName.isEmpty() || lastName.length() > 32) {
            String string = getResources().getString(R.string.last_name);
            et_lastName.setError(string);
            valid = false;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            String string = getResources().getString(R.string.email);
            et_email.setError(string);
            valid = false;
        }

        if (organization.isEmpty() || organization.length() > 32) {
            String string = getResources().getString(R.string.organization);
            et_organization.setError(string);
            valid = false;
        }

        if (department.isEmpty() || department.length() > 32) {
            String string = getResources().getString(R.string.department);
            et_department.setError(string);
            valid = false;
        }

        if (address1.isEmpty() || address1.length() > 32) {
            String string = getResources().getString(R.string.address);
            et_address1.setError(string);
            valid = false;
        }

        if (city.isEmpty() || city.length() > 32) {
            String string = getResources().getString(R.string.city);
            et_city.setError(string);
            valid = false;
        }

        if (zip.isEmpty() || zip.length() > 32) {
            String string = getResources().getString(R.string.zip_code);
            et_zip.setError(string);
            valid = false;
        }

        if (country.isEmpty() || country.length() > 32) {
            String string = getResources().getString(R.string.country);
            et_country.setError(string);
            valid = false;
        }

        return valid;
    }

    public void initialize() {

        firstName = et_firstName.getText().toString().trim();
        lastName = et_lastName.getText().toString().trim();
        email = et_email.getText().toString().trim();
        organization = et_organization.getText().toString().trim();
        department = et_department.getText().toString().trim();
        address1 = et_address1.getText().toString().trim();
        address2 = et_address2.getText().toString().trim();
        city = et_city.getText().toString().trim();
        zip = et_zip.getText().toString().trim();
        country = et_country.getText().toString().trim();
    }

    public void OutputTextFile() {

        File textFile = null;

        try {

            textFile = createTextFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textFile != null) {
            FileOutputStream outText = null;

            try {

                outText = new FileOutputStream(textFile, true);

                outText.write(("First Name: " + firstName).getBytes());
                outText.write(("\n").getBytes());
                outText.write(("Last Name: " + lastName).getBytes());
                outText.write(("\n").getBytes());
                outText.write(("Email: " + email).getBytes());
                outText.write(("\n").getBytes());
                outText.write(("Organization: " + organization).getBytes());
                outText.write(("\n").getBytes());
                outText.write(("Department: " + department).getBytes());
                outText.write(("\n").getBytes());
                outText.write(("Address Line 1: " + address1).getBytes());
                outText.write(("\n").getBytes());
                if (address2.isEmpty()){
                    outText.write(("Address Line 2: " +"N/A").getBytes());
                } else {
                    outText.write(("Address Line 2: "+ address2).getBytes());
                }
                outText.write(("\n").getBytes());
                outText.write(("City: " + city).getBytes());
                outText.write(("\n").getBytes());
                outText.write(("Zip Code: " + zip).getBytes());
                outText.write(("\n").getBytes());
                outText.write(("Country: " + country).getBytes());
                outText.write(("\n").getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outText != null) {
                        outText.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private File createTextFile() throws IOException {

        File Dir = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener");

        File imgFile = new File(Dir, "Info" + ".txt");

        if (imgFile.exists()){
            imgFile.delete();
        }

        return imgFile;
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
