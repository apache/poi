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
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.Locale;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.poi.EmptyFileException;

@Internal
public final class IOUtils {
    private static final POILogger logger = POILogFactory.getLogger( IOUtils.class );

    /**
     * The default buffer size to use for the skip() methods.
     */
    private static final int SKIP_BUFFER_SIZE = 2048;
    private static byte[] SKIP_BYTE_BUFFER;

    /**
     * The current set global allocation limit override,
     * -1 means limits are applied per record type.
     */
    private static int BYTE_ARRAY_MAX_OVERRIDE = -1;

    private IOUtils() {
        // no instances of this class
    }

    /**
     * If this value is set to > 0, {@link #safelyAllocate(long, int)} will ignore the
     * maximum record length parameter.
     *
     * This is designed to allow users to bypass the hard-coded maximum record lengths
     * if they are willing to accept the risk of allocating memory up to the size specified.
     *
     * It also allows to impose a lower limit than used for very memory constrained systems.
     *
     * Note: This is an per-allocation limit and does not allow to limit overall sum of allocations!
     *
     * Use -1 for using the limits specified per record-type.
     *
     * @param maxOverride The maximum number of bytes that should be possible to be allocated in one step.
     * @since 4.0.0
     */
    @SuppressWarnings("unused")
    public static void setByteArrayMaxOverride(int maxOverride) {
        BYTE_ARRAY_MAX_OVERRIDE = maxOverride;
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

    private static void checkByteSizeLimit(int length) {
        if(BYTE_ARRAY_MAX_OVERRIDE != -1 && length > BYTE_ARRAY_MAX_OVERRIDE) {
            throwRFE(length, BYTE_ARRAY_MAX_OVERRIDE);
        }
    }

    /**
     * Peeks at the first N bytes of the stream. Returns those bytes, but
     *  with the stream unaffected. Requires a stream that supports mark/reset,
     *  or a PushbackInputStream. If the stream has &gt;0 but &lt;N bytes,
     *  remaining bytes will be zero.
     * @throws EmptyFileException if the stream is empty
     */
    public static byte[] peekFirstNBytes(InputStream stream, int limit) throws IOException, EmptyFileException {
        checkByteSizeLimit(limit);

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
        byte[] peekedBytes = bos.toByteArray();
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
     *
     * @param stream The byte stream of data to read.
     * @return A byte array with the read bytes.
     * @throws IOException If reading data fails or EOF is encountered too early for the given length.
     */
    public static byte[] toByteArray(InputStream stream) throws IOException {
        return toByteArray(stream, Integer.MAX_VALUE);
    }

    /**
     * Reads up to {@code length} bytes from the input stream, and returns the bytes read.
     *
     * @param stream The byte stream of data to read.
     * @param length The maximum length to read, use Integer.MAX_VALUE to read the stream
     *               until EOF.
     * @return A byte array with the read bytes.
     * @throws IOException If reading data fails or EOF is encountered too early for the given length.
     */
    public static byte[] toByteArray(InputStream stream, final int length) throws IOException {
        return toByteArray(stream, length, Integer.MAX_VALUE);
    }


    /**
     * Reads up to {@code length} bytes from the input stream, and returns the bytes read.
     *
     * @param stream The byte stream of data to read.
     * @param length The maximum length to read, use {@link Integer#MAX_VALUE} to read the stream
     *               until EOF
     * @param maxLength if the input is equal to/longer than {@code maxLength} bytes,
 *                   then throw an {@link IOException} complaining about the length.
*                    use {@link Integer#MAX_VALUE} to disable the check
     * @return A byte array with the read bytes.
     * @throws IOException If reading data fails or EOF is encountered too early for the given length.
     */
    public static byte[] toByteArray(InputStream stream, final int length, final int maxLength) throws IOException {
        if (length < 0L || maxLength < 0L) {
            throw new RecordFormatException("Can't allocate an array of length < 0");
        }
        // if (length > (long)Integer.MAX_VALUE) {
        //     throw new RecordFormatException("Can't allocate an array > "+Integer.MAX_VALUE);
        // }
        if ((length != Integer.MAX_VALUE) || (maxLength != Integer.MAX_VALUE)) {
            checkLength(length, maxLength);
        }

        final int len = Math.min(length, maxLength);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(len == Integer.MAX_VALUE ? 4096 : len);

        byte[] buffer = new byte[4096];
        int totalBytes = 0, readBytes;
        do {
            readBytes = stream.read(buffer, 0, Math.min(buffer.length, len-totalBytes));
            totalBytes += Math.max(readBytes,0);
            if (readBytes > 0) {
                baos.write(buffer, 0, readBytes);
            }

            checkByteSizeLimit(totalBytes);
        } while (totalBytes < len && readBytes > -1);

        if (maxLength != Integer.MAX_VALUE && totalBytes == maxLength) {
            throw new IOException("MaxLength ("+maxLength+") reached - stream seems to be invalid.");
        }

        if (len != Integer.MAX_VALUE && totalBytes < len) {
            throw new EOFException("unexpected EOF - expected len: "+len+" - actual len: "+totalBytes);
        }

        return baos.toByteArray();
    }

    private static void checkLength(long length, int maxLength) {
        if (BYTE_ARRAY_MAX_OVERRIDE > 0) {
            if (length > BYTE_ARRAY_MAX_OVERRIDE) {
                throwRFE(length, BYTE_ARRAY_MAX_OVERRIDE);
            }
        } else if (length > maxLength) {
            throwRFE(length, maxLength);
        }
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

        checkByteSizeLimit(length);
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
     *
     * @param inp The {@link InputStream} which provides the data
     * @param out The {@link OutputStream} to write the data to
     * @return the amount of bytes copied
     *
     * @throws IOException If copying the data fails.
     */
    public static long copy(InputStream inp, OutputStream out) throws IOException {
        return copy(inp, out, -1);
    }

    /**
     * Copies all the data from the given InputStream to the OutputStream. It
     * leaves both streams open, so you will still need to close them once done.
     *
     * @param inp The {@link InputStream} which provides the data
     * @param out The {@link OutputStream} to write the data to
     * @param limit limit the copied bytes - use {@code -1} for no limit
     * @return the amount of bytes copied
     *
     * @throws IOException If copying the data fails.
     */
    public static long copy(InputStream inp, OutputStream out, long limit) throws IOException {
        final byte[] buff = new byte[4096];
        long totalCount = 0;
        int readBytes = -1;
        do {
            int todoBytes = (int)((limit < 0) ? buff.length : Math.min(limit-totalCount, buff.length));
            if (todoBytes > 0) {
                readBytes = inp.read(buff, 0, todoBytes);
                if (readBytes > 0) {
                    out.write(buff, 0, readBytes);
                    totalCount += readBytes;
                }
            }
        } while (readBytes >= 0 && (limit == -1 || totalCount < limit));

        return totalCount;
    }

    /**
     * Copy the contents of the stream to a new file.
     *
     * @param srcStream The {@link InputStream} which provides the data
     * @param destFile The file where the data should be stored
     * @return the amount of bytes copied
     *
     * @throws IOException If the target directory does not exist and cannot be created
     *      or if copying the data fails.
     */
    public static long copy(InputStream srcStream, File destFile) throws IOException {
        File destDirectory = destFile.getParentFile();
        if (!(destDirectory.exists() || destDirectory.mkdirs())) {
            throw new RuntimeException("Can't create destination directory: "+destDirectory);
        }
        try (OutputStream destStream = new FileOutputStream(destFile)) {
            return IOUtils.copy(srcStream, destStream);
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
            logger.log( POILogger.ERROR, "Unable to close resource: ", exc,
                    exc );
        }
    }


    /**
     * Skips bytes from an input byte stream.
     * This implementation guarantees that it will read as many bytes
     * as possible before giving up; this may not always be the case for
     * skip() implementations in subclasses of {@link InputStream}.
     * <p>
     * Note that the implementation uses {@link InputStream#read(byte[], int, int)} rather
     * than delegating to {@link InputStream#skip(long)}.
     * This means that the method may be considerably less efficient than using the actual skip implementation,
     * this is done to guarantee that the correct number of bytes are skipped.
     * </p>
     * <p>
     * This mimics POI's {@link #readFully(InputStream, byte[])}.
     * If the end of file is reached before any bytes are read, returns <tt>-1</tt>. If
     * the end of the file is reached after some bytes are read, returns the
     * number of bytes read. If the end of the file isn't reached before <tt>len</tt>
     * bytes have been read, will return <tt>len</tt> bytes.</p>

     * </p>
     * <p>
     * Copied nearly verbatim from commons-io 41a3e9c
     * </p>
     *
     * @param input byte stream to skip
     * @param toSkip number of bytes to skip.
     * @return number of bytes actually skipped.
     * @throws IOException              if there is a problem reading the file
     * @throws IllegalArgumentException if toSkip is negative
     * @see InputStream#skip(long)
     *
     */
    public static long skipFully(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        if (toSkip == 0) {
            return 0L;
        }
        /*
         * N.B. no need to synchronize this because: - we don't care if the buffer is created multiple times (the data
         * is ignored) - we always use the same size buffer, so if it it is recreated it will still be OK (if the buffer
         * size were variable, we would need to synch. to ensure some other thread did not create a smaller one)
         */
        if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = new byte[SKIP_BUFFER_SIZE];
        }
        long remain = toSkip;
        while (remain > 0) {
            // See https://issues.apache.org/jira/browse/IO-203 for why we use read() rather than delegating to skip()
            final long n = input.read(SKIP_BYTE_BUFFER, 0, (int) Math.min(remain, SKIP_BUFFER_SIZE));
            if (n < 0) { // EOF
                break;
            }
            remain -= n;
        }
        if (toSkip == remain) {
            return -1L;
        }
        return toSkip - remain;
    }

    public static byte[] safelyAllocate(long length, int maxLength) {
        safelyAllocateCheck(length, maxLength);

        checkByteSizeLimit((int)length);

        return new byte[(int)length];
    }

    public static void safelyAllocateCheck(long length, int maxLength) {
        if (length < 0L) {
            throw new RecordFormatException("Can't allocate an array of length < 0, but had " + length + " and " + maxLength);
        }
        if (length > (long)Integer.MAX_VALUE) {
            throw new RecordFormatException("Can't allocate an array > "+Integer.MAX_VALUE);
        }
        checkLength(length, maxLength);
    }

    public static byte[] safelyClone(byte[] src, int offset, int length, int maxLength) {
        if (src == null) {
            return null;
        }
        assert(offset >= 0 && length >= 0 && maxLength >= 0);
        safelyAllocateCheck(Math.min(src.length-offset,length), maxLength);
        return Arrays.copyOfRange(src, offset, offset+length);
    }




    /**
     * Simple utility function to check that you haven't hit EOF
     * when reading a byte.
     *
     * @param is inputstream to read
     * @return byte read, unless
     * @throws IOException on IOException or EOF if -1 is read
     */
    public static int readByte(InputStream is) throws IOException {
        int b = is.read();
        if (b == -1) {
            throw new EOFException();
        }
        return b;
    }

    private static void throwRFE(long length, int maxLength) {
        throw new RecordFormatException(String.format(Locale.ROOT, "Tried to allocate an array of length %,d" +
                ", but the maximum lenght for this record type is %,d.\n" +
                "If the file is not corrupt, please open an issue on bugzilla to request \n" +
                "increasing the maximum allowable size for this record type.\n"+
                "As a temporary workaround, consider setting a higher override value with " +
                "IOUtils.setByteArrayMaxOverride()",
                length, maxLength));

    }
}
