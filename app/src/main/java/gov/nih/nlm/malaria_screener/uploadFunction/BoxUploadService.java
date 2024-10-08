/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

==============================================================================*/

package gov.nih.nlm.malaria_screener.uploadFunction;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.database.ProgressBarEvent;
import gov.nih.nlm.malaria_screener.database.ProgressDoneEvent;

/*
*
* This class displays a floating widget on screen to report upload progress.   02/27/2020
*
*
* */
public class BoxUploadService extends Service {

    private static final String TAG = "MyDebug";

    private WindowManager mWindowManager;
    private View mFloatingWidget;

    private ProgressBar progressBar;
    private ProgressBar progressBar_circle;
    private TextView textView;

    private int fileNum; // total number of files in /NLM_Malaria_Screener/
    private File file;

    private int currentProgress = 0;

    private ArrayList<String > imageNameList = new ArrayList<>();
    private ArrayList<String> folderNameList = new ArrayList<>();

    public static boolean stopUpload = false;

    public BoxUploadService() {

    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //fileNum = intent.getIntExtra("num_of_img_ID", 0);
        imageNameList = intent.getStringArrayListExtra("img_name_array");
        folderNameList = intent.getStringArrayListExtra("folder_name_array");
        fileNum = cal_num_of_images();

        progressBar.setMax(fileNum);

        progressBar_circle.setVisibility(View.VISIBLE);
        currentProgress = 0;

        progressBar.setProgress(currentProgress);
        textView.setText(getResources().getString(R.string.upload_float) + currentProgress + "/" + fileNum);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget_service_box, null);

        progressBar = mFloatingWidget.findViewById(R.id.progressBar_float);
        progressBar_circle = mFloatingWidget.findViewById(R.id.progressBar_upload_circle);
        textView = mFloatingWidget.findViewById(R.id.textView_upload_float);

        final int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG ,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.END;
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mFloatingWidget, params);

        final View collapsedView = mFloatingWidget.findViewById(R.id.collapse_view);
        //final View expandedView = mFloatingWidget.findViewById(R.id.expanded_container);

        ImageButton hideButton = mFloatingWidget.findViewById(R.id.imageButton_hide_upload);
        hideButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopSelf();
                    }
                });

        ImageButton cancelButton = mFloatingWidget.findViewById(R.id.imageButton_cancel_upload);
        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (currentProgress==fileNum) {
                            stopSelf();
                        } else {

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());

                            // Setting Dialog Title
                            alertDialogBuilder.setTitle(R.string.upload_cancel);

                            // Setting Dialog Message
                            alertDialogBuilder.setMessage(R.string.upload_cancel_text);

                            // Setting Positive "Yes" Button
                            String string = getResources().getString(R.string.yes);
                            alertDialogBuilder.setPositiveButton(string, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    stopUpload = true;
                                    stopSelf();

                                    MonitorThread.run = false;
                                }
                            });

                            // Setting Negative "NO" Button
                            String string1 = getResources().getString(R.string.no);
                            alertDialogBuilder.setNegativeButton(string1, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            final AlertDialog alert = alertDialogBuilder.create();

                            alert.getWindow().setType(LAYOUT_FLAG);
                            alert.show();

                        }
                    }
                });

        mFloatingWidget.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        int Xdiff = (int) (event.getRawX() - initialTouchX);
                        int Ydiff = (int) (event.getRawY() - initialTouchY);
                        /*if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                collapsedView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }*/
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mFloatingWidget, params);
                        return true;
                }
                return false;
            }
        });

        EventBus.getDefault().register(this);

        //file = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener");

        //fileNum = ;
        //progressBar.setMax(fileNum);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressBarEvent event) {

        currentProgress = event.getProgress();

        progressBar.setProgress(currentProgress);
        textView.setText(getResources().getString(R.string.upload_float) + currentProgress + "/" + fileNum);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressDone(ProgressDoneEvent event) {
        progressBar_circle.setVisibility(View.GONE);
        textView.setText(R.string.upload_finished);

    }

    private boolean isViewCollapsed() {
        return mFloatingWidget == null || mFloatingWidget.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mFloatingWidget != null) mWindowManager.removeView(mFloatingWidget);
    }

    private int cal_num_of_images(){

        String RootFolderName_str = "NLM_Malaria_Screener";

        int numOfFiles = 0;

        for (int i = 0; i < imageNameList.size(); i++) {

            final String imgNameStr = imageNameList.get(i);
            final String folderNameStr = folderNameList.get(i);

            int endIndex = imgNameStr.lastIndexOf(".");
            final String imageNameOnly = imgNameStr.substring(0, endIndex);

            final File folderFile = new File(Environment.getExternalStorageDirectory(
            ), RootFolderName_str + "/" + folderNameStr);

            File[] imageListing = folderFile.listFiles(); // list all images of one session

            if (imageListing == null){

            } else {

                for (final File imgFile : imageListing) {

                    if (imgFile.toString().contains(imageNameOnly)) {
                        numOfFiles += 1;
                    }
                }
            }
        }

        /*final File rootFile = new File(Environment.getExternalStorageDirectory(
        ), RootFolderName_str);

        if (rootFile.listFiles() != null) {

            final File[] listing = rootFile.listFiles();    // list all files & folders
            final int length = listing.length;

            if (length > 0) {

                // iterate through folders & files
                for (final File file : listing) {

                    String filePathStr = file.toString();

                    if (filePathStr.contains(".txt") || filePathStr.contains(".csv")) {
                        numOfFiles += 1;
                    }
                }
            }
        }*/

        return numOfFiles;
    }
}
