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

package org.apache.poi.xwpf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.XWPFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHeight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHeightRule;

class TestXWPFTableRow {

    @Test
    void testCreateRow() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(1, 1);
            XWPFTableRow tr = table.createRow();
            assertNotNull(tr);
        }
    }

    @Test
    void testSetGetCantSplitRow() throws IOException {
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(1, 1);
            // table has a single row by default; grab it
            XWPFTableRow tr = table.getRow(0);
            assertNotNull(tr);

            // Assert the repeat header is false by default
            boolean isCantSplit = tr.isCantSplitRow();
            assertFalse(isCantSplit);

            // Repeat the header
            tr.setCantSplitRow(true);
            isCantSplit = tr.isCantSplitRow();
            assertTrue(isCantSplit);

            // Make the header no longer repeating
            tr.setCantSplitRow(false);
            isCantSplit = tr.isCantSplitRow();
            assertFalse(isCantSplit);
        }
    }

    @Test
    void testSetGetRepeatHeader() throws IOException {
        // create a table
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTable table = doc.createTable(3, 1);
            // table has a single row by default; grab it
            XWPFTableRow tr = table.getRow(0);
            assertNotNull(tr);

            // Assert the repeat header is false by default
            boolean isRpt = tr.isRepeatHeader();
            assertFalse(isRpt);

            // Repeat the header
            tr.setRepeatHeader(true);
            isRpt = tr.isRepeatHeader();
            assertTrue(isRpt);

            // Make the header no longer repeating
            tr.setRepeatHeader(false);
            isRpt = tr.isRepeatHeader();
            assertFalse(isRpt);

            // If the third row is set to repeat, but not the second,
            // isRepeatHeader should report false because Word will
            // ignore it.
            tr = table.getRow(2);
            tr.setRepeatHeader(true);
            isRpt = tr.isRepeatHeader();
            assertFalse(isRpt);
        }
    }

    // Test that validates the table header value can be parsed from a document
    // generated in Word
    @Test
    void testIsRepeatHeader() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples
                .openSampleDocument("Bug60337.docx")) {
            XWPFTable table = doc.getTables().get(0);
            XWPFTableRow tr = table.getRow(0);
            boolean isRpt = tr.isRepeatHeader();
            assertTrue(isRpt);

            tr = table.getRow(1);
            isRpt = tr.isRepeatHeader();
            assertFalse(isRpt);

            tr = table.getRow(2);
            isRpt = tr.isRepeatHeader();
            assertFalse(isRpt);
        }
    }


    // Test that validates the table header value can be parsed from a document
    // generated in Word
    @Test
    void testIsCantSplit() throws Exception {
        try (XWPFDocument doc = XWPFTestDataSamples
                .openSampleDocument("Bug60337.docx")) {
            XWPFTable table = doc.getTables().get(0);
            XWPFTableRow tr = table.getRow(0);
            boolean isCantSplit = tr.isCantSplitRow();
            assertFalse(isCantSplit);

            tr = table.getRow(1);
            isCantSplit = tr.isCantSplitRow();
            assertFalse(isCantSplit);

            tr = table.getRow(2);
            isCantSplit = tr.isCantSplitRow();
            assertTrue(isCantSplit);
        }
    }

    @Test
    void testRemoveCell() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTableRow tr = doc.createTable(1, 1).createRow();

            assertEquals(1, tr.getTableCells().size());
            assertEquals(tr.getTableCells().size(), tr.getCtRow().sizeOfTcArray());

            tr.removeCell(0);
            assertEquals(0, tr.getTableCells().size());
            assertEquals(tr.getTableCells().size(), tr.getCtRow().sizeOfTcArray());
        }
    }

    @Test
    void testGetSetHeightRule() throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {
            XWPFTableRow tr = doc.createTable(1, 1).createRow();
            assertEquals(TableRowHeightRule.AUTO, tr.getHeightRule());

            tr.setHeightRule(TableRowHeightRule.AT_LEAST);
            assertEquals(TableRowHeightRule.AT_LEAST, tr.getHeightRule());

            tr.setHeightRule(TableRowHeightRule.EXACT);
            assertEquals(TableRowHeightRule.EXACT, tr.getHeightRule());
        }
    }

    @Test
    void testBug62174() throws IOException {
        try (XWPFDocument doc = XWPFTestDataSamples.openSampleDocument("Bug60337.docx")) {
            XWPFTable table = doc.getTables().get(0);
            XWPFTableRow tr = table.getRow(0);

            int twipsPerInch =  1440;
            tr.setHeight(twipsPerInch/10);
            CTHeight height = tr.getCtRow().getTrPr().getTrHeightArray(0);
            height.setHRule(STHeightRule.EXACT);
            assertEquals(twipsPerInch/10., Units.toDXA(POIXMLUnits.parseLength(height.xgetVal())), 0);
        }
    }
}
