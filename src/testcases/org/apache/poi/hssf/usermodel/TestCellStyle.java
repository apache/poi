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
import java.util.Calendar;
import java.util.Date;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.util.TempFile;

/**
 * Class to test cell styling functionality
 *
 * @author Andrew C. Oliver
 */

public final class TestCellStyle extends TestCase {

    private static HSSFWorkbook openSample(String sampleFileName) {
        return HSSFTestDataSamples.openSampleWorkbook(sampleFileName);
    }


    /**
     * TEST NAME:  Test Write Sheet Font <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with fonts.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */
    public void testWriteSheetFont() throws IOException{
        File             file = TempFile.createTempFile("testWriteSheetFont",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;
        HSSFFont         fnt  = wb.createFont();
        HSSFCellStyle    cs   = wb.createCellStyle();

        fnt.setColor(HSSFFont.COLOR_RED);
        fnt.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        cs.setFont(fnt);
        for (int rownum = 0; rownum < 100; rownum++) {
            r = s.createRow(rownum);

            for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c = r.createCell(cellnum + 1);
                c.setCellValue("TEST");
                c.setCellStyle(cs);
            }
        }
        wb.write(out);
        out.close();
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());

        // assert((s.getLastRowNum() == 99));
    }

    /**
     * Tests that is creating a file with a date or an calendar works correctly.
     */
    public void testDataStyle() throws IOException {
        File             file = TempFile.createTempFile("testWriteSheetStyleDate",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFCellStyle    cs   = wb.createCellStyle();
        HSSFRow row = s.createRow(0);

        // with Date:
        HSSFCell cell = row.createCell(1);
        cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        cell.setCellStyle(cs);
        cell.setCellValue(new Date());

        // with Calendar:
        cell = row.createCell(2);
        cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        cell.setCellStyle(cs);
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cell.setCellValue(cal);

        wb.write(out);
        out.close();
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb);

        assertEquals("LAST ROW ", 0, s.getLastRowNum());
        assertEquals("FIRST ROW ", 0, s.getFirstRowNum());
    }
    
    public void testHashEquals() {
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFCellStyle    cs1  = wb.createCellStyle();
        HSSFCellStyle    cs2  = wb.createCellStyle();
        HSSFRow row = s.createRow(0);
        HSSFCell cell1 = row.createCell(1);
        HSSFCell cell2 = row.createCell(2);
        
        cs1.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        cs2.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/dd/yy"));
        
        cell1.setCellStyle(cs1);
        cell1.setCellValue(new Date());
        
        cell2.setCellStyle(cs2);
        cell2.setCellValue(new Date());
        
        assertEquals(cs1.hashCode(), cs1.hashCode());
        assertEquals(cs2.hashCode(), cs2.hashCode());
        assertTrue(cs1.equals(cs1));
        assertTrue(cs2.equals(cs2));
        
        // Change cs1, hash will alter
        int hash1 = cs1.hashCode();
        cs1.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/dd/yy"));
        assertFalse(hash1 == cs1.hashCode());
    }

    /**
     * TEST NAME:  Test Write Sheet Style <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with colors
     *             and borders.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     *
     */
    public void testWriteSheetStyle() throws IOException {
        File             file = TempFile.createTempFile("testWriteSheetStyle",
                                                    ".xls");
        FileOutputStream out  = new FileOutputStream(file);
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFCell         c    = null;
        HSSFFont         fnt  = wb.createFont();
        HSSFCellStyle    cs   = wb.createCellStyle();
        HSSFCellStyle    cs2  = wb.createCellStyle();

        cs.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cs.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cs.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cs.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cs.setFillForegroundColor(( short ) 0xA);
        cs.setFillPattern(( short ) 1);
        fnt.setColor(( short ) 0xf);
        fnt.setItalic(true);
        cs2.setFillForegroundColor(( short ) 0x0);
        cs2.setFillPattern(( short ) 1);
        cs2.setFont(fnt);
        for (int rownum = 0; rownum < 100; rownum++) {
            r = s.createRow(rownum);

            for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                c = r.createCell(cellnum);
                c.setCellValue(rownum * 10000 + cellnum
                               + ((( double ) rownum / 1000)
                                  + (( double ) cellnum / 10000)));
                c.setCellStyle(cs);
                c = r.createCell(cellnum + 1);
                c.setCellValue("TEST");
                c.setCellStyle(cs2);
            }
        }
        wb.write(out);
        out.close();
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());
    }
    
    /**
     * Cloning one HSSFCellStyle onto Another, same
     *  HSSFWorkbook
     */
    public void testCloneStyleSameWB() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFFont fnt = wb.createFont();
        fnt.setFontName("TestingFont");
        assertEquals(5, wb.getNumberOfFonts());
        
        HSSFCellStyle orig = wb.createCellStyle();
        orig.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        orig.setFont(fnt);
        orig.setDataFormat((short)18);
        
        assertTrue(HSSFCellStyle.ALIGN_RIGHT == orig.getAlignment());
        assertTrue(fnt == orig.getFont(wb));
        assertTrue(18 == orig.getDataFormat());
        
        HSSFCellStyle clone = wb.createCellStyle();
        assertFalse(HSSFCellStyle.ALIGN_RIGHT == clone.getAlignment());
        assertFalse(fnt == clone.getFont(wb));
        assertFalse(18 == clone.getDataFormat());
        
        clone.cloneStyleFrom(orig);
        assertTrue(HSSFCellStyle.ALIGN_RIGHT == clone.getAlignment());
        assertTrue(fnt == clone.getFont(wb));
        assertTrue(18 == clone.getDataFormat());
        assertEquals(5, wb.getNumberOfFonts());
    }
    
    /**
     * Cloning one HSSFCellStyle onto Another, across
     *  two different HSSFWorkbooks
     */
    public void testCloneStyleDiffWB() {
        HSSFWorkbook wbOrig = new HSSFWorkbook();
        
        HSSFFont fnt = wbOrig.createFont();
        fnt.setFontName("TestingFont");
        assertEquals(5, wbOrig.getNumberOfFonts());
        
        HSSFDataFormat fmt = wbOrig.createDataFormat();
        fmt.getFormat("MadeUpOne");
        fmt.getFormat("MadeUpTwo");
        
        HSSFCellStyle orig = wbOrig.createCellStyle();
        orig.setAlignment(HSSFCellStyle.ALIGN_RIGHT);
        orig.setFont(fnt);
        orig.setDataFormat(fmt.getFormat("Test##"));
        
        assertTrue(HSSFCellStyle.ALIGN_RIGHT == orig.getAlignment());
        assertTrue(fnt == orig.getFont(wbOrig));
        assertTrue(fmt.getFormat("Test##") == orig.getDataFormat());
        
        // Now a style on another workbook
        HSSFWorkbook wbClone = new HSSFWorkbook();
        assertEquals(4, wbClone.getNumberOfFonts());
        HSSFDataFormat fmtClone = wbClone.createDataFormat();
        
        HSSFCellStyle clone = wbClone.createCellStyle();
        assertEquals(4, wbClone.getNumberOfFonts());
        
        assertFalse(HSSFCellStyle.ALIGN_RIGHT == clone.getAlignment());
        assertFalse("TestingFont" == clone.getFont(wbClone).getFontName());
        
        clone.cloneStyleFrom(orig);
        assertTrue(HSSFCellStyle.ALIGN_RIGHT == clone.getAlignment());
        assertTrue("TestingFont" == clone.getFont(wbClone).getFontName());
        assertTrue(fmtClone.getFormat("Test##") == clone.getDataFormat());
        assertFalse(fmtClone.getFormat("Test##") == fmt.getFormat("Test##"));
        assertEquals(5, wbClone.getNumberOfFonts());
    }
    
    public void testStyleNames() {
        HSSFWorkbook wb = openSample("WithExtendedStyles.xls");
        HSSFSheet s = wb.getSheetAt(0);
        HSSFCell c1 = s.getRow(0).getCell(0);
        HSSFCell c2 = s.getRow(1).getCell(0);
        HSSFCell c3 = s.getRow(2).getCell(0);
        
        HSSFCellStyle cs1 = c1.getCellStyle();
        HSSFCellStyle cs2 = c2.getCellStyle();
        HSSFCellStyle cs3 = c3.getCellStyle();
        
        assertNotNull(cs1);
        assertNotNull(cs2);
        assertNotNull(cs3);
        
        // Check we got the styles we'd expect
        assertEquals(10, cs1.getFont(wb).getFontHeightInPoints());
        assertEquals(9,  cs2.getFont(wb).getFontHeightInPoints());
        assertEquals(12, cs3.getFont(wb).getFontHeightInPoints());
        
        assertEquals(15, cs1.getIndex());
        assertEquals(23, cs2.getIndex());
        assertEquals(24, cs3.getIndex());
        
        assertNull(cs1.getParentStyle());
        assertNotNull(cs2.getParentStyle());
        assertNotNull(cs3.getParentStyle());
        
        assertEquals(21, cs2.getParentStyle().getIndex());
        assertEquals(22, cs3.getParentStyle().getIndex());
        
        // Now check we can get style records for 
        //  the parent ones
        assertNull(wb.getWorkbook().getStyleRecord(15));
        assertNull(wb.getWorkbook().getStyleRecord(23));
        assertNull(wb.getWorkbook().getStyleRecord(24));
        
        assertNotNull(wb.getWorkbook().getStyleRecord(21));
        assertNotNull(wb.getWorkbook().getStyleRecord(22));
        
        // Now check the style names
        assertEquals(null, cs1.getUserStyleName());
        assertEquals(null, cs2.getUserStyleName());
        assertEquals(null, cs3.getUserStyleName());
        assertEquals("style1", cs2.getParentStyle().getUserStyleName());
        assertEquals("style2", cs3.getParentStyle().getUserStyleName());
    }
}
