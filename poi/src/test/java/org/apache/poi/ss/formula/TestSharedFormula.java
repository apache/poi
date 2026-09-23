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

package org.apache.poi.ss.formula;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.AddPtg;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.Area3DPxg;
import org.apache.poi.ss.formula.ptg.AreaNPtg;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.formula.ptg.Ref3DPxg;
import org.apache.poi.ss.formula.ptg.RefNPtg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.util.LittleEndianByteArrayInputStream;
import org.junit.jupiter.api.Test;

/**
 * Tests {@link SharedFormula#convertSharedFormulas(Ptg[], int, int)}: relative references are
 * moved to the cell the shared formula is being resolved for, and a reference to another sheet
 * must stay a reference to that sheet.
 */
final class TestSharedFormula {

    private static final int ROW_AND_COL_RELATIVE = 0xC000;

    /** a tRefN token: signed row offset, column offset with the relative flags */
    private static RefNPtg refN(int rowOffset, int colOffset) {
        byte[] data = {
                (byte) rowOffset, (byte) (rowOffset >> 8),
                (byte) colOffset, (byte) ((colOffset >> 8) | (ROW_AND_COL_RELATIVE >> 8))
        };
        return new RefNPtg(new LittleEndianByteArrayInputStream(data));
    }

    /** a tAreaN token covering a single-column, {@code rows}-row range starting at the given offset */
    private static AreaNPtg areaN(int firstRowOffset, int rows, int colOffset) {
        int lastRowOffset = firstRowOffset + rows - 1;
        byte[] data = {
                (byte) firstRowOffset, (byte) (firstRowOffset >> 8),
                (byte) lastRowOffset, (byte) (lastRowOffset >> 8),
                (byte) colOffset, (byte) ((colOffset >> 8) | (ROW_AND_COL_RELATIVE >> 8)),
                (byte) colOffset, (byte) ((colOffset >> 8) | (ROW_AND_COL_RELATIVE >> 8)),
        };
        return new AreaNPtg(new LittleEndianByteArrayInputStream(data));
    }

    private static SheetIdentifier sheet(String name) {
        return new SheetIdentifier(null, new NameIdentifier(name, false));
    }

    @Test
    void relativeReferencesBecomePlainReferencesAtTheTargetCell() {
        // shared formula "=B1+SUM(A1:A3)" written relative to its master cell (HSSF encodes it
        // with tRefN/tAreaN offsets), resolved for the cell in row 5, column 2
        Ptg[] shared = {refN(-1, 1), areaN(-1, 3, -1), AddPtg.instance};
        Ptg[] converted = new SharedFormula(SpreadsheetVersion.EXCEL97).convertSharedFormulas(shared, 5, 2);

        RefPtg ref = assertInstanceOf(RefPtg.class, converted[0]);
        assertEquals(4, ref.getRow());
        assertEquals(3, ref.getColumn());
        assertTrue(ref.isRowRelative());
        assertTrue(ref.isColRelative());

        AreaPtg area = assertInstanceOf(AreaPtg.class, converted[1]);
        assertEquals(4, area.getFirstRow());
        assertEquals(6, area.getLastRow());
        assertEquals(1, area.getFirstColumn());
        assertEquals(1, area.getLastColumn());

        assertEquals(AddPtg.instance, converted[2]);
    }

    @Test
    void xssfReferencesToOtherSheetsKeepTheirSheet() {
        // XSSF stores the master's text and parses it to Ref3DPxg/Area3DPxg with the master's
        // coordinates; the conversion for a dependent cell gets the offset from the master
        Ref3DPxg relativeRef = new Ref3DPxg(sheet("Prices"), "B2");
        Area3DPxg absoluteArea = new Area3DPxg(sheet("Prices"), "$A$2:$B$4");
        Area3DPxg relativeArea = new Area3DPxg(sheet("Prices"), "A2:B4");
        Ptg[] shared = {relativeRef, absoluteArea, relativeArea};

        // dependent cell two rows below and one column right of the master
        Ptg[] converted = new SharedFormula(SpreadsheetVersion.EXCEL2007).convertSharedFormulas(shared, 2, 1);

        Ref3DPxg ref = assertInstanceOf(Ref3DPxg.class, converted[0]);
        assertNotSame(relativeRef, ref);
        assertEquals("Prices", ref.getSheetName());
        assertEquals(3, ref.getRow());
        assertEquals(2, ref.getColumn());
        assertEquals("Prices!C4", ref.toFormulaString());

        Area3DPxg area = assertInstanceOf(Area3DPxg.class, converted[1]);
        assertEquals("Prices!$A$2:$B$4", area.toFormulaString());

        Area3DPxg moved = assertInstanceOf(Area3DPxg.class, converted[2]);
        assertEquals("Prices!B4:C6", moved.toFormulaString());
        // the originals are untouched
        assertEquals("Prices!A2:B4", relativeArea.toFormulaString());
    }

    @Test
    void hssfReferencesToOtherSheetsKeepTheirSheet() {
        Ref3DPtg ref3D = new Ref3DPtg("B2", 7);
        Area3DPtg area3D = new Area3DPtg("$A$2:$B$4", 7);
        Ptg[] shared = {ref3D, area3D};

        Ptg[] converted = new SharedFormula(SpreadsheetVersion.EXCEL97).convertSharedFormulas(shared, 1, 0);

        Ref3DPtg ref = assertInstanceOf(Ref3DPtg.class, converted[0]);
        assertEquals(7, ref.getExternSheetIndex());
        assertEquals(2, ref.getRow());
        assertEquals(1, ref.getColumn());

        Area3DPtg area = assertInstanceOf(Area3DPtg.class, converted[1]);
        assertEquals(7, area.getExternSheetIndex());
        assertEquals(1, area.getFirstRow());
        assertEquals(3, area.getLastRow());
        assertEquals(0, area.getFirstColumn());
        assertEquals(1, area.getLastColumn());
    }

    @Test
    void operandClassIsPreserved() {
        Ref3DPxg ref = new Ref3DPxg(sheet("Prices"), "B2");
        ref.setClass(Ptg.CLASS_REF);
        RefNPtg refN = refN(0, 0);
        refN.setClass(Ptg.CLASS_ARRAY);

        Ptg[] converted = new SharedFormula(SpreadsheetVersion.EXCEL2007).convertSharedFormulas(new Ptg[] {ref, refN}, 0, 0);
        assertEquals(Ptg.CLASS_REF, converted[0].getPtgClass());
        assertEquals(Ptg.CLASS_ARRAY, converted[1].getPtgClass());
    }
}
