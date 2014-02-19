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
    /**
     * Creates a new SAX Reader, with sensible defaults
     */
    public static SAXReader getSAXReader() {
        SAXReader xmlReader = new SAXReader();
        xmlReader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId)
                    throws SAXException, IOException {
                return new InputSource(new StringReader(""));
            }
        });
        return xmlReader;
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
