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

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTDecimalNumber;
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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;

/**
 * <p>Sketch of XWPFTable class. Only table's text is being hold.</p>
 * <p>Specifies the contents of a table present in the document. A table is a set
 * of paragraphs (and other block-level content) arranged in rows and columns.</p>
 */
public class XWPFTable implements IBodyElement, ISDTContents {
    protected StringBuffer text = new StringBuffer();
    private CTTbl ctTbl;
    protected List<XWPFTableRow> tableRows;
    protected List<String> styleIDs;

    // Create a map from this XWPF-level enum to the STBorder.Enum values
    public static enum XWPFBorderType { NIL, NONE, SINGLE, THICK, DOUBLE, DOTTED, DASHED, DOT_DASH };
    private static EnumMap<XWPFBorderType, STBorder.Enum> xwpfBorderTypeMap;
    // Create a map from the STBorder.Enum values to the XWPF-level enums
    private static HashMap<Integer, XWPFBorderType> stBorderTypeMap;

    protected IBody part;

    static {
        // populate enum maps
        xwpfBorderTypeMap = new EnumMap<XWPFBorderType, STBorder.Enum>(XWPFBorderType.class);
        xwpfBorderTypeMap.put(XWPFBorderType.NIL, STBorder.Enum.forInt(STBorder.INT_NIL));
        xwpfBorderTypeMap.put(XWPFBorderType.NONE, STBorder.Enum.forInt(STBorder.INT_NONE));
        xwpfBorderTypeMap.put(XWPFBorderType.SINGLE, STBorder.Enum.forInt(STBorder.INT_SINGLE));
        xwpfBorderTypeMap.put(XWPFBorderType.THICK, STBorder.Enum.forInt(STBorder.INT_THICK));
        xwpfBorderTypeMap.put(XWPFBorderType.DOUBLE, STBorder.Enum.forInt(STBorder.INT_DOUBLE));
        xwpfBorderTypeMap.put(XWPFBorderType.DOTTED, STBorder.Enum.forInt(STBorder.INT_DOTTED));
        xwpfBorderTypeMap.put(XWPFBorderType.DASHED, STBorder.Enum.forInt(STBorder.INT_DASHED));
        xwpfBorderTypeMap.put(XWPFBorderType.DOT_DASH, STBorder.Enum.forInt(STBorder.INT_DOT_DASH));

        stBorderTypeMap = new HashMap<Integer, XWPFBorderType>();
        stBorderTypeMap.put(STBorder.INT_NIL, XWPFBorderType.NIL);
        stBorderTypeMap.put(STBorder.INT_NONE, XWPFBorderType.NONE);
        stBorderTypeMap.put(STBorder.INT_SINGLE, XWPFBorderType.SINGLE);
        stBorderTypeMap.put(STBorder.INT_THICK, XWPFBorderType.THICK);
        stBorderTypeMap.put(STBorder.INT_DOUBLE, XWPFBorderType.DOUBLE);
        stBorderTypeMap.put(STBorder.INT_DOTTED, XWPFBorderType.DOTTED);
        stBorderTypeMap.put(STBorder.INT_DASHED, XWPFBorderType.DASHED);
        stBorderTypeMap.put(STBorder.INT_DOT_DASH, XWPFBorderType.DOT_DASH); 
    }
    
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

    public XWPFTable(CTTbl table, IBody part){
        this.part = part;
        this.ctTbl = table;

        tableRows = new ArrayList<XWPFTableRow>();

        // is an empty table: I add one row and one column as default
        if (table.sizeOfTrArray() == 0)
            createEmptyTable(table);

        for (CTRow row : table.getTrList()) {
            StringBuffer rowText = new StringBuffer();
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
        getRows();
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
     * @param width
     */
    public void setWidth(int width) {
        CTTblPr tblPr = getTrPr();
        CTTblWidth tblWidth = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
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
    
    /**
     * get the StyleID of the table
     * @return	style-ID of the table
     */
    public String getStyleID(){
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
     * @param styleName - the style name to apply to this table
     */
    public void setStyleID(String styleName) {
        CTTblPr tblPr = getTrPr();
        CTString styleStr = tblPr.getTblStyle();
        if (styleStr == null) {
            styleStr = tblPr.addNewTblStyle();
        }
        styleStr.setVal(styleName);
    }

    public XWPFBorderType getInsideHBorderType() {
        XWPFBorderType bt = null;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideH()) {
                CTBorder border = ctb.getInsideH();
                bt = stBorderTypeMap.get(border.getVal().intValue());
            }
        }
        return bt;
    }

    public int getInsideHBorderSize() {
        int size = -1;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideH()) {
                CTBorder border = ctb.getInsideH();
                size = border.getSz().intValue();
            }
        }
        return size;
    }

    public int getInsideHBorderSpace() {
        int space = -1;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideH()) {
                CTBorder border = ctb.getInsideH();
                space = border.getSpace().intValue();
            }
        }
        return space;
    }

    public String getInsideHBorderColor() {
        String color = null;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideH()) {
                CTBorder border = ctb.getInsideH();
                color = border.xgetColor().getStringValue();
            }
        }
        return color;
    }

    public XWPFBorderType getInsideVBorderType() {
        XWPFBorderType bt = null;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideV()) {
                CTBorder border = ctb.getInsideV();
                bt = stBorderTypeMap.get(border.getVal().intValue());
            }
        }
        return bt;
    }

    public int getInsideVBorderSize() {
        int size = -1;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideV()) {
                CTBorder border = ctb.getInsideV();
                size = border.getSz().intValue();
            }
        }
        return size;
    }

    public int getInsideVBorderSpace() {
        int space = -1;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideV()) {
                CTBorder border = ctb.getInsideV();
                space = border.getSpace().intValue();
            }
        }
        return space;
    }

    public String getInsideVBorderColor() {
        String color = null;

        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblBorders()) {
            CTTblBorders ctb = tblPr.getTblBorders();
            if (ctb.isSetInsideV()) {
                CTBorder border = ctb.getInsideV();
                color = border.xgetColor().getStringValue();
            }
        }
        return color;
    }

    public int getRowBandSize() {
        int size = 0;
        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblStyleRowBandSize()) {
            CTDecimalNumber rowSize = tblPr.getTblStyleRowBandSize();
            size = rowSize.getVal().intValue();
        }
        return size;
    }

    public void setRowBandSize(int size) {
        CTTblPr tblPr = getTrPr();
        CTDecimalNumber rowSize = tblPr.isSetTblStyleRowBandSize() ? tblPr.getTblStyleRowBandSize() : tblPr.addNewTblStyleRowBandSize();
        rowSize.setVal(BigInteger.valueOf(size));
    }

    public int getColBandSize() {
        int size = 0;
        CTTblPr tblPr = getTrPr();
        if (tblPr.isSetTblStyleColBandSize()) {
            CTDecimalNumber colSize = tblPr.getTblStyleColBandSize();
            size = colSize.getVal().intValue();
        }
        return size;
    }

    public void setColBandSize(int size) {
        CTTblPr tblPr = getTrPr();
        CTDecimalNumber colSize = tblPr.isSetTblStyleColBandSize() ? tblPr.getTblStyleColBandSize() : tblPr.addNewTblStyleColBandSize();
        colSize.setVal(BigInteger.valueOf(size));
    }

    public void setInsideHBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTTblPr tblPr = getTrPr();
        CTTblBorders ctb = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        CTBorder b = ctb.isSetInsideH() ? ctb.getInsideH() : ctb.addNewInsideH();
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }

    public void setInsideVBorder(XWPFBorderType type, int size, int space, String rgbColor) {
        CTTblPr tblPr = getTrPr();
        CTTblBorders ctb = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        CTBorder b = ctb.isSetInsideV() ? ctb.getInsideV() : ctb.addNewInsideV();
        b.setVal(xwpfBorderTypeMap.get(type));
        b.setSz(BigInteger.valueOf(size));
        b.setSpace(BigInteger.valueOf(space));
        b.setColor(rgbColor);
    }

    public int getCellMarginTop() {
        int margin = 0;
        CTTblPr tblPr = getTrPr();
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
        CTTblPr tblPr = getTrPr();
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
        CTTblPr tblPr = getTrPr();
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
        CTTblPr tblPr = getTrPr();
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
        CTTblPr tblPr = getTrPr();
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
     * @param row	the row which should be added
     */
    public void addRow(XWPFTableRow row){
        ctTbl.addNewTr();
        ctTbl.setTrArray(getNumberOfRows()-1, row.getCtRow());
        tableRows.add(row);
    }

    /**
     * add a new Row to the table
     * at position pos
     * @param row	the row which should be added
     */
    public boolean addRow(XWPFTableRow row, int pos){
        if(pos >= 0 && pos <= tableRows.size()){
            ctTbl.insertNewTr(pos);
            ctTbl.setTrArray(pos,row.getCtRow());
            tableRows.add(pos, row);
            return true;
        }
        return false;
    }

    /**
     * inserts a new tablerow 
     * @param pos
     * @return  the inserted row
     */
    public XWPFTableRow insertNewTableRow(int pos){
        if(pos >= 0 && pos <= tableRows.size()){
            CTRow row = ctTbl.insertNewTr(pos);
            XWPFTableRow tableRow = new XWPFTableRow(row, this);
            tableRows.add(pos, tableRow);
            return tableRow;
        }
        return null;
    }


    /**
     * Remove a row at position pos from the table
     * @param pos	position the Row in the Table
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
        return tableRows;
    }


    /**
     * returns the type of the BodyElement Table
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
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public POIXMLDocumentPart getPart() {
        if(part != null){
            return part.getPart();
        }
        return null;
    }

    /**
     * returns the partType of the bodyPart which owns the bodyElement
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
        for(int i=0; i<getRows().size(); i++){
            if(getRows().get(i).getCtRow()== row) return getRow(i); 
        }
        return null;
    }
}
