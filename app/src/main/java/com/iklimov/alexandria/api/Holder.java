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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iklimov.alexandria.R;
import com.iklimov.alexandria.activities.DetailActivity;
import com.iklimov.alexandria.activities.MainActivity;
import com.iklimov.alexandria.data.AlexandriaContract;

import java.util.ArrayList;

/**
 * A book {@link android.support.v7.widget.RecyclerView.ViewHolder}
 */
public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private Context mContext;
    private BookAdapterOnClickHandler mHandler;
    private ArrayList<Book> mSearchResults;
    ImageView image;
    TextView title;
    TextView authors;
    long id;

    public Holder(View view, Context context, ArrayList<Book> searchResults,
                  BookAdapterOnClickHandler handler) {
        super(view);
        view.setOnClickListener(this);
        this.mContext = context;
        this.mHandler = handler;
        this.mSearchResults = searchResults;
        image = (ImageView) view.findViewById(R.id.image);
        title = (TextView) view.findViewById(R.id.title);
        authors = (TextView) view.findViewById(R.id.authors);
    }

    @Override
    public void onClick(View v) {
        mHandler.onClick(AlexandriaContract.Favorites.buildBookUri(id), mSearchResults, this);
    }

    public interface BookAdapterOnClickHandler {
        void onClick(Uri uri, ArrayList<Book> searchResult, Holder holder);
    }
}
