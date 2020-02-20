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

package org.apache.poi.hsmf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.exceptions.ChunkNotFoundException;

/**
 * Tests to verify that we can read properties identified by name or id in property sets.
 */
public class TestNameIdChunks {
    private static MAPIMessage keywordsMsg;

    /**
     * Initialize this test, load up the keywords.msg mapi message.
     * 
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        keywordsMsg = new MAPIMessage(samples.openResourceAsStream("keywords.msg"));
    }

    @AfterClass
    public static void tearDown() throws IOException {
        keywordsMsg.close();
    }

    /**
     * Test to see if we can read the keywords list from the msg.
     * The keywords property is a property identified by the name "Keywords" in the property set PS_PUBLIC_STRINGS.
     * 
     * @throws ChunkNotFoundException
     * 
     */
    @Test
    public void testReadKeywords() {
        long keywordsPropTag = keywordsMsg.getNameIdChunks().GetPropertyTag(NameIdChunks.PS_PUBLIC_STRINGS, "Keywords", 0);
        assertEquals(0x8003, keywordsPropTag);
        boolean keywordsfound = false;
        for (Map.Entry<MAPIProperty, List<Chunk>> chunk : keywordsMsg.getMainChunks().getAll().entrySet()) {
            if (chunk.getKey().id == keywordsPropTag) {
                keywordsfound = true;
                assertEquals(4, chunk.getValue().size());
                assertEquals("TODO", ((StringChunk)chunk.getValue().get(0)).getValue());
                assertEquals("Currently Important", ((StringChunk)chunk.getValue().get(1)).getValue());
                assertEquals("Currently To Do", ((StringChunk)chunk.getValue().get(2)).getValue());
                assertEquals("Test", ((StringChunk)chunk.getValue().get(3)).getValue());
            }
          }
        assertTrue(keywordsfound);
    }
    
    /**
     * Test to see if we can read the current version name from the msg.
     * The current version name property is a property identified by the id 0x8554 in the property set PSETID_Common.
     * 
     * @throws ChunkNotFoundException
     * 
     */
    @Test
    public void testCurrentVersionName() {
        boolean currentversionnamefound = false;
        long testPropTag = keywordsMsg.getNameIdChunks().GetPropertyTag(NameIdChunks.PSETID_Common, null, 0x8554);
        assertEquals(0x8006, testPropTag);
        for (Map.Entry<MAPIProperty, List<Chunk>> chunk : keywordsMsg.getMainChunks().getAll().entrySet()) {
            if (chunk.getKey().id == testPropTag) {
                currentversionnamefound = true;
                assertEquals("16.0", ((StringChunk)chunk.getValue().get(0)).getValue());
            }
        }
        assertTrue(currentversionnamefound);
    }
}
