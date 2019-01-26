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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * This example demonstrates opening a workbook, modifying it and writing
 * the results back out.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class ReadWriteWorkbook {
    public static void main(String[] args) throws IOException {
        try (FileInputStream fileIn = new FileInputStream("workbook.xls")) {
            POIFSFileSystem fs = new POIFSFileSystem(fileIn);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row = sheet.getRow(2);
            if (row == null)
                row = sheet.createRow(2);
            HSSFCell cell = row.getCell(3);
            if (cell == null)
                cell = row.createCell(3);
            cell.setCellValue("a test");

            // Write the output to a file
            try (FileOutputStream fileOut = new FileOutputStream("workbookout.xls")) {
                wb.write(fileOut);
            }
        }
    }
}
