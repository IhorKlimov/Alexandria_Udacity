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

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * Model of a book
 */
public class Book implements Parcelable {
    private static final String LOG_TAG = "Book";
    String title;
    String authors;
    String imageThumbnail;
    String description;
    float rating;
    String pageCount;
    String categories;
    String shareLink;
    String volumeId;
    String previewLink;

    public Book(String title, String authors, String imageThumbnail, String description,
                float rating, String pageCount, String categories, String shareLink,
                String volumeId, String previewLink) {
        this.title = title;
        this.authors = authors;
        this.imageThumbnail = imageThumbnail;
        this.description = description;
        this.rating = rating;
        this.pageCount = pageCount;
        this.categories = categories;
        this.shareLink = shareLink;
        this.volumeId = volumeId;
        this.previewLink = previewLink;
    }

    private Book(Parcel in) {
        title = in.readString();
        authors = in.readString();
        imageThumbnail = in.readString();
        description = in.readString();
        rating = in.readFloat();
        pageCount = in.readString();
        categories = in.readString();
        shareLink = in.readString();
        volumeId = in.readString();
        previewLink = in.readString();
    }

    public String getTitle() {
        return title;
    }

    public String getAuthors() {
        return authors;
    }

    public String getImageThumbnail() {
        return imageThumbnail;
    }

    public String getDescription() {
        return description;
    }

    public float getRating() {
        return rating;
    }

    public String getPageCount() {
        return pageCount;
    }

    public String getCategories() {
        return categories;
    }

    public String getShareLink() {
        return shareLink;
    }

    public String getPreviewLink() {
        return previewLink;
    }

    public String getVolumeId() {
        Log.d(LOG_TAG, "getVolumeId() returned: " + volumeId);
        return volumeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(authors);
        dest.writeString(imageThumbnail);
        dest.writeString(description);
        dest.writeFloat(rating);
        dest.writeString(pageCount);
        dest.writeString(categories);
        dest.writeString(shareLink);
        dest.writeString(volumeId);
        dest.writeString(previewLink);
    }

    public static final Creator CREATOR = new Creator<Book>() {

        @Override
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;

        Book book = (Book) o;

        if (Float.compare(book.rating, rating) != 0) return false;
        if (title != null ? !title.equals(book.title) : book.title != null) return false;
        if (authors != null ? !authors.equals(book.authors) : book.authors != null) return false;
        if (imageThumbnail != null ? !imageThumbnail.equals(book.imageThumbnail) : book.imageThumbnail != null)
            return false;
        if (description != null ? !description.equals(book.description) : book.description != null)
            return false;
        if (pageCount != null ? !pageCount.equals(book.pageCount) : book.pageCount != null)
            return false;
        if (categories != null ? !categories.equals(book.categories) : book.categories != null)
            return false;
        if (shareLink != null ? !shareLink.equals(book.shareLink) : book.shareLink != null)
            return false;
        return volumeId != null ? volumeId.equals(book.volumeId) : book.volumeId == null;

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (authors != null ? authors.hashCode() : 0);
        result = 31 * result + (imageThumbnail != null ? imageThumbnail.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (rating != +0.0f ? Float.floatToIntBits(rating) : 0);
        result = 31 * result + (pageCount != null ? pageCount.hashCode() : 0);
        result = 31 * result + (categories != null ? categories.hashCode() : 0);
        result = 31 * result + (shareLink != null ? shareLink.hashCode() : 0);
        result = 31 * result + (volumeId != null ? volumeId.hashCode() : 0);
        return result;
    }
}
