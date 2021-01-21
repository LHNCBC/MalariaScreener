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

package gov.nih.nlm.malaria_screener.custom.Utils;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.box.androidsdk.content.models.BoxSession;

import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.imageProcessing.SVM_Classifier;
import gov.nih.nlm.malaria_screener.imageProcessing.TensorFlowClassifier;

/**
 * Created by yuh5 on 2/8/2018.
 */

public final class UtilsCustom {

    private static final String TAG = "MyDebug";

    public static TensorFlowClassifier tensorFlowClassifier_thin;
    public static TensorFlowClassifier tensorFlowClassifier_thick;
    //public static Classifier_Lite tensorFlowClassifier_thin_lite;
    //public static Classifier_Lite tensorFlowClassifier_thick_lite;
    public static SVM_Classifier svm_classifier;

    public static Mat oriSizeMat;
    public static Bitmap canvasBitmap;

    //for blur detection
    public static Mat rectMat;
    public static TensorFlowClassifier tensorFlowClassifier_fMeasure_thin;

    public static int whichClassifier = 0; // 0 is DL, 1 is SVM
    public static double Th = 0.5;
    public static double Th_thick = 0.5;

    // Cell global variables
    public static ArrayList<Integer> results = new ArrayList<>();   // pred label for each patch
    public static ArrayList<Float> confs = new ArrayList<>();       // confidence for each patch
    public static ArrayList<Float> pos_confs_im = new ArrayList<>();       // image confidence

    public static int[][] cellLocation;
    public static int cellCount = 0;

    public static int batch_size = 8;

    //for blur detection
    public static int TF_input_width = 115;
    public static int TF_input_height = 85;

    public static ArrayList<Integer> results_fm = new ArrayList<>();

    public static ArrayList<Float> conf_fm = new ArrayList<>();

    //-----------------------------------------------------------------------------------------

    /* for camera light exposure */
    public static int maxExposure = 0;
    public static int minExposure = 0;

    public static void write_fm_conf_File(Bundle bundle) {

        String picFile = bundle.getString("picFile");

        String imgStr = picFile.substring(picFile.lastIndexOf("/") + 1);
        int endIndex = imgStr.lastIndexOf(".");
        String imageName = imgStr.substring(0, endIndex);

        Log.d(TAG, "imageName: " + imageName);

        File textFile = null;

        try {
            textFile = createTextFile(imageName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (textFile != null) {
            FileOutputStream outText = null;

            try {

                outText = new FileOutputStream(textFile, true);

                if (textFile.length() == 0) {
                    outText.write(imageName.getBytes());
                    outText.write(("\n").getBytes());
                }

                // write conf for each tile
                for (int i=0;i<conf_fm.size();i++) {
                    outText.write(conf_fm.get(i).toString().getBytes());
                    outText.write(("\n").getBytes());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (outText != null) {
                        outText.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static File createTextFile(String str) throws IOException {

        File direct = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        File Dir = new File(Environment.getExternalStorageDirectory(), "NLM_Malaria_Screener/New");
        File imgFile = new File(Dir, str + ".txt");
        if (!imgFile.exists()) {
            imgFile.createNewFile();
        }

        return imgFile;
    }



}
