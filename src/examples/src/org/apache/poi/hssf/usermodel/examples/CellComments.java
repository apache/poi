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
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFComment;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;

/**
 * Demonstrates how to work with excel cell comments.<p>
 *
 * Excel comment is a kind of a text shape,
 * so inserting a comment is very similar to placing a text box in a worksheet
 */
public class CellComments {

    public static void main(String[] args) throws IOException  {
        try (HSSFWorkbook wb = new HSSFWorkbook()) {
            HSSFSheet sheet = wb.createSheet("Cell comments in POI HSSF");

            // Create the drawing patriarch. This is the top level container for all shapes including cell comments.
            HSSFPatriarch patr = sheet.createDrawingPatriarch();

            //create a cell in row 3
            HSSFCell cell1 = sheet.createRow(3).createCell(1);
            cell1.setCellValue(new HSSFRichTextString("Hello, World"));

            //anchor defines size and position of the comment in worksheet
            HSSFComment comment1 = patr.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));

            // set text in the comment
            comment1.setString(new HSSFRichTextString("We can set comments in POI"));

            //set comment author.
            //you can see it in the status bar when moving mouse over the commented cell
            comment1.setAuthor("Apache Software Foundation");

            // The first way to assign comment to a cell is via HSSFCell.setCellComment method
            cell1.setCellComment(comment1);

            //create another cell in row 6
            HSSFCell cell2 = sheet.createRow(6).createCell(1);
            cell2.setCellValue(36.6);


            HSSFComment comment2 = patr.createComment(new HSSFClientAnchor(0, 0, 0, 0, (short) 4, 8, (short) 6, 11));
            //modify background color of the comment
            comment2.setFillColor(204, 236, 255);

            HSSFRichTextString string = new HSSFRichTextString("Normal body temperature");

            //apply custom font to the text in the comment
            HSSFFont font = wb.createFont();
            font.setFontName("Arial");
            font.setFontHeightInPoints((short) 10);
            font.setBold(true);
            font.setColor(HSSFColorPredefined.RED.getIndex());
            string.applyFont(font);

            comment2.setString(string);
            comment2.setVisible(true); //by default comments are hidden. This one is always visible.

            comment2.setAuthor("Bill Gates");

            /*
             * The second way to assign comment to a cell is to implicitly specify its row and column.
             * Note, it is possible to set row and column of a non-existing cell.
             * It works, the comment is visible.
             */
            comment2.setRow(6);
            comment2.setColumn(1);

            try (FileOutputStream out = new FileOutputStream("poi_comment.xls")) {
                wb.write(out);
            }
        }
    }
}
