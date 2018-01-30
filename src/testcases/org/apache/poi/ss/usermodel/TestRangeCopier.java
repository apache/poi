package org.apache.poi.ss.usermodel;

import static org.junit.Assert.assertEquals;

import org.apache.poi.ss.ITestDataProvider;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRangeCopier;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRangeCopier {
    protected Sheet sheet1;
    protected Sheet sheet2;
    protected Workbook workbook;
    protected RangeCopier rangeCopier; 
    protected RangeCopier transSheetRangeCopier; 
    protected ITestDataProvider testDataProvider;

    protected void initSheets(){
        sheet1 = workbook.getSheet("sheet1");
        sheet2 = workbook.getSheet("sheet2");
    }
    
    @Test
    public void copySheetRangeWithoutFormulas(){
        CellRangeAddress rangeToCopy = CellRangeAddress.valueOf("B1:C2");   //2x2
        CellRangeAddress destRange = CellRangeAddress.valueOf("C2:D3");     //2x2
        writeRangeToLog(sheet1, rangeToCopy);
        rangeCopier.copyRange(rangeToCopy, destRange);
        writeSheetToLog(sheet1);
        assertEquals("1.1", sheet1.getRow(2).getCell(2).toString());
        assertEquals("2.1", sheet1.getRow(2).getCell(3).toString());
    }

    @Test
    public void tileTheRangeAway(){
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5");
        CellRangeAddress destRange = CellRangeAddress.valueOf("F4:K5"); 
        //writeRangeToLog(sheet1, tileRange);
        rangeCopier.copyRange(tileRange, destRange);
        //writeSheetToLog(sheet1);
        assertEquals("1.3", getCellContent(sheet1, "H4"));  
        assertEquals("1.3", getCellContent(sheet1, "J4"));  
        assertEquals("$C1+G$2", getCellContent(sheet1, "G5"));  
        assertEquals("SUM(G3:I3)", getCellContent(sheet1, "H5"));   
        assertEquals("$C1+I$2", getCellContent(sheet1, "I5"));  
        assertEquals("", getCellContent(sheet1, "L5"));  //out of borders
        assertEquals("", getCellContent(sheet1, "G7")); //out of borders
    }
    
    @Test
    public void tileTheRangeOver(){
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5");
        CellRangeAddress destRange = CellRangeAddress.valueOf("A4:C5"); 
        //writeRangeToLog(sheet1, tileRange);
        rangeCopier.copyRange(tileRange, destRange);
        //writeSheetToLog(sheet1);
        assertEquals("1.3", getCellContent(sheet1, "A4"));
        assertEquals("$C1+B$2", getCellContent(sheet1, "B5"));
        assertEquals("SUM(B3:D3)", getCellContent(sheet1, "C5"));
    }

    @Test
    public void copyRangeToOtherSheet(){
        Sheet destSheet = sheet2;
        CellRangeAddress tileRange = CellRangeAddress.valueOf("C4:D5"); // on sheet1
        CellRangeAddress destRange = CellRangeAddress.valueOf("F4:J6"); // on sheet2 
        writeRangeToLog(sheet1, tileRange);
        transSheetRangeCopier.copyRange(tileRange, destRange);
        writeSheetToLog(destSheet);
        assertEquals("1.3", getCellContent(destSheet, "H4"));
        assertEquals("1.3", getCellContent(destSheet, "J4"));
        assertEquals("$C1+G$2", getCellContent(destSheet, "G5"));
        assertEquals("SUM(G3:I3)", getCellContent(destSheet, "H5"));
        assertEquals("$C1+I$2", getCellContent(destSheet, "I5"));
    }
    
    protected static String getCellContent(Sheet sheet, String coordinates){
        try {
            CellReference p = new CellReference(coordinates);
            return sheet.getRow(p.getRow()).getCell(p.getCol()).toString();
        }
        catch (NullPointerException e){ // row or cell does not exist
            return "";
        }
    }
    public static void writeRangeToLog(Sheet sheet, CellRangeAddress range){
        System.out.println(range.toString());
        int rowIndex = Math.max(sheet.getFirstRowNum(), range.getFirstRow());
        while(rowIndex <= Math.min(sheet.getLastRowNum(), range.getLastRow())){
            Row row = sheet.getRow(rowIndex);
            if(row == null){
                System.out.println("null row!");
                continue;
            }
            else{ 
                String line = "";
                int columnIndex = Math.max(row.getFirstCellNum(), range.getFirstColumn());
                while(columnIndex <= Math.min(row.getLastCellNum(), range.getLastColumn())){
                    Cell cell = row.getCell(columnIndex);
                    String cellAsString =  cell != null ? cell.toString() : "null";
                    line += "; " + String.format("%1$12s", cellAsString);
                    columnIndex++;
                }
                if(line.length() > 0)
                    line = line.substring(2);
                System.out.println(line);
            }
            rowIndex++;
        }
    }

    public static void writeSheetToLog(Sheet sheet){
        int rowIndex = sheet.getFirstRowNum();
        while(rowIndex <= sheet.getLastRowNum()){
            Row row = sheet.getRow(rowIndex);
            if(row == null)
                System.out.println("null row!");
            else System.out.println(String.format("%1$12s; %2$12s; %3$12s; %4$12s; %5$12s; %6$12s; %7$12s; %8$12s; %9$12s; %10$12s; %11$12s", 
                    row.getCell(0) != null ? row.getCell(0) : "null", 
                    row.getCell(1) != null ? row.getCell(1) : "null", 
                    row.getCell(2) != null ? row.getCell(2) : "null",
                    row.getCell(3) != null ? row.getCell(3) : "null",
                    row.getCell(4) != null ? row.getCell(4) : "null",
                    row.getCell(5) != null ? row.getCell(5) : "null",
                    row.getCell(6) != null ? row.getCell(6) : "null",
                    row.getCell(7) != null ? row.getCell(7) : "null",
                    row.getCell(8) != null ? row.getCell(8) : "null",
                    row.getCell(9) != null ? row.getCell(9) : "null",
                    row.getCell(10) != null ? row.getCell(10) : "null"
                        ));
            rowIndex++;
        }
        System.out.println("");
    }
    

}
