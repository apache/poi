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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Sheet writer that supports gzip compression of the temp files.
 */
public class GZIPSheetDataWriter extends SheetDataWriter {

    public GZIPSheetDataWriter() throws IOException {
        super();
    }

    /**
     * @return temp file to write sheet data
     */
    @Override
	public File createTempFile()throws IOException {
        File fd = File.createTempFile("poi-sxssf-sheet-xml", ".gz");
        return fd;
    }

    /**
     * @return a wrapped instance of GZIPOutputStream
     */
    @Override
	public Writer createWriter(File fd)throws IOException {
        return new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(fd)));
    }


    /**
     * @return a GZIPInputStream stream to read the compressed temp file
     */
    @Override
	public InputStream getWorksheetXMLInputStream() throws IOException {
        File fd = getTempFile();
        return new GZIPInputStream(new FileInputStream(fd));
    }

}
