package gov.nih.nlm.malaria_screener.frontEnd;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter_Counts;
import gov.nih.nlm.malaria_screener.custom.RowItem_CountsNtexts;
import gov.nih.nlm.malaria_screener.custom.TouchImageView;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuh5 on 12/18/2015.
 */
public class getResultNdisplay extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    static final int REQUEST_CAM = 2;

    private ListView listView_cells;
    private TouchImageView imageView;
    Bitmap canvasBitmap;

    Matrix matrix;

    private boolean autoSVMThres;
    private String CThres = "";

    Button continueButton;
    //Button cancelButton;

    private int cellTotal = 0;
    private int infectedTotal = 0;

    private int cellCurrent = 0;
    private int infectedCurrent = 0;

    String[] values_title;
    int[] values_cells = new int[2];
    int[] values_infectedcells = new int[2];

    List<RowItem_CountsNtexts> rowItemCellCount = new ArrayList<RowItem_CountsNtexts>();

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView progressText;

    private Intent intent;
    private Bundle bundle;

    String picFile = null;

    int totalCellNeeded = 1000;

    String WB;
    double SVM_Th;
    long processingTime;

    private String nameEachImage;

    private String cellEachImage;
    private String infectedEachImage;

    private String cellCountManual = "N/A";
    private String infectedCountManual = "N/A";

    private String cellEachImageGT;
    private String infectedEachImageGT;

    Bitmap smallOriBitmap;

    boolean imageAcquisition = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        imageAcquisition = sharedPreferences.getBoolean("image_acquire", false);

        if (imageAcquisition) {
            setContentView(R.layout.activity_display_acquisition_mode);

            Button finishButton = findViewById(R.id.finishButton);

            finishButton.setOnClickListener(
                    new Button.OnClickListener() {
                        public void onClick(View view) {

                            if (picFile != null) {
                                // output result image & write log file
                                createDirectoryAndSaveFile2(canvasBitmap);
                                writeLogFile();
                            }

                            canvasBitmap.recycle();

                            finishActivity(REQUEST_CAM);

                            Intent patientInfoIntent = new Intent(getBaseContext(), PatientInfoActivity.class);
                            bundle.putString("nameStringEachImage", nameEachImage);
                            bundle.putString("cellCountEachImage", cellEachImage);
                            bundle.putString("infectedCountEachImage", infectedEachImage);

                            // add the last one to string
                            if (cellEachImageGT != null && infectedEachImageGT != null) {
                                cellEachImageGT = cellEachImageGT + (cellCountManual + ",");
                                infectedEachImageGT = infectedEachImageGT + (infectedCountManual + ",");
                            } else {
                                cellEachImageGT = cellCountManual + ",";
                                infectedEachImageGT = infectedCountManual + ",";
                            }

                            bundle.putString("cellCountEachImageGT", cellEachImageGT);
                            bundle.putString("infectedCountEachImageGT", infectedEachImageGT);

                            patientInfoIntent.putExtras(bundle);
                            startActivity(patientInfoIntent);
                            finish();

                        }
                    }
            );
        }

        values_title = getResources().getStringArray(R.array.count_item);

        intent = getIntent();
        bundle = intent.getExtras();

        WB = intent.getStringExtra("WB");
        SVM_Th = Double.valueOf(intent.getStringExtra("Th"));
        totalCellNeeded = Integer.valueOf(intent.getStringExtra("totalcell"));

        nameEachImage = intent.getStringExtra("nameStringEachImage");
        cellEachImage = intent.getStringExtra("cellCountEachImage");
        infectedEachImage = intent.getStringExtra("infectedCountEachImage");

        cellEachImageGT = intent.getStringExtra("cellCountEachImageGT");
        infectedEachImageGT = intent.getStringExtra("infectedCountEachImageGT");

        Log.d(TAG, "cellEachImageGT: " + cellEachImageGT);
        Log.d(TAG, "infectedEachImageGT: " + infectedEachImageGT);

        processingTime = Long.valueOf(intent.getStringExtra("time"));

        // get pic File
        picFile = intent.getStringExtra("picFile");

        //picturePath = bundle.getString("imagePath");

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_result);
        toolbar.setTitle(R.string.title_result);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setDisplayShowHomeEnabled(false);

        listView_cells = (ListView) findViewById(R.id.listView_counts);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressText = (TextView) findViewById(R.id.textView_progress);

        imageView = (TouchImageView) findViewById(R.id.processed);
        matrix = new Matrix();

        continueButton = (Button) findViewById(R.id.continueButton);

        continueButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {

                        if (picFile != null) {
                            // output result image & write log file
                            createDirectoryAndSaveFile2(canvasBitmap);
                            writeLogFile();
                        }

                        if (cellTotal < totalCellNeeded) {
                            canvasBitmap.recycle();
                            Intent returnIntent = new Intent();

                            returnIntent.putExtra("cellsCountManual", cellCountManual);
                            returnIntent.putExtra("infectedCountManual", infectedCountManual);

                            setResult(Activity.RESULT_OK, returnIntent);
                            finish();
                        } else {
                            canvasBitmap.recycle();

                            finishActivity(REQUEST_CAM);

                            Intent PatientInfoIntent = new Intent(view.getContext(), PatientInfoActivity.class);
                            bundle.putString("nameStringEachImage", nameEachImage);
                            bundle.putString("cellCountEachImage", cellEachImage);
                            bundle.putString("infectedCountEachImage", infectedEachImage);

                            // add the last one to string
                            if (cellEachImageGT != null && infectedEachImageGT != null) {
                                cellEachImageGT = cellEachImageGT + (cellCountManual + ",");
                                infectedEachImageGT = infectedEachImageGT + (infectedCountManual + ",");
                            } else {
                                cellEachImageGT = cellCountManual + ",";
                                infectedEachImageGT = infectedCountManual + ",";
                            }

                            bundle.putString("cellCountEachImageGT", cellEachImageGT);
                            bundle.putString("infectedCountEachImageGT", infectedEachImageGT);

                            PatientInfoIntent.putExtras(bundle);
                            startActivity(PatientInfoIntent);
                            finish();
                        }

                    }
                }
        );

        byte[] byteArray = bundle.getByteArray("resImage");
        Bitmap bmp_res = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        canvasBitmap = bmp_res.copy(Bitmap.Config.ARGB_8888, true); // set the second parameter put it to mutable
        bmp_res.recycle();
        imageView.changeEnlargedFlag(true);
        imageView.setImageBitmap(canvasBitmap);
        imageView.setMaxZoom(10.0f);

        // get original image and resize it for display
        float RV = bundle.getFloat("RV");
        boolean takenFromCam = bundle.getBoolean("cam");
        int orientation = bundle.getInt("Orientation");

        Log.d(TAG, "picFile: " + picFile);
        Mat oriSizeMat = UtilsCustom.oriSizeMat;
        //Mat oriSizeMat = Imgcodecs.imread(picFile, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Log.d(TAG, "oriSizeMat: " + oriSizeMat.size());
        //Imgproc.cvtColor(oriSizeMat, oriSizeMat, Imgproc.COLOR_BGR2RGB);
        int width = (int) ((float) oriSizeMat.cols() / RV);
        int height = (int) ((float) oriSizeMat.rows() / RV);
        Mat resizedMat = new Mat();
        Imgproc.resize(oriSizeMat, resizedMat, new Size(width, height), 0, 0, Imgproc.INTER_CUBIC);
        smallOriBitmap = Bitmap.createBitmap(resizedMat.width(), resizedMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(resizedMat, smallOriBitmap);

        if (takenFromCam) {

            Matrix m = new Matrix(); // rotate image according to phone orientation when image was taken
            if (orientation == Surface.ROTATION_0) {
                m.postRotate(90);
            } else if (orientation == Surface.ROTATION_270) {
                m.postRotate(180);
            } else if (orientation == Surface.ROTATION_180) {
                m.postRotate(270);
            } else if (orientation == Surface.ROTATION_90) {
                m.postRotate(0);
            }
            smallOriBitmap = Bitmap.createBitmap(smallOriBitmap, 0, 0, resizedMat.width(), resizedMat.height(), m, false);
        }

        final Switch imageSwitch = (Switch) findViewById(R.id.switch_image);
        imageSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    imageSwitch.setText(R.string.result_image_switch);
                    imageView.setImageBitmap(smallOriBitmap);

                } else {
                    imageSwitch.setText(R.string.original_image_switch);
                    imageView.setImageBitmap(canvasBitmap);
                }
            }
        });

        //set up listView
        cellCurrent = Integer.valueOf(intent.getStringExtra("cellCountC"));
        infectedCurrent = Integer.valueOf(intent.getStringExtra("infectedCountC"));

        cellTotal = Integer.valueOf(intent.getStringExtra("cellTotal")) + cellCurrent;
        infectedTotal = Integer.valueOf(intent.getStringExtra("infectedTotal")) + infectedCurrent;

        values_cells[0] = cellCurrent;
        values_cells[1] = cellTotal;
        values_infectedcells[0] = infectedCurrent;
        values_infectedcells[1] = infectedTotal;

        for (int i = 0; i < values_cells.length; i++) {
            RowItem_CountsNtexts item = new RowItem_CountsNtexts(values_title[i], values_cells[i], values_infectedcells[i], R.string.cells, R.string.infectedcells);
            rowItemCellCount.add(item);
        }

        CustomAdapter_Counts adapter_cellCount = new CustomAdapter_Counts(this, rowItemCellCount);
        listView_cells.setAdapter(adapter_cellCount);

        progressStatus = cellTotal;
        progressBar.setProgress(progressStatus);
        progressBar.setMax(totalCellNeeded);
        progressText.setText(cellTotal + "/" + totalCellNeeded);

        // when get enough cells
        if (cellTotal > totalCellNeeded) {
            continueButton.setText(R.string.finish_button);
            continueButton.setTextColor(Color.parseColor("red"));
            continueButton.setTextSize(20);
            continueButton.setTypeface(continueButton.getTypeface(), Typeface.BOLD);
            String string = getResources().getString(R.string.enough_cells);
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
        }

        UtilsCustom.oriSizeMat.release();

    }

    private void createDirectoryAndSaveFile2(Bitmap imageToSave) {

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        // get image name
        String imgStr = picFile.toString().substring(picFile.toString().lastIndexOf("/") + 1);
        int endIndex = imgStr.lastIndexOf(".");
        String imageName = imgStr.substring(0, endIndex);

        File file = new File(new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New"), imageName + "_result.png");

        if (file.exists()) {
            file.delete();
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            imageToSave.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    outText.write(("ImageName,WhiteBalance,SVMThreshold,ProcessingTime(sec)").getBytes());
                    outText.write(("\n").getBytes());
                }

                // get image name
                String imgStr = picFile.toString().substring(picFile.toString().lastIndexOf("/") + 1);
                int endIndex = imgStr.lastIndexOf(".");
                String imageName = imgStr.substring(0, endIndex);

                outText.write((imageName + "," + WB + "," + SVM_Th + "," + processingTime).getBytes());
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
        File imgFile = new File(Dir, "Log.txt");
        if (!imgFile.exists()) {
            imgFile.createNewFile();
        }

        return imgFile;
    }

    @Override
    /*public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (picFile != null) {
                    // delete saved pics
                    File fdelete = new File(picFile);

                    int endIndex = picFile.toString().lastIndexOf(".");
                    String maskStr = picFile.toString().substring(0, endIndex);
                    maskStr = maskStr + "_mask.png";
                    File mdelete = new File(maskStr);
                    Log.d(TAG, "mdelete: " + mdelete);

                    if (fdelete.exists()) {
                        fdelete.delete();
                    }

                    if (mdelete.exists()) {
                        mdelete.delete();
                    }

                    Toast.makeText(getApplicationContext(), "User rejects result, image files deleted.", Toast.LENGTH_LONG).show();

                }

                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

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

                    int endIndex = picFile.toString().lastIndexOf(".");
                    String maskStr = picFile.toString().substring(0, endIndex);
                    maskStr = maskStr + "_mask.png";
                    File mdelete = new File(maskStr);

                    if (fdelete.exists()) {
                        fdelete.delete();
                    }

                    if (mdelete.exists()) {
                        mdelete.delete();
                    }

                    // delete last auto count in strings
                    String[] nameEI = nameEachImage.split(",");
                    String[] cellEI = cellEachImage.split(",");
                    String[] infectedEI = infectedEachImage.split(",");

                    String nameEachImageTemp = null;
                    String cellEachImageTemp = null;
                    String infectedEachImageTemp = null;

                    for (int i=0;i<cellEI.length-1;i++){
                        if (cellEachImageTemp != null && infectedEachImageTemp != null) { // when it's the second image
                            cellEachImageTemp = cellEachImageTemp + (cellEI[i] + ",");
                            infectedEachImageTemp = infectedEachImageTemp + (infectedEI[i] + ",");
                            nameEachImageTemp = nameEachImageTemp + (nameEI[i] + ",");
                        } else {
                            cellEachImageTemp = cellEI[i] + ",";
                            infectedEachImageTemp = infectedEI[i] + ",";
                            nameEachImageTemp = nameEI[i] + ",";
                        }
                    }

                    returnIntent.putExtra("nameStringEachImage", nameEachImageTemp);
                    returnIntent.putExtra("cellCountEachImage", cellEachImageTemp);
                    returnIntent.putExtra("infectedCountEachImage", infectedEachImageTemp);

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

        if (id == R.id.action_endSession) {

            if (picFile != null) {
                // output result image & write log file
                createDirectoryAndSaveFile2(canvasBitmap);
                writeLogFile();
            }

            canvasBitmap.recycle();

            finishActivity(REQUEST_CAM);

            Intent patientInfoIntent = new Intent(getBaseContext(), PatientInfoActivity.class);
            bundle.putString("nameStringEachImage", nameEachImage);
            bundle.putString("cellCountEachImage", cellEachImage);
            bundle.putString("infectedCountEachImage", infectedEachImage);

            // add the last one to string
            if (cellEachImageGT != null && infectedEachImageGT != null) {
                cellEachImageGT = cellEachImageGT + (cellCountManual + ",");
                infectedEachImageGT = infectedEachImageGT + (infectedCountManual + ",");
            } else {
                cellEachImageGT = cellCountManual + ",";
                infectedEachImageGT = infectedCountManual + ",";
            }

            bundle.putString("cellCountEachImageGT", cellEachImageGT);
            bundle.putString("infectedCountEachImageGT", infectedEachImageGT);

            patientInfoIntent.putExtras(bundle);
            startActivity(patientInfoIntent);
            finish();

        } else if (id == R.id.action_manualCounts) {

            final Dialog dialog_cellcounts = new Dialog(this);
            dialog_cellcounts.setContentView(R.layout.input_box_manualcounts);
            dialog_cellcounts.setCancelable(false);
            dialog_cellcounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            TextView textView_cellcounts = (TextView) dialog_cellcounts.findViewById(R.id.textView_manualcounts);
            final EditText input_cellcount = (EditText) dialog_cellcounts.findViewById(R.id.editText_manualcounts);
            final String[] cellCount = new String[1];
            Button button_cellcounts = (Button) dialog_cellcounts.findViewById(R.id.button_okay);
            Button buttonCancel_cellcounts = (Button) dialog_cellcounts.findViewById(R.id.button_cancel);

            final Dialog dialog_infectedcounts = new Dialog(this);
            dialog_infectedcounts.setCancelable(false);
            dialog_infectedcounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog_infectedcounts.setContentView(R.layout.input_box_manualcounts);
            TextView textView_infectedcounts = (TextView) dialog_infectedcounts.findViewById(R.id.textView_manualcounts);
            final EditText input_infected = (EditText) dialog_infectedcounts.findViewById(R.id.editText_manualcounts);
            final String[] infectedCount = new String[1];
            Button button_infectedcounts = (Button) dialog_infectedcounts.findViewById(R.id.button_okay);
            Button buttonCancel_infectedcounts = (Button) dialog_infectedcounts.findViewById(R.id.button_cancel);

            textView_cellcounts.setText(R.string.manual_cell_count);
            textView_infectedcounts.setText(R.string.manual_count_infected);

            button_cellcounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            cellCount[0] = input_cellcount.getText().toString();

                            dialog_infectedcounts.show();
                            dialog_cellcounts.dismiss();
                        }
                    }
            );

            buttonCancel_cellcounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog_cellcounts.dismiss();
                        }
                    }
            );

            button_infectedcounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            infectedCount[0] = input_infected.getText().toString();

                            if (cellCount[0].isEmpty()) {
                                cellCount[0] = "N/A";
                            }

                            if (infectedCount[0].isEmpty()) {
                                infectedCount[0] = "N/A";
                            }

                            //saveCounts2Txt(cellCount[0], infectedCount[0]);

                            cellCountManual = cellCount[0];
                            infectedCountManual = infectedCount[0];

                            dialog_infectedcounts.dismiss();
                        }
                    }
            );

            buttonCancel_infectedcounts.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog_infectedcounts.dismiss();
                        }
                    }
            );

            dialog_cellcounts.show();


//            alert_cellcount.setView(input_cellcount);
//            alert_cellcount.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int whichButton) {
//                    //Put actions for OK button here
//                    cellCount[0] = input_cellcount.getText().toString();
//
//                    input_infected.setInputType(InputType.TYPE_CLASS_NUMBER);
//                    input_infected.setRawInputType(Configuration.KEYBOARD_12KEY);
//                    alert_infected.setView(input_infected);
//                    alert_infected.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            //Put actions for OK button here
//                            infectedCount[0] = input_infected.getText().toString();
//
//                            saveCounts2Txt(cellCount[0], infectedCount[0]);
//                        }
//                    });
//                    alert_infected.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//                            //Put actions for CANCEL button here, or leave in blank
//                        }
//                    });
//                    alert_infected.show();
//                }
//            });
//            alert_cellcount.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                public void onClick(DialogInterface dialog, int whichButton) {
//                    //Put actions for CANCEL button here, or leave in blank
//                }
//            });
//            alert_cellcount.show();

        }

        return super.onOptionsItemSelected(item);
    }

    private void saveCounts2Txt(String cellCount, String infectedCount) {

        // get image name
        String imgStr = picFile.toString().substring(picFile.toString().lastIndexOf("/") + 1);
        int endIndex = imgStr.lastIndexOf(".");
        String imageName = imgStr.substring(0, endIndex);

        File textFile = null;

        try {

            textFile = createTextFile4Counts(imageName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textFile != null) {
            FileOutputStream outText = null;

            try {

                if (textFile.length() == 0) {

                    outText = new FileOutputStream(textFile, true);

                    outText.write((imageName + ".png " + cellCount + " " + infectedCount).getBytes());
                } else {

                    outText = new FileOutputStream(textFile, false);

                    outText.write((imageName + ".png " + cellCount + " " + infectedCount).getBytes());
                }


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

    private File createTextFile4Counts(String imageName) throws IOException {

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File Dir = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New");
        File txtFile = new File(Dir, imageName + ".txt");
        if (!txtFile.exists()) {
            txtFile.createNewFile();
        }

        return txtFile;
    }


}
