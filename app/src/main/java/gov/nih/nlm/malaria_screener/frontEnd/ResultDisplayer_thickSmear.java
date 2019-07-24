package gov.nih.nlm.malaria_screener.frontEnd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter_Counts;
import gov.nih.nlm.malaria_screener.custom.RowItem_CountsNtexts;
import gov.nih.nlm.malaria_screener.custom.TouchImageView;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsData;
import gov.nih.nlm.malaria_screener.frontEnd.baseClass.ResultDisplayerBaseActivity;

public class ResultDisplayer_thickSmear extends ResultDisplayerBaseActivity {

    private static final String TAG = "MyDebug";

    static final int REQUEST_CAM = 2;

    String picFile;

    String[] wbcCount = new String[1];
    String[] parasiteCount = new String[1];

    String WB;
    long processingTime;

    private Bundle bundle;

    Bitmap resultBitmap;

    boolean imageAcquisition = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final int totalWBCNeeded = sharedPreferences.getInt("wbc_th", 200);

        imageAcquisition = sharedPreferences.getBoolean("image_acquire", false);

        if (imageAcquisition) {
            setContentView(R.layout.activity_display_acquisition_mode);

            Button finishButton = findViewById(R.id.finishButton);

            finishButton.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View view) {

                            // save results image
                            createDirectoryAndSaveResultImage(resultBitmap, bundle);
                            writeLogFile();

                            setManualCounts();

                            finishActivity(REQUEST_CAM);

                            Intent PatientInfoIntent = new Intent(view.getContext(), PatientInfoActivity.class);

                            PatientInfoIntent.putExtras(bundle);
                            startActivity(PatientInfoIntent);
                            finish();

                        }
                    }
            );
        }

        Toolbar toolbar = findViewById(R.id.navigate_bar_result);
        toolbar.setTitle(R.string.title_result);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);

        ListView listView_counts = findViewById(R.id.listView_counts);
        ProgressBar progressBar = findViewById(R.id.progressBar2);
        TextView progressText = findViewById(R.id.textView_progress);
        TouchImageView imageView = findViewById(R.id.processed);
        Button continueButton = findViewById(R.id.continueButton);
        Button endButton = findViewById(R.id.endButton);

        Intent intent = getIntent();
        bundle = intent.getExtras();
        picFile = bundle.getString("picFile");

        WB = intent.getStringExtra("WB");
        processingTime = Long.valueOf(intent.getStringExtra("time"));

        //set up bitmaps
        resultBitmap = getResBitmap(bundle, imageView);

        displayOriginalImage(bundle, imageView, resultBitmap);

        continueButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {

                        // save results image
                        createDirectoryAndSaveResultImage(resultBitmap, bundle);
                        writeLogFile();

                        setManualCounts();

                        if (UtilsData.WBCTotal < totalWBCNeeded) {

                            Intent returnIntent = new Intent();

                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        } else {
                            finishActivity(REQUEST_CAM);

                            Intent PatientInfoIntent = new Intent(view.getContext(), PatientInfoActivity.class);

                            PatientInfoIntent.putExtras(bundle);
                            startActivity(PatientInfoIntent);
                            finish();
                        }
                    }
                }
        );

        endButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {

                        // save results image
                        createDirectoryAndSaveResultImage(resultBitmap, bundle);
                        writeLogFile();

                        setManualCounts();

                        finishActivity(REQUEST_CAM);

                        Intent PatientInfoIntent = new Intent(getBaseContext(), PatientInfoActivity.class);

                        PatientInfoIntent.putExtras(bundle);
                        startActivity(PatientInfoIntent);
                        finish();
                    }
                }
        );

        String[] values_title = getResources().getStringArray(R.array.count_item);

        int[] values_parasites = new int[2];
        int[] values_wbcs = new int[2];
        values_parasites[0] = UtilsData.parasiteCurrent;
        values_parasites[1] = UtilsData.parasiteTotal;
        values_wbcs[0] = UtilsData.WBCCurrent;
        values_wbcs[1] = UtilsData.WBCTotal;

        List<RowItem_CountsNtexts> rowItemCellCount = new ArrayList<RowItem_CountsNtexts>();
        for (int i = 0; i < values_parasites.length; i++) {
            RowItem_CountsNtexts item = new RowItem_CountsNtexts(values_title[i], values_parasites[i], values_wbcs[i], R.string.parasites, R.string.wbcs);
            rowItemCellCount.add(item);
        }

        CustomAdapter_Counts adapter_cellCount = new CustomAdapter_Counts(this, rowItemCellCount);
        listView_counts.setAdapter(adapter_cellCount);

        int progressStatus = UtilsData.WBCTotal;
        progressBar.setProgress(progressStatus);
        progressBar.setMax(totalWBCNeeded);
        progressText.setText(UtilsData.WBCTotal + "/" + totalWBCNeeded);

        // when get enough cells
        if (UtilsData.WBCTotal > totalWBCNeeded) {
            continueButton.setText(R.string.finish_button);
            continueButton.setTextColor(Color.parseColor("red"));
            continueButton.setTextSize(20);
            continueButton.setTypeface(continueButton.getTypeface(), Typeface.BOLD);
            String string = getResources().getString(R.string.enough_wbcs);
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        }

        UtilsCustom.oriSizeMat.release();

        System.gc();
        Runtime.getRuntime().gc();

    }

    private void setManualCounts() {

        if (wbcCount[0] == null) {
            wbcCount[0] = "N/A";
        }

        if (parasiteCount[0] == null) {
            parasiteCount[0] = "N/A";
        }

        UtilsData.addWBCCount_GT(wbcCount[0]);
        UtilsData.addParasiteCount_GT(parasiteCount[0]);
    }

    public void onBackPressed() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.delete);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.reject_image);

        // Setting Positive "Yes" Button
        String string = getResources().getString(R.string.yes);
        alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                Intent returnIntent = new Intent();

                if (picFile != null) {
                    // delete saved pics
                    File fdelete = new File(picFile);

                    if (fdelete.exists()) {
                        fdelete.delete();
                    }

                    // delete data from current image
                    UtilsData.parasiteTotal = UtilsData.parasiteTotal - UtilsData.parasiteCurrent;
                    UtilsData.WBCTotal = UtilsData.WBCTotal - UtilsData.WBCCurrent;
                    UtilsData.removeImageName();
                    UtilsData.removeParasiteCount();
                    UtilsData.removeWBCCount();
                    UtilsData.resetCurrentCounts_thick();

                }

                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();

                // Write your code here to invoke YES event
                String string = getResources().getString(R.string.image_deleted);
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_resultpage, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_manualCounts) {

            final Dialog dialog_wbccounts = new Dialog(this);
            dialog_wbccounts.setContentView(R.layout.input_box_manualcounts);
            dialog_wbccounts.setCancelable(false);
            dialog_wbccounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            TextView textView_wbccounts = dialog_wbccounts.findViewById(R.id.textView_manualcounts);
            final EditText input_wbccount = dialog_wbccounts.findViewById(R.id.editText_manualcounts);

            Button button_wbccounts = dialog_wbccounts.findViewById(R.id.button_okay);
            Button buttonCancel_wbccounts = dialog_wbccounts.findViewById(R.id.button_cancel);

            final Dialog dialog_parasitecounts = new Dialog(this);
            dialog_parasitecounts.setCancelable(false);
            dialog_parasitecounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog_parasitecounts.setContentView(R.layout.input_box_manualcounts);
            TextView textView_parasitecounts = dialog_parasitecounts.findViewById(R.id.textView_manualcounts);
            final EditText input_parasite = dialog_parasitecounts.findViewById(R.id.editText_manualcounts);

            Button button_parasitecounts = dialog_parasitecounts.findViewById(R.id.button_okay);
            Button buttonCancel_parasitecounts = dialog_parasitecounts.findViewById(R.id.button_cancel);

            textView_wbccounts.setText(R.string.manual_wbc_count);
            textView_parasitecounts.setText(R.string.manual_parasite_infected);

            button_wbccounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            wbcCount[0] = input_wbccount.getText().toString();

                            dialog_parasitecounts.show();
                            dialog_wbccounts.dismiss();
                        }
                    }
            );

            buttonCancel_wbccounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog_wbccounts.dismiss();
                        }
                    }
            );

            button_parasitecounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            parasiteCount[0] = input_parasite.getText().toString();

                            if (wbcCount[0].isEmpty()) {
                                wbcCount[0] = "N/A";
                            }

                            if (parasiteCount[0].isEmpty()) {
                                parasiteCount[0] = "N/A";
                            }

                            dialog_parasitecounts.dismiss();
                        }
                    }
            );

            buttonCancel_parasitecounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog_parasitecounts.dismiss();
                        }
                    }
            );

            dialog_wbccounts.show();

        } else if (id == R.id.action_endSession) {

            // save results image
            createDirectoryAndSaveResultImage(resultBitmap, bundle);
            writeLogFile();

            setManualCounts();

            finishActivity(REQUEST_CAM);

            Intent PatientInfoIntent = new Intent(getBaseContext(), PatientInfoActivity.class);

            PatientInfoIntent.putExtras(bundle);
            startActivity(PatientInfoIntent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void writeLogFile() {

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

                if (textFile.length() == 0) {
                    outText.write(("ImageName,WhiteBalance,ProcessingTime(sec)").getBytes());
                    outText.write(("\n").getBytes());
                }

                // get image name
                String imgStr = picFile.toString().substring(picFile.toString().lastIndexOf("/") + 1);
                int endIndex = imgStr.lastIndexOf(".");
                String imageName = imgStr.substring(0, endIndex);

                outText.write((imageName + "," + WB + "," + processingTime).getBytes());
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

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File Dir = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/");
        File imgFile = new File(Dir, "Log_thick.txt");
        if (!imgFile.exists()) {
            imgFile.createNewFile();
        }

        return imgFile;
    }

}
