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

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Created by yuh5 on 3/24/2016.
 */
public class Histogram {

    private static final String TAG = "MyDebug";

    private Mat histMat;

    private boolean retakeIm = false;

    public void runHistogram(Mat im, int bins){

        int[] hist = new int[bins];

        im.convertTo(im, CvType.CV_64FC1);

        Core.MinMaxLocResult res = Core.minMaxLoc(im);

        //Mat im_new = im.clone();
        Mat setVauleMat = Mat.zeros(im.rows(), im.cols(), CvType.CV_64FC1);

        double setValue = (res.maxVal - res.minVal)/255;
        setVauleMat.setTo(new Scalar(setValue));

        //Core.multiply(im_new, setVauleMat, im_new);

        double range = (res.maxVal - res.minVal)/256;

        Log.d(TAG, "res.maxVal: " + res.maxVal);
        Log.d(TAG, "res.minVal: " + res.minVal);

        double im_JP[] = new double[(int)im.total()];
        im.get(0,0, im_JP);

        for(int i= 0;i<im_JP.length;i++) {
            int h = (int)(im_JP[i]/range);

            if(h==256){
                hist[h-1]++;
            } else if(h>256){
                retakeIm = true;
            } else {
                hist[h]++;
            }
        }

        histMat = Mat.zeros(1, bins, CvType.CV_32SC1);

        histMat.put(0,0,hist);
        histMat.convertTo(histMat, CvType.CV_32F);

        //release memory
        //im_new.release();
        setVauleMat.release();
    }

    public boolean getRetakeFlag(){
        return retakeIm;
    }

    public Mat getHistMat(){
        return histMat;
    }




}
