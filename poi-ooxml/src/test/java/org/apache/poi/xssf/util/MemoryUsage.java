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

package org.apache.poi.xssf.util;

import junit.framework.TestCase;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Mixed utilities for testing memory usage in XSSF
 *
 * @author Yegor Kozlov
 */
public class MemoryUsage extends TestCase {
    private static final int NUM_COLUMNS = 255;

    /**
     * Generate a spreadsheet until OutOfMemoryError
     * <p>
     *  cells in even columns are numbers, cells in odd columns are strings
     * </p>
     *
     * @param wb        the workbook to write to
     * @param numCols   the number of columns in a row
     */
    public static void mixedSpreadsheet(Workbook wb, int numCols){

        System.out.println("Testing " + wb.getClass().getName());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory()/(1024*1024) + "MB");
        int i=0, cnt=0;
        try {
            Sheet sh = wb.createSheet();
            for(i=0; ; i++){
                Row row = sh.createRow(i);
                for(int j=0; j < numCols; j++){
                    Cell cell = row.createCell(j);
                    if(j % 2 == 0) cell.setCellValue(j);
                    else cell.setCellValue(new CellReference(j, i).formatAsString());
                    cnt++;
                }
            }
        } catch (OutOfMemoryError er){
            System.out.println("Failed at row=" + i + ", objects : " + cnt);
        }
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory()/(1024*1024) + "MB");
    }

    /**
     * Generate a spreadsheet who's all cell values are numbers.
     * The data is generated until OutOfMemoryError. 
     * <p>
     * as compared to {@link #mixedSpreadsheet(org.apache.poi.ss.usermodel.Workbook, int)},
     * this method does not set string values and, hence, does not invole the Shared Strings Table.
     * </p>
     *
     * @param wb        the workbook to write to
     * @param numCols   the number of columns in a row
     */
    public static void numberSpreadsheet(Workbook wb, int numCols){

        System.out.println("Testing " + wb.getClass().getName());
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory()/(1024*1024) + "MB");
        int i=0, cnt=0;
        try {
            Sheet sh = wb.createSheet();
            for(i=0; ; i++){
                Row row = sh.createRow(i);
                for(int j=0; j < numCols; j++){
                    Cell cell = row.createCell(j);
                    cell.setCellValue(j);
                    cnt++;
                }
            }
        } catch (OutOfMemoryError er){
            System.out.println("Failed at row=" + i + ", objects : " + cnt);
        }
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory()/(1024*1024) + "MB");
    }

    /**
     * Generate a spreadsheet until OutOfMemoryError using low-level OOXML XmlBeans.
     * Similar to {@link #numberSpreadsheet(org.apache.poi.ss.usermodel.Workbook, int)}
     *
     * <p>
     *
     * @param numCols  the number of columns in a row
     */
    public static void xmlBeans(int numCols) {
        int i = 0, cnt = 0;
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");

        CTWorksheet sh = CTWorksheet.Factory.newInstance();
        CTSheetData data = sh.addNewSheetData();
        try {
            for (i = 0; ; i++) {
                CTRow row = data.addNewRow();
                row.setR(i);
                for (int j = 0; j < numCols; j++) {
                    CTCell cell = row.addNewC();
                    cell.setT(STCellType.N);
                    cell.setV(String.valueOf(j));
                    cnt++;
                }
            }
        } catch (OutOfMemoryError er) {
            System.out.println("Failed at row=" + i + ", objects: " + cnt);
        }
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
    }

    /**
     * Generate detached (parentless) Xml beans until OutOfMemoryError
     *
     * @see #testXmlAttached()
     */
    public void testXmlDetached(){
        List<CTRow> rows = new ArrayList<CTRow>();
        int i = 0;
        try {
            for(;;){
                //create a standalone CTRow bean
                CTRow r = CTRow.Factory.newInstance();
                r.setR(++i);
                rows.add(r);
            }
        } catch (OutOfMemoryError er) {
            System.out.println("Failed at row=" + i);
        }
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
    }

    /**
     * Generate atatched (having a parent bean) Xml beans until OutOfMemoryError.
     * This is MUCH more memory-efficient than {@link #testXmlDetached()}
     *
     * @see #testXmlAttached()
     */
    public void testXmlAttached(){
        List<CTRow> rows = new ArrayList<CTRow>();
        int i = 0;
        //top-level element in sheet.xml
        CTWorksheet sh = CTWorksheet.Factory.newInstance();
        CTSheetData data = sh.addNewSheetData();
        try {
            for(;;){
                //create CTRow attached to the parent object
                CTRow r = data.addNewRow();
                r.setR(++i);
                rows.add(r);
            }
        } catch (OutOfMemoryError er) {
            System.out.println("Failed at row=" + i);
        }
        System.out.println("Memory: " + Runtime.getRuntime().totalMemory() / (1024 * 1024) + "MB");
    }

    public void testMixedHSSF(){
        numberSpreadsheet(new HSSFWorkbook(), NUM_COLUMNS);
    }

    public void testMixedXSSF(){
        numberSpreadsheet(new XSSFWorkbook(), NUM_COLUMNS);
    }

    public void testNumberHSSF(){
        numberSpreadsheet(new HSSFWorkbook(), NUM_COLUMNS);
    }

    public void testNumberXSSF(){
        numberSpreadsheet(new XSSFWorkbook(), NUM_COLUMNS);
    }

}