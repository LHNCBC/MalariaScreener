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

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;

/**
 * Created by yuh5 on 9/14/2017.
 */

public class CustomAdapter_ManualCounts extends RecyclerView.Adapter<CustomAdapter_ManualCounts.ViewHolder> {

    public static final String TAG = "MyDebug";

    private Context context;
    int itemCount;

    String[] slide_item;
    public String[] slide_txt;
    List<RowItem> rowItems_slide;
    List<List<RowItem>> r;
    List<CustomAdapter> adapters;

    public CustomAdapter_ManualCounts(Context context, int itemCount, String[] cell_eachImageGT, String[] infected_eachImageGT) {
        this.context = context;
        this.itemCount = itemCount;

        r = new ArrayList<List<RowItem>>();
        adapters = new ArrayList<CustomAdapter>();

        //rowItems_slide = new ArrayList<RowItem>();
        slide_item = context.getResources().getStringArray(R.array.manual_counts);
//        if (counts.length != 0 ) {
//            slide_txt = counts;
//        } else {
//            slide_txt = new String[itemCount * 2];
//            Arrays.fill(slide_txt, "N/A");
//        }

        slide_txt = new String[itemCount * 2];
        for (int i = 0; i < this.itemCount; i++) {

            slide_txt[2*i+0] = cell_eachImageGT[i];
            slide_txt[2*i+1] = infected_eachImageGT[i];

            rowItems_slide = new ArrayList<RowItem>();
            r.add(rowItems_slide);

            RowItem item = new RowItem(slide_item[0], slide_txt[2 * i + 0]);
            rowItems_slide.add(item);
            RowItem item1 = new RowItem(slide_item[1], slide_txt[2 * i + 1]);
            rowItems_slide.add(item1);

        }


    }

    @Override
    public CustomAdapter_ManualCounts.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_manualcounts, viewGroup, false);
        return new CustomAdapter_ManualCounts.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomAdapter_ManualCounts.ViewHolder holder, int position) {

        holder.textView_imageTitle.setText("Image " + (position + 1));

        final int pos = position;

        adapters.add(new CustomAdapter(context, r.get(position)));

        holder.listView_manualcounts.setAdapter(adapters.get(position));

        holder.listView_manualcounts.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        showInputBox(position, pos);
                    }
                }
        );


    }

    public void showInputBox(final int pos, final int posInRecycleV) {
        final Dialog dialog = new Dialog(context);
        dialog.setCancelable(false);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.setContentView(R.layout.input_box_manualcounts);
        TextView textView = (TextView) dialog.findViewById(R.id.textView_manualcounts);
        if (pos == 0) {
            textView.setText(R.string.manual_cell_count);
        } else if (pos == 1) {
            textView.setText(R.string.manual_count_infected);
        }
        final EditText editText = (EditText) dialog.findViewById(R.id.editText_manualcounts);
        Button button_okay = (Button) dialog.findViewById(R.id.button_okay);
        button_okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getText().toString().length() == 0) { // if input from user is empty
                    slide_txt[posInRecycleV * 2 + pos] = "N/A";
                } else {
                    slide_txt[posInRecycleV * 2 + pos] = editText.getText().toString();
                }

                RowItem item = new RowItem(slide_item[pos], slide_txt[posInRecycleV * 2 + pos]);

                r.get(posInRecycleV).set(pos, item);

                adapters.get(posInRecycleV).updateRowItems(r.get(posInRecycleV));
                adapters.get(posInRecycleV).notifyDataSetChanged();

                dialog.dismiss();
            }
        });

        Button button_cancel = (Button) dialog.findViewById(R.id.button_cancel);
        button_cancel.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }

        );
        dialog.show();
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView_imageTitle;
        private ListView listView_manualcounts;


        public ViewHolder(View view) {
            super(view);

            textView_imageTitle = (TextView) view.findViewById(R.id.textView_image_title);
            listView_manualcounts = (ListView) view.findViewById(R.id.listView_manualcount);

        }
    }


}
