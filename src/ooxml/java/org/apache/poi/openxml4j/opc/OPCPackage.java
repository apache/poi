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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.opc.internal.ContentType;
import org.apache.poi.openxml4j.opc.internal.ContentTypeManager;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.PartMarshaller;
import org.apache.poi.openxml4j.opc.internal.PartUnmarshaller;
import org.apache.poi.openxml4j.opc.internal.ZipContentTypeManager;
import org.apache.poi.openxml4j.opc.internal.marshallers.DefaultMarshaller;
import org.apache.poi.openxml4j.opc.internal.marshallers.ZipPackagePropertiesMarshaller;
import org.apache.poi.openxml4j.opc.internal.unmarshallers.PackagePropertiesUnmarshaller;
import org.apache.poi.openxml4j.opc.internal.unmarshallers.UnmarshallContext;
import org.apache.poi.openxml4j.util.Nullable;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

/**
 * Represents a container that can store multiple data objects.
 *
 * @author Julien Chable, CDubet
 * @version 0.1
 */
public abstract class OPCPackage implements RelationshipSource {

	/**
	 * Logger.
	 */
    private static POILogger logger = POILogFactory.getLogger(OPCPackage.class);

	/**
	 * Default package access.
	 */
	protected static final PackageAccess defaultPackageAccess = PackageAccess.READ_WRITE;

	/**
	 * Package access.
	 */
	private PackageAccess packageAccess;

	/**
	 * Package parts collection.
	 */
	protected PackagePartCollection partList;

	/**
	 * Package relationships.
	 */
	protected PackageRelationshipCollection relationships;

	/**
	 * Part marshallers by content type.
	 */
	protected Hashtable<ContentType, PartMarshaller> partMarshallers;

	/**
	 * Default part marshaller.
	 */
	protected PartMarshaller defaultPartMarshaller;

	/**
	 * Part unmarshallers by content type.
	 */
	protected Hashtable<ContentType, PartUnmarshaller> partUnmarshallers;

	/**
	 * Core package properties.
	 */
	protected PackagePropertiesPart packageProperties;

	/**
	 * Manage parts content types of this package.
	 */
	protected ContentTypeManager contentTypeManager;

	/**
	 * Flag if a modification is done to the document.
	 */
	protected boolean isDirty = false;

	/**
	 * File path of this package.
	 */
	protected String originalPackagePath;

	/**
	 * Output stream for writing this package.
	 */
	protected OutputStream output;

	/**
	 * Constructor.
	 *
	 * @param access
	 *            Package access.
	 */
	OPCPackage(PackageAccess access) {
		if (getClass() != ZipPackage.class) {
			throw new IllegalArgumentException("PackageBase may not be subclassed");
		}
		init();
		this.packageAccess = access;
	}

	/**
	 * Initialize the package instance.
	 */
	private void init() {
		this.partMarshallers = new Hashtable<ContentType, PartMarshaller>(5);
		this.partUnmarshallers = new Hashtable<ContentType, PartUnmarshaller>(2);

		try {
			// Add 'default' unmarshaller
			this.partUnmarshallers.put(new ContentType(
					ContentTypes.CORE_PROPERTIES_PART),
					new PackagePropertiesUnmarshaller());

			// Add default marshaller
			this.defaultPartMarshaller = new DefaultMarshaller();
			// TODO Delocalize specialized marshallers
			this.partMarshallers.put(new ContentType(
					ContentTypes.CORE_PROPERTIES_PART),
					new ZipPackagePropertiesMarshaller());
		} catch (InvalidFormatException e) {
			// Should never happen
			throw new OpenXML4JRuntimeException(
					"Package.init() : this exception should never happen, if you read this message please send a mail to the developers team.");
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
	public static OPCPackage open(String path) throws InvalidFormatException {
		return open(path, defaultPackageAccess);
	}

	/**
	 * Open a package.
	 *
	 * @param path
	 *            The document path.
	 * @param access
	 *            PackageBase access.
	 * @return A PackageBase object, else <b>null</b>.
	 * @throws InvalidFormatException
	 *             If the specified file doesn't exist, and a parsing error
	 *             occur.
	 */
	public static OPCPackage open(String path, PackageAccess access)
			throws InvalidFormatException {
		if (path == null || "".equals(path.trim())
				|| (new File(path).exists() && new File(path).isDirectory()))
			throw new IllegalArgumentException("path");

		OPCPackage pack = new ZipPackage(path, access);
		if (pack.partList == null && access != PackageAccess.WRITE) {
			pack.getParts();
		}
		pack.originalPackagePath = new File(path).getAbsolutePath();
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
	 *            The InputStream to read the package from
	 * @return A PackageBase object
	 */
	public static OPCPackage open(InputStream in) throws InvalidFormatException,
			IOException {
		OPCPackage pack = new ZipPackage(in, PackageAccess.READ);
		if (pack.partList == null) {
			pack.getParts();
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
	public static OPCPackage openOrCreate(File file) throws InvalidFormatException {
		OPCPackage retPackage = null;
		if (file.exists()) {
			retPackage = open(file.getAbsolutePath());
		} else {
			retPackage = create(file);
		}
		return retPackage;
	}

	/**
	 * Creates a new package.
	 *
	 * @param path
	 *            Path of the document.
	 * @return A newly created PackageBase ready to use.
	 */
	public static OPCPackage create(String path) {
		return create(new File(path));
	}

	/**
	 * Creates a new package.
	 *
	 * @param file
	 *            Path of the document.
	 * @return A newly created PackageBase ready to use.
	 */
	public static OPCPackage create(File file) {
		if (file == null || (file.exists() && file.isDirectory()))
			throw new IllegalArgumentException("file");

		if (file.exists()) {
			throw new InvalidOperationException(
					"This package (or file) already exists : use the open() method or delete the file.");
		}

		// Creates a new package
		OPCPackage pkg = null;
		pkg = new ZipPackage();
		pkg.originalPackagePath = file.getAbsolutePath();

		configurePackage(pkg);
		return pkg;
	}

	public static OPCPackage create(OutputStream output) {
		OPCPackage pkg = null;
		pkg = new ZipPackage();
		pkg.originalPackagePath = null;
		pkg.output = output;

		configurePackage(pkg);
		return pkg;
	}

	/**
	 * Configure the package.
	 *
	 * @param pkg
	 */
	private static void configurePackage(OPCPackage pkg) {
		try {
			// Content type manager
			pkg.contentTypeManager = new ZipContentTypeManager(null, pkg);
			// Add default content types for .xml and .rels
			pkg.contentTypeManager
					.addContentType(
							PackagingURIHelper
									.createPartName(PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_URI),
							ContentTypes.RELATIONSHIPS_PART);
			pkg.contentTypeManager
					.addContentType(PackagingURIHelper
							.createPartName("/default.xml"),
							ContentTypes.PLAIN_OLD_XML);

			// Init some PackageBase properties
			pkg.packageProperties = new PackagePropertiesPart(pkg,
					PackagingURIHelper.CORE_PROPERTIES_PART_NAME);
			pkg.packageProperties.setCreatorProperty("Generated by OpenXML4J");
			pkg.packageProperties.setCreatedProperty(new Nullable<Date>(
					new Date()));
		} catch (InvalidFormatException e) {
			// Should never happen
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Flush the package : save all.
	 *
	 * @see #close()
	 */
	public void flush() {
		throwExceptionIfReadOnly();

		if (this.packageProperties != null) {
			this.packageProperties.flush();
		}

		this.flushImpl();
	}

	/**
	 * Close the package and save its content.
	 *
	 * @throws IOException
	 *             If an IO exception occur during the saving process.
	 */
	public void close() throws IOException {
		if (this.packageAccess == PackageAccess.READ) {
			logger
					.log(POILogger.WARN, "The close() method is intended to SAVE a package. This package is open in READ ONLY mode, use the revert() method instead !");
			return;
		}

		// Save the content
		ReentrantReadWriteLock l = new ReentrantReadWriteLock();
		try {
			l.writeLock().lock();
			if (this.originalPackagePath != null
					&& !"".equals(this.originalPackagePath.trim())) {
				File targetFile = new File(this.originalPackagePath);
				if (!targetFile.exists()
						|| !(this.originalPackagePath
								.equalsIgnoreCase(targetFile.getAbsolutePath()))) {
					// Case of a package created from scratch
					save(targetFile);
				} else {
					closeImpl();
				}
			} else if (this.output != null) {
				save(this.output);
			}
		} finally {
			l.writeLock().unlock();
		}

		// Clear
		this.contentTypeManager.clearAll();

		// Call the garbage collector
		Runtime.getRuntime().gc();
	}

	/**
	 * Close the package WITHOUT saving its content. Reinitialize this package
	 * and cancel all changes done to it.
	 */
	public void revert() {
		revertImpl();
	}

	/**
	 * Add a thumbnail to the package. This method is provided to make easier
	 * the addition of a thumbnail in a package. You can do the same work by
	 * using the traditionnal relationship and part mechanism.
	 *
	 * @param path
	 *            The full path to the image file.
	 */
	public void addThumbnail(String path) throws IOException {
		// Check parameter
		if ("".equals(path))
			throw new IllegalArgumentException("path");

		// Get the filename from the path
		String filename = path
				.substring(path.lastIndexOf(File.separatorChar) + 1);

		// Create the thumbnail part name
		String contentType = ContentTypes
				.getContentTypeFromFileExtension(filename);
		PackagePartName thumbnailPartName = null;
		try {
			thumbnailPartName = PackagingURIHelper.createPartName("/docProps/"
					+ filename);
		} catch (InvalidFormatException e) {
			try {
				thumbnailPartName = PackagingURIHelper
						.createPartName("/docProps/thumbnail"
								+ path.substring(path.lastIndexOf(".") + 1));
			} catch (InvalidFormatException e2) {
				throw new InvalidOperationException(
						"Can't add a thumbnail file named '" + filename + "'");
			}
		}

		// Check if part already exist
		if (this.getPart(thumbnailPartName) != null)
			throw new InvalidOperationException(
					"You already add a thumbnail named '" + filename + "'");

		// Add the thumbnail part to this package.
		PackagePart thumbnailPart = this.createPart(thumbnailPartName,
				contentType, false);

		// Add the relationship between the package and the thumbnail part
		this.addRelationship(thumbnailPartName, TargetMode.INTERNAL,
				PackageRelationshipTypes.THUMBNAIL);

		// Copy file data to the newly created part
		StreamHelper.copyStream(new FileInputStream(path), thumbnailPart
				.getOutputStream());
	}

	/**
	 * Throws an exception if the package access mode is in read only mode
	 * (PackageAccess.Read).
	 *
	 * @throws InvalidOperationException
	 *             Throws if a writing operation is done on a read only package.
	 * @see org.apache.poi.openxml4j.opc.PackageAccess
	 */
	void throwExceptionIfReadOnly() throws InvalidOperationException {
		if (packageAccess == PackageAccess.READ)
			throw new InvalidOperationException(
					"Operation not allowed, document open in read only mode!");
	}

	/**
	 * Throws an exception if the package access mode is in write only mode
	 * (PackageAccess.Write). This method is call when other methods need write
	 * right.
	 *
	 * @throws InvalidOperationException
	 *             Throws if a read operation is done on a write only package.
	 * @see org.apache.poi.openxml4j.opc.PackageAccess
	 */
	void throwExceptionIfWriteOnly() throws InvalidOperationException {
		if (packageAccess == PackageAccess.WRITE)
			throw new InvalidOperationException(
					"Operation not allowed, document open in write only mode!");
	}

	/**
	 * Retrieves or creates if none exists, core package property part.
	 *
	 * @return The PackageProperties part of this package.
	 */
	public PackageProperties getPackageProperties()
			throws InvalidFormatException {
		this.throwExceptionIfWriteOnly();
		// If no properties part has been found then we create one
		if (this.packageProperties == null) {
			this.packageProperties = new PackagePropertiesPart(this,
					PackagingURIHelper.CORE_PROPERTIES_PART_NAME);
		}
		return this.packageProperties;
	}

	/**
	 * Retrieve a part identified by its name.
	 *
	 * @param partName
	 *            Part name of the part to retrieve.
	 * @return The part with the specified name, else <code>null</code>.
	 */
	public PackagePart getPart(PackagePartName partName) {
		throwExceptionIfWriteOnly();

		if (partName == null)
			throw new IllegalArgumentException("partName");

		// If the partlist is null, then we parse the package.
		if (partList == null) {
			try {
				getParts();
			} catch (InvalidFormatException e) {
				return null;
			}
		}
		return getPartImpl(partName);
	}

	/**
	 * Retrieve parts by content type.
	 *
	 * @param contentType
	 *            The content type criteria.
	 * @return All part associated to the specified content type.
	 */
	public ArrayList<PackagePart> getPartsByContentType(String contentType) {
		ArrayList<PackagePart> retArr = new ArrayList<PackagePart>();
		for (PackagePart part : partList.values()) {
			if (part.getContentType().equals(contentType))
				retArr.add(part);
		}
		return retArr;
	}

	/**
	 * Retrieve parts by relationship type.
	 *
	 * @param relationshipType
	 *            Relationship type.
	 * @return All parts which are the target of a relationship with the
	 *         specified type, if the method can't retrieve relationships from
	 *         the package, then return <code>null</code>.
	 */
	public ArrayList<PackagePart> getPartsByRelationshipType(
			String relationshipType) {
		if (relationshipType == null)
			throw new IllegalArgumentException("relationshipType");
		ArrayList<PackagePart> retArr = new ArrayList<PackagePart>();
		for (PackageRelationship rel : getRelationshipsByType(relationshipType)) {
			retArr.add(getPart(rel));
		}
		return retArr;
	}

	/**
	 * Get the target part from the specified relationship.
	 *
	 * @param partRel
	 *            The part relationship uses to retrieve the part.
	 */
	public PackagePart getPart(PackageRelationship partRel) {
		PackagePart retPart = null;
		ensureRelationships();
		for (PackageRelationship rel : relationships) {
			if (rel.getRelationshipType().equals(partRel.getRelationshipType())) {
				try {
					retPart = getPart(PackagingURIHelper.createPartName(rel
							.getTargetURI()));
				} catch (InvalidFormatException e) {
					continue;
				}
				break;
			}
		}
		return retPart;
	}

	/**
	 * Load the parts of the archive if it has not been done yet The
	 * relationships of each part are not loaded
	 *
	 * @return All this package's parts.
	 */
	public ArrayList<PackagePart> getParts() throws InvalidFormatException {
		throwExceptionIfWriteOnly();

		// If the part list is null, we parse the package to retrieve all parts.
		if (partList == null) {
			/* Variables use to validate OPC Compliance */

			// Ensure rule M4.1 -> A format consumer shall consider more than
			// one core properties relationship for a package to be an error
			boolean hasCorePropertiesPart = false;

			PackagePart[] parts = this.getPartsImpl();
			this.partList = new PackagePartCollection();
			for (PackagePart part : parts) {
				if (partList.containsKey(part._partName))
					throw new InvalidFormatException(
							"A part with the name '"
									+ part._partName
									+ "' already exist : Packages shall not contain equivalent part names and package implementers shall neither create nor recognize packages with equivalent part names. [M1.12]");

				// Check OPC compliance rule M4.1
				if (part.getContentType().equals(
						ContentTypes.CORE_PROPERTIES_PART)) {
					if (!hasCorePropertiesPart)
						hasCorePropertiesPart = true;
					else
						throw new InvalidFormatException(
								"OPC Compliance error [M4.1]: there is more than one core properties relationship in the package !");
				}

				PartUnmarshaller partUnmarshaller = partUnmarshallers
						.get(part._contentType);

				if (partUnmarshaller != null) {
					UnmarshallContext context = new UnmarshallContext(this,
							part._partName);
					try {
						PackagePart unmarshallPart = partUnmarshaller
								.unmarshall(context, part.getInputStream());
						partList.put(unmarshallPart._partName, unmarshallPart);

						// Core properties case
						if (unmarshallPart instanceof PackagePropertiesPart)
							this.packageProperties = (PackagePropertiesPart) unmarshallPart;
					} catch (IOException ioe) {
						logger.log(POILogger.WARN, "Unmarshall operation : IOException for "
								+ part._partName);
						continue;
					} catch (InvalidOperationException invoe) {
						throw new InvalidFormatException(invoe.getMessage());
					}
				} else {
					try {
						partList.put(part._partName, part);
					} catch (InvalidOperationException e) {
						throw new InvalidFormatException(e.getMessage());
					}
				}
			}
		}
		return new ArrayList<PackagePart>(partList.values());
	}

	/**
	 * Create and add a part, with the specified name and content type, to the
	 * package.
	 *
	 * @param partName
	 *            Part name.
	 * @param contentType
	 *            Part content type.
	 * @return The newly created part.
	 * @throws InvalidFormatException
	 *             If rule M1.12 is not verified : Packages shall not contain
	 *             equivalent part names and package implementers shall neither
	 *             create nor recognize packages with equivalent part names.
	 * @see #createPartImpl(PackagePartName, String, boolean)
	 */
	public PackagePart createPart(PackagePartName partName, String contentType) {
		return this.createPart(partName, contentType, true);
	}

	/**
	 * Create and add a part, with the specified name and content type, to the
	 * package. For general purpose, prefer the overload version of this method
	 * without the 'loadRelationships' parameter.
	 *
	 * @param partName
	 *            Part name.
	 * @param contentType
	 *            Part content type.
	 * @param loadRelationships
	 *            Specify if the existing relationship part, if any, logically
	 *            associated to the newly created part will be loaded.
	 * @return The newly created part.
	 * @throws InvalidFormatException
	 *             If rule M1.12 is not verified : Packages shall not contain
	 *             equivalent part names and package implementers shall neither
	 *             create nor recognize packages with equivalent part names.
	 * @see {@link#createPartImpl(URI, String)}
	 */
	PackagePart createPart(PackagePartName partName, String contentType,
			boolean loadRelationships) {
		throwExceptionIfReadOnly();
		if (partName == null) {
			throw new IllegalArgumentException("partName");
		}

		if (contentType == null || contentType.equals("")) {
			throw new IllegalArgumentException("contentType");
		}

		// Check if the specified part name already exists
		if (partList.containsKey(partName)
				&& !partList.get(partName).isDeleted()) {
			throw new InvalidOperationException(
					"A part with the name '"
							+ partName.getName()
							+ "' already exists : Packages shall not contain equivalent part names and package implementers shall neither create nor recognize packages with equivalent part names. [M1.12]");
		}

		/* Check OPC compliance */

		// Rule [M4.1]: The format designer shall specify and the format
		// producer
		// shall create at most one core properties relationship for a package.
		// A format consumer shall consider more than one core properties
		// relationship for a package to be an error. If present, the
		// relationship shall target the Core Properties part.
		if (contentType.equals(ContentTypes.CORE_PROPERTIES_PART)) {
			if (this.packageProperties != null)
				throw new InvalidOperationException(
						"OPC Compliance error [M4.1]: you try to add more than one core properties relationship in the package !");
		}

		/* End check OPC compliance */

		PackagePart part = this.createPartImpl(partName, contentType,
				loadRelationships);
		this.contentTypeManager.addContentType(partName, contentType);
		this.partList.put(partName, part);
		this.isDirty = true;
		return part;
	}

	/**
	 * Add a part to the package.
	 *
	 * @param partName
	 *            Part name of the part to create.
	 * @param contentType
	 *            type associated with the file
	 * @param content
	 *            the contents to add. In order to have faster operation in
	 *            document merge, the data are stored in memory not on a hard
	 *            disk
	 *
	 * @return The new part.
	 * @see #createPart(PackagePartName, String)
	 */
	public PackagePart createPart(PackagePartName partName, String contentType,
			ByteArrayOutputStream content) {
		PackagePart addedPart = this.createPart(partName, contentType);
		if (addedPart == null) {
			return null;
		}
		// Extract the zip entry content to put it in the part content
		if (content != null) {
			try {
				OutputStream partOutput = addedPart.getOutputStream();
				if (partOutput == null) {
					return null;
				}

				partOutput.write(content.toByteArray(), 0, content.size());
				partOutput.close();

			} catch (IOException ioe) {
				return null;
			}
		} else {
			return null;
		}
		return addedPart;
	}

	/**
	 * Add the specified part to the package. If a part already exists in the
	 * package with the same name as the one specified, then we replace the old
	 * part by the specified part.
	 *
	 * @param part
	 *            The part to add (or replace).
	 * @return The part added to the package, the same as the one specified.
	 * @throws InvalidFormatException
	 *             If rule M1.12 is not verified : Packages shall not contain
	 *             equivalent part names and package implementers shall neither
	 *             create nor recognize packages with equivalent part names.
	 */
	protected PackagePart addPackagePart(PackagePart part) {
		throwExceptionIfReadOnly();
		if (part == null) {
			throw new IllegalArgumentException("part");
		}

		if (partList.containsKey(part._partName)) {
			if (!partList.get(part._partName).isDeleted()) {
				throw new InvalidOperationException(
						"A part with the name '"
								+ part._partName.getName()
								+ "' already exists : Packages shall not contain equivalent part names and package implementers shall neither create nor recognize packages with equivalent part names. [M1.12]");
			}
			// If the specified partis flagged as deleted, we make it
			// available
			part.setDeleted(false);
			// and delete the old part to replace it thereafeter
			this.partList.remove(part._partName);
		}
		this.partList.put(part._partName, part);
		this.isDirty = true;
		return part;
	}

	/**
	 * Remove the specified part in this package. If this part is relationship
	 * part, then delete all relationships in the source part.
	 *
	 * @param part
	 *            The part to remove. If <code>null</code>, skip the action.
	 * @see #removePart(PackagePartName)
	 */
	public void removePart(PackagePart part) {
		if (part != null) {
			removePart(part.getPartName());
		}
	}

	/**
	 * Remove a part in this package. If this part is relationship part, then
	 * delete all relationships in the source part.
	 *
	 * @param partName
	 *            The part name of the part to remove.
	 */
	public void removePart(PackagePartName partName) {
		throwExceptionIfReadOnly();
		if (partName == null || !this.containPart(partName))
			throw new IllegalArgumentException("partName");

		// Delete the specified part from the package.
		if (this.partList.containsKey(partName)) {
			this.partList.get(partName).setDeleted(true);
			this.removePartImpl(partName);
			this.partList.remove(partName);
		} else {
			this.removePartImpl(partName);
		}

		// Delete content type
		this.contentTypeManager.removeContentType(partName);

		// If this part is a relationship part, then delete all relationships of
		// the source part.
		if (partName.isRelationshipPartURI()) {
			URI sourceURI = PackagingURIHelper
					.getSourcePartUriFromRelationshipPartUri(partName.getURI());
			PackagePartName sourcePartName;
			try {
				sourcePartName = PackagingURIHelper.createPartName(sourceURI);
			} catch (InvalidFormatException e) {
				logger
						.log(POILogger.ERROR, "Part name URI '"
								+ sourceURI
								+ "' is not valid ! This message is not intended to be displayed !");
				return;
			}
			if (sourcePartName.getURI().equals(
					PackagingURIHelper.PACKAGE_ROOT_URI)) {
				clearRelationships();
			} else if (containPart(sourcePartName)) {
				PackagePart part = getPart(sourcePartName);
				if (part != null)
					part.clearRelationships();
			}
		}

		this.isDirty = true;
	}

	/**
	 * Remove a part from this package as well as its relationship part, if one
	 * exists, and all parts listed in the relationship part. Be aware that this
	 * do not delete relationships which target the specified part.
	 *
	 * @param partName
	 *            The name of the part to delete.
	 * @throws InvalidFormatException
	 *             Throws if the associated relationship part of the specified
	 *             part is not valid.
	 */
	public void removePartRecursive(PackagePartName partName)
			throws InvalidFormatException {
		// Retrieves relationship part, if one exists
		PackagePart relPart = this.partList.get(PackagingURIHelper
				.getRelationshipPartName(partName));
		// Retrieves PackagePart object from the package
		PackagePart partToRemove = this.partList.get(partName);

		if (relPart != null) {
			PackageRelationshipCollection partRels = new PackageRelationshipCollection(
					partToRemove);
			for (PackageRelationship rel : partRels) {
				PackagePartName partNameToRemove = PackagingURIHelper
						.createPartName(PackagingURIHelper.resolvePartUri(rel
								.getSourceURI(), rel.getTargetURI()));
				removePart(partNameToRemove);
			}

			// Finally delete its relationship part if one exists
			this.removePart(relPart._partName);
		}

		// Delete the specified part
		this.removePart(partToRemove._partName);
	}

	/**
	 * Delete the part with the specified name and its associated relationships
	 * part if one exists. Prefer the use of this method to delete a part in the
	 * package, compare to the remove() methods that don't remove associated
	 * relationships part.
	 *
	 * @param partName
	 *            Name of the part to delete
	 */
	public void deletePart(PackagePartName partName) {
		if (partName == null)
			throw new IllegalArgumentException("partName");

		// Remove the part
		this.removePart(partName);
		// Remove the relationships part
		this.removePart(PackagingURIHelper.getRelationshipPartName(partName));
	}

	/**
	 * Delete the part with the specified name and all part listed in its
	 * associated relationships part if one exists. This process is recursively
	 * apply to all parts in the relationships part of the specified part.
	 * Prefer the use of this method to delete a part in the package, compare to
	 * the remove() methods that don't remove associated relationships part.
	 *
	 * @param partName
	 *            Name of the part to delete
	 */
	public void deletePartRecursive(PackagePartName partName) {
		if (partName == null || !this.containPart(partName))
			throw new IllegalArgumentException("partName");

		PackagePart partToDelete = this.getPart(partName);
		// Remove the part
		this.removePart(partName);
		// Remove all relationship parts associated
		try {
			for (PackageRelationship relationship : partToDelete
					.getRelationships()) {
				PackagePartName targetPartName = PackagingURIHelper
						.createPartName(PackagingURIHelper.resolvePartUri(
								partName.getURI(), relationship.getTargetURI()));
				this.deletePartRecursive(targetPartName);
			}
		} catch (InvalidFormatException e) {
			logger.log(POILogger.WARN, "An exception occurs while deleting part '"
					+ partName.getName()
					+ "'. Some parts may remain in the package. - "
					+ e.getMessage());
			return;
		}
		// Remove the relationships part
		PackagePartName relationshipPartName = PackagingURIHelper
				.getRelationshipPartName(partName);
		if (relationshipPartName != null && containPart(relationshipPartName))
			this.removePart(relationshipPartName);
	}

	/**
	 * Check if a part already exists in this package from its name.
	 *
	 * @param partName
	 *            Part name to check.
	 * @return <i>true</i> if the part is logically added to this package, else
	 *         <i>false</i>.
	 */
	public boolean containPart(PackagePartName partName) {
		return (this.getPart(partName) != null);
	}

	/**
	 * Add a relationship to the package (except relationships part).
	 *
	 * Check rule M4.1 : The format designer shall specify and the format
	 * producer shall create at most one core properties relationship for a
	 * package. A format consumer shall consider more than one core properties
	 * relationship for a package to be an error. If present, the relationship
	 * shall target the Core Properties part.
	 *
	 * Check rule M1.25: The Relationships part shall not have relationships to
	 * any other part. Package implementers shall enforce this requirement upon
	 * the attempt to create such a relationship and shall treat any such
	 * relationship as invalid.
	 *
	 * @param targetPartName
	 *            Target part name.
	 * @param targetMode
	 *            Target mode, either Internal or External.
	 * @param relationshipType
	 *            Relationship type.
	 * @param relID
	 *            ID of the relationship.
	 * @see PackageRelationshipTypes
	 */
	public PackageRelationship addRelationship(PackagePartName targetPartName,
			TargetMode targetMode, String relationshipType, String relID) {
		/* Check OPC compliance */

		// Check rule M4.1 : The format designer shall specify and the format
		// producer
		// shall create at most one core properties relationship for a package.
		// A format consumer shall consider more than one core properties
		// relationship for a package to be an error. If present, the
		// relationship shall target the Core Properties part.
		if (relationshipType.equals(PackageRelationshipTypes.CORE_PROPERTIES)
				&& this.packageProperties != null)
			throw new InvalidOperationException(
					"OPC Compliance error [M4.1]: can't add another core properties part ! Use the built-in package method instead.");

		/*
		 * Check rule M1.25: The Relationships part shall not have relationships
		 * to any other part. Package implementers shall enforce this
		 * requirement upon the attempt to create such a relationship and shall
		 * treat any such relationship as invalid.
		 */
		if (targetPartName.isRelationshipPartURI()) {
			throw new InvalidOperationException(
					"Rule M1.25: The Relationships part shall not have relationships to any other part.");
		}

		/* End OPC compliance */

		ensureRelationships();
		PackageRelationship retRel = relationships.addRelationship(
				targetPartName.getURI(), targetMode, relationshipType, relID);
		this.isDirty = true;
		return retRel;
	}

	/**
	 * Add a package relationship.
	 *
	 * @param targetPartName
	 *            Target part name.
	 * @param targetMode
	 *            Target mode, either Internal or External.
	 * @param relationshipType
	 *            Relationship type.
	 * @see PackageRelationshipTypes
	 */
	public PackageRelationship addRelationship(PackagePartName targetPartName,
			TargetMode targetMode, String relationshipType) {
		return this.addRelationship(targetPartName, targetMode,
				relationshipType, null);
	}

	/**
	 * Adds an external relationship to a part (except relationships part).
	 *
	 * The targets of external relationships are not subject to the same
	 * validity checks that internal ones are, as the contents is potentially
	 * any file, URL or similar.
	 *
	 * @param target
	 *            External target of the relationship
	 * @param relationshipType
	 *            Type of relationship.
	 * @return The newly created and added relationship
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addExternalRelationship(java.lang.String,
	 *      java.lang.String)
	 */
	public PackageRelationship addExternalRelationship(String target,
			String relationshipType) {
		return addExternalRelationship(target, relationshipType, null);
	}

	/**
	 * Adds an external relationship to a part (except relationships part).
	 *
	 * The targets of external relationships are not subject to the same
	 * validity checks that internal ones are, as the contents is potentially
	 * any file, URL or similar.
	 *
	 * @param target
	 *            External target of the relationship
	 * @param relationshipType
	 *            Type of relationship.
	 * @param id
	 *            Relationship unique id.
	 * @return The newly created and added relationship
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addExternalRelationship(java.lang.String,
	 *      java.lang.String)
	 */
	public PackageRelationship addExternalRelationship(String target,
			String relationshipType, String id) {
		if (target == null) {
			throw new IllegalArgumentException("target");
		}
		if (relationshipType == null) {
			throw new IllegalArgumentException("relationshipType");
		}

		URI targetURI;
		try {
			targetURI = new URI(target);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid target - " + e);
		}

		ensureRelationships();
		PackageRelationship retRel = relationships.addRelationship(targetURI,
				TargetMode.EXTERNAL, relationshipType, id);
		this.isDirty = true;
		return retRel;
	}

	/**
	 * Delete a relationship from this package.
	 *
	 * @param id
	 *            Id of the relationship to delete.
	 */
	public void removeRelationship(String id) {
		if (relationships != null) {
			relationships.removeRelationship(id);
			this.isDirty = true;
		}
	}

	/**
	 * Retrieves all package relationships.
	 *
	 * @return All package relationships of this package.
	 * @throws OpenXML4JException
	 * @see #getRelationshipsHelper(String)
	 */
	public PackageRelationshipCollection getRelationships() {
		return getRelationshipsHelper(null);
	}

	/**
	 * Retrieves all relationships with the specified type.
	 *
	 * @param relationshipType
	 *            The filter specifying the relationship type.
	 * @return All relationships with the specified relationship type.
	 */
	public PackageRelationshipCollection getRelationshipsByType(
			String relationshipType) {
		throwExceptionIfWriteOnly();
		if (relationshipType == null) {
			throw new IllegalArgumentException("relationshipType");
		}
		return getRelationshipsHelper(relationshipType);
	}

	/**
	 * Retrieves all relationships with specified id (normally just ine because
	 * a relationship id is supposed to be unique).
	 *
	 * @param id
	 *            Id of the wanted relationship.
	 */
	private PackageRelationshipCollection getRelationshipsHelper(String id) {
		throwExceptionIfWriteOnly();
		ensureRelationships();
		return this.relationships.getRelationships(id);
	}

	/**
	 * Clear package relationships.
	 */
	public void clearRelationships() {
		if (relationships != null) {
			relationships.clear();
			this.isDirty = true;
		}
	}

	/**
	 * Ensure that the relationships collection is not null.
	 */
	public void ensureRelationships() {
		if (this.relationships == null) {
			try {
				this.relationships = new PackageRelationshipCollection(this);
			} catch (InvalidFormatException e) {
				this.relationships = new PackageRelationshipCollection();
			}
		}
	}

	/**
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#getRelationship(java.lang.String)
	 */
	public PackageRelationship getRelationship(String id) {
		return this.relationships.getRelationshipByID(id);
	}

	/**
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#hasRelationships()
	 */
	public boolean hasRelationships() {
		return (relationships.size() > 0);
	}

	/**
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#isRelationshipExists(org.apache.poi.openxml4j.opc.PackageRelationship)
	 */
	public boolean isRelationshipExists(PackageRelationship rel) {
        for (PackageRelationship r : this.getRelationships()) {
            if (r == rel)
                return true;
        }
        return false;
	}

	/**
	 * Add a marshaller.
	 *
	 * @param contentType
	 *            The content type to bind to the specified marshaller.
	 * @param marshaller
	 *            The marshaller to register with the specified content type.
	 */
	public void addMarshaller(String contentType, PartMarshaller marshaller) {
		try {
			partMarshallers.put(new ContentType(contentType), marshaller);
		} catch (InvalidFormatException e) {
			logger.log(POILogger.WARN, "The specified content type is not valid: '"
					+ e.getMessage() + "'. The marshaller will not be added !");
		}
	}

	/**
	 * Add an unmarshaller.
	 *
	 * @param contentType
	 *            The content type to bind to the specified unmarshaller.
	 * @param unmarshaller
	 *            The unmarshaller to register with the specified content type.
	 */
	public void addUnmarshaller(String contentType,
			PartUnmarshaller unmarshaller) {
		try {
			partUnmarshallers.put(new ContentType(contentType), unmarshaller);
		} catch (InvalidFormatException e) {
			logger.log(POILogger.WARN, "The specified content type is not valid: '"
					+ e.getMessage()
					+ "'. The unmarshaller will not be added !");
		}
	}

	/**
	 * Remove a marshaller by its content type.
	 *
	 * @param contentType
	 *            The content type associated with the marshaller to remove.
	 */
	public void removeMarshaller(String contentType) {
		partMarshallers.remove(contentType);
	}

	/**
	 * Remove an unmarshaller by its content type.
	 *
	 * @param contentType
	 *            The content type associated with the unmarshaller to remove.
	 */
	public void removeUnmarshaller(String contentType) {
		partUnmarshallers.remove(contentType);
	}

	/* Accesseurs */

	/**
	 * Get the package access mode.
	 *
	 * @return the packageAccess The current package access.
	 */
	public PackageAccess getPackageAccess() {
		return packageAccess;
	}

	/**
	 * Validates the package compliance with the OPC specifications.
	 *
	 * @return <b>true</b> if the package is valid else <b>false</b>
	 */
	public boolean validatePackage(OPCPackage pkg) throws InvalidFormatException {
		throw new InvalidOperationException("Not implemented yet !!!");
	}

	/**
	 * Save the document in the specified file.
	 *
	 * @param targetFile
	 *            Destination file.
	 * @throws IOException
	 *             Throws if an IO exception occur.
	 * @see #save(OutputStream)
	 */
	public void save(File targetFile) throws IOException {
		if (targetFile == null)
			throw new IllegalArgumentException("targetFile");

		this.throwExceptionIfReadOnly();
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);
		} catch (FileNotFoundException e) {
			throw new IOException(e.getLocalizedMessage());
		}
		this.save(fos);
	}

	/**
	 * Save the document in the specified output stream.
	 *
	 * @param outputStream
	 *            The stream to save the package.
	 * @see #saveImpl(OutputStream)
	 */
	public void save(OutputStream outputStream) throws IOException {
		throwExceptionIfReadOnly();
		this.saveImpl(outputStream);
	}

	/**
	 * Core method to create a package part. This method must be implemented by
	 * the subclass.
	 *
	 * @param partName
	 *            URI of the part to create.
	 * @param contentType
	 *            Content type of the part to create.
	 * @return The newly created package part.
	 */
	protected abstract PackagePart createPartImpl(PackagePartName partName,
			String contentType, boolean loadRelationships);

	/**
	 * Core method to delete a package part. This method must be implemented by
	 * the subclass.
	 *
	 * @param partName
	 *            The URI of the part to delete.
	 */
	protected abstract void removePartImpl(PackagePartName partName);

	/**
	 * Flush the package but not save.
	 */
	protected abstract void flushImpl();

	/**
	 * Close the package and cause a save of the package.
	 *
	 */
	protected abstract void closeImpl() throws IOException;

	/**
	 * Close the package without saving the document. Discard all changes made
	 * to this package.
	 */
	protected abstract void revertImpl();

	/**
	 * Save the package into the specified output stream.
	 *
	 * @param outputStream
	 *            The output stream use to save this package.
	 */
	protected abstract void saveImpl(OutputStream outputStream)
			throws IOException;

	/**
	 * Get the package part mapped to the specified URI.
	 *
	 * @param partName
	 *            The URI of the part to retrieve.
	 * @return The package part located by the specified URI, else <b>null</b>.
	 */
	protected abstract PackagePart getPartImpl(PackagePartName partName);

	/**
	 * Get all parts link to the package.
	 *
	 * @return A list of the part owned by the package.
	 */
	protected abstract PackagePart[] getPartsImpl()
			throws InvalidFormatException;
}
