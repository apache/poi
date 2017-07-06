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

package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.ClientAnchor.AnchorType;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Units;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;

/**
 * A client anchor is attached to an excel worksheet.  It anchors against:
 * <ol>
 * <li>A fixed position and fixed size
 * <li>A position relative to a cell (top-left) and a fixed size
 * <li>A position relative to a cell (top-left) and sized relative to another cell (bottom right)
 * </ol>
 *
 * which method is used is determined by the {@link AnchorType}.  
 */
public class XSSFClientAnchor extends XSSFAnchor implements ClientAnchor {
    
    /**
     * placeholder for zeros when needed for dynamic position calculations
     */
    private static final CTMarker EMPTY_MARKER = CTMarker.Factory.newInstance();
    
    private AnchorType anchorType;

    /**
     * Starting anchor point (top-left cell + relative offset)
     * if left null recalculate as needed from point
     */
    private CTMarker cell1;

    /**
     * Ending anchor point (bottom-right cell + relative offset)
     * if left null, re-calculate as needed from size and cell1
     */
    private CTMarker cell2;

    /**
     * if present, fixed size of the object to use instead of cell2, which is inferred instead
     */
    private CTPositiveSize2D size;
    
    /**
     * if present, fixed top-left position to use instead of cell1, which is inferred instead
     */
    private CTPoint2D position;
    
    /**
     * sheet to base dynamic calculations on, if needed.  Required if size and/or position or set.
     * Not needed if cell1/2 are set explicitly (dynamic sizing and position relative to cells).
     */
    private XSSFSheet sheet;
    
    /**
     * Creates a new client anchor and defaults all the anchor positions to 0.
     * Sets the type to {@link AnchorType#MOVE_AND_RESIZE} relative to cell range A1:A1.
     */
    public XSSFClientAnchor() {
        this(0,0,0,0,0,0,0,0);
    }

    /**
     * Creates a new client anchor and sets the top-left and bottom-right
     * coordinates of the anchor by cell references and offsets.
     * Sets the type to {@link AnchorType#MOVE_AND_RESIZE}.
     *
     * @param dx1  the x coordinate within the first cell.
     * @param dy1  the y coordinate within the first cell.
     * @param dx2  the x coordinate within the second cell.
     * @param dy2  the y coordinate within the second cell.
     * @param col1 the column (0 based) of the first cell.
     * @param row1 the row (0 based) of the first cell.
     * @param col2 the column (0 based) of the second cell.
     * @param row2 the row (0 based) of the second cell.
     */
    public XSSFClientAnchor(int dx1, int dy1, int dx2, int dy2, int col1, int row1, int col2, int row2) {
        anchorType = AnchorType.MOVE_AND_RESIZE;
        cell1 = CTMarker.Factory.newInstance();
        cell1.setCol(col1);
        cell1.setColOff(dx1);
        cell1.setRow(row1);
        cell1.setRowOff(dy1);
        cell2 = CTMarker.Factory.newInstance();
        cell2.setCol(col2);
        cell2.setColOff(dx2);
        cell2.setRow(row2);
        cell2.setRowOff(dy2);
    }

    /**
     * Create XSSFClientAnchor from existing xml beans, sized and positioned relative to a pair of cells.
     * Sets the type to {@link AnchorType#MOVE_AND_RESIZE}.
     * @param cell1 starting anchor point
     * @param cell2 ending anchor point
     */
    protected XSSFClientAnchor(CTMarker cell1, CTMarker cell2) {
        anchorType = AnchorType.MOVE_AND_RESIZE;
        this.cell1 = cell1;
        this.cell2 = cell2;
    }

    /**
     * Create XSSFClientAnchor from existing xml beans, sized and positioned relative to a pair of cells.
     * Sets the type to {@link AnchorType#MOVE_DONT_RESIZE}.
     *
     * @param sheet needed to calculate ending point based on column/row sizes
     * @param cell1 starting anchor point
     * @param size object size, to calculate ending anchor point
     */
    protected XSSFClientAnchor(XSSFSheet sheet, CTMarker cell1, CTPositiveSize2D size) {
        anchorType = AnchorType.MOVE_DONT_RESIZE;
        this.sheet = sheet;
        this.size = size;
        this.cell1 = cell1;
//        this.cell2 = calcCell(sheet, cell1, size.getCx(), size.getCy());
    }
    
    /**
     * Create XSSFClientAnchor from existing xml beans, sized and positioned relative to a pair of cells.
     * Sets the type to {@link AnchorType#DONT_MOVE_AND_RESIZE}.
     *
     * @param sheet needed to calculate starting and ending points based on column/row sizes
     * @param position starting absolute position
     * @param size object size, to calculate ending position
     */
    protected XSSFClientAnchor(XSSFSheet sheet, CTPoint2D position, CTPositiveSize2D size) {
        anchorType = AnchorType.DONT_MOVE_AND_RESIZE;
        this.sheet = sheet;
        this.position = position;
        this.size = size;
        // zeros for row/col/offsets
//        this.cell1 = calcCell(sheet, EMPTY_MARKER, position.getCx(), position.getCy());
//        this.cell2 = calcCell(sheet, cell1, size.getCx(), size.getCy());
    }
    
    /**
     *
     * @param sheet
     * @param cell starting point and offsets (may be zeros)
     * @param size dimensions to calculate relative to starting point
     */
    private CTMarker calcCell(CTMarker cell, long w, long h) {
        CTMarker c2 = CTMarker.Factory.newInstance();
        
        int r = cell.getRow();
        int c = cell.getCol();
        
        int cw = Units.columnWidthToEMU(sheet.getColumnWidth(c));
        
        // start with width - offset, then keep adding column widths until the next one puts us over w
        long wPos = cw - cell.getColOff();
        
        while (wPos < w) {
            c++;
            cw = Units.columnWidthToEMU(sheet.getColumnWidth(c));
            wPos += cw;
        }
        // now wPos >= w, so end column = c, now figure offset
        c2.setCol(c);
        c2.setColOff(cw - (wPos - w));
        
        int rh = Units.toEMU(getRowHeight(sheet, r));
        // start with height - offset, then keep adding row heights until the next one puts us over h
        long hPos = rh - cell.getRowOff();
        
        while (hPos < h) {
            r++;
            rh = Units.toEMU(getRowHeight(sheet, r));
            hPos += rh;
        }
        // now hPos >= h, so end row = r, now figure offset
        c2.setRow(r);
        c2.setRowOff(rh - (hPos - h));
        
        return c2;
    }
    
    /**
     * @param sheet
     * @param row
     * @return height in twips (1/20th of point) for row or default
     */
    private static float getRowHeight(XSSFSheet sheet, int row) {
        XSSFRow r = sheet.getRow(row);
        return r == null ? sheet.getDefaultRowHeightInPoints() : r.getHeightInPoints();
    }
    
    private CTMarker getCell1() {
        return cell1 != null ? cell1 : calcCell(EMPTY_MARKER, position.getX(), position.getY());
    }
    
    private CTMarker getCell2() {
        return cell2 != null ? cell2 : calcCell(getCell1(), size.getCx(), size.getCy());
    }
    
    public short getCol1() {
        return (short)getCell1().getCol();
    }

    /**
     * @throws NullPointerException if cell1 is null (fixed position)
     * @see org.apache.poi.ss.usermodel.ClientAnchor#setCol1(int)
     */
    public void setCol1(int col1) {
        cell1.setCol(col1);
    }

    public short getCol2() {
        return (short) getCell2().getCol();
    }

    /**
     * @throws NullPointerException if cell2 is null (fixed size)
     * @see org.apache.poi.ss.usermodel.ClientAnchor#setCol2(int)
     */
    public void setCol2(int col2) {
        cell2.setCol(col2);
    }

    public int getRow1() {
        return getCell1().getRow();
    }

    /**
     * @throws NullPointerException if cell1 is null (fixed position)
     * @see org.apache.poi.ss.usermodel.ClientAnchor#setRow1(int)
     */
    public void setRow1(int row1) {
        cell1.setRow(row1);
    }

    public int getRow2() {
        return getCell2().getRow();
    }

    /**
     * @throws NullPointerException if cell2 is null (fixed size)
     * @see org.apache.poi.ss.usermodel.ClientAnchor#setRow2(int)
     */
    public void setRow2(int row2) {
        cell2.setRow(row2);
    }

    public int getDx1() {
        return (int) getCell1().getColOff();
    }

    /**
     * @throws NullPointerException if cell1 is null (fixed position)
     * @see org.apache.poi.ss.usermodel.ChildAnchor#setDx1(int)
     */
    public void setDx1(int dx1) {
        cell1.setColOff(dx1);
    }

    public int getDy1() {
        return (int) getCell1().getRowOff();
    }

    /**
     * @throws NullPointerException if cell1 is null (fixed position)
     * @see org.apache.poi.ss.usermodel.ChildAnchor#setDy1(int)
     */
    public void setDy1(int dy1) {
        cell1.setRowOff(dy1);
    }

    public int getDy2() {
        return (int) getCell2().getRowOff();
    }

    /**
     * @throws NullPointerException if cell2 is null (fixed size)
     * @see org.apache.poi.ss.usermodel.ChildAnchor#setDy2(int)
     */
    public void setDy2(int dy2) {
        cell2.setRowOff(dy2);
    }

    public int getDx2() {
        return (int) getCell2().getColOff();
    }

    /**
     * @throws NullPointerException if cell2 is null (fixed size)
     * @see org.apache.poi.ss.usermodel.ChildAnchor#setDx2(int)
     */
    public void setDx2(int dx2) {
        cell2.setColOff(dx2);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XSSFClientAnchor)) return false;

        XSSFClientAnchor anchor = (XSSFClientAnchor) o;
        return  getDx1() == anchor.getDx1() &&
                getDx2() == anchor.getDx2() &&
                getDy1() == anchor.getDy1() &&
                getDy2() == anchor.getDy2() &&
                getCol1() == anchor.getCol1() &&
                getCol2() == anchor.getCol2() &&
                getRow1() == anchor.getRow1() &&
                getRow2() == anchor.getRow2() ;

    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
    }

    @Override
    public String toString(){
        return "from : " + getCell1() + "; to: " + getCell2();
    }

    /**
     * Return starting anchor point
     *
     * @return starting anchor point
     */
    @Internal
    public CTMarker getFrom(){
        return getCell1();
    }

    protected void setFrom(CTMarker from){
        cell1 = from;
    }

    /**
     * Return ending anchor point
     *
     * @return ending anchor point
     */
    @Internal
    public CTMarker getTo(){
        return getCell2();
    }

    protected void setTo(CTMarker to){
        cell2 = to;
    }

    /**
     * @return absolute top-left position, or null if position is determined from the "from" cell
     * @since POI 3.17 beta 1
     */
    public CTPoint2D getPosition() {
        return position;
    }
    
    /**
     * Sets the top-left absolute position of the object.  To use this, "from" must be set to null.
     * @param position
     * @since POI 3.17 beta 1
     */
    public void setPosition(CTPoint2D position) {
        this.position = position;
    }

    /**
     *
     * @return size or null, if size is determined from the to and from cells
     * @since POI 3.17 beta 1
     */
    public CTPositiveSize2D getSize() {
        return size;
    }
    
    /**
     * Sets the size of the object.  To use this, "to" must be set to null.
     * @param size
     * @since POI 3.17 beta 1
     */
    public void setSize(CTPositiveSize2D size) {
        this.size = size;
    }

    /**
     * Sets the anchor type
     * @param anchorType the anchor type to set
     * @since POI 3.14
     */
    @Override
    public void setAnchorType( AnchorType anchorType )
    {
        this.anchorType = anchorType;
    }

    /**
     * Gets the anchor type
     * Changed from returning an int to an enum in POI 3.14 beta 1.
     * @return the anchor type
     */
    @Override
    public AnchorType getAnchorType()
    {
        return anchorType;
    }

    public boolean isSet(){
        CTMarker c1 = getCell1();
        CTMarker c2 = getCell2();
        return !(c1.getCol() == 0 && c2.getCol() == 0 &&
                 c1.getRow() == 0 && c2.getRow() == 0);
    }
}
