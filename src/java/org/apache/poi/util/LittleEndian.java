/* ====================================================================
   Copyright 2003-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
import java.util.Arrays;

/**
 *  a utility class for handling little-endian numbers, which the 80x86 world is
 *  replete with. The methods are all static, and input/output is from/to byte
 *  arrays, or from InputStreams.
 *
 *@author     Marc Johnson (mjohnson at apache dot org)
 *@author     Andrew Oliver (acoliver at apache dot org)
 */

public class LittleEndian
         implements LittleEndianConsts {

    // all methods are static, so an accessible constructor makes no
    // sense
    /**
     *  Constructor for the LittleEndian object
     */
    private LittleEndian() { }


    /**
     *  get a short value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the short (16-bit) value
     */

    public static short getShort(final byte[] data, final int offset) {
        return (short) getNumber(data, offset, SHORT_SIZE);
    }


    /**
     *  get an unsigned short value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the unsigned short (16-bit) value in an integer
     */
    public static int getUShort(final byte[] data, final int offset) {
        short num = (short) getNumber(data, offset, SHORT_SIZE);
        int retNum;
        if (num < 0) {
            retNum = ((int) Short.MAX_VALUE + 1) * 2 + (int) num;
        } else {
            retNum = (int) num;
        }
        return retNum;
    }


    /**
     *  get a short array from a byte array.
     *
     *@param  data    Description of the Parameter
     *@param  offset  Description of the Parameter
     *@param  size    Description of the Parameter
     *@return         The simpleShortArray value
     */
    public static short[] getSimpleShortArray(final byte[] data, final int offset, final int size) {
        short[] results = new short[size];
        for (int i = 0; i < size; i++) {
            results[i] = getShort(data, offset + 2 + (i * 2));
        }
        return results;
    }


    /**
     *  get a short array from a byte array. The short array is assumed to start
     *  with a word describing the length of the array.
     *
     *@param  data    Description of the Parameter
     *@param  offset  Description of the Parameter
     *@return         The shortArray value
     */
    public static short[] getShortArray(final byte[] data, final int offset) {
        int size = (short) getNumber(data, offset, SHORT_SIZE);
        short[] results = getSimpleShortArray(data, offset, size);
        return results;
    }


    /**
     *  get a short value from the beginning of a byte array
     *
     *@param  data  the byte array
     *@return       the short (16-bit) value
     */

    public static short getShort(final byte[] data) {
        return getShort(data, 0);
    }


    /**
     *  get an unsigned short value from the beginning of a byte array
     *
     *@param  data  the byte array
     *@return       the unsigned short (16-bit) value in an int
     */
    public static int getUShort(final byte[] data) {
        return getUShort(data, 0);
    }


    /**
     *  get an int value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the int (32-bit) value
     */

    public static int getInt(final byte[] data, final int offset) {
        return (int) getNumber(data, offset, INT_SIZE);
    }


    /**
     *  get an int value from the beginning of a byte array
     *
     *@param  data  the byte array
     *@return       the int (32-bit) value
     */

    public static int getInt(final byte[] data) {
        return getInt(data, 0);
    }


    /**
     *  get an unsigned int value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the unsigned int (32-bit) value in a long
     */
    public static long getUInt(final byte[] data, final int offset) {
        int num = (int) getNumber(data, offset, INT_SIZE);
        long retNum;
        if (num < 0) {
            retNum = ((long) Integer.MAX_VALUE + 1) * 2 + (long) num;
        } else {
            retNum = (int) num;
        }
        return retNum;
    }

    /**
     *  get an unsigned int value from a byte array
     *
     *@param  data    the byte array
     *@return         the unsigned int (32-bit) value in a long
     */
    public static long getUInt(final byte[] data) {
	return getUInt(data,0);
    }

    /**
     *  get a long value from a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the long (64-bit) value
     */

    public static long getLong(final byte[] data, final int offset) {
        return getNumber(data, offset, LONG_SIZE);
    }


    /**
     *  get a long value from the beginning of a byte array
     *
     *@param  data  the byte array
     *@return       the long (64-bit) value
     */

    public static long getLong(final byte[] data) {
        return getLong(data, 0);
    }


    /**
     *  get a double value from a byte array, reads it in little endian format
     *  then converts the resulting revolting IEEE 754 (curse them) floating
     *  point number to a happy java double
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@return         the double (64-bit) value
     */

    public static double getDouble(final byte[] data, final int offset) {
        return Double.longBitsToDouble(getNumber(data, offset, DOUBLE_SIZE));
    }


    /**
     *  get a double value from the beginning of a byte array
     *
     *@param  data  the byte array
     *@return       the double (64-bit) value
     */

    public static double getDouble(final byte[] data) {
        return getDouble(data, 0);
    }


    /**
     *  put a short value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the short (16-bit) value
     */
    public static void putShort(final byte[] data, final int offset,
            final short value) {
        putNumber(data, offset, value, SHORT_SIZE);
    }


    /**
     *  put a array of shorts into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the short array
     */
    public static void putShortArray(final byte[] data, final int offset, final short[] value) {
        putNumber(data, offset, value.length, SHORT_SIZE);
        for (int i = 0; i < value.length; i++) {
            putNumber(data, offset + 2 + (i * 2), value[i], SHORT_SIZE);
        }
    }

    /**
     * put an unsigned short value into a byte array
     *
     * @param data the byte array
     * @param offset a starting offset into the byte array
     * @param value the short (16-bit) value
     *
     * @exception ArrayIndexOutOfBoundsException may be thrown
     */
    public static void putUShort(final byte[] data, final int offset,
                                final int value)
    {
        putNumber(data, offset, value, SHORT_SIZE);
    }

    /**
     *  put a short value into beginning of a byte array
     *
     *@param  data   the byte array
     *@param  value  the short (16-bit) value
     */

    public static void putShort(final byte[] data, final short value) {
        putShort(data, 0, value);
    }


    /**
     *  put an int value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the int (32-bit) value
     */

    public static void putInt(final byte[] data, final int offset,
            final int value) {
        putNumber(data, offset, value, INT_SIZE);
    }


    /**
     *  put an int value into beginning of a byte array
     *
     *@param  data   the byte array
     *@param  value  the int (32-bit) value
     */

    public static void putInt(final byte[] data, final int value) {
        putInt(data, 0, value);
    }


    /**
     *  put a long value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the long (64-bit) value
     */

    public static void putLong(final byte[] data, final int offset,
            final long value) {
        putNumber(data, offset, value, LONG_SIZE);
    }


    /**
     *  put a long value into beginning of a byte array
     *
     *@param  data   the byte array
     *@param  value  the long (64-bit) value
     */

    public static void putLong(final byte[] data, final long value) {
        putLong(data, 0, value);
    }


    /**
     *  put a double value into a byte array
     *
     *@param  data    the byte array
     *@param  offset  a starting offset into the byte array
     *@param  value   the double (64-bit) value
     */

    public static void putDouble(final byte[] data, final int offset,
            final double value) {
        // Excel likes NaN to be a specific value.
        if (Double.isNaN(value))
            putNumber(data, offset, -276939487313920L, DOUBLE_SIZE);
        else
            putNumber(data, offset, Double.doubleToLongBits(value), DOUBLE_SIZE);
    }


    /**
     *  put a double value into beginning of a byte array
     *
     *@param  data   the byte array
     *@param  value  the double (64-bit) value
     */

    public static void putDouble(final byte[] data, final double value) {
        putDouble(data, 0, value);
    }


    /**
     *  Exception to handle buffer underruns
     *
     *@author     Marc Johnson (mjohnson at apache dot org)
     */

    public static class BufferUnderrunException
             extends IOException {

        /**
         *  simple constructor
         */

        BufferUnderrunException() {
            super("buffer underrun");
        }
    }


    /**
     *  get a short value from an InputStream
     *
     *@param  stream                       the InputStream from which the short
     *      is to be read
     *@return                              the short (16-bit) value
     *@exception  IOException              will be propagated back to the caller
     *@exception  BufferUnderrunException  if the stream cannot provide enough
     *      bytes
     */

    public static short readShort(final InputStream stream)
             throws IOException, BufferUnderrunException {
        return getShort(readFromStream(stream, SHORT_SIZE));
    }


    /**
     *  get an int value from an InputStream
     *
     *@param  stream                       the InputStream from which the int is
     *      to be read
     *@return                              the int (32-bit) value
     *@exception  IOException              will be propagated back to the caller
     *@exception  BufferUnderrunException  if the stream cannot provide enough
     *      bytes
     */

    public static int readInt(final InputStream stream)
             throws IOException, BufferUnderrunException {
        return getInt(readFromStream(stream, INT_SIZE));
    }


    /**
     *  get a long value from an InputStream
     *
     *@param  stream                       the InputStream from which the long
     *      is to be read
     *@return                              the long (64-bit) value
     *@exception  IOException              will be propagated back to the caller
     *@exception  BufferUnderrunException  if the stream cannot provide enough
     *      bytes
     */

    public static long readLong(final InputStream stream)
             throws IOException, BufferUnderrunException {
        return getLong(readFromStream(stream, LONG_SIZE));
    }

    /**
     *  Read the appropriate number of bytes from the stream and return them to
     *  the caller. <p>
     *
     *  However, for the purposes of the POI project, this risk is deemed
     *  negligible. It is, however, so noted.
     *
     *@param  stream                       the InputStream we're reading from
     *@param  size                         the number of bytes to read; in
     *      99.99% of cases, this will be SHORT_SIZE, INT_SIZE, or LONG_SIZE --
     *      but it doesn't have to be.
     *@return                              the byte array containing the
     *      required number of bytes. The array will contain all zero's on end
     *      of stream
     *@exception  IOException              will be propagated back to the caller
     *@exception  BufferUnderrunException  if the stream cannot provide enough
     *      bytes
     */

    public static byte[] readFromStream(final InputStream stream,
            final int size)
             throws IOException, BufferUnderrunException {
        byte[] buffer = new byte[size];

        int count = stream.read(buffer);

        if (count == -1) {

            // return a zero-filled buffer
            Arrays.fill(buffer, (byte) 0);
        } else if (count != size) {
            throw new BufferUnderrunException();
        }
        return buffer;
    }


    /**
     *  Gets the number attribute of the LittleEndian class
     *
     *@param  data    Description of the Parameter
     *@param  offset  Description of the Parameter
     *@param  size    Description of the Parameter
     *@return         The number value
     */
    private static long getNumber(final byte[] data, final int offset,
            final int size) {
        long result = 0;

        for (int j = offset + size - 1; j >= offset; j--) {
            result <<= 8;
            result |= 0xff & data[j];
        }
        return result;
    }


    /**
     *  Description of the Method
     *
     *@param  data    Description of the Parameter
     *@param  offset  Description of the Parameter
     *@param  value   Description of the Parameter
     *@param  size    Description of the Parameter
     */
    private static void putNumber(final byte[] data, final int offset,
            final long value, final int size) {
        int limit = size + offset;
        long v = value;

        for (int j = offset; j < limit; j++) {
            data[j] = (byte) (v & 0xFF);
            v >>= 8;
        }
    }


    /**
     *  Convert an 'unsigned' byte to an integer. ie, don't carry across the
     *  sign.
     *
     *@param  b  Description of the Parameter
     *@return    Description of the Return Value
     */
    public static int ubyteToInt(byte b) {
        return ((b & 0x80) == 0 ? (int) b : (int) (b & (byte) 0x7f) + 0x80);
    }


    /**
     *  get the unsigned value of a byte.
     *
     *@param  data    the byte array.
     *@param  offset  a starting offset into the byte array.
     *@return         the unsigned value of the byte as a 32 bit integer
     */
    public static int getUnsignedByte(final byte[] data, final int offset) {
        return (int) getNumber(data, offset, BYTE_SIZE);
    }


    /**
     *  get the unsigned value of a byte.
     *
     *@param  data  the byte array
     *@return       the unsigned value of the byte as a 32 bit integer
     */
    public static int getUnsignedByte(final byte[] data) {
        return getUnsignedByte(data, 0);
    }


    /**
     *  Copy a portion of a byte array
     *
     *@param  data                        the original byte array
     *@param  offset                      Where to start copying from.
     *@param  size                        Number of bytes to copy.
     *@return                             The byteArray value
     *@throws  IndexOutOfBoundsException  - if copying would cause access of
     *      data outside array bounds.
     */
    public static byte[] getByteArray(final byte[] data, int offset, int size) {
        byte[] copy = new byte[size];
        System.arraycopy(data, offset, copy, 0, size);

        return copy;
    }


}
