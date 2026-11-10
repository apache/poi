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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collections;

import org.apache.poi.hemf.record.emfplus.HemfPlusBrush.EmfPlusBrush;
import org.apache.poi.hemf.record.emfplus.HemfPlusDraw.EmfPlusDrawPath;
import org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImage;
import org.apache.poi.hemf.record.emfplus.HemfPlusImage.EmfPlusImageDataType;
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

    @Test
    void readsUnknownImageType() throws IOException {
        byte[] data = new byte[12];
        // EmfPlusGraphicsVersion: metafile signature 0xDBC01, graphics version 1
        LittleEndian.putInt(data, 0, 0xDBC01001);
        // image data type 0x7F isn't defined
        LittleEndian.putInt(data, 4, 0x7F);
        LittleEndian.putInt(data, 8, 0x04030201);

        EmfPlusImage img = new EmfPlusImage();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            // the image data is what follows the version and type, not the whole record
            assertEquals(data.length, img.init(leis, data.length, EmfPlusObjectType.IMAGE, 0));
        }
        assertEquals(EmfPlusImageDataType.UNKNOWN, img.getImageDataType());
        assertArrayEquals(new byte[]{1, 2, 3, 4}, img.getImageData());
    }

    @Test
    void rejectsRecordReadingPastItsSize() {
        byte[] data = new byte[16];
        LittleEndian.putUShort(data, 0, (int) HemfPlusRecordType.drawPath.id);
        // the record size only covers the header, but DrawPath reads a 4 byte pen id
        LittleEndian.putInt(data, 4, 12);
        LittleEndian.putInt(data, 8, 4);

        RecordFormatException ex = assertThrows(RecordFormatException.class,
            () -> new HemfPlusRecordIterator(new LittleEndianInputStream(new ByteArrayInputStream(data))));
        assertEquals("Record limit exceeded - readBytes: 4 / remBytes: 0", ex.getMessage());
    }

    @Test
    void readsPenIdOutsideTheSpec() throws IOException {
        byte[] data = new byte[4];
        // the spec allows 0 to 63, which parsing only warns about
        LittleEndian.putInt(data, 0, 100);

        EmfPlusDrawPath drawPath = new EmfPlusDrawPath();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            drawPath.init(leis, data.length, HemfPlusRecordType.drawPath.id, 0);
        }
        assertEquals(100, drawPath.getPenId());
    }
}
