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
package org.apache.poi.xwpf.usermodel;

import java.math.BigInteger;

import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.apache.poi.util.Internal;

/**
 * Sketch of XWPFTable class. Only table's text is being hold.
 * <p/>
 * Specifies the contents of a table present in the document. A table is a set
 * of paragraphs (and other block-level content) arranged in rows and columns.
 *
 * @author Yury Batrakov (batrakov at gmail.com)
 */
public class XWPFTable {

    protected StringBuffer text = new StringBuffer();
    private CTTbl ctTbl;


    public XWPFTable(XWPFDocument doc, CTTbl table, int row, int col) {
        this(doc, table);
        for (int i = 0; i < row; i++) {
            XWPFTableRow tabRow = (getRow(i) == null) ? createRow() : getRow(i);
            for (int k = 0; k < col; k++) {
                XWPFTableCell tabCell = (tabRow.getCell(k) == null) ? tabRow
                        .createCell() : null;
            }
        }
    }


    public XWPFTable(XWPFDocument doc, CTTbl table) {
        this.ctTbl = table;

        // is an empty table: I add one row and one column as default
        if (table.sizeOfTrArray() == 0)
            createEmptyTable(table);

        for (CTRow row : table.getTrArray()) {
            StringBuffer rowText = new StringBuffer();
            for (CTTc cell : row.getTcArray()) {
                for (CTP ctp : cell.getPArray()) {
                    XWPFParagraph p = new XWPFParagraph(ctp, doc);
                    if (rowText.length() > 0) {
                        rowText.append('\t');
                    }
                    rowText.append(p.getText());
                }
            }
            if (rowText.length() > 0) {
                this.text.append(rowText);
                this.text.append('\n');
            }
        }
    }

    private void createEmptyTable(CTTbl table) {
        // MINIMUM ELEMENTS FOR A TABLE
        table.addNewTr().addNewTc().addNewP();

        CTTblPr tblpro = table.addNewTblPr();
        tblpro.addNewTblW().setW(new BigInteger("0"));
        tblpro.getTblW().setType(STTblWidth.AUTO);

        // layout
        // tblpro.addNewTblLayout().setType(STTblLayoutType.AUTOFIT);

        // borders
        CTTblBorders borders = tblpro.addNewTblBorders();
        borders.addNewBottom().setVal(STBorder.SINGLE);
        borders.addNewInsideH().setVal(STBorder.SINGLE);
        borders.addNewInsideV().setVal(STBorder.SINGLE);
        borders.addNewLeft().setVal(STBorder.SINGLE);
        borders.addNewRight().setVal(STBorder.SINGLE);
        borders.addNewTop().setVal(STBorder.SINGLE);

        /*
       * CTTblGrid tblgrid=table.addNewTblGrid();
       * tblgrid.addNewGridCol().setW(new BigInteger("2000"));
       */

    }

    /**
     * @return ctTbl object
     */
    @Internal
    public CTTbl getCTTbl() {
        return ctTbl;
    }

    /**
     * @return text
     */
    public String getText() {
        return text.toString();
    }


    public void addNewRowBetween(int start, int end) {
        // TODO
    }


    /**
     * add a new column for each row in this table
     */
    public void addNewCol() {
        if (ctTbl.sizeOfTrArray() == 0) createRow();
        for (int i = 0; i < ctTbl.sizeOfTrArray(); i++) {
            XWPFTableRow tabRow = new XWPFTableRow(ctTbl.getTrArray(i));
            tabRow.createCell();
        }
    }

    /**
     * create a new XWPFTableRow object with as many cells as the number of columns defined in that moment
     *
     * @return tableRow
     */
    public XWPFTableRow createRow() {
        int sizeCol = ctTbl.sizeOfTrArray() > 0 ? ctTbl.getTrArray(0)
                .sizeOfTcArray() : 0;
        XWPFTableRow tabRow = new XWPFTableRow(ctTbl.addNewTr());
        addColumn(tabRow, sizeCol);
        return tabRow;
    }

    /**
     * @param pos - index of the row
     * @return the row at the position specified or null if no rows is defined or if the position is greather than the max size of rows array
     */
    public XWPFTableRow getRow(int pos) {
        if (pos >= 0 && pos < ctTbl.sizeOfTrArray()) {
            return new XWPFTableRow(ctTbl.getTrArray(pos));
        }
        return null;
    }


    /**
     * @param width
     */
    public void setWidth(int width) {
        CTTblPr tblPr = getTrPr();
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr
                .addNewTblW();
        tblWidth.setW(new BigInteger("" + width));
    }

    /**
     * @return width value
     */
    public int getWidth() {
        CTTblPr tblPr = getTrPr();
        return tblPr.isSetTblW() ? tblPr.getTblW().getW().intValue() : -1;
    }

    /**
     * @return number of rows in table
     */
    public int getNumberOfRows() {
        return ctTbl.sizeOfTrArray();
    }

    private CTTblPr getTrPr() {
        return (ctTbl.getTblPr() != null) ? ctTbl.getTblPr() : ctTbl
                .addNewTblPr();
    }

    private void addColumn(XWPFTableRow tabRow, int sizeCol) {
        if (sizeCol > 0) {
            for (int i = 0; i < sizeCol; i++) {
                tabRow.createCell();
            }
        }
    }

}// end class
