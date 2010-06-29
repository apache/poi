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
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.util.IOUtils;

public abstract class POIXMLDocument extends POIXMLDocumentPart{
    public static final String DOCUMENT_CREATOR = "Apache POI";

    // OLE embeddings relation name
    public static final String OLE_OBJECT_REL_TYPE="http://schemas.openxmlformats.org/officeDocument/2006/relationships/oleObject";

    // Embedded OPC documents relation name
    public static final String PACK_OBJECT_REL_TYPE="http://schemas.openxmlformats.org/officeDocument/2006/relationships/package";

    /** The OPC Package */
    private OPCPackage pkg;

    /**
     * The properties of the OPC package, opened as needed
     */
    private POIXMLProperties properties;

    protected POIXMLDocument(OPCPackage pkg) {
        super(pkg);
        this.pkg = pkg;
    }

    /**
     * Wrapper to open a package, returning an IOException
     *  in the event of a problem.
     * Works around shortcomings in java's this() constructor calls
     */
    public static OPCPackage openPackage(String path) throws IOException {
        try {
            return OPCPackage.open(path);
        } catch (InvalidFormatException e) {
            throw new IOException(e.toString());
        }
    }

    public OPCPackage getPackage() {
        return this.pkg;
    }

    protected PackagePart getCorePart() {
        return getPackagePart();
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
    protected static PackagePart getTargetPart(OPCPackage pkg, PackageRelationship rel) throws InvalidFormatException {
        PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
        PackagePart part = pkg.getPart(relName);
        if (part == null) {
            throw new IllegalArgumentException("No part found for relationship " + rel);
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
            getPackagePart().getRelationshipsByType(contentType);

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
    public POIXMLProperties getProperties() {
        if(properties == null) {
            try {
                properties = new POIXMLProperties(pkg);
            } catch (Exception e){
                throw new POIXMLException(e);
            }
        }
        return properties;
    }

    /**
     * Get the document's embedded files.
     */
    public abstract List<PackagePart> getAllEmbedds() throws OpenXML4JException;

    protected final void load(POIXMLFactory factory) throws IOException {
    	Map<PackagePart, POIXMLDocumentPart> context = new HashMap<PackagePart, POIXMLDocumentPart>();
        try {
            read(factory, context);
        } catch (OpenXML4JException e){
            throw new POIXMLException(e);
        }
    	onDocumentRead();
    	context.clear();
    }

    /**
     * Write out this document to an Outputstream.
     *
     * @param stream - the java OutputStream you wish to write the file to
     *
     * @exception IOException if anything can't be written.
     */
    public final void write(OutputStream stream) throws IOException {
        //force all children to commit their changes into the underlying OOXML Package
        Set<PackagePart> context = new HashSet<PackagePart>();
        onSave(context);
        context.clear();

        //save extended and custom properties
        getProperties().commit();

        getPackage().save(stream);
    }
}
