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

import static org.apache.poi.POITestCase.assertContains;
import static org.apache.poi.hssf.HSSFTestDataSamples.openSampleWorkbook;
import static org.apache.poi.hssf.HSSFTestDataSamples.writeOutAndReadBack;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.apache.poi.hpsf.PropertySet;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hssf.model.InternalSheet;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.CommonObjectDataSubRecord;
import org.apache.poi.hssf.record.EmbeddedObjectRefSubRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.TabIdRecord;
import org.apache.poi.hssf.record.UnknownRecord;
import org.apache.poi.hssf.record.aggregates.FormulaRecordAggregate;
import org.apache.poi.hssf.record.aggregates.PageSettingsBlock;
import org.apache.poi.hssf.record.common.UnicodeString;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.DeletedArea3DPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.BaseTestBugzillaIssues;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.ss.usermodel.ComparisonOperator;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 * <p>
 * <b>YK: If a bug can be tested in terms of common ss interfaces,
 * define the test in the base class {@link BaseTestBugzillaIssues}</b>
 */
final class TestBugs extends BaseTestBugzillaIssues {

    public TestBugs() {
        super(HSSFITestDataProvider.instance);
    }

    private static final Map<String,HSSFFormulaEvaluator> SIMPLE_REFS = new LinkedHashMap<>();

    // References used for the simpleTest convenience method
    @BeforeAll
    public static void initSimpleRefs() {
        String[] refs = {
            "Calculations",
            "/Documents and Settings/crawformk.EUU/Local Settings/Temporary Internet Files/OLK64/Daily Status Report Generation Files/DST - Daily Data Transfer Sheet - 2002.xls",
            "Sheet1",
            "/Documents and Settings/donnag/Local Settings/Temporary Internet Files/OLK7/0231324V1-1.xls",
            "Sheet1",
            "refs/airport.xls",
            "Sheet1",
            "9http://www.principlesofeconometrics.com/excel/airline.xls",
            "Sheet1",
            "C:Documents and Settings/Yegor/My Documents/csco.xls",
        };

        for (int i=0; i<refs.length; i+=2) {
            HSSFWorkbook wb = new HSSFWorkbook();
            wb.createSheet(refs[i]);
            String fileName = refs[i+1];
            if (!fileName.contains("://")) {
                fileName = fileName.replace("/", File.separator);
            }
            SIMPLE_REFS.put(fileName, wb.getCreationHelper().createFormulaEvaluator());
        }

        LocaleUtil.setUserLocale(Locale.US);
    }

    @AfterAll
    public static void cleanUpRefs() {
        SIMPLE_REFS.clear();
    }


    private static void setCellText(HSSFCell cell, String text) {
        cell.setCellValue(new HSSFRichTextString(text));
    }

    /**
     * test rewriting a file with large number of unique strings
     * open resulting file in Excel to check results!
     */
    @Test
    void bug15375() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("15375.xls")) {
            HSSFSheet sheet = wb1.getSheetAt(0);

            HSSFRow row = sheet.getRow(5);
            HSSFCell cell = row.getCell(3);
            if (cell == null) {
                cell = row.createCell(3);
            }

            // Write test
            setCellText(cell, "a test");

            // change existing numeric cell value

            HSSFRow oRow = sheet.getRow(14);
            oRow.getCell(4).setCellValue(75);
            setCellText(oRow.getCell(5), "0.3");

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                HSSFRow oRow2 = wb2.getSheetAt(0).getRow(14);
                assertEquals(75, oRow2.getCell(4).getNumericCellValue());
                assertEquals("0.3", oRow2.getCell(5).getStringCellValue());
            }
        }
    }

    /**
     * Double byte strings
     */
    @Test
    void bug15556() throws Exception {
        simpleTest("15556.xls", wb -> assertNotNull(wb.getSheetAt(0).getRow(45)));
    }

    @Test
    void bug24215() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("24215.xls")) {

            for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
                HSSFSheet sheet = wb.getSheetAt(sheetIndex);
                assertNotNull(sheet);
                int rows = sheet.getLastRowNum();

                for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
                    HSSFRow row = sheet.getRow(rowIndex);
                    assertNotNull(row);
                    int cells = row.getLastCellNum();

                    for (int cellIndex = 0; cellIndex < cells; cellIndex++) {
                        row.getCell(cellIndex);
                    }
                }
            }
        }
    }

    /**
     * Tests read and write of Unicode strings in formula results
     * bug and testcase submitted by Sompop Kumnoonsate
     * The file contains THAI unicode characters.
     */
    @Test
    void bugUnicodeStringFormulaRead() throws Exception {

        try (HSSFWorkbook w = openSampleWorkbook("25695.xls")) {

            HSSFCell a1 = w.getSheetAt(0).getRow(0).getCell(0);
            HSSFCell a2 = w.getSheetAt(0).getRow(0).getCell(1);
            HSSFCell b1 = w.getSheetAt(0).getRow(1).getCell(0);
            HSSFCell b2 = w.getSheetAt(0).getRow(1).getCell(1);
            HSSFCell c1 = w.getSheetAt(0).getRow(2).getCell(0);
            HSSFCell c2 = w.getSheetAt(0).getRow(2).getCell(1);
            HSSFCell d1 = w.getSheetAt(0).getRow(3).getCell(0);
            HSSFCell d2 = w.getSheetAt(0).getRow(3).getCell(1);

        /*
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
        */

            confirmSameCellText(a1, a2);
            confirmSameCellText(b1, b2);
            confirmSameCellText(c1, c2);
            confirmSameCellText(d1, d2);

            try (HSSFWorkbook rw = writeOutAndReadBack(w)) {

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
        }
    }

    private static void confirmSameCellText(HSSFCell a, HSSFCell b) {
        assertEquals(a.getRichStringCellValue().getString(), b.getRichStringCellValue().getString());
    }

    /**
     * names and macros
     */
    @Test
    void bug27852() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("27852.xls")) {
            for (HSSFName name : wb.getAllNames()) {
                assertNotNull(name.getNameName());
                if (!name.isFunctionName()) {
                    assertNotNull(name.getRefersToFormula());
                }
            }
        }
    }

    /**
     * Bug 29206: NPE on HSSFSheet.getRow for blank rows
     */
    @Test
    void bug29206() throws Exception {
        //the first check with blank workbook
        try (HSSFWorkbook wb = openSampleWorkbook("Simple.xls")) {
            HSSFSheet sheet = wb.createSheet();
            for (int i = 1; i < 400; i++) {
                HSSFRow row = sheet.getRow(i);
                assertNull(row);
            }
        }
    }

    /**
     * Bug 29942: Importing Excel files that have been created by Open Office on Linux
     */
    @Test
    void bug29942() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("29942.xls")) {

            HSSFSheet sheet = wb.getSheetAt(0);
            int count = 0;
            for (int i = sheet.getFirstRowNum(); i <= sheet.getLastRowNum(); i++) {
                HSSFRow row = sheet.getRow(i);
                if (row != null) {
                    HSSFCell cell = row.getCell(0);
                    assertEquals(CellType.STRING, cell.getCellType());
                    count++;
                }
            }
            assertEquals(85, count); //should read 85 rows

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                assertNotNull(wb2.getSheetAt(0));
            }
        }
    }

    /**
     * Bug 30540: HSSFSheet.setRowBreak throws NullPointerException
     */
    @Test
    void bug30540() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("30540.xls")) {

            HSSFSheet s1 = wb.getSheetAt(0);
            s1.setRowBreak(1);
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                HSSFSheet s2 = wb2.getSheetAt(0);
                int[] act = s2.getRowBreaks();
                int[] exp = { 1 };
                assertArrayEquals(exp, act);
            }
        }
    }

    /**
     * Bug 35564: HSSFCell.java: NullPtrExc in isGridsPrinted() and getProtect()
     * when HSSFWorkbook is created from file
     */
    @Test
    void bug35564() throws Exception {
        simpleTest("35564.xls", wb -> {
            HSSFSheet sheet = wb.getSheetAt(0);
            assertFalse(sheet.isGridsPrinted());
            assertFalse(sheet.getProtect());
        });
    }

    /**
     * Bug 35565: HSSFCell.java: NullPtrExc in getColumnBreaks() when HSSFWorkbook is created from file
     */
    @Test
    void bug35565() throws Exception {
        simpleTest("35565.xls", wb -> assertNotNull(wb.getSheetAt(0)));
    }

    /**
     * Bug 40285:      CellIterator Skips First Column
     */
    @Test
    void bug40285() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("40285.xls")) {

            HSSFSheet sheet = wb.getSheetAt(0);
            int rownum = 0;
            for (Iterator<Row> it = sheet.rowIterator(); it.hasNext(); rownum++) {
                Row row = it.next();
                assertEquals(rownum, row.getRowNum());
                int cellNum = 0;
                for (Iterator<Cell> it2 = row.cellIterator(); it2.hasNext(); cellNum++) {
                    Cell cell = it2.next();
                    assertEquals(cellNum, cell.getColumnIndex());
                }
            }
        }
    }

    /**
     * Test bug 38266: NPE when adding a row break
     * <p>
     * User's diagnosis:
     * 1. Manually (i.e., not using POI) create an Excel Workbook, making sure it
     * contains a sheet that doesn't have any row breaks
     * 2. Using POI, create a new HSSFWorkbook from the template in step #1
     * 3. Try adding a row break (via sheet.setRowBreak()) to the sheet mentioned in step #1
     * 4. Get a NullPointerException
     */
    @Test
    void bug38266() throws Exception {
        String[] files = {"Simple.xls", "SimpleMultiCell.xls", "duprich1.xls"};
        for (String file : files) {
            try (HSSFWorkbook wb = openSampleWorkbook(file)) {

                HSSFSheet sheet = wb.getSheetAt(0);
                int[] breaks = sheet.getRowBreaks();
                assertEquals(0, breaks.length);

                //add 3 row breaks
                for (int j = 1; j <= 3; j++) {
                    sheet.setRowBreak(j * 20);
                }
            }
        }
    }

    /**
     * Bug 44200: Sheet not cloneable when Note added to excel cell
     */
    @Test
    void bug44200() throws Exception {
        simpleTest("44200.xls", wb -> assertDoesNotThrow(() -> wb.cloneSheet(0)));
    }

    /**
     * Bug 41546: Constructing HSSFWorkbook is failed,
     * Unknown Ptg in Formula: 0x1a (26)
     */
    @Test
    void bug41546() throws Exception {
        simpleTest("41546.xls", wb -> assertEquals(1, wb.getNumberOfSheets()));
    }

    /**
     * Bug 42618: RecordFormatException reading a file containing
     * =CHOOSE(2,A2,A3,A4)
     */
    @Test
    void bug42618() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("SimpleWithChoose.xls");
            HSSFWorkbook wb2 = writeOutAndReadBack(wb1)){
            // Check we detect the string properly too
            HSSFSheet s = wb2.getSheetAt(0);

            // Textual value
            HSSFRow r1 = s.getRow(0);
            HSSFCell c1 = r1.getCell(1);
            assertEquals("=CHOOSE(2,A2,A3,A4)", c1.getRichStringCellValue().toString());

            // Formula Value
            HSSFRow r2 = s.getRow(1);
            HSSFCell c2 = r2.getCell(1);
            assertEquals(25, (int) c2.getNumericCellValue());

            try {
                assertEquals("CHOOSE(2,A2,A3,A4)", c2.getCellFormula());
            } catch (IllegalStateException e) {
                if (e.getMessage().startsWith("Too few arguments")
                        && e.getMessage().indexOf("ConcatPtg") > 0) {
                    fail("identified bug 44306");
                }
            }
        }
    }

    /**
     * Something up with the FileSharingRecord
     */
    @Test
    void bug43251() throws Exception {
        // Used to blow up with an IllegalArgumentException when creating a FileSharingRecord
        simpleTest("43251.xls", wb -> assertEquals(1, wb.getNumberOfSheets()));
    }

    /**
     * Crystal reports generates files with short
     * StyleRecords, which is against the spec
     */
    @Test
    void bug44471() throws Exception {
        // Used to blow up with an ArrayIndexOutOfBounds when creating a StyleRecord
        simpleTest("OddStyleRecord.xls", wb -> assertEquals(1, wb.getNumberOfSheets()));
    }

    /**
     * Files with "read only recommended" were giving
     * grief on the FileSharingRecord
     */
    @Test
    void bug44536() throws Exception {
        // Used to blow up with an IllegalArgumentException when creating a FileSharingRecord
        simpleTest("ReadOnlyRecommended.xls", wb -> {
            // Check read only advised
            assertEquals(3, wb.getNumberOfSheets());
            assertTrue(wb.isWriteProtected());
        });


        // But also check that another wb isn't
        simpleTest("SimpleWithChoose.xls", wb -> assertFalse(wb.isWriteProtected()));
    }

    /**
     * Some files were having problems with the DVRecord,
     * probably due to dropdowns
     */
    @Test
    void bug44593() throws Exception {
        // Used to blow up with an IllegalArgumentException when creating a DVRecord
        // Now won't, but no idea if this means we have rubbish in the DVRecord or not...
        simpleTest("44593.xls", wb -> assertEquals(2, wb.getNumberOfSheets()));
    }

    /**
     * Used to give problems due to trying to read a zero
     * length string, but that's now properly handled
     */
    @Test
    void bug44643() throws Exception {
        // Used to blow up with an IllegalArgumentException
        simpleTest("44643.xls", wb -> assertEquals(1, wb.getNumberOfSheets()));
    }

    /**
     * User reported the wrong number of rows from the
     * iterator, but we can't replicate that
     */
    @Test
    void bug44693() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("44693.xls")) {
            HSSFSheet s = wb.getSheetAt(0);

            // Rows are 1 to 713
            assertEquals(0, s.getFirstRowNum());
            assertEquals(712, s.getLastRowNum());
            assertEquals(713, s.getPhysicalNumberOfRows());

            // Now check the iterator
            int rowsSeen = 0;
            for (Iterator<Row> i = s.rowIterator(); i.hasNext(); ) {
                Row r = i.next();
                assertNotNull(r);
                rowsSeen++;
            }
            assertEquals(713, rowsSeen);
        }
    }

    /**
     * Problems with extracting check boxes from
     * HSSFObjectData
     */
    @Test
    void bug44840() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("WithCheckBoxes.xls")) {

            // Take a look at the embedded objects
            List<HSSFObjectData> objects = wb.getAllEmbeddedObjects();
            assertEquals(1, objects.size());

            HSSFObjectData obj = objects.get(0);
            assertNotNull(obj);

            // Peek inside the underlying record
            EmbeddedObjectRefSubRecord rec = obj.findObjectRecord();
            assertNotNull(rec);

            // assertEquals(32, rec.field_1_stream_id_offset);
            assertEquals(0, rec.getStreamId().intValue()); // WRONG!
            assertEquals("Forms.CheckBox.1", rec.getOLEClassName());
            assertEquals(12, rec.getObjectData().length);

            // Doesn't have a directory
            assertFalse(obj.hasDirectoryEntry());
            assertNotNull(obj.getObjectData());
            assertEquals(12, obj.getObjectData().length);
            assertEquals("Forms.CheckBox.1", obj.getOLE2ClassName());

            assertThrows(IllegalArgumentException.class, obj::getDirectory);
        }
    }

    /**
     * Test that we can delete sheets without
     * breaking the build in named ranges
     * used for printing stuff.
     */
    @Test
    void bug30978() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("30978-alt.xls")) {
            assertEquals(1, wb1.getNumberOfNames());
            assertEquals(3, wb1.getNumberOfSheets());

            // Check all names fit within range, and use
            //  DeletedArea3DPtg
            InternalWorkbook w = wb1.getWorkbook();
            assertNames(wb1, w);

            // Delete the 2nd sheet
            wb1.removeSheetAt(1);

            // Re-check
            assertEquals(1, wb1.getNumberOfNames());
            assertEquals(2, wb1.getNumberOfSheets());
            assertNames(wb1, w);

            // Save and re-load
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                w = wb2.getWorkbook();

                assertEquals(1, wb2.getNumberOfNames());
                assertEquals(2, wb2.getNumberOfSheets());

                assertNames(wb2, w);
            }
        }
    }

    private void assertNames(HSSFWorkbook wb1, InternalWorkbook w) {
        for (int i = 0; i < w.getNumNames(); i++) {
            NameRecord r = w.getNameRecord(i);
            assertTrue(r.getSheetNumber() <= wb1.getNumberOfSheets());

            Ptg[] nd = r.getNameDefinition();
            assertEquals(1, nd.length);
            assertTrue(nd[0] instanceof DeletedArea3DPtg);
        }
    }

    /**
     * Test that fonts get added properly
     */
    @Test
    void bug45338() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            assertEquals(4, wb.getNumberOfFonts());

            HSSFSheet s = wb.createSheet();
            s.createRow(0);
            s.createRow(1);
            s.getRow(0).createCell(0);
            s.getRow(1).createCell(0);

            assertEquals(4, wb.getNumberOfFonts());

            HSSFFont f1 = wb.getFontAt(0);
            assertFalse(f1.getBold());

            // Check that asking for the same font multiple times gives you the same thing.
            // Otherwise, our tests wouldn't work!
            assertSame(wb.getFontAt(0), wb.getFontAt(0));
            assertEquals(wb.getFontAt(0), wb.getFontAt(0));
            assertEquals(wb.getFontAt(2), wb.getFontAt(2));
            assertNotSame(wb.getFontAt(0), wb.getFontAt(2));

            // Look for a new font we have yet to add
            assertNull(wb.findFont(false, (short) 123, (short) 22, "Thingy", false, true, (short) 2, (byte) 2));

            HSSFFont nf = wb.createFont();
            assertEquals(5, wb.getNumberOfFonts());

            assertEquals(5, nf.getIndex());
            assertEquals(nf, wb.getFontAt(5));

            nf.setBold(false);
            nf.setColor((short) 123);
            nf.setFontHeight((short) 22);
            nf.setFontName("Thingy");
            nf.setItalic(false);
            nf.setStrikeout(true);
            nf.setTypeOffset((short) 2);
            nf.setUnderline((byte) 2);

            assertEquals(5, wb.getNumberOfFonts());
            assertEquals(nf, wb.getFontAt(5));

            // Find it now
            assertNotNull(wb.findFont(false, (short) 123, (short) 22, "Thingy", false, true, (short) 2, (byte) 2));
            HSSFFont font = wb.findFont(false, (short) 123, (short) 22, "Thingy", false, true, (short) 2, (byte) 2);
            assertNotNull(font);
            assertEquals(5, font.getIndex());
            assertEquals(nf, wb.findFont(false, (short) 123, (short) 22, "Thingy", false, true, (short) 2, (byte) 2));
        }
    }

    /**
     * From the mailing list - ensure we can handle a formula
     * containing a zip code, eg ="70164"
     */
    @Test
    void bugZipCodeFormulas() throws Exception {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet s = wb1.createSheet();
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
            IllegalStateException e = assertThrows(IllegalStateException.class, c3::getNumericCellValue);
            assertEquals("Cannot get a NUMERIC value from a STRING formula cell", e.getMessage());

            // Now evaluate, they should all be changed
            HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb1);
            eval.evaluateFormulaCell(c1);
            eval.evaluateFormulaCell(c2);
            eval.evaluateFormulaCell(c3);

            // Check that the cells now contain
            //  the correct values
            confirmCachedValue(70164.0, c1);
            confirmCachedValue("70164", c2);
            confirmCachedValue("90210", c3);


            // Write and read
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                HSSFSheet ns = wb2.getSheetAt(0);
                HSSFCell nc1 = ns.getRow(0).getCell(0);
                HSSFCell nc2 = ns.getRow(0).getCell(1);
                HSSFCell nc3 = ns.getRow(0).getCell(2);

                // Re-check
                confirmCachedValue(70164.0, nc1);
                confirmCachedValue("70164", nc2);
                confirmCachedValue("90210", nc3);

                int i = 0;
                for (Iterator<CellValueRecordInterface> it = ns.getSheet().getCellValueIterator(); it.hasNext(); i++) {
                    CellValueRecordInterface cvr = it.next();
                    if (cvr instanceof FormulaRecordAggregate) {
                        FormulaRecordAggregate fr = (FormulaRecordAggregate) cvr;

                        if (i == 0) {
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
                assertEquals(3, i);
            }
        }
    }

    private static void confirmCachedValue(double expectedValue, HSSFCell cell) {
        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals(CellType.NUMERIC, cell.getCachedFormulaResultType());
        assertEquals(expectedValue, cell.getNumericCellValue(), 0.0);
    }

    private static void confirmCachedValue(String expectedValue, HSSFCell cell) {
        assertEquals(CellType.FORMULA, cell.getCellType());
        assertEquals(CellType.STRING, cell.getCachedFormulaResultType());
        assertEquals(expectedValue, cell.getRichStringCellValue().getString());
    }

    /**
     * Problem with "Vector Rows", eg a whole
     * column which is set to the result of
     * {=sin(B1:B9)}(9,1), so that each cell is
     * shown to have the contents
     * {=sin(B1:B9){9,1)[rownum][0]
     * In this sample file, the vector column
     * is C, and the data column is B.
     * <p>
     * Expected ExpPtg to be converted from Shared to Non-Shared...
     */
    @Disabled("For now, blows up with an exception from ExtPtg")
    @Test
    void test43623() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("43623.xls")) {
            assertEquals(1, wb1.getNumberOfSheets());

            HSSFSheet s1 = wb1.getSheetAt(0);

            HSSFCell c1 = s1.getRow(0).getCell(2);
            HSSFCell c2 = s1.getRow(1).getCell(2);
            HSSFCell c3 = s1.getRow(2).getCell(2);

            // These formula contents are a guess...
            assertEquals("{=sin(B1:B9){9,1)[0][0]", c1.getCellFormula());
            assertEquals("{=sin(B1:B9){9,1)[1][0]", c2.getCellFormula());
            assertEquals("{=sin(B1:B9){9,1)[2][0]", c3.getCellFormula());

            // Save and re-open, ensure it still works
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                HSSFSheet ns1 = wb2.getSheetAt(0);
                HSSFCell nc1 = ns1.getRow(0).getCell(2);
                HSSFCell nc2 = ns1.getRow(1).getCell(2);
                HSSFCell nc3 = ns1.getRow(2).getCell(2);

                assertEquals("{=sin(B1:B9){9,1)[0][0]", nc1.getCellFormula());
                assertEquals("{=sin(B1:B9){9,1)[1][0]", nc2.getCellFormula());
                assertEquals("{=sin(B1:B9){9,1)[2][0]", nc3.getCellFormula());
            }
        }
    }

    /**
     * People are all getting confused about the last
     * row and cell number
     */
    @Test
    void bug30635() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet s = wb.createSheet();

            // No rows, first/last return -1
            assertEquals(-1, s.getFirstRowNum());
            assertEquals(-1, s.getLastRowNum());
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
    }

    /**
     * Data Tables - ptg 0x2
     */
    @Test
    void bug44958() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("44958.xls")) {
            HSSFSheet s;
            HSSFRow r;
            HSSFCell c;

            // Check the contents of the formulas

            // E4 to G9 of sheet 4 make up the table
            s = wb.getSheet("OneVariable Table Completed");
            r = s.getRow(3);
            c = r.getCell(4);
            assertEquals(CellType.FORMULA, c.getCellType());

            // TODO - check the formula once tables and
            //  arrays are properly supported


            // E4 to H9 of sheet 5 make up the table
            s = wb.getSheet("TwoVariable Table Example");
            r = s.getRow(3);
            c = r.getCell(4);
            assertEquals(CellType.FORMULA, c.getCellType());

            // TODO - check the formula once tables and
            //  arrays are properly supported
        }
    }

    /**
     * 45322: HSSFSheet.autoSizeColumn fails when style.getDataFormat() returns -1
     */
    @Test
    void bug45322() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("44958.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            for (short i = 0; i < 30; i++) {
                sh.autoSizeColumn(i);
                assertNotEquals(0, sh.getColumnWidth(i));
            }
        }
    }

    /**
     * We used to add too many UncalcRecords to sheets
     * with diagrams on. Don't any more
     */
    @Test
    void bug45414() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("WithThreeCharts.xls")) {
            wb1.getSheetAt(0).setForceFormulaRecalculation(true);
            wb1.getSheetAt(1).setForceFormulaRecalculation(false);
            wb1.getSheetAt(2).setForceFormulaRecalculation(true);

            // Write out and back in again
            // This used to break
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {

                // Check now set as it should be
                assertTrue(wb2.getSheetAt(0).getForceFormulaRecalculation());
                assertFalse(wb2.getSheetAt(1).getForceFormulaRecalculation());
                assertTrue(wb2.getSheetAt(2).getForceFormulaRecalculation());
            }
        }
    }

    /**
     * Very hidden sheets not displaying as such
     */
    @Test
    void bug45761() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("45761.xls")) {
            assertEquals(3, wb1.getNumberOfSheets());

            assertFalse(wb1.isSheetHidden(0));
            assertFalse(wb1.isSheetVeryHidden(0));
            assertTrue(wb1.isSheetHidden(1));
            assertFalse(wb1.isSheetVeryHidden(1));
            assertFalse(wb1.isSheetHidden(2));
            assertTrue(wb1.isSheetVeryHidden(2));

            // Change sheet 0 to be very hidden, and re-load
            wb1.setSheetVisibility(0, SheetVisibility.VERY_HIDDEN);

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                assertFalse(wb2.isSheetHidden(0));
                assertTrue(wb2.isSheetVeryHidden(0));
                assertTrue(wb2.isSheetHidden(1));
                assertFalse(wb2.isSheetVeryHidden(1));
                assertFalse(wb2.isSheetHidden(2));
                assertTrue(wb2.isSheetVeryHidden(2));
            }
        }
    }

    /**
     * The resolution for bug 45777 assumed that the maximum text length in a header / footer
     * record was 256 bytes.  This assumption appears to be wrong.  Since the fix for bug 47244,
     * POI now supports header / footer text lengths beyond 256 bytes.
     */
    @Test
    void bug45777() throws Exception {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
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
            writeOutAndReadBack(wb).close();

            s.getHeader().setCenter(s251);
            assertEquals(257, s.getHeader().getRawText().length());
            writeOutAndReadBack(wb).close();

            // header can be more than 256 bytes
            s.getHeader().setCenter(s250); // 256 bytes required
            s.getHeader().setCenter(s251); // 257 bytes required

            // Now try on footers
            s.getFooter().setCenter(s248);
            assertEquals(254, s.getFooter().getRawText().length());
            writeOutAndReadBack(wb).close();

            s.getFooter().setCenter(s251);
            assertEquals(257, s.getFooter().getRawText().length());
            writeOutAndReadBack(wb).close();

            // footer can be more than 256 bytes
            s.getFooter().setCenter(s250); // 256 bytes required
            s.getFooter().setCenter(s251); // 257 bytes required

        }
    }

    /**
     * Charts with long titles
     */
    @Test
    void bug45784() throws Exception {
        simpleTest("45784.xls", wb -> assertEquals(1, wb.getNumberOfSheets()));
    }

    /**
     * Cell background colours
     */
    @Test
    void bug45492() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("45492.xls")) {
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
    }

    /**
     * ContinueRecord after EOF
     */
    @Test
    void bug46137() throws Exception {
        simpleTest("46137.xls", wb -> assertEquals(7, wb.getNumberOfSheets()));
    }

    /**
     * Odd POIFS blocks issue:
     * block[ 44 ] already removed from org.apache.poi.poifs.storage.BlockListImpl.remove
     */
    @Test
    void bug45290() throws Exception {
        simpleTest("45290.xls", wb -> assertEquals(1, wb.getNumberOfSheets()));
    }

    /**
     * In POI-2.5 user reported exception when parsing a name with a custom VBA function:
     * =MY_VBA_FUNCTION("lskdjflsk")
     */
    @Test
    void bug30070() throws Exception {
        //contains custom VBA function 'Commission'
        try (HSSFWorkbook wb = openSampleWorkbook("30070.xls")) {
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
    }

    /**
     * The link formulas which is referring to other books cannot be taken (the bug existed prior to POI-3.2)
     * Expected:
     * <p>
     * [link_sub.xls]Sheet1!$A$1
     * [link_sub.xls]Sheet1!$A$2
     * [link_sub.xls]Sheet1!$A$3
     * <p>
     * POI-3.1 output:
     * <p>
     * Sheet1!$A$1
     * Sheet1!$A$2
     * Sheet1!$A$3
     */
    @Test
    void bug27364() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("27364.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);

            assertEquals("[link_sub.xls]Sheet1!$A$1", sheet.getRow(0).getCell(0).getCellFormula());
            assertEquals("[link_sub.xls]Sheet1!$A$2", sheet.getRow(1).getCell(0).getCellFormula());
            assertEquals("[link_sub.xls]Sheet1!$A$3", sheet.getRow(2).getCell(0).getCellFormula());
        }
    }

    /**
     * Similar to bug#27364:
     * HSSFCell.getCellFormula() fails with references to external workbooks
     */
    @Test
    void bug31661() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("31661.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFCell cell = sheet.getRow(11).getCell(10); //K11
            assertEquals("+'[GM Budget.xls]8085.4450'!$B$2", cell.getCellFormula());
        }
    }

    /**
     * Incorrect handling of non-ISO 8859-1 characters in Windows ANSII Code Page 1252
     */
    @Test
    void bug27394() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("27394.xls")) {
            assertEquals("\u0161\u017E", wb.getSheetName(0));
            assertEquals("\u0161\u017E\u010D\u0148\u0159", wb.getSheetName(1));
            HSSFSheet sheet = wb.getSheetAt(0);

            assertEquals("\u0161\u017E", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("\u0161\u017E\u010D\u0148\u0159", sheet.getRow(1).getCell(0).getStringCellValue());
        }
    }

    /**
     * Multiple calls of HSSFWorkbook.write result in corrupted xls
     */
    @Test
    void bug32191() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("27394.xls");
             ByteArrayOutputStream out1 = new ByteArrayOutputStream();
             ByteArrayOutputStream out2 = new ByteArrayOutputStream();
             ByteArrayOutputStream out3 = new ByteArrayOutputStream()) {
            wb.write(out1);
            wb.write(out2);
            wb.write(out3);
            assertArrayEquals(out1.toByteArray(), out2.toByteArray());
            assertArrayEquals(out2.toByteArray(), out3.toByteArray());
        }
    }

    /**
     * java.lang.NegativeArraySizeException reading long
     * non-unicode data for a name record
     */
    @Test
    void bug47034() throws Exception {
        simpleTest("47034.xls", wb -> {
            assertEquals(893, wb.getNumberOfNames());
            assertEquals("Matthew\\Matthew11_1\\Matthew2331_1\\Matthew2351_1\\Matthew2361_1___lab", wb.getNameName(300));
        });
    }

    /**
     * HSSFRichTextString.length() returns negative for really long strings.
     * The test file was created in OpenOffice 3.0 as Excel does not allow cell text longer than 32,767 characters
     */
    @Test
    void bug46368() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("46368.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFCell cell1 = s.getRow(0).getCell(0);
            assertEquals(32770, cell1.getStringCellValue().length());

            HSSFCell cell2 = s.getRow(2).getCell(0);
            assertEquals(32766, cell2.getStringCellValue().length());
        }
    }

    /**
     * Short records on certain sheets with charts in them
     */
    @Test
    void bug48180() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("48180.xls")) {

            HSSFSheet s = wb.getSheetAt(0);
            HSSFCell cell1 = s.getRow(0).getCell(0);
            assertEquals("test ", cell1.getStringCellValue());

            HSSFCell cell2 = s.getRow(0).getCell(1);
            assertEquals(1.0, cell2.getNumericCellValue(), 0.0);
        }
    }

    /**
     * Round trip a file with an unusual UnicodeString/ExtRst record parts
     */
    @Test
    void bug47847() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("47847.xls")) {
            assertEquals(3, wb1.getNumberOfSheets());

            // Find the SST record
            UnicodeString withExt = wb1.getWorkbook().getSSTString(0);
            UnicodeString withoutExt = wb1.getWorkbook().getSSTString(31);

            assertEquals("O:Alloc:Qty", withExt.getString());
            assertEquals(0x0004, (withExt.getOptionFlags() & 0x0004));

            assertEquals("RT", withoutExt.getString());
            assertEquals(0x0000, (withoutExt.getOptionFlags() & 0x0004));

            // Something about continues...


            // Write out and re-read
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                assertEquals(3, wb2.getNumberOfSheets());

                // Check it's the same now
                withExt = wb2.getWorkbook().getSSTString(0);
                withoutExt = wb2.getWorkbook().getSSTString(31);

                assertEquals("O:Alloc:Qty", withExt.getString());
                assertEquals(0x0004, (withExt.getOptionFlags() & 0x0004));

                assertEquals("RT", withoutExt.getString());
                assertEquals(0x0000, (withoutExt.getOptionFlags() & 0x0004));
            }
        }
    }

    /**
     * Problem with cloning a sheet with a chart
     * contained in it.
     */
    @Test
    void bug49096() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("49096.xls")) {
            assertEquals(1, wb1.getNumberOfSheets());

            assertNotNull(wb1.getSheetAt(0));
            wb1.cloneSheet(0);
            assertEquals(2, wb1.getNumberOfSheets());

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                assertEquals(2, wb2.getNumberOfSheets());
            }
        }
    }

    /**
     * Newly created sheets need to get a
     * proper TabID, otherwise print setup
     * gets confused on them.
     * Also ensure that print setup refs are
     * by reference not value
     */
    @Test
    void bug46664() throws Exception {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet sheet = wb1.createSheet("new_sheet");
            HSSFRow row = sheet.createRow((short) 0);
            row.createCell(0).setCellValue(new HSSFRichTextString("Column A"));
            row.createCell(1).setCellValue(new HSSFRichTextString("Column B"));
            row.createCell(2).setCellValue(new HSSFRichTextString("Column C"));
            row.createCell(3).setCellValue(new HSSFRichTextString("Column D"));
            row.createCell(4).setCellValue(new HSSFRichTextString("Column E"));
            row.createCell(5).setCellValue(new HSSFRichTextString("Column F"));

            //set print area from column a to column c (on first row)
            wb1.setPrintArea(
                    0, //sheet index
                    0, //start column
                    2, //end column
                    0, //start row
                    0  //end row
            );

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                // Ensure the tab index
                TabIdRecord tr = null;
                for (org.apache.poi.hssf.record.Record r : wb2.getWorkbook().getRecords()) {
                    if (r instanceof TabIdRecord) {
                        tr = (TabIdRecord) r;
                    }
                }
                assertNotNull(tr);
                assertEquals(1, tr.getTabIdSize());
                assertEquals(0, tr.getTabIdAt(0));

                // Ensure the print setup
                assertEquals("new_sheet!$A$1:$C$1", wb2.getPrintArea(0));
                HSSFName printArea = wb2.getName("Print_Area");
                assertNotNull(printArea);
                assertEquals("new_sheet!$A$1:$C$1", printArea.getRefersToFormula());

                // Needs reference not value
                NameRecord nr = wb2.getWorkbook().getNameRecord(
                        wb2.getNameIndex("Print_Area")
                );
                assertEquals("Print_Area", nr.getNameText());
                assertEquals(1, nr.getNameDefinition().length);
                assertEquals(
                        "new_sheet!$A$1:$C$1",
                        ((Area3DPtg) nr.getNameDefinition()[0]).toFormulaString(HSSFEvaluationWorkbook.create(wb2))
                );
                assertEquals('R', nr.getNameDefinition()[0].getRVAType());
            }
        }
    }

    /**
     * Problems with formula references to
     * sheets via URLs
     */
    @Test
    void bug45970() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("FormulaRefs.xls")) {
            assertEquals(3, wb1.getNumberOfSheets());

            HSSFSheet s = wb1.getSheetAt(0);
            HSSFRow row;

            row = s.getRow(0);
            assertEquals(CellType.NUMERIC, row.getCell(1).getCellType());
            assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

            row = s.getRow(1);
            assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
            assertEquals("B1", row.getCell(1).getCellFormula());
            assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

            row = s.getRow(2);
            assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
            assertEquals("Sheet1!B1", row.getCell(1).getCellFormula());
            assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

            row = s.getRow(3);
            assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
            assertEquals("[Formulas2.xls]Sheet1!B2", row.getCell(1).getCellFormula());
            assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

            row = s.getRow(4);
            assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
            assertEquals("'[$http://gagravarr.org/FormulaRefs.xls]Sheet1'!B1", row.getCell(1).getCellFormula());
            assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

            // Link our new workbook
            Workbook externalWb1 = new HSSFWorkbook();
            externalWb1.createSheet("Sheet1");
            assertEquals(4, wb1.linkExternalWorkbook("$http://gagravarr.org/FormulaRefs2.xls", externalWb1));

            // Change 4
            row.getCell(1).setCellFormula("'[$http://gagravarr.org/FormulaRefs2.xls]Sheet1'!B2");
            row.getCell(1).setCellValue(123.0);

            // Link our new workbook
            Workbook externalWb2 = new HSSFWorkbook();
            externalWb2.createSheet("Sheet1");
            assertEquals(5, wb1.linkExternalWorkbook("$http://example.com/FormulaRefs.xls", externalWb2));

            // Add 5
            row = s.createRow(5);
            row.createCell(1, CellType.FORMULA);
            row.getCell(1).setCellFormula("'[$http://example.com/FormulaRefs.xls]Sheet1'!B1");
            row.getCell(1).setCellValue(234.0);

            // Re-test
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                s = wb2.getSheetAt(0);

                row = s.getRow(0);
                assertEquals(CellType.NUMERIC, row.getCell(1).getCellType());
                assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

                row = s.getRow(1);
                assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
                assertEquals("B1", row.getCell(1).getCellFormula());
                assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

                row = s.getRow(2);
                assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
                assertEquals("Sheet1!B1", row.getCell(1).getCellFormula());
                assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

                row = s.getRow(3);
                assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
                assertEquals("[Formulas2.xls]Sheet1!B2", row.getCell(1).getCellFormula());
                assertEquals(112.0, row.getCell(1).getNumericCellValue(), 0);

                row = s.getRow(4);
                assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
                assertEquals("'[$http://gagravarr.org/FormulaRefs2.xls]Sheet1'!B2", row.getCell(1).getCellFormula());
                assertEquals(123.0, row.getCell(1).getNumericCellValue(), 0);

                row = s.getRow(5);
                assertEquals(CellType.FORMULA, row.getCell(1).getCellType());
                assertEquals("'[$http://example.com/FormulaRefs.xls]Sheet1'!B1", row.getCell(1).getCellFormula());
                assertEquals(234.0, row.getCell(1).getNumericCellValue(), 0);

            }
        }
    }

    /**
     * Test for a file with NameRecord with NameCommentRecord comments
     */
    @Test
    void bug49185() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("49185.xls")) {
            Name name = wb1.getName("foobarName");
            assertNotNull(name);
            assertEquals("This is a comment", name.getComment());

            // Rename the name, comment comes with it
            name.setNameName("ChangedName");
            assertEquals("This is a comment", name.getComment());

            // Save and re-check
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                name = wb2.getName("ChangedName");
                assertNotNull(name);
                assertEquals("This is a comment", name.getComment());

                // Now try to change it
                name.setComment("Changed Comment");
                assertEquals("Changed Comment", name.getComment());

                // Save and re-check
                try (HSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    name = wb3.getName("ChangedName");
                    assertNotNull(name);
                    assertEquals("Changed Comment", name.getComment());
                }
            }
        }
    }

    /** Vertically aligned text */
    @Test
    void bug49524() throws Exception {
        try (HSSFWorkbook wb1 = openSampleWorkbook("49524.xls")) {
            Sheet s = wb1.getSheetAt(0);
            Row r = s.getRow(0);
            Cell rotated = r.getCell(0);
            Cell normal = r.getCell(1);

            // Check the current ones
            assertEquals(0, normal.getCellStyle().getRotation());
            assertEquals(0xff, rotated.getCellStyle().getRotation());

            // Add a new style, also rotated
            CellStyle cs = wb1.createCellStyle();
            cs.setRotation((short) 0xff);
            Cell nc = r.createCell(2);
            nc.setCellValue("New Rotated Text");
            nc.setCellStyle(cs);
            assertEquals(0xff, nc.getCellStyle().getRotation());

            // Write out and read back
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                // Re-check
                s = wb2.getSheetAt(0);
                r = s.getRow(0);
                rotated = r.getCell(0);
                normal = r.getCell(1);
                nc = r.getCell(2);

                assertEquals(0, normal.getCellStyle().getRotation());
                assertEquals(0xff, rotated.getCellStyle().getRotation());
                assertEquals(0xff, nc.getCellStyle().getRotation());
            }
        }
    }

    /**
     * Setting the user style name on custom styles
     */
    @Test
    void bug49689() throws Exception {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            HSSFSheet s = wb1.createSheet("Test");
            HSSFRow r = s.createRow(0);
            HSSFCell c = r.createCell(0);

            HSSFCellStyle cs1 = wb1.createCellStyle();
            HSSFCellStyle cs2 = wb1.createCellStyle();
            HSSFCellStyle cs3 = wb1.createCellStyle();

            assertEquals(21, cs1.getIndex());
            cs1.setUserStyleName("Testing");

            assertEquals(22, cs2.getIndex());
            cs2.setUserStyleName("Testing 2");

            assertEquals(23, cs3.getIndex());
            cs3.setUserStyleName("Testing 3");

            // Set one
            c.setCellStyle(cs1);

            // Write out and read back
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                // Re-check
                assertEquals("Testing", wb2.getCellStyleAt((short) 21).getUserStyleName());
                assertEquals("Testing 2", wb2.getCellStyleAt((short) 22).getUserStyleName());
                assertEquals("Testing 3", wb2.getCellStyleAt((short) 23).getUserStyleName());
            }
        }
    }

    @Test
    void bug49751() throws Exception {
        Set<String> exp = new HashSet<>(Arrays.asList(
            "20% - Accent1", "20% - Accent2", "20% - Accent3", "20% - Accent4", "20% - Accent5",
            "20% - Accent6", "40% - Accent1", "40% - Accent2", "40% - Accent3", "40% - Accent4",
            "40% - Accent5", "40% - Accent6", "60% - Accent1", "60% - Accent2", "60% - Accent3",
            "60% - Accent4", "60% - Accent5", "60% - Accent6", "Accent1", "Accent2", "Accent3",
            "Accent4", "Accent5", "Accent6", "Bad", "Calculation", "Check Cell", "Explanatory Text",
            "Good", "Heading 1", "Heading 2", "Heading 3", "Heading 4", "Input", "Linked Cell",
            "Neutral", "Note", "Output", "Title", "Total", "Warning Text"));

        try (HSSFWorkbook wb = openSampleWorkbook("49751.xls")) {
            Set<String> act = IntStream
                .range(0, wb.getNumCellStyles())
                .mapToObj(wb::getCellStyleAt)
                .map(HSSFCellStyle::getUserStyleName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
            assertEquals(exp, act);
        }
    }

    /**
     * Regression with the PageSettingsBlock
     */
    @Test
    void bug49931() throws Exception {
        simpleTest("49931.xls", wb -> {
            assertEquals(1, wb.getNumberOfSheets());
            assertEquals("Foo", wb.getSheetAt(0).getRow(0).getCell(0).getRichStringCellValue().toString());
        });
    }

    /**
     * Missing left/right/centre options on a footer
     */
    @Test
    void bug48325() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("48325.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            HSSFFooter f = sh.getFooter();

            // Will show as the center, as that is what excel does
            //  with an invalid footer lacking left/right/center details
            assertEquals("", f.getLeft(), "Left text should be empty");
            assertEquals("", f.getRight(), "Right text should be empty");
            assertEquals("BlahBlah blah blah  ", f.getCenter(),
                "Center text should contain the illegal value");
        }
    }

    /**
     * Last row number when shifting rows
     */
    @Test
    void bug50416LastRowNumber() throws IOException {
        // Create the workbook with 1 sheet which contains 3 rows
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Bug50416");
            Row row1 = sheet.createRow(0);
            Cell cellA_1 = row1.createCell(0, CellType.STRING);
            cellA_1.setCellValue("Cell A,1");
            Row row2 = sheet.createRow(1);
            Cell cellA_2 = row2.createCell(0, CellType.STRING);
            cellA_2.setCellValue("Cell A,2");
            Row row3 = sheet.createRow(2);
            Cell cellA_3 = row3.createCell(0, CellType.STRING);
            cellA_3.setCellValue("Cell A,3");

            // Test the last Row number it currently correct
            assertEquals(2, sheet.getLastRowNum());

            // Shift the first row to the end
            sheet.shiftRows(0, 0, 3);
            assertEquals(3, sheet.getLastRowNum());
            assertEquals(-1, sheet.getRow(0).getLastCellNum());
            assertEquals("Cell A,2", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Cell A,3", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Cell A,1", sheet.getRow(3).getCell(0).getStringCellValue());

            // Shift the 2nd row up to the first one
            sheet.shiftRows(1, 1, -1);
            assertEquals(3, sheet.getLastRowNum());
            assertEquals("Cell A,2", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals(-1, sheet.getRow(1).getLastCellNum());
            assertEquals("Cell A,3", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Cell A,1", sheet.getRow(3).getCell(0).getStringCellValue());

            // Shift the 4th row up into the gap in the 3rd row
            sheet.shiftRows(3, 3, -2);
            assertEquals(2, sheet.getLastRowNum());
            assertEquals("Cell A,2", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Cell A,1", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Cell A,3", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals(-1, sheet.getRow(3).getLastCellNum());

            // Now zap the empty 4th row - won't do anything
            sheet.removeRow(sheet.getRow(3));

            // Test again the last row number which should be 2
            assertEquals(2, sheet.getLastRowNum());
            assertEquals("Cell A,2", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Cell A,1", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Cell A,3", sheet.getRow(2).getCell(0).getStringCellValue());

        }
    }

    /**
     * If you send a file between Excel and OpenOffice enough, something
     * will turn the "General" format into "GENERAL"
     */
    @Test
    void bug50756() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("50756.xls")) {
            HSSFSheet s = wb.getSheetAt(0);
            HSSFRow r17 = s.getRow(16);
            HSSFRow r18 = s.getRow(17);
            HSSFDataFormatter df = new HSSFDataFormatter();

            assertEquals(10.0, r17.getCell(1).getNumericCellValue(), 0);
            assertEquals(20.0, r17.getCell(2).getNumericCellValue(), 0);
            assertEquals(20.0, r17.getCell(3).getNumericCellValue(), 0);
            assertEquals("GENERAL", r17.getCell(1).getCellStyle().getDataFormatString());
            assertEquals("GENERAL", r17.getCell(2).getCellStyle().getDataFormatString());
            assertEquals("GENERAL", r17.getCell(3).getCellStyle().getDataFormatString());
            assertEquals("10", df.formatCellValue(r17.getCell(1)));
            assertEquals("20", df.formatCellValue(r17.getCell(2)));
            assertEquals("20", df.formatCellValue(r17.getCell(3)));

            assertEquals(16.0, r18.getCell(1).getNumericCellValue(), 0);
            assertEquals(35.0, r18.getCell(2).getNumericCellValue(), 0);
            assertEquals(123.0, r18.getCell(3).getNumericCellValue(), 0);
            assertEquals("GENERAL", r18.getCell(1).getCellStyle().getDataFormatString());
            assertEquals("GENERAL", r18.getCell(2).getCellStyle().getDataFormatString());
            assertEquals("GENERAL", r18.getCell(3).getCellStyle().getDataFormatString());
            assertEquals("16", df.formatCellValue(r18.getCell(1)));
            assertEquals("35", df.formatCellValue(r18.getCell(2)));
            assertEquals("123", df.formatCellValue(r18.getCell(3)));
        }
    }

    /**
     * A protected sheet with comments, when written out by
     * POI, ends up upsetting excel.
     * TODO Identify the cause and add extra asserts for
     * the bit excel cares about
     */
    @Test
    void bug50833() throws Exception {
        HSSFWorkbook wb1 = openSampleWorkbook("50833.xls");
        HSSFSheet s = wb1.getSheetAt(0);
        assertEquals("Sheet1", s.getSheetName());
        assertFalse(s.getProtect());

        HSSFCell c = s.getRow(0).getCell(0);
        assertEquals("test cell value", c.getRichStringCellValue().getString());

        HSSFComment cmt = c.getCellComment();
        assertNotNull(cmt);
        assertEquals("Robert Lawrence", cmt.getAuthor());
        assertEquals("Robert Lawrence:\ntest comment", cmt.getString().getString());

        // Reload
        HSSFWorkbook wb2 = writeOutAndReadBack(wb1);
        wb1.close();
        s = wb2.getSheetAt(0);
        c = s.getRow(0).getCell(0);

        // Re-check the comment
        cmt = c.getCellComment();
        assertNotNull(cmt);
        assertEquals("Robert Lawrence", cmt.getAuthor());
        assertEquals("Robert Lawrence:\ntest comment", cmt.getString().getString());

        // TODO Identify what excel doesn't like, and check for that
        wb2.close();
    }

    /**
     * The spec says that ChartEndObjectRecord has 6 reserved
     * bytes on the end, but we sometimes find files without...
     */
    @Test
    void bug50939() throws Exception {
        simpleTest("50939.xls", wb -> assertEquals(2, wb.getNumberOfSheets()));
    }

    @Test
    void bug49219() throws Exception {
        simpleTest("49219.xls", wb -> {
            assertEquals(1, wb.getNumberOfSheets());
            assertEquals("DGATE", wb.getSheetAt(0).getRow(1).getCell(0).getStringCellValue());
        });
    }

    @Test
    void bug48968() throws Exception {
        TimeZone userTimeZone = LocaleUtil.getUserTimeZone();
        LocaleUtil.setUserTimeZone(TimeZone.getTimeZone("CET"));
        try {
            HSSFWorkbook wb = openSampleWorkbook("48968.xls");
            assertEquals(1, wb.getNumberOfSheets());

            DataFormatter fmt = new DataFormatter();

            // Check the dates
            HSSFSheet s = wb.getSheetAt(0);
            Cell cell_d20110325 = s.getRow(0).getCell(0);
            Cell cell_d19000102 = s.getRow(11).getCell(0);
            Cell cell_d19000100 = s.getRow(21).getCell(0);
            assertEquals(s.getRow(0).getCell(3).getStringCellValue(), fmt.formatCellValue(cell_d20110325));
            assertEquals(s.getRow(11).getCell(3).getStringCellValue(), fmt.formatCellValue(cell_d19000102));
            // There is no such thing as 00/01/1900...
            assertEquals("00/01/1900 06:14:24", s.getRow(21).getCell(3).getStringCellValue());
            assertEquals("31/12/1899 06:14:24", fmt.formatCellValue(cell_d19000100));

            // Check the cached values
            assertEquals("HOUR(A1)", s.getRow(5).getCell(0).getCellFormula());
            assertEquals(11.0, s.getRow(5).getCell(0).getNumericCellValue(), 0);
            assertEquals("MINUTE(A1)", s.getRow(6).getCell(0).getCellFormula());
            assertEquals(39.0, s.getRow(6).getCell(0).getNumericCellValue(), 0);
            assertEquals("SECOND(A1)", s.getRow(7).getCell(0).getCellFormula());
            assertEquals(54.0, s.getRow(7).getCell(0).getNumericCellValue(), 0);

            // Re-evaluate and check
            HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
            assertEquals("HOUR(A1)", s.getRow(5).getCell(0).getCellFormula());
            assertEquals(11.0, s.getRow(5).getCell(0).getNumericCellValue(), 0);
            assertEquals("MINUTE(A1)", s.getRow(6).getCell(0).getCellFormula());
            assertEquals(39.0, s.getRow(6).getCell(0).getNumericCellValue(), 0);
            assertEquals("SECOND(A1)", s.getRow(7).getCell(0).getCellFormula());
            assertEquals(54.0, s.getRow(7).getCell(0).getNumericCellValue(), 0);

            // Push the time forward a bit and check
            double date = s.getRow(0).getCell(0).getNumericCellValue();
            s.getRow(0).getCell(0).setCellValue(date + 1.26);

            HSSFFormulaEvaluator.evaluateAllFormulaCells(wb);
            assertEquals("HOUR(A1)", s.getRow(5).getCell(0).getCellFormula());
            assertEquals(11.0 + 6.0, s.getRow(5).getCell(0).getNumericCellValue(), 0);
            assertEquals("MINUTE(A1)", s.getRow(6).getCell(0).getCellFormula());
            assertEquals(39.0 + 14.0 + 1, s.getRow(6).getCell(0).getNumericCellValue(), 0);
            assertEquals("SECOND(A1)", s.getRow(7).getCell(0).getCellFormula());
            assertEquals(54.0 + 24.0 - 60, s.getRow(7).getCell(0).getNumericCellValue(), 0);

            wb.close();
        } finally {
            LocaleUtil.setUserTimeZone(userTimeZone);
        }
    }


    /**
     * Mixture of Ascii and Unicode strings in a
     * NameComment record
     */
    @Test
    void bug51143() throws Exception {
        simpleTest("51143.xls", wb -> assertEquals(1, wb.getNumberOfSheets()));
    }

    /**
     * File with exactly 256 data blocks (+header block)
     * shouldn't break on POIFS loading
     */
    @Test
    void bug51461() throws Exception {
        byte[] data = HSSFITestDataProvider.instance.getTestDataFileContent("51461.xls");

        HSSFWorkbook wbPOIFS = new HSSFWorkbook(new POIFSFileSystem(
                new ByteArrayInputStream(data)).getRoot(), false);
        HSSFWorkbook wbPOIFS2 = new HSSFWorkbook(new POIFSFileSystem(
                new ByteArrayInputStream(data)).getRoot(), false);

        assertEquals(2, wbPOIFS.getNumberOfSheets());
        assertEquals(2, wbPOIFS2.getNumberOfSheets());
    }

    @Test
    void bug51535() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("51535.xls")) {
            assertEquals(3, wb.getNumberOfSheets());

            // Check directly
            HSSFSheet s = wb.getSheetAt(0);
            assertEquals("Top Left Cell", s.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Top Right Cell", s.getRow(0).getCell(255).getStringCellValue());
            assertEquals("Bottom Left Cell", s.getRow(65535).getCell(0).getStringCellValue());
            assertEquals("Bottom Right Cell", s.getRow(65535).getCell(255).getStringCellValue());

            // Extract and check
            try (ExcelExtractor ex = new ExcelExtractor(wb)) {
                String text = ex.getText();
                assertContains(text, "Top Left Cell");
                assertContains(text, "Top Right Cell");
                assertContains(text, "Bottom Left Cell");
                assertContains(text, "Bottom Right Cell");
            }
        }
    }

    /**
     * Sum across multiple workbooks
     * eg =SUM($Sheet2.A1:$Sheet3.A1)
     */
    @Test
    void test48703() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("48703.xls")) {
            assertEquals(3, wb.getNumberOfSheets());

            // Check reading the formula
            Sheet sheet = wb.getSheetAt(0);
            Row r = sheet.getRow(0);
            Cell c = r.getCell(0);

            assertEquals("SUM(Sheet2:Sheet3!A1)", c.getCellFormula());
            assertEquals(4.0, c.getNumericCellValue(), 0);

            // Check the evaluated result
            HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb);
            eval.evaluateFormulaCell(c);
            assertEquals(4.0, c.getNumericCellValue(), 0);
        }
    }

    @Test
    void bug49896() throws Exception {
        String exp = "VLOOKUP(A2,'[C:Documents and Settings/Yegor/My Documents/csco.xls]Sheet1'!$A$2:$B$3,2,FALSE)"
            .replace("/", File.separator);
        simpleTest("49896.xls", wb -> assertEquals(exp, wb.getSheetAt(0).getRow(1).getCell(1).getCellFormula()));
    }

    @Test
    void bug49529() throws Exception {
        // user code reported in Bugzilla #49529
        simpleTest("49529.xls", wb -> {
            wb.getSheetAt(0).createDrawingPatriarch();
            // prior to the fix the line below failed with
            // java.lang.IllegalStateException: EOF - next record not available
            wb.cloneSheet(0);
        });
    }

    @Test
    void bug49612_part() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("49612.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            HSSFRow row = sh.getRow(0);
            HSSFCell c1 = row.getCell(2);
            HSSFCell d1 = row.getCell(3);
            HSSFCell e1 = row.getCell(2);

            assertEquals("SUM(BOB+JIM)", c1.getCellFormula());

            // Problem 1: See TestUnfixedBugs#test49612()
            // Problem 2: TestUnfixedBugs#test49612()

            // Problem 3: These used to fail, now pass
            HSSFFormulaEvaluator eval = new HSSFFormulaEvaluator(wb);
            assertEquals(30.0, eval.evaluate(c1).getNumberValue(), 0.001, "evaluating c1");
            assertEquals(30.0, eval.evaluate(d1).getNumberValue(), 0.001, "evaluating d1");
            assertEquals(30.0, eval.evaluate(e1).getNumberValue(), 0.001, "evaluating e1");
        }
    }

    @Test
    void bug51675() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("51675.xls")) {
            HSSFSheet sh = wb.getSheetAt(0);
            InternalSheet ish = HSSFTestHelper.getSheetForTest(sh);
            PageSettingsBlock psb = (PageSettingsBlock) ish.getRecords().get(13);
            List<Short> list = new ArrayList<>();
            psb.visitContainedRecords(r -> list.add(r.getSid()));
            assertEquals(UnknownRecord.BITMAP_00E9, list.get(list.size() - 1).intValue());
            assertEquals(UnknownRecord.HEADER_FOOTER_089C, list.get(list.size() - 2).intValue());
        }
    }

    @Test
    void bug52272() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sh = wb.createSheet();
            HSSFPatriarch p = sh.createDrawingPatriarch();

            HSSFSimpleShape s = p.createSimpleShape(new HSSFClientAnchor());
            s.setShapeType(HSSFSimpleShape.OBJECT_TYPE_LINE);

            HSSFSheet sh2 = wb.cloneSheet(0);
            assertNotNull(sh2.getDrawingPatriarch());
        }
    }

    @Test
    void bug53432() throws IOException {
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            wb1.addPicture(new byte[]{123, 22}, Workbook.PICTURE_TYPE_JPEG);
            assertEquals(wb1.getAllPictures().size(), 1);
        }
        try (HSSFWorkbook wb1 = new HSSFWorkbook()) {
            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                assertEquals(wb2.getAllPictures().size(), 0);
                wb2.addPicture(new byte[]{123, 22}, Workbook.PICTURE_TYPE_JPEG);
                assertEquals(wb2.getAllPictures().size(), 1);

                try (HSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    assertEquals(wb3.getAllPictures().size(), 1);
                }
            }
        }
    }

    @Test
    void bug46250() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("46250.xls")) {
            Sheet sh = wb.getSheet("Template");
            Sheet cSh = wb.cloneSheet(wb.getSheetIndex(sh));
            int sIdx = wb.getSheetIndex(cSh);

            HSSFPatriarch patriarch = (HSSFPatriarch) cSh.createDrawingPatriarch();
            HSSFTextbox tb = (HSSFTextbox) patriarch.getChildren().get(2);

            tb.setString(new HSSFRichTextString("POI test"));
            tb.setAnchor(new HSSFClientAnchor(0, 0, 0, 0, (short) 0, 0, (short) 10, 10));

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                HSSFSheet sh2 = wb2.getSheetAt(sIdx);
                assertNotNull(sh2);
                assertNotNull(sh2.getDrawingPatriarch());
            }
        }
    }

    @Test
    void bug53404() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("53404.xls")) {
            Sheet sheet = wb.getSheet("test-sheet");
            int rowCount = sheet.getLastRowNum() + 1;
            int newRows = 5;
            Calendar cal = LocaleUtil.getLocaleCalendar();
            for (int r = rowCount; r < rowCount + newRows; r++) {
                Row row = sheet.createRow((short) r);
                row.createCell(0).setCellValue(1.03 * (r + 7));
                row.createCell(1).setCellValue(cal.getTime());
                row.createCell(2).setCellValue(cal);
                row.createCell(3).setCellValue(String.format(Locale.ROOT, "row:%d/col:%d", r, 3));
                row.createCell(4).setCellValue(true);
                row.createCell(5).setCellErrorValue(FormulaError.NUM.getCode());
                row.createCell(6).setCellValue("added cells.");
            }

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb)) {
                Sheet sheet2 = wb2.getSheet("test-sheet");
                Row row2 = sheet2.getRow(5);
                assertNotNull(row2);
                assertEquals(cal.getTime(),row2.getCell(2).getDateCellValue());
            }
        }
    }

    /**
     * Row style information is 12 not 16 bits
     */
    @Test
    void bug49237() throws Exception {
        try (Workbook wb = openSampleWorkbook("49237.xls")) {
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);
            CellStyle rstyle = row.getRowStyle();
            assertNotNull(rstyle);
            assertEquals(BorderStyle.DOUBLE, rstyle.getBorderBottom());
        }
    }

    /**
     * POI does now support the RC4 CryptoAPI encryption header structure
     */
    @ParameterizedTest
    @CsvSource({
        "xor-encryption-abc.xls, abc",
        "35897-type4.xls, freedom"
    })
    void bug35897(String file, String pass) throws Exception {
        Biff8EncryptionKey.setCurrentUserPassword(pass);
        try {
            simpleTest(file, null);
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    @Test
    void bug56450() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("56450.xls")) {
            HSSFSheet sheet = wb.getSheetAt(0);
            int comments = 0;
            for (Row r : sheet) {
                for (Cell c : r) {
                    if (c.getCellComment() != null) {
                        assertNotNull(c.getCellComment().getString().getString());
                        comments++;
                    }
                }
            }
            assertEquals(0, comments);
        }
    }

    /**
     * Files initially created with Excel 2010 can have >3 CF rules
     */
    @Test
    void bug56482() throws Exception {
        try (HSSFWorkbook wb = openSampleWorkbook("56482.xls")) {
            assertEquals(1, wb.getNumberOfSheets());

            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFSheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();

            assertEquals(5, cf.getNumConditionalFormattings());
        }
    }

    @Test
    void bug56325() throws IOException {
        try (HSSFWorkbook wb1 = openSampleWorkbook("56325.xls")) {
            assertEquals(3, wb1.getNumberOfSheets());
            wb1.removeSheetAt(0);
            assertEquals(2, wb1.getNumberOfSheets());

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                assertEquals(2, wb2.getNumberOfSheets());
                wb2.removeSheetAt(0);
                assertEquals(1, wb2.getNumberOfSheets());
                wb2.removeSheetAt(0);
                assertEquals(0, wb2.getNumberOfSheets());

                try (HSSFWorkbook wb3 = writeOutAndReadBack(wb2)) {
                    assertEquals(0, wb3.getNumberOfSheets());
                }
            }
        }
    }

    @Test
    void bug56325a() throws IOException {
        try (HSSFWorkbook wb1 = openSampleWorkbook("56325a.xls")) {

            HSSFSheet sheet = wb1.cloneSheet(2);
            wb1.setSheetName(3, "Clone 1");
            sheet.setRepeatingRows(CellRangeAddress.valueOf("2:3"));
            wb1.setPrintArea(3, "$A$4:$C$10");

            sheet = wb1.cloneSheet(2);
            wb1.setSheetName(4, "Clone 2");
            sheet.setRepeatingRows(CellRangeAddress.valueOf("2:3"));
            wb1.setPrintArea(4, "$A$4:$C$10");

            wb1.removeSheetAt(2);

            try (Workbook wb2 = writeOutAndReadBack(wb1)) {
                assertEquals(4, wb2.getNumberOfSheets());
            }
        }
    }

    /**
     * Formulas which reference named ranges, either in other
     * sheets, or workbook scoped but in other workbooks.
     * Used to fail with
     * java.lang.RuntimeException: Unexpected eval class (org.apache.poi.ss.formula.eval.NameXEval)
     */
    @Test
    void bug56737() throws IOException {
        try (Workbook wb = openSampleWorkbook("56737.xls")) {

            // Check the named range definitions
            Name nSheetScope = wb.getName("NR_To_A1");
            Name nWBScope = wb.getName("NR_Global_B2");

            assertNotNull(nSheetScope);
            assertNotNull(nWBScope);

            assertEquals("Defines!$A$1", nSheetScope.getRefersToFormula());
            assertEquals("Defines!$B$2", nWBScope.getRefersToFormula());

            // Check the different kinds of formulas
            Sheet s = wb.getSheetAt(0);
            Cell cRefSName = s.getRow(1).getCell(3);
            Cell cRefWName = s.getRow(2).getCell(3);

            assertEquals("Defines!NR_To_A1", cRefSName.getCellFormula());

            // TODO Correct this, so that the filename is shown too, see bug #56742
            // This is what Excel itself shows
            //assertEquals("'56737.xls'!NR_Global_B2", cRefWName.getCellFormula());
            // TODO This isn't right, but it's what we currently generate....
            assertEquals("NR_Global_B2", cRefWName.getCellFormula());

            // Try to evaluate them
            FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
            assertEquals("Test A1", eval.evaluate(cRefSName).getStringValue());
            assertEquals(142, (int) eval.evaluate(cRefWName).getNumberValue());

            // Try to evaluate everything
            eval.evaluateAll();
        }
    }

    /**
     * ClassCastException in HSSFOptimiser - StyleRecord cannot be cast to
     * ExtendedFormatRecord when removing un-used styles
     */
    @Test
    void bug54443() throws Exception {
        try (HSSFWorkbook workbook = new HSSFWorkbook()) {
            HSSFCellStyle style = workbook.createCellStyle();
            HSSFCellStyle newStyle = workbook.createCellStyle();

            HSSFSheet mySheet = workbook.createSheet();
            HSSFRow row = mySheet.createRow(0);
            HSSFCell cell = row.createCell(0);

            // Use style
            cell.setCellStyle(style);
            // Switch to newStyle, style is now un-used
            cell.setCellStyle(newStyle);

            // Optimize
            int nbrOfCellStyles = workbook.getNumCellStyles();
            HSSFOptimiser.optimiseCellStyles(workbook);
            assertEquals(nbrOfCellStyles-1, workbook.getNumCellStyles());
        }
    }

    /**
     * Intersection formula ranges, eg =(C2:D3 D3:E4)
     */
    @Test
    void bug52111() throws Exception {
        try (Workbook wb = openSampleWorkbook("Intersection-52111.xls")) {
            Sheet s = wb.getSheetAt(0);
            assertFormula(wb, s.getRow(2).getCell(0), "(C2:D3 D3:E4)", "4.0");
            assertFormula(wb, s.getRow(6).getCell(0), "Tabelle2!E:E Tabelle2!$A11:$IV11", "5.0");
            assertFormula(wb, s.getRow(8).getCell(0), "Tabelle2!E:F Tabelle2!$A11:$IV12", null);
        }
    }

    @Test
    void bug42016() throws Exception {
        try (Workbook wb = openSampleWorkbook("42016.xls")) {
            Sheet s = wb.getSheetAt(0);
            for (int row = 0; row < 7; row++) {
                assertEquals("A$1+B$1", s.getRow(row).getCell(2).getCellFormula());
            }
        }
    }

    /**
     * Unexpected record type (org.apache.poi.hssf.record.ColumnInfoRecord)
     */
    @Test
    void bug53984() throws Exception {
        try (Workbook wb = openSampleWorkbook("53984.xls")) {
            Sheet s = wb.getSheetAt(0);
            assertEquals("International Communication Services SA", s.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Saudi Arabia-Riyadh", s.getRow(210).getCell(0).getStringCellValue());
        }
    }

    /**
     * Read, write, read for formulas point to cells in other files.
     * See {@link #bug46670()} for the main test, this just
     * covers reading an existing file and checking it.
     *
     * See base-test-class for some related tests that still fail
     */
    @Test
    void bug46670_existing() throws Exception {
        // Expected values
        String refLocal = "'[refs" + File.separator + "airport.xls]Sheet1'!$A$2";
        String refHttp = "'[9http://www.principlesofeconometrics.com/excel/airline.xls]Sheet1'!$A$2";

        // Check we can read them correctly
        simpleTest("46670_local.xls",
               wb -> assertEquals(refLocal, wb.getSheetAt(0).getRow(0).getCell(0).getCellFormula()));

        simpleTest("46670_http.xls",
               wb -> assertEquals(refHttp, wb.getSheetAt(0).getRow(0).getCell(0).getCellFormula()));

        // Now try to set them to the same values, and ensure that
        //  they end up as they did before, even with a save and re-load
        try (HSSFWorkbook wb3 = openSampleWorkbook("46670_local.xls")) {
            Sheet s = wb3.getSheetAt(0);
            Cell c = s.getRow(0).getCell(0);
            c.setCellFormula(refLocal);
            assertEquals(refLocal, c.getCellFormula());

            try (HSSFWorkbook wb4 = writeOutAndReadBack(wb3)) {
                s = wb4.getSheetAt(0);
                assertEquals(refLocal, s.getRow(0).getCell(0).getCellFormula());
            }
        }

        try (HSSFWorkbook wb5 = openSampleWorkbook("46670_http.xls")) {
            Sheet s = wb5.getSheetAt(0);
            Cell c = s.getRow(0).getCell(0);
            c.setCellFormula(refHttp);
            assertEquals(refHttp, c.getCellFormula());

            try (Workbook wb6 = writeOutAndReadBack(wb5)) {
                s = wb6.getSheetAt(0);
                assertEquals(refHttp, s.getRow(0).getCell(0).getCellFormula());
            }
        }
    }

    @Test
    void test57163() throws IOException {
        simpleTest("57163.xls", wb -> {
            while (wb.getNumberOfSheets() > 1) {
                wb.removeSheetAt(1);
            }
        });
    }

    @Test
    void test48043() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("56325a.xls")) {

            wb.removeSheetAt(2);
            wb.removeSheetAt(1);

            //Sheet s = wb.createSheet("sheetname");
            Sheet s = wb.getSheetAt(0);
            Row row = s.createRow(0);
            Cell cell = row.createCell(0);

            cell.setCellFormula(
                "IF(AND(ISBLANK(A10)," +
                "ISBLANK(B10)),\"\"," +
                "CONCATENATE(A10,\"-\",B10))");

            FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();

            eval.evaluateAll();
            try (Workbook wbBack = writeOutAndReadBack(wb)) {
                assertNotNull(wbBack);
            }
        }
    }

    @Test
    void test57925() throws IOException {
        try (Workbook wb = openSampleWorkbook("57925.xls")) {
            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
            DataFormatter df = new DataFormatter();
            for (Sheet sheet : wb) {
                for (Row row : sheet) {
                    for (Cell cell : row) {
                        assertNotNull(df.formatCellValue(cell));
                    }
                }
            }
        }
    }

    @Test
    void test46515() throws IOException {
        try (Workbook wb = openSampleWorkbook("46515.xls")) {

            // Get structure from webservice
            String urlString = "https://poi.apache.org/components/spreadsheet/images/calendar.jpg";
            URL structURL = new URL(urlString);
            BufferedImage bimage;
            try {
                bimage = ImageIO.read(structURL);
            } catch (IOException e) {
                Assumptions.assumeFalse(true, "Downloading a jpg from poi.apache.org should work");
                return;
            }

            // Convert BufferedImage to byte[]
            ByteArrayOutputStream imageBAOS = new ByteArrayOutputStream();
            ImageIO.write(bimage, "jpeg", imageBAOS);
            imageBAOS.flush();
            byte[] imageBytes = imageBAOS.toByteArray();
            imageBAOS.close();

            // Pop structure into Structure HSSFSheet
            int pict = wb.addPicture(imageBytes, HSSFWorkbook.PICTURE_TYPE_JPEG);
            Sheet sheet = wb.getSheet("Structure");
            assertNotNull(sheet, "Did not find sheet");
            HSSFPatriarch patriarch = (HSSFPatriarch) sheet.createDrawingPatriarch();
            HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) 1, 1, (short) 10, 22);
            anchor.setAnchorType(AnchorType.MOVE_DONT_RESIZE);
            patriarch.createPicture(anchor, pict);
        }
    }

    @Test
    void test55668() throws IOException {
        try (Workbook wb = openSampleWorkbook("55668.xls")) {

            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);
            Cell cell = row.getCell(0);
            assertEquals(CellType.FORMULA, cell.getCellType());
            assertEquals("IF(TRUE,\"\",\"\")", cell.getCellFormula());
            assertEquals("", cell.getStringCellValue());
            //noinspection deprecation
            cell.setCellType(CellType.STRING);

            assertEquals(CellType.BLANK, cell.getCellType());
            assertEquals("", cell.getStringCellValue());

            assertThrows(IllegalStateException.class, cell::getCellFormula);
        }
    }

    @Test
    void test55982() throws IOException {
        simpleTest("55982.xls", wb -> assertNotNull(wb.cloneSheet(1)));
    }

    /**
     * Test generator of ids for the CommonObjectDataSubRecord record.
     */
    @Test
    void test51332() {
        HSSFClientAnchor anchor = new HSSFClientAnchor();
        HSSFSimpleShape shape;
        CommonObjectDataSubRecord cmo;

        shape = new HSSFTextbox(null, anchor);
        shape.setShapeId(1025);
        cmo = (CommonObjectDataSubRecord) shape.getObjRecord().getSubRecords().get(0);
        assertEquals(1, cmo.getObjectId());

        shape = new HSSFPicture(null, anchor);
        shape.setShapeId(1026);
        cmo = (CommonObjectDataSubRecord) shape.getObjRecord().getSubRecords().get(0);
        assertEquals(2, cmo.getObjectId());

        shape = new HSSFComment(null, anchor);
        shape.setShapeId(1027);
        cmo = (CommonObjectDataSubRecord) shape.getObjRecord().getSubRecords().get(0);
        assertEquals(1027, cmo.getObjectId());
    }

    // As of POI 3.15 beta 2, LibreOffice does not display the diagonal border while it does display the bottom border
    // I have not checked Excel to know if this is a LibreOffice or a POI problem.
    @Test
    void test53564() throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet("Page 1");
            final short BLUE = 30;

            HSSFSheetConditionalFormatting scf = sheet.getSheetConditionalFormatting();
            HSSFConditionalFormattingRule rule = scf.createConditionalFormattingRule(ComparisonOperator.GT, "10");

            HSSFBorderFormatting bord = rule.createBorderFormatting();
            bord.setBorderDiagonal(BorderStyle.THICK);
            assertEquals(BorderStyle.THICK, bord.getBorderDiagonal());

            bord.setBackwardDiagonalOn(true);
            assertTrue(bord.isBackwardDiagonalOn());

            bord.setForwardDiagonalOn(true);
            assertTrue(bord.isForwardDiagonalOn());

            bord.setDiagonalBorderColor(BLUE);
            assertEquals(BLUE, bord.getDiagonalBorderColor());

            // Create the bottom border style so we know what a border is supposed to look like
            bord.setBorderBottom(BorderStyle.THICK);
            assertEquals(BorderStyle.THICK, bord.getBorderBottom());
            bord.setBottomBorderColor(BLUE);
            assertEquals(BLUE, bord.getBottomBorderColor());

            CellRangeAddress[] A2_D4 = {new CellRangeAddress(1, 3, 0, 3)};
            scf.addConditionalFormatting(A2_D4, rule);

            // Set a cell value within the conditional formatting range whose rule would resolve to True.
            Cell C3 = sheet.createRow(2).createCell(2);
            C3.setCellValue(30.0);
        }
    }

    @Test
    void test61287() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("61287.xls");
            ExcelExtractor ex = new ExcelExtractor(wb)) {
            String text = ex.getText();
            assertContains(text, "\u8D44\u4EA7\u8D1F\u503A\u8868");
        }
    }

    @Test
    void test61300() throws Exception {
        try (POIFSFileSystem poifs = new POIFSFileSystem(HSSFTestDataSamples.openSampleFileStream("61300.xls"))) {

            DocumentEntry entry =
                    (DocumentEntry) poifs.getRoot().getEntry(SummaryInformation.DEFAULT_STREAM_NAME);

            RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> new PropertySet(new DocumentInputStream(entry))
            );
            assertEquals("Can't read negative number of bytes", ex.getMessage());
        }
    }

    @Test
    void test51262() throws IOException {
        try (HSSFWorkbook wb = openSampleWorkbook("51262.xls")) {
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(2);

            Cell cell = row.getCell(1);
            CellStyle style = cell.getCellStyle();
            assertEquals(26, style.getFontIndex());

            row = sheet.getRow(3);
            cell = row.getCell(1);
            style = cell.getCellStyle();
            assertEquals(28, style.getFontIndex());

            // check the two fonts
            HSSFFont font = wb.getFontAt(26);
            assertTrue(font.getBold());
            assertEquals(10, font.getFontHeightInPoints());
            assertEquals("\uFF2D\uFF33 \uFF30\u30B4\u30B7\u30C3\u30AF", font.getFontName());

            font = wb.getFontAt(28);
            assertTrue(font.getBold());
            assertEquals(10, font.getFontHeightInPoints());
            assertEquals("\uFF2D\uFF33 \uFF30\u30B4\u30B7\u30C3\u30AF", font.getFontName());
        }
    }

    @Test
    void test60460() throws IOException {
        try (final Workbook wb = openSampleWorkbook("60460.xls")) {
            assertEquals(2, wb.getAllNames().size());

            Name rangedName = wb.getAllNames().get(0);
            assertFalse(rangedName.isFunctionName());
            assertEquals("'[\\\\HEPPC3\\gt$\\Teaching\\Syn\\physyn.xls]#REF'!$AK$70:$AL$70",
                    // replace '/' to make test work equally on Windows and Linux
                    rangedName.getRefersToFormula().replace("/", "\\"));

            rangedName = wb.getAllNames().get(1);
            assertFalse(rangedName.isFunctionName());
            assertEquals("Questionnaire!$A$1:$L$65", rangedName.getRefersToFormula());
        }
    }

    @Test
    void test63819() throws IOException {
        LocaleUtil.setUserLocale(Locale.UK);
        try {
            simpleTest("63819.xls", null);
        } finally {
            LocaleUtil.resetUserLocale();
        }
    }

    /**
     * Test that VALUE behaves properly as array function and its result is handled by aggregate function
     */
    @Test
    void testValueAsArrayFunction() throws IOException {
        try (final Workbook wb = openSampleWorkbook("TestValueAsArrayFunction.xls")) {
            wb.getCreationHelper().createFormulaEvaluator().evaluateAll();
            Sheet sheet = wb.getSheetAt(0);
            Row row = sheet.getRow(0);
            Cell cell = row.getCell(0);
            assertEquals(6.0, cell.getNumericCellValue(), 0.0);
        }
    }

    // a simple test which rewrites the file once and evaluates its formulas
    @ParameterizedTest
    @CsvSource({
        "15228.xls", "13796.xls", "14460.xls", "14330-1.xls", "14330-2.xls", "22742.xls", "12561-1.xls", "12561-2.xls",
        "12843-1.xls", "12843-2.xls", "13224.xls", "19599-1.xls", "19599-2.xls", "32822.xls", "15573.xls",
        "33082.xls", "34775.xls", "37630.xls", "25183.xls", "26100.xls", "27933.xls", "29675.xls", "29982.xls",
        "31749.xls", "37376.xls", "SimpleWithAutofilter.xls", "44201.xls", "37684-1.xls", "37684-2.xls",
        "41139.xls", "ex42564-21435.xls", "ex42564-21503.xls", "28774.xls", "44891.xls", "44235.xls", "36947.xls",
        "39634.xls", "47701.xls", "48026.xls", "47251.xls", "47251_1.xls", "50020.xls", "50426.xls", "50779_1.xls",
        "50779_2.xls", "51670.xls", "54016.xls", "57456.xls", "53109.xls", "com.aida-tour.www_SPO_files_maldives%20august%20october.xls",
        "named-cell-in-formula-test.xls", "named-cell-test.xls", "bug55505.xls", "SUBSTITUTE.xls", "64261.xls"
    })
    void simpleTest(String fileName) throws IOException {
        simpleTest(fileName, null);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @CsvSource({
        "46904.xls, org.apache.poi.hssf.OldExcelFormatException, The supplied spreadsheet seems to be Excel",
        "51832.xls, org.apache.poi.EncryptedDocumentException, Default password is invalid for salt/verifier/verifierHash"
    })
    void simpleTest(String fileName, String exClazz, String exMessage) throws IOException, ClassNotFoundException {
        Class<? extends Exception> ex = (Class<? extends Exception>)Class.forName(exClazz);
        Exception e = assertThrows(ex, () -> simpleTest(fileName, null));
        assertTrue(e.getMessage().startsWith(exMessage));
    }

    private void simpleTest(String fileName, Consumer<HSSFWorkbook> addTest) throws IOException {
        try (HSSFWorkbook wb1 = openSampleWorkbook(fileName)) {
            if (addTest != null) {
                addTest.accept(wb1);
            }

            HSSFSheet s = wb1.getSheetAt(0);
            HSSFRow r = s.createRow(10_000);
            HSSFCell c = r.createCell(0);
            c.setCellValue(10);

            HSSFSheet tmpSheet = wb1.createSheet("POITESTSHEET");
            tmpSheet.createRow(10).createCell(10).setCellValue("Test");
            wb1.removeSheetAt(wb1.getSheetIndex(tmpSheet));

            simpleTestHelper(wb1, fileName);

            try (HSSFWorkbook wb2 = writeOutAndReadBack(wb1)) {
                if (addTest != null) {
                    addTest.accept(wb2);
                }

                double act = wb2.getSheetAt(0).getRow(10_000).getCell(0).getNumericCellValue();
                assertEquals(10, act, 0);
                simpleTestHelper(wb2, fileName);
            }
        }
    }

    private void simpleTestHelper(HSSFWorkbook wb, String fileName) {
        List<String> files = new ArrayList<>();
        files.add(fileName);
        files.addAll(SIMPLE_REFS.keySet());
        List<HSSFFormulaEvaluator> evals = new ArrayList<>();
        evals.add(wb.getCreationHelper().createFormulaEvaluator());
        evals.addAll(SIMPLE_REFS.values());

        try {
            HSSFFormulaEvaluator.setupEnvironment(files.toArray(new String[0]), evals.toArray(new HSSFFormulaEvaluator[0]));
            evals.get(0).evaluateAll();
        } catch (RuntimeException e) {
            throw new RuntimeException("While handling files " + files + " and evals " + evals, e);
        }
    }


}
