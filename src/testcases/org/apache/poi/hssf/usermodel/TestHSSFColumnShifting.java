package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.usermodel.helpers.HSSFColumnShifter;
import org.apache.poi.ss.usermodel.TestColumnShifting;

public class TestHSSFColumnShifting extends TestColumnShifting {
    public TestHSSFColumnShifting(){
        super(); 
        wb = new HSSFWorkbook();
    }
    @Override
    protected void initColumnShifter(){
        columnShifter = new HSSFColumnShifter((HSSFSheet)sheet1);
    }

}
