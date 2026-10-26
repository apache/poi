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

package org.apache.poi.hemf.record.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.poi.hemf.record.emf.HemfComment.EmfComment;
import org.apache.poi.hemf.record.emf.HemfComment.EmfCommentDataWMF;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;
import org.junit.jupiter.api.Test;

class TestHemfComment {

    /** EMR_COMMENT body (after type and size) holding a public EMR_COMMENT_WINDOWS_METAFILE of wmfSize bytes */
    private static byte[] wmfComment(int wmfSize) {
        byte[] data = new byte[28 + wmfSize];
        LittleEndian.putInt(data, 0, 24 + wmfSize);       // dataSize
        LittleEndian.putInt(data, 4, 0x43494447);         // EMR_COMMENT_PUBLIC
        LittleEndian.putInt(data, 8, 0x80000001);         // EMR_COMMENT_WINDOWS_METAFILE
        LittleEndian.putUInt(data, 24, wmfSize);          // WinMetafileSize
        return data;
    }

    private static EmfComment parse(byte[] data) throws IOException {
        EmfComment comment = new EmfComment();
        try (LittleEndianInputStream leis = new LittleEndianInputStream(new ByteArrayInputStream(data))) {
            comment.init(leis, data.length, HemfRecordType.comment.id);
        }
        return comment;
    }

    /**
     * Comment records carry whole embedded metafiles, which are often a few MB; a 1 MB default
     * dropped them, so the default matches HwmfPicture's.
     */
    @Test
    void embeddedWmfOverOneMegabyte() throws IOException {
        int wmfSize = 2_000_000;
        EmfComment comment = parse(wmfComment(wmfSize));
        assertEquals(wmfSize, ((EmfCommentDataWMF) comment.getCommentData()).getWMFData().length);
    }

    @Test
    void maxRecordLengthIsEnforced() {
        int saved = HemfComment.getMaxRecordLength();
        try {
            HemfComment.setMaxRecordLength(1000);
            assertThrows(RecordFormatException.class, () -> parse(wmfComment(2000)));
        } finally {
            HemfComment.setMaxRecordLength(saved);
        }
    }
}
