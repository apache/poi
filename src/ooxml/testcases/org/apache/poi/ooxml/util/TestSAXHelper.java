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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;

import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;

public class TestSAXHelper {
    @Test
    public void testXMLReader() throws Exception {
        XMLReader reader = SAXHelper.newXMLReader();
        assertNotSame(reader, SAXHelper.newXMLReader());
        try {
            assertTrue(reader.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
            assertFalse(reader.getFeature(POIXMLConstants.FEATURE_LOAD_DTD_GRAMMAR));
            assertFalse(reader.getFeature(POIXMLConstants.FEATURE_LOAD_EXTERNAL_DTD));
            assertEquals(SAXHelper.IGNORING_ENTITY_RESOLVER, reader.getEntityResolver());
            assertNotNull(reader.getProperty(POIXMLConstants.PROPERTY_ENTITY_EXPANSION_LIMIT));
            assertEquals("1", reader.getProperty(POIXMLConstants.PROPERTY_ENTITY_EXPANSION_LIMIT));
            assertNotNull(reader.getProperty(POIXMLConstants.PROPERTY_SECURITY_MANAGER));
        } catch(SAXNotRecognizedException e) {
            // ignore exceptions from old parsers that don't support these features
            // (https://bz.apache.org/bugzilla/show_bug.cgi?id=62692)
        }
        reader.parse(new InputSource(new ByteArrayInputStream("<xml></xml>".getBytes(StandardCharsets.UTF_8))));
    }

    @Test
    public void testCreatingManyXMLReaders() throws Exception {
        int limit = 1000;
        ArrayList<CompletableFuture<XMLReader>> futures = new ArrayList<>();
        for(int i = 0; i < limit; i++) {
            futures.add(CompletableFuture.supplyAsync(() -> {
                try {
                    return SAXHelper.newXMLReader();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        HashSet<XMLReader> readers = new HashSet<>();
        for(CompletableFuture<XMLReader> future : futures) {
            XMLReader reader = future.get(10, TimeUnit.SECONDS);
            try {
                assertTrue(reader.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
            } catch (SAXNotRecognizedException e) {
                // can happen for older XML Parsers, e.g. we have a CI Job which runs with Xerces XML Parser
                assertTrue("Had Exception about not-recognized SAX feature: " + e + " which is only expected" +
                                " for Xerces XML Parser, but had parser: " + reader,
                        reader.getClass().getName().contains("org.apache.xerces"));
            }
            readers.add(reader);
        }
        assertEquals(limit, readers.size());
    }
}
