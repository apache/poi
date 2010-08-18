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

import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.util.CellRangeAddress;

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
     * Gets the first row on the sheet
     *
     * @return the number of the first logical row on the sheet (0-based)
     */
    int getFirstRowNum();

    /**
     * Gets the last row on the sheet
     *
     * @return last row contained n this sheet (0-based)
     */
    int getLastRowNum();

    /**
     * Get the visibility state for a given column
     *
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visiblity state of the column
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
     * Set the width (in units of 1/256th of a character width)
     * <p>
     * The maximum column width for an individual cell is 255 characters.
     * This value represents the number of characters that can be displayed
     * in a cell that is formatted with the standard font.
     * </p>
     *
     * @param columnIndex - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     */
    void setColumnWidth(int columnIndex, int width);

    /**
     * get the width (in units of 1/256th of a character width )
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    int getColumnWidth(int columnIndex);

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

    /**
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
     *  Returns an iterator of the physical rows
     *
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     */
    Iterator<Row> rowIterator();

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
     * @return <code>true</code> if this sheet displays gridlines.
     * @see #isPrintGridlines() to check if printing of gridlines is turned on or off
     */
    boolean isPrintGridlines();

    /**
     * Sets the flag indicating whether this sheet should display the lines
     * between rows and columns to make editing and reading easier.
     * To turn printing of gridlines use {@link #setPrintGridlines(boolean)}
     *
     *
     * @param show <code>true</code> if this sheet should display gridlines.
     * @see #setPrintGridlines(boolean)
     */
    void setPrintGridlines(boolean show);

    /**
     * Gets the print setup object.
     *
     * @return The user model for the print setup object.
     */
    PrintSetup getPrintSetup();

    /**
     * Gets the user model for the default document header.
     * <p/>
     * Note that XSSF offers more kinds of document headers than HSSF does
     * </p>
     * @return the document header. Never <code>null</code>
     */
    Header getHeader();

    /**
     * Gets the user model for the default document footer.
     * <p/>
     * Note that XSSF offers more kinds of document footers than HSSF does.
     *
     * @return the document footer. Never <code>null</code>
     */
    Footer getFooter();

    /**
     * Sets a flag indicating whether this sheet is selected.
     *<p>
     * Note: multiple sheets can be selected, but only one sheet can be active at one time.
     *</p>
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
     * @return true => protection enabled; false => protection disabled
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
     * @return true => protection enabled; false => protection disabled
     */
    boolean getScenarioProtect();

    /**
     * Sets the zoom magnication for the sheet.  The zoom is expressed as a
     * fraction.  For example to express a zoom of 75% use 3 for the numerator
     * and 4 for the denominator.
     *
     * @param numerator     The numerator for the zoom magnification.
     * @param denominator   The denominator for the zoom magnification.
     */
    void setZoom(int numerator, int denominator);

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
    void showInPane(short toprow, short leftcol);

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
     * rows (ie. merged 2 cells on a row to be shifted).
     * <p>
     * @param startRow the row to start shifting
     * @param endRow the row to end shifting
     * @param n the number of rows to shift
     * @param copyRowHeight whether to copy the row height during the shift
     * @param resetOriginalRowHeight whether to set the original row's height to the default
     */
    void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight);

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param leftmostColumn   Left column visible in right pane.
     * @param topRow        Top row visible in bottom pane
     */
    void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow);

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     */
    void createFreezePane(int colSplit, int rowSplit);

    /**
     * Creates a split pane. Any existing freezepane or split pane is overwritten.
     * @param xSplitPos      Horizonatal position of split (in 1/20th of a point).
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
     * @param row FIXME: Document this!
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
     * @param row
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
     * Sets a page break at the indicated column
     * @param column
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
     * @param column
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
     * Ungroup a range of columns that were previously groupped
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
     * Ungroup a range of rows that were previously groupped
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    void ungroupRow(int fromRow, int toRow);

    /**
     * Set view state of a groupped range of rows
     *
     * @param row   start row of a groupped range of rows (0-based)
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
     * Returns cell comment for the specified row and column
     *
     * @return cell comment or <code>null</code> if not found
     */
    Comment getCellComment(int row, int column);

    /**
     * Creates the top-level drawing patriarch.
     *
     * @return  The new drawing patriarch.
     */
    Drawing createDrawingPatriarch();


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
     *
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
    
}
