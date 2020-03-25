package gov.nih.nlm.malaria_screener.uploadFunction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxSession;

import gov.nih.nlm.malaria_screener.R;

public class UploadActivity extends AppCompatActivity implements BoxAuthentication.AuthListener {

    public static BoxSession mSession = null;
    BoxSession mOldSession = null;

    private BoxApiFolder mFolderApi;
    private BoxApiFile mFileApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_upload);
        toolbar.setTitle(R.string.title_upload);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        BoxConfig.IS_LOG_ENABLED = true;
        configureClient();
        initSession();

        Button uploadButton = (Button) findViewById(R.id.button_upload_box);
        uploadButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {
                        new AllFile_Uploader_Box(getApplicationContext()).execute();
                    }
                }
        );

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

        mSession = new BoxSession(this);
        mSession.setSessionAuthListener(this);
        mSession.authenticate(this);

    }

    @Override
    public void onRefreshed(BoxAuthentication.BoxAuthenticationInfo info) {

    }

    @Override
    public void onAuthCreated(BoxAuthentication.BoxAuthenticationInfo info) {
        //Init file, and folder apis; and use them to fetch the root folder
        mFolderApi = new BoxApiFolder(mSession);
        mFileApi = new BoxApiFile(mSession);
    }

    @Override
    public void onAuthFailure(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }

    @Override
    public void onLoggedOut(BoxAuthentication.BoxAuthenticationInfo info, Exception ex) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
