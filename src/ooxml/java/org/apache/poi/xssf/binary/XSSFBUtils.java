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

package org.apache.poi.xssf.binary;


import java.nio.charset.StandardCharsets;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * @since 3.16-beta3
 */
@Internal
public class XSSFBUtils {

    /**
     * Reads an XLNullableWideString.
     * @param data data from which to read
     * @param offset in data from which to start
     * @param sb buffer to which to write.  You must setLength(0) before calling!
     * @return number of bytes read
     * @throws XSSFBParseException if there was an exception during reading
     */
    static int readXLNullableWideString(byte[] data, int offset, StringBuilder sb) throws XSSFBParseException {
        long numChars = LittleEndian.getUInt(data, offset);
        if (numChars < 0) {
            throw new XSSFBParseException("too few chars to read");
        } else if (numChars == 0xFFFFFFFFL) { //this means null value (2.5.166), do not read any bytes!!!
            return 0;
        } else if (numChars > 0xFFFFFFFFL) {
            throw new XSSFBParseException("too many chars to read");
        }

        int numBytes = 2*(int)numChars;
        offset += 4;
        if (offset+numBytes > data.length) {
            throw new XSSFBParseException("trying to read beyond data length: " +
             "offset="+offset+", numBytes="+numBytes+", data.length="+data.length);
        }
        sb.append(new String(data, offset, numBytes, StandardCharsets.UTF_16LE));
        numBytes+=4;
        return numBytes;
    }


    /**
     * Reads an XLNullableWideString.
     * @param data data from which to read
     * @param offset in data from which to start
     * @param sb buffer to which to write.  You must setLength(0) before calling!
     * @return number of bytes read
     * @throws XSSFBParseException if there was an exception while trying to read the string
     */
    public static int readXLWideString(byte[] data, int offset, StringBuilder sb) throws XSSFBParseException {
        long numChars = LittleEndian.getUInt(data, offset);
        if (numChars < 0) {
            throw new XSSFBParseException("too few chars to read");
        } else if (numChars > 0xFFFFFFFFL) {
            throw new XSSFBParseException("too many chars to read");
        }
        int numBytes = 2*(int)numChars;
        offset += 4;
        if (offset+numBytes > data.length) {
            throw new XSSFBParseException("trying to read beyond data length");
        }
        sb.append(new String(data, offset, numBytes, StandardCharsets.UTF_16LE));
        numBytes+=4;
        return numBytes;
    }

    static int castToInt(long val) {
        if (val < Integer.MAX_VALUE && val > Integer.MIN_VALUE) {
            return (int)val;
        }
        throw new POIXMLException("val ("+val+") can't be cast to int");
    }

    static short castToShort(int val) {
        if (val < Short.MAX_VALUE && val > Short.MIN_VALUE) {
            return (short)val;
        }
        throw new POIXMLException("val ("+val+") can't be cast to short");

    }

    //TODO: move to LittleEndian?
    static int get24BitInt( byte[] data, int offset) {
        int i = offset;
        int b0 = data[i++] & 0xFF;
        int b1 = data[i++] & 0xFF;
        int b2 = data[i] & 0xFF;
        return ( b2 << 16 ) + ( b1 << 8 ) + b0;
    }
}
