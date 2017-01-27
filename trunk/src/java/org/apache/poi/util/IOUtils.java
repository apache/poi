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

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.poi.EmptyFileException;

public final class IOUtils {
    private static final POILogger logger = POILogFactory.getLogger( IOUtils.class );

    private IOUtils() {
        // no instances of this class
    }

    /**
     * Peeks at the first 8 bytes of the stream. Returns those bytes, but
     *  with the stream unaffected. Requires a stream that supports mark/reset,
     *  or a PushbackInputStream. If the stream has &gt;0 but &lt;8 bytes, 
     *  remaining bytes will be zero.
     * @throws EmptyFileException if the stream is empty
     */
    public static byte[] peekFirst8Bytes(InputStream stream) throws IOException, EmptyFileException {
        return peekFirstNBytes(stream, 8);
    }

    /**
     * Peeks at the first N bytes of the stream. Returns those bytes, but
     *  with the stream unaffected. Requires a stream that supports mark/reset,
     *  or a PushbackInputStream. If the stream has &gt;0 but &lt;N bytes, 
     *  remaining bytes will be zero.
     * @throws EmptyFileException if the stream is empty
     */
    public static byte[] peekFirstNBytes(InputStream stream, int limit) throws IOException, EmptyFileException {
        stream.mark(limit);
        ByteArrayOutputStream bos = new ByteArrayOutputStream(limit);
        copy(new BoundedInputStream(stream, limit), bos);

        int readBytes = bos.size();
        if (readBytes == 0) {
            throw new EmptyFileException();
        }
        
        if (readBytes < limit) {
            bos.write(new byte[limit-readBytes]);
        }
        byte peekedBytes[] = bos.toByteArray();
        if(stream instanceof PushbackInputStream) {
            PushbackInputStream pin = (PushbackInputStream)stream;
            pin.unread(peekedBytes, 0, readBytes);
        } else {
            stream.reset();
        }

        return peekedBytes;
    }
    
    
    
    /**
     * Reads all the data from the input stream, and returns the bytes read.
     */
    public static byte[] toByteArray(InputStream stream) throws IOException {
        return toByteArray(stream, Integer.MAX_VALUE);
    }

    /**
     * Reads up to {@code length} bytes from the input stream, and returns the bytes read.
     */
    public static byte[] toByteArray(InputStream stream, int length) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(length == Integer.MAX_VALUE ? 4096 : length);

        byte[] buffer = new byte[4096];
        int totalBytes = 0, readBytes;
        do {
            readBytes = stream.read(buffer, 0, Math.min(buffer.length, length-totalBytes));
            totalBytes += Math.max(readBytes,0);
            if (readBytes > 0) {
                baos.write(buffer, 0, readBytes);
            }
        } while (totalBytes < length && readBytes > -1);

        if (length != Integer.MAX_VALUE && totalBytes < length) {
            throw new IOException("unexpected EOF");
        }
        
        return baos.toByteArray();
    }

    
    /**
     * Returns an array (that shouldn't be written to!) of the
     *  ByteBuffer. Will be of the requested length, or possibly
     *  longer if that's easier.
     */
    public static byte[] toByteArray(ByteBuffer buffer, int length) {
        if(buffer.hasArray() && buffer.arrayOffset() == 0) {
            // The backing array should work out fine for us
            return buffer.array();
        }

        byte[] data = new byte[length];
        buffer.get(data);
        return data;
    }

    /**
     * Helper method, just calls <tt>readFully(in, b, 0, b.length)</tt>
     */
    public static int readFully(InputStream in, byte[] b) throws IOException {
        return readFully(in, b, 0, b.length);
    }

    /**
     * <p>Same as the normal {@link InputStream#read(byte[], int, int)}, but tries to ensure
     * that the entire len number of bytes is read.</p>
     * 
     * <p>If the end of file is reached before any bytes are read, returns <tt>-1</tt>. If
     * the end of the file is reached after some bytes are read, returns the
     * number of bytes read. If the end of the file isn't reached before <tt>len</tt>
     * bytes have been read, will return <tt>len</tt> bytes.</p>
     * 
     * @param in the stream from which the data is read.
     * @param b the buffer into which the data is read.
     * @param off the start offset in array <tt>b</tt> at which the data is written.
     * @param len the maximum number of bytes to read.
     */
    public static int readFully(InputStream in, byte[] b, int off, int len) throws IOException {
        int total = 0;
        while (true) {
            int got = in.read(b, off + total, len - total);
            if (got < 0) {
                return (total == 0) ? -1 : total;
            }
            total += got;
            if (total == len) {
                return total;
            }
        }
    }

    /**
     * Same as the normal <tt>channel.read(b)</tt>, but tries to ensure
     * that the buffer is filled completely if possible, i.e. b.remaining()
     * returns 0.
     * <p>
     * If the end of file is reached before any bytes are read, returns -1. If
     * the end of the file is reached after some bytes are read, returns the
     * number of bytes read. If the end of the file isn't reached before the
     * buffer has no more remaining capacity, will return the number of bytes
     * that were read.
     */
    public static int readFully(ReadableByteChannel channel, ByteBuffer b) throws IOException {
        int total = 0;
        while (true) {
            int got = channel.read(b);
            if (got < 0) {
                return (total == 0) ? -1 : total;
            }
            total += got;
            if (total == b.capacity() || b.position() == b.capacity()) {
                return total;
            }
        }
    }

    /**
     * Copies all the data from the given InputStream to the OutputStream. It
     * leaves both streams open, so you will still need to close them once done.
     */
    public static void copy(InputStream inp, OutputStream out) throws IOException {
        byte[] buff = new byte[4096];
        int count;
        while ((count = inp.read(buff)) != -1) {
            if (count > 0) {
                out.write(buff, 0, count);
            }
        }
    }

    /**
     * Calculate checksum on input data
     */
    public static long calculateChecksum(byte[] data) {
        Checksum sum = new CRC32();
        sum.update(data, 0, data.length);
        return sum.getValue();
    }

    /**
     * Calculate checksum on all the data read from input stream.
     *
     * This should be more efficient than the equivalent code
     * {@code IOUtils.calculateChecksum(IOUtils.toByteArray(stream))}
     */
    public static long calculateChecksum(InputStream stream) throws IOException {
        Checksum sum = new CRC32();

        byte[] buf = new byte[4096];
        int count;
        while ((count = stream.read(buf)) != -1) {
            if (count > 0) {
                sum.update(buf, 0, count);
            }
        }
        return sum.getValue();
    }
    
    
    /**
     * Quietly (no exceptions) close Closable resource. In case of error it will
     * be printed to {@link IOUtils} class logger.
     * 
     * @param closeable
     *            resource to close
     */
    public static void closeQuietly( final Closeable closeable ) {
        // no need to log a NullPointerException here
        if(closeable == null) {
            return;
        }

        try {
            closeable.close();
        } catch ( Exception exc ) {
            logger.log( POILogger.ERROR, "Unable to close resource: " + exc,
                    exc );
        }
    }

    /**
     * Skips bytes from a stream.  Returns -1L if EOF was hit before
     * the end of the stream.
     *
     * @param in inputstream
     * @param len length to skip
     * @return number of bytes skipped
     * @throws IOException on IOException
     */
    public static long skipFully(InputStream in, long len) throws IOException {
        int total = 0;
        while (true) {
            long got = in.skip(len-total);
            if (got < 0) {
                return -1L;
            }
            total += got;
            if (total == len) {
                return total;
            }
        }
    }
}
