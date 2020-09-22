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

package gov.nih.nlm.malaria_screener.frontEnd;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.DatabasePage;
import gov.nih.nlm.malaria_screener.database.ProgressBarEvent;
import gov.nih.nlm.malaria_screener.database.ProgressDoneEvent;
import gov.nih.nlm.malaria_screener.database.Register;

/*
*
* This class checks for the upload progress. It also displays a floating widget on screen.   02/27/2020
*
*
* */
public class UploadService extends Service {

    private static final String TAG = "MyDebug";

    private WindowManager mWindowManager;
    private View mFloatingWidget;

    private ProgressBar progressBar;
    private TextView textView;

    private int fileNum; // total number of files in /NLM_Malaria_Screener/
    private File file;

    boolean pause = true;

    public UploadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {

        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        fileNum = getFileCount(file);
        progressBar.setMax(fileNum);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mFloatingWidget = LayoutInflater.from(this).inflate(R.layout.floating_widget_service, null);

        progressBar = mFloatingWidget.findViewById(R.id.progressBar_float);
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

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());

                        // Setting Dialog Title
                        alertDialogBuilder.setTitle(R.string.upload_cancel);

                        // Setting Dialog Message
                        alertDialogBuilder.setMessage(R.string.upload_cancel_text);

                        // Setting Positive "Yes" Button
                        String string = getResources().getString(R.string.yes);
                        alertDialogBuilder.setPositiveButton(string, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                stopSelf();
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
                });


        final ImageButton pausePlayButton = mFloatingWidget.findViewById(R.id.imageButton_pause_upload);
        pausePlayButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (pause) {
                            pausePlayButton.setBackgroundResource(R.drawable.play_arrow_white_18dp);
                            pause = false;
                        } else {
                            pausePlayButton.setBackgroundResource(R.drawable.pause_white_18dp);
                            pause = true;
                        }
                    }
                });

        /*ImageButton cancelButton_1 = mFloatingWidget.findViewById(R.id.imageButton_cancel_upload_1);
        cancelButton_1.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        stopSelf();
                    }
                });*/

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

        file = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener");

        fileNum = getFileCount(file);
        progressBar.setMax(fileNum);

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgressEvent(ProgressBarEvent event) {

        progressBar.setProgress(event.getProgress());
        textView.setText(getResources().getString(R.string.upload_float) + event.getProgress() + "/" + fileNum);
    }

    @Subscribe
    public void onProgressDone(ProgressDoneEvent event) {
        textView.setText(R.string.upload_finished);
        stopSelf();
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
}
