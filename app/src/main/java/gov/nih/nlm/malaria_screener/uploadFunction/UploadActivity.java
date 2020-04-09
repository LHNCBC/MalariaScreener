package gov.nih.nlm.malaria_screener.uploadFunction;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.box.androidsdk.content.BoxApiFile;
import com.box.androidsdk.content.BoxApiFolder;
import com.box.androidsdk.content.BoxConfig;
import com.box.androidsdk.content.BoxException;
import com.box.androidsdk.content.auth.BoxAuthentication;
import com.box.androidsdk.content.models.BoxSession;
import com.box.androidsdk.content.requests.BoxRequestsFile;

import java.util.ArrayList;
import java.util.Map;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsMethods;

public class UploadActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    public static BoxSession mSession = null;

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

        Button uploadButton = (Button) findViewById(R.id.button_upload_box);
        uploadButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        if (!UploadHashManager.hashmap_for_upload.isEmpty()) {

                            //export database for upload
                            UtilsMethods.exportDB(getApplicationContext());

                            ArrayList[] entries = returnImgFromMap_all();

                            ArrayList<String> imageNameList = entries[0];
                            ArrayList<String> folderNameList = entries[1];

                            UploadSessionManager uploadSessionManager = new UploadSessionManager();
                            uploadSessionManager.authenticate(getApplicationContext(), imageNameList, folderNameList);


                        } else {
                            String string = getApplicationContext().getResources().getString(R.string.empty_upload);
                            Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                        }

                    }
                }
        );

    }

    private ArrayList[] returnImgFromMap_all(){

        ArrayList[] enrties = new ArrayList[2];

        ArrayList<String > imageNameList = new ArrayList<>();
        ArrayList<String> folderNameList = new ArrayList<>();

        for(Map.Entry<String, String> entry: UploadHashManager.hashmap_for_upload.entrySet()){
            imageNameList.add(entry.getKey());
            folderNameList.add(entry.getValue());
        }

        enrties[0] = imageNameList;
        enrties[1] = folderNameList;

        return enrties;
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
