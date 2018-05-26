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

import java.util.regex.Pattern;

import org.apache.poi.ss.util.CellReference;

/**
 * XSSF Only!
 * High level abstraction of table in a workbook.
 */
public interface Table {
    /**
     * Regular expression matching a Structured Reference (Table syntax) for XSSF table expressions.
     * Public for unit tests
     * @see <a href="https://support.office.com/en-us/article/Using-structured-references-with-Excel-tables-F5ED2452-2337-4F71-BED3-C8AE6D2B276E">
     *         Excel Structured Reference Syntax
     *      </a>
     */
    Pattern isStructuredReference = Pattern.compile("[a-zA-Z_\\\\][a-zA-Z0-9._]*\\[.*\\]");
    
    /**
     *  Get the top-left column index relative to the sheet
     * @return table start column index on sheet
     */
    int getStartColIndex();
    /**
     *  Get the top-left row index on the sheet
     * @return table start row index on sheet
     */
    int getStartRowIndex();
    /**
     *  Get the bottom-right column index on the sheet
     * @return table end column index on sheet
     */
    int getEndColIndex();
    /**
     *  Get the bottom-right row index
     * @return table end row index on sheet
     */
    int getEndRowIndex();
    /**
     * Get the name of the table.
     * @return table name
     */
    String getName();
    
    /**
     * @return name of the table style, if there is one.  May be a built-in name or user-defined.
     * @since 3.17 beta 1
     */
    String getStyleName();
    
    /**
     * Returns the index of a given named column in the table (names are case insensitive in XSSF).
     * Note this list is lazily loaded and cached for performance. 
     * Changes to the underlying table structure are not reflected in later calls
     * unless <code>XSSFTable.updateHeaders()</code> is called to reset the cache.
     * @param columnHeader the column header name to get the table column index of
     * @return column index corresponding to <code>columnHeader</code>
     */
    int findColumnIndex(String columnHeader);
    /**
     * Returns the sheet name that the table belongs to.
     * @return sheet name
     */
    String getSheetName();

    /**
     * Note: This is misleading.  The OOXML spec indicates this is true if the totals row
     * has <b><i>ever</i></b> been shown, not whether or not it is currently displayed.
     * Use {@link #getTotalsRowCount()} > 0 to decide whether or not the totals row is visible.
     * @return true if a totals row has ever been shown for this table
     * @since 3.15 beta 2
     * @see #getTotalsRowCount()
     */
    boolean isHasTotalsRow();
    
    /**
     * @return 0 for no totals rows, 1 for totals row shown.
     * Values > 1 are not currently used by Excel up through 2016, and the OOXML spec
     * doesn't define how they would be implemented.
     * @since 3.17 beta 1
     */
    int getTotalsRowCount();
    
    /**
     * @return 0 for no header rows, 1 for table headers shown.
     * Values > 1 might be used by Excel for pivot tables?
     * @since 3.17 beta 1
     */
    int getHeaderRowCount();
    
    /**
     * @return TableStyleInfo for this instance
     * @since 3.17 beta 1
     */
    TableStyleInfo getStyle();
    
    /**
     * checks if the given cell is part of the table.  Includes checking that they are on the same sheet.
     * @param cell
     * @return true if the table and cell are on the same sheet and the cell is within the table range.
     * @since 3.17 beta 1
     * @see #contains(CellReference) (prefered, faster execution and handles undefined cells)
     */
    default boolean contains(Cell cell) {
        if (cell == null) return false;
        return contains(new CellReference(cell.getSheet().getSheetName(), cell.getRowIndex(), cell.getColumnIndex(), true, true));
    }
    
    /**
     * checks if the given cell is part of the table.  Includes checking that they are on the same sheet.
     * @param cell reference to a possibly undefined cell location
     * @return true if the table and cell are on the same sheet and the cell is within the table range.
     * @since 3.17 beta 1
     */
    boolean contains(CellReference cell);
}
