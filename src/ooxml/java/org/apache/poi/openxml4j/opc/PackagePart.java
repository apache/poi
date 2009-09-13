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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.internal.ContentType;

/**
 * Provides a base class for parts stored in a Package.
 *
 * @author Julien Chable
 * @version 0.9
 */
public abstract class PackagePart implements RelationshipSource {

	/**
	 * This part's container.
	 */
	protected OPCPackage _container;

	/**
	 * The part name. (required by the specification [M1.1])
	 */
	protected PackagePartName _partName;

	/**
	 * The type of content of this part. (required by the specification [M1.2])
	 */
	protected ContentType _contentType;

	/**
	 * Flag to know if this part is a relationship.
	 */
	private boolean _isRelationshipPart;

	/**
	 * Flag to know if this part has been logically deleted.
	 */
	private boolean _isDeleted;

	/**
	 * This part's relationships.
	 */
	private PackageRelationshipCollection _relationships;

	/**
	 * Constructor.
	 *
	 * @param pack
	 *            Parent package.
	 * @param partName
	 *            The part name, relative to the parent Package root.
	 * @param contentType
	 *            The content type.
	 * @throws InvalidFormatException
	 *             If the specified URI is not valid.
	 */
	protected PackagePart(OPCPackage pack, PackagePartName partName,
			ContentType contentType) throws InvalidFormatException {
		this(pack, partName, contentType, true);
	}

	/**
	 * Constructor.
	 *
	 * @param pack
	 *            Parent package.
	 * @param partName
	 *            The part name, relative to the parent Package root.
	 * @param contentType
	 *            The content type.
	 * @param loadRelationships
	 *            Specify if the relationships will be loaded
	 * @throws InvalidFormatException
	 *             If the specified URI is not valid.
	 */
	protected PackagePart(OPCPackage pack, PackagePartName partName,
			ContentType contentType, boolean loadRelationships)
			throws InvalidFormatException {
		_partName = partName;
		_contentType = contentType;
		_container = pack;

		// Check if this part is a relationship part
		_isRelationshipPart = this._partName.isRelationshipPartURI();

		// Load relationships if any
		if (loadRelationships)
			loadRelationships();
	}

	/**
	 * Constructor.
	 *
	 * @param pack
	 *            Parent package.
	 * @param partName
	 *            The part name, relative to the parent Package root.
	 * @param contentType
	 *            The Multipurpose Internet Mail Extensions (MIME) content type
	 *            of the part's data stream.
	 */
	public PackagePart(OPCPackage pack, PackagePartName partName,
			String contentType) throws InvalidFormatException {
		this(pack, partName, new ContentType(contentType));
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

		if (_relationships == null) {
			_relationships = new PackageRelationshipCollection();
		}

		URI targetURI;
		try {
			targetURI = new URI(target);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid target - " + e);
		}

		return _relationships.addRelationship(targetURI, TargetMode.EXTERNAL,
				relationshipType, id);
	}

	/**
	 * Add a relationship to a part (except relationships part).
	 *
	 * @param targetPartName
	 *            Name of the target part. This one must be relative to the
	 *            source root directory of the part.
	 * @param targetMode
	 *            Mode [Internal|External].
	 * @param relationshipType
	 *            Type of relationship.
	 * @return The newly created and added relationship
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addRelationship(org.apache.poi.openxml4j.opc.PackagePartName,
	 *      org.apache.poi.openxml4j.opc.TargetMode, java.lang.String)
	 */
	public PackageRelationship addRelationship(PackagePartName targetPartName,
			TargetMode targetMode, String relationshipType) {
		return addRelationship(targetPartName, targetMode, relationshipType,
				null);
	}

	/**
	 * Add a relationship to a part (except relationships part).
	 * <p>
	 * Check rule M1.25: The Relationships part shall not have relationships to
	 * any other part. Package implementers shall enforce this requirement upon
	 * the attempt to create such a relationship and shall treat any such
	 * relationship as invalid.
	 * </p>
	 * @param targetPartName
	 *            Name of the target part. This one must be relative to the
	 *            source root directory of the part.
	 * @param targetMode
	 *            Mode [Internal|External].
	 * @param relationshipType
	 *            Type of relationship.
	 * @param id
	 *            Relationship unique id.
	 * @return The newly created and added relationship
	 *
	 * @throws InvalidFormatException
	 *             If the URI point to a relationship part URI.
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addRelationship(org.apache.poi.openxml4j.opc.PackagePartName,
	 *      org.apache.poi.openxml4j.opc.TargetMode, java.lang.String, java.lang.String)
	 */
	public PackageRelationship addRelationship(PackagePartName targetPartName,
			TargetMode targetMode, String relationshipType, String id) {
		_container.throwExceptionIfReadOnly();

		if (targetPartName == null) {
			throw new IllegalArgumentException("targetPartName");
		}
		if (targetMode == null) {
			throw new IllegalArgumentException("targetMode");
		}
		if (relationshipType == null) {
			throw new IllegalArgumentException("relationshipType");
		}

		if (this._isRelationshipPart || targetPartName.isRelationshipPartURI()) {
			throw new InvalidOperationException(
					"Rule M1.25: The Relationships part shall not have relationships to any other part.");
		}

		if (_relationships == null) {
			_relationships = new PackageRelationshipCollection();
		}

		return _relationships.addRelationship(targetPartName.getURI(),
				targetMode, relationshipType, id);
	}

	/**
	 * Add a relationship to a part (except relationships part).
	 *
	 * @param targetURI
	 *            URI the target part. Must be relative to the source root
	 *            directory of the part.
	 * @param targetMode
	 *            Mode [Internal|External].
	 * @param relationshipType
	 *            Type of relationship.
	 * @return The newly created and added relationship
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addRelationship(org.apache.poi.openxml4j.opc.PackagePartName,
	 *      org.apache.poi.openxml4j.opc.TargetMode, java.lang.String)
	 */
	public PackageRelationship addRelationship(URI targetURI,
			TargetMode targetMode, String relationshipType) {
		return addRelationship(targetURI, targetMode, relationshipType, null);
	}

	/**
	 * Add a relationship to a part (except relationships part).
	 * <p>
	 * Check rule M1.25: The Relationships part shall not have relationships to
	 * any other part. Package implementers shall enforce this requirement upon
	 * the attempt to create such a relationship and shall treat any such
	 * relationship as invalid.
	 * </p>
	 * @param targetURI
	 *            URI of the target part. Must be relative to the source root
	 *            directory of the part.
	 * @param targetMode
	 *            Mode [Internal|External].
	 * @param relationshipType
	 *            Type of relationship.
	 * @param id
	 *            Relationship unique id.
	 * @return The newly created and added relationship
	 *
	 * @throws InvalidFormatException
	 *             If the URI point to a relationship part URI.
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#addRelationship(org.apache.poi.openxml4j.opc.PackagePartName,
	 *      org.apache.poi.openxml4j.opc.TargetMode, java.lang.String, java.lang.String)
	 */
	public PackageRelationship addRelationship(URI targetURI,
			TargetMode targetMode, String relationshipType, String id) {
		_container.throwExceptionIfReadOnly();

		if (targetURI == null) {
			throw new IllegalArgumentException("targetPartName");
		}
		if (targetMode == null) {
			throw new IllegalArgumentException("targetMode");
		}
		if (relationshipType == null) {
			throw new IllegalArgumentException("relationshipType");
		}

		// Try to retrieve the target part

		if (this._isRelationshipPart
				|| PackagingURIHelper.isRelationshipPartURI(targetURI)) {
			throw new InvalidOperationException(
					"Rule M1.25: The Relationships part shall not have relationships to any other part.");
		}

		if (_relationships == null) {
			_relationships = new PackageRelationshipCollection();
		}

		return _relationships.addRelationship(targetURI,
				targetMode, relationshipType, id);
	}

	/**
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#clearRelationships()
	 */
	public void clearRelationships() {
		if (_relationships != null) {
			_relationships.clear();
		}
	}

	/**
	 * Delete the relationship specified by its id.
	 *
	 * @param id
	 *            The ID identified the part to delete.
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#removeRelationship(java.lang.String)
	 */
	public void removeRelationship(String id) {
		this._container.throwExceptionIfReadOnly();
		if (this._relationships != null)
			this._relationships.removeRelationship(id);
	}

	/**
	 * Retrieve all the relationships attached to this part.
	 *
	 * @return This part's relationships.
	 * @throws OpenXML4JException
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#getRelationships()
	 */
	public PackageRelationshipCollection getRelationships()
			throws InvalidFormatException {
		return getRelationshipsCore(null);
	}

	/**
	 * Retrieves a package relationship from its id.
	 *
	 * @param id
	 *            ID of the package relationship to retrieve.
	 * @return The package relationship
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#getRelationship(java.lang.String)
	 */
	public PackageRelationship getRelationship(String id) {
		return this._relationships.getRelationshipByID(id);
	}

	/**
	 * Retrieve all relationships attached to this part which have the specified
	 * type.
	 *
	 * @param relationshipType
	 *            Relationship type filter.
	 * @return All relationships from this part that have the specified type.
	 * @throws InvalidFormatException
	 *             If an error occurs while parsing the part.
	 * @throws InvalidOperationException
	 *             If the package is open in write only mode.
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#getRelationshipsByType(java.lang.String)
	 */
	public PackageRelationshipCollection getRelationshipsByType(
			String relationshipType) throws InvalidFormatException {
		_container.throwExceptionIfWriteOnly();

		return getRelationshipsCore(relationshipType);
	}

	/**
	 * Implementation of the getRelationships method().
	 *
	 * @param filter
	 *            Relationship type filter. If <i>null</i> then the filter is
	 *            disabled and return all the relationships.
	 * @return All relationships from this part that have the specified type.
	 * @throws InvalidFormatException
	 *             Throws if an error occurs during parsing the relationships
	 *             part.
	 * @throws InvalidOperationException
	 *             Throws if the package is open en write only mode.
	 * @see #getRelationshipsByType(String)
	 */
	private PackageRelationshipCollection getRelationshipsCore(String filter)
			throws InvalidFormatException {
		this._container.throwExceptionIfWriteOnly();
		if (_relationships == null) {
			this.throwExceptionIfRelationship();
			_relationships = new PackageRelationshipCollection(this);
		}
		return new PackageRelationshipCollection(_relationships, filter);
	}

	/**
	 * Knows if the part have any relationships.
	 *
	 * @return <b>true</b> if the part have at least one relationship else
	 *         <b>false</b>.
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#hasRelationships()
	 */
	public boolean hasRelationships() {
		return (!this._isRelationshipPart && (_relationships != null && _relationships
				.size() > 0));
	}

	/**
	 * Checks if the specified relationship is part of this package part.
	 *
	 * @param rel
	 *            The relationship to check.
	 * @return <b>true</b> if the specified relationship exists in this part,
	 *         else returns <b>false</b>
	 * @see org.apache.poi.openxml4j.opc.RelationshipSource#isRelationshipExists(org.apache.poi.openxml4j.opc.PackageRelationship)
	 */
	public boolean isRelationshipExists(PackageRelationship rel) {
        try {
            for (PackageRelationship r : this.getRelationships()) {
                if (r == rel)
                    return true;
            }
        } catch (InvalidFormatException e){
            ;
        }
        return false;
	}

	/**
	 * Get the input stream of this part to read its content.
	 *
	 * @return The input stream of the content of this part, else
	 *         <code>null</code>.
	 */
	public InputStream getInputStream() throws IOException {
		InputStream inStream = this.getInputStreamImpl();
		if (inStream == null) {
			throw new IOException("Can't obtain the input stream from "
					+ _partName.getName());
		}
		return inStream;
	}

	/**
	 * Get the output stream of this part. If the part is originally embedded in
	 * Zip package, it'll be transform intot a <i>MemoryPackagePart</i> in
	 * order to write inside (the standard Java API doesn't allow to write in
	 * the file)
	 *
	 * @see org.apache.poi.openxml4j.opc.internal.MemoryPackagePart
	 */
	public OutputStream getOutputStream() {
		OutputStream outStream;
		// If this part is a zip package part (read only by design) we convert
		// this part into a MemoryPackagePart instance for write purpose.
		if (this instanceof ZipPackagePart) {
			// Delete logically this part
			_container.removePart(this._partName);

			// Create a memory part
			PackagePart part = _container.createPart(this._partName,
					this._contentType.toString(), false);
			part._relationships = this._relationships;
			if (part == null) {
				throw new InvalidOperationException(
						"Can't create a temporary part !");
			}
			outStream = part.getOutputStreamImpl();
		} else {
			outStream = this.getOutputStreamImpl();
		}
		return outStream;
	}

	/**
	 * Throws an exception if this package part is a relationship part.
	 *
	 * @throws InvalidOperationException
	 *             If this part is a relationship part.
	 */
	private void throwExceptionIfRelationship()
			throws InvalidOperationException {
		if (this._isRelationshipPart)
			throw new InvalidOperationException(
					"Can do this operation on a relationship part !");
	}

	/**
	 * Ensure the package relationships collection instance is built.
	 *
	 * @throws InvalidFormatException
	 *             Throws if
	 */
	private void loadRelationships() throws InvalidFormatException {
		if (this._relationships == null && !this._isRelationshipPart) {
			this.throwExceptionIfRelationship();
			_relationships = new PackageRelationshipCollection(this);
		}
	}

	/*
	 * Accessors
	 */

	/**
	 * @return the uri
	 */
	public PackagePartName getPartName() {
		return _partName;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return _contentType.toString();
	}

	/**
	 * Set the content type.
	 *
	 * @param contentType
	 *            the contentType to set
	 *
	 * @throws InvalidFormatException
	 *             Throws if the content type is not valid.
	 * @throws InvalidOperationException
	 *             Throws if you try to change the content type whereas this
	 *             part is already attached to a package.
	 */
	public void setContentType(String contentType)
			throws InvalidFormatException {
		if (_container == null)
			this._contentType = new ContentType(contentType);
		else
			throw new InvalidOperationException(
					"You can't change the content type of a part.");
	}

	public OPCPackage getPackage() {
		return _container;
	}

	/**
	 * @return true if this part is a relationship
	 */
	public boolean isRelationshipPart() {
		return this._isRelationshipPart;
	}

	/**
	 * @return true if this part has been logically deleted
	 */
	public boolean isDeleted() {
		return _isDeleted;
	}

	/**
	 * @param isDeleted
	 *            the isDeleted to set
	 */
	public void setDeleted(boolean isDeleted) {
		this._isDeleted = isDeleted;
	}

	@Override
	public String toString() {
		return "Name: " + this._partName + " - Content Type: "
				+ this._contentType.toString();
	}

	/*-------------- Abstract methods ------------- */

	/**
	 * Abtract method that get the input stream of this part.
	 *
	 * @exception IOException
	 *                Throws if an IO Exception occur in the implementation
	 *                method.
	 */
	protected abstract InputStream getInputStreamImpl() throws IOException;

	/**
	 * Abstract method that get the output stream of this part.
	 */
	protected abstract OutputStream getOutputStreamImpl();

	/**
	 * Save the content of this part and the associated relationships part (if
	 * this part own at least one relationship) into the specified output
	 * stream.
	 *
	 * @param zos
	 *            Output stream to save this part.
	 * @throws OpenXML4JException
	 *             If any exception occur.
	 */
	public abstract boolean save(OutputStream zos) throws OpenXML4JException;

	/**
	 * Load the content of this part.
	 *
	 * @param ios
	 *            The input stream of the content to load.
	 * @return <b>true</b> if the content has been successfully loaded, else
	 *         <b>false</b>.
	 * @throws InvalidFormatException
	 *             Throws if the content format is invalid.
	 */
	public abstract boolean load(InputStream ios) throws InvalidFormatException;

	/**
	 * Close this part : flush this part, close the input stream and output
	 * stream. After this method call, the part must be available for packaging.
	 */
	public abstract void close();

	/**
	 * Flush the content of this part. If the input stream and/or output stream
	 * as in a waiting state to read or write, the must to empty their
	 * respective buffer.
	 */
	public abstract void flush();
}
