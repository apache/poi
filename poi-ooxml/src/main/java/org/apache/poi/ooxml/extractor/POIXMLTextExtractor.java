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

public interface POIXMLTextExtractor extends POITextExtractor {
	/**
	 * Returns the core document properties
	 *
	 * @return the core document properties
	 */
	default CoreProperties getCoreProperties() {
		 return getDocument().getProperties().getCoreProperties();
	}
	/**
	 * Returns the extended document properties
	 *
	 * @return the extended document properties
	 */
	default ExtendedProperties getExtendedProperties() {
		return getDocument().getProperties().getExtendedProperties();
	}
	/**
	 * Returns the custom document properties
	 *
	 * @return the custom document properties
	 */
	default CustomProperties getCustomProperties() {
		return getDocument().getProperties().getCustomProperties();
	}

	/**
	 * Returns opened document
	 *
	 * @return the opened document
	 */
	@Override
	POIXMLDocument getDocument();

	/**
	 * Returns the opened OPCPackage that contains the document
	 *
	 * @return the opened OPCPackage
	 */
	default OPCPackage getPackage() {
		POIXMLDocument doc = getDocument();
	   	return doc != null ? doc.getPackage() : null;
	}

	/**
	 * Returns an OOXML properties text extractor for the
	 *  document properties metadata, such as title and author.
	 */
	@Override
    default POIXMLPropertiesTextExtractor getMetadataTextExtractor() {
		return new POIXMLPropertiesTextExtractor(getDocument());
	}

	@Override
	default void close() throws IOException {
		// e.g. XSSFEventBaseExcelExtractor passes a null-document
		if (isCloseFilesystem()) {
			@SuppressWarnings("resource")
            OPCPackage pkg = getPackage();
			if (pkg != null) {
			    // revert the package to not re-write the file, which is very likely not wanted for a TextExtractor!
				pkg.revert();
			}
		}
	}

	default void checkMaxTextSize(CharSequence text, String string) {
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
