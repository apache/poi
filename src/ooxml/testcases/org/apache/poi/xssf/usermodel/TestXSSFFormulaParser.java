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

import junit.framework.TestCase;

import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.RefPtg;
import org.apache.poi.hssf.record.formula.IntPtg;
import org.apache.poi.hssf.record.formula.FuncPtg;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;

public final class TestXSSFFormulaParser extends TestCase {


    public void testParse() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        String fmla;
        Ptg[] ptgs;

        fmla = "ABC10";
        ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
        assertEquals(1, ptgs.length);
        assertTrue("",(ptgs[0] instanceof RefPtg));

        fmla = "A500000";
        ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
        assertEquals(1, ptgs.length);
        assertTrue("",(ptgs[0] instanceof RefPtg));

        fmla = "ABC500000";
        ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
        assertEquals(1, ptgs.length);
        assertTrue("",(ptgs[0] instanceof RefPtg));

        //highest allowed rows and column (XFD and 0x100000)
        fmla = "XFD1048576";
        ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
        assertEquals(1, ptgs.length);
        assertTrue("",(ptgs[0] instanceof RefPtg));


        //column greater than XFD
        fmla = "XFE10";
        try {
            ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
            fail("expected exception");
        } catch (Exception e){
            assertEquals("Specified named range 'XFE10' does not exist in the current workbook.", e.getMessage());
        }

        //row greater than 0x100000
        fmla = "XFD1048577";
        try {
            ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
            fail("expected exception");
        } catch (Exception e){
            assertEquals("Specified named range 'XFD1048577' does not exist in the current workbook.", e.getMessage());
        }
    }

    public void testBuiltInFormulas() {
        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(wb);
        String fmla;
        Ptg[] ptgs;

        fmla = "LOG10";
        ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
        assertEquals(1, ptgs.length);
        assertTrue("",(ptgs[0] instanceof RefPtg));

        fmla = "LOG10(100)";
        ptgs = FormulaParser.parse(fmla, fpb, FormulaType.CELL);
        assertEquals(2, ptgs.length);
        assertTrue("",(ptgs[0] instanceof IntPtg));
        assertTrue("",(ptgs[1] instanceof FuncPtg));

    }
}