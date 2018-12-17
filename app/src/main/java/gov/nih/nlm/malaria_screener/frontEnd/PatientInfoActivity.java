package gov.nih.nlm.malaria_screener.frontEnd;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import gov.nih.nlm.malaria_screener.MainActivity;
import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.Images;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;
import gov.nih.nlm.malaria_screener.database.Patients;
import gov.nih.nlm.malaria_screener.database.Slides;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatientInfoActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private static final String TAG = "MyDebug";

    private TextInputLayout pIDLayout;
    private TextInputLayout initialLayout;
    private TextInputLayout ageLayout;

    private TextView genderTextView;

    private EditText patientID;
    private EditText initials;
    private EditText age;
    private CheckBox checkBox_male;
    private CheckBox checkBox_female;
    private String genderStr = "";
    //private Button continueButton;
    MyDBHandler dbHandler;
    //private boolean fromDB = false;

    Bundle bundle;

    SearchView searchView;

    String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_info);

        Intent in = getIntent();
        bundle = in.getExtras();

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar);
        toolbar.setTitle(R.string.title_patient_info);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        // set home button
        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pIDLayout = (TextInputLayout) findViewById(R.id.layout_patientID);
        initialLayout = (TextInputLayout) findViewById(R.id.layout_initial);
        ageLayout = (TextInputLayout) findViewById(R.id.layout_age);
        genderTextView = (TextView) findViewById(R.id.textView_genderString);

        dbHandler = new MyDBHandler(this, null, null, 1);

        checkBox_male = (CheckBox) findViewById(R.id.checkBox_male);

        checkBox_male.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (isChecked) {
                    genderTextView.setText(R.string.gender1);
                    checkBox_female.setChecked(false);
                    genderStr = "male";
                } else {
                    genderStr = "";
                }
            }
        });

        checkBox_female = (CheckBox) findViewById(R.id.checkBox_female);
        checkBox_female.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked
                if (isChecked) {
                    genderTextView.setText(R.string.gender1);
                    checkBox_male.setChecked(false);
                    genderStr = "female";
                } else {
                    genderStr = "";
                }
            }
        });

        patientID = (EditText) findViewById(R.id.editText_patientID);
        initials = (EditText) findViewById(R.id.editText_initials);
        age = (EditText) findViewById(R.id.editText_age);

        patientID.addTextChangedListener(watcher);
        initials.addTextChangedListener(initialWatcher);
        age.addTextChangedListener(watcher);

        //cursorAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.list_item_search, null, new String[]{SearchManager.SUGGEST_COLUMN_TEXT_1}, new int[] {R.id.textView_search}, 0);

        //listView.setAdapter(cursorAdapter);

    }

    private void nextpageclicked() {

        Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
        Matcher ms = ps.matcher(initials.getText().toString());
        boolean bs = ms.matches();

        if (patientID.getText().toString().isEmpty() || initials.getText().toString().isEmpty() || age.getText().toString().isEmpty() || genderStr.isEmpty() || bs == false) {

            if (patientID.getText().toString().isEmpty()) {
                pIDLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.patient_id_empty);
                pIDLayout.setError(string);
            }
            if (initials.getText().toString().isEmpty()) {
                initialLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.initial_empty);
                initialLayout.setError(string);
            }
            if (age.getText().toString().isEmpty()) {
                ageLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.age_empty);
                ageLayout.setError(string);
            }
            if (genderStr.isEmpty()) {
                genderTextView.setText(R.string.gender);
            }

        } else {

            // check patient ID to see if this patient is already in the database
            if (!dbHandler.checkExist_Patient(patientID.getText().toString())) { // not exists in the database
                // add patient info into database
                //String pIDStr = patientID.getText().toString();
                //String initialStr = initials.getText().toString();
                //String ageStr = age.getText().toString();

                //Patients patients = new Patients(pIDStr, genderStr, initialStr, ageStr);
                //dbHandler.addPatient(patients);

                Intent intent = new Intent(getApplicationContext(), SlideInfoActivity.class);
                bundle.putString("patientID", patientID.getText().toString());
                bundle.putString("initial", initials.getText().toString().toUpperCase());
                bundle.putString("gender", genderStr);
                bundle.putString("age", age.getText().toString());
                bundle.putString("newPatient", "true");
                intent.putExtras(bundle);
                startActivity(intent);
            } else { // exists in the database

                boolean ifSame = dbHandler.checkIfSame_Patient(patientID.getText().toString(), genderStr, initials.getText().toString(), age.getText().toString());

                if (ifSame) {
                    Intent intent = new Intent(getApplicationContext(), SlideInfoActivity.class);
                    bundle.putString("patientID", patientID.getText().toString());
                    bundle.putString("initial", initials.getText().toString().toUpperCase());
                    bundle.putString("gender", genderStr);
                    bundle.putString("age", age.getText().toString());
                    intent.putExtras(bundle);
                    startActivity(intent);

                } else {
                    String string = getResources().getString(R.string.pid_exist);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
                }
            }


        }

    }

    private TextWatcher initialWatcher = new TextWatcher() {

        boolean bs;

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int length, int count) {
            Pattern ps = Pattern.compile("^[a-zA-Z ]+$");
            Matcher ms = ps.matcher(initials.getText().toString());
            bs = ms.matches();

            if (bs == false && initials.getText().toString().isEmpty() == false) {
                initialLayout.setErrorEnabled(true);
                String string = getResources().getString(R.string.initial_are_letters);
                initialLayout.setError(string);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {

            if ((!initials.getText().toString().isEmpty() && bs) || initials.getText().toString().isEmpty()) {
                //initialLayout.setErrorEnabled(false);
                initialLayout.setError(null);
            }

        }
    };

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            if (!patientID.getText().toString().isEmpty()) {
                //pIDLayout.setErrorEnabled(false);
                pIDLayout.setError(null);
            }
            if (!age.getText().toString().isEmpty()) {
                //ageLayout.setErrorEnabled(false);
                ageLayout.setError(null);
            }

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                onBackPressed();
                return true;

            case R.id.nextPage:
                nextpageclicked();
                return true;

            case R.id.skip:
                onSkipPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        searchView.requestFocus();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {

        Cursor cursor = dbHandler.findPatient(s);

        if (cursor.moveToFirst()) {

            patientID.setText(cursor.getString(1));
            initials.setText(cursor.getString(3));
            age.setText(cursor.getString(4));
            genderStr = cursor.getString(2);


            if (genderStr.equals("male")) {
                checkBox_male.setChecked(true);
                checkBox_female.setChecked(false);
            } else {
                checkBox_male.setChecked(false);
                checkBox_female.setChecked(true);
            }

        } else {
            String string = getResources().getString(R.string.no_resultDB);
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {


        return false;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    public void onBackPressed() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.abort);

        alertDialog.setIcon(R.drawable.warning);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.quit_session_message);

        // Setting Positive "Yes" Button
        String string = getResources().getString(R.string.yes);
        alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // kill all the other activities on top of the old MainActivity.class activity
                startActivity(intent);

                finish();

                // Write your code here to invoke YES event
                String string = getResources().getString(R.string.quit_session_aborted);
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        });

        // Setting Negative "NO" Button
        String string1 = getResources().getString(R.string.no);
        alertDialog.setNegativeButton(string1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // Write your code here to invoke NO event
                String string = getResources().getString(R.string.click_no);
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();

        return;
    }

    /*private void onSkipPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.test);

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();

                // Write your code here to invoke YES event
                Toast.makeText(getApplicationContext(), R.string.skip_moved, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // kill all the other activities on top of the old MainActivity.class activity
                startActivity(intent);
                finish();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }*/

    private void onSkipPressed() {

        // Move files to Extra & clear New folder
        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/New");

        if (file.listFiles() != null) {

            final File[] list = file.listFiles();
            final int length = file.listFiles().length;

            if (length > 0) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final AlertDialog dialog;
                builder.setTitle(R.string.test);

                // Set up the input
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

//                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//
//                // Setting Dialog Title
//                alertDialog.setTitle(R.string.skip_title);
//
//                // Setting Dialog Message
//                alertDialog.setMessage(R.string.skip_message);

                // Set up the buttons
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog = builder.create();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {

                        if (input.getText().toString().isEmpty()) {

                            Toast.makeText(getApplicationContext(), R.string.slide_id_empty, Toast.LENGTH_SHORT).show();


                        } else { // not empty input from user

                            m_Text = input.getText().toString();

                            if (!dbHandler.checkExist_Slide("test", m_Text)) {  // no such test slide before
                                // delete if there is already a folder with the same Slide ID there
                                deleteImagesInSlide("test", m_Text);
                            }

                            File file_extras = new File(Environment.getExternalStorageDirectory(
                            ), "NLM_Malaria_Screener/Test/" + m_Text);

                            if (!file_extras.exists()) {
                                if (!file_extras.mkdirs()) {
                                    Log.d("MalariaPics", "failed to create directory");
                                }
                            }

                            // move files
                            try {

                                for (int i = 0; i < length; i++) {

                                    String imgStr = list[i].toString().substring(list[i].toString().lastIndexOf("/") + 1);
                                    imgStr = file_extras.toString() + "/" + imgStr;
                                    File dstFile = new File(imgStr);

                                    copyFile(list[i], dstFile);
                                }

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            // delete files
                            for (int i = 0; i < length; i++) {
                                list[i].delete();
                            }

                            file.delete();

                            // save info to database-----------------------------------------------------------------------------------------

                            // if no test patient before add into database
                            if (!dbHandler.checkExist_Patient("test")) {
                                Patients patients = new Patients("test", "N/A", "N/A", "N/A");
                                dbHandler.addPatient(patients);
                            }

                            // add/edit slide info
                            if (!dbHandler.checkExist_Slide("test", m_Text)) {  // if this test slide doesn't exist before add into database
                                Slides slides = new Slides("test", m_Text, "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A", "N/A");
                                dbHandler.addSlide(slides);
                            }

                            // add images to image table
                            String cellCount[] = bundle.getString("cellCountEachImage").split(",");
                            String infectedCount[] = bundle.getString("infectedCountEachImage").split(",");
                            String cellCountGT[] = bundle.getString("cellCountEachImageGT").split(",");
                            String infectedCountGT[] = bundle.getString("infectedCountEachImageGT").split(",");
                            String imageName[] = bundle.getString("nameStringEachImage").split(",");

                            for (int i=0;i<imageName.length;i++) {
                                Images images = new Images("test", m_Text, imageName[i], cellCount[i], infectedCount[i], cellCountGT[i], infectedCountGT[i]);
                                dbHandler.addImage(images);
                            }

                            //----------------------------------------------------------------------------------------------------------------

                            Toast.makeText(getApplicationContext(), R.string.skip_moved, Toast.LENGTH_SHORT).show();

                            dialog.dismiss();

                            Intent intent = new Intent(getBaseContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // kill all the other activities on top of the old MainActivity.class activity
                            startActivity(intent);
                            finish();

                        }
                    }
                });

            }

        }
    }

    public static void copyFile(File src, File dst) throws IOException {

        FileInputStream var2 = new FileInputStream(src);
        FileOutputStream var3 = new FileOutputStream(dst);
        byte[] var4 = new byte[1024];

        int var5;
        while ((var5 = var2.read(var4)) > 0) {
            var3.write(var4, 0, var5);
        }

        var2.close();
        var3.close();
    }

    private void deleteImagesInSlide(String PID, String SID) {

        Log.d(TAG, "PID: " + PID);
        Log.d(TAG, "SID: " + SID);

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/Test");

        File[] folderList = file.listFiles();

        if (folderList != null) {
            int length = folderList.length;

            // delete files
            for (int i = 0; i < length; i++) {

                String imagePath = folderList[i].getAbsolutePath().toString();
                String SlideIDStr = imagePath.substring(imagePath.lastIndexOf("/") + 1);

                if (SlideIDStr.equals(SID)) {
                    // delete files
                    File[] imageList = folderList[i].listFiles();

                    if (imageList != null) {
                        int length1 = imageList.length;

                        if (length1 != 0) {
                            for (int j = 0; j < length1; j++) {
                                imageList[j].delete();
                            }

                            folderList[i].delete();
                        }
                    }
                }

            }

        }

    }

}
