package gov.nih.nlm.malaria_screener.findmarkers;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * Created by yuh5 on 3/24/2016.
 */
public class Histogram {

    private Mat histMat;

    private boolean retakeIm = false;

    public Histogram(Mat im, int bins){

        int[] hist = new int[bins];

        im.convertTo(im, CvType.CV_64FC1);

        Core.MinMaxLocResult res = Core.minMaxLoc(im);

        Mat im_new = im.clone();
        Mat setVauleMat = Mat.zeros(im.rows(), im.cols(), CvType.CV_64FC1);

        double setValue = (res.maxVal - res.minVal)/255;
        setVauleMat.setTo(new Scalar(setValue));

        Core.multiply(im_new, setVauleMat, im_new);

        double range = (res.maxVal - res.minVal)/256;

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
        im_new.release();
        setVauleMat.release();
    }

    public boolean getRetakeFlag(){
        return retakeIm;
    }

    public Mat getHistMat(){
        return histMat;
    }




}
