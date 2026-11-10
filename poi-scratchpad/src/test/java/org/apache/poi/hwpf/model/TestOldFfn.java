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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public final class TestOldFfn {

    /**
     * A font description length of 0x80 or more must be read as an unsigned byte.
     * When read as signed it goes negative, the alternate-font-name block is skipped
     * and the computed record length is wrong, desyncing the rest of the font table.
     */
    @Test
    void readsLongFontDescriptionLengthUnsigned() {
        final byte[] mainName = "A".repeat(140).getBytes(StandardCharsets.US_ASCII);
        final byte[] altName = "Alt".getBytes(StandardCharsets.US_ASCII);

        // record layout: 1 length byte + 3 unknown + 1 chs + 1 unknown
        // + zero-terminated main name + zero-terminated alt name
        final int descLength = 6 + mainName.length + 1 + altName.length + 1; // == 151
        final byte[] buf = new byte[descLength + 1]; // one trailing byte of table slack
        int p = 0;
        buf[p++] = (byte) descLength; // 0x97, negative when read as a signed byte
        p += 3;                       // unknown bytes
        buf[p++] = 0;                 // chs (ANSI)
        p += 1;                       // unknown byte
        System.arraycopy(mainName, 0, buf, p, mainName.length);
        p += mainName.length;
        buf[p++] = 0;
        System.arraycopy(altName, 0, buf, p, altName.length);
        p += altName.length;
        buf[p] = 0;

        OldFfn ffn = OldFfn.build(buf, 0, buf.length);
        assertNotNull(ffn);
        assertEquals("A".repeat(140), ffn.getMainFontName());
        assertEquals("Alt", ffn.getAltFontName());
        assertEquals(descLength, ffn.getLength());
    }
}
