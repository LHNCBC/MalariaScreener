package gov.nih.nlm.malaria_screener.uploadFunction;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxSession;

import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsCustom;
import gov.nih.nlm.malaria_screener.frontEnd.Uploader;

public class UploadSessionManager implements BoxAuthentication.AuthListener{

    private static final String TAG = "MyDebug";

    private static BoxSession mSession = null;

    BoxSession mOldSession = null;

//    private BoxApiFolder mFolderApi;
//    private BoxApiFile mFileApi;

    public Context context;

    private ArrayList<String> imageNameList = new ArrayList<>();
    private ArrayList<String> folderNameList = new ArrayList<>();

    public void authenticate(Context context, ArrayList<String> imageNameList, ArrayList<String> folderNameList){

        this.context = context;
        this.imageNameList = imageNameList;
        this.folderNameList = folderNameList;

        BoxConfig.IS_LOG_ENABLED = true;
        configureClient();
        initSession();

    }

    /**
     * Set required config parameters. Use values from your application settings in the box developer console.
     */
    private void configureClient() {
        BoxConfig.CLIENT_ID = "ab45urhtlfkbg5tyvswkmeg385g97488";
        BoxConfig.CLIENT_SECRET = "ydVkYwFsXS27ofYQ9HZ2srLK3sFEM2h0";

        // needs to match redirect uri in developer settings if set.
        //   BoxConfig.REDIRECT_URL = "<YOUR_REDIRECT_URI>";
    }

    private void initSession() {

        mSession = new BoxSession(context);
        mSession.setSessionAuthListener(this);
        mSession.authenticate(context);

    }

    @Override
    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {

    }

    @Override
    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
        //Init file, and folder apis; and use them to fetch the root folder
//        mFolderApi = new BoxApiFolder(mSession);
////        mFileApi = new BoxApiFile(mSession);
        Log.d(TAG, "onAuthCreated 1");

        new Thread() {
            @Override
            public void run() {
                ListOfImagesUploader listOfImagesUploader = new ListOfImagesUploader(context, mSession);
                listOfImagesUploader.upload_images(imageNameList, folderNameList);
            }

        }.start();

    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }

}
