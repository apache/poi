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

import static org.apache.poi.hwpf.HWPFTestDataSamples.openSampleFile;
import static org.apache.poi.hwpf.HWPFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Locale;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.junit.jupiter.api.Test;

public class TestSprms {
    /**
     * Test correct processing of "sprmPItap" (0x6649) and "sprmPFInTable"
     * (0x2416)
     */
    @Test
    void testInnerTable() throws Exception {
        try (HWPFDocument hwpfDocument = openSampleFile("innertable.doc")) {
            testInnerTable(hwpfDocument);
            try (HWPFDocument hwpfDocument2 = writeOutAndReadBack(hwpfDocument)) {
                testInnerTable(hwpfDocument2);
            }
        }
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
    @Test
    void testSprmPJc() throws IOException {
        try (HWPFDocument hwpfDocument = openSampleFile( "Bug49820.doc" )) {
            assertEquals(1, hwpfDocument.getStyleSheet().getParagraphStyle(8).getJustification());

            try (HWPFDocument hwpfDocument2 = writeOutAndReadBack(hwpfDocument)) {
                assertEquals(1, hwpfDocument2.getStyleSheet().getParagraphStyle(8).getJustification());
            }
        }
    }
}
