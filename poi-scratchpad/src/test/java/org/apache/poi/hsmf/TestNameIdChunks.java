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
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.NameIdChunks;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.datatypes.Types;
import org.apache.poi.util.LittleEndian;
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

    /**
     * A string named property whose StringOffset leaves no room for the
     * 4-byte name length field in the string stream must be treated as
     * not found rather than read past the end of the stream.
     */
    @Test
    void testStringOffsetWithoutRoomForNameLength() {
        for (int nameOffset : new int[] { 13, 14, 15 }) {
            NameIdChunks nameIdChunks = createNameIdChunks(stringEntry(nameOffset), new byte[16]);
            assertEquals(0, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), "Test", 0),
                    "name offset " + nameOffset);
        }
    }

    /**
     * StringOffset is an unsigned 32-bit value. An offset of 0x80000000 or
     * above must not be narrowed to a negative int and used to index the
     * string stream.
     */
    @Test
    void testOversizedStringOffset() {
        for (long nameOffset : new long[] { 0x80000000L, 0xFFFFFFFFL }) {
            NameIdChunks nameIdChunks = createNameIdChunks(stringEntry((int) nameOffset), new byte[16]);
            assertEquals(0, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), "Test", 0),
                    "name offset " + nameOffset);
        }
    }

    /**
     * A zero-length name whose length field occupies the last four bytes of
     * the string stream is still in bounds.
     */
    @Test
    void testEmptyNameAtEndOfStringStream() {
        NameIdChunks nameIdChunks = createNameIdChunks(stringEntry(12), new byte[16]);
        assertEquals(0, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), "Test", 0));
    }

    private static byte[] stringEntry(int nameOffset) {
        byte[] entry = new byte[8];
        LittleEndian.putInt(entry, 0, nameOffset);
        // guid index 2 (PS_PUBLIC_STRINGS) with the string named property bit set
        LittleEndian.putUShort(entry, 4, (2 << 1) | 1);
        LittleEndian.putUShort(entry, 6, 0);
        return entry;
    }

    private static NameIdChunks createNameIdChunks(byte[] entryStreamBytes, byte[] stringStreamBytes) {
        NameIdChunks nameIdChunks = new NameIdChunks();
        ByteChunk guidStream = new ByteChunk(2, Types.BINARY);
        guidStream.setValue(new byte[0]);
        ByteChunk entryStream = new ByteChunk(3, Types.BINARY);
        entryStream.setValue(entryStreamBytes);
        ByteChunk stringStream = new ByteChunk(4, Types.BINARY);
        stringStream.setValue(stringStreamBytes);
        nameIdChunks.record(guidStream);
        nameIdChunks.record(entryStream);
        nameIdChunks.record(stringStream);
        return nameIdChunks;
    }
}
