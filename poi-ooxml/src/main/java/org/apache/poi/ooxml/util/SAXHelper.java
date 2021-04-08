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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.util.Removal;
import org.apache.poi.util.XMLHelper;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * Provides handy methods for working with SAX parsers and readers
 * @deprecated use {@link XMLHelper}
 */
@Deprecated
@Removal(version = "6.0.0")
public final class SAXHelper {
    /**
     * Creates a new SAX XMLReader, with sensible defaults
     */
    public static XMLReader newXMLReader() throws SAXException, ParserConfigurationException {
        return XMLHelper.newXMLReader();
    }
}
