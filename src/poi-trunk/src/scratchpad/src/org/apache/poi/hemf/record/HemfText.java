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

package org.apache.poi.hemf.record;

import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInputStream;
import org.apache.poi.util.RecordFormatException;

/**
 * Container class to gather all text-related commands
 * This is starting out as read only, and very little is actually
 * implemented at this point!
 */
@Internal
public class HemfText {

    private static final int MAX_RECORD_LENGTH = 1_000_000;

    public static class ExtCreateFontIndirectW extends UnimplementedHemfRecord {
    }

    public static class ExtTextOutA implements HemfRecord {

        private long left,top,right,bottom;

        //TODO: translate this to a graphicsmode enum
        private long graphicsMode;

        private long exScale;
        private long eyScale;
        EmrTextObject textObject;

        @Override
        public HemfRecordType getRecordType() {
            return HemfRecordType.exttextouta;
        }

        @Override
        public long init(LittleEndianInputStream leis, long recordId, long recordSize) throws IOException {
            //note that the first 2 uInts have been read off and the recordsize has
            //been decreased by 8
            left = leis.readInt();
            top = leis.readInt();
            right = leis.readInt();
            bottom = leis.readInt();
            graphicsMode = leis.readUInt();
            exScale = leis.readUInt();
            eyScale = leis.readUInt();

            int recordSizeInt = -1;
            if (recordSize < Integer.MAX_VALUE) {
                recordSizeInt = (int)recordSize;
            } else {
                throw new RecordFormatException("can't have text length > Integer.MAX_VALUE");
            }
            //guarantee to read the rest of the EMRTextObjectRecord
            //emrtextbytes start after 7*4 bytes read above
            byte[] emrTextBytes = IOUtils.safelyAllocate(recordSizeInt-(7*LittleEndian.INT_SIZE), MAX_RECORD_LENGTH);
            IOUtils.readFully(leis, emrTextBytes);
            textObject = new EmrTextObject(emrTextBytes, getEncodingHint(), 20);//should be 28, but recordSizeInt has already subtracted 8
            return recordSize;
        }

        protected Charset getEncodingHint() {
            return null;
        }

        /**
         *
         * To be implemented!  We need to get the current character set
         * from the current font for {@link ExtTextOutA},
         * which has to be tracked in the playback device.
         *
         * For {@link ExtTextOutW}, the charset is "UTF-16LE"
         *
         * @param charset the charset to be used to decode the character bytes
         * @return text from this text element
         * @throws IOException
         */
        public String getText(Charset charset) throws IOException {
            return textObject.getText(charset);
        }

        /**
         *
         * @return the x offset for the EmrTextObject
         */
        public long getX() {
            return textObject.x;
        }

        /**
         *
         * @return the y offset for the EmrTextObject
         */
        public long getY() {
            return textObject.y;
        }

        public long getLeft() {
            return left;
        }

        public long getTop() {
            return top;
        }

        public long getRight() {
            return right;
        }

        public long getBottom() {
            return bottom;
        }

        public long getGraphicsMode() {
            return graphicsMode;
        }

        public long getExScale() {
            return exScale;
        }

        public long getEyScale() {
            return eyScale;
        }

    }

    public static class ExtTextOutW extends ExtTextOutA {

        @Override
        public HemfRecordType getRecordType() {
            return HemfRecordType.exttextoutw;
        }

        @Override
        protected Charset getEncodingHint() {
            return UTF_16LE;
        }

        public String getText() throws IOException {
            return getText(UTF_16LE);
        }
    }

    /**
     * Needs to be implemented.  Couldn't find example.
     */
    public static class PolyTextOutA extends UnimplementedHemfRecord {

    }

    /**
     * Needs to be implemented.  Couldn't find example.
     */
    public static class PolyTextOutW extends UnimplementedHemfRecord {

    }

    public static class SetTextAlign extends UnimplementedHemfRecord {
    }

    public static class SetTextColor extends UnimplementedHemfRecord {
    }


    public static class SetTextJustification extends UnimplementedHemfRecord {

    }

    private static class EmrTextObject {
        long x;
        long y;
        int numChars;
        byte[] rawTextBytes;//this stores _all_ of the bytes to the end of the EMRTextObject record.
                            //Because of potential variable length encodings, must
                            //carefully read only the numChars from this byte array.

        EmrTextObject(byte[] emrTextObjBytes, Charset charsetHint, int readSoFar) throws IOException {

            int offset = 0;
            x = LittleEndian.getUInt(emrTextObjBytes, offset); offset+= LittleEndian.INT_SIZE;
            y = LittleEndian.getUInt(emrTextObjBytes, offset); offset+= LittleEndian.INT_SIZE;
            long numCharsLong = LittleEndian.getUInt(emrTextObjBytes, offset); offset += LittleEndian.INT_SIZE;
            long offString = LittleEndian.getUInt(emrTextObjBytes, offset); offset += LittleEndian.INT_SIZE;
            int start = (int)offString-offset-readSoFar;

            if (numCharsLong == 0) {
                rawTextBytes = new byte[0];
                numChars = 0;
                return;
            }
            if (numCharsLong > Integer.MAX_VALUE) {
                throw new RecordFormatException("Number of characters can't be > Integer.MAX_VALUE");
            } else if (numCharsLong < 0) {
                throw new RecordFormatException("Number of characters can't be < 0");
            }

            numChars = (int)numCharsLong;
            rawTextBytes = IOUtils.safelyAllocate(emrTextObjBytes.length-start, MAX_RECORD_LENGTH);
            System.arraycopy(emrTextObjBytes, start, rawTextBytes, 0, emrTextObjBytes.length-start);
        }

        String getText(Charset charset) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (Reader r = new InputStreamReader(new ByteArrayInputStream(rawTextBytes), charset)) {
                for (int i = 0; i < numChars; i++) {
                    sb.appendCodePoint(readCodePoint(r));
                }
            }
            return sb.toString();
        }

        //TODO: move this to IOUtils?
        private int readCodePoint(Reader r) throws IOException {
            int c1 = r.read();
            if (c1 == -1) {
                throw new EOFException("Tried to read beyond byte array");
            }
            if (!Character.isHighSurrogate((char)c1)) {
                return c1;
            }
            int c2 = r.read();
            if (c2 == -1) {
                throw new EOFException("Tried to read beyond byte array");
            }
            if (!Character.isLowSurrogate((char)c2)) {
                throw new RecordFormatException("Expected low surrogate after high surrogate");
            }
            return Character.toCodePoint((char)c1, (char)c2);
        }
    }


}
