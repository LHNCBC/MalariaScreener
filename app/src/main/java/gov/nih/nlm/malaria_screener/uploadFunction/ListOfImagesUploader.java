package gov.nih.nlm.malaria_screener.uploadFunction;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConstants;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.listeners.ProgressListener;
import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.database.ProgressBarEvent;
import gov.nih.nlm.malaria_screener.database.ProgressDoneEvent;
import gov.nih.nlm.malaria_screener.database.UpdateListViewEvent;

/*  <<Class Description>>
    This class is to provide function that:
    1. from the input para, extract the list of image IDs.
    2. for each image ID, find all image files associated from the corresponding slide folder, and
        upload each of them using a worker Thread.
    (for every file, only if it does not exist in user's remote Box repository, then upload that file.)
    3. upload the updated versions of all metadata files (.txt and .csv).

* 03/31/2020 by Hang Yu
*/
public class ListOfImagesUploader {

    private static final String TAG = "MyDebug";

    BoxSession mSession;
    String RootFolderName_str = "NLM_Malaria_Screener";
    String destinationRootFolderID = null;
    String response_error_msg_str = "item_name_in_use"; // this is the default Box error msg when the folder already exist
    Context context;
    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;

    public ListOfImagesUploader(Context context, BoxSession boxSession){

        this.context = context;

        mSession = boxSession;

        //Init file, and folder apis; and use them to fetch the root folder
        mFolderApi = new BoxApiFolder(mSession);
        mFileApi = new BoxApiFile(mSession);

    }

    // execute 2 and 3, which is listed above in the Class Description.
    public Void upload_images(ArrayList<String>... arrayLists) {

        Log.d(TAG, "start to upload");

        // create NLM_Malaria_Screener folder under user's root
        create_root_folder();

        // get the id of "NLM_Malaria_Screener" folder id on Box
        destinationRootFolderID = check_folder_id(BoxConstants.ROOT_FOLDER_ID, RootFolderName_str);

        ArrayList<String> imgName_arrayList = arrayLists[0];
        ArrayList<String> folderName_arrayList = arrayLists[1];

        String currentFolderStr = "";

        // ------- 1. iterate all image ID to be uploaded in the provided list ---------------------
        if(imgName_arrayList.size()  == folderName_arrayList.size()) {

            for (int i = 0; i < imgName_arrayList.size(); i++) {

                final String folderNameStr = folderName_arrayList.get(i);
                final String imgNameStr = imgName_arrayList.get(i);

                Log.d(TAG, "imgNameStr: " + imgNameStr);
                Log.d(TAG, "folderNameStr: " + folderNameStr);

                // --------- 2. check if folder exist on local, if not, no need to proceed ---------
                final File folderFile = new File(Environment.getExternalStorageDirectory(
                ), RootFolderName_str + "/" + folderNameStr);

                File[] imageListing = folderFile.listFiles(); // list all images of one session

                if (imageListing == null){

                } else {

                    // 3. check if folder for current image ID need to be created on remote Box repo.
                    // (When it's the first folder to be uploaded OR a new folder starts to be uploaded,
                    // create this folder in remote Box repository.)
                    if (currentFolderStr.equals("") || !currentFolderStr.equals(folderNameStr)) {

                        // change currentFolderStr to new folder name
                        currentFolderStr = folderNameStr;

                        //create a folder in remote Box repo
                        BoxRequestsFolder.CreateFolder requestsFolder = mFolderApi.getCreateRequest(destinationRootFolderID, folderNameStr);
                        try {
                            requestsFolder.send();
                        } catch (BoxException e) {
                            e.printStackTrace();
                        }

                    }

                    // get the id of the created slide folder
                    final String cur_folder_id = check_folder_id(destinationRootFolderID, folderNameStr);

                    // ------ 4. search for all images under current ID in the local folder --------
                    int endIndex = imgNameStr.lastIndexOf(".");
                    final String imageNameOnly = imgNameStr.substring(0, endIndex);

                    for (final File imgFile: imageListing) {

                        if (imgFile.toString().contains(imageNameOnly)){

                            // -------- 5. create new thread and upload each image file ------------
                            new Thread() {
                                @Override
                                public void run() {

                                    // get image file path
                                    //String imagePathStr = RootFolderName_str + "/" + folderNameStr + "/" + imgNameStr;
                                    //File imgFile = new File(Environment.getExternalStorageDirectory(), imagePathStr);

                                    BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(imgFile, cur_folder_id).setProgressListener(new ProgressListener() {
                                        @Override
                                        public void onProgressChanged(long numBytes, long totalBytes) {

                                            if (numBytes == totalBytes) {

                                                // -------------------- 6. delete image ID from HashMap ------------------------
                                                Map<String, String> map = UploadHashManager.hashmap_for_upload;

                                                if (map.containsKey(imgNameStr)) {
                                                    map.remove(imgNameStr);
                                                }
                                                UploadHashManager.saveMap(context, UploadHashManager.hashmap_for_upload);

                                                // update ListView for Upload Activity's UI
                                                EventBus.getDefault().post(new UpdateListViewEvent(folderNameStr));

                                                // **** update upload progress to floating Service widget *****
                                                EventBus.getDefault().post(new ProgressBarEvent(1));
                                            }

                                        }
                                    });

                                    try {
                                        request.send();
                                    } catch (BoxException e) {
                                        e.printStackTrace();

                                    }

                                }

                            }.start();
                        }
                    }


                }

            }



            // ------------ 7. Upload .txt and database (.csv) files in root folder ----------------
            final File rootFile = new File(Environment.getExternalStorageDirectory(
            ), RootFolderName_str);

            if (rootFile.listFiles() != null) {

                final File[] listing = rootFile.listFiles();    // list all files & folders
                final int length = listing.length;

                if (length > 0) {

                    // iterate through folders & files
                    for (final File file : listing) {

                        String filePathStr = file.toString();

                        if (filePathStr.contains(".txt") || filePathStr.contains(".csv")) {

                            new Thread() {
                                @Override
                                public void run() {
                                    upload_metadata_file(file);

                                    EventBus.getDefault().post(new ProgressBarEvent(1));
                                }

                            }.start();


                        }
                    }

                }
            }

            // **** update upload progress to floating Service widget *****
            //EventBus.getDefault().post(new ProgressDoneEvent(true));

        }

        return null;
    }

    /* Upload files such as log file / database file / user_info.
       1. Check if file already exists:
            yes: update existing file
            no: upload new file
    */
    private void upload_metadata_file(File file){

        BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(file, destinationRootFolderID);

        try {
            request.send();
        } catch (BoxException e) {
            e.printStackTrace();

            BoxError error = e.getAsBoxError();

            if (error != null && error.getStatus() == 409 && error.getCode().equals(response_error_msg_str)){
                ArrayList<BoxEntity> conflicts = error.getContextInfo().getConflicts();
                if (conflicts != null && conflicts.size() == 1 && conflicts.get(0) instanceof BoxFile) {
                    uploadNewVersion(file, (BoxFile) conflicts.get(0));

                }
            }
        }
    }

    private void uploadNewVersion(File localFile, final BoxFile file){

        BoxRequestsFile.UploadNewVersion request = mFileApi.getUploadNewVersionRequest(localFile, file.getId());
        try {
            request.send();
        } catch (BoxException e) {
            e.printStackTrace();
        }

    }

    // this function creates a root folder (NLM_Malaria_Screener) on user's Box repository
    private void create_root_folder(){

        BoxRequestsFolder.CreateFolder requestsFolder = mFolderApi.getCreateRequest(BoxConstants.ROOT_FOLDER_ID, RootFolderName_str);

        try {
            requestsFolder.send();

        } catch (BoxException e) {
            e.printStackTrace();
        }


    }

    // check folder's ID in remote Box repository. (Folder ID can be viewed through Box web application. It is at the end of the URL.)
    private String check_folder_id(String parentID, String folderName){

        String folder_box_id = null;

        try {
            BoxIteratorItems boxIteratorItems = mFolderApi.getFolderWithAllItems(parentID).send().getItemCollection();
            for (BoxItem boxItem: boxIteratorItems) {
                if (boxItem.getName().equals(folderName)){

                    folder_box_id = boxItem.getId();
                }

            }

        } catch (BoxException e) {
            e.printStackTrace();
        }

        return folder_box_id;
    }

}
