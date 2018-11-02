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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.PaneInformation;

/**
 * High level representation of a Excel worksheet.
 *
 * <p>
 * Sheets are the central structures within a workbook, and are where a user does most of his spreadsheet work.
 * The most common type of sheet is the worksheet, which is represented as a grid of cells. Worksheet cells can
 * contain text, numbers, dates, and formulas. Cells can also be formatted.
 * </p>
 */
public interface Sheet extends Iterable<Row> {

    /* Constants for margins */
    public static final short LeftMargin = 0;

    public static final short RightMargin = 1;

    public static final short TopMargin = 2;

    public static final short BottomMargin = 3;

    public static final short HeaderMargin = 4;

    public static final short FooterMargin = 5;

    public static final byte PANE_LOWER_RIGHT = (byte) 0;

    public static final byte PANE_UPPER_RIGHT = (byte) 1;

    public static final byte PANE_LOWER_LEFT = (byte) 2;

    public static final byte PANE_UPPER_LEFT = (byte) 3;

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return high level Row object representing a row in the sheet
     * @see #removeRow(Row)
     */
    Row createRow(int rownum);

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row   representing a row to remove.
     */
    void removeRow(Row row);

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rownum  row to get (0-based)
     * @return Row representing the rownumber or null if its not defined on the sheet
     */
    Row getRow(int rownum);

    /**
     * Returns the number of physically defined rows (NOT the number of rows in the sheet)
     *
     * @return the number of physically defined rows in this sheet
     */
    int getPhysicalNumberOfRows();

    /**
     * Gets the first row on the sheet.
     *
     * Note: rows which had content before and were set to empty later might
     * still be counted as rows by Excel and Apache POI, so the result of this
     * method will include such rows and thus the returned value might be lower
     * than expected!
     *
     * @return the number of the first logical row on the sheet (0-based)
     */
    int getFirstRowNum();

    /**
     * Gets the last row on the sheet
     *
     * Note: rows which had content before and were set to empty later might
     * still be counted as rows by Excel and Apache POI, so the result of this
     * method will include such rows and thus the returned value might be higher
     * than expected!
     *
     * @return last row contained on this sheet (0-based)
     */
    int getLastRowNum();

    /**
     * Get the visibility state for a given column
     *
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visibility state of the column
     */
    void setColumnHidden(int columnIndex, boolean hidden);

    /**
     * Get the hidden state for a given column
     *
     * @param columnIndex - the column to set (0-based)
     * @return hidden - <code>false</code> if the column is visible
     */
    boolean isColumnHidden(int columnIndex);

    /**
     * Sets whether the worksheet is displayed from right to left instead of from left to right.
     *
     * @param value true for right to left, false otherwise.
     */
    public void setRightToLeft(boolean value);

    /**
     * Whether the text is displayed in right-to-left mode in the window
     *
     * @return whether the text is displayed in right-to-left mode in the window
     */
    public boolean isRightToLeft();

    /**
     * Set the width (in units of 1/256th of a character width)<p>
     *
     * The maximum column width for an individual cell is 255 characters.
     * This value represents the number of characters that can be displayed
     * in a cell that is formatted with the standard font (first font in the workbook).<p>
     *
     * Character width is defined as the maximum digit width
     * of the numbers <code>0, 1, 2, ... 9</code> as rendered
     * using the default font (first font in the workbook).<p>
     * 
     * Unless you are using a very special font, the default character is '0' (zero),
     * this is true for Arial (default font font in HSSF) and Calibri (default font in XSSF)<p>
     *
     * Please note, that the width set by this method includes 4 pixels of margin padding (two on each side),
     * plus 1 pixel padding for the gridlines (Section 3.3.1.12 of the OOXML spec).
     * This results is a slightly less value of visible characters than passed to this method (approx. 1/2 of a character).<p>
     * 
     * To compute the actual number of visible characters,
     * Excel uses the following formula (Section 3.3.1.12 of the OOXML spec):<p>
     * 
     * <code>
     *     width = Truncate([{Number of Visible Characters} *
     *      {Maximum Digit Width} + {5 pixel padding}]/{Maximum Digit Width}*256)/256
     * </code>
     * 
     * Using the Calibri font as an example, the maximum digit width of 11 point font size is 7 pixels (at 96 dpi).
     * If you set a column width to be eight characters wide, e.g. <code>setColumnWidth(columnIndex, 8*256)</code>,
     * then the actual value of visible characters (the value shown in Excel) is derived from the following equation:
     *  <code>
     *      Truncate([numChars*7+5]/7*256)/256 = 8;
     *  </code>
     *
     * which gives <code>7.29</code>.
     *
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     * @throws IllegalArgumentException if width &gt; 255*256 (the maximum column width in Excel is 255 characters)
     */
    void setColumnWidth(int columnIndex, int width);

    /**
     * get the width (in units of 1/256th of a character width )
     *
     * <p>
     * Character width is defined as the maximum digit width
     * of the numbers <code>0, 1, 2, ... 9</code> as rendered
     * using the default font (first font in the workbook)
     * </p>
     *
     * @param columnIndex - the column to get (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    int getColumnWidth(int columnIndex);

    /**
     * get the width in pixel
     * 
     * <p>
     * Please note, that this method works correctly only for workbooks
     * with the default font size (Arial 10pt for .xls and Calibri 11pt for .xlsx).
     * If the default font is changed the column width can be stretched
     * </p>
     *
     * @param columnIndex - the column to set (0-based)
     * @return width in pixels
     */
    float getColumnWidthInPixels(int columnIndex);
    
    
    /**
     * Set the default column width for the sheet (if the columns do not define their own width)
     * in characters
     *
     * @param width default column width measured in characters
     */
    void setDefaultColumnWidth(int width);

    /**
     * Get the default column width for the sheet (if the columns do not define their own width)
     * in characters
     *
     * @return default column width measured in characters
     */
    int getDefaultColumnWidth();

    /**
     * Get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @return  default row height measured in twips (1/20 of  a point)
     */
    short getDefaultRowHeight();
    
    /**
     * Get the default row height for the sheet (if the rows do not define their own height) in
     * points.
     *
     * @return  default row height in points
     */
    float getDefaultRowHeightInPoints();

    /**
     * Set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @param  height default row height measured in twips (1/20 of  a point)
     */
    void setDefaultRowHeight(short height);

    /**
     * Set the default row height for the sheet (if the rows do not define their own height) in
     * points
     * @param height default row height
     */
    void setDefaultRowHeightInPoints(float height);

    /**
     * Returns the CellStyle that applies to the given
     *  (0 based) column, or null if no style has been
     *  set for that column
     */
    public CellStyle getColumnStyle(int column);

    /*
     * Sets the CellStyle that applies to the given
     *  (0 based) column.
     */
//    public CellStyle setColumnStyle(int column, CellStyle style);

    /**
     * Adds a merged region of cells (hence those cells form one)
     *
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */
    int addMergedRegion(CellRangeAddress region);

    /**
     * Adds a merged region of cells (hence those cells form one).
     * Skips validation. It is possible to create overlapping merged regions
     * or create a merged region that intersects a multi-cell array formula
     * with this formula, which may result in a corrupt workbook.
     *
     * To check for merged regions overlapping array formulas or other merged regions
     * after addMergedRegionUnsafe has been called, call {@link #validateMergedRegions()}, which runs in O(n^2) time.
     *
     * @param region to merge
     * @return index of this region
     * @throws IllegalArgumentException if region contains fewer than 2 cells
     */
    int addMergedRegionUnsafe(CellRangeAddress region);

    /**
     * Verify that merged regions do not intersect multi-cell array formulas and
     * no merged regions intersect another merged region in this sheet.
     *
     * @throws IllegalStateException if region intersects with a multi-cell array formula
     * @throws IllegalStateException if at least one region intersects with another merged region in this sheet
     */
    void validateMergedRegions();

    /**
     * Determines whether the output is vertically centered on the page.
     *
     * @param value true to vertically center, false otherwise.
     */
    void setVerticallyCenter(boolean value);

    /**
     * Determines whether the output is horizontally centered on the page.
     *
     * @param value true to horizontally center, false otherwise.
     */
    void setHorizontallyCenter(boolean value);

    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */

    boolean getHorizontallyCenter();

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     */
    boolean getVerticallyCenter();

    /**
     * Removes a merged region of cells (hence letting them free)
     *
     * @param index of the region to unmerge
     */
    void removeMergedRegion(int index);
    
    /**
     * Removes a number of merged regions of cells (hence letting them free)
     *
     * @param indices A set of the regions to unmerge
     */
    void removeMergedRegions(Collection<Integer> indices);

    /**
     * Returns the number of merged regions
     *
     * @return number of merged regions
     */
    int getNumMergedRegions();

    /**
     * Returns the merged region at the specified index
     *
     * @return the merged region at the specified index
     */
    public CellRangeAddress getMergedRegion(int index);

    /**
     * Returns the list of merged regions.
     *
     * @return the list of merged regions
     */
    public List<CellRangeAddress> getMergedRegions();

    /**
     *  Returns an iterator of the physical rows
     *
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     */
    Iterator<Row> rowIterator();

    /**
     * Control if Excel should be asked to recalculate all formulas on this sheet
     * when the workbook is opened.
     *
     *  <p>
     *  Calculating the formula values with {@link FormulaEvaluator} is the
     *  recommended solution, but this may be used for certain cases where
     *  evaluation in POI is not possible.
     *  </p>
     *
     *  To force recalculation of formulas in the entire workbook
     *  use {@link Workbook#setForceFormulaRecalculation(boolean)} instead.
     *
     * @param value true if the application will perform a full recalculation of
     * this worksheet values when the workbook is opened
     *
     * @see Workbook#setForceFormulaRecalculation(boolean)
     */
    void setForceFormulaRecalculation(boolean value);

    /**
     * Whether Excel will be asked to recalculate all formulas in this sheet when the
     *  workbook is opened.  
     */
    boolean getForceFormulaRecalculation();
    
    /**
     * Flag indicating whether the sheet displays Automatic Page Breaks.
     *
     * @param value <code>true</code> if the sheet displays Automatic Page Breaks.
     */
    void setAutobreaks(boolean value);

    /**
     * Set whether to display the guts or not
     *
     * @param value - guts or no guts
     */
    void setDisplayGuts(boolean value);

    /**
     * Set whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     *
     * @param value whether to display or hide all zero values on the worksheet
     */
    void setDisplayZeros(boolean value);


    /**
     * Gets the flag indicating whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     *
     * @return whether all zero values on the worksheet are displayed
     */
    boolean isDisplayZeros();

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @param value <code>true</code> if the Fit to Page print option is enabled.
     */
    void setFitToPage(boolean value);

    /**
     * Flag indicating whether summary rows appear below detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary row is inserted below the detailed data being summarized and a
     * new outline level is established on that row.
     * </p>
     * <p>
     * When false a summary row is inserted above the detailed data being summarized and a new outline level
     * is established on that row.
     * </p>
     * @param value <code>true</code> if row summaries appear below detail in the outline
     */
    void setRowSumsBelow(boolean value);

    /**
     * Flag indicating whether summary columns appear to the right of detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary column is inserted to the right of the detailed data being summarized
     * and a new outline level is established on that column.
     * </p>
     * <p>
     * When false a summary column is inserted to the left of the detailed data being
     * summarized and a new outline level is established on that column.
     * </p>
     * @param value <code>true</code> if col summaries appear right of the detail in the outline
     */
    void setRowSumsRight(boolean value);

    /**
     * Flag indicating whether the sheet displays Automatic Page Breaks.
     *
     * @return <code>true</code> if the sheet displays Automatic Page Breaks.
     */
    boolean getAutobreaks();

    /**
     * Get whether to display the guts or not,
     * default value is true
     *
     * @return boolean - guts or no guts
     */
    boolean getDisplayGuts();

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @return <code>true</code> if the Fit to Page print option is enabled.
     */
    boolean getFitToPage();

    /**
     * Flag indicating whether summary rows appear below detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary row is inserted below the detailed data being summarized and a
     * new outline level is established on that row.
     * </p>
     * <p>
     * When false a summary row is inserted above the detailed data being summarized and a new outline level
     * is established on that row.
     * </p>
     * @return <code>true</code> if row summaries appear below detail in the outline
     */
    boolean getRowSumsBelow();

    /**
     * Flag indicating whether summary columns appear to the right of detail in an outline, when applying an outline.
     *
     * <p>
     * When true a summary column is inserted to the right of the detailed data being summarized
     * and a new outline level is established on that column.
     * </p>
     * <p>
     * When false a summary column is inserted to the left of the detailed data being
     * summarized and a new outline level is established on that column.
     * </p>
     * @return <code>true</code> if col summaries appear right of the detail in the outline
     */
    boolean getRowSumsRight();

    /**
     * Gets the flag indicating whether this sheet displays the lines
     * between rows and columns to make editing and reading easier.
     *
     * @return <code>true</code> if this sheet prints gridlines.
     * @see #isDisplayGridlines() to check if gridlines are displayed on screen
     */
    boolean isPrintGridlines();

    /**
     * Sets the flag indicating whether this sheet should print the lines
     * between rows and columns to make editing and reading easier.
     *
     * @param show <code>true</code> if this sheet should print gridlines.
     * @see #setDisplayGridlines(boolean) to display gridlines on screen
     */
    void setPrintGridlines(boolean show);
    
    /**
     * Gets the flag indicating whether this sheet prints the
     * row and column headings when printing.
     *
     * @return <code>true</code> if this sheet prints row and column headings.
     */
    boolean isPrintRowAndColumnHeadings();

    /**
     * Sets the flag indicating whether this sheet should print
     * row and columns headings when printing.
     *
     * @param show <code>true</code> if this sheet should print row and column headings.
     */
    void setPrintRowAndColumnHeadings(boolean show);

    /**
     * Gets the print setup object.
     *
     * @return The user model for the print setup object.
     */
    PrintSetup getPrintSetup();

    /**
     * Gets the user model for the default document header.<p>
     * 
     * Note that XSSF offers more kinds of document headers than HSSF does
     * 
     * @return the document header. Never <code>null</code>
     */
    Header getHeader();

    /**
     * Gets the user model for the default document footer.<p>
     * 
     * Note that XSSF offers more kinds of document footers than HSSF does.
     *
     * @return the document footer. Never <code>null</code>
     */
    Footer getFooter();

    /**
     * Sets a flag indicating whether this sheet is selected.<p>
     * 
     * Note: multiple sheets can be selected, but only one sheet can be active at one time.
     *
     * @param value <code>true</code> if this sheet is selected
     * @see Workbook#setActiveSheet(int)
     */
    void setSelected(boolean value);

    /**
     * Gets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @return the size of the margin
     */
    double getMargin(short margin);

    /**
     * Sets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @param size the size of the margin
     */
    void setMargin(short margin, double size);

    /**
     * Answer whether protection is enabled or disabled
     *
     * @return true =&gt; protection enabled; false =&gt; protection disabled
     */
    boolean getProtect();
    
    /**
     * Sets the protection enabled as well as the password
     * @param password to set for protection. Pass <code>null</code> to remove protection
     */
    public void protectSheet(String password);
    
    /**
     * Answer whether scenario protection is enabled or disabled
     *
     * @return true =&gt; protection enabled; false =&gt; protection disabled
     */
    boolean getScenarioProtect();
    
    /**
     * Window zoom magnification for current view representing percent values.
     * Valid values range from 10 to 400. Horizontal &amp; Vertical scale together.
     *
     * For example:
     * <pre>
     * 10 - 10%
     * 20 - 20%
     * ...
     * 100 - 100%
     * ...
     * 400 - 400%
     * </pre>
     *
     * @param scale window zoom magnification
     * @throws IllegalArgumentException if scale is invalid
     */
    public void setZoom(int scale);

    /**
     * The top row in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return short indicating the rownum (0 based) of the top row
     */
    short getTopRow();

    /**
     * The left col in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return short indicating the rownum (0 based) of the top row
     */
    short getLeftCol();

    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     *
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    void showInPane(int toprow, int leftcol);

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around.
     *
     * Calls shiftRows(startRow, endRow, n, false, false);
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted).
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     */
    void shiftRows(int startRow, int endRow, int n);

    /**
     * Shifts rows between startRow and endRow n number of rows.
     * If you use a negative number, it will shift rows up.
     * Code ensures that rows don't wrap around
     *
     * <p>
     * Additionally shifts merged regions that are completely defined in these
     * rows (ie. merged 2 cells on a row to be shifted). All merged regions that are
     * completely overlaid by shifting will be deleted.
     * <p>
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
    void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight);

    /**
     * Shifts columns between startColumn and endColumn, n number of columns.
     * If you use a negative number, it will shift columns left.
     * Code ensures that columns don't wrap around
     *
     * @param startColumn the column to start shifting
     * @param endColumn the column to end shifting
     * @param n the number of columns to shift
     */
    void shiftColumns(int startColumn, int endColumn, int n);

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * <p>
     *     If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * </p>
     * @param colSplit      Horizontal position of split.
     * @param rowSplit      Vertical position of split.
     * @param leftmostColumn   Left column visible in right pane.
     * @param topRow        Top row visible in bottom pane
     */
    void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow);

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * <p>
     *     If both colSplit and rowSplit are zero then the existing freeze pane is removed
     * </p>
     * @param colSplit      Horizontal position of split.
     * @param rowSplit      Vertical position of split.
     */
    void createFreezePane(int colSplit, int rowSplit);

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizontal position of split (in 1/20th of a point).
     * @param ySplitPos      Vertical position of split (in 1/20th of a point).
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
     * @param activePane    Active pane.  One of: PANE_LOWER_RIGHT,
     *                      PANE_UPPER_RIGHT, PANE_LOWER_LEFT, PANE_UPPER_LEFT
     * @see #PANE_LOWER_LEFT
     * @see #PANE_LOWER_RIGHT
     * @see #PANE_UPPER_LEFT
     * @see #PANE_UPPER_RIGHT
     */
    void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane);

    /**
     * Returns the information regarding the currently configured pane (split or freeze)
     *
     * @return null if no pane configured, or the pane information.
     */
    PaneInformation getPaneInformation();

    /**
     * Sets whether the gridlines are shown in a viewer
     *
     * @param show whether to show gridlines or not
     */
    void setDisplayGridlines(boolean show);

    /**
     * Returns if gridlines are displayed
     *
     * @return whether gridlines are displayed
     */
    boolean isDisplayGridlines();

    /**
     * Sets whether the formulas are shown in a viewer
     *
     * @param show whether to show formulas or not
     */
    void setDisplayFormulas(boolean show);

    /**
     * Returns if formulas are displayed
     *
     * @return whether formulas are displayed
     */
    boolean isDisplayFormulas();

    /**
     * Sets whether the RowColHeadings are shown in a viewer
     *
     * @param show whether to show RowColHeadings or not
     */
    void setDisplayRowColHeadings(boolean show);

    /**
     * Returns if RowColHeadings are displayed.
     * @return whether RowColHeadings are displayed
     */
    boolean isDisplayRowColHeadings();

    /**
     * Sets a page break at the indicated row
     * Breaks occur above the specified row and left of the specified column inclusive.
     *
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Similar, <code>sheet.setRowBreak(2);</code>
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param row the row to break, inclusive
     */
    void setRowBreak(int row);

    /**
     * Determines if there is a page break at the indicated row
     * @param row FIXME: Document this!
     * @return FIXME: Document this!
     */
    boolean isRowBroken(int row);

    /**
     * Removes the page break at the indicated row
     * @param row The 0-based index of the row.
     */
    void removeRowBreak(int row);

    /**
     * Retrieves all the horizontal page breaks
     * @return all the horizontal page breaks, or null if there are no row page breaks
     */
    int[] getRowBreaks();

    /**
     * Retrieves all the vertical page breaks
     * @return all the vertical page breaks, or null if there are no column page breaks
     */
    int[] getColumnBreaks();

    /**
     * Sets a page break at the indicated column.
     * Breaks occur above the specified row and left of the specified column inclusive.
     *
     * For example, <code>sheet.setColumnBreak(2);</code> breaks the sheet into two parts
     * with columns A,B,C in the first and D,E,... in the second. Similar, <code>sheet.setRowBreak(2);</code>
     * breaks the sheet into two parts with first three rows (rownum=1...3) in the first part
     * and rows starting with rownum=4 in the second.
     *
     * @param column the column to break, inclusive
     */
    void setColumnBreak(int column);

    /**
     * Determines if there is a page break at the indicated column
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
    boolean isColumnBroken(int column);

    /**
     * Removes a page break at the indicated column
     * @param column The 0-based index of the column.
     */
    void removeColumnBreak(int column);

    /**
     * Expands or collapses a column group.
     *
     * @param columnNumber      One of the columns in the group.
     * @param collapsed         true = collapse group, false = expand group.
     */
    void setColumnGroupCollapsed(int columnNumber, boolean collapsed);

    /**
     * Create an outline for the provided column range.
     *
     * @param fromColumn        beginning of the column range.
     * @param toColumn          end of the column range.
     */
    void groupColumn(int fromColumn, int toColumn);

    /**
     * Ungroup a range of columns that were previously grouped
     *
     * @param fromColumn   start column (0-based)
     * @param toColumn     end column (0-based)
     */
    void ungroupColumn(int fromColumn, int toColumn);

    /**
     * Tie a range of rows together so that they can be collapsed or expanded
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    void groupRow(int fromRow, int toRow);

    /**
     * Ungroup a range of rows that were previously grouped
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    void ungroupRow(int fromRow, int toRow);

    /**
     * Set view state of a grouped range of rows
     *
     * @param row   start row of a grouped range of rows (0-based)
     * @param collapse whether to expand/collapse the detail rows
     */
    void setRowGroupCollapsed(int row, boolean collapse);

    /**
     * Sets the default column style for a given column.  POI will only apply this style to new cells added to the sheet.
     *
     * @param column the column index
     * @param style the style to set
     */
    void setDefaultColumnStyle(int column, CellStyle style);

    /**
     * Adjusts the column width to fit the contents.
     *
     * <p>
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     * </p>
     * You can specify whether the content of merged cells should be considered or ignored.
     *  Default is to ignore merged cells.
     *
     * @param column the column index
     */
    void autoSizeColumn(int column);

    /**
     * Adjusts the column width to fit the contents.
     * <p>
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     * </p>
     * You can specify whether the content of merged cells should be considered or ignored.
     *  Default is to ignore merged cells.
     *
     * @param column the column index
     * @param useMergedCells whether to use the contents of merged cells when calculating the width of the column
     */
    void autoSizeColumn(int column, boolean useMergedCells);
    
    /**
     * Returns cell comment for the specified location
     *
     * @return cell comment or <code>null</code> if not found
     */
    Comment getCellComment(CellAddress ref);

    /**
     * Returns all cell comments on this sheet.
     * @return A map of each Comment in the sheet, keyed on the cell address where
     * the comment is located.
     */
    Map<CellAddress, ? extends Comment> getCellComments();

    /**
     * Return the sheet's existing drawing, or null if there isn't yet one.
     * 
     * Use {@link #createDrawingPatriarch()} to get or create
     *
     * @return a SpreadsheetML drawing
     */
    Drawing<?> getDrawingPatriarch();
    
    /**
     * Creates the top-level drawing patriarch. 
     * <p>This may then be used to add graphics or charts.</p>
     * <p>Note that this will normally have the effect of removing 
     *  any existing drawings on this sheet.</p>
     *
     * @return  The new drawing patriarch.
     */
    Drawing<?> createDrawingPatriarch();


    /**
     * Return the parent workbook
     *
     * @return the parent workbook
     */
    Workbook getWorkbook();

    /**
     * Returns the name of this sheet
     *
     * @return the name of this sheet
     */
    String getSheetName();

    /**
     * Note - this is not the same as whether the sheet is focused (isActive)
     * @return <code>true</code> if this sheet is currently selected
     */
    boolean isSelected();


    /**
     * Sets array formula to specified region for result.
     * <p>
     * Note if there are shared formulas this will invalidate any 
     * {@link FormulaEvaluator} instances based on this workbook
     *</p>
     * @param formula text representation of the formula
     * @param range Region of array formula for result.
     * @return the {@link CellRange} of cells affected by this change
     */
    CellRange<? extends Cell> setArrayFormula(String formula, CellRangeAddress range);

    /**
     * Remove a Array Formula from this sheet.  All cells contained in the Array Formula range are removed as well
     *
     * @param cell   any cell within Array Formula range
     * @return the {@link CellRange} of cells affected by this change
     */
    CellRange<? extends Cell> removeArrayFormula(Cell cell);
    
    public DataValidationHelper getDataValidationHelper();

    /**
     * Returns the list of DataValidation in the sheet.
     * @return list of DataValidation in the sheet
     */
    public List<? extends DataValidation> getDataValidations();

    /**
     * Creates a data validation object
     * @param dataValidation The Data validation object settings
     */
    public void addValidationData(DataValidation dataValidation);

    /**
     * Enable filtering for a range of cells
     * 
     * @param range the range of cells to filter
     */
    AutoFilter setAutoFilter(CellRangeAddress range);

    /**
     * The 'Conditional Formatting' facet for this <tt>Sheet</tt>
     *
     * @return  conditional formatting rule for this sheet
     */
    SheetConditionalFormatting getSheetConditionalFormatting();


    /**
     * Gets the repeating rows used when printing the sheet, as found in 
     * File-&gt;PageSetup-&gt;Sheet.<p>
     * 
     * Repeating rows cover a range of contiguous rows, e.g.:
     * <pre>
     * Sheet1!$1:$1
     * Sheet2!$5:$8
     * </pre>
     * The {@link CellRangeAddress} returned contains a column part which spans 
     * all columns, and a row part which specifies the contiguous range of 
     * repeating rows.<p>
     * 
     * If the Sheet does not have any repeating rows defined, null is returned.
     * 
     * @return an {@link CellRangeAddress} containing the repeating rows for the 
     *         Sheet, or null.
     */
    CellRangeAddress getRepeatingRows();


    /**
     * Gets the repeating columns used when printing the sheet, as found in 
     * File-&gt;PageSetup-&gt;Sheet.<p>
     * 
     * Repeating columns cover a range of contiguous columns, e.g.:
     * <pre>
     * Sheet1!$A:$A
     * Sheet2!$C:$F
     * </pre>
     * The {@link CellRangeAddress} returned contains a row part which spans all 
     * rows, and a column part which specifies the contiguous range of 
     * repeating columns.<p>
     * 
     * If the Sheet does not have any repeating columns defined, null is 
     * returned.
     * 
     * @return an {@link CellRangeAddress} containing the repeating columns for 
     *         the Sheet, or null.
     */
    CellRangeAddress getRepeatingColumns();


    /**
     * Sets the repeating rows used when printing the sheet, as found in 
     * File-&gt;PageSetup-&gt;Sheet.<p>
     * 
     * Repeating rows cover a range of contiguous rows, e.g.:
     * <pre>
     * Sheet1!$1:$1
     * Sheet2!$5:$8</pre>
     * The parameter {@link CellRangeAddress} should specify a column part 
     * which spans all columns, and a row part which specifies the contiguous 
     * range of repeating rows, e.g.:
     * <pre>
     * sheet.setRepeatingRows(CellRangeAddress.valueOf("2:3"));</pre>
     * A null parameter value indicates that repeating rows should be removed 
     * from the Sheet:
     * <pre>
     * sheet.setRepeatingRows(null);</pre>
     * 
     * @param rowRangeRef a {@link CellRangeAddress} containing the repeating 
     *        rows for the Sheet, or null.
     */
    void setRepeatingRows(CellRangeAddress rowRangeRef);


    /**
     * Sets the repeating columns used when printing the sheet, as found in 
     * File-&gt;PageSetup-&gt;Sheet.<p>
     * 
     * Repeating columns cover a range of contiguous columns, e.g.:
     * <pre>
     * Sheet1!$A:$A
     * Sheet2!$C:$F</pre>
     * The parameter {@link CellRangeAddress} should specify a row part 
     * which spans all rows, and a column part which specifies the contiguous 
     * range of repeating columns, e.g.:
     * <pre>
     * sheet.setRepeatingColumns(CellRangeAddress.valueOf("B:C"));</pre>
     * A null parameter value indicates that repeating columns should be removed 
     * from the Sheet:
     * <pre>
     * sheet.setRepeatingColumns(null);</pre>
     * 
     * @param columnRangeRef a {@link CellRangeAddress} containing the repeating 
     *        columns for the Sheet, or null.
     */
    void setRepeatingColumns(CellRangeAddress columnRangeRef);
    
    /**
     * Returns the column outline level. Increased as you
     *  put it into more groups (outlines), reduced as
     *  you take it out of them.
     */
    int getColumnOutlineLevel(int columnIndex);
    
    /**
     * Get a Hyperlink in this sheet anchored at row, column
     *
     * @param row The 0-based index of the row to look at.
     * @param column The 0-based index of the column to look at.
     * @return hyperlink if there is a hyperlink anchored at row, column; otherwise returns null
     */
    public Hyperlink getHyperlink(int row, int column);
    
    /**
     * Get a Hyperlink in this sheet located in a cell specified by {code addr}
     *
     * @param addr The address of the cell containing the hyperlink
     * @return hyperlink if there is a hyperlink anchored at {@code addr}; otherwise returns {@code null}
     * @since POI 3.15 beta 3
     */
    public Hyperlink getHyperlink(CellAddress addr);
    
    /**
     * Get a list of Hyperlinks in this sheet
     *
     * @return Hyperlinks for the sheet
     */
    public List<? extends Hyperlink> getHyperlinkList();

    /**
     * Return location of the active cell, e.g. <code>A1</code>.
     *
     * @return the location of the active cell.
     * @since 3.14beta1
     */
    public CellAddress getActiveCell();

    /**
      * Sets location of the active cell
      *
      * @param address the location of the active cell, e.g. <code>A1</code>.
      * @since 3.14beta1
      */
    public void setActiveCell(CellAddress address);
}
