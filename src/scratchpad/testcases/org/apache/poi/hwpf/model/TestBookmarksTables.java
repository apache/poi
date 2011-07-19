package org.apache.poi.hwpf.model;

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.usermodel.Bookmark;

public class TestBookmarksTables extends TestCase
{
    public void test()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "pageref.doc" );
        BookmarksTables bookmarksTables = doc.getBookmarksTables();

        assertEquals( 1, bookmarksTables.getBookmarksCount() );

        Bookmark bookmark = bookmarksTables.getBookmark( 0 );
        assertEquals( "userref", bookmark.getName() );
        assertEquals( 27, bookmark.getStart() );
        assertEquals( 38, bookmark.getEnd() );
    }
}
