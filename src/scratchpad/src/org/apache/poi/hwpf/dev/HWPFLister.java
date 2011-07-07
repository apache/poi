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

import java.io.FileInputStream;
import java.util.Arrays;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFDocumentCore;
import org.apache.poi.hwpf.model.FileInformationBlock;
import org.apache.poi.hwpf.model.PAPX;
import org.apache.poi.hwpf.sprm.SprmIterator;
import org.apache.poi.hwpf.sprm.SprmOperation;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

/**
 * Used by developers to list out key information on a HWPF file. End users will
 * probably never need to use this program.
 * 
 * @author Nick Burch (nick at torchbox dot com)
 * @author Sergey Vladimirov (vlsergey at gmail dot com)
 */
public final class HWPFLister
{
    public static void main( String[] args ) throws Exception
    {
        if ( args.length == 0 )
        {
            System.err.println( "Use:" );
            System.err.println( "   HWPFLister <filename> "
                    + "[--papx] [--papxProperties] "
                    + "[--paragraphs] [--paragraphsSprms] [--paragraphsText]" );
            System.exit( 1 );
        }

        boolean outputParagraphs = false;
        boolean outputParagraphsSprms = false;
        boolean outputParagraphsText = false;

        boolean outputPapx = false;
        boolean outputPapxProperties = false;

        for ( String arg : Arrays.asList( args ).subList( 1, args.length ) )
        {
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
        }

        HWPFLister lister = new HWPFLister( new HWPFDocument(
                new FileInputStream( args[0] ) ) );
        lister.dumpFIB();

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
        Range range = _doc.getRange();
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
}
