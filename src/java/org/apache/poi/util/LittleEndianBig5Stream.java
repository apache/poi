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

package org.apache.poi.util;

import java.io.ByteArrayInputStream;

/**
 * Stream that converts MSOffice's way of storing Big5, with
 * zero-byte padding for ASCII and in LittleEndianOrder.
 */
@Internal
public class LittleEndianBig5Stream extends ByteArrayInputStream {
    private static final int EOF = -1;
    private static final int INVALID_PAIR = -2;
    private static final int EMPTY_TRAILING = -3;

    //the char that is logically trailing in Big5 encoding
    //however in LittleEndian order, this is the first encountered.
    int trailing = EMPTY_TRAILING;
    public LittleEndianBig5Stream(byte[] buf) {
        super(buf);
    }

    public LittleEndianBig5Stream(byte[] buf, int offset, int length) {
        super(buf, offset, length);
    }

    @Override
    public int read() {

        if (trailing != EMPTY_TRAILING) {
            int tmp = trailing;
            trailing = EMPTY_TRAILING;
            return tmp;
        }
        int leading = readNext();
        while (leading == INVALID_PAIR) {
            leading = readNext();
        }

        if (leading == EOF) {
            return EOF;
        }
        return leading;
    }

    //returns leading, sets trailing appropriately
    //returns -1 if it hits the end of the stream
    //returns -2 for an invalid big5 code pair
    private final int readNext() {
        trailing = super.read();
        if (trailing == -1) {
            return EOF;
        }
        int leading = super.read();
        if (leading == EOF) {
            return EOF;
        }
        int lead = leading&0xff;
        if (lead > 0x80) {
            return leading;
        } else if (lead == 0) {
            int ret = trailing;
            trailing = EMPTY_TRAILING;
            return ret;
        } else {
            int ret = trailing;
            trailing = EMPTY_TRAILING;
            return ret;
            //return INVALID_PAIR;
        }

    }

    @Override
    public int read(byte[] buff, int off, int len) {
        int bytesRead = 0;
        for (int i = off; i < off+len; i++) {
            int b = read();
            if (b == -1) {
                if (bytesRead == 0) {
                    return -1;
                } else {
                    return bytesRead;
                }
            }
            bytesRead++;
            buff[i] = (byte)b;
        }
        return bytesRead;
    }
}
