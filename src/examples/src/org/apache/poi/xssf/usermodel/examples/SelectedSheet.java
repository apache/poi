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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class SelectedSheet {

    public static void main(String[]args) throws Exception {
        Workbook wb = new XSSFWorkbook(); //or new HSSFWorkbook();

        Sheet sheet = wb.createSheet("row sheet");
        Sheet sheet2 = wb.createSheet("another sheet");
        Sheet sheet3 = wb.createSheet(" sheet 3 ");
        sheet3.setSelected(true);
        wb.setActiveSheet(2);

        // Create various cells and rows for spreadsheet.

        FileOutputStream fileOut = new FileOutputStream("selectedSheet.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }

}
