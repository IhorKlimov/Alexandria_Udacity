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
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iklimov.alexandria.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Igor Klimov on 1/14/2016.
 */
public class BookResultAdapter extends RecyclerView.Adapter<BookResultAdapter.BookHolder> {

    private static final String LOG_TAG = "BookResultAdapter";


    ArrayList<Book> mSearchResults;
    private Context mContext;

    public BookResultAdapter(ArrayList<Book> searchResults, Context context) {
        this.mSearchResults = searchResults;
        this.mContext = context;
    }

    @Override
    public BookHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_item, parent, false);
        return new BookHolder(view);
    }

    @Override
    public void onBindViewHolder(BookHolder holder, int position) {
        Book book = mSearchResults.get(position);
        if (!book.imageThumbnail.equals("")) {
            Picasso.with(mContext).load(book.imageThumbnail).into(holder.image);
        } else {
            Picasso.with(mContext).load(R.drawable.ic_launcher).into(holder.image);
        }
        holder.title.setText(book.title);
        holder.authors.setText(book.authors);
    }

    @Override
    public int getItemCount() {
        return mSearchResults.size();
    }

    public class BookHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title;
        TextView authors;

        public BookHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image);
            title = (TextView) view.findViewById(R.id.title);
            authors = (TextView) view.findViewById(R.id.authors);
        }
    }
}
