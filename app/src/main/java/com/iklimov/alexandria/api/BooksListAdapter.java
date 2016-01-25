package com.iklimov.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iklimov.alexandria.R;
import com.iklimov.alexandria.activities.MainActivity;
import com.iklimov.alexandria.fragments.MyBooksListFragment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Adapter for {@link MyBooksListFragment} and
 * {@link com.iklimov.alexandria.fragments.SearchBookFragment}
 * since they're very similar
 */
public class BooksListAdapter extends RecyclerView.Adapter<Holder> {
    private static final String LOG_TAG = "BooksListAdapter";
    private ArrayList<Book> mSearchResults;
    private Cursor mCursor;
    private Context mContext;


    public BooksListAdapter(Context context, Cursor cursor, ArrayList<Book> searchResults) {
        this.mContext = context;
        this.mCursor = cursor;
        this.mSearchResults = searchResults;
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.books_list_item, parent, false);
        return new Holder(inflate, mContext, mSearchResults, new Holder.BookAdapterOnClickHandler() {
            @Override
            public void onClick(Uri uri, ArrayList<Book> searchResults, Holder holder) {
                ((MainActivity) mContext).onItemSelected(uri, searchResults, holder);
            }
        });
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        String img;
        String title;
        String authors;
        Log.d(LOG_TAG, "onBindViewHolder: ");
//        Check if this is MyBooksListFragment
        if (mCursor != null) {
            mCursor.moveToPosition(position);
            int id = mCursor.getInt(0);
            Log.d(LOG_TAG, "onBindViewHolder: " + id);
            holder.id = id;
            title = mCursor.getString(1);
            authors = mCursor.getString(3);
            img = mCursor.getString(2);
        } else {
//            Else this is SearchBookFragment
            Book book = mSearchResults.get(position);
            title = book.title;
            authors = book.authors;
            img = book.imageThumbnail;
        }
        holder.title.setText(title);
        holder.authors.setText(authors);
        Log.d(LOG_TAG, "onBindViewHolder: " + img);
        if (!img.equals("")) {
            Picasso.with(mContext).load(img).into(holder.image);
        } else {
            Picasso.with(mContext).load(R.drawable.ic_launcher).into(holder.image);
        }
    }

    @Override
    public int getItemCount() {
        if (mCursor == null && mSearchResults == null) return 0;
        else return mCursor == null ? mSearchResults.size() : mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
