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
package org.apache.poi;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackagePartName;
import org.openxml4j.opc.PackageRelationship;
import org.openxml4j.opc.PackageRelationshipCollection;
import org.openxml4j.opc.PackageRelationshipTypes;
import org.openxml4j.opc.PackagingURIHelper;

public abstract class POIXMLDocument {

    public static final String CORE_PROPERTIES_REL_TYPE = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";
    public static final String EXTENDED_PROPERTIES_REL_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";
    public static final String CUSTOM_PROPERTIES_REL_TYPE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";
    
    // OLE embeddings relation name
    public static final String OLE_OBJECT_REL_TYPE="http://schemas.openxmlformats.org/officeDocument/2006/relationships/oleObject";
    
    // Embedded OPC documents relation name
    public static final String PACK_OBJECT_REL_TYPE="http://schemas.openxmlformats.org/officeDocument/2006/relationships/package";
    
    /** The OPC Package */
    private Package pkg;

    /** The OPC core Package Part */
    private PackagePart corePart;
    
    /**
     * The properties of the OPC package, opened as needed
     */
    private POIXMLProperties properties;
    
	/**
	 * The embedded OLE2 files in the OPC package
	 */
    protected List<PackagePart> embedds = new LinkedList<PackagePart>();
    
    protected POIXMLDocument() {}
    
    protected POIXMLDocument(Package pkg) throws IOException {
    	try {
	    	this.pkg = pkg;
	    	
	        PackageRelationship coreDocRelationship = this.pkg.getRelationshipsByType(
	                PackageRelationshipTypes.CORE_DOCUMENT).getRelationship(0);
	    
	        // Get core part
	        this.corePart = this.pkg.getPart(coreDocRelationship);

	        // Verify it's there
	        if(corePart == null) {
	        	throw new IllegalArgumentException("No core part found for this document! Nothing with " + coreDocRelationship.getRelationshipType() + " present as a relation.");
	        }
        } catch (OpenXML4JException e) {
            throw new IOException(e.toString());
    	}
    }
    
    protected POIXMLDocument(String path) throws IOException {
   		this(openPackage(path));
    }
    
    /**
     * Wrapper to open a package, returning an IOException
     *  in the event of a problem.
     * Works around shortcomings in java's this() constructor calls
     */
    public static Package openPackage(String path) throws IOException {
        try {
            return Package.open(path);
        } catch (InvalidFormatException e) {
            throw new IOException(e.toString());
        }
    }
    public static Package openPackage(InputStream is) throws IOException {
        try {
            return Package.open(is);
        } catch (InvalidFormatException e) {
            throw new IOException(e.toString());
        }
    }

    protected Package getPackage() {
        return this.pkg;
    }
    
    protected PackagePart getCorePart() {
        return this.corePart;
    }

    /**
     * Get the PackagePart that is the target of a relationship.
     * 
     * @param rel The relationship
     * @return The target part
     * @throws InvalidFormatException
     */
    protected PackagePart getTargetPart(PackageRelationship rel) throws InvalidFormatException {
        return getTargetPart(getPackage(), rel);
    }
    /**
     * Get the PackagePart that is the target of a relationship.
     * 
     * @param rel The relationship
     * @param pkg The package to fetch from
     * @return The target part
     * @throws InvalidFormatException
     */
    public static PackagePart getTargetPart(Package pkg, PackageRelationship rel) throws InvalidFormatException {
        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        PackagePart part = pkg.getPart(relName);
        if (part == null) {
            throw new IllegalArgumentException("No part found for relationship " + rel);
        }
        return part;
    }

	/**
	 * Fetches the (single) PackagePart which is defined as
	 *  the supplied relation content type of the base
	 *  package/container, or null if none found.
	 * @param relationType The relation content type to search for
	 * @throws IllegalArgumentException If we find more than one part of that type
	 */
	protected PackagePart getSinglePartByRelationType(String relationType) throws IllegalArgumentException, OpenXML4JException {
		PackageRelationshipCollection rels =
			pkg.getRelationshipsByType(relationType);
		if(rels.size() == 0) {
			return null;
		}
		if(rels.size() > 1) {
			throw new IllegalArgumentException("Found " + rels.size() + " relations for the type " + relationType + ", should only ever be one!");
		}
		PackageRelationship rel = rels.getRelationship(0);
		return getTargetPart(rel);
	}

	/**
	 * Retrieves all the PackageParts which are defined as
	 *  relationships of the base document with the
	 *  specified content type.
	 */
	protected PackagePart[] getRelatedByType(String contentType) throws InvalidFormatException {
		PackageRelationshipCollection partsC =
			getCorePart().getRelationshipsByType(contentType);
		
		PackagePart[] parts = new PackagePart[partsC.size()];
		int count = 0;
		for (PackageRelationship rel : partsC) {
			parts[count] = getTargetPart(rel);
			count++;
		}
		return parts;
	}

	
    
    /**
     * Checks that the supplied InputStream (which MUST
     *  support mark and reset, or be a PushbackInputStream) 
     *  has a OOXML (zip) header at the start of it.
     * If your InputStream does not support mark / reset,
     *  then wrap it in a PushBackInputStream, then be
     *  sure to always use that, and not the original!
     * @param inp An InputStream which supports either mark/reset, or is a PushbackInputStream 
     */
    public static boolean hasOOXMLHeader(InputStream inp) throws IOException {
    	// We want to peek at the first 4 bytes 
    	inp.mark(4);

    	byte[] header = new byte[4];
    	IOUtils.readFully(inp, header);

        // Wind back those 4 bytes
        if(inp instanceof PushbackInputStream) {
        	PushbackInputStream pin = (PushbackInputStream)inp;
        	pin.unread(header);
        } else {
        	inp.reset();
        }
    	
    	// Did it match the ooxml zip signature?
        return (
        	header[0] == POIFSConstants.OOXML_FILE_HEADER[0] && 
        	header[1] == POIFSConstants.OOXML_FILE_HEADER[1] && 
        	header[2] == POIFSConstants.OOXML_FILE_HEADER[2] && 
        	header[3] == POIFSConstants.OOXML_FILE_HEADER[3]
        );        	                                            
    }

	/**
	 * Get the document properties. This gives you access to the
	 *  core ooxml properties, and the extended ooxml properties.
	 */
	public POIXMLProperties getProperties() throws OpenXML4JException, IOException, XmlException {
		if(properties == null) {
			properties = new POIXMLProperties(pkg);
		}
		return properties;
	}
	
    /**
     * Get the document's embedded files.
     */
    public List<PackagePart> getAllEmbedds() throws OpenXML4JException
    {
        return embedds;
    }
}
