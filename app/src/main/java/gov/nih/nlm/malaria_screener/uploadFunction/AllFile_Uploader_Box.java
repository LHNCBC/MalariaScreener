package gov.nih.nlm.malaria_screener.uploadFunction;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;

import com.box.androidsdk.content.BoxConstants;
import com.box.androidsdk.content.BoxException;

import com.box.androidsdk.content.models.BoxEntity;
import com.box.androidsdk.content.models.BoxError;
import com.box.androidsdk.content.models.BoxFile;
import com.box.androidsdk.content.models.BoxItem;
import com.box.androidsdk.content.models.BoxIteratorItems;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;
import com.box.androidsdk.content.requests.BoxRequestsFolder;

import java.io.File;
import java.util.ArrayList;


/* This class is to provide function that:
    1. create root folder in user's remote Box repository
    2. scans all files within each subfolder under the local "NLM_Malaria_screener" folder
    3. for every file, only if it does not exist in user's remote Box repository, then upload that file.

* 03/23/2020 by Hang Yu
*/

public class AllFile_Uploader_Box extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "MyDebug";

    BoxSession mSession;
    String MS_parent_folderName_str = "NLM_Malaria_Screener";
    String MS_destinationFolderId = null;
    String response_error_msg_str = "item_name_in_use"; // this is the default Box error msg when the folder already exist
    Context context;
    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;



    public AllFile_Uploader_Box(Context context){

        this.context = context;
        //this.path = path;

        mSession = UploadActivity.mSession;

        //Init file, and folder apis; and use them to fetch the root folder
        mFolderApi = new BoxApiFolder(mSession);
        mFileApi = new BoxApiFile(mSession);

    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        // create NLM_Malaria_Screener folder under user's root
        create_root_folder();

        long startTimeNN = System.currentTimeMillis();

        // iterate through all files & upload
        scan_all_files();

        long endTime_NN = System.currentTimeMillis();
        long totalTime_NN = endTime_NN - startTimeNN;
        Log.d(TAG, "Box upload Time: " + totalTime_NN);

        return null;
    }

    // this function creates a root folder (NLM_Malaria_Screener) on user's Box repository
    private void create_root_folder(){

        BoxRequestsFolder.CreateFolder requestsFolder = mFolderApi.getCreateRequest(BoxConstants.ROOT_FOLDER_ID, MS_parent_folderName_str);

        try {
            requestsFolder.send();
        } catch (BoxException e) {
            e.printStackTrace();
        }


    }

    private void scan_all_files(){

        // get the id of "NLM_Malaria_Screener" folder id on Box
        MS_destinationFolderId = check_folder_id(BoxConstants.ROOT_FOLDER_ID, MS_parent_folderName_str);

        final File file = new File(Environment.getExternalStorageDirectory(
        ), MS_parent_folderName_str);

        if (file.listFiles() != null) {

            final File[] folderListing = file.listFiles();    // list all folders
            final int length = file.listFiles().length;

            if (length > 0) {

                // iterate through each folder
                for (File folderFile : folderListing) {

                    String folderNameStr = folderFile.toString().substring(folderFile.toString().lastIndexOf("/") + 1);

                    Log.d(TAG, "folderStr: " + folderNameStr);

                    // -- if the item is a NOT a folder, Upload txt files/csv (database) file --------------
                    if (folderNameStr.contains(".txt") || folderNameStr.contains(".csv")) {

                        upload_files(folderFile);

                        continue;
                    }

                    // -------------------------------------------------------------------------

                    // if the item is a folder, then create a folder in remote Box repo
                    BoxRequestsFolder.CreateFolder requestsFolder = mFolderApi.getCreateRequest(MS_destinationFolderId, folderNameStr);
                    try {
                        requestsFolder.send();
                    } catch (BoxException e) {
                        e.printStackTrace();
                    }

                    // get the id of the created folder
                    final String cur_folder_id = check_folder_id(MS_destinationFolderId, folderNameStr);

                    File[] imageListing = folderFile.listFiles(); // list all images of one session

                    if (imageListing != null) {

                        // iterate through each image
                        for (final File imgFile: imageListing){

                            Log.d(TAG, "imgFile: " + imgFile);

                            new Thread() {
                                @Override
                                public void run() {

                                    BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(imgFile, cur_folder_id);

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
        }


    }

    /* Upload files such as log file / database file / user_info.
       1. Check if file already exists:
            yes: update existing file
            no: upload new file
    */
    private void upload_files(File file){

        BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(file, MS_destinationFolderId);

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
