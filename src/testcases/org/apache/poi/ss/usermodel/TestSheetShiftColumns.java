package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

public class TestSheetShiftColumns {
    protected Sheet sheet1, sheet2;
    protected Workbook wb;

    protected final ITestDataProvider _testDataProvider;

    public TestSheetShiftColumns(){
        _testDataProvider = XSSFITestDataProvider.instance; 
    }

    @Before
    public void init() {
        int rowIndex = 0;
        sheet1 = wb.createSheet("sheet1");
        Row row = sheet1.createRow(rowIndex++);
        row.createCell(0, CellType.NUMERIC).setCellValue(0);
        row.createCell(1, CellType.NUMERIC).setCellValue(1);
        row.createCell(2, CellType.NUMERIC).setCellValue(2);

        row = sheet1.createRow(rowIndex++);
        row.createCell(0, CellType.NUMERIC).setCellValue(0.1);
        row.createCell(1, CellType.NUMERIC).setCellValue(1.1);
        row.createCell(2, CellType.NUMERIC).setCellValue(2.1);
        row = sheet1.createRow(rowIndex++);
        row.createCell(0, CellType.NUMERIC).setCellValue(0.2);
        row.createCell(1, CellType.NUMERIC).setCellValue(1.2);
        row.createCell(2, CellType.NUMERIC).setCellValue(2.2);
        row = sheet1.createRow(rowIndex++);
        row.createCell(0, CellType.FORMULA).setCellFormula("A2*B3");
        row.createCell(1, CellType.NUMERIC).setCellValue(1.3);
        row.createCell(2, CellType.FORMULA).setCellFormula("B1-B3");
        row = sheet1.createRow(rowIndex++);
        row.createCell(0, CellType.FORMULA).setCellFormula("SUM(C1:C4)");
        row.createCell(1, CellType.FORMULA).setCellFormula("SUM(A3:C3)");
        row.createCell(2, CellType.FORMULA).setCellFormula("$C1+C$2");
        row = sheet1.createRow(rowIndex++);
        row.createCell(1, CellType.NUMERIC).setCellValue(1.5);
        row = sheet1.createRow(rowIndex++);
        row.createCell(1, CellType.BOOLEAN).setCellValue(false);
        Cell textCell =  row.createCell(2, CellType.STRING);
        textCell.setCellValue("TEXT");
        textCell.setCellStyle(newCenterBottomStyle());

        sheet2 = wb.createSheet("sheet2"); 
        row = sheet2.createRow(0); row.createCell(0, CellType.NUMERIC).setCellValue(10); 
        row.createCell(1, CellType.NUMERIC).setCellValue(11); 
        row.createCell(2, CellType.FORMULA).setCellFormula("SUM(sheet1!B3:C3)"); 
        row = sheet2.createRow(1); 
        row.createCell(0, CellType.NUMERIC).setCellValue(21); 
        row.createCell(1, CellType.NUMERIC).setCellValue(22); 
        row.createCell(2, CellType.NUMERIC).setCellValue(23); 
        row = sheet2.createRow(2);
        row.createCell(0, CellType.FORMULA).setCellFormula("sheet1!A4+sheet1!C2+A2");
        row.createCell(1, CellType.FORMULA).setCellFormula("SUM(sheet1!A3:$C3)"); 
        row = sheet2.createRow(3); 
        row.createCell(0, CellType.STRING).setCellValue("dummy");

        writeSheetToLog(sheet1);
    }
    private CellStyle newCenterBottomStyle(){
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.BOTTOM);
        return style;
    }
    @Test
    public void testShiftOneColumnRight() {
        //writeSheetToLog(sheet2);
        sheet1.shiftColumns(1, 2, 1);
        writeSheetToLog(sheet1);
        //writeSheetToLog(sheet2);
        double c1Value = sheet1.getRow(0).getCell(2).getNumericCellValue();
        assertEquals(1d, c1Value, 0.01);
        String formulaA4 = sheet1.getRow(3).getCell(0).getCellFormula();
        assertEquals("A2*C3", formulaA4);
        String formulaC4 = sheet1.getRow(3).getCell(3).getCellFormula();
        assertEquals("C1-C3", formulaC4);
        String formulaB5 = sheet1.getRow(4).getCell(2).getCellFormula();
        assertEquals("SUM(A3:D3)", formulaB5);
        String formulaD5 = sheet1.getRow(4).getCell(3).getCellFormula(); // $C1+C$2
        assertEquals("$D1+D$2", formulaD5);

        Cell newb5Null = sheet1.getRow(4).getCell(1);
        assertEquals(newb5Null, null);
        boolean logicalValue = sheet1.getRow(6).getCell(2).getBooleanCellValue();
        assertEquals(logicalValue, false);
        Cell textCell = sheet1.getRow(6).getCell(3);
        assertEquals(textCell.getStringCellValue(), "TEXT");
        assertEquals(textCell.getCellStyle().getAlignment(), HorizontalAlignment.CENTER);
        
        // other sheet
        String formulaC1 = sheet2.getRow(0).getCell(2).getCellFormula(); // SUM(sheet1!B3:C3)
        assertEquals("SUM(sheet1!C3:D3)", formulaC1);
        String formulaA3 = sheet2.getRow(2).getCell(0).getCellFormula(); // sheet1!A4+sheet1!C2+A2
        assertEquals("sheet1!A4+sheet1!D2+A2", formulaA3);
    }

    @Test
    public void testShiftTwoColumnsRight() {
        sheet1.shiftColumns(1, 2, 2);
        writeSheetToLog(sheet1);
        String formulaA4 = sheet1.getRow(3).getCell(0).getCellFormula();
        assertEquals("A2*D3", formulaA4);
        String formulaD4 = sheet1.getRow(3).getCell(4).getCellFormula();
        assertEquals("D1-D3", formulaD4);
        String formulaD5 = sheet1.getRow(4).getCell(3).getCellFormula();
        assertEquals("SUM(A3:E3)", formulaD5);

        Cell b5Null = sheet1.getRow(4).getCell(1);
        assertEquals(b5Null, null);
        Object c6Null = sheet1.getRow(5).getCell(2); // null cell A5 is shifted
                                                        // for 2 columns, so now
                                                        // c5 should be null
        assertEquals(c6Null, null);
    }

    @Test
    public void testShiftOneColumnLeft() {
        sheet1.shiftColumns(1, 2, -1);
        writeSheetToLog(sheet1);
        
        String formulaA5 = sheet1.getRow(4).getCell(0).getCellFormula();
        assertEquals("SUM(A3:B3)", formulaA5);
        String formulaB4 = sheet1.getRow(3).getCell(1).getCellFormula();
        assertEquals("A1-A3", formulaB4);
        String formulaB5 = sheet1.getRow(4).getCell(1).getCellFormula();
        assertEquals("$B1+B$2", formulaB5);
        Cell newb6Null = sheet1.getRow(5).getCell(1);
        assertEquals(newb6Null, null);
    }
    
    @Test
    public void testShiftTwoColumnsLeft() {
        try {
            sheet1.shiftColumns(1, 2, -2);
            writeSheetToLog(sheet1);
            assertTrue("shiftColumns(1, 2, -2) should raise exception, because 1-2=-1<0", false);
        }
        catch (IllegalStateException e) {
            // this is expected be cause first column tries to be shifted to index -1
            assertTrue(true);
        }
    }
    
    public void testShiftHyperlinks() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");
        Row row = sheet.createRow(0);

        // How to create hyperlinks
        // https://poi.apache.org/spreadsheet/quick-guide.html#Hyperlinks
        CreationHelper helper = wb.getCreationHelper();
        CellStyle hlinkStyle = wb.createCellStyle();
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        hlinkStyle.setFont(hlinkFont);

        // 3D relative document link
        // CellAddress=A1, shifted to A4
        Cell cell = row.createCell(0);
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.DOCUMENT, "test!E1");

        // URL
        cell = row.createCell(1);
        // CellAddress=B1, shifted to B4
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.URL, "http://poi.apache.org/");

        // row0 will be shifted on top of row1, so this URL should be removed
        // from the workbook
        Row overwrittenRow = sheet.createRow(3);
        cell = overwrittenRow.createCell(2);
        // CellAddress=C4, will be overwritten (deleted)
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.EMAIL, "mailto:poi@apache.org");

        Row unaffectedRow = sheet.createRow(20);
        cell = unaffectedRow.createCell(3);
        // CellAddress=D21, will be unaffected
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.FILE, "54524.xlsx");

        cell = wb.createSheet("other").createRow(0).createCell(0);
        // CellAddress=Other!A1, will be unaffected
        cell.setCellStyle(hlinkStyle);
        createHyperlink(helper, cell, HyperlinkType.URL, "http://apache.org/");

        int startRow = 0;
        int endRow = 4;
        int n = 3;
        writeSheetToLog(sheet);
        sheet.shiftColumns(startRow, endRow, n);
        writeSheetToLog(sheet);

        Workbook read = _testDataProvider.writeOutAndReadBack(wb);
        wb.close();

        Sheet sh = read.getSheet("test");

        Row shiftedRow = sh.getRow(0);

        // document link anchored on a shifted cell should be moved
        // Note that hyperlinks do not track what they point to, so this
        // hyperlink should still refer to test!E1
        verifyHyperlink(shiftedRow.getCell(3), HyperlinkType.DOCUMENT, "test!E1");

        // URL, EMAIL, and FILE links anchored on a shifted cell should be moved
        verifyHyperlink(shiftedRow.getCell(4), HyperlinkType.URL, "http://poi.apache.org/");

        // Make sure hyperlinks were moved and not copied
        assertNull("Document hyperlink should be moved, not copied", sh.getHyperlink(0, 0));
        assertNull("URL hyperlink should be moved, not copied", sh.getHyperlink(1, 0));

        assertEquals(4, sh.getHyperlinkList().size());
        read.close();
    }

    private void createHyperlink(CreationHelper helper, Cell cell, HyperlinkType linkType, String ref) {
        cell.setCellValue(ref);
        Hyperlink link = helper.createHyperlink(linkType);
        link.setAddress(ref);
        cell.setHyperlink(link);
    }

    private void verifyHyperlink(Cell cell, HyperlinkType linkType, String ref) {
        assertTrue(cellHasHyperlink(cell));
        Hyperlink link = cell.getHyperlink();
        assertEquals(linkType, link.getType());
        assertEquals(ref, link.getAddress());
    }

    private boolean cellHasHyperlink(Cell cell) {
        return (cell != null) && (cell.getHyperlink() != null);
    }

    @Test
    public void shiftMergedColumnsToMergedColumnsRight() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");

        // populate sheet cells
        populateSheetCells(sheet);
        writeSheetToLog(sheet);
        CellRangeAddress A1_A5 = new CellRangeAddress(0, 4, 0, 0);
        CellRangeAddress B1_B3 = new CellRangeAddress(0, 2, 1, 1);

        sheet.addMergedRegion(B1_B3);
        sheet.addMergedRegion(A1_A5);

        // A1:A5 should be moved to B1:B5
        // B1:B3 will be removed
        sheet.shiftColumns(0, 0, 1);
        writeSheetToLog(sheet);
        
        assertEquals(1, sheet.getNumMergedRegions());
        assertEquals(CellRangeAddress.valueOf("B1:B5"), sheet.getMergedRegion(0));

        wb.close();
    }
    @Test
    public void shiftMergedColumnsToMergedColumnsLeft() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet("test");
        populateSheetCells(sheet);

        CellRangeAddress A1_A5 = new CellRangeAddress(0, 4, 0, 0);
        CellRangeAddress B1_B3 = new CellRangeAddress(0, 2, 1, 1);

        sheet.addMergedRegion(A1_A5);
        sheet.addMergedRegion(B1_B3);

        // A1:E1 should be removed
        // B1:B3 will be A1:A3
        sheet.shiftColumns(1, 5, -1);

        assertEquals(1, sheet.getNumMergedRegions());
        assertEquals(CellRangeAddress.valueOf("A1:A3"), sheet.getMergedRegion(0));

        wb.close();
    }

    private void populateSheetCells(Sheet sheet) {
        // populate sheet cells
        for (int i = 0; i < 2; i++) {
            Row row = sheet.createRow(i);
            for (int j = 0; j < 5; j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(i + "x" + j);
            }
        }
    }

    @Test
    public final void testShiftWithMergedRegions() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row row = sheet.createRow(0);
        row.createCell(0).setCellValue(1.1);
        row = sheet.createRow(1);
        row.createCell(0).setCellValue(2.2);
        CellRangeAddress region = new CellRangeAddress(0, 2, 0, 0);
        assertEquals("A1:A3", region.formatAsString());

        sheet.addMergedRegion(region);

        sheet.shiftColumns(0, 1, 2);
        region = sheet.getMergedRegion(0);
        assertEquals("C1:C3", region.formatAsString());
        wb.close();
    }

    @Test
    public void testCommentsShifting() throws IOException {
        Workbook wb = XSSFTestDataSamples.openSampleWorkbook("56017.xlsx");

        Sheet sheet = wb.getSheetAt(0);
        Comment comment = sheet.getCellComment(new CellAddress(0, 0));
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
        
        writeSheetToLog(sheet);
        System.out.println("shifting column...");
        sheet.shiftColumns(0, 1, 1);
        writeSheetToLog(sheet);
        
        // comment in column 0 is gone
        comment = sheet.getCellComment(new CellAddress(0, 0));
        assertNull(comment);

        // comment is column in column 1
        comment = sheet.getCellComment(new CellAddress(0, 1));
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());

        Workbook wbBack = XSSFTestDataSamples.writeOutAndReadBack(wb);
        wb.close();
        assertNotNull(wbBack);

        Sheet sheetBack = wbBack.getSheetAt(0);

        // comment in column 0 is gone
        comment = sheetBack.getCellComment(new CellAddress(0, 0));
        assertNull(comment);

        // comment is now in column 1
        comment = sheetBack.getCellComment(new CellAddress(0, 1));
        assertNotNull(comment);
        assertEquals("Amdocs", comment.getAuthor());
        assertEquals("Amdocs:\ntest\n", comment.getString().getString());
        wbBack.close();
    }
    
    // transposed version of TestXSSFSheetShiftRows.testBug54524()
    @Test
    public void testBug54524() throws IOException {
        Workbook wb = _testDataProvider.createWorkbook();
        Sheet sheet = wb.createSheet();
        Row firstRow = sheet.createRow(0); 
        firstRow.createCell(0).setCellValue("");
        firstRow.createCell(1).setCellValue(1);
        firstRow.createCell(2).setCellValue(2);
        firstRow.createCell(3).setCellFormula("SUM(B1:C1)");
        firstRow.createCell(4).setCellValue("X");
        
        writeSheetToLog(sheet);
        sheet.shiftColumns(3, 5, -1);
        writeSheetToLog(sheet);

        Cell cell = CellUtil.getCell(sheet.getRow(0), 1);
        assertEquals(1.0, cell.getNumericCellValue(), 0);
        cell = CellUtil.getCell(sheet.getRow(0), 2);
        assertEquals("SUM(B1:B1)", cell.getCellFormula());
        cell = CellUtil.getCell(sheet.getRow(0), 3);
        assertEquals("X", cell.getStringCellValue());
        wb.close();
    }


    // I need these methods for testing. When we finish with project, we can easily remove them. 
    // Dragan Jovanović
    public static void writeSheetToLog(Sheet sheet) {
        DataFormatter formatter = new DataFormatter();
        int rowIndex = sheet.getFirstRowNum();
        while (rowIndex <= sheet.getLastRowNum()) {
            Row row = sheet.getRow(rowIndex);
            if (row == null)
                //log.trace("null row");
                System.out.println("null row");
            else {
                String line = "";
                for(int columnIndex = 0; columnIndex < 7; columnIndex++){
                    //String comment = sheet.getCellComment(new CellAddress(rowIndex, columnIndex)) == null ? "no comment" : sheet.getCellComment(new CellAddress(rowIndex, columnIndex)).getString().getString();
                    //line +=  String.format("; %1$12s    %2$20s",  ""/*getValue(row.getCell(columnIndex))*/, comment);
                    line +=  String.format("; %1$12s",  formatter.formatCellValue(row.getCell(columnIndex)));
                }
                line = line.substring(2);
                System.out.println(line);
                //log.trace(line);
            }
            rowIndex++;
        }
        //log.trace("");
        System.out.println("");
    }

}
