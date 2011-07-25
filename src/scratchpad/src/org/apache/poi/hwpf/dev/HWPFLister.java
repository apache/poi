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

package org.apache.poi.hwpf.dev;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.model.CHPX;
import org.apache.poi.hwpf.model.FieldsDocumentPart;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.model.StyleSheet;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.sprm.SprmIterator;
import org.apache.poi.hwpf.sprm.SprmOperation;
import org.apache.poi.hwpf.usermodel.Bookmark;
import org.apache.poi.hwpf.usermodel.Bookmarks;
import org.apache.poi.hwpf.usermodel.Field;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Picture;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;

/**
 * Used by developers to list out key information on a HWPF file. End users will
 * probably never need to use this program.
 * 
 * @author Nick Burch (nick at torchbox dot com)
 * @author Sergey Vladimirov (vlsergey at gmail dot com)
 */
public final class HWPFLister
{
    private static HWPFDocumentCore loadDoc( File docFile ) throws IOException
    {
        final FileInputStream istream = new FileInputStream( docFile );
        try
        {
            return loadDoc( istream );
        }
        finally
        {
            IOUtils.closeQuietly( istream );
        }
    }

    private static HWPFDocumentCore loadDoc( InputStream inputStream )
            throws IOException
    {
        final POIFSFileSystem poifsFileSystem = HWPFDocumentCore
                .verifyAndBuildPOIFS( inputStream );
        try
        {
            return new HWPFDocument( poifsFileSystem );
        }
        catch ( OldWordFileFormatException exc )
        {
            return new HWPFOldDocument( poifsFileSystem );
        }
    }

    public static void main( String[] args ) throws Exception
    {
        if ( args.length == 0 )
        {
            System.err.println( "Use:" );
            System.err
                    .println( "\tHWPFLister <filename>\n"
                            + "\t\t[--textPieces] [--textPiecesText]\n"
                            + "\t\t[--chpx] [--chpxProperties] [--chpxSprms]\n"
                            + "\t\t[--papx] [--papxProperties]\n"
                            + "\t\t[--paragraphs] [--paragraphsSprms] [--paragraphsText]\n"
                            + "\t\t[--bookmarks]\n" +"\t\t[--fields]\n" + "\t\t[--pictures]\n"
                            + "\t\t[--writereadback]\n" );
            System.exit( 1 );
        }

        boolean outputTextPieces = false;
        boolean outputTextPiecesText = false;

        boolean outputChpx = false;
        boolean outputChpxProperties = false;
        boolean outputChpxSprms = false;

        boolean outputParagraphs = false;
        boolean outputParagraphsSprms = false;
        boolean outputParagraphsText = false;

        boolean outputPapx = false;
        boolean outputPapxProperties = false;

        boolean outputBookmarks = false;
        boolean outputFields = false;
        boolean outputPictures = false;

        boolean writereadback = false;

        for ( String arg : Arrays.asList( args ).subList( 1, args.length ) )
        {
            if ( "--textPieces".equals( arg ) )
                outputTextPieces = true;
            if ( "--textPiecesText".equals( arg ) )
                outputTextPiecesText = true;

            if ( "--chpx".equals( arg ) )
                outputChpx = true;
            if ( "--chpxProperties".equals( arg ) )
                outputChpxProperties = true;
            if ( "--chpxSprms".equals( arg ) )
                outputChpxSprms = true;

            if ( "--paragraphs".equals( arg ) )
                outputParagraphs = true;
            if ( "--paragraphsSprms".equals( arg ) )
                outputParagraphsSprms = true;
            if ( "--paragraphsText".equals( arg ) )
                outputParagraphsText = true;

            if ( "--papx".equals( arg ) )
                outputPapx = true;
            if ( "--papxProperties".equals( arg ) )
                outputPapxProperties = true;

            if ( "--bookmarks".equals( arg ) )
                outputBookmarks = true;
            if ( "--fields".equals( arg ) )
                outputFields = true;
            if ( "--pictures".equals( arg ) )
                outputPictures = true;

            if ( "--writereadback".equals( arg ) )
                writereadback = true;
        }

        HWPFDocumentCore doc = loadDoc( new File( args[0] ) );
        if ( writereadback )
            doc = writeOutAndReadBack( doc );

        HWPFLister lister = new HWPFLister( doc );
        lister.dumpFIB();

        if ( outputTextPieces )
        {
            System.out.println( "== Text pieces ==" );
            lister.dumpTextPieces( outputTextPiecesText );
        }

        if ( outputChpx )
        {
            System.out.println( "== CHPX ==" );
            lister.dumpChpx( outputChpxProperties, outputChpxSprms );
        }

        if ( outputPapx )
        {
            System.out.println( "== PAPX ==" );
            lister.dumpPapx( outputPapxProperties );
        }

        if ( outputParagraphs )
        {
            System.out.println( "== Text paragraphs ==" );
            lister.dumpParagraphs( true );

            System.out.println( "== DOM paragraphs ==" );
            lister.dumpParagraphsDom( outputParagraphsSprms, outputPapx,
                    outputParagraphsText );
        }

        if ( outputBookmarks )
        {
            System.out.println( "== BOOKMARKS ==" );
            lister.dumpBookmarks();
        }

        if ( outputFields )
        {
            System.out.println( "== FIELDS ==" );
            lister.dumpFields();
        }

        if ( outputPictures )
        {
            System.out.println( "== PICTURES ==" );
            lister.dumpPictures();
        }
    }

    private static HWPFDocumentCore writeOutAndReadBack(
            HWPFDocumentCore original )
    {
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream( 4096 );
            original.write( baos );
            ByteArrayInputStream bais = new ByteArrayInputStream(
                    baos.toByteArray() );
            return loadDoc( bais );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( e );
        }
    }

    private final HWPFDocumentCore _doc;

    private LinkedHashMap<Integer, String> paragraphs;

    public HWPFLister( HWPFDocumentCore doc )
    {
        _doc = doc;

        buildParagraphs();
    }

    private void buildParagraphs()
    {
        paragraphs = new LinkedHashMap<Integer, String>();

        StringBuilder part = new StringBuilder();
        String text = _doc.getDocumentText();
        for ( int charIndex = 0; charIndex < text.length(); charIndex++ )
        {
            char c = text.charAt( charIndex );
            part.append( c );
            if ( c == 13 || c == 7 || c == 12 )
            {
                paragraphs.put( Integer.valueOf( charIndex ), part.toString() );
                part.setLength( 0 );
            }
        }
    }

    private void dumpBookmarks()
    {
        if ( !( _doc instanceof HWPFDocument ) )
        {
            System.out.println( "Word 95 not supported so far" );
            return;
        }

        HWPFDocument document = (HWPFDocument) _doc;
        Bookmarks bookmarks = document.getBookmarks();
        for ( int b = 0; b < bookmarks.getBookmarksCount(); b++ )
        {
            Bookmark bookmark = bookmarks.getBookmark( b );
            System.out.println( "[" + bookmark.getStart() + "; "
                    + bookmark.getEnd() + "): " + bookmark.getName() );
        }
    }

    public void dumpChpx( boolean withProperties, boolean withSprms )
    {
        for ( CHPX chpx : _doc.getCharacterTable().getTextRuns() )
        {
            System.out.println( chpx );

            if ( withProperties )
            {
                System.out.println( chpx.getCharacterProperties(
                        _doc.getStyleSheet(), (short) StyleSheet.NIL_STYLE ) );
            }

            if ( withSprms )
            {
                SprmIterator sprmIt = new SprmIterator( chpx.getGrpprl(), 0 );
                while ( sprmIt.hasNext() )
                {
                    SprmOperation sprm = sprmIt.next();
                    System.out.println( "\t" + sprm.toString() );
                }
            }

            if ( true )
            {
                String text = new Range( chpx.getStart(), chpx.getEnd(),
                        _doc.getOverallRange() )
                {
                    public String toString()
                    {
                        return "CHPX range (" + super.toString() + ")";
                    }
                }.text();
                StringBuilder stringBuilder = new StringBuilder();
                for ( char c : text.toCharArray() )
                {
                    if ( c < 30 )
                        stringBuilder
                                .append( "\\0x" + Integer.toHexString( c ) );
                    else
                        stringBuilder.append( c );
                }
                System.out.println( stringBuilder );
            }
        }
    }

    public void dumpFIB()
    {
        FileInformationBlock fib = _doc.getFileInformationBlock();
        System.out.println( fib );

    }

    private void dumpFields()
    {
        if ( !( _doc instanceof HWPFDocument ) )
        {
            System.out.println( "Word 95 not supported so far" );
            return;
        }

        HWPFDocument document = (HWPFDocument) _doc;

        for ( FieldsDocumentPart part : FieldsDocumentPart.values() )
        {
            System.out.println( "=== Document part: " + part + " ===" );
            for ( Field field : document.getFields().getFields( part ) )
            {
                System.out.println( field );
            }
        }
    }

    public void dumpPapx( boolean withProperties ) throws Exception
    {
//        if ( _doc instanceof HWPFDocument )
//        {
//            System.out.println( "binary PAP pages " );
//
//            HWPFDocument doc = (HWPFDocument) _doc;
//
//            java.lang.reflect.Field fMainStream = HWPFDocumentCore.class
//                    .getDeclaredField( "_mainStream" );
//            fMainStream.setAccessible( true );
//            byte[] mainStream = (byte[]) fMainStream.get( _doc );
//
//            PlexOfCps binTable = new PlexOfCps( doc.getTableStream(), doc
//                    .getFileInformationBlock().getFcPlcfbtePapx(), doc
//                    .getFileInformationBlock().getLcbPlcfbtePapx(), 4 );
//
//            List<PAPX> papxs = new ArrayList<PAPX>();
//
//            int length = binTable.length();
//            for ( int x = 0; x < length; x++ )
//            {
//                GenericPropertyNode node = binTable.getProperty( x );
//
//                int pageNum = LittleEndian.getInt( node.getBytes() );
//                int pageOffset = POIFSConstants.SMALLER_BIG_BLOCK_SIZE
//                        * pageNum;
//
//                PAPFormattedDiskPage pfkp = new PAPFormattedDiskPage(
//                        mainStream, doc.getDataStream(), pageOffset,
//                        doc.getTextTable() );
//
//                System.out.println( "* PFKP: " + pfkp );
//
//                for ( PAPX papx : pfkp.getPAPXs() )
//                {
//                    System.out.println( "** " + papx );
//                    papxs.add( papx );
//                    if ( papx != null && true )
//                    {
//                        SprmIterator sprmIt = new SprmIterator(
//                                papx.getGrpprl(), 2 );
//                        while ( sprmIt.hasNext() )
//                        {
//                            SprmOperation sprm = sprmIt.next();
//                            System.out.println( "*** " + sprm.toString() );
//                        }
//                    }
//
//                }
//            }
//
//            Collections.sort( papxs );
//            System.out.println( "* Sorted by END" );
//            for ( PAPX papx : papxs )
//            {
//                System.out.println( "** " + papx );
//                SprmIterator sprmIt = new SprmIterator( papx.getGrpprl(), 2 );
//                while ( sprmIt.hasNext() )
//                {
//                    SprmOperation sprm = sprmIt.next();
//                    System.out.println( "*** " + sprm.toString() );
//                }
//            }
//        }

        // for ( PAPX papx : _doc.getParagraphTable().getParagraphs() )
        // {
        // System.out.println( papx );
        //
        // if ( withProperties )
        // System.out.println( papx.getParagraphProperties( _doc
        // .getStyleSheet() ) );
        //
        // if ( true )
        // {
        // SprmIterator sprmIt = new SprmIterator( papx.getGrpprl(), 2 );
        // while ( sprmIt.hasNext() )
        // {
        // SprmOperation sprm = sprmIt.next();
        // System.out.println( "\t" + sprm.toString() );
        // }
        // }
        // }
    }

    public void dumpParagraphs( boolean dumpAssotiatedPapx )
    {
        for ( Map.Entry<Integer, String> entry : paragraphs.entrySet() )
        {
            Integer endOfParagraphCharOffset = entry.getKey();
            System.out.println( "[...; " + ( endOfParagraphCharOffset + 1 )
                    + "): " + entry.getValue() );

            if ( dumpAssotiatedPapx )
            {
                boolean hasAssotiatedPapx = false;
                for ( PAPX papx : _doc.getParagraphTable().getParagraphs() )
                {
                    if ( papx.getStart() <= endOfParagraphCharOffset.intValue()
                            && endOfParagraphCharOffset.intValue() < papx
                                    .getEnd() )
                    {
                        hasAssotiatedPapx = true;
                        System.out.println( "* " + papx );

                        SprmIterator sprmIt = new SprmIterator(
                                papx.getGrpprl(), 2 );
                        while ( sprmIt.hasNext() )
                        {
                            SprmOperation sprm = sprmIt.next();
                            System.out.println( "** " + sprm.toString() );
                        }
                    }
                }
                if ( !hasAssotiatedPapx )
                {
                    System.out.println( "* "
                            + "NO PAPX ASSOTIATED WITH PARAGRAPH!" );
                }
            }
        }
    }

    public void dumpParagraphsDom( boolean withSprms, boolean withPapx,
            boolean withText )
    {
        Range range = _doc.getOverallRange();
        for ( int p = 0; p < range.numParagraphs(); p++ )
        {
            Paragraph paragraph = range.getParagraph( p );
            System.out.println( p + ":\t" + paragraph.toString() );

            if ( withText )
                System.out.println( paragraph.text() );
        }
    }

    private void dumpPictures()
    {
        if ( _doc instanceof HWPFOldDocument )
        {
            System.out.println( "Word 95 not supported so far" );
            return;
        }

        List<Picture> allPictures = ( (HWPFDocument) _doc ).getPicturesTable()
                .getAllPictures();
        for ( Picture picture : allPictures )
        {
            System.out.println( picture.toString() );
        }
    }

    public void dumpTextPieces( boolean withText )
    {
        for ( TextPiece textPiece : _doc.getTextTable().getTextPieces() )
        {
            System.out.println( textPiece );

            if ( withText )
            {
                System.out.println( "\t" + textPiece.getStringBuffer() );
            }
        }
    }
}
