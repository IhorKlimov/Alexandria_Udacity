package com.iklimov.alexandria.activities;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.Scope;
import com.iklimov.alexandria.BuildConfig;
import com.iklimov.alexandria.R;
import com.iklimov.alexandria.api.Book;
import com.iklimov.alexandria.helpers.Utils;
import com.iklimov.alexandria.data.AlexandriaContract.Favorites;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API;
import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;
import static com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder;
import static com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.iklimov.alexandria.R.id.sign_in_button;
import static com.iklimov.alexandria.R.layout.activity_sign_in;

public class SignInActivity extends AppCompatActivity
        implements OnConnectionFailedListener, OnClickListener {

    private static final String LOG_TAG = "SignInActivity";
    private static final int RC_GET_TOKEN = 9002;
    private static final String BOOKS_API_SCOPE = "https://www.googleapis.com/auth/books";
    private static final String SCOPE = "oauth2:" + BOOKS_API_SCOPE;

    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog mProgressDialog;
    Context context;
    AppCompatActivity activity;
    private View mSignInBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_sign_in);

        context = this;
        activity = this;

        mSignInBtn = findViewById(sign_in_button);
        mSignInBtn.setOnClickListener(this);

        GoogleSignInOptions gso = new Builder(DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.MY_CLIENT_ID)
                .requestScopes(new Scope(BOOKS_API_SCOPE))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> silentSignIn =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (silentSignIn.isDone()) {
            mSignInBtn.setVisibility(GONE);
            if (Utils.isInternetAvailable(context)) {
                if (mProgressDialog == null) showProgressDialog();
                new SyncFavorites().execute();
            } else {
                Utils.noInternetMessage(activity, "1");
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSignInBtn.setOnClickListener(null);
        if (mProgressDialog != null) mProgressDialog.dismiss();
    }

    private void getIdToken() {
        Intent signInIntent = GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_TOKEN) {
            final GoogleSignInResult result = GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                GoogleSignInAccount acc = result.getSignInAccount();
                if (mProgressDialog == null) showProgressDialog();
                new GetAccountInfoAndToken().execute(acc);
            }
        }
    }

    private class GetAccountInfoAndToken extends AsyncTask<GoogleSignInAccount, Void, Void> {
        @Override
        protected Void doInBackground(GoogleSignInAccount... params) {
            try {
                GoogleSignInAccount acc = params[0];

                String email = acc.getEmail();
                Uri photoUrl = acc.getPhotoUrl();
                String name = acc.getDisplayName();
                String token = GoogleAuthUtil.getToken(context, email, SCOPE);

                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(context);

                preferences.edit().putString(context.getString(R.string.pref_token), token).apply();
                preferences.edit().putString(context.getString(R.string.pref_user_email), email).apply();
                preferences.edit().putString(context.getString(R.string.pref_user_photo), photoUrl.toString()).apply();
                preferences.edit().putString(context.getString(R.string.pref_user_name), name).apply();

            } catch (IOException | GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (Utils.isInternetAvailable(context)) {
                if (mProgressDialog == null) showProgressDialog();
                new SyncFavorites().execute();
            } else {
                Utils.noInternetMessage(activity, "1");
            }
        }
    }

    public class SyncFavorites extends AsyncTask<Void, Void, Void> {

        private boolean mCrushed;

        @Override
        protected Void doInBackground(Void... params) {
            String token = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(getString(R.string.pref_token), "");

            try {
                InputStream is;
                URL url = new URL("https://www.googleapis.com/books/v1/mylibrary/bookshelves/0/volumes");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15_000);
                conn.setConnectTimeout(15_000);
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.connect();
                int response = conn.getResponseCode();
                Log.i(LOG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                    builder.append("\n");
                }

                Log.d(LOG_TAG, "doInBackground: " + builder.toString());

                Book[] books = Utils.readJsonBook(builder.toString());
                if (books != null) syncFavorites(books);
            } catch (SocketTimeoutException e) {
                mCrushed = true;
                e.printStackTrace();
                Utils.noInternetMessage(activity, "1");
            } catch (IOException e) {
                mCrushed = true;
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!mCrushed) startActivity(new Intent(context, MainActivity.class));
            finish();
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case sign_in_button:
                if (Utils.isInternetAvailable(this)) getIdToken();
                else Utils.noInternetMessage(this, "1");
                break;
        }
    }

    private void syncFavorites(Book[] books) {
        ContentResolver contentResolver = context.getContentResolver();

        Cursor localBooks = contentResolver
                .query(Favorites.CONTENT_URI, Utils.PROJECTION, null, null, null);

        if (localBooks != null) {
            while (localBooks.moveToNext()) {
                String title = localBooks.getString(0);
                boolean isOnline = false;

                for (Book b : books) {
                    if (b.getTitle().equals(title)) {
                        isOnline = true;
                        break;
                    }
                }

                if (!isOnline) contentResolver.delete(Favorites.CONTENT_URI,
                        Favorites.COL_TITLE + "=?", new String[]{localBooks.getString(1)});
            }
            localBooks.close();
        }

        for (Book b : books) {
            String title = b.getTitle();
            Cursor cursor = contentResolver.query(Favorites.CONTENT_URI, Utils.PROJECTION,
                    Favorites.COL_TITLE + "=?", new String[]{title}, null);

            if (cursor == null) continue;
            if (cursor.getCount() == 0) Utils.saveToDb(contentResolver, b);

            cursor.close();
        }
    }
}
