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

import static org.apache.poi.hsmf.datatypes.NameIdChunks.PredefinedPropertySet.PSETID_COMMON;
import static org.apache.poi.hsmf.datatypes.NameIdChunks.PropertySetType.PS_PUBLIC_STRINGS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.POIDataSamples;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests to verify that we can read properties identified by name or id in property sets.
 */
public class TestNameIdChunks {
    private static MAPIMessage keywordsMsg;

    /**
     * Initialize this test, load up the keywords.msg mapi message.
     */
    @BeforeAll
    public static void setUp() throws IOException {
        POIDataSamples samples = POIDataSamples.getHSMFInstance();
        try (InputStream is = samples.openResourceAsStream("keywords.msg")) {
            keywordsMsg = new MAPIMessage(is);
        }
    }

    @AfterAll
    public static void tearDown() throws IOException {
        keywordsMsg.close();
    }

    /**
     * Test to see if we can read the keywords list from the msg.
     * The keywords property is a property identified by the name "Keywords" in the property set PS_PUBLIC_STRINGS.
     */
    @Test
    void testReadKeywords() {
        long keywordsPropTag = keywordsMsg.getNameIdChunks().getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), "Keywords", 0);
        assertEquals(0x8003, keywordsPropTag);
        String[] exp = { "TODO", "Currently Important", "Currently To Do", "Test" };
        String[] act = getValues(keywordsPropTag);
        assertArrayEquals(exp, act);
    }

    /**
     * Test to see if we can read the current version name from the msg.
     * The current version name property is a property identified by the id 0x8554 in the property set PSETID_Common.
     */
    @Test
    void testCurrentVersionName() {
        long testPropTag = keywordsMsg.getNameIdChunks().getPropertyTag(PSETID_COMMON.getClassID(), null, 0x8554);
        assertEquals(0x8006, testPropTag);
        String[] exp = { "16.0" };
        String[] act = getValues(testPropTag);
        assertArrayEquals(exp, act);
    }

    private String[] getValues(long tag) {
        return keywordsMsg.getMainChunks().getAll().entrySet().stream()
            .filter(me -> me.getKey().id == tag)
            .flatMap(me -> me.getValue().stream())
            .map(c -> ((StringChunk)c).getValue())
            .toArray(String[]::new);
    }
}
