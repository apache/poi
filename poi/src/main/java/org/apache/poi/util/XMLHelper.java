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

package org.apache.poi.util;

import static javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA;
import static javax.xml.XMLConstants.ACCESS_EXTERNAL_STYLESHEET;
import static javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static javax.xml.stream.XMLInputFactory.IS_NAMESPACE_AWARE;
import static javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES;
import static javax.xml.stream.XMLInputFactory.IS_VALIDATING;
import static javax.xml.stream.XMLInputFactory.SUPPORT_DTD;
import static javax.xml.stream.XMLOutputFactory.IS_REPAIRING_NAMESPACES;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.validation.SchemaFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.poi.POIException;
import org.apache.poi.logging.PoiLogManager;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;

/**
 * Helper methods for working with javax.xml classes.
 *
 * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">OWASP XXE</a>
 */
@Internal
public final class XMLHelper {
    static final String FEATURE_LOAD_DTD_GRAMMAR = "http://apache.org/xml/features/nonvalidating/load-dtd-grammar";
    static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
    static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
    static final String FEATURE_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    static final String FEATURE_EXTERNAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    static final String PROPERTY_ENTITY_EXPANSION_LIMIT = "http://www.oracle.com/xml/jaxp/properties/entityExpansionLimit";
    static final String PROPERTY_SECURITY_MANAGER = "http://apache.org/xml/properties/security-manager";
    static final String METHOD_ENTITY_EXPANSION_XERCES = "setEntityExpansionLimit";

    static final String[] SECURITY_MANAGERS = {
            //"com.sun.org.apache.xerces.internal.util.SecurityManager",
            "org.apache.xerces.util.SecurityManager"
    };

    private static final Logger LOG = PoiLogManager.getLogger(XMLHelper.class);
    private static long lastLog;

    // JAXP does not state whether these two factories may be used by several threads at once, but
    // the JDK implementations only read their feature/attribute state when creating a parser, and
    // both are configured here once and never mutated afterwards, so they are shared unguarded.
    // Guarding them would serialize the much hotter parser-creation paths.
    private static final DocumentBuilderFactory documentBuilderFactory = getDocumentBuilderFactory();

    private static final SAXParserFactory saxFactory = getSaxParserFactory();

    // TransformerFactory differs: its javadoc says "Different TransformerFactories can be used
    // concurrently by different Threads", i.e. one factory is not meant to be shared, so
    // newTransformer synchronizes on it. Only the (cheap) transformer creation is guarded -
    // the transformation itself runs outside the lock.
    private static final TransformerFactory transformerFactory = getTransformerFactory();

    @FunctionalInterface
    private interface SecurityFeature {
        void accept(String name, boolean value) throws ParserConfigurationException, SAXException, TransformerException;
    }

    @FunctionalInterface
    private interface SecurityProperty {
        void accept(String name, Object value) throws SAXException;
    }

    private XMLHelper() {
    }

    /**
     * Creates a new DocumentBuilderFactory, with sensible defaults
     */
    @SuppressWarnings({"squid:S2755"})
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        // this doesn't appear to work, and we still need to limit
        // entity expansions to 1 in trySet(XercesSecurityManager)
        factory.setExpandEntityReferences(false);
        factory.setValidating(false);
        trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
        quietSet(factory::setAttribute, ACCESS_EXTERNAL_SCHEMA, "");
        quietSet(factory::setAttribute, ACCESS_EXTERNAL_DTD, "");
        trySet(factory::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
        trySet(factory::setFeature, FEATURE_PARAMETER_ENTITIES, false);
        trySet(factory::setFeature, FEATURE_LOAD_EXTERNAL_DTD, false);
        trySet(factory::setFeature, FEATURE_LOAD_DTD_GRAMMAR, false);
        trySet(factory::setFeature, FEATURE_DISALLOW_DOCTYPE_DECL, true);
        trySet((n, b) -> factory.setXIncludeAware(b), "XIncludeAware", false);

        Object manager = getXercesSecurityManager();
        if (manager == null || !trySet(factory::setAttribute, PROPERTY_SECURITY_MANAGER, manager)) {
            // separate old version of Xerces not found => use the builtin way of setting the property
            // Note: when entity_expansion_limit==0, there is no limit!
            trySet(factory::setAttribute, PROPERTY_ENTITY_EXPANSION_LIMIT, 1);
        }

        return factory;
    }

    /**
     * Creates a new document builder, with sensible defaults
     *
     * @throws IllegalStateException If creating the DocumentBuilder fails, e.g.
     *                               due to {@link ParserConfigurationException}.
     */
    public static DocumentBuilder newDocumentBuilder() {
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(XMLHelper::ignoreEntity);
            documentBuilder.setErrorHandler(new DocHelperErrorHandler(true));
            return documentBuilder;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("cannot create a DocumentBuilder", e);
        }
    }

    @SuppressWarnings("squid:S2755")
    public static SAXParserFactory getSaxParserFactory() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
            trySet(factory::setFeature, FEATURE_LOAD_DTD_GRAMMAR, false);
            trySet(factory::setFeature, FEATURE_LOAD_EXTERNAL_DTD, false);
            trySet(factory::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
            trySet(factory::setFeature, FEATURE_DISALLOW_DOCTYPE_DECL, true);
            return factory;
        } catch (RuntimeException | Error re) { // NOSONAR
            // this also catches NoClassDefFoundError, which may be due to a local class path issue
            // This may occur if the code is run inside a web container or a restricted JVM
            // See bug 61170: https://bz.apache.org/bugzilla/show_bug.cgi?id=61170
            if (ExceptionUtil.isFatal(re)) {
                ExceptionUtil.rethrow(re);
            }
            logThrowable(re, "Failed to create SAXParserFactory", "-");
            throw re;
        } catch (Exception e) {
            if (ExceptionUtil.isFatal(e)) {
                ExceptionUtil.rethrow(e);
            }
            logThrowable(e, "Failed to create SAXParserFactory", "-");
            throw new IllegalStateException("Failed to create SAXParserFactory", e);
        }
    }

    /**
     * Creates a new SAX XMLReader, with sensible defaults
     */
    public static XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(XMLHelper::ignoreEntity);
        xmlReader.setErrorHandler(new DocHelperErrorHandler(false));
        trySet(xmlReader::setFeature, FEATURE_SECURE_PROCESSING, true);
        trySet(xmlReader::setFeature, FEATURE_EXTERNAL_ENTITIES, false);
        Object manager = getXercesSecurityManager();
        if (manager == null || !trySet(xmlReader::setProperty, PROPERTY_SECURITY_MANAGER, manager)) {
            // separate old version of Xerces not found => use the builtin way of setting the property
            trySet(xmlReader::setProperty, PROPERTY_ENTITY_EXPANSION_LIMIT, 1);
        }
        return xmlReader;
    }

    /**
     * Creates a new StAX XMLInputFactory, with sensible defaults
     */
    @SuppressWarnings({"squid:S2755"})
    public static XMLInputFactory newXMLInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        trySet(factory::setProperty, IS_NAMESPACE_AWARE, true);
        trySet(factory::setProperty, IS_VALIDATING, false);
        trySet(factory::setProperty, SUPPORT_DTD, false);
        trySet(factory::setProperty, IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        return factory;
    }

    /**
     * Creates a new StAX XMLOutputFactory, with sensible defaults
     */
    public static XMLOutputFactory newXMLOutputFactory() {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        trySet(factory::setProperty, IS_REPAIRING_NAMESPACES, true);
        return factory;
    }

    /**
     * Creates a new StAX XMLEventFactory, with sensible defaults
     */
    public static XMLEventFactory newXMLEventFactory() {
        // this method seems safer on Android than getFactory()
        return XMLEventFactory.newInstance();
    }

    @SuppressWarnings({"squid:S4435","java:S2755"})
    public static TransformerFactory getTransformerFactory() {
        TransformerFactory factory = TransformerFactory.newInstance();
        trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
        quietSet(factory::setAttribute, ACCESS_EXTERNAL_DTD, "");
        quietSet(factory::setAttribute, ACCESS_EXTERNAL_STYLESHEET, "");
        quietSet(factory::setAttribute, ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    public static Transformer newTransformer() throws TransformerConfigurationException {
        final Transformer serializer;
        synchronized (transformerFactory) {
            serializer = transformerFactory.newTransformer();
        }
        // TODO set encoding from a command argument
        serializer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        serializer.setOutputProperty(OutputKeys.INDENT, "no");
        serializer.setOutputProperty(OutputKeys.METHOD, "xml");
        return serializer;
    }

    @SuppressWarnings("java:S2755")
    public static SchemaFactory getSchemaFactory() {
        SchemaFactory factory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
        trySet(factory::setFeature, FEATURE_SECURE_PROCESSING, true);
        quietSet(factory::setProperty, ACCESS_EXTERNAL_DTD, "");
        quietSet(factory::setProperty, ACCESS_EXTERNAL_STYLESHEET, "");
        quietSet(factory::setProperty, ACCESS_EXTERNAL_SCHEMA, "");
        return factory;
    }

    /**
     * Counts the depth of the DOM tree starting from the given node.
     *
     * @param node the node to check
     * @param maxSupportedDepth the maximum supported depth of the DOM tree
     * @return the depth
     * @throws POIException if the depth exceeds <code>maxSupportedDepth</code>
     */
    public static int getDepthOfChildNodes(final Node node, final int maxSupportedDepth) throws POIException {
        return getDepthOfChildNodes(node, maxSupportedDepth, 0);
    }

    /**
     * Escapes the five predefined XML entities ({@code & < > " '}) in the supplied string so it
     * can be safely embedded as XML element text or as a single- or double-quoted attribute value.
     * <p>
     * This is intended for the rare cases where XML is assembled by hand; prefer building XML
     * through a DOM/{@link javax.xml.stream.XMLStreamWriter} where practical.
     *
     * @param value the string to escape, may be {@code null}
     * @return the escaped string, or {@code null} if {@code value} was {@code null}
     * @since POI 6.0.0
     */
    public static String escapeXml(final String value) {
        if (value == null) {
            return null;
        }
        StringBuilder sb = null;
        for (int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            final String replacement;
            switch (c) {
                case '&':  replacement = "&amp;";  break;
                case '<':  replacement = "&lt;";   break;
                case '>':  replacement = "&gt;";   break;
                case '"':  replacement = "&quot;"; break;
                case '\'': replacement = "&apos;"; break;
                default:   replacement = null;     break;
            }
            if (replacement == null) {
                if (sb != null) {
                    sb.append(c);
                }
            } else {
                if (sb == null) {
                    sb = new StringBuilder(value.length() + 16);
                    sb.append(value, 0, i);
                }
                sb.append(replacement);
            }
        }
        return sb == null ? value : sb.toString();
    }

    private static int getDepthOfChildNodes(final Node node, final int maxSupportedDepth,
                                            final int nodeDepth) throws POIException {
        final int currentDepth = nodeDepth + 1;
        int maxDepth = currentDepth;
        Node child = node.getFirstChild();
        while (child != null) {
            int childDepth = getDepthOfChildNodes(child, maxSupportedDepth, currentDepth);
            if (childDepth > maxDepth) {
                maxDepth = childDepth;
                if (maxDepth > maxSupportedDepth) {
                    throw new POIException(String.format(Locale.ROOT,
                            "Node depth exceeds maximum supported depth of %s" ,
                            maxSupportedDepth));
                }
            }
            child = child.getNextSibling();
        }
        return maxDepth;
    }

    private static Object _xercesSecurityManager;
    private static volatile boolean _xercesSecurityManagerSet = false;

    private static Object getXercesSecurityManager() {
        if (_xercesSecurityManagerSet) {
            return _xercesSecurityManager;
        } else {
            synchronized (XMLHelper.class) {
                if (!_xercesSecurityManagerSet) {
                    _xercesSecurityManager = tryGetXercesSecurityManager();
                    _xercesSecurityManagerSet = true;
                }
            }
            return _xercesSecurityManager;
        }
    }

    private static Object tryGetXercesSecurityManager() {
        // Try built-in JVM one first, standalone if not
        for (String securityManagerClassName : SECURITY_MANAGERS) {
            try {
                Object mgr = Class.forName(securityManagerClassName).getDeclaredConstructor().newInstance();
                Method setLimit = mgr.getClass().getMethod(METHOD_ENTITY_EXPANSION_XERCES, Integer.TYPE);
                setLimit.invoke(mgr, 1);
                // Stop once one can be setup without error
                return mgr;
            } catch (ClassNotFoundException ignored) {
                // continue without log, this is expected in some setups
            } catch (Throwable e) {     // NOSONAR - also catch things like NoClassDefError here
                if (ExceptionUtil.isFatal(e)) {
                    ExceptionUtil.rethrow(e);
                }
                logThrowable(e, "SAX Feature unsupported", securityManagerClassName);
            }
        }
        return null;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static boolean trySet(SecurityFeature feature, String name, boolean value) {
        try {
            feature.accept(name, value);
            return true;
        } catch (Exception e) {
            if (ExceptionUtil.isFatal(e)) {
                ExceptionUtil.rethrow(e);
            }
            logThrowable(e, "SAX Feature unsupported", name);
        } catch (Error e) {
            if (ExceptionUtil.isFatal(e)) {
                ExceptionUtil.rethrow(e);
            }
            logThrowable(e, "Cannot set SAX feature because outdated XML parser in classpath", name);
        }
        return false;
    }

    private static boolean trySet(SecurityProperty property, String name, Object value) {
        try {
            property.accept(name, value);
            return true;
        } catch (Exception e) {
            if (ExceptionUtil.isFatal(e)) {
                ExceptionUtil.rethrow(e);
            }
            logThrowable(e, "SAX Feature unsupported", name);
        } catch (Error e) {
            if (ExceptionUtil.isFatal(e)) {
                ExceptionUtil.rethrow(e);
            }
            // ignore all top error object - GraalVM in native mode is not coping with java.xml error message resources
            logThrowable(e, "Cannot set SAX feature because outdated XML parser in classpath", name);
        }
        return false;
    }

    private static boolean quietSet(SecurityProperty property, String name, Object value) {
        try {
            property.accept(name, value);
            return true;
        } catch (Throwable e) {
            if (ExceptionUtil.isFatal(e)) {
                ExceptionUtil.rethrow(e);
            }
        }
        return false;
    }

    private static void logThrowable(Throwable t, String message, String name) {
        if (System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
            LOG.atWarn().withThrowable(t).log("{} [log suppressed for 5 minutes] {}", message, name);
            lastLog = System.currentTimeMillis();
        }
    }

    private static class DocHelperErrorHandler implements ErrorHandler {
        private final boolean logException;

        public DocHelperErrorHandler(boolean logException) {
            this.logException = logException;
        }

        public void warning(SAXParseException exception) {
            printError(Level.WARN, exception);
        }

        public void error(SAXParseException exception) {
            printError(Level.ERROR, exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            printError(Level.FATAL, exception);
            throw exception;
        }

        /**
         * Prints the error message.
         */
        private void printError(Level type, SAXParseException ex) {
            String systemId = ex.getSystemId();
            if (systemId != null) {
                int index = systemId.lastIndexOf('/');
                if (index != -1) {
                    systemId = systemId.substring(index + 1);
                }
            }
            String message = (systemId == null ? "" : systemId) +
                    ':' + ex.getLineNumber() +
                    ':' + ex.getColumnNumber() +
                    ':' + ex.getMessage();

            LogBuilder builder = LOG.atLevel(type);

            if (logException) {
                builder = builder.withThrowable(ex);
            }

            builder.log(message);
        }
    }

    private static InputSource ignoreEntity(String publicId, String systemId) {
        return new InputSource(new StringReader(""));
    }
}
