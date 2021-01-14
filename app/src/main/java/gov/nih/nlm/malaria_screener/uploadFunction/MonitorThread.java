/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

==============================================================================*/

package gov.nih.nlm.malaria_screener.uploadFunction;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Future;

import gov.nih.nlm.malaria_screener.database.ProgressBarEvent;
import gov.nih.nlm.malaria_screener.database.ProgressDoneEvent;

/*
*   This runnable class runs on a separate thread to monitor the progress of all upload threads, it
*   posts current upload progress to BoxUploadService.
*
*   It shuts down the thread pool when:
*       1. User presses Stop button in BoxUploadService.
*       2. All upload threads all finished.
*
* */
public class MonitorThread implements Runnable{

    private static final String TAG = "MyDebug";

    int numOfFiles; // number of images to be uploaded
    public static boolean run;  // When sets to false, monitor thread stops running, and shuts down thread pool.

    public MonitorThread(int numOfFiles){
        this.numOfFiles = numOfFiles;
    }

    @Override
    public void run() {

        run = true;

        while (run){
            boolean allDone = true; // flag for if all threads all done.
            int totalDone = 0;      // number of upload threads are done.

            // reference to futures in UploadExecutorManager.
            List<Future<?>> futures = UploadExecutorManager.getUploadExecutorManager().futures;

            // This section is synchronized so that adding new threads (in ListOfImagesUploader) and checking upload progress
            // here in a for loop does not result in multi-threading conflict.
            synchronized (futures) {

                for (Future<?> future : futures) {
                    allDone &= future.isDone();
                    if (future.isDone()) {
                        totalDone += 1;
                    }
                }

            }

            // Interval between each time of checking progress
            try {
                Thread.sleep(3*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // notify floating widget the current number of images uploaded
            EventBus.getDefault().post(new ProgressBarEvent(totalDone));

            // only when both "allDone" is true & number of finished threads equals to the number of
            // images to be uploaded, does it mean that all upload tasks are finished. Then, post this msg to
            // BoxUploadService.
            if (allDone && futures.size() == numOfFiles){
                run = false;
                // notify floating widget that all upload tasks are done
                EventBus.getDefault().post(new ProgressDoneEvent(true));
            }

        }

        // clear futures that were used to monitor this upload
        UploadExecutorManager.getUploadExecutorManager().clearFutures();

        //  shutdown thread pool
        UploadExecutorManager.getUploadExecutorManager().shutdownThreads();
    }


}
