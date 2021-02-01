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

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.xmlbeans.XmlException;

/**
 * Represents a descriptor of a OOXML relation.
 */
public abstract class POIXMLRelation {

    @Internal
    public interface NoArgConstructor {
        POIXMLDocumentPart init();
    }

    @Internal
    public interface PackagePartConstructor {
        POIXMLDocumentPart init(PackagePart part) throws IOException, XmlException;
    }

    @Internal
    public interface ParentPartConstructor {
        POIXMLDocumentPart init(POIXMLDocumentPart parent, PackagePart part) throws IOException, XmlException;
    }

    private static final POILogger log = POILogFactory.getLogger(POIXMLRelation.class);

    /**
     * Describes the content stored in a part.
     */
    private final String _type;

    /**
     * The kind of connection between a source part and a target part in a package.
     */
    private final String _relation;

    /**
     * The path component of a pack URI.
     */
    private final String _defaultName;

    /**
     * Constructors or factory method to construct instances of this relationship
     */
    private final NoArgConstructor noArgConstructor;
    private final PackagePartConstructor packagePartConstructor;
    private final ParentPartConstructor parentPartConstructor;

    /**
     * Instantiates a POIXMLRelation.
     *
     * @param type content type
     * @param rel  relationship
     * @param defaultName default item name
     * @param noArgConstructor method used to construct instances of this relationship from scratch
     * @param packagePartConstructor method used to construct instances of this relationship with a package part
     */
    protected POIXMLRelation(String type, String rel, String defaultName,
                             NoArgConstructor noArgConstructor,
                             PackagePartConstructor packagePartConstructor,
                             ParentPartConstructor parentPartConstructor) {
        _type = type;
        _relation = rel;
        _defaultName = defaultName;
        this.noArgConstructor = noArgConstructor;
        this.packagePartConstructor = packagePartConstructor;
        this.parentPartConstructor = parentPartConstructor;
    }

    /**
     * Instantiates a POIXMLRelation.
     *
     * @param type content type
     * @param rel  relationship
     * @param defaultName default item name
     */
    protected POIXMLRelation(String type, String rel, String defaultName) {
        this(type, rel, defaultName, null, null, null);
    }
    /**
     * Return the content type. Content types define a media type, a subtype, and an
     * optional set of parameters, as defined in RFC 2616.
     *
     * @return the content type
     */
    public String getContentType() {
        return _type;
    }

    /**
     * Return the relationship, the kind of connection between a source part and a target part in a package.
     * Relationships make the connections between parts directly discoverable without looking at the content
     * in the parts, and without altering the parts themselves.
     *
     * @return the relationship
     */
    public String getRelation() {
        return _relation;
    }

    /**
     * Return the default part name. Part names are used to refer to a part in the context of a
     * package, typically as part of a URI.
     *
     * @return the default part name
     */
    public String getDefaultFileName() {
        return _defaultName;
    }

    /**
     * Returns the filename for the nth one of these, e.g. /xl/comments4.xml
     *
     * @param index the suffix for the document type
     * @return the filename including the suffix
     */
    public String getFileName(int index) {
        if(! _defaultName.contains("#")) {
            // Generic filename in all cases
            return getDefaultFileName();
        }
        return _defaultName.replace("#", Integer.toString(index));
    }

    /**
     * Returns the index of the filename within the package for the given part.
     *  e.g. 4 for /xl/comments4.xml
     *
     * @param part the part to read the suffix from
     * @return the suffix
     */
    public Integer getFileNameIndex(POIXMLDocumentPart part) {
        String regex = _defaultName.replace("#", "(\\d+)");
        return Integer.valueOf(part.getPackagePart().getPartName().getName().replaceAll(regex, "$1"));
    }

    /**
     * @return the constructor method used to construct instances of this relationship from scratch
     *
     *  @since 4.1.2
     */
    public NoArgConstructor getNoArgConstructor() {
        return noArgConstructor;
    }

    /**
     * @return the constructor method used to construct instances of this relationship with a package part
     *
     *  @since 4.1.2
     */
    public PackagePartConstructor getPackagePartConstructor() {
        return packagePartConstructor;
    }

    /**
     * @return the constructor method used to construct instances of this relationship with a package part
     *
     *  @since 4.1.2
     */
    public ParentPartConstructor getParentPartConstructor() {
        return parentPartConstructor;
    }

    /**
     *  Fetches the InputStream to read the contents, based
     *  of the specified core part, for which we are defined
     *  as a suitable relationship
     *
     *  @since 3.16-beta3
     */
    public InputStream getContents(PackagePart corePart) throws IOException, InvalidFormatException {
        PackageRelationshipCollection prc =
                corePart.getRelationshipsByType(getRelation());
        Iterator<PackageRelationship> it = prc.iterator();
        if(it.hasNext()) {
            PackageRelationship rel = it.next();
            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
            PackagePart part = corePart.getPackage().getPart(relName);
            return part.getInputStream();
        }
        log.log(POILogger.WARN, "No part " + getDefaultFileName() + " found");
        return null;
    }
}
