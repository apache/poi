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
package org.apache.poi.ooxml;

import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;

import java.net.URI;

/**
 * Represents a hyperlink relationship.
 *
 * @since POI 5.3.0
 */
public class HyperlinkRelationship extends ReferenceRelationship {
    /**
     * Initializes a new instance of the HyperlinkRelationship.
     *
     * @param hyperlinkUri The target uri of the hyperlink relationship.
     * @param isExternal Is the URI external.
     * @param id The relationship ID.
     */
    protected HyperlinkRelationship(POIXMLDocumentPart container, URI hyperlinkUri, boolean isExternal, String id) {
        super(container, hyperlinkUri, isExternal, PackageRelationshipTypes.HYPERLINK_PART, id);
    }

    @Override
    public String getRelationshipType() {
        return PackageRelationshipTypes.HYPERLINK_PART;
    }
}
