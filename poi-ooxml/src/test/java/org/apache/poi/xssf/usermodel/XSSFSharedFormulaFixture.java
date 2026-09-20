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

package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellFormula;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCellFormulaType;

/**
 * Turns the fill-down formulas of the {@code BaseTestFormulaEvaluatorFixture} workbook into
 * shared formulas, the way Excel stores them, so that the evaluator tests also run against
 * shared formulas.
 * <p>
 * POI never creates shared formulas itself, and only registers them when it reads a sheet, so the
 * formulas are rewritten at the {@code CTCellFormula} level and the workbook is written out and
 * read back. Two groups are created: {@code D2:D4} (the {@code VLOOKUP}s, filled down) and
 * {@code A6:C6} (the {@code SUM}s over the data columns, filled right; {@code B6} is empty).
 */
final class XSSFSharedFormulaFixture {

    private XSSFSharedFormulaFixture() {
    }

    /** the shared formula groups: master cell, range, shared index */
    private static final Object[][] GROUPS = {
            {"D2", "D2:D4", 0},
            {"A6", "A6:C6", 1},
    };

    static XSSFWorkbook shareFillDownFormulasAndReload(Workbook built) {
        XSSFWorkbook wb = (XSSFWorkbook) built;
        XSSFSheet data = wb.getSheet("Data");
        for (Object[] group : GROUPS) {
            CellRangeAddress range = CellRangeAddress.valueOf((String) group[1]);
            int si = (Integer) group[2];
            boolean master = true;
            for (int r = range.getFirstRow(); r <= range.getLastRow(); r++) {
                for (int c = range.getFirstColumn(); c <= range.getLastColumn(); c++) {
                    XSSFRow row = data.getRow(r);
                    XSSFCell cell = row == null ? null : row.getCell(c);
                    if (cell == null || cell.getCTCell().getF() == null) {
                        continue;
                    }
                    CTCellFormula f = cell.getCTCell().getF();
                    f.setT(STCellFormulaType.SHARED);
                    f.setSi(si);
                    if (master) {
                        assertEquals(group[0], cell.getAddress().formatAsString(), "master of " + group[1]);
                        f.setRef(range.formatAsString());
                        master = false;
                    } else {
                        // dependents carry no text of their own
                        f.setStringValue("");
                    }
                }
            }
        }
        XSSFWorkbook reloaded = XSSFTestDataSamples.writeOutAndReadBack(wb);
        try {
            wb.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return reloaded;
    }

    /** asserts that the workbook really contains the shared formula groups */
    static void assertSharedFormulas(Workbook wb) {
        Sheet data = wb.getSheet("Data");
        for (Object[] group : GROUPS) {
            CellRangeAddress range = CellRangeAddress.valueOf((String) group[1]);
            int si = (Integer) group[2];
            for (int r = range.getFirstRow(); r <= range.getLastRow(); r++) {
                for (int c = range.getFirstColumn(); c <= range.getLastColumn(); c++) {
                    Row row = data.getRow(r);
                    Cell cell = row == null ? null : row.getCell(c);
                    if (cell == null) {
                        continue;
                    }
                    CTCellFormula f = ((XSSFCell) cell).getCTCell().getF();
                    assertEquals(STCellFormulaType.SHARED, f.getT(), cell.getAddress() + " should be shared");
                    assertEquals(si, f.getSi(), cell.getAddress() + " shared index");
                    if (cell.getAddress().formatAsString().equals(group[0])) {
                        assertTrue(f.isSetRef(), "master " + group[0] + " has the range");
                        assertFalse(f.getStringValue().isEmpty(), "master " + group[0] + " has the text");
                    } else {
                        assertFalse(f.isSetRef(), cell.getAddress() + " is a dependent");
                    }
                }
            }
        }
    }
}
