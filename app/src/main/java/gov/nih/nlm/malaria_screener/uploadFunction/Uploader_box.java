package gov.nih.nlm.malaria_screener.uploadFunction;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;

import com.box.androidsdk.content.BoxException;

import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;


import java.io.File;


public class Uploader_box extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "MyDebug";

    BoxSession mSession = null;

    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;

    Context context;
    private String path;

    public Uploader_box(Context context, String path){

        this.context = context;
        this.path = path;

        mSession = UploadActivity.mSession;

        //Init file, and folder apis; and use them to fetch the root folder
        mFolderApi = new BoxApiFolder(mSession);
        mFileApi = new BoxApiFile(mSession);

    }

    @Override
    protected Boolean doInBackground(Void... voids) {

        upload_files();

        return null;
    }

    private void upload_files(){

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener");

        try {

            if (file.listFiles() != null) {

                final File[] folderListing = file.listFiles(); // list of folders from each session
                final int length = file.listFiles().length;

                if (length > 0) {
                    for (File folder : folderListing) {

                        File[] imageListing = folder.listFiles();

                        String folderStr = folder.toString().substring(folder.toString().lastIndexOf("/") + 1);

                        if (imageListing != null) {

                            String destinationFolderId = "0";
                            for (File img: imageListing){

                                String imgStr = img.toString().substring(img.toString().lastIndexOf("/") + 1);

                                File image_file = new File(file, folderStr + "/" + imgStr);

                                Log.d(TAG, "image_file: " + image_file);

                                BoxRequestsFile.UploadFile request = mFileApi.getUploadRequest(image_file, destinationFolderId);
                                request.send();
                            }
                        }
                    }
                }
            }

        } catch (BoxException e) {
            e.printStackTrace();
        }

    }

}
