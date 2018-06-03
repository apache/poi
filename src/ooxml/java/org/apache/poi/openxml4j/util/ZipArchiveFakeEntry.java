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

package org.apache.poi.openxml4j.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.poi.util.IOUtils;


/**
 * So we can close the real zip entry and still
 *  effectively work with it.
 * Holds the (decompressed!) data in memory, so
 *  close this as soon as you can!
 */
/* package */ class ZipArchiveFakeEntry extends ZipArchiveEntry {
    private final byte[] data;

    ZipArchiveFakeEntry(ZipArchiveEntry entry, InputStream inp) throws IOException {
        super(entry.getName());

        final long entrySize = entry.getSize();

        if (entrySize < -1 || entrySize>=Integer.MAX_VALUE) {
            throw new IOException("ZIP entry size is too large or invalid");
        }

        // Grab the de-compressed contents for later
        data = (entrySize == -1) ? IOUtils.toByteArray(inp) : IOUtils.toByteArray(inp, (int)entrySize);
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }
}
