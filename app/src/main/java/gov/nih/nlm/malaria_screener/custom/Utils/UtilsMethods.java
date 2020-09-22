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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import gov.nih.nlm.malaria_screener.database.CSVFileWriter;
import gov.nih.nlm.malaria_screener.database.MyDBHandler;

public class UtilsMethods {

    private final static String LOCAL_DATA_DIR = "/NLM_Malaria_Screener/";

    public static void exportDB(Context context) {

        MyDBHandler dbHandler = new MyDBHandler(context, null, null, 1);

        File exportDir = new File(Environment.getExternalStorageDirectory(), LOCAL_DATA_DIR);

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
