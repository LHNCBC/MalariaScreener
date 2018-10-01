package gov.nih.nlm.malaria_screener.findmarkers;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by yuh5 on 2/24/2016.
 */
public class OtsuThreshold {

    double threshold;

    public OtsuThreshold(Mat image, Mat mask){

        int[] histData = new int[256];

        Mat newImage = new Mat();
        Core.normalize(image, newImage, 0, 255, Core.NORM_MINMAX);
        //newImage.convertTo(newImage, CvType.CV_8U);

        mask.convertTo(mask, CvType.CV_32S);

        Core.MinMaxLocResult res = Core.minMaxLoc(image);

        int length = 0;
        int mask_JP[] = new int[(int)mask.total()];
        double newImage_JP[] = new double[(int)newImage.total()];

        mask.get(0,0, mask_JP);
        newImage.get(0,0, newImage_JP);

        for (int i=0;i<newImage_JP.length;i++){
            if (mask_JP[i]==1){
                int h = (int)newImage_JP[i];
                histData[h]++;
                length++;
            }
        }

        // Total number of pixels
        //int total = srcData.length;
        int total = length;

        float sum = 0;
        for (int t=0 ; t<256 ; t++) sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        threshold = 0;

        for (int t=0 ; t<256 ; t++) {
            wB += histData[t];               // Weight Background
            if (wB == 0) continue;

            wF = total - wB;                 // Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;            // Mean Background
            float mF = (sum - sumB) / wF;    // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        //release memory
        newImage.release();
    }

    public double getThreshold() {
        return threshold;
    }
}
