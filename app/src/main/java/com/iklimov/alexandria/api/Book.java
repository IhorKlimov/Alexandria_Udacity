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

/**
 * Created by Igor Klimov on 1/14/2016.
 */
public class Book {
    public String title;
    String authors;
    public String imageThumbnail;
    String description;

    public Book(String title, String authors, String imageThumbnail, String description) {
        this.title = title;
        this.authors = authors;
        this.imageThumbnail = imageThumbnail;
        this.description = description;
    }
}
