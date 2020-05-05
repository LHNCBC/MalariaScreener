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

import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;
import gov.nih.nlm.malaria_screener.uploadFunction.UploadHashManager;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by yuh5 on 6/1/2016.
 */
public class CustomAdapter_PatientDB extends BaseSwipeAdapter {

    private static final String TAG = "MyDebug";

    MyDBHandler dbHandler;

    private Context context;
    List<RowItem_Patient> rowItemPatients;

    public boolean testPatient = false;

    public CustomAdapter_PatientDB(Context context, List<RowItem_Patient> rowItemPatients, boolean testPatient) {
        this.context = context;
        this.rowItemPatients = rowItemPatients;
        this.testPatient = testPatient;

        dbHandler = new MyDBHandler(context, null, null, 1);
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return gov.nih.nlm.malaria_screener.R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(gov.nih.nlm.malaria_screener.R.layout.list_item_patient, null);
        SwipeLayout swipeLayout = (SwipeLayout) view.findViewById(getSwipeLayoutResourceId(position));

        swipeLayout.addSwipeListener(
                new SimpleSwipeListener() {
                    @Override
                    public void onOpen(SwipeLayout layout) {
                        //YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
                    }

                }
        );

        view.findViewById(gov.nih.nlm.malaria_screener.R.id.trash).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext());

                        // Setting Dialog Title
                        alertDialog.setTitle(gov.nih.nlm.malaria_screener.R.string.delete);

                        // Setting Dialog Message
                        alertDialog.setMessage(R.string.delete_patient);

                        // Setting Positive "Yes" Button
                        String string = context.getResources().getString(R.string.yes);
                        alertDialog.setPositiveButton(string, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                RowItem_Patient rowItem_patient = rowItemPatients.get(position);

                                String PID = rowItem_patient.getID();

                                deleteAllPatientImagesFromHashMap(PID);

                                if (!testPatient) {
                                    dbHandler.deletePatient(PID);
                                    deleteImagesInPatient(PID);
                                } else {
                                    dbHandler.deletePatient("test");
                                    deleteImagesInTest();
                                }

                                rowItemPatients.remove(position);
                                notifyDataSetChanged();

                                // Write your code here to invoke YES event
                                String string = context.getResources().getString(R.string.p_deleted);
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

    private void deleteImagesInPatient(String PID) {

        final File file = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener");

        File[] folderList = file.listFiles(); // list all folder with "PID_"

        if (folderList != null) {
            int length = folderList.length;

            // delete files
            for (int i = 0; i < length; i++) {

                if (folderList[i].getAbsolutePath().toString().contains(PID + "_")) {

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

    private void deleteImagesInTest() {

        final File fileTest = new File(Environment.getExternalStorageDirectory(
        ), "NLM_Malaria_Screener/Test");


        File[] folderListTest = fileTest.listFiles();

        if (folderListTest != null) {
            int length = folderListTest.length;

            // delete files
            for (int i = 0; i < length; i++) {

                File[] imageList = folderListTest[i].listFiles();

                if (imageList != null) {
                    int length1 = imageList.length;

                    if (length1 != 0) {
                        for (int j = 0; j < length1; j++) {
                            imageList[j].delete();
                        }

                        folderListTest[i].delete();
                    }
                }
            }

            fileTest.delete();
        }

    }

    // search PID in image table thin&thick and delete corresponding imageIDs from HashMap
    private void deleteAllPatientImagesFromHashMap(String PID){

        // image table thin
        Cursor cursor1 = dbHandler.returnAllPatientImages(PID);
        Log.d(TAG,"Thin: ");

        if (cursor1.moveToFirst()) {
            do {

                String imgIDStr = cursor1.getString(cursor1.getColumnIndex("image_id"));

                Log.d(TAG, "imgIDStr: " + imgIDStr);

                if (UploadHashManager.hashmap_for_upload.containsKey(imgIDStr)) {
                    UploadHashManager.hashmap_for_upload.remove(imgIDStr);
                }
            } while (cursor1.moveToNext());
        }

        cursor1.close();

        // image table thick
        Cursor cursor2 = dbHandler.returnAllPatientImages_thick(PID);
        Log.d(TAG,"Thick: ");

        if (cursor2.moveToFirst()) {
            do {
                String imgIDStr = cursor2.getString(cursor2.getColumnIndex("image_id_thick"));

                Log.d(TAG, "imgIDStr: " + imgIDStr);

                if (UploadHashManager.hashmap_for_upload.containsKey(imgIDStr)) {
                    UploadHashManager.hashmap_for_upload.remove(imgIDStr);
                }

            } while (cursor2.moveToNext());
        }

        cursor2.close();

        UploadHashManager.saveMap(context, UploadHashManager.hashmap_for_upload);

    }

    /* private view holder class */
    private class ViewHolder {

        TextView ID;
        TextView initial;
        TextView gender;
        TextView age;

    }

    @Override
    public void fillValues(int position, View convertView) {

        ViewHolder holder = new ViewHolder();

        holder.ID = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_ID);
        holder.initial = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_initial);
        holder.gender = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_gender);
        holder.age = (TextView) convertView.findViewById(gov.nih.nlm.malaria_screener.R.id.textView_age);

        convertView.setTag(holder);

        RowItem_Patient row_pos = rowItemPatients.get(position);

        holder.ID.setText(row_pos.getID());
        holder.initial.setText(row_pos.getInitial());
        holder.gender.setText(row_pos.getGender());
        holder.age.setText(row_pos.getAge());


    }

    @Override
    public int getCount() {
        return rowItemPatients.size();
    }

    @Override
    public Object getItem(int position) {
        return rowItemPatients.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
