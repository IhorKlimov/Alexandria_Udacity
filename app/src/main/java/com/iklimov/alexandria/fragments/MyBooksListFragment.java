package com.iklimov.alexandria.fragments;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iklimov.alexandria.R;
import com.iklimov.alexandria.activities.MainActivity;
import com.iklimov.alexandria.api.BooksListAdapter;
import com.iklimov.alexandria.data.AlexandriaContract;
import com.iklimov.alexandria.data.AlexandriaContract.Favorites;
import com.iklimov.alexandria.helpers.Utils;


public class MyBooksListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int LOADER_ID = 10;

    private BooksListAdapter mBooksListAdapter;
    private RecyclerView mMyBooksList;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_my_books, container, false);
        mMyBooksList = (RecyclerView) inflate.findViewById(R.id.my_books);
        mContext = getContext();
        mBooksListAdapter = new BooksListAdapter(mContext, null, null);
        mMyBooksList.setAdapter(mBooksListAdapter);
        int orientation = mContext.getResources().getConfiguration().orientation;
        mMyBooksList.setLayoutManager(new LinearLayoutManager(mContext,
                orientation == Configuration.ORIENTATION_PORTRAIT && MainActivity.sIsTablet
                        ? LinearLayoutManager.HORIZONTAL
                        : LinearLayoutManager.VERTICAL, false));

        return inflate;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, Favorites.CONTENT_URI,
                Utils.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        mBooksListAdapter.swapCursor(data);
        if (MainActivity.sIsTablet) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    data.moveToFirst();
                    MainActivity activity = (MainActivity) mContext;
                    if (data.getCount() == 0) {
                        activity.showDetailsForTablet(null, null, 0);
                    } else {
                        activity.showDetailsForTablet(Favorites.buildBookUri(data.getLong(0)), null, 0);
                    }
                }
            }, 100);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBooksListAdapter.swapCursor(null);
    }

}
