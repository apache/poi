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
import java.io.IOException;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;

/**
 * Stream that converts CP950 (MSOffice's dialect of Big5), with
 * zero-byte padding for ASCII and in LittleEndianOrder.
 */
@Internal
public class LittleEndianCP950Reader extends Reader {

    private static final POILogger LOGGER = POILogFactory.getLogger(LittleEndianCP950Reader.class);


    private static final char UNMAPPABLE = '?';
    private final ByteBuffer doubleByteBuffer = ByteBuffer.allocate(2);
    private final CharBuffer charBuffer = CharBuffer.allocate(2);
    private final CharsetDecoder decoder = StringUtil.BIG5.newDecoder();

    //https://en.wikipedia.org/wiki/Code_page_950
    //see private use area
    private final static char range1Low = '\u8140';
    private final static char range1High = '\u8DFE';
    private final static char range2Low = '\u8E40';
    private final static char range2High = '\uA0FE';
    private final static char range3Low = '\uC6A1';
    private final static char range3High = '\uC8FE';
    private final static char range4Low = '\uFA40';
    private final static char range4High = '\uFEFE';

    private final byte[] data;
    private final int startOffset;
    private final int length;
    private int offset;
    private int trailing;
    private int leading;
    int cnt;
    //the char that is logically trailing in Big5 encoding
    //however in LittleEndian order, this is the first encountered.
    public LittleEndianCP950Reader(byte[] data) {
        this(data, 0, data.length);
    }

    public LittleEndianCP950Reader(byte[] data, int offset, int length) {
        this.data = data;
        this.startOffset = offset;
        this.offset = startOffset;
        this.length = length;
    }

    @Override
    public int read() {
        if (offset + 1 > data.length || offset - startOffset > length) {
            return -1;
        }
        trailing = data[offset++] & 0xff;
        leading = data[offset++] & 0xff;
        decoder.reset();
        if (leading < 0x81) {
            //return trailing alone
            //there may be some subtleties here
            return trailing;
        } else if (leading == 0xf9) {
            return handleF9(trailing);
        } else {
            int ch = (leading << 8) + trailing;
            if (ch >= range1Low && ch <= range1High) {
                return handleRange1(leading, trailing);
            } else if (ch >= range2Low && ch <= range2High) {
                return handleRange2(leading, trailing);
            } else if (ch >= range3Low && ch <= range3High) {
                return handleRange3(leading, trailing);
            } else if (ch >= range4Low && ch <= range4High) {
                return handleRange4(leading, trailing);
            }

            charBuffer.clear();
            doubleByteBuffer.clear();
            doubleByteBuffer.put((byte) leading);
            doubleByteBuffer.put((byte) trailing);
            doubleByteBuffer.flip();
            decoder.decode(doubleByteBuffer, charBuffer, true);
            charBuffer.flip();

            if (charBuffer.length() == 0) {
                LOGGER.log(POILogger.WARN, "couldn't create char for: "
                        + Integer.toString((leading & 0xff), 16)
                        + " " + Integer.toString((trailing & 0xff), 16));
                return UNMAPPABLE;
            } else {
                return Character.codePointAt(charBuffer, 0);
            }
        }


    }


    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        //there may be some efficiencies, but this should do for now.

        for (int i = off; i < off + len; i++) {
            int c = read();
            if (c == -1) {
                return i - off;
            }
            cbuf[i] = (char) c;
        }
        return len;
    }

    @Override
    public void close() {
    }

    private int handleRange1(int leading, int trailing) {
        return (0xeeb8 + (157 * (leading - 0x81))) +
                ((trailing < 0x80) ? trailing - 0x40 : trailing - 0x62);
    }

    private int handleRange2(int leading, int trailing) {
        return (0xe311 + (157 * (leading - 0x8e))) +
                ((trailing < 0x80) ? trailing - 0x40 : trailing - 0x62);
    }

    private int handleRange3(int leading, int trailing) {
        return (0xf672 + (157 * (leading - 0xc6))) +
                ((trailing < 0x80) ? trailing - 0x40 : trailing - 0x62);
    }

    private int handleRange4(int leading, int trailing) {
        return (0xe000 + (157 * (leading - 0xfa))) +
                ((trailing < 0x80) ? trailing - 0x40 : trailing - 0x62);
    }

    private int handleF9(int trailing) {
        switch (trailing) {
            case 0x40:
                return 0x7e98;
            case 0x41:
                return 0x7e9b;
            case 0x42:
                return 0x7e99;
            case 0x43:
                return 0x81e0;
            case 0x44:
                return 0x81e1;
            case 0x45:
                return 0x8646;
            case 0x46:
                return 0x8647;
            case 0x47:
                return 0x8648;
            case 0x48:
                return 0x8979;
            case 0x49:
                return 0x897a;
            case 0x4a:
                return 0x897c;
            case 0x4b:
                return 0x897b;
            case 0x4c:
                return 0x89ff;
            case 0x4d:
                return 0x8b98;
            case 0x4e:
                return 0x8b99;
            case 0x4f:
                return 0x8ea5;
            case 0x50:
                return 0x8ea4;
            case 0x51:
                return 0x8ea3;
            case 0x52:
                return 0x946e;
            case 0x53:
                return 0x946d;
            case 0x54:
                return 0x946f;
            case 0x55:
                return 0x9471;
            case 0x56:
                return 0x9473;
            case 0x57:
                return 0x9749;
            case 0x58:
                return 0x9872;
            case 0x59:
                return 0x995f;
            case 0x5a:
                return 0x9c68;
            case 0x5b:
                return 0x9c6e;
            case 0x5c:
                return 0x9c6d;
            case 0x5d:
                return 0x9e0b;
            case 0x5e:
                return 0x9e0d;
            case 0x5f:
                return 0x9e10;
            case 0x60:
                return 0x9e0f;
            case 0x61:
                return 0x9e12;
            case 0x62:
                return 0x9e11;
            case 0x63:
                return 0x9ea1;
            case 0x64:
                return 0x9ef5;
            case 0x65:
                return 0x9f09;
            case 0x66:
                return 0x9f47;
            case 0x67:
                return 0x9f78;
            case 0x68:
                return 0x9f7b;
            case 0x69:
                return 0x9f7a;
            case 0x6a:
                return 0x9f79;
            case 0x6b:
                return 0x571e;
            case 0x6c:
                return 0x7066;
            case 0x6d:
                return 0x7c6f;
            case 0x6e:
                return 0x883c;
            case 0x6f:
                return 0x8db2;
            case 0x70:
                return 0x8ea6;
            case 0x71:
                return 0x91c3;
            case 0x72:
                return 0x9474;
            case 0x73:
                return 0x9478;
            case 0x74:
                return 0x9476;
            case 0x75:
                return 0x9475;
            case 0x76:
                return 0x9a60;
            case 0x77:
                return 0x9c74;
            case 0x78:
                return 0x9c73;
            case 0x79:
                return 0x9c71;
            case 0x7a:
                return 0x9c75;
            case 0x7b:
                return 0x9e14;
            case 0x7c:
                return 0x9e13;
            case 0x7d:
                return 0x9ef6;
            case 0x7e:
                return 0x9f0a;
            case 0xa1:
                return 0x9fa4;
            case 0xa2:
                return 0x7068;
            case 0xa3:
                return 0x7065;
            case 0xa4:
                return 0x7cf7;
            case 0xa5:
                return 0x866a;
            case 0xa6:
                return 0x883e;
            case 0xa7:
                return 0x883d;
            case 0xa8:
                return 0x883f;
            case 0xa9:
                return 0x8b9e;
            case 0xaa:
                return 0x8c9c;
            case 0xab:
                return 0x8ea9;
            case 0xac:
                return 0x8ec9;
            case 0xad:
                return 0x974b;
            case 0xae:
                return 0x9873;
            case 0xaf:
                return 0x9874;
            case 0xb0:
                return 0x98cc;
            case 0xb1:
                return 0x9961;
            case 0xb2:
                return 0x99ab;
            case 0xb3:
                return 0x9a64;
            case 0xb4:
                return 0x9a66;
            case 0xb5:
                return 0x9a67;
            case 0xb6:
                return 0x9b24;
            case 0xb7:
                return 0x9e15;
            case 0xb8:
                return 0x9e17;
            case 0xb9:
                return 0x9f48;
            case 0xba:
                return 0x6207;
            case 0xbb:
                return 0x6b1e;
            case 0xbc:
                return 0x7227;
            case 0xbd:
                return 0x864c;
            case 0xbe:
                return 0x8ea8;
            case 0xbf:
                return 0x9482;
            case 0xc0:
                return 0x9480;
            case 0xc1:
                return 0x9481;
            case 0xc2:
                return 0x9a69;
            case 0xc3:
                return 0x9a68;
            case 0xc4:
                return 0x9b2e;
            case 0xc5:
                return 0x9e19;
            case 0xc6:
                return 0x7229;
            case 0xc7:
                return 0x864b;
            case 0xc8:
                return 0x8b9f;
            case 0xc9:
                return 0x9483;
            case 0xca:
                return 0x9c79;
            case 0xcb:
                return 0x9eb7;
            case 0xcc:
                return 0x7675;
            case 0xcd:
                return 0x9a6b;
            case 0xce:
                return 0x9c7a;
            case 0xcf:
                return 0x9e1d;
            case 0xd0:
                return 0x7069;
            case 0xd1:
                return 0x706a;
            case 0xd2:
                return 0x9ea4;
            case 0xd3:
                return 0x9f7e;
            case 0xd4:
                return 0x9f49;
            case 0xd5:
                return 0x9f98;
            case 0xd6:
                return 0x7881;
            case 0xd7:
                return 0x92b9;
            case 0xd8:
                return 0x88cf;
            case 0xd9:
                return 0x58bb;
            case 0xda:
                return 0x6052;
            case 0xdb:
                return 0x7ca7;
            case 0xdc:
                return 0x5afa;
            case 0xdd:
                return 0x2554;
            case 0xde:
                return 0x2566;
            case 0xdf:
                return 0x2557;
            case 0xe0:
                return 0x2560;
            case 0xe1:
                return 0x256c;
            case 0xe2:
                return 0x2563;
            case 0xe3:
                return 0x255a;
            case 0xe4:
                return 0x2569;
            case 0xe5:
                return 0x255d;
            case 0xe6:
                return 0x2552;
            case 0xe7:
                return 0x2564;
            case 0xe8:
                return 0x2555;
            case 0xe9:
                return 0x255e;
            case 0xea:
                return 0x256a;
            case 0xeb:
                return 0x2561;
            case 0xec:
                return 0x2558;
            case 0xed:
                return 0x2567;
            case 0xee:
                return 0x255b;
            case 0xef:
                return 0x2553;
            case 0xf0:
                return 0x2565;
            case 0xf1:
                return 0x2556;
            case 0xf2:
                return 0x255f;
            case 0xf3:
                return 0x256b;
            case 0xf4:
                return 0x2562;
            case 0xf5:
                return 0x2559;
            case 0xf6:
                return 0x2568;
            case 0xf7:
                return 0x255c;
            case 0xf8:
                return 0x2551;
            case 0xf9:
                return 0x2550;
            case 0xfa:
                return 0x256d;
            case 0xfb:
                return 0x256e;
            case 0xfc:
                return 0x2570;
            case 0xfd:
                return 0x256f;
            case 0xfe:
                return 0x2593;
            default:
                LOGGER.log(POILogger.WARN, "couldn't create char for: f9"
                        + " " + Integer.toString((trailing & 0xff), 16));
                return UNMAPPABLE;
        }
    }
}
