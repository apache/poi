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

import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.*;

import java.io.FileOutputStream;
import java.io.Writer;
import java.io.StringWriter;

/**
 *  Demonstrates how to work with rich text
 */
public class WorkingWithRichText {

    public static void main(String[] args)
                throws Exception
        {
            XSSFWorkbook wb = new XSSFWorkbook();

            XSSFSheet sheet = wb.createSheet();
            XSSFRow row = sheet.createRow((short) 2);

            XSSFCell cell = row.createCell(1);
            XSSFRichTextString rt = new XSSFRichTextString("The quick");

            XSSFFont font1 = wb.createFont();
            font1.setBold(true);
            rt.append(" brown fox", font1);

            XSSFFont font2 = wb.createFont();
            font2.setItalic(true);
            font2.setColor(IndexedColors.RED.getIndex());
            rt.applyFont((short)0);
            cell.setCellValue(rt);

            // Write the output to a file
            FileOutputStream fileOut = new FileOutputStream("rich_text.xlsx");
            wb.write(fileOut);
            fileOut.close();

        }

 }
