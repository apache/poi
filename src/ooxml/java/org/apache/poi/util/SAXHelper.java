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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;

import javax.xml.XMLConstants;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Provides handy methods for working with SAX parsers and readers
 */
public final class SAXHelper {
    private static POILogger logger = POILogFactory.getLogger(SAXHelper.class);
            
    /**
     * Creates a new SAX Reader, with sensible defaults
     */
    public static SAXReader getSAXReader() {
        SAXReader xmlReader = new SAXReader();
        xmlReader.setValidation(false);
        xmlReader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        });
        trySetSAXFeature(xmlReader, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetXercesSecurityManager(xmlReader);
        return xmlReader;
    }
    private static void trySetSAXFeature(SAXReader xmlReader, String feature, boolean enabled) {
        try {
            xmlReader.setFeature(feature, enabled);
        } catch (Exception e) {
            logger.log(POILogger.INFO, "SAX Feature unsupported", feature, e);
        }
    }
    private static void trySetXercesSecurityManager(SAXReader xmlReader) {
        // Try built-in JVM one first, standalone if not
        for (String securityManagerClassName : new String[] {
                "com.sun.org.apache.xerces.internal.util.SecurityManager",
                "org.apache.xerces.util.SecurityManager"
        }) {
            try {
                Object mgr = Class.forName(securityManagerClassName).newInstance();
                Method setLimit = mgr.getClass().getMethod("setEntityExpansionLimit", Integer.TYPE);
                setLimit.invoke(mgr, 4096);
                xmlReader.setProperty("http://apache.org/xml/properties/security-manager", mgr);
                // Stop once one can be setup without error
                return;
            } catch (Exception e) {
                logger.log(POILogger.INFO, "SAX Security Manager could not be setup", e);
            }
        }
    }

    /**
     * Parses the given stream via the default (sensible)
     * SAX Reader
     * @param inp Stream to read the XML data from
     * @return the SAX processed Document 
     */
    public static Document readSAXDocument(InputStream inp) throws DocumentException {
        return getSAXReader().read(inp);
    }
}
