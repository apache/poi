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
import java.io.OutputStream;

import org.apache.poi.util.Beta;
import org.apache.poi.xssf.usermodel.XSSFSheet;

/**
 * A variant of SXSSFSheet that uses a <code>RowGeneratorFunction</code></code> to lazily create rows.
 *
 *  This variant is experimental and APIs may change at short notice.
 *
 * @see DeferredSXSSFWorkbook
 * @see RowGeneratorFunction
 * @since 5.0.0
 */
@Beta
public class DeferredSXSSFSheet extends SXSSFSheet {
    private RowGeneratorFunction rowGenerator;
    
    public DeferredSXSSFSheet(DeferredSXSSFWorkbook workbook, XSSFSheet xSheet) throws IOException {
        super(workbook, xSheet, workbook.getRandomAccessWindowSize());
    }
    
    @Override
    public InputStream getWorksheetXMLInputStream() throws IOException {
        throw new RuntimeException("Not supported by DeferredSXSSFSheet");
    }
    
    public void setRowGenerator(RowGeneratorFunction rowGenerator) {
        this.rowGenerator = rowGenerator;
    }
    
    public void writeRows(OutputStream out) throws IOException {
        // delayed creation of SheetDataWriter
        _writer = ((DeferredSXSSFWorkbook) _workbook).createSheetDataWriter(out);
        try {
            if (this.rowGenerator != null) {
                this.rowGenerator.generateRows(this);
            }
        } catch (Exception e) {
            throw new IOException("Error generating Excel rows", e);
        } finally {
            // flush buffered rows
            flushRows(0);
            // flush writer buffer
            _writer.close();
            out.flush();
        }
    }
}
