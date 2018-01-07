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

package org.apache.poi.xslf.usermodel;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.sl.usermodel.TableCell.BorderEdge;
import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;

/**
 * PPTX Tables
 */
public class Tutorial4 {

    public static void main(String[] args) throws IOException{
        try (XMLSlideShow ppt = new XMLSlideShow()) {
            // XSLFSlide#createSlide() with no arguments creates a blank slide
            XSLFSlide slide = ppt.createSlide();

            XSLFTable tbl = slide.createTable();
            tbl.setAnchor(new Rectangle(50, 50, 450, 300));

            int numColumns = 3;
            int numRows = 5;
            XSLFTableRow headerRow = tbl.addRow();
            headerRow.setHeight(50);
            // header
            for (int i = 0; i < numColumns; i++) {
                XSLFTableCell th = headerRow.addCell();
                XSLFTextParagraph p = th.addNewTextParagraph();
                p.setTextAlign(TextAlign.CENTER);
                XSLFTextRun r = p.addNewTextRun();
                r.setText("Header " + (i + 1));
                r.setBold(true);
                r.setFontColor(Color.white);
                th.setFillColor(new Color(79, 129, 189));
                th.setBorderWidth(BorderEdge.bottom, 2.0);
                th.setBorderColor(BorderEdge.bottom, Color.white);

                tbl.setColumnWidth(i, 150);  // all columns are equally sized
            }

            // rows

            for (int rownum = 0; rownum < numRows; rownum++) {
                XSLFTableRow tr = tbl.addRow();
                tr.setHeight(50);
                // header
                for (int i = 0; i < numColumns; i++) {
                    XSLFTableCell cell = tr.addCell();
                    XSLFTextParagraph p = cell.addNewTextParagraph();
                    XSLFTextRun r = p.addNewTextRun();

                    r.setText("Cell " + (i + 1));
                    if (rownum % 2 == 0)
                        cell.setFillColor(new Color(208, 216, 232));
                    else
                        cell.setFillColor(new Color(233, 247, 244));

                }
            }

            try (FileOutputStream out = new FileOutputStream("table.pptx")) {
                ppt.write(out);
            }
        }
    }
}
