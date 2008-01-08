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
package org.apache.poi.hxf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.POIXMLDocument;
import org.apache.xmlbeans.XmlException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackageAccess;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackagingURIHelper;
import org.openxml4j.opc.internal.PackagePropertiesPart;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties;
import org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.PropertiesDocument;

/**
 * Parent class of the low level interface to  
 *  all POI XML (OOXML) implementations.
 * Normal users should probably deal with things that
 *  extends {@link POIXMLDocument}, unless they really
 *  do need to get low level access to the files.
 *  
 * If you are using these low level classes, then you
 *  will almost certainly need to refer to the OOXML
 *  specifications from
 *  http://www.ecma-international.org/publications/standards/Ecma-376.htm
 *  
 * WARNING - APIs expected to change rapidly
 */
public abstract class HXFDocument {
	public static final String CORE_PROPERTIES_REL_TYPE = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";
	public static final String EXTENDED_PROPERTIES_REL_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";
	
	/**
	 * File package/container.
	 */
	protected Package container;
	/**
	 * The Package Part for our base document
	 */
	protected PackagePart basePart;
	/**
	 * The base document of this instance, eg Workbook for
	 *  xslsx
	 */
	protected Document baseDocument;
	
	protected HXFDocument(Package container, String baseContentType) throws OpenXML4JException {
		this.container = container;
		
		// Find the base document
		basePart = getSinglePartByType(baseContentType);
		
		// And load it up
		try {
			SAXReader reader = new SAXReader();
			baseDocument = reader.read(basePart.getInputStream());
		} catch (DocumentException e) {
			throw new OpenXML4JException(e.getMessage());
		} catch (IOException ioe) {
			throw new OpenXML4JException(ioe.getMessage());
		}
	}
	
	/**
	 * Fetches the (single) PackagePart with the supplied
	 *  content type.
	 * @param contentType The content type to search for
	 * @throws IllegalArgumentException If we don't find a single part of that type
	 */
	private PackagePart getSinglePartByType(String contentType) throws IllegalArgumentException {
		ArrayList<PackagePart> parts =
			container.getPartsByContentType(contentType);
		if(parts.size() != 1) {
			throw new IllegalArgumentException("Expecting one entry with content type of " + contentType + ", but found " + parts.size());
		}
		return parts.get(0);
	}

	/**
	 * Fetches the (single) PackagePart which is defined as
	 *  the supplied relation content type of the specified part, 
	 *  or null if none found.
	 * @param relationType The relation content type to search for
	 * @throws IllegalArgumentException If we find more than one part of that type
	 * TODO: this sucks! Make Package and PackagePart implement common intf that defines getRelationshipsByType & friends
	 */
	protected PackagePart getSinglePartByRelationType(String relationType, PackagePart part) throws IllegalArgumentException, OpenXML4JException {
		PackageRelationshipCollection rels =
			part.getRelationshipsByType(relationType);
		if(rels.size() == 0) {
			return null;
		}
		if(rels.size() > 1) {
			throw new IllegalArgumentException("Found " + rels.size() + " relations for the type " + relationType + ", should only ever be one!");
		}
		PackageRelationship rel = rels.getRelationship(0);
		return getPackagePart(rel);
	}
	
	/**
	 * Fetches the (single) PackagePart which is defined as
	 *  the supplied relation content type of the base
	 *  container, or null if none found.
	 * @param relationType The relation content type to search for
	 * @throws IllegalArgumentException If we find more than one part of that type
	 */
	protected PackagePart getSinglePartByRelationType(String relationType) throws IllegalArgumentException, OpenXML4JException {
		PackageRelationshipCollection rels =
			container.getRelationshipsByType(relationType);
		if(rels.size() == 0) {
			return null;
		}
		if(rels.size() > 1) {
			throw new IllegalArgumentException("Found " + rels.size() + " relations for the type " + relationType + ", should only ever be one!");
		}
		PackageRelationship rel = rels.getRelationship(0);
		return getPackagePart(rel);
	}
	
	/**
	 * Retrieves the PackagePart for the given relation
	 *  id. This will normally come from a r:id attribute
	 *  on part of the base document. 
	 * @param partId The r:id pointing to the other PackagePart
	 */
	protected PackagePart getRelatedPackagePart(String partId) {
		PackageRelationship rel =
			basePart.getRelationship(partId);
		return getPackagePart(rel);
	}

	/**
	 * Retrieves the PackagePart for the given Relationship
	 *  object. Normally you'll want to go via a content type
	 *  or r:id to get one of those.
	 */
	protected PackagePart getPackagePart(PackageRelationship rel) {
		PackagePartName relName;
		try {
			relName = PackagingURIHelper.createPartName(rel.getTargetURI());
		} catch(InvalidFormatException e) {
			throw new InternalError(e.getMessage());
		}
		
		PackagePart part = container.getPart(relName);
		if(part == null) {
			throw new IllegalArgumentException("No part found for rel " + rel);
		}
		return part;
	}
	
	/**
	 * Retrieves all the PackageParts which are defined as
	 *  relationships of the base document with the
	 *  specified content type.
	 */
	protected PackagePart[] getRelatedByType(String contentType) throws InvalidFormatException {
		PackageRelationshipCollection partsC =
			basePart.getRelationshipsByType(contentType);
		
		PackagePart[] parts = new PackagePart[partsC.size()];
		int count = 0;
		for (PackageRelationship rel : partsC) {
			parts[count] = getPackagePart(rel);
			count++;
		}
		return parts;
	}

	/**
	 * Get the package container.
	 * @return The package associated to this document.
	 */
	public Package getPackage() {
		return container;
	}
	
	/**
	 * Get the core document properties (core ooxml properties).
	 */
	public PackagePropertiesPart getCoreProperties() throws OpenXML4JException, XmlException, IOException {
		PackagePart propsPart = getSinglePartByRelationType(CORE_PROPERTIES_REL_TYPE);
		if(propsPart == null) {
			return null;
		}
		return (PackagePropertiesPart)propsPart;
	}
	
	/**
	 * Get the extended document properties (extended ooxml properties)
	 */
	public CTProperties getExtendedProperties() throws OpenXML4JException, XmlException, IOException {
		PackagePart propsPart = getSinglePartByRelationType(EXTENDED_PROPERTIES_REL_TYPE);
		
		PropertiesDocument props = PropertiesDocument.Factory.parse(
				propsPart.getInputStream());
		return props.getProperties();
	}
	
	/**
	 * Returns an opened OOXML Package for the supplied File
	 * @param f File to open
	 */
	public static Package openPackage(File f) throws InvalidFormatException {
		return Package.open(f.toString(), PackageAccess.READ_WRITE);
	}
}
