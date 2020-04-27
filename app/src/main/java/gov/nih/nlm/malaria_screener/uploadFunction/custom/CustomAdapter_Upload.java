package gov.nih.nlm.malaria_screener.uploadFunction.custom;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // update your model (or other business logic) based on isChecked

                if (isChecked) {
                    int pos = (int) holder.checkBox.getTag();
                    rowItem_foldersArrayList.get(pos).setSelected(true);

                    select_num++;
                    onSelectedListener.onSelectionNumChanged(select_num);

                } else {
                    int pos = (int) holder.checkBox.getTag();
                    rowItem_foldersArrayList.get(pos).setSelected(false);

                    select_num--;
                    onSelectedListener.onSelectionNumChanged(select_num);
                }


            }
        });

        return convertView;
    }

    public void showCheckbox(int checkedPos_longclicked) {
        isSelectionMode = true;

        notifyDataSetChanged();  // Required for update
    }

    public void checkAllCheckbox_setFolderList(){

        isSelectionMode = true;

        for (RowItem_Folders rowItem_folders: rowItem_foldersArrayList) {
            rowItem_folders.setSelected(true);
        }

        notifyDataSetChanged();

    }

    public void undoCheckbox_resetFolderList(){
        isSelectionMode = false;

        for (RowItem_Folders rowItem_folders: rowItem_foldersArrayList) {
            rowItem_folders.setSelected(false);
        }

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

}
