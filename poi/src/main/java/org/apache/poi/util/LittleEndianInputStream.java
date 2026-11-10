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

import java.io.BufferedInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wraps an {@link InputStream} providing {@link LittleEndianInput}<p>
 *
 * This class does not buffer any input, so the stream read position maintained
 * by this class is consistent with that of the inner stream.
 */
public class LittleEndianInputStream extends FilterInputStream implements LittleEndianInput {

    private static final int BUFFERED_SIZE = 8192;
    private static final int SKIP_BUFFER_SIZE = 2048;

    private static final int EOF = -1;
    private int readIndex = 0;
    private int markIndex = -1;
    /** reused by the primitive reads, so they don't allocate per call */
    private final byte[] primitiveBuffer = new byte[LittleEndianConsts.LONG_SIZE];
    /** reused by skipFully, allocated on first use */
    private byte[] skipBuffer;

    public LittleEndianInputStream(InputStream is) {
        super(is.markSupported() ? is : new BufferedInputStream(is, BUFFERED_SIZE));
    }

    @Override
    @SuppressForbidden("just delegating")
    public int available() {
        try {
            return super.available();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public byte readByte() {
        return (byte)readUByte();
    }

    @Override
    public int readUByte() {
        final int b;
        try {
            b = read();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (b == EOF) {
            throw new IllegalStateException("Unexpected end-of-file");
        }
        return b;
    }

    /**
     * get a float value, reads it in little endian format
     * then converts the resulting revolting IEEE 754 (curse them) floating
     * point number to a happy java float
     *
     * @return the float (32-bit) value
     */
    public float readFloat() {
        return Float.intBitsToFloat( readInt() );
    }

    @Override
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    @Override
    public int readInt() {
        readFully(primitiveBuffer, 0, LittleEndianConsts.INT_SIZE);
        return LittleEndian.getInt(primitiveBuffer, 0);
    }

    /**
     * get an unsigned int value from an InputStream
     *
     * @return the unsigned int (32-bit) value
     * @throws IllegalStateException
     *                wraps any IOException thrown from reading the stream.
     */
    //@Override
    public long readUInt() {
       long retNum = readInt();
       return retNum & 0x00FFFFFFFFL;
    }

    @Override
    public long readLong() {
        readFully(primitiveBuffer, 0, LittleEndianConsts.LONG_SIZE);
        return LittleEndian.getLong(primitiveBuffer, 0);
    }

    @Override
    public short readShort() {
        return (short)readUShort();
    }

    @Override
    public int readUShort() {
        readFully(primitiveBuffer, 0, LittleEndianConsts.SHORT_SIZE);
        return LittleEndian.getUShort(primitiveBuffer, 0);
    }

    private static void checkEOF(int actualBytes, int expectedBytes) {
        if (expectedBytes != 0 && (actualBytes == -1 || actualBytes != expectedBytes)) {
            throw new IllegalStateException("Unexpected end-of-file");
        }
    }

    @Override
    public void readFully(byte[] buf) {
        readFully(buf, 0, buf.length);
    }

    @Override
    public void readFully(byte[] buf, int off, int len) {
        try {
            checkEOF(_read(buf, off, len), len);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != EOF) {
            readIndex++;
        }
        return b;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        readIndex += (int) Math.max(0, skipped);
        return skipped;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int readBytes = super.read(b, off, len);

        // only increase read-index when we did read some bytes
        readIndex += Math.max(0, readBytes);

        return readBytes;
    }

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
        markIndex = readIndex;
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        if (markIndex > -1) {
            readIndex = markIndex;
            markIndex = -1;
        }
    }

    public int getReadIndex() {
        return readIndex;
    }



    //Makes repeated calls to super.read() until length is read or EOF is reached
    private int _read(byte[] buffer, int offset, int length) throws IOException {
        //lifted directly from org.apache.commons.io.IOUtils 2.4
        int remaining = length;
        while (remaining > 0) {
            int location = length - remaining;
            int count = read(buffer, offset + location, remaining);
            if (EOF == count) {
                break;
            }
            remaining -= count;
        }

        return length - remaining;
    }

    @Override
    public void readPlain(byte[] buf, int off, int len) {
        readFully(buf, off, len);
    }


    public void skipFully(int len) throws IOException {
        skipFully((long) len);
    }

    /**
     * Skips exactly {@code len} bytes.
     * <p>
     * The bytes are read rather than skipped with {@link #skip(long)}, as some streams
     * don't report the end of the stream when skipping (see IO-203).
     *
     * @param len the number of bytes to skip
     * @throws IllegalArgumentException if {@code len} is negative
     * @throws IllegalStateException if the end of the stream is reached first
     * @throws IOException if the stream can't be read
     */
    public void skipFully(long len) throws IOException {
        if (len < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + len);
        }
        if (len == 0) {
            return;
        }
        if (skipBuffer == null) {
            skipBuffer = new byte[SKIP_BUFFER_SIZE];
        }
        long remaining = len;
        while (remaining > 0) {
            int count = read(skipBuffer, 0, (int) Math.min(remaining, SKIP_BUFFER_SIZE));
            if (count == EOF) {
                throw new IllegalStateException("Unexpected end-of-file");
            }
            remaining -= count;
        }
    }
}
