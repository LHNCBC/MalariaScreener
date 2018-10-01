package gov.nih.nlm.malaria_screener.database;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.SearchErrorException;
import com.dropbox.core.v2.files.SearchResult;
import com.dropbox.core.v2.files.WriteMode;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;

/**
 * Created by yuh5 on 3/10/2017.
 */

public class Upload extends AsyncTask<Void, Integer, Boolean> {

    private static final String TAG = "MyDebug";

    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String DROPBOX_PROGRESS = "progress";
    private final static String DROPBOX_UPDATE_STATE = "app_state"; //0 initial, 1 success, 2 fail, 3 uploading

    private DbxClientV2 dbxClientV2;
    private String path;
    private Context context;

    MyDBHandler dbHandler;

    public Upload(Context context, DbxClientV2 dbxClientV2, String path) {

        this.context = context;
        this.dbxClientV2 = dbxClientV2;
        this.path = path;

        dbHandler = new MyDBHandler(context, null, null, 1);

    }

    @Override
    protected void onPreExecute() {
        String string = context.getResources().getString(R.string.start_upload);
        Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener");

        boolean isUpload = false;

        try {

            if (file.listFiles() != null) {

                final File[] folder_list = file.listFiles(); // list of folders from each session
                final int length = file.listFiles().length;

                if (length > 0) {

                    isUpload = true;

                    // upload files
                    int count = 0; // counter for update upload progress
                    for (int index = 0; index < folder_list.length; index++) {

                        File[] imageListing = folder_list[index].listFiles();

                        String dirStr = folder_list[index].toString().substring(folder_list[index].toString().lastIndexOf("/") + 1);

                        if (imageListing != null) {

                            if (dirStr.equals("Test")) { // if it's Test folder

                                for (int i = 0; i < imageListing.length; i++) { // for Test folder "imageListing" list all slides/sessions

                                    String folderStr = imageListing[i].toString().substring(imageListing[i].toString().lastIndexOf("/") + 1); // name of the slide folder in Test

                                    Boolean notfound = false;
                                    try {
                                        Metadata metadata = dbxClientV2.files().getMetadata(path + "/" + dirStr + "/" + folderStr);
                                    } catch (GetMetadataErrorException e){
                                        notfound = e.errorValue.getPathValue().isNotFound();

                                    }

                                    //SearchResult searchResult_folder = dbxClientV2.files().search(path + "/" + dirStr, folderStr);

                                    if (notfound) { // if session folder doesn't exist, then directly upload all files

                                        File[] imageListingHere = imageListing[i].listFiles();

                                        for (int j = 0; j < imageListingHere.length; j++) { // list all images in a test session

                                            count++;

                                            String imgStr = imageListingHere[j].toString().substring(imageListingHere[j].toString().lastIndexOf("/") + 1);

                                            FileInputStream fileInputStream = new FileInputStream(imageListingHere[j]);

                                            dbxClientV2.files().uploadBuilder(path + "/" + dirStr + "/" + folderStr + "/" + imgStr).withMode(WriteMode.OVERWRITE).uploadAndFinish(fileInputStream);

                                            publishProgress(count + 1);

                                        }

                                    } else { // if session folder exists, search for file first, if doesn't exist then upload files

                                        File[] imageListingHere = imageListing[i].listFiles();

                                        for (int j = 0; j < imageListingHere.length; j++) { // list all images in a test session

                                            count++;

                                            String imgStr = imageListingHere[j].toString().substring(imageListingHere[j].toString().lastIndexOf("/") + 1);

                                            SearchResult searchResult = dbxClientV2.files().search(path + "/" + dirStr + "/" + folderStr, imgStr);

                                            if (searchResult.toString().isEmpty()) { // if image file doesn't exist, then upload

                                                FileInputStream fileInputStream = new FileInputStream(imageListingHere[j]);

                                                dbxClientV2.files().uploadBuilder(path + "/" + dirStr + "/" + folderStr + "/" + imgStr).withMode(WriteMode.OVERWRITE).uploadAndFinish(fileInputStream);

                                            }

                                            publishProgress(count + 1);

                                        }
                                    }


                                }

                            } else { // normal slide folder upload

                                Boolean notfound = false;
                                try {
                                    Metadata metadata = dbxClientV2.files().getMetadata(path + "/" + dirStr);
                                } catch (GetMetadataErrorException e){
                                    notfound = e.errorValue.getPathValue().isNotFound();

                                }

                                if (notfound) { // if session folder doesn't exist, then directly upload all files

                                    for (int i = 0; i < imageListing.length; i++) {

                                        count++;

                                        String imgStr = imageListing[i].toString().substring(imageListing[i].toString().lastIndexOf("/") + 1);

                                        FileInputStream fileInputStream = new FileInputStream(imageListing[i]);

                                        dbxClientV2.files().uploadBuilder(path + "/" + dirStr + "/" + imgStr).withMode(WriteMode.OVERWRITE).uploadAndFinish(fileInputStream);

                                        publishProgress(count + 1);

                                    }

                                } else { // if session folder exists, search for file first, if doesn't exist then upload files

                                    for (int i = 0; i < imageListing.length; i++) {

                                        count++;

                                        String imgStr = imageListing[i].toString().substring(imageListing[i].toString().lastIndexOf("/") + 1);

                                        SearchResult searchResult = dbxClientV2.files().search(path + "/" + dirStr, imgStr);

                                        if (searchResult.getMatches().isEmpty()) { // if image file doesn't exist, then upload

                                            FileInputStream fileInputStream = new FileInputStream(imageListing[i]);

                                            dbxClientV2.files().uploadBuilder(path + "/" + dirStr + "/" + imgStr).withMode(WriteMode.OVERWRITE).uploadAndFinish(fileInputStream);

                                        }

                                        publishProgress(count + 1);

                                    }
                                }

                                if (folder_list[index].getName().lastIndexOf("_") != -1) { // if folder is not "Extra" or "Test", then mark their upload flag
                                    markAsUploaded(folder_list[index]);
                                }

                            }

                        } else {

                            count++;

                            String imgStr = folder_list[index].toString().substring(folder_list[index].toString().lastIndexOf("/") + 1);

                            FileInputStream fileInputStream = new FileInputStream(folder_list[index]);
                            dbxClientV2.files().uploadBuilder(path + "/" + imgStr).withMode(WriteMode.OVERWRITE).uploadAndFinish(fileInputStream);

                            publishProgress(count + 1);
                        }

                    }

                }

            }

        } catch (IOException ioe) {

        } catch (SearchErrorException e) {
            e.printStackTrace();
        } catch (DbxException e) {
            e.printStackTrace();
        }

        return isUpload;

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);

        EventBus.getDefault().post(new ProgressBarEvent(values[0]));

        SharedPreferences sharedPreferences = context.getSharedPreferences(DROPBOX_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(DROPBOX_PROGRESS, values[0]);
        editor.commit();

    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);

        if (aBoolean) {
            String string = context.getResources().getString(R.string.upload_finished);
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
        } else {
            String string = context.getResources().getString(R.string.image_empty);
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
        }

        EventBus.getDefault().post(new ProgressDoneEvent(true));

        SharedPreferences sharedPreferences = context.getSharedPreferences(DROPBOX_NAME, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(DROPBOX_PROGRESS, 0);
        editor.putInt(DROPBOX_UPDATE_STATE, 1);
        editor.commit();

    }

    private void markAsUploaded(File file) {

        String FolderName = file.getName();

        String PatientIDStr = FolderName.substring(0, FolderName.lastIndexOf("_"));
        String SlideIDStr = FolderName.substring(FolderName.lastIndexOf("_") + 1);
        dbHandler.updateUploadedFlag(PatientIDStr, SlideIDStr);

    }

}


