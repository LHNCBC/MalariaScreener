/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

==============================================================================*/

package gov.nih.nlm.malaria_screener.uploadFunction;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
*   This class is a singleton class that manages the thread pool for image upload.
* */

public class UploadExecutorManager {

    private ThreadPoolExecutor uploadThreadPool;
    private final BlockingQueue<Runnable> uploadWorkQueue;

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE*2;
    private static final int KEEP_ALIVE_TIME = 6;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private static UploadExecutorManager uploadExecutorManager;

    public List<Future<?>> futures = Collections.synchronizedList(new ArrayList<Future<?>>());

    // Initiate manager class instance. Only one instance is needed for this manager class.
    static {
        uploadExecutorManager = new UploadExecutorManager();
    }

    // constructor
    private UploadExecutorManager(){
        uploadWorkQueue = new LinkedBlockingQueue<Runnable>();

    }

    // get the instance of this class
    public static UploadExecutorManager getUploadExecutorManager(){
        return uploadExecutorManager;
    }

    // add image upload task to thread pool & add future to report back when upload is done.
    public void runUploadImage(Runnable task){
        //uploadThreadPool.execute(task);

        Future<?> future = uploadThreadPool.submit(task);
        futures.add(future);

    }

    public void clearFutures(){
        futures.clear();
    }

    public void shutdownThreads(){

        uploadThreadPool.shutdownNow();
    }

    public void startpool(){
        uploadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, uploadWorkQueue);
    }

}
