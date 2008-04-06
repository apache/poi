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
import org.apache.poi.ss.util.Region;

public interface Sheet extends Iterable<Row> {

    /* Constants for margins */
    public static final short LeftMargin = Sheet.LeftMargin;

    public static final short RightMargin = Sheet.RightMargin;

    public static final short TopMargin = Sheet.TopMargin;

    public static final short BottomMargin = Sheet.BottomMargin;

    public static final byte PANE_LOWER_RIGHT = (byte) 0;

    public static final byte PANE_UPPER_RIGHT = (byte) 1;

    public static final byte PANE_LOWER_LEFT = (byte) 2;

    public static final byte PANE_UPPER_LEFT = (byte) 3;

    /**
     * Used for compile-time optimization.  This is the initial size for the collection of
     * rows.  It is currently set to 20.  If you generate larger sheets you may benefit
     * by setting this to a higher number and recompiling a custom edition of HSSFSheet.
     */

    public final static int INITIAL_CAPACITY = 20;

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return High level HSSFRow object representing a row in the sheet
     * @see org.apache.poi.hssf.usermodel.HSSFRow
     * @see #removeRow(HSSFRow)
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
     * @param rownum  row to get
     * @return HSSFRow representing the rownumber or null if its not defined on the sheet
     */

    Row getRow(int rownum);

    /**
     * Returns the number of phsyically defined rows (NOT the number of rows in the sheet)
     */

    int getPhysicalNumberOfRows();

    /**
     * gets the first row on the sheet
     * @return the number of the first logical row on the sheet
     */

    int getFirstRowNum();

    /**
     * gets the last row on the sheet
     * @return last row contained n this sheet.
     */

    int getLastRowNum();

    /**
     * Get the visibility state for a given column.
     * @param column - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */

    void setColumnHidden(short column, boolean hidden);

    /**
     * Get the hidden state for a given column.
     * @param column - the column to set (0-based)
     * @return hidden - the visiblity state of the column
     */

    boolean isColumnHidden(short column);

    /**
     * set the width (in units of 1/256th of a character width)
     * @param column - the column to set (0-based)
     * @param width - the width in units of 1/256th of a character width
     */

    void setColumnWidth(short column, short width);

    /**
     * get the width (in units of 1/256th of a character width )
     * @param column - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */

    short getColumnWidth(short column);

    /**
     * get the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @return default column width
     */

    short getDefaultColumnWidth();

    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @return  default row height
     */

    short getDefaultRowHeight();

    /**
     * get the default row height for the sheet (if the rows do not define their own height) in
     * points.
     * @return  default row height in points
     */

    float getDefaultRowHeightInPoints();

    /**
     * set the default column width for the sheet (if the columns do not define their own width) in
     * characters
     * @param width default column width
     */

    void setDefaultColumnWidth(short width);

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     * @param  height default row height
     */

    void setDefaultRowHeight(short height);

    /**
     * set the default row height for the sheet (if the rows do not define their own height) in
     * points
     * @param height default row height
     */

    void setDefaultRowHeightInPoints(float height);

    /**
     * get whether gridlines are printed.
     * @return true if printed
     */

    boolean isGridsPrinted();

    /**
     * set whether gridlines printed.
     * @param value  false if not printed.
     */

    void setGridsPrinted(boolean value);

    /**
     * adds a merged region of cells (hence those cells form one)
     * @param region (rowfrom/colfrom-rowto/colto) to merge
     * @return index of this region
     */

    int addMergedRegion(Region region);

    /**
     * determines whether the output is vertically centered on the page.
     * @param value true to vertically center, false otherwise.
     */

    void setVerticallyCenter(boolean value);

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     */

    boolean getVerticallyCenter(boolean value);

    /**
     * determines whether the output is horizontally centered on the page.
     * @param value true to horizontally center, false otherwise.
     */

    void setHorizontallyCenter(boolean value);

    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */

    boolean getHorizontallyCenter();

    /**
     * removes a merged region of cells (hence letting them free)
     * @param index of the region to unmerge
     */

    void removeMergedRegion(int index);

    /**
     * returns the number of merged regions
     * @return number of merged regions
     */

    int getNumMergedRegions();

    /**
     * gets the region at a particular index
     * @param index of the region to fetch
     * @return the merged region (simple eh?)
     */

    Region getMergedRegionAt(int index);

    /**
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     */
    Iterator<Row> rowIterator();
    
    /**
     * Alias for {@link #rowIterator()} to allow 
     *  foreach loops
     */
    Iterator<Row> iterator();

    /**
     * whether alternate expression evaluation is on
     * @param b  alternative expression evaluation or not
     */

    void setAlternativeExpression(boolean b);

    /**
     * whether alternative formula entry is on
     * @param b  alternative formulas or not
     */

    void setAlternativeFormula(boolean b);

    /**
     * show automatic page breaks or not
     * @param b  whether to show auto page breaks
     */

    void setAutobreaks(boolean b);

    /**
     * set whether sheet is a dialog sheet or not
     * @param b  isDialog or not
     */

    void setDialog(boolean b);

    /**
     * set whether to display the guts or not
     *
     * @param b  guts or no guts (or glory)
     */

    void setDisplayGuts(boolean b);

    /**
     * fit to page option is on
     * @param b  fit or not
     */

    void setFitToPage(boolean b);

    /**
     * set if row summaries appear below detail in the outline
     * @param b  below or not
     */

    void setRowSumsBelow(boolean b);

    /**
     * set if col summaries appear right of the detail in the outline
     * @param b  right or not
     */

    void setRowSumsRight(boolean b);

    /**
     * whether alternate expression evaluation is on
     * @return alternative expression evaluation or not
     */

    boolean getAlternateExpression();

    /**
     * whether alternative formula entry is on
     * @return alternative formulas or not
     */

    boolean getAlternateFormula();

    /**
     * show automatic page breaks or not
     * @return whether to show auto page breaks
     */

    boolean getAutobreaks();

    /**
     * get whether sheet is a dialog sheet or not
     * @return isDialog or not
     */

    boolean getDialog();

    /**
     * get whether to display the guts or not
     *
     * @return guts or no guts (or glory)
     */

    boolean getDisplayGuts();

    /**
     * fit to page option is on
     * @return fit or not
     */

    boolean getFitToPage();

    /**
     * get if row summaries appear below detail in the outline
     * @return below or not
     */

    boolean getRowSumsBelow();

    /**
     * get if col summaries appear right of the detail in the outline
     * @return right or not
     */

    boolean getRowSumsRight();

    /**
     * Returns whether gridlines are printed.
     * @return Gridlines are printed
     */
    boolean isPrintGridlines();

    /**
     * Turns on or off the printing of gridlines.
     * @param newPrintGridlines boolean to turn on or off the printing of
     * gridlines
     */
    void setPrintGridlines(boolean newPrintGridlines);

    /**
     * Gets the print setup object.
     * @return The user model for the print setup object.
     */
    PrintSetup getPrintSetup();

    /**
     * Gets the user model for the document header.
     * @return The Document header.
     */
    Header getHeader();

    /**
     * Gets the user model for the document footer.
     * @return The Document footer.
     */
    Footer getFooter();

    /**
     * Sets whether sheet is selected.
     * @param sel Whether to select the sheet or deselect the sheet.
     */
    void setSelected(boolean sel);

    /**
     * Gets the size of the margin in inches.
     * @param margin which margin to get
     * @return the size of the margin
     */
    double getMargin(short margin);

    /**
     * Sets the size of the margin in inches.
     * @param margin which margin to get
     * @param size the size of the margin
     */
    void setMargin(short margin, double size);

    /**
     * Answer whether protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    boolean getProtect();

    /**
     * @return hashed password
     */
    short getPassword();

    /**
     * Answer whether object protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    boolean getObjectProtect();

    /**
     * Answer whether scenario protection is enabled or disabled
     * @return true => protection enabled; false => protection disabled
     */
    boolean getScenarioProtect();

    /**
     * Sets the protection on enabled or disabled
     * @param protect true => protection enabled; false => protection disabled
     * @deprecated use protectSheet(String, boolean, boolean)
     */
    void setProtect(boolean protect);

    /**
     * Sets the protection enabled as well as the password
     * @param password to set for protection
     */
    void protectSheet(String password);

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
     * @return short indicating the rownum (0 based) of the top row
     */
    short getTopRow();

    /**
     * The left col in the visible view when the sheet is 
     * first viewed after opening it in a viewer 
     * @return short indicating the rownum (0 based) of the top row
     */
    short getLeftCol();

    /**
     * Sets desktop window pane display area, when the 
     * file is first opened in a viewer.
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
     * TODO Might want to add bounds checking here
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
     * @param topRow        Top row visible in bottom pane
     * @param leftmostColumn   Left column visible in right pane.
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
     * Returns the information regarding the currently configured pane (split or freeze).
     * @return null if no pane configured, or the pane information.
     */
    PaneInformation getPaneInformation();

    /**
     * Sets whether the gridlines are shown in a viewer.
     * @param show whether to show gridlines or not
     */
    void setDisplayGridlines(boolean show);

    /**
     * Returns if gridlines are displayed.
     * @return whether gridlines are displayed
     */
    boolean isDisplayGridlines();

    /**
     * Sets whether the formulas are shown in a viewer.
     * @param show whether to show formulas or not
     */
    void setDisplayFormulas(boolean show);

    /**
     * Returns if formulas are displayed.
     * @return whether formulas are displayed
     */
    boolean isDisplayFormulas();

    /**
     * Sets whether the RowColHeadings are shown in a viewer.
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
    short[] getColumnBreaks();

    /**
     * Sets a page break at the indicated column
     * @param column
     */
    void setColumnBreak(short column);

    /**
     * Determines if there is a page break at the indicated column
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
    boolean isColumnBroken(short column);

    /**
     * Removes a page break at the indicated column
     * @param column
     */
    void removeColumnBreak(short column);

    /**
     * Aggregates the drawing records and dumps the escher record hierarchy
     * to the standard output.
     */
    void dumpDrawingRecords(boolean fat);

    /**
     * Creates the toplevel drawing patriarch.  This will have the effect of
     * removing any existing drawings on this sheet.
     *
     * @return  The new patriarch.
     */
    Patriarch createDrawingPatriarch();

    /**
     * Expands or collapses a column group.
     *
     * @param columnNumber      One of the columns in the group.
     * @param collapsed         true = collapse group, false = expand group.
     */
    void setColumnGroupCollapsed(short columnNumber, boolean collapsed);

    /**
     * Create an outline for the provided column range.
     *
     * @param fromColumn        beginning of the column range.
     * @param toColumn          end of the column range.
     */
    void groupColumn(short fromColumn, short toColumn);

    void ungroupColumn(short fromColumn, short toColumn);

    void groupRow(int fromRow, int toRow);

    void ungroupRow(int fromRow, int toRow);

    void setRowGroupCollapsed(int row, boolean collapse);

    /**
     * Sets the default column style for a given column.  POI will only apply this style to new cells added to the sheet.
     *
     * @param column the column index
     * @param style the style to set
     */
    void setDefaultColumnStyle(short column, CellStyle style);

    /**
     * Adjusts the column width to fit the contents.
     *
     * This process can be relatively slow on large sheets, so this should
     *  normally only be called once per column, at the end of your
     *  processing.
     *
     * @param column the column index
     */
    void autoSizeColumn(short column);

    /**
     * Returns cell comment for the specified row and column
     *
     * @return cell comment or <code>null</code> if not found
     */
    Comment getCellComment(int row, int column);

}
