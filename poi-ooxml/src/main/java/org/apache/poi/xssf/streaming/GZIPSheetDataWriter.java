/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.xssf.streaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.poi.util.Removal;
import org.apache.poi.util.TempFile;
import org.apache.poi.xssf.model.SharedStringsTable;

/**
 * Sheet writer that supports gzip compression of the temp files.
 */
public class GZIPSheetDataWriter extends SheetDataWriter {

    public GZIPSheetDataWriter() throws IOException {
        super();
    }
    
    /**
     * @param sharedStringsTable the shared strings table, or null if inline text is used
     */
    public GZIPSheetDataWriter(SharedStringsTable sharedStringsTable) throws IOException {
        super(sharedStringsTable);
    }

    /**
     * @return temp file to write sheet data
     * @deprecated no need for this be public, will be made private or protected in an upcoming release
     */
    @Override
    @Deprecated
    @Removal(version = "6.0.0")
    public File createTempFile() throws IOException {
        return TempFile.createTempFile("poi-sxssf-sheet-xml", ".gz");
    }

    @Override
    protected InputStream decorateInputStream(FileInputStream fis) throws IOException {
        return new GZIPInputStream(fis);
    }

    @Override
    protected OutputStream decorateOutputStream(FileOutputStream fos) throws IOException {
        return new GZIPOutputStream(fos);
    }

}
