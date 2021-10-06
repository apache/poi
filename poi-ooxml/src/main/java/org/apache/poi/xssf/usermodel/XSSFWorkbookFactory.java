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

package org.apache.poi.xssf.usermodel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.DocumentFactoryHelper;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookProvider;
import org.apache.poi.util.Internal;

@Internal
public class XSSFWorkbookFactory implements WorkbookProvider {

    @Override
    public boolean accepts(FileMagic fm) {
        return fm == FileMagic.OOXML;
    }

    /**
     * Create a new empty Workbook
     *
     * @return The created workbook
     */
    @Override
    public XSSFWorkbook create() {
        return new XSSFWorkbook();
    }

    @Override
    public XSSFWorkbook create(DirectoryNode root, String password) throws IOException {
        try (InputStream stream = DocumentFactoryHelper.getDecryptedStream(root, password)) {
            return create(stream);
            // don't close root.getFileSystem() otherwise the container filesystem becomes invalid
        }
    }

    @Override
    public Workbook create(InputStream inp, String password) throws IOException {
        InputStream bufInp = FileMagic.prepareToCheckMagic(inp);
        FileMagic fm = FileMagic.valueOf(bufInp);

        if (fm == FileMagic.OLE2) {
            try (POIFSFileSystem poifs = new POIFSFileSystem(bufInp);
                 InputStream stream = DocumentFactoryHelper.getDecryptedStream(poifs.getRoot(), password)) {
                return create(stream);
            }
        }

        if (fm == FileMagic.OOXML) {
            return create(bufInp);
        }

        return null;
    }

    /**
     * Creates a XSSFWorkbook from the given InputStream
     *
     * <p>Note that in order to properly release resources the
     * Workbook should be closed after use.</p>
     *
     * @param stream The {@link InputStream} to read data from.
     *
     * @return The created Workbook
     *
     * @throws IOException if an error occurs while reading the data
     */
    @SuppressWarnings("resource")
    @Override
    public XSSFWorkbook create(InputStream stream) throws IOException {
        try {
            OPCPackage pkg = OPCPackage.open(stream);
            return createWorkbook(pkg);
        } catch (InvalidFormatException e) {
            throw new IOException(e);
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
    public static XSSFWorkbook createWorkbook(OPCPackage pkg) throws IOException {
        try {
            return new XSSFWorkbook(pkg);
        } catch (RuntimeException ioe) {
            // ensure that file handles are closed (use revert() to not re-write the file)
            pkg.revert();
            //pkg.close();

            // rethrow exception
            throw ioe;
        }
    }

    /**
     * Creates the XSSFWorkbook from the given File, which must exist and be readable.
     * <p>Note that in order to properly release resources the Workbook should be closed after use.
     *
     *  @param file The file to read data from.
     *  @param readOnly If the Workbook should be opened in read-only mode to avoid writing back
     *      changes when the document is closed.
     *
     *  @return The created Workbook
     *
     *  @throws IOException if an error occurs while reading the data
     *  @throws EncryptedDocumentException If the wrong password is given for a protected file
     */
    @Override
    @SuppressWarnings("resource")
    public XSSFWorkbook create(File file, String password, boolean readOnly) throws IOException {
        FileMagic fm = FileMagic.valueOf(file);

        if (fm == FileMagic.OLE2) {
            try (POIFSFileSystem poifs = new POIFSFileSystem(file, true);
                 InputStream stream = DocumentFactoryHelper.getDecryptedStream(poifs.getRoot(), password)) {
                return create(stream);
            }
        }

        try {
            OPCPackage pkg = OPCPackage.open(file, readOnly ? PackageAccess.READ : PackageAccess.READ_WRITE);
            return createWorkbook(pkg);
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        }
    }
}
