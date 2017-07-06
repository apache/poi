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

import org.apache.poi.util.Internal;

/**
 * A client anchor is attached to an excel worksheet.  It anchors against 
 * absolute coordinates, a top-left cell and fixed height and width, or
 * a top-left and bottom-right cell, depending on the {@link AnchorType}:
 * <ol>
 * <li> {@link AnchorType#DONT_MOVE_AND_RESIZE} == absolute top-left coordinates and width/height, no cell references
 * <li> {@link AnchorType#MOVE_DONT_RESIZE} == fixed top-left cell reference, absolute width/height
 * <li> {@link AnchorType#MOVE_AND_RESIZE} == fixed top-left and bottom-right cell references, dynamic width/height
 * </ol>
 * Note this class only reports the current values for possibly calculated positions and sizes.
 * If the sheet row/column sizes or positions shift, this needs updating via external calculations.
 * 
 */
public interface ClientAnchor {
    
    /**
     * @since POI 3.14beta1
     */
    public static enum AnchorType {
        /**
         * Move and Resize With Anchor Cells (0)
         * <p>
         * Specifies that the current drawing shall move and
         * resize to maintain its row and column anchors (i.e. the
         * object is anchored to the actual from and to row and column)
         * </p>
         */
        MOVE_AND_RESIZE(0),
        
        /**
         * Don't Move but do Resize With Anchor Cells (1)
         * <p>
         * Specifies that the current drawing shall not move with its
         * row and column, but should be resized. This option is not normally
         * used, but is included for completeness. 
         * </p>
         * Note: Excel has no setting for this combination, nor does the ECMA standard.
         */
        DONT_MOVE_DO_RESIZE(1),
        
        /**
         * Move With Cells but Do Not Resize (2)
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
        MOVE_DONT_RESIZE(2),
        
        /**
         * Do Not Move or Resize With Underlying Rows/Columns (3)
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
        DONT_MOVE_AND_RESIZE(3);
        
        public final short value;

        // disallow non-sequential enum instance creation
        private AnchorType(int value) {
            this.value = (short) value;
        }
        
        /**
         * return the AnchorType corresponding to the code
         *
         * @param value the anchor type code
         * @return the anchor type enum
         */
        @Internal
        public static AnchorType byId(int value) {
            return values()[value];
        }
    }
    
    /**
     * Returns the column (0 based) of the first cell, or -1 if there is no top-left anchor cell.
     * This is the case for absolute positioning {@link AnchorType#MOVE_AND_RESIZE}
     *
     * @return 0-based column of the first cell or -1 if none.
     */
    public short getCol1();

    /**
     * Sets the column (0 based) of the first cell.
     *
     * @param col1 0-based column of the first cell.
     */
    public void setCol1(int col1);

    /**
     * Returns the column (0 based) of the second cell, or -1 if there is no bottom-right anchor cell.
     * This is the case for absolute positioning ({@link AnchorType#DONT_MOVE_AND_RESIZE})
     * and absolute sizing ({@link AnchorType#MOVE_DONT_RESIZE}.
     *
     * @return 0-based column of the second cell or -1 if none.
     */
    public short getCol2();

    /**
     * Returns the column (0 based) of the second cell.
     *
     * @param col2 0-based column of the second cell.
     */
    public void setCol2(int col2);

    /**
     * Returns the row (0 based) of the first cell, or -1 if there is no bottom-right anchor cell.
     * This is the case for absolute positioning ({@link AnchorType#DONT_MOVE_AND_RESIZE}).
     *
     * @return 0-based row of the first cell or -1 if none.
     */
    public int getRow1();

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @param row1 0-based row of the first cell.
     */
    public void setRow1(int row1);

    /**
     * Returns the row (0 based) of the second cell, or -1 if there is no bottom-right anchor cell.
     * This is the case for absolute positioning ({@link AnchorType#DONT_MOVE_AND_RESIZE})
     * and absolute sizing ({@link AnchorType#MOVE_DONT_RESIZE}.
     *
     * @return 0-based row of the second cell or -1 if none.
     */
    public int getRow2();

    /**
     * Returns the row (0 based) of the first cell.
     *
     * @param row2 0-based row of the first cell.
     */
    public void setRow2(int row2);

    /**
     * Returns the x coordinate within the first cell.
     * 
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @return the x coordinate within the first cell
     */
    public int getDx1();

    /**
     * Sets the x coordinate within the first cell
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @param dx1 the x coordinate within the first cell
     */
    public void setDx1(int dx1);

    /**
     * Returns the y coordinate within the first cell
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @return the y coordinate within the first cell
     */
    public int getDy1();

    /**
     * Sets the y coordinate within the first cell
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @param dy1 the y coordinate within the first cell
     */
    public void setDy1(int dy1);

    /**
     * Sets the y coordinate within the second cell
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @return the y coordinate within the second cell
     */
    public int getDy2();

    /**
     * Sets the y coordinate within the second cell
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @param dy2 the y coordinate within the second cell
     */
    public void setDy2(int dy2);

    /**
     * Returns the x coordinate within the second cell
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @return the x coordinate within the second cell
     */
    public int getDx2();

    /**
     * Sets the x coordinate within the second cell
     *
     * Note - XSSF and HSSF have a slightly different coordinate
     *  system, values in XSSF are larger by a factor of
     *  {@link org.apache.poi.util.Units#EMU_PER_PIXEL}
     *
     * @param dx2 the x coordinate within the second cell
     */
    public void setDx2(int dx2);


    /**
     * Sets the anchor type
     * @param anchorType the anchor type to set
     * @since POI 3.14
     */
    public void setAnchorType( AnchorType anchorType );

    /**
     * Gets the anchor type
     * Changed from returning an int to an enum in POI 3.14 beta 1.
     * @return the anchor type
     */
    public AnchorType getAnchorType();

}
