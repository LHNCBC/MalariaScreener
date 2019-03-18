package gov.nih.nlm.malaria_screener;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.imageProcessing.SVM_Classifier;
import gov.nih.nlm.malaria_screener.imageProcessing.TFClassifier_Lite;
import gov.nih.nlm.malaria_screener.imageProcessing.TensorFlowClassifier;


public class Cells {

    private int cellCount = 0;
    //
    public static final String TAG = "MyDebug";

    List<Mat> featureVecs = new ArrayList<>();

    public String locationStr;
    public Mat featureTable = new Mat();

    private ArrayList<Mat> cellChip = new ArrayList<>();

    int CCAreaTh = 2500;
    int chipIndex = 1;

    double ori_height = 2988;
    double ori_width = 5312;

    TensorFlowClassifier tensorFlowClassifier;
    SVM_Classifier svm_classifier;

    TFClassifier_Lite tfClassifier_lite;

    int height = UtilsCustom.TF_input_size;
    int width = UtilsCustom.TF_input_size;
    int channels = 3;
    int batchSize = UtilsCustom.batch_size;

    int[] intPixels = new int[width * height];

    int ccNum;

    public void runCells(Mat mask, Mat WBC_Mask) {

        long startTime = System.currentTimeMillis();

        this.tensorFlowClassifier = UtilsCustom.tensorFlowClassifier_thin;
        this.svm_classifier = UtilsCustom.svm_classifier;

        this.tfClassifier_lite = UtilsCustom.tfClassifier_lite;

        //------------------------------------

        double new_height = UtilsCustom.oriSizeMat.height();
        double new_width = UtilsCustom.oriSizeMat.width();

        double scale = (ori_height * ori_width) / (new_height * new_width);

        //blow up the mask to original size----------------------------------------------

        // turn mask from 0-255 to 0-1
        Core.divide(mask, mask, mask);

        mask.convertTo(mask, CvType.CV_8U);
        Mat newMask = new Mat();
        Imgproc.resize(mask, newMask, new Size(UtilsCustom.oriSizeMat.cols(), UtilsCustom.oriSizeMat.rows()), 0, 0, Imgproc.INTER_CUBIC);
        mask.release();

        // find contour
        Mat maskCopy = newMask.clone();
        ArrayList<MatOfPoint> watershed_contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(maskCopy, watershed_contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        maskCopy.release();

        Mat contour = Mat.zeros(newMask.size(), CvType.CV_8UC1);
        for (int contourIdx = 0; contourIdx < watershed_contours.size(); contourIdx++) {
            Imgproc.drawContours(contour, watershed_contours, contourIdx, new Scalar(1), 1);
        }

        Core.subtract(newMask, contour, newMask);
        contour.release();
        //----------------------------------------------------------------------------------

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        ccNum = Imgproc.connectedComponentsWithStats(newMask, labels, stats, centroids, 4, CvType.CV_32S);
        newMask.release();
        centroids.release();

        StringBuilder cellLoca = new StringBuilder();

        int stats_JP[] = new int[(int) stats.total()];
        stats.get(0, 0, stats_JP);
        stats.release();

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.d(TAG, "init Time : " + totalTime);

        long startTime1 = System.currentTimeMillis();

        // WBC: pick out WBC regions--------------------------------------------------------------
        Mat labels_small = new Mat();
        Mat labelC = labels.clone();
        labelC.convertTo(labelC, WBC_Mask.type());
        Imgproc.resize(labelC, labels_small, new Size(WBC_Mask.cols(), WBC_Mask.rows()), 0, 0, Imgproc.INTER_NEAREST);
        labelC.release();

        Mat numMat = Mat.zeros(labels_small.size(), labels_small.type());
        Mat singleCellMask = new Mat();
        Mat And_res = new Mat();
        int OverlapwWBC_index[] = new int[ccNum - 1];


        for (int i = 1; i < ccNum; i++) {

            numMat.setTo(new Scalar(i)); //30ms
            Core.compare(labels_small, numMat, singleCellMask, Core.CMP_EQ); //70ms
            Core.divide(singleCellMask, singleCellMask, singleCellMask); //50ms

            Core.bitwise_and(singleCellMask, WBC_Mask, And_res);
            singleCellMask.release();
            int nonZero = Core.countNonZero(And_res);
            And_res.release();


            if (nonZero > 0) {
                OverlapwWBC_index[i - 1] = 1;
            }

        }

        WBC_Mask.release();
        labels_small.release();
        numMat.release();
        long endTime1 = System.currentTimeMillis();
        long totalTime1 = endTime1 - startTime1;
        Log.d(TAG, "WBC Time 2: " + totalTime1);
        //--------------------------------------------------------------


        long startTime3 = System.currentTimeMillis();

        for (int i = 1; i < ccNum; i++) { //start at 1 because first rect in stats is whole image

            if (OverlapwWBC_index[i - 1] == 1) { // skip WBC region
                continue;
            } else {

                int minCol = stats_JP[i * 5];
                int minRow = stats_JP[i * 5 + 1];
                int w = stats_JP[i * 5 + 2];
                int h = stats_JP[i * 5 + 3];
                //int area = stats_JP[i * 5 + 4];

                Rect roi = new Rect(minCol, minRow, w, h);

                // get mask of current CC
                Mat labelChip = new Mat(labels, roi);
                labelChip.convertTo(labelChip, CvType.CV_64F);
                double labelChip_copy[] = new double[(int) labelChip.total()];
                labelChip.get(0, 0, labelChip_copy);

                // get rid of small connected component
                if (labelChip.total() <= CCAreaTh / scale) {
                    continue;
                }

                // clean the label of other cells
                for (int index = 0; index < labelChip_copy.length; index++) {

                    if (labelChip_copy[index] != i && labelChip_copy[index] != 0) {
                        labelChip_copy[index] = 0;
                    }
                }

                Mat cleanedChip = Mat.zeros(labelChip.size(), labelChip.type());
                cleanedChip.put(0, 0, labelChip_copy);
                labelChip.release();

                Core.divide(cleanedChip, cleanedChip, cleanedChip);
                cleanedChip.convertTo(cleanedChip, CvType.CV_8U);

                cellLoca.append(minRow + h / 2);
                cellLoca.append(",");
                cellLoca.append(minCol + w / 2);
                cellLoca.append("\n");

                Mat chip = new Mat(UtilsCustom.oriSizeMat, roi);

                Vector<Mat> chipRGB = new Vector<Mat>();
                Mat newChip = new Mat();
                Core.split(chip, chipRGB);
                Core.multiply(chipRGB.get(0), cleanedChip, chipRGB.get(0));
                Core.multiply(chipRGB.get(1), cleanedChip, chipRGB.get(1));
                Core.multiply(chipRGB.get(2), cleanedChip, chipRGB.get(2));
                Core.merge(chipRGB, newChip);
                cleanedChip.release();
                chip.release();

                Mat featureVec = computeFeatureVector(newChip);

                cellChip.add(newChip);

                featureVecs.add(featureVec);

                cellCount++;
            }

        }
        labels.release();
        long endTime3 = System.currentTimeMillis();
        long totalTime3 = endTime3 - startTime3;
        Log.d(TAG, "cell chip loop Time 2: " + totalTime3);

        UtilsCustom.cellCount = cellCount;

        locationStr = cellLoca.toString();

        String[] locaStr = locationStr.split("\n");
        UtilsCustom.cellLocation = new int[locaStr.length][2];

        for (int i = 0; i < locaStr.length; i++) {
            String[] temp1 = locaStr[i].split(",");
            for (int j = 0; j < temp1.length; j++) {
                UtilsCustom.cellLocation[i][j] = Integer.parseInt(temp1[j]);
            }
        }

        // feature table Nx48
        Core.vconcat(featureVecs, featureTable);

        // re-scale for different image size, should be deleted after normalization is applied
        // training image size
        featureTable.convertTo(featureTable, CvType.CV_64F);
        Mat scaleMat = Mat.zeros(featureTable.size(), CvType.CV_64F);
        scaleMat.setTo(new Scalar(scale));
        Core.multiply(featureTable, scaleMat, featureTable);


        runClassification();
        /*long startTimeNN = System.currentTimeMillis();


        tfClassifier_lite.process_by_batch(cellChip);

        long endTime_NN = System.currentTimeMillis();
        long totalTime_NN = endTime_NN - startTimeNN;
        Log.d(TAG, "Deep learning Time, TF Lite: " + totalTime_NN);*/


//        if (picFile!=null) {
//            forSave.convertTo(forSave, CvType.CV_8U);
//            bitmap = Bitmap.createBitmap(forSave.cols(), forSave.rows(), Bitmap.Config.ARGB_8888);
//            UtilsCustom.matToBitmap(forSave, bitmap);
//
//        }

        //release memory
        cellChip.clear();

    }

    private void runClassification(){

        if (UtilsCustom.whichClassifier == 0) { // Deep Learning

            long startTimeNN = System.currentTimeMillis();

            UtilsCustom.results.clear();

            float[] floatPixels = new float[width * height * 3 * batchSize];

            float[] floatPixels_last;

            int NumOfImage = cellChip.size();

            int iteration = NumOfImage / batchSize;
            int lastBatchSize = NumOfImage % batchSize;

            floatPixels_last = new float[width * height * 3 * lastBatchSize];

            // normal batches
            for (int i = 0; i < iteration; i++) {

                for (int n = 0; n < batchSize; n++) {

                    floatPixels = putInPixels(i, n, floatPixels);
                }

                tensorFlowClassifier.recongnize_batch(floatPixels,  batchSize);

            }

            // last batch
            for (int n = 0; n < lastBatchSize; n++) {

                floatPixels_last = putInPixels(iteration, n, floatPixels_last);
            }

            tensorFlowClassifier.recongnize_batch(floatPixels_last, lastBatchSize);

            long endTime_NN = System.currentTimeMillis();
            long totalTime_NN = endTime_NN - startTimeNN;
            Log.d(TAG, "Deep learning Time, TF mobile: " + totalTime_NN);
            //--------------------------------------------------------

        } else if (UtilsCustom.whichClassifier==1){ // SVM
            svm_classifier.run(featureTable);

        }

    }

    private float[] putInPixels(int i, int n, float[] floatPixels) {

        Bitmap chip_bitmap;
        Mat singlechip;

        singlechip = cellChip.get(i * batchSize + n);

        singlechip.convertTo(singlechip, CvType.CV_8U);

        Imgproc.resize(singlechip, singlechip, new Size(width, height), 0, 0, Imgproc.INTER_CUBIC);

        chip_bitmap = Bitmap.createBitmap(singlechip.cols(), singlechip.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(singlechip, chip_bitmap);

        chip_bitmap.getPixels(intPixels, 0, chip_bitmap.getWidth(), 0, 0, chip_bitmap.getWidth(), chip_bitmap.getHeight());

        for (int j = 0; j < intPixels.length; ++j) {
            floatPixels[n * width * height * 3 + j * 3 + 0] = ((intPixels[j] >> 16) & 0xFF) / 255.0f; //R
            floatPixels[n * width * height * 3 + j * 3 + 1] = ((intPixels[j] >> 8) & 0xFF) / 255.0f;  //G
            floatPixels[n * width * height * 3 + j * 3 + 2] = (intPixels[j] & 0xFF) / 255.0f;         //B
        }

        return floatPixels;
    }

    // compute feature vector for each chip/cell
    private Mat computeFeatureVector(Mat roi) {

        Mat chip = roi;
        Mat mask = Mat.zeros(roi.rows(), roi.cols(), CvType.CV_32SC1);

        //get normalized RGB
        Vector<Mat> channels = new Vector<Mat>();
        Core.split(chip, channels);
        Mat sumRGB = Mat.zeros(roi.rows(), roi.cols(), CvType.CV_32SC1);
        Mat normalized_R = Mat.zeros(roi.rows(), roi.cols(), CvType.CV_32FC1);
        Mat normalized_G = Mat.zeros(roi.rows(), roi.cols(), CvType.CV_32FC1);
        Mat normalized_B = Mat.zeros(roi.rows(), roi.cols(), CvType.CV_32FC1);

        channels.get(0).convertTo(channels.get(0), CvType.CV_32S);
        channels.get(1).convertTo(channels.get(1), CvType.CV_32S);
        channels.get(2).convertTo(channels.get(2), CvType.CV_32S);
        Core.add(channels.get(0), channels.get(1), sumRGB);
        Core.add(sumRGB, channels.get(2), sumRGB);

        //roi: cell with original color, background black. So when divide by itself gives us mask
        Core.divide(channels.get(0), channels.get(0), mask);
        mask.convertTo(mask, CvType.CV_8U);

        channels.get(0).convertTo(channels.get(0), CvType.CV_32F);
        channels.get(1).convertTo(channels.get(1), CvType.CV_32F);
        channels.get(2).convertTo(channels.get(2), CvType.CV_32F);
        sumRGB.convertTo(sumRGB, CvType.CV_32F);
        Core.divide(channels.get(0), sumRGB, normalized_R);
        Core.divide(channels.get(1), sumRGB, normalized_G);
        Core.divide(channels.get(2), sumRGB, normalized_B);
        channels.clear();
        sumRGB.release();

        // get histogram
        // channel R
        List<Mat> imagesList1 = new ArrayList<>();
        imagesList1.add(normalized_R);

        Mat hist1 = new Mat();
        MatOfInt channel = new MatOfInt(0);
        MatOfInt histSize = new MatOfInt(16);
        Core.MinMaxLocResult minMax4R = Core.minMaxLoc(normalized_R, mask);
        MatOfFloat rangesR = new MatOfFloat((float) minMax4R.minVal, (float) minMax4R.maxVal);
        Imgproc.calcHist(imagesList1, channel, mask, hist1, histSize, rangesR);
        normalized_R.release();
        imagesList1.clear();

        // channel G
        List<Mat> imagesList2 = new ArrayList<>();
        imagesList2.add(normalized_G);
        Core.MinMaxLocResult minMax4G = Core.minMaxLoc(normalized_G, mask);
        MatOfFloat rangesG = new MatOfFloat((float) minMax4G.minVal, (float) minMax4G.maxVal);
        Mat hist2 = new Mat();
        Imgproc.calcHist(imagesList2, channel, mask, hist2, histSize, rangesG);
        normalized_G.release();
        imagesList2.clear();

        // channel B
        List<Mat> imagesList3 = new ArrayList<>();
        imagesList3.add(normalized_B);
        Core.MinMaxLocResult minMax4B = Core.minMaxLoc(normalized_B, mask);
        MatOfFloat rangesB = new MatOfFloat((float) minMax4B.minVal, (float) minMax4B.maxVal);
        Mat hist3 = new Mat();
        Imgproc.calcHist(imagesList3, channel, mask, hist3, histSize, rangesB);
        normalized_B.release();
        imagesList3.clear();
        mask.release();

        // combine RGB hist together 1x48
        List<Mat> allHist = new ArrayList<>();
        Mat hist = new Mat();
        allHist.add(hist1);
        allHist.add(hist2);
        allHist.add(hist3);
        Core.vconcat(allHist, hist);
        Mat reshapedHist = hist.reshape(1, 1);
        hist1.release();
        hist2.release();
        hist3.release();
        hist.release();

        return reshapedHist;

    }

    public void outputChipFiles(Mat chipMat) {

        Bitmap bitmap = Bitmap.createBitmap(chipMat.cols(), chipMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(chipMat, bitmap);

        File imgFile = null;

        try {
            imgFile = createChipFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (imgFile != null) {
            FileOutputStream out = null;

            try {
                out = new FileOutputStream(imgFile);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }


    private File createTextFile() throws IOException {

        File Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imgFile = new File(Dir, "temp" + ".txt");


        return imgFile;
    }

    private File createChipFile() throws IOException {

        File Dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imgFile = new File(Dir, "chip_" + chipIndex + ".PNG");

        chipIndex++;

        return imgFile;
    }


}
