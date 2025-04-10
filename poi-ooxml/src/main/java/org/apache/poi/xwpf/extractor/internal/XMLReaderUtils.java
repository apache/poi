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
package org.apache.poi.xwpf.extractor.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.poi.util.XMLHelper;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * <p>
 * This is copied from Apache Tika.
 * </p>
 *
 * @since POI 5.4.2
 */
public final class XMLReaderUtils implements Serializable {

    /**
     * This checks context for a user specified {@link SAXParser}.
     * If one is not found, this reuses a SAXParser from the pool.
     */
    public static void parseSAX(InputStream is, ContentHandler contentHandler)
            throws IOException, SAXException {
        try {
            XMLHelper.getSaxParserFactory().newSAXParser().parse(is, new OfflineContentHandler(contentHandler));
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }
}
