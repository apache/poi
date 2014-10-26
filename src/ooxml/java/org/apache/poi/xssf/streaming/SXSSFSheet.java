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

package org.apache.poi.xssf.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.hssf.util.PaneInformation;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.usermodel.AutoFilter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellRange;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Footer;
import org.apache.poi.ss.usermodel.Header;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.SheetUtil;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetFormatPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorksheet;

/**
 * Streaming version of XSSFSheet implementing the "BigGridDemo" strategy.
 *
 * @author Alex Geller, Four J's Development Tools
*/
public class SXSSFSheet implements Sheet, Cloneable
{
    SXSSFWorkbook _workbook;
    XSSFSheet _sh;
    TreeMap<Integer,SXSSFRow> _rows=new TreeMap<Integer,SXSSFRow>();
    SheetDataWriter _writer;
    int _randomAccessWindowSize = SXSSFWorkbook.DEFAULT_WINDOW_SIZE;
    int outlineLevelRow = 0;

    public SXSSFSheet(SXSSFWorkbook workbook, XSSFSheet xSheet) throws IOException
    {
        _workbook=workbook;
        _sh=xSheet;
        _writer = workbook.createSheetDataWriter();
        setRandomAccessWindowSize(_workbook.getRandomAccessWindowSize());

    }

    /**
     * for testing purposes only
     */
    SheetDataWriter getSheetDataWriter(){
        return _writer;
    }

/* Gets "<sheetData>" document fragment*/
    public InputStream getWorksheetXMLInputStream() throws IOException 
    {
        // flush all remaining data and close the temp file writer
        flushRows(0);
        _writer.close();
        return _writer.getWorksheetXMLInputStream();
    }

//start of interface implementation
    public Iterator<Row> iterator()
    {
        return rowIterator();
    }

    /**
     * Create a new row within the sheet and return the high level representation
     *
     * @param rownum  row number
     * @return high level Row object representing a row in the sheet
     * @see #removeRow(Row)
     */
    public Row createRow(int rownum)
    {
        int maxrow = SpreadsheetVersion.EXCEL2007.getLastRowIndex();
        if (rownum < 0 || rownum > maxrow) {
            throw new IllegalArgumentException("Invalid row number (" + rownum
                    + ") outside allowable range (0.." + maxrow + ")");
        }

        // attempt to overwrite a row that is already flushed to disk
        if(rownum <= _writer.getLastFlushedRow() ) {
            throw new IllegalArgumentException(
                    "Attempting to write a row["+rownum+"] " +
                    "in the range [0," + _writer.getLastFlushedRow() + "] that is already written to disk.");
        }

        // attempt to overwrite a existing row in the input template
        if(_sh.getPhysicalNumberOfRows() > 0 && rownum <= _sh.getLastRowNum() ) {
            throw new IllegalArgumentException(
                    "Attempting to write a row["+rownum+"] " +
                            "in the range [0," + _sh.getLastRowNum() + "] that is already written to disk.");
        }

//Make the initial allocation as big as the row above.
        Row previousRow=rownum>0?getRow(rownum-1):null;
        int initialAllocationSize=0;
//have previous row in memory -> take that value.
        if(previousRow!=null)
            initialAllocationSize=previousRow.getLastCellNum();
//are we called after a flush(0)? If yes, ask the writer for the value.
        if(initialAllocationSize<=0&&_writer.getNumberOfFlushedRows()>0)
            initialAllocationSize=_writer.getNumberOfCellsOfLastFlushedRow();
//default to 10 on the first row.
        if(initialAllocationSize<=0)
            initialAllocationSize=10;
        SXSSFRow newRow=new SXSSFRow(this,initialAllocationSize);
        _rows.put(new Integer(rownum),newRow);
        if(_randomAccessWindowSize>=0&&_rows.size()>_randomAccessWindowSize)
        {
            try
            {
               flushRows(_randomAccessWindowSize);
            }
            catch (IOException ioe)
            {
                throw new RuntimeException(ioe);
            }
        }
        return newRow;
    }

    /**
     * Remove a row from this sheet.  All cells contained in the row are removed as well
     *
     * @param row   representing a row to remove.
     */
    public void removeRow(Row row)
    {
        if (row.getSheet() != this) {
            throw new IllegalArgumentException("Specified row does not belong to this sheet");
        }

        for(Iterator<Map.Entry<Integer,SXSSFRow>> iter=_rows.entrySet().iterator();iter.hasNext();)
        {
            Map.Entry<Integer,SXSSFRow> entry=iter.next();
            if(entry.getValue()==row)
            {
                iter.remove();
                return;
            }
        }
    }

    /**
     * Returns the logical row (not physical) 0-based.  If you ask for a row that is not
     * defined you get a null.  This is to say row 4 represents the fifth row on a sheet.
     *
     * @param rownum  row to get (0-based)
     * @return Row representing the rownumber or null if its not defined on the sheet
     */
    public Row getRow(int rownum)
    {
        return _rows.get(new Integer(rownum));
    }

    /**
     * Returns the number of physically defined rows (NOT the number of rows in the sheet)
     *
     * @return the number of physically defined rows in this sheet
     */
    public int getPhysicalNumberOfRows()
    {
        return _rows.size()+_writer.getNumberOfFlushedRows();
    }

    /**
     * Gets the first row on the sheet
     *
     * @return the number of the first logical row on the sheet (0-based)
     */
    public int getFirstRowNum()
    {
        if(_writer.getNumberOfFlushedRows() > 0)
            return _writer.getLowestIndexOfFlushedRows();
        return _rows.size() == 0 ? 0 : _rows.firstKey();
    }

    /**
     * Gets the last row on the sheet
     *
     * @return last row contained n this sheet (0-based)
     */
    public int getLastRowNum()
    {
        return _rows.size() == 0 ? 0 : _rows.lastKey();
    }

    /**
     * Get the visibility state for a given column
     *
     * @param columnIndex - the column to get (0-based)
     * @param hidden - the visiblity state of the column
     */
    public void setColumnHidden(int columnIndex, boolean hidden)
    {
        _sh.setColumnHidden(columnIndex,hidden);
    }

    /**
     * Get the hidden state for a given column
     *
     * @param columnIndex - the column to set (0-based)
     * @return hidden - <code>false</code> if the column is visible
     */
    public boolean isColumnHidden(int columnIndex)
    {
        return _sh.isColumnHidden(columnIndex);
    }

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
    public void setColumnWidth(int columnIndex, int width)
    {
        _sh.setColumnWidth(columnIndex,width);
    }

    /**
     * get the width (in units of 1/256th of a character width )
     * @param columnIndex - the column to set (0-based)
     * @return width - the width in units of 1/256th of a character width
     */
    public int getColumnWidth(int columnIndex)
    {
        return _sh.getColumnWidth(columnIndex);
    }

    /**
     * Set the default column width for the sheet (if the columns do not define their own width)
     * in characters
     *
     * @param width default column width measured in characters
     */
    public void setDefaultColumnWidth(int width)
    {
        _sh.setDefaultColumnWidth(width);
    }

    /**
     * Get the default column width for the sheet (if the columns do not define their own width)
     * in characters
     *
     * @return default column width measured in characters
     */
    public int getDefaultColumnWidth()
    {
        return _sh.getDefaultColumnWidth();
    }
 

    /**
     * Get the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @return  default row height measured in twips (1/20 of  a point)
     */
    public short getDefaultRowHeight()
    {
        return _sh.getDefaultRowHeight();
    }

    /**
     * Get the default row height for the sheet (if the rows do not define their own height) in
     * points.
     *
     * @return  default row height in points
     */
    public float getDefaultRowHeightInPoints()
    {
        return _sh.getDefaultRowHeightInPoints();
    }

    /**
     * Set the default row height for the sheet (if the rows do not define their own height) in
     * twips (1/20 of  a point)
     *
     * @param  height default row height measured in twips (1/20 of  a point)
     */
    public void setDefaultRowHeight(short height)
    {
        _sh.setDefaultRowHeight(height);
    }

    /**
     * Set the default row height for the sheet (if the rows do not define their own height) in
     * points
     * @param height default row height
     */
    public void setDefaultRowHeightInPoints(float height)
    {
        _sh.setDefaultRowHeightInPoints(height);
    }
    

    /**
     * Returns the CellStyle that applies to the given
     *  (0 based) column, or null if no style has been
     *  set for that column
     */
    public CellStyle getColumnStyle(int column)
    {
        return _sh.getColumnStyle(column);
    }

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
    public int addMergedRegion(CellRangeAddress region)
    {
        return _sh.addMergedRegion(region);
    }

    /**
     * Determines whether the output is vertically centered on the page.
     *
     * @param value true to vertically center, false otherwise.
     */
    public void setVerticallyCenter(boolean value)
    {
        _sh.setVerticallyCenter(value);
    }

    /**
     * Determines whether the output is horizontally centered on the page.
     *
     * @param value true to horizontally center, false otherwise.
     */
    public void setHorizontallyCenter(boolean value)
    {
        _sh.setHorizontallyCenter(value);
    }

    /**
     * Determine whether printed output for this sheet will be horizontally centered.
     */

    public boolean getHorizontallyCenter()
    {
        return _sh.getHorizontallyCenter();
    }

    /**
     * Determine whether printed output for this sheet will be vertically centered.
     */
    public boolean getVerticallyCenter()
    {
        return _sh.getVerticallyCenter();
    }

    /**
     * Removes a merged region of cells (hence letting them free)
     *
     * @param index of the region to unmerge
     */
    public void removeMergedRegion(int index)
    {
        _sh.removeMergedRegion(index);
    }

    /**
     * Returns the number of merged regions
     *
     * @return number of merged regions
     */
    public int getNumMergedRegions()
    {
        return _sh.getNumMergedRegions();
    }

    /**
     * Returns the merged region at the specified index
     *
     * @return the merged region at the specified index
     */
    public CellRangeAddress getMergedRegion(int index)
    {
        return _sh.getMergedRegion(index);
    }

    /**
     *  Returns an iterator of the physical rows
     *
     * @return an iterator of the PHYSICAL rows.  Meaning the 3rd element may not
     * be the third row if say for instance the second row is undefined.
     */
    public Iterator<Row> rowIterator()
    {
        @SuppressWarnings("unchecked")
        Iterator<Row> result = (Iterator<Row>)(Iterator<? extends Row>)_rows.values().iterator();
        return result;
    }

    /**
     * Flag indicating whether the sheet displays Automatic Page Breaks.
     *
     * @param value <code>true</code> if the sheet displays Automatic Page Breaks.
     */
    public void setAutobreaks(boolean value)
    {
        _sh.setAutobreaks(value);
    }

    /**
     * Set whether to display the guts or not
     *
     * @param value - guts or no guts
     */
    public void setDisplayGuts(boolean value)
    {
        _sh.setDisplayGuts(value);
    }

    /**
     * Set whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     *
     * @param value whether to display or hide all zero values on the worksheet
     */
    public void setDisplayZeros(boolean value)
    {
        _sh.setDisplayZeros(value);
    }


    /**
     * Gets the flag indicating whether the window should show 0 (zero) in cells containing zero value.
     * When false, cells with zero value appear blank instead of showing the number zero.
     *
     * @return whether all zero values on the worksheet are displayed
     */
    public boolean isDisplayZeros()
    {
        return _sh.isDisplayZeros();
    }
    
    /**
     * Sets whether the worksheet is displayed from right to left instead of from left to right.
     *
     * @param value true for right to left, false otherwise.
     */
    public void setRightToLeft(boolean value)
    {
       _sh.setRightToLeft(value);
    }

    /**
     * Whether the text is displayed in right-to-left mode in the window
     *
     * @return whether the text is displayed in right-to-left mode in the window
     */
    public boolean isRightToLeft()
    {
       return _sh.isRightToLeft();
    }

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @param value <code>true</code> if the Fit to Page print option is enabled.
     */
    public void setFitToPage(boolean value)
    {
        _sh.setFitToPage(value);
    }

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
    public void setRowSumsBelow(boolean value)
    {
        _sh.setRowSumsBelow(value);
    }

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
    public void setRowSumsRight(boolean value)
    {
        _sh.setRowSumsRight(value);
    }

    /**
     * Flag indicating whether the sheet displays Automatic Page Breaks.
     *
     * @return <code>true</code> if the sheet displays Automatic Page Breaks.
     */
    public boolean getAutobreaks()
    {
        return _sh.getAutobreaks();
    }

    /**
     * Get whether to display the guts or not,
     * default value is true
     *
     * @return boolean - guts or no guts
     */
    public boolean getDisplayGuts()
    {
        return _sh.getDisplayGuts();
    }

    /**
     * Flag indicating whether the Fit to Page print option is enabled.
     *
     * @return <code>true</code> if the Fit to Page print option is enabled.
     */
    public boolean getFitToPage()
    {
        return _sh.getFitToPage();
    }

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
    public boolean getRowSumsBelow()
    {
        return _sh.getRowSumsBelow();
    }

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
    public boolean getRowSumsRight()
    {
        return _sh.getRowSumsRight();
    }

    /**
     * Gets the flag indicating whether this sheet displays the lines
     * between rows and columns to make editing and reading easier.
     *
     * @return <code>true</code> if this sheet displays gridlines.
     * @see #isPrintGridlines() to check if printing of gridlines is turned on or off
     */
    public boolean isPrintGridlines()
    {
        return _sh.isPrintGridlines();
    }

    /**
     * Sets the flag indicating whether this sheet should display the lines
     * between rows and columns to make editing and reading easier.
     * To turn printing of gridlines use {@link #setPrintGridlines(boolean)}
     *
     *
     * @param show <code>true</code> if this sheet should display gridlines.
     * @see #setPrintGridlines(boolean)
     */
    public void setPrintGridlines(boolean show)
    {
        _sh.setPrintGridlines(show);
    }

    /**
     * Gets the print setup object.
     *
     * @return The user model for the print setup object.
     */
    public PrintSetup getPrintSetup()
    {
        return _sh.getPrintSetup();
    }

    /**
     * Gets the user model for the default document header.
     * <p/>
     * Note that XSSF offers more kinds of document headers than HSSF does
     * </p>
     * @return the document header. Never <code>null</code>
     */
    public Header getHeader()
    {
        return _sh.getHeader();
    }

    /**
     * Gets the user model for the default document footer.
     * <p/>
     * Note that XSSF offers more kinds of document footers than HSSF does.
     *
     * @return the document footer. Never <code>null</code>
     */
    public Footer getFooter()
    {
        return _sh.getFooter();
    }

    /**
     * Sets a flag indicating whether this sheet is selected.
     *<p>
     * Note: multiple sheets can be selected, but only one sheet can be active at one time.
     *</p>
     * @param value <code>true</code> if this sheet is selected
     * @see Workbook#setActiveSheet(int)
     */
    public void setSelected(boolean value)
    {
        _sh.setSelected(value);
    }

    /**
     * Gets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @return the size of the margin
     */
    public double getMargin(short margin)
    {
        return _sh.getMargin(margin);
    }

    /**
     * Sets the size of the margin in inches.
     *
     * @param margin which margin to get
     * @param size the size of the margin
     */
    public void setMargin(short margin, double size)
    {
        _sh.setMargin(margin,size);
    }

    /**
     * Answer whether protection is enabled or disabled
     *
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getProtect()
    {
        return _sh.getProtect();
    }
    
    /**
     * Sets the protection enabled as well as the password
     * @param password to set for protection. Pass <code>null</code> to remove protection
     */
    public void protectSheet(String password)
    {
        _sh.protectSheet(password);
    }
    
    /**
     * Answer whether scenario protection is enabled or disabled
     *
     * @return true => protection enabled; false => protection disabled
     */
    public boolean getScenarioProtect()
    {
        return _sh.getScenarioProtect();
    }

    /**
     * Sets the zoom magnication for the sheet.  The zoom is expressed as a
     * fraction.  For example to express a zoom of 75% use 3 for the numerator
     * and 4 for the denominator.
     *
     * @param numerator     The numerator for the zoom magnification.
     * @param denominator   The denominator for the zoom magnification.
     */
    public void setZoom(int numerator, int denominator)
    {
        _sh.setZoom(numerator,denominator);
    }

    /**
     * The top row in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getTopRow()
    {
        return _sh.getTopRow();
    }

    /**
     * The left col in the visible view when the sheet is
     * first viewed after opening it in a viewer
     *
     * @return short indicating the rownum (0 based) of the top row
     */
    public short getLeftCol()
    {
        return _sh.getLeftCol();
    }

    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     *
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     */
    public void showInPane(int toprow, int leftcol)
    {
        _sh.showInPane(toprow, leftcol);
    }

    /**
     * Sets desktop window pane display area, when the
     * file is first opened in a viewer.
     *
     * @param toprow the top row to show in desktop window pane
     * @param leftcol the left column to show in desktop window pane
     * 
     * @deprecated Use the version of showInPane() with ints as there can be more than 32767 rows. 
     */
    public void showInPane(short toprow, short leftcol)
    {
        _sh.showInPane(toprow, leftcol);
    }

    /**
     * Control if Excel should be asked to recalculate all formulas when the
     *  workbook is opened, via the "sheetCalcPr fullCalcOnLoad" option.
     *  Calculating the formula values with {@link org.apache.poi.ss.usermodel.FormulaEvaluator} is the
     *  recommended solution, but this may be used for certain cases where
     *  evaluation in POI is not possible.
     */
    public void setForceFormulaRecalculation(boolean value) {
       _sh.setForceFormulaRecalculation(value);
    }

    /**
     * Whether Excel will be asked to recalculate all formulas when the
     *  workbook is opened.
     */
    public boolean getForceFormulaRecalculation() {
       return _sh.getForceFormulaRecalculation();
    }

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
    public void shiftRows(int startRow, int endRow, int n)
    {
        throw new RuntimeException("NotImplemented");
    }

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
    public void shiftRows(int startRow, int endRow, int n, boolean copyRowHeight, boolean resetOriginalRowHeight)
    {
        throw new RuntimeException("NotImplemented");
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     * @param leftmostColumn   Left column visible in right pane.
     * @param topRow        Top row visible in bottom pane
     */
    public void createFreezePane(int colSplit, int rowSplit, int leftmostColumn, int topRow)
    {
        _sh.createFreezePane(colSplit, rowSplit, leftmostColumn, topRow);
    }

    /**
     * Creates a split (freezepane). Any existing freezepane or split pane is overwritten.
     * @param colSplit      Horizonatal position of split.
     * @param rowSplit      Vertical position of split.
     */
    public void createFreezePane(int colSplit, int rowSplit)
    {
        _sh.createFreezePane(colSplit,rowSplit);
    }

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
    public void createSplitPane(int xSplitPos, int ySplitPos, int leftmostColumn, int topRow, int activePane)
    {
        _sh.createSplitPane(xSplitPos, ySplitPos, leftmostColumn, topRow, activePane);
    }

    /**
     * Returns the information regarding the currently configured pane (split or freeze)
     *
     * @return null if no pane configured, or the pane information.
     */
    public PaneInformation getPaneInformation()
    {
        return _sh.getPaneInformation();
    }

    /**
     * Sets whether the gridlines are shown in a viewer
     *
     * @param show whether to show gridlines or not
     */
    public void setDisplayGridlines(boolean show)
    {
        _sh.setDisplayGridlines(show);
    }

    /**
     * Returns if gridlines are displayed
     *
     * @return whether gridlines are displayed
     */
    public boolean isDisplayGridlines()
    {
        return _sh.isDisplayGridlines();
    }

    /**
     * Sets whether the formulas are shown in a viewer
     *
     * @param show whether to show formulas or not
     */
    public void setDisplayFormulas(boolean show)
    {
        _sh.setDisplayFormulas(show);
    }

    /**
     * Returns if formulas are displayed
     *
     * @return whether formulas are displayed
     */
    public boolean isDisplayFormulas()
    {
        return _sh.isDisplayFormulas();
    }

    /**
     * Sets whether the RowColHeadings are shown in a viewer
     *
     * @param show whether to show RowColHeadings or not
     */
    public void setDisplayRowColHeadings(boolean show)
    {
        _sh.setDisplayRowColHeadings(show);
    }

    /**
     * Returns if RowColHeadings are displayed.
     * @return whether RowColHeadings are displayed
     */
    public boolean isDisplayRowColHeadings()
    {
        return _sh.isDisplayRowColHeadings();
    }

    /**
     * Sets a page break at the indicated row
     * @param row FIXME: Document this!
     */
    public void setRowBreak(int row)
    {
        _sh.setRowBreak(row);
    }

    /**
     * Determines if there is a page break at the indicated row
     * @param row FIXME: Document this!
     * @return FIXME: Document this!
     */
    public boolean isRowBroken(int row)
    {
        return _sh.isRowBroken(row);
    }

    /**
     * Removes the page break at the indicated row
     * @param row
     */
    public void removeRowBreak(int row)
    {
        _sh.removeRowBreak(row);
    }

    /**
     * Retrieves all the horizontal page breaks
     * @return all the horizontal page breaks, or null if there are no row page breaks
     */
    public int[] getRowBreaks()
    {
        return _sh.getRowBreaks();
    }

    /**
     * Retrieves all the vertical page breaks
     * @return all the vertical page breaks, or null if there are no column page breaks
     */
    public int[] getColumnBreaks()
    {
        return _sh.getColumnBreaks();
    }

    /**
     * Sets a page break at the indicated column
     * @param column
     */
    public void setColumnBreak(int column)
    {
        _sh.setColumnBreak(column);
    }

    /**
     * Determines if there is a page break at the indicated column
     * @param column FIXME: Document this!
     * @return FIXME: Document this!
     */
    public boolean isColumnBroken(int column)
    {
        return _sh.isColumnBroken(column);
    }

    /**
     * Removes a page break at the indicated column
     * @param column
     */
    public void removeColumnBreak(int column)
    {
        _sh.removeColumnBreak(column);
    }

    /**
     * Expands or collapses a column group.
     *
     * @param columnNumber      One of the columns in the group.
     * @param collapsed         true = collapse group, false = expand group.
     */
    public void setColumnGroupCollapsed(int columnNumber, boolean collapsed)
    {
        _sh.setColumnGroupCollapsed(columnNumber, collapsed);
    }

    /**
     * Create an outline for the provided column range.
     *
     * @param fromColumn        beginning of the column range.
     * @param toColumn          end of the column range.
     */
    public void groupColumn(int fromColumn, int toColumn)
    {
        _sh.groupColumn(fromColumn,toColumn);
    }

    /**
     * Ungroup a range of columns that were previously groupped
     *
     * @param fromColumn   start column (0-based)
     * @param toColumn     end column (0-based)
     */
    public void ungroupColumn(int fromColumn, int toColumn)
    {
        _sh.ungroupColumn(fromColumn, toColumn);
    }

    /**
     * Tie a range of rows together so that they can be collapsed or expanded
     *
     * <p>
     *     Please note the rows being grouped <em>must</em> be in the current window,
     *     if the rows are already flushed then groupRow has no effect.
     * </p>
     * <p>
     *      Correct code:
     *      <pre><code>
     *       Workbook wb = new SXSSFWorkbook(100);  // keep 100 rows in memory
     *       Sheet sh = wb.createSheet();
     *       for (int rownum = 0; rownum &lt; 1000; rownum++) {
     *           Row row = sh.createRow(rownum);
     *           if(rownum == 200)  {
     *               sh.groupRow(100, 200);
     *           }
     *       }
     *
     *      </code></pre>
     * </p>
     * <p>
     *      Incorrect code:
     *      <pre><code>
     *       Workbook wb = new SXSSFWorkbook(100);  // keep 100 rows in memory
     *       Sheet sh = wb.createSheet();
     *       for (int rownum = 0; rownum &lt; 1000; rownum++) {
     *           Row row = sh.createRow(rownum);
     *       }
     *       sh.groupRow(100, 200); // the rows in the range [100, 200] are already flushed and groupRows has no effect
     *
     *      </code></pre>
     * </p>
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    public void groupRow(int fromRow, int toRow)
    {
        for(SXSSFRow row : _rows.subMap(fromRow, toRow + 1).values()){
            int level = row.getOutlineLevel() + 1;
            row.setOutlineLevel(level);

            if(level > outlineLevelRow) outlineLevelRow = level;
        }

        CTWorksheet ct = _sh.getCTWorksheet();
        CTSheetFormatPr pr = ct.isSetSheetFormatPr() ?
                ct.getSheetFormatPr() :
                ct.addNewSheetFormatPr();
        if(outlineLevelRow > 0) pr.setOutlineLevelRow((short)outlineLevelRow);
    }

    /**
     * Ungroup a range of rows that were previously groupped
     *
     * @param fromRow   start row (0-based)
     * @param toRow     end row (0-based)
     */
    public void ungroupRow(int fromRow, int toRow)
    {
        _sh.ungroupRow(fromRow, toRow);
    }

    /**
     * Set view state of a groupped range of rows
     *
     * @param row   start row of a groupped range of rows (0-based)
     * @param collapse whether to expand/collapse the detail rows
     */
    public void setRowGroupCollapsed(int row, boolean collapse)
    {
        if (collapse) {
            collapseRow(row);
        } else {
            //expandRow(rowIndex);
            throw new RuntimeException("Not Implemented");
        }
    }
    
    /**
     * @param rowIndex the zero based row index to collapse
     */
    private void collapseRow(int rowIndex) {
        SXSSFRow row = (SXSSFRow) getRow(rowIndex);
        if(row == null) {
			throw new IllegalArgumentException("Invalid row number("+ rowIndex + "). Row does not exist.");
		} else {
            int startRow = findStartOfRowOutlineGroup(rowIndex);

            // Hide all the columns until the end of the group
            int lastRow = writeHidden(row, startRow, true);
            SXSSFRow lastRowObj = (SXSSFRow) getRow(lastRow); 
            if (lastRowObj != null) {
                lastRowObj.setCollapsed(true);
            } else {
                SXSSFRow newRow = (SXSSFRow) createRow(lastRow);
                newRow.setCollapsed(true);
            }
        }
    }
    
    /**
     * @param rowIndex the zero based row index to find from
     */
    private int findStartOfRowOutlineGroup(int rowIndex) {
        // Find the start of the group.
		Row row = getRow(rowIndex);
        int level = ((SXSSFRow) row).getOutlineLevel();
        if(level == 0) {
        	throw new IllegalArgumentException("Outline level is zero for the row (" + rowIndex + ").");
        }
        int currentRow = rowIndex;
        while (getRow(currentRow) != null) {
            if (((SXSSFRow) getRow(currentRow)).getOutlineLevel() < level)
                return currentRow + 1;
            currentRow--;
        }
        return currentRow + 1;
    }
    
    private int writeHidden(SXSSFRow xRow, int rowIndex, boolean hidden) {
        int level = xRow.getOutlineLevel();
        SXSSFRow currRow = (SXSSFRow) getRow(rowIndex);

        while (currRow != null && currRow.getOutlineLevel() >= level) {
            currRow.setHidden(hidden);
            rowIndex++;
            currRow = (SXSSFRow) getRow(rowIndex);
        }
        return rowIndex;
    }

    /**
     * Sets the default column style for a given column.  POI will only apply this style to new cells added to the sheet.
     *
     * @param column the column index
     * @param style the style to set
     */
    public void setDefaultColumnStyle(int column, CellStyle style)
    {
        _sh.setDefaultColumnStyle(column, style);
    }

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
    public void autoSizeColumn(int column)
    {
        autoSizeColumn(column, false);
    }

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
    public void autoSizeColumn(int column, boolean useMergedCells)
    {
        double width = SheetUtil.getColumnWidth(this, column, useMergedCells);

        if (width != -1) {
            width *= 256;
            int maxColumnWidth = 255*256; // The maximum column width for an individual cell is 255 characters
            if (width > maxColumnWidth) {
                width = maxColumnWidth;
            }
            setColumnWidth(column, (int)(width));
        }
    }

    /**
     * Returns cell comment for the specified row and column
     *
     * @return cell comment or <code>null</code> if not found
     */
    public Comment getCellComment(int row, int column)
    {
        return _sh.getCellComment(row, column);
    }

    /**
     * Creates the top-level drawing patriarch.
     *
     * @return  The new drawing patriarch.
     */
    public Drawing createDrawingPatriarch()
    {
        return _sh.createDrawingPatriarch();
    }


    /**
     * Return the parent workbook
     *
     * @return the parent workbook
     */
    public Workbook getWorkbook()
    {
        return _workbook;
    }

    /**
     * Returns the name of this sheet
     *
     * @return the name of this sheet
     */
    public String getSheetName()
    {
        return _sh.getSheetName();
    }

    /**
     * Note - this is not the same as whether the sheet is focused (isActive)
     * @return <code>true</code> if this sheet is currently selected
     */
    public boolean isSelected()
    {
        return _sh.isSelected();
    }


    /**
     * Sets array formula to specified region for result.
     *
     * @param formula text representation of the formula
     * @param range Region of array formula for result.
     * @return the {@link CellRange} of cells affected by this change
     */
    public CellRange<? extends Cell> setArrayFormula(String formula, CellRangeAddress range)
    {
        return _sh.setArrayFormula(formula, range);
    }

    /**
     * Remove a Array Formula from this sheet.  All cells contained in the Array Formula range are removed as well
     *
     * @param cell   any cell within Array Formula range
     * @return the {@link CellRange} of cells affected by this change
     */
    public CellRange<? extends Cell> removeArrayFormula(Cell cell)
    {
        return _sh.removeArrayFormula(cell);
    }
    
    public DataValidationHelper getDataValidationHelper()
    {
        return _sh.getDataValidationHelper();
    }

    public List<XSSFDataValidation> getDataValidations()
    {
        return _sh.getDataValidations();
    }

    /**
     * Creates a data validation object
     * @param dataValidation The Data validation object settings
     */
    public void addValidationData(DataValidation dataValidation)
    {
        _sh.addValidationData(dataValidation);
    }

    /**
     * Enable filtering for a range of cells
     * 
     * @param range the range of cells to filter
     */
    public AutoFilter setAutoFilter(CellRangeAddress range)
    {
        return _sh.setAutoFilter(range);
    }

    public SheetConditionalFormatting getSheetConditionalFormatting(){
        return _sh.getSheetConditionalFormatting();
    }
    
    
    public CellRangeAddress getRepeatingRows() {
      return _sh.getRepeatingRows();
    }
    
    public CellRangeAddress getRepeatingColumns() {
      return _sh.getRepeatingColumns();
    }
    
    public void setRepeatingRows(CellRangeAddress rowRangeRef) {
      _sh.setRepeatingRows(rowRangeRef);
    }
    
    public void setRepeatingColumns(CellRangeAddress columnRangeRef) {
      _sh.setRepeatingColumns(columnRangeRef);
    }
    
    
    
//end of interface implementation
    /**
     * Specifies how many rows can be accessed at most via getRow().
     * When a new node is created via createRow() and the total number
     * of unflushed records would exeed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via getRow() anymore.
     * A value of -1 indicates unlimited access. In this case all
     * records that have not been flushed by a call to flush() are available
     * for random access.
     * A value of 0 is not allowed because it would flush any newly created row
     * without having a chance to specify any cells.
     */
    public void setRandomAccessWindowSize(int value)
    {
         if(value == 0 || value < -1) {
             throw new IllegalArgumentException("RandomAccessWindowSize must be either -1 or a positive integer");
         }
         _randomAccessWindowSize=value;
    }

    /**
     * Specifies how many rows can be accessed at most via getRow().
     * The exeeding rows (if any) are flushed to the disk while rows
     * with lower index values are flushed first.
     */
    public void flushRows(int remaining) throws IOException
    {
        while(_rows.size() > remaining) flushOneRow();
    }

    /**
     * Flush all rows to disk. After this call no rows can be accessed via getRow()
     *
     * @throws IOException
     */
    public void flushRows() throws IOException
    {
        this.flushRows(0);
    }

    private void flushOneRow() throws IOException
    {
        Integer firstRowNum = _rows.firstKey();
        if (firstRowNum!=null) {
            int rowIndex = firstRowNum.intValue();
            SXSSFRow row = _rows.get(firstRowNum);
            _writer.writeRow(rowIndex, row);
            _rows.remove(firstRowNum);
        }
    }
    public void changeRowNum(SXSSFRow row, int newRowNum)
    {
        
        removeRow(row);
        _rows.put(new Integer(newRowNum),row);
    }

    public int getRowNum(SXSSFRow row)
    {
        for(Iterator<Map.Entry<Integer,SXSSFRow>> iter=_rows.entrySet().iterator();iter.hasNext();)
        {
            Map.Entry<Integer,SXSSFRow> entry=iter.next();
            if(entry.getValue()==row)
                return entry.getKey().intValue();
        }
        return -1;
    }

    /**
     * Deletes the temporary file that backed this sheet on disk.
     * @return true if the file was deleted, false if it wasn't.
     */
    boolean dispose() {
        return _writer.dispose();
    }
}
