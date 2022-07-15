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

import static org.apache.poi.hssf.record.crypto.Biff8EncryptionKey.getCurrentUserPassword;
import static org.apache.poi.poifs.crypt.Decryptor.DEFAULT_POIFS_ENTRY;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.EmptyFileException;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;

/**
 * Figures out the correct POIOLE2TextExtractor for your supplied
 *  document, and returns it.
 *
 * <p>Note 1 - will fail for many file formats if the POI Scratchpad jar is
 *  not present on the runtime classpath</p>
 * <p>Note 2 - for text extractor creation across all formats, use
 *  {@link org.apache.poi.ooxml.extractor.POIXMLExtractorFactory} contained within
 *  the OOXML jar.</p>
 * <p>Note 3 - rather than using this, for most cases you would be better
 *  off switching to <a href="http://tika.apache.org">Apache Tika</a> instead!</p>
 */
@SuppressWarnings({"WeakerAccess", "JavadocReference"})
public final class ExtractorFactory {
    /**
     * Some OPCPackages are packed in side an OLE2 container.
     * If encrypted, the {@link DirectoryNode} is called {@link Decryptor#DEFAULT_POIFS_ENTRY "EncryptedPackage"},
     * otherwise the node is called "Package"
     */
    public static final String OOXML_PACKAGE = "Package";

    private static final Logger LOGGER = LogManager.getLogger(ExtractorFactory.class);

    /** Should this thread prefer event based over usermodel based extractors? */
    private static final ThreadLocal<Boolean> threadPreferEventExtractors = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /** Should all threads prefer event based over usermodel based extractors? */
    private static Boolean allPreferEventExtractors;


    private static class Singleton {
        private static final ExtractorFactory INSTANCE = new ExtractorFactory();
    }

    private interface ProviderMethod {
        POITextExtractor create(ExtractorProvider prov) throws IOException;
    }

    private final List<ExtractorProvider> provider = new ArrayList<>();


    private ExtractorFactory() {
        ClassLoader cl = ExtractorFactory.class.getClassLoader();
        ServiceLoader.load(ExtractorProvider.class, cl).forEach(provider::add);
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
        return (allPreferEventExtractors != null) ? allPreferEventExtractors : threadPreferEventExtractors.get();
    }

    /**
     * Create an extractor that can be used to read text from the given file.
     *
     * @param fs The file-system which wraps the data of the file.
     * @return A POITextExtractor that can be used to fetch text-content of the file.
     * @throws IOException If reading the file-data fails
     */
    public static POITextExtractor createExtractor(POIFSFileSystem fs) throws IOException {
        return createExtractor(fs, getCurrentUserPassword());
    }

    /**
     * Create an extractor that can be used to read text from the given file.
     *
     * @param fs The file-system which wraps the data of the file.
     * @param password The password that is necessary to open the file
     * @return A POITextExtractor that can be used to fetch text-content of the file.
     * @throws IOException If reading the file-data fails
     */
    public static POITextExtractor createExtractor(POIFSFileSystem fs, String password) throws IOException {
        return createExtractor(fs.getRoot(), password);
    }

    /**
     * Create an extractor that can be used to read text from the given file.
     *
     * @param input A stream which wraps the data of the file.
     * @return A POITextExtractor that can be used to fetch text-content of the file.
     * @throws IOException If reading the file-data fails
     * @throws EmptyFileException If the given file is empty
     */
    public static POITextExtractor createExtractor(InputStream input) throws IOException {
        return createExtractor(input, getCurrentUserPassword());
    }

    /**
     * Create an extractor that can be used to read text from the given file.
     *
     * @param input A stream which wraps the data of the file.
     * @param password The password that is necessary to open the file
     * @return A POITextExtractor that can be used to fetch text-content of the file.
     * @throws IOException If reading the file-data fails
     * @throws EmptyFileException If the given file is empty
     */
    public static POITextExtractor createExtractor(InputStream input, String password) throws IOException {
        final InputStream is = FileMagic.prepareToCheckMagic(input);
        byte[] emptyFileCheck = new byte[1];
        is.mark(emptyFileCheck.length);
        if (is.read(emptyFileCheck) < emptyFileCheck.length) {
            throw new EmptyFileException();
        }
        is.reset();

        final FileMagic fm = FileMagic.valueOf(is);
        if (FileMagic.OOXML == fm) {
            return wp(fm, w -> w.create(is, password));
        }

        if (FileMagic.OLE2 != fm) {
            throw new IOException("Can't create extractor - unsupported file type: "+fm);
        }

        POIFSFileSystem poifs = new POIFSFileSystem(is);
        DirectoryNode root = poifs.getRoot();
        boolean isOOXML = root.hasEntry(DEFAULT_POIFS_ENTRY) || root.hasEntry(OOXML_PACKAGE);

        return wp(isOOXML ? FileMagic.OOXML : fm, w -> w.create(root, password));
    }

    /**
     * Create an extractor that can be used to read text from the given file.
     *
     * @param file The file to read
     * @return A POITextExtractor that can be used to fetch text-content of the file.
     * @throws IOException If reading the file-data fails
     * @throws EmptyFileException If the given file is empty
     */
    public static POITextExtractor createExtractor(File file) throws IOException {
        return createExtractor(file, getCurrentUserPassword());
    }

    /**
     * Create an extractor that can be used to read text from the given file.
     *
     * @param file The file to read
     * @param password The password that is necessary to open the file
     * @return A POITextExtractor that can be used to fetch text-content of the file.
     * @throws IOException If reading the file-data fails
     * @throws EmptyFileException If the given file is empty
     */
    @SuppressWarnings({"java:S2095"})
    public static POITextExtractor createExtractor(File file, String password) throws IOException {
        if (file.length() == 0) {
            throw new EmptyFileException(file);
        }

        final FileMagic fm = FileMagic.valueOf(file);
        if (FileMagic.OOXML == fm) {
            return wp(fm, w -> w.create(file, password));
        }

        if (FileMagic.OLE2 != fm) {
            throw new IOException("Can't create extractor - unsupported file type: "+fm);
        }

        POIFSFileSystem poifs = null;
        try {
            poifs = new POIFSFileSystem(file, true);
            DirectoryNode root = poifs.getRoot();
            boolean isOOXML = root.hasEntry(DEFAULT_POIFS_ENTRY) || root.hasEntry(OOXML_PACKAGE);
            return wp(isOOXML ? FileMagic.OOXML : fm, w -> w.create(root, password));
        } catch (IOException | RuntimeException e) {
            IOUtils.closeQuietly(poifs);
            throw e;
        }
    }


    /**
     * Create the Extractor, if possible. Generally needs the Scratchpad jar.
     * Note that this won't check for embedded OOXML resources either, use
     *  {@link org.apache.poi.ooxml.extractor.POIXMLExtractorFactory} for that.
     *
     * @param root The {@link DirectoryNode} pointing to a document.
     *
     * @return The resulting {@link POITextExtractor}, an exception is thrown if
     *      no TextExtractor can be created for some reason.
     *
     * @throws IOException If converting the {@link DirectoryNode} into a HSSFWorkbook fails
     * @throws org.apache.poi.OldFileFormatException If the {@link DirectoryNode} points to a format of
     *      an unsupported version of Excel.
     * @throws IllegalArgumentException If creating the Extractor fails
     */
    public static POITextExtractor createExtractor(DirectoryNode root) throws IOException {
        return createExtractor(root, getCurrentUserPassword());
    }

    /**
     * Create the Extractor, if possible. Generally needs the Scratchpad jar.
     * Note that this won't check for embedded OOXML resources either, use
     *  {@link org.apache.poi.ooxml.extractor.POIXMLExtractorFactory} for that.
     *
     * @param root The {@link DirectoryNode} pointing to a document.
     * @param password The password that is necessary to open the file
     *
     * @return The resulting {@link POITextExtractor}, an exception is thrown if
     *      no TextExtractor can be created for some reason.
     *
     * @throws IOException If converting the {@link DirectoryNode} into a HSSFWorkbook fails
     * @throws org.apache.poi.OldFileFormatException If the {@link DirectoryNode} points to a format of
     *      an unsupported version of Excel.
     * @throws IllegalArgumentException If creating the Extractor fails
     */
    public static POITextExtractor createExtractor(final DirectoryNode root, String password) throws IOException {
        // Encrypted OOXML files go inside OLE2 containers, is this one?
        if (root.hasEntry(DEFAULT_POIFS_ENTRY) || root.hasEntry(OOXML_PACKAGE)) {
            return wp(FileMagic.OOXML, w -> w.create(root, password));
        } else {
            return wp(FileMagic.OLE2, w ->  w.create(root, password));
        }
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
     * @throws org.apache.poi.OldFileFormatException If the {@link DirectoryNode} points to a format of
     *      an unsupported version of Excel.
     * @throws IllegalArgumentException If creating the Extractor fails
     */
    public static POITextExtractor[] getEmbeddedDocsTextExtractors(POIOLE2TextExtractor ext) throws IOException {
        if (ext == null) {
            throw new IllegalStateException("extractor must be given");
        }

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
            StreamSupport.stream(root.spliterator(), false)
                .filter(entry -> entry.getName().startsWith("MBD"))
                .forEach(dirs::add);
        } else {
            for (ExtractorProvider prov : Singleton.INSTANCE.provider) {
                if (prov.accepts(FileMagic.OLE2)) {
                    prov.identifyEmbeddedResources(ext, dirs, nonPOIFS);
                    break;
                }
            }
        }

        // Create the extractors
        if(dirs.isEmpty() && nonPOIFS.isEmpty()){
            return new POITextExtractor[0];
        }

        ArrayList<POITextExtractor> textExtractors = new ArrayList<>();
        for (Entry dir : dirs) {
            textExtractors.add(createExtractor((DirectoryNode) dir));
        }
        for (InputStream stream : nonPOIFS) {
            try {
                textExtractors.add(createExtractor(stream));
            } catch (IOException e) {
                // Ignore, just means it didn't contain a format we support as yet
                LOGGER.atInfo().log("Format not supported yet ({})", e.getLocalizedMessage());
            }
        }
        return textExtractors.toArray(new POITextExtractor[0]);
    }

    private static POITextExtractor wp(FileMagic fm, ProviderMethod fun) throws IOException {
        for (ExtractorProvider prov : Singleton.INSTANCE.provider) {
            if (prov.accepts(fm)) {
                POITextExtractor ext = fun.create(prov);
                if (ext != null) {
                    return ext;
                }
            }
        }
        throw new IOException(
            "Your InputStream was neither an OLE2 stream, nor an OOXML stream " +
            "or you haven't provide the poi-ooxml*.jar and/or poi-scratchpad*.jar in the classpath/modulepath - FileMagic: " + fm +
            ", providers: " + Singleton.INSTANCE.provider);
    }

    public static void addProvider(ExtractorProvider provider){
        Singleton.INSTANCE.provider.add(provider);
    }

    public static void removeProvider(Class<? extends ExtractorProvider> provider){
        Singleton.INSTANCE.provider.removeIf(p -> p.getClass().isAssignableFrom(provider));
    }
}
