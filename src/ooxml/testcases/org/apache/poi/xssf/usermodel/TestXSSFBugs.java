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
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;

public final class TestXSSFBugs extends BaseTestBugzillaIssues {
    @Override
    protected XSSFITestDataProvider getTestDataProvider(){
        return XSSFITestDataProvider.getInstance();
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
        XSSFWorkbook wb = getTestDataProvider().openSampleWorkbook("45430.xlsx");
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
        XSSFWorkbook wb = getTestDataProvider().openSampleWorkbook("45431.xlsm");
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
        XSSFWorkbook wb = getTestDataProvider().openSampleWorkbook("47504.xlsx");
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

}
