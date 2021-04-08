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

package org.apache.poi.poifs.crypt.dsig;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;

public class DSigRelation extends POIXMLRelation {
    /**
     * A map to lookup POIXMLRelation by its relation type
     */
    private static final Map<String, DSigRelation> _table = new HashMap<>();

    public static final DSigRelation ORIGIN_SIGS = new DSigRelation(
        ContentTypes.DIGITAL_SIGNATURE_ORIGIN_PART,
        PackageRelationshipTypes.DIGITAL_SIGNATURE_ORIGIN,
        "/_xmlsignatures/origin.sigs"
    );

    public static final DSigRelation SIG = new DSigRelation(
        ContentTypes.DIGITAL_SIGNATURE_XML_SIGNATURE_PART,
            PackageRelationshipTypes.DIGITAL_SIGNATURE,
        "/_xmlsignatures/sig#.xml"
    );

    private DSigRelation(String type, String rel, String defaultName) {
        super(type, rel, defaultName);
        _table.put(rel, this);
    }

    /**
     * Get POIXMLRelation by relation type
     *
     * @param rel relation type, for example,
     *            <code>http://schemas.openxmlformats.org/officeDocument/2006/relationships/image</code>
     * @return registered POIXMLRelation or null if not found
     */
    public static DSigRelation getInstance(String rel) {
        return _table.get(rel);
    }

}
