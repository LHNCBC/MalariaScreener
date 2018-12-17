package gov.nih.nlm.malaria_screener.imageProcessing;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;

public class TFClassifier_Lite {

    public static final String TAG = "MyDebug";

    private Interpreter tfLite;

    private static final int BATCH_SIZE = UtilsCustom.batch_size;

    private static final int DIM_IMG_SIZE = UtilsCustom.TF_input_size;

    private static final int DIM_PIXEL_SIZE = 3;

    private static final int BYTE_NUM = 1;

    private int[] intValues;

    private ByteBuffer imgData = null;



    /**
     * Memory-map the model file in Assets.
     */

    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename)

            throws IOException {

        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());

        FileChannel fileChannel = inputStream.getChannel();

        long startOffset = fileDescriptor.getStartOffset();

        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

    }

    public static TFClassifier_Lite create(AssetManager assetManager, String modelFilename, int inputSize) {

        // initialize a classifier
        TFClassifier_Lite c = new TFClassifier_Lite();

        try {
            c.tfLite = new Interpreter(loadModelFile(assetManager, modelFilename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Pre-allocate buffers.

        c.intValues = new int[inputSize * inputSize];

        return c;
    }

    public void recongnize(int current_batchSize) {

        int[] dims = new int[4];
        dims[0] = current_batchSize;
        dims[1] = DIM_IMG_SIZE;
        dims[2] = DIM_IMG_SIZE;
        dims[3] = DIM_PIXEL_SIZE;

        tfLite.resizeInput(0, dims);

        float[][] labelProb = new float[current_batchSize][2];

        long startTime = SystemClock.uptimeMillis();

        tfLite.run(imgData, labelProb);

        long endTime = SystemClock.uptimeMillis();

        Log.d(TAG, "TF Lite run() on one batch of chips: " + Long.toString(endTime - startTime));

        for(int i=0;i<current_batchSize;i++) {

            if (labelProb[i][0] > labelProb[i][1]) {  // in the loaded TF model 0 is abnormal, 1 is normal
                UtilsCustom.results_NN.add(1);
            } else {
                UtilsCustom.results_NN.add(0);
            }
        }

        //imgData.clear();
        //Log.d(TAG, "conf: " + labelProb[0][0] + ", " + labelProb[0][1]);

    }

    public void process_by_batch(ArrayList<Mat> cellChip){

        int[] input = tfLite.getInputTensor(0).shape();

        Log.d(TAG, "input Tensor: " + input[0] + ", " + input[1] + ", " + input[2] + ", " + input[3]);

        Log.d(TAG, "input Tensor: " + tfLite.getInputTensor(0).numBytes());

        tfLite.setNumThreads(8);

        UtilsCustom.results_NN.clear();

        int NumOfImage = cellChip.size();

        int iteration = NumOfImage / BATCH_SIZE;
        int lastBatchSize = NumOfImage % BATCH_SIZE;

        // normal batches
        imgData = ByteBuffer.allocateDirect(BYTE_NUM * BATCH_SIZE * DIM_IMG_SIZE * DIM_IMG_SIZE * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());

        for (int i = 0; i < iteration; i++) {

            for (int n = 0; n < BATCH_SIZE; n++) {

                convert2Bitmap(cellChip.get(i*BATCH_SIZE + n));

            }

            recongnize(BATCH_SIZE);

            imgData.rewind();
        }

        // last batch
        imgData = ByteBuffer.allocateDirect(BYTE_NUM * lastBatchSize * DIM_IMG_SIZE * DIM_IMG_SIZE * DIM_PIXEL_SIZE);
        imgData.order(ByteOrder.nativeOrder());

        for (int n = 0; n < lastBatchSize; n++) {

            convert2Bitmap(cellChip.get(iteration*BATCH_SIZE + n));

        }

        recongnize(lastBatchSize);

    }

    /**
     * Writes Image data into a {@code ByteBuffer}.
     */

    private void convertBitmapToByteBuffer(Bitmap bitmap) {

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        //imgData.rewind();

        // Convert the image to floating point.

        int pixel = 0;

        for (int i = 0; i < DIM_IMG_SIZE; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE; ++j) {

                final int val = intValues[pixel++];

                imgData.put((byte)((val >> 16) & 0xFF));
                imgData.put((byte)((val >> 8) & 0xFF));
                imgData.put((byte)(val & 0xFF));

//                imgData.putFloat(((val >> 16) & 0xFF) / 255.0f);
//                imgData.putFloat(((val >> 8) & 0xFF) / 255.0f);
//                imgData.putFloat((val & 0xFF) / 255.0f);

            }
        }

    }

    private void convert2Bitmap(Mat singlechip){

        singlechip.convertTo(singlechip, CvType.CV_8U);

        Imgproc.resize(singlechip, singlechip, new Size(DIM_IMG_SIZE, DIM_IMG_SIZE), 0, 0, Imgproc.INTER_CUBIC);

        Bitmap chip_bitmap = Bitmap.createBitmap(singlechip.cols(), singlechip.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(singlechip, chip_bitmap);
        singlechip.release();

        convertBitmapToByteBuffer(chip_bitmap);
    }

}
