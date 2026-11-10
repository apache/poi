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
     * the string stream is still in bounds and resolves through its stream entry.
     */
    @Test
    void testEmptyNameAtEndOfStringStream() {
        NameIdChunks nameIdChunks = createNameIdChunks(stringEntry(12), new byte[16]);
        // CRC32 of the empty name is 0, so the stream ID is 0x1000 + (0 ^ ((2 << 1) | 1)) % 0x1F
        nameIdChunks.record(binaryChunk(0x1005, entry(0, 2, 1, 7)));
        assertEquals(0x8007, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), "", 0));
        assertEquals(0, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), "Test", 0));
    }

    /**
     * A numerical named property ID is an unsigned 32-bit value, so an ID of
     * 0x80000000 or above must still map to the right stream.
     */
    @Test
    void testHighNumericalPropertyId() {
        NameIdChunks nameIdChunks = createNameIdChunks(entry(0x80000000, 2, 0, 9), new byte[0]);
        // stream ID is 0x1000 + (0x80000000 ^ (2 << 1)) % 0x1F
        nameIdChunks.record(binaryChunk(0x1006, entry(0x80000000, 2, 0, 9)));
        assertEquals(0x8009, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), null, 0x80000000L));
    }

    /**
     * A string stream chunk without a value must not fail string named
     * property lookups, and must not affect numerical named property lookups.
     */
    @Test
    void testStringStreamWithoutValue() {
        byte[] entries = new byte[16];
        System.arraycopy(stringEntry(0), 0, entries, 0, 8);
        System.arraycopy(entry(0x8554, 2, 0, 3), 0, entries, 8, 8);
        NameIdChunks nameIdChunks = createNameIdChunks(new byte[0], entries, null);
        nameIdChunks.record(binaryChunk(0x1000 + (0x8554 ^ (2 << 1)) % 0x1F, entry(0x8554, 2, 0, 3)));
        assertEquals(0, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), "Test", 0));
        assertEquals(0x8003, nameIdChunks.getPropertyTag(PS_PUBLIC_STRINGS.getClassID(), null, 0x8554));
    }

    /**
     * A GUID stream chunk without a value must not fail lookups of property
     * sets that are stored in the GUID stream.
     */
    @Test
    void testGuidStreamWithoutValue() {
        NameIdChunks nameIdChunks = createNameIdChunks(null, entry(0x8554, 3, 0, 0), new byte[0]);
        assertEquals(0, nameIdChunks.getPropertyTag(PSETID_COMMON.getClassID(), null, 0x8554));
    }

    private static byte[] stringEntry(int nameOffset) {
        // guid index 2 (PS_PUBLIC_STRINGS) with the string named property bit set
        return entry(nameOffset, 2, 1, 0);
    }

    private static byte[] entry(int nameOffsetOrId, int guidIndex, int propertyKind, int propertyIndex) {
        byte[] entry = new byte[8];
        LittleEndian.putInt(entry, 0, nameOffsetOrId);
        LittleEndian.putUShort(entry, 4, (guidIndex << 1) | propertyKind);
        LittleEndian.putUShort(entry, 6, propertyIndex);
        return entry;
    }

    private static ByteChunk binaryChunk(int chunkId, byte[] value) {
        ByteChunk chunk = new ByteChunk(chunkId, Types.BINARY);
        chunk.setValue(value);
        return chunk;
    }

    private static NameIdChunks createNameIdChunks(byte[] entryStreamBytes, byte[] stringStreamBytes) {
        return createNameIdChunks(new byte[0], entryStreamBytes, stringStreamBytes);
    }

    private static NameIdChunks createNameIdChunks(byte[] guidStreamBytes, byte[] entryStreamBytes,
            byte[] stringStreamBytes) {
        NameIdChunks nameIdChunks = new NameIdChunks();
        nameIdChunks.record(binaryChunk(2, guidStreamBytes));
        nameIdChunks.record(binaryChunk(3, entryStreamBytes));
        nameIdChunks.record(binaryChunk(4, stringStreamBytes));
        return nameIdChunks;
    }
}
