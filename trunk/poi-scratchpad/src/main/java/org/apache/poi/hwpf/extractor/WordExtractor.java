/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.hwpf.extractor;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.extractor.POIOLE2TextExtractor;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.converter.WordToTextConverter;
import org.apache.poi.hwpf.usermodel.HeaderStories;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Class to extract the text from a Word Document.
 *
 * You should use either getParagraphText() or getText() unless you have a
 * strong reason otherwise.
 */
public final class WordExtractor implements POIOLE2TextExtractor {
    private final HWPFDocument doc;
    private boolean doCloseFilesystem = true;

    /**
     * Create a new Word Extractor
     *
     * @param is
     *            InputStream containing the word file
     */
    public WordExtractor( InputStream is ) throws IOException {
        this(HWPFDocumentCore.verifyAndBuildPOIFS(is ) );
    }

    /**
     * Create a new Word Extractor
     *
     * @param fs
     *            POIFSFileSystem containing the word file
     */
    public WordExtractor( POIFSFileSystem fs ) throws IOException {
        this( new HWPFDocument( fs ) );
    }

    public WordExtractor( DirectoryNode dir ) throws IOException {
        this( new HWPFDocument( dir ) );
    }

    /**
     * Create a new Word Extractor
     *
     * @param doc
     *            The HWPFDocument to extract from
     */
    public WordExtractor( HWPFDocument doc ) {
        this.doc = doc;
    }

    /**
     * Get the text from the word file, as an array with one String per
     * paragraph
     */
    public String[] getParagraphText() {
        String[] ret;

        // Extract using the model code
        try {
            Range r = doc.getRange();

            ret = getParagraphText( r );
        } catch ( Exception e ) {
            // Something's up with turning the text pieces into paragraphs
            // Fall back to ripping out the text pieces
            ret = new String[1];
            ret[0] = getTextFromPieces();
        }

        return ret;
    }

    public String[] getFootnoteText() {
        Range r = doc.getFootnoteRange();

        return getParagraphText( r );
    }

    public String[] getMainTextboxText() {
        Range r = doc.getMainTextboxRange();

        return getParagraphText( r );
    }

    public String[] getEndnoteText() {
        Range r = doc.getEndnoteRange();

        return getParagraphText( r );
    }

    public String[] getCommentsText() {
        Range r = doc.getCommentsRange();

        return getParagraphText( r );
    }

    static String[] getParagraphText( Range r ) {
        String[] ret;
        ret = new String[r.numParagraphs()];
        for ( int i = 0; i < ret.length; i++ ) {
            Paragraph p = r.getParagraph( i );
            ret[i] = p.text();

            // Fix the line ending
            if ( ret[i].endsWith( "\r" )) {
                ret[i] = ret[i] + "\n";
            }
        }
        return ret;
    }

    /**
     * Add the header/footer text, if it's not empty
     */
    private void appendHeaderFooter( String text, StringBuilder out ) {
        if ( text == null || text.length() == 0 )
            return;

        text = text.replace( '\r', '\n' );
        if ( !text.endsWith( "\n" ))
        {
            out.append( text );
            out.append( '\n' );
            return;
        }
        if ( text.endsWith( "\n\n" ))
        {
            out.append(text, 0, text.length() - 1);
            return;
        }
        out.append( text );
    }

    /**
     * Grab the text from the headers
     * @deprecated 3.8 beta 4
     */
    @Deprecated
    public String getHeaderText() {
        HeaderStories hs = new HeaderStories( doc );

        StringBuilder ret = new StringBuilder();
        if ( hs.getFirstHeader() != null ) {
            appendHeaderFooter( hs.getFirstHeader(), ret );
        }
        if ( hs.getEvenHeader() != null ) {
            appendHeaderFooter( hs.getEvenHeader(), ret );
        }
        if ( hs.getOddHeader() != null ) {
            appendHeaderFooter( hs.getOddHeader(), ret );
        }

        return ret.toString();
    }

    /**
     * Grab the text from the footers
     * @deprecated 3.8 beta 4
     */
    @Deprecated
    public String getFooterText() {
        HeaderStories hs = new HeaderStories( doc );

        StringBuilder ret = new StringBuilder();
        if ( hs.getFirstFooter() != null ) {
            appendHeaderFooter( hs.getFirstFooter(), ret );
        }
        if ( hs.getEvenFooter() != null ) {
            appendHeaderFooter( hs.getEvenFooter(), ret );
        }
        if ( hs.getOddFooter() != null ) {
            appendHeaderFooter( hs.getOddFooter(), ret );
        }

        return ret.toString();
    }

    /**
     * Grab the text out of the text pieces. Might also include various bits of
     * crud, but will work in cases where the text piece -&gt; paragraph mapping is
     * broken. Fast too.
     */
    public String getTextFromPieces() {
        String text = doc.getDocumentText();

        // Fix line endings (Note - won't get all of them
        text = text.replace( "\r\r\r", "\r\n\r\n\r\n" );
        text = text.replace( "\r\r", "\r\n\r\n" );

        if ( text.endsWith( "\r" )) {
            text += "\n";
        }

        return text;
    }

    /**
     * Grab the text, based on the WordToTextConverter. Shouldn't include any
     * crud, but slower than getTextFromPieces().
     */
    @Override
    public String getText() {
        try {
            WordToTextConverter wordToTextConverter = new WordToTextConverter();

            HeaderStories hs = new HeaderStories(doc);

            if (hs.getFirstHeaderSubrange() != null)
                wordToTextConverter.processDocumentPart(doc,
                        hs.getFirstHeaderSubrange());
            if (hs.getEvenHeaderSubrange() != null)
                wordToTextConverter.processDocumentPart(doc,
                        hs.getEvenHeaderSubrange());
            if (hs.getOddHeaderSubrange() != null)
                wordToTextConverter.processDocumentPart(doc,
                        hs.getOddHeaderSubrange());

            wordToTextConverter.processDocument(doc);
            wordToTextConverter.processDocumentPart(doc,
                    doc.getMainTextboxRange());

            if (hs.getFirstFooterSubrange() != null)
                wordToTextConverter.processDocumentPart(doc,
                        hs.getFirstFooterSubrange());
            if (hs.getEvenFooterSubrange() != null)
                wordToTextConverter.processDocumentPart(doc,
                        hs.getEvenFooterSubrange());
            if (hs.getOddFooterSubrange() != null)
                wordToTextConverter.processDocumentPart(doc,
                        hs.getOddFooterSubrange());

            return wordToTextConverter.getText();
        } catch (RuntimeException e) {
            throw e;
        } catch ( Exception exc ) {
            throw new RuntimeException( exc );
        }
    }

    /**
     * Removes any fields (eg macros, page markers etc) from the string.
     */
    public static String stripFields( String text ) {
        return Range.stripFields( text );
    }

    @Override
    public HWPFDocument getDocument() {
        return doc;
    }

    @Override
    public void setCloseFilesystem(boolean doCloseFilesystem) {
        this.doCloseFilesystem = doCloseFilesystem;
    }

    @Override
    public boolean isCloseFilesystem() {
        return doCloseFilesystem;
    }

    @Override
    public HWPFDocument getFilesystem() {
        return doc;
    }
}
