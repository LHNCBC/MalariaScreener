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
import android.os.Environment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager.widget.ViewPager.OnPageChangeListener;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
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
import gov.nih.nlm.malaria_screener.frontEnd.EnterManualCounts;


public class ImageGallery extends AppCompatActivity implements CustomAdapter_ImageGalleryDB.OnPhotoCallback {

    private static final String TAG = "MyDebug";

    String patientStr;
    String slideStr;

    private ViewPager viewPager;
    private PagerAdapter mPagerAdapter;
    private OnPageChangeListener mPageChangeListener;
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

    String[] cell_eachImage;
    String[] infected_eachImage;
    String[] cell_eachImageGT;
    String[] infected_eachImageGT;

    CustomAdapter_ImageGalleryDB adapter_imageGalleryDB;
    RecyclerView recyclerView;

    ArrayList<String> imageNameList;

    int thinOrThick = 0;

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

        adapter_imageGalleryDB = new CustomAdapter_ImageGalleryDB(this, imageList, this, cell_eachImage, infected_eachImage, patientStr, slideStr, cell_eachImageGT, infected_eachImageGT, thinOrThick);
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
            imageInfoTextView.setText(totalStr + cell_eachImage[position] + "  " + infectedStr + infected_eachImage[position]);
            imageInfoTextViewGT.setText(totalGTStr + cell_eachImageGT[position] + "  " + infectedStr + infected_eachImageGT[position]);

        } else if (LeftOrRight == "right") {
            viewPager.setCurrentItem(position * 2 + 1);
            imageTitleTextView.setText(imgStr + " " + (position + 1) + " " + resStr);
            imageInfoTextView.setText(totalStr + cell_eachImage[position] + "  " + infectedStr + infected_eachImage[position]);
            imageInfoTextViewGT.setText(totalGTStr + cell_eachImageGT[position] + "  " + infectedStr + infected_eachImageGT[position]);
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

        final Dialog dialog_cellcounts = new Dialog(this);
        dialog_cellcounts.setContentView(R.layout.input_box_manualcounts);
        dialog_cellcounts.setCancelable(false);
        dialog_cellcounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        TextView textView_cellcounts = (TextView) dialog_cellcounts.findViewById(R.id.textView_manualcounts);
        final EditText input_cellcount = (EditText) dialog_cellcounts.findViewById(R.id.editText_manualcounts);
        final String[] cellCount = new String[1];
        Button button_cellcounts = (Button) dialog_cellcounts.findViewById(R.id.button_okay);
        Button buttonCancel_cellcounts = (Button) dialog_cellcounts.findViewById(R.id.button_cancel);

        final Dialog dialog_infectedcounts = new Dialog(this);
        dialog_infectedcounts.setCancelable(false);
        dialog_infectedcounts.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog_infectedcounts.setContentView(R.layout.input_box_manualcounts);
        TextView textView_infectedcounts = (TextView) dialog_infectedcounts.findViewById(R.id.textView_manualcounts);
        final EditText input_infected = (EditText) dialog_infectedcounts.findViewById(R.id.editText_manualcounts);
        final String[] infectedCount = new String[1];
        Button button_infectedcounts = (Button) dialog_infectedcounts.findViewById(R.id.button_okay);
        Button buttonCancel_infectedcounts = (Button) dialog_infectedcounts.findViewById(R.id.button_cancel);

        textView_cellcounts.setText(R.string.manual_cell_count);
        textView_infectedcounts.setText(R.string.manual_count_infected);

        button_cellcounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cellCount[0] = input_cellcount.getText().toString();

                        dialog_infectedcounts.show();
                        dialog_cellcounts.dismiss();
                    }
                }
        );

        buttonCancel_cellcounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog_cellcounts.dismiss();
                    }
                }
        );

        button_infectedcounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        infectedCount[0] = input_infected.getText().toString();

                        if (cellCount[0].isEmpty()) {
                            cellCount[0] = "N/A";
                        }

                        if (infectedCount[0].isEmpty()) {
                            infectedCount[0] = "N/A";
                        }

                        cell_eachImageGT[position] = cellCount[0];
                        infected_eachImageGT[position] = infectedCount[0];

                        dbHandler.updateImageManulCounts(patientStr, slideStr, imageNameList.get(position), cellCount[0], infectedCount[0]);
                        adapter_imageGalleryDB.notifyDataSetChanged();

                        dialog_infectedcounts.dismiss();
                    }
                }
        );

        buttonCancel_infectedcounts.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dialog_infectedcounts.dismiss();
                    }
                }
        );

        dialog_cellcounts.show();
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

        viewPager.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                String imgStr = getResources().getString(R.string.image);
                String oriStr = getResources().getString(R.string.original);
                String resStr = getResources().getString(R.string.res);
                String totalStr = getResources().getString(R.string.total_cell_count);
                String infectedStr = getResources().getString(R.string.infected_cell_count);
                String totalGTStr = getResources().getString(R.string.total_cell_count_GT);
                String infectedGTStr = getResources().getString(R.string.infected_cell_count_GT);

                if (position % 2 == 0) {
                    imageTitleTextView.setText(imgStr + " " + (position / 2 + 1) + " " + oriStr);
                    imageInfoTextView.setText(totalStr + cell_eachImage[position / 2] + "  " + infectedStr + infected_eachImage[position / 2]);
                    imageInfoTextViewGT.setText(totalGTStr + cell_eachImageGT[position / 2] + "  " + infectedGTStr + infected_eachImageGT[position / 2]);

                } else {
                    imageTitleTextView.setText(imgStr + " " + (position / 2 + 1) + " " + resStr);
                    imageInfoTextView.setText(totalStr + cell_eachImage[position / 2] + "  " + infectedStr + infected_eachImage[position / 2]);
                    imageInfoTextViewGT.setText(totalGTStr + cell_eachImageGT[position / 2] + "  " + infectedGTStr + infected_eachImageGT[position / 2]);
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

            Intent manualCountsIntent = new Intent(getBaseContext(), EnterManualCounts.class);
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

            if (position % 2 == 0) { // Calculate current
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

        cell_eachImage = new String[imageNameList.size()];
        infected_eachImage = new String[imageNameList.size()];
        cell_eachImageGT = new String[imageNameList.size()];
        infected_eachImageGT = new String[imageNameList.size()];

        for (int i = 0; i < imageNameList.size(); i++) {

            Cursor cursor = dbHandler.returnSlideImage(patientID, slideID, imageNameList.get(i));

            cell_eachImage[i] = cursor.getString(cursor.getColumnIndex("cell_count"));
            infected_eachImage[i] = cursor.getString(cursor.getColumnIndex("infected_count"));
            cell_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("cell_count_gt"));
            infected_eachImageGT[i] = cursor.getString(cursor.getColumnIndex("infected_count_gt"));
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
        adapter_imageGalleryDB = new CustomAdapter_ImageGalleryDB(this, imageList, this, cell_eachImage, infected_eachImage, patientStr, slideStr, cell_eachImageGT, infected_eachImageGT, thinOrThick);
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
