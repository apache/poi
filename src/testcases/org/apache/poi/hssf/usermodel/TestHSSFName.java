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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.BaseTestNamedRange;
import org.apache.poi.ss.util.AreaReference;

/**
 * Tests various functionality having to do with {@link org.apache.poi.ss.usermodel.Name}.
 *
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author ROMANL
 * @author Danny Mui (danny at muibros.com)
 * @author Amol S. Deshmukh &lt; amol at ap ache dot org &gt;
 */
public final class TestHSSFName extends BaseTestNamedRange {

    /**
     * For manipulating the internals of {@link HSSFName} during testing.<br/>
     * Some tests need a {@link NameRecord} with unusual state, not normally producible by POI.
     * This method achieves the aims at low cost without augmenting the POI usermodel api.
     * @return a reference to the wrapped {@link NameRecord}
     */
    public static NameRecord getNameRecord(HSSFName definedName) {

        Field f;
        try {
            f = HSSFName.class.getDeclaredField("_definedNameRec");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        f.setAccessible(true);
        try {
            return (NameRecord) f.get(definedName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public TestHSSFName() {
        super(HSSFITestDataProvider.instance);
    }

    public void testRepeatingRowsAndColumsNames() {
         // First test that setting RR&C for same sheet more than once only creates a
         // single  Print_Titles built-in record
         HSSFWorkbook wb = new HSSFWorkbook();
         HSSFSheet sheet = wb.createSheet("FirstSheet");

         // set repeating rows and columns twice for the first sheet
         for (int i = 0; i < 2; i++) {
             wb.setRepeatingRowsAndColumns(0, 0, 0, 0, 3-1);
             sheet.createFreezePane(0, 3);
         }
         assertEquals(1, wb.getNumberOfNames());
         HSSFName nr1 = wb.getNameAt(0);

         assertEquals("Print_Titles", nr1.getNameName());
         if (false) {
             //     TODO - full column references not rendering properly, absolute markers not present either
             assertEquals("FirstSheet!$A:$A,FirstSheet!$1:$3", nr1.getRefersToFormula());
         } else {
             assertEquals("FirstSheet!A:A,FirstSheet!$A$1:$IV$3", nr1.getRefersToFormula());
         }

         // Save and re-open
         HSSFWorkbook nwb = HSSFTestDataSamples.writeOutAndReadBack(wb);

         assertEquals(1, nwb.getNumberOfNames());
         nr1 = nwb.getNameAt(0);

         assertEquals("Print_Titles", nr1.getNameName());
         assertEquals("FirstSheet!A:A,FirstSheet!$A$1:$IV$3", nr1.getRefersToFormula());

         // check that setting RR&C on a second sheet causes a new Print_Titles built-in
         // name to be created
         sheet = nwb.createSheet("SecondSheet");
         nwb.setRepeatingRowsAndColumns(1, 1, 2, 0, 0);

         assertEquals(2, nwb.getNumberOfNames());
         HSSFName nr2 = nwb.getNameAt(1);

         assertEquals("Print_Titles", nr2.getNameName());
         assertEquals("SecondSheet!B:C,SecondSheet!$A$1:$IV$1", nr2.getRefersToFormula());

         if (false) {
             // In case you fancy checking in excel, to ensure it
             //  won't complain about the file now
             try {
                 File tempFile = File.createTempFile("POI-45126-", ".xls");
                 FileOutputStream fout = new FileOutputStream(tempFile);
                 nwb.write(fout);
                 fout.close();
                 System.out.println("check out " + tempFile.getAbsolutePath());
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }

    public void testNamedRange() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("Simple.xls");

        //Creating new Named Range
        HSSFName newNamedRange = wb.createName();

        //Getting Sheet Name for the reference
        String sheetName = wb.getSheetName(0);

        //Setting its name
        newNamedRange.setNameName("RangeTest");
        //Setting its reference
        newNamedRange.setRefersToFormula(sheetName + "!$D$4:$E$8");

        //Getting NAmed Range
        HSSFName namedRange1 = wb.getNameAt(0);
        //Getting it sheet name
        sheetName = namedRange1.getSheetName();

        // sanity check
        SanityChecker c = new SanityChecker();
        c.checkHSSFWorkbook(wb);

        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
        HSSFName nm =wb.getNameAt(wb.getNameIndex("RangeTest"));
        assertTrue("Name is "+nm.getNameName(),"RangeTest".equals(nm.getNameName()));
        assertEquals(wb.getSheetName(0)+"!$D$4:$E$8", nm.getRefersToFormula());
    }

    /**
     * Reads an excel file already containing a named range.
     * <p>
     * Addresses Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=9632" target="_bug">#9632</a>
     */
    public void testNamedRead() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("namedinput.xls");

        //Get index of the named range with the name = "NamedRangeName" , which was defined in input.xls as A1:D10
        int NamedRangeIndex     = wb.getNameIndex("NamedRangeName");

        //Getting NAmed Range
        HSSFName namedRange1 = wb.getNameAt(NamedRangeIndex);
        String sheetName = wb.getSheetName(0);

        //Getting its reference
        String reference = namedRange1.getRefersToFormula();

        assertEquals(sheetName+"!$A$1:$D$10", reference);

        HSSFName namedRange2 = wb.getNameAt(1);

        assertEquals(sheetName+"!$D$17:$G$27", namedRange2.getRefersToFormula());
        assertEquals("SecondNamedRange", namedRange2.getNameName());
    }

    /**
     * Reads an excel file already containing a named range and updates it
     * <p>
     * Addresses Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=16411" target="_bug">#16411</a>
     */
    public void testNamedReadModify() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("namedinput.xls");

        HSSFName name = wb.getNameAt(0);
        String sheetName = wb.getSheetName(0);

        assertEquals(sheetName+"!$A$1:$D$10", name.getRefersToFormula());

        name = wb.getNameAt(1);
        String newReference = sheetName +"!$A$1:$C$36";

        name.setRefersToFormula(newReference);
        assertEquals(newReference, name.getRefersToFormula());
    }

     /**
      * Test to see if the print area can be retrieved from an excel created file
      */
     public void testPrintAreaFileRead() {
         HSSFWorkbook workbook = HSSFTestDataSamples.openSampleWorkbook("SimpleWithPrintArea.xls");

        String sheetName = workbook.getSheetName(0);
        String reference = sheetName+"!$A$1:$C$5";

        assertEquals(reference, workbook.getPrintArea(0));
    }


    public void testDeletedReference() {
        HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("24207.xls");
        assertEquals(2, wb.getNumberOfNames());

        HSSFName name1 = wb.getNameAt(0);
        assertEquals("a", name1.getNameName());
        assertEquals("Sheet1!$A$1", name1.getRefersToFormula());
        new AreaReference(name1.getRefersToFormula());
        assertTrue("Successfully constructed first reference", true);

        HSSFName name2 = wb.getNameAt(1);
        assertEquals("b", name2.getNameName());
        assertEquals("Sheet1!#REF!", name2.getRefersToFormula());
        assertTrue(name2.isDeleted());
        try {
            new AreaReference(name2.getRefersToFormula());
            fail("attempt to supply an invalid reference to AreaReference constructor results in exception");
        } catch (StringIndexOutOfBoundsException e) { // TODO - use a different exception for this condition
            // expected during successful test
        }
    }

    /**
     * When setting A1 type of references with HSSFName.setRefersToFormula
     * must set the type of operands to Ptg.CLASS_REF,
     * otherwise created named don't appear in the drop-down to the left of formula bar in Excel
     */
    public void testTypeOfRootPtg(){
        HSSFWorkbook wb = new HSSFWorkbook();
        wb.createSheet("CSCO");

        Ptg[] ptgs = HSSFFormulaParser.parse("CSCO!$E$71", wb, FormulaType.NAMEDRANGE, 0);
        for (int i = 0; i < ptgs.length; i++) {
            assertEquals('R', ptgs[i].getRVAType());
        }

    }
}
