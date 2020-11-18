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

package gov.nih.nlm.malaria_screener.frontEnd;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import gov.nih.nlm.malaria_screener.R;
import gov.nih.nlm.malaria_screener.database.CSVFileWriter;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;
import gov.nih.nlm.malaria_screener.database.Upload;

public class Uploader {

    Context context;

    private final static String DROPBOX_FILE_DIR = "/NLM_Malaria_Screener/";
    private final static String DROPBOX_NAME = "dropbox_prefs";
    private final static String DROPBOX_DIR = "dropbox_dir"; // folder name on dropbox for current user

    private String folderName;

    private String path;

    DbxClientV2 dbxClientV2;

    MyDBHandler dbHandler;

    public Uploader(Context context) {

        this.context = context;

        dbHandler = new MyDBHandler(context, null, null, 1);

        DbxRequestConfig config = new DbxRequestConfig("dropbox/sample-app", "en_US");

        dbxClientV2 = new DbxClientV2(config, "K-CP-L6LtbAAAAAAAAAAasMaZq16SliwAnpOYA2KYlCkH2EXPv_eTiuJ4BxvASG3");

        SharedPreferences sharedPreferences = context.getSharedPreferences(DROPBOX_NAME, 0);
        folderName = sharedPreferences.getString(DROPBOX_DIR, null);

    }

    public void checkConnectionAndUpload() {

        exportDB();

        new Uploader.CheckFolderName().execute();
    }

    class CheckFolderName extends AsyncTask<Void, Void, Void> {  // at the moment is only for checking internet connection

        List<String> isFolder;
        FullAccount fullAccount;

        @Override
        protected void onPreExecute() {
            String string = context.getResources().getString(R.string.start_upload);
            Toast.makeText(context, string, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {
                //isFolder = dropboxAPI.search(DROPBOX_FILE_DIR, folderName, 1, false);
                fullAccount = dbxClientV2.users().getCurrentAccount();
            } catch (DbxException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void values) {
            super.onPostExecute(values);

            if (fullAccount != null) {
                if (fullAccount.getName().toString().isEmpty()) {
                    String string = context.getResources().getString(R.string.no_internet);
                    Toast.makeText(context.getApplicationContext(), string, Toast.LENGTH_SHORT).show();
                } else {

                    path = DROPBOX_FILE_DIR + folderName;

                    SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences(DROPBOX_NAME, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.commit();

                    new Upload(context.getApplicationContext(), dbxClientV2, path).execute();

                }
            } else {
                String string = context.getResources().getString(R.string.no_internet);
                Toast.makeText(context.getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void exportDB() {

        File exportDir = new File(Environment.getExternalStorageDirectory(), DROPBOX_FILE_DIR);

        File file = new File(exportDir, "MalariaScreenerDB.csv");

        try {
            file.createNewFile();
            CSVFileWriter csvFileWriter = new CSVFileWriter(new FileWriter(file));
            SQLiteDatabase db = dbHandler.getReadableDatabase();

            String queryPatient = "SELECT * FROM patients";
            Cursor cursor = db.rawQuery(queryPatient, null);
            csvFileWriter.writeNext(cursor.getColumnNames());  // write column names into excel sheet

            int patientColCount = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[patientColCount];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            String querySlide = "SELECT * FROM slides";
            cursor = db.rawQuery(querySlide, null);
            csvFileWriter.writeNext(cursor.getColumnNames());

            int slideColCount = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[slideColCount];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            String queryImage = "SELECT * FROM images";
            cursor = db.rawQuery(queryImage, null);
            csvFileWriter.writeNext(cursor.getColumnNames());

            int imageColCount = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[imageColCount];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            String queryImage_thick = "SELECT * FROM images_thick";
            cursor = db.rawQuery(queryImage_thick, null);
            csvFileWriter.writeNext(cursor.getColumnNames());

            int imageColCount_thick = cursor.getColumnCount();

            while (cursor.moveToNext()) {
                String arrStr[] = new String[imageColCount_thick];
                for (int i = 0; i < arrStr.length; i++) {
                    arrStr[i] = cursor.getString(i);
                }
                csvFileWriter.writeNext(arrStr);
            }

            csvFileWriter.close();
            cursor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
