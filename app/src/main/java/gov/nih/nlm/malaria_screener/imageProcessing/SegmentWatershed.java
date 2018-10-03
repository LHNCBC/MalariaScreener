package gov.nih.nlm.malaria_screener.imageProcessing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by yuh5 on 6/16/2016.
 */
public class SegmentWatershed {

    private static final String TAG = "MyDebug";

    Mat result;

    public void runSegmentWatershed(Mat image, Mat mask, Mat marker) {

        Vector<Mat> channels = new Vector<Mat>();
        Core.split(image, channels); // b g r

        Mat green = channels.get(1);
        Mat blue = channels.get(2);

        Mat Imin = new Mat();
        Core.min(green, blue, Imin);
        channels.clear();
        green.release();
        blue.release();

        Imin.convertTo(Imin, CvType.CV_64F); // same as matlab

        Mat kernelX = new Mat(1, 3, CvType.CV_64FC1);
        double data[] = {-0.5, 0, 0.5};
        kernelX.put(0, 0, data);

        Mat kernelY = new Mat(3, 1, CvType.CV_64FC1);
        kernelY.put(0, 0, data);

        // get dx and dy
        Mat dx = new Mat();
        Mat dy = new Mat();

        Imgproc.filter2D(Imin, dx, Imin.depth(), kernelX); // results still different from Matlab (only the outside border)
        Imgproc.filter2D(Imin, dy, Imin.depth(), kernelY); // results still different from Matlab

        Core.pow(dx, 2, dx); // the results are actually dx^2, dy^2. Dont create new Mat variables here to save memory
        Core.pow(dy, 2, dy);

        Mat g = new Mat();
        Core.add(dx, dy, g); // make sure no saturation occurred
        Core.sqrt(g, g);
        dx.release();
        dy.release();

        Mat kernel = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(9, 9));
        Mat mask_dilated = new Mat();
        Imgproc.dilate(mask, mask_dilated, kernel);

        mask_dilated.convertTo(mask_dilated, CvType.CV_64F);
        Mat ones = Mat.ones(mask_dilated.rows(), mask_dilated.cols(), CvType.CV_64FC1);
        Mat BG = new Mat();
        Core.subtract(ones, mask_dilated, BG);
        mask_dilated.release();

        //get BG>0 mask
        Mat all0 = Mat.zeros(BG.rows(), BG.cols(), CvType.CV_64FC1);
        Mat BG_compare = new Mat();
        Core.compare(BG, all0, BG_compare, Core.CMP_GT);
        Core.divide(BG_compare, BG_compare, BG_compare);
        all0.release();

        // make BG>0 all zeors, and BG<0 remain same values
        BG_compare.convertTo(BG_compare, CvType.CV_64F);
        Mat BG_compare_invert = new Mat();
        Core.subtract(ones, BG_compare, BG_compare_invert);
        Core.multiply(BG_compare_invert, BG, BG);
        // add together
        Core.add(BG, BG_compare, BG);
        BG_compare.release();
        BG_compare_invert.release();

        // get BG|marker
        Mat MarkOrBG = new Mat();
        marker.convertTo(marker, CvType.CV_64F);
        Core.divide(marker, marker, marker);
        marker.convertTo(marker, CvType.CV_32F);
        BG.convertTo(BG, CvType.CV_32F);
        Core.bitwise_or(BG, marker, MarkOrBG);
        BG.release();

        //Core.normalize(MarkOrBG, MarkOrBG, 0, 255, Core.NORM_MINMAX);
        Mat In_MarkOrBG = new Mat();
        //Core.bitwise_not(MarkOrBG, In_MarkOrBG);
//        Core.divide(In_MarkOrBG, In_MarkOrBG, In_MarkOrBG);
        ones.convertTo(ones, CvType.CV_32F);
        Core.bitwise_xor(MarkOrBG, ones, In_MarkOrBG);
        MarkOrBG.convertTo(MarkOrBG, CvType.CV_64F);

        //imimposemin
        Mat fm = g.clone();
        Mat PosInf = Mat.zeros(MarkOrBG.rows(), MarkOrBG.cols(), CvType.CV_64FC1);
        PosInf.setTo(new Scalar(Double.POSITIVE_INFINITY));

        In_MarkOrBG.convertTo(In_MarkOrBG, CvType.CV_64F);
        Mat NegOnes = Mat.zeros(In_MarkOrBG.size(), In_MarkOrBG.type());
        NegOnes.setTo(new Scalar(-1));
        Core.multiply(In_MarkOrBG, NegOnes, In_MarkOrBG);
        NegOnes.release();

        Mat InfMask = new Mat();
        Core.add(MarkOrBG, In_MarkOrBG, InfMask);
        Core.multiply(InfMask, PosInf, fm);
        ones.convertTo(ones, fm.type());
        MarkOrBG.release();
        In_MarkOrBG.release();
        InfMask.release();
        PosInf.release();

        Mat allOne = Mat.ones(fm.size(), fm.type());
        Core.subtract(allOne, fm, fm); // has werid holes
        allOne.release();

        Core.MinMaxLocResult res = Core.minMaxLoc(g);
        double range = res.maxVal - res.minVal;
        double h;

        if (range == 0) {
            h = 0.1;
        } else {
            h = range * 0.001;
        }

        // fp1 = I + h
        Mat fp1 = new Mat();
        Mat hMat = Mat.ones(g.rows(), g.cols(), g.type());
        hMat.setTo(new Scalar(h));
        Core.add(g, hMat, fp1);
        g.release();
        hMat.release();

        Core.min(fp1, fm, Imin);
        fp1.release();

        ones.convertTo(ones, fm.type());
        Mat in_fm = new Mat();
        Core.subtract(ones, fm, in_fm);
        Mat in_Imin = new Mat();
        Core.subtract(ones, Imin, in_Imin);
        Imin.release();
        fm.release();

        //imreconstruct
        Mat J = morphReconstruct(in_fm, in_Imin);
        in_fm.release();
        in_Imin.release();

        Core.subtract(ones, J, J);
        ones.release();

        double J_JP[] = new double[(int) J.total()];
        J.get(0, 0, J_JP);

        //change the pixel values from inf&-inf&positive float to 0&1&int labels for openCV watershed
        for (int i = 0; i < J_JP.length; i++) {
            if (J_JP[i] == Double.NEGATIVE_INFINITY | J_JP[i] == Double.POSITIVE_INFINITY) {
                J_JP[i] = 1;
            } else {
                J_JP[i] = 0;
            }
        }

        J.put(0, 0, J_JP);

        Mat marker_copy = marker.clone();
        ArrayList<MatOfPoint> markers_contours = new ArrayList<MatOfPoint>();
        marker_copy.convertTo(marker_copy, CvType.CV_8U);
        Imgproc.findContours(marker_copy, markers_contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        marker_copy.release();
        for (int contourIdx = 0; contourIdx < markers_contours.size(); contourIdx++) {
            Imgproc.drawContours(marker, markers_contours, contourIdx, new Scalar(contourIdx + 1), -1);
        }
        markers_contours.clear();

        marker.convertTo(marker, J.type());
        Core.add(marker, J, J);
        marker.release();

        result = J;

        //release memory
    }

    public Mat morphReconstruct(Mat marker, Mat mask) {

        Mat dst = new Mat();
        Core.min(marker, mask, dst);

        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
        Imgproc.dilate(dst, dst, kernelDilate);
        Core.min(dst, mask, dst);

        Mat temp1 = Mat.zeros(marker.size(), CvType.CV_8UC1);
        Mat temp2 = Mat.zeros(marker.size(), CvType.CV_8UC1);
        marker.release();

        do {
            dst.copyTo(temp1);
            Imgproc.dilate(dst, dst, kernelDilate);
            Core.min(dst, mask, dst);
            Core.compare(temp1, dst, temp2, Core.CMP_NE);

        } while (Core.sumElems(temp2).val[0] != 0);
        mask.release();
        temp1.release();
        temp2.release();

        return dst;
    }


}
