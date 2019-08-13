package gov.nih.nlm.malaria_screener.camera;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.amlcurran.showcaseview.ShowcaseView;
//import com.github.amlcurran.showcaseview.targets.ViewTarget;

import gov.nih.nlm.malaria_screener.imageProcessing.Cells;
import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.TouchImageView;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsData;
import gov.nih.nlm.malaria_screener.frontEnd.ResultDisplayer;
import gov.nih.nlm.malaria_screener.frontEnd.ResultDisplayer_thickSmear;
import gov.nih.nlm.malaria_screener.imageProcessing.MarkerBasedWatershed;
import gov.nih.nlm.malaria_screener.imageProcessing.SVM_Classifier;
import gov.nih.nlm.malaria_screener.imageProcessing.TensorFlowClassifier;
import gov.nih.nlm.malaria_screener.imageProcessing.ThickSmearProcessor;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    static final int REQUEST_RESULTS = 3;

    private Activity context;

    private Toolbar toolbar;
    private String info = "";
    public static final AlphaAnimation BUTTON_PRESS = new AlphaAnimation(1F, 0.0F);
    //private TextView sidInfo;
    //private TextView pidInfo;


    private TouchImageView imageViewTaken;

    private ImageButton imageButton_NO;
    private ImageButton imageButton_YES;

    private Canvas canvas;
    private Paint paint;
    Bitmap canvasBitmap;
    //Bitmap smallOriBitmap;

    float RV = 6; //resize value

    private int cellCurrent = 0;
    private int infectedCurrent = 0;

    //Mat oriSizeMat;
    Mat resizedMat = new Mat();
    Mat watershed_mask;

    File pictureFileCopy;

    boolean takenFromCam = false; // flag for saving image taken from cam

    boolean inPreview = false; // flag for when image is taken and in preview mode, so the app won't listen to bluetooth remote

    String WB = "0";

    long processingTime = 0;

    int orientation; // save phone orientation when image taken

    int[][] cellLocation;

    public CharSequence[] cs;

    private boolean imageAcquisition = false;

    private CameraViewModel cameraViewModel;

    private ProgressDialog inProgress;

    private String smearType = "Thin"; // for now

    private int captureCount = 0;

    //----------------------------------------------


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_camera);

        Log.d("ThreadUtils", getThreadSignature());

        Log.d(TAG, "Number of cores: " + Runtime.getRuntime().availableProcessors());

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        Log.d(TAG, "maxMemory: " + Long.toString(maxMemory / 1024 / 1024));

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.d(TAG, "memoryClass: " + Integer.toString(memoryClass));

        context = this;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // smear type
        smearType = sharedPreferences.getString("smeartype", "Thin");

        // read pre-trained SVM data structure & TF deep learning model // put read SVM data structure & TF model here to reduce processing time
        readSVMHandler.sendEmptyMessage(0);

        if (null == savedInstanceState) {
            Bundle bundle = new Bundle();
            bundle.putInt("capturecount", captureCount);
            Camera2RawFragment camera2RawFragment = Camera2RawFragment.newInstance();
            camera2RawFragment.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container, camera2RawFragment)
                    .commit();
        }

        cameraViewModel = ViewModelProviders.of(this).get(CameraViewModel.class);

        cameraViewModel.getCamByteData().observe(this, new Observer<byte[]>() {
            @Override
            public void onChanged(byte[] bytes) {
                Log.d(TAG, "image data received in activity.");

                processCameraData(bytes);
            }
        });

        cameraViewModel.getImageString().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                processGalleryData(s);
            }
        });

        cameraViewModel.getImageFile().observe(this, new Observer<File>() {
            @Override
            public void onChanged(File file) {
                pictureFileCopy = file;
            }
        });

        cameraViewModel.getOrientation().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer ori) {
                orientation = ori;
            }
        });

        cameraViewModel.getCaptureCountReset().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean reset) {
                if (reset) {
                    captureCount = 0;
                }
            }
        });

    }

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
                        UtilsCustom.tensorFlowClassifier_thin = TensorFlowClassifier.create(context.getAssets(), "malaria_thinsmear_44.h5.pb", UtilsCustom.TF_input_size, "conv2d_20_input", "output_node0");
                        //UtilsCustom.tensorFlowClassifier_thin = TensorFlowClassifier.create(context.getAssets(), "malaria_thinsmear.h5.pb", UtilsCustom.TF_input_size, "input_2", "output_node0");
                        //UtilsCustom.tfClassifier_lite = TFClassifier_Lite.create(context.getAssets(), "thinSmear_100_quantized.tflite", UtilsCustom.TF_input_size);

                        //thick smear
                        UtilsCustom.tensorFlowClassifier_thick = TensorFlowClassifier.create(context.getAssets(), "ThickSmearModel.h5.pb", UtilsCustom.TF_input_size, "conv2d_1_input", "output_node0");

                        //UtilsCustom.tensorFlowClassifier_thick = TensorFlowClassifier.create(context.getAssets(), "ThickSmearModel_7LayerConv.h5.pb", UtilsCustom.TF_input_size, "conv2d_1_input", "output_node0");


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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_RESULTS && resultCode == Activity.RESULT_OK) {

            setContentView(R.layout.activity_camera);

            captureCount++;

            Bundle bundle = new Bundle();
            bundle.putInt("capturecount", captureCount);
            Camera2RawFragment camera2RawFragment = Camera2RawFragment.newInstance();
            camera2RawFragment.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container,camera2RawFragment )
                    .commit();

        } else if (requestCode == REQUEST_RESULTS && resultCode == Activity.RESULT_CANCELED) {

            setContentView(R.layout.activity_camera);

            Bundle bundle = new Bundle();
            bundle.putInt("capturecount", captureCount);
            Camera2RawFragment camera2RawFragment = Camera2RawFragment.newInstance();
            camera2RawFragment.setArguments(bundle);

            getFragmentManager().beginTransaction()
                    .replace(R.id.container,camera2RawFragment )
                    .commit();

        }

    }

    private void processCameraData(final byte[] data){

            Runnable r = new Runnable() {
                @Override
                public void run() {

                    Mat jpegData = new Mat(1, data.length, CvType.CV_8UC1);
                    jpegData.put(0, 0, data);

                    //UtilsCustom.oriSizeMat = Imgcodecs.imdecode(jpegData, -1); // produce a 3 channel bgr image
                    UtilsCustom.oriSizeMat = Imgcodecs.imdecode(jpegData, Imgcodecs.CV_LOAD_IMAGE_COLOR);

                    //blurry detection
                    //blurScore = blurDetection.computeBlur();

                    Log.d(TAG, "UtilsCustom.oriSizeMat: " + UtilsCustom.oriSizeMat);
                    Imgproc.cvtColor(UtilsCustom.oriSizeMat, UtilsCustom.oriSizeMat, Imgproc.COLOR_BGR2RGB);

                    resizeImage();

                    messageHandler.sendEmptyMessage(0);

                }
            };

            Thread imgpreviewThread = new Thread(r);
            imgpreviewThread.start();

            //Log.d(TAG, "Camera safe to use again");

    }

    private void processGalleryData(final String str){

        // show in progress sign
        String str1 = getResources().getString(R.string.image_process1);
        String str2 = getResources().getString(R.string.image_process2);
        inProgress = ProgressDialog.show(context, str1, str2, false, false);
        inProgress.show();

        Runnable r = new Runnable() {
            @Override
            public void run() {

                Log.d(TAG, "pictureFileCopy: " + pictureFileCopy);

                //UtilsCustom.oriSizeMat = Imgcodecs.imread(picturePath, -1);
                UtilsCustom.oriSizeMat = Imgcodecs.imread(str, Imgcodecs.CV_LOAD_IMAGE_COLOR);

                //blurry detection
                //blurScore = blurDetection.computeBlur();

                Imgproc.cvtColor(UtilsCustom.oriSizeMat, UtilsCustom.oriSizeMat, Imgproc.COLOR_BGR2RGB);

                Log.d(TAG, "oriSizeMat: " + UtilsCustom.oriSizeMat);

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

    }

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

        // put resized image on canvas for drawing results after image processing
        canvasBitmap = Bitmap.createBitmap(resizedMat.width(), resizedMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(resizedMat, canvasBitmap);

        canvas = new Canvas(canvasBitmap);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(5);
        paint.setColor(Color.BLACK);
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

        Bitmap rotatedBitmap = Bitmap.createBitmap(resizedMat.width(), resizedMat.height(), Bitmap.Config.RGB_565);
        //Mat temp4View = new Mat();
        //Imgproc.cvtColor(resizedMat, temp4View, Imgproc.COLOR_BGR2RGB);

        Utils.matToBitmap(resizedMat, rotatedBitmap);

        /*Log.d(TAG, "ori preview: " + orientation);

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
        }*/

        rotatedBitmap = Bitmap.createBitmap(rotatedBitmap, 0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight(), null, false);
        imageViewTaken.setImageBitmap(rotatedBitmap);

        imageButton_YES.setOnClickListener(
                new View.OnClickListener() {
                    @Override

                    public void onClick(View view) {

                        inPreview = false;

                        // show in progress sign
                        String str1 = getResources().getString(R.string.image_process1);
                        String str2 = getResources().getString(R.string.image_process2);
                        inProgress = ProgressDialog.show(context, str1, str2, false, false);
                        inProgress.show();

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

                        setContentView(R.layout.activity_camera);

                        getFragmentManager().beginTransaction()
                                .replace(R.id.container, Camera2RawFragment.newInstance())
                                .commit();


                        //initCam();
                    }
                }
        );

    }

    private void ImageAcquisition_thick() {

        UtilsData.addParasiteCount("N/A");
        UtilsData.addWBCCount("N/A");

        processingTime = 0;

        // set Bitmap to paint
        Bitmap bitmap = Bitmap.createBitmap(UtilsCustom.oriSizeMat.width(), UtilsCustom.oriSizeMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(UtilsCustom.oriSizeMat, bitmap);

        saveImageHandler.sendEmptyMessage(0);
        goToNextActivity_thickSmear(bitmap);
    }

    private void ProcessThickSmearImage() {

        long startTime_w = System.currentTimeMillis();

        ThickSmearProcessor thickSmearProcessor = new ThickSmearProcessor(UtilsCustom.oriSizeMat);
        thickSmearProcessor.processImage();

        long endTime_w = System.currentTimeMillis();
        long totalTime_w = endTime_w - startTime_w;
        Log.d(TAG, "One image time: " + totalTime_w);
        processingTime = totalTime_w;

        saveImageHandler.sendEmptyMessage(0);

        goToNextActivity_thickSmear(thickSmearProcessor.getResultBitmap());
    }

    private void ImageAcquisition() {

        UtilsData.addCellCount("N/A");
        UtilsData.addInfectedCount("N/A");

        processingTime = 0;

        resizedMat.release();

        saveImageHandler.sendEmptyMessage(0);

        goToNextActivity();

    }

    private void ProcessThinSmearImage() {

        long startTime_w = System.currentTimeMillis();

        MarkerBasedWatershed watershed = new MarkerBasedWatershed();
        watershed.runMarkerBasedWatershed(resizedMat, RV);
        resizedMat.release();

        long endTime_w = System.currentTimeMillis();
        long totalTime_w = endTime_w - startTime_w;
        Log.d(TAG, "Watershed Time: " + totalTime_w);

        if (watershed.getRetakeFlag()) { // take care of the case (avoid crash) when segmentation failed due to a plain black image was taken

            retakeHandler.sendEmptyMessage(0);
        } else {

            watershed_mask = watershed.watershed_result.clone(); //when segmentation is successful, copy seg mask to later save it in worker thread

            long startTime_C = System.currentTimeMillis();

            Cells c = new Cells();
            c.runCells(watershed.watershed_result, watershed.output_WBCMask);

            watershed = null;
            c = null;

            long endTime_C = System.currentTimeMillis();
            long totalTime_C = endTime_C - startTime_C;
            Log.d(TAG, "Cell Time: " + totalTime_C);

            doAFewThings();

            drawAll();

            saveResults();

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime_w;
            Log.d(TAG, "Single Image Processing Time: " + totalTime);
            processingTime = totalTime;

            goToNextActivity();

        }

        System.gc();
        Runtime.getRuntime().gc();

    }

    private void saveResults() {

        UtilsData.cellCurrent = cellCurrent;
        UtilsData.infectedCurrent = infectedCurrent;
        UtilsData.cellTotal = UtilsData.cellTotal + cellCurrent;
        UtilsData.infectedTotal = UtilsData.infectedTotal + infectedCurrent;
        UtilsData.addCellCount(String.valueOf(cellCurrent));
        UtilsData.addInfectedCount(String.valueOf(infectedCurrent));

    }

    public void doAFewThings() {

        if (UtilsCustom.cellLocation.length == 0) {  //take care of the case(avoid crash) when segmentation passed but no cell chips extracted

            retakeHandler.sendEmptyMessage(0);
        } else {

            //save image to file
            //saveOriImage(); // taken out of handler, otherwise original image not saved before needed in next activity. 09/26/2017
            saveImageHandler.sendEmptyMessage(0);                // put in handler again, original image is copied for saving, display in result page using image in memory. 03/12/2019
            //if (takenFromCam) {
            saveMaskImageHandler.sendEmptyMessage(0);
            //}

            cellLocation = UtilsCustom.cellLocation;

            //reset
            cellCurrent = 0;
            infectedCurrent = 0;

            cellCurrent = UtilsCustom.cellCount;
        }
    }

    public void drawAll() {

        for (int i = 0; i < UtilsCustom.results.size(); i++) {

            if (UtilsCustom.results.get(i) == 0) {
                //infectedNum++;
                paint.setColor(Color.BLUE); // not infected
                canvas.drawCircle(UtilsCustom.cellLocation[i][1] / RV, UtilsCustom.cellLocation[i][0] / RV, 2, paint);
                //canvas.drawText(String.valueOf(infectedNum), cellLocation[i][1] - 7, cellLocation[i][0] - 7, paint);
            } else if (UtilsCustom.results.get(i) == 1) {
                infectedCurrent++;
                paint.setColor(Color.RED);
                canvas.drawCircle(UtilsCustom.cellLocation[i][1] / RV, UtilsCustom.cellLocation[i][0] / RV, 2, paint);

                if (takenFromCam) { // test this canvas rotate
                    canvas.save();
                    // draw texts according to phone rotation while image was taken
                    if (orientation == Surface.ROTATION_0) { //portrait
                        canvas.rotate(270, UtilsCustom.cellLocation[i][1] / RV, UtilsCustom.cellLocation[i][0] / RV);
                    } else if (orientation == Surface.ROTATION_270) { //landscape
                        canvas.rotate(180, UtilsCustom.cellLocation[i][1] / RV, UtilsCustom.cellLocation[i][0] / RV);
                    } else if (orientation == Surface.ROTATION_180) { //reverse portrait
                        canvas.rotate(90, UtilsCustom.cellLocation[i][1] / RV, UtilsCustom.cellLocation[i][0] / RV);
                    } else if (orientation == Surface.ROTATION_90) { // reverse landscape
                        canvas.rotate(0, UtilsCustom.cellLocation[i][1] / RV, UtilsCustom.cellLocation[i][0] / RV);
                    }
                    canvas.drawText(String.valueOf(infectedCurrent), UtilsCustom.cellLocation[i][1] / RV - 7, UtilsCustom.cellLocation[i][0] / RV - 7, paint);
                    canvas.restore();
                } else {
                    canvas.drawText(String.valueOf(infectedCurrent), UtilsCustom.cellLocation[i][1] / RV - 7, UtilsCustom.cellLocation[i][0] / RV - 7, paint);
                }
            }

        }
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
            canvasBitmap = Bitmap.createBitmap(canvasBitmap, 0, 0, canvasBitmap.getWidth(), canvasBitmap.getHeight(), m, false);
        }

        //pass original file dir
        intent.putExtra("picFile", pictureFileCopy.toString());

        // pass resize value of original image
        intent.putExtra("RV", RV);

        // pass resized result image to new activity
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        canvasBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream2);
        canvasBitmap.recycle();
        byte[] resImageByteArray = stream2.toByteArray();
        intent.putExtra("resImage", resImageByteArray);

        //intent.putExtra("WB", cs[Integer.valueOf(WB)]);
        intent.putExtra("WB", "auto");  // temp WB by Hang

        intent.putExtra("time", String.valueOf(processingTime));

        /*// pass total cell count info
        intent.putExtra("cellTotal", String.valueOf(cellTotal));
        intent.putExtra("infectedTotal", String.valueOf(infectedTotal));

        // pass cell count info of current image
        intent.putExtra("cellCountC", String.valueOf(cellCurrent));
        intent.putExtra("infectedCountC", String.valueOf(infectedCurrent));*/

        String imgNameStr = pictureFileCopy.toString().substring(pictureFileCopy.toString().lastIndexOf("/") + 1);
        UtilsData.addImageName(imgNameStr);

        /*// append cell info and image name per image together as string to store in database
        if (cellEachImage != null && infectedEachImage != null) { // when it's the second image
            cellEachImage = cellEachImage + (cellCurrent + ",");
            infectedEachImage = infectedEachImage + (infectedCurrent + ",");
            nameEachImage = nameEachImage + (imgNameStr + ",");
        } else {
            cellEachImage = cellCurrent + ",";
            infectedEachImage = infectedCurrent + ",";
            nameEachImage = imgNameStr + ",";
        }


        intent.putExtra("cellCountEachImage", cellEachImage);
        intent.putExtra("infectedCountEachImage", infectedEachImage);
        intent.putExtra("nameStringEachImage", nameEachImage);

        // append cell info GT per image together as string to store in database
        if (cellCountManual != null && infectedCountManual != null) { // only do this part after first image is taken
            if (cellEachImageGT != null && infectedEachImageGT != null) {
                cellEachImageGT = cellEachImageGT + (cellCountManual + ",");
                infectedEachImageGT = infectedEachImageGT + (infectedCountManual + ",");
            } else {
                cellEachImageGT = cellCountManual + ",";
                infectedEachImageGT = infectedCountManual + ",";
            }
        }
        intent.putExtra("cellCountEachImageGT", cellEachImageGT);
        intent.putExtra("infectedCountEachImageGT", infectedEachImageGT);*/

        System.gc();
        Runtime.getRuntime().gc();

        startActivityForResult(intent, REQUEST_RESULTS);

    }

    private void goToNextActivity_thickSmear(Bitmap resultBitmap) {

        inProgress.dismiss();

        Intent intent = new Intent(context, ResultDisplayer_thickSmear.class);

        // pass resized result image to new activity
        int width = (int) ((float) resultBitmap.getWidth() / RV);
        int height = (int) ((float) resultBitmap.getHeight() / RV);

        Bitmap rescaledBitmap = Bitmap.createScaledBitmap(resultBitmap, width, height, false);

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
            rescaledBitmap = Bitmap.createBitmap(rescaledBitmap, 0, 0, rescaledBitmap.getWidth(), rescaledBitmap.getHeight(), m, false);
        }

        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        rescaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream2);
        rescaledBitmap.recycle();
        byte[] resImageByteArray = stream2.toByteArray();
        intent.putExtra("resImage", resImageByteArray);

        // pass image names
        String imgNameStr = pictureFileCopy.toString().substring(pictureFileCopy.toString().lastIndexOf("/") + 1);
        UtilsData.addImageName(imgNameStr);

        intent.putExtra("picFile", pictureFileCopy.toString());

        // pass resize value of original image
        intent.putExtra("RV", RV);

        // pass white balance
        //intent.putExtra("WB", cs[Integer.valueOf(WB)]);
        intent.putExtra("WB", "auto");  // temp WB by Hang

        intent.putExtra("time", String.valueOf(processingTime));

        System.gc();
        Runtime.getRuntime().gc();

        startActivityForResult(intent, REQUEST_RESULTS);
    }

    private Handler saveMaskImageHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    //SaveImage saveImage = new SaveImage(pictureFileCopy, dataCopy);

                    saveMaskImage();

                }
            };

            Thread saveMaskImgThread = new Thread(r);
            saveMaskImgThread.start();

        }
    };

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

        //UtilsCustom.oriSizeMat.release();
    }

    public void saveMaskImage() {

        String file_name = null;
        try {
            file_name = createImageFile().toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Imgcodecs.imwrite(file_name, watershed_mask);
        watershed_mask.release();
    }


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

        //initCam();

    }

    private File createImageFile() throws IOException {

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        // get image name
        String imgStr = pictureFileCopy.toString().substring(pictureFileCopy.toString().lastIndexOf("/") + 1);
        int endIndex = imgStr.lastIndexOf(".");
        String imageName = imgStr.substring(0, endIndex);

        File imgFile = new File(new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New"), imageName + "_mask.png");

        return imgFile;
    }

    @Override
    public void onStart() {
        super.onStart();

        //orientationEventListener.enable();
    }

    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // smear type
        smearType = sharedPreferences.getString("smeartype", "Thin");

//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        WB = sharedPreferences.getString("whitebalance", "0");
//        //ImgQ = sharedPreferences.getInt("imagequality", 3);
//        //EC = sharedPreferences.getInt("exposure", 0);

//        double value = sharedPreferences.getInt("SVM_Th", 65);
//        SVM_Th = value/100;

    }

    @Override
    public void onPause() {
        super.onPause();
        //Log.d(TAG, "onPause");

    }

    public void onStop() {
        super.onStop();

        //orientationEventListener.enable();
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        int action = event.getAction();
        int keyCode = event.getKeyCode();
        switch (keyCode) {

            case KeyEvent.KEYCODE_VOLUME_UP:
                if (action == KeyEvent.ACTION_DOWN) {

                    if (!inPreview) {
                        inPreview = true;
                    }

                }
                return true;

//            case KeyEvent.KEYCODE_ENTER:
//                if(action==KeyEvent.ACTION_DOWN){
//
//                    Toast.makeText(getApplication(), "ANDROID button clicked", Toast.LENGTH_SHORT).show();
//                }

            default:
                return super.dispatchKeyEvent(event);
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


}