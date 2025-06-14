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

package org.apache.poi.openxml4j.opc;

import static org.apache.poi.openxml4j.opc.ContentTypes.PLAIN_OLD_XML;
import static org.apache.poi.openxml4j.opc.ContentTypes.RELATIONSHIPS_PART;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.opc.internal.InvalidZipException;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.ZipContentTypeManager;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.TempFileCreationStrategy;

/**
 * Defines a class to open OPC packages with particular configuration.
 *
 * @since POI 5.5.0
 */
public final class OPCPackageOpener {
    private static final OPCComplianceFlags defaultOpcComplianceFlags = OPCComplianceFlags.enforceAll();

    /**
     * Package access.
     */
    private PackageAccess packageAccess;

    /**
     * Whether OPC compliance requirements are checked (e.g., M4.2, M4.3, M4.4, and M4.5)
     */
    private OPCComplianceFlags opcComplianceFlags;

    /**
     * Strategy to create temporary files.
     */
    private TempFileCreationStrategy tmpStrategy;

    public OPCPackageOpener() {
        this.packageAccess = OPCPackage.defaultPackageAccess;
        this.opcComplianceFlags = defaultOpcComplianceFlags;
        this.tmpStrategy = TempFileCreationStrategy.getDefaultStrategy();
    }

    /**
     * Sets the package access.
     *
     * @param packageAccess the package access. May not be {@code null}.
     */
    public void setPackageAccess(PackageAccess packageAccess) {
        this.packageAccess = Objects.requireNonNull(packageAccess, "packageAccess");
    }

    /**
     * Sets whether OPC compliance requirements are checked (e.g., M4.2, M4.3, M4.4, and M4.5).
     *
     * @param opcComplianceFlags whether OPC compliance requirements are checked (e.g., M4.2, M4.3, M4.4, and M4.5).
     *   May not be {@code null}.
     */
    public void setOpcComplianceFlags(OPCComplianceFlags opcComplianceFlags) {
        this.opcComplianceFlags = Objects.requireNonNull(opcComplianceFlags, "opcComplianceFlags");
    }

    /**
     * Sets the strategy to create temporary files.
     *
     * @param tmpStrategy the strategy to create temporary files. May not be {@code null}.
     */
    public void setTmpStrategy(TempFileCreationStrategy tmpStrategy) {
        this.tmpStrategy = Objects.requireNonNull(tmpStrategy, "tmpStrategy");
    }

    /**
     * Open a user provided {@link ZipEntrySource} with read-only permission.
     * This method can be used to stream data into POI.
     * Opposed to other open variants, the data is read as-is, e.g. there aren't
     * any zip-bomb protection put in place.
     *
     * @param zipEntry the custom source
     * @return A Package object
     * @throws InvalidFormatException if a parsing error occur.
     */
    public OPCPackage open(ZipEntrySource zipEntry) throws InvalidFormatException {
        OPCPackage pack = new ZipPackage(zipEntry, packageAccess, opcComplianceFlags, tmpStrategy);
        try {
            if (pack.partList == null) {
                pack.getParts();
            }
            // pack.originalPackagePath = file.getAbsolutePath();
            return pack;
        } catch (InvalidFormatException | RuntimeException e) {
            // use revert() to free resources when the package is opened read-only
            pack.revert();

            throw e;
        }
    }

    /**
     * Open a package with read/write permission.
     *
     * @param path
     *            The document path.
     * @return A Package object, else <b>null</b>.
     * @throws InvalidFormatException
     *             If the specified file doesn't exist, and a parsing error
     *             occur.
     */
    public OPCPackage open(String path)
            throws InvalidFormatException, InvalidOperationException {
        if (StringUtil.isBlank(path)) {
            throw new IllegalArgumentException("'path' must be given");
        }

        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            throw new IllegalArgumentException("path must not be a directory");
        }

        OPCPackage pack = new ZipPackage(path, packageAccess, opcComplianceFlags, tmpStrategy); // NOSONAR
        boolean success = false;
        if (pack.partList == null && packageAccess != PackageAccess.WRITE) {
            try {
                pack.getParts();
                success = true;
            } finally {
                if (! success) {
                    IOUtils.closeQuietly(pack);
                }
            }
        }

        pack.originalPackagePath = new File(path).getAbsolutePath();
        return pack;
    }

    /**
     * Open a package with read/write permission.
     *
     * @param file
     *            The file to open.
     * @return A Package object, else <b>null</b>.
     * @throws InvalidFormatException
     *             If the specified file doesn't exist, and a parsing error
     *             occur.
     */
    public OPCPackage open(File file)
            throws InvalidFormatException {
        if (file == null) {
            throw new IllegalArgumentException("'file' must be given");
        }
        if (file.exists() && file.isDirectory()) {
            throw new IllegalArgumentException("file must not be a directory");
        }

        final OPCPackage pack;
        try {
            pack = new ZipPackage(file, packageAccess, opcComplianceFlags, tmpStrategy); //NOSONAR
        } catch (InvalidOperationException e) {
            throw new InvalidFormatException(e.getMessage(), e);
        }
        try {
            if (pack.partList == null && packageAccess != PackageAccess.WRITE) {
                pack.getParts();
            }
            pack.originalPackagePath = file.getAbsolutePath();
            return pack;
        } catch (InvalidFormatException | RuntimeException e) {
            if (packageAccess == PackageAccess.READ) {
                pack.revert();
            } else {
                IOUtils.closeQuietly(pack);
            }
            throw e;
        }
    }

    /**
     * Open a package.
     *
     * Note - uses quite a bit more memory than {@link #open(String)}, which
     * doesn't need to hold the whole zip file in memory, and can take advantage
     * of native methods
     *
     * @param in
     *            The InputStream to read the package from. The stream is closed.
     * @return A PackageBase object
     *
     * @throws InvalidFormatException
     *              Throws if the specified file exist and is not valid.
     * @throws IOException If reading the stream fails
     */
    public OPCPackage open(InputStream in) throws InvalidFormatException,
            IOException {
        final OPCPackage pack;
        try {
            pack = new ZipPackage(in, packageAccess, opcComplianceFlags, tmpStrategy);
        } catch (InvalidZipException e) {
            throw new InvalidFormatException(e.getMessage(), e);
        }
        try {
            if (pack.partList == null) {
                pack.getParts();
            }
        } catch (InvalidFormatException | RuntimeException e) {
            IOUtils.closeQuietly(pack);
            throw e;
        }
        return pack;
    }

    /**
     * Open a package.
     *
     * Note - uses quite a bit more memory than {@link #open(String)}, which
     * doesn't need to hold the whole zip file in memory, and can take advantage
     * of native methods
     *
     * @param in
     *            The InputStream to read the package from.
     * @param closeStream
     *            Whether to close the input stream.
     * @return A PackageBase object
     *
     * @throws InvalidFormatException
     *              Throws if the specified file exist and is not valid.
     * @throws IOException If reading the stream fails
     */
    public OPCPackage open(InputStream in, boolean closeStream) throws InvalidFormatException,
            IOException {
        final OPCPackage pack;
        try {
            pack = new ZipPackage(in, packageAccess, closeStream, opcComplianceFlags, tmpStrategy);
        } catch (InvalidZipException e) {
            throw new InvalidFormatException(e.getMessage(), e);
        }
        try {
            if (pack.partList == null) {
                pack.getParts();
            }
        } catch (InvalidFormatException | RuntimeException e) {
            IOUtils.closeQuietly(pack);
            throw e;
        }
        return pack;
    }

    /**
     * Opens a package if it exists, else it creates one.
     *
     * @param file
     *            The file to open or to create.
     * @return A newly created package if the specified file does not exist,
     *         else the package extract from the file.
     * @throws InvalidFormatException
     *             Throws if the specified file exist and is not valid.
     */
    public OPCPackage openOrCreate(File file) throws InvalidFormatException {
        if (file.exists()) {
            return open(file.getAbsolutePath());
        } else {
            return create(file);
        }
    }

    /**
     * Creates a new package.
     *
     * @param path
     *            Path of the document.
     * @return A newly created PackageBase ready to use.
     */
    public OPCPackage create(String path) {
        return create(new File(path));
    }

    /**
     * Creates a new package.
     *
     * @param file
     *            Path of the document.
     * @return A newly created PackageBase ready to use.
     */
    public OPCPackage create(File file) {
        if (file == null || (file.exists() && file.isDirectory())) {
            throw new IllegalArgumentException("file");
        }

        if (file.exists()) {
            throw new InvalidOperationException(
                    "This package (or file) already exists : use the open() method or delete the file.");
        }

        // Creates a new package
        OPCPackage pkg = newZipPackage();
        pkg.originalPackagePath = file.getAbsolutePath();

        configurePackage(pkg);
        return pkg;
    }

    public OPCPackage create(OutputStream output) {
        OPCPackage pkg = newZipPackage();
        pkg.originalPackagePath = null;
        pkg.output = output;

        configurePackage(pkg);
        return pkg;
    }

    private ZipPackage newZipPackage() {
        return new ZipPackage(packageAccess, opcComplianceFlags, tmpStrategy);
    }

    private static void configurePackage(OPCPackage pkg) {
        try {
            // Content type manager
            pkg.contentTypeManager = new ZipContentTypeManager(null, pkg);

            // Add default content types for .xml and .rels
            pkg.contentTypeManager.addContentType(
                    PackagingURIHelper.createPartName(
                            PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_URI),
                    RELATIONSHIPS_PART);
            pkg.contentTypeManager.addContentType(
                    PackagingURIHelper.createPartName("/default.xml"),
                    PLAIN_OLD_XML);

            // Initialise some PackageBase properties
            pkg.packageProperties = new PackagePropertiesPart(pkg,
                    PackagingURIHelper.CORE_PROPERTIES_PART_NAME);
            pkg.packageProperties.setCreatorProperty("Generated by Apache POI OpenXML4J");
            pkg.packageProperties.setCreatedProperty(Optional.of(new Date()));
        } catch (InvalidFormatException e) {
            // Should never happen
            throw new IllegalStateException(e);
        }
    }
}
