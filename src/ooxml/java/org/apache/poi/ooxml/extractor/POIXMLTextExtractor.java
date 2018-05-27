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

package org.apache.poi.ooxml.extractor;

import java.io.IOException;

import org.apache.poi.extractor.POITextExtractor;
import org.apache.poi.ooxml.POIXMLDocument;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.ooxml.POIXMLProperties.CustomProperties;
import org.apache.poi.ooxml.POIXMLProperties.ExtendedProperties;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.util.ZipSecureFile;

public abstract class POIXMLTextExtractor extends POITextExtractor {
	/** The POIXMLDocument that's open */
	private final POIXMLDocument _document;

	/**
	 * Creates a new text extractor for the given document
	 * 
	 * @param document the document to extract from
	 */
	public POIXMLTextExtractor(POIXMLDocument document) {
		_document = document;
	}

	/**
	 * Returns the core document properties
	 * 
	 * @return the core document properties
	 */
	public CoreProperties getCoreProperties() {
		 return _document.getProperties().getCoreProperties();
	}
	/**
	 * Returns the extended document properties
	 * 
	 * @return the extended document properties
	 */
	public ExtendedProperties getExtendedProperties() {
		return _document.getProperties().getExtendedProperties();
	}
	/**
	 * Returns the custom document properties
	 * 
	 * @return the custom document properties
	 */
	public CustomProperties getCustomProperties() {
		return _document.getProperties().getCustomProperties();
	}

	/**
	 * Returns opened document
	 * 
	 * @return the opened document
	 */
	@Override
	public final POIXMLDocument getDocument() {
		return _document;
	}

	/**
	 * Returns the opened OPCPackage that contains the document
	 * 
	 * @return the opened OPCPackage
	 */
	public OPCPackage getPackage() {
	   return _document.getPackage();
	}

	/**
	 * Returns an OOXML properties text extractor for the
	 *  document properties metadata, such as title and author.
	 */
	@Override
    public POIXMLPropertiesTextExtractor getMetadataTextExtractor() {
		return new POIXMLPropertiesTextExtractor(_document);
	}

	@Override
	public void close() throws IOException {
		// e.g. XSSFEventBaseExcelExtractor passes a null-document
		if(_document != null) {
			@SuppressWarnings("resource")
            OPCPackage pkg = _document.getPackage();
			if(pkg != null) {
			    // revert the package to not re-write the file, which is very likely not wanted for a TextExtractor!
				pkg.revert();
			}
		}
		super.close();
	}

	protected void checkMaxTextSize(CharSequence text, String string) {
        if(string == null) {
            return;
        }

        int size = text.length() + string.length();
        if(size > ZipSecureFile.getMaxTextSize()) {
            throw new IllegalStateException("The text would exceed the max allowed overall size of extracted text. "
                    + "By default this is prevented as some documents may exhaust available memory and it may indicate that the file is used to inflate memory usage and thus could pose a security risk. "
                    + "You can adjust this limit via ZipSecureFile.setMaxTextSize() if you need to work with files which have a lot of text. "
                    + "Size: " + size + ", limit: MAX_TEXT_SIZE: " + ZipSecureFile.getMaxTextSize());
        }
    }
}
