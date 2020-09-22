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

package gov.nih.nlm.malaria_screener.tutorial;

import android.app.Fragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.custom.TouchImageView;

/**
 * Created by yuh5 on 11/30/2016.
 */

public class ScreenSlidePageFragment extends Fragment {

    private static final String TAG = "MyDebug";

    private int position;

    public ScreenSlidePageFragment() {

    }

    public static ScreenSlidePageFragment create(int pos) {

        ScreenSlidePageFragment frag = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");
        //position = getArguments() != null ? getArguments().getInt("pos") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.fragment_screen_slide_page, container, false);

        ImageView backgroundImage = (ImageView) rootView.findViewById(R.id.imageView_background);

//        TextView pageText = (TextView) rootView.findViewById(R.id.textView_page);
//        pageText.setText( (position+1) + "/7");
//
//        TextView titleText = (TextView) rootView.findViewById(R.id.textView_pageTitle);

        if (position == 0) {
            backgroundImage.setBackgroundResource(R.drawable.home);

        } else if (position == 1) {
            backgroundImage.setBackgroundResource(R.drawable.camera);

        } else if (position == 2) {
            backgroundImage.setBackgroundResource(R.drawable.preview);

        } else if (position == 3) {
            backgroundImage.setBackgroundResource(R.drawable.result);

        } else if (position == 4) {
            backgroundImage.setBackgroundResource(R.drawable.patientinfo);

        } else if (position == 5) {
            backgroundImage.setBackgroundResource(R.drawable.summary);

        } else if (position == 6) {
            backgroundImage.setBackgroundResource(R.drawable.database);

        } else if (position == 7) {
            backgroundImage.setBackgroundResource(R.drawable.database_patient);

        } else if (position == 8) {
            backgroundImage.setBackgroundResource(R.drawable.database_slide);

        } else if (position == 9) {
            backgroundImage.setBackgroundResource(R.drawable.manual_counts1);

        } else if (position == 10) {
            backgroundImage.setBackgroundResource(R.drawable.manual_counts2);

        } else if (position == 11) {
            backgroundImage.setBackgroundResource(R.drawable.manual_counts3);

        } else if (position == 12) {
            backgroundImage.setBackgroundResource(R.drawable.manual_counts4);

        } else if (position == 13) {
            backgroundImage.setBackgroundResource(R.drawable.manual_counts5);

        } else if (position == 14) {
            backgroundImage.setBackgroundResource(R.drawable.upload1);

        } else if (position == 15) {
            backgroundImage.setBackgroundResource(R.drawable.upload2);

        } else if (position == 16) {
            backgroundImage.setBackgroundResource(R.drawable.upload3);

        } else if (position == 17) {
            backgroundImage.setBackgroundResource(R.drawable.upload4);

        } else if (position == 18) {
            backgroundImage.setBackgroundResource(R.drawable.setting);

        } else if (position == 19) {
            backgroundImage.setBackgroundResource(R.drawable.appcrash);
        }

        return rootView;
    }
}
