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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.OldFileFormatException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Removal;

/**
 * Factory for creating the appropriate kind of Workbook
 *  (be it {@link HSSFWorkbook} or XSSFWorkbook),
 *  by auto-detecting from the supplied input.
 */
public class WorkbookFactory {
    /**
     * Create a new empty Workbook, either XSSF or HSSF depending
     * on the parameter
     *
     * @param xssf If an XSSFWorkbook or a HSSFWorkbook should be created
     *
     * @return The created workbook
     *
     * @throws IOException if an error occurs while reading the data
     */
    public static Workbook create(boolean xssf) throws IOException {
        if(xssf) {
            return createXSSFWorkbook();
        } else {
            return createHSSFWorkbook();
        }
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
        if (root.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
            InputStream stream = null;
            try {
                stream = DocumentFactoryHelper.getDecryptedStream(root, password);

                return createXSSFWorkbook(stream);
            } finally {
                IOUtils.closeQuietly(stream);

                // as we processed the full stream already, we can close the filesystem here
                // otherwise file handles are leaked
                root.getFileSystem().close();
            }
        }

        // If we get here, it isn't an encrypted PPTX file
        // So, treat it as a regular HSLF PPT one
        boolean passwordSet = false;
        if (password != null) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
            passwordSet = true;
        }
        try {
            return createHSSFWorkbook(root);
        } finally {
            if (passwordSet) {
                Biff8EncryptionKey.setCurrentUserPassword(null);
            }
        }
    }

    /**
     * Creates a XSSFWorkbook from the given OOXML Package.
     * As the WorkbookFactory is located in the POI module, which doesn't know about the OOXML formats,
     * this can be only achieved by using an Object reference to the OPCPackage.
     *
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use.</p>
     *
     *  @param pkg The {@link org.apache.poi.openxml4j.opc.OPCPackage} opened for reading data.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *
     * @deprecated use XSSFWorkbookFactory.create
     */
    @Deprecated
    @Removal(version = "4.2.0")
    public static Workbook create(Object pkg) throws IOException {
        return createXSSFWorkbook(pkg);
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
        FileMagic fm = FileMagic.valueOf(is);

        switch (fm) {
            case OLE2:
                POIFSFileSystem fs = new POIFSFileSystem(is);
                return create(fs, password);
            case OOXML:
                return createXSSFWorkbook(is);
            default:
                throw new IOException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
        }
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

        POIFSFileSystem fs = null;
        try {
            fs = new POIFSFileSystem(file, readOnly);
            return create(fs, password);
        } catch(OfficeXmlFileException e) {
            IOUtils.closeQuietly(fs);
            return createXSSFWorkbook(file, readOnly);
        } catch(RuntimeException e) {
            IOUtils.closeQuietly(fs);
            throw e;
        }
    }

    private static Workbook createHSSFWorkbook(Object... args) throws IOException, EncryptedDocumentException {
        return createWorkbook("org.apache.poi.hssf.usermodel.HSSFWorkbookFactory", args);
    }

    private static Workbook createXSSFWorkbook(Object... args) throws IOException, EncryptedDocumentException {
        return createWorkbook("org.apache.poi.xssf.usermodel.XSSFWorkbookFactory", args);
    }

    /**
     * Does the actual call to HSSF or XSSF to do the creation.
     * Uses reflection, so that this class can be in the Core non-OOXML
     *  POI jar without errors / broken references to the OOXML / XSSF code.
     */
    private static Workbook createWorkbook(String factoryClass, Object[] args) throws IOException, EncryptedDocumentException {
        try {
            Class<?> clazz = WorkbookFactory.class.getClassLoader().loadClass(factoryClass);
            Class<?>[] argsClz = new Class<?>[args.length];
            int i=0;
            for (Object o : args) {
                Class<?> c = o.getClass();
                if (Boolean.class.isAssignableFrom(c)) {
                    c = boolean.class;
                } else if (InputStream.class.isAssignableFrom(c)) {
                    c = InputStream.class;
                } else if (File.class.isAssignableFrom(c)) {
                    c = File.class;
                }
                argsClz[i++] = c;
            }
            Method m = clazz.getMethod("createWorkbook", argsClz);
            return (Workbook)m.invoke(null, args);
        } catch (InvocationTargetException e) {
            Throwable t = e.getCause();
            if (t instanceof IOException) {
                throw (IOException)t;
            } else if (t instanceof EncryptedDocumentException) {
                throw (EncryptedDocumentException)t;
            } else if (t instanceof OldFileFormatException) {
                throw (OldFileFormatException)t;
            } else if (t instanceof RuntimeException) {
                throw (RuntimeException)t;
            } else {
                throw new IOException(t.getMessage(), t);
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

}
