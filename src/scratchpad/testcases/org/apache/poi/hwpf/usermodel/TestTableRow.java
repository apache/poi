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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hwpf.HWPFDocument;
import org.junit.Test;

public class TestTableRow {
    private static final POIDataSamples SAMPLES = POIDataSamples.getDocumentInstance();
    
    @Test
    public void testInnerTableCellsDetection() throws IOException {
        InputStream is = SAMPLES.openResourceAsStream( "innertable.doc" );
        HWPFDocument hwpfDocument = new HWPFDocument( is );
        is.close();
        hwpfDocument.getRange();

        Range documentRange = hwpfDocument.getRange();
        Paragraph startOfInnerTable = documentRange.getParagraph( 6 );

        Table innerTable = documentRange.getTable( startOfInnerTable );
        assertEquals( 2, innerTable.numRows() );

        TableRow tableRow = innerTable.getRow( 0 );
        assertEquals( 2, tableRow.numCells() );
        hwpfDocument.close();
    }

    @Test
    public void testOuterTableCellsDetection() throws IOException {
        InputStream is = SAMPLES.openResourceAsStream( "innertable.doc" );
        HWPFDocument hwpfDocument = new HWPFDocument( is );
        is.close();
        hwpfDocument.getRange();

        Range documentRange = hwpfDocument.getRange();
        Paragraph startOfOuterTable = documentRange.getParagraph( 0 );

        Table outerTable = documentRange.getTable( startOfOuterTable );
        assertEquals( 3, outerTable.numRows() );

        assertEquals( 3, outerTable.getRow( 0 ).numCells() );
        assertEquals( 3, outerTable.getRow( 1 ).numCells() );
        assertEquals( 3, outerTable.getRow( 2 ).numCells() );
        
        hwpfDocument.close();
    }

}
