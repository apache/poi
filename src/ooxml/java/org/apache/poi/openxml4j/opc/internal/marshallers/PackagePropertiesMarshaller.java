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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.apache.poi.openxml4j.opc.internal.PartMarshaller;

/**
 * Package properties marshaller.
 *
 * @author CDubet, Julien Chable
 */
public class PackagePropertiesMarshaller implements PartMarshaller {

	private final static Namespace namespaceDC = new Namespace("dc",
			PackagePropertiesPart.NAMESPACE_DC_URI);

	private final static Namespace namespaceCoreProperties = new Namespace("",
			PackagePropertiesPart.NAMESPACE_CP_URI);

	private final static Namespace namespaceDcTerms = new Namespace("dcterms",
			PackagePropertiesPart.NAMESPACE_DCTERMS_URI);

	private final static Namespace namespaceXSI = new Namespace("xsi",
			PackagePropertiesPart.NAMESPACE_XSI_URI);

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
	Document xmlDoc = null;

	/**
	 * Marshall package core properties to an XML document. Always return
	 * <code>true</code>.
	 */
	public boolean marshall(PackagePart part, OutputStream out)
			throws OpenXML4JException {
		if (!(part instanceof PackagePropertiesPart))
			throw new IllegalArgumentException(
					"'part' must be a PackagePropertiesPart instance.");
		propsPart = (PackagePropertiesPart) part;

		// Configure the document
		xmlDoc = DocumentHelper.createDocument();
		Element rootElem = xmlDoc.addElement(new QName("coreProperties",
				namespaceCoreProperties));
		rootElem.addNamespace("cp", PackagePropertiesPart.NAMESPACE_CP_URI);
		rootElem.addNamespace("dc", PackagePropertiesPart.NAMESPACE_DC_URI);
		rootElem.addNamespace("dcterms",
				PackagePropertiesPart.NAMESPACE_DCTERMS_URI);
		rootElem.addNamespace("xsi", PackagePropertiesPart.NAMESPACE_XSI_URI);

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
	 * Add category property element if needed.
	 */
	private void addCategory() {
		if (!propsPart.getCategoryProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_CATEGORY, namespaceCoreProperties));
		if (elem == null) {
			// Missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_CATEGORY, namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getCategoryProperty().getValue());
	}

	/**
	 * Add content status property element if needed.
	 */
	private void addContentStatus() {
		if (!propsPart.getContentStatusProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_CONTENT_STATUS, namespaceCoreProperties));
		if (elem == null) {
			// Missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_CONTENT_STATUS, namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getContentStatusProperty().getValue());
	}

	/**
	 * Add content type property element if needed.
	 */
	private void addContentType() {
		if (!propsPart.getContentTypeProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_CONTENT_TYPE, namespaceCoreProperties));
		if (elem == null) {
			// Missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_CONTENT_TYPE, namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getContentTypeProperty().getValue());
	}

	/**
	 * Add created property element if needed.
	 */
	private void addCreated() {
		if (!propsPart.getCreatedProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_CREATED, namespaceDcTerms));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_CREATED, namespaceDcTerms));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addAttribute(new QName("type", namespaceXSI), "dcterms:W3CDTF");
		elem.addText(propsPart.getCreatedPropertyString());
	}

	/**
	 * Add creator property element if needed.
	 */
	private void addCreator() {
		if (!propsPart.getCreatorProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_CREATOR, namespaceDC));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_CREATOR, namespaceDC));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getCreatorProperty().getValue());
	}

	/**
	 * Add description property element if needed.
	 */
	private void addDescription() {
		if (!propsPart.getDescriptionProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_DESCRIPTION, namespaceDC));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_DESCRIPTION, namespaceDC));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getDescriptionProperty().getValue());
	}

	/**
	 * Add identifier property element if needed.
	 */
	private void addIdentifier() {
		if (!propsPart.getIdentifierProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_IDENTIFIER, namespaceDC));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_IDENTIFIER, namespaceDC));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getIdentifierProperty().getValue());
	}

	/**
	 * Add keywords property element if needed.
	 */
	private void addKeywords() {
		if (!propsPart.getKeywordsProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_KEYWORDS, namespaceCoreProperties));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_KEYWORDS, namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getKeywordsProperty().getValue());
	}

	/**
	 * Add language property element if needed.
	 */
	private void addLanguage() {
		if (!propsPart.getLanguageProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_LANGUAGE, namespaceDC));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_LANGUAGE, namespaceDC));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getLanguageProperty().getValue());
	}

	/**
	 * Add 'last modified by' property if needed.
	 */
	private void addLastModifiedBy() {
		if (!propsPart.getLastModifiedByProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_LAST_MODIFIED_BY, namespaceCoreProperties));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement()
					.addElement(
							new QName(KEYWORD_LAST_MODIFIED_BY,
									namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getLastModifiedByProperty().getValue());
	}

	/**
	 * Add 'last printed' property if needed.
	 *
	 */
	private void addLastPrinted() {
		if (!propsPart.getLastPrintedProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_LAST_PRINTED, namespaceCoreProperties));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_LAST_PRINTED, namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getLastPrintedPropertyString());
	}

	/**
	 * Add modified property element if needed.
	 */
	private void addModified() {
		if (!propsPart.getModifiedProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_MODIFIED, namespaceDcTerms));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_MODIFIED, namespaceDcTerms));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addAttribute(new QName("type", namespaceXSI), "dcterms:W3CDTF");
		elem.addText(propsPart.getModifiedPropertyString());
	}

	/**
	 * Add revision property if needed.
	 */
	private void addRevision() {
		if (!propsPart.getRevisionProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_REVISION, namespaceCoreProperties));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_REVISION, namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getRevisionProperty().getValue());
	}

	/**
	 * Add subject property if needed.
	 */
	private void addSubject() {
		if (!propsPart.getSubjectProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_SUBJECT, namespaceDC));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_SUBJECT, namespaceDC));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getSubjectProperty().getValue());
	}

	/**
	 * Add title property if needed.
	 */
	private void addTitle() {
		if (!propsPart.getTitleProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_TITLE, namespaceDC));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_TITLE, namespaceDC));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getTitleProperty().getValue());
	}

	private void addVersion() {
		if (!propsPart.getVersionProperty().hasValue())
			return;

		Element elem = xmlDoc.getRootElement().element(
				new QName(KEYWORD_VERSION, namespaceCoreProperties));
		if (elem == null) {
			// missing, we add it
			elem = xmlDoc.getRootElement().addElement(
					new QName(KEYWORD_VERSION, namespaceCoreProperties));
		} else {
			elem.clearContent();// clear the old value
		}
		elem.addText(propsPart.getVersionProperty().getValue());
	}
}
