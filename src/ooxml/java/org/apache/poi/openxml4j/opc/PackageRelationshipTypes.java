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
 * Relationship types.
 *
 * @author Julien Chable
 * @version 0.2
 */
public interface PackageRelationshipTypes {

	/**
	 * Core properties relationship type.
	 */
	String CORE_PROPERTIES = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";

	/**
	 * Digital signature relationship type.
	 */
	String DIGITAL_SIGNATURE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/signature";

	/**
	 * Digital signature certificate relationship type.
	 */
	String DIGITAL_SIGNATURE_CERTIFICATE = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/certificate";

	/**
	 * Digital signature origin relationship type.
	 */
	String DIGITAL_SIGNATURE_ORIGIN = "http://schemas.openxmlformats.org/package/2006/relationships/digital-signature/origin";

	/**
	 * Thumbnail relationship type.
	 */
	String THUMBNAIL = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/thumbnail";

	/**
	 * Extended properties relationship type.
	 */
	String EXTENDED_PROPERTIES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties";

	/**
	 * Custom properties relationship type.
	 */
	String CUSTOM_PROPERTIES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";
	
	/**
	 * Core properties relationship type.
	 */
	String CORE_DOCUMENT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";

	/**
	 * Custom XML relationship type.
	 */
	String CUSTOM_XML = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml";

	/**
	 * Image type.
	 */
	String IMAGE_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";

	/**
	 * Style type.
	 */
	String STYLE_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";
}
