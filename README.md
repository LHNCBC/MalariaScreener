# Malaria Screener
Official repository for Malaria Screener

# Contents

1. [Introduction](#introduction)
1. [Device Compatibility](#device-compatibility)
1. [Features](#features)
1. [Getting Started with Development](#getting-started-with-development)

# Introduction

# Device Compatibility

|          |                             |
| -------- | --------------------------- |
| Android  | API Level 21+ (KitKat 5.0+) |


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
This project uses OpenCV native development. It has been setup in the project files for the most part. However, you need to download [OpenCV Android 3.4.2](https://opencv.org/releases/) and [CMake 3.4.1+](https://cmake.org/). Again, older/Newer versions of the components may work as well, but have not necessarily been tested.

You also need to point following path in the project file "CMakeLists.txt" to the OpenCV Android library that you downloaded yourself:
   ```
   include_directories(/#your path here#/OpenCV-android-sdk/sdk/native/jni/include)
   ```
      
### Running on a physical device
1. Follow the instructions in the Android Developer Docs to [Enable Developer Options and USB Debugging](https://developer.android.com/studio/debug/dev-options.html)
1. Connect your Android tablet to your computer
1. Tap "OK" on the tablet when prompted to "Allow USB debugging" (Check the box to always allow if you want to make future development easier)
1. Run the app
