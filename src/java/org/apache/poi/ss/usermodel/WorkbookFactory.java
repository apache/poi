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
package org.apache.poi.ss.usermodel;

import static org.apache.poi.extractor.ExtractorFactory.OOXML_PACKAGE;
import static org.apache.poi.poifs.crypt.Decryptor.DEFAULT_POIFS_ENTRY;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * Factory for creating the appropriate kind of Workbook
 *  (be it {@link HSSFWorkbook} or XSSFWorkbook),
 *  by auto-detecting from the supplied input.
 */
public final class WorkbookFactory {

    private static class Singleton {
        private static final WorkbookFactory INSTANCE = new WorkbookFactory();
    }

    private interface ProviderMethod {
        Workbook create(WorkbookProvider prov) throws IOException;
    }

    private final List<WorkbookProvider> provider = new ArrayList<>();

    private WorkbookFactory() {
        ClassLoader cl = WorkbookFactory.class.getClassLoader();
        ServiceLoader.load(WorkbookProvider.class, cl).forEach(provider::add);
    }


    /**
     * Create a new empty Workbook, either XSSF or HSSF depending
     * on the parameter
     *
     * @param xssf If an XSSFWorkbook or a HSSFWorkbook should be created
     *
     * @return The created workbook
     *
     * @throws IOException if an error occurs while creating the objects
     */
    public static Workbook create(boolean xssf) throws IOException {
        return wp(xssf ? FileMagic.OOXML : FileMagic.OLE2, WorkbookProvider::create);
    }

    /**
     * Creates a HSSFWorkbook from the given POIFSFileSystem<p>
     *
     * Note that in order to properly release resources the
     * Workbook should be closed after use.
     *
     * @param fs The {@link POIFSFileSystem} to read the document from
     *
     * @return The created workbook
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static Workbook create(POIFSFileSystem fs) throws IOException {
        return create(fs, null);
    }

    /**
     * Creates a Workbook from the given POIFSFileSystem, which may
     *  be password protected
     *
     *  @param fs The {@link POIFSFileSystem} to read the document from
     *  @param password The password that should be used or null if no password is necessary.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     */
    private static Workbook create(final POIFSFileSystem fs, String password) throws IOException {
        return create(fs.getRoot(), password);
    }


    /**
     * Creates a Workbook from the given DirectoryNode.
     *
     * @param root The {@link DirectoryNode} to start reading the document from
     *
     * @return The created Workbook
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static Workbook create(final DirectoryNode root) throws IOException {
        return create(root, null);
    }


    /**
     * Creates a Workbook from the given DirectoryNode, which may
     * be password protected
     *
     * @param root The {@link DirectoryNode} to start reading the document from
     * @param password The password that should be used or null if no password is necessary.
     *
     * @return The created Workbook
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static Workbook create(final DirectoryNode root, String password) throws IOException {
        // Encrypted OOXML files go inside OLE2 containers, is this one?
        if (root.hasEntry(DEFAULT_POIFS_ENTRY) || root.hasEntry(OOXML_PACKAGE)) {
            return wp(FileMagic.OOXML, w -> w.create(root, password));
        } else {
            return wp(FileMagic.OLE2, w ->  w.create(root, password));
        }
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given InputStream.
     *
     * <p>Your input stream MUST either support mark/reset, or
     *  be wrapped as a {@link BufferedInputStream}!
     *  Note that using an {@link InputStream} has a higher memory footprint
     *  than using a {@link File}.</p>
     *
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use. Note also that loading
     *  from an InputStream requires more memory than loading
     *  from a File, so prefer {@link #create(File)} where possible.
     *
     *  @param inp The {@link InputStream} to read data from.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the Workbook given is password protected
     */
    public static Workbook create(InputStream inp) throws IOException, EncryptedDocumentException {
        return create(inp, null);
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given InputStream, which may be password protected.
     *
     * <p>Your input stream MUST either support mark/reset, or
     *  be wrapped as a {@link BufferedInputStream}!
     *  Note that using an {@link InputStream} has a higher memory footprint
     *  than using a {@link File}.</p>
     *
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use. Note also that loading
     *  from an InputStream requires more memory than loading
     *  from a File, so prefer {@link #create(File)} where possible.</p>
     *
     *  @param inp The {@link InputStream} to read data from.
     *  @param password The password that should be used or null if no password is necessary.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    public static Workbook create(InputStream inp, String password) throws IOException, EncryptedDocumentException {
        InputStream is = FileMagic.prepareToCheckMagic(inp);
        byte[] emptyFileCheck = new byte[1];
        is.mark(emptyFileCheck.length);
        if (is.read(emptyFileCheck) < emptyFileCheck.length) {
            throw new EmptyFileException();
        }
        is.reset();

        final FileMagic fm = FileMagic.valueOf(is);
        if (FileMagic.OOXML == fm) {
            return wp(fm, w -> w.create(is));
        }

        if (FileMagic.OLE2 != fm) {
            throw new IOException("Can't open workbook - unsupported file type: "+fm);
        }

        POIFSFileSystem poifs = new POIFSFileSystem(is);
        DirectoryNode root = poifs.getRoot();
        boolean isOOXML = root.hasEntry(DEFAULT_POIFS_ENTRY) || root.hasEntry(OOXML_PACKAGE);

        return wp(isOOXML ? FileMagic.OOXML : fm, w -> w.create(root, password));
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given File, which must exist and be readable.
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use.
     *
     *  @param file The file to read data from.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the Workbook given is password protected
     */
    public static Workbook create(File file) throws IOException, EncryptedDocumentException {
        return create(file, null);
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given File, which must exist and be readable, and
     *  may be password protected
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use.
     *
     *  @param file The file to read data from.
     *  @param password The password that should be used or null if no password is necessary.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    public static Workbook create(File file, String password) throws IOException, EncryptedDocumentException {
        return create(file, password, false);
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given File, which must exist and be readable, and
     *  may be password protected
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use.
     *
     *  @param file The file to read data from.
     *  @param password The password that should be used or null if no password is necessary.
     *  @param readOnly If the Workbook should be opened in read-only mode to avoid writing back
     *      changes when the document is closed.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    public static Workbook create(File file, String password, boolean readOnly) throws IOException, EncryptedDocumentException {
        if (!file.exists()) {
            throw new FileNotFoundException(file.toString());
        }

        if (file.length() == 0) {
            throw new EmptyFileException(file);
        }

        FileMagic fm = FileMagic.valueOf(file);
        if (fm == FileMagic.OOXML) {
            return wp(fm, w -> w.create(file, password, readOnly));
        } else if (fm == FileMagic.OLE2) {
            final boolean ooxmlEnc;
            try (POIFSFileSystem fs = new POIFSFileSystem(file, true)) {
                DirectoryNode root = fs.getRoot();
                ooxmlEnc = root.hasEntry(DEFAULT_POIFS_ENTRY) || root.hasEntry(OOXML_PACKAGE);
            }
            return wp(ooxmlEnc ? FileMagic.OOXML : fm, w -> w.create(file, password, readOnly));
        }

        return null;
    }


    private static Workbook wp(FileMagic fm, ProviderMethod fun) throws IOException {

        for (WorkbookProvider prov : Singleton.INSTANCE.provider) {
            if (prov.accepts(fm)) {
                return fun.create(prov);
            }
        }
        throw new IOException("Your InputStream was neither an OLE2 stream, nor an OOXML stream " +
            "or you haven't provide the poi-ooxml*.jar in the classpath/modulepath - FileMagic: " + fm +
                ", having providers: " + Singleton.INSTANCE.provider);
    }

    public static void addProvider(WorkbookProvider provider){
        Singleton.INSTANCE.provider.add(provider);
    }

    public static void removeProvider(Class<? extends WorkbookProvider> provider){
        Singleton.INSTANCE.provider.removeIf(p -> p.getClass().isAssignableFrom(provider));
    }
}
