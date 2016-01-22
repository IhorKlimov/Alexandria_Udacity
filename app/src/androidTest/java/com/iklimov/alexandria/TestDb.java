package com.iklimov.alexandria;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;
import android.util.Log;

import com.iklimov.alexandria.data.AlexandriaContract.Favorites;


/**
 * Test class for my Data Base
 */
public class TestDb extends AndroidTestCase {
    private static final String LOG_TAG = "TestDb";

    public final static String title = "Artificial Intelligence";
    public final static String subtitle = "A Modern Approach";
    public final static String imgUrl = "http://books.google.com/books/content?id=KI2WQgAACAAJ&printsec=frontcover&img=1&zoom=1";
    public final static String desc = "Presents a guide to artificial intelligence, covering such topics as intelligent agents, problem-solving, logical agents, planning, uncertainty, learning, and robotics.";
    public final static String author = "Stuart Jonathan Russell";
    public final static String category = "Computers";

    private final String[] mProjection = {
            Favorites.COL_TITLE, Favorites.COL_IMAGE_URL, Favorites.COL_AUTHORS, Favorites.COL_DESC
    };

    /*public void testInsert() {
        getContext().deleteDatabase(DbHelper.DATABASE_NAME);
        ContentResolver contentResolver = getContext().getContentResolver();

        ContentValues values = new ContentValues();
        values.put(Favorites.COL_TITLE, title);
        values.put(Favorites.COL_IMAGE_URL, imgUrl);
        values.put(Favorites.COL_DESC, desc);
        values.put(Favorites.COL_AUTHORS, author);

        Uri insert = contentResolver.insert(Favorites.CONTENT_URI, values);
        assertTrue(insert != null);
        Log.d(LOG_TAG, "testInsert: " + insert.toString());
    }*/

    /*public void testQuery() {
        ContentResolver contentResolver = getContext().getContentResolver();

        Cursor cursor = contentResolver.query(Favorites.CONTENT_URI, mProjection, null, null, null);
        assertTrue(cursor != null);
        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == 1);
        Log.d(LOG_TAG, "testQuery: "+ cursor.getString(0));
        Log.d(LOG_TAG, "testQuery: "+ cursor.getString(1));
        Log.d(LOG_TAG, "testQuery: "+ cursor.getString(2));
        Log.d(LOG_TAG, "testQuery: "+ cursor.getString(3));

        cursor.close();
    }*/

    public void testBulkInsert() {
        ContentResolver contentResolver = getContext().getContentResolver();

        ContentValues v1 = new ContentValues();
        v1.put(Favorites.COL_TITLE, title);
        v1.put(Favorites.COL_IMAGE_URL, imgUrl);
        v1.put(Favorites.COL_DESC, desc);
        v1.put(Favorites.COL_AUTHORS, author);

        ContentValues v2 = new ContentValues();
        v2.put(Favorites.COL_TITLE, "Second Title");
        v2.put(Favorites.COL_IMAGE_URL, "Second Imagee Url");
        v2.put(Favorites.COL_DESC, "Second Desc");
        v2.put(Favorites.COL_AUTHORS, "Second Author");

        ContentValues[] values = {v1, v2};
        int bulkInsert = contentResolver.bulkInsert(Favorites.CONTENT_URI, values);
        assertTrue(bulkInsert == 2);

        Cursor cursor = contentResolver.query(Favorites.CONTENT_URI, mProjection, null, null, null);
        assertTrue(cursor != null);
        assertTrue(cursor.moveToFirst());
        assertTrue(cursor.getCount() == 2);

        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(0));
        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(1));
        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(2));
        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(3));

        assertTrue(cursor.moveToNext());

        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(0));
        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(1));
        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(2));
        Log.d(LOG_TAG, "testBulkInsert: " + cursor.getString(3));

    }

    /*public void testDelete() {
        ContentResolver contentResolver = getContext().getContentResolver();

        int delete = contentResolver.delete(Favorites.CONTENT_URI, null, null);
        assertTrue(delete == 1);

        Cursor cursor = contentResolver.query(Favorites.CONTENT_URI, mProjection, null, null, null);
        assertTrue(cursor != null);
        assertTrue(cursor.getCount() == 0);
        cursor.close();
    }*/


}