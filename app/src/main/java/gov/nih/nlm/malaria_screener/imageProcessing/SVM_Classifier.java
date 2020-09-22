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

import android.content.Context;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;

public class SVM_Classifier {

    private static final String TAG = "MyDebug";

    Context context;

    double[][] supportVector1;
    double[] alpha1;
    double[] scaleFactor1;
    double[] shift1;
    double bias1;

    double[][] supportVector2;
    double[] alpha2;
    double[] scaleFactor2;
    double[] shift2;
    double bias2;

    double[] labelSVM;
    double[] distSVM;

    public static SVM_Classifier create(Context context) {

        // initialize a classifier
        SVM_Classifier c = new SVM_Classifier();

        c.context = context;

        return c;
    }

    public void run(Mat featureTable) {

        // get feature table from Cell class
        int cellNum = featureTable.rows();

        // read matlab SVM data struct and classify cells with it ***
        int classNum = 2;

        double[][] all_labels = new double[cellNum][classNum];
        double[][] all_dists = new double[cellNum][classNum];

        long startTime = System.currentTimeMillis();

        for (int index = 0; index < classNum; index++) {

            // predict
            Mat testT = featureTable.clone();
            runSVM(testT, index);

            for (int j = 0; j < cellNum; j++) {
                all_labels[j][index] = labelSVM[j];
                all_dists[j][index] = distSVM[j] * labelSVM[j];

            }

        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        Log.d(TAG, "SVM Time: " + totalTime);

        double[] classLabels = new double[cellNum];
        double[] classDists = new double[cellNum];

        for (int j = 0; j < cellNum; j++) {
            if (all_dists[j][0] > all_dists[j][1]) {
                classLabels[j] = 1;
                classDists[j] = all_dists[j][0];
            } else {
                classLabels[j] = 2;
                classDists[j] = all_dists[j][1];
            }

        }

        //adjust sensitivity for SVM
        for (int j = 0; j < cellNum; j++) {

            if (classLabels[j] == 1) {
                classDists[j] = classDists[j] * -1;
            }

            classDists[j] = 1 / (1 + Math.exp(classDists[j] * -1));

            //Log.d(TAG, "classDists[" + j + "]: " + classDists[j]);

            if (classDists[j] < UtilsCustom.Th) {
                classLabels[j] = 1;
            } else {
                classLabels[j] = 2;
            }
        }

        // Note: classLabels in SVM is 1(normal) & 2(infected). However, to keep it same with TensorFlow classifier I change it to 0&1 while putting them in UtilsCustom.results
        UtilsCustom.results.clear();
        for (int i = 0; i < classLabels.length; i++) {

            if (classLabels[i] == 1) {
                UtilsCustom.results.add(0);
            } else if (classLabels[i] == 2) {
                UtilsCustom.results.add(1);
            }
        }

        Log.d(TAG, "SVM done.");


    }

    private void runSVM(Mat mat, int num) {

        Mat Xnew = Mat.zeros(mat.rows(), 48, CvType.CV_64FC1);

        List<Mat> all_Xnew = new ArrayList<>();

        //Log.d(TAG, "supportVector: " + supportVector.length);

        if (num == 0) {

            for (int i = 0; i < supportVector1[0].length; i++) {

                Rect roi = new Rect(i, 0, 1, mat.rows());

                Mat Xnew_col = new Mat(mat, roi);

                Xnew_col.convertTo(Xnew_col, CvType.CV_64FC1);

                Core.add(Xnew_col, new Scalar(shift1[i]), Xnew_col);
                Core.multiply(Xnew_col, new Scalar(scaleFactor1[i]), Xnew_col);
                all_Xnew.add(Xnew_col);

            }

            Core.hconcat(all_Xnew, Xnew);

            Mat svMat = Mat.zeros(supportVector1.length, supportVector1[0].length, CvType.CV_64FC1);
            double sv[] = new double[supportVector1[0].length * supportVector1.length];
            for (int i = 0; i < sv.length; i++) {
                sv[i] = supportVector1[i / 48][i % 48];
            }
            svMat.put(0, 0, sv);

            Mat alphaMat = Mat.zeros(alpha1.length, 1, CvType.CV_64FC1);
            alphaMat.put(0, 0, alpha1);

            Mat t = new Mat();
            Mat XnewT = new Mat();
            Mat tT = new Mat();
            Mat t1 = new Mat();
            Mat fMat = new Mat();

            Core.transpose(Xnew, XnewT);

            Core.gemm(svMat, XnewT, 1, new Mat(), 0, t);
            Core.transpose(t, tT);

            Core.gemm(tT, alphaMat, 1, new Mat(), 0, t1);
            Core.add(t1, new Scalar(bias1), fMat);

            double f[] = new double[(int) fMat.total()];
            double out[] = new double[(int) fMat.total()];
            double dist[] = new double[(int) fMat.total()];

            fMat.get(0, 0, f);
            for (int i = 0; i < f.length; i++) {
                if (f[i] >= 0) {
                    out[i] = 1;
                    dist[i] = f[i];
                } else {
                    out[i] = -1;
                    dist[i] = f[i] * out[i];
                }

            }

            labelSVM = new double[(int) fMat.total()];
            distSVM = new double[(int) fMat.total()];
            labelSVM = out;
            distSVM = dist;
        } else {

            for (int i = 0; i < supportVector2[0].length; i++) {

                Rect roi = new Rect(i, 0, 1, mat.rows());

                Mat Xnew_col = new Mat(mat, roi);

                Xnew_col.convertTo(Xnew_col, CvType.CV_64FC1);

                Core.add(Xnew_col, new Scalar(shift2[i]), Xnew_col);
                Core.multiply(Xnew_col, new Scalar(scaleFactor2[i]), Xnew_col);
                all_Xnew.add(Xnew_col);
            }

            Core.hconcat(all_Xnew, Xnew);

            Mat svMat = Mat.zeros(supportVector2.length, supportVector2[0].length, CvType.CV_64FC1);
            double sv[] = new double[supportVector2[0].length * supportVector2.length];
            for (int i = 0; i < sv.length; i++) {
                sv[i] = supportVector2[i / 48][i % 48];
            }
            svMat.put(0, 0, sv);

            Mat alphaMat = Mat.zeros(alpha2.length, 1, CvType.CV_64FC1);
            alphaMat.put(0, 0, alpha2);

            Mat t = new Mat();
            Mat XnewT = new Mat();
            Mat tT = new Mat();
            Mat t1 = new Mat();
            Mat fMat = new Mat();

            Core.transpose(Xnew, XnewT);

            Core.gemm(svMat, XnewT, 1, new Mat(), 0, t);
            Core.transpose(t, tT);

            Core.gemm(tT, alphaMat, 1, new Mat(), 0, t1);
            Core.add(t1, new Scalar(bias2), fMat);

            double f[] = new double[(int) fMat.total()];
            double out[] = new double[(int) fMat.total()];
            double dist[] = new double[(int) fMat.total()];

            fMat.get(0, 0, f);
            for (int i = 0; i < f.length; i++) {
                if (f[i] >= 0) {
                    out[i] = 1;
                    dist[i] = f[i];
                } else {
                    out[i] = -1;
                    dist[i] = f[i] * out[i];
                }

            }

            labelSVM = new double[(int) fMat.total()];
            distSVM = new double[(int) fMat.total()];
            labelSVM = out;
            distSVM = dist;
        }

    }

    // this function separately read
    public void readSVMTextFile(int num) {

        String svStr;
        String alphaStr;
        String sfStr;
        String shiftStr;

        if (num == 0) {
            svStr = "supportvector";
            alphaStr = "alpha";
            sfStr = "scalefactor";
            shiftStr = "shift";
            bias1 = 0.565332615798344;
        } else {
            svStr = "supportvector1";
            alphaStr = "alpha1";
            sfStr = "scalefactor1";
            shiftStr = "shift1";
            bias2 = -0.565349892568091;
        }

        for (int index = 0; index < 4; index++) {
            //Read text from file
            StringBuilder text = new StringBuilder();

            BufferedReader br;
            try {
                if (index == 0) {
                    InputStream svIS = context.getResources().openRawResource(context.getResources().getIdentifier(svStr, "raw", context.getPackageName()));
                    br = new BufferedReader(new InputStreamReader(svIS));
                } else if (index == 1) {
                    InputStream alphaIS = context.getResources().openRawResource(context.getResources().getIdentifier(alphaStr, "raw", context.getPackageName()));
                    br = new BufferedReader(new InputStreamReader(alphaIS));
                } else if (index == 2) {
                    InputStream sfIS = context.getResources().openRawResource(context.getResources().getIdentifier(sfStr, "raw", context.getPackageName()));
                    br = new BufferedReader(new InputStreamReader(sfIS));
                } else {
                    InputStream shiftIS = context.getResources().openRawResource(context.getResources().getIdentifier(shiftStr, "raw", context.getPackageName()));
                    br = new BufferedReader(new InputStreamReader(shiftIS));
                }
                String line;

                while ((line = br.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String Str = text.toString();

            String[] str1 = Str.split("\n");
            int FTnum = str1.length;  // FeatureTable rows

            if (index == 0) {
                double[][] data = new double[FTnum][48]; // entire table includes index + label

                for (int i = 0; i < str1.length; i++) {
                    String[] temp = str1[i].split(" ");
                    for (int j = 0; j < data[0].length; j++) {
                        data[i][j] = Double.parseDouble(temp[j]);
                    }
                }
                if (num == 0) {
                    supportVector1 = new double[data.length][data[0].length];

                    supportVector1 = data;
                } else {
                    supportVector2 = new double[data.length][data[0].length];

                    supportVector2 = data;
                }
            } else {
                double[] data = new double[FTnum];

                for (int i = 0; i < data.length; i++) {
                    data[i] = Double.parseDouble(str1[i]);
                }

                if (num == 0) {
                    if (index == 1) {
                        alpha1 = new double[data.length];
                        alpha1 = data;

                    } else if (index == 2) {
                        scaleFactor1 = new double[data.length];
                        scaleFactor1 = data;

                    } else {
                        shift1 = new double[data.length];
                        shift1 = data;
                    }
                } else {
                    if (index == 1) {
                        alpha2 = new double[data.length];
                        alpha2 = data;

                    } else if (index == 2) {
                        scaleFactor2 = new double[data.length];
                        scaleFactor2 = data;

                    } else {
                        shift2 = new double[data.length];
                        shift2 = data;
                    }
                }
            }

        }


    }
}
