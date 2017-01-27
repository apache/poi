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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.poi.EmptyFileException;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.hssf.record.crypto.Biff8EncryptionKey;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Factory for creating the appropriate kind of Workbook
 *  (be it {@link HSSFWorkbook} or {@link XSSFWorkbook}),
 *  by auto-detecting from the supplied input.
 */
public class WorkbookFactory {
    /**
     * Creates a HSSFWorkbook from the given POIFSFileSystem
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use.
     */
    public static Workbook create(POIFSFileSystem fs) throws IOException {
        return new HSSFWorkbook(fs);
    }

    /**
     * Creates a HSSFWorkbook from the given NPOIFSFileSystem
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use.
     */
    public static Workbook create(NPOIFSFileSystem fs) throws IOException {
        try {
            return create(fs, null);
        } catch (InvalidFormatException e) {
            // Special case of OOXML-in-POIFS which is broken
            throw new IOException(e);
        }
    }

    /**
     * Creates a Workbook from the given NPOIFSFileSystem, which may
     *  be password protected
     *
     *  @param fs The {@link NPOIFSFileSystem} to read the document from
     *  @param password The password that should be used or null if no password is necessary.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws InvalidFormatException if the contents of the file cannot be parsed into a {@link Workbook}
     */
    private static Workbook create(final NPOIFSFileSystem fs, String password) throws IOException, InvalidFormatException {
        DirectoryNode root = fs.getRoot();

        // Encrypted OOXML files go inside OLE2 containers, is this one?
        if (root.hasEntry(Decryptor.DEFAULT_POIFS_ENTRY)) {
            InputStream stream = DocumentFactoryHelper.getDecryptedStream(fs, password);

            OPCPackage pkg = OPCPackage.open(stream);
            return create(pkg);
        }

        // If we get here, it isn't an encrypted XLSX file
        // So, treat it as a regular HSSF XLS one
        if (password != null) {
            Biff8EncryptionKey.setCurrentUserPassword(password);
        }
        try {
            return new HSSFWorkbook(root, true);
        } finally {
            Biff8EncryptionKey.setCurrentUserPassword(null);
        }
    }

    /**
     * Creates a XSSFWorkbook from the given OOXML Package
     *
     * <p>Note that in order to properly release resources the
     *  Workbook should be closed after use.</p>
     *
     *  @param pkg The {@link OPCPackage} opened for reading data.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     */
    public static Workbook create(OPCPackage pkg) throws IOException {
        return new XSSFWorkbook(pkg);
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given InputStream.
     *
     * <p>Your input stream MUST either support mark/reset, or
     *  be wrapped as a {@link PushbackInputStream}! Note that
     *  using an {@link InputStream} has a higher memory footprint
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
     *  @throws InvalidFormatException if the contents of the file cannot be parsed into a {@link Workbook}
     *  @throws EncryptedDocumentException If the workbook given is password protected
     */
    public static Workbook create(InputStream inp) throws IOException, InvalidFormatException, EncryptedDocumentException {
        return create(inp, null);
    }

    /**
     * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
     *  the given InputStream, which may be password protected.
     * <p>Your input stream MUST either support mark/reset, or
     *  be wrapped as a {@link PushbackInputStream}! Note that
     *  using an {@link InputStream} has a higher memory footprint
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
     *  @throws InvalidFormatException if the contents of the file cannot be parsed into a {@link Workbook}
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     *  @throws EmptyFileException If an empty stream is given
     */
    public static Workbook create(InputStream inp, String password) throws IOException, InvalidFormatException, EncryptedDocumentException {
        // If clearly doesn't do mark/reset, wrap up
        if (! inp.markSupported()) {
            inp = new PushbackInputStream(inp, 8);
        }

        // Ensure that there is at least some data there
        byte[] header8 = IOUtils.peekFirst8Bytes(inp);

        // Try to create
        if (NPOIFSFileSystem.hasPOIFSHeader(header8)) {
            NPOIFSFileSystem fs = new NPOIFSFileSystem(inp);
            return create(fs, password);
        }
        if (DocumentFactoryHelper.hasOOXMLHeader(inp)) {
            return new XSSFWorkbook(OPCPackage.open(inp));
        }
        throw new InvalidFormatException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
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
     *  @throws InvalidFormatException if the contents of the file cannot be parsed into a {@link Workbook}
     *  @throws EncryptedDocumentException If the workbook given is password protected
     */
    public static Workbook create(File file) throws IOException, InvalidFormatException, EncryptedDocumentException {
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
     *  @throws InvalidFormatException if the contents of the file cannot be parsed into a {@link Workbook}
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     *  @throws EmptyFileException If an empty stream is given
     */
    public static Workbook create(File file, String password) throws IOException, InvalidFormatException, EncryptedDocumentException {
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
     *  	changes when the document is closed.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws InvalidFormatException if the contents of the file cannot be parsed into a {@link Workbook}
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     *  @throws EmptyFileException If an empty stream is given
     */
    public static Workbook create(File file, String password, boolean readOnly) throws IOException, InvalidFormatException, EncryptedDocumentException {
        if (! file.exists()) {
            throw new FileNotFoundException(file.toString());
        }

        try {
            NPOIFSFileSystem fs = new NPOIFSFileSystem(file, readOnly);
            try {
                return create(fs, password);
            } catch (RuntimeException e) {
                // ensure that the file-handle is closed again
                IOUtils.closeQuietly(fs);
                throw e;
            }
        } catch(OfficeXmlFileException e) {
            // opening as .xls failed => try opening as .xlsx
            OPCPackage pkg = OPCPackage.open(file, readOnly ? PackageAccess.READ : PackageAccess.READ_WRITE); // NOSONAR
            try {
                return new XSSFWorkbook(pkg);
            } catch (Exception ioe) {
                // ensure that file handles are closed - use revert() to not re-write the file
                pkg.revert();
                // do not pkg.close();

                if (ioe instanceof IOException) {
                    throw (IOException)ioe;
                } else if (ioe instanceof RuntimeException) {
                    throw (RuntimeException)ioe;
                } else {
                    throw new IOException(ioe);
                }
            }
        }
    }
}
