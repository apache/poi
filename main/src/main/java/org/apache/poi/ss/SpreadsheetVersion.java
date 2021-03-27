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

package org.apache.poi.ss;

import org.apache.poi.ss.util.CellReference;

/**
 * This enum allows spreadsheets from multiple Excel versions to be handled by the common code.
 * <p>Properties of this enum correspond to attributes of the <i>spreadsheet</i> that are easily
 * discernable to the user.  It is not intended to deal with low-level issues like file formats.
 */
public enum SpreadsheetVersion {
    /**
     * Excel97 format aka BIFF8
     * <ul>
     * <li>The total number of available rows is 64k (2^16)</li>
     * <li>The total number of available columns is 256 (2^8)</li>
     * <li>The maximum number of arguments to a function is 30</li>
     * <li>Number of conditional format conditions on a cell is 3</li>
     * <li>Number of cell styles is 4000</li>
     * <li>Length of text cell contents is 32767</li>
     * </ul>
     */
    EXCEL97(0x10000, 0x0100, 30, 3, 4000, 32767),

    /**
     * Excel2007
     *
     * <ul>
     * <li>The total number of available rows is 1M (2^20)</li>
     * <li>The total number of available columns is 16K (2^14)</li>
     * <li>The maximum number of arguments to a function is 255</li>
     * <li>Number of conditional format conditions on a cell is unlimited
     * (actually limited by available memory in Excel)</li>
     * <li>Number of cell styles is 64000</li>
     * <li>Length of text cell contents is 32767</li>
     * <ul>
     */
    EXCEL2007(0x100000, 0x4000, 255, Integer.MAX_VALUE, 64000, 32767);

    private final int _maxRows;
    private final int _maxColumns;
    private final int _maxFunctionArgs;
    private final int _maxCondFormats;
    private final int _maxCellStyles;
    private final int _maxTextLength;

    private SpreadsheetVersion(int maxRows, int maxColumns, int maxFunctionArgs, int maxCondFormats, int maxCellStyles, int maxText) {
        _maxRows = maxRows;
        _maxColumns = maxColumns;
        _maxFunctionArgs = maxFunctionArgs;
        _maxCondFormats = maxCondFormats;
        _maxCellStyles = maxCellStyles;
        _maxTextLength = maxText;
    }

    /**
     * @return the maximum number of usable rows in each spreadsheet
     */
    public int getMaxRows() {
        return _maxRows;
    }

    /**
     * @return the last (maximum) valid row index, equals to <code> getMaxRows() - 1 </code>
     */
    public int getLastRowIndex() {
        return _maxRows - 1;
    }

    /**
     * @return the maximum number of usable columns in each spreadsheet
     */
    public int getMaxColumns() {
        return _maxColumns;
    }

    /**
     * @return the last (maximum) valid column index, equals to <code> getMaxColumns() - 1 </code>
     */
    public int getLastColumnIndex() {
        return _maxColumns - 1;
    }

    /**
     * @return the maximum number arguments that can be passed to a multi-arg function (e.g. COUNTIF)
     */
    public int getMaxFunctionArgs() {
        return _maxFunctionArgs;
    }

    /**
     * @return the maximum number of conditional format conditions on a cell
     */
    public int getMaxConditionalFormats() {
        return _maxCondFormats;
    }

    /**
     * @return the maximum number of cell styles per spreadsheet
     */
    public int getMaxCellStyles() {
        return _maxCellStyles;
    }

    /**
     *
     * @return the last valid column index in a ALPHA-26 representation
     *  (<code>IV</code> or <code>XFD</code>).
     */
    public String getLastColumnName() {
        return CellReference.convertNumToColString(getLastColumnIndex());
    }

    /**
     * @return the maximum length of a text cell
     */
    public int getMaxTextLength() {
        return _maxTextLength;
    }
}
