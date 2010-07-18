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

import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
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
     */
    public void DISABLEDtest48539() throws Exception {
       XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48539.xlsx");
       assertEquals(3, wb.getNumberOfSheets());
       
       // Try each cell individually
       XSSFFormulaEvaluator eval = new XSSFFormulaEvaluator(wb);
       for(int i=0; i<wb.getNumberOfSheets(); i++) {
          Sheet s = wb.getSheetAt(i);
          for(Row r : s) {
             for(Cell c : r) {
                if(c.getCellType() == Cell.CELL_TYPE_FORMULA) {
                   eval.evaluate(c);
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
}
