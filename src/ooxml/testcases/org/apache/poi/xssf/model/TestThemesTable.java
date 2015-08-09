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

package org.apache.poi.xssf.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.model.ThemesTable.ThemeElement;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

public class TestThemesTable {
    private String testFileSimple = "Themes.xlsx";
    private String testFileComplex = "Themes2.xlsx";
    // TODO .xls version available too, add HSSF support then check 
    
    // What colours they should show up as
    private static String rgbExpected[] = {
            "ffffff", // Lt1
            "000000", // Dk1
            "eeece1", // Lt2
            "1f497d", // DK2
            "4f81bd", // Accent1
            "c0504d", // Accent2
            "9bbb59", // Accent3
            "8064a2", // Accent4
            "4bacc6", // Accent5
            "f79646", // Accent6
            "0000ff", // Hlink
            "800080"  // FolHlink
    };

    @Test
    public void testThemesTableColors() throws Exception {
        // Load our two test workbooks
        XSSFWorkbook simple = XSSFTestDataSamples.openSampleWorkbook(testFileSimple);
        XSSFWorkbook complex = XSSFTestDataSamples.openSampleWorkbook(testFileComplex);
        // Save and re-load them, to check for stability across that
        XSSFWorkbook simpleRS = XSSFTestDataSamples.writeOutAndReadBack(simple);
        XSSFWorkbook complexRS = XSSFTestDataSamples.writeOutAndReadBack(complex);
        // Fetch fresh copies to test with
        simple = XSSFTestDataSamples.openSampleWorkbook(testFileSimple);
        complex = XSSFTestDataSamples.openSampleWorkbook(testFileComplex);
        // Files and descriptions
        Map<String,XSSFWorkbook> workbooks = new LinkedHashMap<String, XSSFWorkbook>();
        workbooks.put(testFileSimple, simple);
        workbooks.put("Re-Saved_" + testFileSimple, simpleRS);
        workbooks.put(testFileComplex, complex);
        workbooks.put("Re-Saved_" + testFileComplex, complexRS);
        
        // Sanity check
        assertEquals(rgbExpected.length, rgbExpected.length);
        
        // For offline testing
        boolean createFiles = false;
        
        // Check each workbook in turn, and verify that the colours
        //  for the theme-applied cells in Column A are correct
        for (String whatWorkbook : workbooks.keySet()) {
            XSSFWorkbook workbook = workbooks.get(whatWorkbook);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int startRN = 0;
            if (whatWorkbook.endsWith(testFileComplex)) startRN++;
            
            for (int rn=startRN; rn<rgbExpected.length+startRN; rn++) {
                XSSFRow row = sheet.getRow(rn);
                assertNotNull("Missing row " + rn + " in " + whatWorkbook, row);
                String ref = (new CellReference(rn, 0)).formatAsString();
                XSSFCell cell = row.getCell(0);
                assertNotNull(
                        "Missing cell " + ref + " in " + whatWorkbook, cell);

                int expectedThemeIdx = rn-startRN;
                ThemeElement themeElem = ThemeElement.byId(expectedThemeIdx);
                assertEquals(
                        "Wrong theme at " + ref + " in " + whatWorkbook,
                        themeElem.name.toLowerCase(), cell.getStringCellValue());

                // Fonts are theme-based in their colours
                XSSFFont font = cell.getCellStyle().getFont();
                CTColor ctColor = font.getCTFont().getColorArray(0);
                assertNotNull(ctColor);
                assertEquals(true, ctColor.isSetTheme());
                assertEquals(themeElem.idx, ctColor.getTheme());
                
                // Get the colour, via the theme
                XSSFColor color = font.getXSSFColor();
                // Theme colours aren't tinted
                assertEquals(false, color.hasTint());
                // Check the RGB part (no tint)
                assertEquals(
                        "Wrong theme colour " + themeElem.name + " on " + whatWorkbook,
                        rgbExpected[expectedThemeIdx], Hex.encodeHexString(color.getRGB()));
                long themeIdx = font.getCTFont().getColorArray(0).getTheme();
                assertEquals(
                        "Wrong theme index " + expectedThemeIdx + " on " + whatWorkbook,
                        expectedThemeIdx, themeIdx);
                
                if (createFiles) {
                    XSSFCellStyle cs = row.getSheet().getWorkbook().createCellStyle();
                    cs.setFillForegroundColor(color);
                    cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
                    row.createCell(1).setCellStyle(cs);
                }
            }
            
            if (createFiles) {
                FileOutputStream fos = new FileOutputStream("Generated_"+whatWorkbook);
                workbook.write(fos);
                fos.close();
            }
        }
    }

    /**
     * Ensure that, for a file with themes, we can correctly
     *  read both the themed and non-themed colours back
     */
    @Test
    public void themedAndNonThemedColours() {
        // TODO Implement this using Theme2.xls{x}
    }
    
    @Test
    @SuppressWarnings("resource")
    public void testAddNew() throws Exception {
        XSSFWorkbook wb = new XSSFWorkbook();
        wb.createSheet();
        assertEquals(null, wb.getTheme());
        
        StylesTable styles = wb.getStylesSource();
        assertEquals(null, styles.getTheme());
        
        styles.ensureThemesTable();
        
        assertNotNull(styles.getTheme());
        assertNotNull(wb.getTheme());
        
        wb = XSSFTestDataSamples.writeOutAndReadBack(wb);
        styles = wb.getStylesSource();
        assertNotNull(styles.getTheme());
        assertNotNull(wb.getTheme());
    }
}
