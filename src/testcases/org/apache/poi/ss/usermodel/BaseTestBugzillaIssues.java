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

import junit.framework.TestCase;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * A base class for bugzilla issues that can be described in terms of common ss interfaces.
 *
 * @author Yegor Kozlov
 */
public abstract class BaseTestBugzillaIssues extends TestCase {

    protected abstract ITestDataProvider getTestDataProvider();

    /**
     *
     * Test writing a hyperlink
     * Open resulting sheet in Excel and check that A1 contains a hyperlink
     *
     * Also tests bug 15353 (problems with hyperlinks to Google)
     */
    public void test23094() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);
        r.createCell(0).setCellFormula("HYPERLINK(\"http://jakarta.apache.org\",\"Jakarta\")");
        r.createCell(1).setCellFormula("HYPERLINK(\"http://google.com\",\"Google\")");

        wb = getTestDataProvider().writeOutAndReadBack(wb);
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
    public void baseTest15375(int num) {
        Workbook wb = getTestDataProvider().createWorkbook();
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
        wb = getTestDataProvider().writeOutAndReadBack(wb);
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
    public void test22720() {
       Workbook workBook = getTestDataProvider().createWorkbook();
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

    public void test28031() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet sheet = wb.createSheet();
        wb.setSheetName(0, "Sheet1");

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        String formulaText =
            "IF(ROUND(A2*B2*C2,2)>ROUND(B2*D2,2),ROUND(A2*B2*C2,2),ROUND(B2*D2,2))";
        cell.setCellFormula(formulaText);

        assertEquals(formulaText, cell.getCellFormula());
        wb = getTestDataProvider().writeOutAndReadBack(wb);
        cell = wb.getSheetAt(0).getRow(0).getCell(0);
        assertEquals("IF(ROUND(A2*B2*C2,2)>ROUND(B2*D2,2),ROUND(A2*B2*C2,2),ROUND(B2*D2,2))", cell.getCellFormula());
    }

    /**
     * Bug 21334: "File error: data may have been lost" with a file
     * that contains macros and this formula:
     * {=SUM(IF(FREQUENCY(IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),""),IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),""))>0,1))}
     */
    public void test21334() {
        Workbook wb = getTestDataProvider().createWorkbook();
        Sheet sh = wb.createSheet();
        Cell cell = sh.createRow(0).createCell(0);
        String formula = "SUM(IF(FREQUENCY(IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),\"\"),IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),\"\"))>0,1))";
        cell.setCellFormula(formula);

        Workbook wb_sv = getTestDataProvider().writeOutAndReadBack(wb);
        Cell cell_sv = wb_sv.getSheetAt(0).getRow(0).getCell(0);
        assertEquals(formula, cell_sv.getCellFormula());
    }

    /** another test for the number of unique strings issue
     *test opening the resulting file in Excel*/
    public void test22568() {
        int r=2000;int c=3;

        Workbook wb = getTestDataProvider().createWorkbook();
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

        wb = getTestDataProvider().writeOutAndReadBack(wb);
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
    public void test42448(){
        Workbook wb = getTestDataProvider().createWorkbook();
        Cell cell = wb.createSheet().createRow(0).createCell(0);
        cell.setCellFormula("SUMPRODUCT(A!C7:A!C67, B8:B68) / B69");
        assertTrue("no errors parsing formula", true);
    }

    public void test18800() {
       Workbook book = getTestDataProvider().createWorkbook();
       book.createSheet("TEST");
       Sheet sheet = book.cloneSheet(0);
       book.setSheetName(1,"CLONE");
       sheet.createRow(0).createCell(0).setCellValue("Test");

       book = getTestDataProvider().writeOutAndReadBack(book);
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

    public void testBug43093() {
        Workbook xlw = getTestDataProvider().createWorkbook();

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

    public void testMaxFunctionArguments_bug46729(){
        String[] func = {"COUNT", "AVERAGE", "MAX", "MIN", "OR", "SUBTOTAL", "SKEW"};

        SpreadsheetVersion ssVersion = getTestDataProvider().getSpreadsheetVersion();
        Workbook wb = getTestDataProvider().createWorkbook();
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

    private String createFunction(String name, int maxArgs){
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
}
