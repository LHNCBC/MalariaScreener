/* Copyright 2020 The Malaria Screener Authors. All Rights Reserved.

This software was developed under contract funded by the National Library of Medicine,
which is part of the National Institutes of Health, an agency of the Department of Health and Human
Services, United States Government.

Licensed under GNU General Public License v3.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.gnu.org/licenses/gpl-3.0.html

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package gov.nih.nlm.malaria_screener.uploadFunction;


import android.content.Context;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxSession;

import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;

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

        boolean isWifiConnected = check_Wifi_connection();

        if (isWifiConnected) {
            new Thread() {
                @Override
                public void run() {

                    Intent intent = new Intent(context, BoxUploadService.class);
                    intent.putStringArrayListExtra("img_name_array", imageNameList);
                    intent.putStringArrayListExtra("folder_name_array", folderNameList);
                    context.startService(intent);

                    ListOfImagesUploader listOfImagesUploader = new ListOfImagesUploader(context, mSession);
                    listOfImagesUploader.upload_images(imageNameList, folderNameList);
                }

            }.start();

        } else {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    String msg = context.getResources().getString(R.string.no_wifi);;
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });

        }

    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }


    private boolean check_Wifi_connection(){

        ConnectivityManager connMgr =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isWifiConn = false;
        //boolean isMobileConn = false;

        for (Network network : connMgr.getAllNetworks()) {
            NetworkInfo networkInfo = connMgr.getNetworkInfo(network);
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                isWifiConn |= networkInfo.isConnected();
            }
            /*if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                isMobileConn |= networkInfo.isConnected();
            }*/
        }

        return isWifiConn;
    }

}
