package com.iklimov.alexandria.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
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

import com.iklimov.alexandria.activities.MainActivity;
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


public class SearchBookFragment extends Fragment implements View.OnClickListener {
    private static final String LOG_TAG = "SearchBookFragment";
    public static final String ISBN = "ISBN";
    public static final String RESULTS = "Results";

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

        Log.d(LOG_TAG, "onSaveInstanceState: ");
        if (mEan != null) {
            String value = mEan.getText().toString();
            Log.d(LOG_TAG, "onSaveInstanceState: " + value);
            outState.putString(EAN_CONTENT, value);
        }
        if (mSearchResults.size() != 0) {
            outState.putParcelableArrayList(RESULTS, mSearchResults);
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search_book, container, false);
        mEan = (EditText) rootView.findViewById(R.id.ean);
        mResultsView = (RecyclerView) rootView.findViewById(R.id.results);
        mContext = getContext();

        boolean b = savedInstanceState != null;

        if (b && savedInstanceState.containsKey(RESULTS)) {
            mSearchResults = savedInstanceState.getParcelableArrayList(RESULTS);
        } else {
            mSearchResults = new ArrayList<>();
        }
        mResultsView.setAdapter(new BooksListAdapter(mContext, null, mSearchResults));
        int orientation = mContext.getResources().getConfiguration().orientation;
        mResultsView.setLayoutManager(new LinearLayoutManager(mContext,
                orientation == Configuration.ORIENTATION_PORTRAIT && MainActivity.sIsTablet
                        ? LinearLayoutManager.HORIZONTAL
                        : LinearLayoutManager.VERTICAL, false));

        scanBtn = (ImageButton) rootView.findViewById(R.id.scan_button);
        scanBtn.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle arguments = getArguments();
        boolean b = arguments != null;
        if (b && arguments.containsKey(ISBN)) {
            new Search().execute(arguments.getString(ISBN));
            arguments.remove(ISBN);
        }
        if (b && arguments.containsKey(EAN_CONTENT)) {
            String string = savedInstanceState.getString(EAN_CONTENT);
            Log.d(LOG_TAG, "onViewCreated: "+ string);
            mEan.setText(string);
            mEan.setHint("");
        }

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
            if (MainActivity.sIsTablet) {
                MainActivity activity = (MainActivity) mContext;
                if (mSearchResults.size() == 0) {
                    activity.showDetailsForTablet(null, null, 0);
                } else {
                    activity.showDetailsForTablet(null, mSearchResults, 0);
                }
            }
        }

    }

    private boolean isInternetAvailable() {
        ConnectivityManager service = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = service.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void search(String query) {
        Log.d(LOG_TAG, "search: ");
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String bookJsonString = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
            Uri builtUri;

            if (isIsbn(query)) {
                if (query.length() == 10 && !query.startsWith("978")) query = "978" + query;
                builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter("q", "isbn:" + query)
                        .build();
            } else {
                builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter("q", "intitle:" + query)
                        .appendQueryParameter("maxResults", "40")
                        .build();
            }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scan_button:
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
    }

    private boolean isIsbn(String query) {
        for (char ch : query.toCharArray()) {
            if (!Character.isDigit(ch)) return false;
        }
        return !(query.length() != 10 && query.length() != 13);
    }
}
