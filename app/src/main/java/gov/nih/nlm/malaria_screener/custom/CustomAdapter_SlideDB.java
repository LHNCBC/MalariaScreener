package gov.nih.nlm.malaria_screener.custom;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

import java.io.File;
import java.util.List;

/**
 * Created by yuh5 on 5/25/2016.
 */
public class CustomAdapter_SlideDB extends BaseSwipeAdapter {

    private static final String TAG = "MyDebug";

    Context context;
    List<RowItem_Slide> rowItemSlides;

    MyDBHandler dbHandler;

    public CustomAdapter_SlideDB(Context context, List<RowItem_Slide> rowItemSlides) {
        this.context = context;
        this.rowItemSlides = rowItemSlides;

        dbHandler = new MyDBHandler(context, null, null, 1);
    }

    @Override
    public int getCount() {
        return rowItemSlides.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItemSlides.get(position);
    }

    @Override
    public long getItemId(int position) {
        return rowItemSlides.indexOf(getItem(position));
    }

    private class ViewHolder {
        TextView slideID;
        TextView time;
        TextView date;
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.slideLog;
    }

    @Override
    public View generateView(final int position, ViewGroup viewGroup) {

        View view = LayoutInflater.from(context).inflate(R.layout.list_item_slidedb, null);
        SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(getSwipeLayoutResourceId(position));

        view.findViewById(R.id.trash).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());

                        // Setting Dialog Title
                        alertDialog.setTitle(R.string.delete);

                        // Setting Dialog Message
                        alertDialog.setMessage(R.string.delete_slide);

                        // Setting Positive "Yes" Button
                        String string = context.getResources().getString(R.string.yes);
                        alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                RowItem_Slide rowItem_slide = rowItemSlides.get(position);

                                Cursor cursor = dbHandler.returnSlideCursor(rowItem_slide.getPatientID(), rowItem_slide.getSlideID());

                                String p_thin = cursor.getString(cursor.getColumnIndex("parasitemia_thin"));

                                if (!p_thin.equals("")) { // if slide is thin smear
                                    dbHandler.deleteSlide(rowItem_slide.getSlideID(), rowItem_slide.getPatientID());
                                } else { // if slide is thick smear
                                    dbHandler.deleteSlide_thick(rowItem_slide.getSlideID(), rowItem_slide.getPatientID());
                                }

                                deleteImagesInSlide(rowItem_slide.getPatientID(), rowItem_slide.getSlideID());

                                rowItemSlides.remove(position);
                                notifyDataSetChanged();

                                // Write your code here to invoke YES event
                                String string = context.getResources().getString(R.string.s_deleted);
                                Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
                            }
                        });

                        // Setting Negative "NO" Button
                        String string1 = context.getResources().getString(R.string.no);
                        alertDialog.setNegativeButton(string1, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to invoke NO event
                                String string = context.getResources().getString(R.string.click_no);
                                Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
                                dialog.cancel();
                            }
                        });

                        // Showing Alert Message
                        alertDialog.show();
                    }

                }

        );

        return view;
    }

    private void deleteImagesInSlide(String PID, String SID) {

        if (PID.equals("test")) {

            final File file = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener/Test");

            File[] folderList = file.listFiles();

            if (folderList != null) {
                int length = folderList.length;

                // delete files
                for (int i = 0; i < length; i++) {

                    String imagePath = folderList[i].getAbsolutePath().toString();
                    String SlideIDStr = imagePath.substring(imagePath.lastIndexOf("/") + 1);

                    if (SlideIDStr.equals(SID)) {
                        // delete files
                        File[] imageList = folderList[i].listFiles();

                        if (imageList != null) {
                            int length1 = imageList.length;

                            if (length1 != 0) {
                                for (int j = 0; j < length1; j++) {
                                    imageList[j].delete();
                                }

                                folderList[i].delete();
                            }
                        }
                    }

                }

            }

        } else {

            final File file = new File(Environment.getExternalStorageDirectory(
            ), "NLM_Malaria_Screener");

            File[] folderList = file.listFiles(); // list all folder with "PID_"

            if (folderList != null) {
                int length = folderList.length;

                // delete files
                for (int i = 0; i < length; i++) {

                    if (folderList[i].getAbsolutePath().toString().contains(PID + "_" + SID)) {

                        // delete files
                        File[] imageList = folderList[i].listFiles();

                        if (imageList != null) {
                            int length1 = imageList.length;

                            if (length1 != 0) {
                                for (int j = 0; j < length1; j++) {
                                    imageList[j].delete();
                                }

                                folderList[i].delete();
                            }
                        }
                    }
                }

            }

        }

    }

    @Override
    public void fillValues(int position, View convertView) {

        ViewHolder holder = new ViewHolder();

        holder = new ViewHolder();
        holder.slideID = (TextView) convertView.findViewById(R.id.textView_slideID);
        holder.time = (TextView) convertView.findViewById(R.id.textView_time);
        holder.date = (TextView) convertView.findViewById(R.id.textView_date);

        convertView.setTag(holder);

        RowItem_Slide row_pos = rowItemSlides.get(position);

        holder.slideID.setText(row_pos.getSlideID());
        holder.time.setText(row_pos.getTime());
        holder.date.setText(row_pos.getDate());

    }

}
