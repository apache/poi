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

package org.apache.poi.openxml4j.opc.internal.unmarshallers;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.XMLConstants;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageProperties;
import org.apache.poi.openxml4j.opc.ZipPackage;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.PartUnmarshaller;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Package properties unmarshaller.
 */
public final class PackagePropertiesUnmarshaller implements PartUnmarshaller {

    protected static final String KEYWORD_CATEGORY = "category";

    protected static final String KEYWORD_CONTENT_STATUS = "contentStatus";

    protected static final String KEYWORD_CONTENT_TYPE = "contentType";

    protected static final String KEYWORD_CREATED = "created";

    protected static final String KEYWORD_CREATOR = "creator";

    protected static final String KEYWORD_DESCRIPTION = "description";

    protected static final String KEYWORD_IDENTIFIER = "identifier";

    protected static final String KEYWORD_KEYWORDS = "keywords";

    protected static final String KEYWORD_LANGUAGE = "language";

    protected static final String KEYWORD_LAST_MODIFIED_BY = "lastModifiedBy";

    protected static final String KEYWORD_LAST_PRINTED = "lastPrinted";

    protected static final String KEYWORD_MODIFIED = "modified";

    protected static final String KEYWORD_REVISION = "revision";

    protected static final String KEYWORD_SUBJECT = "subject";

    protected static final String KEYWORD_TITLE = "title";

    protected static final String KEYWORD_VERSION = "version";

    // TODO Load element with XMLBeans or dynamic table
    // TODO Check every element/namespace for compliance
    public PackagePart unmarshall(UnmarshallContext context, InputStream in)
            throws InvalidFormatException, IOException {
        PackagePropertiesPart coreProps = new PackagePropertiesPart(context
                .getPackage(), context.getPartName());

        // If the input stream is null then we try to get it from the
        // package.
        if (in == null) {
            if (context.getZipEntry() != null) {
                in = ((ZipPackage) context.getPackage()).getZipArchive()
                        .getInputStream(context.getZipEntry());
            } else if (context.getPackage() != null) {
                // Try to retrieve the part inputstream from the URI
                ZipArchiveEntry zipEntry = ZipHelper
                        .getCorePropertiesZipEntry((ZipPackage) context
                                .getPackage());
                in = ((ZipPackage) context.getPackage()).getZipArchive()
                        .getInputStream(zipEntry);
            } else
                throw new IOException(
                        "Error while trying to get the part input stream.");
        }

        Document xmlDoc;
        try {
            xmlDoc = DocumentHelper.readDocument(in);

            /* Check OPC compliance */

            // Rule M4.2, M4.3, M4.4 and M4.5/
            checkElementForOPCCompliance(xmlDoc.getDocumentElement());

            /* End OPC compliance */

        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }

        coreProps.setCategoryProperty(loadCategory(xmlDoc));
        coreProps.setContentStatusProperty(loadContentStatus(xmlDoc));
        coreProps.setContentTypeProperty(loadContentType(xmlDoc));
        coreProps.setCreatedProperty(loadCreated(xmlDoc));
        coreProps.setCreatorProperty(loadCreator(xmlDoc));
        coreProps.setDescriptionProperty(loadDescription(xmlDoc));
        coreProps.setIdentifierProperty(loadIdentifier(xmlDoc));
        coreProps.setKeywordsProperty(loadKeywords(xmlDoc));
        coreProps.setLanguageProperty(loadLanguage(xmlDoc));
        coreProps.setLastModifiedByProperty(loadLastModifiedBy(xmlDoc));
        coreProps.setLastPrintedProperty(loadLastPrinted(xmlDoc));
        coreProps.setModifiedProperty(loadModified(xmlDoc));
        coreProps.setRevisionProperty(loadRevision(xmlDoc));
        coreProps.setSubjectProperty(loadSubject(xmlDoc));
        coreProps.setTitleProperty(loadTitle(xmlDoc));
        coreProps.setVersionProperty(loadVersion(xmlDoc));

        return coreProps;
    }

    private String readElement(Document xmlDoc, String localName, String namespaceURI) {
        Element el = (Element)xmlDoc.getDocumentElement().getElementsByTagNameNS(namespaceURI, localName).item(0);
        if (el == null) {
            return null;
        }
        return el.getTextContent();
    }

    private String loadCategory(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_CATEGORY, PackageNamespaces.CORE_PROPERTIES);
    }

    private String loadContentStatus(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_CONTENT_STATUS, PackageNamespaces.CORE_PROPERTIES);
    }

    private String loadContentType(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_CONTENT_TYPE, PackageNamespaces.CORE_PROPERTIES);
    }

    private String loadCreated(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_CREATED, PackageProperties.NAMESPACE_DCTERMS);
    }

    private String loadCreator(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_CREATOR, PackageProperties.NAMESPACE_DC);
    }

    private String loadDescription(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_DESCRIPTION, PackageProperties.NAMESPACE_DC);
    }

    private String loadIdentifier(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_IDENTIFIER, PackageProperties.NAMESPACE_DC);
    }

    private String loadKeywords(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_KEYWORDS, PackageNamespaces.CORE_PROPERTIES);
    }

    private String loadLanguage(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_LANGUAGE, PackageProperties.NAMESPACE_DC);
    }

    private String loadLastModifiedBy(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_LAST_MODIFIED_BY, PackageNamespaces.CORE_PROPERTIES);
    }

    private String loadLastPrinted(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_LAST_PRINTED, PackageNamespaces.CORE_PROPERTIES);
    }

    private String loadModified(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_MODIFIED, PackageProperties.NAMESPACE_DCTERMS);
    }

    private String loadRevision(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_REVISION, PackageNamespaces.CORE_PROPERTIES);
    }

    private String loadSubject(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_SUBJECT, PackageProperties.NAMESPACE_DC);
    }

    private String loadTitle(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_TITLE, PackageProperties.NAMESPACE_DC);
    }

    private String loadVersion(Document xmlDoc) {
        return readElement(xmlDoc, KEYWORD_VERSION, PackageNamespaces.CORE_PROPERTIES);
    }

    /* OPC Compliance methods */

    /**
     * Check the element for the following OPC compliance rules:
     * <p>
     * Rule M4.2: A format consumer shall consider the use of the Markup
     * Compatibility namespace to be an error.
     * <p>
     * Rule M4.3: Producers shall not create a document element that contains
     * refinements to the Dublin Core elements, except for the two specified in
     * the schema: &lt;dcterms:created&gt; and &lt;dcterms:modified&gt; Consumers shall
     * consider a document element that violates this constraint to be an error.
     * <p>
     * Rule M4.4: Producers shall not create a document element that contains
     * the xml:lang attribute. Consumers shall consider a document element that
     * violates this constraint to be an error.
     * <p>
     * Rule M4.5: Producers shall not create a document element that contains
     * the xsi:type attribute, except for a &lt;dcterms:created&gt; or
     * &lt;dcterms:modified&gt; element where the xsi:type attribute shall be present
     * and shall hold the value dcterms:W3CDTF, where dcterms is the namespace
     * prefix of the Dublin Core namespace. Consumers shall consider a document
     * element that violates this constraint to be an error.
     */
    public void checkElementForOPCCompliance(Element el)
            throws InvalidFormatException {
        // Check the current element
        NamedNodeMap namedNodeMap = el.getAttributes();
        int namedNodeCount = namedNodeMap.getLength();
        for (int i = 0; i < namedNodeCount; i++) {
            Attr attr = (Attr)namedNodeMap.item(0);

            if (attr != null && XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(attr.getNamespaceURI())) {
                // Rule M4.2
                if (PackageNamespaces.MARKUP_COMPATIBILITY.equals(attr.getValue())) {
                    throw new InvalidFormatException(
                            "OPC Compliance error [M4.2]: A format consumer shall consider the use of the Markup Compatibility namespace to be an error.");
                }
            }
        }

        // Rule M4.3
        String elName = el.getLocalName();
        if (PackageProperties.NAMESPACE_DCTERMS.equals(el.getNamespaceURI())) {
            if (!(KEYWORD_CREATED.equals(elName) || KEYWORD_MODIFIED.equals(elName))) {
                throw new InvalidFormatException(
                        "OPC Compliance error [M4.3]: Producers shall not create a document element that contains refinements to the Dublin Core elements, except for the two specified in the schema: <dcterms:created> and <dcterms:modified> Consumers shall consider a document element that violates this constraint to be an error.");
            }
        }

        // Rule M4.4
        if (el.getAttributeNodeNS(XMLConstants.XML_NS_URI, "lang") != null) {
            throw new InvalidFormatException(
                    "OPC Compliance error [M4.4]: Producers shall not create a document element that contains the xml:lang attribute. Consumers shall consider a document element that violates this constraint to be an error.");
        }

        // Rule M4.5
        if (PackageProperties.NAMESPACE_DCTERMS.equals(el.getNamespaceURI())) {
            // DCTerms namespace only use with 'created' and 'modified' elements
            if (!(elName.equals(KEYWORD_CREATED) || elName.equals(KEYWORD_MODIFIED))) {
                throw new InvalidFormatException("Namespace error : " + elName
                        + " shouldn't have the following namespace -> "
                        + PackageProperties.NAMESPACE_DCTERMS);
            }

            // Check for the 'xsi:type' attribute
            Attr typeAtt = el.getAttributeNodeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type");
            if (typeAtt == null) {
                throw new InvalidFormatException("The element '" + elName
                        + "' must have the 'xsi:type' attribute present !");
            }

            // Check for the attribute value => 'dcterms:W3CDTF'
            if (!typeAtt.getValue().equals(el.getPrefix() + ":W3CDTF")) {
                throw new InvalidFormatException("The element '" + elName
                        + "' must have the 'xsi:type' attribute with the value '" + el.getPrefix() + ":W3CDTF', but had '"
                        + typeAtt.getValue() + "' !");
            }
        }

        // Check its children
        NodeList childElements = el.getElementsByTagName("*");
        int childElementCount = childElements.getLength();
        for (int i = 0; i < childElementCount; i++)
            checkElementForOPCCompliance((Element)childElements.item(i));
    }
}
