package gov.nih.nlm.malaria_screener.imageProcessing;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.datavec.image.loader.BaseImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.opencv.core.Mat;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import gov.nih.nlm.malaria_screener.Cells;

/**
 * Created by yuh5 on 12/13/2017.
 */

public class NN_Manager {

    public static final String TAG = "MyDebug";

    // Sets the Time Unit to seconds
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT;

    // Sets the amount of time an idle thread will wait for a task before terminating
    private static final int KEEP_ALIVE_TIME = 5000;

    // Sets the initial threadpool size to 8
    private static final int CORE_POOL_SIZE = 2;

    // Sets the maximum threadpool size to 8
    private static final int MAXIMUM_POOL_SIZE = 2;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    // A queue of Runnables for the image decoding pool
    private  static BlockingQueue<Runnable> NN_WorkQueue = null;

    // A queue of NN_Manager tasks. Tasks are handed to a ThreadPool.
    private final Queue<NNTask> mNNWorkQueue;

    // A managed pool of background download threads
    static public ThreadPoolExecutor NN_threadPoolExecutor;

    // An object that manages Messages in a Thread
    private Handler mHandler;

    // A single instance of PhotoManager, used to implement the singleton pattern
    private static NN_Manager sInstance = null;

    // A static block that sets class fields
    static {

        // The time unit for "keep alive" is in seconds
        KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;

        // Creates a single static instance of PhotoManager
        sInstance = new NN_Manager();
    }

    private NN_Manager(){

        NN_WorkQueue = new LinkedBlockingQueue<Runnable>();

        mNNWorkQueue = new LinkedBlockingQueue<NNTask>();

        NN_threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, NN_WorkQueue);

        mHandler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(Message inputMessage) {
                NNTask nnTask = (NNTask) inputMessage.obj;

                Cells cells = nnTask.getCell();

                recycleTask(nnTask);

//                if (NN_threadPoolExecutor.isTerminated()){
//                    cells.threadPoolFinished();
//                }
            }

        };

    }

    public static NN_Manager getInstance(){
        return sInstance;
    }


    /**
     * Handles state messages for a particular task object
     * @param nnTask A task object
     * @param result The state of the task
     */


    @SuppressLint("HandlerLeak")
    public void handleState(NNTask nnTask, int result, int iteration) {

            Message message = mHandler.obtainMessage(result, iteration, 0, nnTask);

            message.sendToTarget();

    }

    static public void startDownload(Cells cells, Mat chip, BaseImageLoader imageLoader, int iteration){

        if (sInstance.NN_threadPoolExecutor.isTerminated()) {
            NN_threadPoolExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
                    KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, NN_WorkQueue);
        }

        /*
         * Gets a task from the pool of tasks, returning null if the pool is empty
         */
        NNTask nnTask = sInstance.mNNWorkQueue.poll();

        // If the queue was empty, create a new task instead.
        if (null == nnTask) {
            nnTask = new NNTask();
        }

        // Initializes the task
        nnTask.initializeNNTask(NN_Manager.sInstance, cells, chip, imageLoader, iteration);

        sInstance.NN_threadPoolExecutor.execute(nnTask.getNNRunnable());
    }

    /**
     * Recycles tasks by calling their internal recycle() method and then putting them back into
     * the task queue.
     * @param nnTask The task to recycle
     */
    void recycleTask(NNTask nnTask) {

        // Frees up memory in the task
        nnTask.recycle();

        // Puts the task object back into the queue for re-use.
        mNNWorkQueue.offer(nnTask);
    }


}
