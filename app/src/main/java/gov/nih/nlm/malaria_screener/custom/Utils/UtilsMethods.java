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

package gov.nih.nlm.malaria_screener.custom.Utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gov.nih.nlm.malaria_screener.database.CSVFileWriter;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

public class UtilsMethods {

    private final static String LOCAL_DATA_DIR = "/NLM_Malaria_Screener/";

    /*
    *   This function export database to a .csv file.
    *   @param context: context of the caller activity
    *   @return
    * */
    public static void exportDB(Context context) {

        MyDBHandler dbHandler = new MyDBHandler(context, null, null, 1);

        File exportDir = new File(Environment.getExternalStorageDirectory(), LOCAL_DATA_DIR);

        File file = new File(exportDir, "MalariaScreenerDB.csv");

        try {
            file.createNewFile();
            CSVFileWriter csvFileWriter = new CSVFileWriter(new FileWriter(file));
            SQLiteDatabase db = dbHandler.getReadableDatabase();

            String queryTables = "SELECT name FROM sqlite_master WHERE type ='table'";
            Cursor cursor_table = db.rawQuery(queryTables, null);
            while (cursor_table.moveToNext()) {

                String table_name = cursor_table.getString(0);

                if (table_name.equals("android_metadata") || table_name.equals("sqlite_sequence")){
                    continue;
                }

                String query = "SELECT * FROM " + table_name;

                Cursor cursor = db.rawQuery(query, null);
                csvFileWriter.writeNext(cursor.getColumnNames());  // write column names into excel sheet

                int colCount = cursor.getColumnCount();

                while (cursor.moveToNext()) {
                    String arrStr[] = new String[colCount];
                    for (int i = 0; i < arrStr.length; i++) {
                        arrStr[i] = cursor.getString(i);
                    }
                    csvFileWriter.writeNext(arrStr);
                }

            }

            csvFileWriter.close();
            //cursor.close();

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}
