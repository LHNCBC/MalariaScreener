/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

==============================================================================*/

package gov.nih.nlm.malaria_screener.uploadFunction;

import android.content.Context;
import android.util.Log;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.listeners.ProgressListener;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Map;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.database.ProgressBarEvent;
import gov.nih.nlm.malaria_screener.database.UpdateListViewEvent;

/*
*   This runnable class executes an image upload request using Box API.
*   The upload request is added to thread pool to be executed.
* */

public class ImageUploadTask implements Runnable {

    private static final String TAG = "MyDebug";

    Context context;
    BoxApiFile mFileApi;
    File imgFile;
    String cur_folder_id;

    String folderNameStr;
    String imgNameStr;

    public ImageUploadTask(Context context, BoxApiFile mFileApi, File imgFile, String cur_folder_id, String folderNameStr, String imgNameStr){
        this.context = context;
        this.mFileApi = mFileApi;
        this.imgFile = imgFile;
        this.cur_folder_id = cur_folder_id;

        this.folderNameStr = folderNameStr;
        this.imgNameStr = imgNameStr;
    }

    @Override
    public void run() {

        BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(imgFile, cur_folder_id).setProgressListener(new ProgressListener() {
            @Override
            public void onProgressChanged(long numBytes, long totalBytes) {

                if (numBytes == totalBytes) {

                    Log.d(TAG, "numBytes: " + numBytes + "; " + "totalBytes: " + totalBytes);

                    // -------------------- 6. delete image ID from HashMap ------------------------
                    Map<String, String> map = UploadHashManager.hashmap_for_upload;

                    if (map.containsKey(imgNameStr)) {
                        map.remove(imgNameStr);
                    }
                    UploadHashManager.saveMap(context, UploadHashManager.hashmap_for_upload);

                    // update ListView for Upload Activity's UI
                    EventBus.getDefault().post(new UpdateListViewEvent(folderNameStr));

                }

            }
        });

        // send upload request. keeps trying until upload succeeded when socket error happens.
        while (true) {
            try {
                request.send();
            } catch (BoxException e) {
                //e.printStackTrace();

                // Check type of error. Response code 0 means internet error (socket connection timeout on server side?).
                if (e.getResponseCode()==0){

                    try {
                        Thread.sleep(3*1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                    continue;
                }
            }
            break;
        }

    }
}
