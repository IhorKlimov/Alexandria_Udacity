package com.iklimov.alexandria;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.iklimov.alexandria.api.Book;
import com.iklimov.alexandria.api.BookResultAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class AddBook extends Fragment {
    private static final String LOG_TAG = "AddBook";

    private Context mContext;
    private EditText mEan;
    private final String EAN_CONTENT = "eanContent";
    private ArrayList<Book> mSearchResults;
    private RecyclerView mResultsView;
    private boolean mRunning;
    private ImageButton scanBtn;

    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mEan != null) {
            outState.putString(EAN_CONTENT, mEan.getText().toString());
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEan = (EditText) rootView.findViewById(R.id.ean);
        mResultsView = (RecyclerView) rootView.findViewById(R.id.results);
        mContext = getContext();

        mSearchResults = new ArrayList<>();
        mResultsView.setAdapter(new BookResultAdapter(mSearchResults, mContext));
        mResultsView.setLayoutManager(new LinearLayoutManager(mContext));

        scanBtn = (ImageButton) rootView.findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = mEan.getText().toString();
                if (isInternetAvailable()) {
                    mSearchResults.clear();
                    if (!mRunning) {
                        scanBtn.setImageResource(R.drawable.ic_cached_24dp);
                        Animation rotation = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
                        rotation.setRepeatCount(Animation.INFINITE);
                        scanBtn.startAnimation(rotation);
                        mRunning = true;
                        new Search().execute(query);
                    } else {
                        scanBtn.clearAnimation();
                        scanBtn.setImageResource(R.drawable.ic_search_24dp);
                    }
                } else {
                    Toast.makeText(mContext, "Please check your Internet connection",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (savedInstanceState != null) {
            mEan.setText(savedInstanceState.getString(EAN_CONTENT));
            mEan.setHint("");
        }

        return rootView;
    }

    private class Search extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... params) {
            search(params[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mResultsView.getAdapter().notifyDataSetChanged();
            scanBtn.clearAnimation();
            scanBtn.setImageResource(R.drawable.ic_search_24dp);
            mRunning = false;
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager service = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = service.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void search(String query) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter("q", "intitle:" + query)
                    .appendQueryParameter("maxResults", "40")
                    .build();

            URL url = new URL(builtUri.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) return;
            Log.i(LOG_TAG, "search: " + url.toString());
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (buffer.length() == 0) return;
            bookJsonString = buffer.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        readJsonBook(bookJsonString);
    }

    private void readJsonBook(String jsonBook) {
        final String ITEMS = "items";
        final String VOLUME_INFO = "volumeInfo";
        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        try {
            JSONArray bookArray = new JSONObject(jsonBook).getJSONArray(ITEMS);

            for (int i = 0; i < bookArray.length(); i++) {
                JSONObject bookInfo = bookArray.getJSONObject(i).getJSONObject(VOLUME_INFO);

                String title = bookInfo.getString(TITLE);

                String a = "";
                if (bookInfo.has(AUTHORS)) {
                    JSONArray authors = bookInfo.getJSONArray(AUTHORS);
                    a = authors.getString(0);
                }

                String desc = "";
                if (bookInfo.has(DESC)) desc = bookInfo.getString(DESC);

                String imgUrl = "";
                if (bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                    imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
                }

                mSearchResults.add(new Book(title, a, imgUrl, desc));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error ", e);
        }
    }
}
