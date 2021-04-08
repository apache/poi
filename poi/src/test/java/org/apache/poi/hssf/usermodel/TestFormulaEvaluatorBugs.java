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

import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.ss.formula.EvaluationCell;
import org.apache.poi.ss.formula.EvaluationListener;
import org.apache.poi.ss.formula.WorkbookEvaluator;
import org.apache.poi.ss.formula.WorkbookEvaluatorTestHelper;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.FuncPtg;
import org.apache.poi.ss.formula.ptg.FuncVarPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *
 */
final class TestFormulaEvaluatorBugs {
    private static boolean OUTPUT_TEST_FILES;
    private static String tmpDirName;

    @BeforeAll
    public static void setUp() {
        tmpDirName = System.getProperty("java.io.tmpdir");
        OUTPUT_TEST_FILES = Boolean.parseBoolean(
                System.getProperty("org.apache.poi.test.output_test_files", "False"));
    }

    /**
     * An odd problem with evaluateFormulaCell giving the
     *  right values when file is opened, but changes
     *  to the source data in some versions of excel
     *  doesn't cause them to be updated. However, other
     *  versions of excel, and gnumeric, work just fine
     * WARNING - tedious bug where you actually have to
     *  open up excel
     */
    @Test
    void test44636() throws Exception {
        // Open the existing file, tweak one value and
        // re-calculate

        HSSFWorkbook wb = openSampleWorkbook("44636.xls");
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(0);

        row.getCell(0).setCellValue(4.2);
        row.getCell(2).setCellValue(25);

        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
        assertEquals(4.2 * 25, row.getCell(3).getNumericCellValue(), 0.0001);

        if (OUTPUT_TEST_FILES) {
            // Save
            File existing = new File(tmpDirName, "44636-existing.xls");
            FileOutputStream out = new FileOutputStream(existing);
            wb.write(out);
            out.close();
            System.err.println("Existing file for bug #44636 written to " + existing);
        }
        wb.close();

        // Now, do a new file from scratch
        wb = new HSSFWorkbook();
        sheet = wb.createSheet();

        row = sheet.createRow(0);
        row.createCell(0).setCellValue(1.2);
        row.createCell(1).setCellValue(4.2);

        row = sheet.createRow(1);
        row.createCell(0).setCellFormula("SUM(A1:B1)");

        HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
        assertEquals(5.4, row.getCell(0).getNumericCellValue(), 0.0001);

        if (OUTPUT_TEST_FILES) {
            // Save
            File scratch = new File(tmpDirName, "44636-scratch.xls");
            FileOutputStream out = new FileOutputStream(scratch);
            wb.write(out);
            out.close();
            System.err.println("New file for bug #44636 written to " + scratch);
        }
        wb.close();
    }

    /**
     * Bug 44297: 32767+32768 is evaluated to -1
     * Fix: IntPtg must operate with unsigned short. Reading signed short results in incorrect formula calculation
     * if a formula has values in the interval [Short.MAX_VALUE, (Short.MAX_VALUE+1)*2]
     */
    @Test
    void test44297() throws Exception {

        HSSFWorkbook wb = openSampleWorkbook("44297.xls");

        HSSFRow row;
        HSSFCell cell;

        HSSFSheet sheet = wb.getSheetAt(0);

        HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(wb);

        row = sheet.getRow(0);
        cell = row.getCell(0);
        assertEquals("31+46", cell.getCellFormula());
        assertEquals(77, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(1);
        cell = row.getCell(0);
        assertEquals("30+53", cell.getCellFormula());
        assertEquals(83, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(2);
        cell = row.getCell(0);
        assertEquals("SUM(A1:A2)", cell.getCellFormula());
        assertEquals(160, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(4);
        cell = row.getCell(0);
        assertEquals("32767+32768", cell.getCellFormula());
        assertEquals(65535, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(7);
        cell = row.getCell(0);
        assertEquals("32744+42333", cell.getCellFormula());
        assertEquals(75077, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(8);
        cell = row.getCell(0);
        assertEquals("327680/32768", cell.getCellFormula());
        assertEquals(10, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(9);
        cell = row.getCell(0);
        assertEquals("32767+32769", cell.getCellFormula());
        assertEquals(65536, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(10);
        cell = row.getCell(0);
        assertEquals("35000+36000", cell.getCellFormula());
        assertEquals(71000, eva.evaluate(cell).getNumberValue(), 0);

        row = sheet.getRow(11);
        cell = row.getCell(0);
        assertEquals("-1000000-3000000", cell.getCellFormula());
        assertEquals(-4000000, eva.evaluate(cell).getNumberValue(), 0);

        wb.close();
    }

    /**
     * Bug 44410: SUM(C:C) is valid in excel, and means a sum
     *  of all the rows in Column C
     */
    @Test
    void test44410() throws Exception {
        HSSFWorkbook wb = openSampleWorkbook("SingleLetterRanges.xls");

        HSSFSheet sheet = wb.getSheetAt(0);

        HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(wb);

        // =index(C:C,2,1) -> 2
        HSSFRow rowIDX = sheet.getRow(3);
        // =sum(C:C) -> 6
        HSSFRow rowSUM = sheet.getRow(4);
        // =sum(C:D) -> 66
        HSSFRow rowSUM2D = sheet.getRow(5);

        // Test the sum
        HSSFCell cellSUM = rowSUM.getCell(0);

        FormulaRecordAggregate frec = (FormulaRecordAggregate) cellSUM.getCellValueRecord();
        Ptg[] ops = frec.getFormulaRecord().getParsedExpression();
        assertEquals(2, ops.length);
        assertEquals(AreaPtg.class, ops[0].getClass());
        assertEquals(FuncVarPtg.class, ops[1].getClass());

        // Actually stored as C1 to C65536
        // (last row is -1 === 65535)
        AreaPtg ptg = (AreaPtg) ops[0];
        assertEquals(2, ptg.getFirstColumn());
        assertEquals(2, ptg.getLastColumn());
        assertEquals(0, ptg.getFirstRow());
        assertEquals(65535, ptg.getLastRow());
        assertEquals("C:C", ptg.toFormulaString());

        // Will show as C:C, but won't know how many
        // rows it covers as we don't have the sheet
        // to hand when turning the Ptgs into a string
        assertEquals("SUM(C:C)", cellSUM.getCellFormula());

        // But the evaluator knows the sheet, so it
        // can do it properly
        assertEquals(6, eva.evaluate(cellSUM).getNumberValue(), 0);

        // Test the index
        // Again, the formula string will be right but
        // lacking row count, evaluated will be right
        HSSFCell cellIDX = rowIDX.getCell(0);
        assertEquals("INDEX(C:C,2,1)", cellIDX.getCellFormula());
        assertEquals(2, eva.evaluate(cellIDX).getNumberValue(), 0);

        // Across two colums
        HSSFCell cellSUM2D = rowSUM2D.getCell(0);
        assertEquals("SUM(C:D)", cellSUM2D.getCellFormula());
        assertEquals(66, eva.evaluate(cellSUM2D).getNumberValue(), 0);

        wb.close();
    }

    /**
     * Tests that we can evaluate boolean cells properly
     */
    @Test
    void testEvaluateBooleanInCell_bug44508() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();
        wb.setSheetName(0, "Sheet1");
        HSSFRow row = sheet.createRow(0);
        HSSFCell cell = row.createCell(0);

        cell.setCellFormula("1=1");

        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
        try {
            fe.evaluateInCell(cell);
        } catch (NumberFormatException e) {
            fail("Identified bug 44508");
        }
        assertTrue(cell.getBooleanCellValue());

        wb.close();
    }

    @Test
    void testClassCast_bug44861() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("44861.xls")) {
            // Check direct
            HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);

            // And via calls
            for (Sheet s : wb) {
                HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb);

                for (Row r : s) {
                    for (Cell c : r) {
                        CellType ct = eval.evaluateFormulaCell(c);
                        assertNotNull(ct);
                    }
                }
            }

        }
    }

    @Test
    void testEvaluateInCellWithErrorCode_bug44950() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet("Sheet1");
            HSSFRow row = sheet.createRow(1);
            HSSFCell cell = row.createCell(0);
            cell.setCellFormula("na()"); // this formula evaluates to an Excel error code '#N/A'
            HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);
            fe.evaluateInCell(cell);
        } catch (NumberFormatException e) {
            if (e.getMessage().equals("You cannot get an error value from a non-error cell")) {
                fail("Identified bug 44950 b");
            }
            throw e;
        }
    }

    @Test
    void testDateWithNegativeParts_bug48528() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(1);
        HSSFCell cell = row.createCell(0);
        HSSFFormulaEvaluator fe = new HSSFFormulaEvaluator(wb);

        // 5th Feb 2012 = 40944
        // 1st Feb 2012 = 40940
        // 5th Jan 2012 = 40913
        // 5th Dec 2011 = 40882
        // 5th Feb 2011 = 40579

        cell.setCellFormula("DATE(2012,2,1)");
        fe.notifyUpdateCell(cell);
        assertEquals(40940.0, fe.evaluate(cell).getNumberValue(), 0);

        cell.setCellFormula("DATE(2012,2,1+4)");
        fe.notifyUpdateCell(cell);
        assertEquals(40944.0, fe.evaluate(cell).getNumberValue(), 0);

        cell.setCellFormula("DATE(2012,2-1,1+4)");
        fe.notifyUpdateCell(cell);
        assertEquals(40913.0, fe.evaluate(cell).getNumberValue(), 0);

        cell.setCellFormula("DATE(2012,2,1-27)");
        fe.notifyUpdateCell(cell);
        assertEquals(40913.0, fe.evaluate(cell).getNumberValue(), 0);

        cell.setCellFormula("DATE(2012,2-2,1+4)");
        fe.notifyUpdateCell(cell);
        assertEquals(40882.0, fe.evaluate(cell).getNumberValue(), 0);

        cell.setCellFormula("DATE(2012,2,1-58)");
        fe.notifyUpdateCell(cell);
        assertEquals(40882.0, fe.evaluate(cell).getNumberValue(), 0);

        cell.setCellFormula("DATE(2012,2-12,1+4)");
        fe.notifyUpdateCell(cell);
        assertEquals(40579.0, fe.evaluate(cell).getNumberValue(), 0);

        wb.close();
    }

    private static final class EvalListener extends EvaluationListener {
        private int _countCacheHits;
        private int _countCacheMisses;

        public EvalListener() {
            _countCacheHits = 0;
            _countCacheMisses = 0;
        }
        public int getCountCacheHits() {
            return _countCacheHits;
        }
        public int getCountCacheMisses() {
            return _countCacheMisses;
        }

        @Override
        public void onCacheHit(int sheetIndex, int srcRowNum, int srcColNum, ValueEval result) {
            _countCacheHits++;
        }
        @Override
        public void onStartEvaluate(EvaluationCell cell, ICacheEntry entry) {
            _countCacheMisses++;
        }
    }

    /**
     * The HSSFFormula evaluator performance benefits greatly from caching of intermediate cell values
     */
    @Test
    void testSlowEvaluate45376() throws Exception {
        /*
         * Note - to observe behaviour without caching, disable the call to
         * updateValue() from FormulaCellCacheEntry.updateFormulaResult().
         */

        // Firstly set up a sequence of formula cells where each depends on the  previous multiple
        // times.  Without caching, each subsequent cell take about 4 times longer to evaluate.
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("Sheet1");
        HSSFRow row = sheet.createRow(0);
        for(int i=1; i<10; i++) {
            HSSFCell cell = row.createCell(i);
            char prevCol = (char) ('A' + i-1);
            String prevCell = prevCol + "1";
            // this formula is inspired by the offending formula of the attachment for bug 45376
            // IF(DATE(YEAR(A1),MONTH(A1)+1,1)<=$D$3,DATE(YEAR(A1),MONTH(A1)+1,1),NA()) etc
            String formula = "IF(DATE(YEAR(" + prevCell + "),MONTH(" + prevCell + ")+1,1)<=$D$3," +
                    "DATE(YEAR(" + prevCell + "),MONTH(" + prevCell + ")+1,1),NA())";
            cell.setCellFormula(formula);
        }
        Calendar cal = LocaleUtil.getLocaleCalendar(2000, 0, 1, 0, 0, 0);
        row.createCell(0).setCellValue(cal);

        // Choose cell A9 instead of A10, so that the failing test case doesn't take too long to execute.
        HSSFCell cell = row.getCell(8);
        EvalListener evalListener = new EvalListener();
        WorkbookEvaluator evaluator = WorkbookEvaluatorTestHelper.createEvaluator(wb, evalListener);
        ValueEval ve = evaluator.evaluate(HSSFEvaluationTestHelper.wrapCell(cell));
        int evalCount = evalListener.getCountCacheMisses();
        // Without caching, evaluating cell 'A9' takes 21845 evaluations which consumes
        // much time (~3 sec on Core 2 Duo 2.2GHz)
        // short-circuit-if optimisation cuts this down to 255 evaluations which is still too high
        // System.err.println("Cell A9 took " + evalCount + " intermediate evaluations");
        assertTrue(evalCount <= 10, "Identifed bug 45376 - Formula evaluator should cache values");
        // With caching, the evaluationCount is 8 which is exactly the
        // number of formula cells that needed to be evaluated.
        assertEquals(8, evalCount);

        // The cache hits would be 24 if fully evaluating all arguments of the
        // "IF()" functions (Each of the 8 formulas has 4 refs to formula cells
        // which result in 1 cache miss and 3 cache hits). However with the
        // short-circuit-if optimisation, 2 of the cell refs get skipped
        // reducing this metric 8.
        assertEquals(8, evalListener.getCountCacheHits());

        // confirm the evaluation result too
        assertEquals(ErrorEval.NA, ve);

        wb.close();
    }

    @SuppressWarnings("resource")
    @Test
    void test55747_55324() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFFormulaEvaluator ev = wb.getCreationHelper().createFormulaEvaluator();
        HSSFSheet ws = wb.createSheet();
        HSSFRow row = ws.createRow(0);
        HSSFCell cell;

        // Our test value
        cell = row.createCell(0);
        cell.setCellValue("abc");

        // Lots of IF cases

        cell = row.createCell(1);
        cell.setCellFormula("IF(A1<>\"\",MID(A1,1,2),\"X\")");//if(expr,func,val)

        cell = row.createCell(2);
        cell.setCellFormula("IF(A1<>\"\",\"A\",\"B\")");// if(expr,val,val)

        cell = row.createCell(3);
        cell.setCellFormula("IF(A1=\"\",\"X\",MID(A1,1,2))");//if(expr,val,func),

        cell = row.createCell(4);
        cell.setCellFormula("IF(A1<>\"\",\"X\",MID(A1,1,2))");//if(expr,val,func),

        cell = row.createCell(5);
        cell.setCellFormula("IF(A1=\"\",MID(A1,1,2),MID(A1,2,2))");//if(exp,func,func)
        cell = row.createCell(6);
        cell.setCellFormula("IF(A1<>\"\",MID(A1,1,2),MID(A1,2,2))");//if(exp,func,func)

        cell = row.createCell(7);
        cell.setCellFormula("IF(MID(A1,1,2)<>\"\",\"A\",\"B\")");//if(func_expr,val,val)

        // And some MID ones just to check
        row = ws.createRow(1);
        cell = row.createCell(1);
        cell.setCellFormula("MID(A1,1,2)");
        cell = row.createCell(2);
        cell.setCellFormula("MID(A1,2,2)");
        cell = row.createCell(3);
        cell.setCellFormula("MID(A1,2,1)");
        cell = row.createCell(4);
        cell.setCellFormula("MID(A1,3,1)");

        // Evaluate
        ev.evaluateAll();

        // Save and re-load
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        ws = wb.getSheetAt(0);

        // Check the MID Ptgs in Row 2 have V RefPtgs for A1
        row = ws.getRow(1);
        for (int i=1; i<=4; i++) {
            cell = row.getCell(i);
            Ptg[] ptgs = getPtgs(cell);
            assertEquals(4, ptgs.length);
            assertEquals(FuncPtg.class,   ptgs[3].getClass());
            assertEquals("MID", ((FuncPtg)ptgs[3]).getName());
            assertRefPtgA1('V', ptgs, 0);
        }

        // Now check the IF formulas
        row = ws.getRow(0);

        // H1, MID is used in the expression IF checks, so A1 should be V
        cell = row.getCell(CellReference.convertColStringToIndex("H"));
        assertRefPtgA1('V', getPtgs(cell), 0);

        // E1, MID is used in the FALSE route, so:
        //  A1 should be V in the IF check
        //  A1 should be R in the FALSE route
        cell = row.getCell(CellReference.convertColStringToIndex("E"));
        assertRefPtgA1('V', getPtgs(cell), 0);
        assertRefPtgA1('R', getPtgs(cell), 6);

        // Check that, for B1, D1, F1 and G1, the references to A1
        //  from all of IF check, True and False are V
        cell = row.getCell(CellReference.convertColStringToIndex("B"));
        assertRefPtgA1('V', getPtgs(cell), 0);
//      assertRefPtgA1('V', getPtgs(cell), 4); // FIXME!

        cell = row.getCell(CellReference.convertColStringToIndex("D"));
        assertRefPtgA1('V', getPtgs(cell), 0);
//      assertRefPtgA1('V', getPtgs(cell), 6); // FIXME!

        cell = row.getCell(CellReference.convertColStringToIndex("F"));
        assertRefPtgA1('V', getPtgs(cell), 0);
//      assertRefPtgA1('V', getPtgs(cell), 4); // FIXME!
//      assertRefPtgA1('V', getPtgs(cell), 9); // FIXME!

        cell = row.getCell(CellReference.convertColStringToIndex("G"));
        assertRefPtgA1('V', getPtgs(cell), 0);
//      assertRefPtgA1('V', getPtgs(cell), 4); // FIXME!
//      assertRefPtgA1('V', getPtgs(cell), 9); // FIXME!


        // Check our cached values were correctly evaluated
        cell = row.getCell(CellReference.convertColStringToIndex("A"));
        assertEquals("abc", cell.getStringCellValue());
        cell = row.getCell(CellReference.convertColStringToIndex("B"));
        assertEquals("ab", cell.getStringCellValue());
        cell = row.getCell(CellReference.convertColStringToIndex("C"));
        assertEquals("A", cell.getStringCellValue());
        cell = row.getCell(CellReference.convertColStringToIndex("D"));
        assertEquals("ab", cell.getStringCellValue());
        cell = row.getCell(CellReference.convertColStringToIndex("E"));
        assertEquals("X", cell.getStringCellValue());
        cell = row.getCell(CellReference.convertColStringToIndex("F"));
        assertEquals("bc", cell.getStringCellValue());
        cell = row.getCell(CellReference.convertColStringToIndex("G"));
        assertEquals("ab", cell.getStringCellValue());
        cell = row.getCell(CellReference.convertColStringToIndex("H"));
        assertEquals("A", cell.getStringCellValue());

        // Enable this to write out + check in Excel
        if (OUTPUT_TEST_FILES) {
            FileOutputStream out = new FileOutputStream("/tmp/test.xls");
            wb.write(out);
            out.close();
        }
    }
    private Ptg[] getPtgs(HSSFCell cell) {
        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals(FormulaRecordAggregate.class, cell.getCellValueRecord().getClass());
        FormulaRecordAggregate agg = (FormulaRecordAggregate)cell.getCellValueRecord();
        FormulaRecord rec = agg.getFormulaRecord();
        return rec.getParsedExpression();
    }
    private void assertRefPtgA1(char rv, Ptg[] ptgs, int at) {
        Ptg ptg = ptgs[at];
        assertEquals(RefPtg.class, ptg.getClass());
        assertEquals(0,  ((RefPtg)ptg).getRow());
        assertEquals(0,  ((RefPtg)ptg).getColumn());
        assertEquals(rv, ptg.getRVAType());
    }
}
