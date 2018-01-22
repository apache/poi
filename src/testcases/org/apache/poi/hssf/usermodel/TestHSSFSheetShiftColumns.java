package org.apache.poi.hssf.usermodel;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestSheetShiftColumns;
import org.apache.poi.ss.usermodel.Workbook;

public class TestHSSFSheetShiftColumns extends BaseTestSheetShiftColumns {
    public TestHSSFSheetShiftColumns(){
        super(); 
        workbook = new HSSFWorkbook();
        _testDataProvider = HSSFITestDataProvider.instance; 
    }

    protected Workbook openWorkbook(String spreadsheetFileName)
            throws IOException {
        return HSSFTestDataSamples.openSampleWorkbook(spreadsheetFileName);
    }

    protected Workbook getReadBackWorkbook(Workbook wb) throws IOException {
        return HSSFTestDataSamples.writeOutAndReadBack((HSSFWorkbook)wb);
    }
}
