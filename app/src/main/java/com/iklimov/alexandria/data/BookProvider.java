package com.iklimov.alexandria.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.iklimov.alexandria.data.AlexandriaContract;
import com.iklimov.alexandria.data.AlexandriaContract.Favorites;


/**
 * Created by saj on 24/12/14.
 */
public class BookProvider extends ContentProvider {

    private static final int FAVORITE = 100;
    private static final int FAVORITE_WITH_ID = 101;
    private static final UriMatcher uriMatcher = buildUriMatcher();

    private DbHelper dbHelper;
    private ContentResolver mContentResolver;


    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AlexandriaContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, AlexandriaContract.PATH_FAVORITES, FAVORITE);
        matcher.addURI(authority, AlexandriaContract.PATH_FAVORITES + "/#", FAVORITE_WITH_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        Context c = getContext();
        dbHelper = new DbHelper(c);
        mContentResolver = c.getContentResolver();
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case FAVORITE:
                cursor = dbHelper.getReadableDatabase().query(
                        Favorites.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case FAVORITE_WITH_ID:
                String id = Favorites.getIdFromUri(uri);
                cursor = dbHelper.getReadableDatabase().query(
                        Favorites.TABLE_NAME, projection, Favorites._ID + "=?", new String[]{id},
                        null, null, null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(mContentResolver, uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        Uri returnUri;
        long id;
        switch (uriMatcher.match(uri)) {
            case FAVORITE:
                id = db.insert(Favorites.TABLE_NAME, null, values);
                returnUri = Favorites.buildBookUri(id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (id != -1) mContentResolver.notifyChange(returnUri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int inserted = 0;
        switch (uriMatcher.match(uri)) {
            case FAVORITE:
                try {
                    db.beginTransaction();
                    for (ContentValues v : values) {
                        long i = db.insert(Favorites.TABLE_NAME, null, v);
                        if (i != -1) inserted++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (inserted != 0) mContentResolver.notifyChange(uri, null);
        return inserted;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deleted = 0;
        switch (uriMatcher.match(uri)) {
            case FAVORITE:
                deleted = db.delete(Favorites.TABLE_NAME, selection, selectionArgs);

                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (deleted != 0) mContentResolver.notifyChange(Favorites.CONTENT_URI, null);
        return deleted;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}