package gov.nih.nlm.malaria_screener;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import gov.nih.nlm.malaria_screener.custom.UtilsCustom;
import gov.nih.nlm.malaria_screener.findmarkers.TensorFlowClassifier;


public class Cells {

    private int cellCount = 0;
    //
    public static final String TAG = "MyDebug";

    List<Mat> featureVecs = new ArrayList<>();

    public String locationStr;
    public Mat featureTable = new Mat();
    public Vector<Mat> FTchannels;

    private ArrayList<Mat> cellChip;

    int CCAreaTh = 2500;
    int chipIndex = 1;

    double ori_height = 2988;
    double ori_width = 5312;

    TensorFlowClassifier tensorFlowClassifier;

    int height = 44;
    int width = 44;
    int channels = 3;
    int batchSize = 32;

    int[] intPixels = new int[width * height];

    int ccNum;

    long startTimeNN;

    public Cells(Mat mask, Mat oriSizeImage, Context context, Mat WBC_Mask, int whichClassifier) {

        this.tensorFlowClassifier = UtilsCustom.tensorFlowClassifier;

        cellChip = new ArrayList<>();

        //------------------------------------

        double new_height = oriSizeImage.height();
        double new_width = oriSizeImage.width();

        Log.d(TAG, "Ori image type: " + oriSizeImage);

        double scale = (ori_height * ori_width) / (new_height * new_width);

        //blow up the mask to original size
//        Mat resizedMask = new Mat();
//        Imgproc.resize(mask, resizedMask, new Size(oriSizeImage.cols(), oriSizeImage.rows()), 0, 0, Imgproc.INTER_NEAREST);

        // turn mask from 0-255 to 0-1
        Core.divide(mask, mask, mask);

        mask.convertTo(mask, CvType.CV_8U);
        Mat newMask = new Mat();
        Imgproc.resize(mask, newMask, new Size(oriSizeImage.cols(), oriSizeImage.rows()), 0, 0, Imgproc.INTER_CUBIC);

        // find contour
        Mat maskCopy = newMask.clone();
        ArrayList<MatOfPoint> watershed_contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(maskCopy, watershed_contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

        Mat contour = Mat.zeros(newMask.size(), CvType.CV_8UC1);
        for (int contourIdx = 0; contourIdx < watershed_contours.size(); contourIdx++) {
            Imgproc.drawContours(contour, watershed_contours, contourIdx, new Scalar(1), 1);
        }

        Core.subtract(newMask, contour, newMask);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroids = new Mat();
        ccNum = Imgproc.connectedComponentsWithStats(newMask, labels, stats, centroids, 4, CvType.CV_32S);

        StringBuilder cellLoca = new StringBuilder();

        int stats_JP[] = new int[(int) stats.total()];
        stats.get(0, 0, stats_JP);

        // WBC--------------------------------------------------------------
        Mat labels_small = new Mat();
        Mat labelC = labels.clone();
        labelC.convertTo(labelC, WBC_Mask.type());
        Imgproc.resize(labelC, labels_small, new Size(WBC_Mask.cols(), WBC_Mask.rows()), 0, 0, Imgproc.INTER_NEAREST);

        Mat numMat = Mat.zeros(labels_small.size(), labels_small.type());
        Mat singleCellMask = new Mat();
        Mat And_res = new Mat();
        int OverlapwWBC_index[] = new int[ccNum - 1];

        long startTime1 = System.currentTimeMillis();
        for (int i = 1; i < ccNum; i++) {

            numMat.setTo(new Scalar(i)); //30ms
            Core.compare(labels_small, numMat, singleCellMask, Core.CMP_EQ); //70ms
            Core.divide(singleCellMask, singleCellMask, singleCellMask); //50ms

            Core.bitwise_and(singleCellMask, WBC_Mask, And_res);
            int nonZero = Core.countNonZero(And_res);

            if (nonZero > 0) {
                OverlapwWBC_index[i - 1] = 1;
            }

        }
        long endTime1 = System.currentTimeMillis();
        long totalTime1 = endTime1 - startTime1;
        Log.d(TAG, "WBC Time 2: " + totalTime1);
        //--------------------------------------------------------------

        for (int i = 1; i < ccNum; i++) { //start at 1 because first rect in stats is whole image

            if (OverlapwWBC_index[i - 1] == 1) {
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

                Core.divide(cleanedChip, cleanedChip, cleanedChip);
                cleanedChip.convertTo(cleanedChip, CvType.CV_8U);

                cellLoca.append(minRow + h / 2);
                cellLoca.append(",");
                cellLoca.append(minCol + w / 2);
                cellLoca.append("\n");

                Mat chip = new Mat(oriSizeImage, roi);

                Vector<Mat> chipRGB = new Vector<Mat>();
                Mat newChip = new Mat();
                Core.split(chip, chipRGB);
                Core.multiply(chipRGB.get(0), cleanedChip, chipRGB.get(0));
                Core.multiply(chipRGB.get(1), cleanedChip, chipRGB.get(1));
                Core.multiply(chipRGB.get(2), cleanedChip, chipRGB.get(2));
                Core.merge(chipRGB, newChip);

                Mat featureVec = computeFeatureVector(newChip);

                cellChip.add(newChip.clone());

                featureVecs.add(featureVec);

                cellCount++;
            }

        }

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

        FTchannels = new Vector<Mat>();
        Core.split(featureTable, FTchannels);
        UtilsCustom.featureTable = featureTable;
        UtilsCustom.FTchannels = FTchannels;

        startTimeNN = System.currentTimeMillis();

        if (whichClassifier == 0) {
            UtilsCustom.results_NN.clear();

            float[] floatPixels = new float[width * height * 3 * batchSize];

            float[] floatPixels_last;

            int NumOfImage = cellChip.size();

            Log.d(TAG, "NumOfImage: " + NumOfImage);

            int iteration = NumOfImage / batchSize;
            int lastBatchSize = NumOfImage % batchSize;

            floatPixels_last = new float[width * height * 3 * lastBatchSize];

            // normal batches
            for (int i = 0; i < iteration; i++) {

                for (int n = 0; n < batchSize; n++) {

                    floatPixels = putInPixels(i, n, floatPixels);
                }

                long startTime_rec = System.currentTimeMillis();

                tensorFlowClassifier.recongnize_batch(floatPixels,  batchSize);

                long endTime_rec = System.currentTimeMillis();
                long totalTime_rec = endTime_rec - startTime_rec;
                Log.d(TAG, "recongnize Time: " + totalTime_rec);


            }

            // last batch
            for (int n = 0; n < lastBatchSize; n++) {

                floatPixels_last = putInPixels(iteration, n, floatPixels_last);
            }

            tensorFlowClassifier.recongnize_batch(floatPixels_last, lastBatchSize);


            long endTime_NN = System.currentTimeMillis();
            long totalTime_NN = endTime_NN - startTimeNN;
            Log.d(TAG, "Deep learning Time: " + totalTime_NN);
            //--------------------------------------------------------

        }

//        if (picFile!=null) {
//            forSave.convertTo(forSave, CvType.CV_8U);
//            bitmap = Bitmap.createBitmap(forSave.cols(), forSave.rows(), Bitmap.Config.ARGB_8888);
//            UtilsCustom.matToBitmap(forSave, bitmap);
//
//        }

        //release memory
        newMask.release();
        maskCopy.release();
        contour.release();
        labels.release();
        labelC.release();
        stats.release();
        centroids.release();
        labels_small.release();
        numMat.release();
        singleCellMask.release();
        And_res.release();
        cellChip.clear();


    }

    private float[] putInPixels(int i, int n, float[] floatPixels) {

        Bitmap chip_bitmap;
        Mat singlechip;
        //Mat resizedChip = new Mat();

        singlechip = cellChip.get(i * batchSize + n);

        //Log.d(TAG, "singleChip: " + singlechip);

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

        Mat chip = roi.clone();
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

        // channel G
        List<Mat> imagesList2 = new ArrayList<>();
        imagesList2.add(normalized_G);
        Core.MinMaxLocResult minMax4G = Core.minMaxLoc(normalized_G, mask);
        MatOfFloat rangesG = new MatOfFloat((float) minMax4G.minVal, (float) minMax4G.maxVal);
        Mat hist2 = new Mat();
        Imgproc.calcHist(imagesList2, channel, mask, hist2, histSize, rangesG);

        // channel B
        List<Mat> imagesList3 = new ArrayList<>();
        imagesList3.add(normalized_B);
        Core.MinMaxLocResult minMax4B = Core.minMaxLoc(normalized_B, mask);
        MatOfFloat rangesB = new MatOfFloat((float) minMax4B.minVal, (float) minMax4B.maxVal);
        Mat hist3 = new Mat();
        Imgproc.calcHist(imagesList3, channel, mask, hist3, histSize, rangesB);

        // combine RGB hist together 1x48
        List<Mat> allHist = new ArrayList<>();
        Mat hist = new Mat();
        allHist.add(hist1);
        allHist.add(hist2);
        allHist.add(hist3);
        Core.vconcat(allHist, hist);
        Mat reshapedHist = hist.reshape(1, 1);

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
