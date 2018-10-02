package gov.nih.nlm.malaria_screener.imageProcessing;

import android.graphics.Bitmap;
import android.os.Process;
import android.util.Log;

import org.datavec.image.loader.BaseImageLoader;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by yuh5 on 12/14/2017.
 */

public class NN_runnable implements Runnable {

    public static final String TAG = "MyDebug";

    BaseImageLoader imgLoader;

    public long NNprocessingTime = 0;

    //public ArrayList<Integer> results_NN;
    int result;

    // Defines a field that contains the calling object of type PhotoTask.
    final TaskRunnableNNMethods mNN_Task;

    int iteration;


    interface TaskRunnableNNMethods {

        /**
         * Sets the Thread that this instance is running on
         *
         * @param currentThread the current Thread
         */
        void setThread(Thread currentThread);

        Mat getChip();

        BaseImageLoader getImageLoader();

        int getiteration();


    }

    /**
     * This constructor creates an instance of PhotoDownloadRunnable and stores in it a reference
     * to the PhotoTask instance that instantiated it.
     *
     * @param NNTask The PhotoTask, which implements TaskRunnableDecodeMethods
     */
    NN_runnable(TaskRunnableNNMethods NNTask) {
        mNN_Task = NNTask;
    }

    @Override
    public void run() {

        //Log.d("ThreadUtils", UtilsCustom.getThreadSignature());

        mNN_Task.setThread(Thread.currentThread());

        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

        iteration = mNN_Task.getiteration();
        imgLoader = mNN_Task.getImageLoader();
        Mat chip4NN = mNN_Task.getChip();

        chip4NN.convertTo(chip4NN, CvType.CV_8U);
        Bitmap chip_bitmap = Bitmap.createBitmap(chip4NN.cols(), chip4NN.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(chip4NN, chip_bitmap);
        //NeuralNetwork neuralNetwork = new NeuralNetwork(bitmap, context);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        chip_bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] bitmapdata = bos.toByteArray();
        ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);

        INDArray imageMat = null;
        try {
            imageMat = imgLoader.asMatrix(bs);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "imageMat: " + imageMat.shape());



        try {
            bos.close();
            bs.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        java.lang.System.gc();

        // Sets the reference to the current Thread to null, releasing its storage
        mNN_Task.setThread(null);

        // Clears the Thread's interrupt flag
        Thread.interrupted();

    }

}
