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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.ODFNotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.internal.ContentTypeManager;
import org.apache.poi.openxml4j.opc.internal.FileHelper;
import org.apache.poi.openxml4j.opc.internal.MemoryPackagePart;
import org.apache.poi.openxml4j.opc.internal.PartMarshaller;
import org.apache.poi.openxml4j.opc.internal.ZipContentTypeManager;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.openxml4j.opc.internal.marshallers.ZipPartMarshaller;
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.openxml4j.util.ZipFileZipEntrySource;
import org.apache.poi.openxml4j.util.ZipInputStreamZipEntrySource;
import org.apache.poi.openxml4j.util.ZipSecureFile.ThresholdInputStream;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.TempFile;

/**
 * Physical zip package.
 */
public final class ZipPackage extends OPCPackage {
    private static final String MIMETYPE = "mimetype";
    private static final String SETTINGS_XML = "settings.xml";

    private static final POILogger logger = POILogFactory.getLogger(ZipPackage.class);

    /**
     * Zip archive, as either a file on disk,
     *  or a stream
     */
    private final ZipEntrySource zipArchive;

    /**
     * Constructor. Creates a new, empty ZipPackage.
     */
    public ZipPackage() {
        super(defaultPackageAccess);
        this.zipArchive = null;

        try {
            this.contentTypeManager = new ZipContentTypeManager(null, this);
        } catch (InvalidFormatException e) {
            logger.log(POILogger.WARN,"Could not parse ZipPackage", e);
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
     *             If the specified input stream not an instance of
     *             ZipInputStream.
     * @throws IOException
     *            if input stream cannot be opened, read, or closed
     */
    ZipPackage(InputStream in, PackageAccess access) throws IOException {
        super(access);
        ThresholdInputStream zis = ZipHelper.openZipStream(in);
        try {
            this.zipArchive = new ZipInputStreamZipEntrySource(zis);
        } catch (final IOException e) {
            try {
                zis.close();
            } catch (final IOException e2) {
                throw new IOException("Failed to close zip input stream while cleaning up. " + e.getMessage(), e2);
            }
            throw new IOException("Failed to read zip entry source", e);
        }
    }

    /**
     * Constructor. Opens a Zip based Open XML document from a file.
     *
     * @param path
     *            The path of the file to open or create.
     * @param access
     *            The package access mode.
     * @throws InvalidOperationException
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
     * @throws InvalidOperationException
     */
    ZipPackage(File file, PackageAccess access) throws InvalidOperationException {
        super(access);

        ZipEntrySource ze;
        try {
            final ZipFile zipFile = ZipHelper.openZipFile(file);
            ze = new ZipFileZipEntrySource(zipFile);
        } catch (IOException e) {
            // probably not happening with write access - not sure how to handle the default read-write access ...
            if (access == PackageAccess.WRITE) {
                throw new InvalidOperationException("Can't open the specified file: '" + file + "'", e);
            }
            logger.log(POILogger.ERROR, "Error in zip file "+file+" - falling back to stream processing (i.e. ignoring zip central directory)");
            ze = openZipEntrySourceStream(file);
        }
        this.zipArchive = ze;
    }
    
    private static ZipEntrySource openZipEntrySourceStream(File file) throws InvalidOperationException {
        final FileInputStream fis;
        // Acquire a resource that is needed to read the next level of openZipEntrySourceStream
        try {
            // open the file input stream
            fis = new FileInputStream(file);
        } catch (final FileNotFoundException e) {
            // If the source cannot be acquired, abort (no resources to free at this level)
            throw new InvalidOperationException("Can't open the specified file input stream from file: '" + file + "'", e);
        }
        
        // If an error occurs while reading the next level of openZipEntrySourceStream, free the acquired resource
        try {
            // read from the file input stream
            return openZipEntrySourceStream(fis);
        } catch (final Exception e) {
            try {
                // abort: close the file input stream
                fis.close();
            } catch (final IOException e2) {
                throw new InvalidOperationException("Could not close the specified file input stream from file: '" + file + "'", e2);
            }
            throw new InvalidOperationException("Failed to read the file input stream from file: '" + file + "'", e);
        }
    }
    
    private static ZipEntrySource openZipEntrySourceStream(FileInputStream fis) throws InvalidOperationException {
        final ThresholdInputStream zis;
        // Acquire a resource that is needed to read the next level of openZipEntrySourceStream
        try {
            // open the zip input stream
            zis = ZipHelper.openZipStream(fis);
        } catch (final IOException e) {
            // If the source cannot be acquired, abort (no resources to free at this level)
            throw new InvalidOperationException("Could not open the file input stream", e);
        }
        
        // If an error occurs while reading the next level of openZipEntrySourceStream, free the acquired resource
        try {
            // read from the zip input stream
            return openZipEntrySourceStream(zis);
        } catch (final Exception e) {
            try {
                // abort: close the zip input stream
                zis.close();
            } catch (final IOException e2) {
                throw new InvalidOperationException("Failed to read the zip entry source stream and could not close the zip input stream", e2);
            }
            throw new InvalidOperationException("Failed to read the zip entry source stream", e);
        }
    }
    
    private static ZipEntrySource openZipEntrySourceStream(ThresholdInputStream zis) throws InvalidOperationException {
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
     * archive and look for all parts contain inside it. If the package part
     * list is not empty, it will be emptied.
     *
     * @return All parts contain in this package.
     * @throws InvalidFormatException
     *             Throws if the package is not valid.
     */
    @Override
    protected PackagePart[] getPartsImpl() throws InvalidFormatException {
        if (this.partList == null) {
            // The package has just been created, we create an empty part
            // list.
            this.partList = new PackagePartCollection();
        }

        if (this.zipArchive == null) {
            return this.partList.values().toArray(
                    new PackagePart[this.partList.values().size()]);
        }

        // First we need to parse the content type part
        Enumeration<? extends ZipEntry> entries = this.zipArchive.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equalsIgnoreCase(
                    ContentTypeManager.CONTENT_TYPES_PART_NAME)) {
                try {
                    this.contentTypeManager = new ZipContentTypeManager(
                            getZipArchive().getInputStream(entry), this);
                } catch (IOException e) {
                    throw new InvalidFormatException(e.getMessage(), e);
                }
                break;
            }
        }

        // At this point, we should have loaded the content type part
        if (this.contentTypeManager == null) {
            // Is it a different Zip-based format?
            int numEntries = 0;
            boolean hasMimetype = false;
            boolean hasSettingsXML = false;
            entries = this.zipArchive.getEntries();
            while (entries.hasMoreElements()) {
                final ZipEntry entry = entries.nextElement();
                final String name = entry.getName();
                if (MIMETYPE.equals(name)) {
                    hasMimetype = true;
                }
                if (SETTINGS_XML.equals(name)) {
                    hasSettingsXML = true;
                }
                numEntries++;
            }
            if (hasMimetype && hasSettingsXML) {
                throw new ODFNotOfficeXmlFileException(
                   "The supplied data appears to be in ODF (Open Document) Format. " +
                   "Formats like these (eg ODS, ODP) are not supported, try Apache ODFToolkit");
            }
            if (numEntries == 0) {
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
        entries = this.zipArchive.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            PackagePartName partName = buildPartName(entry);
            if(partName == null) continue;

            // Only proceed for Relationships at this stage
            String contentType = contentTypeManager.getContentType(partName);
            if (contentType != null && contentType.equals(ContentTypes.RELATIONSHIPS_PART)) {
                try {
                    PackagePart part = new ZipPackagePart(this, entry, partName, contentType);
                    partList.put(partName, part);
                } catch (InvalidOperationException e) {
                    throw new InvalidFormatException(e.getMessage(), e);
                }
            }
        }

        // Then we can go through all the other parts
        entries = this.zipArchive.getEntries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            PackagePartName partName = buildPartName(entry);
            if(partName == null) continue;

            String contentType = contentTypeManager.getContentType(partName);
            if (contentType != null && contentType.equals(ContentTypes.RELATIONSHIPS_PART)) {
                // Already handled
            }
            else if (contentType != null) {
                try {
                    PackagePart part = new ZipPackagePart(this, entry, partName, contentType);
                    partList.put(partName, part);
                } catch (InvalidOperationException e) {
                    throw new InvalidFormatException(e.getMessage(), e);
                }
            } else {
                throw new InvalidFormatException(
                        "The part "
                                + partName.getURI().getPath()
                                + " does not have any content type ! Rule: Package require content types when retrieving a part from a package. [M.1.14]");
            }
        }

        return partList.values().toArray(new ZipPackagePart[partList.size()]);
    }

    /**
     * Builds a PackagePartName for the given ZipEntry,
     *  or null if it's the content types / invalid part
     */
    private PackagePartName buildPartName(ZipEntry entry) {
        try {
            // We get an error when we parse [Content_Types].xml
            // because it's not a valid URI.
            if (entry.getName().equalsIgnoreCase(
                    ContentTypeManager.CONTENT_TYPES_PART_NAME)) {
                return null;
            }
            return PackagingURIHelper.createPartName(ZipHelper
                    .getOPCNameFromZipItemName(entry.getName()));
        } catch (Exception e) {
            // We assume we can continue, even in degraded mode ...
            logger.log(POILogger.WARN,"Entry "
                                      + entry.getName()
                                      + " is not valid, so this part won't be add to the package.", e);
            return null;
        }
    }

    /**
     * Create a new MemoryPackagePart from the specified URI and content type
     *
     *
     * aram partName The part URI.
     *
     * @param contentType
     *            The part content type.
     * @return The newly created zip package part, else <b>null</b>.
     */
    @Override
    protected PackagePart createPartImpl(PackagePartName partName,
            String contentType, boolean loadRelationships) {
        if (contentType == null)
            throw new IllegalArgumentException("contentType");

        if (partName == null)
            throw new IllegalArgumentException("partName");

        try {
            return new MemoryPackagePart(this, partName, contentType,
                    loadRelationships);
        } catch (InvalidFormatException e) {
            logger.log(POILogger.WARN, e);
            return null;
        }
    }

    /**
     * Delete a part from the package
     *
     * @throws IllegalArgumentException
     *             Throws if the part URI is nulll or invalid.
     */
    @Override
    protected void removePartImpl(PackagePartName partName) {
        if (partName == null)
            throw new IllegalArgumentException("partUri");
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

		// Save the content
		if (this.originalPackagePath != null
				&& !"".equals(this.originalPackagePath)) {
			File targetFile = new File(this.originalPackagePath);
			if (targetFile.exists()) {
				// Case of a package previously open

				File tempFile = TempFile.createTempFile(
						generateTempFileName(FileHelper
								.getDirectory(targetFile)), ".tmp");

				// Save the final package to a temporary file
				try {
					save(tempFile);
				} finally {
					try {
						// Close the current zip file, so we can
						//  overwrite it on all platforms
						this.zipArchive.close();
						// Copy the new file over the old one
						FileHelper.copyFile(tempFile, targetFile);
					} finally {
						// Either the save operation succeed or not, we delete the
						// temporary file
						if (!tempFile.delete()) {
							logger
									.log(POILogger.WARN,"The temporary file: '"
											+ targetFile.getAbsolutePath()
											+ "' cannot be deleted ! Make sure that no other application use it.");
						}
					}
				}
			} else {
				throw new InvalidOperationException(
						"Can't close a package not previously open with the open() method !");
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
			if (this.zipArchive != null)
				this.zipArchive.close();
		} catch (IOException e) {
			// Do nothing, user dont have to know
		}
	}

    /**
     * Implement the getPart() method to retrieve a part from its URI in the
     * current package
     *
     *
     * @see #getPart(PackageRelationship)
     */
    @Override
    protected PackagePart getPartImpl(PackagePartName partName) {
        if (partList.containsKey(partName)) {
            return partList.get(partName);
        }
        return null;
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

		final ZipOutputStream zos;
		try {
			if (!(outputStream instanceof ZipOutputStream))
				zos = new ZipOutputStream(outputStream);
			else
				zos = (ZipOutputStream) outputStream;

			// If the core properties part does not exist in the part list,
			// we save it as well
			if (this.getPartsByRelationshipType(PackageRelationshipTypes.CORE_PROPERTIES).size() == 0 &&
                this.getPartsByRelationshipType(PackageRelationshipTypes.CORE_PROPERTIES_ECMA376).size() == 0    ) {
				logger.log(POILogger.DEBUG,"Save core properties part");
				
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

			// Save package relationships part.
			logger.log(POILogger.DEBUG,"Save package relationships");
			ZipPartMarshaller.marshallRelationshipPart(this.getRelationships(),
					PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_PART_NAME,
					zos);

			// Save content type part.
			logger.log(POILogger.DEBUG,"Save content types part");
			this.contentTypeManager.save(zos);

			// Save parts.
			for (PackagePart part : getParts()) {
				// If the part is a relationship part, we don't save it, it's
				// the source part that will do the job.
				if (part.isRelationshipPart())
					continue;

				logger.log(POILogger.DEBUG,"Save part '"
						+ ZipHelper.getZipItemNameFromOPCName(part
								.getPartName().getName()) + "'");
				PartMarshaller marshaller = partMarshallers
						.get(part._contentType);
				if (marshaller != null) {
					if (!marshaller.marshall(part, zos)) {
						throw new OpenXML4JException(
								"The part "
										+ part.getPartName().getURI()
										+ " fail to be saved in the stream with marshaller "
										+ marshaller);
					}
				} else {
					if (!defaultPartMarshaller.marshall(part, zos))
						throw new OpenXML4JException(
								"The part "
										+ part.getPartName().getURI()
										+ " fail to be saved in the stream with marshaller "
										+ defaultPartMarshaller);
				}
			}
			zos.close();
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
}
