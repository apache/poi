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
import java.io.IOException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;

/**
 * This class wraps a {@link ZipFile} in order to check the
 * entries for <a href="https://en.wikipedia.org/wiki/Zip_bomb">zip bombs</a>
 * while reading the archive.<p>
 *
 * The alert limits can be globally defined via {@link #setMaxEntrySize(long)}
 * and {@link #setMinInflateRatio(double)}.
 */
public class ZipSecureFile extends ZipFile {
    /* package */ static double MIN_INFLATE_RATIO = 0.01d;
    /* package */ static long MAX_ENTRY_SIZE = 0xFFFFFFFFL;
    
    // The default maximum size of extracted text
    private static long MAX_TEXT_SIZE = 10*1024*1024L;

    private final String fileName;

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

    public ZipSecureFile(File file) throws IOException {
        super(file);
        this.fileName = file.getAbsolutePath();
    }

    public ZipSecureFile(String name) throws IOException {
        super(name);
        this.fileName = new File(name).getAbsolutePath();
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
     * @throws IOException if an I/O error has occurred
     * @throws IllegalStateException if the zip file has been closed
     */
    @Override
    @SuppressWarnings("resource")
    public ZipArchiveThresholdInputStream getInputStream(ZipArchiveEntry entry) throws IOException {
        ZipArchiveThresholdInputStream zatis = new ZipArchiveThresholdInputStream(super.getInputStream(entry));
        zatis.setEntry(entry);
        return zatis;
    }

    /**
     * Returns the path name of the ZIP file.
     * @return the path name of the ZIP file
     */
    public String getName() {
        return fileName;
    }
}
