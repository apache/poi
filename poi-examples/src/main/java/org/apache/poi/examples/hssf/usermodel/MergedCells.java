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

package org.apache.poi.examples.hssf.usermodel;

import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * An example of how to merge regions of cells.
 */
public class MergedCells {
   public static void main(String[] args) throws IOException {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
             HSSFSheet sheet = wb.createSheet("new sheet");

             HSSFRow row = sheet.createRow(1);
             HSSFCell cell = row.createCell(1);
             cell.setCellValue("This is a test of merging");

             sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 2));

             // Write the output to a file
             try (FileOutputStream fileOut = new FileOutputStream("workbook.xls")) {
                  wb.write(fileOut);
             }
        }
    }
}
