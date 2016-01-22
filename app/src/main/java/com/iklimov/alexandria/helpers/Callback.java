package com.iklimov.alexandria.helpers;

import android.net.Uri;

import com.iklimov.alexandria.api.Book;
import com.iklimov.alexandria.api.Holder;

import java.util.ArrayList;

/**
 * Created by saj on 25/01/15.
 */
public interface Callback {
    void onItemSelected(Uri uri, ArrayList<Book> searchResult, Holder holder);
}
