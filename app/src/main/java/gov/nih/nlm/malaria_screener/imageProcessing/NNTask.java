package gov.nih.nlm.malaria_screener.imageProcessing;

import android.util.Log;

import org.datavec.image.loader.BaseImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.opencv.core.Mat;

import java.lang.ref.WeakReference;

import gov.nih.nlm.malaria_screener.Cells;

/**
 * Created by yuh5 on 12/18/2017.
 */

public class NNTask implements NN_runnable.TaskRunnableNNMethods{

    public static final String TAG = "MyDebug";

    private Mat mChip;

    private WeakReference<Cells> cellsWeakReference;

    private BaseImageLoader mbaseImageLoader;

    private MultiLayerNetwork multiLayerNetwork;

    private int iteration;

    /*
     * Fields containing references to the two runnable objects that handle downloading and
     * decoding of the image.
     */
    private Runnable mNN_runnable;

    // The Thread on which this task is currently running.
    private Thread mCurrentThread;

    /*
     * An object that contains the ThreadPool singleton.
     */
    private static NN_Manager sNNManager;

    /**
     * Creates an PhotoTask containing a download object and a decoder object.
     */
    NNTask() {
        // Create the runnables
        mNN_runnable = new NN_runnable(this);

        sNNManager = NN_Manager.getInstance();

        Log.v(TAG, "New Task");
    }

    void initializeNNTask(NN_Manager nn_manager, Cells cells, Mat chip, BaseImageLoader imageLoader, int iteration){

        sNNManager = nn_manager;

        cellsWeakReference = new WeakReference<Cells>(cells);

        mChip = chip;

        mbaseImageLoader = imageLoader;


        this.iteration = iteration;

    }

    /**
     * Recycles an PhotoTask object before it's put back into the pool. One reason to do
     * this is to avoid memory leaks.
     */
    void recycle() {

        // Deletes the weak reference to the imageView
        if ( null != cellsWeakReference ) {
            cellsWeakReference.clear();
            cellsWeakReference = null;
        }

        multiLayerNetwork = null;
        mbaseImageLoader = null;
        mChip = null;

        // Releases references to the byte buffer and the BitMap

    }

    // Returns the instance
    Runnable getNNRunnable() {
        return mNN_runnable;
    }

    /*
     * Returns the Thread that this Task is running on. The method must first get a lock on a
     * static field, in this case the ThreadPool singleton. The lock is needed because the
     * Thread object reference is stored in the Thread object itself, and that object can be
     * changed by processes outside of this app.
     */
    public Thread getCurrentThread() {
        synchronized(sNNManager) {
            return mCurrentThread;
        }
    }

    /*
     * Sets the identifier for the current Thread. This must be a synchronized operation; see the
     * notes for getCurrentThread()
     */
    public void setCurrentThread(Thread thread) {
        synchronized(sNNManager) {
            mCurrentThread = thread;
        }
    }

    // Implements NN_Runnable.setHTTPDownloadThread(). Calls setThread().
    @Override
    public void setThread(Thread currentThread) {
        setCurrentThread(currentThread);
    }

    // Implements NN_Runnable.getChip. Returns the Mat chip.
    @Override
    public Mat getChip() {
        return mChip;
    }

    @Override
    public BaseImageLoader getImageLoader() {
        return mbaseImageLoader;
    }



    public Cells getCell(){
        return cellsWeakReference.get();
    }

    @Override
    public int getiteration() {
        return iteration;
    }

}
