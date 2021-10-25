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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

/**
 * This class provides common functionality for the
 * various LZW implementations in the different file
 * formats.
 * It's currently used by HDGF and HMEF.
 * <p>
 * Two good resources on LZW are:
 * http://en.wikipedia.org/wiki/LZW
 * http://marknelson.us/1989/10/01/lzw-data-compression/
 */
public abstract class LZWDecompresser {

    /** the size of our dictionary */
    public static final int DICT_SIZE = 0x1000;
    /** the mask for calculating / wrapping dictionary offsets */
    public static final int DICT_MASK = 0xFFF;

    //arbitrarily selected; may need to increase
    private static final int DEFAULT_MAX_RECORD_LENGTH = 1_000_000;
    private static int MAX_RECORD_LENGTH = DEFAULT_MAX_RECORD_LENGTH;

    /**
     * Does the mask bit mean it's compressed or uncompressed?
     */
    private final boolean maskMeansCompressed;
    /**
     * How much to append to the code length in the stream
     * to get the real code length? Normally 2 or 3
     */
    private final int codeLengthIncrease;
    /**
     * Does the 12 bits of the position get stored in
     * Little Endian or Big Endian form?
     * This controls whether a pos+length of 0x12 0x34
     * becomes a position of 0x123 or 0x312
     */
    private final boolean positionIsBigEndian;

    /**
     * @param length the max record length allowed for LZWDecompresser
     */
    public static void setMaxRecordLength(int length) {
        MAX_RECORD_LENGTH = length;
    }

    /**
     * @return the max record length allowed for LZWDecompresser
     */
    public static int getMaxRecordLength() {
        return MAX_RECORD_LENGTH;
    }

    protected LZWDecompresser(boolean maskMeansCompressed,
                              int codeLengthIncrease, boolean positionIsBigEndian) {
        this.maskMeansCompressed = maskMeansCompressed;
        this.codeLengthIncrease = codeLengthIncrease;
        this.positionIsBigEndian = positionIsBigEndian;
    }

    /**
     * Populates the dictionary, and returns where in it
     * to begin writing new codes.
     * Generally, if the dictionary is pre-populated, then new
     * codes should be placed at the end of that block.
     * Equally, if the dictionary is left with all zeros, then
     * usually the new codes can go in at the start.
     */
    protected abstract int populateDictionary(byte[] dict);

    /**
     * Adjusts the position offset if needed when looking
     * something up in the dictionary.
     */
    protected abstract int adjustDictionaryOffset(int offset);

    /**
     * Decompresses the given input stream, returning the array of bytes
     * of the decompressed input.
     */
    public byte[] decompress(InputStream src) throws IOException {
        UnsynchronizedByteArrayOutputStream res = new UnsynchronizedByteArrayOutputStream();
        decompress(src, res);
        return res.toByteArray();
    }

    /**
     * Perform a streaming decompression of the input.
     * Works by:
     * 1) Reading a flag byte, the 8 bits of which tell you if the
     * following 8 codes are compressed our un-compressed
     * 2) Consider the 8 bits in turn
     * 3) If the bit is set, the next code is un-compressed, so
     * add it to the dictionary and output it
     * 4) If the bit isn't set, then read in the length and start
     * position in the dictionary, and output the bytes there
     * 5) Loop until we've done all 8 bits, then read in the next
     * flag byte
     */
    public void decompress(InputStream src, OutputStream res) throws IOException {
        // How far through the output we've got
        // (This is normally used &4095, so it nicely wraps)
        // The initial value is set when populating the dictionary
        int pos;
        // The flag byte is treated as its 8 individual
        //  bits, which tell us if the following 8 codes
        //  are compressed or un-compressed
        int flag;
        // The mask, between 1 and 255, which is used when
        //  processing each bit of the flag byte in turn
        int mask;

        // We use 12 bit codes:
        // * 0-255 are real bytes
        // * 256-4095 are the substring codes
        // Java handily initialises our buffer / dictionary
        //  to all zeros
        final byte[] buffer = new byte[DICT_SIZE];
        pos = populateDictionary(buffer);

        // These are bytes as looked up in the dictionary
        // It needs to be signed, as it'll get passed on to
        //  the output stream
        final byte[] dataB = IOUtils.safelyAllocate(16L + codeLengthIncrease, MAX_RECORD_LENGTH);
        // This is an unsigned byte read from the stream
        // It needs to be unsigned, so that bit stuff works
        int dataI;
        // The compressed code sequence is held over 2 bytes
        int dataIPt1, dataIPt2;
        // How long a code sequence is, and where in the
        //  dictionary to start at
        int len, pntr;

        while ((flag = src.read()) != -1) {
            // Compare each bit in our flag byte in turn:
            for (mask = 1; mask < 0x100; mask <<= 1) {
                // Is this a new code (un-compressed), or
                //  the use of existing codes (compressed)?
                boolean isMaskSet = (flag & mask) > 0;
                if (isMaskSet ^ maskMeansCompressed) {
                    // Retrieve the un-compressed code
                    if ((dataI = src.read()) != -1) {
                        // Save the byte into the dictionary
                        buffer[pos++ & DICT_MASK] = (byte) dataI;
                        // And output the byte
                        res.write(dataI);
                    }
                } else {
                    // We have a compressed sequence
                    // Grab the next 16 bits of data
                    dataIPt1 = src.read();
                    dataIPt2 = src.read();
                    if (dataIPt1 == -1 || dataIPt2 == -1) break;

                    // Build up how long the code sequence is, and
                    //  what position of the code to start at
                    // (The position is the usually the first 12 bits,
                    //  and the length is usually the last 4 bits)
                    len = (dataIPt2 & 0x0F) + codeLengthIncrease;
                    if (positionIsBigEndian) {
                        pntr = (dataIPt1 << 4) + (dataIPt2 >>> 4);
                    } else {
                        pntr = dataIPt1 + ((dataIPt2 & 0xF0) << 4);
                    }

                    // Adjust the pointer as needed
                    pntr = adjustDictionaryOffset(pntr);

                    // Loop over the codes, outputting what they correspond to
                    for (int i = 0; i < len; i++) {
                        dataB[i] = buffer[(pntr + i) & DICT_MASK];
                        buffer[(pos + i) & DICT_MASK] = dataB[i];
                    }
                    res.write(dataB, 0, len);

                    // Record how far along the stream we have moved
                    pos += len;
                }
            }
        }
    }
}
