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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.CalculationChain;
import org.apache.poi.xssf.usermodel.extensions.XSSFCellFill;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

public final class TestXSSFBugs extends BaseTestBugzillaIssues {

    public TestXSSFBugs() {
        super(XSSFITestDataProvider.instance);
    }

    /**
     * test writing a file with large number of unique strings,
     * open resulting file in Excel to check results!
     */
    public void test15375_2() {
        baseTest15375(1000);
    }

    /**
     * Named ranges had the right reference, but
     *  the wrong sheet name
     */
    public void test45430() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("45430.xlsx");
        assertFalse(wb.isMacroEnabled());
        assertEquals(3, wb.getNumberOfNames());

        assertEquals(0, wb.getNameAt(0).getCTName().getLocalSheetId());
        assertFalse(wb.getNameAt(0).getCTName().isSetLocalSheetId());
        assertEquals("SheetA!$A$1", wb.getNameAt(0).getRefersToFormula());
        assertEquals("SheetA", wb.getNameAt(0).getSheetName());

        assertEquals(0, wb.getNameAt(1).getCTName().getLocalSheetId());
        assertFalse(wb.getNameAt(1).getCTName().isSetLocalSheetId());
        assertEquals("SheetB!$A$1", wb.getNameAt(1).getRefersToFormula());
        assertEquals("SheetB", wb.getNameAt(1).getSheetName());

        assertEquals(0, wb.getNameAt(2).getCTName().getLocalSheetId());
        assertFalse(wb.getNameAt(2).getCTName().isSetLocalSheetId());
        assertEquals("SheetC!$A$1", wb.getNameAt(2).getRefersToFormula());
        assertEquals("SheetC", wb.getNameAt(2).getSheetName());

        // Save and re-load, still there
        XSSFWorkbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(3, nwb.getNumberOfNames());
        assertEquals("SheetA!$A$1", nwb.getNameAt(0).getRefersToFormula());
    }

    /**
     * We should carry vba macros over after save
     */
    public void test45431() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("45431.xlsm");
        OPCPackage pkg = wb.getPackage();
        assertTrue(wb.isMacroEnabled());

        // Check the various macro related bits can be found
        PackagePart vba = pkg.getPart(
                PackagingURIHelper.createPartName("/xl/vbaProject.bin")
        );
        assertNotNull(vba);
        // And the drawing bit
        PackagePart drw = pkg.getPart(
                PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
        );
        assertNotNull(drw);


        // Save and re-open, both still there
        XSSFWorkbook nwb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        OPCPackage nPkg = nwb.getPackage();
        assertTrue(nwb.isMacroEnabled());

        vba = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/vbaProject.bin")
        );
        assertNotNull(vba);
        drw = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
        );
        assertNotNull(drw);

        // And again, just to be sure
        nwb = XSSFTestDataSamples.writeOutAndReadBack(nwb);
        nPkg = nwb.getPackage();
        assertTrue(nwb.isMacroEnabled());

        vba = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/vbaProject.bin")
        );
        assertNotNull(vba);
        drw = nPkg.getPart(
                PackagingURIHelper.createPartName("/xl/drawings/vmlDrawing1.vml")
        );
        assertNotNull(drw);
    }

    public void test47504() {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("47504.xlsx");
        assertEquals(1, wb.getNumberOfSheets());
        XSSFSheet sh = wb.getSheetAt(0);
        XSSFDrawing drawing = sh.createDrawingPatriarch();
        List<POIXMLDocumentPart> rels = drawing.getRelations();
        assertEquals(1, rels.size());
        assertEquals("Sheet1!A1", rels.get(0).getPackageRelationship().getTargetURI().getFragment());

        // And again, just to be sure
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        assertEquals(1, wb.getNumberOfSheets());
        sh = wb.getSheetAt(0);
        drawing = sh.createDrawingPatriarch();
        rels = drawing.getRelations();
        assertEquals(1, rels.size());
        assertEquals("Sheet1!A1", rels.get(0).getPackageRelationship().getTargetURI().getFragment());
    }
    
    /**
     * Excel will sometimes write a button with a textbox
     *  containing &gt;br&lt; (not closed!).
     * Clearly Excel shouldn't do this, but test that we can
     *  read the file despite the naughtyness
     */
    public void test49020() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("BrNotClosed.xlsx");
    }

    /**
     * ensure that CTPhoneticPr is loaded by the ooxml test suite so that it is included in poi-ooxml-schemas
     */
    public void test49325() throws Exception {
        XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("49325.xlsx");
        CTWorksheet sh = wb.getSheetAt(0).getCTWorksheet();
        assertNotNull(sh.getPhoneticPr());
    }
    
    /**
     * Names which are defined with a Sheet
     *  should return that sheet index properly 
     */
    public void test48923() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48923.xlsx");
       assertEquals(4, wb.getNumberOfNames());
       
       Name b1 = wb.getName("NameB1");
       Name b2 = wb.getName("NameB2");
       Name sheet2 = wb.getName("NameSheet2");
       Name test = wb.getName("Test");
       
       assertNotNull(b1);
       assertEquals("NameB1", b1.getNameName());
       assertEquals("Sheet1", b1.getSheetName());
       assertEquals(-1, b1.getSheetIndex());
       
       assertNotNull(b2);
       assertEquals("NameB2", b2.getNameName());
       assertEquals("Sheet1", b2.getSheetName());
       assertEquals(-1, b2.getSheetIndex());
       
       assertNotNull(sheet2);
       assertEquals("NameSheet2", sheet2.getNameName());
       assertEquals("Sheet2", sheet2.getSheetName());
       assertEquals(-1, sheet2.getSheetIndex());
       
       assertNotNull(test);
       assertEquals("Test", test.getNameName());
       assertEquals("Sheet1", test.getSheetName());
       assertEquals(-1, test.getSheetIndex());
    }
    
    /**
     * Problem with evaluation formulas due to
     *  NameXPtgs.
     * Blows up on:
     *   IF(B6= (ROUNDUP(B6,0) + ROUNDDOWN(B6,0))/2, MROUND(B6,2),ROUND(B6,0))
     * 
     * TODO: delete this test case when MROUND and VAR are implemented
     */
    public void test48539() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48539.xlsx");
       assertEquals(3, wb.getNumberOfSheets());
       
       // Try each cell individually
       XSSFFormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
       for(int i=0; i<wb.getNumberOfSheets(); i++) {
          Sheet s = wb.getSheetAt(i);
          for(Row r : s) {
             for(Cell c : r) {
                if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    CellValue cv = eval.evaluate(c);
                    if(cv.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        // assert that the calculated value agrees with
                        // the cached formula result calculated by Excel
                        double cachedFormulaResult = c.getNumericCellValue();
                        double evaluatedFormulaResult = cv.getNumberValue();
                        assertEquals(c.getCellFormula(), cachedFormulaResult, evaluatedFormulaResult, 1E-7);
                    }
                }
             }
          }
       }
       
       // Now all of them
        XSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
    }
    
    /**
     * Foreground colours should be found even if
     *  a theme is used 
     */
    public void test48779() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48779.xlsx");
       XSSFCell cell = wb.getSheetAt(0).getRow(0).getCell(0);
       XSSFCellStyle cs = cell.getCellStyle();
       
       assertNotNull(cs);
       assertEquals(1, cs.getIndex());

       // Look at the low level xml elements
       assertEquals(2, cs.getCoreXf().getFillId());
       assertEquals(0, cs.getCoreXf().getXfId());
       assertEquals(true, cs.getCoreXf().getApplyFill());
       
       XSSFCellFill fg = wb.getStylesSource().getFillAt(2);
       assertEquals(0, fg.getFillForegroundColor().getIndexed());
       assertEquals(0.0, fg.getFillForegroundColor().getTint());
       assertEquals("FFFF0000", fg.getFillForegroundColor().getARGBHex());
       assertEquals(64, fg.getFillBackgroundColor().getIndexed());
       
       // Now look higher up
       assertNotNull(cs.getFillForegroundXSSFColor());
       assertEquals(0, cs.getFillForegroundColor());
       assertEquals("FFFF0000", cs.getFillForegroundXSSFColor().getARGBHex());
       assertEquals("FFFF0000", cs.getFillForegroundColorColor().getARGBHex());
       
       assertNotNull(cs.getFillBackgroundColor());
       assertEquals(64, cs.getFillBackgroundColor());
       assertEquals(null, cs.getFillBackgroundXSSFColor().getARGBHex());
       assertEquals(null, cs.getFillBackgroundColorColor().getARGBHex());
    }
    
    /**
     * With HSSF, if you create a font, don't change it, and
     *  create a 2nd, you really do get two fonts that you 
     *  can alter as and when you want.
     * With XSSF, that wasn't the case, but this verfies
     *  that it now is again
     */
    public void test48718() throws Exception {
       // Verify the HSSF behaviour
       // Then ensure the same for XSSF
       Workbook[] wbs = new Workbook[] {
             new HSSFWorkbook(),
             new XSSFWorkbook()
       };
       int[] initialFonts = new int[] { 4, 1 };
       for(int i=0; i<wbs.length; i++) {
          Workbook wb = wbs[i];
          int startingFonts = initialFonts[i];
          
          assertEquals(startingFonts, wb.getNumberOfFonts());
          
          // Get a font, and slightly change it
          Font a = wb.createFont();
          assertEquals(startingFonts+1, wb.getNumberOfFonts());
          a.setFontHeightInPoints((short)23);
          assertEquals(startingFonts+1, wb.getNumberOfFonts());
          
          // Get two more, unchanged
          Font b = wb.createFont();
          assertEquals(startingFonts+2, wb.getNumberOfFonts());
          Font c = wb.createFont();
          assertEquals(startingFonts+3, wb.getNumberOfFonts());
       }
    }
    
    /**
     * Ensure General and @ format are working properly
     *  for integers 
     */
    public void test47490() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("GeneralFormatTests.xlsx");
       Sheet s = wb.getSheetAt(1);
       Row r;
       DataFormatter df = new DataFormatter();
       
       r = s.getRow(1);
       assertEquals(1.0, r.getCell(2).getNumericCellValue());
       assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
       assertEquals("1", df.formatCellValue(r.getCell(2)));
       assertEquals("1", df.formatRawCellContents(1.0, -1, "@"));
       assertEquals("1", df.formatRawCellContents(1.0, -1, "General"));
              
       r = s.getRow(2);
       assertEquals(12.0, r.getCell(2).getNumericCellValue());
       assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
       assertEquals("12", df.formatCellValue(r.getCell(2)));
       assertEquals("12", df.formatRawCellContents(12.0, -1, "@"));
       assertEquals("12", df.formatRawCellContents(12.0, -1, "General"));
       
       r = s.getRow(3);
       assertEquals(123.0, r.getCell(2).getNumericCellValue());
       assertEquals("General", r.getCell(2).getCellStyle().getDataFormatString());
       assertEquals("123", df.formatCellValue(r.getCell(2)));
       assertEquals("123", df.formatRawCellContents(123.0, -1, "@"));
       assertEquals("123", df.formatRawCellContents(123.0, -1, "General"));
    }
    
    /**
     * Ensures that XSSF and HSSF agree with each other,
     *  and with the docs on when fetching the wrong
     *  kind of value from a Formula cell
     */
    public void test47815() {
       Workbook[] wbs = new Workbook[] {
             new HSSFWorkbook(),
             new XSSFWorkbook()
       };
       for(Workbook wb : wbs) {
          Sheet s = wb.createSheet();
          Row r = s.createRow(0);
          
          // Setup
          Cell cn = r.createCell(0, Cell.CELL_TYPE_NUMERIC);
          cn.setCellValue(1.2);
          Cell cs = r.createCell(1, Cell.CELL_TYPE_STRING);
          cs.setCellValue("Testing");
          
          Cell cfn = r.createCell(2, Cell.CELL_TYPE_FORMULA);
          cfn.setCellFormula("A1");  
          Cell cfs = r.createCell(3, Cell.CELL_TYPE_FORMULA);
          cfs.setCellFormula("B1");
          
          FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
          assertEquals(Cell.CELL_TYPE_NUMERIC, fe.evaluate(cfn).getCellType());
          assertEquals(Cell.CELL_TYPE_STRING, fe.evaluate(cfs).getCellType());
          fe.evaluateFormulaCell(cfn);
          fe.evaluateFormulaCell(cfs);
          
          // Now test
          assertEquals(Cell.CELL_TYPE_NUMERIC, cn.getCellType());
          assertEquals(Cell.CELL_TYPE_STRING, cs.getCellType());
          assertEquals(Cell.CELL_TYPE_FORMULA, cfn.getCellType());
          assertEquals(Cell.CELL_TYPE_NUMERIC, cfn.getCachedFormulaResultType());
          assertEquals(Cell.CELL_TYPE_FORMULA, cfs.getCellType());
          assertEquals(Cell.CELL_TYPE_STRING, cfs.getCachedFormulaResultType());
          
          // Different ways of retrieving
          assertEquals(1.2, cn.getNumericCellValue());
          try {
             cn.getRichStringCellValue();
             fail();
          } catch(IllegalStateException e) {}
          
          assertEquals("Testing", cs.getStringCellValue());
          try {
             cs.getNumericCellValue();
             fail();
          } catch(IllegalStateException e) {}
          
          assertEquals(1.2, cfn.getNumericCellValue());
          try {
             cfn.getRichStringCellValue();
             fail();
          } catch(IllegalStateException e) {}
          
          assertEquals("Testing", cfs.getStringCellValue());
          try {
             cfs.getNumericCellValue();
             fail();
          } catch(IllegalStateException e) {}
       }
    }

    /**
     * A problem file from a non-standard source (a scientific instrument that saves its
     * output as an .xlsx file) that have two issues:
     * 1. The Content Type part name is lower-case:  [content_types].xml
     * 2. The file appears to use backslashes as path separators
     *
     * The OPC spec tolerates both of these peculiarities, so does POI
     */
    public void test49609() throws Exception {
        XSSFWorkbook wb =  XSSFTestDataSamples.openSampleWorkbook("49609.xlsx");
        assertEquals("FAM", wb.getSheetName(0));
        assertEquals("Cycle", wb.getSheetAt(0).getRow(0).getCell(1).getStringCellValue());

    }

    public void test49783() throws Exception {
        Workbook wb =  XSSFTestDataSamples.openSampleWorkbook("49783.xlsx");
        Sheet sheet = wb.getSheetAt(0);
        FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
        Cell cell;

        cell = sheet.getRow(0).getCell(0);
        assertEquals("#REF!*#REF!", cell.getCellFormula());
        assertEquals(Cell.CELL_TYPE_ERROR, evaluator.evaluateInCell(cell).getCellType());
        assertEquals("#REF!", FormulaError.forInt(cell.getErrorCellValue()).getString());

        Name nm1 = wb.getName("sale_1");
        assertNotNull("name sale_1 should be present", nm1);
        assertEquals("Sheet1!#REF!", nm1.getRefersToFormula());
        Name nm2 = wb.getName("sale_2");
        assertNotNull("name sale_2 should be present", nm2);
        assertEquals("Sheet1!#REF!", nm2.getRefersToFormula());

        cell = sheet.getRow(1).getCell(0);
        assertEquals("sale_1*sale_2", cell.getCellFormula());
        assertEquals(Cell.CELL_TYPE_ERROR, evaluator.evaluateInCell(cell).getCellType());
        assertEquals("#REF!", FormulaError.forInt(cell.getErrorCellValue()).getString());
    }
    
    /**
     * Creating a rich string of "hello world" and applying
     *  a font to characters 1-5 means we have two strings,
     *  "hello" and " world". As such, we need to apply
     *  preserve spaces to the 2nd bit, lest we end up
     *  with something like "helloworld" !
     */
    public void test49941() throws Exception {
       XSSFWorkbook wb = new XSSFWorkbook();
       XSSFSheet s = wb.createSheet();
       XSSFRow r = s.createRow(0);
       XSSFCell c = r.createCell(0);
       
       // First without fonts
       c.setCellValue(
             new XSSFRichTextString(" with spaces ")
       );
       assertEquals(" with spaces ", c.getRichStringCellValue().toString());
       assertEquals(0, c.getRichStringCellValue().getCTRst().sizeOfRArray());
       assertEquals(true, c.getRichStringCellValue().getCTRst().isSetT());
       // Should have the preserve set
       assertEquals(
             1,
             c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().getLength()
       );
       assertEquals(
             "preserve",
             c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().item(0).getNodeValue()
       );
       
       // Save and check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s = wb.getSheetAt(0);
       r = s.getRow(0);
       c = r.getCell(0);
       assertEquals(" with spaces ", c.getRichStringCellValue().toString());
       assertEquals(0, c.getRichStringCellValue().getCTRst().sizeOfRArray());
       assertEquals(true, c.getRichStringCellValue().getCTRst().isSetT());
       
       // Change the string
       c.setCellValue(
             new XSSFRichTextString("hello world")
       );
       assertEquals("hello world", c.getRichStringCellValue().toString());
       // Won't have preserve
       assertEquals(
             0,
             c.getRichStringCellValue().getCTRst().xgetT().getDomNode().getAttributes().getLength()
       );
       
       // Apply a font
       XSSFFont f = wb.createFont();
       f.setBold(true);
       c.getRichStringCellValue().applyFont(0, 5, f);
       assertEquals("hello world", c.getRichStringCellValue().toString());
       // Does need preserving on the 2nd part
       assertEquals(2, c.getRichStringCellValue().getCTRst().sizeOfRArray());
       assertEquals(
             0,
             c.getRichStringCellValue().getCTRst().getRArray(0).xgetT().getDomNode().getAttributes().getLength()
       );
       assertEquals(
             1,
             c.getRichStringCellValue().getCTRst().getRArray(1).xgetT().getDomNode().getAttributes().getLength()
       );
       assertEquals(
             "preserve",
             c.getRichStringCellValue().getCTRst().getRArray(1).xgetT().getDomNode().getAttributes().item(0).getNodeValue()
       );
       
       // Save and check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s = wb.getSheetAt(0);
       r = s.getRow(0);
       c = r.getCell(0);
       assertEquals("hello world", c.getRichStringCellValue().toString());
    }
    
    /**
     * Repeatedly writing the same file which has styles
     * TODO Currently failing
     */
    public void DISABLEDtest49940() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("styles.xlsx");
       assertEquals(3, wb.getNumberOfSheets());
       assertEquals(10, wb.getStylesSource().getNumCellStyles());
       
       ByteArrayOutputStream b1 = new ByteArrayOutputStream();
       ByteArrayOutputStream b2 = new ByteArrayOutputStream();
       ByteArrayOutputStream b3 = new ByteArrayOutputStream();
       wb.write(b1);
       wb.write(b2);
       wb.write(b3);
       
       for(byte[] data : new byte[][] {
             b1.toByteArray(), b2.toByteArray(), b3.toByteArray()
       }) {
          ByteArrayInputStream bais = new ByteArrayInputStream(data);
          wb = new XSSFWorkbook(bais);
          assertEquals(3, wb.getNumberOfSheets());
          assertEquals(10, wb.getStylesSource().getNumCellStyles());
       }
    }

    /**
     * Various ways of removing a cell formula should all zap
     *  the calcChain entry.
     */
    public void test49966() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("shared_formulas.xlsx");
       XSSFSheet sheet = wb.getSheetAt(0);
       
       // CalcChain has lots of entries
       CalculationChain cc = wb.getCalculationChain();
       assertEquals("A2", cc.getCTCalcChain().getCArray(0).getR());
       assertEquals("A3", cc.getCTCalcChain().getCArray(1).getR());
       assertEquals("A4", cc.getCTCalcChain().getCArray(2).getR());
       assertEquals("A5", cc.getCTCalcChain().getCArray(3).getR());
       assertEquals("A6", cc.getCTCalcChain().getCArray(4).getR());
       assertEquals("A7", cc.getCTCalcChain().getCArray(5).getR());
       assertEquals("A8", cc.getCTCalcChain().getCArray(6).getR());
       assertEquals(40, cc.getCTCalcChain().sizeOfCArray());

       // Try various ways of changing the formulas
       // If it stays a formula, chain entry should remain
       // Otherwise should go
       sheet.getRow(1).getCell(0).setCellFormula("A1"); // stay
       sheet.getRow(2).getCell(0).setCellFormula(null);  // go
       sheet.getRow(3).getCell(0).setCellType(Cell.CELL_TYPE_FORMULA); // stay
       sheet.getRow(4).getCell(0).setCellType(Cell.CELL_TYPE_STRING);  // go
       sheet.getRow(5).removeCell(
             sheet.getRow(5).getCell(0)  // go
       );
        sheet.getRow(6).getCell(0).setCellType(Cell.CELL_TYPE_BLANK);  // go
        sheet.getRow(7).getCell(0).setCellValue((String)null);  // go

       // Save and check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       assertEquals(35, cc.getCTCalcChain().sizeOfCArray());

       cc = wb.getCalculationChain();
       assertEquals("A2", cc.getCTCalcChain().getCArray(0).getR());
       assertEquals("A4", cc.getCTCalcChain().getCArray(1).getR());
       assertEquals("A9", cc.getCTCalcChain().getCArray(2).getR());

    }

    public void test49156() throws Exception {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("49156.xlsx");
        FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();

        Sheet sheet = wb.getSheetAt(0);
        for(Row row : sheet){
            for(Cell cell : row){
                if(cell.getCellType() == Cell.CELL_TYPE_FORMULA){
                    formulaEvaluator.evaluateInCell(cell); // caused NPE on some cells
                }
            }
        }
    }
    
    /**
     * Newlines are valid characters in a formula
     */
    public void test50440() throws Exception {
       Workbook wb = XSSFTestDataSamples.openSampleWorkbook("NewlineInFormulas.xlsx");
       Sheet s = wb.getSheetAt(0);
       Cell c = s.getRow(0).getCell(0);
       
       assertEquals("SUM(\n1,2\n)", c.getCellFormula());
       assertEquals(3.0, c.getNumericCellValue());
       
       FormulaEvaluator formulaEvaluator = wb.getCreationHelper().createFormulaEvaluator();
       formulaEvaluator.evaluateFormulaCell(c);
       
       assertEquals("SUM(\n1,2\n)", c.getCellFormula());
       assertEquals(3.0, c.getNumericCellValue());
    }
    
    /**
     * Moving a cell comment from one cell to another
     */
    public void test50795() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50795.xlsx");
       XSSFSheet sheet = wb.getSheetAt(0);
       XSSFRow row = sheet.getRow(0);

       XSSFCell cellWith = row.getCell(0);
       XSSFCell cellWithoutComment = row.getCell(1);
       
       assertNotNull(cellWith.getCellComment());
       assertNull(cellWithoutComment.getCellComment());
       
       String exp = "\u0410\u0432\u0442\u043e\u0440:\ncomment";
       XSSFComment comment = cellWith.getCellComment();
       assertEquals(exp, comment.getString().getString());
       
       
       // Check we can write it out and read it back as-is
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sheet = wb.getSheetAt(0);
       row = sheet.getRow(0);
       cellWith = row.getCell(0);
       cellWithoutComment = row.getCell(1);
       
       // Double check things are as expected
       assertNotNull(cellWith.getCellComment());
       assertNull(cellWithoutComment.getCellComment());
       comment = cellWith.getCellComment();
       assertEquals(exp, comment.getString().getString());

       
       // Move the comment
       cellWithoutComment.setCellComment(comment);
       
       
       // Write out and re-check
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sheet = wb.getSheetAt(0);
       row = sheet.getRow(0);
       
       // Ensure it swapped over
       cellWith = row.getCell(0);
       cellWithoutComment = row.getCell(1);
       assertNull(cellWith.getCellComment());
       assertNotNull(cellWithoutComment.getCellComment());
       
       comment = cellWithoutComment.getCellComment();
       assertEquals(exp, comment.getString().getString());
    }
    
    /**
     * When the cell background colour is set with one of the first
     *  two columns of the theme colour palette, the colours are 
     *  shades of white or black.
     * For those cases, ensure we don't break on reading the colour
     */
    public void test50299() throws Exception {
       Workbook wb = XSSFTestDataSamples.openSampleWorkbook("50299.xlsx");
       
       // Check all the colours
       for(int sn=0; sn<wb.getNumberOfSheets(); sn++) {
          Sheet s = wb.getSheetAt(sn);
          for(Row r : s) {
             for(Cell c : r) {
                CellStyle cs = c.getCellStyle();
                if(cs != null) {
                   cs.getFillForegroundColor();
                }
             }
          }
       }
       
       // Check one bit in detail
       // Check that we get back foreground=0 for the theme colours,
       //  and background=64 for the auto colouring
       Sheet s = wb.getSheetAt(0);
       assertEquals(0,  s.getRow(0).getCell(8).getCellStyle().getFillForegroundColor());
       assertEquals(64, s.getRow(0).getCell(8).getCellStyle().getFillBackgroundColor());
       assertEquals(0,  s.getRow(1).getCell(8).getCellStyle().getFillForegroundColor());
       assertEquals(64, s.getRow(1).getCell(8).getCellStyle().getFillBackgroundColor());
    }
    
    /**
     * Excel .xls style indexed colours in a .xlsx file
     */
    public void test50786() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50786-indexed_colours.xlsx");
       XSSFSheet s = wb.getSheetAt(0);
       XSSFRow r = s.getRow(2);
       
       // Check we have the right cell
       XSSFCell c = r.getCell(1);
       assertEquals("test\u00a0", c.getRichStringCellValue().getString());
       
       // It should be light green
       XSSFCellStyle cs = c.getCellStyle();
       assertEquals(42, cs.getFillForegroundColor());
       assertEquals(42, cs.getFillForegroundColorColor().getIndexed());
       assertNotNull(cs.getFillForegroundColorColor().getRgb());
       assertEquals("FFCCFFCC", cs.getFillForegroundColorColor().getARGBHex());
    }
    
    /**
     * If the border colours are set with themes, then we 
     *  should still be able to get colours
     */
    public void test50846() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50846-border_colours.xlsx");
       
       XSSFSheet sheet = wb.getSheetAt(0);
       XSSFRow row = sheet.getRow(0);
       
       // Border from a theme, brown
       XSSFCell cellT = row.getCell(0);
       XSSFCellStyle styleT = cellT.getCellStyle();
       XSSFColor colorT = styleT.getBottomBorderXSSFColor();
       
       assertEquals(5, colorT.getTheme());
       assertEquals("FFC0504D", colorT.getARGBHex());
       
       // Border from a style direct, red
       XSSFCell cellS = row.getCell(1);
       XSSFCellStyle styleS = cellS.getCellStyle();
       XSSFColor colorS = styleS.getBottomBorderXSSFColor();
       
       assertEquals(0, colorS.getTheme());
       assertEquals("FFFF0000", colorS.getARGBHex());
    }
    
    /**
     * Fonts where their colours come from the theme rather
     *  then being set explicitly still should allow the
     *  fetching of the RGB.
     */
    public void test50784() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50784-font_theme_colours.xlsx");
       XSSFSheet s = wb.getSheetAt(0);
       XSSFRow r = s.getRow(0);
       
       // Column 1 has a font with regular colours
       XSSFCell cr = r.getCell(1);
       XSSFFont fr = wb.getFontAt( cr.getCellStyle().getFontIndex() );
       XSSFColor colr =  fr.getXSSFColor();
       // No theme, has colours
       assertEquals(0, colr.getTheme());
       assertNotNull( colr.getRgb() );
       
       // Column 0 has a font with colours from a theme
       XSSFCell ct = r.getCell(0);
       XSSFFont ft = wb.getFontAt( ct.getCellStyle().getFontIndex() );
       XSSFColor colt =  ft.getXSSFColor();
       // Has a theme, which has the colours on it
       assertEquals(9, colt.getTheme());
       XSSFColor themeC = wb.getTheme().getThemeColor(colt.getTheme());
       assertNotNull( themeC.getRgb() );
       assertNotNull( colt.getRgb() );
       assertEquals( themeC.getARGBHex(), colt.getARGBHex() ); // The same colour
    }

    /**
     * New lines were being eaten when setting a font on
     *  a rich text string
     */
    public void test48877() throws Exception {
       String text = "Use \n with word wrap on to create a new line.\n" +
          "This line finishes with two trailing spaces.  ";
       
       Workbook wb = new XSSFWorkbook();
       Sheet sheet = wb.createSheet();

       Font font1 = wb.createFont();
       font1.setColor((short) 20);

       Row row = sheet.createRow(2);
       Cell cell = row.createCell(2);

       RichTextString richTextString =
          wb.getCreationHelper().createRichTextString(text);
       
       // Check the text has the newline
       assertEquals(text, richTextString.getString());
       
       // Apply the font
       richTextString.applyFont(0, 3, font1);
       cell.setCellValue(richTextString);

       // To enable newlines you need set a cell styles with wrap=true
       CellStyle cs = wb.createCellStyle();
       cs.setWrapText(true);
       cell.setCellStyle(cs);

       // Check the text has the
       assertEquals(text, cell.getStringCellValue());
       
       // Save the file and re-read it
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       sheet = wb.getSheetAt(0);
       row = sheet.getRow(2);
       cell = row.getCell(2);
       assertEquals(text, cell.getStringCellValue());
    }
    
    /**
     * Adding sheets when one has a table, then re-ordering
     */
    public void test50867() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50867_with_table.xlsx");
       assertEquals(3, wb.getNumberOfSheets());
       
       XSSFSheet s1 = wb.getSheetAt(0);
       XSSFSheet s2 = wb.getSheetAt(1);
       XSSFSheet s3 = wb.getSheetAt(2);
       assertEquals(1, s1.getTables().size());
       assertEquals(0, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       
       XSSFTable t = s1.getTables().get(0);
       assertEquals("Tabella1", t.getName());
       assertEquals("Tabella1", t.getDisplayName());
       assertEquals("A1:C3", t.getCTTable().getRef());
       
       // Add a sheet and re-order
       XSSFSheet s4 = wb.createSheet("NewSheet");
       wb.setSheetOrder(s4.getSheetName(), 0);
       
       // Check on tables
       assertEquals(1, s1.getTables().size());
       assertEquals(0, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       // Refetch to get the new order
       s1 = wb.getSheetAt(0);
       s2 = wb.getSheetAt(1);
       s3 = wb.getSheetAt(2);
       s4 = wb.getSheetAt(3);
       assertEquals(0, s1.getTables().size());
       assertEquals(1, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       // Save and re-load
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s1 = wb.getSheetAt(0);
       s2 = wb.getSheetAt(1);
       s3 = wb.getSheetAt(2);
       s4 = wb.getSheetAt(3);
       assertEquals(0, s1.getTables().size());
       assertEquals(1, s2.getTables().size());
       assertEquals(0, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       t = s2.getTables().get(0);
       assertEquals("Tabella1", t.getName());
       assertEquals("Tabella1", t.getDisplayName());
       assertEquals("A1:C3", t.getCTTable().getRef());

       
       // Add some more tables, and check
       t = s2.createTable();
       t.setName("New 2");
       t.setDisplayName("New 2");
       t = s3.createTable();
       t.setName("New 3");
       t.setDisplayName("New 3");
       
       wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
       s1 = wb.getSheetAt(0);
       s2 = wb.getSheetAt(1);
       s3 = wb.getSheetAt(2);
       s4 = wb.getSheetAt(3);
       assertEquals(0, s1.getTables().size());
       assertEquals(2, s2.getTables().size());
       assertEquals(1, s3.getTables().size());
       assertEquals(0, s4.getTables().size());
       
       t = s2.getTables().get(0);
       assertEquals("Tabella1", t.getName());
       assertEquals("Tabella1", t.getDisplayName());
       assertEquals("A1:C3", t.getCTTable().getRef());
       
       t = s2.getTables().get(1);
       assertEquals("New 2", t.getName());
       assertEquals("New 2", t.getDisplayName());
       
       t = s3.getTables().get(0);
       assertEquals("New 3", t.getName());
       assertEquals("New 3", t.getDisplayName());
       
       // Check the relationships
       assertEquals(0, s1.getRelations().size());
       assertEquals(3, s2.getRelations().size());
       assertEquals(1, s3.getRelations().size());
       assertEquals(0, s4.getRelations().size());
       
       assertEquals(
             XSSFRelation.PRINTER_SETTINGS.getContentType(), 
             s2.getRelations().get(0).getPackagePart().getContentType()
       );
       assertEquals(
             XSSFRelation.TABLE.getContentType(), 
             s2.getRelations().get(1).getPackagePart().getContentType()
       );
       assertEquals(
             XSSFRelation.TABLE.getContentType(), 
             s2.getRelations().get(2).getPackagePart().getContentType()
       );
       assertEquals(
             XSSFRelation.TABLE.getContentType(), 
             s3.getRelations().get(0).getPackagePart().getContentType()
       );
       assertEquals(
             "/xl/tables/table3.xml",
             s3.getRelations().get(0).getPackagePart().getPartName().toString()
       );
    }
    
    /**
     * Setting repeating rows and columns shouldn't break
     *  any print settings that were there before
     */
    public void test49253() throws Exception {
       XSSFWorkbook wb1 = new XSSFWorkbook();
       XSSFWorkbook wb2 = new XSSFWorkbook();
       
       // No print settings before repeating
       XSSFSheet s1 = wb1.createSheet(); 
       assertEquals(false, s1.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s1.getCTWorksheet().isSetPageMargins());
       
       wb1.setRepeatingRowsAndColumns(0, 2, 3, 1, 2);
       
       assertEquals(true, s1.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s1.getCTWorksheet().isSetPageMargins());
       
       XSSFPrintSetup ps1 = s1.getPrintSetup();
       assertEquals(false, ps1.getValidSettings());
       assertEquals(false, ps1.getLandscape());
       
       
       // Had valid print settings before repeating
       XSSFSheet s2 = wb2.createSheet();
       XSSFPrintSetup ps2 = s2.getPrintSetup();
       assertEquals(true, s2.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s2.getCTWorksheet().isSetPageMargins());
       
       ps2.setLandscape(false);
       assertEquals(true, ps2.getValidSettings());
       assertEquals(false, ps2.getLandscape());
       
       wb2.setRepeatingRowsAndColumns(0, 2, 3, 1, 2);
       
       ps2 = s2.getPrintSetup();
       assertEquals(true, s2.getCTWorksheet().isSetPageSetup());
       assertEquals(true, s2.getCTWorksheet().isSetPageMargins());
       assertEquals(true, ps2.getValidSettings());
       assertEquals(false, ps2.getLandscape());
    }
    
    /**
     * CreateFreezePane column/row order check 
     */
    public void test49381() throws Exception {
       Workbook[] wbs = new Workbook[] { new HSSFWorkbook(), new XSSFWorkbook() };
       int colSplit = 1;
       int rowSplit = 2;
       int leftmostColumn = 3;
       int topRow = 4;

       for(Workbook wb : wbs) {
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
          if(wb == wbs[0]) {
             assertEquals(64, paneInfo.getVerticalSplitLeftColumn()); // HSSF
          } else {
             assertEquals(0,  paneInfo.getVerticalSplitLeftColumn()); // XSSF
          }
          assertEquals(3,  paneInfo.getHorizontalSplitTopRow());
          
          // Now a column only freezepane
          s.createFreezePane(4, 0);
          paneInfo = s.getPaneInformation();
          
          assertEquals(4,  paneInfo.getVerticalSplitPosition());
          assertEquals(0,  paneInfo.getHorizontalSplitPosition());
          assertEquals(4 , paneInfo.getVerticalSplitLeftColumn());
          assertEquals(0,  paneInfo.getHorizontalSplitTopRow());
       }
    }
    
    /**
     * Repeatedly writing a file.
     * Something with the SharedStringsTable currently breaks...
     */
    public void DISABLEDtest46662() throws Exception {
       // New file
       XSSFWorkbook wb = new XSSFWorkbook();
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       
       // Simple file
       wb = XSSFTestDataSamples.openSampleWorkbook("sample.xlsx");
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       XSSFTestDataSamples.writeOutAndReadBack(wb);
       
       // Complex file
       // TODO
    }
}
