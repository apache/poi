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

package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.Test;

/**
 * A base class for bugzilla issues that can be described in terms of common ss interfaces.
 *
 * @author Yegor Kozlov
 */
public abstract class BaseTestBugzillaIssues {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestBugzillaIssues(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }
    
    public static void assertAlmostEquals(double expected, double actual, float factor) {
        double diff = Math.abs(expected - actual);
        double fuzz = expected * factor;
        if (diff > fuzz)
            fail(actual + " not within " + fuzz + " of " + expected);
    }

    /**
     * Test writing a hyperlink
     * Open resulting sheet in Excel and check that A1 contains a hyperlink
     *
     * Also tests bug 15353 (problems with hyperlinks to Google)
     */
    @Test
    public final void bug23094() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        r.createCell(0).setCellFormula("HYPERLINK(\"http://jakarta.apache.org\",\"Jakarta\")");
        r.createCell(1).setCellFormula("HYPERLINK(\"http://google.com\",\"Google\")");

        wb = _testDataProvider.writeOutAndReadBack(wb);
        r = wb.getSheetAt(0).getRow(0);

        Cell cell_0 = r.getCell(0);
        assertEquals("HYPERLINK(\"http://jakarta.apache.org\",\"Jakarta\")", cell_0.getCellFormula());
        Cell cell_1 = r.getCell(1);
        assertEquals("HYPERLINK(\"http://google.com\",\"Google\")", cell_1.getCellFormula());
    }

    /**
     * test writing a file with large number of unique strings,
     * open resulting file in Excel to check results!
     * @param  num the number of strings to generate
     */
    public final void bug15375(int num) {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        CreationHelper factory = wb.getCreationHelper();

        String tmp1 = null;
        String tmp2 = null;
        String tmp3 = null;

        for (int i = 0; i < num; i++) {
            tmp1 = "Test1" + i;
            tmp2 = "Test2" + i;
            tmp3 = "Test3" + i;

            Row row = sheet.createRow(i);

            Cell cell = row.createCell(0);
            cell.setCellValue(factory.createRichTextString(tmp1));
            cell = row.createCell(1);
            cell.setCellValue(factory.createRichTextString(tmp2));
            cell = row.createCell(2);
            cell.setCellValue(factory.createRichTextString(tmp3));
        }
        wb = _testDataProvider.writeOutAndReadBack(wb);
        for (int i = 0; i < num; i++) {
            tmp1 = "Test1" + i;
            tmp2 = "Test2" + i;
            tmp3 = "Test3" + i;

            Row row = sheet.getRow(i);

            assertEquals(tmp1, row.getCell(0).getStringCellValue());
            assertEquals(tmp2, row.getCell(1).getStringCellValue());
            assertEquals(tmp3, row.getCell(2).getStringCellValue());
        }
    }

    /**
     * Merged regions were being removed from the parent in cloned sheets
     */
    @Test
    public final void bug22720() {
       Workbook workBook = _testDataProvider.createWorkbook();
       workBook.createSheet("TEST");
       Sheet template = workBook.getSheetAt(0);

       template.addMergedRegion(new CellRangeAddress(0, 1, 0, 2));
       template.addMergedRegion(new CellRangeAddress(1, 2, 0, 2));

       Sheet clone = workBook.cloneSheet(0);
       int originalMerged = template.getNumMergedRegions();
       assertEquals("2 merged regions", 2, originalMerged);

       //remove merged regions from clone
       for (int i=template.getNumMergedRegions()-1; i>=0; i--) {
           clone.removeMergedRegion(i);
       }

       assertEquals("Original Sheet's Merged Regions were removed", originalMerged, template.getNumMergedRegions());
       //check if template's merged regions are OK
       if (template.getNumMergedRegions()>0) {
            // fetch the first merged region...EXCEPTION OCCURS HERE
            template.getMergedRegion(0);
       }
       //make sure we dont exception

    }

    @Test
    public final void bug28031() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        wb.setSheetName(0, "Sheet1");

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        String formulaText =
            "IF(ROUND(A2*B2*C2,2)>ROUND(B2*D2,2),ROUND(A2*B2*C2,2),ROUND(B2*D2,2))";
        cell.setCellFormula(formulaText);

        assertEquals(formulaText, cell.getCellFormula());
        wb = _testDataProvider.writeOutAndReadBack(wb);
        cell = wb.getSheetAt(0).getRow(0).getCell(0);
        assertEquals("IF(ROUND(A2*B2*C2,2)>ROUND(B2*D2,2),ROUND(A2*B2*C2,2),ROUND(B2*D2,2))", cell.getCellFormula());
    }

    /**
     * Bug 21334: "File error: data may have been lost" with a file
     * that contains macros and this formula:
     * {=SUM(IF(FREQUENCY(IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),""),IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),""))>0,1))}
     */
    @Test
    public final void bug21334() {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sh = wb.createSheet();
        Cell cell = sh.createRow(0).createCell(0);
        String formula = "SUM(IF(FREQUENCY(IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),\"\"),IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),\"\"))>0,1))";
        cell.setCellFormula(formula);

        Workbook wb_sv = _testDataProvider.writeOutAndReadBack(wb);
        Cell cell_sv = wb_sv.getSheetAt(0).getRow(0).getCell(0);
        assertEquals(formula, cell_sv.getCellFormula());
    }

    /** another test for the number of unique strings issue
     *test opening the resulting file in Excel*/
    @Test
    public final void bug22568() {
        int r=2000;int c=3;

        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("ExcelTest") ;

        int col_cnt=0, rw_cnt=0 ;

        col_cnt = c;
        rw_cnt = r;

        Row rw ;
        rw = sheet.createRow(0) ;
        //Header row
        for(int j=0; j<col_cnt; j++){
            Cell cell = rw.createCell(j) ;
            cell.setCellValue("Col " + (j+1));
        }

        for(int i=1; i<rw_cnt; i++){
            rw = sheet.createRow(i) ;
            for(int j=0; j<col_cnt; j++){
                Cell cell = rw.createCell(j) ;
                cell.setCellValue("Row:" + (i+1) + ",Column:" + (j+1));
            }
        }

        sheet.setDefaultColumnWidth(18) ;

        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheetAt(0);
        rw = sheet.getRow(0);
        //Header row
        for(int j=0; j<col_cnt; j++){
            Cell cell = rw.getCell(j) ;
            assertEquals("Col " + (j+1), cell.getStringCellValue());
        }
        for(int i=1; i<rw_cnt; i++){
            rw = sheet.getRow(i) ;
            for(int j=0; j<col_cnt; j++){
                Cell cell = rw.getCell(j) ;
                assertEquals("Row:" + (i+1) + ",Column:" + (j+1), cell.getStringCellValue());
            }
        }
    }

    /**
     * Bug 42448: Can't parse SUMPRODUCT(A!C7:A!C67, B8:B68) / B69
     */
    @Test
    public final void bug42448(){
        Workbook wb = _testDataProvider.createWorkbook();
        Cell cell = wb.createSheet().createRow(0).createCell(0);
        cell.setCellFormula("SUMPRODUCT(A!C7:A!C67, B8:B68) / B69");
        assertTrue("no errors parsing formula", true);
    }

    @Test
    public final void bug18800() {
       Workbook book = _testDataProvider.createWorkbook();
       book.createSheet("TEST");
       Sheet sheet = book.cloneSheet(0);
       book.setSheetName(1,"CLONE");
       sheet.createRow(0).createCell(0).setCellValue("Test");

       book = _testDataProvider.writeOutAndReadBack(book);
       sheet = book.getSheet("CLONE");
       Row row = sheet.getRow(0);
       Cell cell = row.getCell(0);
       assertEquals("Test", cell.getRichStringCellValue().getString());
   }

    private static void addNewSheetWithCellsA1toD4(Workbook book, int sheet) {

        Sheet sht = book .createSheet("s" + sheet);
        for (int r=0; r < 4; r++) {

            Row   row = sht.createRow (r);
            for (int c=0; c < 4; c++) {

                Cell cel = row.createCell(c);
                cel.setCellValue(sheet*100 + r*10 + c);
            }
        }
    }

    @Test
    public final void bug43093() {
        Workbook xlw = _testDataProvider.createWorkbook();

        addNewSheetWithCellsA1toD4(xlw, 1);
        addNewSheetWithCellsA1toD4(xlw, 2);
        addNewSheetWithCellsA1toD4(xlw, 3);
        addNewSheetWithCellsA1toD4(xlw, 4);

        Sheet s2   = xlw.getSheet("s2");
        Row   s2r3 = s2.getRow(3);
        Cell  s2E4 = s2r3.createCell(4);
        s2E4.setCellFormula("SUM(s3!B2:C3)");

        FormulaEvaluator eva = xlw.getCreationHelper().createFormulaEvaluator();
        double d = eva.evaluate(s2E4).getNumberValue();

        assertEquals(d, (311+312+321+322), 0.0000001);
    }

    @Test
    public final void bug46729_testMaxFunctionArguments(){
        String[] func = {"COUNT", "AVERAGE", "MAX", "MIN", "OR", "SUBTOTAL", "SKEW"};

        SpreadsheetVersion ssVersion = _testDataProvider.getSpreadsheetVersion();
        Workbook wb = _testDataProvider.createWorkbook();
        Cell cell = wb.createSheet().createRow(0).createCell(0);

        String fmla;
        for (String name : func) {

            fmla = createFunction(name, 5);
            cell.setCellFormula(fmla);

            fmla = createFunction(name, ssVersion.getMaxFunctionArgs());
            cell.setCellFormula(fmla);

            try {
                fmla = createFunction(name, ssVersion.getMaxFunctionArgs() + 1);
                cell.setCellFormula(fmla);
                fail("Expected FormulaParseException");
            } catch (RuntimeException e){
                 assertTrue(e.getMessage().startsWith("Too many arguments to function '"+name+"'"));
            }
        }
    }

    private static String createFunction(String name, int maxArgs){
        StringBuffer fmla = new StringBuffer();
        fmla.append(name);
        fmla.append("(");
        for(int i=0; i < maxArgs; i++){
            if(i > 0) fmla.append(',');
            fmla.append("A1");
        }
        fmla.append(")");
        return fmla.toString();
    }

    @Test
    public final void bug506819_testAutoSize() {
        Workbook wb = _testDataProvider.createWorkbook();
        BaseTestSheetAutosizeColumn.fixFonts(wb);
        Sheet sheet = wb.createSheet("Sheet1");
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);

        String longValue = "www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, www.hostname.com";

        cell0.setCellValue(longValue);

        sheet.autoSizeColumn(0);
        assertEquals(255*256, sheet.getColumnWidth(0)); // maximum column width is 255 characters
        sheet.setColumnWidth(0, sheet.getColumnWidth(0)); // Bug 506819 reports exception at this point
    }

    /**
     * CreateFreezePane column/row order check
     */
    @Test
    public void bug49381() throws Exception {
       Workbook wb = _testDataProvider.createWorkbook();
       int colSplit = 1;
       int rowSplit = 2;
       int leftmostColumn = 3;
       int topRow = 4;

        Sheet s = wb.createSheet();

        // Populate
        for(int rn=0; rn<= topRow; rn++) {
           Row r = s.createRow(rn);
           for(int cn=0; cn<leftmostColumn; cn++) {
              Cell c = r.createCell(cn, Cell.CELL_TYPE_NUMERIC);
              c.setCellValue(100*rn + cn);
           }
        }

        // Create the Freeze Pane
        s.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
        PaneInformation paneInfo = s.getPaneInformation();

        // Check it
        assertEquals(colSplit,       paneInfo.getVerticalSplitPosition());
        assertEquals(rowSplit,       paneInfo.getHorizontalSplitPosition());
        assertEquals(leftmostColumn, paneInfo.getVerticalSplitLeftColumn());
        assertEquals(topRow,         paneInfo.getHorizontalSplitTopRow());


        // Now a row only freezepane
        s.createFreezePane(0, 3);
        paneInfo = s.getPaneInformation();

        assertEquals(0,  paneInfo.getVerticalSplitPosition());
        assertEquals(3,  paneInfo.getHorizontalSplitPosition());
        assertEquals(0,  paneInfo.getVerticalSplitLeftColumn());
        assertEquals(3,  paneInfo.getHorizontalSplitTopRow());

        // Now a column only freezepane
        s.createFreezePane(4, 0);
        paneInfo = s.getPaneInformation();

        assertEquals(4,  paneInfo.getVerticalSplitPosition());
        assertEquals(0,  paneInfo.getHorizontalSplitPosition());
        assertEquals(4 , paneInfo.getVerticalSplitLeftColumn());
        assertEquals(0,  paneInfo.getHorizontalSplitTopRow());
    }

    /** 
     * Test hyperlinks
     * open resulting file in excel, and check that there is a link to Google
     */
    @Test
    public void bug15353() {
        String hyperlinkF = "HYPERLINK(\"http://google.com\",\"Google\")";
        
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("My sheet");

        Row row = sheet.createRow( 0 );
        Cell cell = row.createCell( 0 );
        cell.setCellFormula(hyperlinkF);
        
        assertEquals(hyperlinkF, cell.getCellFormula());

        wb = _testDataProvider.writeOutAndReadBack(wb);
        sheet = wb.getSheet("My Sheet");
        row = sheet.getRow( 0 );
        cell = row.getCell( 0 );
        
        assertEquals(hyperlinkF, cell.getCellFormula());
    }

    /**
     * HLookup and VLookup with optional arguments 
     */
    @Test
    public void bug51024() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r1 = s.createRow(0);
        Row r2 = s.createRow(1);

        r1.createCell(0).setCellValue("v A1");
        r2.createCell(0).setCellValue("v A2");
        r1.createCell(1).setCellValue("v B1");

        Cell c = r1.createCell(4);

        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();

        c.setCellFormula("VLOOKUP(\"v A1\", A1:B2, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("VLOOKUP(\"v A1\", A1:B2, 1, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("VLOOKUP(\"v A1\", A1:B2, 1, )");
        assertEquals("v A1", eval.evaluate(c).getStringValue());


        c.setCellFormula("HLOOKUP(\"v A1\", A1:B2, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("HLOOKUP(\"v A1\", A1:B2, 1, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("HLOOKUP(\"v A1\", A1:B2, 1, )");
        assertEquals("v A1", eval.evaluate(c).getStringValue());
    }
    
    @Test
    public void stackoverflow23114397() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        DataFormat format = wb.getCreationHelper().createDataFormat();
        
        // How close the sizing should be, given that not all
        //  systems will have quite the same fonts on them
        float fontAccuracy = 0.22f;
        
        // x%
        CellStyle iPercent = wb.createCellStyle();
        iPercent.setDataFormat(format.getFormat("0%"));
        // x.x%
        CellStyle d1Percent = wb.createCellStyle();
        d1Percent.setDataFormat(format.getFormat("0.0%"));
        // x.xx%
        CellStyle d2Percent = wb.createCellStyle();
        d2Percent.setDataFormat(format.getFormat("0.00%"));
        
        Sheet s = wb.createSheet();
        Row r1 = s.createRow(0);
        
        for (int i=0; i<3; i++) {
            r1.createCell(i, Cell.CELL_TYPE_NUMERIC).setCellValue(0);
        }
        for (int i=3; i<6; i++) {
            r1.createCell(i, Cell.CELL_TYPE_NUMERIC).setCellValue(1);
        }
        for (int i=6; i<9; i++) {
            r1.createCell(i, Cell.CELL_TYPE_NUMERIC).setCellValue(0.12345);
        }
        for (int i=9; i<12; i++) {
            r1.createCell(i, Cell.CELL_TYPE_NUMERIC).setCellValue(1.2345);
        }
        for (int i=0; i<12; i+=3) {
            r1.getCell(i+0).setCellStyle(iPercent);
            r1.getCell(i+1).setCellStyle(d1Percent);
            r1.getCell(i+2).setCellStyle(d2Percent);
        }
        for (int i=0; i<12; i++) {
            s.autoSizeColumn(i);
        }
        
        // Check the 0(.00)% ones
        assertAlmostEquals(980, s.getColumnWidth(0), fontAccuracy);
        assertAlmostEquals(1400, s.getColumnWidth(1), fontAccuracy);
        assertAlmostEquals(1700, s.getColumnWidth(2), fontAccuracy);
        
        // Check the 100(.00)% ones
        assertAlmostEquals(1500, s.getColumnWidth(3), fontAccuracy);
        assertAlmostEquals(1950, s.getColumnWidth(4), fontAccuracy);
        assertAlmostEquals(2225, s.getColumnWidth(5), fontAccuracy);
        
        // Check the 12(.34)% ones
        assertAlmostEquals(1225, s.getColumnWidth(6), fontAccuracy);
        assertAlmostEquals(1650, s.getColumnWidth(7), fontAccuracy);
        assertAlmostEquals(1950, s.getColumnWidth(8), fontAccuracy);
        
        // Check the 123(.45)% ones
        assertAlmostEquals(1500, s.getColumnWidth(9), fontAccuracy);
        assertAlmostEquals(1950, s.getColumnWidth(10), fontAccuracy);
        assertAlmostEquals(2225, s.getColumnWidth(11), fontAccuracy);
    }
    
    /**
     * =ISNUMBER(SEARCH("AM",A1)) evaluation 
     */
    @Test
    public void stackoverflow26437323() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r1 = s.createRow(0);
        Row r2 = s.createRow(1);
        
        // A1 is a number
        r1.createCell(0).setCellValue(1.1);
        // B1 is a string, with the wanted text in it
        r1.createCell(1).setCellValue("This is text with AM in it");
        // C1 is a string, with different text
        r1.createCell(2).setCellValue("This some other text");
        // D1 is a blank cell
        r1.createCell(3, Cell.CELL_TYPE_BLANK);
        // E1 is null
        
        // A2 will hold our test formulas
        Cell cf = r2.createCell(0, Cell.CELL_TYPE_FORMULA);
        
        
        // First up, check that TRUE and ISLOGICAL both behave
        cf.setCellFormula("TRUE()");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISLOGICAL(TRUE())");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISLOGICAL(4)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        
        // Now, check ISNUMBER / ISTEXT / ISNONTEXT
        cf.setCellFormula("ISNUMBER(A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(E1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        
        cf.setCellFormula("ISTEXT(A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISTEXT(B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISTEXT(C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISTEXT(D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISTEXT(E1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        
        cf.setCellFormula("ISNONTEXT(A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNONTEXT(B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNONTEXT(C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNONTEXT(D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNONTEXT(E1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue()); // Blank and Null the same

        
        // Next up, SEARCH on its own
        cf.setCellFormula("SEARCH(\"am\", A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(ErrorConstants.ERROR_VALUE, cf.getErrorCellValue());
        
        cf.setCellFormula("SEARCH(\"am\", B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(19, (int)cf.getNumericCellValue());
        
        cf.setCellFormula("SEARCH(\"am\", C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(ErrorConstants.ERROR_VALUE, cf.getErrorCellValue());
        
        cf.setCellFormula("SEARCH(\"am\", D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(ErrorConstants.ERROR_VALUE, cf.getErrorCellValue());
        
        
        // Finally, bring it all together
        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", A1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", B1))");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", C1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", D1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());
        
        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", E1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());        
    }
    private Cell evaluateCell(Workbook wb, Cell c) {
        Sheet s = c.getSheet();
        wb.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(c);
        return s.getRow(c.getRowIndex()).getCell(c.getColumnIndex());
    }
    
    /**
     * Should be able to write then read formulas with references
     *  to cells in other files, eg '[refs/airport.xls]Sheet1'!$A$2
     *  or 'http://192.168.1.2/[blank.xls]Sheet1'!$A$1 .
     * Additionally, if a reference to that file is provided, it should
     *  be possible to evaluate them too
     * TODO Fix this to evaluate for XSSF
     * TODO Fix this to work at all for HSSF
     */
//    @Test
    public void bug46670() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r1 = s.createRow(0);
        
        
        // References to try
        String ext = "xls";
        if (! (wb instanceof HSSFWorkbook)) ext += "x";
        String refLocal = "'[test."+ext+"]Sheet1'!$A$2";
        String refHttp  = "'[http://example.com/test."+ext+"]Sheet1'!$A$2";
        String otherCellText = "In Another Workbook";

        
        // Create the references
        Cell c1 = r1.createCell(0, Cell.CELL_TYPE_FORMULA);
        c1.setCellFormula(refLocal);
        
        Cell c2 = r1.createCell(1, Cell.CELL_TYPE_FORMULA);
        c2.setCellFormula(refHttp);
        
        
        // Check they were set correctly
        assertEquals(refLocal, c1.getCellFormula());
        assertEquals(refHttp,  c2.getCellFormula());
        
        
        // Reload, and ensure they were serialised and read correctly
        wb = _testDataProvider.writeOutAndReadBack(wb);
        s = wb.getSheetAt(0);
        r1 = s.getRow(0);
        
        c1 = r1.getCell(0);
        c2 = r1.getCell(1);
        assertEquals(refLocal, c1.getCellFormula());
        assertEquals(refHttp,  c2.getCellFormula());

        
        // Try to evalutate, without giving a way to get at the other file
        try {
            evaluateCell(wb, c1);
            fail("Shouldn't be able to evaluate without the other file");
        } catch (Exception e) {}
        try {
            evaluateCell(wb, c2);
            fail("Shouldn't be able to evaluate without the other file");
        } catch (Exception e) {}
        
        
        // Set up references to the other file
        Workbook wb2 = _testDataProvider.createWorkbook();
        wb2.createSheet().createRow(1).createCell(0).setCellValue(otherCellText);
        
        Map<String,FormulaEvaluator> evaluators = new HashMap<String, FormulaEvaluator>();
        evaluators.put(refLocal, wb2.getCreationHelper().createFormulaEvaluator());
        evaluators.put(refHttp,  wb2.getCreationHelper().createFormulaEvaluator());
        
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        evaluator.setupReferencedWorkbooks(evaluators);
        
        
        // Try to evaluate, with the other file
        evaluator.evaluateFormulaCell(c1);
        evaluator.evaluateFormulaCell(c2);
        
        assertEquals(otherCellText, c1.getStringCellValue());
        assertEquals(otherCellText, c2.getStringCellValue());
    }
}
