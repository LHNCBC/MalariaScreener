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

package gov.nih.nlm.malaria_screener.custom;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;

import gov.nih.nlm.malaria_screener.R;

/**
 * Created by yuh5 on 4/3/2017.
 */

public class PhotoFragment extends Fragment{

    private static final String TAG = "MyDebug";

    private Context context;

    private int position;

    private String imagePath;

    //PassView passView;

    TouchImageView backgroundImage;

    public static PhotoFragment create(int pos, String imgPath) {

        PhotoFragment frag = new PhotoFragment();
        Bundle args = new Bundle();
        args.putInt("pos", pos);
        args.putString("img_path", imgPath);
        frag.setArguments(args);
        return frag;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
        //passView = (PassView) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt("pos");

        imagePath = getArguments().getString("img_path");

        //position = getArguments() != null ? getArguments().getInt("pos") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.item_photo_full, container, false);

        //FrameLayout frameLayout = (FrameLayout)rootView.findViewById(R.id.container);

        backgroundImage = (TouchImageView) rootView.findViewById(R.id.imageView_full);

        if (position%2==0) {
            Glide.with(this).load(imagePath).transform(new RotateTransformation(context, 90f)).skipMemoryCache(true).into(backgroundImage);
        } else {
            Glide.with(this).load(imagePath).skipMemoryCache(true).into(backgroundImage);
        }

        //passView.passExpandedImageView_Container(backgroundImage, frameLayout);

        return rootView;
    }

//    public void setImageView(){
//        Glide.with(this).load(imagePath).into(backgroundImage);
//
//    }

//    public interface PassView{
//
//        void passExpandedImageView_Container(TouchImageView imageView, FrameLayout frameLayout);
//
//    }

}
