package gov.nih.nlm.malaria_screener.custom.Utils;

import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import gov.nih.nlm.malaria_screener.imageProcessing.SVM_Classifier;
import gov.nih.nlm.malaria_screener.imageProcessing.TFClassifier_Lite;
import gov.nih.nlm.malaria_screener.imageProcessing.TensorFlowClassifier;

/**
 * Created by yuh5 on 2/8/2018.
 */

public final class UtilsCustom {

    public static TensorFlowClassifier tensorFlowClassifier_thin;
    public static TensorFlowClassifier tensorFlowClassifier_thick;
    public static TFClassifier_Lite tfClassifier_lite;
    public static SVM_Classifier svm_classifier;

    public static Mat oriSizeMat;

    public static int whichClassifier = 1; // 0 is DL, 1 is SVM
    public static double SVM_Th = 0.65;

    // Cell global variables
    public static ArrayList<Integer> results_NN = new ArrayList<>();
    public static int[][] cellLocation;
    public static int cellCount = 0;

    public static int TF_input_size = 44;
    public static int batch_size = 1;

    //-----------------------------------------------------------------------------------------
    public static final String getThreadSignature()
    {
        Thread t = Thread.currentThread();
        long l = t.getId();
        String name = t.getName();
        long p = t.getPriority();
        String gname = t.getThreadGroup().getName();
        return (name
                + ":(id)" + l
                + ":(priority)" + p
                + ":(group)" + gname);
    }

    public static final int sizeOf(Object object) throws IOException {

        if (object == null)
            return -1;

        // Special output stream use to write the content
        // of an output stream to an internal byte array.
        ByteArrayOutputStream byteArrayOutputStream =
                new ByteArrayOutputStream();

        // Output stream that can write object
        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(byteArrayOutputStream);

        // Write object and close the output stream
        objectOutputStream.writeObject(object);
        objectOutputStream.flush();
        objectOutputStream.close();

        // Get the byte array
        byte[] byteArray = byteArrayOutputStream.toByteArray();

        // TODO can the toByteArray() method return a
        // null array ?
        return byteArray == null ? 0 : byteArray.length;

    }
}
