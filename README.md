# Malaria Screener
Official repository for Malaria Screener.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

# Contents

1. [Introduction](#introduction)
2. [Device Compatibility](#device-compatibility)
3. [Features](#features)
4. [Getting Started with Development](#getting-started-with-development)
5. [Substitute Components in Parasite Detection Module](#substitute-components-in-parasite-detection-module)
6. [User Guideline](#user-guideline)
7. [Citation](#citation)


# Introduction
To improve malaria diagnostics, the Lister Hill National Center for Biomedical Communications, an R&D division of the US National Library of Medicine, in collaboration with NIH’s National Institute of Allergy and Infectious Diseases (NIAID) and Mahidol-Oxford University, is developing a fully-automated system for parasite detection and counting in blood films.

Automated malaria light microscopy remains a challenge and an active area of research.
Malaria Screener is an Android mobile application designed to make smartphones an
affordable yet effective solution to this issue. The idea is to utilize the high-resolution
camera and powerful computing power of modern smartphones to screen blood smear
images automatically for parasites and infected red blood cells. Malaria Screener
combines image acquisition, smear image analysis, and result visualization in its slide
screening process, and is equipped with a database to give both app-users and researchers
easy access to the acquired data. Malaria Screener makes the screening process faster,
more consistent, and less dependent on human expertise. The app is modular, allowing
other research groups to integrate their own methods and models for image processing and
machine learning, while acquiring and analyzing their data.



# Features

* Capture smear images with phone camera
* Parasite detection:
  * Thin smear: identify infected red blood cells
  * Thick smear: identify parasites
* Local database and image gallery for the user to access old data.    
* Upload images and metadata from each session to user's Box repository.  

# Device Compatibility

|          |                             |
| -------- | --------------------------- |
| Android  | API Level 21+ (Lollipop 5.0+) |


# Getting Started with Development

## Install IDE: Android Studio
Follow the guide for installing [Android Studio](https://developer.android.com/studio/install), the official IDE for Google's Android operating system.

## Building/Running the Android App

### Import Project to Android Studio
Follow the guide of [import an existing project](https://developer.android.com/studio/projects/create-project#ImportAProject) to setup an Android project from the source code.

### SDK Setup
Upon importing the project, Android Studio should automatically prompt you to install the SDKs that are specified in the project files. However, if not, please follow the guide for using the [Android SDK Manager](https://developer.android.com/studio/intro/update) to install the following SDK components. Older/Newer versions of the components may work as well, but have not necessarily been tested:
   * Platforms:
        * Android 9.0 - Pie (API Level 28)
            * Android SDK Platform 28
            * This version (28) is used to build the project in its current configuration. You can adjust this requirement in the build.gradle file.
   * Tools:
      * Android SDK Tools 26.1.1+
      * Android SDK Platform-Tools 28.0.0+
      * Android SDK Build-Tools 27.0.3+

### Setup OpenCV for native development
This project uses OpenCV native development for some part of the code. OpenCV native development needs OpenCV Android library and CMake. OpenCV Android libary is included in the source code, and CMake should be automatically installed upon importing the project to Android Studio. So, you should not need to do anything for this part.

However, if things do not go as planned, you can always manually install OpenCV and CMake yourself:

Download [OpenCV Android 3.4.2](https://opencv.org/releases/) and [CMake 3.4.1+](https://cmake.org/). Again, older/Newer versions of the components may work as well, but have not necessarily been tested.

Once you have downloaded OpenCV Android and CMake, go to the project directory, and find the file "CMakeLists.txt" under the "app" folder. Then, open that file and edit the following path to point to the OpenCV Android library that you downloaded yourself:
   ```
   include_directories(/#your path here#/OpenCV-android-sdk/sdk/native/jni/include)
   ```
      
### Running the app on a physical device
1. Follow the instructions in the Android Developer Docs to [Enable Developer Options and USB Debugging](https://developer.android.com/studio/debug/dev-options.html)
1. Connect your Android tablet to your computer
1. Tap "OK" on the tablet when prompted to "Allow USB debugging" (Check the box to always allow if you want to make future development easier)
1. Run the app

### Running the app on a emulator
Follow the instructions in the Android Developer Docs to [Run apps on Android Emulator.](https://developer.android.com/studio/run/emulator/?gclid=CjwKCAjwwMn1BRAUEiwAZ_jnEouUFSTsFQaCMKyKCBUu4nbTYeagGnU8L1tVJrWe1k9ojV3rVDYbHxoCmy8QAvD_BwE&gclsrc=aw.ds)

# Substitute Components in Parasite Detection Module
One of our objectives is to allow other research groups to utilize this app to advance their own study. The components in the parasite detection module can be easily substituted so that people can test their own algorithms.

The parasite detection module consists of two components: the segmentation component and the patch classifier component.
In the following section, you will learn, for both thin smear processing and thick smear processing, how to substite the parasite detection module as a whole, and also how to substitute the segmentation and patch classifier components individually. 

## Substitute the parasite detection module entirely

### Thin Smear 
Replace class `ThinSmearProcessor`, which has the following input and output parameters:

Input:
* `Mat UtilsCustom.oriSizeMat` - input image (Mat is the image container in OpenCV library. See this [OpenCV documentation](https://docs.opencv.org/2.4/doc/tutorials/core/mat_the_basic_image_container/mat_the_basic_image_container.html) for more details.)

Output:
* `int cellCount` - number of cells detected in the image
* `int infectedCount` - number of infected cells detected in the image.

### Thick Smear 
Replace class `ThickSmearProcessor`, which has the following input and output parameters:

Input:
* `Mat UtilsCustom.oriSizeMat` - input image

Output:
* `int wbcCount` - number of white blood cells(WBC) detected in the image
* `int parasiteCount` - number of infected cells detected in the image.

## Substitute the Segmentation Component

### Thin Smear
In class `ThinSmearProcessor`, replace the following code snippet with your own implementation:
```Java
MarkerBasedWatershed watershed = new MarkerBasedWatershed();
watershed.runMarkerBasedWatershed(resizedMat, RV);
```
Your class should have the following input and output parameters:

Input:
* `Mat UtilsCustom.oriSizeMat` - input image. Or, the resized image `resizedMat` and the resize scale `RV` if you wish to operate the segmentation on a smaller image to reduce the computational work.

Output:
* `Mat watershedMask` - segmentation mask. A segmentation mask that marks the boundaries of the identified cells.

### Thick Smear
In class `ThickSmearProcessor`, replace the following code snippet with your own implementation:
```Java
int wbcCount = processThickImage(oriSizeMat.getNativeObjAddr(), candi_patches.getNativeObjAddr(), x, y, extra_Mat.getNativeObjAddr());
```
Your class should have the following input and output parameters:

Input:
* `Mat UtilsCustom.oriSizeMat` - input image. Or, the resized image `resizedMat` and the resize scale `RV` if you wish to operate the segmentation on a smaller image to reduce the computational work.

Output:
* `candi_patches` - Segmented candidate patches.
* `wbcCount` - White blood cell number .


## Substitute the Patch Classifier
In Malaria Screener, we use pre-trained Convolutional Neural Network(CNN) models to make binary classifications for the candidate patch images, which are segmented from the original smear images, of both thin and thick smear images. The models can be substituted as follow:

1. Add model to the project by placing your TensorFlow model (Protocol Buffers (.pb) format) under the project directory: `/app/src/main/assets/` 
1. Call the model from code. In `CameraActivity` class, locate and edit the following code snippet accordingly:
```Java
// load TF model
try {

  // thin smear
  String modelNameStr_thin = "XXX.pb";            // model name
  int TF_input_width_thin = M;                    // model input layer width 
  int TF_input_height_thin = N;                   // model input layer height 
  String inputLayerNameStr_thin = "xxx";          // name of the input layer, e.g. "conv2d_20_input"
  String outputLayerNameStr_thin = "xxx";         // name of the output layer, e.g. "output_node0" 

  UtilsCustom.tensorFlowClassifier_thin = TensorFlowClassifier.create(context.getAssets(), modelNameStr_thin, TF_input_width_thin, TF_input_height_thin, inputLayerNameStr_thin, outputLayerNameStr_thin);
                       

  //thick smear
  String modelNameStr_thick = "XXX.pb";           // model name
  int TF_input_width_thick = M;                   // model input layer width 
  int TF_input_height_thick = N;                  // model input layer height
  String inputLayerNameStr_thick = "xxx";         // name of the input layer, e.g. "conv2d_20_input"   
  String outputLayerNameStr_thick = "xxx";        // name of the output layer, e.g. "output_node0"

  UtilsCustom.tensorFlowClassifier_thick = TensorFlowClassifier.create(context.getAssets(), modelNameStr_thick, TF_input_width_thick, TF_input_height_thick, inputLayerNameStr_thick, outputLayerNameStr_thick);

```

# User Guideline

User Guideline is attached as a PDF file (User_guideline.pdf) in the source code package. 

# Citation
This repository is the code for our paper [Malaria Screener: a smartphone application for automated malaria screening](https://bmcinfectdis.biomedcentral.com/articles/10.1186/s12879-020-05453-1#citeas). If you find this work useful in your research, please cite:
```
@article{yu2020malaria,
  title={Malaria Screener: a smartphone application for automated malaria screening},
  author={Yu, Hang and Yang, Feng and Rajaraman, Sivaramakrishnan and Ersoy, Ilker and Moallem, Golnaz and Poostchi, Mahdieh and Palaniappan, Kannappan and Antani, Sameer and Maude, Richard J and Jaeger, Stefan},
  journal={BMC Infectious Diseases},
  volume={20},
  number={1},
  pages={1--8},
  year={2020},
  publisher={BioMed Central}
}
```



