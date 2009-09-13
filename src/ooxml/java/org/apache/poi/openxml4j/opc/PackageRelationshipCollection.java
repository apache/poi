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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

/**
 * Represents a collection of PackageRelationship elements that are owned by a
 * given PackagePart or the Package.
 *
 * @author Julien Chable, CDubettier
 * @version 0.1
 */
public final class PackageRelationshipCollection implements
		Iterable<PackageRelationship> {

    private static POILogger logger = POILogFactory.getLogger(PackageRelationshipCollection.class);

	/**
	 * Package relationships ordered by ID.
	 */
	private TreeMap<String, PackageRelationship> relationshipsByID;

	/**
	 * Package relationships ordered by type.
	 */
	private TreeMap<String, PackageRelationship> relationshipsByType;

	/**
	 * This relationshipPart.
	 */
	private PackagePart relationshipPart;

	/**
	 * Source part.
	 */
	private PackagePart sourcePart;

	/**
	 * This part name.
	 */
	private PackagePartName partName;

	/**
	 * Reference to the package.
	 */
	private OPCPackage container;

	/**
	 * Constructor.
	 */
	PackageRelationshipCollection() {
		relationshipsByID = new TreeMap<String, PackageRelationship>();
		relationshipsByType = new TreeMap<String, PackageRelationship>();
	}

	/**
	 * Copy constructor.
	 *
	 * This collection will contain only elements from the specified collection
	 * for which the type is compatible with the specified relationship type
	 * filter.
	 *
	 * @param coll
	 *            Collection to import.
	 * @param filter
	 *            Relationship type filter.
	 */
	public PackageRelationshipCollection(PackageRelationshipCollection coll,
			String filter) {
		this();
		for (PackageRelationship rel : coll.relationshipsByID.values()) {
			if (filter == null || rel.getRelationshipType().equals(filter))
				addRelationship(rel);
		}
	}

	/**
	 * Constructor.
	 */
	public PackageRelationshipCollection(OPCPackage container)
			throws InvalidFormatException {
		this(container, null);
	}

	/**
	 * Constructor.
	 *
	 * @throws InvalidFormatException
	 *             Throws if the format of the content part is invalid.
	 *
	 * @throws InvalidOperationException
	 *             Throws if the specified part is a relationship part.
	 */
	public PackageRelationshipCollection(PackagePart part)
			throws InvalidFormatException {
		this(part._container, part);
	}

	/**
	 * Constructor. Parse the existing package relationship part if one exists.
	 *
	 * @param container
	 *            The parent package.
	 * @param part
	 *            The part that own this relationships collection. If <b>null</b>
	 *            then this part is considered as the package root.
	 * @throws InvalidFormatException
	 *             If an error occurs during the parsing of the relatinships
	 *             part fo the specified part.
	 */
	public PackageRelationshipCollection(OPCPackage container, PackagePart part)
			throws InvalidFormatException {
		this();

		if (container == null)
			throw new IllegalArgumentException("container");

		// Check if the specified part is not a relationship part
		if (part != null && part.isRelationshipPart())
			throw new IllegalArgumentException("part");

		this.container = container;
		this.sourcePart = part;
		this.partName = getRelationshipPartName(part);
		if ((container.getPackageAccess() != PackageAccess.WRITE)
				&& container.containPart(this.partName)) {
			relationshipPart = container.getPart(this.partName);
			parseRelationshipsPart(relationshipPart);
		}
	}

	/**
	 * Get the relationship part name of the specified part.
	 *
	 * @param part
	 *            The part .
	 * @return The relationship part name of the specified part. Be careful,
	 *         only the correct name is returned, this method does not check if
	 *         the part really exist in a package !
	 * @throws InvalidOperationException
	 *             Throws if the specified part is a relationship part.
	 */
	private static PackagePartName getRelationshipPartName(PackagePart part)
			throws InvalidOperationException {
		PackagePartName partName;
		if (part == null) {
			partName = PackagingURIHelper.PACKAGE_ROOT_PART_NAME;
		} else {
			partName = part.getPartName();
		}
		return PackagingURIHelper.getRelationshipPartName(partName);
	}

	/**
	 * Add the specified relationship to the collection.
	 *
	 * @param relPart
	 *            The relationship to add.
	 */
	public void addRelationship(PackageRelationship relPart) {
		relationshipsByID.put(relPart.getId(), relPart);
		relationshipsByType.put(relPart.getRelationshipType(), relPart);
	}

	/**
	 * Add a relationship to the collection.
	 *
	 * @param targetUri
	 *            Target URI.
	 * @param targetMode
	 *            The target mode : INTERNAL or EXTERNAL
	 * @param relationshipType
	 *            Relationship type.
	 * @param id
	 *            Relationship ID.
	 * @return The newly created relationship.
	 * @see PackageAccess
	 */
	public PackageRelationship addRelationship(URI targetUri,
			TargetMode targetMode, String relationshipType, String id) {

		if (id == null) {
			// Generate a unique ID is id parameter is null.
			int i = 0;
			do {
				id = "rId" + ++i;
			} while (relationshipsByID.get(id) != null);
		}

		PackageRelationship rel = new PackageRelationship(container,
				sourcePart, targetUri, targetMode, relationshipType, id);
		relationshipsByID.put(rel.getId(), rel);
		relationshipsByType.put(rel.getRelationshipType(), rel);
		return rel;
	}

	/**
	 * Remove a relationship by its ID.
	 *
	 * @param id
	 *            The relationship ID to remove.
	 */
	public void removeRelationship(String id) {
		if (relationshipsByID != null && relationshipsByType != null) {
			PackageRelationship rel = relationshipsByID.get(id);
			if (rel != null) {
				relationshipsByID.remove(rel.getId());
				relationshipsByType.values().remove(rel);
			}
		}
	}

	/**
	 * Remove a relationship by its reference.
	 *
	 * @param rel
	 *            The relationship to delete.
	 */
	public void removeRelationship(PackageRelationship rel) {
		if (rel == null)
			throw new IllegalArgumentException("rel");

		relationshipsByID.values().remove(rel);
		relationshipsByType.values().remove(rel);
	}

	/**
	 * Retrieves a relationship by its index in the collection.
	 *
	 * @param index
	 *            Must be a value between [0-relationships_count-1]
	 */
	public PackageRelationship getRelationship(int index) {
		if (index < 0 || index > relationshipsByID.values().size())
			throw new IllegalArgumentException("index");

		PackageRelationship retRel = null;
		int i = 0;
		for (PackageRelationship rel : relationshipsByID.values()) {
			if (index == i++)
				return rel;
		}
		return retRel;
	}

	/**
	 * Retrieves a package relationship based on its id.
	 *
	 * @param id
	 *            ID of the package relationship to retrieve.
	 * @return The package relationship identified by the specified id.
	 */
	public PackageRelationship getRelationshipByID(String id) {
		return relationshipsByID.get(id);
	}

	/**
	 * Get the numbe rof relationships in the collection.
	 */
	public int size() {
		return relationshipsByID.values().size();
	}

	/**
	 * Parse the relationship part and add all relationship in this collection.
	 *
	 * @param relPart
	 *            The package part to parse.
	 * @throws InvalidFormatException
	 *             Throws if the relationship part is invalid.
	 */
	private void parseRelationshipsPart(PackagePart relPart)
			throws InvalidFormatException {
		try {
			SAXReader reader = new SAXReader();
			logger.log(POILogger.DEBUG, "Parsing relationship: " + relPart.getPartName());
			Document xmlRelationshipsDoc = reader
					.read(relPart.getInputStream());

			// Browse default types
			Element root = xmlRelationshipsDoc.getRootElement();

			// Check OPC compliance M4.1 rule
			boolean fCorePropertiesRelationship = false;

			for (Iterator i = root
					.elementIterator(PackageRelationship.RELATIONSHIP_TAG_NAME); i
					.hasNext();) {
				Element element = (Element) i.next();
				// Relationship ID
				String id = element.attribute(
						PackageRelationship.ID_ATTRIBUTE_NAME).getValue();
				// Relationship type
				String type = element.attribute(
						PackageRelationship.TYPE_ATTRIBUTE_NAME).getValue();

				/* Check OPC Compliance */
				// Check Rule M4.1
				if (type.equals(PackageRelationshipTypes.CORE_PROPERTIES))
					if (!fCorePropertiesRelationship)
						fCorePropertiesRelationship = true;
					else
						throw new InvalidFormatException(
								"OPC Compliance error [M4.1]: there is more than one core properties relationship in the package !");

				/* End OPC Compliance */

				// TargetMode (default value "Internal")
				Attribute targetModeAttr = element
						.attribute(PackageRelationship.TARGET_MODE_ATTRIBUTE_NAME);
				TargetMode targetMode = TargetMode.INTERNAL;
				if (targetModeAttr != null) {
					targetMode = targetModeAttr.getValue().toLowerCase()
							.equals("internal") ? TargetMode.INTERNAL
							: TargetMode.EXTERNAL;
				}

				// Target converted in URI
				URI target;
				String value = "";
				try {
					value = element.attribute(
							PackageRelationship.TARGET_ATTRIBUTE_NAME)
							.getValue();

					if (value.indexOf("\\") != -1) {
						logger
								.log(POILogger.INFO, "target contains \\ therefore not a valid URI"
										+ value + " replaced by /");
						value = value.replaceAll("\\\\", "/");
						// word can save external relationship with a \ instead
						// of /
					}

					target = new URI(value);
				} catch (URISyntaxException e) {
					logger.log(POILogger.ERROR, "Cannot convert " + value
							+ " in a valid relationship URI-> ignored", e);
					continue;
				}
				addRelationship(target, targetMode, type, id);
			}
		} catch (Exception e) {
			logger.log(POILogger.ERROR, e);
			throw new InvalidFormatException(e.getMessage());
		}
	}

	/**
	 * Retrieves all relations with the specified type.
	 *
	 * @param typeFilter
	 *            Relationship type filter. If <b>null</b> then all
	 *            relationships are returned.
	 * @return All relationships of the type specified by the filter.
	 */
	public PackageRelationshipCollection getRelationships(String typeFilter) {
		PackageRelationshipCollection coll = new PackageRelationshipCollection(
				this, typeFilter);
		return coll;
	}

	/**
	 * Get this collection's iterator.
	 */
	public Iterator<PackageRelationship> iterator() {
		return relationshipsByID.values().iterator();
	}

	/**
	 * Get an iterator of a collection with all relationship with the specified
	 * type.
	 *
	 * @param typeFilter
	 *            Type filter.
	 * @return An iterator to a collection containing all relationships with the
	 *         specified type contain in this collection.
	 */
	public Iterator<PackageRelationship> iterator(String typeFilter) {
		ArrayList<PackageRelationship> retArr = new ArrayList<PackageRelationship>();
		for (PackageRelationship rel : relationshipsByID.values()) {
			if (rel.getRelationshipType().equals(typeFilter))
				retArr.add(rel);
		}
		return retArr.iterator();
	}

	/**
	 * Clear all relationships.
	 */
	public void clear() {
		relationshipsByID.clear();
		relationshipsByType.clear();
	}

	@Override
	public String toString() {
		String str;
		if (relationshipsByID == null) {
			str = "relationshipsByID=null";
		} else {
			str = relationshipsByID.size() + " relationship(s) = [";
		}
		if ((relationshipPart != null) && (relationshipPart._partName != null)) {
			str = str + "," + relationshipPart._partName;
		} else {
			str = str + ",relationshipPart=null";
		}

		// Source of this relationship
		if ((sourcePart != null) && (sourcePart._partName != null)) {
			str = str + "," + sourcePart._partName;
		} else {
			str = str + ",sourcePart=null";
		}
		if (partName != null) {
			str = str + "," + partName;
		} else {
			str = str + ",uri=null)";
		}
		return str + "]";
	}
}
