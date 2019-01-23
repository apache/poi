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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.poi.openxml4j.util.ZipArchiveThresholdInputStream;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFChartSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * An update of SXSSFWorkbook avoids generating a temporary file and writes data directly to
 * the provided OutputStream.
 * @since 4.1.0
 */
public class SuperSXSSFWorkbook extends SXSSFWorkbook {
    private static final POILogger logger = POILogFactory.getLogger(SuperSXSSFWorkbook.class);
    
    public SuperSXSSFWorkbook() {
        this(null);
    }
    
    public SuperSXSSFWorkbook(XSSFWorkbook workbook) {
        this(workbook, SXSSFWorkbook.DEFAULT_WINDOW_SIZE);
    }
    
    public SuperSXSSFWorkbook(XSSFWorkbook workbook, int rowAccessWindowSize) {
        setRandomAccessWindowSize(rowAccessWindowSize);
        _sharedStringSource = null;
        if (workbook == null) {
            _wb = new XSSFWorkbook();
        } else {
            _wb = workbook;
        }
    }
    
    @Override
    public void setCompressTempFiles(boolean compress) {
        // NOOP
    }
    
    @Override
    protected SheetDataWriter createSheetDataWriter() throws IOException {
        throw new RuntimeException("Not supported by SuperSXSSFWorkbook");
    }
    
    protected StreamingSheetWriter createSheetDataWriter(OutputStream out) throws IOException {
        return new StreamingSheetWriter(out);
    }
    
    @Override
    protected void injectData(ZipEntrySource zipEntrySource, OutputStream out) throws IOException {
        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(out);
        zos.setUseZip64(zip64Mode);
        try {
            Enumeration<? extends ZipArchiveEntry> en = zipEntrySource.getEntries();
            while (en.hasMoreElements()) {
                ZipArchiveEntry ze = en.nextElement();
                ZipArchiveEntry zeOut = new ZipArchiveEntry(ze.getName());
                zeOut.setSize(ze.getSize());
                zeOut.setTime(ze.getTime());
                zos.putArchiveEntry(zeOut);
                try (final InputStream is = zipEntrySource.getInputStream(ze)) {
                    if (is instanceof ZipArchiveThresholdInputStream) {
                        // #59743 - disable Threshold handling for SXSSF copy
                        // as users tend to put too much repetitive data in when using SXSSF :)
                        ((ZipArchiveThresholdInputStream) is).setGuardState(false);
                    }
                    XSSFSheet xSheet = getSheetFromZipEntryName(ze.getName());
                    // See bug 56557, we should not inject data into the special ChartSheets
                    if (xSheet != null && !(xSheet instanceof XSSFChartSheet)) {
                        SuperSXSSFSheet sxSheet = (SuperSXSSFSheet) getSXSSFSheet(xSheet);
                        if (sxSheet != null) {
                            copyStreamAndInjectWorksheet(is, zos, sxSheet);
                        } else {
                            IOUtils.copy(is, zos);
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
    
    private static void copyStreamAndInjectWorksheet(InputStream in, OutputStream out, SuperSXSSFSheet sxSheet)
            throws IOException {
        InputStreamReader inReader = new InputStreamReader(in, "UTF-8");
        OutputStreamWriter outWriter = new OutputStreamWriter(out, "UTF-8");
        boolean needsStartTag = true;
        int c;
        int pos = 0;
        String s = "<sheetData";
        int n = s.length();
        // Copy from "in" to "out" up to the string "<sheetData/>" or "</sheetData>"
        // (excluding).
        while (((c = inReader.read()) != -1)) {
            if (c == s.charAt(pos)) {
                pos++;
                if (pos == n) {
                    if ("<sheetData".equals(s)) {
                        c = inReader.read();
                        if (c == -1) {
                            outWriter.write(s);
                            break;
                        }
                        if (c == '>') {
                            // Found <sheetData>
                            outWriter.write(s);
                            outWriter.write(c);
                            s = "</sheetData>";
                            n = s.length();
                            pos = 0;
                            needsStartTag = false;
                            continue;
                        }
                        if (c == '/') {
                            // Found <sheetData/
                            c = inReader.read();
                            if (c == -1) {
                                outWriter.write(s);
                                break;
                            }
                            if (c == '>') {
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
                    } else {
                        // Found </sheetData>
                        break;
                    }
                }
            } else {
                if (pos > 0) {
                    outWriter.write(s, 0, pos);
                }
                if (c == s.charAt(0)) {
                    pos = 1;
                } else {
                    outWriter.write(c);
                    pos = 0;
                }
            }
        }
        outWriter.flush();
        if (needsStartTag) {
            outWriter.write("<sheetData>\n");
            outWriter.flush();
        }
        
        // Invoke row generation in SXSSFSheet
        sxSheet.writeRows(out);
        
        outWriter.write("</sheetData>");
        outWriter.flush();
        // Copy the rest of "in" to "out".
        while (((c = inReader.read()) != -1)) {
            outWriter.write(c);
        }
        outWriter.flush();
    }
    
    @Override
    SXSSFSheet createAndRegisterSXSSFSheet(XSSFSheet xSheet) {
        final SuperSXSSFSheet sxSheet;
        try {
            sxSheet = new SuperSXSSFSheet(this, xSheet);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        registerSheetMapping(sxSheet, xSheet);
        return sxSheet;
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
            SuperSXSSFSheet sxSheet = (SuperSXSSFSheet) getSXSSFSheet(xssfSheet);
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
        throw new RuntimeException("Not supported by SuperSXSSFWorkbook");
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
    public SXSSFSheet getStreamingSheetAt(int index) {
        XSSFSheet xSheet = _wb.getSheetAt(index);
        SXSSFSheet sxSheet = getSXSSFSheet(xSheet);
        if (sxSheet == null) {
            return createAndRegisterSXSSFSheet(xSheet);
        } else {
            return sxSheet;
        }
    }
    
    @Override
    public SXSSFSheet getSheet(String name) {
        throw new RuntimeException("Not supported by SuperSXSSFWorkbook");
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
    public SuperSXSSFSheet getStreamingSheet(String name) {
        XSSFSheet xSheet = _wb.getSheet(name);
        SuperSXSSFSheet sxSheet = (SuperSXSSFSheet) getSXSSFSheet(xSheet);
        if (sxSheet == null) {
            return (SuperSXSSFSheet) createAndRegisterSXSSFSheet(xSheet);
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
