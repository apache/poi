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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.util.Internal;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.Removal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTString;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblCellMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STJc;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

/**
 * <p>Sketch of XWPFTable class. Only table's text is being hold.</p>
 * <p>Specifies the contents of a table present in the document. A table is a set
 * of paragraphs (and other block-level content) arranged in rows and columns.</p>
 */
public class XWPFTable implements IBodyElement, ISDTContents {
    private static EnumMap<XWPFBorderType, STBorder.Enum> xwpfBorderTypeMap;
    // Create a map from the STBorder.Enum values to the XWPF-level enums
    private static HashMap<Integer, XWPFBorderType> stBorderTypeMap;

    static {
        // populate enum maps
        xwpfBorderTypeMap = new EnumMap<>(XWPFBorderType.class);
        xwpfBorderTypeMap.put(XWPFBorderType.NIL, STBorder.Enum.forInt(STBorder.INT_NIL));
        xwpfBorderTypeMap.put(XWPFBorderType.NONE, STBorder.Enum.forInt(STBorder.INT_NONE));
        xwpfBorderTypeMap.put(XWPFBorderType.SINGLE, STBorder.Enum.forInt(STBorder.INT_SINGLE));
        xwpfBorderTypeMap.put(XWPFBorderType.THICK, STBorder.Enum.forInt(STBorder.INT_THICK));
        xwpfBorderTypeMap.put(XWPFBorderType.DOUBLE, STBorder.Enum.forInt(STBorder.INT_DOUBLE));
        xwpfBorderTypeMap.put(XWPFBorderType.DOTTED, STBorder.Enum.forInt(STBorder.INT_DOTTED));
        xwpfBorderTypeMap.put(XWPFBorderType.DASHED, STBorder.Enum.forInt(STBorder.INT_DASHED));
        xwpfBorderTypeMap.put(XWPFBorderType.DOT_DASH, STBorder.Enum.forInt(STBorder.INT_DOT_DASH));
        xwpfBorderTypeMap.put(XWPFBorderType.DOT_DOT_DASH, STBorder.Enum.forInt(STBorder.INT_DOT_DOT_DASH));
        xwpfBorderTypeMap.put(XWPFBorderType.TRIPLE, STBorder.Enum.forInt(STBorder.INT_TRIPLE));
        xwpfBorderTypeMap.put(XWPFBorderType.THIN_THICK_SMALL_GAP, STBorder.Enum.forInt(STBorder.INT_THIN_THICK_SMALL_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THICK_THIN_SMALL_GAP, STBorder.Enum.forInt(STBorder.INT_THICK_THIN_SMALL_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THIN_THICK_THIN_SMALL_GAP, STBorder.Enum.forInt(STBorder.INT_THIN_THICK_THIN_SMALL_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THIN_THICK_MEDIUM_GAP, STBorder.Enum.forInt(STBorder.INT_THIN_THICK_MEDIUM_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THICK_THIN_MEDIUM_GAP, STBorder.Enum.forInt(STBorder.INT_THICK_THIN_MEDIUM_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THIN_THICK_THIN_MEDIUM_GAP, STBorder.Enum.forInt(STBorder.INT_THIN_THICK_THIN_MEDIUM_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THIN_THICK_LARGE_GAP, STBorder.Enum.forInt(STBorder.INT_THIN_THICK_LARGE_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THICK_THIN_LARGE_GAP, STBorder.Enum.forInt(STBorder.INT_THICK_THIN_LARGE_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.THIN_THICK_THIN_LARGE_GAP, STBorder.Enum.forInt(STBorder.INT_THIN_THICK_THIN_LARGE_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.WAVE, STBorder.Enum.forInt(STBorder.INT_WAVE));
        xwpfBorderTypeMap.put(XWPFBorderType.DOUBLE_WAVE, STBorder.Enum.forInt(STBorder.INT_DOUBLE_WAVE));
        xwpfBorderTypeMap.put(XWPFBorderType.DASH_SMALL_GAP, STBorder.Enum.forInt(STBorder.INT_DASH_SMALL_GAP));
        xwpfBorderTypeMap.put(XWPFBorderType.DASH_DOT_STROKED, STBorder.Enum.forInt(STBorder.INT_DASH_DOT_STROKED));
        xwpfBorderTypeMap.put(XWPFBorderType.THREE_D_EMBOSS, STBorder.Enum.forInt(STBorder.INT_THREE_D_EMBOSS));
        xwpfBorderTypeMap.put(XWPFBorderType.THREE_D_ENGRAVE, STBorder.Enum.forInt(STBorder.INT_THREE_D_ENGRAVE));
        xwpfBorderTypeMap.put(XWPFBorderType.OUTSET, STBorder.Enum.forInt(STBorder.INT_OUTSET));
        xwpfBorderTypeMap.put(XWPFBorderType.INSET, STBorder.Enum.forInt(STBorder.INT_INSET));

        stBorderTypeMap = new HashMap<>();
        stBorderTypeMap.put(STBorder.INT_NIL, XWPFBorderType.NIL);
        stBorderTypeMap.put(STBorder.INT_NONE, XWPFBorderType.NONE);
        stBorderTypeMap.put(STBorder.INT_SINGLE, XWPFBorderType.SINGLE);
        stBorderTypeMap.put(STBorder.INT_THICK, XWPFBorderType.THICK);
        stBorderTypeMap.put(STBorder.INT_DOUBLE, XWPFBorderType.DOUBLE);
        stBorderTypeMap.put(STBorder.INT_DOTTED, XWPFBorderType.DOTTED);
        stBorderTypeMap.put(STBorder.INT_DASHED, XWPFBorderType.DASHED);
        stBorderTypeMap.put(STBorder.INT_DOT_DASH, XWPFBorderType.DOT_DASH);
        stBorderTypeMap.put(STBorder.INT_DOT_DOT_DASH, XWPFBorderType.DOT_DOT_DASH);
        stBorderTypeMap.put(STBorder.INT_TRIPLE, XWPFBorderType.TRIPLE);
        stBorderTypeMap.put(STBorder.INT_THIN_THICK_SMALL_GAP, XWPFBorderType.THIN_THICK_SMALL_GAP);
        stBorderTypeMap.put(STBorder.INT_THICK_THIN_SMALL_GAP, XWPFBorderType.THICK_THIN_SMALL_GAP);
        stBorderTypeMap.put(STBorder.INT_THIN_THICK_THIN_SMALL_GAP, XWPFBorderType.THIN_THICK_THIN_SMALL_GAP);
        stBorderTypeMap.put(STBorder.INT_THIN_THICK_MEDIUM_GAP, XWPFBorderType.THIN_THICK_MEDIUM_GAP);
        stBorderTypeMap.put(STBorder.INT_THICK_THIN_MEDIUM_GAP, XWPFBorderType.THICK_THIN_MEDIUM_GAP);
        stBorderTypeMap.put(STBorder.INT_THIN_THICK_THIN_MEDIUM_GAP, XWPFBorderType.THIN_THICK_THIN_MEDIUM_GAP);
        stBorderTypeMap.put(STBorder.INT_THIN_THICK_LARGE_GAP, XWPFBorderType.THIN_THICK_LARGE_GAP);
        stBorderTypeMap.put(STBorder.INT_THICK_THIN_LARGE_GAP, XWPFBorderType.THICK_THIN_LARGE_GAP);
        stBorderTypeMap.put(STBorder.INT_THIN_THICK_THIN_LARGE_GAP, XWPFBorderType.THIN_THICK_THIN_LARGE_GAP);
        stBorderTypeMap.put(STBorder.INT_WAVE, XWPFBorderType.WAVE);
        stBorderTypeMap.put(STBorder.INT_DOUBLE_WAVE, XWPFBorderType.DOUBLE_WAVE);
        stBorderTypeMap.put(STBorder.INT_DASH_SMALL_GAP, XWPFBorderType.DASH_SMALL_GAP);
        stBorderTypeMap.put(STBorder.INT_DASH_DOT_STROKED, XWPFBorderType.DASH_DOT_STROKED);
        stBorderTypeMap.put(STBorder.INT_THREE_D_EMBOSS, XWPFBorderType.THREE_D_EMBOSS);
        stBorderTypeMap.put(STBorder.INT_THREE_D_ENGRAVE, XWPFBorderType.THREE_D_ENGRAVE);
        stBorderTypeMap.put(STBorder.INT_OUTSET, XWPFBorderType.OUTSET);
        stBorderTypeMap.put(STBorder.INT_INSET, XWPFBorderType.INSET);
    }

    protected StringBuilder text = new StringBuilder(64);
    protected final List<XWPFTableRow> tableRows = new ArrayList<>();

    // Unused: UUF_UNUSED_PUBLIC_OR_PROTECTED_FIELD
    //protected List<String> styleIDs;
    protected IBody part;
    private CTTbl ctTbl;

    public XWPFTable(CTTbl table, IBody part, int row, int col) {
        this(table, part);

        for (int i = 0; i < row; i++) {
            XWPFTableRow tabRow = (getRow(i) == null) ? createRow() : getRow(i);
            for (int k = 0; k < col; k++) {
                if (tabRow.getCell(k) == null) {
                    tabRow.createCell();
                }
            }
        }
    }

    public XWPFTable(CTTbl table, IBody part) {
        this.part = part;
        this.ctTbl = table;

        // is an empty table: I add one row and one column as default
        if (table.sizeOfTrArray() == 0)
            createEmptyTable(table);

        for (CTRow row : table.getTrList()) {
            StringBuilder rowText = new StringBuilder();
            XWPFTableRow tabRow = new XWPFTableRow(row, this);
            tableRows.add(tabRow);
            for (CTTc cell : row.getTcList()) {
                for (CTP ctp : cell.getPList()) {
                    XWPFParagraph p = new XWPFParagraph(ctp, part);
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
        //getRows();
    }

    /**
     * @return ctTbl object
     */
    @Internal
    public CTTbl getCTTbl() {
        return ctTbl;
    }

    /**
     * Convenience method to extract text in cells.  This
     * does not extract text recursively in cells, and it does not
     * currently include text in SDT (form) components.
     * <p>
     * To get all text within a table, see XWPFWordExtractor's appendTableText
     * as an example.
     *
     * @return text
     */
    public String getText() {
        return text.toString();
    }

    
    /**
     * This method has existed since 2008 without an implementation.
     * It will be removed unless an implementation is provided.
     * @deprecated 4.0.0 due to lack of implementation.
     */
    @Deprecated
    @Removal
    @NotImplemented
    public void addNewRowBetween(int start, int end) {
        throw new UnsupportedOperationException("XWPFTable#addNewRowBetween(int, int) not implemented");
    }

    /**
     * add a new column for each row in this table
     */
    public void addNewCol() {
        if (ctTbl.sizeOfTrArray() == 0) {
            createRow();
        }
        for (int i = 0; i < ctTbl.sizeOfTrArray(); i++) {
            XWPFTableRow tabRow = new XWPFTableRow(ctTbl.getTrArray(i), this);
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
        XWPFTableRow tabRow = new XWPFTableRow(ctTbl.addNewTr(), this);
        addColumn(tabRow, sizeCol);
        tableRows.add(tabRow);
        return tabRow;
    }

    /**
     * @param pos - index of the row
     * @return the row at the position specified or null if no rows is defined or if the position is greather than the max size of rows array
     */
    public XWPFTableRow getRow(int pos) {
        if (pos >= 0 && pos < ctTbl.sizeOfTrArray()) {
            //return new XWPFTableRow(ctTbl.getTrArray(pos));
            return getRows().get(pos);
        }
        return null;
    }

    /**
     * @return width value
     */
    public int getWidth() {
        CTTblPr tblPr = getTblPr();
        return tblPr.isSetTblW() ? tblPr.getTblW().getW().intValue() : -1;
    }

    /**
     * @param width
     */
    public void setWidth(int width) {
        CTTblPr tblPr = getTblPr();
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        tblWidth.setW(new BigInteger(Integer.toString(width)));
    }

    /**
     * @return number of rows in table
     */
    public int getNumberOfRows() {
        return ctTbl.sizeOfTrArray();
    }

    /**
     * Returns CTTblPr object for table. Creates it if it does not exist.
     */
    private CTTblPr getTblPr() {
        return getTblPr(true);
    }

    /**
     * Returns CTTblPr object for table. If force parameter is true, will 
     * create the element if necessary. If force parameter is false, returns
     * null when CTTblPr element is missing.
     *
     * @param force - force creation of CTTblPr element if necessary
     */
    private CTTblPr getTblPr(boolean force) {
        return (ctTbl.getTblPr() != null) ? ctTbl.getTblPr() 
                : (force ? ctTbl.addNewTblPr() : null);
    }

    /**
     * Return CTTblBorders object for table. If force parameter is true, will
     * create the element if necessary. If force parameter is false, returns
     * null when CTTblBorders element is missing.
     *
     * @param force - force creation of CTTblBorders element if necessary
     */
    private CTTblBorders getTblBorders(boolean force) {
        CTTblPr tblPr = getTblPr(force);
        return tblPr == null ? null
               : tblPr.isSetTblBorders() ? tblPr.getTblBorders() 
               : force ? tblPr.addNewTblBorders()
               : null;
    }
    
    /**
     * Return CTBorder object for Inside Vertical border. If force parameter is true,
     * will create the element if necessary. If force parameter is false, returns 
     * null when Inside Vertical border is missing.
     *
     * @param force - force creation of Inside Vertical border if necessary.
     */
    private CTBorder getTblInsideVBorder(boolean force) {
        CTTblBorders ctb = getTblBorders(force);
        return ctb == null ? null
               : ctb.isSetInsideV() ? ctb.getInsideV() 
               : force ? ctb.addNewInsideV() 
               : null;
    }

    /**
     * Return CTBorder object for Inside Horizontal border. If force parameter is true,
     * will create the element if necessary. If force parameter is false, returns 
     * null when Inside Horizontal border is missing.
     *
     * @param force - force creation of Inside Horizontal border if necessary.
     */
    private CTBorder getTblInsideHBorder(boolean force) {
        CTTblBorders ctb = getTblBorders(force);
        return ctb == null ? null
               : ctb.isSetInsideH() ? ctb.getInsideH() 
               : force ? ctb.addNewInsideH() 
               : null;
    }

    /**
     * Return CTBorder object for Top border. If force parameter is true,
     * will create the element if necessary. If force parameter is false, returns 
     * null when Top border is missing.
     *
     * @param force - force creation of Top border if necessary.
     */
    private CTBorder getTblTopBorder(boolean force) {
        CTTblBorders ctb = getTblBorders(force);
        return ctb == null ? null
               : ctb.isSetTop() ? ctb.getTop() 
               : force ? ctb.addNewTop() 
               : null;
    }

    /**
     * Return CTBorder object for Bottom border. If force parameter is true,
     * will create the element if necessary. If force parameter is false, returns 
     * null when Bottom border is missing.
     *
     * @param force - force creation of Bottom border if necessary.
     */
    private CTBorder getTblBottomBorder(boolean force) {
        CTTblBorders ctb = getTblBorders(force);
        return ctb == null ? null
               : ctb.isSetBottom() ? ctb.getBottom() 
               : force ? ctb.addNewBottom() 
               : null;
    }

    /**
     * Return CTBorder object for Left border. If force parameter is true,
     * will create the element if necessary. If force parameter is false, returns 
     * null when Left border is missing.
     *
     * @param force - force creation of Left border if necessary.
     */
    private CTBorder getTblLeftBorder(boolean force) {
        CTTblBorders ctb = getTblBorders(force);
        return ctb == null ? null
               : ctb.isSetLeft() ? ctb.getLeft() 
               : force ? ctb.addNewLeft() 
               : null;
    }

    /**
     * Return CTBorder object for Right border. If force parameter is true,
     * will create the element if necessary. If force parameter is false, returns 
     * null when Right border is missing.
     *
     * @param force - force creation of Right border if necessary.
     */
    private CTBorder getTblRightBorder(boolean force) {
        CTTblBorders ctb = getTblBorders(force);
        return ctb == null ? null
               : ctb.isSetRight() ? ctb.getRight() 
               : force ? ctb.addNewRight() 
               : null;
    }
    
    /**
     * Returns the current table alignment or NULL
     *
     * @return Table Alignment as a {@link TableRowAlign} enum
     */
    public TableRowAlign getTableAlignment() {
        CTTblPr tPr = getTblPr(false);
        return tPr == null ? null
                : tPr.isSetJc() ? TableRowAlign.valueOf(tPr.getJc().getVal().intValue())
                : null;
    }
    
    /**
     * Set table alignment to specified {@link TableRowAlign}
     *
     * @param ha {@link TableRowAlign} to set
     */
    public void setTableAlignment(TableRowAlign tra) {
        CTTblPr tPr = getTblPr(true);
        CTJc jc = tPr.isSetJc() ? tPr.getJc() : tPr.addNewJc();
        jc.setVal(STJc.Enum.forInt(tra.getValue()));
    }
    
    /**
     * Removes the table alignment attribute from a table
     */
    public void removeTableAlignment() {
        CTTblPr tPr = getTblPr(false);
        if (tPr != null && tPr.isSetJc()) {
            tPr.unsetJc();
        }
    }
    
    private void addColumn(XWPFTableRow tabRow, int sizeCol) {
        if (sizeCol > 0) {
            for (int i = 0; i < sizeCol; i++) {
                tabRow.createCell();
            }
        }
    }

    /**
     * get the StyleID of the table
     *
     * @return style-ID of the table
     */
    public String getStyleID() {
        String styleId = null;
        CTTblPr tblPr = ctTbl.getTblPr();
        if (tblPr != null) {
            CTString styleStr = tblPr.getTblStyle();
            if (styleStr != null) {
                styleId = styleStr.getVal();
            }
        }
        return styleId;
    }

    /**
     * Set the table style. If the style is not defined in the document, MS Word
     * will set the table style to "Normal".
     *
     * @param styleName - the style name to apply to this table
     */
    public void setStyleID(String styleName) {
        CTTblPr tblPr = getTblPr();
        CTString styleStr = tblPr.getTblStyle();
        if (styleStr == null) {
            styleStr = tblPr.addNewTblStyle();
        }
        styleStr.setVal(styleName);
    }

    /**
     * Get inside horizontal border type
     *
     * @return {@link XWPFBorderType} of the inside horizontal borders or null if missing
     */
    public XWPFBorderType getInsideHBorderType() {
        CTBorder b = getTblInsideHBorder(false);
        return (b != null) ? stBorderTypeMap.get(b.getVal().intValue()) : null;
    }

    /**
     * Get inside horizontal border size
     * 
     * @return The width of the Inside Horizontal borders in 1/8th points,
     * -1 if missing.
     */
    public int getInsideHBorderSize() {
        CTBorder b = getTblInsideHBorder(false);
        return (b != null) 
                ? (b.isSetSz() ? b.getSz().intValue() : -1)
                        : -1;        
    }

    /**
     * Get inside horizontal border spacing
     * 
     * @return The offset to the Inside Horizontal borders in points,
     * -1 if missing.
     */
    public int getInsideHBorderSpace() {
        CTBorder b = getTblInsideHBorder(false);
        return (b != null) 
                ? (b.isSetSpace() ? b.getSpace().intValue() : -1)
                        : -1;        
    }

    /**
     * Get inside horizontal border color
     * 
     * @return The color of the Inside Horizontal borders, null if missing.
     */
    public String getInsideHBorderColor() {
        CTBorder b = getTblInsideHBorder(false);
        return (b != null) 
                ? (b.isSetColor() ? b.xgetColor().getStringValue() : null)
                        : null;        
    }

    /**
     * Get inside vertical border type
     *
     * @return {@link XWPFBorderType} of the inside vertical borders or null if missing
     */
    public XWPFBorderType getInsideVBorderType() {
        CTBorder b = getTblInsideVBorder(false);
        return (b != null) ? stBorderTypeMap.get(b.getVal().intValue()) : null;
    }

    /**
     * Get inside vertical border size
     * 
     * @return The width of the Inside vertical borders in 1/8th points,
     * -1 if missing.
     */
    public int getInsideVBorderSize() {
        CTBorder b = getTblInsideVBorder(false);
        return (b != null) 
                ? (b.isSetSz() ? b.getSz().intValue() : -1)
                        : -1;        
    }

    /**
     * Get inside vertical border spacing
     * 
     * @return The offset to the Inside vertical borders in points,
     * -1 if missing.
     */
    public int getInsideVBorderSpace() {
        CTBorder b = getTblInsideVBorder(false);
        return (b != null) 
                ? (b.isSetSpace() ? b.getSpace().intValue() : -1)
                        : -1;        
    }

    /**
     * Get inside vertical border color
     * 
     * @return The color of the Inside vertical borders, null if missing.
     */
    public String getInsideVBorderColor() {
        CTBorder b = getTblInsideVBorder(false);
        return (b != null) 
                ? (b.isSetColor() ? b.xgetColor().getStringValue() : null)
                        : null;        
    }

    /**
     * Get top border type
     *
     * @return {@link XWPFBorderType} of the top borders or null if missing
     */
    public XWPFBorderType getTopBorderType() {
        CTBorder b = getTblTopBorder(false);
        return (b != null) ? stBorderTypeMap.get(b.getVal().intValue()) : null;
    }

    /**
     * Get top border size
     * 
     * @return The width of the top borders in 1/8th points,
     * -1 if missing.
     */
    public int getTopBorderSize() {
        CTBorder b = getTblTopBorder(false);
        return (b != null) 
                ? (b.isSetSz() ? b.getSz().intValue() : -1)
                        : -1;        
    }

    /**
     * Get top border spacing
     * 
     * @return The offset to the top borders in points,
     * -1 if missing.
     */
    public int getTopBorderSpace() {
        CTBorder b = getTblTopBorder(false);
        return (b != null) 
                ? (b.isSetSpace() ? b.getSpace().intValue() : -1)
                        : -1;        
    }

    /**
     * Get top border color
     * 
     * @return The color of the top borders, null if missing.
     */
    public String getTopBorderColor() {
        CTBorder b = getTblTopBorder(false);
        return (b != null) 
                ? (b.isSetColor() ? b.xgetColor().getStringValue() : null)
                        : null;        
    }

    /**
     * Get bottom border type
     *
     * @return {@link XWPFBorderType} of the bottom borders or null if missing
     */
    public XWPFBorderType getBottomBorderType() {
        CTBorder b = getTblBottomBorder(false);
        return (b != null) ? stBorderTypeMap.get(b.getVal().intValue()) : null;
    }

    /**
     * Get bottom border size
     * 
     * @return The width of the bottom borders in 1/8th points,
     * -1 if missing.
     */
    public int getBottomBorderSize() {
        CTBorder b = getTblBottomBorder(false);
        return (b != null) 
                ? (b.isSetSz() ? b.getSz().intValue() : -1)
                        : -1;        
    }

    /**
     * Get bottom border spacing
     * 
     * @return The offset to the bottom borders in points,
     * -1 if missing.
     */
    public int getBottomBorderSpace() {
        CTBorder b = getTblBottomBorder(false);
        return (b != null) 
                ? (b.isSetSpace() ? b.getSpace().intValue() : -1)
                        : -1;        
    }

    /**
     * Get bottom border color
     * 
     * @return The color of the bottom borders, null if missing.
     */
    public String getBottomBorderColor() {
        CTBorder b = getTblBottomBorder(false);
        return (b != null) 
                ? (b.isSetColor() ? b.xgetColor().getStringValue() : null)
                        : null;        
    }

    /**
     * Get Left border type
     *
     * @return {@link XWPFBorderType} of the Left borders or null if missing
     */
    public XWPFBorderType getLeftBorderType() {
        CTBorder b = getTblLeftBorder(false);
        return (b != null) ? stBorderTypeMap.get(b.getVal().intValue()) : null;
    }

    /**
     * Get Left border size
     * 
     * @return The width of the Left borders in 1/8th points,
     * -1 if missing.
     */
    public int getLeftBorderSize() {
        CTBorder b = getTblLeftBorder(false);
        return (b != null) 
                ? (b.isSetSz() ? b.getSz().intValue() : -1)
                        : -1;        
    }

    /**
     * Get Left border spacing
     * 
     * @return The offset to the Left borders in points,
     * -1 if missing.
     */
    public int getLeftBorderSpace() {
        CTBorder b = getTblLeftBorder(false);
        return (b != null) 
                ? (b.isSetSpace() ? b.getSpace().intValue() : -1)
                        : -1;        
    }

    /**
     * Get Left border color
     * 
     * @return The color of the Left borders, null if missing.
     */
    public String getLeftBorderColor() {
        CTBorder b = getTblLeftBorder(false);
        return (b != null) 
                ? (b.isSetColor() ? b.xgetColor().getStringValue() : null)
                        : null;        
    }

    /**
     * Get Right border type
     *
     * @return {@link XWPFBorderType} of the Right borders or null if missing
     */
    public XWPFBorderType getRightBorderType() {
        CTBorder b = getTblRightBorder(false);
        return (b != null) ? stBorderTypeMap.get(b.getVal().intValue()) : null;
    }

    /**
     * Get Right border size
     * 
     * @return The width of the Right borders in 1/8th points,
     * -1 if missing.
     */
    public int getRightBorderSize() {
        CTBorder b = getTblRightBorder(false);
        return (b != null) 
                ? (b.isSetSz() ? b.getSz().intValue() : -1)
                        : -1;        
    }

    /**
     * Get Right border spacing
     * 
     * @return The offset to the Right borders in points,
     * -1 if missing.
     */
    public int getRightBorderSpace() {
        CTBorder b = getTblRightBorder(false);
        return (b != null) 
                ? (b.isSetSpace() ? b.getSpace().intValue() : -1)
                        : -1;        
    }

    /**
     * Get Right border color
     * 
     * @return The color of the Right borders, null if missing.
     */
    public String getRightBorderColor() {
        CTBorder b = getTblRightBorder(false);
        return (b != null) 
                ? (b.isSetColor() ? b.xgetColor().getStringValue() : null)
                        : null;        
    }

    public int getRowBandSize() {
        int size = 0;
        CTTblPr tblPr = getTblPr();
        if (tblPr.isSetTblStyleRowBandSize()) {
            CTDecimalNumber rowSize = tblPr.getTblStyleRowBandSize();
            size = rowSize.getVal().intValue();
        }
        return size;
    }

    public void setRowBandSize(int size) {
        CTTblPr tblPr = getTblPr();
        CTDecimalNumber rowSize = tblPr.isSetTblStyleRowBandSize() ? tblPr.getTblStyleRowBandSize() : tblPr.addNewTblStyleRowBandSize();
        rowSize.setVal(BigInteger.valueOf(size));
    }

    public int getColBandSize() {
        int size = 0;
        CTTblPr tblPr = getTblPr();
        if (tblPr.isSetTblStyleColBandSize()) {
            CTDecimalNumber colSize = tblPr.getTblStyleColBandSize();
            size = colSize.getVal().intValue();
        }
        return size;
    }

    public void setColBandSize(int size) {
        CTTblPr tblPr = getTblPr();
        CTDecimalNumber colSize = tblPr.isSetTblStyleColBandSize() ? tblPr.getTblStyleColBandSize() : tblPr.addNewTblStyleColBandSize();
        colSize.setVal(BigInteger.valueOf(size));
    }

    /**
     * Set Inside horizontal borders for a table
     *
     * @param type - {@link XWPFBorderType} e.g. single, double, thick
     * @param size - Specifies the width of the current border. The width of this border is
     *      specified in measurements of eighths of a point, with a minimum value of two (onefourth
     *      of a point) and a maximum value of 96 (twelve points). Any values outside this
     *      range may be reassigned to a more appropriate value.
     * @param space - Specifies the spacing offset that shall be used to place this border on the table
     * @param rgbColor - This color may either be presented as a hex value (in RRGGBB format), 
     *      or auto to allow a consumer to automatically determine the border color as appropriate.
     */
    public void setInsideHBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTBorder b = getTblInsideHBorder(true);
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }

    /**
     * Set Inside Vertical borders for table
     *
     * @param type - {@link XWPFBorderType} e.g. single, double, thick
     * @param size - Specifies the width of the current border. The width of this border is
     *      specified in measurements of eighths of a point, with a minimum value of two (onefourth
     *      of a point) and a maximum value of 96 (twelve points). Any values outside this
     *      range may be reassigned to a more appropriate value.
     * @param space - Specifies the spacing offset that shall be used to place this border on the table
     * @param rgbColor - This color may either be presented as a hex value (in RRGGBB format), 
     *      or auto to allow a consumer to automatically determine the border color as appropriate.
     */
    public void setInsideVBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTBorder b = getTblInsideVBorder(true);
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }

    /**
     * Set Top borders for table
     *
     * @param type - {@link XWPFBorderType} e.g. single, double, thick
     * @param size - Specifies the width of the current border. The width of this border is
     *      specified in measurements of eighths of a point, with a minimum value of two (onefourth
     *      of a point) and a maximum value of 96 (twelve points). Any values outside this
     *      range may be reassigned to a more appropriate value.
     * @param space - Specifies the spacing offset that shall be used to place this border on the table
     * @param rgbColor - This color may either be presented as a hex value (in RRGGBB format), 
     *      or auto to allow a consumer to automatically determine the border color as appropriate.
     */
    public void setTopBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTBorder b = getTblTopBorder(true);
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }

    /**
     * Set Bottom borders for table
     *
     * @param type - {@link XWPFBorderType} e.g. single, double, thick
     * @param size - Specifies the width of the current border. The width of this border is
     *      specified in measurements of eighths of a point, with a minimum value of two (onefourth
     *      of a point) and a maximum value of 96 (twelve points). Any values outside this
     *      range may be reassigned to a more appropriate value.
     * @param space - Specifies the spacing offset that shall be used to place this border on the table
     * @param rgbColor - This color may either be presented as a hex value (in RRGGBB format), 
     *      or auto to allow a consumer to automatically determine the border color as appropriate.
     */
    public void setBottomBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTBorder b = getTblBottomBorder(true);
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }

    /**
     * Set Left borders for table
     *
     * @param type - {@link XWPFBorderType} e.g. single, double, thick
     * @param size - Specifies the width of the current border. The width of this border is
     *      specified in measurements of eighths of a point, with a minimum value of two (onefourth
     *      of a point) and a maximum value of 96 (twelve points). Any values outside this
     *      range may be reassigned to a more appropriate value.
     * @param space - Specifies the spacing offset that shall be used to place this border on the table
     * @param rgbColor - This color may either be presented as a hex value (in RRGGBB format), 
     *      or auto to allow a consumer to automatically determine the border color as appropriate.
     */
    public void setLeftBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTBorder b = getTblLeftBorder(true);
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }

    /**
     * Set Right borders for table
     *
     * @param type - {@link XWPFBorderType} e.g. single, double, thick
     * @param size - Specifies the width of the current border. The width of this border is
     *      specified in measurements of eighths of a point, with a minimum value of two (onefourth
     *      of a point) and a maximum value of 96 (twelve points). Any values outside this
     *      range may be reassigned to a more appropriate value.
     * @param space - Specifies the spacing offset that shall be used to place this border on the table
     * @param rgbColor - This color may either be presented as a hex value (in RRGGBB format), 
     *      or auto to allow a consumer to automatically determine the border color as appropriate.
     */
    public void setRightBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTBorder b = getTblRightBorder(true);
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }
    
    /**
     * Remove inside horizontal borders for table
     */
    public void removeInsideHBorder() {
        CTBorder b = getTblInsideHBorder(false);
        if (b != null) {
            getTblBorders(false).unsetInsideH();
            cleanupTblBorders();
        }
    }
    
    /**
     * Remove inside vertical borders for table
     */
    public void removeInsideVBorder() {
        CTBorder b = getTblInsideVBorder(false);
        if (b != null) {
            getTblBorders(false).unsetInsideV();
            cleanupTblBorders();
        }
    }
    
    /**
     * Remove top borders for table
     */
    public void removeTopBorder() {
        CTBorder b = getTblTopBorder(false);
        if (b != null) {
            getTblBorders(false).unsetTop();
            cleanupTblBorders();
        }
    }
    
    /**
     * Remove bottom borders for table
     */
    public void removeBottomBorder() {
        CTBorder b = getTblBottomBorder(false);
        if (b != null) {
            getTblBorders(false).unsetBottom();
            cleanupTblBorders();
        }
    }
    
    /**
     * Remove left borders for table
     */
    public void removeLeftBorder() {
        CTBorder b = getTblLeftBorder(false);
        if (b != null) {
            getTblBorders(false).unsetLeft();
            cleanupTblBorders();
        }
    }
    
    /**
     * Remove right borders for table
     */
    public void removeRightBorder() {
        CTBorder b = getTblRightBorder(false);
        if (b != null) {
            getTblBorders(false).unsetRight();
            cleanupTblBorders();
        }
    }
    
    /**
     * Remove all borders from table
     */
    public void removeBorders() {
        CTTblBorders b = getTblBorders(false);
        if (b != null) {
            getTblPr(false).unsetTblBorders();
        }
    }
    
    /**
     * removes the Borders node from Table properties if there are 
     * no border elements
     */
    private void cleanupTblBorders() {
        CTTblBorders b = getTblBorders(false);
        if (b != null) {
            if (b.getInsideH() == null &&
                    b.getInsideV() == null &&
                    b.getTop() == null &&
                    b.getBottom() == null &&
                    b.getLeft() == null &&
                    b.getRight() == null) {
                getTblPr(false).unsetTblBorders();
            }
        }
    }
    
    public int getCellMarginTop() {
        int margin = 0;
        CTTblPr tblPr = getTblPr();
        CTTblCellMar tcm = tblPr.getTblCellMar();
        if (tcm != null) {
            CTTblWidth tw = tcm.getTop();
            if (tw != null) {
                margin = tw.getW().intValue();
            }
        }
        return margin;
    }

    public int getCellMarginLeft() {
        int margin = 0;
        CTTblPr tblPr = getTblPr();
        CTTblCellMar tcm = tblPr.getTblCellMar();
        if (tcm != null) {
            CTTblWidth tw = tcm.getLeft();
            if (tw != null) {
                margin = tw.getW().intValue();
            }
        }
        return margin;
    }

    public int getCellMarginBottom() {
        int margin = 0;
        CTTblPr tblPr = getTblPr();
        CTTblCellMar tcm = tblPr.getTblCellMar();
        if (tcm != null) {
            CTTblWidth tw = tcm.getBottom();
            if (tw != null) {
                margin = tw.getW().intValue();
            }
        }
        return margin;
    }

    public int getCellMarginRight() {
        int margin = 0;
        CTTblPr tblPr = getTblPr();
        CTTblCellMar tcm = tblPr.getTblCellMar();
        if (tcm != null) {
            CTTblWidth tw = tcm.getRight();
            if (tw != null) {
                margin = tw.getW().intValue();
            }
        }
        return margin;
    }

    public void setCellMargins(int top, int left, int bottom, int right) {
        CTTblPr tblPr = getTblPr();
        CTTblCellMar tcm = tblPr.isSetTblCellMar() ? tblPr.getTblCellMar() : tblPr.addNewTblCellMar();

        CTTblWidth tw = tcm.isSetLeft() ? tcm.getLeft() : tcm.addNewLeft();
        tw.setType(STTblWidth.DXA);
        tw.setW(BigInteger.valueOf(left));

        tw = tcm.isSetTop() ? tcm.getTop() : tcm.addNewTop();
        tw.setType(STTblWidth.DXA);
        tw.setW(BigInteger.valueOf(top));

        tw = tcm.isSetBottom() ? tcm.getBottom() : tcm.addNewBottom();
        tw.setType(STTblWidth.DXA);
        tw.setW(BigInteger.valueOf(bottom));

        tw = tcm.isSetRight() ? tcm.getRight() : tcm.addNewRight();
        tw.setType(STTblWidth.DXA);
        tw.setW(BigInteger.valueOf(right));
    }

    /**
     * add a new Row to the table
     *
     * @param row the row which should be added
     */
    public void addRow(XWPFTableRow row) {
        ctTbl.addNewTr();
        ctTbl.setTrArray(getNumberOfRows() - 1, row.getCtRow());
        tableRows.add(row);
    }

    /**
     * add a new Row to the table
     * at position pos
     *
     * @param row the row which should be added
     */
    public boolean addRow(XWPFTableRow row, int pos) {
        if (pos >= 0 && pos <= tableRows.size()) {
            ctTbl.insertNewTr(pos);
            ctTbl.setTrArray(pos, row.getCtRow());
            tableRows.add(pos, row);
            return true;
        }
        return false;
    }

    /**
     * inserts a new tablerow
     *
     * @param pos
     * @return the inserted row
     */
    public XWPFTableRow insertNewTableRow(int pos) {
        if (pos >= 0 && pos <= tableRows.size()) {
            CTRow row = ctTbl.insertNewTr(pos);
            XWPFTableRow tableRow = new XWPFTableRow(row, this);
            tableRows.add(pos, tableRow);
            return tableRow;
        }
        return null;
    }

    /**
     * Remove a row at position pos from the table
     *
     * @param pos position the Row in the Table
     */
    public boolean removeRow(int pos) throws IndexOutOfBoundsException {
        if (pos >= 0 && pos < tableRows.size()) {
            if (ctTbl.sizeOfTrArray() > 0) {
                ctTbl.removeTr(pos);
            }
            tableRows.remove(pos);
            return true;
        }
        return false;
    }

    public List<XWPFTableRow> getRows() {
        return Collections.unmodifiableList(tableRows);
    }

    /**
     * returns the type of the BodyElement Table
     *
     * @see org.apache.poi.xwpf.usermodel.IBodyElement#getElementType()
     */
    public BodyElementType getElementType() {
        return BodyElementType.TABLE;
    }

    public IBody getBody() {
        return part;
    }

    /**
     * returns the part of the bodyElement
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public POIXMLDocumentPart getPart() {
        if (part != null) {
            return part.getPart();
        }
        return null;
    }

    /**
     * returns the partType of the bodyPart which owns the bodyElement
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    public BodyType getPartType() {
        return part.getPartType();
    }

    /**
     * returns the XWPFRow which belongs to the CTRow row
     * if this row is not existing in the table null will be returned
     */
    public XWPFTableRow getRow(CTRow row) {
        for (int i = 0; i < getRows().size(); i++) {
            if (getRows().get(i).getCtRow() == row) return getRow(i);
        }
        return null;
    }

    // Create a map from this XWPF-level enum to the STBorder.Enum values
    public static enum XWPFBorderType {
        NIL, NONE, SINGLE, THICK, DOUBLE, DOTTED, DASHED, DOT_DASH, DOT_DOT_DASH, TRIPLE,
        THIN_THICK_SMALL_GAP, THICK_THIN_SMALL_GAP, THIN_THICK_THIN_SMALL_GAP, 
        THIN_THICK_MEDIUM_GAP, THICK_THIN_MEDIUM_GAP, THIN_THICK_THIN_MEDIUM_GAP,
        THIN_THICK_LARGE_GAP, THICK_THIN_LARGE_GAP, THIN_THICK_THIN_LARGE_GAP,
        WAVE, DOUBLE_WAVE, DASH_SMALL_GAP, DASH_DOT_STROKED, THREE_D_EMBOSS, THREE_D_ENGRAVE,
        OUTSET, INSET;
    }
}
