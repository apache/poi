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

import junit.framework.TestCase;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.HWPFTestDataSamples;

/**
 * API for BorderCode, see Bugzill 49919
 */
public final class TestBorderCode extends TestCase {

    private int pos = 0;
    private Range range;

    public void test() {
        HWPFDocument doc = HWPFTestDataSamples.openSampleFile("Bug49919.doc");
        range = doc.getRange();

        Paragraph par = findParagraph("Paragraph normal\r");
        assertEquals(0, par.getLeftBorder().getBorderType());
        assertEquals(0, par.getRightBorder().getBorderType());
        assertEquals(0, par.getTopBorder().getBorderType());
        assertEquals(0, par.getBottomBorder().getBorderType());

        par = findParagraph("Paragraph with border\r");
        assertEquals(18, par.getLeftBorder().getBorderType());
        assertEquals(17, par.getRightBorder().getBorderType());
        assertEquals(18, par.getTopBorder().getBorderType());
        assertEquals(17, par.getBottomBorder().getBorderType());
        assertEquals(15, par.getLeftBorder().getColor());

        par = findParagraph("Paragraph with red border\r");
        assertEquals(1, par.getLeftBorder().getBorderType());
        assertEquals(1, par.getRightBorder().getBorderType());
        assertEquals(1, par.getTopBorder().getBorderType());
        assertEquals(1, par.getBottomBorder().getBorderType());
        assertEquals(6, par.getLeftBorder().getColor());

        par = findParagraph("Paragraph with bordered words.\r");
        assertEquals(0, par.getLeftBorder().getBorderType());
        assertEquals(0, par.getRightBorder().getBorderType());
        assertEquals(0, par.getTopBorder().getBorderType());
        assertEquals(0, par.getBottomBorder().getBorderType());

        assertEquals(3, par.numCharacterRuns());
        CharacterRun chr = par.getCharacterRun(0);
        assertEquals(0, chr.getBorder().getBorderType());
        chr = par.getCharacterRun(1);
        assertEquals(1, chr.getBorder().getBorderType());
        assertEquals(0, chr.getBorder().getColor());
        chr = par.getCharacterRun(2);
        assertEquals(0, chr.getBorder().getBorderType());

        while (pos < range.numParagraphs() - 1) {
            par = range.getParagraph(pos++);
            if (par.isInTable())
                break;
        }

        assertEquals(true, par.isInTable());
        Table tab = range.getTable(par);

        // Border could be defined for the entire row, or for each cell, with the same visual appearance.
        assertEquals(2, tab.numRows());
        TableRow row = tab.getRow(0);
        assertEquals(1, row.getLeftBorder().getBorderType());
        assertEquals(1, row.getRightBorder().getBorderType());
        assertEquals(1, row.getTopBorder().getBorderType());
        assertEquals(1, row.getBottomBorder().getBorderType());

        assertEquals(2, row.numCells());
        TableCell cell = row.getCell(1);
        assertEquals(3, cell.getBrcTop().getBorderType());

        row = tab.getRow(1);
        cell = row.getCell(0);
        // 255 clears border inherited from row
        assertEquals(255, cell.getBrcBottom().getBorderType());
    }

    private Paragraph findParagraph(String expectedText) {
        while (pos < range.numParagraphs() - 1) {
            Paragraph par = range.getParagraph(pos);
            pos++;
            if (par.text().equals(expectedText))
                return par;
        }

        fail("Expected paragraph not found");

        // should never come here
        throw null;
    }

}
