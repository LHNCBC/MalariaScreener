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
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.googlecode.flickrjandroid.photos.Photo;

import java.util.List;

import gov.nih.nlm.malaria_screener.R;


/**
 * Created by yuh5 on 3/30/2017.
 */

public class PhotoPagerAdapter extends RecyclePagerAdapter<PhotoPagerAdapter.ViewHolder> {

    private Context context;

    private ViewPager viewPager;
    private List<Photo> photos; // class from flickr library to hold photos

    private boolean activated;

    public PhotoPagerAdapter(Context context, ViewPager viewPager){
        this.viewPager = viewPager;
        this.context = context;
    }

    public void setPhotos(List<Photo> photos){
        this.photos = photos;

    }

    public Photo getPhoto(int pos){
        if (photos==null || pos<0 || pos>=photos.size()){
            return null;
        } else {
            return photos.get(pos);
        }
    }

    public void setActivated(boolean activated){
        if (this.activated != activated) {
            this.activated = activated;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup container) {

        //final ViewHolder holder = new ViewHolder(container);

        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_photo_full, container, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Photo photo = photos.get(position);

        final String photoUrl = photo.getLargeSize() == null
                ? photo.getMediumUrl() : photo.getLargeUrl();

        Glide.with(context).load(photoUrl).into(holder.imageView);

    }

    @Override
    public int getCount() {
        return !activated || photos == null ? 0 : photos.size();
    }

    static class ViewHolder extends RecyclePagerAdapter.ViewHolder{

        final ImageView imageView;


        public ViewHolder(@NonNull View view) {
            super(view);

            imageView = (ImageView)view.findViewById(R.id.imageView_full);
        }
    }
}
