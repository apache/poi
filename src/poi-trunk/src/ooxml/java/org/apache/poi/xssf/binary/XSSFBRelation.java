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

package org.apache.poi.xssf.binary;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.util.Internal;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Need to have this mirror class of {@link org.apache.poi.xssf.usermodel.XSSFRelation}
 * because of conflicts with regular ooxml relations.
 * If we failed to break this into a separate class, in the cases of SharedStrings and Styles,
 * 2 parts would exist, and &quot;Packages shall not contain equivalent part names...&quot;
 * <p>
 * Also, we need to avoid the possibility of breaking the marshalling process for xml.
 */
@Internal
public class XSSFBRelation extends POIXMLRelation {
    private static final POILogger log = POILogFactory.getLogger(XSSFBRelation.class);

    static final XSSFBRelation SHARED_STRINGS_BINARY = new XSSFBRelation(
            "application/vnd.ms-excel.sharedStrings",
            "http://schemas.openxmlformats.org/officeDocument/2006/relationships/sharedStrings",
            "/xl/sharedStrings.bin",
            null
    );

    public static final XSSFBRelation STYLES_BINARY = new XSSFBRelation(
            "application/vnd.ms-excel.styles",
            PackageRelationshipTypes.STYLE_PART,
            "/xl/styles.bin",
            null
    );

    private XSSFBRelation(String type, String rel, String defaultName, Class<? extends POIXMLDocumentPart> cls) {
        super(type, rel, defaultName, cls);
    }

}
