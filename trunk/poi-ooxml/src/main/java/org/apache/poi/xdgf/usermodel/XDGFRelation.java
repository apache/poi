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

package org.apache.poi.xdgf.usermodel;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ooxml.POIXMLRelation;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;

public class XDGFRelation extends POIXMLRelation {

    /**
     * A map to lookup POIXMLRelation by its relation type
     */
    private static final Map<String, XDGFRelation> _table = new HashMap<>();

    public static final XDGFRelation DOCUMENT = new XDGFRelation(
        "application/vnd.ms-visio.drawing.main+xml",
        PackageRelationshipTypes.VISIO_CORE_DOCUMENT,
        "/visio/document.xml", null);

    public static final XDGFRelation MASTERS = new XDGFRelation(
        "application/vnd.ms-visio.masters+xml",
        "http://schemas.microsoft.com/visio/2010/relationships/masters",
        "/visio/masters/masters.xml", XDGFMasters::new);

    public static final XDGFRelation MASTER = new XDGFRelation(
        "application/vnd.ms-visio.master+xml",
        "http://schemas.microsoft.com/visio/2010/relationships/master",
        "/visio/masters/master#.xml", XDGFMasterContents::new);

    public static final XDGFRelation IMAGES = new XDGFRelation(null,
        PackageRelationshipTypes.IMAGE_PART, null, null // XSSFPictureData.class
    );

    public static final XDGFRelation PAGES = new XDGFRelation(
        "application/vnd.ms-visio.pages+xml",
        "http://schemas.microsoft.com/visio/2010/relationships/pages",
        "/visio/pages/pages.xml", XDGFPages::new);

    public static final XDGFRelation PAGE = new XDGFRelation(
        "application/vnd.ms-visio.page+xml",
        "http://schemas.microsoft.com/visio/2010/relationships/page",
        "/visio/pages/page#.xml", XDGFPageContents::new);

    public static final XDGFRelation WINDOW = new XDGFRelation(
        "application/vnd.ms-visio.windows+xml",
        "http://schemas.microsoft.com/visio/2010/relationships/windows",
        "/visio/windows.xml", null);

    private XDGFRelation(String type, String rel, String defaultName,
                         PackagePartConstructor packagePartConstructor) {
        super(type, rel, defaultName, null, packagePartConstructor, null);
        _table.put(rel, this);
    }

    /**
     * Get POIXMLRelation by relation type
     *
     * @param rel
     *            relation type, for example,
     *            <code>http://schemas.openxmlformats.org/officeDocument/2006/relationships/image</code>
     * @return registered POIXMLRelation or null if not found
     */
    public static XDGFRelation getInstance(String rel) {
        return _table.get(rel);
    }
}
