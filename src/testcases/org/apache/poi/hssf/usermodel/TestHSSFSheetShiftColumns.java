package org.apache.poi.hssf.usermodel;

import java.io.IOException;

import org.apache.poi.hssf.HSSFITestDataProvider;
import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.ss.usermodel.BaseTestSheetShiftColumns;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Ignore;
import org.junit.Test;

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
    
    @Override    
    @Ignore("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>") @Test
    public void shiftMergedColumnsToMergedColumnsLeft() throws IOException {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf, 
        // so that original method from BaseTestSheetShiftColumns can be executed. 
    }
    @Override    
    @Ignore("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>") @Test
    public void shiftMergedColumnsToMergedColumnsRight() throws IOException {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf, 
        // so that original method from BaseTestSheetShiftColumns can be executed. 
    }
    @Override    
    @Ignore("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>") @Test
    public void testBug54524() throws IOException {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf, 
        // so that original method from BaseTestSheetShiftColumns can be executed. 
    }
    @Override    
    @Ignore("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>") @Test
    public void testCommentsShifting() throws IOException {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf, 
        // so that original method from BaseTestSheetShiftColumns can be executed. 
    }
    @Override    
    @Ignore("see <https://bz.apache.org/bugzilla/show_bug.cgi?id=62030>") @Test
    public void testShiftWithMergedRegions() throws IOException {
        // This override is used only in order to test failing for HSSF. Please remove method after code is fixed on hssf, 
        // so that original method from BaseTestSheetShiftColumns can be executed. 
        // After removing, you can re-add 'final' keyword to specification of original method. 
    }
}
