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
package org.apache.poi.ss.usermodel;

/**
 * A client anchor is attached to an excel worksheet.  It anchors against a
 * top-left and bottom-right cell.
 *
 * @author Yegor Kozlov
 */
public interface ClientAnchor {
    /**
     * Move and Resize With Anchor Cells
     * <p>
     * Specifies that the current drawing shall move and
     * resize to maintain its row and column anchors (i.e. the
     * object is anchored to the actual from and to row and column)
     * </p>
     */
    public static final int MOVE_AND_RESIZE = 0;

    /**
     * Move With Cells but Do Not Resize
     * <p>
     * Specifies that the current drawing shall move with its
     * row and column (i.e. the object is anchored to the
     * actual from row and column), but that the size shall remain absolute.
     * </p>
     * <p>
     * If additional rows/columns are added between the from and to locations of the drawing,
     * the drawing shall move its to anchors as needed to maintain this same absolute size.
     * </p>
     */
    public static final int MOVE_DONT_RESIZE = 2;

    /**
     * Do Not Move or Resize With Underlying Rows/Columns
     * <p>
     * Specifies that the current start and end positions shall
     * be maintained with respect to the distances from the
     * absolute start point of the worksheet.
     * </p>
     * <p>
     * If additional rows/columns are added before the
     * drawing, the drawing shall move its anchors as needed
     * to maintain this same absolute position.
     * </p>
     */
    public static final int DONT_MOVE_AND_RESIZE = 3;

    /**
     * Returns the column (0 based) of the first cell.
     *
     * @return 0-based column of the first cell.
     */
    public short getCol1();

    /**
     * Sets the column (0 based) of the first cell.
     *
     * @param col1 0-based column of the first cell.
     */
    public void setCol1(int col1);

    /**
     * Returns the column (0 based) of the second cell.
     *
     * @return 0-based column of the second cell.
     */
    public short getCol2();

    /**
     * Returns the column (0 based) of the second cell.
     *
     * @param col2 0-based column of the second cell.
     */
    public void setCol2(int col2);

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @return 0-based row of the first cell.
     */
    public int getRow1();

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @param row1 0-based row of the first cell.
     */
    public void setRow1(int row1);

    /**
     * Returns the row (0 based) of the second cell.
     *
     * @return 0-based row of the second cell.
     */
    public int getRow2();

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @param row2 0-based row of the first cell.
     */
    public void setRow2(int row2);

    /**
     * Returns the x coordinate within the first cell
     *
     * @return the x coordinate within the first cell
     */
    public int getDx1();

    /**
     * Sets the x coordinate within the first cell
     *
     * @param dx1 the x coordinate within the first cell
     */
    public void setDx1(int dx1);

    /**
     * Returns the y coordinate within the first cell
     *
     * @return the y coordinate within the first cell
     */
    public int getDy1();

    /**
     * Sets the y coordinate within the first cell
     *
     * @param dy1 the y coordinate within the first cell
     */
    public void setDy1(int dy1);

    /**
     * Sets the y coordinate within the second cell
     *
     * @return the y coordinate within the second cell
     */
    public int getDy2();

    /**
     * Sets the y coordinate within the second cell
     *
     * @param dy2 the y coordinate within the second cell
     */
    public void setDy2(int dy2);

    /**
     * Returns the x coordinate within the second cell
     *
     * @return the x coordinate within the second cell
     */
    public int getDx2();

    /**
     * Sets the x coordinate within the second cell
     *
     * @param dx2 the x coordinate within the second cell
     */
    public void setDx2(int dx2);


    /**
     * Sets the anchor type
     * <p>
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     * </p>
     * @param anchorType the anchor type
     * @see #MOVE_AND_RESIZE
     * @see #MOVE_DONT_RESIZE
     * @see #DONT_MOVE_AND_RESIZE
     */
    public void setAnchorType( int anchorType );

    /**
     * Gets the anchor type
     * <p>
     * 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.
     * </p>
     * @return the anchor type
     * @see #MOVE_AND_RESIZE
     * @see #MOVE_DONT_RESIZE
     * @see #DONT_MOVE_AND_RESIZE
     */
    public int getAnchorType();

}
