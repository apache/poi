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

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.SuppressForbidden;

/**
 * This class wraps a {@link ZipFile} in order to check the
 * entries for <a href="https://en.wikipedia.org/wiki/Zip_bomb">zip bombs</a>
 * while reading the archive.
 * If a {@link ZipInputStream} is directly used, the wrapper
 * can be applied via {@link #addThreshold(InputStream)}.
 * The alert limits can be globally defined via {@link #setMaxEntrySize(long)}
 * and {@link #setMinInflateRatio(double)}.
 */
public class ZipSecureFile extends ZipFile {
    private static final POILogger LOG = POILogFactory.getLogger(ZipSecureFile.class);
    
    private static double MIN_INFLATE_RATIO = 0.01d;
    private static long MAX_ENTRY_SIZE = 0xFFFFFFFFL;
    
    // don't alert for expanded sizes smaller than 100k
    private final static long GRACE_ENTRY_SIZE = 100*1024L;

    // The default maximum size of extracted text 
    private static long MAX_TEXT_SIZE = 10*1024*1024L;
    
    /**
     * Sets the ratio between de- and inflated bytes to detect zipbomb.
     * It defaults to 1% (= 0.01d), i.e. when the compression is better than
     * 1% for any given read package part, the parsing will fail indicating a 
     * Zip-Bomb.
     *
     * @param ratio the ratio between de- and inflated bytes to detect zipbomb
     */
    public static void setMinInflateRatio(double ratio) {
        MIN_INFLATE_RATIO = ratio;
    }
    
    /**
     * Returns the current minimum compression rate that is used.
     * 
     * See setMinInflateRatio() for details.
     *
     * @return The min accepted compression-ratio.  
     */
    public static double getMinInflateRatio() {
        return MIN_INFLATE_RATIO;
    }

    /**
     * Sets the maximum file size of a single zip entry. It defaults to 4GB,
     * i.e. the 32-bit zip format maximum.
     * 
     * This can be used to limit memory consumption and protect against 
     * security vulnerabilities when documents are provided by users.
     *
     * @param maxEntrySize the max. file size of a single zip entry
     */
    public static void setMaxEntrySize(long maxEntrySize) {
        if (maxEntrySize < 0 || maxEntrySize > 0xFFFFFFFFL) {   // don't use MAX_ENTRY_SIZE here!
            throw new IllegalArgumentException("Max entry size is bounded [0-4GB], but had " + maxEntrySize);
        }
        MAX_ENTRY_SIZE = maxEntrySize;
    }

    /**
     * Returns the current maximum allowed uncompressed file size.
     * 
     * See setMaxEntrySize() for details.
     *
     * @return The max accepted uncompressed file size. 
     */
    public static long getMaxEntrySize() {
        return MAX_ENTRY_SIZE;
    }

    /**
     * Sets the maximum number of characters of text that are
     * extracted before an exception is thrown during extracting
     * text from documents.
     * 
     * This can be used to limit memory consumption and protect against 
     * security vulnerabilities when documents are provided by users.
     *
     * @param maxTextSize the max. file size of a single zip entry
     */
    public static void setMaxTextSize(long maxTextSize) {
        if (maxTextSize < 0 || maxTextSize > 0xFFFFFFFFL) {     // don't use MAX_ENTRY_SIZE here!
            throw new IllegalArgumentException("Max text size is bounded [0-4GB], but had " + maxTextSize);
        }
        MAX_TEXT_SIZE = maxTextSize;
    }

    /**
     * Returns the current maximum allowed text size.
     * 
     * See setMaxTextSize() for details.
     *
     * @return The max accepted text size. 
     */
    public static long getMaxTextSize() {
        return MAX_TEXT_SIZE;
    }

    public ZipSecureFile(File file, int mode) throws ZipException, IOException {
        super(file, mode);
    }

    public ZipSecureFile(File file) throws ZipException, IOException {
        super(file);
    }

    public ZipSecureFile(String name) throws ZipException, IOException {
        super(name);
    }

    /**
     * Returns an input stream for reading the contents of the specified
     * zip file entry.
     *
     * <p> Closing this ZIP file will, in turn, close all input
     * streams that have been returned by invocations of this method.
     *
     * @param entry the zip file entry
     * @return the input stream for reading the contents of the specified
     * zip file entry.
     * @throws ZipException if a ZIP format error has occurred
     * @throws IOException if an I/O error has occurred
     * @throws IllegalStateException if the zip file has been closed
     */
    @Override
    @SuppressWarnings("resource")
    public InputStream getInputStream(ZipEntry entry) throws IOException {
        InputStream zipIS = super.getInputStream(entry);
        return addThreshold(zipIS);
    }

    public static ThresholdInputStream addThreshold(final InputStream zipIS) throws IOException {
        ThresholdInputStream newInner;
        if (zipIS instanceof InflaterInputStream) {
            newInner = AccessController.doPrivileged(new PrivilegedAction<ThresholdInputStream>() { // NOSONAR
                @Override
                @SuppressForbidden("TODO: Fix this to not use reflection (it will break in Java 9)! " +
                        "Better would be to wrap *before* instead of trying to insert wrapper afterwards.")
                public ThresholdInputStream run() {
                    try {
                        Field f = FilterInputStream.class.getDeclaredField("in");
                        f.setAccessible(true);
                        InputStream oldInner = (InputStream)f.get(zipIS);
                        ThresholdInputStream newInner2 = new ThresholdInputStream(oldInner, null);
                        f.set(zipIS, newInner2);
                        return newInner2;
                    } catch (Exception ex) {
                        LOG.log(POILogger.WARN, "SecurityManager doesn't allow manipulation via reflection for zipbomb detection - continue with original input stream", ex);
                    }
                    return null;
                }
            });
        } else {
            // the inner stream is a ZipFileInputStream, i.e. the data wasn't compressed
            newInner = null;
        }

        return new ThresholdInputStream(zipIS, newInner);
    }

    public static class ThresholdInputStream extends PushbackInputStream {
        long counter = 0;
        ThresholdInputStream cis;

        public ThresholdInputStream(InputStream is, ThresholdInputStream cis) {
            super(is,1);
            this.cis = cis;
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
            counter = 0;
            return in.skip(n);
        }

        @Override
        public synchronized void reset() throws IOException {
            counter = 0;
            in.reset();
        }

        public void advance(int advance) throws IOException {
            counter += advance;
            
            // check the file size first, in case we are working on uncompressed streams
            if(counter > MAX_ENTRY_SIZE) {
                throw new IOException("Zip bomb detected! The file would exceed the max size of the expanded data in the zip-file. "
                        + "This may indicates that the file is used to inflate memory usage and thus could pose a security risk. "
                        + "You can adjust this limit via ZipSecureFile.setMaxEntrySize() if you need to work with files which are very large. "
                        + "Counter: " + counter + ", cis.counter: " + (cis == null ? 0 : cis.counter)
                        + "Limits: MAX_ENTRY_SIZE: " + MAX_ENTRY_SIZE);
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
            throw new IOException("Zip bomb detected! The file would exceed the max. ratio of compressed file size to the size of the expanded data. "
                    + "This may indicate that the file is used to inflate memory usage and thus could pose a security risk. "
                    + "You can adjust this limit via ZipSecureFile.setMinInflateRatio() if you need to work with files which exceed this limit. "
                    + "Counter: " + counter + ", cis.counter: " + cis.counter + ", ratio: " + (((double)cis.counter)/counter)
                    + "Limits: MIN_INFLATE_RATIO: " + MIN_INFLATE_RATIO);
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
            in.mark(readlimit);
        }
    }
}
