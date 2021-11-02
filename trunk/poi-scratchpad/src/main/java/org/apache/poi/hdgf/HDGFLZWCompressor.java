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

package org.apache.poi.hdgf;

import static org.apache.poi.util.LZWDecompresser.DICT_MASK;
import static org.apache.poi.util.LZWDecompresser.DICT_SIZE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class to handle the Visio compatible streaming LZW compression.
 * Need our own class to handle keeping track of the code buffer, pending bytes to write out etc.
 * <p>
 * TODO Fix this, as it starts to go wrong on large streams
 */
/* package */ final class HDGFLZWCompressor {
    // We use 12 bit codes:
    // * 0-255 are real bytes
    // * 256-4095 are the substring codes
    // Java handily initialises our buffer / dictionary
    //  to all zeros
    private final byte[] dict = new byte[DICT_SIZE];

    // The next block of data to be written out, minus its mask byte
    private final byte[] buffer = new byte[16];
    // And how long it is
    // (Un-compressed codes are 1 byte each, compressed codes are two)
    private int bufferLen;

    // The raw length of a code is limited to 4 bits + 2
    private final byte[] rawCode = new byte[18];
    // And how much we're using
    private int rawCodeLen;

    // How far through the input and output streams we are
    private int posInp;
    private int posOut;

    // What the next mask byte to output will be
    private int nextMask;
    // And how many bits we've already set
    private int maskBitsSet;

    private final OutputStream res;

    public HDGFLZWCompressor(OutputStream res) {
        this.res = res;
    }

    /**
     * Returns the last place that the bytes from rawCode are found
     * at in the buffer, or -1 if they can't be found
     */
    private int findRawCodeInBuffer() {
        // Work our way through all the codes until we
        //  find the right one. Visio starts from the end
        for (int i = rawCodeLen+1; i < DICT_SIZE; i++) {
            int pos = (posInp - i) & DICT_MASK;
            // in the example data it seems, that the compressor doesn't like to wrap beyond DICT_SIZE
            // if (pos + rawCodeLen > DICT_SIZE) continue;
            boolean matches = true;
            for (int j = 0; j < rawCodeLen; j++) {
                if (dict[(pos + j) & DICT_MASK] != rawCode[j]) {
                    // Doesn't fit, can't be a match
                    matches = false;
                    break;
                }
            }

            // Was this position a match?
            if (matches) {
                return pos;
            }
        }

        // Not found
        return -1;
    }

    /**
     * Output the compressed representation for the bytes
     * found in rawCode
     */
    private void outputCompressed() throws IOException {
        // It's not worth compressing only 1 or two bytes, due to the overheads
        // So if asked, just output uncompressed
        if (rawCodeLen < 3) {
            final int rcl = rawCodeLen;
            for (int i = 0; i < rcl; i++) {
                outputUncompressed(rawCode[i]);
            }
            return;
        }

        // Grab where the data lives
        int codesAt = findRawCodeInBuffer();
        codesAt = (codesAt-18) & DICT_MASK;

        // Increment the mask bit count, we've done another code
        maskBitsSet++;

        // Add the length+code to the buffer
        // (The position is the first 12 bits, the length is the last 4 bits)
        int bp1 = (codesAt & 0xFF);
        int bp2 = (rawCodeLen - 3) + ((codesAt - bp1) >>> 4);
        buffer[bufferLen++] = (byte) bp1;
        buffer[bufferLen++] = (byte) bp2;

        assert(maskBitsSet <= 8);

        // If we're now at 8 codes, output
        if (maskBitsSet == 8) {
            output8Codes();
        }

        rawCodeLen = 0;
    }

    /**
     * Output the un-compressed byte
     */
    private void outputUncompressed(byte b) throws IOException {
        // Set the mask bit for us
        nextMask += (1 << maskBitsSet);
        maskBitsSet++;

        // And add us to the buffer + dictionary
        buffer[bufferLen++] = b;

        // If we're now at 8 codes, output
        if (maskBitsSet == 8) {
            output8Codes();
        }

        rawCodeLen = 0;
    }

    /**
     * We've got 8 code worth to write out, so
     * output along with the header
     */
    private void output8Codes() throws IOException {
        // Output the mask and the data
        res.write(nextMask);
        res.write(buffer, 0, bufferLen);
        posOut += 1 + bufferLen;

        // Reset things
        nextMask = 0;
        maskBitsSet = 0;
        bufferLen = 0;
    }

    /**
     * Does the compression
     */
    public void compress(InputStream src) throws IOException {
        int dataI = -1;
        while (true) {
            if (dataI > -1) {
                // copy the last read byte into the dictionary.
                // the example data compressor used self references, so we don't wait for filling the dictionary
                // until we know if it's a un-/compressed token.
                dict[(posInp++) & DICT_MASK] = (byte)dataI;
            }
            // This is an unsigned byte read from the stream
            // It needs to be unsigned, so that bit stuff works
            dataI = src.read();

            // If we've run out of data, output anything that's pending then finish
            if (dataI == -1) {
                if (rawCodeLen > 0) {
                    outputCompressed();
                    if (maskBitsSet > 0) {
                        output8Codes();
                    }
                }
                break;
            }

            // This is a byte as looked up in the dictionary
            // It needs to be signed, as it'll get passed on to the output stream
            byte dataB = (byte) dataI;

            // Try adding this new byte onto rawCode, and see if all of that is still found
            // in the buffer dictionary or not
            rawCode[rawCodeLen++] = dataB;
            int rawAt = findRawCodeInBuffer();

            if (rawAt > -1) {
                // If we found it and are now at 18 bytes, we need to output our pending code block
                if (rawCodeLen == 18) {
                    outputCompressed();
                }

                // If we did find all of rawCode with our new byte added on,
                // we can wait to see what happens with the next byte
                continue;
            }

            // If we get here, then the rawCode + this byte weren't found in the dictionary

            // If there was something in rawCode before, then that was
            // found in the dictionary, so output that compressed
            rawCodeLen--;
            if (rawCodeLen > 0) {
                // Output the old rawCode
                outputCompressed();

                // Can this byte start a new rawCode, or does it need outputting itself?
                rawCode[0] = dataB;
                rawCodeLen = 1;
                if (findRawCodeInBuffer() > -1) {
                    // Fits in, wait for next byte
                    continue;
                }
                // Doesn't fit, output
                outputUncompressed(dataB);
            } else {
                // Nothing in rawCode before, so this byte isn't in the buffer dictionary
                // Output it un-compressed
                outputUncompressed(dataB);
            }
        }
    }
}
