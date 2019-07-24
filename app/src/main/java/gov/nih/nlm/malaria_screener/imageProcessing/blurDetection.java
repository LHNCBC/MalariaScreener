package gov.nih.nlm.malaria_screener.imageProcessing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;

public class blurDetection {

    private static final String TAG = "MyDebug";

    public blurDetection(){

    }

    public static double computeBlur(){

        Mat gray = new Mat();
        Mat destination  = new Mat();

        Imgproc.cvtColor(UtilsCustom.oriSizeMat, gray, Imgproc.COLOR_BGR2GRAY);

        Log.d(TAG, "gray:" + gray);
        Log.d(TAG, "depth:" + gray.depth());
        Log.d(TAG, "channel:" + gray.channels());
        Imgproc.Laplacian(gray, destination, 0);
        MatOfDouble median = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(destination, median, std);

        Log.d(TAG, "std: " + std);

        return Math.pow(std.get(0, 0)[0], 2.0);
    }
}
