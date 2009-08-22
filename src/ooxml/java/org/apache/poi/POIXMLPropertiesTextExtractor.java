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

import org.apache.poi.openxml4j.opc.internal.PackagePropertiesPart;
import org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperty;

/**
 * A {@link POITextExtractor} for returning the textual
 *  content of the OOXML file properties, eg author
 *  and title.
 */
public class POIXMLPropertiesTextExtractor extends POIXMLTextExtractor {
	/**
	 * Creates a new POIXMLPropertiesTextExtractor for the
	 *  given open document.
	 */
	public POIXMLPropertiesTextExtractor(POIXMLDocument doc) {
		super(doc);
	}
	/**
	 * Creates a new POIXMLPropertiesTextExtractor, for the
	 *  same file that another TextExtractor is already
	 *  working on.
	 */
	public POIXMLPropertiesTextExtractor(POIXMLTextExtractor otherExtractor) {
		super(otherExtractor.getDocument());
	}

	/**
	 * Returns the core document properties, eg author
	 */
	public String getCorePropertiesText() {
		StringBuffer text = new StringBuffer();
		PackagePropertiesPart props =
			getDocument().getProperties().getCoreProperties().getUnderlyingProperties();

		text.append("Category = " + props.getCategoryProperty().getValue() + "\n");
		text.append("ContentStatus = " + props.getContentStatusProperty().getValue() + "\n");
		text.append("ContentType = " + props.getContentTypeProperty().getValue() + "\n");
		text.append("Created = " + props.getCreatedProperty().getValue() + "\n");
		text.append("CreatedString = " + props.getCreatedPropertyString() + "\n");
		text.append("Creator = " + props.getCreatorProperty().getValue() + "\n");
		text.append("Description = " + props.getDescriptionProperty().getValue() + "\n");
		text.append("Identifier = " + props.getIdentifierProperty().getValue() + "\n");
		text.append("Keywords = " + props.getKeywordsProperty().getValue() + "\n");
		text.append("Language = " + props.getLanguageProperty().getValue() + "\n");
		text.append("LastModifiedBy = " + props.getLastModifiedByProperty().getValue() + "\n");
		text.append("LastPrinted = " + props.getLastPrintedProperty().getValue() + "\n");
		text.append("LastPrintedString = " + props.getLastPrintedPropertyString() + "\n");
		text.append("Modified = " + props.getModifiedProperty().getValue() + "\n");
		text.append("ModifiedString = " + props.getModifiedPropertyString() + "\n");
		text.append("Revision = " + props.getRevisionProperty().getValue() + "\n");
		text.append("Subject = " + props.getSubjectProperty().getValue() + "\n");
		text.append("Title = " + props.getTitleProperty().getValue() + "\n");
		text.append("Version = " + props.getVersionProperty().getValue() + "\n");

		return text.toString();
	}
	/**
	 * Returns the extended document properties, eg
	 *  application
	 */
	public String getExtendedPropertiesText() {
		StringBuffer text = new StringBuffer();
		org.openxmlformats.schemas.officeDocument.x2006.extendedProperties.CTProperties
			props = getDocument().getProperties().getExtendedProperties().getUnderlyingProperties();

		text.append("Application = " + props.getApplication() + "\n");
		text.append("AppVersion = " + props.getAppVersion() + "\n");
		text.append("Characters = " + props.getCharacters() + "\n");
		text.append("CharactersWithSpaces = " + props.getCharactersWithSpaces() + "\n");
		text.append("Company = " + props.getCompany() + "\n");
		text.append("HyperlinkBase = " + props.getHyperlinkBase() + "\n");
		text.append("HyperlinksChanged = " + props.getHyperlinksChanged() + "\n");
		text.append("Lines = " + props.getLines() + "\n");
		text.append("LinksUpToDate = " + props.getLinksUpToDate() + "\n");
		text.append("Manager = " + props.getManager() + "\n");
		text.append("Pages = " + props.getPages() + "\n");
		text.append("Paragraphs = " + props.getParagraphs() + "\n");
		text.append("PresentationFormat = " + props.getPresentationFormat() + "\n");
		text.append("Template = " + props.getTemplate() + "\n");
		text.append("TotalTime = " + props.getTotalTime() + "\n");

		return text.toString();
	}
	/**
	 * Returns the custom document properties, if
	 *  there are any
	 */
	public String getCustomPropertiesText() {
		StringBuffer text = new StringBuffer();
		org.openxmlformats.schemas.officeDocument.x2006.customProperties.CTProperties
			props = getDocument().getProperties().getCustomProperties().getUnderlyingProperties();

		CTProperty[] properties = props.getPropertyArray();
		for(int i = 0; i<properties.length; i++) {
			// TODO - finish off
			String val = "(not implemented!)";

			text.append(
					properties[i].getName() +
					" = " + val + "\n"
			);
		}

		return text.toString();
	}

	public String getText() {
		try {
			return
				getCorePropertiesText() +
				getExtendedPropertiesText() +
				getCustomPropertiesText();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	public POIXMLPropertiesTextExtractor getMetadataTextExtractor() {
		throw new IllegalStateException("You already have the Metadata Text Extractor, not recursing!");
	}
}
