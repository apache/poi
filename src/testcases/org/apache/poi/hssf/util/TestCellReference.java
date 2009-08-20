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

package org.apache.poi.hssf.util;


import junit.framework.TestCase;

import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.CellReference.NameType;
import org.apache.poi.ss.SpreadsheetVersion;


public final class TestCellReference extends TestCase {

    public void testAbsRef1(){
        CellReference cf = new CellReference("$B$5");
        confirmCell(cf, null, 4, 1, true, true, "$B$5");
    }

    public void  testAbsRef2(){
        CellReference cf = new CellReference(4,1,true,true);
        confirmCell(cf, null, 4, 1, true, true, "$B$5");
    }

    public void  testAbsRef3(){
        CellReference cf = new CellReference("B$5");
        confirmCell(cf, null, 4, 1, true, false, "B$5");
    }

    public void  testAbsRef4(){
        CellReference cf = new CellReference(4,1,true,false);
        confirmCell(cf, null, 4, 1, true, false, "B$5");
    }

    public void  testAbsRef5(){
        CellReference cf = new CellReference("$B5");
        confirmCell(cf, null, 4, 1, false, true, "$B5");
    }

    public void  testAbsRef6(){
        CellReference cf = new CellReference(4,1,false,true);
        confirmCell(cf, null, 4, 1, false, true, "$B5");
    }

    public void  testAbsRef7(){
        CellReference cf = new CellReference("B5");
        confirmCell(cf, null, 4, 1, false, false, "B5");
    }

    public void  testAbsRef8(){
        CellReference cf = new CellReference(4,1,false,false);
        confirmCell(cf, null, 4, 1, false, false, "B5");
    }

    public void testSpecialSheetNames() {
        CellReference cf;
        cf = new CellReference("'profit + loss'!A1");
        confirmCell(cf, "profit + loss", 0, 0, false, false, "'profit + loss'!A1");

        cf = new CellReference("'O''Brien''s Sales'!A1");
        confirmCell(cf, "O'Brien's Sales", 0, 0, false, false, "'O''Brien''s Sales'!A1");

        cf = new CellReference("'Amazing!'!A1");
        confirmCell(cf, "Amazing!", 0, 0, false, false, "'Amazing!'!A1");
    }

    /* package */ static void confirmCell(CellReference cf, String expSheetName, int expRow,
            int expCol, boolean expIsRowAbs, boolean expIsColAbs, String expText) {

        assertEquals(expSheetName, cf.getSheetName());
        assertEquals("row index is wrong", expRow, cf.getRow());
        assertEquals("col index is wrong", expCol, cf.getCol());
        assertEquals("isRowAbsolute is wrong", expIsRowAbs, cf.isRowAbsolute());
        assertEquals("isColAbsolute is wrong", expIsColAbs, cf.isColAbsolute());
        assertEquals("text is wrong", expText, cf.formatAsString());
    }

    public void testClassifyCellReference() {
        confirmNameType("a1", NameType.CELL);
        confirmNameType("pfy1", NameType.NAMED_RANGE);
        confirmNameType("pf1", NameType.NAMED_RANGE); // (col) out of cell range
        confirmNameType("fp1", NameType.CELL);
        confirmNameType("pf$1", NameType.BAD_CELL_OR_NAMED_RANGE);
        confirmNameType("_A1", NameType.NAMED_RANGE);
        confirmNameType("A_1", NameType.NAMED_RANGE);
        confirmNameType("A1_", NameType.NAMED_RANGE);
        confirmNameType(".A1", NameType.BAD_CELL_OR_NAMED_RANGE);
        confirmNameType("A.1", NameType.NAMED_RANGE);
        confirmNameType("A1.", NameType.NAMED_RANGE);
    }

    public void testClassificationOfRowReferences(){
        confirmNameType("10", NameType.ROW);
        confirmNameType("$10", NameType.ROW);
        confirmNameType("65536", NameType.ROW);

        confirmNameType("65537", NameType.BAD_CELL_OR_NAMED_RANGE);
        confirmNameType("$100000", NameType.BAD_CELL_OR_NAMED_RANGE);
        confirmNameType("$1$1", NameType.BAD_CELL_OR_NAMED_RANGE);
    }

    private void confirmNameType(String ref, NameType expectedResult) {
        NameType actualResult = CellReference.classifyCellReference(ref, SpreadsheetVersion.EXCEL97);
        assertEquals(expectedResult, actualResult);
    }
}
