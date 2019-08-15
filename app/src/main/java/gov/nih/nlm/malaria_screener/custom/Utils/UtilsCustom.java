package gov.nih.nlm.malaria_screener.custom.Utils;

import android.graphics.Bitmap;

import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.imageProcessing.SVM_Classifier;
import gov.nih.nlm.malaria_screener.imageProcessing.TensorFlowClassifier;

/**
 * Created by yuh5 on 2/8/2018.
 */

public final class UtilsCustom {

    public static TensorFlowClassifier tensorFlowClassifier_thin;
    public static TensorFlowClassifier tensorFlowClassifier_thick;
    //public static TFClassifier_Lite tfClassifier_lite;
    public static SVM_Classifier svm_classifier;

    public static Mat oriSizeMat;
    public static Bitmap canvasBitmap;

    public static int whichClassifier = 0; // 0 is DL, 1 is SVM
    public static double SVM_Th = 0.65;

    // Cell global variables
    public static ArrayList<Integer> results = new ArrayList<>();
    public static int[][] cellLocation;
    public static int cellCount = 0;

    public static int TF_input_size = 44;
    public static int batch_size = 8;

    //-----------------------------------------------------------------------------------------

    /* for camera light exposure */
    public static int maxExposure = 0;
    public static int minExposure = 0;

}
