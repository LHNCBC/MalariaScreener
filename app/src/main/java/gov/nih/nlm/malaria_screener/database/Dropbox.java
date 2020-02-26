package gov.nih.nlm.malaria_screener.database;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import gov.nih.nlm.malaria_screener.MainActivity;
import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.ImageUploadEvent;
import gov.nih.nlm.malaria_screener.frontEnd.UploadService;

public class Dropbox extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    // private TextInputLayout uploadFolderLayout;

    private Button uploadButton;
    //private Button uploadButtonServer;
    //private Button loginButton;

    //private EditText editText_uploadfolder;
    private String path;

    //private DropboxAPI dropboxAPI;
    DbxClientV2 dbxClientV2;

    private final static String DROPBOX_FILE_DIR = "/NLM_Malaria_Screener/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String ACCESS_KEY = "66gkai9g3vjhhl0";
    private final static String ACCESS_SECRET = "lydtaghntawlr1k";
    private final static String DROPBOX_DIR = "dropbox_dir"; // folder name on dropbox for current user
    private final static String DROPBOX_PROGRESS = "progress";
    private final static String DROPBOX_UPDATE_STATE = "app_state"; //0 initial, 1 success, 2 fail, 3 uploading

    private final static Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    private ProgressBar progressBar;
    private ProgressBar progressBarCircle;
    private TextView textView;
    private TextView textView_progress;

    private String folderName;
    private int fileNum; // total number of files in /NLM_Malaria_Screener/

    MyDBHandler dbHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dropbox);

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_dropbox);
        toolbar.setTitle(R.string.title_dropbox);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbHandler = new MyDBHandler(this, null, null, 1);

        uploadButton = (Button) findViewById(R.id.button_upload);
        //uploadButtonServer = (Button) findViewById(R.id.button_upload2Server);
        //loginButton = (Button) findViewById(R.id.button_login);

        //uploadFolderLayout = (TextInputLayout) findViewById(R.id.layout_uploadFolder);
        //editText_uploadfolder = (EditText) findViewById(R.id.editText_uploadfolder);

        AppKeyPair appKeyPair = new AppKeyPair(ACCESS_KEY, ACCESS_SECRET);
        final AndroidAuthSession session;

        SharedPreferences sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
        //String key = sharedPreferences.getString(ACCESS_KEY, null);
        //String secret = sharedPreferences.getString(ACCESS_SECRET, null);
        folderName = sharedPreferences.getString(DROPBOX_DIR, null);

//        if (dir_name != null) {
//            editText_uploadfolder.setText(dir_name);
//        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar_upload);
        progressBarCircle = (ProgressBar) findViewById(R.id.progressBar_upload1);

//        if (key != null && secret != null) {
//            AccessTokenPair tokenPair = new AccessTokenPair(key, secret);
//            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, tokenPair);
//        } else {
        session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
        session.setOAuth2AccessToken("K-CP-L6LtbAAAAAAAAAAMdsVY4qFqw5Fs_BqqBz1E7Doz4eUiZaRkhHkKZNeF4nR");
//        }

        //dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);

        DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app", "en_US");

        dbxClientV2 = new DbxClientV2(config, "K-CP-L6LtbAAAAAAAAAAasMaZq16SliwAnpOYA2KYlCkH2EXPv_eTiuJ4BxvASG3");

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener");

        fileNum = getFileCount(file);

        progressBar.setMax(fileNum);
        progressBar.setVisibility(View.GONE);
        progressBarCircle.setVisibility(View.GONE);

        textView = (TextView) findViewById(R.id.textView_notice);
        textView_progress = (TextView) findViewById(R.id.textView_notice_progress);
        textView.setVisibility(View.GONE);
        textView_progress.setVisibility(View.GONE);

        int progressNum = sharedPreferences.getInt(DROPBOX_PROGRESS, 0);

        if (sharedPreferences.getInt(DROPBOX_UPDATE_STATE, 0) == 0) {

        } else if (sharedPreferences.getInt(DROPBOX_UPDATE_STATE, 0) == 1) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.upload_succeeded);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(DROPBOX_UPDATE_STATE, 0);
            editor.apply();

        } else if (sharedPreferences.getInt(DROPBOX_UPDATE_STATE, 0) == 2) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.upload_failed);
        } else if (sharedPreferences.getInt(DROPBOX_UPDATE_STATE, 0) == 3) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(R.string.uploading);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(progressNum);
            progressBarCircle.setVisibility(View.VISIBLE);
        }

        uploadButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

//                        if (editText_uploadfolder.getText().toString().isEmpty()) {
//                            uploadFolderLayout.setErrorEnabled(true);
//                            uploadFolderLayout.setError("Name of upload folder can not be empty!");
//                        } else {

                        startService(new Intent(Dropbox.this, UploadService.class));

                        exportDB();

                        new CheckFolderName().execute();

                        // Write your code here to invoke YES event
                        //Toast.makeText(getApplicationContext(), "Starting to upload...", Toast.LENGTH_SHORT).show();
                        //}

                    }
                }
        );

        /*uploadButtonServer.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        new DB_connect().execute();

                    }
                }
        );*/

//        loginButton.setOnClickListener(
//                new Button.OnClickListener() {
//                    public void onClick(View v) {
//                        ((AndroidAuthSession) dropboxAPI.getSession()).startAuthentication(Dropbox.this);
//
//                    }
//                }
//        );

        //editText_uploadfolder.addTextChangedListener(watcher);

    }

    public int getFileCount(File file) {

        int fileNum = 0;

        if (file.listFiles() != null) {

            final File[] folder_list = file.listFiles(); // list of folders from each session

            // calculate number of files to get uploaded
            for (int i = 0; i < folder_list.length; i++) {

                String dirStr = folder_list[i].toString().substring(folder_list[i].toString().lastIndexOf("/") + 1);

                if (dirStr.equals("Test")) { // for Test folder

                    File[] Listing = folder_list[i].listFiles(); // list of slides in Test

                    for (int k = 0; k < Listing.length; k++) {

                        File[] imageList = Listing[k].listFiles();

                        fileNum = fileNum + imageList.length;
                    }

                } else {

                    if (folder_list[i].listFiles() == null) { // for log files

                        fileNum = fileNum + 1;

                    } else { // for normal slide folder

                        File[] Listing = folder_list[i].listFiles(); // list of images from one session

                        fileNum = fileNum + Listing.length;

                    }

                }
            }
        }

        return fileNum;
    }

//    private TextWatcher watcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//
//            if (!editText_uploadfolder.getText().toString().isEmpty()) {
//                //pIDLayout.setErrorEnabled(false);
//                uploadFolderLayout.setError(null);
//
//            }
//
//        }
//    };

    private void exportDB() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), DROPBOX_FILE_DIR);

        File file = new File(exportDir, "MalariaScreenerDB.csv");

        try {
            file.createNewFile();
            CSVFileWriter csvFileWriter = new CSVFileWriter(new FileWriter(file));
            SQLiteDatabase db = dbHandler.getReadableDatabase();

            String queryPatient = "SELECT * FROM patients";
            Cursor cursor = db.rawQuery(queryPatient, null);
            csvFileWriter.writeNext(cursor.getColumnNames());  // write column names into excel sheet

            int patientColCount = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[patientColCount];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            String querySlide = "SELECT * FROM slides";
            cursor = db.rawQuery(querySlide, null);
            csvFileWriter.writeNext(cursor.getColumnNames());

            int slideColCount = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[slideColCount];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            String queryImage = "SELECT * FROM images";
            cursor = db.rawQuery(queryImage, null);
            csvFileWriter.writeNext(cursor.getColumnNames());

            int imageColCount = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[imageColCount];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            String queryImage_thick = "SELECT * FROM images_thick";
            cursor = db.rawQuery(queryImage_thick, null);
            csvFileWriter.writeNext(cursor.getColumnNames());

            int imageColCount_thick = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[imageColCount_thick];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            csvFileWriter.close();
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressBarEvent event) {

        progressBar.setVisibility(View.VISIBLE);
        textView.setVisibility(View.VISIBLE);
        textView_progress.setVisibility(View.VISIBLE);

        progressBar.setProgress(event.getProgress());
        textView.setText(R.string.uploading);
        textView_progress.setText(event.getProgress() + "/" + fileNum);

        progressBarCircle.setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(DROPBOX_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(DROPBOX_UPDATE_STATE, 3);
        editor.apply();
    }

    @Subscribe
    public void onProgressDone(ProgressDoneEvent event) {
        //if (event.getProgressDone()){
        progressBar.setVisibility(View.GONE);
        textView.setText(R.string.upload_finished);
        textView_progress.setVisibility(View.GONE);

        progressBarCircle.setVisibility(View.GONE);
        //}
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

//        AndroidAuthSession session = (AndroidAuthSession) dropboxAPI.getSession();
//        if (session.authenticationSuccessful()) {
//            try {
//                session.finishAuthentication();
//
//                TokenPair tokenPair = session.getAccessTokenPair();
//
//                SharedPreferences sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
//                SharedPreferences.Editor editor = sharedPreferences.edit();
//                editor.putString(ACCESS_KEY, tokenPair.key);
//                editor.putString(ACCESS_SECRET, tokenPair.secret);
//                editor.commit();
//
//            } catch (IllegalStateException e) {
//
//            }
//
//        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    class CheckFolderName extends AsyncTask<Void, Void, Void> {  // at the moment is only for checking internet connection

        List<String> isFolder;
        FullAccount fullAccount;

        @Override
        protected Void doInBackground(Void... params) {

            try {
                //isFolder = dropboxAPI.search(DROPBOX_FILE_DIR, folderName, 1, false);
                fullAccount = dbxClientV2.users().getCurrentAccount();
            } catch (DbxException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void values) {
            super.onPostExecute(values);

            if (fullAccount!=null) {
                if (fullAccount.getName().toString().isEmpty()) {
                    String string = getResources().getString(R.string.no_internet);
                    Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
//
                } else {

                    Log.d(TAG, "account Name: " + fullAccount.getName().toString());

//                if (!isFolder.isEmpty()) { // have folder with same name
//
//                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Dropbox.this);
//
//                    // Setting Dialog Title
//                    alertDialog.setTitle(R.string.update);
//
//                    // Setting Dialog Message
//                    alertDialog.setMessage("There is already a folder with the name '" + folderName + "' in this Dropbox account. Do you want to update it?");
//
//                    // Setting Positive "Yes" Button
//                    alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            progressBar.setVisibility(View.VISIBLE);
//                            progressBar.setProgress(0);
//                            textView.setVisibility(View.VISIBLE);
//                            textView.setText("Please do not close the app while uploading.");
//                            path = DROPBOX_FILE_DIR + folderName;
//
//                            new Upload(getApplicationContext(), dropboxAPI, path).execute();
//                        }
//                    });
//
//                    // Setting Negative "NO" Button
//                    alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            // Write your code here to invoke NO event
//                            Toast.makeText(getApplicationContext(), "Upload canceled", Toast.LENGTH_SHORT).show();
//                            dialog.cancel();
//                        }
//                    });
//
//                    alertDialog.show();
//
//                } else {
                    progressBarCircle.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setProgress(0);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(R.string.uploading);

                    path = DROPBOX_FILE_DIR + folderName;

                    SharedPreferences sharedPreferences = getBaseContext().getSharedPreferences(DROPBOX_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.apply();

                    new Upload(getApplicationContext(), dbxClientV2, path).execute();
                    //}
                }
            } else {
                String string = getResources().getString(R.string.no_internet);
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        }

    }

    /*private class DB_connect extends AsyncTask<Void, Void, Void> {

        private static final String URL = "jdbc:mysql://130.14.109.11:3306/mydb";
        private static final String userName = "yuh5";
        private static final String password = "6n8R0VzOVYKVU4mT$";

        private String users_auto_id; // users table auto id in server DB
        private String patients_auto_id; // patients table auto id in server DB

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                Class.forName("com.mysql.jdbc.Driver");
                Connection myConn = DriverManager.getConnection(URL, userName, password);

                Statement myStmt = myConn.createStatement();

                // add to users table
                ResultSet myRs = myStmt.executeQuery("select * from users");

                while (myRs.next()) {

                    // old user
                    if (folderName.equals(myRs.getString("first_name") + myRs.getString("last_name"))) {
                        users_auto_id = myRs.getString("auto_id");
                        break;
                    }

                    // new user
                    if (myRs.isLast()) {

                        for (int i = 1; i < folderName.length(); i++) {
                            if (Character.isUpperCase(folderName.charAt(i))) {
                                String query = "INSERT INTO users (first_name, last_name) VALUES('" + folderName.substring(0, i) + "', '" + folderName.substring(i) + "')";
                                myStmt.executeUpdate(query);
                                break;
                            }
                        }

                        ResultSet resultSet = myStmt.executeQuery("SELECT * FROM users ORDER BY auto_id DESC LIMIT 1");
                        resultSet.next();
                        users_auto_id = resultSet.getString("auto_id");
                        Log.d(TAG, "users_auto_id: " + users_auto_id);
                        break;
                    }
                }

                // add to patients table
                Cursor cursor = dbHandler.returnPatients();
                cursor.moveToFirst();

                do {
                    String pID_local = cursor.getString(cursor.getColumnIndex("_id"));
                    String gender_local = cursor.getString(cursor.getColumnIndex("gender"));
                    String initial_local = cursor.getString(cursor.getColumnIndex("initial"));
                    String age_local = cursor.getString(cursor.getColumnIndex("age"));

                    int oldOrnew = 1; // old = 0; new = 1;

                    ResultSet myRs_patient = myStmt.executeQuery("select * from patients");
                    while (myRs_patient.next()) {

                        // old patient
                        if (pID_local.equals(myRs_patient.getString("id")) && users_auto_id.equals(myRs_patient.getString("users_auto_id"))) {
                            patients_auto_id = myRs_patient.getString("auto_id");

                            oldOrnew = 0;
                            break;
                        }

                        // new patient
                        if (myRs_patient.isLast()) {
                            String query = "INSERT INTO patients (id, gender, initial, age, users_auto_id) VALUES('" + pID_local + "', '" + gender_local + "', '" + initial_local + "', '" + age_local + "', '" + users_auto_id + "')";
                            myStmt.executeUpdate(query);

                            ResultSet resultSet = myStmt.executeQuery("SELECT * FROM patients ORDER BY auto_id DESC LIMIT 1");
                            resultSet.next();
                            patients_auto_id = resultSet.getString("auto_id");

                            oldOrnew = 1;
                            break;
                        }
                    }

                    // add to slides table
                    Cursor cursorSlides = dbHandler.returnPatientSlides(pID_local);
                    cursorSlides.moveToFirst();
                    do {

                        String sID_local = cursorSlides.getString(cursorSlides.getColumnIndex("slideID"));
                        String date_local = cursorSlides.getString(cursorSlides.getColumnIndex("date"));
                        String time_local = cursorSlides.getString(cursorSlides.getColumnIndex("time"));
                        String site_local = cursorSlides.getString(cursorSlides.getColumnIndex("site"));

                        if (oldOrnew == 0) {

                            ResultSet myRs_slide = myStmt.executeQuery("select * from slides where patients_auto_id = " + patients_auto_id);

                            while (myRs_slide.next()) {
                                // old slide
                                if (sID_local.equals(myRs_slide.getString("slide_id")) && patients_auto_id.equals(myRs_slide.getString("patients_auto_id"))) {
                                    break;
                                }

                                // new slide
                                if (myRs_slide.isLast()) {
                                    String query = "INSERT INTO slides (slide_id, date, time, site, patients_auto_id) VALUES('" + sID_local + "', '" + date_local + "', '" + time_local + "', '" + site_local + "', '" + patients_auto_id + "')";
                                    myStmt.executeUpdate(query);
                                    break;
                                }
                            }
                        } else {

                            String query = "INSERT INTO slides (slide_id, date, time, site, patients_auto_id) VALUES('" + sID_local + "', '" + date_local + "', '" + time_local + "', '" + site_local + "', '" + patients_auto_id + "')";
                            myStmt.executeUpdate(query);
                        }

                    } while (cursorSlides.moveToNext());

                } while (cursor.moveToNext());


                myConn.close();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

        }
    }*/

}




