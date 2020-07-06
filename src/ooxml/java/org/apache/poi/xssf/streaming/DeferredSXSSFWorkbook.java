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

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.Beta;
import org.apache.poi.util.NotImplemented;
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
public class DeferredSXSSFWorkbook extends SXSSFWorkbook {
    private static final POILogger logger = POILogFactory.getLogger(DeferredSXSSFWorkbook.class);
    
    public DeferredSXSSFWorkbook() {
        this(null);
    }

    public DeferredSXSSFWorkbook(int rowAccessWindowSize) { this(null, rowAccessWindowSize); }
    
    public DeferredSXSSFWorkbook(XSSFWorkbook workbook) {
        this(workbook, SXSSFWorkbook.DEFAULT_WINDOW_SIZE);
    }
    
    public DeferredSXSSFWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize) {
        super(workbook, rowAccessWindowSize, false, false);
    }

    @NotImplemented
    @Override
    protected SheetDataWriter createSheetDataWriter() throws IOException {
        throw new RuntimeException("Not supported by DeferredSXSSFWorkbook");
    }
    
    protected StreamingSheetWriter createSheetDataWriter(OutputStream out) throws IOException {
        return new StreamingSheetWriter(out);
    }
    
    @Override
    protected ISheetInjector createSheetInjector(SXSSFSheet sxSheet) throws IOException {
        DeferredSXSSFSheet ssxSheet = (DeferredSXSSFSheet) sxSheet;
        return (output) -> {
            ssxSheet.writeRows(output);
        };
    }
    
    @Override
    SXSSFSheet createAndRegisterSXSSFSheet(XSSFSheet xSheet) {
        final DeferredSXSSFSheet sxSheet;
        try {
            sxSheet = new DeferredSXSSFSheet(this, xSheet);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        registerSheetMapping(sxSheet, xSheet);
        return sxSheet;
    }
    
    public DeferredSXSSFSheet createSheet() {
        return (DeferredSXSSFSheet) super.createSheet();
    }
    
    public DeferredSXSSFSheet createSheet(String sheetname) {
        return (DeferredSXSSFSheet) super.createSheet(sheetname);
    }
    
    /**
     * Returns an iterator of the sheets in the workbook in sheet order. Includes hidden and very hidden sheets.
     *
     * @return an iterator of the sheets.
     */
    @Override
    public Iterator<Sheet> sheetIterator() {
        return new SheetIterator<>();
    }
    
    /**
     * Gets the sheet at the given index for streaming.
     *
     * @param index the index
     * @return the streaming sheet at
     */
    public DeferredSXSSFSheet getStreamingSheetAt(int index) {
        XSSFSheet xSheet = _wb.getSheetAt(index);
        SXSSFSheet sxSheet = getSXSSFSheet(xSheet);
        if (sxSheet == null && xSheet != null) {
            return (DeferredSXSSFSheet) createAndRegisterSXSSFSheet(xSheet);
        } else {
            return (DeferredSXSSFSheet) sxSheet;
        }
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
    public DeferredSXSSFSheet getStreamingSheet(String name) {
        XSSFSheet xSheet = _wb.getSheet(name);
        DeferredSXSSFSheet sxSheet = (DeferredSXSSFSheet) getSXSSFSheet(xSheet);
        if (sxSheet == null && xSheet != null) {
            return (DeferredSXSSFSheet) createAndRegisterSXSSFSheet(xSheet);
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
