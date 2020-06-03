package gov.nih.nlm.malaria_screener.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.amlcurran.showcaseview.ShowcaseView;
//import com.github.amlcurran.showcaseview.targets.ViewTarget;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.TouchImageView;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsData;
import gov.nih.nlm.malaria_screener.frontEnd.ResultDisplayer;
import gov.nih.nlm.malaria_screener.frontEnd.ResultDisplayer_thickSmear;
import gov.nih.nlm.malaria_screener.imageProcessing.SVM_Classifier;
import gov.nih.nlm.malaria_screener.imageProcessing.TensorFlowClassifier;
import gov.nih.nlm.malaria_screener.frontEnd.SettingsActivity;
import gov.nih.nlm.malaria_screener.imageProcessing.ThickSmearProcessor;
import gov.nih.nlm.malaria_screener.imageProcessing.ThinSmearProcessor;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* *
* This activity class:
*   1. controls the UI for camera
*   2. sends image data for processing
*   3. start and send processed data to ResultDisplayer
*
*/
public class CameraActivity extends AppCompatActivity {

    private Camera cam;
    private CameraPreview camPreview;
    private FrameLayout frameLayout;
    private static final String TAG = "MyDebug";
    public static final int MEDIA_TYPE_IMAGE = 1;
    private boolean safeToTakePicture = false;

    public static final int REQUEST_GALLERY = 10;
    static final int REQUEST_RESULTS = 3;
    static final int REQUEST_SETTING = 4;

    private Activity context;

    private int captureCount = 0;
    private String patientId = "";
    private String slideId = "";
    public static final AlphaAnimation BUTTON_PRESS = new AlphaAnimation(1F, 0.0F);

    private TextView typeInfo;
    private TextView fieldInfo;
    private TextView countInfo;
    private TextView infectedCountInfo;
    private TextView parasiteInfo;
    private TextView wbcInfo;
    private String smearType = "Thin"; // for now
    private ImageButton captureButton;
    private ImageButton galleryButton;
    private View.OnClickListener onClickListener;
    private boolean segment = false;

    private ProgressDialog inProgress;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView progressText;

    private TouchImageView imageViewTaken;

    private ImageButton imageButton_NO;
    private ImageButton imageButton_YES;

    private ImageButton settingButton;

    float RV = 6; //resize value

    //Mat oriSizeMat;
    Mat resizedMat = new Mat();

    File pictureFileCopy;

    boolean takenFromCam = false; // flag for saving image taken from cam

    boolean inPreview = false; // flag for when image is taken and in preview mode, so the app won't listen to bluetooth remote

    String WB = "0";

    Bundle bundle;

    int totalCellNeeded = 1000;
    int totalWBCNeeded = 200;

    long processingTime = 0;

    int orientation; // save phone orientation when image taken

    public CharSequence[] cs;

    private boolean imageAcquisition = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");

        Log.d("ThreadUtils", getThreadSignature());

        Log.d(TAG, "Number of cores: " + Runtime.getRuntime().availableProcessors());

        // check to see if has Camera
        checkCameraHardware(this);

        // read pre-trained SVM data structure & TF deep learning model // put read SVM data structure & TF model here to reduce processing time
        readSVMHandler.sendEmptyMessage(0);

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        Log.d(TAG, "maxMemory: " + Long.toString(maxMemory / 1024 / 1024));

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.d(TAG, "memoryClass: " + Integer.toString(memoryClass));

        chooseSmearType();

    }

    // prompts a popup window for the user to choose the smear type
    private void chooseSmearType(){

        final String[] types = {"Thin", "Thick"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a smear type");
        builder.setItems(types, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                smearType = types[which];

                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("smeartype", smearType).commit();

                initAll();

                showCaseView();
            }
        });
        builder.show();

    }

    private void showCaseView(){

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = settings.getBoolean("firstTime_camera", true);

        if (firstTime) {

            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("firstTime_camera", false).apply();

            final ShowcaseView sv_camera = new ShowcaseView.Builder(this)
                    .withMaterialShowcase()
                    .setTarget(new ViewTarget(R.id.button_capture, this))
                    .setContentTitle(R.string.capture_btn_title)
                    .setContentText(R.string.capture_btn)
                    .setStyle(R.style.AppTheme)
                    .build();

            sv_camera.overrideButtonClick(
                    new Button.OnClickListener() {
                        public void onClick(View v) {
                            sv_camera.hide();

                        }
                    }
            );

            sv_camera.show();

        }
    }

    // load pre-trained classifier models
    private Handler readSVMHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Runnable r = new Runnable() {
                @Override
                public void run() {

                    long startTime_w = System.currentTimeMillis();

                    // load TF model
                    try {

                        // thin smear
                        String modelNameStr_thin = "malaria_thinsmear_44.h5.pb";
                        int TF_input_size_thin = 44;
                        String inputLayerNameStr_thin = "conv2d_20_input";
                        String outputLayerNameStr_thin = "output_node0";

                        UtilsCustom.tensorFlowClassifier_thin = TensorFlowClassifier.create(context.getAssets(), modelNameStr_thin, TF_input_size_thin, TF_input_size_thin, inputLayerNameStr_thin, outputLayerNameStr_thin);
                        //UtilsCustom.tensorFlowClassifier_thin = TensorFlowClassifier.create(context.getAssets(), "malaria_thinsmear.h5.pb", UtilsCustom.TF_input_size, "input_2", "output_node0");
                        //UtilsCustom.tfClassifier_lite = TFClassifier_Lite.create(context.getAssets(), "thinSmear_100_quantized.tflite", UtilsCustom.TF_input_size);

                        //thick smear
                        String modelNameStr_thick = "ThickSmearModel.h5.pb";
                        int TF_input_size_thick = 44;
                        String inputLayerNameStr_thick = "conv2d_1_input";
                        String outputLayerNameStr_thick = "output_node0";

                        UtilsCustom.tensorFlowClassifier_thick = TensorFlowClassifier.create(context.getAssets(), modelNameStr_thick, TF_input_size_thick, TF_input_size_thick, inputLayerNameStr_thick, outputLayerNameStr_thick);

                        //UtilsCustom.tensorFlowClassifier_thick = TensorFlowClassifier.create(context.getAssets(), "ThickSmearModel_7LayerConv.h5.pb", UtilsCustom.TF_input_size, "conv2d_1_input", "output_node0");

                        //for blur detection
                        //UtilsCustom.tensorFlowClassifier_fMeasure_thin = TensorFlowClassifier.create(context.getAssets(), "ThinSmear_7LayerConv_fMeasure_115x85.h5.pb", UtilsCustom.TF_input_width, UtilsCustom.TF_input_height, "conv2d_1_input", "output_node0");


                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //load SVM model
                    UtilsCustom.svm_classifier = SVM_Classifier.create(context);

                    int classNum = 2;
                    for (int index = 0; index < classNum; index++) {
                        UtilsCustom.svm_classifier.readSVMTextFile(index);
                    }

                    long endTime_w = System.currentTimeMillis();
                    long totalTime_w = endTime_w - startTime_w;
                    Log.d(TAG, "Read Classifier Time: " + totalTime_w);

                }
            };

            Thread readSVMThread = new Thread(r);
            readSVMThread.start();

        }
    };


    private static String digitFormat(int num) {
        String str = "" + num;
        //if (num < 10)
        //    str = "0" + num;
        return str;
    }

    private void updateToolbar() {
        String captureStr = digitFormat(captureCount);
        String imgStr = getResources().getString(R.string.image_count);
        String cellCountStr = getResources().getString(R.string.cell_count);
        String infectedCountStr = getResources().getString(R.string.infected_count);
        String parasiteCountStr = getResources().getString(R.string.parasite_count);
        String wbcCountStr = getResources().getString(R.string.wbc_count);
        //pidInfo.setText("PID: " + patientId);
        //sidInfo.setText("SID: " + slideId);

        fieldInfo.setText(imgStr + captureStr);

        if (smearType.equals("Thin")) {
            typeInfo.setText(R.string.smear_type);
            countInfo.setText(cellCountStr + UtilsData.cellTotal);
            infectedCountInfo.setText(infectedCountStr + UtilsData.infectedTotal);
            //update progress bar
            progressStatus = UtilsData.cellTotal;
            progressBar.setProgress(progressStatus);
            progressText.setText(UtilsData.cellTotal + "/" + totalCellNeeded);
        } else if (smearType.equals("Thick")) {
            typeInfo.setText(R.string.smear_type1);
            parasiteInfo.setText(parasiteCountStr + UtilsData.parasiteTotal);
            wbcInfo.setText(wbcCountStr + UtilsData.WBCTotal);
            //update progress bar
            progressStatus = UtilsData.WBCTotal;
            progressBar.setProgress(progressStatus);
            progressText.setText(UtilsData.WBCTotal + "/" + totalWBCNeeded);
        }
    }

    public void initAll() {

        setContentView(R.layout.camera_preview); // set layout to camera preview

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // smear type
        smearType = sharedPreferences.getString("smeartype", "Thin");

        // set up patient, slide,& field info
        int textSize = 20;

        typeInfo = (TextView) findViewById(R.id.type);
        typeInfo.setTextSize(textSize);
        typeInfo.setSingleLine(true);
        typeInfo.setTextColor(getResources().getColor(R.color.toolbar_text));

        // capture number
        fieldInfo = (TextView) findViewById(R.id.field);
        fieldInfo.setSingleLine(true);
        fieldInfo.setTextSize(textSize);
        fieldInfo.setTextColor(getResources().getColor(R.color.toolbar_text));

        // set up progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(progressStatus);
        progressText = (TextView) findViewById(R.id.textView_progress);

        if (smearType.equals("Thin")) {
            // set up count and infected count text views
            countInfo = (TextView) findViewById(R.id.count1);
            countInfo.setSingleLine(true);
            countInfo.setTextSize(textSize);
            countInfo.setTextColor(getResources().getColor(R.color.toolbar_text));

            infectedCountInfo = (TextView) findViewById(R.id.count2);
            infectedCountInfo.setSingleLine(true);
            infectedCountInfo.setTextSize(textSize);
            infectedCountInfo.setTextColor(getResources().getColor(R.color.toolbar_text));

            progressBar.setMax(totalCellNeeded);

        } else if (smearType.equals("Thick")) {
            parasiteInfo = (TextView) findViewById(R.id.count1);
            parasiteInfo.setSingleLine(true);
            parasiteInfo.setTextSize(textSize);
            parasiteInfo.setTextColor(getResources().getColor(R.color.toolbar_text));

            wbcInfo = (TextView) findViewById(R.id.count2);
            wbcInfo.setSingleLine(true);
            wbcInfo.setTextSize(textSize);
            wbcInfo.setTextColor(getResources().getColor(R.color.toolbar_text));

            progressBar.setMax(totalWBCNeeded);
        }

        context = this;
        cam = getCamera();

        // set camera parameters
        Camera.Parameters parameters = cam.getParameters();

        //---------------------------temp cam res--------------------------------------------
        // choose the highest resolution to take picture
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();

        int tempSize;
        int maxSize = 0;
        int maxWidth = 0; int maxHeight = 0;
        for (Camera.Size s: pictureSizes){

            int width = s.width;
            int height = s.height;

            tempSize = width * height;

            if (tempSize > maxSize){
                maxSize = tempSize;
                maxWidth = width; maxHeight = height;
            }

            Log.d(TAG, "pictureSizes: " + s.width + "x" + s.height);
        }

        Log.d(TAG, "maxSizes: " + maxSize);
        parameters.setPictureSize(maxWidth, maxHeight);

        double picRatio = (double) maxWidth / (double) maxHeight;
        Log.d(TAG, "picRatio: " + picRatio);

        writeToCamResFile(pictureSizes, maxWidth, maxHeight);
        //---------------------------temp cam res--------------------------------------------

        parameters.setJpegQuality(100);
        Log.d(TAG, "JpegQuality: " + parameters.getJpegQuality());

        int exposure = sharedPreferences.getInt("exposure_compensation", 0);

        int maxExposure = parameters.getMaxExposureCompensation();
        int minExposure = parameters.getMinExposureCompensation();

        UtilsCustom.maxExposure = maxExposure;
        UtilsCustom.minExposure = minExposure;

        parameters.setExposureCompensation(exposure);

        // get supported white balance options
        List<String> whitelist = parameters.getSupportedWhiteBalance();

        cs = new String[whitelist.size()];
        for (int i = 0; i < whitelist.size(); i++) {
            cs[i] = whitelist.get(i);
        }

        // record white balance options for setting
        bundle = new Bundle();
        bundle.putCharSequenceArray("WB_list", cs);

        // get setting from preference

        WB = sharedPreferences.getString("whitebalance", "0");

        UtilsCustom.whichClassifier = Integer.valueOf(sharedPreferences.getString("classifier", "0"));

        double value = sharedPreferences.getInt("Th", 50);
        UtilsCustom.Th = (100 - value) / 100;

        totalCellNeeded = sharedPreferences.getInt("celltotal", 1000);

        totalWBCNeeded = sharedPreferences.getInt("wbc_th", 200);

        imageAcquisition = sharedPreferences.getBoolean("image_acquire", false);

        parameters.setWhiteBalance(whitelist.get(Integer.valueOf(WB)));

        cam.setParameters(parameters);

        // Create preview and set to content of activity
        camPreview = new CameraPreview(this, cam, picRatio);
        safeToTakePicture = true;
        frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        frameLayout.addView(camPreview);

        // set ratio of the framelayout the same as pic ratio
        ConstraintSet set = new ConstraintSet();
        ConstraintLayout layout = (ConstraintLayout) findViewById(R.id.cam_activity_layout);
        set.clone(layout);
        String ratio = String.valueOf(1/picRatio);
        set.setDimensionRatio(frameLayout.getId(), ratio);
        set.applyTo(layout);

        updateToolbar();

        // set up capture button
        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // take picture!
                if (safeToTakePicture) {

                    // save phone orientation when image taken
                    WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    Display display = wm.getDefaultDisplay();
                    orientation = display.getRotation();

                    Thread takePicThread = new Thread(new Runnable() {
                        public void run() {
                            cam.takePicture(shutterCallback, null, pictureCallback);
                            //Log.d(TAG, "Taking picture...");
                            safeToTakePicture = false;
                        }
                    });
                    takePicThread.start();

                }
            }
        };

        captureButton = (ImageButton) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(onClickListener);

        galleryButton = (ImageButton) findViewById(R.id.button_gallery);
        galleryButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, REQUEST_GALLERY);
                    }
                }
        );

        settingButton = (ImageButton) findViewById(R.id.settingButton);
        settingButton.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent settingIntent = new Intent(context, SettingsActivity.class);

                        settingIntent.putExtras(bundle);

                        startActivityForResult(settingIntent, REQUEST_SETTING);
                    }
                }
        );

    }

    // shutter
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };

    Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {

        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // when return from Gallery event
        if (requestCode == REQUEST_GALLERY && resultCode == Activity.RESULT_OK) {

            String str1 = getResources().getString(R.string.image_process1);
            String str2 = getResources().getString(R.string.image_process2);

            // show in progress sign
            inProgress = ProgressDialog.show(this, str1, str2, false, false);
            //inProgress.show();

            Uri selectedImageUri = data.getData();

            String[] filepath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImageUri, filepath, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filepath[0]);
            final String picturePath = cursor.getString(columnIndex);
            cursor.close();

            //create image file for saving
            final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, patientId, slideId, captureCount, smearType);
            pictureFileCopy = pictureFile;

            Runnable r = new Runnable() {
                @Override
                public void run() {

                    //UtilsCustom.oriSizeMat = Imgcodecs.imread(picturePath, -1);
                    UtilsCustom.oriSizeMat = Imgcodecs.imread(picturePath, Imgcodecs.CV_LOAD_IMAGE_COLOR);

                    Imgproc.cvtColor(UtilsCustom.oriSizeMat, UtilsCustom.oriSizeMat, Imgproc.COLOR_BGR2RGB);

                    Log.d(TAG, "oriSizeMat: " + UtilsCustom.oriSizeMat);

                    // blurry detection
                    //blurFlag = BlurDetection.computeBlur();

                    resizeImage();

                    if (smearType.equals("Thin")) {
                        ProcessThinSmearImage();
                    } else if (smearType.equals("Thick")) {
                        ProcessThickSmearImage();
                    }
                }
            };

            Thread imgprocessThread = new Thread(r);
            imgprocessThread.start();

        } else if (requestCode == REQUEST_RESULTS && resultCode == Activity.RESULT_OK) {
         // when return from ResultDisplayer with RESULT_OK
            captureCount++;

            updateToolbar();

        } else if (requestCode == REQUEST_RESULTS && resultCode == Activity.RESULT_CANCELED) {
            // when return from ResultDisplayer with RESULT_CANCELED
        }

    }

    // camera picture callback.
    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(final byte[] data, Camera camera) {
            //Log.d(TAG, "Picture taken.");
            captureButton.setOnClickListener(null);

            final File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE, patientId, slideId, captureCount, smearType);
            camera.startPreview();
            if (pictureFile == null) {
                //Log.d(TAG, "Error creating media file, check storage permissions. ");
                safeToTakePicture = true;
                return;
            }

            takenFromCam = true;

            pictureFileCopy = pictureFile;

            String str1 = getResources().getString(R.string.image_process1);
            String str2 = getResources().getString(R.string.image_process2);
            inProgress = ProgressDialog.show(context, str1, str2, false, false);

            Runnable r = new Runnable() {
                @Override
                public void run() {

                    Mat jpegData = new Mat(1, data.length, CvType.CV_8UC1);
                    jpegData.put(0, 0, data);

                    //UtilsCustom.oriSizeMat = Imgcodecs.imdecode(jpegData, -1); // produce a 3 channel bgr image
                    UtilsCustom.oriSizeMat = Imgcodecs.imdecode(jpegData, Imgcodecs.CV_LOAD_IMAGE_COLOR);

                    Log.d(TAG, "UtilsCustom.oriSizeMat: " + UtilsCustom.oriSizeMat);

                    Imgproc.cvtColor(UtilsCustom.oriSizeMat, UtilsCustom.oriSizeMat, Imgproc.COLOR_BGR2RGB);

                    resizeImage();

                    inProgress.dismiss();

                    messageHandler.sendEmptyMessage(0);

                }
            };

            Thread imgpreviewThread = new Thread(r);
            imgpreviewThread.start();

            safeToTakePicture = true;
            //Log.d(TAG, "Camera safe to use again");
        }
    };

    private void resizeImage() {

        float ori_height = 2988;
        float ori_width = 5312;
        float cur_height = UtilsCustom.oriSizeMat.height();
        float cur_width = UtilsCustom.oriSizeMat.width();

        float scaleFactor = (float) Math.sqrt((ori_height * ori_width) / (cur_height * cur_width));  // size ratio between 5312/2988 and current images

        RV = 6 / scaleFactor; // SF for the current images
        //Log.d(TAG, "RV: " + RV);

        int width = (int) ((float) UtilsCustom.oriSizeMat.cols() / RV);
        int height = (int) ((float) UtilsCustom.oriSizeMat.rows() / RV);

        Imgproc.resize(UtilsCustom.oriSizeMat, resizedMat, new Size(width, height), 0, 0, Imgproc.INTER_CUBIC);

    }

    private Handler messageHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            previewImage();
        }
    };

    public void previewImage() {

        //inProgress.dismiss();

        // preview image
        setContentView(R.layout.taken_image_preview);

        imageViewTaken = findViewById(R.id.imageView_preview);
        imageButton_NO = findViewById(R.id.imageButton_no);
        imageButton_YES = findViewById(R.id.imageButton_yes);

        //for blur detection-----------------------------------------------
        /*TextView textView_blur = findViewById(R.id.textView_blur);

        if (blurFlag){
            textView_blur.setText("Blurry.");
        }else {
            textView_blur.setText("Sharp.");
        }*/

        /*Bitmap rotatedBitmap = Bitmap.createBitmap(UtilsCustom.rectMat.width(), UtilsCustom.rectMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(UtilsCustom.rectMat, rotatedBitmap);*/
        //for blur detection-----------------------------------------------

        Bitmap rotatedBitmap = Bitmap.createBitmap(resizedMat.width(), resizedMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(resizedMat, rotatedBitmap);

        // rotate preview image according to phone orientation when image was taken
        Matrix m = new Matrix();
        if (orientation == Surface.ROTATION_0) {
            m.postRotate(90);
        } else if (orientation == Surface.ROTATION_270) {
            m.postRotate(180);
        } else if (orientation == Surface.ROTATION_180) {
            m.postRotate(270);
        } else if (orientation == Surface.ROTATION_90) {
            m.postRotate(0);
        }

        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), m, false);
        imageViewTaken.setImageBitmap(rotatedBitmap);

        imageButton_YES.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        inPreview = false;

                        String str1 = getResources().getString(R.string.image_process1);
                        String str2 = getResources().getString(R.string.image_process2);

                        // show in progress sign
                        inProgress = ProgressDialog.show(context, str1, str2, false, false);
                        // inProgress.show();

                        Runnable r = new Runnable() {
                            @Override
                            public void run() {

                                if (smearType.equals("Thin")) {

                                    if (imageAcquisition) {
                                        ImageAcquisition();
                                    } else {

                                        ProcessThinSmearImage();
                                    }
                                } else if (smearType.equals("Thick")) {
                                    if (imageAcquisition) {
                                        ImageAcquisition_thick();
                                    } else {
                                        ProcessThickSmearImage();
                                    }
                                }
                            }
                        };

                        Thread imgprocessThread = new Thread(r);
                        imgprocessThread.start();

                    }
                }
        );

        imageButton_NO.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        inPreview = false;
                        takenFromCam = false;
                        initAll();
                    }
                }
        );

    }

    // ************* This section executes when the user is in image acquisition mode **************

    // save image, then go to ResultDisplayer
    private void ImageAcquisition() {

        UtilsData.addCellCount("N/A");
        UtilsData.addInfectedCount("N/A");

        processingTime = 0;

        resizedMat.release();

        // set Bitmap to paint
        UtilsCustom.canvasBitmap = Bitmap.createBitmap(UtilsCustom.oriSizeMat.width(), UtilsCustom.oriSizeMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(UtilsCustom.oriSizeMat, UtilsCustom.canvasBitmap);

        saveImageHandler.sendEmptyMessage(0);

        goToNextActivity();

    }

    private void ImageAcquisition_thick() {

        UtilsData.addParasiteCount("N/A");
        UtilsData.addWBCCount("N/A");

        processingTime = 0;

        // set Bitmap to paint
        UtilsCustom.canvasBitmap = Bitmap.createBitmap(UtilsCustom.oriSizeMat.width(), UtilsCustom.oriSizeMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(UtilsCustom.oriSizeMat, UtilsCustom.canvasBitmap);

        saveImageHandler.sendEmptyMessage(0);
        goToNextActivity_thickSmear();
    }
    //  ********************************** image acquisition mode **********************************


    private void ProcessThinSmearImage() {

        long startTime = System.currentTimeMillis();

        ThinSmearProcessor thinSmearProcessor = new ThinSmearProcessor();
        int[] res = thinSmearProcessor.processImage(resizedMat, orientation, RV, takenFromCam, pictureFileCopy);

        if (res == null){

            Log.d(TAG, "Here");

            inProgress.dismiss();
            retakeHandler.sendEmptyMessage(0);

        } else {

            saveResults(res[0], res[1]);

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            Log.d(TAG, "Single Image Processing Time: " + totalTime);
            processingTime = totalTime;

            //save image to file
            //saveOriImage(); // taken out of handler, otherwise original image not saved before needed in next activity. 09/26/2017
            saveImageHandler.sendEmptyMessage(0);                // put in handler again, original image is copied for saving, display in result page using image in memory. 03/12/2019
            //if (takenFromCam) {

            //}

            goToNextActivity();

            System.gc();
            Runtime.getRuntime().gc();
        }

    }

    private void ProcessThickSmearImage() {

        long startTime_w = System.currentTimeMillis();

        ThickSmearProcessor thickSmearProcessor = new ThickSmearProcessor(UtilsCustom.oriSizeMat);
        int[] res = thickSmearProcessor.processImage();

        saveResults_thick(res[0], res[1]);

        long endTime_w = System.currentTimeMillis();
        long totalTime_w = endTime_w - startTime_w;
        Log.d(TAG, "One image time: " + totalTime_w);
        processingTime = totalTime_w;

        saveImageHandler.sendEmptyMessage(0);

        goToNextActivity_thickSmear();
    }

    private void saveResults(int infectedCurrent, int cellCurrent) {

        UtilsData.cellCurrent = cellCurrent;
        UtilsData.infectedCurrent = infectedCurrent;
        UtilsData.cellTotal = UtilsData.cellTotal + cellCurrent;
        UtilsData.infectedTotal = UtilsData.infectedTotal + infectedCurrent;
        UtilsData.addCellCount(String.valueOf(cellCurrent));
        UtilsData.addInfectedCount(String.valueOf(infectedCurrent));

    }

    private void saveResults_thick(int parasiteNum, int wbc_num){

        UtilsData.parasiteCurrent = parasiteNum;
        UtilsData.WBCCurrent = wbc_num;
        UtilsData.parasiteTotal = UtilsData.parasiteTotal + parasiteNum;
        UtilsData.WBCTotal = UtilsData.WBCTotal + wbc_num;
        UtilsData.addParasiteCount(String.valueOf(parasiteNum));
        UtilsData.addWBCCount(String.valueOf(wbc_num));
    }

    private void goToNextActivity() {

        inProgress.dismiss();

        Intent intent = new Intent(context, ResultDisplayer.class);

        if (takenFromCam) {
            intent.putExtra("Orientation", orientation); // pass orientation when image was taken for next activity
            intent.putExtra("cam", takenFromCam);
            takenFromCam = false;
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
            UtilsCustom.canvasBitmap = Bitmap.createBitmap(UtilsCustom.canvasBitmap, 0, 0, UtilsCustom.canvasBitmap.getWidth(), UtilsCustom.canvasBitmap.getHeight(), m, false);
        }


        intent.putExtra("picFile", pictureFileCopy.toString()); //pass original file dir
        intent.putExtra("RV", RV); // pass resize value of original image
        intent.putExtra("WB", cs[Integer.valueOf(WB)]); // white balance setting
        intent.putExtra("time", String.valueOf(processingTime));
        intent.putExtra("imgCount", captureCount + 1);

        String imgNameStr = pictureFileCopy.toString().substring(pictureFileCopy.toString().lastIndexOf("/") + 1);
        UtilsData.addImageName(imgNameStr);

        System.gc();
        Runtime.getRuntime().gc();

        startActivityForResult(intent, REQUEST_RESULTS);

    }

    private void goToNextActivity_thickSmear() {

        inProgress.dismiss();

        Intent intent = new Intent(context, ResultDisplayer_thickSmear.class);

        // pass resized result image to new activity
        int width = (int) ((float) UtilsCustom.canvasBitmap.getWidth() / RV);
        int height = (int) ((float) UtilsCustom.canvasBitmap.getHeight() / RV);

        UtilsCustom.canvasBitmap = Bitmap.createScaledBitmap(UtilsCustom.canvasBitmap, width, height, false);

        if (takenFromCam) {
            intent.putExtra("Orientation", orientation); // pass orientation when image was taken for next activity
            intent.putExtra("cam", takenFromCam);
            takenFromCam = false;
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
            UtilsCustom.canvasBitmap = Bitmap.createBitmap(UtilsCustom.canvasBitmap, 0, 0, UtilsCustom.canvasBitmap.getWidth(), UtilsCustom.canvasBitmap.getHeight(), m, false);
        }

        // pass image names
        String imgNameStr = pictureFileCopy.toString().substring(pictureFileCopy.toString().lastIndexOf("/") + 1);
        UtilsData.addImageName(imgNameStr);

        intent.putExtra("picFile", pictureFileCopy.toString());
        intent.putExtra("RV", RV); // pass resize value of original image
        intent.putExtra("WB", cs[Integer.valueOf(WB)]); // white balance setting
        intent.putExtra("time", String.valueOf(processingTime));
        intent.putExtra("imgCount", captureCount + 1);

        System.gc();
        Runtime.getRuntime().gc();

        startActivityForResult(intent, REQUEST_RESULTS);
    }

    private Handler saveImageHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Runnable r1 = new Runnable() {
                @Override
                public void run() {

                    saveOriImage();

                }
            };

            Thread saveImgThread = new Thread(r1);
            saveImgThread.start();

        }
    };

    public void saveOriImage() {

        long startTime = System.currentTimeMillis();

        Mat oriSizeMat_clone = UtilsCustom.oriSizeMat.clone();

        String file_name = pictureFileCopy.toString();
        Imgproc.cvtColor(oriSizeMat_clone, oriSizeMat_clone, Imgproc.COLOR_RGB2BGR);

        ArrayList<Integer> parameters = new ArrayList<>();
        parameters.add(Imgcodecs.CV_IMWRITE_PNG_COMPRESSION);
        parameters.add(3);
        MatOfInt matOfInt = new MatOfInt();
        matOfInt.fromList(parameters);

        Imgcodecs.imwrite(file_name, oriSizeMat_clone, matOfInt);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.d(TAG, "save Pic Time: " + totalTime);

        oriSizeMat_clone.release();

    }

    // handle the event to retake image when the program rejects the captured image
    private Handler retakeHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            retakeImage();
        }
    };

    private void retakeImage() {

        takenFromCam = false;

        String string = getResources().getString(R.string.seg_failed);
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();

        initAll();

    }

    // Create image file to be saved. Image name is current time stamp + smearType
    private static File getOutputMediaFile(int type, String pid, String sid, int num, String smearType) {

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/New");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MalariaPics", "failed to create directory");
                return null;
            }
        }
        String fieldNum = digitFormat(num);

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    timeStamp + "_" + smearType + ".png");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");

        if (cam == null) {
            Log.d(TAG, "here.");
            initAll();
        }

        // tutorial page
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int secondTime = settings.getInt("firstSecondTime_cam", 0);
        if (secondTime == 1) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("firstSecondTime_cam", 2).commit();

        } else if (secondTime == 0) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("firstSecondTime_cam", 1).commit();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause");

        cam = null;

    }

    public void onStop() {
        super.onStop();
    }

    public static Camera getCamera() {
        Camera cam = null;
        try {
            cam = Camera.open();

        } catch (Exception e) {
            //Log.e(TAG, "Camera is not available (in use by another application)");
        }
        return cam;
    }

    /**
     * Check if this device has a camera
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            // this device has a camera
            //Log.e(TAG, "Device has camera");
            return true;
        } else {
            // no camera on this device
            String string = getResources().getString(R.string.no_cam);
            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_LONG).show();
            //Log.e(TAG, "No camera on this device!");
            finish();
            return false;
        }
    }

    // when user presses back button on the phone
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

                reset_utils_data();

                finish();

                String string = getResources().getString(R.string.quit_session_aborted);
                // Write your code here to invoke YES event
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

    // reset all relevant variables of when user exits a screening session
    private void reset_utils_data() {

        UtilsData.resetImageNames();

        // thin
        UtilsData.resetCurrentCounts();
        UtilsData.resetTotalCounts();
        UtilsData.resetCountLists();
        UtilsData.resetCountLists_GT();

        //thick
        UtilsData.resetCurrentCounts_thick();
        UtilsData.resetTotalCounts_thick();
        UtilsData.resetCountLists_thick();
        UtilsData.resetCountLists_GT_thick();

    }

    // give volume up button the function to trigger a capture event
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {

                    if (!inPreview) {
                        inPreview = true;
                        takePic();
                    }

                }
                return true;

            default:
                return super.dispatchKeyEvent(event);
        }
    }

    // this function execute a picture event when volume up button is pressed
    private void takePic() {
        // take picture!
        if (safeToTakePicture) {

            // save phone orientation when image taken
            WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            orientation = display.getRotation();

            Thread takePicThread = new Thread(new Runnable() {
                public void run() {
                    cam.takePicture(shutterCallback, null, pictureCallback);
                    //Log.d(TAG, "Taking picture...");
                    safeToTakePicture = false;
                }
            });
            takePicThread.start();

        }
    }

    public static String getThreadSignature() {
        Thread t = Thread.currentThread();
        long l = t.getId();
        String name = t.getName();
        long p = t.getPriority();
        String gname = t.getThreadGroup().getName();
        return (name
                + ":(id)" + l
                + ":(priority)" + p
                + ":(group)" + gname);
    }

    // this two functions below are temp func to output a txt file with cam resolution info
    private File createCamResTextFile() throws IOException {

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File Dir = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/");
        File imgFile = new File(Dir, "camera_resolution.txt");
        if (!imgFile.exists()) {
            imgFile.createNewFile();
        }

        return imgFile;
    }

    private void writeToCamResFile(List<Camera.Size> sizes, int maxWidth, int maxHeight) {

        File textFile = null;

        try {
            textFile = createCamResTextFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textFile != null) {
            FileOutputStream outText = null;

            try {

                outText = new FileOutputStream(textFile, true);

                if (textFile.length() == 0) {

                    outText.write(("pictureSizes:").getBytes());
                    outText.write(("\n").getBytes());

                    for (Camera.Size s: sizes){
                        outText.write((s.width + "x" + s.height).getBytes());
                        outText.write(("\n").getBytes());
                    }
                    outText.write(("\n").getBytes());

                    outText.write(("Chosen picture size:").getBytes());
                    outText.write(("\n").getBytes());
                    outText.write((maxWidth + "x" + maxHeight).getBytes());
                    outText.write(("\n").getBytes());
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


}