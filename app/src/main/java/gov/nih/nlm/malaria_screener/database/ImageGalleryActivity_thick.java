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

package gov.nih.nlm.malaria_screener.database;

import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.CustomAdapter_ImageGalleryDB;
import gov.nih.nlm.malaria_screener.custom.RowItem_Image;
import gov.nih.nlm.malaria_screener.custom.TouchImageView;
import gov.nih.nlm.malaria_screener.frontEnd.EnterManualCounts_thick;

public class ImageGalleryActivity_thick extends AppCompatActivity implements CustomAdapter_ImageGalleryDB.OnPhotoCallback {

    private static final String TAG = "MyDebug";

    String patientStr;
    String slideStr;

    private ViewPager viewPager;
    private PagerAdapter mPagerAdapter;
    private ViewPager.OnPageChangeListener mPageChangeListener;
    //private PhotoPagerAdapter photoPagerAdapter;
    //private ZoomImageFromThumb zoomImageFromThumb;

    ArrayList<RowItem_Image> imageList;
    int pageNum;

    TouchImageView imageView;
    FrameLayout frameLayout;

    Toolbar toolbar;
    TextView imageTitleTextView, imageInfoTextView, imageInfoTextViewGT;
    RelativeLayout relativeLayout;
    private boolean pagerVisible = false;

    MyDBHandler dbHandler;

    String[] wbc_eachImage;
    String[] parasite_eachImage;
    String[] wbc_eachImageGT;
    String[] parasite_eachImageGT;

    CustomAdapter_ImageGalleryDB adapter_imageGalleryDB;
    RecyclerView recyclerView;

    ArrayList<String> imageNameList;

    int thinOrThick = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);

        Bundle extras = getIntent().getExtras();
        patientStr = extras.getString("itemPID");
        slideStr = extras.getString("itemSID");

        Toolbar toolbar = (Toolbar) findViewById(R.id.navigate_bar_imageview);
        toolbar.setTitle(R.string.title_image_view);
        toolbar.setTitleTextColor(getResources().getColor(R.color.toolbar_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        dbHandler = new MyDBHandler(this, null, null, 1);

        // Get image path
        File slideDir;

        slideDir = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/" + patientStr + "_" + slideStr);


        ArrayList<String> originalImagePath = new ArrayList<>();
        ArrayList<String> resultImagePath = new ArrayList<>();
        imageNameList = new ArrayList<>();

        if (slideDir.exists()) {

            File[] allImageListing = slideDir.listFiles(); // list all images in this slide directory

            for (int i = 0; i < allImageListing.length; i++) {

                String imagePath = allImageListing[i].getAbsolutePath();

                if ((imagePath.indexOf("result") == -1) && (imagePath.indexOf("mask") == -1)) { // pick out the original image by checking the image name
                    originalImagePath.add(imagePath);

                    int endIndex = imagePath.lastIndexOf(".");
                    String path_till_imageName = imagePath.substring(0, endIndex);
                    String extension = imagePath.substring(endIndex);
                    String fullPath = path_till_imageName + "_result" + extension;

                    resultImagePath.add(fullPath);
                }

            }

        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_imagegallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        recyclerView.setLayoutManager(layoutManager);

        imageList = prepareImageData(originalImagePath, resultImagePath);

        // get image name list
        for (int i = 0; i < originalImagePath.size(); i++) {

            String imagePath = originalImagePath.get(i);
            imageNameList.add(imagePath.substring(imagePath.lastIndexOf("/") + 1));

        }

        // read cell counts from database
        getInfoOfEachImageFromDB(patientStr, slideStr, imageNameList);

        adapter_imageGalleryDB = new CustomAdapter_ImageGalleryDB(this, imageList, this, wbc_eachImage, parasite_eachImage, patientStr, slideStr, wbc_eachImageGT, parasite_eachImageGT, thinOrThick);
        recyclerView.setAdapter(adapter_imageGalleryDB);

        pageNum = imageList.size();
        initPager();

    }

    private ArrayList<RowItem_Image> prepareImageData(ArrayList<String> originalPath, ArrayList<String> resultPath) {

        ArrayList<RowItem_Image> rowItem_imageArrayList = new ArrayList<>();

        int iteration;

        if (originalPath.size() > resultPath.size()) {
            iteration = resultPath.size();
        } else {
            iteration = originalPath.size();
        }

        for (int i = 0; i < iteration; i++) {
            RowItem_Image rowItem_image = new RowItem_Image();
            rowItem_image.setImage_original(originalPath.get(i));
            rowItem_image.setImage_result(resultPath.get(i));
            rowItem_imageArrayList.add(rowItem_image);
        }
        return rowItem_imageArrayList;

    }

    // callback from CustomAdapter_ImageGalleryDB
    @Override
    public void onPhotoClick(int position, String LeftOrRight, View view) {

        String imgStr = getResources().getString(R.string.image);
        String oriStr = getResources().getString(R.string.original);
        String resStr = getResources().getString(R.string.res);
        String totalStr = getResources().getString(R.string.total_cell_count);
        String infectedStr = getResources().getString(R.string.infected_cell_count);
        String totalGTStr = getResources().getString(R.string.total_cell_count_GT);
        String infectedGTStr = getResources().getString(R.string.infected_cell_count_GT);


        // set image in pager to display according to user's selection
        if (LeftOrRight == "left") {
            viewPager.setCurrentItem(position * 2);
            imageTitleTextView.setText(imgStr + " " + (position + 1) + " " + oriStr);
            imageInfoTextView.setText(totalStr + wbc_eachImage[position] + "  " + infectedStr + parasite_eachImage[position]);
            imageInfoTextViewGT.setText(totalGTStr + wbc_eachImageGT[position] + "  " + infectedStr + parasite_eachImageGT[position]);

        } else if (LeftOrRight == "right") {
            viewPager.setCurrentItem(position * 2 + 1);
            imageTitleTextView.setText(imgStr + " " + (position + 1) + " " + resStr);
            imageInfoTextView.setText(totalStr + wbc_eachImage[position] + "  " + infectedStr + parasite_eachImage[position]);
            imageInfoTextViewGT.setText(totalGTStr + wbc_eachImageGT[position] + "  " + infectedStr + parasite_eachImageGT[position]);
        }

        setWidgetsVisible();

//        ScreenSlidePagerAdapter screenSlidePagerAdapter = (ScreenSlidePagerAdapter) viewPager.getAdapter();
//        PhotoFragment photoFragment = screenSlidePagerAdapter.getFragment(1);
//        photoFragment.setImageView();

        //zoomImageFromThumb.zoomImageFromThumb(view, imageView, frameLayout);

    }

    @Override
    public void onPhotoLongClick(int position, String LeftOrRight, View view) {

        showInputBox(position);

    }

    public void showInputBox(final int position) {

        final Dialog dialog_WBCcounts = new Dialog(this);
        dialog_WBCcounts.setContentView(R.layout.input_box_manualcounts);
        dialog_WBCcounts.setCancelable(false);
        dialog_WBCcounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        TextView textView_wbccounts = (TextView) dialog_WBCcounts.findViewById(R.id.textView_manualcounts);
        final EditText input_wbccount = (EditText) dialog_WBCcounts.findViewById(R.id.editText_manualcounts);
        final String[] wbcCount = new String[1];
        Button button_wbccounts = (Button) dialog_WBCcounts.findViewById(R.id.button_okay);
        Button buttonCancel_wbccounts = (Button) dialog_WBCcounts.findViewById(R.id.button_cancel);

        final Dialog dialog_parasitecounts = new Dialog(this);
        dialog_parasitecounts.setCancelable(false);
        dialog_parasitecounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog_parasitecounts.setContentView(R.layout.input_box_manualcounts);
        TextView textView_parasitecounts = (TextView) dialog_parasitecounts.findViewById(R.id.textView_manualcounts);
        final EditText input_parasite = (EditText) dialog_parasitecounts.findViewById(R.id.editText_manualcounts);
        final String[] parasiteCount = new String[1];
        Button button_parasitecounts = (Button) dialog_parasitecounts.findViewById(R.id.button_okay);
        Button buttonCancel_parasitecounts = (Button) dialog_parasitecounts.findViewById(R.id.button_cancel);

        textView_wbccounts.setText(R.string.manual_wbc_count);
        textView_parasitecounts.setText(R.string.manual_parasite_infected);

        button_wbccounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        wbcCount[0] = input_wbccount.getText().toString();

                        dialog_parasitecounts.show();
                        dialog_WBCcounts.dismiss();
                    }
                }
        );

        buttonCancel_wbccounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog_WBCcounts.dismiss();
                    }
                }
        );

        button_parasitecounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parasiteCount[0] = input_parasite.getText().toString();

                        if (wbcCount[0].isEmpty()) {
                            wbcCount[0] = "N/A";
                        }

                        if (parasiteCount[0].isEmpty()) {
                            parasiteCount[0] = "N/A";
                        }

                        wbc_eachImageGT[position] = wbcCount[0];
                        parasite_eachImageGT[position] = parasiteCount[0];

                        dbHandler.updateImageManulCounts_thick(patientStr, slideStr, imageNameList.get(position), wbcCount[0], parasiteCount[0]);
                        adapter_imageGalleryDB.notifyDataSetChanged();

                        dialog_parasitecounts.dismiss();
                    }
                }
        );

        buttonCancel_parasitecounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog_parasitecounts.dismiss();
                    }
                }
        );

        dialog_WBCcounts.show();
    }

    private void initPager() {

        relativeLayout = (RelativeLayout) findViewById(R.id.pagerLayout);

        viewPager = (ViewPager) findViewById(R.id.pager);
        //photoPagerAdapter = new PhotoPagerAdapter(this, viewPager);
        //viewPager.setAdapter(photoPagerAdapter);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mPagerAdapter);

        imageTitleTextView = (TextView) findViewById(R.id.textView_imageTitle);
        imageInfoTextView = (TextView) findViewById(R.id.textView_imageInfo);
        imageInfoTextViewGT = (TextView) findViewById(R.id.textView_imageInfoGT);

        toolbar = (Toolbar) findViewById(R.id.toolbar_full);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setWidgetsGone();
            }
        });

        setWidgetsGone();

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                String imgStr = getResources().getString(R.string.image);
                String oriStr = getResources().getString(R.string.original);
                String resStr = getResources().getString(R.string.res);

                String wbcStr = getResources().getString(R.string.wbc_count_1);
                String parasiteStr = getResources().getString(R.string.parasite_count_1);
                String wbcGTStr = getResources().getString(R.string.wbc_count_GT_1);


                if (position % 2 == 0) {
                    imageTitleTextView.setText(imgStr + " " + (position / 2 + 1) + " " + oriStr);
                    imageInfoTextView.setText(wbcStr + wbc_eachImage[position / 2] + "  " + parasiteStr + parasite_eachImage[position / 2]);
                    imageInfoTextViewGT.setText(wbcGTStr + wbc_eachImageGT[position / 2] + "  " + parasiteStr + parasite_eachImageGT[position / 2]);

                } else {
                    imageTitleTextView.setText(imgStr + " " + (position / 2 + 1) + " " + resStr);
                    imageInfoTextView.setText(wbcStr + wbc_eachImage[position / 2] + "  " + parasiteStr + parasite_eachImage[position / 2]);
                    imageInfoTextViewGT.setText(wbcGTStr + wbc_eachImageGT[position / 2] + "  " + parasiteStr + parasite_eachImageGT[position / 2]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_imagegallery, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        int id = item.getItemId();

        if (id == R.id.action_manualCounts) {

            Intent manualCountsIntent = new Intent(getBaseContext(), EnterManualCounts_thick.class);
            manualCountsIntent.putExtra("imageNum", imageList.size());
            manualCountsIntent.putExtra("imageNameList", imageNameList);
            manualCountsIntent.putExtra("itemPID", patientStr);
            manualCountsIntent.putExtra("itemSID", slideStr);

            startActivity(manualCountsIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setWidgetsVisible() {
        pagerVisible = true;
        relativeLayout.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.VISIBLE);
        viewPager.setVisibility(View.VISIBLE);
        imageTitleTextView.setVisibility(View.VISIBLE);
        imageInfoTextView.setVisibility(View.VISIBLE);
        imageInfoTextViewGT.setVisibility(View.VISIBLE);
    }

    private void setWidgetsGone() {
        pagerVisible = false;
        relativeLayout.setVisibility(View.GONE);
        toolbar.setVisibility(View.GONE);
        viewPager.setVisibility(View.GONE);
        imageTitleTextView.setVisibility(View.GONE);
        imageInfoTextView.setVisibility(View.GONE);
        imageInfoTextViewGT.setVisibility(View.GONE);
    }

    public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        //private SparseArray<PhotoFragment> mPageReferenceMap = new SparseArray<PhotoFragment>();

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            String imgStr; // image path to pass to fragment page

            if (position % 2 == 0) { // Calculate corrent
                imgStr = imageList.get(position / 2).getImage_original();
            } else {
                imgStr = imageList.get(position / 2).getImage_result();
            }

            //PhotoFragment photoFragment = PhotoFragment.create(imgPosition, imgString);
            //mPageReferenceMap.put(position, photoFragment);
            return PhotoFragment.create(position, imgStr);
        }

//        @Override
//        public void destroyItem(ViewGroup container, int position, Object object) {
//            super.destroyItem(container, position, object);
//            mPageReferenceMap.remove(position);
//        }

        @Override
        public int getCount() {
            return pageNum * 2;
        }

//        public PhotoFragment getFragment(int key) {
//            return mPageReferenceMap.get(key);
//        }

    }

    private void getInfoOfEachImageFromDB(String patientID, String slideID, ArrayList<String> imageNameList) {

        wbc_eachImage = new String[imageNameList.size()];
        parasite_eachImage = new String[imageNameList.size()];
        wbc_eachImageGT = new String[imageNameList.size()];
        parasite_eachImageGT = new String[imageNameList.size()];

        for (int i = 0; i < imageNameList.size(); i++) {

            Cursor cursor = dbHandler.returnSlideImage_thick(patientID, slideID, imageNameList.get(i));

            wbc_eachImage[i] = cursor.getString(cursor.getColumnIndex("wbc_count"));
            parasite_eachImage[i] = cursor.getString(cursor.getColumnIndex("parasite_count"));
            wbc_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("wbc_count_gt"));
            parasite_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("parasite_count_gt"));

        }

    }

    public void onBackPressed() {

        if (pagerVisible) {
            setWidgetsGone();
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");

        //getInfoOfEachImage(patientStr, slideStr);
        getInfoOfEachImageFromDB(patientStr, slideStr, imageNameList);
        adapter_imageGalleryDB = new CustomAdapter_ImageGalleryDB(this, imageList, this, wbc_eachImage, parasite_eachImage, patientStr, slideStr, wbc_eachImageGT, parasite_eachImageGT, thinOrThick);
        recyclerView.setAdapter(adapter_imageGalleryDB);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }


}
