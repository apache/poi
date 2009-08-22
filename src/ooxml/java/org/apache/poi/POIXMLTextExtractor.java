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

import org.apache.poi.POIXMLProperties.CoreProperties;
import org.apache.poi.POIXMLProperties.CustomProperties;
import org.apache.poi.POIXMLProperties.ExtendedProperties;

public abstract class POIXMLTextExtractor extends POITextExtractor {
	/** The POIXMLDocument that's open */
	private final POIXMLDocument _document;

	/**
	 * Creates a new text extractor for the given document
	 */
	public POIXMLTextExtractor(POIXMLDocument document) {
		super((POIDocument)null);

		_document = document;
	}

	/**
	 * Returns the core document properties
	 */
	public CoreProperties getCoreProperties() {
		 return _document.getProperties().getCoreProperties();
	}
	/**
	 * Returns the extended document properties
	 */
	public ExtendedProperties getExtendedProperties() {
		return _document.getProperties().getExtendedProperties();
	}
	/**
	 * Returns the custom document properties
	 */
	public CustomProperties getCustomProperties() {
		return _document.getProperties().getCustomProperties();
	}

	/**
	 * Returns opened document
	 */
	public final POIXMLDocument getDocument(){
		return _document;
	}


	/**
	 * Returns an OOXML properties text extractor for the
	 *  document properties metadata, such as title and author.
	 */
	public POIXMLPropertiesTextExtractor getMetadataTextExtractor() {
		return new POIXMLPropertiesTextExtractor(_document);
	}
}
