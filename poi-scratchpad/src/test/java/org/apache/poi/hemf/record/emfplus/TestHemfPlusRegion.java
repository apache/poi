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

package org.apache.poi.hemf.record.emfplus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.apache.poi.hemf.record.emfplus.HemfPlusRegion.EmfPlusRegion;
import org.apache.poi.hemf.record.emfplus.HemfPlusRegion.EmfPlusRegionNode;
import org.apache.poi.hemf.record.emfplus.HemfPlusRegion.EmfPlusRegionNodeDataType;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestHemfPlusRegion {

    @Test
    void readsNestedNodes() throws IOException {
        // AND(OR(EMPTY, INFINITE), EMPTY)
        byte[] data = regionData(
            EmfPlusRegionNodeDataType.AND.id,
            EmfPlusRegionNodeDataType.OR.id,
            EmfPlusRegionNodeDataType.EMPTY.id,
            EmfPlusRegionNodeDataType.INFINITE.id,
            EmfPlusRegionNodeDataType.EMPTY.id
        );

        EmfPlusRegion region = parse(data);
        EmfPlusRegionNode root = assertInstanceOf(EmfPlusRegionNode.class, region.getRegionNode());
        assertEquals(EmfPlusRegionNodeDataType.AND, root.getNodeType());
        EmfPlusRegionNode left = assertInstanceOf(EmfPlusRegionNode.class, root.getLeft());
        assertEquals(EmfPlusRegionNodeDataType.OR, left.getNodeType());
    }

    /**
     * Deeply nested region nodes are rejected instead of overflowing the stack (GitHub #1547)
     */
    @Test
    void rejectsDeeplyNestedNodes() {
        int[] nodes = new int[30_000];
        Arrays.fill(nodes, EmfPlusRegionNodeDataType.AND.id);
        byte[] data = regionData(nodes);

        RecordFormatException ex = assertThrows(RecordFormatException.class, () -> parse(data));
        assertEquals("Region nodes are nested deeper than 1000 levels", ex.getMessage());
    }

    /**
     * An unknown region node type is rejected instead of causing a NullPointerException
     */
    @Test
    void rejectsUnknownNodeType() {
        // AND with an unknown left child
        byte[] data = regionData(EmfPlusRegionNodeDataType.AND.id, 0x12345678);

        RecordFormatException ex = assertThrows(RecordFormatException.class, () -> parse(data));
        assertEquals("Unknown region node type 0x12345678", ex.getMessage());
    }

    private static byte[] regionData(int... nodeTypes) {
        byte[] data = new byte[8 + nodeTypes.length * 4];
        // EmfPlusGraphicsVersion: metafile signature 0xDBC01, graphics version 1
        LittleEndian.putInt(data, 0, 0xDBC01001);
        LittleEndian.putInt(data, 4, nodeTypes.length - 1);
        for (int i = 0; i < nodeTypes.length; i++) {
            LittleEndian.putInt(data, 8 + i * 4, nodeTypes[i]);
        }
        return data;
    }

    private static EmfPlusRegion parse(byte[] data) throws IOException {
        EmfPlusRegion region = new EmfPlusRegion();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            region.init(leis, data.length, EmfPlusObjectType.REGION, 0);
        }
        return region;
    }
}
