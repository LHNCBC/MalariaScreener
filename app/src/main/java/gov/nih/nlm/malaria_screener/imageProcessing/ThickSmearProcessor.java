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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsData;

public class ThickSmearProcessor {

    private static final String TAG = "MyDebug";

    Mat oriSizeMat;

    int inputSize = 44;

    //Bitmap canvasBitmap;

    Bitmap output;

    int num_th = 400;

    int batch_size = UtilsCustom.batch_size;

    Mat candi_patches = new Mat(); // concatenated parasite candidate patches

    Mat extra_Mat = new Mat();
    Bitmap exraBitmap;

    public ThickSmearProcessor(Mat oriSizeMat) {

        this.oriSizeMat = oriSizeMat;
    }

    public int[] processImage() {

        // reset current parasites and WBC counts
        UtilsData.resetCurrentCounts_thick();

        int[] x = new int[num_th];
        int[] y = new int[num_th];

        long startTime = System.currentTimeMillis();

        int wbcCount = processThickImage(oriSizeMat.getNativeObjAddr(), candi_patches.getNativeObjAddr(), x, y, extra_Mat.getNativeObjAddr());

        // added for debug------------

        /*exraBitmap = Bitmap.createBitmap(extra_Mat.width(), extra_Mat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(extra_Mat, exraBitmap);

        OutputStream outStream = null;
        File file = null;
        try {
            file = createImageFileExtra();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            outStream = new FileOutputStream(file);
            exraBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.flush();
            outStream.close();
        } catch(Exception e) {

        }*/
        //------------------------------------------

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.d(TAG, "Greedy method Time: " + totalTime);

        // set Bitmap to paint
        UtilsCustom.canvasBitmap = Bitmap.createBitmap(oriSizeMat.width(), oriSizeMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(oriSizeMat, UtilsCustom.canvasBitmap);

        Canvas canvas = new Canvas(UtilsCustom.canvasBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);

        //classify image patches
        int parasiteCount = 0;

        UtilsCustom.results.clear();

        int patch_num = candi_patches.height()/inputSize;

        int iteration = patch_num / batch_size;
        int lastBatchSize = patch_num % batch_size;

        float[] floatPixels = new float[inputSize * inputSize * 3 * batch_size];
        float[] floatPixels_last = new float[inputSize * inputSize * 3 * lastBatchSize];

        // normal batches
        for (int i = 0; i < iteration; i++) {

            for (int n = 0; n < batch_size; n++) {

                floatPixels = putInPixels(i, n, batch_size, floatPixels);

            }

            UtilsCustom.tensorFlowClassifier_thick.recongnize_batch_thick(floatPixels, batch_size);
        }

        // last batch
        for (int n = 0; n < lastBatchSize; n++) {

            floatPixels_last = putInPixels(iteration, n, batch_size, floatPixels_last);
        }

        UtilsCustom.tensorFlowClassifier_thick.recongnize_batch_thick(floatPixels, batch_size);

        // ------------------------------------ TF Lite -----------------------------------
        /*List<Bitmap> bitmapList = new ArrayList<>();

        for (int i=0;i<patch_num;i++){
            bitmapList.add(convertToBitmap(i));
        }

        long startTimeNN_1 = System.currentTimeMillis();

        List<Float> probs = UtilsCustom.tensorFlowClassifier_thick_lite.recognizeImage(bitmapList, 0);

        Log.d(TAG, "list1: " + probs.size());

        for (int i=0;i<probs.size()/2;i++){
            // in the loaded TF model 0 is normal, 1 is infected

            if (probs.get(i*2)>probs.get(i*2+1)){
                // normal confidence higher
                UtilsCustom.results.add(0);

            } else {
                // infected confidence higher
                UtilsCustom.results.add(1);

            }
        }

        long endTime_NN_1 = System.currentTimeMillis();
        long totalTime_NN_1 = endTime_NN_1 - startTimeNN_1;
        Log.d(TAG, "Deep learning Time, TF Lite: " + totalTime_NN_1);*/
        // --------------------------------------------------------------------------------

        // draw results on image
        for (int i=0; i <patch_num; i++){

            if (UtilsCustom.results.get(i)==1) {
                parasiteCount++;
                paint.setColor(Color.RED);
                canvas.drawCircle(x[i], y[i], 20, paint);
            } else {
                    /*paint.setColor(Color.BLUE);
                    canvas.drawCircle(x[i], y[i], 20, paint);*/
            }
        }

        int[] res = new int[2];

        res[0] = parasiteCount;
        res[1] = wbcCount;

        //save result bitmap to memory
        //UtilsCustom.resultBitmap = canvasBitmap;

        // result.convertTo(result, CvType.CV_8U);
        //output = Bitmap.createBitmap(candi_patches.cols(), candi_patches.rows(), Bitmap.Config.RGB_565);
        //Utils.matToBitmap(candi_patches, output);

        candi_patches.release();

        return res;
    }

    // TF Lite code
    /*private Bitmap convertToBitmap(int i){

        Bitmap chip_bitmap;

        Rect rect = new Rect(0, i * inputSize, inputSize, inputSize);
        Mat temp = new Mat(candi_patches, rect);

        temp.convertTo(temp, CvType.CV_8U);
        chip_bitmap = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(temp, chip_bitmap);

        return chip_bitmap;
    }*/

    private float[] putInPixels(int i, int n, int batch_size, float[] floatPixels) {

        Bitmap chip_bitmap;
        int[] intPixels = new int[inputSize * inputSize];

        Rect rect = new Rect(0, (i * batch_size + n) * inputSize, inputSize, inputSize);
        Mat temp = new Mat(candi_patches, rect);

        temp.convertTo(temp, CvType.CV_8U);
        chip_bitmap = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(temp, chip_bitmap);

        chip_bitmap.getPixels(intPixels, 0, chip_bitmap.getWidth(), 0, 0, chip_bitmap.getWidth(), chip_bitmap.getHeight());

        for (int j = 0; j < intPixels.length; ++j) {
            floatPixels[n * inputSize * inputSize * 3 + j * 3 + 0] = ((intPixels[j] >> 16) & 0xFF) / 255.0f; //R
            floatPixels[n * inputSize * inputSize * 3 + j * 3 + 1] = ((intPixels[j] >> 8) & 0xFF) / 255.0f;  //G
            floatPixels[n * inputSize * inputSize * 3 + j * 3 + 2] = (intPixels[j] & 0xFF) / 255.0f;         //B
        }

        return floatPixels;
    }

    /*OutputStream outStream = null;
                File file = null;
                try {
                    file = createImageFile(i);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    outStream = new FileOutputStream(file);
                    chip_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                    outStream.flush();
                    outStream.close();
                } catch(Exception e) {

                }*/

    private File createImageFile(int i) throws IOException {

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/patches");

        if (!direct.exists()) {
            direct.mkdirs();
        }


        File imgFile = new File(new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/patches"), i + ".png");

        return imgFile;
    }

    private File createImageFileExtra() throws IOException {

        File imgFile = new File(Environment.getExternalStorageDirectory(), "this_extra.png");

        return imgFile;
    }


    public native int processThickImage(long mat, long result, int[] x, int[] y, long extraMat);
}
