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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import org.apache.poi.hemf.record.emfplus.HemfPlusBrush.EmfPlusBrush;
import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObject;
import org.apache.poi.hemf.record.emfplus.HemfPlusObject.EmfPlusObjectType;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestHemfPlusObject {

    @Test
    void rejectsUnknownObjectType() throws IOException {
        EmfPlusObject obj = new EmfPlusObject();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(new byte[0]))) {
            // object type 0x7F isn't defined
            RecordFormatException ex = assertThrows(RecordFormatException.class,
                () -> obj.init(leis, 0, HemfPlusRecordType.object.id, 0x7F00));
            assertEquals("Unknown EMF+ object type 127", ex.getMessage());
        }
    }

    @Test
    void unknownBrushTypeFailsWhenUsed() throws IOException {
        byte[] data = new byte[8];
        // EmfPlusGraphicsVersion: metafile signature 0xDBC01, graphics version 1
        LittleEndian.putInt(data, 0, 0xDBC01001);
        LittleEndian.putInt(data, 4, 0x7FFF);

        EmfPlusBrush brush = new EmfPlusBrush();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            // parsing keeps working, so the rest of the picture can still be read
            brush.init(leis, data.length, EmfPlusObjectType.BRUSH, 0);
        }
        assertNull(brush.getGenericRecordType());
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            () -> brush.getBrushData(Collections.emptyList()));
        assertEquals("Unknown brush type", ex.getMessage());
    }
}
