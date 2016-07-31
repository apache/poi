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

package org.apache.poi.ss.examples;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.BorderExtent;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellStyleTemplate;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Excel Border Drawing - examples
 * 
 * <p>
 * Partly based on the code snippets from
 * org.apache.poi.ss.examples.ConditionalFormats
 * </p>
 */
public class DrawingBorders {

    public static void main(String[] args) throws IOException {
        Workbook wb;

        if (args.length > 0 && args[0].equals("-xls")) {
            wb = new HSSFWorkbook();
        } else {
            wb = new XSSFWorkbook();
        }

        // add a sheet, and put some values into it
        Sheet sh1 = wb.createSheet("Sheet1");
        Row r = sh1.createRow(0);
        Cell c = r.createCell(1);
        c.setCellValue("All Borders Medium Width");
        r = sh1.createRow(4);
        c = r.createCell(1);
        c.setCellValue("Medium Outside / Thin Inside Borders");
        r = sh1.createRow(8);
        c = r.createCell(1);
        c.setCellValue("Colored Borders");

        // draw borders (three 3x3 grids)
        CellStyleTemplate cst = new CellStyleTemplate();
        // #1) these borders will all be medium in default color
        cst.drawBorders(CellRangeAddress.valueOf("B2:D5"),
                BorderStyle.MEDIUM, BorderExtent.ALL);
        // #2) these cells will have medium outside borders and thin inside borders
        cst.drawBorders(CellRangeAddress.valueOf("E2:G5"),
                BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
        cst.drawBorders(CellRangeAddress.valueOf("E2:G5"), 
                BorderStyle.THIN, BorderExtent.INSIDE);
        // #3) these cells will all be medium weight with different colors for the
        //     outside, inside horizontal, and inside vertical borders. The center
        //     cell will have no borders.
        cst.drawBorders(CellRangeAddress.valueOf("I2:K5"),
                BorderStyle.MEDIUM, IndexedColors.RED.getIndex(),
                BorderExtent.OUTSIDE);
        cst.drawBorders(CellRangeAddress.valueOf("I2:K5"),
                BorderStyle.MEDIUM, IndexedColors.BLUE.getIndex(),
                BorderExtent.INSIDE_VERTICAL);
        cst.drawBorders(CellRangeAddress.valueOf("I2:K5"),
                BorderStyle.MEDIUM, IndexedColors.GREEN.getIndex(),
                BorderExtent.INSIDE_HORIZONTAL);
        cst.drawBorders(CellRangeAddress.valueOf("J3"),
                BorderStyle.NONE, 
                BorderExtent.ALL);

        // apply borders to sheet
        cst.applyBorders(sh1);
        
        // add another sheet and apply the borders to it
        Sheet sh2 = wb.createSheet("Sheet2");
        cst.applyBorders(sh2);

        // Write the output to a file
        String file = "db-poi.xls";
        if (wb instanceof XSSFWorkbook)
            file += "x";
        FileOutputStream out = new FileOutputStream(file);
        wb.write(out);
        out.close();
        wb.close();
        System.out.println("Generated: " + file);
    }

}
