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

package gov.nih.nlm.malaria_screener.imageProcessing.Segmentation;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 * Created by yuh5 on 2/24/2016.
 */
public class OtsuThreshold {

    double threshold;

    public void runOtsuThreshold(Mat image, Mat mask){

        int[] histData = new int[256];

        Mat newImage = new Mat();
        Core.normalize(image, newImage, 0, 255, Core.NORM_MINMAX);
        //newImage.convertTo(newImage, CvType.CV_8U);

        mask.convertTo(mask, CvType.CV_32S);

        Core.MinMaxLocResult res = Core.minMaxLoc(image);

        int length = 0;
        int mask_JP[] = new int[(int)mask.total()];
        double newImage_JP[] = new double[(int)newImage.total()];

        mask.get(0,0, mask_JP);
        newImage.get(0,0, newImage_JP);
        newImage.release();

        for (int i=0;i<newImage_JP.length;i++){
            if (mask_JP[i]==1){
                int h = (int)newImage_JP[i];
                histData[h]++;
                length++;
            }
        }

        // Total number of pixels
        //int total = srcData.length;
        int total = length;

        float sum = 0;
        for (int t=0 ; t<256 ; t++) sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        threshold = 0;

        for (int t=0 ; t<256 ; t++) {
            wB += histData[t];               // Weight Background
            if (wB == 0) continue;

            wF = total - wB;                 // Weight Foreground
            if (wF == 0) break;

            sumB += (float) (t * histData[t]);

            float mB = sumB / wB;            // Mean Background
            float mF = (sum - sumB) / wF;    // Mean Foreground

            // Calculate Between Class Variance
            float varBetween = (float)wB * (float)wF * (mB - mF) * (mB - mF);

            // Check if new maximum found
            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        //release memory

    }

}
