package com.iklimov.alexandria.data;


import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Book Contract
 */
public class AlexandriaContract {

    public static final String CONTENT_AUTHORITY = "com.iklimov.alexandria";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    public static final class Favorites implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COL_TITLE = "title";
        public static final String COL_IMAGE_URL = "imgurl";
        public static final String COL_DESC = "description";
        public static final String COL_AUTHORS = "authors";
        public static final String COL_PAGES = "pages";
        public static final String COL_CATEGORIES = "categories";
        public static final String COL_RATING = "rating";
        public static final String COL_SHARE_LINK = "link";
        public static final String COL_VOLUME_ID = "volume_id";


        public static Uri buildBookUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static final String SQL_CREATE_FAVORITES_TABLE =
                "CREATE TABLE " + AlexandriaContract.Favorites.TABLE_NAME + " (" +
                        Favorites._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                        Favorites.COL_TITLE + " TEXT NOT NULL," +
                        Favorites.COL_IMAGE_URL + " TEXT ," +
                        Favorites.COL_DESC + " TEXT ," +
                        Favorites.COL_RATING + " TEXT ," +
                        Favorites.COL_CATEGORIES + " TEXT ," +
                        Favorites.COL_PAGES + " TEXT ," +
                        Favorites.COL_SHARE_LINK + " TEXT ," +
                        Favorites.COL_AUTHORS + " TEXT , "+
                        Favorites.COL_VOLUME_ID + " TEXT );";

        public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME + ";";
    }
}
