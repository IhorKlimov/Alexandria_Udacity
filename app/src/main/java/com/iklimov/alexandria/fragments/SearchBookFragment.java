package com.iklimov.alexandria.fragments;

import android.content.Context;
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
import android.widget.Toast;

import com.iklimov.alexandria.api.BooksListAdapter;
import com.iklimov.alexandria.helpers.Utils;
import com.iklimov.alexandria.R;
import com.iklimov.alexandria.api.Book;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;


public class SearchBookFragment extends Fragment {
    private static final String LOG_TAG = "SearchBookFragment";

    private Context mContext;
    private EditText mEan;
    private final String EAN_CONTENT = "eanContent";
    private ArrayList<Book> mSearchResults;
    private RecyclerView mResultsView;
    private boolean mRunning;
    private ImageButton scanBtn;

    public SearchBookFragment() {
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

        View rootView = inflater.inflate(R.layout.fragment_search_book, container, false);
        mEan = (EditText) rootView.findViewById(R.id.ean);
        mResultsView = (RecyclerView) rootView.findViewById(R.id.results);
        mContext = getContext();

        mSearchResults = new ArrayList<>();
        mResultsView.setAdapter(new BooksListAdapter(mContext, null, mSearchResults));
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

//                    Drawable d = scanBtn.getDrawable();
//                    if (d instanceof Animatable) {
//                        ((Animatable)d).start();
//                    }
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
        Book[] books = Utils.readJsonBook(bookJsonString);
        if (books != null) Collections.addAll(mSearchResults, books);
    }
}
