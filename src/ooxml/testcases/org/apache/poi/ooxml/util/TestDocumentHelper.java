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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

public class TestDocumentHelper {
    @Test
    public void testDocumentBuilder() throws Exception {
        DocumentBuilder documentBuilder = DocumentHelper.newDocumentBuilder();
        assertNotSame(documentBuilder, DocumentHelper.newDocumentBuilder());
        assertTrue(documentBuilder.isNamespaceAware());
        assertFalse(documentBuilder.isValidating());
        documentBuilder.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testCreatingManyDocumentBuilders() throws Exception {
        int limit = 1000;
        ArrayList<CompletableFuture<DocumentBuilder>> futures = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            futures.add(CompletableFuture.supplyAsync(DocumentHelper::newDocumentBuilder));
        }
        HashSet<DocumentBuilder> dbs = new HashSet<>();
        for(CompletableFuture<DocumentBuilder> future : futures) {
            DocumentBuilder documentBuilder = future.get(10, TimeUnit.SECONDS);
            assertTrue(documentBuilder.isNamespaceAware());
            dbs.add(documentBuilder);
        }
        assertEquals(limit, dbs.size());
    }

    @Test
    public void testDocumentBuilderFactory() throws Exception {
        try {
            assertTrue(DocumentHelper.documentBuilderFactory.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
            assertTrue(DocumentHelper.documentBuilderFactory.getFeature(POIXMLConstants.FEATURE_DISALLOW_DOCTYPE_DECL));
            assertFalse(DocumentHelper.documentBuilderFactory.getFeature(POIXMLConstants.FEATURE_LOAD_DTD_GRAMMAR));
            assertFalse(DocumentHelper.documentBuilderFactory.getFeature(POIXMLConstants.FEATURE_LOAD_EXTERNAL_DTD));
        } catch(AbstractMethodError e) {
            // ignore exceptions from old parsers that don't support this API (https://bz.apache.org/bugzilla/show_bug.cgi?id=62692)
        }
    }
}
