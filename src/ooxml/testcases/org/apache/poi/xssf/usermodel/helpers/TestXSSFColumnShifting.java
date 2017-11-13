package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.ss.usermodel.BaseTestColumnShifting;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestXSSFColumnShifting extends BaseTestColumnShifting{
    public TestXSSFColumnShifting(){
        super(); 
        wb = new XSSFWorkbook();
    }
    @Override
    protected void initColumnShifter(){
        columnShifter = new XSSFColumnShifter((XSSFSheet)sheet1);
    }


}
