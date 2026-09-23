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
import java.nio.file.Files;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
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
public final class ZipArchiveFakeEntry extends ZipArchiveEntry implements Closeable {
    private static final Logger LOG = PoiLogManager.getLogger(ZipArchiveFakeEntry.class);

    // how large a single entry in a zip-file should become at max
    // can be overwritten via IOUtils.setByteArrayMaxOverride()
    private static final int DEFAULT_MAX_ENTRY_SIZE = 100_000_000;
    private static int MAX_ENTRY_SIZE = DEFAULT_MAX_ENTRY_SIZE;

    // cap on the initial read-buffer allocation for entries with a known size. The declared
    // entry size comes from the (untrusted) zip local file header, so we do not eagerly
    // allocate more than this from it - the buffer grows beyond it only as real data arrives.
    // Deliberately well below DEFAULT_MAX_ENTRY_SIZE; entries up to this size (the vast
    // majority) are still read with a single exactly-sized allocation.
    private static final int MAX_INIT_BUFFER_SIZE = 2_000_000;

    /**
     * Set the maximum size of a single entry in a zip-file.
     * @param maxEntrySize number of bytes at which a zip entry is regarded as too large for holding in memory
     *                     - defaults to 100_000_000 (approx 100Mb). A value of -1 means the default value is used.
     */
    public static void setMaxEntrySize(int maxEntrySize) {
        if(maxEntrySize < 0) {
            MAX_ENTRY_SIZE = DEFAULT_MAX_ENTRY_SIZE;
        } else {
            MAX_ENTRY_SIZE = maxEntrySize;
        }
    }

    public static int getMaxEntrySize() {
        final int ioMaxSize = IOUtils.getByteArrayMaxOverride();
        return ioMaxSize < 0 ? MAX_ENTRY_SIZE : Math.min(MAX_ENTRY_SIZE, ioMaxSize);
    }

    private byte[] data;
    private File tempFile;
    private EncryptedTempData encryptedTempData;
    private final long numberOfBytes;

    ZipArchiveFakeEntry(ZipArchiveEntry entry, InputStream inp) throws IOException {
        super(entry.getName());

        final long entrySize = entry.getSize();

        final int threshold = ZipInputStreamZipEntrySource.getThresholdBytesForTempFiles();
        if (threshold >= 0 && (entrySize >= threshold || entrySize == -1)) {
            boolean success = false;
            try {
                final long bytes;
                if (entrySize == -1) {
                    // The entry does not declare its uncompressed size (e.g. it was written
                    // with a data descriptor by a streaming zip writer). Most such entries
                    // are small, so buffer in memory up to the temp-file threshold and only
                    // spill to a temp file if the entry really is that large - an entry that
                    // hides its size cannot force more than the threshold onto the heap, and
                    // small entries no longer cost a temp file each.
                    try (UnsynchronizedByteArrayOutputStream baos = UnsynchronizedByteArrayOutputStream.builder().get()) {
                        final long bytesInMemory = IOUtils.copy(inp, baos, threshold);
                        final int nextByte = bytesInMemory < threshold ? -1 : inp.read();
                        if (nextByte == -1) {
                            data = baos.toByteArray();
                            bytes = data.length;
                        } else {
                            try (OutputStream os = createTempDataOutputStream()) {
                                LOG.atWarn().log("Zip entry {} does not declare its uncompressed size and is larger " +
                                                "than the temp-file threshold of {} bytes - spilling it to {}",
                                        entry.getName(), threshold,
                                        tempFile != null ? tempFile.getAbsolutePath() : "encrypted temp data");
                                baos.writeTo(os);
                                os.write(nextByte);
                                bytes = bytesInMemory + 1 + IOUtils.copy(inp, os);
                            }
                        }
                    }
                } else {
                    try (OutputStream os = createTempDataOutputStream()) {
                        if (tempFile != null) {
                            LOG.atInfo().log("Creating temp file {} for zip entry {} of size {} bytes",
                                    tempFile.getAbsolutePath(), entry.getName(), entrySize);
                        }
                        bytes = IOUtils.copy(inp, os);
                    }
                }
                numberOfBytes = bytes;
                success = true;
            } finally {
                if (!success) {
                    try {
                        close();
                    } catch (IOException e) {
                        LOG.atWarn().withThrowable(e).log("Failed to clean up temporary resources on construction failure");
                    }
                }
            }
        } else {
            if (entrySize < -1 || entrySize >= Integer.MAX_VALUE) {
                throw new IOException("ZIP entry size is too large or invalid");
            }

            // Grab the de-compressed contents for later.
            if (entrySize == -1) {
                // size unknown: read what is present, bounded by getMaxEntrySize()
                // (a stream longer than that fails with a RecordFormatException)
                data = IOUtils.toByteArrayWithMaxLength(inp, getMaxEntrySize());
            } else {
                // size known: read exactly entrySize bytes (EOFException if the entry holds
                // fewer). The initial buffer is sized from entrySize but capped at
                // MAX_INIT_BUFFER_SIZE - entrySize comes from the (untrusted) zip local file
                // header, so a tiny entry claiming a huge uncompressed size must not be able
                // to force a large eager allocation before any data is read.
                data = IOUtils.toByteArray(inp, Math.toIntExact(entrySize), getMaxEntrySize(),
                        MAX_INIT_BUFFER_SIZE, "ZipArchiveFakeEntry.setMaxEntrySize()");
                // the entry must not hold more bytes than it declared
                if (inp.read() >= 0) {
                    throw new IOException("Zip entry " + entry.getName()
                            + " has more data than its declared size of " + entrySize + " bytes");
                }
            }
            numberOfBytes = data.length;
        }
    }

    /**
     * Opens the output to buffer this entry's data outside the heap: encrypted temp data if
     * {@link ZipInputStreamZipEntrySource#setEncryptTempFiles(boolean)} is enabled, a plain
     * temp file otherwise. Sets the corresponding field so {@link #getInputStream()} and
     * {@link #close()} can find it.
     */
    private OutputStream createTempDataOutputStream() throws IOException {
        if (ZipInputStreamZipEntrySource.shouldEncryptTempFiles()) {
            encryptedTempData = new EncryptedTempData();
            return encryptedTempData.getOutputStream();
        }
        tempFile = TempFile.createTempFile("poi-zip-entry", ".tmp");
        return Files.newOutputStream(tempFile.toPath());
    }

    @Override
    public long getSize() {
        return numberOfBytes;
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
                return Files.newInputStream(tempFile.toPath());
            } catch (FileNotFoundException e) {
                throw new IOException("temp file " + tempFile.getAbsolutePath() + " is missing");
            }
        } else if (data != null) {
            return UnsynchronizedByteArrayInputStream.builder().setByteArray(data).get();
        } else {
            throw new IOException("Cannot retrieve data from Zip Entry, probably because the Zip Entry was closed before the data was requested.");
        }
    }

    /**
     * Deletes any temp files and releases any byte arrays.
     * @throws IOException If closing the entry fails.
     * @since 5.1.0
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

    // open for testing
    boolean isUnencryptedTempFileBacked() {
        return tempFile != null && tempFile.exists();
    }

    // open for testing
    boolean isEncryptedTempFileBacked() {
        return encryptedTempData != null;
    }

}
