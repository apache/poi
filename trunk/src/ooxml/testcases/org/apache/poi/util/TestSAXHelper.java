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

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class TestSAXHelper {
    @Test
    public void testXMLReader() throws Exception {
        XMLReader reader = SAXHelper.newXMLReader();
        assertNotSame(reader, SAXHelper.newXMLReader());
        assertTrue(reader.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertEquals(SAXHelper.IGNORING_ENTITY_RESOLVER, reader.getEntityResolver());
        assertNotNull(reader.getProperty("http://apache.org/xml/properties/security-manager"));

        reader.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes("UTF-8"))));
    }
}
