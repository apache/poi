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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.apache.xmlbeans.XmlOptions;

public class POIXMLTypeLoader {

    // TODO: Do these have a good home like o.a.p.openxml4j.opc.PackageNamespaces and PackageRelationshipTypes?
    // These constants should be common to all of POI and easy to use by other applications such as Tika
    private static final String MS_OFFICE_URN = "urn:schemas-microsoft-com:office:office";
    private static final String MS_EXCEL_URN = "urn:schemas-microsoft-com:office:excel";
    private static final String MS_WORD_URN = "urn:schemas-microsoft-com:office:word";
    private static final String MS_VML_URN = "urn:schemas-microsoft-com:vml";
    
    public static final XmlOptions DEFAULT_XML_OPTIONS;
    static {
        DEFAULT_XML_OPTIONS = new XmlOptions();
        DEFAULT_XML_OPTIONS.setSaveOuter();
        DEFAULT_XML_OPTIONS.setUseDefaultNamespace();
        DEFAULT_XML_OPTIONS.setSaveAggressiveNamespaces();
        DEFAULT_XML_OPTIONS.setCharacterEncoding("UTF-8");
        DEFAULT_XML_OPTIONS.setDisallowDocTypeDeclaration(true);
        DEFAULT_XML_OPTIONS.setEntityExpansionLimit(1);
        // Piccolo is disabled for POI builts, i.e. JAXP is used for parsing
        // so only user code using XmlObject/XmlToken.Factory.parse
        // directly can bypass the entity check, which is probably unlikely (... and not within our responsibility :)) 
        // DEFAULT_XML_OPTIONS.setLoadEntityBytesLimit(4096);
        
        // POI is not thread-safe - so we can switch to unsynchronized xmlbeans mode - see #61350
        // Update: disabled again for now as it caused strange NPEs and other problems
        // when reading properties in separate workbooks in multiple threads
        // DEFAULT_XML_OPTIONS.setUnsynchronized();

        Map<String, String> map = new HashMap<>();
        map.put(XSSFRelation.NS_DRAWINGML, "a");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/chart", "c");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        map.put(PackageNamespaces.MARKUP_COMPATIBILITY, "ve");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "vt");
        map.put(XSSFRelation.NS_PRESENTATIONML, "p");
        map.put(XSSFRelation.NS_WORDPROCESSINGML, "w");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        map.put(MS_OFFICE_URN, "o");
        map.put(MS_EXCEL_URN, "x");
        map.put(MS_WORD_URN, "w10");
        map.put(MS_VML_URN, "v");
        DEFAULT_XML_OPTIONS.setSaveSuggestedPrefixes(Collections.unmodifiableMap(map));
    }
}
