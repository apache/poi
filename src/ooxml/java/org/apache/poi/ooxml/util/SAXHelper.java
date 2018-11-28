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

package org.apache.poi.ooxml.util;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * Provides handy methods for working with SAX parsers and readers
 */
public final class SAXHelper {
    private static final POILogger logger = POILogFactory.getLogger(SAXHelper.class);
    private static long lastLog;

    private SAXHelper() {}

    /**
     * Creates a new SAX XMLReader, with sensible defaults
     */
    public static synchronized XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        XMLReader xmlReader = saxFactory.newSAXParser().getXMLReader();
        xmlReader.setEntityResolver(IGNORING_ENTITY_RESOLVER);
        trySetSAXFeature(xmlReader, XMLConstants.FEATURE_SECURE_PROCESSING);
        trySetXercesSecurityManager(xmlReader);
        return xmlReader;
    }
    
    static final EntityResolver IGNORING_ENTITY_RESOLVER = new EntityResolver() {
        @Override
        public InputSource resolveEntity(String publicId, String systemId)
                throws SAXException, IOException {
            return new InputSource(new StringReader(""));
        }
    };
    
    private static final SAXParserFactory saxFactory;
    static {
        try {
            saxFactory = SAXParserFactory.newInstance();
            saxFactory.setValidating(false);
            saxFactory.setNamespaceAware(true);
            trySetSAXFeature(saxFactory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
            trySetSAXFeature(saxFactory, POIXMLConstants.FEATURE_LOAD_DTD_GRAMMAR, false);
            trySetSAXFeature(saxFactory, POIXMLConstants.FEATURE_LOAD_EXTERNAL_DTD, false);
        } catch (RuntimeException | Error re) { // NOSONAR
            // this also catches NoClassDefFoundError, which may be due to a local class path issue
            // This may occur if the code is run inside a web container
            // or a restricted JVM
            // See bug 61170: https://bz.apache.org/bugzilla/show_bug.cgi?id=61170
            logger.log(POILogger.WARN, "Failed to create SAXParserFactory", re);
            throw re;
        } catch (Exception e) {
            logger.log(POILogger.WARN, "Failed to create SAXParserFactory", e);
            throw new RuntimeException("Failed to create SAXParserFactory", e);
        }
    }
            
    private static void trySetSAXFeature(SAXParserFactory spf, String feature, boolean flag) {
        try {
            spf.setFeature(feature, flag);
        } catch (Exception e) {
            logger.log(POILogger.WARN, "SAX Feature unsupported", feature, e);
        } catch (AbstractMethodError ame) {
            logger.log(POILogger.WARN, "Cannot set SAX feature because outdated XML parser in classpath", feature, ame);
        }
    }

    private static void trySetSAXFeature(XMLReader xmlReader, String feature) {
        try {
            xmlReader.setFeature(feature, true);
        } catch (Exception e) {
            logger.log(POILogger.WARN, "SAX Feature unsupported", feature, e);
        } catch (AbstractMethodError ame) {
            logger.log(POILogger.WARN, "Cannot set SAX feature because outdated XML parser in classpath", feature, ame);
        }
    }
    
    private static void trySetXercesSecurityManager(XMLReader xmlReader) {
        // Try built-in JVM one first, standalone if not
        for (String securityManagerClassName : new String[] {
                //"com.sun.org.apache.xerces.internal.util.SecurityManager",
                "org.apache.xerces.util.SecurityManager"
        }) {
            try {
                Object mgr = Class.forName(securityManagerClassName).newInstance();
                Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
                setLimit.invoke(mgr, 1);
                xmlReader.setProperty(POIXMLConstants.PROPERTY_SECURITY_MANAGER, mgr);
                // Stop once one can be setup without error
                return;
            } catch (ClassNotFoundException e) {
                // continue without log, this is expected in some setups
            } catch (Throwable e) {     // NOSONAR - also catch things like NoClassDefError here
                // throttle the log somewhat as it can spam the log otherwise
                if(System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
                    logger.log(POILogger.WARN, "SAX Security Manager could not be setup [log suppressed for 5 minutes]", e);
                    lastLog = System.currentTimeMillis();
                }
            }
        }

        // separate old version of Xerces not found => use the builtin way of setting the property
        try {
            xmlReader.setProperty(POIXMLConstants.PROPERTY_ENTITY_EXPANSION_LIMIT, 1);
        } catch (SAXException e) {     // NOSONAR - also catch things like NoClassDefError here
            // throttle the log somewhat as it can spam the log otherwise
            if(System.currentTimeMillis() > lastLog + TimeUnit.MINUTES.toMillis(5)) {
                logger.log(POILogger.WARN, "SAX Security Manager could not be setup [log suppressed for 5 minutes]", e);
                lastLog = System.currentTimeMillis();
            }
        }
    }
}
