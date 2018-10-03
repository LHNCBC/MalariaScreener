package gov.nih.nlm.malaria_screener.imageProcessing;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.CLAHE;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;


/**
 * Created by yuh5 on 7/8/2016.
 */
public class MarkerBasedWatershed {

    private static final String TAG = "MyDebug";

    private boolean retakeIm = false;

    public Mat output_WBCMask; // white blood cell mask
    public Mat watershed_result;

    public void runMarkerBasedWatershed(Mat mat_img, float resizeValue) {

        Vector<Mat> channels = new Vector<Mat>();
        Core.split(mat_img, channels); // b g r
        Mat green = channels.get(1);

        double min_percent = 0.01;
        double max_percent = 0.99; //0.9999;

        Mat stretch_im = stretchHist_8bit(green, min_percent, max_percent);

        if (stretch_im == null) {

        } else {

            stretch_im.convertTo(stretch_im, CvType.CV_64F);

            // im_r % normalize the range to [0,1]
            Mat norm_im = new Mat();
            Core.normalize(stretch_im, norm_im, 0, 1, Core.NORM_MINMAX);
            norm_im.convertTo(norm_im, CvType.CV_64F);
            // negate the image: FG will be light; BG will be dark.
            Mat ones = Mat.ones(stretch_im.rows(), stretch_im.cols(), CvType.CV_64FC1);
            stretch_im.release();

            Core.subtract(ones, norm_im, norm_im);

            // % 90% and up is assumed to be the border in the negative image
            Mat mask_border = Mat.zeros(norm_im.rows(), norm_im.cols(), CvType.CV_32FC1);
            Mat all09 = Mat.zeros(norm_im.rows(), norm_im.cols(), CvType.CV_64FC1);
            // im_r < 0.9
            all09.setTo(new Scalar(0.8)); // need better method to determine this TH
            Core.compare(norm_im, all09, mask_border, Core.CMP_LT);
            Core.divide(mask_border, mask_border, mask_border);

            // imfill  switch to old imfill now 09/11/2017                              // findContours change pixels values
            Mat mask_border_clone = mask_border.clone();
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(mask_border_clone, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

            for (int i = 0; i < contours.size(); i++) {
                Imgproc.drawContours(mask_border, contours, i, new Scalar(1), -1);
            }

            // imfill
            Mat kernel5x5 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(5, 5));
//            mask_border.convertTo(mask_border, CvType.CV_64F);
//            Core.subtract(ones, mask_border, mask_border);
//            Imgproc.morphologyEx(mask_border, mask_border, Imgproc.MORPH_OPEN, kernel5x5);
//            Core.subtract(ones, mask_border, mask_border);

            // imerode
            Imgproc.erode(mask_border, mask_border, kernel5x5);

            //release memory
            all09.release();
            kernel5x5.release();

            long startTime = System.currentTimeMillis();

            // WBC -----------------------------------------------------------------------------------------------------------------
            //Mat mask_border_bi = new Mat();
            mask_border.convertTo(mask_border_clone, CvType.CV_8U);
            ArrayList<MatOfPoint> mask_border_contours = new ArrayList<MatOfPoint>();
            Imgproc.findContours(mask_border_clone, mask_border_contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
            mask_border_clone.release();

            double maxArea = 0;
            int maxAreaIdx = 0;
            if (mask_border_contours.size()>1){
                for (int i=0;i<mask_border_contours.size();i++){

                    double contourArea = Imgproc.contourArea(mask_border_contours.get(i));
                    if (contourArea> maxArea){
                        maxArea = contourArea;
                        maxAreaIdx = i;
                    }

                }
            }

            Rect rect = Imgproc.boundingRect(mask_border_contours.get(maxAreaIdx));
            mask_border_contours.clear();
            Mat cropped = new Mat(mask_border, rect);

            Mat Image_cropped = new Mat(green, rect);
            channels.clear();

            Mat im_d = new Mat();
            Mat im_e = new Mat();
            Mat kernel3x3_1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
            Imgproc.dilate(Image_cropped, im_d, kernel3x3_1);
            Imgproc.erode(Image_cropped, im_e, kernel3x3_1);
            kernel3x3_1.release();
            Mat R = new Mat();
            Core.subtract(im_d, im_e, R);
            Image_cropped.release();
            im_d.release();
            im_e.release();

            Mat kernel4x4 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(4, 4));
            Imgproc.morphologyEx(cropped, cropped, Imgproc.MORPH_ERODE, kernel4x4);
            kernel4x4.release();

            cropped.convertTo(cropped, R.type());
            Core.multiply(R, cropped, R);
            cropped.release();

            Mat twentiesMat = Mat.ones(R.size(), R.type());
            twentiesMat.setTo(new Scalar(20));
            Mat R_compare = new Mat();
            Core.compare(R, twentiesMat, R_compare, Core.CMP_GT);
            Core.divide(R_compare, R_compare, R_compare);
            Mat R_res = new Mat();
            Core.multiply(R, R_compare, R_res);
            twentiesMat.release();
            R_compare.release();

            Scalar sum = Core.sumElems(R_res);
            int countNon0 = Core.countNonZero(R_res);
            double value = 1.7 * sum.val[0] / countNon0;
            Mat valueMat = Mat.ones(R.size(), R.type());
            valueMat.setTo(new Scalar(value));
            Mat WBCMask = new Mat();
            Core.compare(R, valueMat, WBCMask, Core.CMP_GT);
            Core.divide(WBCMask, WBCMask, WBCMask);
            R.release();
            R_res.release();
            valueMat.release();

            //imdilate
            Mat kernel1x1 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));
            Imgproc.morphologyEx(WBCMask, WBCMask, Imgproc.MORPH_DILATE, kernel1x1);
            kernel1x1.release();

            // imfill
//            Mat ones_R = Mat.ones(RMask.size(), RMask.type());
//            //Mat kernel3x3_1 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(5, 5));
//            Core.subtract(ones_R, RMask, RMask);
//            Imgproc.morphologyEx(RMask, RMask, Imgproc.MORPH_OPEN, kernel5x5);
//            Core.subtract(ones_R, RMask, RMask);
            // imfill
            Mat WBCMask_clone = WBCMask.clone();
            List<MatOfPoint> contoursWBC = new ArrayList<MatOfPoint>();
            Imgproc.findContours(WBCMask_clone, contoursWBC, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

            for (int i = 0; i < contoursWBC.size(); i++) {
                Imgproc.drawContours(WBCMask, contoursWBC, i, new Scalar(1), -1);
            }
            contoursWBC.clear();

            // imclearborder
            Mat marker = Mat.ones(WBCMask.size(), WBCMask.type());
            Rect roi_marker = new Rect(1, 1, marker.width() - 2, marker.height() - 2);
            Mat sub = marker.submat(roi_marker);
            sub.setTo(new Scalar(0));
            sub.copyTo(marker.submat(roi_marker));

            Mat im2 = morphReconstruct(marker, WBCMask);
            Core.subtract(WBCMask, im2, WBCMask);
            marker.release();
            im2.release();

            Mat kernel2x2 = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(2, 2));
            Imgproc.morphologyEx(WBCMask, WBCMask, Imgproc.MORPH_ERODE, kernel2x2);
            kernel2x2.release();

            // bwareaopen
            List<MatOfPoint> contoursWBC1 = new ArrayList<MatOfPoint>();
            //Mat RMask_Contours = WBCMask.clone();
            WBCMask_clone = WBCMask.clone();
            Imgproc.findContours(WBCMask_clone, contoursWBC1, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

            Mat WBCMask4draw = new Mat();
            Vector<Mat> ch_RMask = new Vector<Mat>();
            ch_RMask.add(WBCMask);
            ch_RMask.add(WBCMask);
            ch_RMask.add(WBCMask);
            Core.merge(ch_RMask, WBCMask4draw);
            ch_RMask.clear();

            double radius = 20;
            double RBC_avgArea = Math.PI * Math.pow(radius, 2);
            RBC_avgArea = Math.round(RBC_avgArea);
            // size adjust
            RBC_avgArea = RBC_avgArea / resizeValue;

            for (int i = 0; i < contoursWBC1.size(); i++) {
                double area = Imgproc.contourArea(contoursWBC1.get(i));

                if (area <= RBC_avgArea) {
                    Imgproc.drawContours(WBCMask4draw, contoursWBC1, i, new Scalar(0, 0, 0), -1);
                }
            }
            contoursWBC1.clear();

            Vector<Mat> ch_RMask_out = new Vector<Mat>();
            Core.split(WBCMask4draw, ch_RMask_out);
            Core.divide(ch_RMask_out.get(0), ch_RMask_out.get(0), WBCMask);
            WBCMask4draw.release();
            ch_RMask_out.clear();

            List<MatOfPoint> contours_WBCMask_4output = new ArrayList<MatOfPoint>();
            WBCMask_clone = WBCMask.clone();
            Point offset = new Point();
            offset.x = rect.x;  offset.y = rect.y;
            Imgproc.findContours(WBCMask_clone, contours_WBCMask_4output, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE, offset);
            WBCMask_clone.release();

            Mat RMask_unCropped = Mat.zeros(mask_border.size(), WBCMask.type());
            WBCMask.release();
            for (int i = 0; i < contours_WBCMask_4output.size(); i++) {

                Imgproc.drawContours(RMask_unCropped, contours_WBCMask_4output, i, new Scalar(1, 1, 1), -1);
            }

            output_WBCMask = RMask_unCropped;
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            Log.d(TAG, "WBC Time 1: " + totalTime);

            //--------------------------------------------------------------------------------------------------------------------------------------

            //Core.divide(mask_border, mask_border, mask_border);

            // -----------------run Otsu --------------------------------------------
            OtsuThreshold otsuThreshold = new OtsuThreshold();
            otsuThreshold.runOtsuThreshold(norm_im, mask_border);
            double Th = otsuThreshold.threshold / 255;
            otsuThreshold = null;

            Mat mask_alpha = new Mat();
            Mat ThMat = Mat.zeros(norm_im.rows(), norm_im.cols(), CvType.CV_64FC1);
            ThMat.setTo(new Scalar(Th));
            Core.compare(norm_im, ThMat, mask_alpha, Core.CMP_GT);
            Core.divide(mask_alpha, mask_alpha, mask_alpha);
            ThMat.release();

            mask_alpha.convertTo(mask_alpha, CvType.CV_64F);
            mask_border.convertTo(mask_border, CvType.CV_64F);
            Core.multiply(mask_alpha, mask_border, mask_alpha);
            mask_border.release();
            //-------------------------------------------------------------

            // -------------------------- discard small blobs and noise-------------------------
            List<MatOfPoint> contours_mask_alpha = new ArrayList<MatOfPoint>();
            // 1 - mask_alpha
            Core.subtract(ones, mask_alpha, mask_alpha);

            double in_min_area_size = 150;

            mask_alpha.convertTo(mask_alpha, CvType.CV_8U);
            Mat mask4Contours = mask_alpha.clone();
            Imgproc.findContours(mask4Contours, contours_mask_alpha, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
            mask4Contours.release();

//            if (contours1.size()>1500){
//                retakeIm = true;
//            } else {

            // scale up to 255 for drawContours
            mask_alpha.convertTo(mask_alpha, CvType.CV_32F);
            Mat all255 = Mat.zeros(mask_alpha.rows(), mask_alpha.cols(), CvType.CV_32FC1);
            all255.setTo(new Scalar(255));
            Core.multiply(mask_alpha, all255, mask_alpha);

            Mat contour3C = new Mat();
            Vector<Mat> ch_contour = new Vector<Mat>();
            ch_contour.add(mask_alpha);
            ch_contour.add(mask_alpha);
            ch_contour.add(mask_alpha);
            Core.merge(ch_contour, contour3C);
            ch_contour.clear();

            long startTime1 = System.currentTimeMillis();

            // bwareaopen 1
            for (int i = 0; i < contours_mask_alpha.size(); i++) {
                double area = Imgproc.contourArea(contours_mask_alpha.get(i));

                if (area <= in_min_area_size) {
                    Imgproc.drawContours(contour3C, contours_mask_alpha, i, new Scalar(0, 0, 0), -1);
                }
            }

            long endTime1 = System.currentTimeMillis();
            long totalTime1 = endTime1 - startTime1;
            Log.d(TAG, "drawContours Time 1: " + totalTime1);

            Vector<Mat> ch = new Vector<Mat>();
            Core.split(contour3C, ch);
            // 1 - mask_alpha
            Core.subtract(all255, ch.get(0), mask_alpha);
            all255.release();
            ch.clear();

            // save a mask_alpha for bwdist
            Mat mask_alpha_Ones = new Mat();
            Core.divide(mask_alpha, mask_alpha, mask_alpha_Ones); // little difference from matlab, and type differece btw int and double cause error as well
            // convert to int to avoid the error described above
            mask_alpha.convertTo(mask_alpha, CvType.CV_8U);

            //Log.i(TAG, "mask_alpha: " + mask_alpha);

            //Mat contour3C_new = new Mat();
            Vector<Mat> ch_contour1 = new Vector<Mat>();
            ch_contour1.add(mask_alpha);
            ch_contour1.add(mask_alpha);
            ch_contour1.add(mask_alpha);
            Core.merge(ch_contour1, contour3C);
            ch_contour1.clear();

            long startTime2 = System.currentTimeMillis();

            // bwareaopen 2
            for (int i = 0; i < contours_mask_alpha.size(); i++) {
                double area = Imgproc.contourArea(contours_mask_alpha.get(i));

                if (area <= in_min_area_size) {
                    Imgproc.drawContours(contour3C, contours_mask_alpha, i, new Scalar(0, 0, 0), -1);
                }
            }
            contours_mask_alpha.clear();

            long endTime2 = System.currentTimeMillis();
            long totalTime2 = endTime2 - startTime2;
            Log.d(TAG, "drawContours Time 2: " + totalTime2);

            //Mat out_mask_alpha = new Mat();
            Vector<Mat> ch_out = new Vector<Mat>();
            Core.split(contour3C, ch_out);
            Core.divide(ch_out.get(0), ch_out.get(0), mask_alpha);
            contour3C.release();
            ch_out.clear();
            //--------------------------------------------------------------------------------

            Mat Gx = Imgproc.getGaussianKernel(5, 1, CvType.CV_64FC1);
            Mat Gy = Imgproc.getGaussianKernel(5, 1, CvType.CV_64FC1);
            Mat G = new Mat();
            Core.gemm(Gx, Gy.t(), 1, new Mat(), 0, G);

            Core.flip(G, G, 1);
            Mat I = new Mat();
            int anchor = G.cols() - 2 - 1;
            Imgproc.filter2D(norm_im, I, norm_im.depth(), G, new Point(anchor, anchor), 0, Core.BORDER_CONSTANT);
            norm_im.release();

            CLAHE clahe = Imgproc.createCLAHE(2.56, new Size(8, 8)); // 2.56 to 256, as 0.01 to 1 in Matlab, this might not be accurate
            Mat I_0to255 = new Mat();
            Core.normalize(I, I_0to255, 0, 255, Core.NORM_MINMAX);
            I_0to255.convertTo(I_0to255, CvType.CV_8U);
            Mat I2 = new Mat();
            clahe.apply(I_0to255, I2);
            clahe = null;
            I2.convertTo(I2, CvType.CV_64F);
            Core.normalize(I2, I2, 0, 1, Core.NORM_MINMAX);
            I_0to255.release();

            long sTime = System.currentTimeMillis();

            int[] in_sigma = new int[]{5, 6, 9};

            Mat L = Mat.zeros(I.rows(), I.cols(), CvType.CV_64FC3);
            I.release();
            Vector<Mat> ch4L = new Vector<Mat>();
            for (int i = 0; i < 3; i++) {
                ch4L.add(calculate_loG(I2, in_sigma[i]));
            }
            Core.merge(ch4L, L);
            I2.release();
            L.release();

            long eTime = System.currentTimeMillis();
            long tTime = eTime - sTime;
            Log.d(TAG, "long Time: " + tTime);


            // find blob responses across scales.
            Mat Lmin = new Mat();
            //Mat temp = new Mat();
            Core.min(ch4L.get(0), ch4L.get(1), Lmin);
            Core.min(Lmin, ch4L.get(2), Lmin);
            Lmin.convertTo(Lmin, CvType.CV_64FC1);
            ch4L.clear();

            // use distance transform to weigh the blob responses, don't do 1 - mask_alpha like Matlab cause bwdist is opposite(find dist to )
            Mat D = new Mat();
            mask_alpha_Ones.convertTo(mask_alpha_Ones, CvType.CV_8U);
            Imgproc.distanceTransform(mask_alpha_Ones, D, Imgproc.CV_DIST_L2, 5);
            mask_alpha_Ones.release();

            // D < 5
            Mat all5 = Mat.zeros(D.rows(), D.cols(), CvType.CV_64FC1);
            all5.setTo(new Scalar(5));
            Mat D_compare = new Mat();
            D.convertTo(D, CvType.CV_64F);
            Core.compare(D, all5, D_compare, Core.CMP_GT);
            Core.divide(D_compare, D_compare, D_compare);
            D_compare.convertTo(D_compare, CvType.CV_64F);
            Core.multiply(D_compare, D, D);
            //outMat = D.clone();
            all5.release();
            D_compare.release();

            Mat Lm2 = new Mat();
            Core.multiply(D, Lmin, Lm2);
            D.release();
            Lmin.release();
            //outMat = Lm2.clone();

            Mat kernel_ones = Mat.ones(3, 3, CvType.CV_32FC1);
            Core.flip(kernel_ones, kernel_ones, 1);
            int anchor1 = kernel_ones.cols() - 1 - 1;
            Imgproc.filter2D(Lm2, Lm2, Lm2.depth(), kernel_ones, new Point(anchor1, anchor1), 0, Core.BORDER_CONSTANT);

            // produce markers
            Mat markers = imregionalmin(Lm2);
            Lm2.release();

            //Mat out_markers = new Mat();
            Mat kernel3x3 = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
            Imgproc.dilate(markers, markers, kernel3x3);

            // resize the image to 1/3 for seg
//                int resizeValue = 3;
//                Mat oneThird = new Mat();
//                Imgproc.resize(oriSizeMat, oneThird, new Size(oriSizeMat.cols() / resizeValue, oriSizeMat.rows() / resizeValue), 0, 0, Imgproc.INTER_CUBIC);
//
//                Mat out_mask_alpha_Resized = new Mat(); //there is diff between Inter_cubic and Inter_nearest, not sure which one is the best or if it even matters
//                Imgproc.resize(out_mask_alpha, out_mask_alpha_Resized, new Size(oneThird.cols(), oneThird.rows()), 0, 0, Imgproc.INTER_CUBIC);
//
//                Mat out_markers_Resized = new Mat();
//                Imgproc.resize(out_markers, out_markers_Resized, new Size(oneThird.cols(), oneThird.rows()), 0, 0, Imgproc.INTER_NEAREST);

            long st = System.currentTimeMillis();

            // segment watershed
            //SegmentWatershed segmentWatershed = new SegmentWatershed(oneThird, out_mask_alpha_Resized, out_markers_Resized);
            SegmentWatershed segmentWatershed = new SegmentWatershed();
            segmentWatershed.runSegmentWatershed(mat_img, mask_alpha, markers);
            mask_alpha.release();

            long et = System.currentTimeMillis();
            long tT = et - st;
            Log.d(TAG, "SegmentWatershed Time: " + tT);

            Mat WS_result = segmentWatershed.result.clone();
            segmentWatershed.result.release();
            segmentWatershed=null;

            WS_result.convertTo(WS_result, CvType.CV_32S);
//                oneThird.convertTo(oneThird, CvType.CV_8U);
//                Imgproc.watershed(oneThird, WS_result);
            mat_img.convertTo(mat_img, CvType.CV_8U); // change for size

            Imgproc.watershed(mat_img, WS_result);

            // get chips

            // add for the second resize
//                Mat newOnes = Mat.ones(oneThird.size(), CvType.CV_8U);
//                newOnes.convertTo(newOnes, watershed_result.type());
//                Core.compare(watershed_result, newOnes, watershed_result, Core.CMP_GT);

            ones.convertTo(ones, WS_result.type());
            Core.compare(WS_result, ones, WS_result, Core.CMP_GT);

            WS_result.convertTo(WS_result, CvType.CV_8U);

            watershed_result = WS_result;

            //release memory
        }

    }

    private Mat stretchHist_8bit(Mat Green, double min_percent, double max_percent) {

        Mat green = Green.clone();
        green.convertTo(green, CvType.CV_64FC1);

        Mat array = reshape2D(green, 1); //self written function
        Mat centers = histCenters(array, 256); //self written function
        array.release();

        Histogram histogram = new Histogram();
        histogram.runHistogram(green, 256);
        Mat h = histogram.getHistMat().clone();
        histogram.getHistMat().release();
        boolean retakeFlag = histogram.getRetakeFlag();
        histogram = null;

        if (retakeFlag) {
            retakeIm = true;
            return null;
        } else {

            // normalized cumulative histogram
            Mat ch = cumsum(h);                 //self written function
            h.release();
            ch.convertTo(ch, CvType.CV_64F);
            Mat sumH = Mat.zeros(ch.rows(), ch.cols(), CvType.CV_64FC1);
            sumH.setTo(new Scalar(ch.get(0, ch.cols() - 1)[0]));
            Core.divide(ch, sumH, ch);
            sumH.release();
            //outMat = ch.clone();

            // histogram cropping
            double min_P = min_percent;
            double max_P = max_percent;
            int lower = 0;
            int upper = 0;
            double centers_lower;
            double centers_upper;

            double ch_copy[] = new double[(int) ch.total()];
            ch.get(0, 0, ch_copy);
            ch.release();

            for (int i = 0; i < ch_copy.length; i++) {
                if (ch_copy[i] > min_P || ch_copy[i] == min_P) {
                    lower = i;
                    break;
                }
            }

            for (int i = 0; i < ch_copy.length; i++) {
                if (ch_copy[i] > max_P || ch_copy[i] == max_P) {
                    upper = i;
                    break;
                }
            }
            ch_copy = null;

            // set two matrices where all pixel values are lower and upper bound
            centers_lower = centers.get(0, lower)[0];
            centers_upper = centers.get(0, upper)[0];
            centers.release();

            Mat AllLower = Mat.zeros(green.rows(), green.cols(), CvType.CV_32FC1);
            Mat AllUpper = Mat.zeros(green.rows(), green.cols(), CvType.CV_32FC1);
            AllLower.setTo(new Scalar(centers_lower));
            AllUpper.setTo(new Scalar(centers_upper));

            // get mask that says which pixels are stretched
            Mat compare_upper = new Mat();
            Mat compare_lower = new Mat();
            green.convertTo(green, CvType.CV_32F);
            Core.compare(green, AllLower, compare_lower, Core.CMP_LT);
            Core.compare(green, AllUpper, compare_upper, Core.CMP_GT);
            // change mask value from 255 to 1
            Core.divide(compare_lower, compare_lower, compare_lower);
            Core.divide(compare_upper, compare_upper, compare_upper);

            // get complement matrix of the mask
            Mat lowerM_complement = new Mat();
            Mat upperM_complement = new Mat();
            Mat ones = Mat.ones(green.rows(), green.cols(), CvType.CV_32FC1);
            compare_lower.convertTo(compare_lower, CvType.CV_32F);
            compare_upper.convertTo(compare_upper, CvType.CV_32F);
            Core.bitwise_xor(compare_lower, ones, lowerM_complement);
            Core.bitwise_xor(compare_upper, ones, upperM_complement);
            ones.release();

            // get stretched part of the new green matrix
            Mat lowerMat = new Mat();
            Mat upperMat = new Mat();
            Core.multiply(compare_lower, AllLower, lowerMat);
            Core.multiply(compare_upper, AllUpper, upperMat);
            AllLower.release();
            AllUpper.release();
            compare_upper.release();
            compare_lower.release();

            // get un-stretched part of the new green matrix
            Mat new_green = new Mat();
            Core.multiply(green, lowerM_complement, new_green);
            Core.multiply(new_green, upperM_complement, new_green);
            lowerM_complement.release();
            upperM_complement.release();

            // add two parts together
            Core.add(new_green, lowerMat, new_green);
            Core.add(new_green, upperMat, new_green);
            lowerMat.release();
            upperMat.release();

            // set stretched values to int
            new_green.convertTo(new_green, CvType.CV_8U);

            //release memory
            green.release();

            return new_green;
        }


    }

    // "col" is the column number of the new matrix
    private Mat reshape2D(Mat mat, int col) {

        Mat trans = new Mat();

        Core.transpose(mat, trans);

        Mat temp = trans.reshape(1, col);
        trans.release();

        Mat res = new Mat();
        Core.transpose(temp, res);
        temp.release();

        return res;
    }

    private Mat histCenters(Mat array, int bins) {

        Core.MinMaxLocResult res = Core.minMaxLoc(array);

        double dist = (res.maxVal - res.minVal) / (bins * 2);

        Mat centers = Mat.zeros(1, bins, CvType.CV_64FC1);

        centers.put(0, bins - 1, res.maxVal - dist);
        for (int i = 1; i < bins; i++) {
            double center = (res.maxVal - dist) - dist * 2 * i;
            centers.put(0, (bins - 1) - i, center);
        }

        return centers;
    }

    private Mat cumsum(Mat h) {

        float h_copy[] = new float[(int) h.total()];
        h.get(0, 0, h_copy);

        Mat ch = Mat.zeros(h.rows(), h.cols(), CvType.CV_64FC1);
        double ch_copy[] = new double[(int) h.total()];
        double temp = 0;
        for (int i = 0; i < h_copy.length; i++) {
            temp = temp + h_copy[i];
            ch_copy[i] = temp;
        }

        ch.put(0, 0, ch_copy);

        return ch;
    }

    private Mat imregionalmin(Mat im) {

        Mat all0 = Mat.zeros(im.rows(), im.cols(), im.type());
        Mat outSide_mask = new Mat();
        Core.compare(im, all0, outSide_mask, Core.CMP_EQ);
        all0.release();

        Mat eroded = new Mat();
        Imgproc.erode(im, eroded, new Mat());
        Mat localMin = new Mat();
        Core.compare(eroded, im, localMin, Core.CMP_EQ);
        Core.subtract(localMin, outSide_mask, localMin);
        outSide_mask.release();
        eroded.release();

        return localMin;
    }

    private Mat calculate_loG(Mat im, int sigma) {

        int G_length = (int) Math.floor((sigma * 6 + 1) / 2);

        Mat x = Mat.zeros(1, sigma * 3 * 2 + 1, CvType.CV_64FC1);
        int start = -G_length;

        double x_copy[] = new double[(int) x.total()];
        for (int i = 0; i < x_copy.length; i++) {
            x_copy[i] = start;
            start++;
        }
        x.put(0, 0, x_copy);

        Mat x2 = new Mat();
        Core.pow(x, 2, x2);
        x.release();

        Mat sigmaMat = Mat.zeros(1, sigma * 3 * 2 + 1, CvType.CV_64FC1);
        sigmaMat.setTo(new Scalar(sigma));
        Mat sigmaMat2 = new Mat();
        Core.pow(sigmaMat, 2, sigmaMat2);
        Mat sigmaMat4 = new Mat();
        Core.pow(sigmaMat, 4, sigmaMat4);
        sigmaMat.release();
        //outMat = sigmaMat4.clone();

        double D = Math.sqrt(2 * Math.PI) * sigma;

        // build Gx
        Mat GaussX = new Mat();
        Core.divide(x2, sigmaMat2, GaussX);
        Mat all05 = Mat.zeros(1, sigma * 3 * 2 + 1, CvType.CV_64FC1);
        all05.setTo(new Scalar(-0.5));
        Core.multiply(GaussX, all05, GaussX);
        Core.exp(GaussX, GaussX);
        Mat allD = Mat.zeros(1, sigma * 3 * 2 + 1, CvType.CV_64FC1);
        allD.setTo(new Scalar(D));
        Core.divide(GaussX, allD, GaussX);
        //Core.transpose(Imgproc.getGaussianKernel(sigma*3*2+1, 5, CvType.CV_32FC1), GaussX);
        all05.release();
        allD.release();

        // build dGxx
        Mat dGxx = new Mat();
        Core.subtract(x2, sigmaMat2, dGxx);
        Core.divide(dGxx, sigmaMat4, dGxx);
        Core.multiply(dGxx, GaussX, dGxx);
        x2.release();
        sigmaMat2.release();
        sigmaMat4.release();

        Mat Ixx = new Mat();
        Mat Iyy = new Mat();

        Core.flip(GaussX, GaussX, 1);
        Core.flip(dGxx, dGxx, 1);

        Imgproc.filter2D(im, Ixx, -1, GaussX.t(), new Point(0, G_length), 0, Core.BORDER_CONSTANT);
        Imgproc.filter2D(Ixx, Ixx, -1, dGxx, new Point(G_length, 0), 0, Core.BORDER_CONSTANT);

        Imgproc.filter2D(im, Iyy, -1, dGxx.t(), new Point(0, G_length), 0, Core.BORDER_CONSTANT);
        Imgproc.filter2D(Iyy, Iyy, -1, GaussX, new Point(G_length, 0), 0, Core.BORDER_CONSTANT);

        Imgproc.filter2D(Ixx, Ixx, -1, GaussX.t(), new Point(0, G_length), Core.BORDER_CONSTANT);
        Imgproc.filter2D(Ixx, Ixx, -1, GaussX, new Point(G_length, 0), 0, Core.BORDER_CONSTANT);

        Imgproc.filter2D(Iyy, Iyy, -1, GaussX.t(), new Point(0, G_length), Core.BORDER_CONSTANT);
        Imgproc.filter2D(Iyy, Iyy, -1, GaussX, new Point(G_length, 0), 0, Core.BORDER_CONSTANT);
        GaussX.release();

        Mat Laplacian = new Mat();
        Core.add(Ixx, Iyy, Laplacian);
        Ixx.release();
        Iyy.release();

        int pw = (int) Math.floor(dGxx.cols() / 2);
        dGxx.release();

        Laplacian = padBorders(Laplacian, pw, 0);

        return Laplacian;

    }

    // does this padding work?
    private Mat padBorders(Mat I, int w, double value) {

        int rows = I.rows();
        int cols = I.cols();

        // pad left
        Rect roi_L = new Rect(0, 0, w, rows);
        Mat newI_L = new Mat(I, roi_L);
        newI_L.setTo(new Scalar(value));

        // pad right
        Rect roi_R = new Rect(cols - w, 0, w, rows);
        Mat newI_R = new Mat(I, roi_R);
        newI_R.setTo(new Scalar(value));

        // pad top
        Rect roi_U = new Rect(0, 0, cols, w);
        Mat newI_U = new Mat(I, roi_U);
        newI_U.setTo(new Scalar(value));

        // pad down
        Rect roi_D = new Rect(0, rows - w, cols, w);
        Mat newI_D = new Mat(I, roi_D);
        newI_D.setTo(new Scalar(value));

        return I;

    }

    public boolean getRetakeFlag() {
        return retakeIm;
    }

    public Mat morphReconstruct(Mat marker, Mat mask) {

        Mat dst = new Mat();
        Core.min(marker, mask, dst);

        Mat kernelDilate = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_RECT, new Size(3, 3));
        Imgproc.dilate(dst, dst, kernelDilate);
        Core.min(dst, mask, dst);

        Mat temp1 = Mat.zeros(marker.size(), CvType.CV_8UC1);
        Mat temp2 = Mat.zeros(marker.size(), CvType.CV_8UC1);

        do {
            dst.copyTo(temp1);
            Imgproc.dilate(dst, dst, kernelDilate);
            Core.min(dst, mask, dst);
            Core.compare(temp1, dst, temp2, Core.CMP_NE);

        } while (Core.sumElems(temp2).val[0] != 0);
        temp1.release();
        temp2.release();
        kernelDilate.release();

        return dst;
    }

}
