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

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.util.IOUtils;

import junit.framework.TestCase;

/**
 * Class to test row styling functionality
 *
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public final class TestRowStyle extends TestCase {

    /**
     * TEST NAME:  Test Write Sheet Font <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with fonts.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     */
    public void testWriteSheetFont() {
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFFont         fnt  = wb.createFont();
        HSSFCellStyle    cs   = wb.createCellStyle();

        fnt.setColor(HSSFFont.COLOR_RED);
        fnt.setBold(true);
        cs.setFont(fnt);
        for (int rownum = 0; rownum < 100; rownum++)
        {
            r = s.createRow(rownum);
            r.setRowStyle(cs);
            r.createCell(0);
        }
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb);
        IOUtils.closeQuietly(wb);
        		
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb2);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());
        IOUtils.closeQuietly(wb2);
    }

    /**
     * Tests that is creating a file with a date or an calendar works correctly.
     */
    public void testDataStyle() {
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFCellStyle    cs   = wb.createCellStyle();
        HSSFRow row = s.createRow(0);

        // with Date:
        cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        row.setRowStyle(cs);
        row.createCell(0);


        // with Calendar:
        row = s.createRow(1);
        cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy"));
        row.setRowStyle(cs);
        row.createCell(0);

        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb);
        IOUtils.closeQuietly(wb);
        
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb2);

        assertEquals("LAST ROW ", 1, s.getLastRowNum());
        assertEquals("FIRST ROW ", 0, s.getFirstRowNum());
        IOUtils.closeQuietly(wb2);
    }

    /**
     * TEST NAME:  Test Write Sheet Style <P>
     * OBJECTIVE:  Test that HSSF can create a simple spreadsheet with numeric and string values and styled with colors
     *             and borders.<P>
     * SUCCESS:    HSSF creates a sheet.  Filesize matches a known good.  HSSFSheet objects
     *             Last row, first row is tested against the correct values (99,0).<P>
     * FAILURE:    HSSF does not create a sheet or excepts.  Filesize does not match the known good.
     *             HSSFSheet last row or first row is incorrect.             <P>
     */
    public void testWriteSheetStyle() {
        HSSFWorkbook     wb   = new HSSFWorkbook();
        HSSFSheet        s    = wb.createSheet();
        HSSFRow          r    = null;
        HSSFFont         fnt  = wb.createFont();
        HSSFCellStyle    cs   = wb.createCellStyle();
        HSSFCellStyle    cs2  = wb.createCellStyle();

        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setFillForegroundColor(( short ) 0xA);
        cs.setFillPattern(FillPatternType.BRICKS);
        fnt.setColor(( short ) 0xf);
        fnt.setItalic(true);
        cs2.setFillForegroundColor(( short ) 0x0);
        cs2.setFillPattern(FillPatternType.BRICKS);
        cs2.setFont(fnt);
        for (int rownum = 0; rownum < 100; rownum++)
        {
            r = s.createRow(rownum);
            r.setRowStyle(cs);
            r.createCell(0);

            rownum++;
            if (rownum >= 100) break; // I feel too lazy to check if this isreqd :-/ 
            
            r = s.createRow(rownum);
            r.setRowStyle(cs2);
            r.createCell(0);
        }
        HSSFWorkbook wb2 = HSSFTestDataSamples.writeOutAndReadBack(wb);
        IOUtils.closeQuietly(wb);
        
        SanityChecker sanityChecker = new SanityChecker();
        sanityChecker.checkHSSFWorkbook(wb2);
        assertEquals("LAST ROW == 99", 99, s.getLastRowNum());
        assertEquals("FIRST ROW == 0", 0, s.getFirstRowNum());
        
        s    = wb2.getSheetAt(0);
        assertNotNull("Sheet is not null", s);
        
        for (int rownum = 0; rownum < 100; rownum++)
        {
            r = s.getRow(rownum);
            assertNotNull("Row is not null", r);
            
            cs = r.getRowStyle();
            assertEquals("Bottom Border Style for row:", BorderStyle.THIN, cs.getBorderBottom());
            assertEquals("Left Border Style for row:",   BorderStyle.THIN, cs.getBorderLeft());
            assertEquals("Right Border Style for row:",  BorderStyle.THIN, cs.getBorderRight());
            assertEquals("Top Border Style for row:",    BorderStyle.THIN, cs.getBorderTop());
            assertEquals("FillForegroundColor for row:", 0xA, cs.getFillForegroundColor());
            assertEquals("FillPattern for row:",         FillPatternType.BRICKS, cs.getFillPattern());
            
            rownum++;
            if (rownum >= 100) break; // I feel too lazy to check if this isreqd :-/ 
            
            r = s.getRow(rownum);
            assertNotNull("Row is not null", r);
            cs2 = r.getRowStyle();
            assertEquals("FillForegroundColor for row: ", cs2.getFillForegroundColor(), (short) 0x0);
            assertEquals("FillPattern for row: ", cs2.getFillPattern(), FillPatternType.BRICKS);
        }
        IOUtils.closeQuietly(wb2);
    }
}
