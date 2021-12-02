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

package org.apache.poi.ss.formula.eval;

import org.apache.poi.ss.formula.ThreeDEval;
import org.apache.poi.ss.formula.TwoDEval;
/**
 * Evaluation of 2D (Row+Column) and 3D (Sheet+Row+Column) areas
 */
public interface AreaEval extends TwoDEval, ThreeDEval {

    /**
     * returns the 0-based index of the first row in
     * this area.
     */
    int getFirstRow();

    /**
     * returns the 0-based index of the last row in
     * this area.
     */
    int getLastRow();

    /**
     * returns the 0-based index of the first col in
     * this area.
     */
    int getFirstColumn();

    /**
     * returns the 0-based index of the last col in
     * this area.
     */
    int getLastColumn();

    /**
     * @return the ValueEval from within this area at the specified row and col index. Never
     * {@code null} (possibly {@link BlankEval}).  The specified indexes should be absolute
     * indexes in the sheet and not relative indexes within the area.
     */
    ValueEval getAbsoluteValue(int row, int col);

    /**
     * returns true if the cell at row and col specified
     * as absolute indexes in the sheet is contained in
     * this area.
     */
    boolean contains(int row, int col);

    /**
     * returns true if the specified col is in range
     */
    boolean containsColumn(int col);

    /**
     * returns true if the specified row is in range
     */
    boolean containsRow(int row);

    @Override
    int getWidth();
    @Override
    int getHeight();
    /**
     * @return the ValueEval from within this area at the specified relativeRowIndex and
     * relativeColumnIndex. Never {@code null} (possibly {@link BlankEval}). The
     * specified indexes should relative to the top left corner of this area.
     */
    ValueEval getRelativeValue(int relativeRowIndex, int relativeColumnIndex);

    /**
     * Creates an AreaEval offset by a relative amount from the upper left cell
     * of this area
     */
    AreaEval offset(int relFirstRowIx, int relLastRowIx, int relFirstColIx, int relLastColIx);
}
