package gov.nih.nlm.malaria_screener.notused;

/**
 * Created by linw2 on 8/10/2015.
 */
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

public class FeatureTable {

    public static final String path = "C://Users//linw2.NIH//Desktop//seg_images//";
    private ArrayList<Mat> cells;

    public FeatureTable(ArrayList<Mat> myCells){

        cells = myCells;
        Mat featureTable = Mat.zeros(new Size(cells.size(), 18), CvType.CV_32F);
        for(int i=1; i<=cells.size(); i++){
            featureTable.put(i, 1, i);
            featureTable.put(i, 2, 0);
            if(i==1)
                GLSFtVec(cells.get(i));
            //featureTable.put(i, 3, 0);
        }

    }

    private static Mat GLSFtVec(Mat mat_img){

        Mat mask = getColorChannel(mat_img, 2); // green channel
        //Imgproc.floodFill(mask, mask, new Point(1, 1), new Scalar(1));
        Core.multiply(mask, new Scalar(255), mask);

        mask = mask.reshape(1, 1);
        Core.transpose(mask, mask);
        Mat zeroInd = new Mat(mask.rows(), mask.cols(), CvType.CV_32SC1);

        for (int i = 0; i < mask.rows(); i++) {
            for (int j = 0; j < mask.cols(); j++) {
                double val = mask.get(i, j)[0];
                if(val==0)
                    zeroInd.put(i, j, i);
            }
        }

        mat_img.convertTo(mat_img, CvType.CV_64FC3, 1.0/255.0);

        normalizedG(mat_img, zeroInd);

        return mat_img;
    }

    private static Mat normalizedG(Mat mat_img, Mat zeroInd){
        Mat red = getColorChannel(mat_img, 3);
        Mat blue = getColorChannel(mat_img, 1);
        Mat green = getColorChannel(mat_img, 2);

        red = red.reshape(1, 1);
        blue = blue.reshape(1, 1);
        green = green.reshape(1, 1);
        Mat rgb = new Mat();
        Core.add(red, blue, red);
        Core.add(red, green, rgb);

        return mat_img;
    }

    private static Mat getColorChannel(Mat in, int ch) {
        Mat mat = new Mat(in.rows(), in.cols(), CvType.CV_32F);
        for (int i = 0; i < in.rows(); i++) {
            for (int j = 0; j < in.cols(); j++) {
                double[] rgb = in.get(i, j);
                mat.put(i, j, Math.ceil(rgb[ch - 1] / 255));
            }
        }

        return mat;
    }

}
