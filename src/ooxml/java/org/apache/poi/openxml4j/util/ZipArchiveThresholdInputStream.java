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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SuppressForbidden;

public class ZipArchiveThresholdInputStream extends PushbackInputStream {
    private static final POILogger LOG = POILogFactory.getLogger(ZipArchiveThresholdInputStream.class);

    // don't alert for expanded sizes smaller than 100k
    private static final long GRACE_ENTRY_SIZE = 100*1024L;

    private static final String MAX_ENTRY_SIZE_MSG =
        "Zip bomb detected! The file would exceed the max size of the expanded data in the zip-file.\n" +
        "This may indicates that the file is used to inflate memory usage and thus could pose a security risk.\n" +
        "You can adjust this limit via ZipSecureFile.setMaxEntrySize() if you need to work with files which are very large.\n" +
        "Counter: %d, cis.counter: %d\n" +
        "Limits: MAX_ENTRY_SIZE: %d, Entry: %s";

    private static final String MIN_INFLATE_RATIO_MSG =
        "Zip bomb detected! The file would exceed the max. ratio of compressed file size to the size of the expanded data.\n" +
        "This may indicate that the file is used to inflate memory usage and thus could pose a security risk.\n" +
        "You can adjust this limit via ZipSecureFile.setMinInflateRatio() if you need to work with files which exceed this limit.\n" +
        "Counter: %d, cis.counter: %d, ratio: %f\n" +
        "Limits: MIN_INFLATE_RATIO: %f, Entry: %s";

    private static final String SECURITY_BLOCKED =
        "SecurityManager doesn't allow manipulation via reflection for zipbomb detection - continue with original input stream";

    /**
     * the reference to the current entry is only used for a more detailed log message in case of an error
     */
    private ZipEntry entry;

    private long counter;
    private long markPos;
    private final ZipArchiveThresholdInputStream cis;
    private boolean guardState = true;


    public ZipArchiveThresholdInputStream(final InputStream zipIS) throws IOException {
        super(zipIS);
        if (zipIS instanceof InflaterInputStream) {
            cis = AccessController.doPrivileged(inject(zipIS));
        } else {
            // the inner stream is a ZipFileInputStream, i.e. the data wasn't compressed
            cis = null;
        }
    }

    private ZipArchiveThresholdInputStream(InputStream is, ZipArchiveThresholdInputStream cis) {
        super(is);
        this.cis = cis;
    }

    @SuppressForbidden
    private static PrivilegedAction<ZipArchiveThresholdInputStream> inject(final InputStream zipIS) {
        return () -> {
            try {
                final Field f = FilterInputStream.class.getDeclaredField("in");
                f.setAccessible(true);
                final InputStream oldInner = (InputStream)f.get(zipIS);
                final ZipArchiveThresholdInputStream inner = new ZipArchiveThresholdInputStream(oldInner, null);
                f.set(zipIS, inner);
                return inner;
            } catch (Exception ex) {
                LOG.log(POILogger.WARN, SECURITY_BLOCKED, ex);
            }
            return null;
        };
    }

    @Override
    public int read() throws IOException {
        int b = in.read();
        if (b > -1) {
            advance(1);
        }
        return b;
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        int cnt = in.read(b, off, len);
        if (cnt > -1) {
            advance(cnt);
        }
        return cnt;
    }

    @Override
    public long skip(long n) throws IOException {
        long s = in.skip(n);
        counter += s;
        return s;
    }

    @Override
    public synchronized void reset() throws IOException {
        counter = markPos;
        super.reset();
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

    public void advance(int advance) throws IOException {
        counter += advance;

        if (!guardState) {
            return;
        }

        final String entryName = entry == null ? "not set" : entry.getName();
        final long cisCount = (cis == null ? 0 : cis.counter);

        // check the file size first, in case we are working on uncompressed streams
        if(counter > MAX_ENTRY_SIZE) {
            throw new IOException(String.format(Locale.ROOT, MAX_ENTRY_SIZE_MSG, counter, cisCount, MAX_ENTRY_SIZE, entryName));
        }

        // no expanded size?
        if (cis == null) {
            return;
        }

        // don't alert for small expanded size
        if (counter <= GRACE_ENTRY_SIZE) {
            return;
        }

        double ratio = (double)cis.counter/(double)counter;
        if (ratio >= MIN_INFLATE_RATIO) {
            return;
        }

        // one of the limits was reached, report it
        throw new IOException(String.format(Locale.ROOT, MIN_INFLATE_RATIO_MSG, counter, cisCount, ratio, MIN_INFLATE_RATIO, entryName));
    }

    public ZipEntry getNextEntry() throws IOException {
        if (!(in instanceof ZipInputStream)) {
            throw new UnsupportedOperationException("underlying stream is not a ZipInputStream");
        }
        counter = 0;
        return ((ZipInputStream)in).getNextEntry();
    }

    public void closeEntry() throws IOException {
        if (!(in instanceof ZipInputStream)) {
            throw new UnsupportedOperationException("underlying stream is not a ZipInputStream");
        }
        counter = 0;
        ((ZipInputStream)in).closeEntry();
    }

    @Override
    public void unread(int b) throws IOException {
        if (!(in instanceof PushbackInputStream)) {
            throw new UnsupportedOperationException("underlying stream is not a PushbackInputStream");
        }
        if (--counter < 0) {
            counter = 0;
        }
        ((PushbackInputStream)in).unread(b);
    }

    @Override
    public void unread(byte[] b, int off, int len) throws IOException {
        if (!(in instanceof PushbackInputStream)) {
            throw new UnsupportedOperationException("underlying stream is not a PushbackInputStream");
        }
        counter -= len;
        if (--counter < 0) {
            counter = 0;
        }
        ((PushbackInputStream)in).unread(b, off, len);
    }

    @Override
    @SuppressForbidden("just delegating")
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public synchronized void mark(int readlimit) {
        markPos = counter;
        in.mark(readlimit);
    }

    /**
     * Sets the zip entry for a detailed logging
     * @param entry the entry
     */
    void setEntry(ZipEntry entry) {
        this.entry = entry;
    }
}