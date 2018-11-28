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

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.util.Removal;

/**
 * High level representation of a Excel workbook.  This is the first object most users
 * will construct whether they are reading or writing a workbook.  It is also the
 * top level object for creating new sheets/etc.
 */
public interface Workbook extends Closeable, Iterable<Sheet> {

    /** Extended windows meta file */
    int PICTURE_TYPE_EMF = 2;

    /** Windows Meta File */
    int PICTURE_TYPE_WMF = 3;

    /** Mac PICT format */
    int PICTURE_TYPE_PICT = 4;

    /** JPEG format */
    int PICTURE_TYPE_JPEG = 5;

    /** PNG format */
    int PICTURE_TYPE_PNG = 6;

    /** Device independent bitmap */
    int PICTURE_TYPE_DIB = 7;

    /**
     * Convenience method to get the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     *
     * @return the index of the active sheet (0-based)
     */
    int getActiveSheetIndex();

    /**
     * Convenience method to set the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     *
     * @param sheetIndex index of the active sheet (0-based)
     */
    void setActiveSheet(int sheetIndex);

    /**
     * Gets the first tab that is displayed in the list of tabs in excel.
     *
     * @return the first tab that to display in the list of tabs (0-based).
     */
    int getFirstVisibleTab();

    /**
     * Sets the first tab that is displayed in the list of tabs in excel.
     *
     * @param sheetIndex the first tab that to display in the list of tabs (0-based)
     */
    void setFirstVisibleTab(int sheetIndex);

    /**
     * Sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */
    void setSheetOrder(String sheetname, int pos);

    /**
     * Sets the tab whose data is actually seen when the sheet is opened.
     * This may be different from the "selected sheet" since excel seems to
     * allow you to show the data of one sheet when another is seen "selected"
     * in the tabs (at the bottom).
     *
     * @see Sheet#setSelected(boolean)
     * @param index the index of the sheet to select (0 based)
     */
    void setSelectedTab(int index);

    /**
     * Set the sheet name.
     * <p>
     * See {@link org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)}
     *      for a safe way to create valid names
     * </p>
     * @param sheet number (0 based)
     * @throws IllegalArgumentException if the name is null or invalid
     *  or workbook already contains a sheet with this name
     * @see #createSheet(String)
     * @see org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)
     */
    void setSheetName(int sheet, String name);

    /**
     * Get the sheet name
     *
     * @param sheet sheet number (0 based)
     * @return Sheet name
     */
    String getSheetName(int sheet);

    /**
     * Returns the index of the sheet by his name
     *
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    int getSheetIndex(String name);

    /**
     * Returns the index of the given sheet
     *
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based)
     */
    int getSheetIndex(Sheet sheet);

    /**
     * Create a Sheet for this Workbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return Sheet representing the new sheet.
     */
    Sheet createSheet();

    /**
     * Create a new sheet for this Workbook and return the high level representation.
     * Use this to create new sheets.
     *
     * <p>
     *     Note that Excel allows sheet names up to 31 chars in length but other applications
     *     (such as OpenOffice) allow more. Some versions of Excel crash with names longer than 31 chars,
     *     others - truncate such names to 31 character.
     * </p>
     * <p>
     *     POI's SpreadsheetAPI silently truncates the input argument to 31 characters.
     *     Example:
     *
     *     <pre><code>
     *     Sheet sheet = workbook.createSheet("My very long sheet name which is longer than 31 chars"); // will be truncated
     *     assert 31 == sheet.getSheetName().length();
     *     assert "My very long sheet name which i" == sheet.getSheetName();
     *     </code></pre>
     * </p>
     *
     * Except the 31-character constraint, Excel applies some other rules:
     * <p>
     * Sheet name MUST be unique in the workbook and MUST NOT contain the any of the following characters:
     * <ul>
     * <li> 0x0000 </li>
     * <li> 0x0003 </li>
     * <li> colon (:) </li>
     * <li> backslash (\) </li>
     * <li> asterisk (*) </li>
     * <li> question mark (?) </li>
     * <li> forward slash (/) </li>
     * <li> opening square bracket ([) </li>
     * <li> closing square bracket (]) </li>
     * </ul>
     * The string MUST NOT begin or end with the single quote (') character.
     * </p>
     *
     * <p>
     * See {@link org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)}
     *      for a safe way to create valid names
     * </p>
     * @param sheetname The name to set for the sheet.
     * @return Sheet representing the new sheet.
     * @throws IllegalArgumentException if the name is null or invalid
     *  or workbook already contains a sheet with this name
     * @see org.apache.poi.ss.util.WorkbookUtil#createSafeSheetName(String nameProposal)
     */
    Sheet createSheet(String sheetname);

    /**
     * Create an Sheet from an existing sheet in the Workbook.
     *
     * @return Sheet representing the cloned sheet.
     */
    Sheet cloneSheet(int sheetNum);

    
    /**
     *  Returns an iterator of the sheets in the workbook
     *  in sheet order. Includes hidden and very hidden sheets.
     *
     * @return an iterator of the sheets.
     */
    Iterator<Sheet> sheetIterator();

    /**
     * Get the number of spreadsheets in the workbook
     *
     * @return the number of sheets
     */
    int getNumberOfSheets();

    /**
     * Get the Sheet object at the given index.
     *
     * @param index of the sheet number (0-based physical &amp; logical)
     * @return Sheet at the provided index
     * @throws IllegalArgumentException if the index is out of range (index
     *            &lt; 0 || index &gt;= getNumberOfSheets()).
     */
    Sheet getSheetAt(int index);

    /**
     * Get sheet with the given name
     *
     * @param name of the sheet
     * @return Sheet with the name provided or <code>null</code> if it does not exist
     */
    Sheet getSheet(String name);

    /**
     * Removes sheet at the given index
     *
     * @param index of the sheet to remove (0-based)
     */
    void removeSheetAt(int index);

    /**
     * Create a new Font and add it to the workbook's font table
     *
     * @return new font object
     */
    Font createFont();
    
    /**
     * Finds a font that matches the one with the supplied attributes
     *
     * @return the font with the matched attributes or <code>null</code>
     */
    Font findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline);

    /**
     * Get the number of fonts in the font table
     *
     * @return number of fonts
     * @deprecated use <code>getNumberOfFontsAsInt()</code> instead
     */
    @Removal(version = "4.2")
    short getNumberOfFonts();

    /**
     * Get the number of fonts in the font table
     *
     * @return number of fonts
     * @since 4.0.0
     */
    int getNumberOfFontsAsInt();

    /**
     * Get the font at the given index number
     *
     * @param idx  index number (0-based)
     * @return font at the index
     * @deprecated use <code>getFontAt(int)</code>
     */
    @Removal(version = "4.2")
    Font getFontAt(short idx);

    /**
     * Get the font at the given index number
     *
     * @param idx  index number (0-based)
     * @return font at the index
     * @since 4.0.0
     */
    Font getFontAt(int idx);

    /**
     * Create a new Cell style and add it to the workbook's style table
     *
     * @return the new Cell Style object
     * @throws IllegalStateException if the number of cell styles exceeded the limit for this type of Workbook.
     */
    CellStyle createCellStyle();

    /**
     * Get the number of styles the workbook contains
     *
     * @return count of cell styles
     */
    int getNumCellStyles();

    /**
     * Get the cell style object at the given index
     *
     * @param idx  index within the set of styles (0-based)
     * @return CellStyle object at the index
     */
    CellStyle getCellStyleAt(int idx);

    /**
     * Write out this workbook to an Outputstream.
     *
     * @param stream - the java OutputStream you wish to write to
     * @exception IOException if anything can't be written.
     */
    void write(OutputStream stream) throws IOException;

    /**
     * Close the underlying input resource (File or Stream),
     *  from which the Workbook was read.
     *
     * <p>Once this has been called, no further
     *  operations, updates or reads should be performed on the
     *  Workbook.
     */
    @Override
    void close() throws IOException;

    /**
     * @return the total number of defined names in this workbook
     */
    int getNumberOfNames();

    /**
     * @param name the name of the defined name
     * @return the defined name with the specified name. <code>null</code> if not found.
     */
    Name getName(String name);

    /**
     * Returns all defined names with the given name.
     *
     * @param name the name of the defined name
     * @return a list of the defined names with the specified name. An empty list is returned if none is found.
     */
    List<? extends Name> getNames(String name);

    /**
     * Returns all defined names.
     *
     * @return a list of the defined names. An empty list is returned if none is found.
     */
    List<? extends Name> getAllNames();

    /**
     * @param nameIndex position of the named range (0-based)
     * @return the defined name at the specified index
     * @throws IllegalArgumentException if the supplied index is invalid
     * @deprecated 4.0.0. New projects should avoid accessing named ranges by index.
     */
    @Deprecated
    @Removal(version="5.0.0")
    Name getNameAt(int nameIndex);

    /**
     * Creates a new (uninitialised) defined name in this workbook
     *
     * @return new defined name object
     */
    Name createName();

    /**
     * Gets the defined name index by name<br>
     * <i>Note:</i> Excel defined names are case-insensitive and
     * this method performs a case-insensitive search.
     *
     * @param name the name of the defined name
     * @return zero based index of the defined name. <tt>-1</tt> if not found.
     * @deprecated 3.18. New projects should avoid accessing named ranges by index.
     * Use {@link #getName(String)} instead.
     */
    @Deprecated
    @Removal(version="3.20")
    int getNameIndex(String name);

    /**
     * Remove the defined name at the specified index
     *
     * @param index named range index (0 based)
     *
     * @deprecated 3.18. New projects should use {@link #removeName(Name)}.
     */
    @Deprecated
    @Removal(version="3.20")
    void removeName(int index);

    /**
     * Remove a defined name by name
     *
     * @param name the name of the defined name
     * @deprecated 3.18. New projects should use {@link #removeName(Name)}.
     */
    @Deprecated
    @Removal(version="3.20")
    void removeName(String name);

    /**
     * Remove a defined name
     *
     * @param name the name of the defined name
     */
    void removeName(Name name);

    /**
     * Adds the linking required to allow formulas referencing
     *  the specified external workbook to be added to this one.
     * <p>In order for formulas such as "[MyOtherWorkbook]Sheet3!$A$5"
     *  to be added to the file, some linking information must first
     *  be recorded. Once a given external workbook has been linked,
     *  then formulas using it can added. Each workbook needs linking
     *  only once.
     * <p>This linking only applies for writing formulas. To link things
     *  for evaluation, see {@link FormulaEvaluator#setupReferencedWorkbooks(java.util.Map)}
     *
     * @param name The name the workbook will be referenced as in formulas
     * @param workbook The open workbook to fetch the link required information from
     */
    int linkExternalWorkbook(String name, Workbook workbook);

     /**
     * Sets the printarea for the sheet provided
     * <p>
     * i.e. Reference = $A$1:$B$2
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @param reference Valid name Reference for the Print Area
     */
    void setPrintArea(int sheetIndex, String reference);

    /**
     * For the Convenience of Java Programmers maintaining pointers.
     * @see #setPrintArea(int, String)
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     * @param startColumn Column to begin printarea
     * @param endColumn Column to end the printarea
     * @param startRow Row to begin the printarea
     * @param endRow Row to end the printarea
     */
    void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow);

    /**
     * Retrieves the reference for the printarea of the specified sheet,
     * the sheet name is appended to the reference even if it was not specified.
     *
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @return String Null if no print area has been defined
     */
    String getPrintArea(int sheetIndex);

    /**
     * Delete the printarea for the sheet specified
     *
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     */
    void removePrintArea(int sheetIndex);

	/**
	 * Retrieves the current policy on what to do when
	 *  getting missing or blank cells from a row.
     * <p>
	 * The default is to return blank and null cells.
	 *  {@link MissingCellPolicy}
     * </p>
	 */
	MissingCellPolicy getMissingCellPolicy();

    /**
	 * Sets the policy on what to do when
	 *  getting missing or blank cells from a row.
     *
	 * This will then apply to all calls to
	 *  {@link Row#getCell(int)} }. See
	 *  {@link MissingCellPolicy}
	 */
	void setMissingCellPolicy(MissingCellPolicy missingCellPolicy);

    /**
     * Returns the instance of DataFormat for this workbook.
     *
     * @return the DataFormat object
     */
    DataFormat createDataFormat();

    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.
     *
     * @return the index to this picture (1 based).
     * @see #PICTURE_TYPE_EMF
     * @see #PICTURE_TYPE_WMF
     * @see #PICTURE_TYPE_PICT
     * @see #PICTURE_TYPE_JPEG
     * @see #PICTURE_TYPE_PNG
     * @see #PICTURE_TYPE_DIB
     */
    int addPicture(byte[] pictureData, int format);

    /**
     * Gets all pictures from the Workbook.
     *
     * @return the list of pictures (a list of {@link PictureData} objects.)
     */
    List<? extends PictureData> getAllPictures();

    /**
     * Returns an object that handles instantiating concrete
     * classes of the various instances one needs for  HSSF and XSSF.
     */
    CreationHelper getCreationHelper();

    /**
     * @return <code>false</code> if this workbook is not visible in the GUI
     */
    boolean isHidden();

    /**
     * @param hiddenFlag pass <code>false</code> to make the workbook visible in the GUI
     */
    void setHidden(boolean hiddenFlag);

    /**
     * Check whether a sheet is hidden.
     * <p>
     * Note that a sheet could instead be set to be very hidden, which is different
     *  ({@link #isSheetVeryHidden(int)})
     * </p>
     * @param sheetIx Number
     * @return <code>true</code> if sheet is hidden
     * @see #getSheetVisibility(int)
     */
    boolean isSheetHidden(int sheetIx);

    /**
     * Check whether a sheet is very hidden.
     * <p>
     * This is different from the normal hidden status
     *  ({@link #isSheetHidden(int)})
     * </p>
     * @param sheetIx sheet index to check
     * @return <code>true</code> if sheet is very hidden
     * @see #getSheetVisibility(int)
     */
    boolean isSheetVeryHidden(int sheetIx);

    /**
     * Hide or unhide a sheet.
     * 
     * Please note that the sheet currently set as active sheet (sheet 0 in a newly 
     * created workbook or the one set via setActiveSheet()) cannot be hidden. 
     *
     * @param sheetIx the sheet index (0-based)
     * @param hidden True to mark the sheet as hidden, false otherwise
     * @see #setSheetVisibility(int, SheetVisibility)
     */
    void setSheetHidden(int sheetIx, boolean hidden);

    /**
     * Get the visibility (visible, hidden, very hidden) of a sheet in this workbook
     *
     * @param sheetIx  the index of the sheet
     * @return the sheet visibility
     * @since POI 3.16 beta 2
     */
    SheetVisibility getSheetVisibility(int sheetIx);

    /**
     * Hide or unhide a sheet.
     *
     * Please note that the sheet currently set as active sheet (sheet 0 in a newly 
     * created workbook or the one set via setActiveSheet()) cannot be hidden.
     *  
     * @param sheetIx     the sheet index (0-based)
     * @param visibility  the sheet visibility to set
     * @since POI 3.16 beta 2
     */
    void setSheetVisibility(int sheetIx, SheetVisibility visibility);

    /**
     * Register a new toolpack in this workbook.
     *
     * @param toopack the toolpack to register
     */
    void addToolPack(UDFFinder toopack);

    /**
     * Whether the application shall perform a full recalculation when the workbook is opened.
     * <p>
     * Typically you want to force formula recalculation when you modify cell formulas or values
     * of a workbook previously created by Excel. When set to true, this flag will tell Excel
     * that it needs to recalculate all formulas in the workbook the next time the file is opened.
     * </p>
     * <p>
     * Note, that recalculation updates cached formula results and, thus, modifies the workbook.
     * Depending on the version, Excel may prompt you with "Do you want to save the changes in <em>filename</em>?"
     * on close.
     * </p>
     *
     * @param value true if the application will perform a full recalculation of
     * workbook values when the workbook is opened
     * @since 3.8
     */
    void setForceFormulaRecalculation(boolean value);

    /**
     * Whether Excel will be asked to recalculate all formulas when the  workbook is opened.
     *
     * @since 3.8
     */
    boolean getForceFormulaRecalculation();
    
    /**
     * Returns the spreadsheet version of this workbook
     * 
     * @return SpreadsheetVersion enum
     * @since 3.14 beta 2
     */
    SpreadsheetVersion getSpreadsheetVersion();

    /**
     * Adds an OLE package manager object with the given content to the sheet
     *
     * @param oleData the payload
     * @param label the label of the payload
     * @param fileName the original filename
     * @param command the command to open the payload
     * 
     * @return the index of the added ole object, i.e. the storage id
     * 
     * @throws IOException if the object can't be embedded
     */
    int addOlePackage(byte[] oleData, String label, String fileName, String command) throws IOException;
}
