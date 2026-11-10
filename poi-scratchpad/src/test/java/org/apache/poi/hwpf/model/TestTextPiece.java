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
package org.apache.poi.hwpf.model;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

final class TestTextPiece {
    /**
     * A TextPiece and an OldTextPiece over the same text are not equal, either way round
     */
    @Test
    void equalsIsSymmetricWithOldTextPiece() {
        byte[] text = "abc".getBytes(ISO_8859_1);
        // 8 byte piece descriptor, file offset with the "compressed" bit set
        byte[] pdBytes = {0, 0, 0, 0, 0, 0x40, 0, 0};
        TextPiece piece = new TextPiece(0, 3, text, new PieceDescriptor(pdBytes, 0));
        OldTextPiece oldPiece = new OldTextPiece(0, 3, text, new PieceDescriptor(pdBytes, 0));

        assertEquals(piece.equals(oldPiece), oldPiece.equals(piece));
        assertNotEquals(piece, oldPiece);
        assertEquals(piece, new TextPiece(piece));
        assertEquals(oldPiece, new OldTextPiece(oldPiece));
    }

    /**
     * hashCode used to fail an assertion when assertions are enabled
     */
    @Test
    void hashCodeIsConsistentWithEquals() {
        byte[] text = "abc".getBytes(ISO_8859_1);
        byte[] pdBytes = {0, 0, 0, 0, 0, 0x40, 0, 0};
        TextPiece piece = new TextPiece(0, 3, text, new PieceDescriptor(pdBytes, 0));
        assertEquals(piece.hashCode(), new TextPiece(piece).hashCode());

        OldTextPiece oldPiece = new OldTextPiece(0, 3, text, new PieceDescriptor(pdBytes, 0));
        assertEquals(oldPiece.hashCode(), new OldTextPiece(oldPiece).hashCode());
    }
}
