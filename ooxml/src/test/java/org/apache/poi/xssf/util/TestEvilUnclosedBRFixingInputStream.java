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

package org.apache.poi.xssf.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.ReplacingInputStream;
import org.junit.jupiter.api.Test;

public final class TestEvilUnclosedBRFixingInputStream {

    static class EvilUnclosedBRFixingInputStream extends ReplacingInputStream {
        public EvilUnclosedBRFixingInputStream(byte[] source) {
            super(new ByteArrayInputStream(source), "<br>", "<br/>");
        }
    }

    @Test
    void testOK() throws IOException {
        byte[] ok = getBytes("<p><div>Hello There!</div> <div>Tags!</div></p>");

        EvilUnclosedBRFixingInputStream inp = new EvilUnclosedBRFixingInputStream(ok);

        assertArrayEquals(ok, IOUtils.toByteArray(inp));
        inp.close();
    }

    @Test
    void testProblem() throws IOException {
        byte[] orig = getBytes("<p><div>Hello<br>There!</div> <div>Tags!</div></p>");
        byte[] fixed = getBytes("<p><div>Hello<br/>There!</div> <div>Tags!</div></p>");

        EvilUnclosedBRFixingInputStream inp = new EvilUnclosedBRFixingInputStream(orig);

        assertArrayEquals(fixed, IOUtils.toByteArray(inp));
        inp.close();
    }

    /**
     * Checks that we can copy with br tags around the buffer boundaries
     */
    @Test
    void testBufferSize() throws IOException {
        byte[] orig = getBytes("<p><div>Hello<br> <br>There!</div> <div>Tags!<br><br></div></p>");
        byte[] fixed = getBytes("<p><div>Hello<br/> <br/>There!</div> <div>Tags!<br/><br/></div></p>");

        // Vary the buffer size, so that we can end up with the br in the
        //  overflow or only part in the buffer
        for(int i=5; i<orig.length; i++) {
            EvilUnclosedBRFixingInputStream inp = new EvilUnclosedBRFixingInputStream(orig);

            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            boolean going = true;
            while(going) {
                byte[] b = new byte[i];
                int r = inp.read(b);
                if(r > 0) {
                    bout.write(b, 0, r);
                } else {
                    going = false;
                }
            }

            byte[] result = bout.toByteArray();
            assertArrayEquals(fixed, result);
            inp.close();
        }
    }

    private static byte[] getBytes(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }
}
