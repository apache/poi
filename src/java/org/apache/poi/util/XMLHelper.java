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

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Helper methods for working with javax.xml classes.
 */
public final class XMLHelper {
    private static POILogger logger = POILogFactory.getLogger(XMLHelper.class);

    @FunctionalInterface
    private interface SecurityFeature {
        void accept(String name) throws ParserConfigurationException;
    }

    /**
     * Creates a new DocumentBuilderFactory, with sensible defaults
     *
     * @see <a href="https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html">OWASP XXE</a>
     */
    @SuppressWarnings({"squid:S2755"})
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        trySet(XMLConstants.FEATURE_SECURE_PROCESSING, (n) -> factory.setFeature(n, true));
        trySet(XMLConstants.ACCESS_EXTERNAL_SCHEMA, (n) -> factory.setAttribute(n, ""));
        trySet(XMLConstants.ACCESS_EXTERNAL_DTD, (n) -> factory.setAttribute(n, ""));
        trySet("http://xml.org/sax/features/external-general-entities", (n) -> factory.setFeature(n, false));
        trySet("http://xml.org/sax/features/external-parameter-entities", (n) -> factory.setFeature(n, false));
        trySet("http://apache.org/xml/features/nonvalidating/load-external-dtd", (n) -> factory.setFeature(n, false));
        trySet("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", (n) -> factory.setFeature(n, false));
        trySet("http://apache.org/xml/features/disallow-doctype-decl", (n) -> factory.setFeature(n, true));
        trySet("XIncludeAware", (n) -> factory.setXIncludeAware(false));
        return factory;
    }

    private static void trySet(String name, SecurityFeature feature) {
        try {
            feature.accept(name);
        } catch (Exception e) {
            logger.log(POILogger.WARN, "SAX Feature unsupported", name, e);
        } catch (AbstractMethodError ame) {
            logger.log(POILogger.WARN, "Cannot set SAX feature because outdated XML parser in classpath", name, ame);
        }
    }
}
