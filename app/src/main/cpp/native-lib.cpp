#include <jni.h>
#include <string>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/dnn.hpp>
#include <string>
#include <android/log.h>

#define LOG_TAG "FaceDetection/DetectionBasedTracker"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))

extern "C" {

using namespace cv;
using namespace std;

int height = 44;
int width = 44;

JNIEXPORT jstring

JNICALL
Java_com_example_yuh5_nativetest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

void JNICALL Java_com_example_yuh5_nativetest_EdgeDetection_detectEdges(JNIEnv *env, jobject,
                                                                        jlong gray) {

    Mat &edges = *(cv::Mat *) gray;
    Canny(edges, edges, 50, 250);

}

void JNICALL
Java_gov_nih_nlm_malaria_1screener_imageProcessing_ThickSmearProcessor_processThickImage(
        JNIEnv *env, jobject,
        jlong mat,
        jlong result, jintArray intJNIArray_X, jintArray intJNIArray_Y) {

    Mat &mat_this = *(Mat *) mat;
    Mat *mat_result = (Mat *) result;

    // Step 1: Convert the incoming JNI jintarray to C's jint[]
    jint *intCArray_x = env->GetIntArrayElements(intJNIArray_X, NULL);
    jint *intCArray_y = env->GetIntArrayElements(intJNIArray_Y, NULL);

    copyMakeBorder(mat_this, mat_this, 0, height, 0, width, CV_HAL_BORDER_CONSTANT);

    //---------------------------------------get masks----------------------------------------
    Mat gray;
    cvtColor(mat_this, gray, COLOR_RGB2GRAY);

    Mat mask;
    threshold(gray, mask, 0, 255, THRESH_BINARY | THRESH_OTSU);

    // get border mask~~~~~~~~~~
    // imfill
    divide(mask, mask, mask);

    Mat bw_clone = mask.clone();
    vector<vector<Point> > contours;

    findContours(bw_clone, contours, RETR_LIST, CHAIN_APPROX_NONE);
    bw_clone.release();

    // find the largest contour
    double maxVal = 0;
    int maxValIdx = 0;
    for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
        double contour_area = contourArea(contours[contourIdx]);

        if (maxVal < contour_area) {
            maxVal = contour_area;
            maxValIdx = contourIdx;
        }
    }

    Mat border_mask = mask.clone();
    for (int i = 0; i < contours.size(); i++) {
        if (i !=
            maxValIdx) { // do not fill the largest contour since it's the whole field of the view
            drawContours(border_mask, contours, i, (1), -1);
        }
    }
    contours.clear();

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    // get WBC mask~~~~~~~~~~
    Mat WBC_mask;
    subtract(border_mask, mask, WBC_mask);
    mask.release();

    // bwareaopen
    Mat WBC_clone = WBC_mask.clone();
    vector<vector<Point> > contours1;
    findContours(WBC_clone, contours1, RETR_LIST, CHAIN_APPROX_NONE);
    WBC_clone.release();

    for (int i = 0; i < contours1.size(); i++) {
        double area = contourArea(contours1[i]);

        if (area <= 1200) {
            drawContours(WBC_mask, contours1, i, (0), -1);
        }
    }

    //imdilate
    Mat kernel_5x5 = getStructuringElement(MORPH_ELLIPSE, Size(5, 5));
    morphologyEx(WBC_mask, WBC_mask, MORPH_DILATE, kernel_5x5);

    // imfill
    Mat WBC_mask_clone = WBC_mask.clone();
    vector<vector<Point> > contours2;
    findContours(WBC_mask_clone, contours2, RETR_LIST, CHAIN_APPROX_NONE);
    WBC_mask_clone.release();

    for (int i = 0; i < contours2.size(); i++) {
        drawContours(WBC_mask, contours2, i, 1, -1);
    }
    contours2.clear();

    //imerode
    morphologyEx(border_mask, border_mask, MORPH_ERODE, kernel_5x5);
    kernel_5x5.release();

    // change 0&1 to 0&255, then use bitwise_not to invert image
    Mat all255 = Mat::zeros(border_mask.size(), border_mask.type());
    all255.setTo(255);

    multiply(border_mask, all255, border_mask);

    bitwise_not(border_mask, border_mask);

    divide(border_mask, border_mask, border_mask);

    //added 12/13/2018
    //imdilate
    Mat kernel_44x44 = getStructuringElement(MORPH_ELLIPSE, Size(44, 44));
    morphologyEx(border_mask, border_mask, MORPH_DILATE, kernel_44x44);

    //---------------------------------------get masks----------------------------------------

    //----------------------------------greddy method------------------------------------------

    //~~~~~~~~~~~~~~~~~~~~~~~get rif of noise
    Mat allOnes = Mat::zeros(border_mask.size(), border_mask.type());
    allOnes.setTo(1);

    //candi = candi.*(1-border_mask)
    Mat temp;
    subtract(allOnes, border_mask, temp);
    border_mask.release();
    Mat candi;
    candi = gray.mul(temp);

    //candi(candi<0) = 0;
    Mat allZeros = Mat::zeros(gray.size(), gray.type());
    gray.release();
    Mat candi_compare;
    compare(candi, allZeros, candi_compare, CMP_GE);
    divide(candi_compare, candi_compare, candi_compare);
    multiply(candi, candi_compare, candi);
    candi_compare.release();

    //candi(WBC_mask==1) = 0;
    Mat WBC_mask_compare;
    compare(WBC_mask, allOnes, WBC_mask_compare, CMP_NE);
    divide(WBC_mask_compare, WBC_mask_compare, WBC_mask_compare);
    multiply(candi, WBC_mask_compare, candi);
    WBC_mask_compare.release();

    //~~~~~~~~~~~~~~~~~~~~~~~~get rif of noise

    Mat candi_mask = Mat::zeros(candi.size(), candi.type());

    multiply(WBC_mask, all255, WBC_mask);
    bitwise_not(WBC_mask, WBC_mask);
    divide(WBC_mask, WBC_mask, WBC_mask);
    Mat minMaxMask = temp.mul(WBC_mask);
    WBC_mask.release();
    all255.release();
    temp.release();

    double min_Val;
    double max_Val;
    Point minLoc;
    Point maxLoc;
    minMaxLoc(candi, &min_Val, &max_Val, &minLoc, &maxLoc, minMaxMask);
    minMaxMask.release();

    int num_th = 500;
    int num_patch = 0;

    int rad = (int) (height * 0.5);

    Mat patch;
    Mat patch_test;
    Mat temp1;
    Mat mask_cir = Mat::zeros(height, width,CV_8UC1);
    Mat candi_compare1;

    circle(mask_cir, Point(mask_cir.rows / 2, mask_cir.cols / 2), mask_cir.rows / 2, (1), -1); // make a 3-channel circle mask
    Mat mask_cir_3;
    vector<Mat> channels;
    channels.push_back(mask_cir);
    channels.push_back(mask_cir);
    channels.push_back(mask_cir);
    merge(channels, mask_cir_3);

    Mat all_patches;

    while (num_patch < num_th) {

        Rect rect((minLoc.x - rad), (minLoc.y - rad), width, height);

        patch = mat_this(rect);

        patch_test = patch.mul(mask_cir_3);

        // put x&y coordinates into array
        intCArray_x[num_patch] = minLoc.x;
        intCArray_y[num_patch] = minLoc.y;

        if (num_patch == 0) {
            all_patches = patch_test.clone();
        } else {
            vconcat(all_patches, patch_test, all_patches);
        }

        LOGD("all_patches size: %d, %d", all_patches.size[0], all_patches.size[1]);


        //patch.release();
        //patch_test.release();

        circle(candi_mask, Point(minLoc.x, minLoc.y), rad, (1), -1);

        //candi = candi.*(1-candi_mask);
        subtract(allOnes, candi_mask, temp1);
        candi = candi.mul(temp1);
        temp1.release();

        //candi(candi<0) = 0;
        compare(candi, allZeros, candi_compare1, CMP_GE);
        divide(candi_compare1, candi_compare1, candi_compare1);
        multiply(candi, candi_compare1, candi);
        candi_compare1.release();

        minMaxLoc(candi, &min_Val, &max_Val, &minLoc, &maxLoc, candi);

        num_patch = num_patch + 1;

    }

    *mat_result = all_patches;

    env->ReleaseIntArrayElements(intJNIArray_X, intCArray_x, 0); // release resources
    env->ReleaseIntArrayElements(intJNIArray_Y, intCArray_y, 0); // release resources

    allOnes.release();
    allZeros.release();
    candi.release();
    candi_mask.release();
    patch.release();
    patch_test.release();
    temp1.release();
    mask_cir.release();
    candi_compare1.release();

}


}

