package com.iklimov.alexandria.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.iklimov.alexandria.R;
import com.iklimov.alexandria.activities.DetailActivity;
import com.iklimov.alexandria.activities.MainActivity;
import com.iklimov.alexandria.api.Book;
import com.iklimov.alexandria.data.AlexandriaContract;
import com.iklimov.alexandria.data.AlexandriaContract.Favorites;
import com.iklimov.alexandria.helpers.Utils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


public class BookDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {
    private static final String LOG_TAG = "BookDetailFragment";
    public static final String BOOK_URI = "Book Uri";
    public static final String BOOK_PARCELABLE = "Book Parcelable";
    public static final String BOOK_POSITION = "Book Position";
    public static final String SEARCH_RESULT = "Search Result";
    private final int LOADER_ID = 10;

    private Context mContext;
    private Toolbar mToolbar;
    private ShareActionProvider mActionProvider;
    private TextView mTitle;
    private TextView mAuthors;
    private TextView mCategories;
    private TextView mPageCount;
    private TextView mDescription;
    private ImageView mPoster;
    private RatingBar mRatingBar;
    private Button mPreviewBtn;

    private String mShareLink;
    private Uri mBookUri;
    private FloatingActionButton mFab;
    private boolean mInserted;
    private Book mBook;
    private ScrollView mScrollView;
    private String mPreviewLink;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!MainActivity.sIsTablet) setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mContext = getContext();
        mPoster = (ImageView) rootView.findViewById(R.id.details_poster);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        mAuthors = (TextView) rootView.findViewById(R.id.authors);
        mCategories = (TextView) rootView.findViewById(R.id.categories);
        mPageCount = (TextView) rootView.findViewById(R.id.page_count);
        mDescription = (TextView) rootView.findViewById(R.id.description);
        mRatingBar = (RatingBar) rootView.findViewById(R.id.rating_bar);
        mToolbar = (Toolbar) rootView.findViewById(R.id.details_toolbar);
        mScrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
        mPreviewBtn = (Button) rootView.findViewById(R.id.preview);

        Bundle args = getArguments();
        mBookUri = args.getParcelable(BOOK_URI);
        mBook = args.getParcelable(BOOK_PARCELABLE);

        mFab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        mFab.setOnClickListener(this);

        setupToolbar();
        if (MainActivity.sIsTablet) setupParallaxToolbar();
        return rootView;
    }

    private void setupParallaxToolbar() {
        final boolean isPortrait = mContext.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mScrollView.getViewTreeObserver().addOnScrollChangedListener(
                        new ViewTreeObserver.OnScrollChangedListener() {
                            int j = mToolbar.getHeight();

                            @Override
                            public void onScrollChanged() {
                                int i = mScrollView.getScrollY();
                                float k = -mToolbar.getTranslationY();
                                int n = -(i / 2);
                                mToolbar.setTranslationY(n);
//                                if (!isPortrait) {
//                                    if (j + k >= b) {
//                                        int i2 = -(j - (n + b));
//                                        mToolbar.setTranslationY(Math.min(0, i2));
//                                    }
//                                } else {
//                                    int i2 = (b - i - j);
//                                    bar.setTranslationY(Math.min(0, i2));
//                                }
                            }
                        });
            }
        }, 300);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!MainActivity.sIsTablet) {
            inflater.inflate(R.menu.book_detail, menu);
            finishCreatingMenu(menu);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mBookUri != null) {
            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mBook != null) setupUi(null);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(mContext, mBookUri, Utils.PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) return;
        setupUi(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void setupUi(Cursor data) {
        String title;
        String authors;
        String categories;
        String pageCount;
        String desc;
        float rating;
        String img;
        Callback callback = new Callback() {
            @Override
            public void onSuccess() {
                mPoster.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {

            }
        };

        if (mBook == null) {
            title = data.getString(1);
            authors = data.getString(3);
            categories = data.getString(5);
            pageCount = data.getString(7);
            desc = data.getString(4);
            rating = data.getFloat(6);
            img = data.getString(2);
            mPreviewLink = data.getString(10);
            mShareLink = data.getString(8);
            mBook = new Book(title, authors, img, desc, rating, pageCount, categories,
                    mShareLink, data.getString(9), mPreviewLink);
        } else {
            title = mBook.getTitle();
            authors = mBook.getAuthors();
            categories = mBook.getCategories();
            pageCount = mBook.getPageCount();
            desc = mBook.getDescription();
            rating = mBook.getRating();
            img = mBook.getImageThumbnail();
            mShareLink = mBook.getShareLink();
            mPreviewLink = mBook.getPreviewLink();
        }
        if (Utils.isFavorite(mContext, mBook) && !DetailActivity.sBooksToRemove.contains(mBook)) {
            mFab.setImageResource(R.drawable.favorites);
            mFab.setActivated(true);
            mInserted = true;
        }

        mTitle.setText(title);
        mAuthors.setText(mContext.getString(R.string.authors, authors));
        mCategories.setText(mContext.getString(R.string.category, categories));
        mPageCount.setText(mContext.getString(R.string.page_count, pageCount));
        mDescription.setText(desc);
        mRatingBar.setRating(rating);
        if (!img.equals("")) Picasso.with(mContext).load(img).into(mPoster, callback);
        else Picasso.with(mContext).load(R.drawable.ic_launcher).into(mPoster, callback);
        if (mPreviewLink != null) mPreviewBtn.setOnClickListener(this);

        if (mActionProvider != null) mActionProvider.setShareIntent(createShareIntent());
    }

    private void setupToolbar() {
        if (MainActivity.sIsTablet) {
            Menu menu = mToolbar.getMenu();
            if (null != menu) menu.clear();
            mToolbar.inflateMenu(R.menu.book_detail);
            finishCreatingMenu(menu);
        }
    }

    private void finishCreatingMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_share);
//        if (MainActivity.sIsTablet) {
//            mActionProvider = new ShareActionProvider(getActivity()) {
//                @Override
//                public View onCreateActionView() {
//                    return null;
//                }
//            };
//            item.setIcon(R.drawable.share_24dp);
//        } else {
        mActionProvider = new ShareActionProvider(getActivity());
//        }
        MenuItemCompat.setActionProvider(item, mActionProvider);
        if (mShareLink != null) mActionProvider.setShareIntent(createShareIntent());
    }

    private Intent createShareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        Log.d(LOG_TAG, "createShareIntent: " + mShareLink);
        intent.putExtra(Intent.EXTRA_TEXT, "#Alexandria app " + mShareLink);
        return intent;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                if (!mFab.isActivated()) {
                    if (!mInserted) {
                        Toast.makeText(mContext, "Added to Favorites", Toast.LENGTH_SHORT).show();
                        mFab.setImageResource(R.drawable.favorites);
                        if (MainActivity.sIsTablet) {
                            Utils.addToFavorites(mContext, mBook);
                        } else {
                            DetailActivity.sBooksToRemove.remove(mBook);
                            DetailActivity.sBooksToAdd.add(mBook);
                        }

                        mFab.setActivated(true);
                        mInserted = true;
                    }
                } else {
                    if (mInserted) {
                        Toast.makeText(mContext, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                        mFab.setImageResource(R.drawable.favorites_empty);
                        mFab.setActivated(false);

                        if (MainActivity.sIsTablet) {
                            Utils.removeFromFavorites(mContext, mBook);
//                            MoviesGridFragment.sId = Utility.getId(context);
                        } else {
                            DetailActivity.sBooksToRemove.add(mBook);
                            DetailActivity.sBooksToAdd.remove(mBook);
                        }
                        mInserted = false;
                    }
                }
                break;
            case R.id.preview:
                Intent previewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mPreviewLink));
                mContext.startActivity(previewIntent);
        }
    }
}
