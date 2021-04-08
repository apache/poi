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

package org.apache.poi.hwpf.util;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Set;

/**
 * Utilities for working with double byte CodePages.
 *
 * <p>Provides constants for understanding numeric codepages,
 *  along with utilities to translate these into Java Character Sets.</p>
 */
public class DoubleByteUtil
{

    public static final Charset BIG5 = Charset.forName("Big5");

    public static final Set<Charset> DOUBLE_BYTE_CHARSETS = Collections.singleton(BIG5);

    /**
     * This tries to convert a LE byte array in cp950
     * (Microsoft's dialect of Big5) to a String.
     * We know MS zero-padded ascii, and we drop those.
     * There may be areas for improvement in this.
     *
     * @param data
     * @param offset
     * @param lengthInBytes
     * @return Decoded String
     */
    public static String cp950ToString(byte[] data, int offset, int lengthInBytes) {
        StringBuilder sb = new StringBuilder();
        LittleEndianCP950Reader reader = new LittleEndianCP950Reader(data, offset, lengthInBytes);
        int c = reader.read();
        while (c != -1) {
            sb.append((char)c);
            c = reader.read();
        }
        reader.close();
        return sb.toString();
    }
}
