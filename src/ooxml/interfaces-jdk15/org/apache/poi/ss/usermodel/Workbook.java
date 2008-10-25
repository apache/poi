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

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

public interface Workbook {

    /**
     * used for compile-time performance/memory optimization.  This determines the
     * initial capacity for the sheet collection.  Its currently set to 3.
     * Changing it in this release will decrease performance
     * since you're never allowed to have more or less than three sheets!
     */

    public final static int INITIAL_CAPACITY = 3;

    /** Extended windows meta file */
    public static final int PICTURE_TYPE_EMF = 2;

    /** Windows Meta File */
    public static final int PICTURE_TYPE_WMF = 3;

    /** Mac PICT format */
    public static final int PICTURE_TYPE_PICT = 4;

    /** JPEG format */
    public static final int PICTURE_TYPE_JPEG = 5;

    /** PNG format */
    public static final int PICTURE_TYPE_PNG = 6;

    /** Device independent bitmap */
    public static final int PICTURE_TYPE_DIB = 7;

    int getActiveSheetIndex();
    void setActiveSheet(int sheetIndex);

    int getFirstVisibleTab();
    void setFirstVisibleTab(int sheetIndex);

    /**
     * sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */

    void setSheetOrder(String sheetname, int pos);

    /**
     * sets the tab whose data is actually seen when the sheet is opened.
     * This may be different from the "selected sheet" since excel seems to
     * allow you to show the data of one sheet when another is seen "selected"
     * in the tabs (at the bottom).
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#setSelected(boolean)
     * @param index
     */
    void setSelectedTab(short index);

    /**
     * gets the tab whose data is actually seen when the sheet is opened.
     * This may be different from the "selected sheet" since excel seems to
     * allow you to show the data of one sheet when another is seen "selected"
     * in the tabs (at the bottom).
     * @see org.apache.poi.hssf.usermodel.HSSFSheet#setSelected(boolean)
     */
    short getSelectedTab();

    /**
     * set the sheet name.
     * Will throw IllegalArgumentException if the name is greater than 31 chars
     * or contains /\?*[]
     * @param sheet number (0 based)
     */
    void setSheetName(int sheet, String name);

    /**
     * get the sheet name
     * @param sheet Number
     * @return Sheet name
     */

    String getSheetName(int sheet);

    /** Returns the index of the sheet by his name
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    int getSheetIndex(String name);

    /** Returns the index of the given sheet
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based)
     */
    int getSheetIndex(Sheet sheet);

    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return HSSFSheet representing the new sheet.
     */

    Sheet createSheet();

    /**
     * create an HSSFSheet from an existing sheet in the HSSFWorkbook.
     *
     * @return HSSFSheet representing the cloned sheet.
     */

    Sheet cloneSheet(int sheetNum);

    /**
     * create an HSSFSheet for this HSSFWorkbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @param sheetname     sheetname to set for the sheet.
     * @return HSSFSheet representing the new sheet.
     */

    Sheet createSheet(String sheetname);

    /**
     * get the number of spreadsheets in the workbook (this will be three after serialization)
     * @return number of sheets
     */

    int getNumberOfSheets();

    /**
     * Get the HSSFSheet object at the given index.
     * @param index of the sheet number (0-based physical & logical)
     * @return HSSFSheet at the provided index
     */

    Sheet getSheetAt(int index);

    /**
     * Get sheet with the given name
     * @param name of the sheet
     * @return HSSFSheet with the name provided or null if it does not exist
     */

    Sheet getSheet(String name);

    /**
     * removes sheet at the given index
     * @param index of the sheet  (0-based)
     */

    void removeSheetAt(int index);
    
    /**
     * Sets the repeating rows and columns for a sheet (as found in
     * File->PageSetup->Sheet).  This is function is included in the workbook
     * because it creates/modifies name records which are stored at the
     * workbook level.
     * <p>
     * To set just repeating columns:
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,0,1,-1-1);
     * </pre>
     * To set just repeating rows:
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,-1,-1,0,4);
     * </pre>
     * To remove all repeating rows and columns for a sheet.
     * <pre>
     *  workbook.setRepeatingRowsAndColumns(0,-1,-1,-1,-1);
     * </pre>
     *
     * @param sheetIndex    0 based index to sheet.
     * @param startColumn   0 based start of repeating columns.
     * @param endColumn     0 based end of repeating columns.
     * @param startRow      0 based start of repeating rows.
     * @param endRow        0 based end of repeating rows.
     */
    void setRepeatingRowsAndColumns(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow);

    /**
     * create a new Font and add it to the workbook's font table
     * @return new font object
     */

    Font createFont();

    /**
     * Finds a font that matches the one with the supplied attributes
     */
    Font findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline);

    /**
     * get the number of fonts in the font table
     * @return number of fonts
     */

    short getNumberOfFonts();

    /**
     * get the font at the given index number
     * @param idx  index number
     * @return HSSFFont at the index
     */

    Font getFontAt(short idx);

    /**
     * create a new Cell style and add it to the workbook's style table
     * @return the new Cell Style object
     */

    CellStyle createCellStyle();

    /**
     * get the number of styles the workbook contains
     * @return count of cell styles
     */

    short getNumCellStyles();

    /**
     * get the cell style object at the given index
     * @param idx  index within the set of styles
     * @return HSSFCellStyle object at the index
     */

    CellStyle getCellStyleAt(short idx);

    /**
     * Method write - write out this workbook to an Outputstream.  Constructs
     * a new POI POIFSFileSystem, passes in the workbook binary representation  and
     * writes it out.
     *
     * @param stream - the java OutputStream you wish to write the XLS to
     *
     * @exception IOException if anything can't be written.
     * @see org.apache.poi.poifs.filesystem.POIFSFileSystem
     */

    void write(OutputStream stream) throws IOException;

    /** gets the total number of named ranges in the workboko
     * @return number of named ranges
     */
    int getNumberOfNames();

    /** gets the Named range
     * @param index position of the named range
     * @return named range high level
     */
    Name getNameAt(int index);

    /** gets the named range name
     * @param index the named range index (0 based)
     * @return named range name
     */
    String getNameName(int index);

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
     * Retrieves the reference for the printarea of the specified sheet, the sheet name is appended to the reference even if it was not specified.
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @return String Null if no print area has been defined
     */
    String getPrintArea(int sheetIndex);

    /**
     * Delete the printarea for the sheet specified
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     */
    void removePrintArea(int sheetIndex);

	/**
	 * Retrieves the current policy on what to do when
	 *  getting missing or blank cells from a row.
	 * The default is to return blank and null cells.
	 *  {@link MissingCellPolicy}
	 */
	MissingCellPolicy getMissingCellPolicy();
	/**
	 * Sets the policy on what to do when
	 *  getting missing or blank cells from a row.
	 * This will then apply to all calls to 
	 *  {@link Row#getCell(int)} }. See
	 *  {@link MissingCellPolicy}
	 */
	void setMissingCellPolicy(MissingCellPolicy missingCellPolicy);

    /** creates a new named range and add it to the model
     * @return named range high level
     */
    Name createName();

    /** gets the named range index by his name
     * <i>Note:</i>Excel named ranges are case-insensitive and
     * this method performs a case-insensitive search.
     * 
     * @param name named range name
     * @return named range index
     */
    int getNameIndex(String name);

    /** remove the named range by his index
     * @param index named range index (0 based)
     */
    void removeName(int index);

    /**
     * Returns the instance of HSSFDataFormat for this workbook.
     * @return the HSSFDataFormat object
     * @see org.apache.poi.hssf.record.FormatRecord
     * @see org.apache.poi.hssf.record.Record
     */
    DataFormat createDataFormat();

    /** remove the named range by his name
     * @param name named range name
     */
    void removeName(String name);

    /**
     * Adds a picture to the workbook.
     *
     * @param pictureData       The bytes of the picture
     * @param format            The format of the picture.  One of <code>PICTURE_TYPE_*</code>
     *
     * @return the index to this picture (1 based).
     */
    int addPicture(byte[] pictureData, int format);

    /**
     * Gets all pictures from the Workbook.
     *
     * @return the list of pictures (a list of {@link PictureData} objects.)
     */
    List getAllPictures();

    /**
     * Returns an object that handles instantiating concrete
     *  classes of the various instances one needs for 
     *  HSSF and XSSF.
     * Works around a major shortcoming in Java, where we
     *  can't have static methods on interfaces or abstract
     *  classes.
     */
    CreationHelper getCreationHelper();
}
