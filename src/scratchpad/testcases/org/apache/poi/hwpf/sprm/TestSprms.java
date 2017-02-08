/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hwpf.sprm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import junit.framework.TestCase;
import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;

public class TestSprms extends TestCase
{
    private static HWPFDocument reload( HWPFDocument hwpfDocument )
            throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hwpfDocument.write( baos );
        return new HWPFDocument( new ByteArrayInputStream( baos.toByteArray() ) );
    }

    /**
     * Test correct processing of "sprmPItap" (0x6649) and "sprmPFInTable"
     * (0x2416)
     */
    public void testInnerTable() throws Exception
    {
        InputStream resourceAsStream = POIDataSamples.getDocumentInstance()
                .openResourceAsStream( "innertable.doc" );
        HWPFDocument hwpfDocument = new HWPFDocument( resourceAsStream );
        resourceAsStream.close();

        testInnerTable( hwpfDocument );
        hwpfDocument = reload( hwpfDocument );
        testInnerTable( hwpfDocument );
    }

    private void testInnerTable( HWPFDocument hwpfDocument )
    {
        Range range = hwpfDocument.getRange();
        for ( int p = 0; p < range.numParagraphs(); p++ )
        {
            Paragraph paragraph = range.getParagraph( p );
            char first = paragraph.text().toLowerCase(Locale.ROOT).charAt( 0 );
            if ( '1' <= first && first < '4' )
            {
                assertTrue( paragraph.isInTable() );
                assertEquals( 2, paragraph.getTableLevel() );
            }

            if ( 'a' <= first && first < 'z' )
            {
                assertTrue( paragraph.isInTable() );
                assertEquals( 1, paragraph.getTableLevel() );
            }
        }
    }

    /**
     * Test correct processing of "sprmPJc" by uncompressor
     */
    public void testSprmPJc() throws IOException
    {
        InputStream resourceAsStream = POIDataSamples.getDocumentInstance()
                .openResourceAsStream( "Bug49820.doc" );
        HWPFDocument hwpfDocument = new HWPFDocument( resourceAsStream );
        resourceAsStream.close();

        assertEquals( 1, hwpfDocument.getStyleSheet().getParagraphStyle( 8 )
                .getJustification() );

        hwpfDocument = reload( hwpfDocument );

        assertEquals( 1, hwpfDocument.getStyleSheet().getParagraphStyle( 8 )
                .getJustification() );

    }
}
