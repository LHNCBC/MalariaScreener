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

package gov.nih.nlm.malaria_screener;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import gov.nih.nlm.malaria_screener.camera.CameraActivity;
import gov.nih.nlm.malaria_screener.database.DatabasePage;
import gov.nih.nlm.malaria_screener.tutorial.About;
import gov.nih.nlm.malaria_screener.tutorial.Diagram;
import gov.nih.nlm.malaria_screener.tutorial.TutorialActivity;
import gov.nih.nlm.malaria_screener.uploadFunction.UploadHashManager;
import gov.nih.nlm.malaria_screener.settings.UserOnBoardActivity;
//import gov.nih.nlm.malaria_screener.tutorial.Diagram;
//import gov.nih.nlm.malaria_screener.tutorial.TutorialActivity;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* This MainActivity class is the entry of this mobile app program.
* */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String DROPBOX_PROGRESS = "progress";
    private final static String DROPBOX_UPDATE_STATE = "app_state"; //0 initial, 1 success, 2 fail, 3 uploading

    static final int REQUEST_CAM = 2;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    // Static code clocks are used for assigning initial values to static variables. These are also called “static initializers”.
    static {

        if (!OpenCVLoader.initDebug()) {
            Log.i("opencv", "opencv initialization fail");
        } else {
            Log.i("opencv", "opencv initialization successful");
        }
    }

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean doNotShow_register = settings.getBoolean("do_not_show_again_register", false);

        SharedPreferences sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
        boolean registered = sharedPreferences.getBoolean("registered", false);

        Log.d(TAG, "doNotShow_register: " + doNotShow_register);
        Log.d(TAG, "registered: " + registered);

        if (!doNotShow_register & !registered) {

            Intent onBoardIntent = new Intent(getApplicationContext(), UserOnBoardActivity.class);
            startActivity(onBoardIntent);
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        toolbar.setTitle(R.string.app_name_bar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.logo_toolbar);

        Button newSessionButton = (Button) findViewById(R.id.newSession_button);
        Button databaseButton = (Button) findViewById(R.id.database_button);

        //Disable if no camera
        if (!hasCamera()) {
            //Log.i(TAG, "no cam");
            newSessionButton.setEnabled(false);
        }

        newSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Intent infoIntent = new Intent(v.getContext(), CameraActivity.class);
                startActivityForResult(infoIntent, REQUEST_CAM);

            }
        });

        // go to database page
        databaseButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        Intent dbIntent = new Intent(v.getContext(), DatabasePage.class);
                        startActivity(dbIntent);

                    }
                }
        );

        boolean firstTime = settings.getBoolean("firstTime_mainpage", true);
        if (firstTime) {

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("firstTime_mainpage", false).apply();

            ShowcaseView sv = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(new ViewTarget(R.id.newSession_button, this))
                    .setContentTitle(R.string.new_session_btn_title)
                    .setContentText(R.string.new_session_btn)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .build();

            sv.show();
        }

        boolean first_session_done = settings.getBoolean("first_session_done", false);
        if (first_session_done) {

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("first_session_done", false).apply();

            ShowcaseView sv_upload = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(new ViewTarget(R.id.database_button, this))
                    .setContentTitle(R.string.upload_title)
                    .setContentText(R.string.upload_text)
                    .setStyle(R.style.CustomShowcaseTheme2)
                    .build();

            sv_upload.show();
        }

        // load upload list
        UploadHashManager.hashmap_for_upload = new LinkedHashMap<>();
        UploadHashManager.hashmap_for_upload = UploadHashManager.loadMap(getApplicationContext());

        Log.d(TAG, "Hashmap empty: " + UploadHashManager.hashmap_for_upload.isEmpty());

        for(Map.Entry<String, String> entry: UploadHashManager.hashmap_for_upload.entrySet()){

            Log.d(TAG, "Key: " + entry.getKey() + ", Value: " + entry.getValue());
        }

        // check for permissions, camera & read/write storage, internet
        final List<String> permissionsList = new ArrayList<String>();

        addPermission(permissionsList, Manifest.permission.INTERNET);
        addPermission(permissionsList, Manifest.permission.CAMERA);
        addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionsList.size() > 0){
            ActivityCompat.requestPermissions(MainActivity.this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_PERMISSIONS);

            return;
        }

        //mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);

        // Language setting
        Configuration config = getBaseContext().getResources().getConfiguration();

        String language = settings.getString("Language", "");
        if (!"".equals(language) && !config.locale.getLanguage().equals(language)){
            Locale locale = new Locale(language);
            locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
            recreate();
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + getPackageName()));
//            startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
//        }

    }


//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == SYSTEM_ALERT_WINDOW_PERMISSION && resultCode == RESULT_OK) {
//            //initializeView();
//        } else {
//            Toast.makeText(this, "Draw over other app permission not enable.", Toast.LENGTH_SHORT).show();
//        }
//    }

    private boolean addPermission(List<String> permissionsList, String permission) {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:

                Map<String, Integer> perms = new HashMap<String, Integer>();

                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.INTERNET, PackageManager.PERMISSION_GRANTED);

                for(int i=0; i<permissions.length;i++){
                    perms.put(permissions[i], grantResults[i]);
                }

                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED){

                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, R.string.perm_denied, Toast.LENGTH_SHORT)
                            .show();
                    finish();
                    //System.exit(0);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Override
    protected void onResume() {
        super.onResume();

        CleanImagesFromUnfinishedSession();
    }

    // This function moves the images generated from the previous unfinished session
    private void CleanImagesFromUnfinishedSession(){

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/New");

        if (file.listFiles()!=null) {

            final File[] list = file.listFiles();
            final int length = file.listFiles().length;

            if (length > 0) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

                // Setting Dialog Title
                alertDialog.setTitle(R.string.unsaved_title);

                // Setting Dialog Message
                alertDialog.setMessage(R.string.unsaved_message);

                // Setting Positive "Yes" Button
                String string = getResources().getString(R.string.save);
                alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        File file_extras = new File(Environment.getExternalStorageDirectory(
                        ), "NLM_Malaria_Screener/Extras");

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

                        // Write your code here to invoke YES event
                        Toast.makeText(getApplicationContext(), R.string.unsaved_moved, Toast.LENGTH_SHORT).show();
                    }
                });

                // Setting Negative "NO" Button
                String string1 = getResources().getString(R.string.delete1);
                alertDialog.setNegativeButton(string1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        // delete files
                        for (int i = 0; i < length; i++) {
                            list[i].delete();
                        }

                        file.delete();

                        // Write your code here to invoke NO event
                        Toast.makeText(getApplicationContext(), R.string.unsaved_deleted, Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                });

                // Showing Alert Message
                alertDialog.show();

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

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getInt(DROPBOX_PROGRESS, 0) != 0) {
            editor.putInt(DROPBOX_UPDATE_STATE, 2);

        }
        editor.putInt(DROPBOX_PROGRESS, 0);
        editor.apply();
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_tutorial) {

            Intent tutorialIntent = new Intent(this, TutorialActivity.class);

            startActivity(tutorialIntent);

        } else if (id == R.id.action_about) {

            Intent aboutIntent = new Intent(this, About.class);

            startActivity(aboutIntent);

        } else if (id == R.id.action_change_language){

            showChangeLangDialog();
            
        } else if (id == R.id.action_diagram){

            Intent diagramIntent = new Intent(this, Diagram.class);

            startActivity(diagramIntent);

        }

        return super.onOptionsItemSelected(item);
    }

    public void showChangeLangDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.language_dialog, null);
        dialogBuilder.setView(dialogView);

        final Spinner spinnerLanguage = (Spinner) dialogView.findViewById(R.id.spinner_language);
        dialogBuilder.setTitle(getResources().getString(R.string.language_dialog_title));
        dialogBuilder.setMessage(getResources().getString(R.string.language_dialog_message));

        String string = getResources().getString(R.string.change);
        dialogBuilder.setPositiveButton(string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int pos = spinnerLanguage.getSelectedItemPosition();
                switch (pos){
                    case 0: // English
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("Language", "en").commit();
                        setLanguageRecreate("en");
                        return;
                    case 1: //Thai
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("Language", "th").commit();
                        setLanguageRecreate("th");
                        return;
                    case 2: //Chinese
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("Language", "zh").commit();
                        setLanguageRecreate("zh");
                        return;

                    default: //By default set to English
                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("Language", "en").commit();
                        setLanguageRecreate("en");
                        return;
                }
            }
        });

        String string1 = getResources().getString(R.string.cancel);
        dialogBuilder.setNegativeButton(string1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    public void setLanguageRecreate(String langval) {
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(langval);
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        recreate();
    }
}
