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
1. Follow the guide for installing [Android Studio](https://developer.android.com/studio/install). This is the official IDE for Google's Android operating system.

## Building/Running the Android App

### SDK Setup
1. In Visual Studio launch the Android SDK Manager:
    * On Windows: `Tools > Android > Android SDK Manager`
    * On a Mac: `Tools > SDK Manager`
1. Follow the guide for using the [Visual Studio Android SDK Manager](https://docs.microsoft.com/en-us/xamarin/android/get-started/installation/android-sdk) to make sure the following SDK components are installed. Older/Newer versions of the components may work as well, but have not necessarily been tested:
    * Platforms:
      * Android 8.1 - Oreo (API Level 27)
        * Android SDK Platform 27
          * This version (27) is required to build the project in its current configuration
          * Other platform versions may work; however, the flags in AndroidManifest.xml and the csproj must be updated as well. See the Android and Xamarin documentation for details.
        * Sources for Android 27 (optional)
    * Tools
      * Android SDK Tools
        * Android SDK Tools 26.1.1+
      * Android SDK Platform-Tools 27.0.1+
      * Android SDK Build-Tools 27.0.3+
      * Android Emulator 27.1.12+ (only required if you plan on using an emulator)

### Running on an emulator
