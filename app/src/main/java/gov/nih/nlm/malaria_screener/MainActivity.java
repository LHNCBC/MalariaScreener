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
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import gov.nih.nlm.malaria_screener.camera.CameraActivity;
import gov.nih.nlm.malaria_screener.database.DatabasePage;
import gov.nih.nlm.malaria_screener.tutorial.About;
import gov.nih.nlm.malaria_screener.tutorial.Diagram;
import gov.nih.nlm.malaria_screener.tutorial.TutorialActivity;
//import gov.nih.nlm.malaria_screener.tutorial.Diagram;
//import gov.nih.nlm.malaria_screener.tutorial.TutorialActivity;

import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";
//    static final int REQUEST_IMAGE_CAPTURE = 1;
//    public static final int MEDIA_TYPE_IMAGE = 1;
//    public static final String PARAM_PID = "PID";
//    public static final String PARAM_PROCESSED = "processed";

    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String DROPBOX_PROGRESS = "progress";
    private final static String DROPBOX_UPDATE_STATE = "app_state"; //0 initial, 1 success, 2 fail, 3 uploading

    static final int REQUEST_CAM = 2;

    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    private Button newSessionButton;
    private Button databaseButton;
    //    private AlphaAnimation buttonPress;
//    private String pid = "";
//    ImageView imageview;
//    Bitmap photo;
//    Bitmap processed;
    private boolean autoSVMThres = true;
    private String CThres = "";
    CheckBox checkBox;

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

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.app_bar);
        //toolbar.setTitle(R.string.app_name_bar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.mipmap.nlm_logo_white);

        newSessionButton = (Button) findViewById(R.id.newSession_button);
        databaseButton = (Button) findViewById(R.id.database_button);

        //Disable if no camera
        if (!hasCamera()) {
            //Log.i(TAG, "no cam");
            newSessionButton.setEnabled(false);
        }

        //buttonPress = new AlphaAnimation(1F, 0.5F);
        //imageview = (ImageView) findViewById(R.id.)
        newSessionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Intent infoIntent = new Intent(v.getContext(), CameraActivity.class);
//                Bundle bundle = new Bundle();
//
//                String auto = String.valueOf(autoSVMThres);
//                bundle.putString("autoSVM_Th", auto);
//                if (!autoSVMThres) {
//                    bundle.putString("Confidence_Th", CThres);
//                }
//                infoIntent.putExtras(bundle);
                startActivityForResult(infoIntent, REQUEST_CAM);

                /*v.startAnimation(buttonPress);
                final AlertDialog.Builder promptPID = new AlertDialog.Builder(c);
                promptPID.setTitle("Start a new session");
                promptPID.setMessage("Enter Patient ID:");
                final EditText input = new EditText(c);
                promptPID.setView(input);
                // Set up the buttons
                promptPID.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        pid = input.getText().toString();
                        Intent captureIntent = new Intent(v.getContext(), CameraActivity.class);
                        captureIntent.putExtra(PARAM_PID, pid);

                        String auto = String.valueOf(autoSVMThres);
                        captureIntent.putExtra(EXTRA_AUTO, auto);
                        if (!autoSVMThres) {
                            captureIntent.putExtra(EXTRA_CONFIDENCE_T, CThres);
                        }

                        startActivityForResult(captureIntent, REQUEST_IMAGE_CAPTURE);
                    }
                });

                promptPID.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                promptPID.show();*/
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

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = settings.getBoolean("firstTime_main", true);
        if (firstTime) {

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("firstTime_main", false).commit();

//            ShowcaseView sv = new ShowcaseView.Builder(this)
//                    .withMaterialShowcase()
//                    .setTarget(new ViewTarget(R.id.newSession_button, this))
//                    .setContentTitle(R.string.new_session_btn_title)
//                    .setContentText(R.string.new_session_btn)
//                    .setStyle(R.style.CustomShowcaseTheme2)
//                    .build();
//
//            sv.show();
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


    }

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
                    // Permission Granted
//                    Intent infoIntent = new Intent(getApplicationContext(), CameraActivity.class);
//
//                    startActivityForResult(infoIntent, REQUEST_CAM);

                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, R.string.perm_denied, Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    /*

    // Return the taken picture
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //Get the photo
            Bundle extras = data.getExtras();
            Log.d(TAG, "Retrieving photo and decoding byte array to bitmap...");
            String absPath = (String) extras.get(CameraActivity.PARAM_PATH);

            //photo = BitmapFactory.decodeFile(absPath);

        }
    }

    Handler newImgHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            imageview.setImageBitmap(processed);
            return true;
        }
    });*/

    // load openCV package
    /*private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };*/

    @Override
    protected void onResume() {
        super.onResume();
        // you may be tempted, to do something here, but it's *async*, and may take some time,
        // so any opencv call here will lead to unresolved native errors.
        //Log.i(TAG, "Main activity resumed.");
        //OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);

        // Move files to Extra & clear New folder
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
                String string = getResources().getString(R.string.yes);
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
                String string1 = getResources().getString(R.string.no);
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

    @Override
    protected void onStop(){
        super.onStop();

        SharedPreferences sharedPreferences = getSharedPreferences(DROPBOX_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.getInt(DROPBOX_PROGRESS, 0) != 0) {
            editor.putInt(DROPBOX_UPDATE_STATE, 2);

        }
        editor.putInt(DROPBOX_PROGRESS, 0);
        editor.commit();
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

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

        /*if (id == R.id.action_settings) {
            final Context c = MainActivity.this;
            final AlertDialog.Builder ConfidenceSetting = new AlertDialog.Builder(c);

            // Get the layout inflater
            LayoutInflater inflater = getLayoutInflater();

            ConfidenceSetting.setTitle("Set sensitivity level for SVM: ");
            //ConfidenceSetting.setMessage("Enter Patient ID:");

            View view = inflater.inflate(R.layout.confidence_setting, null);
            ConfidenceSetting.setView(view);

            SeekBar seekBar = (SeekBar) view.findViewById(R.id.confidenceBar);
            final TextView textView = (TextView) view.findViewById(R.id.confidenceText);
            checkBox = (CheckBox) view.findViewById(R.id.checkbox);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    double value = (double)i/100;
                    textView.setText("Sensitivity level: " + value);
                    CThres = Double.toString(value);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    checkBox.setChecked(false);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    // update your model (or other business logic) based on isChecked
                    if (isChecked) {
                        autoSVMThres = true;
                    } else {
                        autoSVMThres = false;
                    }
                }
            });


            // Set up the buttons
            ConfidenceSetting.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            ConfidenceSetting.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            ConfidenceSetting.show();

            return true;
        }*/

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
