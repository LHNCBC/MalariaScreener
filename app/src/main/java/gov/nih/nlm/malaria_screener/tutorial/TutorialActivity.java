package gov.nih.nlm.malaria_screener.tutorial;

import android.app.Fragment;
import android.app.FragmentManager;

import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import gov.nih.nlm.malaria_screener.R;

/**
 * Created by yuh5 on 11/30/2016.
 */
public class TutorialActivity extends AppCompatActivity {

    private static final String TAG = "MyDebug";

    /**
     * The number of pages (wizard steps) to show in this demo.
     */
    private static final int NUM_PAGES = 20;

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private ViewPager mPager;
    TextView pageText;
    TextView titleText;

    /**
     * The pager adapter, which provides the pages to the view pager widget.
     */
    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(gov.nih.nlm.malaria_screener.R.layout.activity_screen_slide);

        Toolbar toolbar = (Toolbar) findViewById(gov.nih.nlm.malaria_screener.R.id.navigate_bar_tutorial);
        toolbar.setTitle(gov.nih.nlm.malaria_screener.R.string.title_tutorial);
        toolbar.setTitleTextColor(getResources().getColor(gov.nih.nlm.malaria_screener.R.color.toolbar_title));
        setSupportActionBar(toolbar);
        // set home button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pageText = (TextView) findViewById(gov.nih.nlm.malaria_screener.R.id.textView_page);
        titleText = (TextView) findViewById(gov.nih.nlm.malaria_screener.R.id.textView_pageTitle);

        pageText.setText("1/20");
        titleText.setText(R.string.home);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(gov.nih.nlm.malaria_screener.R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                pageText.setText((position + 1) + "/20");

                if (position == 0) {
                    titleText.setText(gov.nih.nlm.malaria_screener.R.string.home);
                } else if (position == 1) {
                    titleText.setText(gov.nih.nlm.malaria_screener.R.string.camera);
                } else if (position == 2) {
                    titleText.setText(gov.nih.nlm.malaria_screener.R.string.preview);
                } else if (position == 3) {
                    titleText.setText(gov.nih.nlm.malaria_screener.R.string.result);
                } else if (position == 4) {
                    titleText.setText(gov.nih.nlm.malaria_screener.R.string.input);
                } else if (position == 5) {
                    titleText.setText(R.string.summary);
                } else if (position == 6) {
                    titleText.setText(R.string.database1);
                } else if (position == 7) {
                    titleText.setText(R.string.database2);
                } else if (position == 8) {
                    titleText.setText(R.string.database3);
                } else if (position == 9) {
                    titleText.setText(R.string.manual_counts_1);
                } else if (position == 10) {
                    titleText.setText(R.string.manual_counts_2);
                } else if (position == 11) {
                    titleText.setText(R.string.manual_counts_3);
                } else if (position == 12) {
                    titleText.setText(R.string.manual_counts_4);
                } else if (position == 13) {
                    titleText.setText(R.string.manual_counts_5);
                } else if (position == 14) {
                    titleText.setText(R.string.upload_1);
                } else if (position == 15) {
                    titleText.setText(R.string.upload_2);
                } else if (position == 16) {
                    titleText.setText(R.string.upload_3);
                } else if (position == 17) {
                    titleText.setText(R.string.upload_4);
                } else if (position == 18) {
                    titleText.setText(R.string.setting);
                } else if (position == 19) {
                    titleText.setText(R.string.crash);
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        if (mPager.getCurrentItem() == 0) {
//            // If the user is currently looking at the first step, allow the system to handle the
//            // Back button. This calls finish() on this activity and pops the back stack.
//            super.onBackPressed();
//        } else {
//            // Otherwise, select the previous step.
//            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
//        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            return ScreenSlidePageFragment.create(position);
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
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
