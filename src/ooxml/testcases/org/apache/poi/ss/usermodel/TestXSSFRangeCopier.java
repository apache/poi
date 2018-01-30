package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;

import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.apache.poi.xssf.usermodel.XSSFRangeCopier;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Before;
import org.junit.Test;

public class TestXSSFRangeCopier extends TestRangeCopier{
    public TestXSSFRangeCopier(){
        super(); 
        workbook = new XSSFWorkbook();
        testDataProvider = XSSFITestDataProvider.instance; 
    }

    @Before
    public void init() {
        workbook = XSSFTestDataSamples.openSampleWorkbook("tile range test.xlsx");
        initSheets();
        rangeCopier = new XSSFRangeCopier(sheet1, sheet1);
        transSheetRangeCopier = new XSSFRangeCopier(sheet1, sheet2);
    }

    @Test // XSSF only. HSSF version wouldn't be so simple. And also this test is contained in following, more complex tests, so it's not really important.
    public void copyRow(){
        Row existingRow = sheet1.getRow(4);
        XSSFRow newRow = (XSSFRow)sheet1.getRow(5);
        CellCopyPolicy policy = new CellCopyPolicy();
        newRow.copyRowFrom(existingRow, policy);
        assertEquals("$C2+B$2", newRow.getCell(1).getCellFormula());
    }
    
}
