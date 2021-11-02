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

package org.apache.poi.hssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.ss.usermodel.BaseTestSheetHiding;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.junit.jupiter.api.Test;

final class TestSheetHiding extends BaseTestSheetHiding {
    public TestSheetHiding() {
        super(HSSFITestDataProvider.instance,
                "TwoSheetsOneHidden.xls", "TwoSheetsNoneHidden.xls");
    }

    @Test
    void testInternalWorkbookHidden() {
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("MySheet");
        InternalWorkbook intWb = wb.getWorkbook();

        assertFalse(intWb.isSheetHidden(0));
        assertFalse(intWb.isSheetVeryHidden(0));
        assertEquals(SheetVisibility.VISIBLE, intWb.getSheetVisibility(0));

        intWb.setSheetHidden(0, SheetVisibility.HIDDEN);
        assertTrue(intWb.isSheetHidden(0));
        assertFalse(intWb.isSheetVeryHidden(0));
        assertEquals(SheetVisibility.HIDDEN, intWb.getSheetVisibility(0));

        // InternalWorkbook currently behaves slightly different
        // than HSSFWorkbook, but it should not have effect in normal usage
        // as checked limits are more strict in HSSFWorkbook

        // check sheet-index with one more will work and add the sheet
        intWb.setSheetHidden(1, SheetVisibility.HIDDEN);
        assertTrue(intWb.isSheetHidden(1));
        assertFalse(intWb.isSheetVeryHidden(1));
        assertEquals(SheetVisibility.HIDDEN, intWb.getSheetVisibility(1));

        // check sheet-index with index out of bounds => throws exception
        assertThrows(RuntimeException.class, () -> wb.setSheetVisibility(10, SheetVisibility.HIDDEN));
    }
}
