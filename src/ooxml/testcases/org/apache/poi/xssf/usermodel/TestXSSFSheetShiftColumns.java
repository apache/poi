package org.apache.poi.xssf.usermodel;

import java.io.IOException;

import org.apache.poi.ss.usermodel.BaseTestSheetShiftColumns;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;

public class TestXSSFSheetShiftColumns extends BaseTestSheetShiftColumns {
    public TestXSSFSheetShiftColumns(){
        super(); 
        workbook = new XSSFWorkbook();
        _testDataProvider = XSSFITestDataProvider.instance; 
    }

    protected Workbook openWorkbook(String spreadsheetFileName) throws IOException{
        return XSSFTestDataSamples.openSampleWorkbook(spreadsheetFileName);
    }
    protected Workbook getReadBackWorkbook(Workbook wb){
        return XSSFTestDataSamples.writeOutAndReadBack(wb);
    }

}
