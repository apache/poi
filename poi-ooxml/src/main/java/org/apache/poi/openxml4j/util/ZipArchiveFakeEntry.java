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

import java.io.*;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.poifs.crypt.temp.EncryptedTempData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;

/**
 * So we can close the real zip entry and still
 *  effectively work with it.
 * Holds the (decompressed!) data in memory (or since POI 5.1.0, possibly in a temp file), so
 *  close this as soon as you can!
 * @see ZipInputStreamZipEntrySource#setThresholdBytesForTempFiles(int)
 */
/* package */ class ZipArchiveFakeEntry extends ZipArchiveEntry implements Closeable {
    private static final Logger LOG = LogManager.getLogger(ZipArchiveFakeEntry.class);

    // how large a single entry in a zip-file should become at max
    // can be overwritten via IOUtils.setByteArrayMaxOverride()
    private static final int DEFAULT_MAX_ENTRY_SIZE = 100_000_000;
    private static int MAX_ENTRY_SIZE = DEFAULT_MAX_ENTRY_SIZE;

    public static void setMaxEntrySize(int maxEntrySize) {
        MAX_ENTRY_SIZE = maxEntrySize;
    }

    public static int getMaxEntrySize() {
       return MAX_ENTRY_SIZE;
    }

    private byte[] data;
    private File tempFile;
    private EncryptedTempData encryptedTempData;

    ZipArchiveFakeEntry(ZipArchiveEntry entry, InputStream inp) throws IOException {
        super(entry.getName());

        final long entrySize = entry.getSize();

        final int threshold = ZipInputStreamZipEntrySource.getThresholdBytesForTempFiles();
        if (threshold >= 0 && entrySize >= threshold) {
            if (ZipInputStreamZipEntrySource.shouldEncryptTempFiles()) {
                encryptedTempData = new EncryptedTempData();
                try (OutputStream os = encryptedTempData.getOutputStream()) {
                    IOUtils.copy(inp, os);
                }
            } else {
                tempFile = TempFile.createTempFile("poi-zip-entry", ".tmp");
                LOG.atInfo().log("created for temp file {} for zip entry {} of size {} bytes",
                        () -> tempFile.getAbsolutePath(), entry::getName, () -> entrySize);
                IOUtils.copy(inp, tempFile);
            }
        } else {
            if (entrySize < -1 || entrySize >= Integer.MAX_VALUE) {
                throw new IOException("ZIP entry size is too large or invalid");
            }

            // Grab the de-compressed contents for later
            data = (entrySize == -1) ? IOUtils.toByteArrayWithMaxLength(inp, getMaxEntrySize()) :
                    IOUtils.toByteArray(inp, (int)entrySize, getMaxEntrySize());
        }
    }

    /**
     * Returns zip entry.
     * @return input stream
     * @throws IOException since POI 5.2.0,
     * an IOException can occur if the optional temp file has been removed (was a RuntimeException in POI 5.1.0)
     * @see ZipInputStreamZipEntrySource#setThresholdBytesForTempFiles(int)
     */
    public InputStream getInputStream() throws IOException {
        if (encryptedTempData != null) {
            try {
                return encryptedTempData.getInputStream();
            } catch (IOException e) {
                throw new IOException("failed to read from encrypted temp data", e);
            }
        } else if (tempFile != null) {
            try {
                return new FileInputStream(tempFile);
            } catch (FileNotFoundException e) {
                throw new IOException("temp file " + tempFile.getAbsolutePath() + " is missing");
            }
        } else if (data != null) {
            return new UnsynchronizedByteArrayInputStream(data);
        } else {
            throw new IOException("Cannot retrieve data from Zip Entry, probably because the Zip Entry was closed before the data was requested.");
        }
    }

    /**
     * Deletes any temp files and releases any byte arrays.
     * @throws IOException If closing the entry fails.
     * @since POI 5.1.0
     */
    @Override
    public void close() throws IOException {
        data = null;
        if (encryptedTempData != null) {
            encryptedTempData.dispose();
        }
        if (tempFile != null && tempFile.exists()) {
            if (!tempFile.delete()) {
                LOG.atDebug().log("temp file was already deleted (probably due to previous call to close this resource)");
            }
        }
    }
}
