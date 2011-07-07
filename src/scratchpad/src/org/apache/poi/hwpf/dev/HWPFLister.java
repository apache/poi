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

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.HWPFOldDocument;
import org.apache.poi.hwpf.OldWordFileFormatException;
import org.apache.poi.hwpf.model.CHPX;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.model.TextPiece;
import org.apache.poi.hwpf.sprm.SprmIterator;
import org.apache.poi.hwpf.sprm.SprmOperation;
import org.apache.poi.hwpf.usermodel.Paragraph;
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
                            + "\t\t[--textRuns] [--textRunsSprms]\n"
                            + "\t\t[--papx] [--papxProperties]\n"
                            + "\t\t[--paragraphs] [--paragraphsSprms] [--paragraphsText]\n"
                            + "\t\t[--writereadback]\n" );
            System.exit( 1 );
        }

        boolean outputTextPieces = false;
        boolean outputTextPiecesText = false;

        boolean outputTextRuns = false;
        boolean outputTextRunsSprms = false;

        boolean outputParagraphs = false;
        boolean outputParagraphsSprms = false;
        boolean outputParagraphsText = false;

        boolean outputPapx = false;
        boolean outputPapxProperties = false;

        boolean writereadback = false;

        for ( String arg : Arrays.asList( args ).subList( 1, args.length ) )
        {
            if ( "--textPieces".equals( arg ) )
                outputTextPieces = true;
            if ( "--textPiecesText".equals( arg ) )
                outputTextPiecesText = true;

            if ( "--textRuns".equals( arg ) )
                outputTextRuns = true;
            if ( "--textRunsSprms".equals( arg ) )
                outputTextRunsSprms = true;

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

        if ( outputTextRuns )
        {
            System.out.println( "== Text runs ==" );
            lister.dumpTextRuns( outputTextRunsSprms );
        }

        if ( outputParagraphs )
        {
            System.out.println( "== Paragraphs ==" );
            lister.dumpParagraphs( outputParagraphsSprms, outputPapx,
                    outputParagraphsText );
        }

        if ( !outputParagraphs && outputPapx )
        {
            System.out.println( "== PAPX ==" );
            lister.dumpPapx( outputPapxProperties );
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

    public HWPFLister( HWPFDocumentCore doc )
    {
        _doc = doc;
    }

    public void dumpFIB()
    {
        FileInformationBlock fib = _doc.getFileInformationBlock();
        System.out.println( fib );
    }

    public void dumpPapx( boolean withProperties )
    {
        for ( PAPX papx : _doc.getParagraphTable().getParagraphs() )
        {
            System.out.println( papx );

            if ( withProperties )
                System.out.println( papx.getParagraphProperties( _doc
                        .getStyleSheet() ) );
        }
    }

    public void dumpParagraphs( boolean withSprms, boolean withPapx,
            boolean withText )
    {
        Range range = _doc.getOverallRange();
        for ( int p = 0; p < range.numParagraphs(); p++ )
        {
            Paragraph paragraph = range.getParagraph( p );
            System.out.println( p + ":\t" + paragraph.toString( withPapx ) );

            if ( withSprms )
            {
                PAPX papx = _doc.getParagraphTable().getParagraphs().get( p );

                SprmIterator sprmIt = new SprmIterator( papx.getGrpprl(), 2 );
                while ( sprmIt.hasNext() )
                {
                    SprmOperation sprm = sprmIt.next();
                    System.out.println( "\t" + sprm.toString() );
                }
            }

            if ( withText )
                System.out.println( paragraph.text() );
        }
    }

    public void dumpTextRuns( boolean withSprms )
    {
        for ( CHPX chpx  : _doc.getCharacterTable().getTextRuns() )
        {
            System.out.println( chpx );

            if ( withSprms )
            {
                SprmIterator sprmIt = new SprmIterator( chpx.getGrpprl(), 2 );
                while ( sprmIt.hasNext() )
                {
                    SprmOperation sprm = sprmIt.next();
                    System.out.println( "\t" + sprm.toString() );
                }
            }
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
