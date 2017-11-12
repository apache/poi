package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.ss.usermodel.TestColumnShifting;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class TestXSSFColumnShifting extends TestColumnShifting{
    public TestXSSFColumnShifting(){
        super(); 
        wb = new XSSFWorkbook();
    }
    @Override
    protected void initColumnShifter(){
        columnShifter = new XSSFColumnShifter((XSSFSheet)sheet1);
    }


}
