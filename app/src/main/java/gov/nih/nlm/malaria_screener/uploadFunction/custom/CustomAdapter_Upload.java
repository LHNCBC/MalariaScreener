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

package gov.nih.nlm.malaria_screener.uploadFunction.custom;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;

import gov.nih.nlm.malaria_screener.R;

public class CustomAdapter_Upload extends ArrayAdapter<RowItem_Folders> {

    private static final String TAG = "MyDebug";

    private Context context;
    private int mResource;

    private ArrayList<RowItem_Folders> rowItem_foldersArrayList;
    private int select_num;

    private boolean isSelectionMode = false;

    private OnSelectedListener onSelectedListener;

    static class ViewHolder {
        TextView folderName;
        TextView folderDate;
        ImageView imageView;
        CheckBox checkBox;

    }

    public CustomAdapter_Upload(Context context, int resource, ArrayList<RowItem_Folders> rowItem_foldersArrayList, OnSelectedListener onSelectedListener){
        super(context, resource, rowItem_foldersArrayList);

        this.context = context;
        this.mResource = resource;

        this.rowItem_foldersArrayList = rowItem_foldersArrayList;

        this.onSelectedListener = onSelectedListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String folderNameStr = getItem(position).getItem();
        String folderDateStr = getItem(position).getDate();
        Boolean isfolderSelected = getItem(position).getSelected();
        Boolean isfolderUploaded = getItem(position).getUploaded();

        //final View result;
        final ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.imageView_foldericon);
            holder.folderName = (TextView) convertView.findViewById(R.id.textView_folderName);
            holder.folderDate = (TextView) convertView.findViewById(R.id.textView_folderDate);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox_upload);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.folderName.setText(folderNameStr);
        holder.folderDate.setText(folderDateStr);
        holder.checkBox.setChecked(isfolderSelected);

        if (isfolderUploaded){
            holder.imageView.setImageResource(R.drawable.folder_check);
        } else {
            holder.imageView.setImageResource(R.drawable.folder_icon);
        }

        if(isSelectionMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.checkBox.setTag(position);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int pos = (int) holder.checkBox.getTag();

                if (!rowItem_foldersArrayList.get(pos).getSelected()) {

                    Log.d(TAG, "pos: " + pos + " is checked");
                    rowItem_foldersArrayList.get(pos).setSelected(true);

                    select_num++;
                    onSelectedListener.onSelectionNumChanged(select_num);

                } else {
                    Log.d(TAG, "pos: " + pos + " is unchecked");
                    rowItem_foldersArrayList.get(pos).setSelected(false);

                    select_num--;
                    onSelectedListener.onSelectionNumChanged(select_num);
                }

            }
        });

        return convertView;
    }

    public void onLongClickAdapter(int pos_longclicked) {
        isSelectionMode = true;

        if (pos_longclicked != -1) {
            rowItem_foldersArrayList.get(pos_longclicked).setSelected(true);

            select_num++;
        }
        onSelectedListener.onSelectionNumChanged(select_num);

        notifyDataSetChanged();  // Required for update
    }

    public void checkAllCheckbox_setFolderList(){

        isSelectionMode = true;

        for (RowItem_Folders rowItem_folders: rowItem_foldersArrayList) {
            rowItem_folders.setSelected(true);
        }

        select_num = getCount();
        onSelectedListener.onSelectionNumChanged(select_num);

        notifyDataSetChanged();

    }

    public void undoCheckbox_resetFolderList(){
        isSelectionMode = false;

        for (RowItem_Folders rowItem_folders: rowItem_foldersArrayList) {
            rowItem_folders.setSelected(false);
        }

        select_num = 0;
        onSelectedListener.onSelectionNumChanged(select_num);
        notifyDataSetChanged();

    }

    public ArrayList<RowItem_Folders> getRowItem_foldersArrayList(){

        return rowItem_foldersArrayList;
    }

    public interface OnSelectedListener{

        void onSelectionNumChanged(int numOfSelectedItems);
    }

    public void updateFolderList(ArrayList<RowItem_Folders> rowItem_foldersArrayList){

        for (int i = 0; i<rowItem_foldersArrayList.size(); i++){
            boolean isUploaded = rowItem_foldersArrayList.get(i).getUploaded();
            this.rowItem_foldersArrayList.get(i).setUploaded(isUploaded);
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return rowItem_foldersArrayList.size();
    }

    public int getSelect_num() {
        return select_num;
    }
}
