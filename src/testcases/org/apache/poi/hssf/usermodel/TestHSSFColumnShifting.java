package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.usermodel.helpers.HSSFColumnShifter;
import org.apache.poi.ss.usermodel.BaseTestColumnShifting;

public class TestHSSFColumnShifting extends BaseTestColumnShifting {
    public TestHSSFColumnShifting(){
        super(); 
        wb = new HSSFWorkbook();
    }
    @Override
    protected void initColumnShifter(){
        columnShifter = new HSSFColumnShifter((HSSFSheet)sheet1);
    }

}
