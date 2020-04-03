package gov.nih.nlm.malaria_screener.uploadFunction;

import android.content.Context;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxSession;

public class UploadSessionManager implements BoxAuthentication.AuthListener{

    public static BoxSession mSession = null;
    BoxSession mOldSession = null;

//    private BoxApiFolder mFolderApi;
//    private BoxApiFile mFileApi;

    public Context context;

    public void authticateSession(Context context){

        this.context = context;

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
    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }

}
