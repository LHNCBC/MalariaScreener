package gov.nih.nlm.malaria_screener.uploadFunction;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.box.androidsdk.content.models.BoxSession;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.Utils.UtilsMethods;
import gov.nih.nlm.malaria_screener.database.UpdateListViewEvent;
import gov.nih.nlm.malaria_screener.uploadFunction.custom.CustomAdapter_Upload;
import gov.nih.nlm.malaria_screener.uploadFunction.custom.RowItem_Folders;

public class UploadActivity extends AppCompatActivity implements CustomAdapter_Upload.OnSelectedListener{

    private static final String TAG = "MyDebug";

    String RootFolderName_str = "NLM_Malaria_Screener";

    public static BoxSession mSession = null;

    Toolbar toolbar;
    Button uploadButton;
    ListView listView;
    CustomAdapter_Upload customAdapter_upload;

    boolean isSelectionMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        toolbar = (Toolbar) findViewById(R.id.navigate_bar_upload);
        toolbar.setTitle(R.string.title_upload);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        listView = (ListView) findViewById(R.id.listview_upload);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener(){
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        Log.d(TAG, "long clicked: position: " + position + ", id: " + id);

                        on_longClick_select(position);

                        return true;
                    }

        }
        );

        inflate_ListView();

        uploadButton = (Button) findViewById(R.id.button_upload_box);
        uploadButton.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View v) {

                        if (!isSelectionMode) {

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

                        } else {

                            if (customAdapter_upload.getSelect_num()==0){
                                String string = getApplicationContext().getResources().getString(R.string.no_selected_item);
                                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();

                            } else {
                                ArrayList[] entries = get_user_selected_images();

                                ArrayList<String> imageNameList = entries[0];
                                ArrayList<String> folderNameList = entries[1];

                                //export database for upload
                                UtilsMethods.exportDB(getApplicationContext());

                                UploadSessionManager uploadSessionManager = new UploadSessionManager();
                                uploadSessionManager.authenticate(getApplicationContext(), imageNameList, folderNameList);
                            }
                        }


                    }
                }
        );

    }

    private void inflate_ListView(){

        ArrayList<RowItem_Folders> rowItems_allfolders = getAllFolderNames();

        customAdapter_upload = new CustomAdapter_Upload(getApplicationContext(), R.layout.list_item_upload, rowItems_allfolders, this);
        listView.setAdapter(customAdapter_upload);

    }

    private ArrayList<RowItem_Folders> getAllFolderNames(){

        final File rootFile = new File(Environment.getExternalStorageDirectory(
        ), RootFolderName_str);

        ArrayList<RowItem_Folders> rowItems_allfolders = new ArrayList<RowItem_Folders>();

        if (rootFile.listFiles() != null) {

            final File[] listing = rootFile.listFiles();    // list all files & folders
            final int length = listing.length;

            if (length > 0) {

                if (listing.length > 1) {

                    Arrays.sort(listing, new Comparator<File>() {
                        @Override
                        public int compare(File f1, File f2) {
                            return Long.compare(f2.lastModified(), f1.lastModified());
                        }
                    });
                }

                // iterate through folders & files
                for (final File file : listing) {

                    String filePathStr = file.toString();

                    if (!filePathStr.contains(".txt") && !filePathStr.contains(".csv")) {

                        // folder name
                        int startIndex = filePathStr.lastIndexOf("/");
                        String folderNameOnly = filePathStr.substring(startIndex+1);

                        // date
                        Date lastModDate = new Date(file.lastModified());
                        String dateStr = DateFormat.getDateInstance().format(lastModDate);

                        // isUploaded
                        boolean isInMap = UploadHashManager.hashmap_for_upload.containsValue(folderNameOnly);

                        //Log.d(TAG, "folderNameOnly: " + folderNameOnly + ", isInMap: " + isInMap);

                        RowItem_Folders rowItem_folders = new RowItem_Folders(folderNameOnly, dateStr
                                , false, !isInMap);
                        rowItems_allfolders.add(rowItem_folders);

                    }
                }
            }
        }

        return  rowItems_allfolders;
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

    private ArrayList[] get_user_selected_images(){

        ArrayList[] enrties = new ArrayList[2];

        ArrayList<String > imageNameList = new ArrayList<>();
        ArrayList<String> folderNameList = new ArrayList<>();

        ArrayList<RowItem_Folders> rowItems_allfolders = customAdapter_upload.getRowItem_foldersArrayList();

        for (RowItem_Folders rowItem_folders: rowItems_allfolders) {

            if (rowItem_folders.getSelected()) {

                String folderNameStr = rowItem_folders.getItem();

                // ---------------- search all images in the folder -------------------
                final File folderFile = new File(Environment.getExternalStorageDirectory(
                ), RootFolderName_str + "/" + folderNameStr);

                File[] imageListing = folderFile.listFiles(); // list all images of one session

                if (imageListing == null){

                } else {

                    for (final File imgFile: imageListing) {

                        int startIndex = imgFile.toString().lastIndexOf("/");
                        final String imageNameStr = imgFile.toString().substring(startIndex+1);

                        if (!imageNameStr.contains("result") && !imageNameStr.contains("mask")) {
                            imageNameList.add(imageNameStr);
                            folderNameList.add(folderNameStr);

                            Log.d(TAG, "imageNameStr: " + imageNameStr);
                            Log.d(TAG, "folderNameStr: " + folderNameStr);
                        }
                    }

                }

            }

        }

        enrties[0] = imageNameList;
        enrties[1] = folderNameList;

        return enrties;
    }

    @Override
    public void onSelectionNumChanged(int numOfSelectedItems) {

        toolbar.setTitle(numOfSelectedItems + " selected");

    }

    private void on_longClick_select(int position){

        isSelectionMode = true;
        uploadButton.setText(R.string.upload_selected_items);
        customAdapter_upload.onLongClickAdapter(position);

        //change action bar setup
        change_action_bar_select();
    }

    private void on_select_all(){
        isSelectionMode = true;
        uploadButton.setText(R.string.upload_selected_items);
        customAdapter_upload.checkAllCheckbox_setFolderList();

        change_action_bar_select();
    }

    private void cancel_selections(){

        isSelectionMode = false;
        uploadButton.setText(R.string.upload_dropbox);
        customAdapter_upload.undoCheckbox_resetFolderList();

        change_action_bar_deselect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_upload_1, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu (Menu menu) {

        menu.clear();

        if (isSelectionMode){
            getMenuInflater().inflate(R.menu.menu_upload_2, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_upload_1, menu);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if (!isSelectionMode) {
                    onBackPressed();
                } else {
                    cancel_selections();
                }
                return true;

            case R.id.action_upload_select:

                on_longClick_select(-1);

                return true;

            case R.id.action_upload_selectAll:

                on_select_all();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void change_action_bar_deselect(){

        toolbar.setTitle(R.string.title_upload);
        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_normal));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_keyboard_backspace_black_18dp);
    }

    // this function is to set the action bar dynamically when user long press listView item
    private void change_action_bar_select(){

        toolbar.setBackgroundColor(getResources().getColor(R.color.toolbar_longpressed));
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.baseline_close_white_18dp);
    }

    // ************* this section is to set EventBus to listen to upload task update ***************
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateListView(UpdateListViewEvent event) {

        customAdapter_upload.updateFolderList(getAllFolderNames());

    }
    // *********************************************************************************************

}
