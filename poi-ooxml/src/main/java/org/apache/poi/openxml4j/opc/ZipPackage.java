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

import static org.apache.poi.openxml4j.opc.ContentTypes.RELATIONSHIPS_PART;
import static org.apache.poi.openxml4j.opc.internal.ContentTypeManager.CONTENT_TYPES_PART_NAME;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.poi.UnsupportedFileFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.ODFNotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.internal.*;
import org.apache.poi.openxml4j.opc.internal.marshallers.ZipPartMarshaller;
import org.apache.poi.openxml4j.util.ZipArchiveThresholdInputStream;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.openxml4j.util.ZipFileZipEntrySource;
import org.apache.poi.openxml4j.util.ZipInputStreamZipEntrySource;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.TempFile;

/**
 * Physical zip package.
 */
public final class ZipPackage extends OPCPackage {
    private static final String MIMETYPE = "mimetype";
    private static final String SETTINGS_XML = "settings.xml";
    private static boolean useTempFilePackageParts = false;
    private static boolean encryptTempFilePackageParts = false;

    private static final Logger LOG = LogManager.getLogger(ZipPackage.class);

    /**
     * Zip archive, as either a file on disk,
     *  or a stream
     */
    private final ZipEntrySource zipArchive;

    /**
     * @param tempFilePackageParts whether to save package part data in temp files to save memory
     */
    public static void setUseTempFilePackageParts(boolean tempFilePackageParts) {
        useTempFilePackageParts = tempFilePackageParts;
    }

    /**
     * @param encryptTempFiles whether to encrypt package part temp files
     */
    public static void setEncryptTempFilePackageParts(boolean encryptTempFiles) {
        encryptTempFilePackageParts = encryptTempFiles;
    }

    /**
     * @return whether package part data is stored in temp files to save memory
     */
    public static boolean useTempFilePackageParts() {
        return useTempFilePackageParts;
    }

    /**
     * @return whether package part temp files are encrypted
     */
    public static boolean encryptTempFilePackageParts() {
        return encryptTempFilePackageParts;
    }

    /**
     * Constructor. Creates a new, empty ZipPackage.
     */
    public ZipPackage() {
        super(defaultPackageAccess);
        this.zipArchive = null;

        try {
            this.contentTypeManager = new ZipContentTypeManager(null, this);
        } catch (InvalidFormatException e) {
            LOG.atWarn().withThrowable(e).log("Could not parse ZipPackage");
        }
    }

    /**
     * Constructor. Opens a Zip based Open XML document from
     *  an InputStream.
     *
     * @param in
     *            Zip input stream to load.
     * @param access
     *            The package access mode.
     * @throws IllegalArgumentException
     *             If the specified input stream is not an instance of
     *             ZipInputStream.
     * @throws IOException
     *            if input stream cannot be opened, read, or closed
     */
    ZipPackage(InputStream in, PackageAccess access) throws IOException {
        super(access);
        ZipArchiveThresholdInputStream zis = ZipHelper.openZipStream(in); // NOSONAR
        try {
            this.zipArchive = new ZipInputStreamZipEntrySource(zis);
        } catch (final IOException | RuntimeException e) {
            IOUtils.closeQuietly(zis);
            throw e;
        }
    }

    /**
     * Constructor. Opens a Zip based Open XML document from a file.
     *
     * @param path
     *            The path of the file to open or create.
     * @param access
     *            The package access mode.
     * @throws InvalidOperationException If the zip file cannot be opened.
     */
    ZipPackage(String path, PackageAccess access) throws InvalidOperationException {
        this(new File(path), access);
    }

    /**
     * Constructor. Opens a Zip based Open XML document from a File.
     *
     * @param file
     *            The file to open or create.
     * @param access
     *            The package access mode.
     * @throws InvalidOperationException If the zip file cannot be opened.
     */
    ZipPackage(File file, PackageAccess access) throws InvalidOperationException {
        super(access);

        ZipEntrySource ze;
        try {
            final ZipFile zipFile = ZipHelper.openZipFile(file); // NOSONAR
            ze = new ZipFileZipEntrySource(zipFile);
        } catch (IOException e) {
            // probably not happening with write access - not sure how to handle the default read-write access ...
            if (access == PackageAccess.WRITE) {
                throw new InvalidOperationException("Can't open the specified file: '" + file + "'", e);
            }

            LOG.atWarn().log("Error in zip file {} - falling back to stream processing (i.e. ignoring zip central directory)", file);
            ze = openZipEntrySourceStream(file);
        }
        this.zipArchive = ze;
    }

    private static ZipEntrySource openZipEntrySourceStream(File file) throws InvalidOperationException {
        final FileInputStream fis;
        // Acquire a resource that is needed to read the next level of openZipEntrySourceStream
        try {
            // open the file input stream
            fis = new FileInputStream(file); // NOSONAR
        } catch (final FileNotFoundException e) {
            // If the source cannot be acquired, abort (no resources to free at this level)
            throw new InvalidOperationException("Can't open the specified file input stream from file: '" + file + "'", e);
        }

        // If an error occurs while reading the next level of openZipEntrySourceStream, free the acquired resource
        try {
            // read from the file input stream
            return openZipEntrySourceStream(fis);
        } catch (final InvalidOperationException|UnsupportedFileFormatException e) {
            // abort: close the zip input stream
            IOUtils.closeQuietly(fis);
            throw e;
        } catch (final Exception e) {
            // abort: close the file input stream
            IOUtils.closeQuietly(fis);
            throw new InvalidOperationException("Failed to read the file input stream from file: '" + file + "'", e);
        }
    }

    private static ZipEntrySource openZipEntrySourceStream(FileInputStream fis) throws InvalidOperationException {
        final ZipArchiveThresholdInputStream zis;
        // Acquire a resource that is needed to read the next level of openZipEntrySourceStream
        try {
            // open the zip input stream
            zis = ZipHelper.openZipStream(fis); // NOSONAR
        } catch (final IOException e) {
            // If the source cannot be acquired, abort (no resources to free at this level)
            throw new InvalidOperationException("Could not open the file input stream", e);
        }

        // If an error occurs while reading the next level of openZipEntrySourceStream, free the acquired resource
        try {
            // read from the zip input stream
            return openZipEntrySourceStream(zis);
        } catch (final InvalidOperationException|UnsupportedFileFormatException e) {
            // abort: close the zip input stream
            IOUtils.closeQuietly(zis);
            throw e;
        } catch (final Exception e) {
            // abort: close the zip input stream
            IOUtils.closeQuietly(zis);
            throw new InvalidOperationException("Failed to read the zip entry source stream", e);
        }
    }

    private static ZipEntrySource openZipEntrySourceStream(ZipArchiveThresholdInputStream zis) throws InvalidOperationException {
        // Acquire the final level resource. If this is acquired successfully, the zip package was read successfully from the input stream
        try {
            // open the zip entry source stream
            return new ZipInputStreamZipEntrySource(zis);
        } catch (IOException e) {
            throw new InvalidOperationException("Could not open the specified zip entry source stream", e);
        }
    }

    /**
     * Constructor. Opens a Zip based Open XML document from
     *  a custom ZipEntrySource, typically an open archive
     *  from another system
     *
     * @param zipEntry
     *            Zip data to load.
     * @param access
     *            The package access mode.
     */
    ZipPackage(ZipEntrySource zipEntry, PackageAccess access) {
        super(access);
        this.zipArchive = zipEntry;
    }

    /**
     * Retrieves the parts from this package. We assume that the package has not
     * been yet inspect to retrieve all the parts, this method will open the
     * archive and look for all parts contain inside it.
     *
     * @return All parts contain in this package.
     * @throws InvalidFormatException if the package is not valid.
     */
    @Override
    protected PackagePartCollection getPartsImpl() throws InvalidFormatException {
        final PackagePartCollection newPartList = new PackagePartCollection();

        if (zipArchive == null) {
            return newPartList;
        }

        // First we need to parse the content type part
        final ZipArchiveEntry contentTypeEntry =
                zipArchive.getEntry(CONTENT_TYPES_PART_NAME);
        if (contentTypeEntry != null) {
            if (this.contentTypeManager != null) {
                throw new InvalidFormatException("ContentTypeManager can only be created once. This must be a cyclic relation?");
            }
            try {
                this.contentTypeManager = new ZipContentTypeManager(
                        zipArchive.getInputStream(contentTypeEntry), this);
            } catch (IOException e) {
                throw new InvalidFormatException(e.getMessage(), e);
            }
        } else {
            // Is it a different Zip-based format?
            final boolean hasMimetype = zipArchive.getEntry(MIMETYPE) != null;
            final boolean hasSettingsXML = zipArchive.getEntry(SETTINGS_XML) != null;
            if (hasMimetype && hasSettingsXML) {
                throw new ODFNotOfficeXmlFileException(
                        "The supplied data appears to be in ODF (Open Document) Format. " +
                                "Formats like these (eg ODS, ODP) are not supported, try Apache ODFToolkit");
            }
            if (!zipArchive.getEntries().hasMoreElements()) {
                throw new NotOfficeXmlFileException(
                        "No valid entries or contents found, this is not a valid OOXML " +
                                "(Office Open XML) file");
            }
            // Fallback exception
            throw new InvalidFormatException(
                    "Package should contain a content type part [M1.13]");
        }

        // Now create all the relationships
        // (Need to create relationships before other
        //  parts, otherwise we might create a part before
        //  its relationship exists, and then it won't tie up)
        final List<EntryTriple> entries =
                Collections.list(zipArchive.getEntries()).stream()
                        .filter(zipArchiveEntry -> !ignoreEntry(zipArchiveEntry))
                        .map(zae -> new EntryTriple(zae, contentTypeManager))
                        .filter(mm -> mm.partName != null)
                        .sorted()
                        .collect(Collectors.toList());

        for (final EntryTriple et : entries) {
            et.register(newPartList);
        }

        return newPartList;
    }

    private static boolean ignoreEntry(ZipArchiveEntry zipArchiveEntry) {
        String name = zipArchiveEntry.getName();
        return name.startsWith("[trash]") || name.endsWith("/");
    }

    private class EntryTriple implements Comparable<EntryTriple> {
        final ZipArchiveEntry zipArchiveEntry;
        final PackagePartName partName;
        final String contentType;

        EntryTriple(final ZipArchiveEntry zipArchiveEntry, final ContentTypeManager contentTypeManager) {
            this.zipArchiveEntry = zipArchiveEntry;

            final String entryName = zipArchiveEntry.getName();
            PackagePartName ppn = null;
            // ignore trash parts
            if (!ignoreEntry(zipArchiveEntry)) {
                try {
                    // We get an error when we parse [Content_Types].xml
                    // because it's not a valid URI.
                    ppn = (CONTENT_TYPES_PART_NAME.equalsIgnoreCase(entryName)) ? null
                            : PackagingURIHelper.createPartName(ZipHelper.getOPCNameFromZipItemName(entryName));
                } catch (Exception e) {
                    // We assume we can continue, even in degraded mode ...
                    LOG.atWarn().withThrowable(e).log("Entry {} is not valid, so this part won't be added to the package.", entryName);
                }
            }

            this.partName = ppn;
            this.contentType = (ppn == null) ? null : contentTypeManager.getContentType(partName);
        }

        void register(final PackagePartCollection partList) throws InvalidFormatException {
            if (contentType == null) {
                throw new InvalidFormatException("The part " + partName.getURI().getPath() + " does not have any " +
                        "content type ! Rule: Package require content types when retrieving a part from a package. [M.1.14]");
            }

            if (partList.containsKey(partName)) {
                throw new InvalidFormatException(
                    "A part with the name '"+partName+"' already exists : Packages shall not contain equivalent part names " +
                    "and package implementers shall neither create nor recognize packages with equivalent part names. [M1.12]");
            }

            try {
                partList.put(partName, new ZipPackagePart(ZipPackage.this, zipArchiveEntry, partName, contentType, false));
            } catch (InvalidOperationException e) {
                throw new InvalidFormatException(e.getMessage(), e);
            }
        }

        @Override
        public int compareTo(EntryTriple o) {
            final int contentTypeOrder1 = RELATIONSHIPS_PART.equals(contentType) ? -1 : 1;
            final int contentTypeOrder2 = RELATIONSHIPS_PART.equals(o.contentType) ? -1 : 1;
            final int cmpCT = Integer.compare(contentTypeOrder1, contentTypeOrder2);
            return cmpCT != 0 ? cmpCT : partName.compareTo(o.partName);
        }
    }

    /**
     * Create a new MemoryPackagePart from the specified URI and content type
     *
     * @param partName
     *            The part name.
     * @param contentType
     *            The part content type.
     * @param loadRelationships
     *            whether to load relationships.
     * @return The newly created zip package part, else <b>null</b>.
     * @throws IllegalArgumentException if partName or contentType is null
     */
    @Override
    protected PackagePart createPartImpl(PackagePartName partName,
            String contentType, boolean loadRelationships) {
        if (contentType == null) {
            throw new IllegalArgumentException("contentType");
        }

        if (partName == null) {
            throw new IllegalArgumentException("partName");
        }

        try {
            if (useTempFilePackageParts) {
                if (encryptTempFilePackageParts) {
                    return new EncryptedTempFilePackagePart(this, partName, contentType, loadRelationships);
                } else {
                    return new TempFilePackagePart(this, partName, contentType, loadRelationships);
                }
            } else {
                return new MemoryPackagePart(this, partName, contentType, loadRelationships);
            }
        } catch (Exception e) {
            LOG.atWarn().withThrowable(e).log("Failed to create part {}", partName);
            return null;
        }
    }

    /**
     * Delete a part from the package
     *
     * @throws IllegalArgumentException
     *             Throws if the part URI is null or invalid.
     */
    @Override
    protected void removePartImpl(PackagePartName partName) {
        if (partName == null) {
            throw new IllegalArgumentException("partUri");
        }
    }

    /**
     * Flush the package. Do nothing.
     */
    @Override
    protected void flushImpl() {
        // Do nothing
    }

    /**
     * Close and save the package.
     *
     * @see #close()
     */
    @Override
    protected void closeImpl() throws IOException {
        // Flush the package
        flush();

        if (this.originalPackagePath == null || this.originalPackagePath.isEmpty()) {
            return;
        }

        // Save the content
        File targetFile = new File(this.originalPackagePath);
        if (!targetFile.exists()) {
            throw new InvalidOperationException(
                "Can't close a package not previously open with the open() method !");
        }

        // Case of a package previously open
        String tempFileName = generateTempFileName(FileHelper.getDirectory(targetFile));
        File tempFile = TempFile.createTempFile(tempFileName, ".tmp");

        // Save the final package to a temporary file
        boolean success = false;
        try {
            save(tempFile);
            success = true;
        } finally {
            // Close the current zip file, so we can overwrite it on all platforms
            IOUtils.closeQuietly(this.zipArchive);
            try {
                // Copy the new file over the old one if save() succeed
                if(success) {
                    FileHelper.copyFile(tempFile, targetFile);
                }
            } finally {
                // Either the save operation succeed or not, we delete the temporary file
                if (!tempFile.delete()) {
                    LOG.atWarn().log("The temporary file: '{}' cannot be deleted ! Make sure that no other application use it.", targetFile.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Create a unique identifier to be use as a temp file name.
     *
     * @return A unique identifier use to be use as a temp file name.
     */
    private synchronized String generateTempFileName(File directory) {
        File tmpFilename;
        do {
            tmpFilename = new File(directory.getAbsoluteFile() + File.separator
                    + "OpenXML4J" + System.nanoTime());
        } while (tmpFilename.exists());
        return FileHelper.getFilename(tmpFilename.getAbsoluteFile());
    }

    /**
     * Close the package without saving the document. Discard all the changes
     * made to this package.
     */
    @Override
    protected void revertImpl() {
        try {
            if (this.zipArchive != null) {
                this.zipArchive.close();
            }
        } catch (IOException e) {
            // Do nothing, user dont have to know
        }
    }

    /**
     * Save this package into the specified stream
     *
     *
     * @param outputStream
     *            The stream use to save this package.
     *
     * @see #save(OutputStream)
     */
    @Override
    public void saveImpl(OutputStream outputStream) {
        // Check that the document was open in write mode
        throwExceptionIfReadOnly();

        final ZipArchiveOutputStream zos = (outputStream instanceof ZipArchiveOutputStream)
            ? (ZipArchiveOutputStream) outputStream : new ZipArchiveOutputStream(outputStream);

        try {
            // If the core properties part does not exist in the part list,
            // we save it as well
            if (this.getPartsByRelationshipType(PackageRelationshipTypes.CORE_PROPERTIES).isEmpty() &&
                this.getPartsByRelationshipType(PackageRelationshipTypes.CORE_PROPERTIES_ECMA376).isEmpty()) {
                LOG.atDebug().log("Save core properties part");

                // Ensure that core properties are added if missing
                getPackageProperties();
                // Add core properties to part list ...
                addPackagePart(this.packageProperties);
                // ... and to add its relationship ...
                this.relationships.addRelationship(this.packageProperties
                        .getPartName().getURI(), TargetMode.INTERNAL,
                        PackageRelationshipTypes.CORE_PROPERTIES, null);
                // ... and the content if it has not been added yet.
                if (!this.contentTypeManager
                        .isContentTypeRegister(ContentTypes.CORE_PROPERTIES_PART)) {
                    this.contentTypeManager.addContentType(
                            this.packageProperties.getPartName(),
                            ContentTypes.CORE_PROPERTIES_PART);
                }
            }

            // Save content type part.
            LOG.atDebug().log("Save content types part");
            this.contentTypeManager.save(zos);

            // Save package relationships part.
            LOG.atDebug().log("Save package relationships");
            ZipPartMarshaller.marshallRelationshipPart(this.getRelationships(),
                    PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_PART_NAME,
                    zos);

            // Save parts.
            for (PackagePart part : getParts()) {
                // If the part is a relationship part, we don't save it, it's
                // the source part that will do the job.
                if (part.isRelationshipPart()) {
                    continue;
                }

                final PackagePartName ppn = part.getPartName();
                LOG.atDebug().log(() -> new SimpleMessage("Save part '" + ZipHelper.getZipItemNameFromOPCName(ppn.getName()) + "'"));
                final PartMarshaller marshaller = partMarshallers.get(part._contentType);

                final PartMarshaller pm = (marshaller != null) ? marshaller : defaultPartMarshaller;
                if (!pm.marshall(part, zos)) {
                    String errMsg = "The part " + ppn.getURI() + " failed to be saved in the stream with marshaller " + pm +
                            ". Enable logging via Log4j 2 for more details.";
                    throw new OpenXML4JException(errMsg);
                }
            }

            zos.finish();
        } catch (OpenXML4JRuntimeException e) {
            // no need to wrap this type of Exception
            throw e;
        } catch (Exception e) {
            throw new OpenXML4JRuntimeException(
                "Fail to save: an error occurs while saving the package : "
                + e.getMessage(), e);
        }
    }

    /**
     * Get the zip archive
     *
     * @return The zip archive.
     */
    public ZipEntrySource getZipArchive() {
        return zipArchive;
    }

    @Override
    public boolean isClosed() {
        // if zipArchive == null, it might be created on the fly
        // so only return true, if a zip archive was initialized before
        return zipArchive != null && zipArchive.isClosed();
    }
}
