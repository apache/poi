/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * a utility class for handling little-endian numbers, which the 80x86
 * world is replete with. The methods are all static, and input/output
 * is from/to byte arrays, or from InputStreams.
 *
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew Oliver (acoliver at apache dot org)
 *
 */

public class LittleEndian
        implements LittleEndianConsts
{

    // all methods are static, so an accessible constructor makes no
    // sense
    private LittleEndian()
    {
    }

    /**
     * get a short value from a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     *
     * @return the short (16-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static short getShort(final byte[] data, final int offset)
    {
        return (short) getNumber(data, offset, SHORT_SIZE);
    }

    /**
     * get a short array from a byte array.
     */
    public static short[] getSimpleShortArray(final byte[] data, final int offset, final int size)
    {
        short[] results = new short[size];
        for (int i = 0; i < size; i++)
        {
            results[i] = getShort(data, offset + 2 + (i * 2));
        }
        return results;
    }
    /**
     * get a short array from a byte array.  The short array is assumed
     * to start with a word describing the length of the array.
     */
    public static short[] getShortArray(final byte[] data, final int offset)
    {
        int size = (short) getNumber(data, offset, SHORT_SIZE);
        short[] results = getSimpleShortArray(data, offset, size);
        return results;
    }

    /**
     * get a short value from the beginning of a byte array
     *
     * @param data the byte array
     *
     * @return the short (16-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static short getShort(final byte[] data)
    {
        return getShort(data, 0);
    }

    /**
     * get an int value from a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     *
     * @return the int (32-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static int getInt(final byte[] data, final int offset)
    {
        return (int) getNumber(data, offset, INT_SIZE);
    }

    /**
     * get an int value from the beginning of a byte array
     *
     * @param data the byte array
     *
     * @return the int (32-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static int getInt(final byte[] data)
    {
        return getInt(data, 0);
    }

    /**
     * get a long value from a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     *
     * @return the long (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static long getLong(final byte[] data, final int offset)
    {
        return getNumber(data, offset, LONG_SIZE);
    }

    /**
     * get a long value from the beginning of a byte array
     *
     * @param data the byte array
     *
     * @return the long (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static long getLong(final byte[] data)
    {
        return getLong(data, 0);
    }

    /**
     * get a double value from a byte array, reads it in little endian format
     * then converts the resulting revolting IEEE 754 (curse them) floating
     * point number to a happy java double
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     *
     * @return the double (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static double getDouble(final byte[] data, final int offset)
    {
        return Double.longBitsToDouble(getNumber(data, offset, DOUBLE_SIZE));
    }

    /**
     * get a double value from the beginning of a byte array
     *
     * @param data the byte array
     *
     * @return the double (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static double getDouble(final byte[] data)
    {
        return getDouble(data, 0);
    }

    /**
     * put a short value into a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     * @param value the short (16-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putShort(final byte[] data, final int offset,
                                final short value)
    {
        putNumber(data, offset, value, SHORT_SIZE);
    }

    /**
     * put a array of shorts into a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     * @param value the short array
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */
    public static void putShortArray(final byte[] data, final int offset, final short[] value)
    {
        putNumber(data, offset, value.length, SHORT_SIZE);
        for (int i = 0; i < value.length; i++)
        {
            putNumber(data, offset + 2 + (i * 2), value[i], SHORT_SIZE);
        }
    }

    /**
     * put a short value into beginning of a byte array
     *
     * @param data the byte array
     * @param value the short (16-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putShort(final byte[] data, final short value)
    {
        putShort(data, 0, value);
    }

    /**
     * put an int value into a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     * @param value the int (32-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putInt(final byte[] data, final int offset,
                              final int value)
    {
        putNumber(data, offset, value, INT_SIZE);
    }

    /**
     * put an int value into beginning of a byte array
     *
     * @param data the byte array
     * @param value the int (32-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putInt(final byte[] data, final int value)
    {
        putInt(data, 0, value);
    }

    /**
     * put a long value into a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     * @param value the long (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putLong(final byte[] data, final int offset,
                               final long value)
    {
        putNumber(data, offset, value, LONG_SIZE);
    }

    /**
     * put a long value into beginning of a byte array
     *
     * @param data the byte array
     * @param value the long (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putLong(final byte[] data, final long value)
    {
        putLong(data, 0, value);
    }

    /**
     * put a double value into a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     * @param value the double (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putDouble(final byte[] data, final int offset,
                                 final double value)
    {
        putNumber(data, offset, Double.doubleToLongBits(value), DOUBLE_SIZE);
    }

    /**
     * put a double value into beginning of a byte array
     *
     * @param data the byte array
     * @param value the double (64-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */

    public static void putDouble(final byte[] data, final double value)
    {
        putDouble(data, 0, value);
    }

    /**
     * Exception to handle buffer underruns
     *
     * @author Marc Johnson (mjohnson at apache dot org)
     */

    public static class BufferUnderrunException
            extends IOException
    {

        /**
         * simple constructor
         */

        BufferUnderrunException()
        {
            super("buffer underrun");
        }
    }

    /**
     * get a short value from an InputStream
     *
     * @param stream the InputStream from which the short is to be
     *               read
     *
     * @return the short (16-bit) value
     *
     * @exception IOException will be propagated back to the caller
     * @exception BufferUnderrunException if the stream cannot provide
     *            enough bytes
     */

    public static short readShort(final InputStream stream)
            throws IOException, BufferUnderrunException
    {
        return getShort(readFromStream(stream, SHORT_SIZE));
    }

    /**
     * get an int value from an InputStream
     *
     * @param stream the InputStream from which the int is to be read
     *
     * @return the int (32-bit) value
     *
     * @exception IOException will be propagated back to the caller
     * @exception BufferUnderrunException if the stream cannot provide
     *            enough bytes
     */

    public static int readInt(final InputStream stream)
            throws IOException, BufferUnderrunException
    {
        return getInt(readFromStream(stream, INT_SIZE));
    }

    /**
     * get a long value from an InputStream
     *
     * @param stream the InputStream from which the long is to be read
     *
     * @return the long (64-bit) value
     *
     * @exception IOException will be propagated back to the caller
     * @exception BufferUnderrunException if the stream cannot provide
     *            enough bytes
     */

    public static long readLong(final InputStream stream)
            throws IOException, BufferUnderrunException
    {
        return getLong(readFromStream(stream, LONG_SIZE));
    }

    private static final byte[] _short_buffer = new byte[SHORT_SIZE];
    private static final byte[] _int_buffer = new byte[INT_SIZE];
    private static final byte[] _long_buffer = new byte[LONG_SIZE];

    /**
     * Read the appropriate number of bytes from the stream and return
     * them to the caller.
     * <p>
     * It should be noted that, in an attempt to improve system
     * performance and to prevent a transient explosion of discarded
     * byte arrays to be garbage collected, static byte arrays are
     * employed for the standard cases of reading a short, an int, or
     * a long.
     * <p>
     * <b>THIS INTRODUCES A RISK FOR THREADED OPERATIONS!</b>
     * <p>
     * However, for the purposes of the POI project, this risk is
     * deemed negligible. It is, however, so noted.
     *
     * @param stream the InputStream we're reading from
     * @param size the number of bytes to read; in 99.99% of cases,
     *             this will be SHORT_SIZE, INT_SIZE, or LONG_SIZE --
     *             but it doesn't have to be.
     *
     * @return the byte array containing the required number of
     *         bytes. The array will contain all zero's on end of
     *         stream
     *
     * @exception IOException will be propagated back to the caller
     * @exception BufferUnderrunException if the stream cannot provide
     *            enough bytes
     */

    public static byte[] readFromStream(final InputStream stream,
                                        final int size)
            throws IOException, BufferUnderrunException
    {
        byte[] buffer = null;

        switch (size)
        {

            case SHORT_SIZE:
                buffer = _short_buffer;
                break;

            case INT_SIZE:
                buffer = _int_buffer;
                break;

            case LONG_SIZE:
                buffer = _long_buffer;
                break;

            default :
                buffer = new byte[size];
                break;
        }
        int count = stream.read(buffer);

        if (count == -1)
        {

            // return a zero-filled buffer
            Arrays.fill(buffer, (byte) 0);
        } else if (count != size)
        {
            throw new BufferUnderrunException();
        }
        return buffer;
    }

    private static long getNumber(final byte[] data, final int offset,
                                  final int size)
    {
        long result = 0;

        for (int j = offset + size - 1; j >= offset; j--)
        {
            result <<= 8;
            result |= 0xff & data[j];
        }
        return result;
    }

    private static void putNumber(final byte[] data, final int offset,
                                  final long value, final int size)
    {
        int limit = size + offset;
        long v = value;

        for (int j = offset; j < limit; j++)
        {
            data[j] = (byte) (v & 0xFF);
            v >>= 8;
        }
    }

    /**
     * Convert an 'unsigned' byte to an integer.  ie, don't carry across the sign.
     */
    public static int ubyteToInt(byte b)
    {
        return ((b & 0x80) == 0 ? (int) b : (int) (b & (byte) 0x7f) + 0x80);
    }

    /**
     * get the unsigned value of a byte.
     *
     * @param data the byte array.
     * @param offset a starting offset into the byte array.
     *
     * @return the unsigned value of the byte as a 32 bit integer
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */
    public static int getUnsignedByte(final byte[] data, final int offset)
    {
        return (int) getNumber(data, offset, BYTE_SIZE);
    }

    /**
     * get the unsigned value of a byte.
     *
     * @param data the byte array
     *
     * @return the unsigned value of the byte as a 32 bit integer
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */
    public static int getUnsignedByte(final byte[] data)
    {
        return getUnsignedByte(data, 0);
    }
    /**
     * Copy a portion of a byte array
     *
     * @param data the original byte array
     * @param offset Where to start copying from.
     * @param size Number of bytes to copy.
     *
     * @throws IndexOutOfBoundsException - if copying would cause access of data
     *                                     outside array bounds.
     */
    public static byte[] getByteArray(final byte[] data, int offset, int size)
    {
        byte[] copy = new byte[size];
        System.arraycopy(data, offset, copy, 0, size);

        return copy;
    }

}
