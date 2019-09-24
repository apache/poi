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

import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

import javax.xml.XMLConstants;
import javax.xml.xpath.XPathFactory;

public final class XPathHelper {
    private static POILogger logger = POILogFactory.getLogger(XPathHelper.class);

    private XPathHelper() {}

    static final XPathFactory xpathFactory = XPathFactory.newInstance();
    static {
        trySetFeature(xpathFactory, XMLConstants.FEATURE_SECURE_PROCESSING, true);
    }

    public static XPathFactory getFactory() {
        return xpathFactory;
    }

    private static void trySetFeature(XPathFactory xpf, String feature, boolean enabled) {
        try {
            xpf.setFeature(feature, enabled);
        } catch (Exception e) {
            logger.log(POILogger.WARN, "XPathFactory Feature unsupported", feature, e);
        } catch (AbstractMethodError ame) {
            logger.log(POILogger.WARN, "Cannot set XPathFactory feature because outdated XML parser in classpath", feature, ame);
        }
    }
}
