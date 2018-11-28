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
package org.apache.poi.extractor;

import static org.apache.poi.hssf.model.InternalWorkbook.OLD_WORKBOOK_DIR_ENTRY_NAME;
import static org.apache.poi.hssf.model.InternalWorkbook.WORKBOOK_DIR_ENTRY_NAMES;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.hssf.extractor.EventBasedExcelExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Figures out the correct POIOLE2TextExtractor for your supplied
 *  document, and returns it.
 *  
 * <p>Note 1 - will fail for many file formats if the POI Scratchpad jar is
 *  not present on the runtime classpath</p>
 * <p>Note 2 - for text extractor creation across all formats, use
 *  {@link org.apache.poi.ooxml.extractor.ExtractorFactory} contained within
 *  the OOXML jar.</p>
 * <p>Note 3 - rather than using this, for most cases you would be better
 *  off switching to <a href="http://tika.apache.org">Apache Tika</a> instead!</p>
 */
@SuppressWarnings({"WeakerAccess", "JavadocReference"})
public final class OLE2ExtractorFactory {
    private static final POILogger LOGGER = POILogFactory.getLogger(OLE2ExtractorFactory.class); 
    
    /** Should this thread prefer event based over usermodel based extractors? */
    private static final ThreadLocal<Boolean> threadPreferEventExtractors = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** Should all threads prefer event based over usermodel based extractors? */
    private static Boolean allPreferEventExtractors;

    private OLE2ExtractorFactory() {
    }

    /**
     * Should this thread prefer event based over usermodel based extractors?
     * (usermodel extractors tend to be more accurate, but use more memory)
     * Default is false.
     *
     * @return true if event extractors should be preferred in the current thread, fals otherwise.
     */
    public static boolean getThreadPrefersEventExtractors() {
        return threadPreferEventExtractors.get();
    }

    /**
     * Should all threads prefer event based over usermodel based extractors?
     * (usermodel extractors tend to be more accurate, but use more memory)
     * Default is to use the thread level setting, which defaults to false.
     *
     * @return true if event extractors should be preferred in all threads, fals otherwise.
     */
    public static Boolean getAllThreadsPreferEventExtractors() {
        return allPreferEventExtractors;
    }

    /**
     * Should this thread prefer event based over usermodel based extractors?
     * Will only be used if the All Threads setting is null.
     *
     * @param preferEventExtractors If this threads should prefer event based extractors.
     */
    public static void setThreadPrefersEventExtractors(boolean preferEventExtractors) {
        threadPreferEventExtractors.set(preferEventExtractors);
    }

    /**
     * Should all threads prefer event based over usermodel based extractors?
     * If set, will take preference over the Thread level setting.
     *
     * @param preferEventExtractors If all threads should prefer event based extractors.
     */
    public static void setAllThreadsPreferEventExtractors(Boolean preferEventExtractors) {
        allPreferEventExtractors = preferEventExtractors;
    }

    /**
     * Should this thread use event based extractors is available?
     * Checks the all-threads one first, then thread specific.
     *
     * @return If the current thread should use event based extractors.
     */
    public static boolean getPreferEventExtractor() {
        if(allPreferEventExtractors != null) {
            return allPreferEventExtractors;
        }
        return threadPreferEventExtractors.get();
    }

    @SuppressWarnings("unchecked")
    public static <T extends POITextExtractor> T createExtractor(POIFSFileSystem fs) throws IOException {
        return (T)createExtractor(fs.getRoot());
    }

    @SuppressWarnings("unchecked")
    public static <T extends POITextExtractor> T createExtractor(InputStream input) throws IOException {
        Class<?> cls = getOOXMLClass();
        if (cls != null) {
            // Use Reflection to get us the full OOXML-enabled version
            try {
                Method m = cls.getDeclaredMethod("createExtractor", InputStream.class);
                return (T)m.invoke(null, input);
            } catch (IllegalArgumentException iae) {
                throw iae;
            } catch (Exception e) {
                throw new IllegalArgumentException("Error creating Extractor for InputStream", e);
            }
        } else {
            // Best hope it's OLE2....
            return createExtractor(new POIFSFileSystem(input));
        }
    }

    private static Class<?> getOOXMLClass() {
        try {
            return OLE2ExtractorFactory.class.getClassLoader().loadClass(
                    "org.apache.poi.extractor.ExtractorFactory"
            );
        } catch (ClassNotFoundException e) {
            LOGGER.log(POILogger.WARN, "POI OOXML jar missing");
            return null;
        }
    }
    private static Class<?> getScratchpadClass() {
        try {
            return OLE2ExtractorFactory.class.getClassLoader().loadClass(
                    "org.apache.poi.extractor.ole2.OLE2ScratchpadExtractorFactory"
            );
        } catch (ClassNotFoundException e) {
            LOGGER.log(POILogger.ERROR, "POI Scratchpad jar missing");
            throw new IllegalStateException("POI Scratchpad jar missing, required for ExtractorFactory");
        }
    }
    
    /**
     * Create the Extractor, if possible. Generally needs the Scratchpad jar.
     * Note that this won't check for embedded OOXML resources either, use
     *  {@link org.apache.poi.ooxml.extractor.ExtractorFactory} for that.
     *
     * @param poifsDir The {@link DirectoryNode} pointing to a document.
     *
     * @return The resulting {@link POITextExtractor}, an exception is thrown if
     *      no TextExtractor can be created for some reason.
     *
     * @throws IOException If converting the {@link DirectoryNode} into a HSSFWorkbook fails
     * @throws OldFileFormatException If the {@link DirectoryNode} points to a format of
     *      an unsupported version of Excel.
     * @throws IllegalArgumentException If creating the Extractor fails
     */
    public static POITextExtractor createExtractor(DirectoryNode poifsDir) throws IOException {
        // Look for certain entries in the stream, to figure it
        // out from
        for (String workbookName : WORKBOOK_DIR_ENTRY_NAMES) {
            if (poifsDir.hasEntry(workbookName)) {
                if (getPreferEventExtractor()) {
                    return new EventBasedExcelExtractor(poifsDir);
                }
                return new ExcelExtractor(poifsDir);
            }
        }
        if (poifsDir.hasEntry(OLD_WORKBOOK_DIR_ENTRY_NAME)) {
            throw new OldExcelFormatException("Old Excel Spreadsheet format (1-95) "
                    + "found. Please call OldExcelExtractor directly for basic text extraction");
        }
        
        // Ask Scratchpad, or fail trying
        Class<?> cls = getScratchpadClass();
        try {
            Method m = cls.getDeclaredMethod("createExtractor", DirectoryNode.class);
            POITextExtractor ext = (POITextExtractor)m.invoke(null, poifsDir);
            if (ext != null) return ext;
        } catch (IllegalArgumentException iae) {
            throw iae;
        } catch (Exception e) {
            throw new IllegalArgumentException("Error creating Scratchpad Extractor", e);
        }

        throw new IllegalArgumentException("No supported documents found in the OLE2 stream");
    }

    /**
     * Returns an array of text extractors, one for each of
     *  the embedded documents in the file (if there are any).
     * If there are no embedded documents, you'll get back an
     *  empty array. Otherwise, you'll get one open
     *  {@link POITextExtractor} for each embedded file.
     *
     * @param ext The extractor to look at for embedded documents
     *
     * @return An array of resulting extractors. Empty if no embedded documents are found.
     *
     * @throws IOException If converting the {@link DirectoryNode} into a HSSFWorkbook fails
     * @throws OldFileFormatException If the {@link DirectoryNode} points to a format of
     *      an unsupported version of Excel.
     * @throws IllegalArgumentException If creating the Extractor fails
     */
    public static POITextExtractor[] getEmbededDocsTextExtractors(POIOLE2TextExtractor ext) throws IOException {
        // All the embedded directories we spotted
        List<Entry> dirs = new ArrayList<>();
        // For anything else not directly held in as a POIFS directory
        List<InputStream> nonPOIFS = new ArrayList<>();

        // Find all the embedded directories
        DirectoryEntry root = ext.getRoot();
        if(root == null) {
            throw new IllegalStateException("The extractor didn't know which POIFS it came from!");
        }

        if(ext instanceof ExcelExtractor) {
            // These are in MBD... under the root
            Iterator<Entry> it = root.getEntries();
            while(it.hasNext()) {
                Entry entry = it.next();
                if(entry.getName().startsWith("MBD")) {
                    dirs.add(entry);
                }
            }
        } else {
            // Ask Scratchpad, or fail trying
            Class<?> cls = getScratchpadClass();
            try {
                Method m = cls.getDeclaredMethod(
                        "identifyEmbeddedResources", POIOLE2TextExtractor.class, List.class, List.class);
                m.invoke(null, ext, dirs, nonPOIFS);
            } catch (Exception e) {
                throw new IllegalArgumentException("Error checking for Scratchpad embedded resources", e);
            }
        }

        // Create the extractors
        if(dirs.size() == 0 && nonPOIFS.size() == 0){
            return new POITextExtractor[0];
        }

        ArrayList<POITextExtractor> e = new ArrayList<>();
        for (Entry dir : dirs) {
            e.add(createExtractor((DirectoryNode) dir
            ));
        }
        for (InputStream stream : nonPOIFS) {
            try {
                e.add(createExtractor(stream));
            } catch (Exception xe) {
                // Ignore, invalid format
                LOGGER.log(POILogger.WARN, xe);
            }
        }
        return e.toArray(new POITextExtractor[0]);
    }
}
