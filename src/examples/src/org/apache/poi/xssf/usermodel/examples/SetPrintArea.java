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

public class SetPrintArea {


    public static void main(String[]args) throws Exception {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Sheet1");
        //wb.setPrintArea(0, "$A$1:$C$2");
        //sets the print area for the first sheet
        //Alternatively:
        wb.setPrintArea(0, 1, 2, 0, 3); //is equivalent to using the name reference (See the JavaDocs for more details)

        // Create various cells and rows for spreadsheet.

        FileOutputStream fileOut = new FileOutputStream("printArea.xlsx");
        wb.write(fileOut);
        fileOut.close();
    }
}
