package gov.nih.nlm.malaria_screener.imageProcessing;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.IOException;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;

public class TensorFlowClassifier {

    private static final String TAG = "MyDebug";

    private TensorFlowInferenceInterface tfHelper;

    private String inputName;
    private String outputName;
    private int inputSize;

    private String[] outputNames;

    int numClasses = 2;

    public static TensorFlowClassifier create(AssetManager assetManager, String modelName, int inputSize, String inputName, String outputName) throws IOException {

        // initialize a classifier
        TensorFlowClassifier c = new TensorFlowClassifier();

        c.inputName = inputName;
        c.outputName = outputName;

        // Pre-allocate buffer.
        c.outputNames = new String[]{outputName};

        c.tfHelper = new TensorFlowInferenceInterface(assetManager, modelName);

        c.inputSize = inputSize;

        return c;
    }

    public boolean recongnize(float[] pixels, int dims){

        float[] output = new float[numClasses * dims];

        tfHelper.feed(inputName, pixels, dims, inputSize, inputSize, 3);

        //tfHelper.feed("keep_prob", new float[] { 1 });

        //get the possible outputs
        tfHelper.run(outputNames);

        //get the output
        tfHelper.fetch(outputName, output);

        //Log.d(TAG, "output: " + output[0] + ", " + output[1]);

        boolean infected;

        if (output[0] > output[1]) {
            infected = false;
        } else {
            infected = true;
        }

        return infected;
    }

    // for Shiva's thin smear classifier
    public void recongnize_batch(float[] pixels, int dims) {

            float[] output = new float[numClasses * dims];

            tfHelper.feed(inputName, pixels, dims, inputSize, inputSize, 3);

            //get the possible outputs
            tfHelper.run(outputNames);

            //get the output
            tfHelper.fetch(outputName, output);

            for (int i = 0; i < output.length / 2; i++) {

                if (output[i*2] > output[i*2+1]) {  // in the loaded TF model(Shiva's) 0 is infected, 1 is normal
                    UtilsCustom.results.add(1);
                    //Log.d(TAG, "result: " + output[i*2] + ", " + output[i*2+1]);
                } else {
                    UtilsCustom.results.add(0);
                    //Log.d(TAG, "result: " + output[i*2] + ", " + output[i*2+1]);
                }
            }

            Log.d(TAG, "One batch over");
    }

    // for thick smear classifier
    public void recongnize_batch_thick(float[] pixels, int dims) {

        float[] output = new float[numClasses * dims];

        tfHelper.feed(inputName, pixels, dims, inputSize, inputSize, 3);

        //get the possible outputs
        tfHelper.run(outputNames);

        //get the output
        tfHelper.fetch(outputName, output);

        for (int i = 0; i < output.length / 2; i++) {

            if (output[i*2] > output[i*2+1]) {  // in the loaded TF model 0 is normal, 1 is infected
                UtilsCustom.results.add(0);

            } else {
                UtilsCustom.results.add(1);

            }
        }

    }

}
