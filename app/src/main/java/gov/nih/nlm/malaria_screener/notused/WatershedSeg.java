package gov.nih.nlm.malaria_screener.notused;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.imgproc.Imgproc;

/**
 * Created by linw2 on 8/10/2015.
 */
public class WatershedSeg {

    private Mat markers;
    private Mat bin;
    Mat binCopy;
    private Mat mat_img;
    private Mat outputImage = new Mat();
    //public static final String TAG = "MyDebug";

    File imgFile = null;
    Bitmap bitmap;

    public WatershedSeg(Mat img) {
        //Log.d(TAG, "Start watershed");
        mat_img = img;
        Mat gray = new Mat();
        bin = new Mat(); // black and white 0 and 255
        Mat mor = new Mat();
        Mat sure_bg = new Mat();
        Mat dist = new Mat();
        Mat sure_fg = new Mat();

        Imgproc.cvtColor(mat_img , mat_img, Imgproc.COLOR_BGRA2BGR);

        Imgproc.cvtColor(mat_img, gray, Imgproc.COLOR_BGRA2GRAY);

        // dst(x,y) = 0 if src(x,y) > thresh; maxval otherwise
       double a = Imgproc.threshold(gray, bin, 0, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY_INV);

        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hier = new Mat();
        //Log.d(TAG, "Begin contours.");


        binCopy = bin.clone();
        Imgproc.findContours(binCopy, contours, hier, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            Imgproc.drawContours(bin, contours, contourIdx, new Scalar(255, 255, 255), -1);
        }

        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(3, 3));
        Imgproc.morphologyEx(bin, mor, Imgproc.MORPH_OPEN, kernel, new Point(-1, -1), 2);



        Imgproc.dilate(mor, sure_bg, kernel, new Point(-1, -1), 3);

        Imgproc.distanceTransform(mor, dist, Imgproc.CV_DIST_L2, 5);
        MinMaxLocResult dat = Core.minMaxLoc(dist);

        Imgproc.threshold(dist, sure_fg, .7 * dat.maxVal, 255, 0);
        sure_fg.convertTo(sure_fg, CvType.CV_8U);

        Core.divide(dist, new Scalar(dat.maxVal / 255), dist);

        Mat unknown = new Mat();
        markers = new Mat(dist.size(), CvType.CV_32SC1);

        Core.subtract(sure_bg, sure_fg, unknown);
        ArrayList<MatOfPoint> dist_contours = new ArrayList<MatOfPoint>();

        dist.convertTo(dist, CvType.CV_8U);
        //Log.d(TAG, "Begin contours for markers");
        Mat distCopy = dist.clone();
        Imgproc.findContours(distCopy, dist_contours, hier, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int contourIdx = 0; contourIdx < dist_contours.size(); contourIdx++) {
            Imgproc.drawContours(markers, dist_contours, contourIdx, new Scalar(contourIdx+1), -1);
        }

        Imgproc.circle(markers, new Point(5,5), 3, new Scalar(255,255,255), -1);

        Core.add(markers, new Scalar(1), markers);

//        for (int i = 0; i < markers.rows(); i++) {
//            for (int j = 0; j < markers.cols(); j++) {
//                if (unknown.get(i, j)[0] == 255) {
//                    markers.put(i, j, 0, 0, 0);
//                }
//            }
//        }

        // matrix computation approach
        Mat All255 = Mat.ones(markers.rows(), markers.cols(), CvType.CV_32SC1);
        Mat Ones = Mat.ones(markers.rows(), markers.cols(), CvType.CV_32SC1);
        Mat mask = new Mat(); // label mat that tells which pixels are 255
        All255.setTo(new Scalar(255));
        unknown.convertTo(unknown, CvType.CV_32SC1);
        Core.divide(unknown, All255, mask);
        Core.bitwise_xor(mask, Ones, mask);
        Core.multiply(markers,mask,markers);

        //Log.d(TAG, "Actual watershed call.");
        Imgproc.watershed(mat_img, markers);
        //Log.d(TAG, "Watershed call success!");
		//Mat border = new Mat(mat_img.rows()-2, mat_img.cols()-2, CvType.CV_32FC1);

//		for(int i = 1; i < mat_img.rows()-1; i++){
//			for(int j = 1; j < mat_img.cols()-1; j++){
//				if(markers.get(i, j)[0] == -1){
//				//	border.put(i, j, 255, 255, 255);
//					mat_img.put(i, j, 255, 0, 0);
////				}else{
//				//	border.put(i, j, 0, 0, 0);
//				}
//			}
//		}

        // draw cell border red
        Mat mask1 = new Mat();  // label mask that tells which pixels are -1
        Mat minusOnes = Mat.ones(markers.rows(), markers.cols(), CvType.CV_32SC1);
        minusOnes.setTo(new Scalar(-1));
        Core.compare(markers, minusOnes, mask1, Core.CMP_EQ);

        Vector<Mat> imageSplits = new Vector<Mat>();
        Mat channel1 = Mat.zeros(markers.rows(), markers.cols(), CvType.CV_32SC1);
        Mat channel2 = Mat.zeros(markers.rows(), markers.cols(), CvType.CV_32SC1);
        Mat channel3 = Mat.zeros(markers.rows(), markers.cols(), CvType.CV_32SC1);
        Core.split(mat_img, imageSplits);
        Core.add(imageSplits.get(0), mask1, channel1);

        mask1.convertTo(mask1, CvType.CV_32SC1);
        Core.divide(mask1, All255, mask1);
        Core.bitwise_xor(mask1, Ones, mask1);
        Mat temp = new Mat();
        Mat temp1 = new Mat();
        imageSplits.get(1).convertTo(temp, CvType.CV_32SC1);
        imageSplits.get(2).convertTo(temp1, CvType.CV_32SC1);
        Core.multiply(temp, mask1, channel2);
        Core.multiply(temp1, mask1, channel3);

        Vector<Mat> ch = new Vector<Mat>();
        channel1.convertTo(channel1, CvType.CV_32SC1);
        channel2.convertTo(channel2, CvType.CV_32SC1);
        channel3.convertTo(channel3, CvType.CV_32SC1);
        ch.add(channel1); ch.add(channel2); ch.add(channel3);
        Core.merge(ch, outputImage);
        outputImage.convertTo(outputImage, CvType.CV_8U);

        bitmap = Bitmap.createBitmap(outputImage.cols(), outputImage.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImage, bitmap);
        //outputFiles();

        //Log.d(TAG, "Watershed class end.");
    }

    public Mat getMarkers() {
        return markers;
    }

    public Mat getBin() {
        return bin;
    }

    public Mat getCircled(){
        return outputImage;

    }

    public void outputFiles(){

        File textFile = null;

        try{
            imgFile = createImageFile();
            //textFile = createTextFile();
        } catch (IOException e){
            e.printStackTrace();
        }

        if (imgFile!=null){
            FileOutputStream out = null;
            //FileOutputStream outText = null;

            try{
                out = new FileOutputStream(imgFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

//                outText = new FileOutputStream(textFile, true);
//                for (int i = 0; i < bin.rows(); i++) {
//                    for (int j = 0; j < bin.cols(); j++) {
//                        outText.write((bin.get(i, j)[0] + " ").getBytes());
//                    }
//                    outText.write(("\n").getBytes());
//                }


            } catch (IOException e){
                e.printStackTrace();
            } finally {
                try{
                    if(out!=null){
                        out.close();
                        //outText.close();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

    }

    private File createImageFile() throws IOException {

        File Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imgFile = new File(Dir, "Image" + ".PNG");

        return imgFile;
    }

    private File createTextFile() throws IOException {

        File Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imgFile = new File(Dir, "temp" + ".txt");


        return imgFile;
    }

}



