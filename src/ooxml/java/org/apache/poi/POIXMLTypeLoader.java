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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamReader;

import org.apache.poi.util.DocumentHelper;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlBeans;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.xml.stream.XMLInputStream;
import org.apache.xmlbeans.xml.stream.XMLStreamException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings("deprecation")
public class POIXMLTypeLoader {

    private static ThreadLocal<ClassLoader> classLoader = new ThreadLocal<ClassLoader>();
    
    public static final XmlOptions DEFAULT_XML_OPTIONS;
    static {
        DEFAULT_XML_OPTIONS = new XmlOptions();
        DEFAULT_XML_OPTIONS.setSaveOuter();
        DEFAULT_XML_OPTIONS.setUseDefaultNamespace();
        DEFAULT_XML_OPTIONS.setSaveAggressiveNamespaces();
        DEFAULT_XML_OPTIONS.setCharacterEncoding("UTF-8");
        // Piccolo is disabled for POI builts, i.e. JAXP is used for parsing
        // so only user code using XmlObject/XmlToken.Factory.parse
        // directly can bypass the entity check, which is probably unlikely (... and not within our responsibility :)) 
        // DEFAULT_XML_OPTIONS.setLoadEntityBytesLimit(4096);

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
        return options == null ? DEFAULT_XML_OPTIONS : options;
    }

    /**
     * Sets the {@link ClassLoader} which is used, when XmlBeans are dynamically instantiated -
     * opposed to being loaded by the factory class which is accompanied by each generated XmlBeans interface.
     * <p>
     * This is especially necessary in a context which doesn't guarantee that the current (thread) context
     * cassloader has access to all XmlBeans schema definitions (*.xsb) - which is typically in OSGI the case.
     * <p>
     * The classloader will be only set for the current thread in a {@link ThreadLocal}. Although the
     * ThreadLocal is implemented via a {@link WeakReference}, it's good style to {@code null} the classloader
     * when the user code is finalized.
     * 
     * @param cl the classloader to be used when XmlBeans classes and definitions are looked up
     */
    public static void setClassLoader(ClassLoader cl) {
        classLoader.set(cl);
    }
    
    private static SchemaTypeLoader getTypeLoader() {
        ClassLoader cl = classLoader.get();
        return (cl == null)
            ? XmlBeans.getContextTypeLoader()
            : XmlBeans.typeLoaderForClassLoader(cl);
    }
    
    public static XmlObject newInstance(SchemaType type, XmlOptions options) {
        return getTypeLoader().newInstance(type, getXmlOptions(options));
    }

    public static XmlObject parse(String xmlText, SchemaType type, XmlOptions options) throws XmlException {
        try {
            return parse(new StringReader(xmlText), type, options);
        } catch (IOException e) {
            throw new XmlException("Unable to parse xml bean", e);
        }
    }

    public static XmlObject parse(File file, SchemaType type, XmlOptions options) throws XmlException, IOException {
        InputStream is = new FileInputStream(file);
        try {
            return parse(is, type, options);
        } finally {
            is.close();
        }
    }

    public static XmlObject parse(URL file, SchemaType type, XmlOptions options) throws XmlException, IOException {
        InputStream is = file.openStream();
        try {
            return parse(is, type, options);
        } finally {
            is.close();
        }
    }

    public static XmlObject parse(InputStream jiois, SchemaType type, XmlOptions options) throws XmlException, IOException {
        try {
            Document doc = DocumentHelper.readDocument(jiois);
            return getTypeLoader().parse(doc.getDocumentElement(), type, getXmlOptions(options));
        } catch (SAXException e) {
            throw new IOException("Unable to parse xml bean", e);
        }
    }

    public static XmlObject parse(XMLStreamReader xsr, SchemaType type, XmlOptions options) throws XmlException {
        return getTypeLoader().parse(xsr, type, getXmlOptions(options));
    }

    public static XmlObject parse(Reader jior, SchemaType type, XmlOptions options) throws XmlException, IOException {
        try {
            Document doc = DocumentHelper.readDocument(new InputSource(jior));
            return getTypeLoader().parse(doc.getDocumentElement(), type, getXmlOptions(options));
        } catch (SAXException e) {
            throw new XmlException("Unable to parse xml bean", e);
        }
    }

    public static XmlObject parse(Node node, SchemaType type, XmlOptions options) throws XmlException {
        return getTypeLoader().parse(node, type, getXmlOptions(options));
    }

    public static XmlObject parse(XMLInputStream xis, SchemaType type, XmlOptions options) throws XmlException, XMLStreamException {
        return getTypeLoader().parse(xis, type, getXmlOptions(options));
    }
    
    public static XMLInputStream newValidatingXMLInputStream ( XMLInputStream xis, SchemaType type, XmlOptions options ) throws XmlException, XMLStreamException {
        return getTypeLoader().newValidatingXMLInputStream(xis, type, getXmlOptions(options));
    }
}
