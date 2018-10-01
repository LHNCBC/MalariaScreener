package gov.nih.nlm.malaria_screener.custom;

import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Vector;

import gov.nih.nlm.malaria_screener.findmarkers.TensorFlowClassifier;

/**
 * Created by yuh5 on 2/8/2018.
 */

public final class UtilsCustom {

    public static TensorFlowClassifier tensorFlowClassifier;

    // Cell global variables
    public static ArrayList<Integer> results_NN = new ArrayList<>();
    public static int[][] cellLocation;
    public static Mat featureTable;
    public static Vector<Mat> FTchannels;
    public static int cellCount = 0;

    public static String getThreadSignature()
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
