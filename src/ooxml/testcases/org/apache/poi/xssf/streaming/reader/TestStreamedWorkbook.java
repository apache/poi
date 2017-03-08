/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */
package org.apache.poi.xssf.streaming.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Iterator;

import org.apache.poi.POIDataSamples;
import org.junit.Test;


public class TestStreamedWorkbook{

   @Test
    public void testInvalidFilePath() throws Exception{
       StreamedWorkbook workbook = null;
       try {
            workbook = new StreamedWorkbook(null);
            fail("expected exception");
        } catch (Exception e) {
            assertEquals("No sheets found", e.getMessage());
        }
        
        if(workbook != null)workbook.close();
        
    }
    
    @Test
    public void testInvalidFile() throws Exception{ 
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("InvalidFile.txt");
        StreamedWorkbook workbook = null;
        try {
        
            workbook = new StreamedWorkbook(f.getAbsolutePath());
            workbook.getSheetIterator();
        } catch (Exception e) {
            assertEquals("No valid entries or contents found, this is not a valid OOXML (Office Open XML) file", e.getMessage());
        }
        
        if(workbook != null)workbook.close();
    }
    
   
    @Test
    public void testSheetCount() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int count = 0;
        
        try {
            Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
            
            while(sheetIterator.hasNext()){
                sheetIterator.next();
                count++;
            }
            
            assertEquals(2, count);
        } catch (Exception e) {
            
        }
        
        workbook.close();
       
    }
    
    @Test
    public void testTotalNumberOfSheets() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        
        try {
            int sheetCount = workbook.getNumberOfSheets();
            
            assertEquals(2, sheetCount);
        } catch (Exception e) {
        }
       
        workbook.close();
    }
    
/*    @Test
    public void testSheetAt() throws Exception{         //Not supported <TO DO>
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetIndex = 0;
        
        StreamedSheet sheet = (StreamedSheet)workbook.getSheetAt(sheetIndex);
        
        assertEquals(sheetIndex, sheet.getSheetNumber());
        
        sheetIndex = 1;
        
        sheet = (StreamedSheet)workbook.getSheetAt(sheetIndex);
        System.out.println(sheet.getSheetNumber());
        
        assertEquals(sheetIndex, sheet.getSheetNumber());
        
        workbook.close();
        
    }*/
    
    @Test
    public void testTotalRowCount() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;
        
        long count = 0;
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while((sheetIterator.hasNext()) && (sheetCount == 0)){
            StreamedSheet sheet = sheetIterator.next();
            
            Iterator<StreamedRow> rows = sheet.getAllRows();
            
            while(rows.hasNext()){
                rows.next();
                count++;
            }
            
            assertEquals(6, count);
            sheetCount++;
        }
        
        workbook.close();
    }
    
    @Test
    public void testNRowCount() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        
        long count = 0;
        int sheetCount = 0;
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while((sheetIterator.hasNext()) && (sheetCount == 0)){
            StreamedSheet sheet = sheetIterator.next();
            
            Iterator<StreamedRow> rows = sheet.getNRows(4);
            
            while(rows.hasNext()){
                rows.next();
                count++;
            }
            
            assertEquals(4, count);
            sheetCount++;
        }
        
        workbook.close();
    }
    
    @Test
    public void testCellCount() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while((sheetIterator.hasNext()) && (sheetCount == 0)){
            StreamedSheet sheet = sheetIterator.next();
            
            Iterator<StreamedRow> rows = sheet.getAllRows();
            
            while(rows.hasNext()){
                long count = 0;
                StreamedRow row = rows.next();
                
                Iterator<StreamedCell> cellIterator = row.getCellIterator();
                
                while(cellIterator.hasNext()){
                    cellIterator.next();
                    count++;
                }
                
                assertEquals(7, count);
            }
            
            sheetCount++;
        }
        
        workbook.close();
    }
    
    
    @Test
    public void testStartingRowAndCellNumber() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;
        
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while((sheetIterator.hasNext()) && (sheetCount == 0)){
            int rowCount = 0;
            int cellCount = 0;
            StreamedSheet sheet = sheetIterator.next();
            
            Iterator<StreamedRow> rows = sheet.getAllRows();
            
            while(rows.hasNext()){
                StreamedRow row = rows.next();
                if(rowCount == 0){
                    assertEquals(rowCount, row.getRowNum());
                }
                rowCount++;
                
                
                Iterator<StreamedCell> cellIterator = row.getCellIterator();
                
                while(cellIterator.hasNext()){
                    StreamedCell cell = cellIterator.next();
                    if(cellCount == 0){
                        assertEquals(cellCount, cell.getCellNumber());
                    }
                    cellCount++;
                    
                }
                
            }
            
            sheetCount++;
        }
        
        workbook.close();
    }
    
    @Test
    public void testSheetData() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;
        
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while((sheetIterator.hasNext()) && (sheetCount == 0)){
            int rowCount = 0;
            
            StreamedSheet sheet = sheetIterator.next();
            
            Iterator<StreamedRow> rows = sheet.getAllRows();
            
            while(rows.hasNext()){
                
                StreamedRow row = rows.next();
                
                int cellCount = 0;
                
                Iterator<StreamedCell> cellIterator = row.getCellIterator();
                
                while(cellIterator.hasNext()){
                    StreamedCell cell = cellIterator.next();
                    if(rowCount == 1){
                        
                        if(cellCount == 0){
                            assertEquals("1", cell.getValue());
                        }else if(cellCount == 1){
                            assertEquals("Item1", cell.getValue());
                        }else if(cellCount == 2){
                            assertEquals("201", cell.getValue());
                        }else if(cellCount == 3){
                            assertEquals("100.11", cell.getValue());
                        }else if(cellCount == 4){
                            assertEquals("TRUE", cell.getValue());
                        }else if(cellCount == 5){
                            assertEquals("04/02/1917", cell.getValue());
                        }else if(cellCount == 6){
                            assertEquals("90.11", cell.getValue());
                        }
                    }else if(rowCount == 3){
                        if(cellCount == 4){
                            assertEquals(null, cell.getValue());
                        }
                    }
                    
                    cellCount++;
                    
                }
                
                
                rowCount++;
                
            }
            
            sheetCount++;
        }
        
        workbook.close();
    }
    
    @Test
    public void testBatchData() throws Exception{
        
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;
        
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while(sheetIterator.hasNext()){
            
            StreamedSheet sheet = sheetIterator.next();
            
            if(sheetCount == 1){
                Iterator<StreamedRow> rows = sheet.getNRows(1);
                
                while(rows.hasNext()){
                    StreamedRow row = rows.next();
                    assertEquals("Row Number:0 --> Item | item description | Strore | Price | Promotion applied | MFD | Discount rate |", row.toString().trim());
                }
                
                
                rows = sheet.getNRows(4);
                
                while(rows.hasNext()){
                    StreamedRow row = rows.next();
                    assertEquals("Row Number:1 --> 1 | Item1 | 201 | 100.11 | TRUE | 04/02/1917 | 90.11 |", row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:2 --> 2 | Item2 | 202 | 101.11 | TRUE | 05/02/1917 | 91.11 |", row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:3 --> 3 | Item3 | 203 | 102.11 | TRUE | 06/02/1917 | 92.11 |", row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:4 --> 4 | Item4 | 204 | 103.11 | TRUE | 07/02/1917 | 93.11 |", row.toString().trim());
                }
                
                
                
                rows = sheet.getNRows(4);
                
                while(rows.hasNext()){
                    StreamedRow row = rows.next();
                    assertEquals("Row Number:5 --> 5 | Item5 | 205 | 104.11 | TRUE | 08/02/1917 | 94.11 |", row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:6 --> 6 | Item6 | 206 | 105.11 | TRUE | 09/02/1917 | 95.11 |", row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:7 --> 7 | Item7 | 207 | 106.11 | TRUE | 10/02/1917 | 96.11 |", row.toString().trim());
                    row = rows.next();
                    assertEquals("Row Number:8 --> 8 | Item8 | 208 | 107.11 | FALSE | 11/02/1917 | 97.11 |", row.toString().trim());
                }
                
            }
            

            sheetCount++;
            
        }
        
        workbook.close();
        
    }
    
    
    @Test
    public void testGetCell() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;
        
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while((sheetIterator.hasNext()) && (sheetCount == 0)){
            
            StreamedSheet sheet = sheetIterator.next();
            
            Iterator<StreamedRow> rows = sheet.getAllRows();
            
            while(rows.hasNext()){
                
                StreamedRow row = rows.next();
                
                if(row.getRowNum() == 3){
                    StreamedCell cell = (StreamedCell)row.getCell(5);
                    assertEquals("06/02/1917", cell.getValue());
                    
                    try{
                        cell = (StreamedCell)row.getCell(10);
                    }catch(Exception exception){
                        assertEquals(true, exception instanceof IndexOutOfBoundsException);
                    }
                }
                

            }
            
            sheetCount++;
        }
    }
    
    
    @Test
    public void testGetFirstAndLastCellNum() throws Exception{
        POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
        File f= files.getFile("SpreadSheetSample04022017.xlsx");
        StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
        int sheetCount = 0;
        
        
        Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        
        while((sheetIterator.hasNext()) && (sheetCount == 0)){
            
            StreamedSheet sheet = sheetIterator.next();
            
            Iterator<StreamedRow> rows = sheet.getAllRows();
            
            while(rows.hasNext()){
                
                StreamedRow row = rows.next();
                
                if(row.getRowNum() == 3){
                    StreamedCell cell = (StreamedCell)row.getCell(row.getFirstCellNum());
                    assertEquals("3", cell.getValue());
                    assertEquals("3", cell.getStringCellValue());
                    
                    cell = (StreamedCell)row.getCell(row.getLastCellNum());
                    assertEquals("92.11", cell.getValue());
                    assertEquals("92.11", cell.getStringCellValue());
                }
            }
            
            sheetCount++;
        }
    }
    
    @Test
    public void testReopenClosedWorkbook(){
        
        try{
            POIDataSamples files = POIDataSamples.getSpreadSheetInstance();
            File f= files.getFile("SpreadSheetSample04022017.xlsx");
            StreamedWorkbook workbook = new StreamedWorkbook(f.getAbsolutePath());
            
            workbook.close();
            
            Iterator<StreamedSheet> sheetIterator = workbook.getSheetIterator();
        }catch(Exception e){
            assertEquals("Workbook already closed", e.getMessage());
        }
            

    }
    
    
    
    
}
