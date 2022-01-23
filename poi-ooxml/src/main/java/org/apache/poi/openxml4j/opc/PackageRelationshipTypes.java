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
 */
public interface PackageRelationshipTypes {
    /**
     * Core properties relationship type.
     *
     *  <p>
     *  The standard specifies a source relations ship for the Core File Properties part as follows:
     *  <code>http://schemas.openxmlformats.org/officedocument/2006/relationships/metadata/core-properties.</code>
     *  </p>
     *  <p>
     *   Office uses the following source relationship for the Core File Properties part:
     *   <code>http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties.</code>
     * </p>
     * See 2.1.33 Part 1 Section 15.2.11.1, Core File Properties Part in [MS-OE376].pdf
     */
    String CORE_PROPERTIES = "http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties";

    /**
     * Core properties relationship type as defined in ECMA 376.
     * <p>
     * See 2.1.33 Part 1 Section 15.2.11.1, Core File Properties Part in [MS-OE376].pdf.
     * The case of 'officedocument' matches what appears in the pdf.
     * </p>
     */
    String CORE_PROPERTIES_ECMA376 = "http://schemas.openxmlformats.org/officedocument/2006/relationships/metadata/core-properties";

    /**
     * Namespace of Core properties relationship type as defined in ECMA 376
     */
    String CORE_PROPERTIES_ECMA376_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships";

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
     * Extended properties relationship type for strict ooxml.
     */
    String STRICT_EXTENDED_PROPERTIES = "http://purl.oclc.org/ooxml/officeDocument/relationships/extendedProperties";

    /**
     * Custom properties relationship type.
     */
    String CUSTOM_PROPERTIES = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/custom-properties";

    /**
     * Core document relationship type.
     */
    String CORE_DOCUMENT = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";

    /**
     * Core document relationship type for strict ooxml.
     */
    String STRICT_CORE_DOCUMENT = "http://purl.oclc.org/ooxml/officeDocument/relationships/officeDocument";

    /**
     * Custom XML relationship type.
     */
    String CUSTOM_XML = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/customXml";

    /**
     * Image type.
     */
    String IMAGE_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";

    /**
     * hdphoto type.
     */
    String HDPHOTO_PART = "http://schemas.microsoft.com/office/2007/relationships/hdphoto";

    /**
     * Hyperlink type.
     */
    String HYPERLINK_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";

    /**
     * Style type.
     */
    String STYLE_PART = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles";

    /**
     * External Link to another Document
     */
    String EXTERNAL_LINK_PATH = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/externalLinkPath";

    /**
     * Visio 2010 VSDX equivalent of package {@link #CORE_DOCUMENT}
     */
    String VISIO_CORE_DOCUMENT = "http://schemas.microsoft.com/visio/2010/relationships/document";
}
