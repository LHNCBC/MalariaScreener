package gov.nih.nlm.malaria_screener.uploadFunction;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Future;

import gov.nih.nlm.malaria_screener.database.ProgressBarEvent;
import gov.nih.nlm.malaria_screener.database.ProgressDoneEvent;

public class MonitorThread implements Runnable{

    private static final String TAG = "MyDebug";

    int numOfFiles;
    public static boolean run;

    public MonitorThread(int numOfFiles){
        this.numOfFiles = numOfFiles;
    }

    @Override
    public void run() {

        run = true;

        long startTime = System.currentTimeMillis();

        while (run){
            boolean allDone = true;
            int totalDone = 0;
            List<Future<?>> futures = UploadExecutorManager.getUploadExecutorManager().futures;
            //Set<Future<?>> futures = UploadExecutorManager.getUploadExecutorManager().futures;

            synchronized (futures) {

                for (Future<?> future : futures) {
                    allDone &= future.isDone();
                    if (future.isDone()) {
                        totalDone += 1;
                    }
                }

            }

            try {
                Thread.sleep(3*1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            EventBus.getDefault().post(new ProgressBarEvent(totalDone));

            if (allDone && futures.size() == numOfFiles){
                run = false;
                // notify floating widget that all upload tasks are done
                EventBus.getDefault().post(new ProgressDoneEvent(true));
            }


        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.d(TAG, "Total Upload Time, TF mobile: " + totalTime);

        // clear futures that were used to monitor this upload
        UploadExecutorManager.getUploadExecutorManager().clearFutures();

        //shutdown threadpool
        UploadExecutorManager.getUploadExecutorManager().shutdownThreads();
    }


}
