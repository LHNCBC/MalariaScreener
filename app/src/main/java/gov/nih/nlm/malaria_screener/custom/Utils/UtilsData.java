package gov.nih.nlm.malaria_screener.custom.Utils;

import java.util.ArrayList;

// this class includes data used during image processing stage. It is to avoid data transfer between CameraActivity & ResultDisplayer & SummarySheet
public final class UtilsData {

    // for both
    public static ArrayList<String> imageNames = new ArrayList<>();

    // for thin smear


    //for thick smear
    public static int parasiteCurrent = 0;
    public static int WBCCurrent = 0;
    public static int parasiteTotal = 0;
    public static int WBCTotal = 0;

    public static ArrayList<String> parasiteCountList = new ArrayList<>();
    public static ArrayList<String> WBCCountList = new ArrayList<>();

    public static ArrayList<String> parasiteCountList_GT = new ArrayList<>();
    public static ArrayList<String> WBCCountList_GT = new ArrayList<>();

    //resets--------------------------------------------------------
    public static final void resetImageNames(){
        imageNames.clear();
    }

    public static final void resetCurrentCounts(){
        parasiteCurrent = 0;
        WBCCurrent = 0;
    }

    public static final void resetTotalCounts(){
        parasiteTotal = 0;
        WBCTotal = 0;
    }

    public static final void resetCountLists(){
        parasiteCountList.clear();
        WBCCountList.clear();

    }

    public static final void resetCountLists_GT(){
        parasiteCountList_GT.clear();
        WBCCountList_GT.clear();

    }

    //adds-----------------------------------------------------------
    public static final void addImageName(String name){

        imageNames.add(name);
    }

    public static final void addParasiteCount(String count){

        parasiteCountList.add(count);
    }

    public static final void addWBCCount(String count){

        WBCCountList.add(count);
    }

    public static final void addParasiteCount_GT(String count){

        parasiteCountList_GT.add(count);
    }

    public static final void addWBCCount_GT(String count){

        WBCCountList_GT.add(count);
    }

    //removes---------------------------------------------------------
    public static final void removeImageName(){

        imageNames.remove(imageNames.size()-1);
    }

    public static final void removeParasiteCount(){

        parasiteCountList.remove(parasiteCountList.size()-1);
    }

    public static final void removeWBCCount(){

        WBCCountList.remove(WBCCountList.size()-1);
    }






}
