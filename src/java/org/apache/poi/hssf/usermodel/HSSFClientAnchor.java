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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.ddf.EscherRecord;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.ClientAnchor;

/**
 * A client anchor is attached to an excel worksheet.  It anchors against a
 * top-left and buttom-right cell.
 */
public final class HSSFClientAnchor extends HSSFAnchor implements ClientAnchor {

    public static final int MAX_COL = SpreadsheetVersion.EXCEL97.getLastColumnIndex();
    public static final int MAX_ROW = SpreadsheetVersion.EXCEL97.getLastRowIndex();

    private EscherClientAnchorRecord _escherClientAnchor;

    public HSSFClientAnchor(EscherClientAnchorRecord escherClientAnchorRecord) {
        this._escherClientAnchor = escherClientAnchorRecord;
    }

    /**
     * Creates a new client anchor and defaults all the anchor positions to 0.
     */
    public HSSFClientAnchor() {
    }

    /**
     * Creates a new client anchor and sets the top-left and bottom-right
     * coordinates of the anchor.
     * 
     * Note: Microsoft Excel seems to sometimes disallow 
     * higher y1 than y2 or higher x1 than x2, you might need to 
     * reverse them and draw shapes vertically or horizontally flipped! 
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
    public HSSFClientAnchor(int dx1, int dy1, int dx2, int dy2, short col1, int row1, short col2, int row2) {
        super(dx1, dy1, dx2, dy2);

        checkRange(dx1, 0, 1023, "dx1");
        checkRange(dx2, 0, 1023, "dx2");
        checkRange(dy1, 0, 255, "dy1");
        checkRange(dy2, 0, 255, "dy2");
        checkRange(col1, 0, MAX_COL, "col1");
        checkRange(col2, 0, MAX_COL, "col2");
        checkRange(row1, 0, MAX_ROW, "row1");
        checkRange(row2, 0, MAX_ROW, "row2");

        setCol1((short) Math.min(col1, col2));
        setCol2((short) Math.max(col1, col2));
        setRow1(Math.min(row1, row2));
        setRow2(Math.max(row1, row2));

        if (col1 > col2){
            _isHorizontallyFlipped = true;
        }
        if (row1 > row2){
            _isVerticallyFlipped = true;
        }
    }

    /**
     * Calculates the height of a client anchor in points.
     *
     * @param sheet the sheet the anchor will be attached to
     * @return the shape height.
     */
    public float getAnchorHeightInPoints(HSSFSheet sheet) {
        int y1 = getDy1();
        int y2 = getDy2();
        int row1 = Math.min(getRow1(), getRow2());
        int row2 = Math.max(getRow1(), getRow2());

        float points = 0;
        if (row1 == row2) {
            points = ((y2 - y1) / 256.0f) * getRowHeightInPoints(sheet, row2);
        } else {
            points += ((256.0f - y1) / 256.0f) * getRowHeightInPoints(sheet, row1);
            for (int i = row1 + 1; i < row2; i++) {
                points += getRowHeightInPoints(sheet, i);
            }
            points += (y2 / 256.0f) * getRowHeightInPoints(sheet, row2);
        }

        return points;
    }

    private float getRowHeightInPoints(HSSFSheet sheet, int rowNum) {
        HSSFRow row = sheet.getRow(rowNum);
        if (row == null) {
            return sheet.getDefaultRowHeightInPoints();
        }
        return row.getHeightInPoints();
    }

    /**
     * @return the column(0 based) of the first cell.
     */
    public short getCol1() {
        return _escherClientAnchor.getCol1();
    }

    /**
     * @param col1 the column(0 based) of the first cell.
     */
    public void setCol1(short col1) {
        checkRange(col1, 0, MAX_COL, "col1");
        _escherClientAnchor.setCol1(col1);
    }

    /**
     * @param col1 0-based column of the first cell.
     */
    public void setCol1(int col1) {
        setCol1((short) col1);
    }

    /**
     * @return the column(0 based) of the first cell.
     */
    public short getCol2() {
        return _escherClientAnchor.getCol2();
    }

    /**
     * @param col2 the column(0 based) of the second cell.
     */
    public void setCol2(short col2) {
        checkRange(col2, 0, MAX_COL, "col2");
        _escherClientAnchor.setCol2(col2);
    }

    /**
     * @param col2 the column(0 based) of the second cell.
     */
    public void setCol2(int col2) {
        setCol2((short) col2);
    }

    /**
     * @return the row(0 based) of the first cell.
     */
    public int getRow1() {
        return unsignedValue(_escherClientAnchor.getRow1());
    }

    /**
     * @param row1 0-based row of the first cell.
     */
    public void setRow1(int row1) {
        checkRange(row1, 0, MAX_ROW, "row1");
        _escherClientAnchor.setRow1(Integer.valueOf(row1).shortValue());
    }

    /**
     * @return the row(0 based) of the second cell.
     */
    public int getRow2() {
        return unsignedValue(_escherClientAnchor.getRow2());
    }

    /**
     * @param row2 the row(0 based) of the second cell.
     */
    public void setRow2(int row2) {
        checkRange(row2, 0, MAX_ROW, "row2");
        _escherClientAnchor.setRow2(Integer.valueOf(row2).shortValue());
    }

    /**
     * Sets the top-left and bottom-right coordinates of 
     * the anchor.
     * 
     * Note: Microsoft Excel seems to sometimes disallow 
     * higher y1 than y2 or higher x1 than x2, you might need to 
     * reverse them and draw shapes vertically or horizontally flipped! 
     *
     * @param x1   the x coordinate within the first cell.
     * @param y1   the y coordinate within the first cell.
     * @param x2   the x coordinate within the second cell.
     * @param y2   the y coordinate within the second cell.
     * @param col1 the column (0 based) of the first cell.
     * @param row1 the row (0 based) of the first cell.
     * @param col2 the column (0 based) of the second cell.
     * @param row2 the row (0 based) of the second cell.
     */
    public void setAnchor(short col1, int row1, int x1, int y1, short col2, int row2, int x2, int y2) {
        checkRange(getDx1(), 0, 1023, "dx1");
        checkRange(getDx2(), 0, 1023, "dx2");
        checkRange(getDy1(), 0, 255, "dy1");
        checkRange(getDy2(), 0, 255, "dy2");
        checkRange(getCol1(), 0, MAX_COL, "col1");
        checkRange(getCol2(), 0, MAX_COL, "col2");
        checkRange(getRow1(), 0, MAX_ROW, "row1");
        checkRange(getRow2(), 0, MAX_ROW, "row2");

        setCol1(col1);
        setRow1(row1);
        setDx1(x1);
        setDy1(y1);
        setCol2(col2);
        setRow2(row2);
        setDx2(x2);
        setDy2(y2);
    }

    public boolean isHorizontallyFlipped() {
        return _isHorizontallyFlipped;
    }

    public boolean isVerticallyFlipped() {
        return _isVerticallyFlipped;
    }

    @Override
    protected EscherRecord getEscherAnchor() {
        return _escherClientAnchor;
    }

    @Override
    protected void createEscherAnchor() {
        _escherClientAnchor = new EscherClientAnchorRecord();
    }

    /**
     * Gets the anchor type
     * Changed from returning an int to an enum in POI 3.14 beta 1.
     * @return the anchor type
     */
    @Override
    public AnchorType getAnchorType() {
        return AnchorType.byId(_escherClientAnchor.getFlag());
    }

    /**
     * Sets the anchor type
     * @param anchorType the anchor type to set
     * @since POI 3.14
     */
    @Override
    public void setAnchorType(AnchorType anchorType) {
        _escherClientAnchor.setFlag(anchorType.value);
    }

    private void checkRange(int value, int minRange, int maxRange, String varName) {
        if (value < minRange || value > maxRange)
            throw new IllegalArgumentException(varName + " must be between " + minRange + " and " + maxRange + ", but was: " + value);
    }

    /**
     * Given a 16-bit unsigned integer stored in a short, return the unsigned value.
     *
     * @param s A 16-bit value intended to be interpreted as an unsigned integer.
     * @return The value represented by <code>s</code>.
     */
    private static int unsignedValue(final short s) {
        return (s < 0 ? 0x10000 + s : s);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj.getClass() != getClass())
            return false;
        HSSFClientAnchor anchor = (HSSFClientAnchor) obj;

        return anchor.getCol1() == getCol1() && anchor.getCol2() == getCol2() && anchor.getDx1() == getDx1()
                && anchor.getDx2() == getDx2() && anchor.getDy1() == getDy1() && anchor.getDy2() == getDy2()
                && anchor.getRow1() == getRow1() && anchor.getRow2() == getRow2() && anchor.getAnchorType() == getAnchorType();
    }

    @Override
    public int hashCode() {
        assert false : "hashCode not designed";
        return 42; // any arbitrary constant will do
      }

    @Override
    public int getDx1() {
        return _escherClientAnchor.getDx1();
    }

    @Override
    public void setDx1(int dx1) {
        _escherClientAnchor.setDx1(Integer.valueOf(dx1).shortValue());
    }

    @Override
    public int getDy1() {
        return _escherClientAnchor.getDy1();
    }

    @Override
    public void setDy1(int dy1) {
        _escherClientAnchor.setDy1(Integer.valueOf(dy1).shortValue());
    }

    @Override
    public int getDy2() {
        return _escherClientAnchor.getDy2();
    }

    @Override
    public void setDy2(int dy2) {
        _escherClientAnchor.setDy2(Integer.valueOf(dy2).shortValue());
    }

    @Override
    public int getDx2() {
        return _escherClientAnchor.getDx2();
    }

    @Override
    public void setDx2(int dx2) {
        _escherClientAnchor.setDx2(Integer.valueOf(dx2).shortValue());
    }
}
