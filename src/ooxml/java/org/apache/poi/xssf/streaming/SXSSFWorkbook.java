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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipArchiveThresholdInputStream;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.openxml4j.util.ZipFileZipEntrySource;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.SheetVisibility;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.Removal;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFChartSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Streaming version of XSSFWorkbook implementing the "BigGridDemo" strategy.
 *
 * This allows to write very large files without running out of memory as only
 * a configurable portion of the rows are kept in memory at any one time.
 *
 * You can provide a template workbook which is used as basis for the written
 * data.
 *
 * See https://poi.apache.org/spreadsheet/how-to.html#sxssf for details.
 *
 * Please note that there are still things that still may consume a large
 * amount of memory based on which features you are using, e.g. merged regions,
 * comments, ... are still only stored in memory and thus may require a lot of
 * memory if used extensively.
 *
 * SXSSFWorkbook defaults to using inline strings instead of a shared strings
 * table. This is very efficient, since no document content needs to be kept in
 * memory, but is also known to produce documents that are incompatible with
 * some clients. With shared strings enabled all unique strings in the document
 * has to be kept in memory. Depending on your document content this could use
 * a lot more resources than with shared strings disabled.
 *
 * Carefully review your memory budget and compatibility needs before deciding
 * whether to enable shared strings or not.
 */
public class SXSSFWorkbook implements Workbook {
    /**
     * Specifies how many rows can be accessed at most via {@link SXSSFSheet#getRow}.
     * When a new node is created via {@link SXSSFSheet#createRow} and the total number
     * of unflushed records would exceed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via {@link SXSSFSheet#getRow} anymore.
     */
    public static final int DEFAULT_WINDOW_SIZE = 100;
    private static final POILogger logger = POILogFactory.getLogger(SXSSFWorkbook.class);

    private final XSSFWorkbook _wb;

    private final Map<SXSSFSheet,XSSFSheet> _sxFromXHash = new HashMap<>();
    private final Map<XSSFSheet,SXSSFSheet> _xFromSxHash = new HashMap<>();

    private int _randomAccessWindowSize = DEFAULT_WINDOW_SIZE;

    /**
     * whether temp files should be compressed.
     */
    private boolean _compressTmpFiles;

    /**
     * shared string table - a cache of strings in this workbook
     */
    private final SharedStringsTable _sharedStringSource;

    /**
     * Construct a new workbook with default row window size
     */
    public SXSSFWorkbook(){
    	this(null /*workbook*/);
    }

    /**
     * <p>Construct a workbook from a template.</p>
     * 
     * There are three use-cases to use SXSSFWorkbook(XSSFWorkbook) :
     * <ol>
     *   <li>
     *       Append new sheets to existing workbooks. You can open existing
     *       workbook from a file or create on the fly with XSSF.
     *   </li>
     *   <li>
     *       Append rows to existing sheets. The row number MUST be greater
     *       than {@code max(rownum)} in the template sheet.
     *   </li>
     *   <li>
     *       Use existing workbook as a template and re-use global objects such
     *       as cell styles, formats, images, etc.
     *   </li>
     * </ol>
     * All three use cases can work in a combination.
     * 
     * What is not supported:
     * <ul>
     *   <li>
     *   Access initial cells and rows in the template. After constructing
     *   all internal windows are empty and {@link SXSSFSheet#getRow} and
     *   {@link SXSSFRow#getCell} return <code>null</code>.
     *   </li>
     *   <li>
     *    Override existing cells and rows. The API silently allows that but
     *    the output file is invalid and Excel cannot read it.
     *   </li>
     * </ul>
     *
     * @param workbook  the template workbook
     */
    public SXSSFWorkbook(XSSFWorkbook workbook){
    	this(workbook, DEFAULT_WINDOW_SIZE);
    }
    

    /**
     * Constructs an workbook from an existing workbook.
     * <p>
     * When a new node is created via {@link SXSSFSheet#createRow} and the total number
     * of unflushed records would exceed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via {@link SXSSFSheet#getRow} anymore.
     * </p>
     * <p>
     * A value of <code>-1</code> indicates unlimited access. In this case all
     * records that have not been flushed by a call to <code>flush()</code> are available
     * for random access.
     * </p>
     * <p>
     * A value of <code>0</code> is not allowed because it would flush any newly created row
     * without having a chance to specify any cells.
     * </p>
     *
     * @param rowAccessWindowSize the number of rows that are kept in memory until flushed out, see above.
     */
    public SXSSFWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize){
    	this(workbook,rowAccessWindowSize, false);
    }

    /**
     * Constructs an workbook from an existing workbook.
     * <p>
     * When a new node is created via {@link SXSSFSheet#createRow} and the total number
     * of unflushed records would exceed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via {@link SXSSFSheet#getRow} anymore.
     * </p>
     * <p>
     * A value of <code>-1</code> indicates unlimited access. In this case all
     * records that have not been flushed by a call to <code>flush()</code> are available
     * for random access.
     * </p>
     * <p>
     * A value of <code>0</code> is not allowed because it would flush any newly created row
     * without having a chance to specify any cells.
     * </p>
     *
     * @param rowAccessWindowSize the number of rows that are kept in memory until flushed out, see above.
     * @param compressTmpFiles whether to use gzip compression for temporary files
     */
    public SXSSFWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize, boolean compressTmpFiles){
    	this(workbook,rowAccessWindowSize, compressTmpFiles, false);
    }

    /**
     * Constructs an workbook from an existing workbook.
     * <p>
     * When a new node is created via {@link SXSSFSheet#createRow} and the total number
     * of unflushed records would exceed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via {@link SXSSFSheet#getRow} anymore.
     * </p>
     * <p>
     * A value of <code>-1</code> indicates unlimited access. In this case all
     * records that have not been flushed by a call to <code>flush()</code> are available
     * for random access.
     * </p>
     * <p>
     * A value of <code>0</code> is not allowed because it would flush any newly created row
     * without having a chance to specify any cells.
     * </p>
     *
     * @param workbook  the template workbook
     * @param rowAccessWindowSize the number of rows that are kept in memory until flushed out, see above.
     * @param compressTmpFiles whether to use gzip compression for temporary files
     * @param useSharedStringsTable whether to use a shared strings table
     */
    public SXSSFWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize, boolean compressTmpFiles, boolean useSharedStringsTable){
        setRandomAccessWindowSize(rowAccessWindowSize);
        setCompressTempFiles(compressTmpFiles);
        if (workbook == null) {
            _wb=new XSSFWorkbook();
            _sharedStringSource = useSharedStringsTable ? _wb.getSharedStringSource() : null;
        } else {
            _wb=workbook;
            _sharedStringSource = useSharedStringsTable ? _wb.getSharedStringSource() : null;
            for ( Sheet sheet : _wb ) {
                createAndRegisterSXSSFSheet( (XSSFSheet)sheet );
            }
        }
    }
    /**
     * Construct an empty workbook and specify the window for row access.
     * <p>
     * When a new node is created via {@link SXSSFSheet#createRow} and the total number
     * of unflushed records would exceed the specified value, then the
     * row with the lowest index value is flushed and cannot be accessed
     * via {@link SXSSFSheet#getRow} anymore.
     * </p>
     * <p>
     * A value of <code>-1</code> indicates unlimited access. In this case all
     * records that have not been flushed by a call to <code>flush()</code> are available
     * for random access.
     * </p>
     * <p>
     * A value of <code>0</code> is not allowed because it would flush any newly created row
     * without having a chance to specify any cells.
     * </p>
     *
     * @param rowAccessWindowSize the number of rows that are kept in memory until flushed out, see above.
     */
    public SXSSFWorkbook(int rowAccessWindowSize){
    	this(null /*workbook*/, rowAccessWindowSize);
    }

    /**
     * See the constructors for a more detailed description of the sliding window of rows.
     *
     * @return The number of rows that are kept in memory at once before flushing them out.
     */
    public int getRandomAccessWindowSize() {
    	return _randomAccessWindowSize;
    }

    private void setRandomAccessWindowSize(int rowAccessWindowSize) {
        if(rowAccessWindowSize == 0 || rowAccessWindowSize < -1) {
            throw new IllegalArgumentException("rowAccessWindowSize must be greater than 0 or -1");
        }
        _randomAccessWindowSize = rowAccessWindowSize;
    }

    /**
     * Get whether temp files should be compressed.
     *
     * @return whether to compress temp files
     */
    public boolean isCompressTempFiles() {
        return _compressTmpFiles;
    }
    /**
     * Set whether temp files should be compressed.
     * <p>
     *   SXSSF writes sheet data in temporary files (a temp file per-sheet)
     *   and the size of these temp files can grow to to a very large size,
     *   e.g. for a 20 MB csv data the size of the temp xml file become few GB large.
     *   If the "compress" flag is set to <code>true</code> then the temporary XML is gzipped.
     * </p>
     * <p>
     *     Please note the the "compress" option may cause performance penalty.
     * </p>
     * <p>
     *     Setting this option only affects compression for subsequent <code>createSheet()</code> 
     *     calls.
     * </p>
     * @param compress whether to compress temp files
     */
    public void setCompressTempFiles(boolean compress) {
        _compressTmpFiles = compress;
    }
    
    @Internal
    protected SharedStringsTable getSharedStringSource() {
        return _sharedStringSource;
    }

    protected SheetDataWriter createSheetDataWriter() throws IOException {
        if(_compressTmpFiles) {
            return new GZIPSheetDataWriter(_sharedStringSource);
        }
        
        return new SheetDataWriter(_sharedStringSource);
    }

    XSSFSheet getXSSFSheet(SXSSFSheet sheet)
    {
        return _sxFromXHash.get(sheet);
    }

    SXSSFSheet getSXSSFSheet(XSSFSheet sheet)
    {
        return _xFromSxHash.get(sheet);
    }

    void registerSheetMapping(SXSSFSheet sxSheet,XSSFSheet xSheet)
    {
        _sxFromXHash.put(sxSheet,xSheet);
        _xFromSxHash.put(xSheet,sxSheet);
    }

    void deregisterSheetMapping(XSSFSheet xSheet)
    {
        SXSSFSheet sxSheet=getSXSSFSheet(xSheet);
        
        // ensure that the writer is closed in all cases to not have lingering writers
        try {
            sxSheet.getSheetDataWriter().close();
        } catch (IOException e) {
            // ignore exception here
        }
        
        _sxFromXHash.remove(sxSheet);

        _xFromSxHash.remove(xSheet);
    }

    private XSSFSheet getSheetFromZipEntryName(String sheetRef)
    {
        for(XSSFSheet sheet : _sxFromXHash.values())
        {
            if(sheetRef.equals(sheet.getPackagePart().getPartName().getName().substring(1))) {
                return sheet;
            }
        }
        return null;
    }

    protected void injectData(ZipEntrySource zipEntrySource, OutputStream out) throws IOException {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(out);
        try {
            Enumeration<? extends ZipArchiveEntry> en = zipEntrySource.getEntries();
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                zos.putArchiveEntry(new ZipArchiveEntry(ze.getName()));
                try (final InputStream is = zipEntrySource.getInputStream(ze)) {
                    if (is instanceof ZipArchiveThresholdInputStream) {
                        // #59743 - disable Threshold handling for SXSSF copy
                        // as users tend to put too much repetitive data in when using SXSSF :)
                        ((ZipArchiveThresholdInputStream)is).setGuardState(false);
                    }
                    XSSFSheet xSheet = getSheetFromZipEntryName(ze.getName());
                    // See bug 56557, we should not inject data into the special ChartSheets
                    if (xSheet != null && !(xSheet instanceof XSSFChartSheet)) {
                        SXSSFSheet sxSheet = getSXSSFSheet(xSheet);
                        try (InputStream xis = sxSheet.getWorksheetXMLInputStream()) {
                            copyStreamAndInjectWorksheet(is, zos, xis);
                        }
                    } else {
                        IOUtils.copy(is, zos);
                    }
                } finally {
                    zos.closeArchiveEntry();
                }
            }
        } finally {
            zos.finish();
            zipEntrySource.close();
        }
    }

    private static void copyStreamAndInjectWorksheet(InputStream in, OutputStream out, InputStream worksheetData) throws IOException {
        InputStreamReader inReader = new InputStreamReader(in, StandardCharsets.UTF_8);
        OutputStreamWriter outWriter = new OutputStreamWriter(out, StandardCharsets.UTF_8);
        boolean needsStartTag = true;
        int c;
        int pos=0;
        String s="<sheetData";
        int n=s.length();
        //Copy from "in" to "out" up to the string "<sheetData/>" or "</sheetData>" (excluding).
        while(((c=inReader.read())!=-1))
        {
            if(c==s.charAt(pos))
            {
                pos++;
                if(pos==n)
                {
                	if ("<sheetData".equals(s))
                	{
                    	c = inReader.read();
                    	if (c == -1)
                    	{
                    		outWriter.write(s);
                    		break;
                    	}
                    	if (c == '>')
                    	{
                    		// Found <sheetData>
                    		outWriter.write(s);
                    		outWriter.write(c);
                    		s = "</sheetData>";
                    		n = s.length();
                    		pos = 0;
                    		needsStartTag = false;
                    		continue;
                    	}
                    	if (c == '/')
                    	{
                    		// Found <sheetData/
                        	c = inReader.read();
                        	if (c == -1)
                        	{
                        		outWriter.write(s);
                        		break;
                        	}
                        	if (c == '>')
                        	{
                        		// Found <sheetData/>
                        		break;
                        	}
                        	
                    		outWriter.write(s);
                    		outWriter.write('/');
                    		outWriter.write(c);
                    		pos = 0;
                    		continue;
                    	}
                    	
                		outWriter.write(s);
                		outWriter.write('/');
                		outWriter.write(c);
                		pos = 0;
                		continue;
                	}
                	else
                	{
                		// Found </sheetData>
                    	break;
                	}
                }
            }
            else
            {
                if(pos>0) {
                    outWriter.write(s,0,pos);
                }
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
        if (needsStartTag)
        {
        	outWriter.write("<sheetData>\n");
        	outWriter.flush();
        }
        //Copy the worksheet data to "out".
        IOUtils.copy(worksheetData,out);
        outWriter.write("</sheetData>");
        outWriter.flush();
        //Copy the rest of "in" to "out".
        while(((c=inReader.read())!=-1)) {
            outWriter.write(c);
        }
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
    @Override
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
    @Override
    public void setActiveSheet(int sheetIndex)
    {
        _wb.setActiveSheet(sheetIndex);
    }

    /**
     * Gets the first tab that is displayed in the list of tabs in excel.
     *
     * @return the first tab that to display in the list of tabs (0-based).
     */
    @Override
    public int getFirstVisibleTab()
    {
        return _wb.getFirstVisibleTab();
    }

    /**
     * Sets the first tab that is displayed in the list of tabs in excel.
     *
     * @param sheetIndex the first tab that to display in the list of tabs (0-based)
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public int getSheetIndex(Sheet sheet)
    {
        return _wb.getSheetIndex(getXSSFSheet((SXSSFSheet)sheet));
    }

    /**
     * Sreate an Sheet for this Workbook, adds it to the sheets and returns
     * the high level representation.  Use this to create new sheets.
     *
     * @return Sheet representing the new sheet.
     */
    @Override
    public SXSSFSheet createSheet()
    {
        return createAndRegisterSXSSFSheet(_wb.createSheet());
    }

    SXSSFSheet createAndRegisterSXSSFSheet(XSSFSheet xSheet)
    {
        final SXSSFSheet sxSheet;
        try
        {
            sxSheet=new SXSSFSheet(this,xSheet);
        }
        catch (IOException ioe)
        {
            throw new RuntimeException(ioe);
        }
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
    @Override
    public SXSSFSheet createSheet(String sheetname)
    {
        return createAndRegisterSXSSFSheet(_wb.createSheet(sheetname));
    }

    /**
     * <i>Not implemented for SXSSFWorkbook</i>
     *
     * Create an Sheet from an existing sheet in the Workbook.
     *
     * @return Sheet representing the cloned sheet.
     */
    @Override
    @NotImplemented
    public Sheet cloneSheet(int sheetNum) {
        throw new RuntimeException("Not Implemented");
    }


    /**
     * Get the number of spreadsheets in the workbook
     *
     * @return the number of sheets
     */
    @Override
    public int getNumberOfSheets()
    {
        return _wb.getNumberOfSheets();
    }
    
    /**
     *  Returns an iterator of the sheets in the workbook
     *  in sheet order. Includes hidden and very hidden sheets.
     *
     * @return an iterator of the sheets.
     */
    @Override
    public Iterator<Sheet> sheetIterator() {
        return new SheetIterator<>();
    }
    
    private final class SheetIterator<T extends Sheet> implements Iterator<T> {
        final private Iterator<XSSFSheet> it;
        @SuppressWarnings("unchecked")
        public SheetIterator() {
            it = (Iterator<XSSFSheet>)(Iterator<? extends Sheet>) _wb.iterator();
        }
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }
        @Override
        @SuppressWarnings("unchecked")
        public T next() throws NoSuchElementException {
            final XSSFSheet xssfSheet = it.next();
            return (T) getSXSSFSheet(xssfSheet);
        }
        /**
         * Unexpected behavior may occur if sheets are reordered after iterator
         * has been created. Support for the remove method may be added in the future
         * if someone can figure out a reliable implementation.
         */
        @Override
        public void remove() throws IllegalStateException {
            throw new UnsupportedOperationException("remove method not supported on XSSFWorkbook.iterator(). "+
                    "Use Sheet.removeSheetAt(int) instead.");
        }
    }
    
    /**
     * Alias for {@link #sheetIterator()} to allow
     * foreach loops
     */
    @Override
    public Iterator<Sheet> iterator() {
        return sheetIterator();
    }

    /**
     * Get the Sheet object at the given index.
     *
     * @param index of the sheet number (0-based physical and logical)
     * @return Sheet at the provided index
     */
    @Override
    public SXSSFSheet getSheetAt(int index)
    {
        return getSXSSFSheet(_wb.getSheetAt(index));
    }

    /**
     * Get sheet with the given name
     *
     * @param name of the sheet
     * @return Sheet with the name provided or <code>null</code> if it does not exist
     */
    @Override
    public SXSSFSheet getSheet(String name)
    {
        return getSXSSFSheet(_wb.getSheet(name));
    }

    /**
     * Removes sheet at the given index
     *
     * @param index of the sheet to remove (0-based)
     */
    @Override
    public void removeSheetAt(int index)
    {
        // Get the sheet to be removed
        XSSFSheet xSheet = _wb.getSheetAt(index);
        SXSSFSheet sxSheet = getSXSSFSheet(xSheet);
        
        // De-register it
        _wb.removeSheetAt(index);
        deregisterSheetMapping(xSheet);
        
        // Clean up temporary resources
        try {
            sxSheet.dispose();
        } catch (IOException e) {
            logger.log(POILogger.WARN, e);
        }
    }

    /**
     * Create a new Font and add it to the workbook's font table
     *
     * @return new font object
     */
    @Override
    public Font createFont()
    {
        return _wb.createFont();
    }
    
    /**
     * Finds a font that matches the one with the supplied attributes
     *
     * @return the font with the matched attributes or <code>null</code>
     */
    @Override
    public Font findFont(boolean bold, short color, short fontHeight, String name, boolean italic, boolean strikeout, short typeOffset, byte underline)
    {
        return _wb.findFont(bold, color, fontHeight, name, italic, strikeout, typeOffset, underline);
    }

    @Override
    @Deprecated
    public short getNumberOfFonts() {
        return (short)getNumberOfFontsAsInt();
    }

    @Override
    public int getNumberOfFontsAsInt()
    {
        return _wb.getNumberOfFontsAsInt();
    }

    @Override
    @Deprecated
    public Font getFontAt(short idx)
    {
        return getFontAt((int)idx);
    }

    @Override
    public Font getFontAt(int idx)
    {
        return _wb.getFontAt(idx);
    }

    /**
     * Create a new Cell style and add it to the workbook's style table
     *
     * @return the new Cell Style object
     */
    @Override
    public CellStyle createCellStyle()
    {
        return _wb.createCellStyle();
    }

    /**
     * Get the number of styles the workbook contains
     *
     * @return count of cell styles
     */
    @Override
    public int getNumCellStyles()
    {
        return _wb.getNumCellStyles();
    }

    /**
     * Get the cell style object at the given index
     *
     * @param idx  index within the set of styles (0-based)
     * @return CellStyle object at the index
     */
    @Override
    public CellStyle getCellStyleAt(int idx)
    {
        return _wb.getCellStyleAt(idx);
    }

    /**
     * Closes the underlying {@link XSSFWorkbook} and {@link OPCPackage} 
     *  on which this Workbook is based, if any.
     *
     * <p>Once this has been called, no further
     *  operations, updates or reads should be performed on the
     *  Workbook.
     */
    @Override
    public void close() throws IOException {
        // ensure that any lingering writer is closed
        for (SXSSFSheet sheet : _xFromSxHash.values())
        {
            try {
                sheet.getSheetDataWriter().close();
            } catch (IOException e) {
                logger.log(POILogger.WARN,
                        "An exception occurred while closing sheet data writer for sheet "
                        + sheet.getSheetName() + ".", e);
            }
        }

        
        // Tell the base workbook to close, does nothing if 
        //  it's a newly created one
        _wb.close();
    }
    
    /**
     * Write out this workbook to an OutputStream.
     *
     * @param stream - the java OutputStream you wish to write to
     * @exception IOException if anything can't be written.
     */
    @Override
    public void write(OutputStream stream) throws IOException {
        flushSheets();

        //Save the template
        File tmplFile = TempFile.createTempFile("poi-sxssf-template", ".xlsx");
        boolean deleted;
        try {
            try (FileOutputStream os = new FileOutputStream(tmplFile)) {
                _wb.write(os);
            }

            //Substitute the template entries with the generated sheet data files
            try (ZipSecureFile zf = new ZipSecureFile(tmplFile);
                 ZipFileZipEntrySource source = new ZipFileZipEntrySource(zf)) {
                injectData(source, stream);
            }
        } finally {
            deleted = tmplFile.delete();
        }
        if(!deleted) {
            throw new IOException("Could not delete temporary file after processing: " + tmplFile);
        }
    }
    
    protected void flushSheets() throws IOException {
        for (SXSSFSheet sheet : _xFromSxHash.values())
        {
            sheet.flushRows();
        }
    }
    
    /**
     * Dispose of temporary files backing this workbook on disk.
     * Calling this method will render the workbook unusable.
     * @return true if all temporary files were deleted successfully.
     */
    public boolean dispose()
    {
        boolean success = true;
        for (SXSSFSheet sheet : _sxFromXHash.keySet())
        {
            try {
                success = sheet.dispose() && success;
            } catch (IOException e) {
                logger.log(POILogger.WARN, e);
                success = false;
            }
        }
        return success;
    }

    /**
     * @return the total number of defined names in this workbook
     */
    @Override
    public int getNumberOfNames()
    {
        return _wb.getNumberOfNames();
    }

    /**
     * @param name the name of the defined name
     * @return the defined name with the specified name. <code>null</code> if not found.
     */
    @Override
    public Name getName(String name)
    {
        return _wb.getName(name);
    }

    /**
     * Returns all defined names with the given name.
     *
     * @param name the name of the defined name
     * @return a list of the defined names with the specified name. An empty list is returned if none is found.
     */
    @Override
    public List<? extends Name> getNames(String name) {
        return _wb.getNames(name);
    }

    /**
     * Returns all defined names
     *
     * @return all defined names
     */
    @Override
    public List<? extends Name> getAllNames()
    {
        return _wb.getAllNames();
    }

    /**
     * Creates a new (uninitialised) defined name in this workbook
     *
     * @return new defined name object
     */
    @Override
    public Name createName()
    {
        return _wb.createName();
    }

    /**
     * Remove the given defined name
     *
     * @param name the name to remove
     */
    @Override
    public void removeName(Name name)
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
    @Override
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
    @Override
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
    @Override
    public String getPrintArea(int sheetIndex)
    {
        return _wb.getPrintArea(sheetIndex);
    }

    /**
     * Delete the printarea for the sheet specified
     *
     * @param sheetIndex Zero-based sheet index (0 = First Sheet)
     */
    @Override
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
    @Override
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
    @Override
    public void setMissingCellPolicy(MissingCellPolicy missingCellPolicy)
    {
        _wb.setMissingCellPolicy(missingCellPolicy);
    }

    /**
     * Returns the instance of DataFormat for this workbook.
     *
     * @return the DataFormat object
     */
    @Override
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
    @Override
    public int addPicture(byte[] pictureData, int format)
    {
        return _wb.addPicture(pictureData,format);
    }

    /**
     * Gets all pictures from the Workbook.
     *
     * @return the list of pictures (a list of {@link PictureData} objects.)
     */
    @Override
    public List<? extends PictureData> getAllPictures()
    {
        return _wb.getAllPictures();
    }

    /**
     * Returns an object that handles instantiating concrete
     *  classes of the various instances one needs for HSSF, XSSF
     *  and SXSSF.
     */
    @Override
    public CreationHelper getCreationHelper() {
        return new SXSSFCreationHelper(this);
    }

    protected boolean isDate1904() {
        return _wb.isDate1904();
    }
    
    @Override
    @NotImplemented("XSSFWorkbook#isHidden is not implemented")
    public boolean isHidden()
    {
        return _wb.isHidden();
    }

    @Override
    @NotImplemented("XSSFWorkbook#setHidden is not implemented")
    public void setHidden(boolean hiddenFlag)
    {
        _wb.setHidden(hiddenFlag);
    }

    @Override
    public boolean isSheetHidden(int sheetIx)
    {
        return _wb.isSheetHidden(sheetIx);
    }

    @Override
    public boolean isSheetVeryHidden(int sheetIx)
    {
        return _wb.isSheetVeryHidden(sheetIx);
    }
    
    @Override
    public SheetVisibility getSheetVisibility(int sheetIx) {
        return _wb.getSheetVisibility(sheetIx);
    }

    @Override
    public void setSheetHidden(int sheetIx, boolean hidden)
    {
        _wb.setSheetHidden(sheetIx,hidden);
    }

    @Override
    public void setSheetVisibility(int sheetIx, SheetVisibility visibility) {
        _wb.setSheetVisibility(sheetIx, visibility);
    }

    /**
     * @param nameIndex position of the named range (0-based)
     * @return the defined name at the specified index
     * @throws IllegalArgumentException if the supplied index is invalid
     * @deprecated 3.16. New projects should avoid accessing named ranges by index.
     */
    @Override
    @Deprecated
    @Removal(version="3.20")
    public Name getNameAt(int nameIndex) {
        //noinspection deprecation
        return _wb.getNameAt(nameIndex);
    }

    /**
     * Gets the defined name index by name
     *
     * <i>Note:</i> Excel defined names are case-insensitive and
     * this method performs a case-insensitive search.
     *
     * @param name the name of the defined name
     * @return zero based index of the defined name. <code>-1</code> if not found.
     *
     * @deprecated 3.16. New projects should avoid accessing named ranges by index.
     * Use {@link #getName(String)} instead.
     */
    @Override
    @Deprecated
    @Removal(version="3.20")
    public int getNameIndex(String name) {
        //noinspection deprecation
        return _wb.getNameIndex(name);
    }

    /**
     * Remove the defined name at the specified index
     * @param index named range index (0 based)
     *
     * @deprecated 3.16. New projects should use {@link #removeName(Name)}.
     */
    @Override
    @Deprecated
    @Removal(version="3.20")
    public void removeName(int index) {
        //noinspection deprecation
        _wb.removeName(index);
    }

    /**
     * Remove a defined name by name
     *
     * @param name the name of the defined name
     *
     * @deprecated 3.16. New projects should use {@link #removeName(Name)}.
     */
    @Override
    @Deprecated
    @Removal(version="3.20")
    public void removeName(String name) {
        //noinspection deprecation
        _wb.removeName(name);
    }
    
    /**
     * <i>Not implemented for SXSSFWorkbook</i>
     *
     * Adds the LinkTable records required to allow formulas referencing
     *  the specified external workbook to be added to this one. Allows
     *  formulas such as "[MyOtherWorkbook]Sheet3!$A$5" to be added to the 
     *  file, for workbooks not already referenced.
     *
     *  Note: this is not implemented and thus currently throws an Exception stating this.
     *
     * @param name The name the workbook will be referenced as in formulas
     * @param workbook The open workbook to fetch the link required information from
     *
     * @throws RuntimeException stating that this method is not implemented yet.
     */
    @Override
    @NotImplemented
    public int linkExternalWorkbook(String name, Workbook workbook) {
        throw new RuntimeException("Not Implemented");
    }
    
    /**
     * Register a new toolpack in this workbook.
     *
     * @param toopack the toolpack to register
     */
    @Override
    public void addToolPack(UDFFinder toopack)
    {
        _wb.addToolPack(toopack);
    }

    /**
     * Whether the application shall perform a full recalculation when the workbook is opened.
     * <p>
     * Typically you want to force formula recalculation when you modify cell formulas or values
     * of a workbook previously created by Excel. When set to 0, this flag will tell Excel
     * that it needs to recalculate all formulas in the workbook the next time the file is opened.
     * </p>
     *
     * @param value true if the application will perform a full recalculation of
     * workbook values when the workbook is opened
     * @since 3.8
     */
    @Override
    public void setForceFormulaRecalculation(boolean value){
        _wb.setForceFormulaRecalculation(value);
    }

    /**
     * Whether Excel will be asked to recalculate all formulas when the  workbook is opened.
     */
    @Override
    public boolean getForceFormulaRecalculation(){
        return _wb.getForceFormulaRecalculation();
    }

    /**
     * Returns the spreadsheet version (EXCLE2007) of this workbook
     * 
     * @return EXCEL2007 SpreadsheetVersion enum
     * @since 3.14 beta 2
     */
    @Override
    public SpreadsheetVersion getSpreadsheetVersion() {
        return SpreadsheetVersion.EXCEL2007;
    }

    @Override
    public int addOlePackage(byte[] oleData, String label, String fileName, String command) throws IOException {
        return _wb.addOlePackage(oleData, label, fileName, command);
    }
    
//end of interface implementation
}
