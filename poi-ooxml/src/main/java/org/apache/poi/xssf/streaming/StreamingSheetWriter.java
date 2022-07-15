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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.util.Beta;

/**
 * Unlike SheetDataWriter, this writer does not create a temporary file, it writes data directly
 * to the provided OutputStream.
 * @since 5.0.0
 */
@Beta
public class StreamingSheetWriter extends SheetDataWriter {
    private static final Logger LOG = LogManager.getLogger(StreamingSheetWriter.class);
    private boolean closed = false;

    public StreamingSheetWriter() throws IOException {
        throw new RuntimeException("StreamingSheetWriter requires OutputStream");
    }

    public StreamingSheetWriter(OutputStream out) throws IOException {
        super(createWriter(out));
        LOG.atDebug().log("Preparing SXSSF sheet writer");
    }

    @Override
    public File createTempFile() throws IOException {
        throw new RuntimeException("Not supported with StreamingSheetWriter");
    }

    @Override
    public Writer createWriter(File fd) throws IOException {
        throw new RuntimeException("Not supported with StreamingSheetWriter");
    }

    /**
     * Create a writer for the sheet data.
     *
     * @param out the output stream to write to
     */
    protected static Writer createWriter(OutputStream out) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            _out.flush();
        }
    }

    @Override
    public InputStream getWorksheetXMLInputStream() throws IOException {
        throw new RuntimeException("Not supported with StreamingSheetWriter");
    }

    @Override
    boolean dispose() throws IOException {
        if (!closed) {
            _out.close();
        }
        closed = true;
        return true;
    }
}
