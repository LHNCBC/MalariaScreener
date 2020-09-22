/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

Licensed under GNU General Public License v3.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package gov.nih.nlm.malaria_screener.imageProcessing;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;

public class BlurDetection {

    private static final String TAG = "MyDebug";

    private static ArrayList<Mat> cellChip = new ArrayList<>();

    private static TensorFlowClassifier tensorFlowClassifier = UtilsCustom.tensorFlowClassifier_fMeasure_thin;

    static int height = UtilsCustom.TF_input_height;
    static int width = UtilsCustom.TF_input_width;
    static int batchSize = 2;

    static int[] intPixels = new int[width * height];

    static int chipIndex = 1;

    public BlurDetection(){


    }

    public static boolean computeBlur(){

        /*Mat gray = new Mat();
        Mat destination  = new Mat();

        Imgproc.cvtColor(UtilsCustom.oriSizeMat, gray, Imgproc.COLOR_BGR2GRAY);

        Log.d(TAG, "gray:" + gray);
        Log.d(TAG, "depth:" + gray.depth());
        Log.d(TAG, "channel:" + gray.channels());

        Imgproc.Laplacian(gray, destination, 0);
        MatOfDouble median = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(destination, median, std);

        Log.d(TAG, "std: " + std);*/

        //return Math.pow(std.get(0, 0)[0], 2.0);


        Log.d(TAG, "UtilsCustom.oriSizeMat: " + UtilsCustom.oriSizeMat);

        int minCol = 1650;
        int minRow = 850;
        int w = 2300;
        int h = 1700;

        int num = 5, tile_w = w/num, tile_h = h/num;

        Rect roi = new Rect(minCol, minRow, w, h);
        Mat bigRect = new Mat(UtilsCustom.oriSizeMat, roi);

        Log.d(TAG, "bigRect: " + bigRect);

        Log.d(TAG, "tile_w: " + tile_w);
        Log.d(TAG, "tile_h: " + tile_h);

        for (int i=0; i<num; i++){
            for (int j=0; j<num; j++){

                Rect roi_chip = new Rect(i*tile_w, j*tile_h, tile_w, tile_h);
                Mat chip = new Mat(bigRect, roi_chip);
                cellChip.add(chip);

            }
        }

        UtilsCustom.rectMat = bigRect;

        return runClassification();
    }

    private static boolean runClassification() {

            UtilsCustom.results_fm.clear();
            UtilsCustom.conf_fm.clear(); // for conf results

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

                tensorFlowClassifier.recongnize_fm_batch(floatPixels, batchSize);

            }

            // last batch
            for (int n = 0; n < lastBatchSize; n++) {

                floatPixels_last = putInPixels(iteration, n, floatPixels_last);
            }

            tensorFlowClassifier.recongnize_fm_batch(floatPixels_last, lastBatchSize);


            //--------------------------------------------------------

            cellChip.clear();

            int sum = 0;
            for (int i=0;i<UtilsCustom.results_fm.size();i++) {
                sum = sum + UtilsCustom.results_fm.get(i);
            }

            Log.d(TAG, "sum_fm: " + sum);

            if (sum > UtilsCustom.results_fm.size()/2 + 1){
                return true;
            } else {
                return false;
            }
    }

    private static float[] putInPixels(int i, int n, float[] floatPixels) {

        Bitmap chip_bitmap;
        Bitmap chip_bitmap_test;

        Mat singlechip;

        singlechip = cellChip.get(i * batchSize + n);

        singlechip.convertTo(singlechip, CvType.CV_8U);

        Imgproc.resize(singlechip, singlechip, new Size(width, height), 0, 0, Imgproc.INTER_CUBIC);

        chip_bitmap = Bitmap.createBitmap(singlechip.cols(), singlechip.rows(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(singlechip, chip_bitmap);

        // added for test
       /* chip_bitmap_test = Bitmap.createBitmap(singlechip.cols(), singlechip.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(singlechip, chip_bitmap_test);
        outputChipFiles(chip_bitmap_test);*/
        // added for test end

        chip_bitmap.getPixels(intPixels, 0, chip_bitmap.getWidth(), 0, 0, chip_bitmap.getWidth(), chip_bitmap.getHeight());

        for (int j = 0; j < intPixels.length; ++j) {
            floatPixels[n * width * height * 3 + j * 3 + 0] = ((intPixels[j] >> 16) & 0xFF) / 255.0f; //R
            floatPixels[n * width * height * 3 + j * 3 + 1] = ((intPixels[j] >> 8) & 0xFF) / 255.0f;  //G
            floatPixels[n * width * height * 3 + j * 3 + 2] = (intPixels[j] & 0xFF) / 255.0f;         //B
        }

        return floatPixels;
    }

    private static void outputChipFiles(Bitmap bitmap) {

        /*Bitmap bitmap = Bitmap.createBitmap(chipMat.cols(), chipMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(chipMat, bitmap);*/

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

    private static File createChipFile() throws IOException {

        File Dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "chip");

        if (!Dir.exists()) {
            Dir.mkdirs();
        }

        File imgFile = new File(Dir, "chip_" + chipIndex + ".PNG");

        chipIndex++;

        return imgFile;
    }

}
