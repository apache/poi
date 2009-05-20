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

import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.apache.poi.ss.usermodel.ClientAnchor;

/**
 * A client anchor is attached to an excel worksheet.  It anchors against
 * top-left and bottom-right cells.
 *
 * @author Yegor Kozlov
 */
public final class XSSFClientAnchor extends XSSFAnchor implements ClientAnchor {
    private int anchorType;

    /**
     * Starting anchor point
     */
    private CTMarker cell1;

    /**
     * Ending anchor point
     */
    private CTMarker cell2;

    /**
     * Creates a new client anchor and defaults all the anchor positions to 0.
     */
    public XSSFClientAnchor() {
        cell1 = CTMarker.Factory.newInstance();
        cell1.setCol(0);
        cell1.setColOff(0);
        cell1.setRow(0);
        cell1.setRowOff(0);
        cell2 = CTMarker.Factory.newInstance();
        cell2.setCol(0);
        cell2.setColOff(0);
        cell2.setRow(0);
        cell2.setRowOff(0);
    }

    /**
     * Creates a new client anchor and sets the top-left and bottom-right
     * coordinates of the anchor.
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
        this();
        cell1.setCol(col1);
        cell1.setColOff(dx1);
        cell1.setRow(row1);
        cell1.setRowOff(dy1);
        cell2.setCol(col2);
        cell2.setColOff(dx2);
        cell2.setRow(row2);
        cell2.setRowOff(dy2);
    }

    /**
     * Create XSSFClientAnchor from existing xml beans
     *
     * @param cell1 starting anchor point
     * @param cell2 ending anchor point
     */
    protected XSSFClientAnchor(CTMarker cell1, CTMarker cell2) {
        this.cell1 = cell1;
        this.cell2 = cell2;
    }

    public short getCol1() {
        return (short)cell1.getCol();
    }

    public void setCol1(int col1) {
        cell1.setCol(col1);
    }

    public short getCol2() {
        return (short)cell2.getCol();
    }

    public void setCol2(int col2) {
        cell2.setCol(col2);
    }

    public int getRow1() {
        return cell1.getRow();
    }

    public void setRow1(int row1) {
        cell1.setRow(row1);
    }

    public int getRow2() {
        return cell2.getRow();
    }

    public void setRow2(int row2) {
        cell2.setRow(row2);
    }

    public int getDx1() {
        return (int)cell1.getColOff();
    }

    public void setDx1(int dx1) {
        cell1.setColOff(dx1);
    }

    public int getDy1() {
        return (int)cell1.getRowOff();
    }

    public void setDy1(int dy1) {
        cell1.setRowOff(dy1);
    }

    public int getDy2() {
        return (int)cell2.getRowOff();
    }

    public void setDy2(int dy2) {
        cell2.setRowOff(dy2);
    }

    public int getDx2() {
        return (int)cell2.getColOff();
    }

    public void setDx2(int dx2) {
        cell2.setColOff(dx2);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XSSFClientAnchor)) return false;

        XSSFClientAnchor anchor = (XSSFClientAnchor) o;
        return cell1.toString().equals(anchor.getFrom().toString()) &&
               cell2.toString().equals(anchor.getTo().toString()) ;

    }

    @Override
    public String toString(){
        return "from : " + cell1.toString()  + "; to: " + cell2.toString();
    }

    /**
     * Return starting anchor point
     *
     * @return starting anchor point
     */
    public CTMarker getFrom(){
        return cell1;
    }

    protected void setFrom(CTMarker from){
        cell1 = from;
    }

    /**
     * Return ending anchor point
     *
     * @return ending anchor point
     */
    public CTMarker getTo(){
        return cell2;
    }

    protected void setTo(CTMarker to){
        cell2 = to;
    }


    /**
     * Sets the anchor type
     * <p>
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    public void setAnchorType( int anchorType )
    {
        this.anchorType = anchorType;
    }

    /**
     * Gets the anchor type
     * <p>
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     */
    public int getAnchorType()
    {
        return anchorType;
    }

}
