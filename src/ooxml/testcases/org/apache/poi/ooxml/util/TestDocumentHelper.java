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

import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

public class TestDocumentHelper {
    @Test
    public void testDocumentBuilder() throws Exception {
        DocumentBuilder documentBuilder = DocumentHelper.newDocumentBuilder();
        assertNotSame(documentBuilder, DocumentHelper.newDocumentBuilder());
        assertTrue(documentBuilder.isNamespaceAware());
        assertFalse(documentBuilder.isValidating());
        documentBuilder.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes("UTF-8"))));
    }

    @Test
    public void testDocumentBuilderFactory() throws Exception {
        assertTrue(DocumentHelper.documentBuilderFactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        assertFalse(DocumentHelper.documentBuilderFactory.getFeature(POIXMLConstants.FEATURE_LOAD_DTD_GRAMMAR));
        assertFalse(DocumentHelper.documentBuilderFactory.getFeature(POIXMLConstants.FEATURE_LOAD_EXTERNAL_DTD));
    }
}
