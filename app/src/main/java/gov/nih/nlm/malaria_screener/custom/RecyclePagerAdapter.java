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

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.LinkedList;
import java.util.Queue;

import static android.support.v4.view.PagerAdapter.POSITION_NONE;

/**
 * Created by yuh5 on 3/30/2017.
 */


public abstract class RecyclePagerAdapter<VH extends RecyclePagerAdapter.ViewHolder>
        extends PagerAdapter {

    private final Queue<VH> cache = new LinkedList<>();
    private final SparseArray<VH> attached = new SparseArray<>();

    public abstract VH onCreateViewHolder(@NonNull ViewGroup container);

    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    public void onRecycleViewHolder(@NonNull VH holder) {

    }

    /**
     * Returns ViewHolder for given position if it exists within ViewPager, or null otherwise.
     */
    public VH getViewHolder(int position) {
        return attached.get(position);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        VH holder = cache.poll();
        if (holder == null) {
            holder = onCreateViewHolder(container);
        }
        attached.put(position, holder);

        // We should not use previous layout params, since ViewPager stores
        // important information there which cannot be reused
        container.addView(holder.itemView, null);

        onBindViewHolder(holder, position);
        return holder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        VH holder = (VH) object;
        attached.remove(position);
        container.removeView(holder.itemView);
        cache.offer(holder);
        onRecycleViewHolder(holder);

    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        ViewHolder holder = (ViewHolder) object;
        return holder.itemView == view;
    }

    @Override
    public int getItemPosition(Object object) {
        // Forcing all views reinitialization when data set changed.
        // It should be safe because we're using views recycling logic.
        return POSITION_NONE;
    }

    public static class ViewHolder {
        public final View itemView;

        public ViewHolder(@NonNull View itemView) {
            this.itemView = itemView;
        }
    }

}

