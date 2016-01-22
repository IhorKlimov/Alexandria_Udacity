package com.iklimov.alexandria.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
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
import com.iklimov.alexandria.api.BooksListAdapter;
import com.iklimov.alexandria.data.AlexandriaContract;
import com.iklimov.alexandria.helpers.Utils;


public class MyBooksListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final int LOADER_ID = 10;

    private BooksListAdapter mBooksListAdapter;
    private RecyclerView mMyBooksList;
    private Context mContext;
    private boolean mHoldForTransition;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_my_books, container, false);
        mMyBooksList = (RecyclerView) inflate.findViewById(R.id.my_books);
        mContext = getContext();
        mBooksListAdapter = new BooksListAdapter(mContext,null, null);
        mMyBooksList.setAdapter(mBooksListAdapter);
        mMyBooksList.setLayoutManager(new LinearLayoutManager(mContext));

        return inflate;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(LOADER_ID, null, this);
//        if (mHoldForTransition) getActivity().supportPostponeEnterTransition();
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.Book, 0, 0);
        mHoldForTransition =
                a.getBoolean(R.styleable.Book_sharedElementTransition, false);
        a.recycle();
    }

    private void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, AlexandriaContract.Favorites.CONTENT_URI,
                Utils.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mBooksListAdapter.swapCursor(data);
//        if (mHoldForTransition) getActivity().supportStartPostponedEnterTransition();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mBooksListAdapter.swapCursor(null);
    }

}
