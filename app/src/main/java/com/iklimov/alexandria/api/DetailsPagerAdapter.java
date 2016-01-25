/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iklimov.alexandria.api;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.iklimov.alexandria.activities.DetailActivity;
import com.iklimov.alexandria.data.AlexandriaContract;
import com.iklimov.alexandria.fragments.BookDetailFragment;

import java.util.ArrayList;


/**
 * DetailActivity's {@link FragmentStatePagerAdapter} which takes care of preloading
 * {@link BookDetailFragment}
 */
public class DetailsPagerAdapter extends FragmentStatePagerAdapter {
    private static final String LOG_TAG = "DetailsPagerAdapter";
    private Cursor mCursor;
    private ArrayList<Book> mSearchResult;


    public DetailsPagerAdapter(FragmentManager fm, Cursor cursor, ArrayList<Book> searchResult) {
        super(fm);
        this.mCursor = cursor;
        this.mSearchResult = searchResult;
    }

    @Override
    public Fragment getItem(int position) {
        BookDetailFragment fr = new BookDetailFragment();
        Bundle args = new Bundle();

        if (mSearchResult == null) {
            mCursor.moveToPosition(position);
            args.putParcelable(BookDetailFragment.BOOK_URI,
                    AlexandriaContract.Favorites.buildBookUri(mCursor.getLong(0)));
        } else {
            Book book = mSearchResult.get(position);
            Log.d(LOG_TAG, "getItem: "+ book.title);
            args.putParcelable(BookDetailFragment.BOOK_PARCELABLE, book);
        }
        fr.setArguments(args);
        return fr;
    }

    @Override
    public int getCount() {
        if (mCursor == null && mSearchResult == null) return 0;
        else return  mCursor == null ? mSearchResult.size() : mCursor.getCount();
    }


    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
