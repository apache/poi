
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

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;

/**
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class RepeatingRowsAndColumns
{
    public static void main(String[] args)
        throws IOException
    {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet1 = wb.createSheet("first sheet");
        HSSFSheet sheet2 = wb.createSheet("second sheet");
        HSSFSheet sheet3 = wb.createSheet("third sheet");

//        POIFSFileSystem fs      =
//                new POIFSFileSystem(new FileInputStream("workbook.xls"));
//        HSSFWorkbook wb = new HSSFWorkbook(fs);
//        HSSFSheet sheet1 = wb.getSheetAt(0);

        HSSFFont boldFont = wb.createFont();
        boldFont.setFontHeightInPoints((short)22);
        boldFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

        HSSFCellStyle boldStyle = wb.createCellStyle();
        boldStyle.setFont(boldFont);

        HSSFRow row = sheet1.createRow((short)1);
        HSSFCell cell = row.createCell((short)0);
        cell.setCellValue("This quick brown fox");
        cell.setCellStyle(boldStyle);

        // Set the columns to repeat from column 0 to 2 on the first sheet
        wb.setRepeatingRowsAndColumns(0,0,2,-1,-1);
        // Set the rows to repeat from row 0 to 2 on the second sheet.
        wb.setRepeatingRowsAndColumns(1,-1,-1,0,2);
        // Set the the repeating rows and columns on the third sheet.
        wb.setRepeatingRowsAndColumns(2,4,5,1,2);

        FileOutputStream fileOut = new FileOutputStream("workbook.xls");
        wb.write(fileOut);
        fileOut.close();
    }
}
