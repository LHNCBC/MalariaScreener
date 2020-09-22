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

package gov.nih.nlm.malaria_screener.database;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.HashMap;

/**
 * Created by yuh5 on 4/20/2016.
 */
public class MyDBHandler extends SQLiteOpenHelper {

    private static final String TAG = "MyDebug";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "patients.db";

    // columns of patient table
    public static final String TABLE_PATIENTS = "patients";
    public static final String COLUMN_AUTO_ID = "autoID"; // think about how to change this without breaking the current code since this identifier maybe used somewhere for query
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_GENDER = "gender";
    public static final String COLUMN_INITIAL = "initial";
    public static final String COLUMN_AGE = "age";

    // columns of slide table
    public static final String TABLE_SLIDES = "slides";
    public static final String COLUMN_PATIENT_ID = "patient_id";
    public static final String COLUMN_SLIDE_ID = "slideID";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_SITE = "site";
    public static final String COLUMN_PREPARATOR = "preparator";
    public static final String COLUMN_OPERATOR = "operator";
    public static final String COLUMN_STAINING = "stainingMethod";
    public static final String COLUMN_HCT = "hct";
    public static final String COLUMN_PARASITEMIA_THIN = "parasitemia_thin";
    public static final String COLUMN_PARASITEMIA_THICK = "parasitemia_thick";

    // columns of image table (thin smear)
    public static final String TABLE_IMAGES = "images";
    public static final String COLUMN_IMAGE_PATIENT_ID = "patient_id";
    public static final String COLUMN_IMAGE_SLIDE_ID = "slide_id";
    public static final String COLUMN_IMAGE_ID = "image_id";
    public static final String COLUMN_CELL_COUNT = "cell_count";
    public static final String COLUMN_INFECTED_COUNT = "infected_count";
    public static final String COLUMN_CELL_COUNT_GT = "cell_count_gt";
    public static final String COLUMN_INFECTED_COUNT_GT = "infected_count_gt";

    // columns of image table (thick smear)
    public static final String TABLE_IMAGES_THICK = "images_thick";
    public static final String COLUMN_IMAGE_PATIENT_ID_THICK = "patient_id_thick";
    public static final String COLUMN_IMAGE_SLIDE_ID_THICK = "slide_id_thick";
    public static final String COLUMN_IMAGE_ID_THICK = "image_id_thick";
    public static final String COLUMN_PARASITE_COUNT = "parasite_count";
    public static final String COLUMN_WBC_COUNT = "wbc_count";
    public static final String COLUMN_PARASITE_COUNT_GT = "parasite_count_gt";
    public static final String COLUMN_WBC_COUNT_GT = "wbc_count_gt";

    private Context context;

    private HashMap<String, String> mAliasMap;

    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);

        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<String, String>();

        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", COLUMN_ID + " as " + "_id" );

        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, COLUMN_INITIAL + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);

        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_2, COLUMN_GENDER + " as " + SearchManager.SUGGEST_COLUMN_TEXT_2);

        this.context = context;

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create patient table
        String createTable = " CREATE TABLE " + TABLE_PATIENTS + "(" +
                COLUMN_AUTO_ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_ID + " TEXT, " +
                COLUMN_GENDER + " TEXT, " +
                COLUMN_INITIAL + " TEXT, " +
                COLUMN_AGE + " TEXT " +
                ");";

        db.execSQL(createTable);

        // create slide table
        String createSlideTable = " CREATE TABLE " + TABLE_SLIDES + "(" +
                COLUMN_PATIENT_ID + " TEXT NOT NULL, " +
                COLUMN_SLIDE_ID + " TEXT NOT NULL, " +
                COLUMN_DATE + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_SITE + " TEXT, " +
                COLUMN_PREPARATOR + " TEXT, " +
                COLUMN_OPERATOR + " TEXT, " +
                COLUMN_STAINING + " TEXT, " +
                COLUMN_HCT + " TEXT, " +
                COLUMN_PARASITEMIA_THIN + " TEXT, " +
                COLUMN_PARASITEMIA_THICK + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_PATIENT_ID + ", " + COLUMN_SLIDE_ID + ")" +
                ");";

        db.execSQL(createSlideTable);

        // create image table (thin smear)
        String createImageTable = " CREATE TABLE " + TABLE_IMAGES + "(" +
                COLUMN_IMAGE_PATIENT_ID + " TEXT NOT NULL, " +
                COLUMN_IMAGE_SLIDE_ID + " TEXT NOT NULL, " +
                COLUMN_IMAGE_ID + " TEXT NOT NULL, " +
                COLUMN_CELL_COUNT + " TEXT, " +
                COLUMN_INFECTED_COUNT + " TEXT, " +
                COLUMN_CELL_COUNT_GT + " TEXT, " +
                COLUMN_INFECTED_COUNT_GT + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_IMAGE_PATIENT_ID + ", " + COLUMN_IMAGE_SLIDE_ID + ", " + COLUMN_IMAGE_ID + ")" +
                ");";

        db.execSQL(createImageTable);

        // create image table (thick smear)
        String createImageTable_thick = " CREATE TABLE " + TABLE_IMAGES_THICK + "(" +
                COLUMN_IMAGE_PATIENT_ID_THICK + " TEXT NOT NULL, " +
                COLUMN_IMAGE_SLIDE_ID_THICK + " TEXT NOT NULL, " +
                COLUMN_IMAGE_ID_THICK + " TEXT NOT NULL, " +
                COLUMN_PARASITE_COUNT + " TEXT, " +
                COLUMN_WBC_COUNT + " TEXT, " +
                COLUMN_PARASITE_COUNT_GT + " TEXT, " +
                COLUMN_WBC_COUNT_GT + " TEXT, " +
                "PRIMARY KEY (" + COLUMN_IMAGE_PATIENT_ID_THICK + ", " + COLUMN_IMAGE_SLIDE_ID_THICK + ", " + COLUMN_IMAGE_ID_THICK + ")" +
                ");";

        db.execSQL(createImageTable_thick);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {



        /*db.execSQL(" DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_SLIDES);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_IMAGES);
        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_IMAGES_THICK);
        onCreate(db);*/
    }

    // Add a new row to patient table
    public void addPatient(Patients patients){
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, patients.get_id());
        values.put(COLUMN_GENDER, patients.get_gender());
        values.put(COLUMN_INITIAL, patients.get_initial());
        values.put(COLUMN_AGE, patients.get_age());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_PATIENTS, null, values);
        db.close();
    }

    // Add a new row to slide table
    public void addSlide(Slides slides){
        ContentValues values = new ContentValues();
        values.put(COLUMN_PATIENT_ID, slides.get_idPatient());
        values.put(COLUMN_SLIDE_ID, slides.get_idSlide());
        values.put(COLUMN_DATE, slides.get_date());
        values.put(COLUMN_TIME, slides.get_time());
        values.put(COLUMN_SITE, slides.get_site());
        values.put(COLUMN_PREPARATOR, slides.get_preparator());
        values.put(COLUMN_OPERATOR, slides.get_operator());
        values.put(COLUMN_STAINING, slides.get_staining());
        values.put(COLUMN_HCT, slides.get_hct());
        values.put(COLUMN_PARASITEMIA_THIN, slides.get_parasitemia());
        values.put(COLUMN_PARASITEMIA_THICK, slides.get_parasitemia_thick());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_SLIDES, null, values);
        db.close();
    }

    // Add a new row to image table (thin smear)
    public void addImage(Images images){
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_PATIENT_ID, images.get_idPatient());
        values.put(COLUMN_IMAGE_SLIDE_ID, images.get_idSlide());
        values.put(COLUMN_IMAGE_ID, images.get_idImage());
        values.put(COLUMN_CELL_COUNT, images.get_cellCount());
        values.put(COLUMN_INFECTED_COUNT, images.get_infectedCount());
        values.put(COLUMN_CELL_COUNT_GT, images.get_cellCountGT());
        values.put(COLUMN_INFECTED_COUNT_GT, images.get_infectedCountGT());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_IMAGES, null, values);
        db.close();
    }

    // Add a new row to image table (thin smear)
    public void addImage_thick(Images_thick images){
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_PATIENT_ID_THICK, images.get_idPatient());
        values.put(COLUMN_IMAGE_SLIDE_ID_THICK, images.get_idSlide());
        values.put(COLUMN_IMAGE_ID_THICK, images.get_idImage());
        values.put(COLUMN_PARASITE_COUNT, images.get_parasiteCount());
        values.put(COLUMN_WBC_COUNT, images.get_wbcCount());
        values.put(COLUMN_PARASITE_COUNT_GT, images.get_parasiteCountGT());
        values.put(COLUMN_WBC_COUNT_GT, images.get_wbcCountGT());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_IMAGES_THICK, null, values);
        db.close();
    }

    // add results of slides into database
//    public void addSlideResults(String id, String totalCell, String infectedCell, String parasitemia){
//        SQLiteDatabase db = getWritableDatabase();
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(COLUMN_TOTAL_CELL, totalCell);
//        contentValues.put(COLUMN_INFECTED_CELL, infectedCell);
//        contentValues.put(COLUMN_PARASITEMIA_THIN, parasitemia);
//
//        db.update(TABLE_SLIDES, contentValues, "slideID = ?", new String[]{id});
//    }

    /** Returns patients for suggestions */
    public Cursor getPatients(String[] selectionArgs){

        String selection = COLUMN_INITIAL + " like ? ";

        if(selectionArgs!=null){
            selectionArgs[0] = "%"+selectionArgs[0] + "%";
        }

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);

        queryBuilder.setTables(TABLE_PATIENTS);

        Cursor c = queryBuilder.query(getReadableDatabase(),
                new String[] { "_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1,
                        SearchManager.SUGGEST_COLUMN_TEXT_2 } ,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_INITIAL + " asc ","10"
        );
        return c;
    }

    /** Return patient when suggestion selected corresponding to the id */
    public Cursor getPatient(String id){

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(TABLE_PATIENTS);

        Cursor c = queryBuilder.query(getReadableDatabase(),
                new String[] { "_id", "initial", "gender", "age" } ,
                "_id = ?", new String[] { id } , null, null, null ,"1"
        );

        return c;
    }

    public Cursor findPatient(String string){
        SQLiteDatabase db = getWritableDatabase();
        String queryID = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = \"" + string + "\"";
        Cursor cursor = db.rawQuery(queryID, null);

        return cursor;
    }

    public Cursor returnPatients(){

        SQLiteDatabase db = getWritableDatabase();
        String queryID = "SELECT * FROM " + TABLE_PATIENTS;
        Cursor cursorPatient = db.rawQuery(queryID, null);

        cursorPatient.moveToFirst();

        return cursorPatient;
    }

    // return all slides when selected from listView in database patient page
    public Cursor returnPatientSlides(String PIDStr){

        SQLiteDatabase db = getWritableDatabase();
        String queryID = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = \"" + PIDStr + "\"";
        Cursor cursor = db.rawQuery(queryID, null);

        cursor.moveToFirst();
        String pIDStr = cursor.getString(cursor.getColumnIndex("_id"));
        String querySlide = "SELECT * FROM " + TABLE_SLIDES + " WHERE " + COLUMN_PATIENT_ID + " = \"" + pIDStr + "\"";
        Cursor cursorSlide = db.rawQuery(querySlide, null);
        cursorSlide.moveToFirst();

        return cursorSlide;
    }

    // return all images of a patient from image table thin
    public Cursor returnAllPatientImages(String PIDStr){

        SQLiteDatabase db = getWritableDatabase();

        String queryImage = "SELECT * FROM " + TABLE_IMAGES + " WHERE " + COLUMN_IMAGE_PATIENT_ID + " = \"" + PIDStr + "\"";
        Cursor cursorImages = db.rawQuery(queryImage, null);
        cursorImages.moveToFirst();

        return cursorImages;
    }

    // return all images of a slide from image table thick
    public Cursor returnAllPatientImages_thick(String PIDStr){

        SQLiteDatabase db = getWritableDatabase();

        String queryImage = "SELECT * FROM " + TABLE_IMAGES_THICK + " WHERE " + COLUMN_IMAGE_PATIENT_ID_THICK + " = \"" + PIDStr + "\"";
        Cursor cursorImages = db.rawQuery(queryImage, null);
        cursorImages.moveToFirst();

        return cursorImages;
    }

    // return slide info when selected from listView in database slide log page
    public Cursor returnSlideCursor(String patientIDStr, String slideIDStr){

        SQLiteDatabase db = getWritableDatabase();

        String querySlide = "SELECT * FROM " + TABLE_SLIDES + " WHERE " + COLUMN_PATIENT_ID + " = \"" + patientIDStr + "\"" + " AND " + COLUMN_SLIDE_ID + " = \"" + slideIDStr + "\"";
        Cursor cursorSlide = db.rawQuery(querySlide, null);
        cursorSlide.moveToFirst();

        return cursorSlide;

    }

    // return image from image table
    public Cursor returnSlideImage(String PIDStr, String SIDStr, String imageName){

        SQLiteDatabase db = getWritableDatabase();

        String queryImage = "SELECT * FROM " + TABLE_IMAGES + " WHERE " + COLUMN_IMAGE_PATIENT_ID + " = \"" + PIDStr + "\"" + " AND " + COLUMN_IMAGE_SLIDE_ID + " = \"" + SIDStr + "\"" + " AND " + COLUMN_IMAGE_ID + " = \"" + imageName + "\"";
        Cursor cursorImage = db.rawQuery(queryImage, null);
        cursorImage.moveToFirst();

        return cursorImage;
    }

    // return image from image table
    public Cursor returnSlideImage_thick(String PIDStr, String SIDStr, String imageName){

        SQLiteDatabase db = getWritableDatabase();

        String queryImage = "SELECT * FROM " + TABLE_IMAGES_THICK + " WHERE " + COLUMN_IMAGE_PATIENT_ID_THICK + " = \"" + PIDStr + "\"" + " AND " + COLUMN_IMAGE_SLIDE_ID_THICK + " = \"" + SIDStr + "\"" + " AND " + COLUMN_IMAGE_ID_THICK + " = \"" + imageName + "\"";
        Cursor cursorImage = db.rawQuery(queryImage, null);
        cursorImage.moveToFirst();

        return cursorImage;
    }

    // return all images of a slide from image table thin
    public Cursor returnAllSlideImages(String PIDStr, String SIDStr){

        SQLiteDatabase db = getWritableDatabase();

        String queryImage = "SELECT * FROM " + TABLE_IMAGES + " WHERE " + COLUMN_IMAGE_PATIENT_ID + " = \"" + PIDStr + "\"" + " AND " + COLUMN_IMAGE_SLIDE_ID + " = \"" + SIDStr + "\"";
        Cursor cursorImages = db.rawQuery(queryImage, null);
        cursorImages.moveToFirst();

        return cursorImages;
    }

    // return all images of a slide from image table thick
    public Cursor returnAllSlideImages_thick(String PIDStr, String SIDStr){

        SQLiteDatabase db = getWritableDatabase();

        String queryImage = "SELECT * FROM " + TABLE_IMAGES_THICK + " WHERE " + COLUMN_IMAGE_PATIENT_ID_THICK + " = \"" + PIDStr + "\"" + " AND " + COLUMN_IMAGE_SLIDE_ID_THICK + " = \"" + SIDStr + "\"";
        Cursor cursorImages = db.rawQuery(queryImage, null);
        cursorImages.moveToFirst();

        return cursorImages;
    }

    public boolean checkExist_Patient(String string){
        SQLiteDatabase db = getWritableDatabase();
        String queryID = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = \"" + string + "\"";
        Cursor cursor = db.rawQuery(queryID, null);

        if (cursor.moveToFirst()){
            return true;
        } else {
            return false;
        }

    }

    public boolean checkIfSame_Patient(String ID_string, String gender_string, String initial_string, String age_string){
        SQLiteDatabase db = getWritableDatabase();
        String queryID = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = \"" + ID_string + "\"";
        Cursor cursor = db.rawQuery(queryID, null);

        cursor.moveToFirst();

        String genderStr = cursor.getString(cursor.getColumnIndex("gender"));
        String initialStr = cursor.getString(cursor.getColumnIndex("initial"));
        String ageStr = cursor.getString(cursor.getColumnIndex("age"));

        if (gender_string.equals(genderStr) && initial_string.equals(initialStr) && age_string.equals(ageStr)){
            return true;
        } else{
            return false;
        }

    }

    public boolean checkExist_Slide(String pIDString, String sIDString){
        SQLiteDatabase db = getWritableDatabase();
        String queryID = "SELECT * FROM " + TABLE_SLIDES + " WHERE " + COLUMN_PATIENT_ID + " = \"" + pIDString + "\"" + " AND " + COLUMN_SLIDE_ID + " = \"" + sIDString + "\"";
        Cursor cursor = db.rawQuery(queryID, null);

        if (cursor.moveToFirst()){
            return true;
        } else {
            return false;
        }

    }

    //Delete a patient and all his/her slides from the database
    public void deletePatient(String condition){
        SQLiteDatabase db = getWritableDatabase();

        String queryID = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + " = \"" + condition + "\"";
        Cursor cursor = db.rawQuery(queryID, null);
        cursor.moveToFirst();

        String pIDStr = cursor.getString(cursor.getColumnIndex("_id"));
        db.execSQL(" DELETE FROM " + TABLE_PATIENTS + " WHERE " + COLUMN_ID + "='" + condition + "'; ");
        db.execSQL(" DELETE FROM " + TABLE_SLIDES + " WHERE " + COLUMN_PATIENT_ID + "='" + pIDStr + "'; " );
        db.execSQL(" DELETE FROM " + TABLE_IMAGES + " WHERE " + COLUMN_IMAGE_PATIENT_ID + "='" + pIDStr + "'; " );
        db.execSQL(" DELETE FROM " + TABLE_IMAGES_THICK + " WHERE " + COLUMN_IMAGE_PATIENT_ID_THICK + "='" + pIDStr + "'; " );
    }

    //Delete one slide of a patient from the database
    public void deleteSlide(String sIDStr, String pIDStr) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(" DELETE FROM " + TABLE_SLIDES + " WHERE " + COLUMN_PATIENT_ID + "='" + pIDStr + "' " + " AND " + COLUMN_SLIDE_ID + " = \"" + sIDStr + "\"");
        db.execSQL(" DELETE FROM " + TABLE_IMAGES + " WHERE " + COLUMN_IMAGE_PATIENT_ID + "='" + pIDStr + "' " + " AND " + COLUMN_IMAGE_SLIDE_ID + " = \"" + sIDStr + "\"");
    }

    public void deleteSlide_thick(String sIDStr, String pIDStr) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL(" DELETE FROM " + TABLE_SLIDES + " WHERE " + COLUMN_PATIENT_ID + "='" + pIDStr + "' " + " AND " + COLUMN_SLIDE_ID + " = \"" + sIDStr + "\"");
        db.execSQL(" DELETE FROM " + TABLE_IMAGES_THICK + " WHERE " + COLUMN_IMAGE_PATIENT_ID_THICK + "='" + pIDStr + "' " + " AND " + COLUMN_IMAGE_SLIDE_ID_THICK + " = \"" + sIDStr + "\"");
    }

    //Delete all images after slide deleted
    public void deleteImages(String sIDStr, String pIDStr) {


    }

    //Delete entire table
    public void deleteTable(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(" DELETE FROM " + TABLE_PATIENTS);
        db.execSQL(" DELETE FROM SQLITE_SEQUENCE WHERE NAME='" + TABLE_PATIENTS + "'; " );

        db.execSQL(" DELETE FROM " + TABLE_SLIDES);
        db.execSQL(" DELETE FROM SQLITE_SEQUENCE WHERE NAME='" + TABLE_SLIDES + "'; " );

        db.execSQL(" DELETE FROM " + TABLE_IMAGES);
        db.execSQL(" DELETE FROM SQLITE_SEQUENCE WHERE NAME='" + TABLE_IMAGES + "'; " );

        db.execSQL(" DELETE FROM " + TABLE_IMAGES_THICK);
        db.execSQL(" DELETE FROM SQLITE_SEQUENCE WHERE NAME='" + TABLE_IMAGES_THICK + "'; " );

    }

    //Print out database
    public String[][] databaseToString(){

        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE 1 ";

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);

        int dbLength = c.getCount();
        String[][] dbString = new String[dbLength][4];

        //Move to the first row in your results
        c.moveToFirst();

        int i = 0;

        while (!c.isAfterLast()){
            if(c.getString(c.getColumnIndex("initial")) != null){

                dbString[i][0] = c.getString(c.getColumnIndex("_id"));
                dbString[i][1] = c.getString(c.getColumnIndex("initial"));
                dbString[i][2] = c.getString(c.getColumnIndex("gender"));
                dbString[i][3] = c.getString(c.getColumnIndex("age"));

                c.moveToNext();
                i++;
            }
        }

        db.close();
        return dbString;
    }

    // search if there is slide hasn't been uploaded
    public boolean searchNotUploadedSlides(){

        SQLiteDatabase db = getWritableDatabase();

        String query = "SELECT * FROM " + TABLE_SLIDES + " WHERE 1 ";

        // Cursor point to a location in your results
        Cursor c = db.rawQuery(query, null);

        //Move to the first row in your results
        c.moveToFirst();

        while (!c.isAfterLast()){
            String temp = c.getString(c.getColumnIndex("uploaded"));
            if (Integer.parseInt(temp) == 0) {
                return true;
            }
            c.moveToNext();
        }

        return false;

    }

    public int getTableSize(){

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE 1 ";
        Cursor c = db.rawQuery(query, null);

        return c.getCount();
    }

    public void updateImageManulCounts(String PID, String SID, String ImageID, String cellCountGT, String infectedCountGT){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_CELL_COUNT_GT, cellCountGT);
        contentValues.put(COLUMN_INFECTED_COUNT_GT, infectedCountGT);

        String where = COLUMN_IMAGE_PATIENT_ID + "='" + PID + "' " + " AND " + COLUMN_IMAGE_SLIDE_ID + " = '" + SID + "'" + " AND " + COLUMN_IMAGE_ID + " = '" + ImageID + "'";

        db.update(TABLE_IMAGES, contentValues, where, null);

    }

    public void updateImageManulCounts_thick(String PID, String SID, String ImageID, String wbcCountGT, String parasiteCountGT){

        SQLiteDatabase db = getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_WBC_COUNT_GT, wbcCountGT);
        contentValues.put(COLUMN_PARASITE_COUNT_GT, parasiteCountGT);

        String where = COLUMN_IMAGE_PATIENT_ID_THICK + "='" + PID + "' " + " AND " + COLUMN_IMAGE_SLIDE_ID_THICK + " = '" + SID + "'" + " AND " + COLUMN_IMAGE_ID_THICK + " = '" + ImageID + "'";

        db.update(TABLE_IMAGES_THICK, contentValues, where, null);

    }

    public int checkNumberOfSlides(){

        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_SLIDES + " WHERE 1 ";
        Cursor c = db.rawQuery(query, null);

        return c.getCount();
    }


}
