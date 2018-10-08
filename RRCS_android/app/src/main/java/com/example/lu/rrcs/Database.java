package com.example.lu.rrcs;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lu on 2018/5/17.
 */

public class Database extends SQLiteOpenHelper{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Database2";
    private static final String TABLE_NAME = "Database_Table";

    private static final String KEY_ID = "id";
    private static final String NAME = "name";
    private static final String KIND = "kind";
    private static final String TEMP_LOW = "temp_low";
    private static final String TEMP_HIGH = "temp_high";
    private static final String HUM_LOW = "hum_low";
    private static final String HUM_HIGH = "hum_high";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_NAME + "(" +
                KEY_ID + " INTEGER PRIMARY KEY," +
                NAME + " TEXT,"+
                KIND +" TEXT,"+
                TEMP_LOW + " INTEGER,"+
                TEMP_HIGH + " INTEGER,"+
                HUM_LOW + " INTEGER,"+
                HUM_HIGH + " INTEGER)";

        sqLiteDatabase.execSQL(CREATE_CATEGORIES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

//        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_TABELS);
//        onCreate(sqLiteDatabase);
    }

    //新增
    public void insertName(String name,String kind,String temp_low,String temp_high,String hum_low,String hum_high){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv_name = new ContentValues();

        cv_name.put(NAME, name);
        cv_name.put(KIND, kind);
        cv_name.put(TEMP_LOW, temp_low);
        cv_name.put(TEMP_HIGH, temp_high);
        cv_name.put(HUM_LOW, hum_low);
        cv_name.put(HUM_HIGH, hum_high);

        db.insert(TABLE_NAME, null ,cv_name);
        db.close();
    }



    public List<String> getAllLabels(){

        List<String> labels = new ArrayList<String>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                labels.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return labels;
    }
    public String getKind(String name){

        String kind = null;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE TRIM(name) = '"+name.trim()+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                kind = cursor.getString(2);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return kind;
    }

    public String getTempLow(String name){

       String temp_low = null;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE TRIM(name) = '"+name.trim()+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                temp_low = cursor.getString(3);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return temp_low;
    }

    public String getTempHigh(String name){

        String temp_high = null;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE TRIM(name) = '"+name.trim()+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                temp_high = cursor.getString(4);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return temp_high;
    }

    public String getHumLow(String name){

        String hum_low = null;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE TRIM(name) = '"+name.trim()+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                hum_low = cursor.getString(5);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return hum_low;
    }

    public String getHumHigh(String name){

        String hum_high = null;

        String selectQuery = "SELECT  * FROM " + TABLE_NAME + " WHERE TRIM(name) = '"+name.trim()+"'";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                hum_high = cursor.getString(6);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return hum_high;
    }

}
