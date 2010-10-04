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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import junit.framework.AssertionFailedError;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.EmbeddedObjectRefSubRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.TabIdRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.DeletedArea3DPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.TempFile;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 *
 * <b>YK: If a bug can be tested in terms of common ss interfaces,
 *  define the test in the base class {@link BaseTestBugzillaIssues}</b>
 *
 * @author Avik Sengupta
 * @author Yegor Kozlov
 */
public final class TestBugs extends BaseTestBugzillaIssues {

    public TestBugs() {
        super(HSSFITestDataProvider.instance);
    }

    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFITestDataProvider.instance.openSampleWorkbook(sampleFileName);
    }

    private static HSSFWorkbook writeOutAndReadBack(HSSFWorkbook original) {
        return HSSFITestDataProvider.instance.writeOutAndReadBack(original);
    }

    private static void writeTestOutputFileForViewing(HSSFWorkbook wb, String simpleFileName) {
        if (true) { // set to false to output test files
            return;
        }
        System.setProperty("poi.keep.tmp.files", "true");
        File file;
        try {
            file = TempFile.createTempFile(simpleFileName + "#", ".xls");
            FileOutputStream out = new FileOutputStream(file);
            wb.write(out);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!file.exists()) {
            throw new RuntimeException("File was not written");
        }
        System.out.println("Open file '" + file.getAbsolutePath() + "' in Excel");
    }

    /** Test reading AND writing a complicated workbook
     *Test opening resulting sheet in excel*/
    public void test15228() {
        HSSFWorkbook wb = openSample("15228.xls");
        HSSFSheet s = wb.getSheetAt(0);
        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell(0);
        c.setCellValue(10);
        writeTestOutputFileForViewing(wb, "test15228");
    }

    public void test13796() {
        HSSFWorkbook wb = openSample("13796.xls");
        HSSFSheet s = wb.getSheetAt(0);
        HSSFRow r = s.createRow(0);
        HSSFCell c = r.createCell(0);
        c.setCellValue(10);
        writeOutAndReadBack(wb);
    }

     /** test hyperlinks
      * open resulting file in excel, and check that there is a link to Google
      */
    public void test15353() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("My sheet");

        HSSFRow row = sheet.createRow( 0 );
        HSSFCell cell = row.createCell( 0 );
        cell.setCellFormula("HYPERLINK(\"http://google.com\",\"Google\")");

        writeOutAndReadBack(wb);
    }

    /** test reading of a formula with a name and a cell ref in one
     **/
    public void test14460() {
        HSSFWorkbook wb = openSample("14460.xls");
        wb.getSheetAt(0);
    }

    public void test14330() {
        HSSFWorkbook wb = openSample("14330-1.xls");
        wb.getSheetAt(0);

        wb = openSample("14330-2.xls");
        wb.getSheetAt(0);
    }

    private static void setCellText(HSSFCell cell, String text) {
        cell.setCellValue(new HSSFRichTextString(text));
    }

    /** test rewriting a file with large number of unique strings
     *open resulting file in Excel to check results!*/
    public void test15375() {
        HSSFWorkbook wb = openSample("15375.xls");
        HSSFSheet sheet = wb.getSheetAt(0);

        HSSFRow row = sheet.getRow(5);
        HSSFCell cell = row.getCell(3);
        if (cell == null)
            cell = row.createCell(3);

        // Write test
        cell.setCellType(HSSFCell.CELL_TYPE_STRING);
        setCellText(cell, "a test");

        // change existing numeric cell value

        HSSFRow oRow = sheet.getRow(14);
        HSSFCell oCell = oRow.getCell(4);
        oCell.setCellValue(75);
        oCell = oRow.getCell(5);
        setCellText(oCell, "0.3");

        writeTestOutputFileForViewing(wb, "test15375");
    }
    /**
     * test writing a file with large number of unique strings,
     * open resulting file in Excel to check results!
     */
    public void test15375_2() {
        baseTest15375(6000);
    }

    /**Double byte strings*/
    public void test15556() {

        HSSFWorkbook wb = openSample("15556.xls");
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFRow row = sheet.getRow(45);
        assertNotNull("Read row fine!" , row);
    }
    /**Double byte strings */
    public void test22742() {
        openSample("22742.xls");
    }
    /**Double byte strings */
    public void test12561_1() {
        openSample("12561-1.xls");
    }
    /** Double byte strings */
    public void test12561_2() {
        openSample("12561-2.xls");
    }
    /** Double byte strings
     File supplied by jubeson*/
    public void test12843_1() {
        openSample("12843-1.xls");
    }

    /** Double byte strings
     File supplied by Paul Chung*/
    public void test12843_2() {
        openSample("12843-2.xls");
    }

    /** Reference to Name*/
    public void test13224() {
        openSample("13224.xls");
    }

    /** Illegal argument exception - cannot store duplicate value in Map*/
    public void test19599() {
        openSample("19599-1.xls");
        openSample("19599-2.xls");
    }

    public void test24215() {
        HSSFWorkbook wb = openSample("24215.xls");

        for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets();sheetIndex++) {
            HSSFSheet sheet = wb.getSheetAt(sheetIndex);
            int rows = sheet.getLastRowNum();

            for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                HSSFRow row = sheet.getRow(rowIndex);
                int cells = row.getLastCellNum();

                for (int cellIndex = 0; cellIndex < cells; cellIndex++) {
                    row.getCell(cellIndex);
                }
            }
        }
    }

    /**Tests read and write of Unicode strings in formula results
     * bug and testcase submitted by Sompop Kumnoonsate
     * The file contains THAI unicode characters.
     */
    public void testUnicodeStringFormulaRead() {

        HSSFWorkbook w = openSample("25695.xls");

        HSSFCell a1 = w.getSheetAt(0).getRow(0).getCell(0);
        HSSFCell a2 = w.getSheetAt(0).getRow(0).getCell(1);
        HSSFCell b1 = w.getSheetAt(0).getRow(1).getCell(0);
        HSSFCell b2 = w.getSheetAt(0).getRow(1).getCell(1);
        HSSFCell c1 = w.getSheetAt(0).getRow(2).getCell(0);
        HSSFCell c2 = w.getSheetAt(0).getRow(2).getCell(1);
        HSSFCell d1 = w.getSheetAt(0).getRow(3).getCell(0);
        HSSFCell d2 = w.getSheetAt(0).getRow(3).getCell(1);

        if (false) {
            // THAI code page
            System.out.println("a1="+unicodeString(a1));
            System.out.println("a2="+unicodeString(a2));
            // US code page
            System.out.println("b1="+unicodeString(b1));
            System.out.println("b2="+unicodeString(b2));
            // THAI+US
            System.out.println("c1="+unicodeString(c1));
            System.out.println("c2="+unicodeString(c2));
            // US+THAI
            System.out.println("d1="+unicodeString(d1));
            System.out.println("d2="+unicodeString(d2));
        }
        confirmSameCellText(a1, a2);
        confirmSameCellText(b1, b2);
        confirmSameCellText(c1, c2);
        confirmSameCellText(d1, d2);

        HSSFWorkbook rw = writeOutAndReadBack(w);

        HSSFCell ra1 = rw.getSheetAt(0).getRow(0).getCell(0);
        HSSFCell ra2 = rw.getSheetAt(0).getRow(0).getCell(1);
        HSSFCell rb1 = rw.getSheetAt(0).getRow(1).getCell(0);
        HSSFCell rb2 = rw.getSheetAt(0).getRow(1).getCell(1);
        HSSFCell rc1 = rw.getSheetAt(0).getRow(2).getCell(0);
        HSSFCell rc2 = rw.getSheetAt(0).getRow(2).getCell(1);
        HSSFCell rd1 = rw.getSheetAt(0).getRow(3).getCell(0);
        HSSFCell rd2 = rw.getSheetAt(0).getRow(3).getCell(1);

        confirmSameCellText(a1, ra1);
        confirmSameCellText(b1, rb1);
        confirmSameCellText(c1, rc1);
        confirmSameCellText(d1, rd1);

        confirmSameCellText(a1, ra2);
        confirmSameCellText(b1, rb2);
        confirmSameCellText(c1, rc2);
        confirmSameCellText(d1, rd2);
    }

    private static void confirmSameCellText(HSSFCell a, HSSFCell b) {
        assertEquals(a.getRichStringCellValue().getString(), b.getRichStringCellValue().getString());
    }
    private static String unicodeString(HSSFCell cell) {
        String ss = cell.getRichStringCellValue().getString();
        char s[] = ss.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int x=0;x<s.length;x++) {
            sb.append("\\u").append(Integer.toHexString(s[x]));
        }
        return sb.toString();
    }

    /** Error in opening wb*/
    public void test32822() {
        openSample("32822.xls");
    }
    /**fail to read wb with chart */
    public void test15573() {
        openSample("15573.xls");
    }

    /**names and macros */
    public void test27852() {
        HSSFWorkbook wb = openSample("27852.xls");

        for(int i = 0 ; i < wb.getNumberOfNames(); i++){
          HSSFName name = wb.getNameAt(i);
          name.getNameName();
          if (name.isFunctionName()) {
              continue;
          }
          name.getRefersToFormula();
        }
    }

    public void test33082() {
        openSample("33082.xls");
    }

    public void test34775() {
        try {
            openSample("34775.xls");
        } catch (NullPointerException e) {
            throw new AssertionFailedError("identified bug 34775");
        }
    }

    /** Error when reading then writing ArrayValues in NameRecord's*/
    public void test37630() {
        HSSFWorkbook wb = openSample("37630.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 25183: org.apache.poi.hssf.usermodel.HSSFSheet.setPropertiesFromSheet
     */
    public void test25183() {
        HSSFWorkbook wb = openSample("25183.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 26100: 128-character message in IF statement cell causes HSSFWorkbook open failure
     */
    public void test26100() {
        HSSFWorkbook wb = openSample("26100.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 27933: Unable to use a template (xls) file containing a wmf graphic
     */
    public void test27933() {
        HSSFWorkbook wb = openSample("27933.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 29206:      NPE on HSSFSheet.getRow for blank rows
     */
    public void test29206() {
        //the first check with blank workbook
        HSSFWorkbook wb = openSample("Simple.xls");
        HSSFSheet sheet = wb.createSheet();
        for(int i = 1; i < 400; i++) {
            HSSFRow row = sheet.getRow(i);
            if(row != null) {
                row.getCell(0);
            }
        }
    }

    /**
     * Bug 29675: POI 2.5 final corrupts output when starting workbook has a graphic
     */
    public void test29675() {
        HSSFWorkbook wb = openSample("29675.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 29942: Importing Excel files that have been created by Open Office on Linux
     */
    public void test29942() {
        HSSFWorkbook wb = openSample("29942.xls");

        HSSFSheet sheet = wb.getSheetAt(0);
        int count = 0;
        for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
            HSSFRow row =  sheet.getRow(i);
            if (row != null) {
                HSSFCell cell = row .getCell(0);
                assertEquals(HSSFCell.CELL_TYPE_STRING, cell.getCellType());
                count++;
            }
        }
        assertEquals(85, count); //should read 85 rows

        writeOutAndReadBack(wb);
    }

    /**
     * Bug 29982: Unable to read spreadsheet when dropdown list cell is selected -
     *  Unable to construct record instance
     */
    public void test29982() {
        HSSFWorkbook wb = openSample("29982.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 30540: HSSFSheet.setRowBreak throws NullPointerException
     */
    public void test30540() {
        HSSFWorkbook wb = openSample("30540.xls");

        HSSFSheet s = wb.getSheetAt(0);
        s.setRowBreak(1);
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 31749: {Need help urgently}[This is critical] workbook.write() corrupts the file......?
     */
    public void test31749() {
        HSSFWorkbook wb = openSample("31749.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 31979: {urgent help needed .....}poi library does not support form objects properly.
     */
    public void test31979() {
        HSSFWorkbook wb = openSample("31979.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 35564: HSSFCell.java: NullPtrExc in isGridsPrinted() and getProtect()
     *  when HSSFWorkbook is created from file
     */
    public void test35564() {
        HSSFWorkbook wb = openSample("35564.xls");

        HSSFSheet sheet = wb.getSheetAt( 0 );
        assertEquals(false, sheet.isGridsPrinted());
        assertEquals(false, sheet.getProtect());

        writeOutAndReadBack(wb);
    }

    /**
     * Bug 35565: HSSFCell.java: NullPtrExc in getColumnBreaks() when HSSFWorkbook is created from file
     */
    public void test35565() {
        HSSFWorkbook wb = openSample("35565.xls");

        HSSFSheet sheet = wb.getSheetAt( 0 );
        assertNotNull(sheet);
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 37376: Cannot open the saved Excel file if checkbox controls exceed certain limit
     */
    public void test37376() {
        HSSFWorkbook wb = openSample("37376.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 40285:      CellIterator Skips First Column
     */
    public void test40285() {
        HSSFWorkbook wb = openSample("40285.xls");

        HSSFSheet sheet = wb.getSheetAt( 0 );
        int rownum = 0;
        for (Iterator it = sheet.rowIterator(); it.hasNext(); rownum++) {
            HSSFRow row = (HSSFRow)it.next();
            assertEquals(rownum, row.getRowNum());
            int cellNum = 0;
            for (Iterator it2 = row.cellIterator(); it2.hasNext(); cellNum++) {
                HSSFCell cell = (HSSFCell)it2.next();
                assertEquals(cellNum, cell.getColumnIndex());
            }
        }
    }

    /**
     * Test bug 38266: NPE when adding a row break
     *
     * User's diagnosis:
     * 1. Manually (i.e., not using POI) create an Excel Workbook, making sure it
     * contains a sheet that doesn't have any row breaks
     * 2. Using POI, create a new HSSFWorkbook from the template in step #1
     * 3. Try adding a row break (via sheet.setRowBreak()) to the sheet mentioned in step #1
     * 4. Get a NullPointerException
     */
    public void test38266() {
        String[] files = {"Simple.xls", "SimpleMultiCell.xls", "duprich1.xls"};
        for (int i = 0; i < files.length; i++) {
            HSSFWorkbook wb = openSample(files[i]);

            HSSFSheet sheet = wb.getSheetAt( 0 );
            int[] breaks = sheet.getRowBreaks();
            assertEquals(0, breaks.length);

            //add 3 row breaks
            for (int j = 1; j <= 3; j++) {
                sheet.setRowBreak(j*20);
            }
        }
    }

    public void test40738() {
        HSSFWorkbook wb = openSample("SimpleWithAutofilter.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 44200: Sheet not cloneable when Note added to excel cell
     */
    public void test44200() {
        HSSFWorkbook wb = openSample("44200.xls");

        wb.cloneSheet(0);
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 44201: Sheet not cloneable when validation added to excel cell
     */
    public void test44201() {
        HSSFWorkbook wb = openSample("44201.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 37684  : Unhandled Continue Record Error
     */
    public void test37684 () {
        HSSFWorkbook wb = openSample("37684-1.xls");
        writeOutAndReadBack(wb);


        wb = openSample("37684-2.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 41139: Constructing HSSFWorkbook is failed,threw threw ArrayIndexOutOfBoundsException for creating UnknownRecord
     */
    public void test41139() {
        HSSFWorkbook wb = openSample("41139.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 41546: Constructing HSSFWorkbook is failed,
     *  Unknown Ptg in Formula: 0x1a (26)
     */
    public void test41546() {
        HSSFWorkbook wb = openSample("41546.xls");
        assertEquals(1, wb.getNumberOfSheets());
        wb = writeOutAndReadBack(wb);
        assertEquals(1, wb.getNumberOfSheets());
    }

    /**
     * Bug 42564: Some files from Access were giving a RecordFormatException
     *  when reading the BOFRecord
     */
    public void test42564() {
        HSSFWorkbook wb = openSample("ex42564-21435.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 42564: Some files from Access also have issues
     *  with the NameRecord, once you get past the BOFRecord
     *  issue.
     */
    public void test42564Alt() {
        HSSFWorkbook wb = openSample("ex42564-21503.xls");
        writeOutAndReadBack(wb);
    }

    /**
     * Bug 42618: RecordFormatException reading a file containing
     *     =CHOOSE(2,A2,A3,A4)
     */
    public void test42618() {
        HSSFWorkbook wb = openSample("SimpleWithChoose.xls");
        wb = writeOutAndReadBack(wb);
        // Check we detect the string properly too
        HSSFSheet s = wb.getSheetAt(0);

        // Textual value
        HSSFRow r1 = s.getRow(0);
        HSSFCell c1 = r1.getCell(1);
        assertEquals("=CHOOSE(2,A2,A3,A4)", c1.getRichStringCellValue().toString());

        // Formula Value
        HSSFRow r2 = s.getRow(1);
        HSSFCell c2 = r2.getCell(1);
        assertEquals(25, (int)c2.getNumericCellValue());

        try {
            assertEquals("CHOOSE(2,A2,A3,A4)", c2.getCellFormula());
        } catch (IllegalStateException e) {
            if (e.getMessage().startsWith("Too few arguments")
                    && e.getMessage().indexOf("ConcatPtg") > 0) {
                throw new AssertionFailedError("identified bug 44306");
            }
        }
    }

    /**
     * Something up with the FileSharingRecord
     */
    public void test43251() {

        // Used to blow up with an IllegalArgumentException
        //  when creating a FileSharingRecord
        HSSFWorkbook wb;
        try {
            wb = openSample("43251.xls");
        } catch (IllegalArgumentException e) {
            throw new AssertionFailedError("identified bug 43251");
        }

        assertEquals(1, wb.getNumberOfSheets());
    }

    /**
     * Crystal reports generates files with short
     *  StyleRecords, which is against the spec
     */
    public void test44471() {

        // Used to blow up with an ArrayIndexOutOfBounds
        //  when creating a StyleRecord
        HSSFWorkbook wb;
        try {
            wb = openSample("OddStyleRecord.xls");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new AssertionFailedError("Identified bug 44471");
        }

        assertEquals(1, wb.getNumberOfSheets());
    }

    /**
     * Files with "read only recommended" were giving
     *  grief on the FileSharingRecord
     */
    public void test44536() {

        // Used to blow up with an IllegalArgumentException
        //  when creating a FileSharingRecord
        HSSFWorkbook wb = openSample("ReadOnlyRecommended.xls");

        // Check read only advised
        assertEquals(3, wb.getNumberOfSheets());
        assertTrue(wb.isWriteProtected());

        // But also check that another wb isn't
        wb = openSample("SimpleWithChoose.xls");
        assertFalse(wb.isWriteProtected());
    }

    /**
     * Some files were having problems with the DVRecord,
     *  probably due to dropdowns
     */
    public void test44593() {

        // Used to blow up with an IllegalArgumentException
        //  when creating a DVRecord
        // Now won't, but no idea if this means we have
        //  rubbish in the DVRecord or not...
        HSSFWorkbook wb;
        try {
            wb = openSample("44593.xls");
        } catch (IllegalArgumentException e) {
            throw new AssertionFailedError("Identified bug 44593");
        }

        assertEquals(2, wb.getNumberOfSheets());
    }

    /**
     * Used to give problems due to trying to read a zero
     *  length string, but that's now properly handled
     */
    public void test44643() {

        // Used to blow up with an IllegalArgumentException
        HSSFWorkbook wb;
        try {
            wb = openSample("44643.xls");
        } catch (IllegalArgumentException e) {
            throw new AssertionFailedError("identified bug 44643");
        }

        assertEquals(1, wb.getNumberOfSheets());
    }

    /**
     * User reported the wrong number of rows from the
     *  iterator, but we can't replicate that
     */
    public void test44693() {

        HSSFWorkbook wb = openSample("44693.xls");
        HSSFSheet s = wb.getSheetAt(0);

        // Rows are 1 to 713
        assertEquals(0, s.getFirstRowNum());
        assertEquals(712, s.getLastRowNum());
        assertEquals(713, s.getPhysicalNumberOfRows());

        // Now check the iterator
        int rowsSeen = 0;
        for(Iterator i = s.rowIterator(); i.hasNext(); ) {
            HSSFRow r = (HSSFRow)i.next();
            assertNotNull(r);
            rowsSeen++;
        }
        assertEquals(713, rowsSeen);
    }

    /**
     * Bug 28774: Excel will crash when opening xls-files with images.
     */
    public void test28774() {
        HSSFWorkbook wb = openSample("28774.xls");
        assertTrue("no errors reading sample xls", true);
        writeOutAndReadBack(wb);
        assertTrue("no errors writing sample xls", true);
    }

    /**
     * Had a problem apparently, not sure what as it
     *  works just fine...
     */
    public void test44891() {
        HSSFWorkbook wb = openSample("44891.xls");
        assertTrue("no errors reading sample xls", true);
        writeOutAndReadBack(wb);
        assertTrue("no errors writing sample xls", true);
    }

    /**
     * Bug 44235: Ms Excel can't open save as excel file
     *
     * Works fine with poi-3.1-beta1.
     */
    public void test44235() {
        HSSFWorkbook wb = openSample("44235.xls");
        assertTrue("no errors reading sample xls", true);
        writeOutAndReadBack(wb);
        assertTrue("no errors writing sample xls", true);
    }

    public void test36947() {
        HSSFWorkbook wb = openSample("36947.xls");
        assertTrue("no errors reading sample xls", true);
        writeOutAndReadBack(wb);
        assertTrue("no errors writing sample xls", true);
    }

    public void test39634() {
        HSSFWorkbook wb = openSample("39634.xls");
        assertTrue("no errors reading sample xls", true);
        writeOutAndReadBack(wb);
        assertTrue("no errors writing sample xls", true);
    }

    /**
     * Problems with extracting check boxes from
     *  HSSFObjectData
     * @throws Exception
     */
    public void test44840() {
        HSSFWorkbook wb = openSample("WithCheckBoxes.xls");

        // Take a look at the embedded objects
        List objects = wb.getAllEmbeddedObjects();
        assertEquals(1, objects.size());

        HSSFObjectData obj = (HSSFObjectData)objects.get(0);
        assertNotNull(obj);

        // Peek inside the underlying record
        EmbeddedObjectRefSubRecord rec = obj.findObjectRecord();
        assertNotNull(rec);

//        assertEquals(32, rec.field_1_stream_id_offset);
        assertEquals(0, rec.getStreamId().intValue()); // WRONG!
        assertEquals("Forms.CheckBox.1", rec.getOLEClassName());
        assertEquals(12, rec.getObjectData().length);

        // Doesn't have a directory
        assertFalse(obj.hasDirectoryEntry());
        assertNotNull(obj.getObjectData());
        assertEquals(12, obj.getObjectData().length);
        assertEquals("Forms.CheckBox.1", obj.getOLE2ClassName());

        try {
            obj.getDirectory();
            fail();
        } catch(FileNotFoundException e) {
            // expected during successful test
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Test that we can delete sheets without
     *  breaking the build in named ranges
     *  used for printing stuff.
     */
    public void test30978() {
        HSSFWorkbook wb = openSample("30978-alt.xls");
        assertEquals(1, wb.getNumberOfNames());
        assertEquals(3, wb.getNumberOfSheets());

        // Check all names fit within range, and use
        //  DeletedArea3DPtg
        InternalWorkbook w = wb.getWorkbook();
        for(int i=0; i<w.getNumNames(); i++) {
            NameRecord r = w.getNameRecord(i);
            assertTrue(r.getSheetNumber() <= wb.getNumberOfSheets());

            Ptg[] nd = r.getNameDefinition();
            assertEquals(1, nd.length);
            assertTrue(nd[0] instanceof DeletedArea3DPtg);
        }


        // Delete the 2nd sheet
        wb.removeSheetAt(1);


        // Re-check
        assertEquals(1, wb.getNumberOfNames());
        assertEquals(2, wb.getNumberOfSheets());

        for(int i=0; i<w.getNumNames(); i++) {
            NameRecord r = w.getNameRecord(i);
            assertTrue(r.getSheetNumber() <= wb.getNumberOfSheets());

            Ptg[] nd = r.getNameDefinition();
            assertEquals(1, nd.length);
            assertTrue(nd[0] instanceof DeletedArea3DPtg);
        }


        // Save and re-load
        wb = writeOutAndReadBack(wb);
        w = wb.getWorkbook();

        assertEquals(1, wb.getNumberOfNames());
        assertEquals(2, wb.getNumberOfSheets());

        for(int i=0; i<w.getNumNames(); i++) {
            NameRecord r = w.getNameRecord(i);
            assertTrue(r.getSheetNumber() <= wb.getNumberOfSheets());

            Ptg[] nd = r.getNameDefinition();
            assertEquals(1, nd.length);
            assertTrue(nd[0] instanceof DeletedArea3DPtg);
        }
    }

    /**
     * Test that fonts get added properly
     */
    public void test45338() {
        HSSFWorkbook wb = new HSSFWorkbook();
        assertEquals(4, wb.getNumberOfFonts());

        HSSFSheet s = wb.createSheet();
        s.createRow(0);
        s.createRow(1);
        s.getRow(0).createCell(0);
        s.getRow(1).createCell(0);

        assertEquals(4, wb.getNumberOfFonts());

        HSSFFont f1 = wb.getFontAt((short)0);
        assertEquals(400, f1.getBoldweight());

        // Check that asking for the same font
        //  multiple times gives you the same thing.
        // Otherwise, our tests wouldn't work!
        assertEquals(
                wb.getFontAt((short)0),
                wb.getFontAt((short)0)
        );
        assertEquals(
                wb.getFontAt((short)2),
                wb.getFontAt((short)2)
        );
        assertTrue(
                wb.getFontAt((short)0)
                !=
                wb.getFontAt((short)2)
        );

        // Look for a new font we have
        //  yet to add
        assertNull(
            wb.findFont(
                (short)11, (short)123, (short)22,
                "Thingy", false, true, (short)2, (byte)2
            )
        );

        HSSFFont nf = wb.createFont();
        assertEquals(5, wb.getNumberOfFonts());

        assertEquals(5, nf.getIndex());
        assertEquals(nf, wb.getFontAt((short)5));

        nf.setBoldweight((short)11);
        nf.setColor((short)123);
        nf.setFontHeight((short)22);
        nf.setFontName("Thingy");
        nf.setItalic(false);
        nf.setStrikeout(true);
        nf.setTypeOffset((short)2);
        nf.setUnderline((byte)2);

        assertEquals(5, wb.getNumberOfFonts());
        assertEquals(nf, wb.getFontAt((short)5));

        // Find it now
        assertNotNull(
            wb.findFont(
                (short)11, (short)123, (short)22,
                "Thingy", false, true, (short)2, (byte)2
            )
        );
        assertEquals(
            5,
            wb.findFont(
                   (short)11, (short)123, (short)22,
                   "Thingy", false, true, (short)2, (byte)2
               ).getIndex()
        );
        assertEquals(nf,
               wb.findFont(
                   (short)11, (short)123, (short)22,
                   "Thingy", false, true, (short)2, (byte)2
               )
        );
    }

    /**
     * From the mailing list - ensure we can handle a formula
     *  containing a zip code, eg ="70164"
     */
    public void testZipCodeFormulas() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();
        s.createRow(0);
        HSSFCell c1 = s.getRow(0).createCell(0);
        HSSFCell c2 = s.getRow(0).createCell(1);
        HSSFCell c3 = s.getRow(0).createCell(2);

        // As number and string
        c1.setCellFormula("70164");
        c2.setCellFormula("\"70164\"");
        c3.setCellFormula("\"90210\"");

        // Check the formulas
        assertEquals("70164", c1.getCellFormula());
        assertEquals("\"70164\"", c2.getCellFormula());

        // And check the values - blank
        confirmCachedValue(0.0, c1);
        confirmCachedValue(0.0, c2);
        confirmCachedValue(0.0, c3);

        // Try changing the cached value on one of the string
        //  formula cells, so we can see it updates properly
        c3.setCellValue(new HSSFRichTextString("test"));
        confirmCachedValue("test", c3);
        try {
            c3.getNumericCellValue();
            throw new AssertionFailedError("exception should have been thrown");
        } catch (IllegalStateException e) {
            assertEquals("Cannot get a numeric value from a text formula cell", e.getMessage());
        }


        // Now evaluate, they should all be changed
        HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb);
        eval.evaluateFormulaCell(c1);
        eval.evaluateFormulaCell(c2);
        eval.evaluateFormulaCell(c3);

        // Check that the cells now contain
        //  the correct values
        confirmCachedValue(70164.0, c1);
        confirmCachedValue("70164", c2);
        confirmCachedValue("90210", c3);


        // Write and read
        HSSFWorkbook nwb = writeOutAndReadBack(wb);
        HSSFSheet ns = nwb.getSheetAt(0);
        HSSFCell nc1 = ns.getRow(0).getCell(0);
        HSSFCell nc2 = ns.getRow(0).getCell(1);
        HSSFCell nc3 = ns.getRow(0).getCell(2);

        // Re-check
        confirmCachedValue(70164.0, nc1);
        confirmCachedValue("70164", nc2);
        confirmCachedValue("90210", nc3);

        CellValueRecordInterface[] cvrs = ns.getSheet().getValueRecords();
        for (int i = 0; i < cvrs.length; i++) {
            CellValueRecordInterface cvr = cvrs[i];
            if(cvr instanceof FormulaRecordAggregate) {
                FormulaRecordAggregate fr = (FormulaRecordAggregate)cvr;

                if(i == 0) {
                    assertEquals(70164.0, fr.getFormulaRecord().getValue(), 0.0001);
                    assertNull(fr.getStringRecord());
                } else if (i == 1) {
                    assertEquals(0.0, fr.getFormulaRecord().getValue(), 0.0001);
                    assertNotNull(fr.getStringRecord());
                    assertEquals("70164", fr.getStringRecord().getString());
                } else {
                    assertEquals(0.0, fr.getFormulaRecord().getValue(), 0.0001);
                    assertNotNull(fr.getStringRecord());
                    assertEquals("90210", fr.getStringRecord().getString());
                }
            }
        }
        assertEquals(3, cvrs.length);
    }

    private static void confirmCachedValue(double expectedValue, HSSFCell cell) {
        assertEquals(HSSFCell.CELL_TYPE_FORMULA, cell.getCellType());
        assertEquals(HSSFCell.CELL_TYPE_NUMERIC, cell.getCachedFormulaResultType());
        assertEquals(expectedValue, cell.getNumericCellValue(), 0.0);
    }
    private static void confirmCachedValue(String expectedValue, HSSFCell cell) {
        assertEquals(HSSFCell.CELL_TYPE_FORMULA, cell.getCellType());
        assertEquals(HSSFCell.CELL_TYPE_STRING, cell.getCachedFormulaResultType());
        assertEquals(expectedValue, cell.getRichStringCellValue().getString());
    }

    /**
     * Problem with "Vector Rows", eg a whole
     *  column which is set to the result of
     *  {=sin(B1:B9)}(9,1), so that each cell is
     *  shown to have the contents
     *  {=sin(B1:B9){9,1)[rownum][0]
     * In this sample file, the vector column
     *  is C, and the data column is B.
     *
     * For now, blows up with an exception from ExtPtg
     *  Expected ExpPtg to be converted from Shared to Non-Shared...
     */
    public void DISABLEDtest43623() {
        HSSFWorkbook wb = openSample("43623.xls");
        assertEquals(1, wb.getNumberOfSheets());

        HSSFSheet s1 = wb.getSheetAt(0);

        HSSFCell c1 = s1.getRow(0).getCell(2);
        HSSFCell c2 = s1.getRow(1).getCell(2);
        HSSFCell c3 = s1.getRow(2).getCell(2);

        // These formula contents are a guess...
        assertEquals("{=sin(B1:B9){9,1)[0][0]", c1.getCellFormula());
        assertEquals("{=sin(B1:B9){9,1)[1][0]", c2.getCellFormula());
        assertEquals("{=sin(B1:B9){9,1)[2][0]", c3.getCellFormula());

        // Save and re-open, ensure it still works
        HSSFWorkbook nwb = writeOutAndReadBack(wb);
        HSSFSheet ns1 = nwb.getSheetAt(0);
        HSSFCell nc1 = ns1.getRow(0).getCell(2);
        HSSFCell nc2 = ns1.getRow(1).getCell(2);
        HSSFCell nc3 = ns1.getRow(2).getCell(2);

        assertEquals("{=sin(B1:B9){9,1)[0][0]", nc1.getCellFormula());
        assertEquals("{=sin(B1:B9){9,1)[1][0]", nc2.getCellFormula());
        assertEquals("{=sin(B1:B9){9,1)[2][0]", nc3.getCellFormula());
    }

    /**
     * People are all getting confused about the last
     *  row and cell number
     */
    public void test30635() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();

        // No rows, everything is 0
        assertEquals(0, s.getFirstRowNum());
        assertEquals(0, s.getLastRowNum());
        assertEquals(0, s.getPhysicalNumberOfRows());

        // One row, most things are 0, physical is 1
        s.createRow(0);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(0, s.getLastRowNum());
        assertEquals(1, s.getPhysicalNumberOfRows());

        // And another, things change
        s.createRow(4);
        assertEquals(0, s.getFirstRowNum());
        assertEquals(4, s.getLastRowNum());
        assertEquals(2, s.getPhysicalNumberOfRows());


        // Now start on cells
        HSSFRow r = s.getRow(0);
        assertEquals(-1, r.getFirstCellNum());
        assertEquals(-1, r.getLastCellNum());
        assertEquals(0, r.getPhysicalNumberOfCells());

        // Add a cell, things move off -1
        r.createCell(0);
        assertEquals(0, r.getFirstCellNum());
        assertEquals(1, r.getLastCellNum()); // last cell # + 1
        assertEquals(1, r.getPhysicalNumberOfCells());

        r.createCell(1);
        assertEquals(0, r.getFirstCellNum());
        assertEquals(2, r.getLastCellNum()); // last cell # + 1
        assertEquals(2, r.getPhysicalNumberOfCells());

        r.createCell(4);
        assertEquals(0, r.getFirstCellNum());
        assertEquals(5, r.getLastCellNum()); // last cell # + 1
        assertEquals(3, r.getPhysicalNumberOfCells());
    }

    /**
     * Data Tables - ptg 0x2
     */
    public void test44958() {
        HSSFWorkbook wb = openSample("44958.xls");
        HSSFSheet s;
        HSSFRow r;
        HSSFCell c;

        // Check the contents of the formulas

        // E4 to G9 of sheet 4 make up the table
        s = wb.getSheet("OneVariable Table Completed");
        r = s.getRow(3);
        c = r.getCell(4);
        assertEquals(HSSFCell.CELL_TYPE_FORMULA, c.getCellType());

        // TODO - check the formula once tables and
        //  arrays are properly supported


        // E4 to H9 of sheet 5 make up the table
        s = wb.getSheet("TwoVariable Table Example");
        r = s.getRow(3);
        c = r.getCell(4);
        assertEquals(HSSFCell.CELL_TYPE_FORMULA, c.getCellType());

        // TODO - check the formula once tables and
        //  arrays are properly supported
    }

    /**
     * 45322: HSSFSheet.autoSizeColumn fails when style.getDataFormat() returns -1
     */
    public void test45322() {
        HSSFWorkbook wb = openSample("44958.xls");
        HSSFSheet sh = wb.getSheetAt(0);
        for(short i=0; i < 30; i++) sh.autoSizeColumn(i);
     }

    /**
     * We used to add too many UncalcRecords to sheets
     *  with diagrams on. Don't any more
     */
    public void test45414() {
        HSSFWorkbook wb = openSample("WithThreeCharts.xls");
        wb.getSheetAt(0).setForceFormulaRecalculation(true);
        wb.getSheetAt(1).setForceFormulaRecalculation(false);
        wb.getSheetAt(2).setForceFormulaRecalculation(true);

        // Write out and back in again
        // This used to break
        HSSFWorkbook nwb = writeOutAndReadBack(wb);

        // Check now set as it should be
        assertTrue(nwb.getSheetAt(0).getForceFormulaRecalculation());
        assertFalse(nwb.getSheetAt(1).getForceFormulaRecalculation());
        assertTrue(nwb.getSheetAt(2).getForceFormulaRecalculation());
    }

    /**
     * Very hidden sheets not displaying as such
     */
    public void test45761() {
        HSSFWorkbook wb = openSample("45761.xls");
        assertEquals(3, wb.getNumberOfSheets());

        assertFalse(wb.isSheetHidden(0));
        assertFalse(wb.isSheetVeryHidden(0));
        assertTrue(wb.isSheetHidden(1));
        assertFalse(wb.isSheetVeryHidden(1));
        assertFalse(wb.isSheetHidden(2));
        assertTrue(wb.isSheetVeryHidden(2));

        // Change 0 to be very hidden, and re-load
        wb.setSheetHidden(0, HSSFWorkbook.SHEET_STATE_VERY_HIDDEN);

        HSSFWorkbook nwb = writeOutAndReadBack(wb);

        assertFalse(nwb.isSheetHidden(0));
        assertTrue(nwb.isSheetVeryHidden(0));
        assertTrue(nwb.isSheetHidden(1));
        assertFalse(nwb.isSheetVeryHidden(1));
        assertFalse(nwb.isSheetHidden(2));
        assertTrue(nwb.isSheetVeryHidden(2));
    }

    /**
     * The resolution for bug 45777 assumed that the maximum text length in a header / footer
     * record was 256 bytes.  This assumption appears to be wrong.  Since the fix for bug 47244,
     * POI now supports header / footer text lengths beyond 256 bytes.
     */
    public void test45777() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet s = wb.createSheet();

        char[] cc248 = new char[248];
        Arrays.fill(cc248, 'x');
        String s248 = new String(cc248);

        String s249 = s248 + "1";
        String s250 = s248 + "12";
        String s251 = s248 + "123";
        assertEquals(248, s248.length());
        assertEquals(249, s249.length());
        assertEquals(250, s250.length());
        assertEquals(251, s251.length());


        // Try on headers
        s.getHeader().setCenter(s248);
        assertEquals(254, s.getHeader().getRawText().length());
        writeOutAndReadBack(wb);

        s.getHeader().setCenter(s251);
        assertEquals(257, s.getHeader().getRawText().length());
        writeOutAndReadBack(wb);

        try {
            s.getHeader().setCenter(s250); // 256 bytes required
        } catch(IllegalArgumentException e) {
            throw new AssertionFailedError("Identified bug 47244b - header can be more than 256 bytes");
        }

        try {
            s.getHeader().setCenter(s251); // 257 bytes required
        } catch(IllegalArgumentException e) {
            throw new AssertionFailedError("Identified bug 47244b - header can be more than 256 bytes");
        }

        // Now try on footers
        s.getFooter().setCenter(s248);
        assertEquals(254, s.getFooter().getRawText().length());
        writeOutAndReadBack(wb);

        s.getFooter().setCenter(s251);
        assertEquals(257, s.getFooter().getRawText().length());
        writeOutAndReadBack(wb);

        try {
            s.getFooter().setCenter(s250); // 256 bytes required
        } catch(IllegalArgumentException e) {
            throw new AssertionFailedError("Identified bug 47244b - footer can be more than 256 bytes");
        }

        try {
            s.getFooter().setCenter(s251); // 257 bytes required
        } catch(IllegalArgumentException e) {
            throw new AssertionFailedError("Identified bug 47244b - footer can be more than 256 bytes");
        }
    }

    /**
     * Charts with long titles
     */
    public void test45784() {
        // This used to break
        HSSFWorkbook wb = openSample("45784.xls");
        assertEquals(1, wb.getNumberOfSheets());
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
    }

   /**
     * Cell background colours
     */
    public void test45492() {
        HSSFWorkbook wb = openSample("45492.xls");
        HSSFSheet s = wb.getSheetAt(0);
        HSSFRow r = s.getRow(0);
        HSSFPalette p = wb.getCustomPalette();

        HSSFCell auto = r.getCell(0);
        HSSFCell grey = r.getCell(1);
        HSSFCell red = r.getCell(2);
        HSSFCell blue = r.getCell(3);
        HSSFCell green = r.getCell(4);

        assertEquals(64, auto.getCellStyle().getFillForegroundColor());
        assertEquals(64, auto.getCellStyle().getFillBackgroundColor());
        assertEquals("0:0:0", p.getColor(64).getHexString());

        assertEquals(22, grey.getCellStyle().getFillForegroundColor());
        assertEquals(64, grey.getCellStyle().getFillBackgroundColor());
        assertEquals("C0C0:C0C0:C0C0", p.getColor(22).getHexString());

        assertEquals(10, red.getCellStyle().getFillForegroundColor());
        assertEquals(64, red.getCellStyle().getFillBackgroundColor());
        assertEquals("FFFF:0:0", p.getColor(10).getHexString());

        assertEquals(12, blue.getCellStyle().getFillForegroundColor());
        assertEquals(64, blue.getCellStyle().getFillBackgroundColor());
        assertEquals("0:0:FFFF", p.getColor(12).getHexString());

        assertEquals(11, green.getCellStyle().getFillForegroundColor());
        assertEquals(64, green.getCellStyle().getFillBackgroundColor());
        assertEquals("0:FFFF:0", p.getColor(11).getHexString());
    }

    /**
     * ContinueRecord after EOF
     */
    public void test46137() {
        // This used to break
        HSSFWorkbook wb = openSample("46137.xls");
        assertEquals(7, wb.getNumberOfSheets());
        wb = HSSFTestDataSamples.writeOutAndReadBack(wb);
    }

    /**
     * Odd POIFS blocks issue:
     * block[ 44 ] already removed from org.apache.poi.poifs.storage.BlockListImpl.remove
     */
    public void test45290() {
        HSSFWorkbook wb = openSample("45290.xls");
        assertEquals(1, wb.getNumberOfSheets());
    }

    /**
     * In POI-2.5 user reported exception when parsing a name with a custom VBA function:
     *  =MY_VBA_FUNCTION("lskdjflsk")
     */
    public void test30070() {
        HSSFWorkbook wb = openSample("30070.xls"); //contains custom VBA function 'Commission'
        HSSFSheet sh = wb.getSheetAt(0);
        HSSFCell cell = sh.getRow(0).getCell(1);

        //B1 uses VBA in the formula
        assertEquals("Commission(A1)", cell.getCellFormula());

        //name sales_1 refers to Commission(Sheet0!$A$1)
        int idx = wb.getNameIndex("sales_1");
        assertTrue(idx != -1);

        HSSFName name = wb.getNameAt(idx);
        assertEquals("Commission(Sheet0!$A$1)", name.getRefersToFormula());

    }

    /**
     * The link formulas which is referring to other books cannot be taken (the bug existed prior to POI-3.2)
     * Expected:
     *
     * [link_sub.xls]Sheet1!$A$1
     * [link_sub.xls]Sheet1!$A$2
     * [link_sub.xls]Sheet1!$A$3
     *
     * POI-3.1 output:
     *
     * Sheet1!$A$1
     * Sheet1!$A$2
     * Sheet1!$A$3
     *
     */
    public void test27364() {
        HSSFWorkbook wb = openSample("27364.xls");
        HSSFSheet sheet = wb.getSheetAt(0);

        assertEquals("[link_sub.xls]Sheet1!$A$1", sheet.getRow(0).getCell(0).getCellFormula());
        assertEquals("[link_sub.xls]Sheet1!$A$2", sheet.getRow(1).getCell(0).getCellFormula());
        assertEquals("[link_sub.xls]Sheet1!$A$3", sheet.getRow(2).getCell(0).getCellFormula());
    }

    /**
     * Similar to bug#27364:
     * HSSFCell.getCellFormula() fails with references to external workbooks
     */
    public void test31661() {
        HSSFWorkbook wb = openSample("31661.xls");
        HSSFSheet sheet = wb.getSheetAt(0);
        HSSFCell cell = sheet.getRow(11).getCell(10); //K11
        assertEquals("+'[GM Budget.xls]8085.4450'!$B$2", cell.getCellFormula());
    }

    /**
     * Incorrect handling of non-ISO 8859-1 characters in Windows ANSII Code Page 1252
     */
    public void test27394() {
        HSSFWorkbook wb = openSample("27394.xls");
        assertEquals("\u0161\u017E", wb.getSheetName(0));
        assertEquals("\u0161\u017E\u010D\u0148\u0159", wb.getSheetName(1));
        HSSFSheet sheet = wb.getSheetAt(0);

        assertEquals("\u0161\u017E", sheet.getRow(0).getCell(0).getStringCellValue());
        assertEquals("\u0161\u017E\u010D\u0148\u0159", sheet.getRow(1).getCell(0).getStringCellValue());
    }

    /**
     * Multiple calls of HSSFWorkbook.write result in corrupted xls
     */
    public void test32191() throws IOException {
        HSSFWorkbook wb = openSample("27394.xls");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();
        int size1 = out.size();

        out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();
        int size2 = out.size();

        assertEquals(size1, size2);
        out = new ByteArrayOutputStream();
        wb.write(out);
        out.close();
        int size3 = out.size();
        assertEquals(size2, size3);

    }

    /**
     * java.io.IOException: block[ 0 ] already removed
     * (is an excel 95 file though)
     */
    public void test46904() {
        try {
            openSample("46904.xls");
            fail();
        } catch(OldExcelFormatException e) {
            assertTrue(e.getMessage().startsWith(
                    "The supplied spreadsheet seems to be Excel"
            ));
        }
    }

    /**
     * java.lang.NegativeArraySizeException reading long
     *  non-unicode data for a name record
     */
    public void test47034() {
        HSSFWorkbook wb = openSample("47034.xls");
        assertEquals(893, wb.getNumberOfNames());
        assertEquals("Matthew\\Matthew11_1\\Matthew2331_1\\Matthew2351_1\\Matthew2361_1___lab", wb.getNameName(300));
    }

    /**
     * HSSFRichTextString.length() returns negative for really long strings.
     * The test file was created in OpenOffice 3.0 as Excel does not allow cell text longer than 32,767 characters
     */
    public void test46368() {
        HSSFWorkbook wb = openSample("46368.xls");
    	HSSFSheet s = wb.getSheetAt(0);
        HSSFCell cell1 = s.getRow(0).getCell(0);
        assertEquals(32770, cell1.getStringCellValue().length());

        HSSFCell cell2 = s.getRow(2).getCell(0);
        assertEquals(32766, cell2.getStringCellValue().length());
    }

    /**
     * Short records on certain sheets with charts in them
     */
    public void test48180() {
        HSSFWorkbook wb = openSample("48180.xls");

    	HSSFSheet s = wb.getSheetAt(0);
        HSSFCell cell1 = s.getRow(0).getCell(0);
        assertEquals("test ", cell1.getStringCellValue().toString());

        HSSFCell cell2 = s.getRow(0).getCell(1);
        assertEquals(1.0, cell2.getNumericCellValue(), 0.0);
    }

    /**
     * POI 3.5 beta 7 can not read excel file contain list box (Form Control)
     */
    public void test47701() {
        openSample("47701.xls");
    }

    public void test48026() {
        openSample("48026.xls");
    }

    public void test47251() {
        openSample("47251.xls");
    }
    
    /**
     * Round trip a file with an unusual UnicodeString/ExtRst record parts
     */
    public void test47847() throws Exception {
       HSSFWorkbook wb = openSample("47847.xls");
       assertEquals(3, wb.getNumberOfSheets());
       
       // Find the SST record
       UnicodeString withExt = wb.getWorkbook().getSSTString(0);
       UnicodeString withoutExt = wb.getWorkbook().getSSTString(31);
       
       assertEquals("O:Alloc:Qty", withExt.getString());
       assertTrue((withExt.getOptionFlags() & 0x0004) == 0x0004);
       
       assertEquals("RT", withoutExt.getString());
       assertTrue((withoutExt.getOptionFlags() & 0x0004) == 0x0000);
       
       // Something about continues...

       
       // Write out and re-read
       wb = writeOutAndReadBack(wb);
       assertEquals(3, wb.getNumberOfSheets());
       
       // Check it's the same now
       withExt = wb.getWorkbook().getSSTString(0);
       withoutExt = wb.getWorkbook().getSSTString(31);
       
       assertEquals("O:Alloc:Qty", withExt.getString());
       assertTrue((withExt.getOptionFlags() & 0x0004) == 0x0004);
       
       assertEquals("RT", withoutExt.getString());
       assertTrue((withoutExt.getOptionFlags() & 0x0004) == 0x0000);
    }
    
    /**
     * Problem with cloning a sheet with a chart
     *  contained in it.
     */
    public void test49096() throws Exception {
       HSSFWorkbook wb = openSample("49096.xls");
       assertEquals(1, wb.getNumberOfSheets());
       
       assertNotNull(wb.getSheetAt(0));
       wb.cloneSheet(0);
       assertEquals(2, wb.getNumberOfSheets());
       
       wb = writeOutAndReadBack(wb);
       assertEquals(2, wb.getNumberOfSheets());
    }
    
    /**
     * Newly created sheets need to get a 
     *  proper TabID, otherwise print setup
     *  gets confused on them.
     * Also ensure that print setup refs are
     *  by reference not value 
     */
    public void test46664() throws Exception {
       HSSFWorkbook wb = new HSSFWorkbook();
       HSSFSheet sheet = wb.createSheet("new_sheet");
       HSSFRow row = sheet.createRow((short)0);
       row.createCell(0).setCellValue(new HSSFRichTextString("Column A"));
       row.createCell(1).setCellValue(new HSSFRichTextString("Column B"));
       row.createCell(2).setCellValue(new HSSFRichTextString("Column C"));
       row.createCell(3).setCellValue(new HSSFRichTextString("Column D"));
       row.createCell(4).setCellValue(new HSSFRichTextString("Column E"));
       row.createCell(5).setCellValue(new HSSFRichTextString("Column F"));

       //set print area from column a to column c (on first row)
       wb.setPrintArea(
               0, //sheet index
               0, //start column
               2, //end column
               0, //start row
               0  //end row
       );
       
       wb = writeOutAndReadBack(wb);
       
       // Ensure the tab index
       TabIdRecord tr = null;
       for(Record r : wb.getWorkbook().getRecords()) {
          if(r instanceof TabIdRecord) {
             tr = (TabIdRecord)r;
          }
       }
       assertNotNull(tr);
       assertEquals(1, tr._tabids.length);
       assertEquals(0, tr._tabids[0]);
       
       // Ensure the print setup
       assertEquals("new_sheet!$A$1:$C$1", wb.getPrintArea(0));
       assertEquals("new_sheet!$A$1:$C$1", wb.getName("Print_Area").getRefersToFormula());
       
       // Needs reference not value
       NameRecord nr = wb.getWorkbook().getNameRecord(
             wb.getNameIndex("Print_Area")
       ); 
       assertEquals("Print_Area", nr.getNameText());
       assertEquals(1, nr.getNameDefinition().length);
       assertEquals(
             "new_sheet!$A$1:$C$1", 
             ((Area3DPtg)nr.getNameDefinition()[0]).toFormulaString(HSSFEvaluationWorkbook.create(wb))
       );
       // TODO - fix me to be Reference not Value!
if(1==2) {       
       assertEquals('R', nr.getNameDefinition()[0].getRVAType());
}
    }
    
    /**
     * Problems with formula references to 
     *  sheets via URLs
     */
    public void test45970() throws Exception {
       HSSFWorkbook wb = openSample("FormulaRefs.xls");
       assertEquals(3, wb.getNumberOfSheets());
       
       HSSFSheet s = wb.getSheetAt(0);
       HSSFRow row;
       
       row = s.getRow(0);
       assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1).getCellType());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(1);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("B1", row.getCell(1).getCellFormula());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(2);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("Sheet1!B1", row.getCell(1).getCellFormula());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(3);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("[Formulas2.xls]Sheet1!B2", row.getCell(1).getCellFormula());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(4);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("'[\u0005$http://gagravarr.org/FormulaRefs.xls]Sheet1'!B1", row.getCell(1).getCellFormula());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       // Change 4
       row.getCell(1).setCellFormula("'[\u0005$http://gagravarr.org/FormulaRefs2.xls]Sheet1'!B2");
       row.getCell(1).setCellValue(123.0);
       
       // Add 5
       row = s.createRow(5);
       row.createCell(1, Cell.CELL_TYPE_FORMULA);
       row.getCell(1).setCellFormula("'[\u0005$http://example.com/FormulaRefs.xls]Sheet1'!B1");
       row.getCell(1).setCellValue(234.0);
       
       
       // Re-test
       wb = writeOutAndReadBack(wb);
       s = wb.getSheetAt(0);
       
       row = s.getRow(0);
       assertEquals(Cell.CELL_TYPE_NUMERIC, row.getCell(1).getCellType());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(1);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("B1", row.getCell(1).getCellFormula());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(2);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("Sheet1!B1", row.getCell(1).getCellFormula());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(3);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("[Formulas2.xls]Sheet1!B2", row.getCell(1).getCellFormula());
       assertEquals(112.0, row.getCell(1).getNumericCellValue());
       
// TODO - Fix these so they work...
if(1==2) {
       row = s.getRow(4);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("'[\u0005$http://gagravarr.org/FormulaRefs2.xls]Sheet1'!B2", row.getCell(1).getCellFormula());
       assertEquals(123.0, row.getCell(1).getNumericCellValue());
       
       row = s.getRow(5);
       assertEquals(Cell.CELL_TYPE_FORMULA, row.getCell(1).getCellType());
       assertEquals("'[\u0005$http://example.com/FormulaRefs.xls]Sheet1'!B1", row.getCell(1).getCellFormula());
       assertEquals(234.0, row.getCell(1).getNumericCellValue());
}
    }
    
    /**
     * Test for a file with NameRecord with NameCommentRecord comments
     */
    public void test49185() throws Exception {
      HSSFWorkbook wb = openSample("49185.xls");
      Name name = wb.getName("foobarName");
      assertEquals("This is a comment", name.getComment());
      
      // Rename the name, comment comes with it
      name.setNameName("ChangedName");
      assertEquals("This is a comment", name.getComment());
      
      // Save and re-check
      wb = writeOutAndReadBack(wb);
      name = wb.getName("ChangedName");
      assertEquals("This is a comment", name.getComment());
      
      // Now try to change it
      name.setComment("Changed Comment");
      assertEquals("Changed Comment", name.getComment());
      
      // Save and re-check
      wb = writeOutAndReadBack(wb);
      name = wb.getName("ChangedName");
      assertEquals("Changed Comment", name.getComment());
    }
    
    /**
     * Vertically aligned text
     */
    public void test49524() throws Exception {
       HSSFWorkbook wb = openSample("49524.xls");
       Sheet s = wb.getSheetAt(0);
       Row r = s.getRow(0);
       Cell rotated = r.getCell(0);
       Cell normal = r.getCell(1);
       
       // Check the current ones
       assertEquals(0, normal.getCellStyle().getRotation());
       assertEquals(0xff, rotated.getCellStyle().getRotation());
       
       // Add a new style, also rotated
       CellStyle cs = wb.createCellStyle();
       cs.setRotation((short)0xff);
       Cell nc = r.createCell(2);
       nc.setCellValue("New Rotated Text");
       nc.setCellStyle(cs);
       assertEquals(0xff, nc.getCellStyle().getRotation());
       
       // Write out and read back
       wb = writeOutAndReadBack(wb);
       
       // Re-check
       s = wb.getSheetAt(0);
       r = s.getRow(0);
       rotated = r.getCell(0);
       normal = r.getCell(1);
       nc = r.getCell(2);
       
       assertEquals(0, normal.getCellStyle().getRotation());
       assertEquals(0xff, rotated.getCellStyle().getRotation());
       assertEquals(0xff, nc.getCellStyle().getRotation());
    }
    
    /**
     * Setting the user style name on custom styles
     */
    public void test49689() throws Exception {
       HSSFWorkbook wb = new HSSFWorkbook();
       HSSFSheet s = wb.createSheet("Test");
       HSSFRow r = s.createRow(0);
       HSSFCell c = r.createCell(0);
       
       HSSFCellStyle cs1 = wb.createCellStyle();
       HSSFCellStyle cs2 = wb.createCellStyle();
       HSSFCellStyle cs3 = wb.createCellStyle();
       
       assertEquals(21, cs1.getIndex());
       cs1.setUserStyleName("Testing");
       
       assertEquals(22, cs2.getIndex());
       cs2.setUserStyleName("Testing 2");
       
       assertEquals(23, cs3.getIndex());
       cs3.setUserStyleName("Testing 3");
       
       // Set one
       c.setCellStyle(cs1);
       
       // Write out and read back
       wb = writeOutAndReadBack(wb);
       
       // Re-check
       assertEquals("Testing", wb.getCellStyleAt((short)21).getUserStyleName());
       assertEquals("Testing 2", wb.getCellStyleAt((short)22).getUserStyleName());
       assertEquals("Testing 3", wb.getCellStyleAt((short)23).getUserStyleName());
    }

    public void test49751() {
        HSSFWorkbook wb = openSample("49751.xls");
        short numCellStyles = wb.getNumCellStyles();
        List<String> namedStyles = Arrays.asList(
                "20% - Accent1", "20% - Accent2", "20% - Accent3", "20% - Accent4", "20% - Accent5",
                "20% - Accent6", "40% - Accent1", "40% - Accent2", "40% - Accent3", "40% - Accent4", 
                "40% - Accent5", "40% - Accent6", "60% - Accent1", "60% - Accent2", "60% - Accent3",
                "60% - Accent4", "60% - Accent5", "60% - Accent6", "Accent1", "Accent2", "Accent3",
                "Accent4", "Accent5", "Accent6", "Bad", "Calculation", "Check Cell", "Explanatory Text",
                "Good", "Heading 1", "Heading 2", "Heading 3", "Heading 4", "Input", "Linked Cell",
                "Neutral", "Note", "Output", "Title", "Total", "Warning Text");

        List<String> collecteddStyles = new ArrayList<String>();
        for (short i = 0; i < numCellStyles; i++) {
            HSSFCellStyle cellStyle = wb.getCellStyleAt(i);
            String styleName = cellStyle.getUserStyleName();
            if (styleName != null) {
                collecteddStyles.add(styleName);
            }
        }
        assertTrue(namedStyles.containsAll(collecteddStyles));
    }    
    
    /**
     * Regression with the PageSettingsBlock
     */
    public void test49931() throws Exception {
       HSSFWorkbook wb = openSample("49931.xls");
       
       assertEquals(1, wb.getNumberOfSheets());
       assertEquals("Foo", wb.getSheetAt(0).getRow(0).getCell(0).getRichStringCellValue().toString());
    }
    
    /**
     * Missing left/right/centre options on a footer
     */
    public void test48325() throws Exception {
       HSSFWorkbook wb = openSample("48325.xls");
       HSSFSheet sh = wb.getSheetAt(0);
       HSSFFooter f = sh.getFooter();

       // Will show as the centre, as that is what excel does
       //  with an invalid footer lacking left/right/centre details
       assertEquals("Left text should be empty", "", f.getLeft());
       assertEquals("Right text should be empty", "", f.getRight());
       assertEquals(
             "Center text should contain the illegal value", 
             "BlahBlah blah blah  ", f.getCenter()
       );
    }

    /**
     * IllegalStateException received when creating Data validation in sheet with macro
     */
    public void test50020() throws Exception {
       HSSFWorkbook wb = openSample("50020.xls");
       writeOutAndReadBack(wb);
    }
}
