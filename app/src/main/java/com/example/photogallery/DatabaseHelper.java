package com.example.photogallery;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "galleryDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_UPLOADS = "uploads";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_IMAGE_URL = "imageUrl";
    private static final String COLUMN_QUOTE = "quote";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_COUNTRY = "country";
    private static final String COLUMN_POPULATION = "population";
    private static final String COLUMN_IS_CAPITAL = "is_capital";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_UPLOADS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_QUOTE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_COUNTRY + " TEXT, " +
                    COLUMN_POPULATION + " INTEGER, " +
                    COLUMN_IS_CAPITAL + " INTEGER" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_UPLOADS);
        onCreate(db);
    }

    public long addUpload(Upload upload) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, upload.getName());
        values.put(COLUMN_IMAGE_URL, upload.getImageUrl());
        values.put(COLUMN_QUOTE, upload.getQuote());
        values.put(COLUMN_LOCATION, upload.getLocation());

        long id = db.insert(TABLE_UPLOADS, null, values);
        db.close();
        return id;
    }

    public int updateUpload(Upload upload) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, upload.getName());
        values.put(COLUMN_IMAGE_URL, upload.getImageUrl());
        values.put(COLUMN_QUOTE, upload.getQuote());
        values.put(COLUMN_LOCATION, upload.getLocation());

        return db.update(TABLE_UPLOADS, values, COLUMN_ID + " = ?", new String[] { String.valueOf(upload.getId()) });
    }

    public void deleteUpload(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_UPLOADS, COLUMN_ID + " = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public List<Upload> getAllUploads() {
        List<Upload> uploadList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_UPLOADS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        uploadList.clear();

        if (cursor.moveToFirst()) {
            do {
                Upload upload = new Upload();
                upload.setId(cursor.getInt(0));
                upload.setName(cursor.getString(1));
                upload.setImageUrl(cursor.getString(2));
                upload.setQuote(cursor.getString(3));
                upload.setLocation(cursor.getString(4));
                uploadList.add(upload);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return uploadList;
    }

    public Upload getUpload(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_UPLOADS, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_IMAGE_URL, COLUMN_QUOTE, COLUMN_LOCATION}, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            Upload upload = new Upload(
                    cursor.getString(1), // Name
                    cursor.getString(2), // Image URL
                    cursor.getString(3), // Quote
                    cursor.getString(4)  // Location
            );
            upload.setId(cursor.getInt(0)); // ID
            cursor.close();
            return upload;
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }
}
