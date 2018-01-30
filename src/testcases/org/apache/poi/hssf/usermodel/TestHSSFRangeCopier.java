package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.TestRangeCopier;
import org.junit.Before;

public class TestHSSFRangeCopier extends TestRangeCopier {

    public TestHSSFRangeCopier(){
        super(); 
        workbook = new HSSFWorkbook();
        testDataProvider = HSSFITestDataProvider.instance; 
    }

    @Before
    public void init() {
        workbook = HSSFTestDataSamples.openSampleWorkbook("tile range test.xls");
        initSheets();
        rangeCopier = new HSSFRangeCopier(sheet1, sheet1);
        transSheetRangeCopier = new HSSFRangeCopier(sheet1, sheet2);
    }
}
