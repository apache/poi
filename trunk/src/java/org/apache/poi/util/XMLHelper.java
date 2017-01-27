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

/**
 * Helper methods for working with javax.xml classes.
 */
public final class XMLHelper
{
    private static POILogger logger = POILogFactory.getLogger(XMLHelper.class);
    
    /**
     * Creates a new DocumentBuilderFactory, with sensible defaults
     */
    public static DocumentBuilderFactory getDocumentBuilderFactory() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(false);
        trySetSAXFeature(factory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
        trySetSAXFeature(factory, "http://xml.org/sax/features/external-general-entities", false);
        trySetSAXFeature(factory, "http://xml.org/sax/features/external-parameter-entities", false);
        trySetSAXFeature(factory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        trySetSAXFeature(factory, "http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        return factory;
    }
    
    private static void trySetSAXFeature(DocumentBuilderFactory documentBuilderFactory, String feature, boolean enabled) {
        try {
            documentBuilderFactory.setFeature(feature, enabled);
        } catch (Exception e) {
            logger.log(POILogger.WARN, "SAX Feature unsupported", feature, e);
        } catch (AbstractMethodError ame) {
            logger.log(POILogger.WARN, "Cannot set SAX feature because outdated XML parser in classpath", feature, ame);
        }
    }
    

}
