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

import java.awt.font.FontRenderContext;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.text.AttributedString;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

/**
 * A base class for bugzilla issues that can be described in terms of common ss interfaces.
 *
 * @author Yegor Kozlov
 */
public abstract class BaseTestBugzillaIssues {
    private static final POILogger logger = POILogFactory.getLogger(BaseTestBugzillaIssues.class);

    private static final String TEST_32 = "Some text with 32 characters to ";
    private static final String TEST_255 = "Some very long text that is exactly 255 characters, which are allowed here, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla.....";
    private static final String TEST_256 = "Some very long text that is longer than the 255 characters allowed in HSSF here, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla bla, bla1";
    private static final String TEST_SPECIAL_TITLE = "special \n\t\r\u0002characters";
    private static final String TEST_SPECIAL = "Some text with special \n\t\r\u0002characters to s";

    private final ITestDataProvider _testDataProvider;

    protected BaseTestBugzillaIssues(ITestDataProvider testDataProvider) {
        _testDataProvider = testDataProvider;
    }

    /**
     * Unlike org.junit.Assert.assertEquals(double expected, double actual, double delta),
     * where delta is an absolute error value, this function's factor is a relative error,
     * so it's easier to express "actual is within 5% of expected".
     */
    public static void assertAlmostEquals(double expected, double actual, float factor) {
        double diff = Math.abs(expected - actual);
        double fuzz = expected * factor;
        if (diff > fuzz) {
            fail(actual + " not within " + fuzz + " of " + expected);
        }
    }

    /**
     * Test writing a hyperlink
     * Open resulting sheet in Excel and check that A1 contains a hyperlink
     *
     * Also tests bug 15353 (problems with hyperlinks to Google)
     */
    @Test
    public final void bug23094() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet s = wb1.createSheet();
        Row r = s.createRow(0);
        r.createCell(0).setCellFormula("HYPERLINK(\"http://jakarta.apache.org\",\"Jakarta\")");
        r.createCell(1).setCellFormula("HYPERLINK(\"http://google.com\",\"Google\")");

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        r = wb2.getSheetAt(0).getRow(0);

        Cell cell_0 = r.getCell(0);
        assertEquals("HYPERLINK(\"http://jakarta.apache.org\",\"Jakarta\")", cell_0.getCellFormula());
        Cell cell_1 = r.getCell(1);
        assertEquals("HYPERLINK(\"http://google.com\",\"Google\")", cell_1.getCellFormula());

        wb2.close();
    }

    /**
     * test writing a file with large number of unique strings,
     * open resulting file in Excel to check results!
     * @param  num the number of strings to generate
     */
    public final void bug15375(int num) throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sheet = wb1.createSheet();
        CreationHelper factory = wb1.getCreationHelper();

        for (int i = 0; i < num; i++) {
            String tmp1 = "Test1" + i;
            String tmp2 = "Test2" + i;
            String tmp3 = "Test3" + i;

            Row row = sheet.createRow(i);

            Cell cell = row.createCell(0);
            cell.setCellValue(factory.createRichTextString(tmp1));
            cell = row.createCell(1);
            cell.setCellValue(factory.createRichTextString(tmp2));
            cell = row.createCell(2);
            cell.setCellValue(factory.createRichTextString(tmp3));
        }
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        sheet = wb2.getSheetAt(0);
        for (int i = 0; i < num; i++) {
            String tmp1 = "Test1" + i;
            String tmp2 = "Test2" + i;
            String tmp3 = "Test3" + i;

            Row row = sheet.getRow(i);

            assertEquals(tmp1, row.getCell(0).getStringCellValue());
            assertEquals(tmp2, row.getCell(1).getStringCellValue());
            assertEquals(tmp3, row.getCell(2).getStringCellValue());
        }
        wb2.close();
    }

    /**
     * Merged regions were being removed from the parent in cloned sheets
     */
    @Test
    public void bug22720() throws IOException {
       Workbook wb = _testDataProvider.createWorkbook();
       wb.createSheet("TEST");
       Sheet template = wb.getSheetAt(0);

       template.addMergedRegion(new CellRangeAddress(0, 1, 0, 2));
       template.addMergedRegion(new CellRangeAddress(2, 3, 0, 2));

       Sheet clone = wb.cloneSheet(0);
       int originalMerged = template.getNumMergedRegions();
       assertEquals("2 merged regions", 2, originalMerged);

       //remove merged regions from clone
       for (int i=template.getNumMergedRegions()-1; i>=0; i--) {
           clone.removeMergedRegion(i);
       }

       assertEquals("Original Sheet's Merged Regions were removed", originalMerged, template.getNumMergedRegions());
       //check if template's merged regions are OK
       if (template.getNumMergedRegions()>0) {
            // fetch the first merged region...EXCEPTION OCCURS HERE
            template.getMergedRegion(0);
       }

       wb.close();
    }

    @Test
    public final void bug28031() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sheet = wb1.createSheet();
        wb1.setSheetName(0, "Sheet1");

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        String formulaText =
            "IF(ROUND(A2*B2*C2,2)>ROUND(B2*D2,2),ROUND(A2*B2*C2,2),ROUND(B2*D2,2))";
        cell.setCellFormula(formulaText);

        assertEquals(formulaText, cell.getCellFormula());
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();

        cell = wb2.getSheetAt(0).getRow(0).getCell(0);
        assertEquals("IF(ROUND(A2*B2*C2,2)>ROUND(B2*D2,2),ROUND(A2*B2*C2,2),ROUND(B2*D2,2))", cell.getCellFormula());
        wb2.close();
    }

    /**
     * Bug 21334: "File error: data may have been lost" with a file
     * that contains macros and this formula:
     * {=SUM(IF(FREQUENCY(IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),""),IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),""))>0,1))}
     */
    @Test
    public final void bug21334() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sh = wb1.createSheet();
        Cell cell = sh.createRow(0).createCell(0);
        String formula = "SUM(IF(FREQUENCY(IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),\"\"),IF(LEN(V4:V220)>0,MATCH(V4:V220,V4:V220,0),\"\"))>0,1))";
        cell.setCellFormula(formula);

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        Cell cell_sv = wb2.getSheetAt(0).getRow(0).getCell(0);
        assertEquals(formula, cell_sv.getCellFormula());
        wb2.close();
    }

    /** another test for the number of unique strings issue
     *test opening the resulting file in Excel*/
    @Test
    public final void bug22568() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sheet = wb1.createSheet("ExcelTest") ;

        int col_cnt = 3;
        int rw_cnt = 2000;

        Row rw ;
        rw = sheet.createRow(0) ;
        //Header row
        for(int j=0; j<col_cnt; j++){
            Cell cell = rw.createCell(j) ;
            cell.setCellValue("Col " + (j+1));
        }

        for(int i=1; i<rw_cnt; i++){
            rw = sheet.createRow(i) ;
            for(int j=0; j<col_cnt; j++){
                Cell cell = rw.createCell(j) ;
                cell.setCellValue("Row:" + (i+1) + ",Column:" + (j+1));
            }
        }

        sheet.setDefaultColumnWidth(18) ;

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheetAt(0);
        rw = sheet.getRow(0);
        //Header row
        for(int j=0; j<col_cnt; j++){
            Cell cell = rw.getCell(j) ;
            assertEquals("Col " + (j+1), cell.getStringCellValue());
        }
        for(int i=1; i<rw_cnt; i++){
            rw = sheet.getRow(i) ;
            for(int j=0; j<col_cnt; j++){
                Cell cell = rw.getCell(j) ;
                assertEquals("Row:" + (i+1) + ",Column:" + (j+1), cell.getStringCellValue());
            }
        }
        wb2.close();
    }

    /**
     * Bug 42448: Can't parse SUMPRODUCT(A!C7:A!C67, B8:B68) / B69
     */
    @Test
    public final void bug42448() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Cell cell = wb.createSheet().createRow(0).createCell(0);
        cell.setCellFormula("SUMPRODUCT(A!C7:A!C67, B8:B68) / B69");
        assertTrue("no errors parsing formula", true);
        wb.close();
    }

    @Test
    public void bug18800() throws IOException {
       Workbook wb1 = _testDataProvider.createWorkbook();
       wb1.createSheet("TEST");
       Sheet sheet = wb1.cloneSheet(0);
       wb1.setSheetName(1,"CLONE");
       sheet.createRow(0).createCell(0).setCellValue("Test");

       Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
       wb1.close();
       sheet = wb2.getSheet("CLONE");
       Row row = sheet.getRow(0);
       Cell cell = row.getCell(0);
       assertEquals("Test", cell.getRichStringCellValue().getString());
       wb2.close();
   }

    private static void addNewSheetWithCellsA1toD4(Workbook book, int sheet) {

        Sheet sht = book .createSheet("s" + sheet);
        for (int r=0; r < 4; r++) {

            Row   row = sht.createRow (r);
            for (int c=0; c < 4; c++) {

                Cell cel = row.createCell(c);
                cel.setCellValue(sheet*100 + r*10 + c);
            }
        }
    }

    @Test
    public void bug43093() throws IOException {
        Workbook xlw = _testDataProvider.createWorkbook();

        addNewSheetWithCellsA1toD4(xlw, 1);
        addNewSheetWithCellsA1toD4(xlw, 2);
        addNewSheetWithCellsA1toD4(xlw, 3);
        addNewSheetWithCellsA1toD4(xlw, 4);

        Sheet s2   = xlw.getSheet("s2");
        Row   s2r3 = s2.getRow(3);
        Cell  s2E4 = s2r3.createCell(4);
        s2E4.setCellFormula("SUM(s3!B2:C3)");

        FormulaEvaluator eva = xlw.getCreationHelper().createFormulaEvaluator();
        double d = eva.evaluate(s2E4).getNumberValue();

        assertEquals(d, (311+312+321+322), 0.0000001);

        xlw.close();
    }

    @Test
    public void bug46729_testMaxFunctionArguments() throws IOException {
        String[] func = {"COUNT", "AVERAGE", "MAX", "MIN", "OR", "SUBTOTAL", "SKEW"};

        SpreadsheetVersion ssVersion = _testDataProvider.getSpreadsheetVersion();
        Workbook wb = _testDataProvider.createWorkbook();
        Cell cell = wb.createSheet().createRow(0).createCell(0);

        String fmla;
        for (String name : func) {

            fmla = createFunction(name, 5);
            cell.setCellFormula(fmla);

            fmla = createFunction(name, ssVersion.getMaxFunctionArgs());
            cell.setCellFormula(fmla);

            try {
                fmla = createFunction(name, ssVersion.getMaxFunctionArgs() + 1);
                cell.setCellFormula(fmla);
                fail("Expected FormulaParseException");
            } catch (RuntimeException e){
                 assertTrue(e.getMessage().startsWith("Too many arguments to function '"+name+"'"));
            }
        }
        wb.close();
    }

    private static String createFunction(String name, int maxArgs){
        StringBuilder fmla = new StringBuilder();
        fmla.append(name);
        fmla.append("(");
        for(int i=0; i < maxArgs; i++){
            if(i > 0) {
                fmla.append(',');
            }
            fmla.append("A1");
        }
        fmla.append(")");
        return fmla.toString();
    }

    @Test
    public final void bug50681_testAutoSize() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        BaseTestSheetAutosizeColumn.fixFonts(wb);
        Sheet sheet = wb.createSheet("Sheet1");
        _testDataProvider.trackAllColumnsForAutosizing(sheet);
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);

        String longValue = "www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, " +
                "www.hostname.com, www.hostname.com, www.hostname.com, www.hostname.com";

        cell0.setCellValue(longValue);

        // autoSize will fail if required fonts are not installed, skip this test then
        Font font = wb.getFontAt(cell0.getCellStyle().getFontIndex());
        Assume.assumeTrue("Cannot verify autoSizeColumn() because the necessary Fonts are not installed on this machine: " + font,
                SheetUtil.canComputeColumnWidth(font));

        assertEquals("Expecting no indentation in this test",
                0, cell0.getCellStyle().getIndention());
        assertEquals("Expecting no rotation in this test",
                0, cell0.getCellStyle().getRotation());

        // check computing size up to a large size
//        StringBuilder b = new StringBuilder();
//        for(int i = 0;i < longValue.length()*5;i++) {
//            b.append("w");
//            assertTrue("Had zero length starting at length " + i, computeCellWidthFixed(font, b.toString()) > 0);
//        }
        double widthManual = computeCellWidthManually(cell0, font);
        double widthBeforeCell = SheetUtil.getCellWidth(cell0, 8, null, false);
        double widthBeforeCol = SheetUtil.getColumnWidth(sheet, 0, false);

        String info = widthManual + "/" + widthBeforeCell + "/" + widthBeforeCol + "/" +
                        SheetUtil.canComputeColumnWidth(font) + "/" + computeCellWidthFixed(font, "1") + "/" + computeCellWidthFixed(font, "w") + "/" +
                        computeCellWidthFixed(font, "1w") + "/" + computeCellWidthFixed(font, "0000") + "/" + computeCellWidthFixed(font, longValue);
        assertTrue("Expected to have cell width > 0 when computing manually, but had " + info, widthManual > 0);
        assertTrue("Expected to have cell width > 0 BEFORE auto-size, but had " + info, widthBeforeCell > 0);
        assertTrue("Expected to have column width > 0 BEFORE auto-size, but had " + info, widthBeforeCol > 0);

        sheet.autoSizeColumn(0);

        double width = SheetUtil.getColumnWidth(sheet, 0, false);
        assertTrue("Expected to have column width > 0 AFTER auto-size, but had " + width, width > 0);
        width = SheetUtil.getCellWidth(cell0, 8, null, false);
        assertTrue("Expected to have cell width > 0 AFTER auto-size, but had " + width, width > 0);

        assertEquals(255*256, sheet.getColumnWidth(0)); // maximum column width is 255 characters
        sheet.setColumnWidth(0, sheet.getColumnWidth(0)); // Bug 50681 reports exception at this point
        wb.close();
    }
    
    @Test
    public final void bug51622_testAutoSizeShouldRecognizeLeadingSpaces() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        BaseTestSheetAutosizeColumn.fixFonts(wb);
        Sheet sheet = wb.createSheet();
        _testDataProvider.trackAllColumnsForAutosizing(sheet);
        Row row = sheet.createRow(0);
        Cell cell0 = row.createCell(0);
        Cell cell1 = row.createCell(1);
        Cell cell2 = row.createCell(2);
        
        cell0.setCellValue("Test Column AutoSize");
        cell1.setCellValue("         Test Column AutoSize");
        cell2.setCellValue("Test Column AutoSize         ");
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
        sheet.autoSizeColumn(2);
        
        int noWhitespaceColWidth = sheet.getColumnWidth(0);
        int leadingWhitespaceColWidth = sheet.getColumnWidth(1);
        int trailingWhitespaceColWidth = sheet.getColumnWidth(2);
        
        // Based on the amount of text and whitespace used, and the default font
        // assume that the cell with whitespace should be at least 20% wider than
        // the cell without whitespace. This number is arbitrary, but should be large
        // enough to guarantee that the whitespace cell isn't wider due to chance.
        // Experimentally, I calculated the ratio as 1.2478181, though this ratio may change
        // if the default font or margins change.
        final double expectedRatioThreshold = 1.2f;
        double leadingWhitespaceRatio = ((double) leadingWhitespaceColWidth)/noWhitespaceColWidth;
        double trailingWhitespaceRatio = ((double) leadingWhitespaceColWidth)/noWhitespaceColWidth;
        
        assertGreaterThan("leading whitespace is longer than no whitespace", leadingWhitespaceRatio, expectedRatioThreshold);
        assertGreaterThan("trailing whitespace is longer than no whitespace", trailingWhitespaceRatio, expectedRatioThreshold);
        assertEquals("cells with equal leading and trailing whitespace have equal width",
                leadingWhitespaceColWidth, trailingWhitespaceColWidth);
        
        wb.close();
    }
    
    /**
     * Test if a > b. Fails if false.
     */
    private void assertGreaterThan(String message, double a, double b) {
        if (a <= b) {
            String msg = "Expected: " + a + " > " + b;
            fail(message + ": " + msg);
        }
    }

    // FIXME: this function is a self-fulfilling prophecy: this test will always pass as long
    // as the code-under-test and the testcase code are written the same way (have the same bugs). 
    private double computeCellWidthManually(Cell cell0, Font font) {
        final FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);
        RichTextString rt = cell0.getRichStringCellValue();
        String[] lines = rt.getString().split("\\n");
        assertEquals(1, lines.length);
        String txt = lines[0] + "0";

        AttributedString str = new AttributedString(txt);
        copyAttributes(font, str, txt.length());

        // TODO: support rich text fragments
        /*if (rt.numFormattingRuns() > 0) {
        }*/

        TextLayout layout = new TextLayout(str.getIterator(), fontRenderContext);
        double frameWidth = getFrameWidth(layout);
        return ((frameWidth / 1) / 8);
    }
    
    private double getFrameWidth(TextLayout layout) {
        Rectangle2D bounds = layout.getBounds();
        return bounds.getX() + bounds.getWidth();
    }

    private double computeCellWidthFixed(Font font, String txt) {
        final FontRenderContext fontRenderContext = new FontRenderContext(null, true, true);
        AttributedString str = new AttributedString(txt);
        copyAttributes(font, str, txt.length());

        TextLayout layout = new TextLayout(str.getIterator(), fontRenderContext);
        return getFrameWidth(layout);
    }

    private static void copyAttributes(Font font, AttributedString str, int endIdx) {
        str.addAttribute(TextAttribute.FAMILY, font.getFontName(), 0, endIdx);
        str.addAttribute(TextAttribute.SIZE, (float)font.getFontHeightInPoints());
        if (font.getBold()) {
            str.addAttribute(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD, 0, endIdx);
        }
        if (font.getItalic() ) {
            str.addAttribute(TextAttribute.POSTURE, TextAttribute.POSTURE_OBLIQUE, 0, endIdx);
        }
        if (font.getUnderline() == Font.U_SINGLE ) {
            str.addAttribute(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON, 0, endIdx);
        }
    }

    /**
     * CreateFreezePane column/row order check
     */
    @Test
    public void bug49381() throws IOException {
       Workbook wb = _testDataProvider.createWorkbook();
       int colSplit = 1;
       int rowSplit = 2;
       int leftmostColumn = 3;
       int topRow = 4;

        Sheet s = wb.createSheet();

        // Populate
        for(int rn=0; rn<= topRow; rn++) {
           Row r = s.createRow(rn);
           for(int cn=0; cn<leftmostColumn; cn++) {
              Cell c = r.createCell(cn, CellType.NUMERIC);
              c.setCellValue(100*rn + cn);
           }
        }

        // Create the Freeze Pane
        s.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
        PaneInformation paneInfo = s.getPaneInformation();

        // Check it
        assertEquals(colSplit,       paneInfo.getVerticalSplitPosition());
        assertEquals(rowSplit,       paneInfo.getHorizontalSplitPosition());
        assertEquals(leftmostColumn, paneInfo.getVerticalSplitLeftColumn());
        assertEquals(topRow,         paneInfo.getHorizontalSplitTopRow());


        // Now a row only freezepane
        s.createFreezePane(0, 3);
        paneInfo = s.getPaneInformation();

        assertEquals(0,  paneInfo.getVerticalSplitPosition());
        assertEquals(3,  paneInfo.getHorizontalSplitPosition());
        assertEquals(0,  paneInfo.getVerticalSplitLeftColumn());
        assertEquals(3,  paneInfo.getHorizontalSplitTopRow());

        // Now a column only freezepane
        s.createFreezePane(4, 0);
        paneInfo = s.getPaneInformation();

        assertEquals(4,  paneInfo.getVerticalSplitPosition());
        assertEquals(0,  paneInfo.getHorizontalSplitPosition());
        assertEquals(4 , paneInfo.getVerticalSplitLeftColumn());
        assertEquals(0,  paneInfo.getHorizontalSplitTopRow());
        wb.close();
    }

    /**
     * Test hyperlinks
     * open resulting file in excel, and check that there is a link to Google
     */
    @Test
    public void bug15353() throws IOException {
        String hyperlinkF = "HYPERLINK(\"http://google.com\",\"Google\")";

        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet sheet = wb1.createSheet("My sheet");

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellFormula(hyperlinkF);

        assertEquals(hyperlinkF, cell.getCellFormula());

        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        sheet = wb2.getSheet("My Sheet");
        row = sheet.getRow(0);
        cell = row.getCell(0);

        assertEquals(hyperlinkF, cell.getCellFormula());
        wb2.close();
    }

    /**
     * HLookup and VLookup with optional arguments
     */
    @Test
    public void bug51024() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r1 = s.createRow(0);
        Row r2 = s.createRow(1);

        r1.createCell(0).setCellValue("v A1");
        r2.createCell(0).setCellValue("v A2");
        r1.createCell(1).setCellValue("v B1");

        Cell c = r1.createCell(4);

        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();

        c.setCellFormula("VLOOKUP(\"v A1\", A1:B2, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("VLOOKUP(\"v A1\", A1:B2, 1, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("VLOOKUP(\"v A1\", A1:B2, 1, )");
        assertEquals("v A1", eval.evaluate(c).getStringValue());


        c.setCellFormula("HLOOKUP(\"v A1\", A1:B2, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("HLOOKUP(\"v A1\", A1:B2, 1, 1)");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        c.setCellFormula("HLOOKUP(\"v A1\", A1:B2, 1, )");
        assertEquals("v A1", eval.evaluate(c).getStringValue());

        wb.close();
    }

    @Test
    public void stackoverflow23114397() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        DataFormat format = wb.getCreationHelper().createDataFormat();

        // How close the sizing should be, given that not all
        //  systems will have quite the same fonts on them
        float fontAccuracy = 0.22f;

        // x%
        CellStyle iPercent = wb.createCellStyle();
        iPercent.setDataFormat(format.getFormat("0%"));
        // x.x%
        CellStyle d1Percent = wb.createCellStyle();
        d1Percent.setDataFormat(format.getFormat("0.0%"));
        // x.xx%
        CellStyle d2Percent = wb.createCellStyle();
        d2Percent.setDataFormat(format.getFormat("0.00%"));

        Sheet s = wb.createSheet();
        _testDataProvider.trackAllColumnsForAutosizing(s);
        Row r1 = s.createRow(0);

        for (int i=0; i<3; i++) {
            r1.createCell(i, CellType.NUMERIC).setCellValue(0);
        }
        for (int i=3; i<6; i++) {
            r1.createCell(i, CellType.NUMERIC).setCellValue(1);
        }
        for (int i=6; i<9; i++) {
            r1.createCell(i, CellType.NUMERIC).setCellValue(0.12345);
        }
        for (int i=9; i<12; i++) {
            r1.createCell(i, CellType.NUMERIC).setCellValue(1.2345);
        }
        for (int i=0; i<12; i+=3) {
            r1.getCell(i).setCellStyle(iPercent);
            r1.getCell(i+1).setCellStyle(d1Percent);
            r1.getCell(i+2).setCellStyle(d2Percent);
        }
        for (int i=0; i<12; i++) {
            s.autoSizeColumn(i);
        }

        // Check the 0(.00)% ones
        assertAlmostEquals(980, s.getColumnWidth(0), fontAccuracy);
        assertAlmostEquals(1400, s.getColumnWidth(1), fontAccuracy);
        assertAlmostEquals(1700, s.getColumnWidth(2), fontAccuracy);

        // Check the 100(.00)% ones
        assertAlmostEquals(1500, s.getColumnWidth(3), fontAccuracy);
        assertAlmostEquals(1950, s.getColumnWidth(4), fontAccuracy);
        assertAlmostEquals(2225, s.getColumnWidth(5), fontAccuracy);

        // Check the 12(.34)% ones
        assertAlmostEquals(1225, s.getColumnWidth(6), fontAccuracy);
        assertAlmostEquals(1650, s.getColumnWidth(7), fontAccuracy);
        assertAlmostEquals(1950, s.getColumnWidth(8), fontAccuracy);

        // Check the 123(.45)% ones
        assertAlmostEquals(1500, s.getColumnWidth(9), fontAccuracy);
        assertAlmostEquals(1950, s.getColumnWidth(10), fontAccuracy);
        assertAlmostEquals(2225, s.getColumnWidth(11), fontAccuracy);

        wb.close();
    }

    /**
     * =ISNUMBER(SEARCH("AM",A1)) evaluation
     */
    @Test
    public void stackoverflow26437323() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r1 = s.createRow(0);
        Row r2 = s.createRow(1);

        // A1 is a number
        r1.createCell(0).setCellValue(1.1);
        // B1 is a string, with the wanted text in it
        r1.createCell(1).setCellValue("This is text with AM in it");
        // C1 is a string, with different text
        r1.createCell(2).setCellValue("This some other text");
        // D1 is a blank cell
        r1.createCell(3, CellType.BLANK);
        // E1 is null

        // A2 will hold our test formulas
        Cell cf = r2.createCell(0, CellType.FORMULA);


        // First up, check that TRUE and ISLOGICAL both behave
        cf.setCellFormula("TRUE()");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISLOGICAL(TRUE())");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISLOGICAL(4)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());


        // Now, check ISNUMBER / ISTEXT / ISNONTEXT
        cf.setCellFormula("ISNUMBER(A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(E1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());


        cf.setCellFormula("ISTEXT(A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISTEXT(B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISTEXT(C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISTEXT(D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISTEXT(E1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());


        cf.setCellFormula("ISNONTEXT(A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISNONTEXT(B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNONTEXT(C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNONTEXT(D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISNONTEXT(E1)");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue()); // Blank and Null the same


        // Next up, SEARCH on its own
        cf.setCellFormula("SEARCH(\"am\", A1)");
        cf = evaluateCell(wb, cf);
        assertEquals(FormulaError.VALUE.getCode(), cf.getErrorCellValue());

        cf.setCellFormula("SEARCH(\"am\", B1)");
        cf = evaluateCell(wb, cf);
        assertEquals(19, (int)cf.getNumericCellValue());

        cf.setCellFormula("SEARCH(\"am\", C1)");
        cf = evaluateCell(wb, cf);
        assertEquals(FormulaError.VALUE.getCode(), cf.getErrorCellValue());

        cf.setCellFormula("SEARCH(\"am\", D1)");
        cf = evaluateCell(wb, cf);
        assertEquals(FormulaError.VALUE.getCode(), cf.getErrorCellValue());


        // Finally, bring it all together
        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", A1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", B1))");
        cf = evaluateCell(wb, cf);
        assertEquals(true, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", C1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", D1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        cf.setCellFormula("ISNUMBER(SEARCH(\"am\", E1))");
        cf = evaluateCell(wb, cf);
        assertEquals(false, cf.getBooleanCellValue());

        wb.close();
    }

    private Cell evaluateCell(Workbook wb, Cell c) {
        Sheet s = c.getSheet();
        wb.getCreationHelper().createFormulaEvaluator().evaluateFormulaCell(c);
        return s.getRow(c.getRowIndex()).getCell(c.getColumnIndex());
    }

    /**
     * Should be able to write then read formulas with references
     *  to cells in other files, eg '[refs/airport.xls]Sheet1'!$A$2
     *  or 'http://192.168.1.2/[blank.xls]Sheet1'!$A$1 .
     * Additionally, if a reference to that file is provided, it should
     *  be possible to evaluate them too
     * TODO Fix this to evaluate for XSSF
     * TODO Fix this to work at all for HSSF
     */
    @Ignore("Fix this to evaluate for XSSF, Fix this to work at all for HSSF")
    @Test
    public void bug46670() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        Sheet s = wb1.createSheet();
        Row r1 = s.createRow(0);


        // References to try
        String ext = _testDataProvider.getStandardFileNameExtension();
        String refLocal = "'[test."+ext+"]Sheet1'!$A$2";
        String refHttp  = "'[http://example.com/test."+ext+"]Sheet1'!$A$2";
        String otherCellText = "In Another Workbook";


        // Create the references
        Cell c1 = r1.createCell(0, CellType.FORMULA);
        c1.setCellFormula(refLocal);

        Cell c2 = r1.createCell(1, CellType.FORMULA);
        c2.setCellFormula(refHttp);


        // Check they were set correctly
        assertEquals(refLocal, c1.getCellFormula());
        assertEquals(refHttp,  c2.getCellFormula());


        // Reload, and ensure they were serialised and read correctly
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        s = wb2.getSheetAt(0);
        r1 = s.getRow(0);

        c1 = r1.getCell(0);
        c2 = r1.getCell(1);
        assertEquals(refLocal, c1.getCellFormula());
        assertEquals(refHttp,  c2.getCellFormula());


        // Try to evaluate, without giving a way to get at the other file
        try {
            evaluateCell(wb2, c1);
            fail("Shouldn't be able to evaluate without the other file");
        } catch (Exception e) {
            // expected here
        }
        try {
            evaluateCell(wb2, c2);
            fail("Shouldn't be able to evaluate without the other file");
        } catch (Exception e) {
            // expected here
        }


        // Set up references to the other file
        Workbook wb3 = _testDataProvider.createWorkbook();
        wb3.createSheet().createRow(1).createCell(0).setCellValue(otherCellText);

        Map<String,FormulaEvaluator> evaluators = new HashMap<>();
        evaluators.put(refLocal, wb3.getCreationHelper().createFormulaEvaluator());
        evaluators.put(refHttp,  wb3.getCreationHelper().createFormulaEvaluator());

        FormulaEvaluator evaluator = wb2.getCreationHelper().createFormulaEvaluator();
        evaluator.setupReferencedWorkbooks(evaluators);


        // Try to evaluate, with the other file
        evaluator.evaluateFormulaCell(c1);
        evaluator.evaluateFormulaCell(c2);

        assertEquals(otherCellText, c1.getStringCellValue());
        assertEquals(otherCellText, c2.getStringCellValue());

        wb3.close();
        wb2.close();
    }

    @Test
    public void test56574OverwriteExistingRow() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();

        { // create the Formula-Cell
            Row row = sheet.createRow(0);
            Cell cell = row.createCell(0);
            cell.setCellFormula("A2");
        }

        { // check that it is there now
            Row row = sheet.getRow(0);

           /* CTCell[] cArray = ((XSSFRow)row).getCTRow().getCArray();
            assertEquals(1, cArray.length);*/

            Cell cell = row.getCell(0);
            assertEquals(CellType.FORMULA, cell.getCellType());
        }

        { // overwrite the row
            Row row = sheet.createRow(0);
            assertNotNull(row);
        }

        { // creating a row in place of another should remove the existing data,
            // check that the cell is gone now
            Row row = sheet.getRow(0);

            /*CTCell[] cArray = ((XSSFRow)row).getCTRow().getCArray();
            assertEquals(0, cArray.length);*/

            Cell cell = row.getCell(0);
            assertNull(cell);
        }

        // the calculation chain in XSSF is empty in a newly created workbook, so we cannot check if it is correctly updated
        /*assertNull(((XSSFWorkbook)wb).getCalculationChain());
        assertNotNull(((XSSFWorkbook)wb).getCalculationChain().getCTCalcChain());
        assertNotNull(((XSSFWorkbook)wb).getCalculationChain().getCTCalcChain().getCArray());
        assertEquals(0, ((XSSFWorkbook)wb).getCalculationChain().getCTCalcChain().getCArray().length);*/

        wb.close();
    }

    /**
     * With HSSF, if you create a font, don't change it, and
     *  create a 2nd, you really do get two fonts that you
     *  can alter as and when you want.
     * With XSSF, that wasn't the case, but this verifies
     *  that it now is again
     */
    @Test
    public void bug48718() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        int startingFonts = wb instanceof HSSFWorkbook ? 4 : 1;

        assertEquals(startingFonts, wb.getNumberOfFontsAsInt());

        // Get a font, and slightly change it
        Font a = wb.createFont();
        assertEquals(startingFonts+1, wb.getNumberOfFontsAsInt());
        a.setFontHeightInPoints((short)23);
        assertEquals(startingFonts+1, wb.getNumberOfFontsAsInt());

        // Get two more, unchanged
        /*Font b =*/ wb.createFont();
        assertEquals(startingFonts+2, wb.getNumberOfFontsAsInt());
        /*Font c =*/ wb.createFont();
        assertEquals(startingFonts+3, wb.getNumberOfFontsAsInt());
        
        wb.close();
    }

    @Test
    public void bug57430() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet1");

        Name name1 = wb.createName();
        name1.setNameName("FMLA");
        name1.setRefersToFormula("Sheet1!$B$3");
        wb.close();
    }

    @Test
    public void bug56981() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        CellStyle vertTop = wb.createCellStyle();
        vertTop.setVerticalAlignment(VerticalAlignment.TOP);
        CellStyle vertBottom = wb.createCellStyle();
        vertBottom.setVerticalAlignment(VerticalAlignment.BOTTOM);
        Sheet sheet = wb.createSheet("Sheet 1");
        Row row = sheet.createRow(0);
        Cell top = row.createCell(0);
        Cell bottom = row.createCell(1);
        top.setCellValue("Top");
        top.setCellStyle(vertTop); // comment this out to get all bottom-aligned
                                   // cells
        bottom.setCellValue("Bottom");
        bottom.setCellStyle(vertBottom);
        row.setHeightInPoints(85.75f); // make it obvious

        /*FileOutputStream out = new FileOutputStream("c:\\temp\\56981.xlsx");
        try {
            wb.write(out);
        } finally {
            out.close();
        }*/

        wb.close();
    }

    @Test
    public void test57973() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        CreationHelper factory = wb.getCreationHelper();

        Sheet sheet = wb.createSheet();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = factory.createClientAnchor();

        Cell cell0 = sheet.createRow(0).createCell(0);
        cell0.setCellValue("Cell0");

        Comment comment0 = drawing.createCellComment(anchor);
        RichTextString str0 = factory.createRichTextString("Hello, World1!");
        comment0.setString(str0);
        comment0.setAuthor("Apache POI");
        cell0.setCellComment(comment0);

        anchor = factory.createClientAnchor();
        anchor.setCol1(1);
        anchor.setCol2(1);
        anchor.setRow1(1);
        anchor.setRow2(1);
        Cell cell1 = sheet.createRow(3).createCell(5);
        cell1.setCellValue("F4");
        Comment comment1 = drawing.createCellComment(anchor);
        RichTextString str1 = factory.createRichTextString("Hello, World2!");
        comment1.setString(str1);
        comment1.setAuthor("Apache POI");
        cell1.setCellComment(comment1);

        Cell cell2 = sheet.createRow(2).createCell(2);
        cell2.setCellValue("C3");

        anchor = factory.createClientAnchor();
        anchor.setCol1(2);
        anchor.setCol2(2);
        anchor.setRow1(2);
        anchor.setRow2(2);

        Comment comment2 = drawing.createCellComment(anchor);
        RichTextString str2 = factory.createRichTextString("XSSF can set cell comments");
        //apply custom font to the text in the comment
        Font font = wb.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short)14);
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        str2.applyFont(font);

        comment2.setString(str2);
        comment2.setAuthor("Apache POI");
        comment2.setColumn(2);
        comment2.setRow(2);

        wb.close();
    }

    /**
     * Ensures that XSSF and HSSF agree with each other,
     *  and with the docs on when fetching the wrong
     *  kind of value from a Formula cell
     */
    @Test
    public void bug47815() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Row r = s.createRow(0);

        // Setup
        Cell cn = r.createCell(0, CellType.NUMERIC);
        cn.setCellValue(1.2);
        Cell cs = r.createCell(1, CellType.STRING);
        cs.setCellValue("Testing");

        Cell cfn = r.createCell(2, CellType.FORMULA);
        cfn.setCellFormula("A1");
        Cell cfs = r.createCell(3, CellType.FORMULA);
        cfs.setCellFormula("B1");

        FormulaEvaluator fe = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals(CellType.NUMERIC, fe.evaluate(cfn).getCellType());
        assertEquals(CellType.STRING, fe.evaluate(cfs).getCellType());
        fe.evaluateFormulaCell(cfn);
        fe.evaluateFormulaCell(cfs);

        // Now test
        assertEquals(CellType.NUMERIC, cn.getCellType());
        assertEquals(CellType.STRING, cs.getCellType());
        assertEquals(CellType.FORMULA, cfn.getCellType());
        assertEquals(CellType.NUMERIC, cfn.getCachedFormulaResultType());
        assertEquals(CellType.FORMULA, cfs.getCellType());
        assertEquals(CellType.STRING, cfs.getCachedFormulaResultType());

        // Different ways of retrieving
        assertEquals(1.2, cn.getNumericCellValue(), 0);
        try {
            cn.getRichStringCellValue();
            fail();
        } catch(IllegalStateException e) {
            // expected here
        }

        assertEquals("Testing", cs.getStringCellValue());
        try {
            cs.getNumericCellValue();
            fail();
        } catch(IllegalStateException e) {
            // expected here
        }

        assertEquals(1.2, cfn.getNumericCellValue(), 0);
        try {
            cfn.getRichStringCellValue();
            fail();
        } catch(IllegalStateException e) {
            // expected here
        }

        assertEquals("Testing", cfs.getStringCellValue());
        try {
            cfs.getNumericCellValue();
            fail();
        } catch(IllegalStateException e) {
            // expected here
        }
        
        wb.close();
    }

    @Test
    public void test58113() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet( "Test" );

        Row row = sheet.createRow(0);

        Cell cell = row.createCell(0);
        // verify that null-values can be set, this was possible up to 3.11, but broken in 3.12
        cell.setCellValue((String)null);
        String value = cell.getStringCellValue();
        assertTrue("HSSF will currently return empty string, XSSF/SXSSF will return null, but had: " + value,
                value == null || value.length() == 0);

        cell = row.createCell(1);
        // also verify that setting formulas to null works
        cell.setCellType(CellType.FORMULA);
        cell.setCellValue((String)null);

        wb.getCreationHelper().createFormulaEvaluator().evaluateAll();

        value = cell.getStringCellValue();
        assertTrue("HSSF will currently return empty string, XSSF/SXSSF will return null, but had: " + value,
                value == null || value.length() == 0);

        // set some value
        cell.setCellType(CellType.STRING);
        cell.setCellValue("somevalue");

        value = cell.getStringCellValue();
        assertTrue("can set value afterwards: " + value,
                value.equals("somevalue"));

        // verify that the null-value is actually set even if there was some value in the cell before
        cell.setCellValue((String)null);
        value = cell.getStringCellValue();
        assertTrue("HSSF will currently return empty string, XSSF/SXSSF will return null, but had: " + value,
                value == null || value.length() == 0);
        
        wb.close();
    }

    /**
     * Formulas with Nested Ifs, or If with text functions like
     *  Mid in it, can give #VALUE in Excel
     */
    @Test
    public void bug55747() throws IOException {
        Workbook wb1 = _testDataProvider.createWorkbook();
        FormulaEvaluator ev = wb1.getCreationHelper().createFormulaEvaluator();
        Sheet s = wb1.createSheet();

        Row row = s.createRow(0);
        row.createCell(0).setCellValue("abc");
        row.createCell(1).setCellValue("");
        row.createCell(2).setCellValue(3);

        Cell cell = row.createCell(5);
        cell.setCellFormula("IF(A1<>\"\",MID(A1,1,2),\" \")");
        ev.evaluateAll();
        assertEquals("ab", cell.getStringCellValue());

        cell = row.createCell(6);
        cell.setCellFormula("IF(B1<>\"\",MID(A1,1,2),\"empty\")");
        ev.evaluateAll();
        assertEquals("empty", cell.getStringCellValue());

        cell = row.createCell(7);
        cell.setCellFormula("IF(A1<>\"\",IF(C1<>\"\",MID(A1,1,2),\"c1\"),\"c2\")");
        ev.evaluateAll();
        assertEquals("ab", cell.getStringCellValue());

        // Write it back out, and re-read
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb1);
        wb1.close();
        ev = wb2.getCreationHelper().createFormulaEvaluator();
        s = wb2.getSheetAt(0);
        row = s.getRow(0);

        // Check read ok, and re-evaluate fine
        cell = row.getCell(5);
        assertEquals("ab", cell.getStringCellValue());
        ev.evaluateFormulaCell(cell);
        assertEquals("ab", cell.getStringCellValue());

        cell = row.getCell(6);
        assertEquals("empty", cell.getStringCellValue());
        ev.evaluateFormulaCell(cell);
        assertEquals("empty", cell.getStringCellValue());

        cell = row.getCell(7);
        assertEquals("ab", cell.getStringCellValue());
        ev.evaluateFormulaCell(cell);
        assertEquals("ab", cell.getStringCellValue());
        wb2.close();
    }

    @Test
    public void bug58260() throws IOException {
        //Create workbook and worksheet
        Workbook wb = _testDataProvider.createWorkbook();
        //Sheet worksheet = wb.createSheet("sample");

        //Loop through and add all values from array list
        // use a fixed seed to always produce the same file which makes comparing stuff easier
        //Random rnd = new Random(4352345);
        int maxStyles = (wb instanceof HSSFWorkbook) ? 4009 : 64000;
        for(int i = 0;i < maxStyles;i++) {
            //Create new row
            //Row row = worksheet.createRow(i);

            //Create cell style
            CellStyle style = null;
            try {
                style = wb.createCellStyle();
            } catch (IllegalStateException e) {
               fail("Failed for row " + i);
            }
            style.setAlignment(HorizontalAlignment.RIGHT);
            if((wb instanceof HSSFWorkbook)) {
                // there are some predefined styles
                assertEquals(i+21, style.getIndex());
            } else {
                // getIndex() returns short, which is not sufficient for > 32767
                // we should really change the API to be "int" for getIndex() but
                // that needs API changes
                assertEquals(i+1, style.getIndex() & 0xffff);
            }

            //Create cell
            //Cell cell = row.createCell(0);

            //Set cell style
            //cell.setCellStyle(style);

            //Set cell value
            //cell.setCellValue("r" + rnd.nextInt());
        }

        // should fail if we try to add more now
        try {
            wb.createCellStyle();
            fail("Should fail after " + maxStyles + " styles, but did not fail");
        } catch (IllegalStateException e) {
            // expected here
        }

        /*//add column width for appearance sake
        worksheet.setColumnWidth(0, 5000);

        // Write the output to a file
        System.out.println("Writing...");
        OutputStream fileOut = new FileOutputStream("C:\\temp\\58260." + _testDataProvider.getStandardFileNameExtension());

        // the resulting file can be compressed nicely, so we need to disable the zip bomb detection here
        double before = ZipSecureFile.getMinInflateRatio();
        try {
            ZipSecureFile.setMinInflateRatio(0.00001);
            wb.write(fileOut);
        } finally {
            fileOut.close();
            ZipSecureFile.setMinInflateRatio(before);
        }*/

        wb.close();
    }

    @Test
    public void test50319() throws IOException {
        Workbook wb = new HSSFWorkbook();
        Sheet sheet = wb.createSheet("Test");
        sheet.createRow(0);
        sheet.groupRow(0, 0);
        sheet.setRowGroupCollapsed(0, true);
        
        sheet.groupColumn(0, 0);
        sheet.setColumnGroupCollapsed(0, true);
        
        wb.close();
    }
    
    // Bug 58648: FormulaParser throws exception in parseSimpleFactor() when getCellFormula()
    // is called on a cell and the formula contains spaces between closing parentheses ") )"
    // https://bz.apache.org/bugzilla/show_bug.cgi?id=58648
    @Test
    public void test58648() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Cell cell = wb.createSheet().createRow(0).createCell(0);
        cell.setCellFormula("((1 + 1) )");
        // fails with
        // org.apache.poi.ss.formula.FormulaParseException: Parse error near char ... ')'
        // in specified formula '((1 + 1) )'. Expected cell ref or constant literal

        wb.close();
    }
    
    /**
     * If someone sets a null string as a cell value, treat
     *  it as an empty cell, and avoid a NPE on auto-sizing
     */
    @Test
    public void test57034() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet s = wb.createSheet();
        Cell cell = s.createRow(0).createCell(0);
        cell.setCellValue((String)null);
        assertEquals(CellType.BLANK, cell.getCellType());
        
        _testDataProvider.trackAllColumnsForAutosizing(s);
        
        s.autoSizeColumn(0);
        assertEquals(2048, s.getColumnWidth(0));

        s.autoSizeColumn(0, true);
        assertEquals(2048, s.getColumnWidth(0));

        wb.close();
    }

    @Test
    public void test52684() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();

        Sheet sheet = wb.createSheet("test");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);

        cell.setCellValue(12312345123L);

        DataFormat format = wb.createDataFormat();
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(format.getFormat("000-00000-000"));
        cell.setCellStyle(style);

        assertEquals("000-00000-000",
                cell.getCellStyle().getDataFormatString());
        assertEquals(164, cell.getCellStyle().getDataFormat());

        DataFormatter formatter = new DataFormatter();

        assertEquals("12-312-345-123", formatter.formatCellValue(cell));

        wb.close();
    }
    
    @Test
    public void test58896() throws IOException {
        final int nrows = 160;
        final int ncols = 139;
        
        // Create a workbook
        final Workbook wb = _testDataProvider.createWorkbook(nrows+1);
        final Sheet sh = wb.createSheet();
        if (logger.check(POILogger.DEBUG)) {
            logger.log(POILogger.DEBUG, wb.getClass().getName() + " column autosizing timing...");
        }

        final long t0 = time();
        _testDataProvider.trackAllColumnsForAutosizing(sh);
        for (int r=0; r<nrows; r++) {
            final Row row = sh.createRow(r);
            for (int c=0; c<ncols; c++) {
                final Cell cell = row.createCell(c);
                cell.setCellValue("Cell[r="+r+",c="+c+"]");
            }
        }
        final double populateSheetTime = delta(t0);
        final double populateSheetTimePerCell_ns = (1000000 * populateSheetTime / (nrows*ncols));
        if (logger.check(POILogger.DEBUG)) {
            logger.log(POILogger.DEBUG, "Populate sheet time: " + populateSheetTime + " ms (" + populateSheetTimePerCell_ns + " ns/cell)");
            
            logger.log(POILogger.DEBUG, "Autosizing...");
        }
        final long t1 = time();
        for (int c=0; c<ncols; c++) {
            final long t2 = time();
            sh.autoSizeColumn(c);
            if (logger.check(POILogger.DEBUG)) {
                logger.log(POILogger.DEBUG, "Column " + c + " took " + delta(t2) + " ms");
            }
            
        }
        final double autoSizeColumnsTime = delta(t1);
        final double autoSizeColumnsTimePerColumn = autoSizeColumnsTime / ncols;
        final double bestFitWidthTimePerCell_ns = 1000000 * autoSizeColumnsTime / (ncols * nrows);
        
        if (logger.check(POILogger.DEBUG)) {
            logger.log(POILogger.DEBUG, "Auto sizing columns took a total of " + autoSizeColumnsTime + " ms (" + autoSizeColumnsTimePerColumn + " ms per column)");
            logger.log(POILogger.DEBUG, "Best fit width time per cell: " + bestFitWidthTimePerCell_ns + " ns");
        }
        
        final double totalTime_s = (populateSheetTime + autoSizeColumnsTime) / 1000;
        if (logger.check(POILogger.DEBUG)) {
            logger.log(POILogger.DEBUG, "Total time: " + totalTime_s + " s");
        }
        
        wb.close();
        
        //if (bestFitWidthTimePerCell_ns > 50000) {
        //    fail("Best fit width time per cell exceeded 50000 ns: " + bestFitWidthTimePerCell_ns + " ns");
        //}
        
        //if (totalTime_s > 10) {
        //    fail("Total time exceeded 10 seconds: " + totalTime_s + " s");
        //}
    }
    
    protected long time() {
        return System.currentTimeMillis();
    }

    protected double delta(long startTimeMillis) {
        return time() - startTimeMillis;
    }
    
    @Ignore("bug 59393")
    @Test
    public void bug59393_commentsCanHaveSameAnchor() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        
        Sheet sheet = wb.createSheet();
        
        CreationHelper helper = wb.getCreationHelper();
        ClientAnchor anchor = helper.createClientAnchor();
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        
        Row row = sheet.createRow(0);
        
        Cell cell1 = row.createCell(0);
        Cell cell2 = row.createCell(1);
        Cell cell3 = row.createCell(2);

        Comment comment1 = drawing.createCellComment(anchor);
        RichTextString richTextString1 = helper.createRichTextString("comment1");
        comment1.setString(richTextString1);
        cell1.setCellComment(comment1);
         
        // fails with IllegalArgumentException("Multiple cell comments in one cell are not allowed, cell: A1")
        // because createCellComment tries to create a cell at A1
        // (from CellAddress(anchor.getRow1(), anchor.getCell1())),
        // but cell A1 already has a comment (comment1).
        // Need to atomically create a comment and attach it to a cell.
        // Current workaround: change anchor between each usage
        // anchor.setCol1(1);
        Comment comment2 = drawing.createCellComment(anchor);
        RichTextString richTextString2 = helper.createRichTextString("comment2");
        comment2.setString(richTextString2);
        cell2.setCellComment(comment2);

        // anchor.setCol1(2);
        Comment comment3 = drawing.createCellComment(anchor);
        RichTextString richTextString3 = helper.createRichTextString("comment3");
        comment3.setString(richTextString3);
        cell3.setCellComment(comment3);
        
        wb.close();
    }


    @Test
    public void bug57798() throws Exception {
        String fileName = "57798." + _testDataProvider.getStandardFileNameExtension();
        Workbook workbook = _testDataProvider.openSampleWorkbook(fileName);

        Sheet sheet = workbook.getSheet("Sheet1");

        // *******************************
        // First cell of array formula, OK
        int rowId = 0;
        int cellId = 1;

        Row row = sheet.getRow(rowId);
        Cell cell = row.getCell(cellId);

        assertEquals("A1", cell.getCellFormula());
        if (CellType.FORMULA == cell.getCellType()) {
            CellType formulaResultType = cell.getCachedFormulaResultType();
            assertEquals(CellType.STRING, formulaResultType);
        }

        // *******************************
        // Second cell of array formula, NOT OK for xlsx files
        rowId = 1;
        cellId = 1;

        row = sheet.getRow(rowId);
        cell = row.getCell(cellId);
        assertEquals("A1", cell.getCellFormula());

        if (CellType.FORMULA == cell.getCellType()) {
            CellType formulaResultType = cell.getCachedFormulaResultType();
            assertEquals(CellType.STRING, formulaResultType);
        }

        workbook.close();
    }

    @Ignore
    @Test
    public void test57929() throws IOException {
        // Create a workbook with print areas on 2 sheets
        Workbook wb = _testDataProvider.createWorkbook();
        wb.createSheet("Sheet0");
        wb.createSheet("Sheet1");
        wb.setPrintArea(0, "$A$1:$C$6");
        wb.setPrintArea(1, "$B$1:$C$5");
        
        // Verify the print areas were set correctly
        assertEquals("Sheet0!$A$1:$C$6", wb.getPrintArea(0));
        assertEquals("Sheet1!$B$1:$C$5", wb.getPrintArea(1));
        
        // Remove the print area on Sheet0 and change the print area on Sheet1
        wb.removePrintArea(0);
        wb.setPrintArea(1, "$A$1:$A$1");
        
        // Verify that the changes were made
        assertNull("Sheet0 before write", wb.getPrintArea(0));
        assertEquals("Sheet1 before write", "Sheet1!$A$1:$A$1", wb.getPrintArea(1));
        
        // Verify that the changes are non-volatile
        Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb);
        wb.close();
        
        assertNull("Sheet0 after write", wb2.getPrintArea(0)); // CURRENTLY FAILS with "Sheet0!$A$1:$C$6"
        assertEquals("Sheet1 after write", "Sheet1!$A$1:$A$1", wb2.getPrintArea(1));
    }


    @Test
    public void test55384() throws Exception {
        try (Workbook wb = _testDataProvider.createWorkbook()) {
            Sheet sh = wb.createSheet();
            for (int rownum = 0; rownum < 10; rownum++) {
                Row row = sh.createRow(rownum);
                for (int cellnum = 0; cellnum < 3; cellnum++) {
                    Cell cell = row.createCell(cellnum);
                    cell.setCellValue(rownum + cellnum);
                }
            }
            Row row = sh.createRow(10);
            // setting no precalculated value works just fine.
            Cell cell1 = row.createCell(0);
            cell1.setCellFormula("SUM(A1:A10)");

            // but setting a precalculated STRING value fails totally in SXSSF
            Cell cell2 = row.createCell(1);
            cell2.setCellFormula("SUM(B1:B10)");
            cell2.setCellValue("55");

            // setting a precalculated int value works as expected
            Cell cell3 = row.createCell(2);
            cell3.setCellFormula("SUM(C1:C10)");
            cell3.setCellValue(65);

            assertEquals(CellType.FORMULA, cell1.getCellType());
            assertEquals(CellType.FORMULA, cell2.getCellType());
            assertEquals(CellType.FORMULA, cell3.getCellType());

            assertEquals("SUM(A1:A10)", cell1.getCellFormula());
            assertEquals("SUM(B1:B10)", cell2.getCellFormula());
            assertEquals("SUM(C1:C10)", cell3.getCellFormula());

            /*String name = wb.getClass().getCanonicalName();
            String ext = (wb instanceof HSSFWorkbook) ? ".xls" : ".xlsx";
            OutputStream output = new FileOutputStream("/tmp" + name + ext);
            try {
                wb.write(output);
            } finally {
                output.close();
            }*/

            Workbook wbBack = _testDataProvider.writeOutAndReadBack(wb);
            checkFormulaPreevaluatedString(wbBack);
            wbBack.close();
        }
    }

    private void checkFormulaPreevaluatedString(Workbook readFile) {
        Sheet sheet = readFile.getSheetAt(0);
        Row row = sheet.getRow(sheet.getLastRowNum());
        assertEquals(10, row.getRowNum());

        for (Cell cell : row) {
            String cellValue;
            switch (cell.getCellType()) {
                case STRING:
                    cellValue = cell.getRichStringCellValue().getString();
                    break;
                case FORMULA:
                    cellValue = cell.getCellFormula();
                    break;
                default:
                    fail("unexpected cell type");
                    return;
            }
            assertNotNull(cellValue);
            cellValue = cellValue.isEmpty() ? null : cellValue;
            assertNotNull(cellValue);
        }
    }
    
    // bug 60197: setSheetOrder should update sheet-scoped named ranges to maintain references to the sheets before the re-order
    @Test
    public void bug60197_NamedRangesReferToCorrectSheetWhenSheetOrderIsChanged() throws Exception {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet1 = wb.createSheet("Sheet1");
        Sheet sheet2 = wb.createSheet("Sheet2");
        Sheet sheet3 = wb.createSheet("Sheet3");
    
        Name nameOnSheet1 = wb.createName();
        nameOnSheet1.setSheetIndex(wb.getSheetIndex(sheet1));
        nameOnSheet1.setNameName("NameOnSheet1");
        nameOnSheet1.setRefersToFormula("Sheet1!A1");
        
        Name nameOnSheet2 = wb.createName();
        nameOnSheet2.setSheetIndex(wb.getSheetIndex(sheet2));
        nameOnSheet2.setNameName("NameOnSheet2");
        nameOnSheet2.setRefersToFormula("Sheet2!A1");
        
        Name nameOnSheet3 = wb.createName();
        nameOnSheet3.setSheetIndex(wb.getSheetIndex(sheet3));
        nameOnSheet3.setNameName("NameOnSheet3");
        nameOnSheet3.setRefersToFormula("Sheet3!A1");
        
        // workbook-scoped name
        Name name = wb.createName();
        name.setNameName("WorkbookScopedName");
        name.setRefersToFormula("Sheet2!A1");
        
        assertEquals("Sheet1", nameOnSheet1.getSheetName());
        assertEquals("Sheet2", nameOnSheet2.getSheetName());
        assertEquals("Sheet3", nameOnSheet3.getSheetName());
        assertEquals(-1, name.getSheetIndex());
        assertEquals("Sheet2!A1", name.getRefersToFormula());
        
        // rearrange the sheets several times to make sure the names always refer to the right sheet
        for (int i=0; i<=9; i++) {
            wb.setSheetOrder("Sheet3", i % 3);
            
            // Current bug in XSSF:
            // Call stack:
            //   XSSFWorkbook.write(OutputStream)
            //   XSSFWorkbook.commit()
            //   XSSFWorkbook.saveNamedRanges()
            // This dumps the current namedRanges to CTDefinedName and writes these to the CTWorkbook
            // Then the XSSFName namedRanges list is cleared and rebuilt
            // Thus, any XSSFName object becomes invalid after a write
            // This re-assignment to the XSSFNames is not necessary if wb.write is not called.
            nameOnSheet1 = wb.getName("NameOnSheet1");
            nameOnSheet2 = wb.getName("NameOnSheet2");
            nameOnSheet3 = wb.getName("NameOnSheet3");
            name = wb.getName("WorkbookScopedName");
            
            // The name should still refer to the same sheet after the sheets are re-ordered
            assertEquals(i % 3, wb.getSheetIndex("Sheet3"));
            assertEquals(nameOnSheet1.getNameName(), "Sheet1", nameOnSheet1.getSheetName());
            assertEquals(nameOnSheet2.getNameName(), "Sheet2", nameOnSheet2.getSheetName());
            assertEquals(nameOnSheet3.getNameName(), "Sheet3", nameOnSheet3.getSheetName());
            assertEquals(name.getNameName(), -1, name.getSheetIndex());
            assertEquals(name.getNameName(), "Sheet2!A1", name.getRefersToFormula());
            
            // make sure the changes to the names stick after writing out the workbook
            Workbook wb2 = _testDataProvider.writeOutAndReadBack(wb);
            
            // See note above. XSSFNames become invalid after workbook write
            // Without reassignment here, an XmlValueDisconnectedException may occur
            nameOnSheet1 = wb.getName("NameOnSheet1");
            nameOnSheet2 = wb.getName("NameOnSheet2");
            nameOnSheet3 = wb.getName("NameOnSheet3");
            name = wb.getName("WorkbookScopedName");
            
            // Saving the workbook should not change the sheet names
            assertEquals(i % 3, wb.getSheetIndex("Sheet3"));
            assertEquals(nameOnSheet1.getNameName(), "Sheet1", nameOnSheet1.getSheetName());
            assertEquals(nameOnSheet2.getNameName(), "Sheet2", nameOnSheet2.getSheetName());
            assertEquals(nameOnSheet3.getNameName(), "Sheet3", nameOnSheet3.getSheetName());
            assertEquals(name.getNameName(), -1, name.getSheetIndex());
            assertEquals(name.getNameName(), "Sheet2!A1", name.getRefersToFormula());
            
            // Verify names in wb2
            nameOnSheet1 = wb2.getName("NameOnSheet1");
            nameOnSheet2 = wb2.getName("NameOnSheet2");
            nameOnSheet3 = wb2.getName("NameOnSheet3");
            name = wb2.getName("WorkbookScopedName");
            
            assertEquals(i % 3, wb2.getSheetIndex("Sheet3"));
            assertEquals(nameOnSheet1.getNameName(), "Sheet1", nameOnSheet1.getSheetName());
            assertEquals(nameOnSheet2.getNameName(), "Sheet2", nameOnSheet2.getSheetName());
            assertEquals(nameOnSheet3.getNameName(), "Sheet3", nameOnSheet3.getSheetName());
            assertEquals(name.getNameName(), -1, name.getSheetIndex());
            assertEquals(name.getNameName(), "Sheet2!A1", name.getRefersToFormula());
            
            wb2.close();
        }
        
        wb.close();
    }

    @Test
    public void test59200() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        final Sheet sheet = wb.createSheet();

        DataValidation dataValidation;
        CellRangeAddressList headerCell = new CellRangeAddressList(0, 1, 0, 1);
        DataValidationConstraint constraint = sheet.getDataValidationHelper().createCustomConstraint("A1<>\"\"");

        dataValidation = sheet.getDataValidationHelper().createValidation(constraint, headerCell);

        // HSSF has 32/255 limits as part of the Spec, XSSF has no limit in the spec, but Excel applies a 255 length limit!
        // more than 255 fail for all
        checkFailures(dataValidation, TEST_256, TEST_32, true);
        checkFailures(dataValidation, TEST_32, TEST_256, true);

        // null does work
        checkFailures(dataValidation, null, null, false);

        // more than 32 title fail for HSSFWorkbook
        checkFailures(dataValidation, TEST_255, TEST_32, wb instanceof HSSFWorkbook);

        // special characters work
        checkFailures(dataValidation, TEST_SPECIAL_TITLE, TEST_SPECIAL, false);

        // 32 length title and 255 length text work for both
        checkFailures(dataValidation, TEST_32, TEST_255, false);

        dataValidation.setShowErrorBox(false);
        sheet.addValidationData(dataValidation);

        // write out and read back in to trigger some more validation
        final Workbook wbBack = _testDataProvider.writeOutAndReadBack(wb);

        final Sheet sheetBack = wbBack.getSheetAt(0);
        final List<? extends DataValidation> dataValidations = sheetBack.getDataValidations();
        assertEquals(1, dataValidations.size());

        /*String ext = (wb instanceof HSSFWorkbook) ? ".xls" : ".xlsx";
        OutputStream str = new FileOutputStream("C:\\temp\\59200" + ext);
        try {
            wb.write(str);
        } finally {
            str.close();
        }*/

        wb.close();
    }

    private void checkFailures(DataValidation dataValidation, String title, String text, boolean shouldFail) {
        try {
            dataValidation.createPromptBox(title, text);
            assertFalse("Should fail in a length-check, had " + (title == null ? null : title.length()) + " and " + (text == null ? null : text.length()), shouldFail);
        } catch (IllegalStateException e) {
            assertTrue("Should not fail in a length-check, had " + (title == null ? null : title.length()) + " and " + (text == null ? null : text.length()), shouldFail);
            // expected here
        }
        try {
            dataValidation.createErrorBox(title, text);
            assertFalse("Should fail in a length-check, had " + (title == null ? null : title.length()) + " and " + (text == null ? null : text.length()), shouldFail);
        } catch (IllegalStateException e) {
            assertTrue("Should not fail in a length-check, had " + (title == null ? null : title.length()) + " and " + (text == null ? null : text.length()), shouldFail);
        }
    }

    @Test
    public void test60370() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        final Sheet sheet = wb.createSheet();

        DataValidation dataValidation;
        CellRangeAddressList headerCell = new CellRangeAddressList(0, 1, 0, 1);
        DataValidationConstraint constraint = sheet.getDataValidationHelper().createCustomConstraint("A1<>\"\"");

        dataValidation = sheet.getDataValidationHelper().createValidation(constraint, headerCell);
        checkFailures(dataValidation, TEST_SPECIAL_TITLE, TEST_SPECIAL, false);

        dataValidation.setShowErrorBox(true);
        dataValidation.setShowPromptBox(true);
        sheet.addValidationData(dataValidation);

        // write out and read back in to trigger some more validation
        final Workbook wbBack = _testDataProvider.writeOutAndReadBack(wb);

        final Sheet sheetBack = wbBack.getSheetAt(0);
        final List<? extends DataValidation> dataValidations = sheetBack.getDataValidations();
        assertEquals(1, dataValidations.size());

        /*String ext = (wb instanceof HSSFWorkbook) ? ".xls" : ".xlsx";
        OutputStream str = new FileOutputStream("/tmp/60370" + ext);
        try {
            wb.write(str);
        } finally {
            str.close();
        }*/

        wb.close();
    }

    protected void assertFormula(Workbook wb, Cell intF, String expectedFormula, String expectedResultOrNull) {
        assertEquals(CellType.FORMULA, intF.getCellType());
        if (null == expectedResultOrNull) {
            assertEquals(CellType.ERROR, intF.getCachedFormulaResultType());
            expectedResultOrNull = "#VALUE!";
        } else {
            assertEquals(CellType.NUMERIC, intF.getCachedFormulaResultType());
        }

        assertEquals(expectedFormula, intF.getCellFormula());

        // Check we can evaluate it correctly
        FormulaEvaluator eval = wb.getCreationHelper().createFormulaEvaluator();
        assertEquals(expectedResultOrNull, eval.evaluate(intF).formatAsString());
    }
}