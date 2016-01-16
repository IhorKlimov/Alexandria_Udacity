package com.iklimov.alexandria;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.view.View.GONE;
import static android.view.View.OnClickListener;
import static android.view.View.VISIBLE;
import static com.google.android.gms.auth.api.Auth.GOOGLE_SIGN_IN_API;
import static com.google.android.gms.auth.api.Auth.GoogleSignInApi;
import static com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder;
import static com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.iklimov.alexandria.R.id.detail;
import static com.iklimov.alexandria.R.id.disconnect_button;
import static com.iklimov.alexandria.R.id.sign_in_button;
import static com.iklimov.alexandria.R.id.sign_out_and_disconnect;
import static com.iklimov.alexandria.R.id.sign_out_button;
import static com.iklimov.alexandria.R.id.status;
import static com.iklimov.alexandria.R.layout.activity_sign_in;
import static com.iklimov.alexandria.R.string.id_token_fmt;
import static com.iklimov.alexandria.R.string.signed_in;
import static com.iklimov.alexandria.R.string.signed_out;

public class SignInActivity extends AppCompatActivity
        implements OnConnectionFailedListener, OnClickListener {

    private static final String LOG_TAG = "SignInActivity";
    private static final int RC_GET_TOKEN = 9002;
    private static final String BOOKS_API_SCOPE = "https://www.googleapis.com/auth/books";
    private static final String SCOPE = "oauth2:" + BOOKS_API_SCOPE;

    private GoogleApiClient mGoogleApiClient;
    private TextView mIdTokenTextView;
    Context context;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_sign_in);

        context = this;

        // Views
        mIdTokenTextView = (TextView) findViewById(detail);

        // Button click listeners
        findViewById(sign_in_button).setOnClickListener(this);
        findViewById(sign_out_button).setOnClickListener(this);
        findViewById(disconnect_button).setOnClickListener(this);

        // [START configure_signin]
        // Request only the user's ID token, which can be used to identify the
        // user securely to your backend. This will contain the user's basic
        // profile (name, profile picture URL, etc) so you should not need to
        // make an additional call to personalize your application.
        GoogleSignInOptions gso = new Builder(DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestScopes(new Scope(BOOKS_API_SCOPE))
                .requestEmail()
                .build();

        // [END configure_signin]

        // Build GoogleAPIClient with the Google Sign-In API and the above options.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> silentSignIn =
                Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (silentSignIn.isDone()) startActivity(new Intent(this, MainActivity.class));
    }

    private void getIdToken() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        Intent signInIntent = GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }

    private void signOut() {
        GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(LOG_TAG, "signOut:onResult:" + status);
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.d(LOG_TAG, "revokeAccess:onResult:" + status);
                        updateUI(false);
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_TOKEN) {
            final GoogleSignInResult result = GoogleSignInApi.getSignInResultFromIntent(data);
            Log.i(LOG_TAG, "onActivityResult:GET_TOKEN:success:" + result.getStatus().isSuccess());

            if (result.isSuccess()) {
                GoogleSignInAccount acc = result.getSignInAccount();
                new SignInWithGoogle().execute(acc);
            } else {
                updateUI(false);
            }
        }
    }

    private class SignInWithGoogle extends AsyncTask<GoogleSignInAccount, Void, Void> {
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

                Log.i(LOG_TAG, "doInBackground: "+ token);
                Log.i(LOG_TAG, "doInBackground: "+ email);
                Log.i(LOG_TAG, "doInBackground: "+ photoUrl.toString());
                Log.i(LOG_TAG, "doInBackground: "+ name);
            } catch (IOException | GoogleAuthException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            startActivity(new Intent(context, MainActivity.class));
        }
    }

    private void getFavorites(String idToken) throws IOException {
        InputStream is;
        URL url = new URL("https://www.googleapis.com/books/v1/mylibrary/bookshelves/0/volumes");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + idToken);
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

        Log.i(LOG_TAG, "onActivityResult: " + builder.toString());
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(LOG_TAG, "onConnectionFailed:" + connectionResult);
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            ((TextView) findViewById(status)).setText(signed_in);

            findViewById(sign_in_button).setVisibility(GONE);
            findViewById(sign_out_and_disconnect).setVisibility(VISIBLE);
        } else {
            ((TextView) findViewById(status)).setText(signed_out);
            mIdTokenTextView.setText(getString(id_token_fmt, "null"));

            findViewById(sign_in_button).setVisibility(VISIBLE);
            findViewById(sign_out_and_disconnect).setVisibility(GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case sign_in_button:
                getIdToken();
                break;
            case sign_out_button:
                signOut();
                break;
            case disconnect_button:
                revokeAccess();
                break;
        }
    }
}
