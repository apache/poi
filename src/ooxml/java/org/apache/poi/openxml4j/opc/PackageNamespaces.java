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
 * Open Packaging Convention namespaces URI.
 *
 * @author Julien Chable
 * @version 1.0
 */
public interface PackageNamespaces {

	/**
	 * Content Types.
	 */
	public static final String CONTENT_TYPES = "http://schemas.openxmlformats.org/package/2006/content-types";

	/**
	 * Core Properties.
	 */
	public static final String CORE_PROPERTIES = "http://schemas.openxmlformats.org/package/2006/metadata/core-properties";

	/**
	 * Digital Signatures.
	 */
	public static final String DIGITAL_SIGNATURE = "http://schemas.openxmlformats.org/package/2006/digital-signature";

	/**
	 * Relationships.
	 */
	public static final String RELATIONSHIPS = "http://schemas.openxmlformats.org/package/2006/relationships";

	/**
	 * Markup Compatibility.
	 */
	public static final String MARKUP_COMPATIBILITY = "http://schemas.openxmlformats.org/markup-compatibility/2006";
}
