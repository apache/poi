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
import java.io.OutputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Beta;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * An variant of SXSSFWorkbook that avoids generating a temporary file and writes data directly to
 * the provided OutputStream.
 * 
 * This variant is experimental and APIs may change at short notice.
 * 
 * @since 5.0.0
 */
@Beta
public class EmittingSXSSFWorkbook extends SXSSFWorkbook {
    private static final POILogger logger = POILogFactory.getLogger(EmittingSXSSFWorkbook.class);
    
    public EmittingSXSSFWorkbook() {
        this(null);
    }
    
    public EmittingSXSSFWorkbook(XSSFWorkbook workbook) {
        this(workbook, SXSSFWorkbook.DEFAULT_WINDOW_SIZE);
    }
    
    public EmittingSXSSFWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize) {
        super(workbook, rowAccessWindowSize, false, false);
    }
    
    @Override
    protected SheetDataWriter createSheetDataWriter() throws IOException {
        throw new RuntimeException("Not supported by EmittingSXSSFWorkbook");
    }
    
    protected StreamingSheetWriter createSheetDataWriter(OutputStream out) throws IOException {
        return new StreamingSheetWriter(out);
    }
    
//    @Override
//    protected ISheetInjector createSheetInjector(InputStream xis) throws IOException
//    protected ISheetInjector createSheetInjector(SXSSFSheet sxSheet) throws IOException {
//        EmittingSXSSFSheet ssxSheet = (EmittingSXSSFSheet) sxSheet;
//        return (output) -> {
//            ssxSheet.writeRows(output);
//        };
//    }
    
    @Override
    SXSSFSheet createAndRegisterSXSSFSheet(XSSFSheet xSheet) {
        final EmittingSXSSFSheet sxSheet;
        try {
            sxSheet = new EmittingSXSSFSheet(this, xSheet);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        registerSheetMapping(sxSheet, xSheet);
        return sxSheet;
    }
    
    public EmittingSXSSFSheet createSheet() {
        return (EmittingSXSSFSheet) super.createSheet();
    }
    
    public EmittingSXSSFSheet createSheet(String sheetname) {
        return (EmittingSXSSFSheet) super.createSheet(sheetname);
    }
    
    /**
     * Returns an iterator of the sheets in the workbook in sheet order. Includes hidden and very hidden sheets.
     *
     * @return an iterator of the sheets.
     */
    @Override
    public Iterator<Sheet> sheetIterator() {
        return new SheetIterator<Sheet>();
    }
    
    private final class SheetIterator<T extends Sheet> implements Iterator<T> {
        final private Iterator<XSSFSheet> it;
        
        @SuppressWarnings("unchecked")
        public SheetIterator() {
            it = (Iterator<XSSFSheet>) (Iterator<? extends Sheet>) _wb.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }
        
        @Override
        @SuppressWarnings("unchecked")
        public T next() throws NoSuchElementException {
            final XSSFSheet xssfSheet = it.next();
            EmittingSXSSFSheet sxSheet = (EmittingSXSSFSheet) getSXSSFSheet(xssfSheet);
            return (T) (sxSheet == null ? xssfSheet : sxSheet);
        }
        
        /**
         * Unexpected behavior may occur if sheets are reordered after iterator has been created. Support for the remove
         * method may be added in the future if someone can figure out a reliable implementation.
         */
        @Override
        public void remove() throws IllegalStateException {
            throw new UnsupportedOperationException("remove method not supported on XSSFWorkbook.iterator(). "
                    + "Use Sheet.removeSheetAt(int) instead.");
        }
    }
    
    /**
     * Alias for {@link #sheetIterator()} to allow foreach loops
     */
    @Override
    public Iterator<Sheet> iterator() {
        return sheetIterator();
    }
    
    @Override
    public SXSSFSheet getSheetAt(int index) {
        throw new RuntimeException("Not supported by EmittingSXSSFWorkbook");
    }
    
    public XSSFSheet getXSSFSheetAt(int index) {
        return _wb.getSheetAt(index);
    }
    
    /**
     * Gets the sheet at the given index for streaming.
     *
     * @param index the index
     * @return the streaming sheet at
     */
    public EmittingSXSSFSheet getStreamingSheetAt(int index) {
        XSSFSheet xSheet = _wb.getSheetAt(index);
        SXSSFSheet sxSheet = getSXSSFSheet(xSheet);
        if (sxSheet == null && xSheet != null) {
            return (EmittingSXSSFSheet) createAndRegisterSXSSFSheet(xSheet);
        } else {
            return (EmittingSXSSFSheet) sxSheet;
        }
    }
    
    @Override
    public SXSSFSheet getSheet(String name) {
        throw new RuntimeException("Not supported by EmittingSXSSFWorkbook");
    }
    
    public XSSFSheet getXSSFSheet(String name) {
        return _wb.getSheet(name);
    }
    
    /**
     * Gets sheet with the given name for streaming.
     *
     * @param name the name
     * @return the streaming sheet
     */
    public EmittingSXSSFSheet getStreamingSheet(String name) {
        XSSFSheet xSheet = _wb.getSheet(name);
        EmittingSXSSFSheet sxSheet = (EmittingSXSSFSheet) getSXSSFSheet(xSheet);
        if (sxSheet == null && xSheet != null) {
            return (EmittingSXSSFSheet) createAndRegisterSXSSFSheet(xSheet);
        } else {
            return sxSheet;
        }
    }
    
    /**
     * Removes sheet at the given index
     *
     * @param index of the sheet to remove (0-based)
     */
    @Override
    public void removeSheetAt(int index) {
        // Get the sheet to be removed
        XSSFSheet xSheet = _wb.getSheetAt(index);
        SXSSFSheet sxSheet = getSXSSFSheet(xSheet);
        
        // De-register it
        _wb.removeSheetAt(index);
        
        // The sheet may not be a streaming sheet and is not mapped
        if (sxSheet != null) {
            deregisterSheetMapping(xSheet);
            
            // Clean up temporary resources
            try {
                sxSheet.dispose();
            } catch (IOException e) {
                logger.log(POILogger.WARN, e);
            }
        }
    }
}
