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
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Demonstrates how to work with excel cell comments.
 *
 * <p>
 * Excel comment is a kind of a text shape,
 * so inserting a comment is very similar to placing a text box in a worksheet
 * </p>
 *
 * @author Yegor Kozlov
 */
public class CellComments {
    public static void main(String[] args) throws IOException {
        try (Workbook wb = new XSSFWorkbook()) {

            CreationHelper factory = wb.getCreationHelper();

            Sheet sheet = wb.createSheet();

            Cell cell1 = sheet.createRow(3).createCell(5);
            cell1.setCellValue("F4");

            Drawing<?> drawing = sheet.createDrawingPatriarch();

            ClientAnchor anchor = factory.createClientAnchor();

            Comment comment1 = drawing.createCellComment(anchor);
            RichTextString str1 = factory.createRichTextString("Hello, World!");
            comment1.setString(str1);
            comment1.setAuthor("Apache POI");
            cell1.setCellComment(comment1);

            Cell cell2 = sheet.createRow(2).createCell(2);
            cell2.setCellValue("C3");

            Comment comment2 = drawing.createCellComment(anchor);
            RichTextString str2 = factory.createRichTextString("XSSF can set cell comments");
            //apply custom font to the text in the comment
            Font font = wb.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 14);
            font.setBold(true);
            font.setColor(IndexedColors.RED.getIndex());
            str2.applyFont(font);

            comment2.setString(str2);
            comment2.setAuthor("Apache POI");
            comment2.setAddress(new CellAddress("C3"));

            try (FileOutputStream out = new FileOutputStream("comments.xlsx")) {
                wb.write(out);
            }
        }
    }
}
