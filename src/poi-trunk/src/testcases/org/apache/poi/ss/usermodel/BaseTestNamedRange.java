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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.IOUtils;
import org.junit.Test;

/**
 * Tests of implementations of {@link org.apache.poi.ss.usermodel.Name}.
 *
 * @author Yegor Kozlov
 */
public abstract class BaseTestNamedRange {

    private final ITestDataProvider _testDataProvider;

    protected BaseTestNamedRange(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    @Test
    public final void testCreate() throws Exception {
        // Create a new workbook
        Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Test1");
        wb.createSheet("Testing Named Ranges");

        Name name1 = wb.createName();
        name1.setNameName("testOne");

        //setting a duplicate name should throw IllegalArgumentException
        Name name2 = wb.createName();
        try {
            name2.setNameName("testOne");
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("The workbook already contains this name: testOne", e.getMessage());
        }
        //the check for duplicates is case-insensitive
        try {
            name2.setNameName("TESTone");
            fail("expected exception");
        } catch (IllegalArgumentException e){
            assertEquals("The workbook already contains this name: TESTone", e.getMessage());
        }

        name2.setNameName("testTwo");

        String ref1 = "Test1!$A$1:$B$1";
        name1.setRefersToFormula(ref1);
        assertEquals(ref1, name1.getRefersToFormula());
        assertEquals("Test1", name1.getSheetName());

        String ref2 = "'Testing Named Ranges'!$A$1:$B$1";
        name1.setRefersToFormula(ref2);
        assertEquals("'Testing Named Ranges'!$A$1:$B$1", name1.getRefersToFormula());
        assertEquals("Testing Named Ranges", name1.getSheetName());

        assertEquals(-1, name1.getSheetIndex());
        name1.setSheetIndex(-1);
        assertEquals(-1, name1.getSheetIndex());
        try {
            name1.setSheetIndex(2);
            fail("should throw IllegalArgumentException");
        } catch(IllegalArgumentException e){
            assertEquals("Sheet index (2) is out of range (0..1)", e.getMessage());
        }

        name1.setSheetIndex(1);
        assertEquals(1, name1.getSheetIndex());

        //-1 means the name applies to the entire workbook
        name1.setSheetIndex(-1);
        assertEquals(-1, name1.getSheetIndex());

        //names cannot be blank and must begin with a letter or underscore and not contain spaces
        String[] invalidNames = {"", "123", "1Name", "Named Range"};
        for (String name : invalidNames) {
            try {
                name1.setNameName(name);
                fail("should have thrown exceptiuon due to invalid name: " + name);
            } catch (IllegalArgumentException e) {
                // expected during successful test
            }
        }
        
        wb.close();
    }

    @Test
    public final void testUnicodeNamedRange() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();
        wb1.createSheet("Test");
        Name name = wb1.createName();
        name.setNameName("\u03B1");
        name.setRefersToFormula("Test!$D$3:$E$8");


        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        Name name2 = wb2.getNameAt(0);

        assertEquals("\u03B1", name2.getNameName());
        assertEquals("Test!$D$3:$E$8", name2.getRefersToFormula());
        
        wb2.close();
        wb1.close();
    }

    @Test
    public final void testAddRemove() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        assertEquals(0, wb.getNumberOfNames());
        Name name1 = wb.createName();
        name1.setNameName("name1");
        assertEquals(1, wb.getNumberOfNames());

        Name name2 = wb.createName();
        name2.setNameName("name2");
        assertEquals(2, wb.getNumberOfNames());

        Name name3 = wb.createName();
        name3.setNameName("name3");
        assertEquals(3, wb.getNumberOfNames());

        wb.removeName("name2");
        assertEquals(2, wb.getNumberOfNames());

        wb.removeName(0);
        assertEquals(1, wb.getNumberOfNames());
        
        wb.close();
    }

    @Test
    public final void testScope() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet();
        wb.createSheet();

        Name name;

        name = wb.createName();
        name.setNameName("aaa");
        name = wb.createName();
        try {
            name.setNameName("aaa");
            fail("Expected exception");
        } catch(Exception e){
            assertEquals("The workbook already contains this name: aaa", e.getMessage());
        }

        name = wb.createName();
        name.setSheetIndex(0);
        name.setNameName("aaa");
        name = wb.createName();
        name.setSheetIndex(0);
        try {
            name.setNameName("aaa");
            fail("Expected exception");
        } catch(Exception e){
            assertEquals("The sheet already contains this name: aaa", e.getMessage());
        }

        name = wb.createName();
        name.setSheetIndex(1);
        name.setNameName("aaa");
        name = wb.createName();
        name.setSheetIndex(1);
        try {
            name.setNameName("aaa");
            fail("Expected exception");
        } catch(Exception e){
            assertEquals("The sheet already contains this name: aaa", e.getMessage());
        }

        assertEquals(3, wb.getNames("aaa").size());
        
        wb.close();
    }

    /**
     * Test case provided by czhang@cambian.com (Chun Zhang)
     * <p>
     * Addresses Bug <a href="http://issues.apache.org/bugzilla/show_bug.cgi?id=13775" target="_bug">#13775</a>
     */
    @Test
    public final void testMultiNamedRange() throws Exception {

         // Create a new workbook
         Workbook wb1 = _testDataProvider.createWorkbook();

         // Create a worksheet 'sheet1' in the new workbook
         wb1.createSheet ();
         wb1.setSheetName (0, "sheet1");

         // Create another worksheet 'sheet2' in the new workbook
         wb1.createSheet ();
         wb1.setSheetName (1, "sheet2");

         // Create a new named range for worksheet 'sheet1'
         Name namedRange1 = wb1.createName();

         // Set the name for the named range for worksheet 'sheet1'
         namedRange1.setNameName("RangeTest1");

         // Set the reference for the named range for worksheet 'sheet1'
         namedRange1.setRefersToFormula("sheet1" + "!$A$1:$L$41");

         // Create a new named range for worksheet 'sheet2'
         Name namedRange2 = wb1.createName();

         // Set the name for the named range for worksheet 'sheet2'
         namedRange2.setNameName("RangeTest2");

         // Set the reference for the named range for worksheet 'sheet2'
         namedRange2.setRefersToFormula("sheet2" + "!$A$1:$O$21");

         // Write the workbook to a file
         // Read the Excel file and verify its content
         Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
         Name nm1 = wb2.getName("RangeTest1");
         assertTrue("Name is "+nm1.getNameName(),"RangeTest1".equals(nm1.getNameName()));
         assertTrue("Reference is "+nm1.getRefersToFormula(),(wb2.getSheetName(0)+"!$A$1:$L$41").equals(nm1.getRefersToFormula()));

         Name nm2 = wb2.getName("RangeTest2");
         assertTrue("Name is "+nm2.getNameName(),"RangeTest2".equals(nm2.getNameName()));
         assertTrue("Reference is "+nm2.getRefersToFormula(),(wb2.getSheetName(1)+"!$A$1:$O$21").equals(nm2.getRefersToFormula()));
         
         wb2.close();
         wb1.close();
     }

    /**
     * Test to see if the print areas can be retrieved/created in memory
     */
    @Test
    public final void testSinglePrintArea() throws Exception {
        Workbook workbook = _testDataProvider.createWorkbook();
        workbook.createSheet("Test Print Area");
        String sheetName = workbook.getSheetName(0);

        String reference = "$A$1:$B$1";
        workbook.setPrintArea(0, reference);

        String retrievedPrintArea = workbook.getPrintArea(0);

        assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
        assertEquals("'" + sheetName + "'!$A$1:$B$1", retrievedPrintArea);
        
        workbook.close();
    }

     /**
      * For Convenience, don't force sheet names to be used
      */
    @Test
     public final void testSinglePrintAreaWOSheet() throws Exception
     {
         Workbook workbook = _testDataProvider.createWorkbook();
         workbook.createSheet("Test Print Area");
         String sheetName = workbook.getSheetName(0);

         String reference = "$A$1:$B$1";
         workbook.setPrintArea(0, reference);

         String retrievedPrintArea = workbook.getPrintArea(0);

         assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
         assertEquals("'" + sheetName + "'!" + reference, retrievedPrintArea);
         
         workbook.close();
     }

     /**
      * Test to see if the print area made it to the file
      */
    @Test
    public final void testPrintAreaFile() throws Exception {
         Workbook wb1 = _testDataProvider.createWorkbook();
         wb1.createSheet("Test Print Area");
         String sheetName = wb1.getSheetName(0);


         String reference = "$A$1:$B$1";
         wb1.setPrintArea(0, reference);

         Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);

         String retrievedPrintArea = wb2.getPrintArea(0);
         assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
         assertEquals("References Match", "'" + sheetName + "'!$A$1:$B$1", retrievedPrintArea);
         
         wb2.close();
         wb1.close();
    }

    /**
     * Test to see if multiple print areas made it to the file
     */
    @Test
    public final void testMultiplePrintAreaFile() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();

        wb1.createSheet("Sheet1");
        wb1.createSheet("Sheet2");
        wb1.createSheet("Sheet3");
        String reference1 = "$A$1:$B$1";
        String reference2 = "$B$2:$D$5";
        String reference3 = "$D$2:$F$5";

        wb1.setPrintArea(0, reference1);
        wb1.setPrintArea(1, reference2);
        wb1.setPrintArea(2, reference3);

        //Check created print areas
        String retrievedPrintArea;

        retrievedPrintArea = wb1.getPrintArea(0);
        assertNotNull("Print Area Not Found (Sheet 1)", retrievedPrintArea);
        assertEquals("Sheet1!" + reference1, retrievedPrintArea);

        retrievedPrintArea = wb1.getPrintArea(1);
        assertNotNull("Print Area Not Found (Sheet 2)", retrievedPrintArea);
        assertEquals("Sheet2!" + reference2, retrievedPrintArea);

        retrievedPrintArea = wb1.getPrintArea(2);
        assertNotNull("Print Area Not Found (Sheet 3)", retrievedPrintArea);
        assertEquals("Sheet3!" + reference3, retrievedPrintArea);

        // Check print areas after re-reading workbook
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);

        retrievedPrintArea = wb2.getPrintArea(0);
        assertNotNull("Print Area Not Found (Sheet 1)", retrievedPrintArea);
        assertEquals("Sheet1!" + reference1, retrievedPrintArea);

        retrievedPrintArea = wb2.getPrintArea(1);
        assertNotNull("Print Area Not Found (Sheet 2)", retrievedPrintArea);
        assertEquals("Sheet2!" + reference2, retrievedPrintArea);

        retrievedPrintArea = wb2.getPrintArea(2);
        assertNotNull("Print Area Not Found (Sheet 3)", retrievedPrintArea);
        assertEquals("Sheet3!" + reference3, retrievedPrintArea);
        
        wb2.close();
        wb1.close();
    }

    /**
     * Tests the setting of print areas with coordinates (Row/Column designations)
     *
     */
    @Test
    public final void testPrintAreaCoords() throws Exception {
        Workbook workbook = _testDataProvider.createWorkbook();
        workbook.createSheet("Test Print Area");
        String sheetName = workbook.getSheetName(0);

        workbook.setPrintArea(0, 0, 1, 0, 0);

        String retrievedPrintArea = workbook.getPrintArea(0);

        assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
        assertEquals("'" + sheetName + "'!$A$1:$B$1", retrievedPrintArea);
        
        workbook.close();
    }


    /**
     * Tests the parsing of union area expressions, and re-display in the presence of sheet names
     * with special characters.
     */
    @Test
    public final void testPrintAreaUnion() throws Exception {
        Workbook workbook = _testDataProvider.createWorkbook();
        workbook.createSheet("Test Print Area");

        String reference = "$A$1:$B$1,$D$1:$F$2";
        workbook.setPrintArea(0, reference);
        String retrievedPrintArea = workbook.getPrintArea(0);
        assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);
        assertEquals("'Test Print Area'!$A$1:$B$1,'Test Print Area'!$D$1:$F$2", retrievedPrintArea);
        
        workbook.close();
    }

    /**
     * Verifies an existing print area is deleted
     *
     */
    @Test
    public final void testPrintAreaRemove() throws Exception {
        Workbook workbook = _testDataProvider.createWorkbook();
        workbook.createSheet("Test Print Area");
        workbook.getSheetName(0);

        workbook.setPrintArea(0, 0, 1, 0, 0);

        String retrievedPrintArea = workbook.getPrintArea(0);

        assertNotNull("Print Area not defined for first sheet", retrievedPrintArea);

        workbook.removePrintArea(0);
        assertNull("PrintArea was not removed", workbook.getPrintArea(0));
        workbook.close();
    }

    /**
     * Test that multiple named ranges can be added written and read
     */
    @Test
    public final void testMultipleNamedWrite() throws Exception {
        Workbook wb1 = _testDataProvider.createWorkbook();


        wb1.createSheet("testSheet1");
        String sheetName = wb1.getSheetName(0);

        assertEquals("testSheet1", sheetName);

        //Creating new Named Range
        Name newNamedRange = wb1.createName();

        newNamedRange.setNameName("RangeTest");
        newNamedRange.setRefersToFormula(sheetName + "!$D$4:$E$8");

        //Creating another new Named Range
        Name newNamedRange2 = wb1.createName();

        newNamedRange2.setNameName("AnotherTest");
        newNamedRange2.setRefersToFormula(sheetName + "!$F$1:$G$6");

        wb1.getNameAt(0);

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        Name nm =wb2.getName("RangeTest");
        assertTrue("Name is "+nm.getNameName(),"RangeTest".equals(nm.getNameName()));
        assertTrue("Reference is "+nm.getRefersToFormula(),(wb2.getSheetName(0)+"!$D$4:$E$8").equals(nm.getRefersToFormula()));

        nm = wb2.getName("AnotherTest");
        assertTrue("Name is "+nm.getNameName(),"AnotherTest".equals(nm.getNameName()));
        assertTrue("Reference is "+nm.getRefersToFormula(),newNamedRange2.getRefersToFormula().equals(nm.getRefersToFormula()));
        
        wb2.close();
        wb1.close();
    }
    /**
     * Verifies correct functioning for "single cell named range" (aka "named cell")
     */
    @Test
    public final void testNamedCell_1() throws Exception {

        // setup for this testcase
        String sheetName = "Test Named Cell";
        String cellName = "named_cell";
        String cellValue = "TEST Value";
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet(sheetName);
        CreationHelper factory = wb.getCreationHelper();
        sheet.createRow(0).createCell(0).setCellValue(factory.createRichTextString(cellValue));

        // create named range for a single cell using areareference
        Name namedCell = wb.createName();
        namedCell.setNameName(cellName);
        String reference = "'" + sheetName + "'" + "!A1:A1";
        namedCell.setRefersToFormula(reference);

        // retrieve the newly created named range
        Name aNamedCell = wb.getName(cellName);
        assertNotNull(aNamedCell);

        // retrieve the cell at the named range and test its contents
        AreaReference aref = wb.getCreationHelper().createAreaReference(aNamedCell.getRefersToFormula());
        assertTrue("Should be exactly 1 cell in the named cell :'" +cellName+"'", aref.isSingleCell());

        CellReference cref = aref.getFirstCell();
        assertNotNull(cref);
        Sheet s = wb.getSheet(cref.getSheetName());
        assertNotNull(s);
        Row r = sheet.getRow(cref.getRow());
        Cell c = r.getCell(cref.getCol());
        String contents = c.getRichStringCellValue().getString();
        assertEquals("Contents of cell retrieved by its named reference", contents, cellValue);
        wb.close();
    }

    /**
     * Verifies correct functioning for "single cell named range" (aka "named cell")
     */
    @Test
    public final void testNamedCell_2() throws Exception {

        // setup for this testcase
        String sname = "TestSheet", cname = "TestName", cvalue = "TestVal";
        Workbook wb = _testDataProvider.createWorkbook();
        CreationHelper factory = wb.getCreationHelper();
        Sheet sheet = wb.createSheet(sname);
        sheet.createRow(0).createCell(0).setCellValue(factory.createRichTextString(cvalue));

        // create named range for a single cell using cellreference
        Name namedCell = wb.createName();
        namedCell.setNameName(cname);
        String reference = sname+"!A1";
        namedCell.setRefersToFormula(reference);

        // retrieve the newly created named range
        Name aNamedCell = wb.getName(cname);
        assertNotNull(aNamedCell);

        // retrieve the cell at the named range and test its contents
        CellReference cref = new CellReference(aNamedCell.getRefersToFormula());
        assertNotNull(cref);
        Sheet s = wb.getSheet(cref.getSheetName());
        assertNotNull(s);
        Row r = sheet.getRow(cref.getRow());
        Cell c = r.getCell(cref.getCol());
        String contents = c.getRichStringCellValue().getString();
        assertEquals("Contents of cell retrieved by its named reference", contents, cvalue);
        
        wb.close();
    }


    /**
     * Bugzilla attachment 23444 (from bug 46973) has a NAME record with the following encoding:
     * <pre>
     * 00000000 | 18 00 17 00 00 00 00 08 00 00 00 00 00 00 00 00 | ................
     * 00000010 | 00 00 00 55 50 53 53 74 61 74 65                | ...UPSState
     * </pre>
     *
     * This caused trouble for anything that requires {@link Name#getRefersToFormula()}
     * It is easy enough to re-create the the same data (by not setting the formula). Excel
     * seems to gracefully remove this uninitialized name record.  It would be nice if POI
     * could do the same, but that would involve adjusting subsequent name indexes across
     * all formulas. <p>
     *
     * For the moment, POI has been made to behave more sensibly with uninitialized name
     * records.
     */
    @Test
    public final void testUninitialisedNameGetRefersToFormula_bug46973() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Name n = wb.createName();
        n.setNameName("UPSState");
        String formula = n.getRefersToFormula();
        
        // bug 46973: fails here with IllegalArgumentException
        // ptgs must not be null

        assertNull(formula);
        // according to exact definition of isDeleted()
        assertFalse(n.isDeleted());
        wb.close();
    }

    @Test
    public final void testDeletedCell() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Name n = wb.createName();
        n.setNameName("MyName");
        // contrived example to expose bug:
        n.setRefersToFormula("if(A1,\"#REF!\", \"\")");

        assertFalse("Identified bug in recoginising formulas referring to deleted cells", n.isDeleted());
        
        wb.close();
    }

    @Test
    public final void testFunctionNames() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Name n = wb.createName();
        assertFalse(n.isFunctionName());

        n.setFunction(false);
        assertFalse(n.isFunctionName());

        n.setFunction(true);
        assertTrue(n.isFunctionName());

        n.setFunction(false);
        assertFalse(n.isFunctionName());
        
        wb.close();
    }

    @Test
    public final void testDefferedSetting() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Name n1 = wb.createName();
        assertNull(n1.getRefersToFormula());
        assertEquals("", n1.getNameName());

        Name n2 = wb.createName();
        assertNull(n2.getRefersToFormula());
        assertEquals("", n2.getNameName());

        n1.setNameName("sale_1");
        n1.setRefersToFormula("10");

        n2.setNameName("sale_2");
        n2.setRefersToFormula("20");

        try {
            n2.setNameName("sale_1");
            fail("Expected exception");
        } catch(Exception e){
            assertEquals("The workbook already contains this name: sale_1", e.getMessage());
        }
        
        wb.close();
    }

    @Test
    public void testBug56930() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        // x1 on sheet1 defines "x=1"
        wb.createSheet("sheet1");
        Name x1 = wb.createName();

        x1.setNameName("x");
        x1.setRefersToFormula("1");
        x1.setSheetIndex(wb.getSheetIndex("sheet1"));

        // x2 on sheet2 defines "x=2"
        wb.createSheet("sheet2");
        Name x2 = wb.createName();
        x2.setNameName("x");
        x2.setRefersToFormula("2");
        x2.setSheetIndex(wb.getSheetIndex("sheet2"));

        List<? extends Name> names = wb.getNames("x");
        assertEquals("Had: " + names, 2, names.size());
        assertEquals("1", names.get(0).getRefersToFormula());
        assertEquals("2", names.get(1).getRefersToFormula());

        assertEquals("1", wb.getName("x").getRefersToFormula());
        wb.removeName("x");
        assertEquals("2", wb.getName("x").getRefersToFormula());
        
        wb.close();
    }
    
    // bug 56781: name validation only checks for first character's validity and presence of spaces
    // bug 60246: validate name does not allow DOT in named ranges
    @Test
    public void testValid() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        
        Name name = wb.createName();
        for (String valid : Arrays.asList(
                "Hello",
                "number1",
                "_underscore",
                "underscore_",
                "p.e.r.o.i.d.s",
                "\\Backslash",
                "Backslash\\"
                )) {
            name.setNameName(valid);
        }
        
        wb.close();
    }
    
    @Test
    public void testInvalid() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        
        Name name = wb.createName();
        try {
            name.setNameName("");
            fail("expected exception: (blank)");
        } catch (final IllegalArgumentException e) {
            assertEquals("Name cannot be blank", e.getMessage());
        }
        
        for (String invalid : Arrays.asList(
                "1number",
                "Sheet1!A1",
                "Exclamation!",
                "Has Space",
                "Colon:",
                "A-Minus",
                "A+Plus",
                "Dollar$",
                ".periodAtBeginning",
                "R", //special shorthand
                "C", //special shorthand
                "A1", // A1-style cell reference
                "R1C1", // R1C1-style cell reference
                "NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters..."+
                "NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters..."+
                "NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters..."+
                "NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters..."+
                "NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters.NameThatIsLongerThan255Characters"
                )) {
            try {
                name.setNameName(invalid);
                fail("expected exception: " + invalid);
            } catch (final IllegalArgumentException e) {
                assertTrue(invalid,
                        e.getMessage().startsWith("Invalid name: '"+invalid+"'"));
            }
        }
        
    }
    
    // bug 60260: renaming a sheet with a named range referring to a unicode (non-ASCII) sheet name
    @Test
    public void renameSheetWithNamedRangeReferringToUnicodeSheetName() {
        Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet\u30FB1");
        
        Name name = wb.createName();
        name.setNameName("test_named_range");
        name.setRefersToFormula("'Sheet\u30FB201'!A1:A6");
        
        wb.setSheetName(0, "Sheet 1");
        IOUtils.closeQuietly(wb);
    }
}
