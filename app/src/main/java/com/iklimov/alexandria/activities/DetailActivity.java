package com.iklimov.alexandria.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.iklimov.alexandria.R;
import com.iklimov.alexandria.api.Book;
import com.iklimov.alexandria.api.DetailsPagerAdapter;
import com.iklimov.alexandria.data.AlexandriaContract;
import com.iklimov.alexandria.fragments.BookDetailFragment;
import com.iklimov.alexandria.helpers.Utils;

import java.util.ArrayList;

/**
 * This class is used to show book details for handsets only
 */
public class DetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String LOG_TAG = "DetailActivity";
    private static final int LOADER_ID = 100;
    public static ArrayList<Book> sBooksToAdd = new ArrayList<>();
    public static ArrayList<Book> sBooksToRemove = new ArrayList<>();


    private DetailsPagerAdapter mAdapter;
    private ViewPager mPager;
    private Toolbar mToolbar;
    private int mPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mToolbar = (Toolbar) findViewById(R.id.details_toolbar);
        mPager = (ViewPager) findViewById(R.id.pager);


        Intent intent = getIntent();
        mPosition = intent.getIntExtra(BookDetailFragment.BOOK_POSITION, 0);
        ArrayList<Book> searchResult =
                intent.getParcelableArrayListExtra(BookDetailFragment.SEARCH_RESULT);

        mAdapter = new DetailsPagerAdapter(getSupportFragmentManager(), null, searchResult);
        mPager.setAdapter(mAdapter);

        setSupportActionBar(mToolbar);
        ActionBar supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);

        supportPostponeEnterTransition();

//        float density = getResources().getDisplayMetrics().density;
//        mPager.setPageMargin((int) (density * 32));
//        mPager.setPageMarginDrawable(R.color.gray_200);

        if (searchResult == null) getLoaderManager().initLoader(LOADER_ID, null, this);
        else mPager.setCurrentItem(mPosition);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        for (Book b : sBooksToRemove) {
            Utils.removeFromFavorites(this, b);
        }
        for (Book b : sBooksToAdd) {
            Utils.addToFavorites(this, b);
        }
        sBooksToRemove.clear();
        sBooksToAdd.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, AlexandriaContract.Favorites.CONTENT_URI, Utils.PROJECTION,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
        mPager.setCurrentItem(mPosition);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
