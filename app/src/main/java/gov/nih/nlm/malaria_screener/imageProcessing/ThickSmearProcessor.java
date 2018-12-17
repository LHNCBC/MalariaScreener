package gov.nih.nlm.malaria_screener.imageProcessing;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.File;
import java.io.IOException;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsData;

public class ThickSmearProcessor {

    private static final String TAG = "MyDebug";

    Mat oriSizeMat;

    int inputSize = 44;

    Bitmap canvasBitmap;

    Bitmap output;

    int num_th = 500;

    public ThickSmearProcessor(Mat oriSizeMat){

        this.oriSizeMat = oriSizeMat;
    }

    public void processImage(){

        // reset current parasites and WBC counts
        UtilsData.resetCurrentCounts();

        Mat candi_patches = new Mat(); // concatenated parasite candidate patches

        //read deep learning model from assets folder
        //String dnnModel = getPath("ThickSmearModel.h5.pb", getApplicationContext());

        int[] x = new int[num_th];
        int[] y = new int[num_th];

        processThickImage(oriSizeMat.getNativeObjAddr(), candi_patches.getNativeObjAddr(), x, y);

        // set Bitmap to paint
        canvasBitmap = Bitmap.createBitmap(oriSizeMat.width(), oriSizeMat.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(oriSizeMat, canvasBitmap);

        Canvas canvas = new Canvas(canvasBitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);

        //classify image patches
        int parasiteNum = 0;

        float[] floatPixels = new float[inputSize * inputSize * 3];
        Bitmap chip_bitmap;
        int[] intPixels;

            for (int i=0;i<num_th;i++) {
                Rect rect = new Rect(0, i*inputSize, inputSize, inputSize);
                Mat temp = new Mat(candi_patches, rect);

                temp.convertTo(temp, CvType.CV_8U);
                chip_bitmap = Bitmap.createBitmap(temp.cols(), temp.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(temp, chip_bitmap);

                intPixels = new int[inputSize * inputSize];
                chip_bitmap.getPixels(intPixels, 0, chip_bitmap.getWidth(), 0, 0, chip_bitmap.getWidth(), chip_bitmap.getHeight());

                for (int j = 0; j < intPixels.length; ++j) {
                    floatPixels[j * 3 + 0] = ((intPixels[j] >> 16) & 0xFF) / 255.0f; //R
                    floatPixels[j * 3 + 1] = ((intPixels[j] >> 8) & 0xFF) / 255.0f;  //G
                    floatPixels[j * 3 + 2] = (intPixels[j] & 0xFF) / 255.0f;         //B
                }

                boolean infected = UtilsCustom.tensorFlowClassifier_thick.recongnize(floatPixels,1);

                if (infected){
                    parasiteNum++;
                    paint.setColor(Color.RED);
                    canvas.drawCircle(x[i], y[i], 20, paint);
                } else {
                    paint.setColor(Color.BLUE);
                    canvas.drawCircle(x[i], y[i], 20, paint);
                }
            }

        // save results
        UtilsData.parasiteCurrent = parasiteNum;
        UtilsData.WBCCurrent = 101;
        UtilsData.parasiteTotal = UtilsData.parasiteTotal + parasiteNum;
        UtilsData.WBCTotal = UtilsData.WBCTotal + 101;
        UtilsData.addParasiteCount(String.valueOf(parasiteNum));
        UtilsData.addWBCCount(String.valueOf(101));

        // result.convertTo(result, CvType.CV_8U);
        output = Bitmap.createBitmap(candi_patches.cols(), candi_patches.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(candi_patches, output);

        candi_patches.release();

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


    public Bitmap getResultBitmap(){
        return canvasBitmap;
    }


    public native void processThickImage(long mat, long result, int[] x, int[] y);
}
