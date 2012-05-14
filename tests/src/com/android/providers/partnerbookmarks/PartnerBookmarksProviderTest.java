/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.providers.partnerbookmarks;

import android.content.ContentProviderClient;
import android.database.Cursor;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContext;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

import junit.framework.TestCase;

public class PartnerBookmarksProviderTest extends InstrumentationTestCase {
    private static final String TAG = "PartnerBookmarksProviderTest";
    private static final long FIXED_ID_PARTNER_BOOKMARKS_ROOT =
            PartnerBookmarksContract.Bookmarks.BOOKMARK_PARENT_ROOT_ID + 1;
    private static final long NO_FOLDER_FILTER = -1;

    @Override
    public void setUp() {
    }

    @Override
    public void tearDown() {
    }

    private int countBookmarksInFolder(long folderFilter) throws android.os.RemoteException {
        ContentProviderClient providerClient =
                getInstrumentation().getTargetContext().
                        getContentResolver().acquireContentProviderClient(
                                PartnerBookmarksContract.Bookmarks.CONTENT_URI);
        assertNotNull(
                "Failed to acquire " + PartnerBookmarksContract.Bookmarks.CONTENT_URI,
                providerClient);
        Cursor cursor = providerClient.query(PartnerBookmarksContract.Bookmarks.CONTENT_URI,
                null, null, null, null);
        assertNotNull("Failed to query for bookmarks", cursor);
        int bookmarksCount = 0;
        while (!cursor.isLast() && !cursor.isAfterLast()) {
            cursor.moveToNext();
            long id = cursor.getLong(
                    cursor.getColumnIndexOrThrow(PartnerBookmarksContract.Bookmarks.ID));
            long parent = cursor.getLong(
                    cursor.getColumnIndexOrThrow(PartnerBookmarksContract.Bookmarks.PARENT));
            if (folderFilter != NO_FOLDER_FILTER && folderFilter != parent)
                continue;
            boolean isFolder = cursor.getInt(
                    cursor.getColumnIndexOrThrow(PartnerBookmarksContract.Bookmarks.TYPE))
                            == PartnerBookmarksContract.Bookmarks.BOOKMARK_TYPE_FOLDER;
            String url = cursor.getString(
                    cursor.getColumnIndexOrThrow(PartnerBookmarksContract.Bookmarks.URL));
            String title = cursor.getString(
                    cursor.getColumnIndexOrThrow(PartnerBookmarksContract.Bookmarks.TITLE));
            bookmarksCount++;
        }
        return bookmarksCount;
    }

    @SmallTest
    public void testExactlyOneRoot() throws android.os.RemoteException {
        int totalBookmarks = countBookmarksInFolder(NO_FOLDER_FILTER);
        if (totalBookmarks > 0) {
            assertEquals("There must be at most one root",
                    countBookmarksInFolder(
                            PartnerBookmarksContract.Bookmarks.BOOKMARK_PARENT_ROOT_ID),
                    1);
        }
    }

    @SmallTest
    public void testRootMustBeNonEmptyIfPresent() throws android.os.RemoteException {
        int totalBookmarks = countBookmarksInFolder(NO_FOLDER_FILTER);
        if (totalBookmarks > 0) {
            assertTrue("If the root exists, it must be non-empty",
                    countBookmarksInFolder(FIXED_ID_PARTNER_BOOKMARKS_ROOT) > 0);
        }
    }

    @SmallTest
    public void testDefaultPBPSupportsOnlyFlatListOfBookmarks()
            throws android.os.RemoteException {
        int totalBookmarks = countBookmarksInFolder(NO_FOLDER_FILTER);
        if (totalBookmarks > 0) {
            assertEquals("Default PBP supports only flat list of bookmarks",
                    countBookmarksInFolder(FIXED_ID_PARTNER_BOOKMARKS_ROOT),
                    totalBookmarks - 1);
        }
    }
}
