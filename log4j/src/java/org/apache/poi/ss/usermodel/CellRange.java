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

import java.util.Iterator;


/**
 * Represents a rectangular region of a {@link Sheet}
 */
public interface CellRange<C extends Cell> extends Iterable<C> {
    int getWidth();
    int getHeight();

    /**
     * Gets the number of cells in this range.
     * @return <tt>height * width </tt>
     */
    int size();

    /**
     * @return the text format of this range.  Single cell ranges are formatted
     *         like single cell references (e.g. 'A1' instead of 'A1:A1').
     */
    String getReferenceText();

    /**
     * @return the cell at relative coordinates (0,0).  Never <code>null</code>.
     */
    C getTopLeftCell();

    /**
     * @param relativeRowIndex must be between <tt>0</tt> and <tt>height-1</tt>
     * @param relativeColumnIndex must be between <tt>0</tt> and <tt>width-1</tt>
     * @return the cell at the specified coordinates.  Never <code>null</code>.
     */
    C getCell(int relativeRowIndex, int relativeColumnIndex);
    /**
     * @return a flattened array of all the cells in this {@link CellRange}
     */
    C[] getFlattenedCells();
    /**
     * @return a 2-D array of all the cells in this {@link CellRange}.  The first
     * array dimension is the row index (values <tt>0...height-1</tt>)
     * and the second dimension is the column index (values <tt>0...width-1</tt>)
     */
    C[][] getCells();

    /**
     * @return an {@link Iterator} over all cells in this range.  Iteration starts
     * with all cells in the first row followed by all cells in the next row, etc.
     */
    Iterator<C> iterator();
}
