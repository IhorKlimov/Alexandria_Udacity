package com.iklimov.alexandria.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.stetho.Stetho;
import com.iklimov.alexandria.api.Book;
import com.iklimov.alexandria.api.Holder;
import com.iklimov.alexandria.fragments.BookDetailFragment;
import com.iklimov.alexandria.fragments.MyBooksListFragment;
import com.iklimov.alexandria.R;
import com.iklimov.alexandria.fragments.SearchBookFragment;
import com.iklimov.alexandria.helpers.Callback;
import com.iklimov.alexandria.helpers.CircleTransform;
import com.iklimov.alexandria.fragments.AboutFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Callback {

    private static final String LOG_TAG = "MainActivity";
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    private static final String DETAILFRAGMENT_TAG = "Detail Fragment";
    public static boolean sIsTablet = false;

    private CharSequence mTitle;
    private BroadcastReceiver mMessageReceiver;
    private FragmentManager mFragmentManager;
    private DrawerLayout mDrawer;
    Context context;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sIsTablet = findViewById(R.id.details_fragment) != null;
        context = this;
        Stetho.initializeWithDefaults(context);

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction()
                .replace(R.id.search_view, new SearchBookFragment())
                .commit();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (sIsTablet) getSupportActionBar().setDisplayShowTitleEnabled(false);
        else mToolbar.setTitle(getString(R.string.find_a_book));

        TextView appTitle = (TextView) findViewById(R.id.app_title);
        if (appTitle != null) {
            Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/Gabrielle.ttf");
            appTitle.setTypeface(typeface);
        }
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                AppCompatActivity act = (AppCompatActivity) context;
                InputMethodManager inputMethodManager = (InputMethodManager) act
                        .getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(
                        act.getCurrentFocus().getWindowToken(), 0
                );
            }
        };
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        mMessageReceiver = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver, filter);

        View headerView = navigationView.getHeaderView(0);
        TextView userName = (TextView) headerView.findViewById(R.id.user_name);
        TextView userEmail = (TextView) headerView.findViewById(R.id.user_email);
        ImageView userPhoto = (ImageView) headerView.findViewById(R.id.user_photo);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        userName.setText(pref.getString(getString(R.string.pref_user_name), ""));
        userEmail.setText(pref.getString(getString(R.string.pref_user_email), ""));
        String photoUrl = pref.getString(getString(R.string.pref_user_photo), "");

        if (!photoUrl.equals("")) Picasso.with(this)
                .load(photoUrl)
                .transform(new CircleTransform())
                .into(userPhoto);

    }


    public void setTitle(int titleId) {
        mTitle = getString(titleId);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(Uri uri, ArrayList<Book> searchResult, Holder holder) {
        if (!sIsTablet) {
            Intent intent = new Intent(context, DetailActivity.class)
                    .putExtra(BookDetailFragment.BOOK_POSITION, holder.getAdapterPosition());
            intent.putParcelableArrayListExtra(BookDetailFragment.SEARCH_RESULT, searchResult);
            startActivity(intent);
        } else {
            showDetailsForTablet(uri, searchResult, holder.getAdapterPosition());
        }
    }

    public void showDetailsForTablet(Uri uri, ArrayList<Book> searchResult, int position) {
        Log.d(LOG_TAG, "showDetailsForTablet: ");
        if (uri == null && searchResult == null) {
            Fragment f = getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
            if (f != null) mFragmentManager.beginTransaction().remove(f).commit();
        } else {
            BookDetailFragment fr = new BookDetailFragment();
            Bundle args = new Bundle();
            args.putParcelable(BookDetailFragment.BOOK_URI, uri);
            args.putParcelable(BookDetailFragment.BOOK_PARCELABLE,
                    searchResult != null
                            ? searchResult.get(position)
                            : null);
            fr.setArguments(args);
            mFragmentManager.beginTransaction().replace(R.id.details_fragment, fr,
                    DETAILFRAGMENT_TAG).commit();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        Fragment nextFragment = null;

        switch (item.getItemId()) {
            case R.id.my_books:
                if (!sIsTablet) {
                    mToolbar.setTitle(getString(R.string.my_books));
                } else {
                    Fragment f = getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                    if (f != null) mFragmentManager.beginTransaction().remove(f).commit();
                }
                nextFragment = new MyBooksListFragment();
                break;
            case R.id.search_book:
                if (!sIsTablet) {
                    mToolbar.setTitle(getString(R.string.find_a_book));
                } else {
                    Fragment f = getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                    if (f != null) mFragmentManager.beginTransaction().remove(f).commit();
                }
                nextFragment = new SearchBookFragment();
                break;
            case R.id.about:
                if (!sIsTablet) {
                    mToolbar.setTitle(getString(R.string.about));
                } else {
                    Fragment f = getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
                    if (f != null)  mFragmentManager.beginTransaction().remove(f).commit();
                }
                nextFragment = new AboutFragment();
        }

        mFragmentManager.beginTransaction()
                .replace(R.id.search_view, nextFragment)
                .commit();

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() < 2) {
            finish();
        }
        super.onBackPressed();
    }
}