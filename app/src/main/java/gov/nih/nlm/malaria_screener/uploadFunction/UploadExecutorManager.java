package gov.nih.nlm.malaria_screener.uploadFunction;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class UploadExecutorManager {

    private ThreadPoolExecutor uploadThreadPool;
    private final BlockingQueue<Runnable> uploadWorkQueue;

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE*2;
    private static final int KEEP_ALIVE_TIME = 6;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private static UploadExecutorManager uploadExecutorManager = null;


    //public HashMap<Integer ,Future<?>> futures = new HashMap<>();
    public List<Future<?>> futures = Collections.synchronizedList(new ArrayList<Future<?>>());

    static {
        uploadExecutorManager = new UploadExecutorManager();
    }

    private UploadExecutorManager(){
        uploadWorkQueue = new LinkedBlockingQueue<Runnable>();

    }

    public static UploadExecutorManager getUploadExecutorManager(){
        return uploadExecutorManager;
    }

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
