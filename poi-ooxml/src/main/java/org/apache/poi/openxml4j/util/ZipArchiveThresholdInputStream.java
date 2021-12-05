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

import static org.apache.poi.openxml4j.util.ZipSecureFile.MAX_ENTRY_SIZE;
import static org.apache.poi.openxml4j.util.ZipSecureFile.MIN_INFLATE_RATIO;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.ZipException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.InputStreamStatistics;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;

@Internal
public class ZipArchiveThresholdInputStream extends FilterInputStream {
    // don't alert for expanded sizes smaller than 100k
    private static final long GRACE_ENTRY_SIZE = 100*1024L;

    private static final String MAX_ENTRY_SIZE_MSG =
        "Zip bomb detected! The file would exceed the max size of the expanded data in the zip-file.\n" +
        "This may indicates that the file is used to inflate memory usage and thus could pose a security risk.\n" +
        "You can adjust this limit via ZipSecureFile.setMaxEntrySize() if you need to work with files which are very large.\n" +
        "Uncompressed size: %d, Raw/compressed size: %d\n" +
        "Limits: MAX_ENTRY_SIZE: %d, Entry: %s";

    private static final String MIN_INFLATE_RATIO_MSG =
        "Zip bomb detected! The file would exceed the max. ratio of compressed file size to the size of the expanded data.\n" +
        "This may indicate that the file is used to inflate memory usage and thus could pose a security risk.\n" +
        "You can adjust this limit via ZipSecureFile.setMinInflateRatio() if you need to work with files which exceed this limit.\n" +
        "Uncompressed size: %d, Raw/compressed size: %d, ratio: %f\n" +
        "Limits: MIN_INFLATE_RATIO: %f, Entry: %s";

    /**
     * the reference to the current entry is only used for a more detailed log message in case of an error
     */
    private ZipArchiveEntry entry;
    private boolean guardState = true;

    public ZipArchiveThresholdInputStream(InputStream is) {
        super(is);
        if (!(is instanceof InputStreamStatistics)) {
            throw new IllegalArgumentException("InputStream of class "+is.getClass()+" is not implementing InputStreamStatistics.");
        }
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b > -1) {
            checkThreshold();
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int cnt = super.read(b, off, len);
        if (cnt > -1) {
            checkThreshold();
        }
        return cnt;
    }

    @Override
    public long skip(long n) throws IOException {
        long cnt = IOUtils.skipFully(super.in, n);
        if (cnt > 0) {
            checkThreshold();
        }
       return cnt;
    }

    /**
     * De-/activate threshold check.
     * A disabled guard might make sense, when POI is processing its own temporary data (see #59743)
     *
     * @param guardState {@code true} (= default) enables the threshold check
     */
    public void setGuardState(boolean guardState) {
        this.guardState = guardState;
    }

    private void checkThreshold() throws IOException {
        if (!guardState) {
            return;
        }

        final InputStreamStatistics stats = (InputStreamStatistics)in;
        final long payloadSize = stats.getUncompressedCount();

        long rawSize;
        try {
            rawSize = stats.getCompressedCount();
        } catch (NullPointerException e) {
            // this can happen with a very specially crafted file
            // see https://issues.apache.org/jira/browse/COMPRESS-598 for a related bug-report
            // therefore we try to handle this gracefully for now
            // this try/catch can be removed when COMPRESS-598 is fixed
            rawSize = 0;
        }

        final String entryName = entry == null ? "not set" : entry.getName();

        // check the file size first, in case we are working on uncompressed streams
        if(payloadSize > MAX_ENTRY_SIZE) {
            throw new IOException(String.format(Locale.ROOT, MAX_ENTRY_SIZE_MSG, payloadSize, rawSize, MAX_ENTRY_SIZE, entryName));
        }

        // don't alert for small expanded size
        if (payloadSize <= GRACE_ENTRY_SIZE) {
            return;
        }

        double ratio = rawSize / (double)payloadSize;
        if (ratio >= MIN_INFLATE_RATIO) {
            return;
        }

        // one of the limits was reached, report it
        throw new IOException(String.format(Locale.ROOT, MIN_INFLATE_RATIO_MSG, payloadSize, rawSize, ratio, MIN_INFLATE_RATIO, entryName));
    }

    ZipArchiveEntry getNextEntry() throws IOException {
        if (!(in instanceof ZipArchiveInputStream)) {
            throw new IllegalStateException("getNextEntry() is only allowed for stream based zip processing.");
        }

        try {
            entry = ((ZipArchiveInputStream) in).getNextZipEntry();
            return entry;
        } catch (ZipException ze) {
            if (ze.getMessage().startsWith("Unexpected record signature")) {
                throw new NotOfficeXmlFileException(
                        "No valid entries or contents found, this is not a valid OOXML (Office Open XML) file", ze);
            }
            throw ze;
        } catch (EOFException e) {
            return null;
        }
    }

    /**
     * Sets the zip entry for a detailed logging
     * @param entry the entry
     */
    void setEntry(ZipArchiveEntry entry) {
        this.entry = entry;
    }
}
