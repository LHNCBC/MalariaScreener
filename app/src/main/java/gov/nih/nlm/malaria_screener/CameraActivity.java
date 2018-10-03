package gov.nih.nlm.malaria_screener;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.github.amlcurran.showcaseview.ShowcaseView;
//import com.github.amlcurran.showcaseview.targets.ViewTarget;

import gov.nih.nlm.malaria_screener.custom.TouchImageView;
import gov.nih.nlm.malaria_screener.custom.UtilsCustom;
import gov.nih.nlm.malaria_screener.imageProcessing.MarkerBasedWatershed;
import gov.nih.nlm.malaria_screener.imageProcessing.SVM_Classifier;
import gov.nih.nlm.malaria_screener.imageProcessing.TensorFlowClassifier;
import gov.nih.nlm.malaria_screener.frontEnd.SettingsActivity;
import gov.nih.nlm.malaria_screener.frontEnd.getResultNdisplay;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private Camera cam;
    private CameraPreview camPreview;
    private FrameLayout frameLayout;
    private static final String TAG = "MyDebug";
    public static final int MEDIA_TYPE_IMAGE = 1;
    private boolean safeToTakePicture = false;
    public static final String PARAM_PHOTO = "jpg";
    public static final String PARAM_PATH = "abspath";
    public static final String PARAM_SEGMENT = "segment";

    public static final int REQUEST_GALLERY = 10;
    public static final int REQUEST_ENABLE_BT = 2;
    static final int REQUEST_RESULTS = 3;
    static final int REQUEST_SETTING = 4;
    static final int REQUEST_REJECT = 5;

    private Activity context;

    private int captureCount = 0;
    private String patientId = "";
    private String slideId = "";
    private Toolbar toolbar;
    private String info = "";
    public static final AlphaAnimation BUTTON_PRESS = new AlphaAnimation(1F, 0.5F);
    //private TextView sidInfo;
    //private TextView pidInfo;
    private TextView typeInfo;
    private TextView fieldInfo;
    private TextView countInfo;
    private TextView infectedCountInfo;
    //private String smearType = "Thick";
    private String smearType = "Thin"; // for now
    private ImageButton captureButton;
    private ImageButton galleryButton;
    //private ImageButton backButton;
    private View.OnClickListener clickList;
    private boolean segment = false;
    //ImageView imageview;

    public final static String EXTRA_ORIIMAGE = "originalImage";

    public static final String EXTRA_AUTO = "AUTO";
    public static final String EXTRA_CONFIDENCE_T = "confidenceThreshold";
    //private boolean autoSVMThres;
    //private String CThres = "";

    private ProgressDialog inProgress;
    private ProgressBar progressBar;
    private int progressStatus = 0;
    private TextView progressText;

    private TouchImageView imageViewTaken;

    private ImageButton imageButton_NO;
    private ImageButton imageButton_YES;

    private ImageButton settingButton;
    private ImageButton bluetoothButton;

    private Canvas canvas;
    private Paint paint;
    Bitmap canvasBitmap;
    //Bitmap smallOriBitmap;


    float RV = 6; //resize value

    private int cellTotal = 0;        // total cell count
    private int infectedTotal = 0;    // total infected count

    private int cellCurrent = 0;
    private int infectedCurrent = 0;

    private String nameEachImage;

    private String cellEachImage;
    private String infectedEachImage;

    private String cellCountManual;
    private String infectedCountManual;

    private String cellEachImageGT;
    private String infectedEachImageGT;

    //Mat oriSizeMat;
    Mat resizedMat = new Mat();
    Mat watershed_mask;

    File pictureFileCopy;

    boolean takenFromCam = false; // flag for saving image taken from cam

    boolean inPreview = false; // flag for when image is taken and in preview mode, so the app won't listen to bluetooth remote

    String WB = "0";
    //int ImgQ = 3;
    //int EC = 0;

    Bundle bundle;

    int totalCellNeeded = 1000;

    long processingTime = 0;

    int orientation; // save phone orientation when image taken

    int[][] cellLocation;

    public CharSequence[] cs;

    // for bluetooth ------------------------------
    private String MY_UUID = "ddec19b4-a607-43bc-b8fe-a2e61161046b";

    private Handler mHandler; // handler that gets info from Bluetooth service

    // Defines several constants used when transmitting messages between the
    // service and the UI.
    private interface MessageConstants {
        public static final int MESSAGE_READ = 0;
        public static final int MESSAGE_WRITE = 1;
        public static final int MESSAGE_TOAST = 2;
    }
    //----------------------------------------------

    //private OrientationEventListener orientationEventListener;
//    private Animation toReversePort, toReversePort_gallery, toReversePort_progreText;
//    private Animation toPort, toPort_gallery, toPort_progreText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Log.d(TAG, "onCreate");

        Log.d("ThreadUtils", getThreadSignature());

        Log.d(TAG, "Number of cores: " + Runtime.getRuntime().availableProcessors());

        // check to see if has Camera
        checkCameraHardware(this);

        // Create an instance of Camera
        initCam();

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        Log.d(TAG, "maxMemory: " + Long.toString(maxMemory / 1024 / 1024));

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.d(TAG, "memoryClass: " + Integer.toString(memoryClass));

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstTime = settings.getBoolean("firstTime_cam", true);
        if (firstTime) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("firstTime_cam", false).commit();

//            ShowcaseView sv = new ShowcaseView.Builder(this)
//                    .withMaterialShowcase()
//                    .setTarget(new ViewTarget(R.id.button_capture, this))
//                    .setContentTitle(R.string.capture_btn_title)
//                    .setContentText(R.string.capture_btn)
//                    .setStyle(R.style.CustomShowcaseTheme2)
//                    .build();
//
//            sv.show();
        }

        // Register for broadcasts when a device is discovered.
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(mReceiver, filter);

        // read pre-trained SVM data structure & TF deep learning model // put read SVM data structure & TF model here to reduce processing time
        readSVMHandler.sendEmptyMessage(0);

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

                        UtilsCustom.tensorFlowClassifier = TensorFlowClassifier.create(context.getAssets(), "malaria_thinsmear_44.h5.pb", 44, "conv2d_20_input", "output_node0");
                        //UtilsCustom.tensorFlowClassifier = TensorFlowClassifier.create(context.getAssets(), "malaria_thinsmear.h5.pb", 100, "input_2", "output_node0");
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
        //pidInfo.setText("PID: " + patientId);
        //sidInfo.setText("SID: " + slideId);
        typeInfo.setText(R.string.smear_type);
        fieldInfo.setText(imgStr + captureStr);
        countInfo.setText(cellCountStr + cellTotal);
        infectedCountInfo.setText(infectedCountStr + infectedTotal);
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Log.d(TAG, "Bluetooth: " + deviceName);
            }
        }
    };

    public void initCam() {
        setContentView(R.layout.camera_preview); // set layout to camera preview

        Bundle extras = getIntent().getExtras();

        // get SVM option
//        String auto = extras.getString("autoSVM_Th");
//        autoSVMThres = Boolean.valueOf(auto);
//        Log.d(TAG, "autoSVM: " + autoSVMThres);
//
//        // get confidence threshold if auto SVM is false
//        if (!autoSVMThres) {
//            CThres = extras.getString("Confidence_Th");
//            SVM_Th = Double.valueOf(CThres);
//        }

        // set up toolbar
        //toolbar = (Toolbar) findViewById(R.id.app_bar_cam);
        //setSupportActionBar(toolbar);
        //toolbar.setLogo(R.mipmap.logo_toolbar);

        // set up patient, slide,& field info
        int textSize = 20;
//        pidInfo = (TextView) findViewById(R.id.pid);
//        pidInfo.setTextSize(textSize);
//        pidInfo.setSingleLine(true);
//        pidInfo.setTextColor(getResources().getColor(R.color.toolbar_text));
//        pidInfo.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        v.startAnimation(BUTTON_PRESS);
//                        prompt('p'); // change patient id
//                    }
//                }
//        );
//
//        sidInfo = (TextView) findViewById(R.id.sid);
//        sidInfo.setTextSize(textSize);
//        sidInfo.setSingleLine(true);
//        sidInfo.setTextColor(getResources().getColor(R.color.toolbar_text));
//        sidInfo.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        v.startAnimation(BUTTON_PRESS);
//                        prompt('s'); // change slide id
//                    }
//                }
//        );

        typeInfo = (TextView) findViewById(R.id.type);
        typeInfo.setTextSize(textSize);
        typeInfo.setSingleLine(true);
        typeInfo.setTextColor(getResources().getColor(R.color.toolbar_text));
        /*typeInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(BUTTON_PRESS);
                        prompt('t'); // toggle blood smear type
                    }
                }
        );*/

        // capture number
        fieldInfo = (TextView) findViewById(R.id.field);
        fieldInfo.setSingleLine(true);
        fieldInfo.setTextSize(textSize);
        fieldInfo.setTextColor(getResources().getColor(R.color.toolbar_text));
        /*fieldInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(BUTTON_PRESS);
                        prompt('f'); // toggle blood smear type
                    }
                }
        );*/

        // set up count and infected count text views
        countInfo = (TextView) findViewById(R.id.count);
        countInfo.setSingleLine(true);
        countInfo.setTextSize(textSize);
        countInfo.setTextColor(getResources().getColor(R.color.toolbar_text));
        //countInfo.setTextColor(getResources().getColor(R.color.red));
        /*countInfo.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(BUTTON_PRESS);
                        Intent segmentIntent = new Intent();
                        segmentIntent.putExtra(PARAM_SEGMENT, !segment);
                        if (segment)
                            countInfo.setTextColor(getResources().getColor(R.color.red));
                        else
                            countInfo.setTextColor(getResources().getColor(R.color.green));
                        updateToolbar();
                        //segment = !segment;
                    }
                }
        );*/

        infectedCountInfo = (TextView) findViewById(R.id.infected_count);
        infectedCountInfo.setSingleLine(true);
        infectedCountInfo.setTextSize(textSize);
        infectedCountInfo.setTextColor(getResources().getColor(R.color.toolbar_text));

//        if (slideId.equals(""))
//            prompt('s'); // prompt slide id

        context = this;
        cam = getCamera();
        //Log.d(TAG, "Got camera.");

        // set camera parameters
        Camera.Parameters parameters = cam.getParameters();
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE); //make camera focus quickly

        // get supported white balance options
        List<String> whitelist = parameters.getSupportedWhiteBalance();

        cs = new String[whitelist.size()];
        for (int i = 0; i < whitelist.size(); i++) {
            cs[i] = whitelist.get(i);
        }

//        int maxEC = parameters.getMaxExposureCompensation();
//        int minEC = parameters.getMinExposureCompensation();
//        Log.d(TAG, "maxEC: " + maxEC);
//        Log.d(TAG, "minEC: " + minEC);

        // record white balance options for setting
        bundle = new Bundle();
        bundle.putCharSequenceArray("WB_list", cs);

        // get setting from preference
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        WB = sharedPreferences.getString("whitebalance", "0");

        UtilsCustom.whichClassifier = Integer.valueOf(sharedPreferences.getString("classifier", "0"));

        double value = sharedPreferences.getInt("SVM_Th", 35);
        UtilsCustom.SVM_Th = (100 - value) / 100;

        totalCellNeeded = sharedPreferences.getInt("celltotal", 1000);

        parameters.setWhiteBalance(whitelist.get(Integer.valueOf(WB)));
        //parameters.setJpegQuality(ImgQ);
        //parameters.setExposureCompensation(EC);


        cam.setParameters(parameters);

        // Create preview and set to content of activity
        camPreview = new CameraPreview(this, cam);
        safeToTakePicture = true;
        frameLayout = (FrameLayout) findViewById(R.id.camera_preview);

        //imageview = new ImageView(this);
        frameLayout.addView(camPreview);
        //frameLayout.addView(imageview);

//        frameLayout.setOnClickListener(
//                new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        cam.cancelAutoFocus();
//
//                        cam.setParameters(parameters);
//                        cam.autoFocus(this);
//                    }
//                }
//        );

        updateToolbar();

        // set up capture button
        clickList = new View.OnClickListener() {
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
        captureButton.setOnClickListener(clickList);

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

        /*bluetoothButton = (ImageButton) findViewById(R.id.button_bluetooth);
        bluetoothButton.setOnClickListener(
                new Button.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        mHandler = new Handler(Looper.getMainLooper()){
                            @Override
                            public void handleMessage(Message inputMessage) {

                                byte[] readBuf = (byte[]) inputMessage.obj;

                                String string = String.valueOf(readBuf[0]);

                                if (string == "1"){

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

                            }
                        };

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

                        String deviceName = null;
                        String deviceHardwareAddress = null;

                        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                        for (BluetoothDevice device : pairedDevices) {
                            deviceName = device.getName();
                            deviceHardwareAddress = device.getAddress(); // MAC address
                        }

                        Log.d(TAG, "deviceName: " + deviceName);

                        BluetoothDevice actualDevice = mBluetoothAdapter.getRemoteDevice(deviceHardwareAddress);

                        ParcelUuid list[] = actualDevice.getUuids();
                        for (int i=0; i<list.length;i++) {
                            Log.d(TAG, "list: " + list[i].toString());
                        }

                        BluetoothSocket socket = null;
                        try {
                            socket = actualDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                        } catch (IOException e) {
                            Log.e(TAG, "Socket's create() method failed", e);
                        }
                        try {
                            socket.connect();
                        } catch (IOException connectException) {
                            // Unable to connect; close the socket and return.
                            try {
                                socket.close();
                            } catch (IOException closeException) {
                                Log.e(TAG, "Could not close the client socket", closeException);
                            }
                            return;
                        }


                        ConnectedThread connectedThread = new ConnectedThread(socket);
                        connectedThread.start();



//                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//                        if (mBluetoothAdapter == null) {
//                            // Device does not support Bluetooth
//                            Log.d(TAG, "NO Bluetooth");
//                        } else {
//
//                            if (!mBluetoothAdapter.isEnabled()) {
//                                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//                            } else {
//                                mBluetoothAdapter.startDiscovery();
//                            }
//                        }

                    }
                }
        );*/

        // set up progress bar
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setProgress(progressStatus);
        progressBar.setMax(totalCellNeeded);

        progressText = (TextView) findViewById(R.id.textView_progress);
        progressText.setText(cellTotal + "/" + totalCellNeeded);

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer store for the stream

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams; using temp objects because
            // member streams are final.
            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1024];
            int numBytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs.
            while (true) {
                try {
                    // Read from the InputStream.
                    numBytes = mmInStream.read(mmBuffer);
                    // Send the obtained bytes to the UI activity.
                    Message readMsg = mHandler.obtainMessage(
                            MessageConstants.MESSAGE_READ, numBytes, -1,
                            mmBuffer);
                    readMsg.sendToTarget();
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        // Call this from the main activity to send data to the remote device.
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);

                // Share the sent message with the UI activity.
                Message writtenMsg = mHandler.obtainMessage(
                        MessageConstants.MESSAGE_WRITE, -1, -1, mmBuffer);
                writtenMsg.sendToTarget();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);

                // Send a failure message back to the activity.
                Message writeErrorMsg =
                        mHandler.obtainMessage(MessageConstants.MESSAGE_TOAST);
                Bundle bundle = new Bundle();
                bundle.putString("toast",
                        "Couldn't send data to the other device");
                writeErrorMsg.setData(bundle);
                mHandler.sendMessage(writeErrorMsg);
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }
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

    //

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

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

                    UtilsCustom.oriSizeMat = Imgcodecs.imread(picturePath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
                    Imgproc.cvtColor(UtilsCustom.oriSizeMat, UtilsCustom.oriSizeMat, Imgproc.COLOR_BGR2RGB);

                    Log.d(TAG, "oriSizeMat: " + UtilsCustom.oriSizeMat);

                    resizeImage();

                    ProcessThinSmearImage();
                }
            };

            Thread imgprocessThread = new Thread(r);
            imgprocessThread.start();

        } else if (requestCode == REQUEST_RESULTS && resultCode == Activity.RESULT_OK) {

            cellCountManual = data.getStringExtra("cellsCountManual");
            infectedCountManual = data.getStringExtra("infectedCountManual");

            cellTotal = cellTotal + cellCurrent;
            infectedTotal = infectedTotal + infectedCurrent;

            captureCount++;
            updateToolbar();

            //update progress bar
            progressStatus = cellTotal;
            progressBar.setProgress(progressStatus);

        } else if (requestCode == REQUEST_RESULTS && resultCode == Activity.RESULT_CANCELED) {

            Log.d(TAG, "Reject");
            cellEachImage = data.getStringExtra("cellCountEachImage");
            infectedEachImage = data.getStringExtra("infectedCountEachImage");
            nameEachImage = data.getStringExtra("nameStringEachImage");
        }

    }

    // Hang Yu 04/04/2016
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

            Runnable r = new Runnable() {
                @Override
                public void run() {

                    Mat jpegData = new Mat(1, data.length, CvType.CV_8UC1);
                    jpegData.put(0, 0, data);

                    UtilsCustom.oriSizeMat = Imgcodecs.imdecode(jpegData, -1); // produce a 3 channel bgr image
                    Imgproc.cvtColor(UtilsCustom.oriSizeMat, UtilsCustom.oriSizeMat, Imgproc.COLOR_BGR2RGB);

                    resizeImage();

                    messageHandler.sendEmptyMessage(0);

                }
            };

            Thread imgpreviewThread = new Thread(r);
            imgpreviewThread.start();

            safeToTakePicture = true;
            //Log.d(TAG, "Camera safe to use again");
        }
    };

    private void resizeImage(){

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

        imageViewTaken = (TouchImageView) findViewById(R.id.imageView_preview);
        imageButton_NO = (ImageButton) findViewById(R.id.imageButton_no);
        imageButton_YES = (ImageButton) findViewById(R.id.imageButton_yes);

        Bitmap rotatedBitmap = Bitmap.createBitmap(resizedMat.width(), resizedMat.height(), Bitmap.Config.RGB_565);
        //Mat temp4View = new Mat();
        //Imgproc.cvtColor(resizedMat, temp4View, Imgproc.COLOR_BGR2RGB);

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

                                ProcessThinSmearImage();
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
                        initCam();
                    }
                }
        );

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
            inProgress.dismiss();
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

            goToNextActivity();

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime_w;
        Log.d(TAG, "Single Image Processing Time: " + totalTime);

        System.gc();
        Runtime.getRuntime().gc();

    }


    public void doAFewThings() {

        if (UtilsCustom.cellLocation.length == 0) {  //take care of the case(avoid crash) when segmentation passed but no cell chips extracted
            inProgress.dismiss();
            retakeHandler.sendEmptyMessage(0);
        } else {

            //save image to file
            saveOriImage(); // taken out of handler, otherwise original image not saved before needed in next activity. 09/26/2017
            //if (takenFromCam) {
            saveImageHandler.sendEmptyMessage(0);
            //}

            cellLocation = UtilsCustom.cellLocation;

            //reset
            cellCurrent = 0;
            infectedCurrent = 0;

            cellCurrent = UtilsCustom.cellCount;
        }
    }

    public void drawAll() {

        for (int i = 0; i < UtilsCustom.results_NN.size(); i++) {

            if (UtilsCustom.results_NN.get(i) == 0) {
                //infectedNum++;
                paint.setColor(Color.BLUE); // not infected
                canvas.drawCircle(UtilsCustom.cellLocation[i][1] / RV, UtilsCustom.cellLocation[i][0] / RV, 2, paint);
                //canvas.drawText(String.valueOf(infectedNum), cellLocation[i][1] - 7, cellLocation[i][0] - 7, paint);
            } else if (UtilsCustom.results_NN.get(i) == 1) {
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

    public void goToNextActivity() {

        inProgress.dismiss();

        Intent intent = new Intent(context, getResultNdisplay.class);

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

//        String auto = String.valueOf(autoSVMThres);
//        intent.putExtra(EXTRA_AUTO, auto);
//        if (!autoSVMThres) {
//            intent.putExtra(EXTRA_CONFIDENCE_T, CThres);
//        }

        intent.putExtra("WB", cs[Integer.valueOf(WB)]);
        intent.putExtra("SVM_Th", String.valueOf(UtilsCustom.SVM_Th));
        intent.putExtra("totalcell", String.valueOf(totalCellNeeded));

        intent.putExtra("time", String.valueOf(processingTime));

        // pass total cell count info
        intent.putExtra("cellTotal", String.valueOf(cellTotal));
        intent.putExtra("infectedTotal", String.valueOf(infectedTotal));

        // pass cell count info of current image
        intent.putExtra("cellCountC", String.valueOf(cellCurrent));
        intent.putExtra("infectedCountC", String.valueOf(infectedCurrent));

        String imgNameStr = pictureFileCopy.toString().substring(pictureFileCopy.toString().lastIndexOf("/") + 1);

        // append cell info and image name per image together as string to store in database
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
        intent.putExtra("infectedCountEachImageGT", infectedEachImageGT);

        System.gc();
        Runtime.getRuntime().gc();

        startActivityForResult(intent, REQUEST_RESULTS);

    }

    private Handler saveImageHandler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    //SaveImage saveImage = new SaveImage(pictureFileCopy, dataCopy);

                    saveMaskImage();

                }
            };

            Thread saveImgThread = new Thread(r);
            saveImgThread.start();

        }
    };

    public void saveOriImage() {

        long startTime = System.currentTimeMillis();

        String file_name = pictureFileCopy.toString();
        Imgproc.cvtColor(UtilsCustom.oriSizeMat, UtilsCustom.oriSizeMat, Imgproc.COLOR_RGB2BGR);

        Imgcodecs.imwrite(file_name, UtilsCustom.oriSizeMat);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.d(TAG, "save Pic Time: " + totalTime);

        UtilsCustom.oriSizeMat.release();
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

        initCam();

    }


    private void prompt(char target) {
        switch (target) {
            case 'f':
                Context c3 = CameraActivity.this;
                final AlertDialog.Builder alertDialog3 = new AlertDialog.Builder(c3);
                alertDialog3.setTitle("Start new capture");
                alertDialog3.setMessage("Enter Field #:");
                final EditText input3 = new EditText(c3);
                alertDialog3.setView(input3);
                alertDialog3.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String countStr = input3.getText().toString();
                        try {
                            int tmp = Integer.parseInt(countStr);
                            if (tmp < 0)
                                tmp = 0;
                            captureCount = tmp;
                        } catch (NumberFormatException ex) {
                            captureCount = 0;
                            //Log.d(TAG, "Input is not integer. Set to 0.");
                        }
                        updateToolbar();
                    }
                });
                alertDialog3.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //context.finish();
                    }
                });
                alertDialog3.show();
                break;
            case 't':
//                if (smearType.equals("Thick"))
//                    smearType = "Thin";
//                else
//                    smearType = "Thick";
                if (smearType.equals("Thin")) // for now
                    smearType = "Thick";
                else
                    smearType = "Thin";
                captureCount = 0;
                cellTotal = 0;
                infectedTotal = 0;
                updateToolbar();
                break;
            case 's':
                Context c = CameraActivity.this;
                final AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
                alertDialog.setTitle("Start a new slide");
                alertDialog.setMessage("Enter Slide ID:");
                final EditText input = new EditText(c);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //smearType = "Thick";
                        smearType = "Thin";
                        captureCount = 0;
                        cellTotal = 0;
                        infectedTotal = 0;
                        slideId = input.getText().toString();
                        updateToolbar();
                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //context.finish();
                    }
                });
                alertDialog.show();
                break;
            case 'p':
                Context c2 = CameraActivity.this;
                final AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(c2);
                alertDialog2.setTitle("Start a new patient");
                alertDialog2.setMessage("Enter Patient ID:");
                final EditText input2 = new EditText(c2);
                alertDialog2.setView(input2);
                // Set up the buttons
                alertDialog2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        smearType = "Thick";
                        captureCount = 0;
                        cellTotal = 0;
                        infectedTotal = 0;
                        patientId = input2.getText().toString();
                        updateToolbar();
                    }
                });
                alertDialog2.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog2.show();
                break;

        }

    }

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
                    timeStamp + ".png");
        } else {
            return null;
        }

        return mediaFile;
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


//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        WB = sharedPreferences.getString("whitebalance", "0");
//        //ImgQ = sharedPreferences.getInt("imagequality", 3);
//        //EC = sharedPreferences.getInt("exposure", 0);

//        double value = sharedPreferences.getInt("SVM_Th", 65);
//        SVM_Th = value/100;

        if (cam == null) {
            initCam();
            //orientationEventListener.enable();
            //Log.d(TAG, "Camera resumed");
        }

        // tutorial page
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        int secondTime = settings.getInt("firstSecondTime_cam", 0);
        if (secondTime == 1) {

            PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("firstSecondTime_cam", 2).commit();

//            ShowcaseView sv = new ShowcaseView.Builder(this)
//                    .withMaterialShowcase()
//                    .setTarget(new ViewTarget(R.id.settingButton, this))
//                    .setContentTitle(R.string.setting_btn_title)
//                    .setContentText(R.string.setting_btn)
//                    .setStyle(R.style.CustomShowcaseTheme2)
//                    .build();
//
//            sv.show();
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

        //orientationEventListener.enable();
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
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
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

//            case KeyEvent.KEYCODE_ENTER:
//                if(action==KeyEvent.ACTION_DOWN){
//
//                    Toast.makeText(getApplication(), "ANDROID button clicked", Toast.LENGTH_SHORT).show();
//                }

            default:
                return super.dispatchKeyEvent(event);
        }
    }

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


}