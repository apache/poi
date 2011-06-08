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

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.File;
import java.util.*;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;

/**
 * Streaming version of XSSFWorkbook implementing the "BigGridDemo" strategy.
 *
 * @author Alex Geller, Four J's Development Tools
*/
public class SXSSFWorkbook implements Workbook
{
    /**
     * Specifies how many rows can be accessed at most via getRow().
     * When a new node is created via createRow() and the total number
     * of unflushed records would exeed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via getRow() anymore.
     */
    public static final int DEFAULT_WINDOW_SIZE = 100;

    XSSFWorkbook _wb=new XSSFWorkbook();

    HashMap<SXSSFSheet,XSSFSheet> _sxFromXHash=new HashMap<SXSSFSheet,XSSFSheet>();
    HashMap<XSSFSheet,SXSSFSheet> _xFromSxHash=new HashMap<XSSFSheet,SXSSFSheet>();

    int _randomAccessWindowSize = DEFAULT_WINDOW_SIZE;

    /**
     * Construct a new workbook
     */
    public SXSSFWorkbook(){

    }

    /**
     * Construct an empty workbook and specify the window for row access.
     * <p>
     * When a new node is created via createRow() and the total number
     * of unflushed records would exeed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via getRow() anymore.
     * </p>
     * <p>
     * A value of -1 indicates unlimited access. In this case all
     * records that have not been flushed by a call to flush() are available
     * for random access.
     * <p>
     * <p></p>
     * A value of 0 is not allowed because it would flush any newly created row
     * without having a chance to specify any cells.
     * </p>
     *
     * @param rowAccessWindowSize
     */
    public SXSSFWorkbook(int rowAccessWindowSize){
        if(rowAccessWindowSize == 0 || rowAccessWindowSize < -1) {
            throw new IllegalArgumentException("rowAccessWindowSize must be greater than 0 or -1");
        }
        _randomAccessWindowSize = rowAccessWindowSize;
    }

    XSSFSheet getXSSFSheet(SXSSFSheet sheet)
    {
        XSSFSheet result=_sxFromXHash.get(sheet);
        assert result!=null;
        return result;
    }

    SXSSFSheet getSXSSFSheet(XSSFSheet sheet)
    {
        SXSSFSheet result=_xFromSxHash.get(sheet);
        assert result!=null;
        return result;
    }

    void registerSheetMapping(SXSSFSheet sxSheet,XSSFSheet xSheet)
    {
        _sxFromXHash.put(sxSheet,xSheet);
        _xFromSxHash.put(xSheet,sxSheet);
    }
    void deregisterSheetMapping(XSSFSheet xSheet)
    {
        SXSSFSheet sxSheet=getSXSSFSheet(xSheet);
        _sxFromXHash.remove(sxSheet);
        _xFromSxHash.remove(xSheet);
    }
    private XSSFSheet getSheetFromZipEntryName(String sheetRef)
    {
        for(XSSFSheet sheet : _sxFromXHash.values())
        {
            if(sheetRef.equals(sheet.getPackagePart().getPartName().getName().substring(1))) return sheet;
        }
        return null;
    }
    private void injectData(File zipfile, OutputStream out) throws IOException 
    {
        ZipFile zip = new ZipFile(zipfile);

        ZipOutputStream zos = new ZipOutputStream(out);

        @SuppressWarnings("unchecked")
        Enumeration<ZipEntry> en = (Enumeration<ZipEntry>) zip.entries();
        while (en.hasMoreElements()) 
        {
            ZipEntry ze = en.nextElement();
            zos.putNextEntry(new ZipEntry(ze.getName()));
            InputStream is = zip.getInputStream(ze);
            XSSFSheet xSheet=getSheetFromZipEntryName(ze.getName());
            if(xSheet!=null)
            {
                SXSSFSheet sxSheet=getSXSSFSheet(xSheet);
                copyStreamAndInjectWorksheet(is,zos,sxSheet.getWorksheetXMLInputStream());
            }
            else
            {
                copyStream(is, zos);
            }
            is.close();
        }

        zos.close();
    }
    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] chunk = new byte[1024];
        int count;
        while ((count = in.read(chunk)) >=0 ) {
          out.write(chunk,0,count);
        }
    }
    private static void copyStreamAndInjectWorksheet(InputStream in, OutputStream out,InputStream worksheetData) throws IOException {
        InputStreamReader inReader=new InputStreamReader(in,"UTF-8"); //TODO: Is it always UTF-8 or do we need to read the xml encoding declaration in the file? If not, we should perhaps use a SAX reader instead.
        OutputStreamWriter outWriter=new OutputStreamWriter(out,"UTF-8");
        int c;
        int pos=0;
        String s="<sheetData/>";
        int n=s.length();
//Copy from "in" to "out" up to the string "<sheetData/>" (excluding).
        while(((c=inReader.read())!=-1))
        {
            if(c==s.charAt(pos))
            {
                pos++;
                if(pos==n) break;
            }
            else
            {
                if(pos>0) outWriter.write(s,0,pos);
                if(c==s.charAt(0))
                {
                    pos=1;
                }
                else
                {
                    outWriter.write(c);
                    pos=0;
                }
            }
        }
        outWriter.flush();
//Copy the worksheet data to "out".
        copyStream(worksheetData,out);
//Copy the rest of "in" to "out".
        while(((c=inReader.read())!=-1))
            outWriter.write(c);
        outWriter.flush();
    }

    public XSSFWorkbook getXSSFWorkbook()
    {
        return _wb;
    }

//start of interface implementation

    /**
     * Convenience method to get the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     *
     * @return the index of the active sheet (0-based)
     */
    public int getActiveSheetIndex()
    {
        return _wb.getActiveSheetIndex();
    }

    /**
     * Convenience method to set the active sheet.  The active sheet is is the sheet
     * which is currently displayed when the workbook is viewed in Excel.
     * 'Selected' sheet(s) is a distinct concept.
     *
     * @param sheetIndex index of the active sheet (0-based)
     */
    public void setActiveSheet(int sheetIndex)
    {
        _wb.setActiveSheet(sheetIndex);
    }

    /**
     * Gets the first tab that is displayed in the list of tabs in excel.
     *
     * @return the first tab that to display in the list of tabs (0-based).
     */
    public int getFirstVisibleTab()
    {
        return _wb.getFirstVisibleTab();
    }

    /**
     * Sets the first tab that is displayed in the list of tabs in excel.
     *
     * @param sheetIndex the first tab that to display in the list of tabs (0-based)
     */
    public void setFirstVisibleTab(int sheetIndex)
    {
        _wb.setFirstVisibleTab(sheetIndex);
    }

    /**
     * Sets the order of appearance for a given sheet.
     *
     * @param sheetname the name of the sheet to reorder
     * @param pos the position that we want to insert the sheet into (0 based)
     */
    public void setSheetOrder(String sheetname, int pos)
    {
        _wb.setSheetOrder(sheetname,pos);
    }

    /**
     * Sets the tab whose data is actually seen when the sheet is opened.
     * This may be different from the "selected sheet" since excel seems to
     * allow you to show the data of one sheet when another is seen "selected"
     * in the tabs (at the bottom).
     *
     * @see Sheet#setSelected(boolean)
     * @param index the index of the sheet to select (0 based)
     */
    public void setSelectedTab(int index)
    {
        _wb.setSelectedTab(index);
    }

    /**
     * Set the sheet name.
     *
     * @param sheet number (0 based)
     * @throws IllegalArgumentException if the name is greater than 31 chars or contains <code>/\?*[]</code>
     */
    public void setSheetName(int sheet, String name)
    {
        _wb.setSheetName(sheet,name);
    }

    /**
     * Set the sheet name
     *
     * @param sheet sheet number (0 based)
     * @return Sheet name
     */
    public String getSheetName(int sheet)
    {
        return _wb.getSheetName(sheet);
    }

    /**
     * Returns the index of the sheet by his name
     *
     * @param name the sheet name
     * @return index of the sheet (0 based)
     */
    public int getSheetIndex(String name)
    {
        return _wb.getSheetIndex(name);
    }

    /**
     * Returns the index of the given sheet
     *
     * @param sheet the sheet to look up
     * @return index of the sheet (0 based)
     */
    public int getSheetIndex(Sheet sheet)
    {
        assert sheet instanceof SXSSFSheet;
        return _wb.getSheetIndex(getXSSFSheet((SXSSFSheet)sheet));
    }

    /**
     * Sreate an Sheet for this Workbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return Sheet representing the new sheet.
     */
    public Sheet createSheet()
    {
        return createAndRegisterSXSSFSheet(_wb.createSheet());
    }
    SXSSFSheet createAndRegisterSXSSFSheet(XSSFSheet xSheet)
    {
        SXSSFSheet sxSheet=null;
        try
        {
            sxSheet=new SXSSFSheet(this,xSheet);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
        sxSheet.setRandomAccessWindowSize(_randomAccessWindowSize);
        registerSheetMapping(sxSheet,xSheet);
        return sxSheet;
    }

    /**
     * Create an Sheet for this Workbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @param sheetname  sheetname to set for the sheet.
     * @return Sheet representing the new sheet.
     * @throws IllegalArgumentException if the name is greater than 31 chars or contains <code>/\?*[]</code>
     */
    public Sheet createSheet(String sheetname)
    {
        return createAndRegisterSXSSFSheet(_wb.createSheet(sheetname));
    }

    /**
     * Create an Sheet from an existing sheet in the Workbook.
     *
     * @return Sheet representing the cloned sheet.
     */
    public Sheet cloneSheet(int sheetNum)
    {
        throw new RuntimeException("NotImplemented");
    }


    /**
     * Get the number of spreadsheets in the workbook
     *
     * @return the number of sheets
     */
    public int getNumberOfSheets()
    {
        return _wb.getNumberOfSheets();
    }

    /**
     * Get the Sheet object at the given index.
     *
     * @param index of the sheet number (0-based physical & logical)
     * @return Sheet at the provided index
     */
    public Sheet getSheetAt(int index)
    {
        return getSXSSFSheet(_wb.getSheetAt(index));
    }

    /**
     * Get sheet with the given name
     *
     * @param name of the sheet
     * @return Sheet with the name provided or <code>null</code> if it does not exist
     */
    public Sheet getSheet(String name)
    {
        return getSXSSFSheet(_wb.getSheet(name));
    }

    /**
     * Removes sheet at the given index
     *
     * @param index of the sheet to remove (0-based)
     */
    public void removeSheetAt(int index)
    {
        XSSFSheet xSheet=_wb.getSheetAt(index);
        _wb.removeSheetAt(index);
        deregisterSheetMapping(xSheet);
    }

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
    public void setRepeatingRowsAndColumns(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow)
    {
        _wb.setRepeatingRowsAndColumns(sheetIndex,startColumn,endColumn,startRow,endRow);
    }

    /**
     * Create a new Font and add it to the workbook's font table
     *
     * @return new font object
     */
    public Font createFont()
    {
        return _wb.createFont();
    }

    /**
     * Finds a font that matches the one with the supplied attributes
     *
     * @return the font with the matched attributes or <code>null</code>
     */
    public Font findFont(short boldWeight, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline)
    {
        return _wb.findFont(boldWeight, color, fontHeight, name, italic, strikeout, typeOffset, underline);
    }
   

    /**
     * Get the number of fonts in the font table
     *
     * @return number of fonts
     */
    public short getNumberOfFonts()
    {
        return _wb.getNumberOfFonts();
    }

    /**
     * Get the font at the given index number
     *
     * @param idx  index number (0-based)
     * @return font at the index
     */
    public Font getFontAt(short idx)
    {
        return _wb.getFontAt(idx);
    }

    /**
     * Create a new Cell style and add it to the workbook's style table
     *
     * @return the new Cell Style object
     */
    public CellStyle createCellStyle()
    {
        return _wb.createCellStyle();
    }

    /**
     * Get the number of styles the workbook contains
     *
     * @return count of cell styles
     */
    public short getNumCellStyles()
    {
        return _wb.getNumCellStyles();
    }

    /**
     * Get the cell style object at the given index
     *
     * @param idx  index within the set of styles (0-based)
     * @return CellStyle object at the index
     */
    public CellStyle getCellStyleAt(short idx)
    {
        return _wb.getCellStyleAt(idx);
    }

    /**
     * Write out this workbook to an Outputstream.
     *
     * @param stream - the java OutputStream you wish to write to
     * @exception IOException if anything can't be written.
     */
    public void write(OutputStream stream) throws IOException
    {
        //Save the template
        File tmplFile = File.createTempFile("template", ".xlsx");
        FileOutputStream os = new FileOutputStream(tmplFile);
        _wb.write(os);
        os.close();

        //Substitute the template entries with the generated sheet data files
        injectData(tmplFile, stream);
        tmplFile.delete();
    }

    /**
     * @return the total number of defined names in this workbook
     */
    public int getNumberOfNames()
    {
        return _wb.getNumberOfNames();
    }

    /**
     * @param name the name of the defined name
     * @return the defined name with the specified name. <code>null</code> if not found.
     */
    public Name getName(String name)
    {
        return _wb.getName(name);
    }
    /**
     * @param nameIndex position of the named range (0-based)
     * @return the defined name at the specified index
     * @throws IllegalArgumentException if the supplied index is invalid
     */
    public Name getNameAt(int nameIndex)
    {
        return _wb.getNameAt(nameIndex);
    }

    /**
     * Creates a new (uninitialised) defined name in this workbook
     *
     * @return new defined name object
     */
    public Name createName()
    {
        return _wb.createName();
    }

    /**
     * Gets the defined name index by name<br/>
     * <i>Note:</i> Excel defined names are case-insensitive and
     * this method performs a case-insensitive search.
     *
     * @param name the name of the defined name
     * @return zero based index of the defined name. <tt>-1</tt> if not found.
     */
    public int getNameIndex(String name)
    {
        return _wb.getNameIndex(name);
    }

    /**
     * Remove the defined name at the specified index
     *
     * @param index named range index (0 based)
     */
    public void removeName(int index)
    {
        _wb.removeName(index);
    }

    /**
     * Remove a defined name by name
     *
      * @param name the name of the defined name
     */
    public void removeName(String name)
    {
        _wb.removeName(name);
    }

     /**
     * Sets the printarea for the sheet provided
     * <p>
     * i.e. Reference = $A$1:$B$2
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @param reference Valid name Reference for the Print Area
     */
    public void setPrintArea(int sheetIndex, String reference)
    {
        _wb.setPrintArea(sheetIndex,reference);
    }

    /**
     * For the Convenience of Java Programmers maintaining pointers.
     * @see #setPrintArea(int, String)
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     * @param startColumn Column to begin printarea
     * @param endColumn Column to end the printarea
     * @param startRow Row to begin the printarea
     * @param endRow Row to end the printarea
     */
    public void setPrintArea(int sheetIndex, int startColumn, int endColumn, int startRow, int endRow)
    {
        _wb.setPrintArea(sheetIndex, startColumn, endColumn, startRow, endRow);
    }

    /**
     * Retrieves the reference for the printarea of the specified sheet,
     * the sheet name is appended to the reference even if it was not specified.
     *
     * @param sheetIndex Zero-based sheet index (0 Represents the first sheet to keep consistent with java)
     * @return String Null if no print area has been defined
     */
    public String getPrintArea(int sheetIndex)
    {
        return _wb.getPrintArea(sheetIndex);
    }

    /**
     * Delete the printarea for the sheet specified
     *
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     */
    public void removePrintArea(int sheetIndex)
    {
        _wb.removePrintArea(sheetIndex);
    }

    /**
     * Retrieves the current policy on what to do when
     *  getting missing or blank cells from a row.
     * <p>
     * The default is to return blank and null cells.
     *  {@link MissingCellPolicy}
     * </p>
     */
    public MissingCellPolicy getMissingCellPolicy()
    {
        return _wb.getMissingCellPolicy();
    }

    /**
     * Sets the policy on what to do when
     *  getting missing or blank cells from a row.
     *
     * This will then apply to all calls to
     *  {@link org.apache.poi.ss.usermodel.Row#getCell(int)}. See
     *  {@link MissingCellPolicy}
     */
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy)
    {
        _wb.setMissingCellPolicy(missingCellPolicy);
    }

    /**
     * Returns the instance of DataFormat for this workbook.
     *
     * @return the DataFormat object
     */
    public DataFormat createDataFormat()
    {
        return _wb.createDataFormat();
    }

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
    public int addPicture(byte[] pictureData, int format)
    {
        return _wb.addPicture(pictureData,format);
    }

    /**
     * Gets all pictures from the Workbook.
     *
     * @return the list of pictures (a list of {@link PictureData} objects.)
     */
    public List<? extends PictureData> getAllPictures()
    {
        return _wb.getAllPictures();
    }

    /**
     * Returns an object that handles instantiating concrete
     * classes of the various instances one needs for  HSSF and XSSF.
     */
    public CreationHelper getCreationHelper()
    {
        return _wb.getCreationHelper();
    }

    /**
     * @return <code>false</code> if this workbook is not visible in the GUI
     */
    public boolean isHidden()
    {
        return _wb.isHidden();
    }

    /**
     * @param hiddenFlag pass <code>false</code> to make the workbook visible in the GUI
     */
    public void setHidden(boolean hiddenFlag)
    {
        _wb.setHidden(hiddenFlag);
    }

    /**
     * Check whether a sheet is hidden.
     * <p>
     * Note that a sheet could instead be set to be very hidden, which is different
     *  ({@link #isSheetVeryHidden(int)})
     * </p>
     * @param sheetIx Number
     * @return <code>true</code> if sheet is hidden
     */
    public boolean isSheetHidden(int sheetIx)
    {
        return _wb.isSheetHidden(sheetIx);
    }

    /**
     * Check whether a sheet is very hidden.
     * <p>
     * This is different from the normal hidden status
     *  ({@link #isSheetHidden(int)})
     * </p>
     * @param sheetIx sheet index to check
     * @return <code>true</code> if sheet is very hidden
     */
    public boolean isSheetVeryHidden(int sheetIx)
    {
        return _wb.isSheetVeryHidden(sheetIx);
    }

    /**
     * Hide or unhide a sheet
     *
     * @param sheetIx the sheet index (0-based)
     * @param hidden True to mark the sheet as hidden, false otherwise
     */
    public void setSheetHidden(int sheetIx, boolean hidden)
    {
        _wb.setSheetHidden(sheetIx,hidden);
    }

    /**
     * Hide or unhide a sheet.
     * 
     * <ul>
     *  <li>0 - visible. </li>
     *  <li>1 - hidden. </li>
     *  <li>2 - very hidden.</li>
     * </ul>
     * @param sheetIx the sheet index (0-based)
     * @param hidden one of the following <code>Workbook</code> constants:
     *        <code>Workbook.SHEET_STATE_VISIBLE</code>,
     *        <code>Workbook.SHEET_STATE_HIDDEN</code>, or
     *        <code>Workbook.SHEET_STATE_VERY_HIDDEN</code>.
     * @throws IllegalArgumentException if the supplied sheet index or state is invalid
     */
    public void setSheetHidden(int sheetIx, int hidden)
    {
        _wb.setSheetHidden(sheetIx,hidden);
    }
    /**
     * Register a new toolpack in this workbook.
     *
     * @param toopack the toolpack to register
     */
    public void addToolPack(UDFFinder toopack)
    {
        _wb.addToolPack(toopack);
    }
//end of interface implementation
}
