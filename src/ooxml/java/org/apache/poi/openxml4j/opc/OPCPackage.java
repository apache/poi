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
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;
import org.apache.poi.openxml4j.exceptions.PartAlreadyExistsException;
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
import org.apache.poi.openxml4j.util.ZipEntrySource;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.NotImplemented;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Represents a container that can store multiple data objects.
 */
public abstract class OPCPackage implements RelationshipSource, Closeable {

	/**
	 * Logger.
	 */
    private static final POILogger logger = POILogFactory.getLogger(OPCPackage.class);

	/**
	 * Default package access.
	 */
	protected static final PackageAccess defaultPackageAccess = PackageAccess.READ_WRITE;

	/**
	 * Package access.
	 */
	private final PackageAccess packageAccess;

	/**
	 * Package parts collection.
	 */
	private PackagePartCollection partList;

	/**
	 * Package relationships.
	 */
	protected PackageRelationshipCollection relationships;

	/**
	 * Part marshallers by content type.
	 */
	protected final Map<ContentType, PartMarshaller> partMarshallers = new HashMap<>(5);

	/**
	 * Default part marshaller.
	 */
	protected final PartMarshaller defaultPartMarshaller = new DefaultMarshaller();

	/**
	 * Part unmarshallers by content type.
	 */
	protected final Map<ContentType, PartUnmarshaller> partUnmarshallers = new HashMap<>(2);

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
	protected boolean isDirty;

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
		this.packageAccess = access;

		final ContentType contentType = newCorePropertiesPart();
		// TODO Delocalize specialized marshallers
		this.partUnmarshallers.put(contentType, new PackagePropertiesUnmarshaller());
		this.partMarshallers.put(contentType, new ZipPackagePropertiesMarshaller());
	}

	private static ContentType newCorePropertiesPart() {
		try {
			return new ContentType(ContentTypes.CORE_PROPERTIES_PART);
		} catch (InvalidFormatException e) {
			// Should never happen
			throw new OpenXML4JRuntimeException(
				"Package.init() : this exception should never happen, " +
				"if you read this message please send a mail to the developers team. : " +
				e.getMessage(), e
			);
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
    * Open a package with read/write permission.
    *
    * @param file
    *            The file to open.
    * @return A Package object, else <b>null</b>.
    * @throws InvalidFormatException
    *             If the specified file doesn't exist, and a parsing error
    *             occur.
    */
   public static OPCPackage open(File file) throws InvalidFormatException {
      return open(file, defaultPackageAccess);
   }

   /**
    * Open an user provided {@link ZipEntrySource} with read-only permission.
    * This method can be used to stream data into POI.
    * Opposed to other open variants, the data is read as-is, e.g. there aren't
    * any zip-bomb protection put in place.
    *
    * @param zipEntry the custom source
    * @return A Package object
    * @throws InvalidFormatException if a parsing error occur.
    */
   public static OPCPackage open(ZipEntrySource zipEntry)
   throws InvalidFormatException {
       OPCPackage pack = new ZipPackage(zipEntry, PackageAccess.READ);
       try {
           if (pack.partList == null) {
               pack.getParts();
           }
           // pack.originalPackagePath = file.getAbsolutePath();
           return pack;
       } catch (InvalidFormatException | RuntimeException e) {
		   IOUtils.closeQuietly(pack);
           throw e;
       }
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
	 * @throws InvalidOperationException If the zip file cannot be opened.
	 * @throws InvalidFormatException if the package is not valid.
	 */
	public static OPCPackage open(String path, PackageAccess access)
			throws InvalidFormatException, InvalidOperationException {
		if (path == null || path.trim().isEmpty()) {
			throw new IllegalArgumentException("'path' must be given");
		}
		
		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			throw new IllegalArgumentException("path must not be a directory");
		}

		OPCPackage pack = new ZipPackage(path, access); // NOSONAR
		boolean success = false;
		if (pack.partList == null && access != PackageAccess.WRITE) {
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
    * Open a package.
    *
    * @param file
    *            The file to open.
    * @param access
    *            PackageBase access.
    * @return A PackageBase object, else <b>null</b>.
    * @throws InvalidFormatException
    *             If the specified file doesn't exist, and a parsing error
    *             occur.
    */
   public static OPCPackage open(File file, PackageAccess access)
         throws InvalidFormatException {
      if (file == null) {
          throw new IllegalArgumentException("'file' must be given");
      }
      if (file.exists() && file.isDirectory()) {
          throw new IllegalArgumentException("file must not be a directory");
      }

      OPCPackage pack = new ZipPackage(file, access);
	   try {
		   if (pack.partList == null && access != PackageAccess.WRITE) {
			   pack.getParts();
		   }
		   pack.originalPackagePath = file.getAbsolutePath();
		   return pack;
	   } catch (InvalidFormatException | RuntimeException e) {
		   IOUtils.closeQuietly(pack);
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
	 *            The InputStream to read the package from
	 * @return A PackageBase object
	 */
	public static OPCPackage open(InputStream in) throws InvalidFormatException,
			IOException {
		OPCPackage pack = new ZipPackage(in, PackageAccess.READ_WRITE);
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
	public static OPCPackage openOrCreate(File file) throws InvalidFormatException {
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
		if (file == null || (file.exists() && file.isDirectory())) {
			throw new IllegalArgumentException("file");
		}

		if (file.exists()) {
			throw new InvalidOperationException(
					"This package (or file) already exists : use the open() method or delete the file.");
		}

		// Creates a new package
		OPCPackage pkg = new ZipPackage();
		pkg.originalPackagePath = file.getAbsolutePath();

		configurePackage(pkg);
		return pkg;
	}

	public static OPCPackage create(OutputStream output) {
		OPCPackage pkg = new ZipPackage();
		pkg.originalPackagePath = null;
		pkg.output = output;

		configurePackage(pkg);
		return pkg;
	}

	private static void configurePackage(OPCPackage pkg) {
		try {
			// Content type manager
			pkg.contentTypeManager = new ZipContentTypeManager(null, pkg);
			
			// Add default content types for .xml and .rels
			pkg.contentTypeManager.addContentType(
					PackagingURIHelper.createPartName(
							PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_URI),
					ContentTypes.RELATIONSHIPS_PART);
			pkg.contentTypeManager.addContentType(
					PackagingURIHelper.createPartName("/default.xml"),
					ContentTypes.PLAIN_OLD_XML);

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
	 * Close the open, writable package and save its content.
	 * 
	 * If your package is open read only, then you should call {@link #revert()}
	 *  when finished with the package.
	 *
	 * @throws IOException
	 *             If an IO exception occur during the saving process.
	 */
	@Override
    public void close() throws IOException {
		if (this.packageAccess == PackageAccess.READ) {
			logger.log(POILogger.WARN, 
			        "The close() method is intended to SAVE a package. This package is open in READ ONLY mode, use the revert() method instead !");
			revert();
			return;
		}
		if (this.contentTypeManager == null) {
		    logger.log(POILogger.WARN,
		            "Unable to call close() on a package that hasn't been fully opened yet");
			revert();
		    return;
		}

		// Save the content
		ReentrantReadWriteLock l = new ReentrantReadWriteLock();
		try {
			l.writeLock().lock();
			if (this.originalPackagePath != null
					&& !this.originalPackagePath.trim().isEmpty()) {
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
				output.close();
			}
		} finally {
			l.writeLock().unlock();
		}

		// Clear
		this.contentTypeManager.clearAll();
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
     * @param path The full path to the image file.
     */
    public void addThumbnail(String path) throws IOException {
        // Check parameter
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("path");
        }
        String name = path.substring(path.lastIndexOf(File.separatorChar) + 1);

		try (FileInputStream is = new FileInputStream(path)) {
			addThumbnail(name, is);
		}
    }
    /**
     * Add a thumbnail to the package. This method is provided to make easier
     * the addition of a thumbnail in a package. You can do the same work by
     * using the traditionnal relationship and part mechanism.
     *
     * @param filename The full path to the image file.
     * @param data the image data
     */
    public void addThumbnail(String filename, InputStream data) throws IOException {
        // Check parameter
        if (filename == null || filename.isEmpty()) {
            throw new IllegalArgumentException("filename");
        }

        // Create the thumbnail part name
        String contentType = ContentTypes
                .getContentTypeFromFileExtension(filename);
        PackagePartName thumbnailPartName;
        try {
            thumbnailPartName = PackagingURIHelper.createPartName("/docProps/"
                    + filename);
        } catch (InvalidFormatException e) {
            String partName = "/docProps/thumbnail" +
                    filename.substring(filename.lastIndexOf(".") + 1);
            try {
                thumbnailPartName = PackagingURIHelper.createPartName(partName);
            } catch (InvalidFormatException e2) {
                throw new InvalidOperationException(
                        "Can't add a thumbnail file named '" + filename + "'", e2);
            }
        }

        // Check if part already exist
        if (this.getPart(thumbnailPartName) != null) {
            throw new InvalidOperationException(
                    "You already add a thumbnail named '" + filename + "'");
        }

        // Add the thumbnail part to this package.
        PackagePart thumbnailPart = this.createPart(thumbnailPartName,
                contentType, false);

        // Add the relationship between the package and the thumbnail part
        this.addRelationship(thumbnailPartName, TargetMode.INTERNAL,
                PackageRelationshipTypes.THUMBNAIL);

        // Copy file data to the newly created part
        StreamHelper.copyStream(data, thumbnailPart.getOutputStream());
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
        if (packageAccess == PackageAccess.READ) {
            throw new InvalidOperationException(
                    "Operation not allowed, document open in read only mode!");
        }
    }

	/**
	 * Throws an exception if the package access mode is in write only mode
	 * (PackageAccess.Write). This method is call when other methods need write
	 * right.
	 *
	 * @throws InvalidOperationException if a read operation is done on a write only package.
	 * @see org.apache.poi.openxml4j.opc.PackageAccess
	 */
	void throwExceptionIfWriteOnly() throws InvalidOperationException {
		if (packageAccess == PackageAccess.WRITE) {
			throw new InvalidOperationException(
					"Operation not allowed, document open in write only mode!");
		}
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

		if (partName == null) {
			throw new IllegalArgumentException("partName");
		}

		// If the partlist is null, then we parse the package.
		if (partList == null) {
			try {
				getParts();
			} catch (InvalidFormatException e) {
				return null;
			}
		}

		return partList.get(partName);
	}

	/**
	 * Retrieve parts by content type.
	 *
	 * @param contentType
	 *            The content type criteria.
	 * @return All part associated to the specified content type.
	 */
	public ArrayList<PackagePart> getPartsByContentType(String contentType) {
		ArrayList<PackagePart> retArr = new ArrayList<>();
		for (PackagePart part : partList.sortedValues()) {
			if (part.getContentType().equals(contentType)) {
				retArr.add(part);
			}
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
		if (relationshipType == null) {
			throw new IllegalArgumentException("relationshipType");
		}
		ArrayList<PackagePart> retArr = new ArrayList<>();
		for (PackageRelationship rel : getRelationshipsByType(relationshipType)) {
			PackagePart part = getPart(rel);
			if (part != null) {
			    retArr.add(part);
			}
		}
		Collections.sort(retArr);
		return retArr;
	}

	/**
	 * Retrieve parts by name
	 *
	 * @param namePattern
	 *            The pattern for matching the names
	 * @return All parts associated to the specified content type, sorted
	 * in alphanumerically by the part-name
	 */
	public List<PackagePart> getPartsByName(final Pattern namePattern) {
	    if (namePattern == null) {
	        throw new IllegalArgumentException("name pattern must not be null");
	    }
	    Matcher matcher = namePattern.matcher("");
	    ArrayList<PackagePart> result = new ArrayList<>();
	    for (PackagePart part : partList.sortedValues()) {
	        PackagePartName partName = part.getPartName();
	        if (matcher.reset(partName.getName()).matches()) {
	            result.add(part);
	        }
	    }
	    return result;
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
	 * Load the parts of the archive if it has not been done yet. The
	 * relationships of each part are not loaded.
	 * 
	 * Note - Rule M4.1 states that there may only ever be one Core
	 *  Properties Part, but Office produced files will sometimes
	 *  have multiple! As Office ignores all but the first, we relax
	 *  Compliance with Rule M4.1, and ignore all others silently too. 
	 *
	 * @return All this package's parts.
	 * @throws InvalidFormatException if the package is not valid.
	 */
	public ArrayList<PackagePart> getParts() throws InvalidFormatException {
		throwExceptionIfWriteOnly();

		// If the part list is null, we parse the package to retrieve all parts.
		if (partList == null) {
			/* Variables use to validate OPC Compliance */

			// Check rule M4.1 -> A format consumer shall consider more than
			// one core properties relationship for a package to be an error
		    // (We just log it and move on, as real files break this!)
			boolean hasCorePropertiesPart = false;
			boolean needCorePropertiesPart = true;

			partList = getPartsImpl();
			for (PackagePart part : new ArrayList<>(partList.sortedValues())) {
			    part.loadRelationships();

				// Check OPC compliance rule M4.1
				if (ContentTypes.CORE_PROPERTIES_PART.equals(part.getContentType())) {
					if (!hasCorePropertiesPart) {
						hasCorePropertiesPart = true;
					} else {
					   logger.log(POILogger.WARN, "OPC Compliance error [M4.1]: " +
					   		"there is more than one core properties relationship in the package! " +
					   		"POI will use only the first, but other software may reject this file.");
					}
				}

				PartUnmarshaller partUnmarshaller = partUnmarshallers.get(part._contentType);

				if (partUnmarshaller != null) {
					UnmarshallContext context = new UnmarshallContext(this, part._partName);
					try {
						PackagePart unmarshallPart = partUnmarshaller.unmarshall(context, part.getInputStream());
						partList.remove(part.getPartName());
						partList.put(unmarshallPart._partName, unmarshallPart);

						// Core properties case-- use first CoreProperties part we come across
						// and ignore any subsequent ones
						if (unmarshallPart instanceof PackagePropertiesPart &&
								hasCorePropertiesPart &&
								needCorePropertiesPart) {
							this.packageProperties = (PackagePropertiesPart) unmarshallPart;
							needCorePropertiesPart = false;
						}
					} catch (IOException ioe) {
						logger.log(POILogger.WARN, "Unmarshall operation : IOException for "
								+ part._partName);
						continue;
					} catch (InvalidOperationException invoe) {
						throw new InvalidFormatException(invoe.getMessage(), invoe);
					}
				}
			}
		}
		return new ArrayList<>(partList.sortedValues());
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
	 * @throws PartAlreadyExistsException
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
	 * @throws PartAlreadyExistsException
	 *             If rule M1.12 is not verified : Packages shall not contain
	 *             equivalent part names and package implementers shall neither
	 *             create nor recognize packages with equivalent part names.
	 * @see #createPartImpl(PackagePartName, String, boolean)
	 */
	PackagePart createPart(PackagePartName partName, String contentType,
			boolean loadRelationships) {
		throwExceptionIfReadOnly();
		if (partName == null) {
			throw new IllegalArgumentException("partName");
		}

		if (contentType == null || contentType.isEmpty()) {
			throw new IllegalArgumentException("contentType");
		}

		// Check if the specified part name already exists
		if (partList.containsKey(partName)
				&& !partList.get(partName).isDeleted()) {
			throw new PartAlreadyExistsException(
					"A part with the name '" + partName.getName() + "'" +
					" already exists : Packages shall not contain equivalent part names and package" +
					" implementers shall neither create nor recognize packages with equivalent part names. [M1.12]");
		}

		/* Check OPC compliance */

		// Rule [M4.1]: The format designer shall specify and the format producer
		// shall create at most one core properties relationship for a package.
		// A format consumer shall consider more than one core properties
		// relationship for a package to be an error. If present, the
		// relationship shall target the Core Properties part.
		// Note - POI will read files with more than one Core Properties, which
		//  Office sometimes produces, but is strict on generation
		if (contentType.equals(ContentTypes.CORE_PROPERTIES_PART)) {
			if (this.packageProperties != null) {
				throw new InvalidOperationException(
						"OPC Compliance error [M4.1]: you try to add more than one core properties relationship in the package !");
			}
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
	 * @throws InvalidOperationException
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
		if (partName == null || !this.containPart(partName)) {
			throw new IllegalArgumentException("partName");
		}

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
				if (part != null) {
					part.clearRelationships();
				}
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
		if (partName == null) {
			throw new IllegalArgumentException("partName");
		}

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
		if (partName == null || !this.containPart(partName)) {
			throw new IllegalArgumentException("partName");
		}

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
		if (relationshipPartName != null && containPart(relationshipPartName)) {
			this.removePart(relationshipPartName);
		}
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
	@Override
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
				&& this.packageProperties != null) {
			throw new InvalidOperationException(
					"OPC Compliance error [M4.1]: can't add another core properties part ! Use the built-in package method instead.");
		}

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
	@Override
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
	@Override
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
	@Override
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
	@Override
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
     * @throws InvalidOperationException if a read operation is done on a write only package.
	 * @see #getRelationshipsHelper(String)
	 */
	@Override
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
	@Override
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
	@Override
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
	@Override
    public PackageRelationship getRelationship(String id) {
		return this.relationships.getRelationshipByID(id);
	}

	/**
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#hasRelationships()
	 */
	@Override
    public boolean hasRelationships() {
		return (relationships.size() > 0);
	}

	/**
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#isRelationshipExists(org.apache.poi.openxml4j.opc.PackageRelationship)
	 */
	@Override
    public boolean isRelationshipExists(PackageRelationship rel) {
        for (PackageRelationship r : relationships) {
            if (r == rel) {
                return true;
            }
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
		try {
            partMarshallers.remove(new ContentType(contentType));
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * Remove an unmarshaller by its content type.
	 *
	 * @param contentType
	 *            The content type associated with the unmarshaller to remove.
	 */
	public void removeUnmarshaller(String contentType) {
        try {
            partUnmarshallers.remove(new ContentType(contentType));
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
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
	@NotImplemented
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
		if (targetFile == null) {
			throw new IllegalArgumentException("targetFile");
		}

		this.throwExceptionIfReadOnly();
		
		// You shouldn't save the the same file, do a close instead
		if(targetFile.exists() && 
		        targetFile.getAbsolutePath().equals(this.originalPackagePath)) {
		    throw new InvalidOperationException(
		            "You can't call save(File) to save to the currently open " +
		            "file. To save to the current file, please just call close()"
		    );
		}
		
		// Do the save
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(targetFile);
			this.save(fos);
		} finally {
			if (fos != null) {
                fos.close();
            }
		}
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
	 * Get all parts link to the package.
	 *
	 * @return A list of the part owned by the package.
	 */
	protected abstract PackagePartCollection getPartsImpl()
			throws InvalidFormatException;

    /**
     * Replace a content type in this package.
     *
     * <p>
     *     A typical scneario to call this method is to rename a template file to the main format, e.g.
     *     ".dotx" to ".docx"
     *     ".dotm" to ".docm"
     *     ".xltx" to ".xlsx"
     *     ".xltm" to ".xlsm"
     *     ".potx" to ".pptx"
     *     ".potm" to ".pptm"
     * </p>
     * For example, a code converting  a .xlsm macro workbook to .xlsx would look as follows:
     * <p>
     *    <pre><code>
     *
     *     OPCPackage pkg = OPCPackage.open(new FileInputStream("macro-workbook.xlsm"));
     *     pkg.replaceContentType(
     *         "application/vnd.ms-excel.sheet.macroEnabled.main+xml",
     *         "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml");
     *
     *     FileOutputStream out = new FileOutputStream("workbook.xlsx");
     *     pkg.save(out);
     *     out.close();
     *
     *    </code></pre>
     * </p>
     *
     * @param oldContentType  the content type to be replaced
     * @param newContentType  the replacement
     * @return whether replacement was succesfull
     * @since POI-3.8
     */
    public boolean replaceContentType(String oldContentType, String newContentType){
        boolean success = false;
        ArrayList<PackagePart> list = getPartsByContentType(oldContentType);
        for (PackagePart packagePart : list) {
            if (packagePart.getContentType().equals(oldContentType)) {
                PackagePartName partName = packagePart.getPartName();
                contentTypeManager.addContentType(partName, newContentType);
                try {
                    packagePart.setContentType(newContentType);
                } catch (InvalidFormatException e) {
                    throw new OpenXML4JRuntimeException("invalid content type - "+newContentType, e);
                }
                success = true;
                this.isDirty = true;
            }
        }
        return success;
    }

    /**
    * Add the specified part, and register its content type with the content
    * type manager.
    *
    * @param part
    *            The part to add.
    */
    public void registerPartAndContentType(PackagePart part) {
        addPackagePart(part);
        this.contentTypeManager.addContentType(part.getPartName(), part.getContentType());
        this.isDirty = true;
    }

    /**
     * Remove the specified part, and clear its content type from the content
     * type manager.
     *
     * @param partName
     *            The part name of the part to remove.
     */
    public void unregisterPartAndContentType(PackagePartName partName) {
        removePart(partName);
        this.contentTypeManager.removeContentType(partName);
        this.isDirty = true;
    }


    /**
     * Get an unused part index based on the namePattern, which doesn't exist yet
     * and has the lowest positive index
     *
     * @param nameTemplate
     *      The template for new part names containing a {@code '#'} for the index,
     *      e.g. "/ppt/slides/slide#.xml"
     * @return the next available part name index
     * @throws InvalidFormatException if the nameTemplate is null or doesn't contain
     *      the index char (#) or results in an invalid part name 
     */
    public int getUnusedPartIndex(final String nameTemplate) throws InvalidFormatException {
        return partList.getUnusedPartIndex(nameTemplate);
    }
}
