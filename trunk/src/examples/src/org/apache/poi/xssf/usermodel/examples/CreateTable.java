/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xssf.usermodel.examples;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTable;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTable;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumn;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableColumns;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTTableStyleInfo;

/**
 * Demonstrates how to create a simple table using Apache POI.
 */
public class CreateTable {
        
    public static void main(String[] args) throws IOException {
        
        Workbook wb = new XSSFWorkbook();
        XSSFSheet sheet = (XSSFSheet) wb.createSheet();
        
        //Create 
        XSSFTable table = sheet.createTable();
        table.setDisplayName("Test");       
        CTTable cttable = table.getCTTable();
        
        //Style configurations
        CTTableStyleInfo style = cttable.addNewTableStyleInfo();
        style.setName("TableStyleMedium2");
        style.setShowColumnStripes(false);
        style.setShowRowStripes(true);
        
        //Set which area the table should be placed in
        AreaReference reference = new AreaReference(new CellReference(0, 0), 
                new CellReference(2,2));
        cttable.setRef(reference.formatAsString());
        cttable.setId(1);
        cttable.setName("Test");
        cttable.setTotalsRowCount(1);
        
        CTTableColumns columns = cttable.addNewTableColumns();
        columns.setCount(3);
        CTTableColumn column;
        XSSFRow row;
        XSSFCell cell;
        for(int i=0; i<3; i++) {
            //Create column
            column = columns.addNewTableColumn();
            column.setName("Column");
            column.setId(i+1);
            //Create row
            row = sheet.createRow(i);
            for(int j=0; j<3; j++) {
                //Create cell
                cell = row.createCell(j);
                if(i == 0) {
                    cell.setCellValue("Column"+j);
                } else {
                    cell.setCellValue("0");
                }
            }
        }
        
        FileOutputStream fileOut = new FileOutputStream("ooxml-table.xlsx");
        wb.write(fileOut);
        fileOut.close();
        wb.close();
    }
}
