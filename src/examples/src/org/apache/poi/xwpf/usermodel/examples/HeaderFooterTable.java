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
package org.apache.poi.xwpf.usermodel.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGrid;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblGridCol;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblLayoutType;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblLayoutType;

public class HeaderFooterTable {

    public static void main(String[] args) throws IOException {
        try (XWPFDocument doc = new XWPFDocument()) {

            // Create a header with a 1 row, 3 column table
            // changes made for issue 57366 allow a new header or footer
            // to be created empty. This is a change. You will have to add
            // either a paragraph or a table to the header or footer for
            // the document to be considered valid.
            XWPFHeader hdr = doc.createHeader(HeaderFooterType.DEFAULT);
            XWPFTable tbl = hdr.createTable(1, 3);

            // Set the padding around text in the cells to 1/10th of an inch
            int pad = (int) (.1 * 1440);
            tbl.setCellMargins(pad, pad, pad, pad);

            // Set table width to 6.5 inches in 1440ths of a point
            tbl.setWidth((int) (6.5 * 1440));
            // Can not yet set table or cell width properly, tables default to
            // autofit layout, and this requires fixed layout
            CTTbl ctTbl = tbl.getCTTbl();
            CTTblPr ctTblPr = ctTbl.addNewTblPr();
            CTTblLayoutType layoutType = ctTblPr.addNewTblLayout();
            layoutType.setType(STTblLayoutType.FIXED);

            // Now set up a grid for the table, cells will fit into the grid
            // Each cell width is 3120 in 1440ths of an inch, or 1/3rd of 6.5"
            BigInteger w = new BigInteger("3120");
            CTTblGrid grid = ctTbl.addNewTblGrid();
            for (int i = 0; i < 3; i++) {
                CTTblGridCol gridCol = grid.addNewGridCol();
                gridCol.setW(w);
            }

            // Add paragraphs to the cells
            XWPFTableRow row = tbl.getRow(0);
            XWPFTableCell cell = row.getCell(0);
            XWPFParagraph p = cell.getParagraphArray(0);
            XWPFRun r = p.createRun();
            r.setText("header left cell");

            cell = row.getCell(1);
            p = cell.getParagraphArray(0);
            r = p.createRun();
            r.setText("header center cell");

            cell = row.getCell(2);
            p = cell.getParagraphArray(0);
            r = p.createRun();
            r.setText("header right cell");

            // Create a footer with a Paragraph
            XWPFFooter ftr = doc.createFooter(HeaderFooterType.DEFAULT);
            p = ftr.createParagraph();

            r = p.createRun();
            r.setText("footer text");

            try (OutputStream os = new FileOutputStream(new File("headertable.docx"))) {
                doc.write(os);
            }
        }
	}
}
