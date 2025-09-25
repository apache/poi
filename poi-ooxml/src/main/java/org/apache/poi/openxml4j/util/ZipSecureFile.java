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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.openxml4j.opc.internal.InvalidZipException;
import org.apache.poi.util.Internal;
import org.apache.poi.util.Removal;

/**
 * This class wraps a {@link ZipFile} in order to check the
 * entries for <a href="https://en.wikipedia.org/wiki/Zip_bomb">zip bombs</a>
 * while reading the archive.<p>
 *
 * The alert limits can be globally defined via {@link #setMaxEntrySize(long)}
 * and {@link #setMinInflateRatio(double)}.
 */
@Internal
public class ZipSecureFile extends ZipFile {
    private static final Logger LOG = PoiLogManager.getLogger(ZipSecureFile.class);
    /* package */ static double MIN_INFLATE_RATIO = 0.01d;
    /* package */ static final long DEFAULT_MAX_ENTRY_SIZE = 0xFFFFFFFFL;
    /* package */ static final long DEFAULT_MAX_FILE_COUNT = 1000;
    /* package */ static final long DEFAULT_GRACE_ENTRY_SIZE = 100*1024L;
    /* package */ static long MAX_ENTRY_SIZE = DEFAULT_MAX_ENTRY_SIZE;
    /* package */ static long MAX_FILE_COUNT = DEFAULT_MAX_FILE_COUNT;
    /* package */ static long GRACE_ENTRY_SIZE = DEFAULT_GRACE_ENTRY_SIZE;

    // The maximum chars of extracted text
    /* package */ static final long DEFAULT_MAX_TEXT_SIZE = 10*1024*1024L;
    private static long MAX_TEXT_SIZE = DEFAULT_MAX_TEXT_SIZE;

    public static final String MAX_FILE_COUNT_MSG =
            "The file appears to be potentially malicious. This file embeds more internal file entries than expected.\n" +
                    "This may indicates that the file could pose a security risk.\n" +
                    "You can adjust this limit via ZipSecureFile.setMaxFileCount() if you need to work with files which are very large.\n" +
                    "Limits: MAX_FILE_COUNT: %d";


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
     * Returns the current maximum file count that is used.
     *
     * See setMaxFileCount() for details.
     *
     * @return The max accepted file count (i.e. the max number of files we allow inside zip files that we read - including OOXML files like xlsx, docx, pptx, etc.).
     * @since POI 5.2.4
     */
    public static long getMaxFileCount() {
        return MAX_FILE_COUNT;
    }

    /**
     * Sets the maximum file count that we allow inside zip files that we read -
     * including OOXML files like xlsx, docx, pptx, etc. The default is 1000.
     *
     * @param maxFileCount The max accepted file count
     * @since POI 5.2.4
     */
    public static void setMaxFileCount(final long maxFileCount) {
        MAX_FILE_COUNT = maxFileCount;
    }

    /**
     * Sets the maximum file size of a single zip entry. It defaults to 4GB,
     * i.e. the 32-bit zip format maximum.
     *
     * This can be used to limit memory consumption and protect against
     * security vulnerabilities when documents are provided by users.
     *
     * @param maxEntrySize the max. file size of a single zip entry
     * @throws IllegalArgumentException for negative maxEntrySize
     */
    public static void setMaxEntrySize(long maxEntrySize) {
        if (maxEntrySize < 0) {
            throw new IllegalArgumentException("Max entry size must be greater than or equal to zero");
        } else if (maxEntrySize > DEFAULT_MAX_ENTRY_SIZE) {
            LOG.warn("setting max entry size greater than 4Gb can be risky; set to {} bytes", maxEntrySize);
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
     * Sets the grace entry size of a single zip entry. It defaults to 100Kb.
     *
     * When decompressed data in a zip entry is smaller than this size, the
     * Minimum Inflation Ratio check is ignored.
     *
     * Setting this to a very small value may lead to more files being flagged
     * as potential Zip Bombs are rejected as a result.
     *
     * @param graceEntrySize the grace entry size of a single zip entry
     * @throws IllegalArgumentException for negative graceEntrySize
     * @since POI 5.2.4
     */
    public static void setGraceEntrySize(long graceEntrySize) {
        if (graceEntrySize < 0) {
            throw new IllegalArgumentException("Grace entry size must be greater than or equal to zero");
        }
        GRACE_ENTRY_SIZE = graceEntrySize;
    }

    /**
     * Returns the current threshold for decompressed data in zip entries that are regarded as too small
     * to worry about from a Zip Bomb perspective (default is 100Kb).
     *
     * See setGraceEntrySize() for details.
     *
     * @return The current grace entry size
     * @since POI 5.2.4
     */
    public static long getGraceEntrySize() {
        return GRACE_ENTRY_SIZE;
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
     * @throws IllegalArgumentException for negative maxTextSize
     */
    public static void setMaxTextSize(long maxTextSize) {
        if (maxTextSize < 0) {
            throw new IllegalArgumentException("Max text size must be greater than or equal to zero");
        }else if (maxTextSize > DEFAULT_MAX_TEXT_SIZE) {
            LOG.warn("setting max text size greater than {} can be risky; set to {} chars", DEFAULT_MAX_TEXT_SIZE, maxTextSize);
        }
        MAX_TEXT_SIZE = maxTextSize;
    }

    /**
     * Returns the current maximum allowed text size.
     *
     * @return The max accepted text size.
     * @see #setMaxTextSize(long)
     */
    public static long getMaxTextSize() {
        return MAX_TEXT_SIZE;
    }

    /**
     * @param file the {@link File}, possibly including path traversal - it is up to users to validate that the input value is safe
     * @throws IOException if an error occurs while reading the file.
     */
    public ZipSecureFile(File file) throws IOException {
        super(file);
        this.fileName = file.getAbsolutePath();
        validateEntryNames();
    }

    /**
     * @param name the file name, possibly including path traversal - it is up to users to validate that the input value is safe
     * @throws IOException  if an error occurs while reading the file.
     */
    public ZipSecureFile(String name) throws IOException {
        super(name);
        this.fileName = new File(name).getAbsolutePath();
        validateEntryNames();
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
     * @deprecated there is no need for this method - it will be removed in a future version of POI (deprecated since POI 5.3.0)
     */
    @Removal(version = "7.0.0")
    public String getName() {
        return fileName;
    }

    private void validateEntryNames() throws IOException {
        final Enumeration<ZipArchiveEntry> en = getEntries();
        final Set<String> filenames = new HashSet<>();
        while (en.hasMoreElements()) {
            final ZipArchiveEntry entry = en.nextElement();
            String name = entry.getName();
            if (name == null || name.isEmpty()) {
                throw new InvalidZipException("Input file contains an entry with an empty name");
            }
            name = name.toLowerCase(Locale.ROOT);
            if (filenames.contains(name)) {
                throw new InvalidZipException("Input file contains more than 1 entry with the name " + entry.getName());
            }
            filenames.add(name);
        }
    }
}
