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

package org.apache.poi.hssf.usermodel.examples;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;

/**
 * Demonstrates many features of the user API at once.  Used in the HOW-TO guide.
 */
public class BigExample {
    public static void main(String[] args) throws IOException {
        int rownum;

        // create a new workbook
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            // create a new sheet
            HSSFSheet s = wb.createSheet();
            // declare a row object reference
            HSSFRow r;
            // declare a cell object reference
            HSSFCell c;
            // create 3 cell styles
            HSSFCellStyle cs = wb.createCellStyle();
            HSSFCellStyle cs2 = wb.createCellStyle();
            HSSFCellStyle cs3 = wb.createCellStyle();
            // create 2 fonts objects
            HSSFFont f = wb.createFont();
            HSSFFont f2 = wb.createFont();

            //set font 1 to 12 point type
            f.setFontHeightInPoints((short) 12);
            //make it red
            f.setColor(HSSFColorPredefined.RED.getIndex());
            // make it bold
            //arial is the default font
            f.setBold(true);

            //set font 2 to 10 point type
            f2.setFontHeightInPoints((short) 10);
            //make it the color at palette index 0xf (white)
            f2.setColor(HSSFColorPredefined.WHITE.getIndex());
            //make it bold
            f2.setBold(true);

            //set cell style
            cs.setFont(f);
            //set the cell format see HSSFDataFormat for a full list
            cs.setDataFormat(HSSFDataFormat.getBuiltinFormat("($#,##0_);[Red]($#,##0)"));

            //set a thin border
            cs2.setBorderBottom(BorderStyle.THIN);
            //fill w fg fill color
            cs2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            // set foreground fill to red
            cs2.setFillForegroundColor(HSSFColorPredefined.RED.getIndex());

            // set the font
            cs2.setFont(f2);

            // set the sheet name to HSSF Test
            wb.setSheetName(0, "HSSF Test");
            // create a sheet with 300 rows (0-299)
            for (rownum = 0; rownum < 300; rownum++) {
                // create a row
                r = s.createRow(rownum);
                // on every other row
                if ((rownum % 2) == 0) {
                    // make the row height bigger  (in twips - 1/20 of a point)
                    r.setHeight((short) 0x249);
                }

                //r.setRowNum(( short ) rownum);
                // create 50 cells (0-49) (the += 2 becomes apparent later
                for (int cellnum = 0; cellnum < 50; cellnum += 2) {
                    // create a numeric cell
                    c = r.createCell(cellnum);
                    // do some goofy math to demonstrate decimals
                    c.setCellValue((rownum * 10000.0) + cellnum
                            + (rownum / 1000.0)
                            + (cellnum / 10000.0));

                    // on every other row
                    if ((rownum % 2) == 0) {
                        // set this cell to the first cell style we defined
                        c.setCellStyle(cs);
                    }

                    // create a string cell (see why += 2 in the
                    c = r.createCell(cellnum + 1);

                    // set the cell's string value to "TEST"
                    c.setCellValue("TEST");
                    // make this column a bit wider
                    s.setColumnWidth(cellnum + 1, (int) ((50 * 8) / ((double) 1 / 20)));

                    // on every other row
                    if ((rownum % 2) == 0) {
                        // set this to the white on red cell style
                        // we defined above
                        c.setCellStyle(cs2);
                    }

                }
            }

            //draw a thick black border on the row at the bottom using BLANKS
            // advance 2 rows
            rownum++;
            rownum++;

            r = s.createRow(rownum);

            // define the third style to be the default
            // except with a thick black border at the bottom
            cs3.setBorderBottom(BorderStyle.THICK);

            //create 50 cells
            for (int cellnum = 0; cellnum < 50; cellnum++) {
                //create a blank type cell (no value)
                c = r.createCell(cellnum);
                // set it to the thick black border style
                c.setCellStyle(cs3);
            }

            //end draw thick black border


            // demonstrate adding/naming and deleting a sheet
            // create a sheet, set its title then delete it
            wb.createSheet();
            wb.setSheetName(1, "DeletedSheet");
            wb.removeSheetAt(1);
            //end deleted sheet

            // create a new file
            try (FileOutputStream out = new FileOutputStream("workbook.xls")) {
                // write the workbook to the output stream
                // close our file (don't blow out our file handles
                wb.write(out);
            }
        }
    }
}
