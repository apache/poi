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

import java.util.Date;
import java.util.List;

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
	
   private void appendIfPresent(StringBuffer text, String thing, boolean value) {
      appendIfPresent(text, thing, Boolean.toString(value));
   }
   private void appendIfPresent(StringBuffer text, String thing, int value) {
      appendIfPresent(text, thing, Integer.toString(value));
   }
   private void appendIfPresent(StringBuffer text, String thing, Date value) {
      if(value == null) { return; }
      appendIfPresent(text, thing, value.toString());
   }
	private void appendIfPresent(StringBuffer text, String thing, String value) {
	   if(value == null) { return; }
	   text.append(thing);
	   text.append(" = ");
	   text.append(value);
	   text.append("\n");
	}

	/**
	 * Returns the core document properties, eg author
	 */
	public String getCorePropertiesText() {
		StringBuffer text = new StringBuffer();
		PackagePropertiesPart props =
			getDocument().getProperties().getCoreProperties().getUnderlyingProperties();

		appendIfPresent(text, "Category", props.getCategoryProperty().getValue());
		appendIfPresent(text, "Category", props.getCategoryProperty().getValue());
		appendIfPresent(text, "ContentStatus", props.getContentStatusProperty().getValue());
		appendIfPresent(text, "ContentType", props.getContentTypeProperty().getValue());
		appendIfPresent(text, "Created", props.getCreatedProperty().getValue());
		appendIfPresent(text, "CreatedString", props.getCreatedPropertyString());
		appendIfPresent(text, "Creator", props.getCreatorProperty().getValue());
		appendIfPresent(text, "Description", props.getDescriptionProperty().getValue());
		appendIfPresent(text, "Identifier", props.getIdentifierProperty().getValue());
		appendIfPresent(text, "Keywords", props.getKeywordsProperty().getValue());
		appendIfPresent(text, "Language", props.getLanguageProperty().getValue());
		appendIfPresent(text, "LastModifiedBy", props.getLastModifiedByProperty().getValue());
		appendIfPresent(text, "LastPrinted", props.getLastPrintedProperty().getValue());
		appendIfPresent(text, "LastPrintedString", props.getLastPrintedPropertyString());
		appendIfPresent(text, "Modified", props.getModifiedProperty().getValue());
		appendIfPresent(text, "ModifiedString", props.getModifiedPropertyString());
		appendIfPresent(text, "Revision", props.getRevisionProperty().getValue());
		appendIfPresent(text, "Subject", props.getSubjectProperty().getValue());
		appendIfPresent(text, "Title", props.getTitleProperty().getValue());
		appendIfPresent(text, "Version", props.getVersionProperty().getValue());

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

		appendIfPresent(text, "Application", props.getApplication());
		appendIfPresent(text, "AppVersion", props.getAppVersion());
		appendIfPresent(text, "Characters", props.getCharacters());
		appendIfPresent(text, "CharactersWithSpaces", props.getCharactersWithSpaces());
		appendIfPresent(text, "Company", props.getCompany());
		appendIfPresent(text, "HyperlinkBase", props.getHyperlinkBase());
		appendIfPresent(text, "HyperlinksChanged", props.getHyperlinksChanged());
		appendIfPresent(text, "Lines", props.getLines());
		appendIfPresent(text, "LinksUpToDate", props.getLinksUpToDate());
		appendIfPresent(text, "Manager", props.getManager());
		appendIfPresent(text, "Pages", props.getPages());
		appendIfPresent(text, "Paragraphs", props.getParagraphs());
		appendIfPresent(text, "PresentationFormat", props.getPresentationFormat());
		appendIfPresent(text, "Template", props.getTemplate());
		appendIfPresent(text, "TotalTime", props.getTotalTime());

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

		List<CTProperty> properties = props.getPropertyList();
		for(int i = 0; i<properties.size(); i++) {
			// TODO - finish off
			String val = "(not implemented!)";

			text.append(
					properties.get(i).getName() +
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
