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
package org.apache.poi.ooxml;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.xmlbeans.impl.common.SystemCache;

/**
 * This holds the common functionality for all POI OOXML Document classes.
 */
public abstract class POIXMLDocument extends POIXMLDocumentPart implements Closeable {
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
        init(pkg);
    }

    protected POIXMLDocument(OPCPackage pkg, String coreDocumentRel) {
        super(pkg, coreDocumentRel);
        init(pkg);
    }

    private void init(OPCPackage p) {
        this.pkg = p;

        // Workaround for XMLBEANS-512 - ensure that when we parse
        //  the file, we start with a fresh XML Parser each time,
        //  and avoid the risk of getting a SaxHandler that's in error
        SystemCache.get().setSaxLoader(null);
    }

    /**
     * Wrapper to open a package, which works around shortcomings in java's this() constructor calls
     *
     * @param path the path to the document
     * @return the new OPCPackage
     *
     * @exception IOException if there was a problem opening the document
     */
    public static OPCPackage openPackage(String path) throws IOException {
        try {
            return OPCPackage.open(path);
        } catch (InvalidFormatException e) {
            throw new IOException(e.toString(), e);
        }
    }

    /**
     * Get the assigned OPCPackage
     *
     * @return the assigned OPCPackage
     */
    public OPCPackage getPackage() {
        return this.pkg;
    }

    protected PackagePart getCorePart() {
        return getPackagePart();
    }

    /**
     * Retrieves all the PackageParts which are defined as relationships of the base document with the
     * specified content type.
     *
     * @param contentType the content type
     *
     * @return all the base document PackageParts which match the content type
     *
     * @throws InvalidFormatException when the relationships or the parts contain errors
     *
     * @see org.apache.poi.xssf.usermodel.XSSFRelation
     * @see org.apache.poi.xslf.usermodel.XSLFRelation
     * @see org.apache.poi.xwpf.usermodel.XWPFRelation
     * @see org.apache.poi.xdgf.usermodel.XDGFRelation
     */
    protected PackagePart[] getRelatedByType(String contentType) throws InvalidFormatException {
        PackageRelationshipCollection partsC =
            getPackagePart().getRelationshipsByType(contentType);

        PackagePart[] parts = new PackagePart[partsC.size()];
        int count = 0;
        for (PackageRelationship rel : partsC) {
            parts[count] = getPackagePart().getRelatedPart(rel);
            count++;
        }
        return parts;
    }

    /**
     * Get the document properties. This gives you access to the
     *  core ooxml properties, and the extended ooxml properties.
     *
     * @return the document properties
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
     *
     * @return the document's embedded files
     *
     * @throws OpenXML4JException if the embedded parts can't be determined
     * @since POI 4.0.0
     */
    public abstract List<PackagePart> getAllEmbeddedParts() throws OpenXML4JException;

    protected final void load(POIXMLFactory factory) throws IOException {
        Map<PackagePart, POIXMLDocumentPart> context = new HashMap<>();
        try {
            read(factory, context);
        } catch (OpenXML4JException e){
            throw new POIXMLException(e);
        }
        onDocumentRead();
        context.clear();
    }

    /**
     * Closes the underlying {@link OPCPackage} from which this
     *  document was read, if there is one
     *
     * <p>Once this has been called, no further
     *  operations, updates or reads should be performed on the
     *  document.
     *
     * @throws IOException for writable packages, if an IO exception occur during the saving process.
     */
    @Override
    public void close() throws IOException {
        if (pkg != null) {
            if (pkg.getPackageAccess() == PackageAccess.READ) {
                pkg.revert();
            } else {
                pkg.close();
            }
            pkg = null;
        }
    }

    /**
     * Write out this document to an {@link OutputStream}.
     *
     * Note - if the Document was opened from a {@link File} rather
     *  than an {@link InputStream}, you <b>must</b> write out to
     *  a different file, overwriting via an OutputStream isn't possible.
     *
     * If {@code stream} is a {@link java.io.FileOutputStream} on a networked drive
     * or has a high cost/latency associated with each written byte,
     * consider wrapping the OutputStream in a {@link java.io.BufferedOutputStream}
     * to improve write performance.
     *
     * @param stream - the java OutputStream you wish to write the file to
     *
     * @exception IOException if anything can't be written.
     */
    @SuppressWarnings("resource")
    public final void write(OutputStream stream) throws IOException {
        OPCPackage p = getPackage();
        if(p == null) {
            throw new IOException("Cannot write data, document seems to have been closed already");
        }

        //force all children to commit their changes into the underlying OOXML Package
        // TODO Shouldn't they be committing to the new one instead?
        Set<PackagePart> context = new HashSet<>();
        onSave(context);
        context.clear();

        //save extended and custom properties
        getProperties().commit();

        p.save(stream);
    }
}
