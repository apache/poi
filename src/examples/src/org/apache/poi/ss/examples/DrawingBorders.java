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
import org.apache.poi.ss.usermodel.BorderExtent;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PropertyTemplate;
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
        try (Workbook wb = (args.length > 0 && args[0].equals("-xls"))
            ? new HSSFWorkbook() : new XSSFWorkbook()) {
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
            PropertyTemplate pt = new PropertyTemplate();
            // #1) these borders will all be medium in default color
            pt.drawBorders(new CellRangeAddress(1, 3, 1, 3),
                    BorderStyle.MEDIUM, BorderExtent.ALL);
            // #2) these cells will have medium outside borders and thin inside borders
            pt.drawBorders(new CellRangeAddress(5, 7, 1, 3),
                    BorderStyle.MEDIUM, BorderExtent.OUTSIDE);
            pt.drawBorders(new CellRangeAddress(5, 7, 1, 3), BorderStyle.THIN,
                    BorderExtent.INSIDE);
            // #3) these cells will all be medium weight with different colors for the
            //     outside, inside horizontal, and inside vertical borders. The center
            //     cell will have no borders.
            pt.drawBorders(new CellRangeAddress(9, 11, 1, 3),
                    BorderStyle.MEDIUM, IndexedColors.RED.getIndex(),
                    BorderExtent.OUTSIDE);
            pt.drawBorders(new CellRangeAddress(9, 11, 1, 3),
                    BorderStyle.MEDIUM, IndexedColors.BLUE.getIndex(),
                    BorderExtent.INSIDE_VERTICAL);
            pt.drawBorders(new CellRangeAddress(9, 11, 1, 3),
                    BorderStyle.MEDIUM, IndexedColors.GREEN.getIndex(),
                    BorderExtent.INSIDE_HORIZONTAL);
            pt.drawBorders(new CellRangeAddress(10, 10, 2, 2),
                    BorderStyle.NONE,
                    BorderExtent.ALL);

            // apply borders to sheet
            pt.applyBorders(sh1);

            // add another sheet and apply the borders to it
            Sheet sh2 = wb.createSheet("Sheet2");
            pt.applyBorders(sh2);

            // Write the output to a file
            String file = "db-poi.xls" + (wb instanceof XSSFWorkbook ? "x" : "");
            try (FileOutputStream out = new FileOutputStream(file)) {
                wb.write(out);
            }
            System.out.println("Generated: " + file);
        }
    }

}
