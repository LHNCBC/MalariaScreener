package gov.nih.nlm.malaria_screener.imageProcessing;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class ThinSmearProcessor {

    private static final String TAG = "MyDebug";

    float RV = 6; //resize value

    public void processImage(String picturePath){

        Mat oriSizeMat = Imgcodecs.imread(picturePath, Imgcodecs.CV_LOAD_IMAGE_COLOR);
        Imgproc.cvtColor(oriSizeMat, oriSizeMat, Imgproc.COLOR_BGR2RGB);

        Log.d(TAG, "oriSizeMat: " + oriSizeMat );

        // calculate resize scale factor
        float ori_height = 2988;
        float ori_width = 5312;
        float cur_height = oriSizeMat.height();
        float cur_width = oriSizeMat.width();

        float scaleFactor = (float) Math.sqrt((ori_height * ori_width) / (cur_height * cur_width));  // size ratio between 5312/2988 and current images

        RV = 6 / scaleFactor; // SF for the current images
        //Log.d(TAG, "RV: " + RV);

        int width = (int) ((float) oriSizeMat.cols() / RV);
        int height = (int) ((float) oriSizeMat.rows() / RV);

        Mat resizedMat = new Mat();

        Imgproc.resize(oriSizeMat, resizedMat, new Size(width, height), 0, 0, Imgproc.INTER_CUBIC);
    }

}
