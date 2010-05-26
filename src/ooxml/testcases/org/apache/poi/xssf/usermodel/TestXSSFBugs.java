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

import java.awt.peer.SystemTrayPeer;
import java.util.List;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
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
}
