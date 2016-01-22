/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iklimov.alexandria.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.iklimov.alexandria.R;
import com.iklimov.alexandria.api.Book;
import com.iklimov.alexandria.data.AlexandriaContract;
import com.iklimov.alexandria.fragments.NoInternet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Utils class
 */
public class Utils {
    private static final String LOG_TAG = "Utils";

    private static final String ITEMS = "items";
    private static final String VOLUME_INFO = "volumeInfo";
    private static final String TITLE = "title";
    private static final String AUTHORS = "authors";
    private static final String DESC = "description";
    private static final String CATEGORIES = "categories";
    private static final String PAGES = "pageCount";
    private static final String RATING = "averageRating";
    private static final String IMG_URL_PATH = "imageLinks";
    private static final String IMG_URL = "thumbnail";
    private static final String SHARE_LINK = "infoLink";
    private static final String VOLUME_ID = "id";

    public static final String[] PROJECTION = {
            AlexandriaContract.Favorites._ID,
            AlexandriaContract.Favorites.COL_TITLE,
            AlexandriaContract.Favorites.COL_IMAGE_URL,
            AlexandriaContract.Favorites.COL_AUTHORS,
            AlexandriaContract.Favorites.COL_DESC,
            AlexandriaContract.Favorites.COL_CATEGORIES,
            AlexandriaContract.Favorites.COL_RATING,
            AlexandriaContract.Favorites.COL_PAGES,
            AlexandriaContract.Favorites.COL_SHARE_LINK,
            AlexandriaContract.Favorites.COL_VOLUME_ID
    };

    public static Uri saveToDb(ContentResolver contentResolver, Book b) {
        ContentValues values = new ContentValues();
        values.put(AlexandriaContract.Favorites.COL_TITLE, b.getTitle());
        values.put(AlexandriaContract.Favorites.COL_IMAGE_URL, b.getImageThumbnail());
        values.put(AlexandriaContract.Favorites.COL_AUTHORS, b.getAuthors());
        values.put(AlexandriaContract.Favorites.COL_DESC, b.getDescription());
        values.put(AlexandriaContract.Favorites.COL_PAGES, b.getPageCount());
        values.put(AlexandriaContract.Favorites.COL_RATING, b.getRating());
        values.put(AlexandriaContract.Favorites.COL_CATEGORIES, b.getCategories());
        values.put(AlexandriaContract.Favorites.COL_SHARE_LINK, b.getShareLink());
        values.put(AlexandriaContract.Favorites.COL_VOLUME_ID, b.getVolumeId());
        return contentResolver.insert(AlexandriaContract.Favorites.CONTENT_URI, values);
    }

    public static int deleteFromDb(ContentResolver contentResolver, Book book) {
        return contentResolver.delete(AlexandriaContract.Favorites.CONTENT_URI,
                AlexandriaContract.Favorites.COL_TITLE + "=?", new String[]{book.getTitle()});
    }

    private static void saveToTheCloud(Context context, Book book) {
        String token = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_token), "");
        new SaveToTheCloud().execute(book.getVolumeId(), token);
    }

    private static void deleteFromTheCloud(Context context, Book book) {
        String token = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.pref_token), "");
        new DeleteFromTheCloud().execute(book.getVolumeId(), token);
    }

    public static Uri addToFavorites(Context context, Book book) {
        Uri uri = saveToDb(context.getContentResolver(), book);
        saveToTheCloud(context, book);

        return uri;
    }

    public static int removeFromFavorites(Context context, Book book) {
        int deleted = deleteFromDb(context.getContentResolver(), book);
        deleteFromTheCloud(context, book);

        return deleted;
    }

    public static boolean isInternetAvailable(Context context) {
        ConnectivityManager systemService = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = systemService.getActiveNetworkInfo();
        boolean r = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        Log.d(LOG_TAG, "isInternetAvailable() returned: " + r);
        return r;
    }

    public static void noInternetMessage(AppCompatActivity activity, String tag) {
        final NoInternet noInternet = new NoInternet();
//        noInternet.setTarget(, 2);
        noInternet.show(activity.getSupportFragmentManager(), tag);
    }

    public static Book[] readJsonBook(String jsonBook) {
        try {
            JSONArray bookArray = new JSONObject(jsonBook).getJSONArray(ITEMS);
            int length = bookArray.length();
            Book[] res = new Book[length];

            for (int i = 0; i < length; i++) {
                JSONObject item = bookArray.getJSONObject(i);
                JSONObject bookInfo = item.getJSONObject(VOLUME_INFO);

                String title = bookInfo.getString(TITLE);
                String a = getAuthors(bookInfo);
                String desc = getDesc(bookInfo);
                String imgUrl = getImageUrl(bookInfo);
                float rating = getRating(bookInfo);
                String pageCount = getPageCount(bookInfo);
                String categories = getCategories(bookInfo);
                String shareLink = getShareLink(bookInfo);
                String volumeId = getVolumeId(item);
                res[i] = new Book(title, a, imgUrl, desc, rating, pageCount, categories, shareLink, volumeId);
            }
            return res;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
        return null;
    }

    private static String getVolumeId(JSONObject item) throws JSONException {
        return item.getString(VOLUME_ID);
    }

    private static String getShareLink(JSONObject accessInfo) throws JSONException {
        return accessInfo.getString(SHARE_LINK);
    }

    private static String getCategories(JSONObject bookInfo) throws JSONException {
        String categories;
        if (bookInfo.has(CATEGORIES)) {
            JSONArray a = bookInfo.getJSONArray(CATEGORIES);
            categories = "";
            int length = a.length() > 1 ? 2 : 1;
            for (int i = 0; i < length; i++) {
                categories = categories.concat(a.getString(i));
                if (i != 1 && length == 2) categories = categories.concat(", ");
            }
        } else {
            categories = "n/a";
        }

        return categories;
    }

    private static String getPageCount(JSONObject bookInfo) throws JSONException {
        String pageCount;
        if (bookInfo.has(PAGES)) pageCount = bookInfo.getString(PAGES);
        else pageCount = "n/a";

        return pageCount;
    }

    private static float getRating(JSONObject bookInfo) throws JSONException {
        float rating;
        if (bookInfo.has(RATING)) rating = Float.parseFloat(bookInfo.getString(RATING));
        else return 0f;
        return rating;
    }

    private static String getDesc(JSONObject bookInfo) throws JSONException {
        String desc = "";
        if (bookInfo.has(DESC)) desc = bookInfo.getString(DESC);
        return desc;
    }

    private static String getImageUrl(JSONObject bookInfo) throws JSONException {
        String url = "";
        if (bookInfo.has(IMG_URL_PATH)
                && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {

            url = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
        }
        return url;
    }

    private static String getAuthors(JSONObject bookInfo) throws JSONException {
        String a = "";
        if (bookInfo.has(AUTHORS)) {
            JSONArray authors = bookInfo.getJSONArray(AUTHORS);
            int l = authors.length() == 1 ? 1 : 2;
            for (int j = 0; j < l; j++) {
                a = a.concat(authors.getString(j));
                if (l == 2 && j == 0) a = a.concat(", ");
            }
        }
        return a;
    }

    public static boolean isFavorite(Context context, Book book) {
        boolean isFavorite = false;
        Cursor cursor = context.getContentResolver().query(AlexandriaContract.Favorites.CONTENT_URI,
                PROJECTION, AlexandriaContract.Favorites.COL_TITLE + "=?",
                new String[]{book.getTitle()}, null);
        if (cursor != null && cursor.getCount() > 0) {
            isFavorite = true;
            cursor.close();
        }
        Log.d(LOG_TAG, "isFavorite() returned: " + isFavorite);
        return isFavorite;
    }

    private static class SaveToTheCloud extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String volumeId = params[0];
            String token = params[1];

            Log.d(LOG_TAG, "doInBackground() called with: " + "params = [" + volumeId + "]");
            try {
                InputStream in;
                URL url = new URL("https://www.googleapis.com/books/v1/mylibrary/bookshelves/0/addVolume?volumeId=" + volumeId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                Log.d(LOG_TAG, "doInBackground: " + conn.getResponseCode());
                Log.d(LOG_TAG, "doInBackground: " + conn.getResponseMessage());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class DeleteFromTheCloud extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            String volumeId = params[0];
            String token = params[1];

            try {
                InputStream in;
                URL url = new URL("https://www.googleapis.com/books/v1/mylibrary/bookshelves/0/removeVolume?volumeId=" + volumeId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();

                Log.d(LOG_TAG, "doInBackground: " + conn.getResponseCode());
                Log.d(LOG_TAG, "doInBackground: " + conn.getResponseMessage());

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }


}