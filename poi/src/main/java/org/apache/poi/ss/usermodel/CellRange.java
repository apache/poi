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
     * @return {@code height * width }
     */
    int size();

    /**
     * @return the text format of this range.  Single cell ranges are formatted
     *         like single cell references (e.g. 'A1' instead of 'A1:A1').
     */
    String getReferenceText();

    /**
     * @return the cell at relative coordinates (0,0).  Never {@code null}.
     */
    C getTopLeftCell();

    /**
     * @param relativeRowIndex must be between {@code 0} and {@code height-1}
     * @param relativeColumnIndex must be between {@code 0} and {@code width-1}
     * @return the cell at the specified coordinates.  Never {@code null}.
     */
    C getCell(int relativeRowIndex, int relativeColumnIndex);
    /**
     * @return a flattened array of all the cells in this CellRange
     */
    C[] getFlattenedCells();
    /**
     * @return a 2-D array of all the cells in this CellRange.  The first
     * array dimension is the row index (values {@code 0...height-1})
     * and the second dimension is the column index (values {@code 0...width-1})
     */
    C[][] getCells();

    /**
     * @return an {@link Iterator} over all cells in this range.  Iteration starts
     * with all cells in the first row followed by all cells in the next row, etc.
     */
    @Override
    Iterator<C> iterator();
}
