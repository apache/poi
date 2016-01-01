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

package org.apache.poi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.w3c.dom.Node;

@SuppressWarnings("deprecation")
public class POIXMLTypeLoader {

    public static final XmlOptions DEFAULT_XML_OPTIONS;
    static {
        DEFAULT_XML_OPTIONS = new XmlOptions();
        DEFAULT_XML_OPTIONS.setSaveOuter();
        DEFAULT_XML_OPTIONS.setUseDefaultNamespace();
        DEFAULT_XML_OPTIONS.setSaveAggressiveNamespaces();
        DEFAULT_XML_OPTIONS.setCharacterEncoding("UTF-8");
        DEFAULT_XML_OPTIONS.setLoadEntityBytesLimit(4096);

        Map<String, String> map = new HashMap<String, String>();
        map.put("http://schemas.openxmlformats.org/drawingml/2006/main", "a");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/chart", "c");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        map.put("http://schemas.openxmlformats.org/markup-compatibility/2006", "ve");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes", "vt");
        map.put("http://schemas.openxmlformats.org/presentationml/2006/main", "p");
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        map.put("urn:schemas-microsoft-com:office:office", "o");
        map.put("urn:schemas-microsoft-com:office:excel", "x");
        map.put("urn:schemas-microsoft-com:office:word", "w10");
        map.put("urn:schemas-microsoft-com:vml", "v");
        DEFAULT_XML_OPTIONS.setSaveSuggestedPrefixes(Collections.unmodifiableMap(map));
    }
    
    private static XmlOptions getXmlOptions(XmlOptions options) {
        XmlOptions opt = (options == null) ? DEFAULT_XML_OPTIONS : options;
        return opt;
    }

    public static XmlObject newInstance(SchemaType type, XmlOptions options) {
        return XmlBeans.getContextTypeLoader().newInstance(type, getXmlOptions(options));
    }

    public static XmlObject parse(String xmlText, SchemaType type, XmlOptions options) throws XmlException {
        return XmlBeans.getContextTypeLoader().parse(xmlText, type, getXmlOptions(options));
    }

    public static XmlObject parse(File file, SchemaType type, XmlOptions options) throws XmlException, IOException {
        return XmlBeans.getContextTypeLoader().parse(file, type, getXmlOptions(options));
    }

    public static XmlObject parse(URL file, SchemaType type, XmlOptions options) throws XmlException, IOException {
        return XmlBeans.getContextTypeLoader().parse(file, type, getXmlOptions(options));
    }

    public static XmlObject parse(InputStream jiois, SchemaType type, XmlOptions options) throws XmlException, IOException {
        return XmlBeans.getContextTypeLoader().parse(jiois, type, getXmlOptions(options));
    }

    public static XmlObject parse(XMLStreamReader xsr, SchemaType type, XmlOptions options) throws XmlException {
        return XmlBeans.getContextTypeLoader().parse(xsr, type, getXmlOptions(options));
    }

    public static XmlObject parse(Reader jior, SchemaType type, XmlOptions options) throws XmlException, IOException {
        return XmlBeans.getContextTypeLoader().parse(jior, type, getXmlOptions(options));
    }

    public static XmlObject parse(Node node, SchemaType type, XmlOptions options) throws XmlException {
        return XmlBeans.getContextTypeLoader().parse(node, type, getXmlOptions(options));
    }

    public static XmlObject parse(XMLInputStream xis, SchemaType type, XmlOptions options) throws XmlException, XMLStreamException {
        return XmlBeans.getContextTypeLoader().parse(xis, type, getXmlOptions(options));
    }
    
    public static XMLInputStream newValidatingXMLInputStream ( XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException {
        return XmlBeans.getContextTypeLoader().newValidatingXMLInputStream(xis, type, getXmlOptions(options));
    }
}
