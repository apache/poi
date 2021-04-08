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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;

class TestXMLHelper {
    @Test
    void testDocumentBuilder() throws Exception {
        DocumentBuilder documentBuilder = XMLHelper.newDocumentBuilder();
        assertNotSame(documentBuilder, XMLHelper.newDocumentBuilder());
        assertTrue(documentBuilder.isNamespaceAware());
        assertFalse(documentBuilder.isValidating());
        documentBuilder.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void testCreatingManyDocumentBuilders() throws Exception {
        int limit = 1000;
        ArrayList<CompletableFuture<DocumentBuilder>> futures = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            futures.add(CompletableFuture.supplyAsync(XMLHelper::newDocumentBuilder));
        }
        HashSet<DocumentBuilder> dbs = new HashSet<>();
        for (CompletableFuture<DocumentBuilder> future : futures) {
            DocumentBuilder documentBuilder = future.get(10, TimeUnit.SECONDS);
            assertTrue(documentBuilder.isNamespaceAware());
            dbs.add(documentBuilder);
        }
        assertEquals(limit, dbs.size());
    }

    @Test
    void testDocumentBuilderFactory() throws Exception {
        try {
            DocumentBuilderFactory dbf = XMLHelper.getDocumentBuilderFactory();
            assertTrue(dbf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
            assertTrue(dbf.getFeature(XMLHelper.FEATURE_DISALLOW_DOCTYPE_DECL));
            assertFalse(dbf.getFeature(XMLHelper.FEATURE_LOAD_DTD_GRAMMAR));
            assertFalse(dbf.getFeature(XMLHelper.FEATURE_LOAD_EXTERNAL_DTD));
        } catch (AbstractMethodError e) {
            // ignore exceptions from old parsers that don't support this API (https://bz.apache.org/bugzilla/show_bug.cgi?id=62692)
        }
    }

    @Test
    void testXMLReader() throws Exception {
        XMLReader reader = XMLHelper.newXMLReader();
        assertNotSame(reader, XMLHelper.newXMLReader());
        try {
            assertTrue(reader.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
            assertFalse(reader.getFeature(XMLHelper.FEATURE_LOAD_DTD_GRAMMAR));
            assertFalse(reader.getFeature(XMLHelper.FEATURE_LOAD_EXTERNAL_DTD));
            // assertEquals(XMLHelper.IGNORING_ENTITY_RESOLVER, reader.getEntityResolver());
            assertNotNull(reader.getProperty(XMLHelper.PROPERTY_ENTITY_EXPANSION_LIMIT));
            assertEquals("1", reader.getProperty(XMLHelper.PROPERTY_ENTITY_EXPANSION_LIMIT));
            assertNotNull(reader.getProperty(XMLHelper.PROPERTY_SECURITY_MANAGER));
        } catch (SAXNotRecognizedException e) {
            // ignore exceptions from old parsers that don't support these features
            // (https://bz.apache.org/bugzilla/show_bug.cgi?id=62692)
        }
        reader.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    void testCreatingManyXMLReaders() throws Exception {
        int limit = 1000;
        ArrayList<CompletableFuture<XMLReader>> futures = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return XMLHelper.newXMLReader();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        HashSet<XMLReader> readers = new HashSet<>();
        for (CompletableFuture<XMLReader> future : futures) {
            XMLReader reader = future.get(10, TimeUnit.SECONDS);
            try {
                assertTrue(reader.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
            } catch (SAXNotRecognizedException e) {
                // can happen for older XML Parsers, e.g. we have a CI Job which runs with Xerces XML Parser
                assertTrue(reader.getClass().getName().contains("org.apache.xerces"),
                    "Had Exception about not-recognized SAX feature: " + e + " which is only expected" +
                    " for Xerces XML Parser, but had parser: " + reader);
            }
            readers.add(reader);
        }
        assertEquals(limit, readers.size());
    }

    /**
     * test that newXMLInputFactory returns a factory with sensible defaults
     */
    @Test
    void testNewXMLInputFactory() {
        XMLInputFactory factory = XMLHelper.newXMLInputFactory();
        assertTrue((boolean)factory.getProperty(XMLInputFactory.IS_NAMESPACE_AWARE));
        assertFalse((boolean)factory.getProperty(XMLInputFactory.IS_VALIDATING));
        assertFalse((boolean)factory.getProperty(XMLInputFactory.SUPPORT_DTD));
        assertFalse((boolean)factory.getProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES));
    }

    /**
     * test that newXMLOutputFactory returns a factory with sensible defaults
     */
    @Test
    void testNewXMLOutputFactory() {
        XMLOutputFactory factory = XMLHelper.newXMLOutputFactory();
        assertTrue((boolean)factory.getProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES));
    }

    /**
     * test that newXMLEventFactory returns a factory
     */
    @Test
    void testNewXMLEventFactory() {
        assertNotNull(XMLHelper.newXMLEventFactory());
    }
}
