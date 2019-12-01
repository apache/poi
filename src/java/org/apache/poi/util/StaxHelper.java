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

import java.util.function.Consumer;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;


/**
 * Provides handy methods for working with StAX parsers and readers
 */
public final class StaxHelper {
    private static final POILogger logger = POILogFactory.getLogger(StaxHelper.class);

    private StaxHelper() {
    }

    /**
     * Creates a new StAX XMLInputFactory, with sensible defaults
     */
    @SuppressWarnings({"squid:S2755"})
    public static XMLInputFactory newXMLInputFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        trySet(XMLInputFactory.IS_NAMESPACE_AWARE, (n) -> factory.setProperty(n, true));
        trySet(XMLInputFactory.IS_VALIDATING, (n) -> factory.setProperty(n, false));
        trySet(XMLInputFactory.SUPPORT_DTD, (n) -> factory.setProperty(n, false));
        trySet(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, (n) -> factory.setProperty(n, false));
        return factory;
    }

    /**
     * Creates a new StAX XMLOutputFactory, with sensible defaults
     */
    public static XMLOutputFactory newXMLOutputFactory() {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        trySet(XMLOutputFactory.IS_REPAIRING_NAMESPACES, (n) -> factory.setProperty(n, true));
        return factory;
    }

    /**
     * Creates a new StAX XMLEventFactory, with sensible defaults
     */
    public static XMLEventFactory newXMLEventFactory() {
        // this method seems safer on Android than getFactory()
        return XMLEventFactory.newInstance();
    }

    private static void trySet(String name, Consumer<String> securityFeature) {
        try {
            securityFeature.accept(name);
        } catch (Exception e) {
            logger.log(POILogger.WARN, "StAX Property unsupported", name, e);
        } catch (AbstractMethodError ame) {
            logger.log(POILogger.WARN, "Cannot set StAX property because outdated StAX parser in classpath", name, ame);
        }
    }
}
