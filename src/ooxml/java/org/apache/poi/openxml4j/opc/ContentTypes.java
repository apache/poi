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

package org.apache.poi.openxml4j.opc;

/**
 * Open Packaging Convention content types (see Annex F : Standard Namespaces
 * and Content Types).
 *
 * @author CDubettier define some constants, Julien Chable
 */
public final class ContentTypes {

	/*
	 * Open Packaging Convention (Annex F : Standard Namespaces and Content
	 * Types)
	 */

	/**
	 * Core Properties part.
	 */
	public static final String CORE_PROPERTIES_PART = "application/vnd.openxmlformats-package.core-properties+xml";

	/**
	 * Digital Signature Certificate part.
	 */
	public static final String DIGITAL_SIGNATURE_CERTIFICATE_PART = "application/vnd.openxmlformats-package.digital-signature-certificate";

	/**
	 * Digital Signature Origin part.
	 */
	public static final String DIGITAL_SIGNATURE_ORIGIN_PART = "application/vnd.openxmlformats-package.digital-signature-origin";

	/**
	 * Digital Signature XML Signature part.
	 */
	public static final String DIGITAL_SIGNATURE_XML_SIGNATURE_PART = "application/vnd.openxmlformats-package.digital-signature-xmlsignature+xml";

	/**
	 * Relationships part.
	 */
	public static final String RELATIONSHIPS_PART = "application/vnd.openxmlformats-package.relationships+xml";

	/**
	 * Custom XML part.
	 */
	public static final String CUSTOM_XML_PART = "application/vnd.openxmlformats-officedocument.customXmlProperties+xml";

	/**
	 * Plain old xml. Note - OOXML uses application/xml, and not text/xml!
	 */
	public static final String PLAIN_OLD_XML = "application/xml";

	public static final String IMAGE_JPEG = "image/jpeg";

	public static final String EXTENSION_JPG_1 = "jpg";

	public static final String EXTENSION_JPG_2 = "jpeg";

	// image/png ISO/IEC 15948:2003 http://www.libpng.org/pub/png/spec/
	public static final String IMAGE_PNG = "image/png";

	public static final String EXTENSION_PNG = "png";

	// image/gif http://www.w3.org/Graphics/GIF/spec-gif89a.txt
	public static final String IMAGE_GIF = "image/gif";

	public static final String EXTENSION_GIF = "gif";

	/**
	 * TIFF image format.
	 *
	 * @see <a href="http://partners.adobe.com/public/developer/tiff/index.html#spec">
	 * http://partners.adobe.com/public/developer/tiff/index.html#spec</a>
	 */
	public static final String IMAGE_TIFF = "image/tiff";

	public static final String EXTENSION_TIFF = "tiff";

	/**
	 * Pict image format.
	 *
	 * @see <a href="http://developer.apple.com/documentation/mac/QuickDraw/QuickDraw-2.html">
	 * http://developer.apple.com/documentation/mac/QuickDraw/QuickDraw-2.html</a>
	 */
	public static final String IMAGE_PICT = "image/pict";

	public static final String EXTENSION_PICT = "tiff";

	/**
	 * XML file.
	 */
	public static final String XML = "text/xml";

	public static final String EXTENSION_XML = "xml";

	public static String getContentTypeFromFileExtension(String filename) {
		String extension = filename.substring(filename.lastIndexOf(".") + 1)
				.toLowerCase();
		if (extension.equals(EXTENSION_JPG_1)
				|| extension.equals(EXTENSION_JPG_2))
			return IMAGE_JPEG;
		else if (extension.equals(EXTENSION_GIF))
			return IMAGE_GIF;
		else if (extension.equals(EXTENSION_PICT))
			return IMAGE_PICT;
		else if (extension.equals(EXTENSION_PNG))
			return IMAGE_PNG;
		else if (extension.equals(EXTENSION_TIFF))
			return IMAGE_TIFF;
		else if (extension.equals(EXTENSION_XML))
			return XML;
		else
			return null;
	}
}
