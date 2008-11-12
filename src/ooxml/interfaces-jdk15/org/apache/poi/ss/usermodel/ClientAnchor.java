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
}
