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

package org.apache.poi.openxml4j.opc.internal.marshallers;

import java.io.OutputStream;
import java.util.Optional;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Namespace;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.PartMarshaller;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Package properties marshaller.
 */
public class PackagePropertiesMarshaller implements PartMarshaller {
    private final static Namespace namespaceDC, namespaceCoreProperties, namespaceDcTerms, namespaceXSI;
	static {
	    final XMLEventFactory f = XMLEventFactory.newInstance();
	    namespaceDC = f.createNamespace("dc", PackagePropertiesPart.NAMESPACE_DC_URI);
	    namespaceCoreProperties = f.createNamespace("cp", PackagePropertiesPart.NAMESPACE_CP_URI);
	    namespaceDcTerms = f.createNamespace("dcterms", PackagePropertiesPart.NAMESPACE_DCTERMS_URI);
	    namespaceXSI = f.createNamespace("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
	}

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

	PackagePropertiesPart propsPart;

	// The document
	Document xmlDoc;

	/**
	 * Marshall package core properties to an XML document. Always return
	 * <code>true</code>.
	 */
	@Override
	public boolean marshall(PackagePart part, OutputStream out)
			throws OpenXML4JException {
		if (!(part instanceof PackagePropertiesPart))
			throw new IllegalArgumentException(
					"'part' must be a PackagePropertiesPart instance.");
		propsPart = (PackagePropertiesPart) part;

		// Configure the document
		xmlDoc = DocumentHelper.createDocument();
        Element rootElem = xmlDoc.createElementNS(namespaceCoreProperties.getNamespaceURI(),
                getQName("coreProperties", namespaceCoreProperties));
        DocumentHelper.addNamespaceDeclaration(rootElem, namespaceCoreProperties);
        DocumentHelper.addNamespaceDeclaration(rootElem, namespaceDC);
        DocumentHelper.addNamespaceDeclaration(rootElem, namespaceDcTerms);
        DocumentHelper.addNamespaceDeclaration(rootElem, namespaceXSI);
        xmlDoc.appendChild(rootElem);

		addCategory();
		addContentStatus();
		addContentType();
		addCreated();
		addCreator();
		addDescription();
		addIdentifier();
		addKeywords();
		addLanguage();
		addLastModifiedBy();
		addLastPrinted();
		addModified();
		addRevision();
		addSubject();
		addTitle();
		addVersion();
		return true;
	}

    /**
     * Sets the given element's text content, creating it if necessary.
     */
    private Element setElementTextContent(String localName, Namespace namespace, Optional<String> property) {
        return setElementTextContent(localName, namespace, property, property.orElse(null));
    }
    
    private String getQName(String localName, Namespace namespace) {
        return namespace.getPrefix().isEmpty() ? localName : namespace.getPrefix() + ':' + localName;
    }

    private Element setElementTextContent(String localName, Namespace namespace, Optional<?> property, String propertyValue) {
        if (!property.isPresent())
            return null;

        Element root = xmlDoc.getDocumentElement();
        Element elem = (Element) root.getElementsByTagNameNS(namespace.getNamespaceURI(), localName).item(0);
        if (elem == null) {
            // missing, we add it
            elem = xmlDoc.createElementNS(namespace.getNamespaceURI(), getQName(localName, namespace));
            root.appendChild(elem);
        }
        elem.setTextContent(propertyValue);
        return elem;
    }

    private Element setElementTextContent(String localName, Namespace namespace, Optional<?> property, String propertyValue, String xsiType) {
        Element element = setElementTextContent(localName, namespace, property, propertyValue);
        if (element != null) {
            element.setAttributeNS(namespaceXSI.getNamespaceURI(), getQName("type", namespaceXSI), xsiType);
        }
        return element;
    }


    /**
	 * Add category property element if needed.
	 */
	private void addCategory() {
        setElementTextContent(KEYWORD_CATEGORY, namespaceCoreProperties, propsPart.getCategoryProperty());
	}

	/**
	 * Add content status property element if needed.
	 */
	private void addContentStatus() {
        setElementTextContent(KEYWORD_CONTENT_STATUS, namespaceCoreProperties, propsPart.getContentStatusProperty());
	}

	/**
	 * Add content type property element if needed.
	 */
	private void addContentType() {
        setElementTextContent(KEYWORD_CONTENT_TYPE, namespaceCoreProperties, propsPart.getContentTypeProperty());
	}

	/**
	 * Add created property element if needed.
	 */
	private void addCreated() {
        setElementTextContent(KEYWORD_CREATED, namespaceDcTerms, propsPart.getCreatedProperty(),
                propsPart.getCreatedPropertyString(), "dcterms:W3CDTF");
	}

	/**
	 * Add creator property element if needed.
	 */
	private void addCreator() {
        setElementTextContent(KEYWORD_CREATOR, namespaceDC, propsPart.getCreatorProperty());
	}

	/**
	 * Add description property element if needed.
	 */
	private void addDescription() {
        setElementTextContent(KEYWORD_DESCRIPTION, namespaceDC, propsPart.getDescriptionProperty());
	}

	/**
	 * Add identifier property element if needed.
	 */
	private void addIdentifier() {
        setElementTextContent(KEYWORD_IDENTIFIER, namespaceDC, propsPart.getIdentifierProperty());
	}

    /**
	 * Add keywords property element if needed.
	 */
	private void addKeywords() {
        setElementTextContent(KEYWORD_KEYWORDS, namespaceCoreProperties, propsPart.getKeywordsProperty());
	}

	/**
	 * Add language property element if needed.
	 */
	private void addLanguage() {
        setElementTextContent(KEYWORD_LANGUAGE, namespaceDC, propsPart.getLanguageProperty());
	}

	/**
	 * Add 'last modified by' property if needed.
	 */
	private void addLastModifiedBy() {
        setElementTextContent(KEYWORD_LAST_MODIFIED_BY, namespaceCoreProperties, propsPart.getLastModifiedByProperty());
	}

	/**
	 * Add 'last printed' property if needed.
	 *
	 */
	private void addLastPrinted() {
        setElementTextContent(KEYWORD_LAST_PRINTED, namespaceCoreProperties, propsPart.getLastPrintedProperty(), propsPart.getLastPrintedPropertyString());
	}

	/**
	 * Add modified property element if needed.
	 */
	private void addModified() {
        setElementTextContent(KEYWORD_MODIFIED, namespaceDcTerms, propsPart.getModifiedProperty(),
                propsPart.getModifiedPropertyString(), "dcterms:W3CDTF");
    }

	/**
	 * Add revision property if needed.
	 */
	private void addRevision() {
        setElementTextContent(KEYWORD_REVISION, namespaceCoreProperties, propsPart.getRevisionProperty());
	}

	/**
	 * Add subject property if needed.
	 */
	private void addSubject() {
        setElementTextContent(KEYWORD_SUBJECT, namespaceDC, propsPart.getSubjectProperty());
	}

	/**
	 * Add title property if needed.
	 */
	private void addTitle() {
        setElementTextContent(KEYWORD_TITLE, namespaceDC, propsPart.getTitleProperty());
	}

	private void addVersion() {
        setElementTextContent(KEYWORD_VERSION, namespaceCoreProperties, propsPart.getVersionProperty());
	}
}
