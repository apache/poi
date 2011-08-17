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
package org.apache.poi.hwpf.usermodel;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;

import junit.framework.TestCase;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;
import org.apache.poi.hwpf.extractor.Word6Extractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.model.FieldsDocumentPart;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.hwpf.model.PlexOfField;
import org.apache.poi.hwpf.model.SubdocumentType;
import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;

/**
 * Test different problems reported in Apache Bugzilla
 * 
 * @author Nick Burch (nick at torchbox dot com)
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
public class TestBugs extends TestCase
{

    public static void assertEquals( String expected, String actual )
    {
        String newExpected = expected.replaceAll( "\r\n", "\n" )
                .replaceAll( "\r", "\n" ).trim();
        String newActual = actual.replaceAll( "\r\n", "\n" )
                .replaceAll( "\r", "\n" ).trim();
        TestCase.assertEquals( newExpected, newActual );
    }

    private static void assertTableStructures( Range expected, Range actual )
    {
        assertEquals( expected.numParagraphs(), actual.numParagraphs() );
        for ( int p = 0; p < expected.numParagraphs(); p++ )
        {
            Paragraph expParagraph = expected.getParagraph( p );
            Paragraph actParagraph = actual.getParagraph( p );

            assertEquals( expParagraph.text(), actParagraph.text() );
            assertEquals( "Diffent isInTable flags for paragraphs #" + p
                    + " -- " + expParagraph + " -- " + actParagraph + ".",
                    expParagraph.isInTable(), actParagraph.isInTable() );
            assertEquals( expParagraph.isTableRowEnd(),
                    actParagraph.isTableRowEnd() );

            if ( expParagraph.isInTable() && actParagraph.isInTable() )
            {
                Table expTable, actTable;
                try
                {
                    expTable = expected.getTable( expParagraph );
                    actTable = actual.getTable( actParagraph );
                }
                catch ( Exception exc )
                {
                    continue;
                }

                assertEquals( expTable.numRows(), actTable.numRows() );
                assertEquals( expTable.numParagraphs(),
                        actTable.numParagraphs() );
            }
        }
    }

    static void fixed( String bugzillaId )
    {
        throw new Error(
                "Bug "
                        + bugzillaId
                        + " seems to be fixed. "
                        + "Please resolve the issue in Bugzilla and remove fail() from the test" );
    }

    private static void test47563_insertTable( int rows, int columns )
    {
        // POI apparently can't create a document from scratch,
        // so we need an existing empty dummy document
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "empty.doc" );

        Range range = doc.getRange();
        Table table = range.insertTableBefore( (short) columns, rows );
        table.sanityCheck();
        range.sanityCheck();

        for ( int rowIdx = 0; rowIdx < table.numRows(); rowIdx++ )
        {
            TableRow row = table.getRow( rowIdx );
            row.sanityCheck();
            for ( int colIdx = 0; colIdx < row.numCells(); colIdx++ )
            {
                TableCell cell = row.getCell( colIdx );
                cell.sanityCheck();

                Paragraph par = cell.getParagraph( 0 );
                par.sanityCheck();

                par.insertBefore( "" + ( rowIdx * row.numCells() + colIdx ) );

                par.sanityCheck();
                cell.sanityCheck();
                row.sanityCheck();
                table.sanityCheck();
                range.sanityCheck();
            }
        }

        String text = range.text();
        int mustBeAfter = 0;
        for ( int i = 0; i < rows * columns; i++ )
        {
            int next = text.indexOf( Integer.toString( i ), mustBeAfter );
            assertFalse( next == -1 );
            mustBeAfter = next;
        }
    }

    /**
     * Bug 33519 - HWPF fails to read a file
     */
    public void test33519()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug33519.doc" );
        WordExtractor extractor = new WordExtractor( doc );
        extractor.getText();
    }

    /**
     * Bug 34898 - WordExtractor doesn't read the whole string from the file
     */
    public void test34898()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug34898.doc" );
        WordExtractor extractor = new WordExtractor( doc );
        assertEquals( "\u30c7\u30a3\u30ec\u30af\u30c8\u30ea", extractor
                .getText().trim() );
    }

    /**
     * [RESOLVED INVALID] 41898 - Word 2003 pictures cannot be extracted
     */
    public void test41898()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug41898.doc" );
        List<Picture> pics = doc.getPicturesTable().getAllPictures();

        assertNotNull( pics );
        assertEquals( 1, pics.size() );

        Picture pic = pics.get( 0 );
        assertNotNull( pic.suggestFileExtension() );
        assertNotNull( pic.suggestFullFileName() );

        assertNotNull( pic.getContent() );
        assertNotNull( pic.getRawContent() );

        // These are probably some sort of offset, need to figure them out
        assertEquals( 4, pic.getSize() );
        assertEquals( 0x80000000l, LittleEndian.getUInt( pic.getContent() ) );
        assertEquals( 0x80000000l, LittleEndian.getUInt( pic.getRawContent() ) );

        /*
         * This is a file with empty EMF image, but present Office Drawing
         * --sergey
         */
        final Collection<OfficeDrawing> officeDrawings = doc
                .getOfficeDrawingsMain().getOfficeDrawings();
        assertNotNull( officeDrawings );
        assertEquals( 1, officeDrawings.size() );

        OfficeDrawing officeDrawing = officeDrawings.iterator().next();
        assertNotNull( officeDrawing );
        assertEquals( 1044, officeDrawing.getShapeId() );
    }

    /**
     * Bug 44331 - HWPFDocument.write destroys fields
     */
    public void test44431()
    {
        HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile( "Bug44431.doc" );
        WordExtractor extractor1 = new WordExtractor( doc1 );

        HWPFDocument doc2 = HWPFTestDataSamples.writeOutAndReadBack( doc1 );
        WordExtractor extractor2 = new WordExtractor( doc2 );

        assertEquals( extractor1.getFooterText(), extractor2.getFooterText() );
        assertEquals( extractor1.getHeaderText(), extractor2.getHeaderText() );
        assertEquals( Arrays.toString( extractor1.getParagraphText() ),
                Arrays.toString( extractor2.getParagraphText() ) );

        assertEquals( extractor1.getText(), extractor2.getText() );
    }

    /**
     * Bug 45473 - HWPF cannot read file after save
     */
    public void test45473()
    {
        HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile( "Bug45473.doc" );
        String text1 = new WordExtractor( doc1 ).getText().trim();

        HWPFDocument doc2 = HWPFTestDataSamples.writeOutAndReadBack( doc1 );
        String text2 = new WordExtractor( doc2 ).getText().trim();

        // the text in the saved document has some differences in line
        // separators but we tolerate that
        assertEquals( text1.replaceAll( "\n", "" ), text2.replaceAll( "\n", "" ) );
    }

    /**
     * Bug 46220 - images are not properly extracted
     */
    public void test46220()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug46220.doc" );
        // reference checksums as in Bugzilla
        String[] md5 = { "851be142bce6d01848e730cb6903f39e",
                "7fc6d8fb58b09ababd036d10a0e8c039",
                "a7dc644c40bc2fbf17b2b62d07f99248",
                "72d07b8db5fad7099d90bc4c304b4666" };
        List<Picture> pics = doc.getPicturesTable().getAllPictures();
        assertEquals( 4, pics.size() );
        for ( int i = 0; i < pics.size(); i++ )
        {
            Picture pic = pics.get( i );
            byte[] data = pic.getRawContent();
            // use Apache Commons Codec utils to compute md5
            assertEquals( md5[i], DigestUtils.md5Hex( data ) );
        }
    }

    /**
     * [RESOLVED FIXED] Bug 46817 - Regression: Text from some table cells
     * missing
     */
    public void test46817()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug46817.doc" );
        WordExtractor extractor = new WordExtractor( doc );
        String text = extractor.getText().trim();

        assertTrue( text.contains( "Nazwa wykonawcy" ) );
        assertTrue( text.contains( "kujawsko-pomorskie" ) );
        assertTrue( text.contains( "ekomel@ekomel.com.pl" ) );
    }

    /**
     * [FAILING] Bug 47286 - Word documents saves in wrong format if source
     * contains form elements
     * 
     * @throws IOException
     */
    public void test47286() throws IOException
    {
        HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile( "Bug47286.doc" );
        String text1 = new WordExtractor( doc1 ).getText().trim();

        HWPFDocument doc2 = HWPFTestDataSamples.writeOutAndReadBack( doc1 );
        String text2 = new WordExtractor( doc2 ).getText().trim();

        // the text in the saved document has some differences in line
        // separators but we tolerate that
        assertEquals( text1.replaceAll( "\n", "" ), text2.replaceAll( "\n", "" ) );

        assertEquals( doc1.getCharacterTable().getTextRuns().size(), doc2
                .getCharacterTable().getTextRuns().size() );

        List<PlexOfField> expectedFields = doc1.getFieldsTables()
                .getFieldsPLCF( FieldsDocumentPart.MAIN );
        List<PlexOfField> actualFields = doc2.getFieldsTables().getFieldsPLCF(
                FieldsDocumentPart.MAIN );
        assertEquals( expectedFields.size(), actualFields.size() );

        assertTableStructures( doc1.getRange(), doc2.getRange() );
    }

    /**
     * [RESOLVED FIXED] Bug 47287 - StringIndexOutOfBoundsException in
     * CharacterRun.replaceText()
     */
    public void test47287()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug47287.doc" );
        String[] values = { "1-1", "1-2", "1-3", "1-4", "1-5", "1-6", "1-7",
                "1-8", "1-9", "1-10", "1-11", "1-12", "1-13", "1-14", "1-15", };
        int usedVal = 0;
        String PLACEHOLDER = "\u2002\u2002\u2002\u2002\u2002";
        Range r = doc.getRange();
        for ( int x = 0; x < r.numSections(); x++ )
        {
            Section s = r.getSection( x );
            for ( int y = 0; y < s.numParagraphs(); y++ )
            {
                Paragraph p = s.getParagraph( y );

                for ( int z = 0; z < p.numCharacterRuns(); z++ )
                {
                    boolean isFound = false;

                    // character run
                    CharacterRun run = p.getCharacterRun( z );
                    // character run text
                    String text = run.text();
                    String oldText = text;
                    int c = text.indexOf( "FORMTEXT " );
                    if ( c < 0 )
                    {
                        int k = text.indexOf( PLACEHOLDER );
                        if ( k >= 0 )
                        {
                            text = text.substring( 0, k ) + values[usedVal]
                                    + text.substring( k + PLACEHOLDER.length() );
                            usedVal++;
                            isFound = true;
                        }
                    }
                    else
                    {
                        for ( ; c >= 0; c = text.indexOf( "FORMTEXT ", c
                                + "FORMTEXT ".length() ) )
                        {
                            int k = text.indexOf( PLACEHOLDER, c );
                            if ( k >= 0 )
                            {
                                text = text.substring( 0, k )
                                        + values[usedVal]
                                        + text.substring( k
                                                + PLACEHOLDER.length() );
                                usedVal++;
                                isFound = true;
                            }
                        }
                    }
                    if ( isFound )
                    {
                        run.replaceText( oldText, text, 0 );
                    }

                }
            }
        }

        String docText = r.text();

        assertTrue( docText.contains( "1-1" ) );
        assertTrue( docText.contains( "1-12" ) );

        assertFalse( docText.contains( "1-13" ) );
        assertFalse( docText.contains( "1-15" ) );
    }

    /**
     * [RESOLVED FIXED] Bug 47563 - Exception when working with table
     */
    public void test47563()
    {
        test47563_insertTable( 1, 5 );
        test47563_insertTable( 1, 6 );
        test47563_insertTable( 5, 1 );
        test47563_insertTable( 6, 1 );
        test47563_insertTable( 2, 2 );
        test47563_insertTable( 3, 2 );
        test47563_insertTable( 2, 3 );
        test47563_insertTable( 3, 3 );
    }

    /**
     * [RESOLVED FIXED] Bug 47731 - Word Extractor considers text copied from
     * some website as an embedded object
     */
    public void test47731() throws Exception
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug47731.doc" );
        String foundText = new WordExtractor( doc ).getText();

        assertTrue( foundText
                .contains( "Soak the rice in water for three to four hours" ) );
    }

    /**
     * Bug 4774 - text extracted by WordExtractor is broken
     */
    public void test47742() throws Exception
    {

        // (1) extract text from MS Word document via POI
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug47742.doc" );
        String foundText = new WordExtractor( doc ).getText();

        // (2) read text from text document (retrieved by saving the word
        // document as text file using encoding UTF-8)
        InputStream is = POIDataSamples.getDocumentInstance()
                .openResourceAsStream( "Bug47742-text.txt" );
        byte[] expectedBytes = IOUtils.toByteArray( is );
        String expectedText = new String( expectedBytes, "utf-8" )
                .substring( 1 ); // strip-off the unicode marker

        assertEquals( expectedText, foundText );
    }

    /**
     * [FAILING] Bug 47958 - Exception during Escher walk of pictures
     */
    public void test47958()
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug47958.doc" );
        try
        {
            for ( Picture pic : doc.getPicturesTable().getAllPictures() )
            {
                System.out.println( pic.suggestFullFileName() );
            }
            fixed( "47958" );
        }
        catch ( Exception e )
        {
            // expected exception
        }
    }

    /**
     * [RESOLVED FIXED] Bug 48065 - Problems with save output of HWPF (losing
     * formatting)
     */
    public void test48065()
    {
        HWPFDocument doc1 = HWPFTestDataSamples.openSampleFile( "Bug48065.doc" );
        HWPFDocument doc2 = HWPFTestDataSamples.writeOutAndReadBack( doc1 );

        Range expected = doc1.getRange();
        Range actual = doc2.getRange();

        assertEquals(
                expected.text().replace( "\r", "\n" ).replaceAll( "\n\n", "\n" ),
                actual.text().replace( "\r", "\n" ).replaceAll( "\n\n", "\n" ) );

        assertTableStructures( expected, actual );
    }

    public void test49933()
    {
        HWPFOldDocument document = HWPFTestDataSamples
                .openOldSampleFile( "Bug49933.doc" );

        Word6Extractor word6Extractor = new Word6Extractor( document );
        String text = word6Extractor.getText();

        assertTrue( text.contains( "best.wine.jump.ru" ) );
    }

    /**
     * Bug 50936 - HWPF fails to read a file
     */
    public void test50936()
    {
        HWPFTestDataSamples.openSampleFile( "Bug50936.doc" );
    }

    /**
     * [FAILING] Bug 50955 - error while retrieving the text file
     */
    public void test50955()
    {
        try
        {
            HWPFOldDocument doc = HWPFTestDataSamples
                    .openOldSampleFile( "Bug50955.doc" );
            Word6Extractor extractor = new Word6Extractor( doc );
            extractor.getText();

            fixed( "50955" );
        }
        catch ( Exception e )
        {
            // expected exception
        }
    }

    /**
     * Bug 51524 - PapBinTable constructor is slow
     */
    public void test51524()
    {
        HWPFTestDataSamples.openSampleFileFromArchive( "Bug51524.zip" );
    }

    /**
     * [RESOLVED FIXED] Bug 51604 - replace text fails for doc ( poi 3.8 beta
     * release from download site )
     */
    public void test51604()
    {
        HWPFDocument document = HWPFTestDataSamples
                .openSampleFile( "Bug51604.doc" );

        Range range = document.getRange();
        int numParagraph = range.numParagraphs();
        int counter = 0;
        for ( int i = 0; i < numParagraph; i++ )
        {
            Paragraph paragraph = range.getParagraph( i );
            int numCharRuns = paragraph.numCharacterRuns();
            for ( int j = 0; j < numCharRuns; j++ )
            {
                CharacterRun charRun = paragraph.getCharacterRun( j );
                String text = charRun.text();
                charRun.replaceText( text, "+" + ( ++counter ) );
            }
        }

        document = HWPFTestDataSamples.writeOutAndReadBack( document );
        String text = document.getDocumentText();
        assertEquals( "+1+2+3+4+5+6+7+8+9+10+11+12", text );
    }

    /**
     * [RESOLVED FIXED] Bug 51604 - replace text fails for doc ( poi 3.8 beta
     * release from download site )
     * 
     * @throws IOException
     * @throws FileNotFoundException
     */
    public void test51604p2() throws Exception
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug51604.doc" );

        Range range = doc.getRange();
        int numParagraph = range.numParagraphs();
        for ( int i = 0; i < numParagraph; i++ )
        {
            Paragraph paragraph = range.getParagraph( i );
            int numCharRuns = paragraph.numCharacterRuns();
            for ( int j = 0; j < numCharRuns; j++ )
            {
                CharacterRun charRun = paragraph.getCharacterRun( j );
                String text = charRun.text();
                if ( text.contains( "Header" ) )
                    charRun.replaceText( text, "added" );
            }
        }

        doc = HWPFTestDataSamples.writeOutAndReadBack( doc );
        final FileInformationBlock fileInformationBlock = doc
                .getFileInformationBlock();

        int totalLength = 0;
        for ( SubdocumentType type : SubdocumentType.values() )
        {
            final int partLength = fileInformationBlock
                    .getSubdocumentTextStreamLength( type );
            assert ( partLength >= 0 );

            totalLength += partLength;
        }
        assertEquals( doc.getText().length(), totalLength );
    }

    /**
     * [RESOLVED FIXED] Bug 51604 - replace text fails for doc ( poi 3.8 beta
     * release from download site )
     */
    public void test51604p3() throws Exception
    {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile( "Bug51604.doc" );

        byte[] originalData = new byte[doc.getFileInformationBlock()
                .getLcbDop()];
        System.arraycopy( doc.getTableStream(), doc.getFileInformationBlock()
                .getFcDop(), originalData, 0, originalData.length );

        HWPFOutputStream outputStream = new HWPFOutputStream();
        doc.getDocProperties().writeTo( outputStream );
        final byte[] oldData = outputStream.toByteArray();

        assertEquals( Arrays.toString( originalData ),
                Arrays.toString( oldData ) );

        Range range = doc.getRange();
        int numParagraph = range.numParagraphs();
        for ( int i = 0; i < numParagraph; i++ )
        {
            Paragraph paragraph = range.getParagraph( i );
            int numCharRuns = paragraph.numCharacterRuns();
            for ( int j = 0; j < numCharRuns; j++ )
            {
                CharacterRun charRun = paragraph.getCharacterRun( j );
                String text = charRun.text();
                if ( text.contains( "Header" ) )
                    charRun.replaceText( text, "added" );
            }
        }

        doc = HWPFTestDataSamples.writeOutAndReadBack( doc );

        outputStream = new HWPFOutputStream();
        doc.getDocProperties().writeTo( outputStream );
        final byte[] newData = outputStream.toByteArray();

        assertEquals( Arrays.toString( oldData ), Arrays.toString( newData ) );
    }

    /**
     * [RESOLVED FIXED] Bug 51671 - HWPFDocument.write based on NPOIFSFileSystem
     * throws a NullPointerException
     */
    public void test51671() throws Exception
    {
        InputStream is = POIDataSamples.getDocumentInstance()
                .openResourceAsStream( "empty.doc" );
        NPOIFSFileSystem npoifsFileSystem = new NPOIFSFileSystem( is );
        HWPFDocument hwpfDocument = new HWPFDocument(
                npoifsFileSystem.getRoot() );
        hwpfDocument.write( new ByteArrayOutputStream() );
    }
}
