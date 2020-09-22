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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by yuh5 on 4/29/2016.
 */
public class CustomAdapter_Counts extends BaseAdapter {

    Context context;
    List<RowItem_CountsNtexts> rowItemCounts;

    public CustomAdapter_Counts(Context context, List<RowItem_CountsNtexts> rowItemCounts){
        this.context = context;
        this.rowItemCounts = rowItemCounts;
    }

    @Override
    public int getCount() {
        return rowItemCounts.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItemCounts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItemCounts.indexOf(getItem(position));
    }

    /* private view holder class */
    private class ViewHolder {

        TextView title;
        TextView count1;
        TextView count2;

        TextView sub_text1;
        TextView sub_text2;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = mInflater.inflate(gov.nih.nlm.malaria_screener.R.layout.list_item_counts, null);

            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_title);
            holder.count1 = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_1);
            holder.count2 = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_2);
            holder.sub_text1 = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView3);
            holder.sub_text2 = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView4);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RowItem_CountsNtexts row_pos = rowItemCounts.get(position);

        holder.title.setText(row_pos.getTitle());
        holder.count1.setText(String.valueOf(row_pos.getCount1()));
        holder.count2.setText(String.valueOf(row_pos.getCount2()));
        holder.sub_text1.setText(row_pos.getTxt1());
        holder.sub_text2.setText(row_pos.getTxt2());

        return convertView;
    }




}
