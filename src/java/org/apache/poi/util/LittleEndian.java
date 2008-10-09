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

/**
 *  a utility class for handling little-endian numbers, which the 80x86 world is
 *  replete with. The methods are all static, and input/output is from/to byte
 *  arrays, or from InputStreams.
 *
 *@author     Marc Johnson (mjohnson at apache dot org)
 *@author     Andrew Oliver (acoliver at apache dot org)
 */
public final class LittleEndian implements LittleEndianConsts {

    private LittleEndian() {
    	// no instances of this class
    }


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
            retNum = (Short.MAX_VALUE + 1) * 2 + num;
        } else {
            retNum = num;
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
        int size = (int) getNumber(data, offset, SHORT_SIZE);
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
            retNum = ((long) Integer.MAX_VALUE + 1) * 2 + num;
        } else {
            retNum = num;
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
     * executes:<p/>
     * <code>
     * data[offset] = (byte)value;
     * </code></p>
     * Added for consistency with other put~() methods
     */
    public static void putByte(byte[] data, int offset, int value) {
        putNumber(data, offset, value, LittleEndianConsts.BYTE_SIZE);
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

    public static final class BufferUnderrunException extends IOException {

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
    public static short readShort(InputStream stream) throws IOException, BufferUnderrunException {

		return (short) readUShort(stream);
	}

	public static int readUShort(InputStream stream) throws IOException, BufferUnderrunException {

		int ch1 = stream.read();
		int ch2 = stream.read();
		if ((ch1 | ch2) < 0) {
			throw new BufferUnderrunException();
		}
		return ((ch2 << 8) + (ch1 << 0));
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
		int ch1 = stream.read();
		int ch2 = stream.read();
		int ch3 = stream.read();
		int ch4 = stream.read();
		if ((ch1 | ch2 | ch3 | ch4) < 0) {
			throw new BufferUnderrunException();
		}
		return ((ch4 << 24) + (ch3<<16) + (ch2 << 8) + (ch1 << 0));
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
		int ch1 = stream.read();
		int ch2 = stream.read();
		int ch3 = stream.read();
		int ch4 = stream.read();
		int ch5 = stream.read();
		int ch6 = stream.read();
		int ch7 = stream.read();
		int ch8 = stream.read();
		if ((ch1 | ch2 | ch3 | ch4 | ch5 | ch6 | ch7 | ch8) < 0) {
			throw new BufferUnderrunException();
		}
		
		return 
			((long)ch8 << 56) +
            ((long)ch7 << 48) +
            ((long)ch6 << 40) +
            ((long)ch5 << 32) +
            ((long)ch4 << 24) + // cast to long to preserve bit 31 (sign bit for ints)
                  (ch3 << 16) +
                  (ch2 <<  8) +
                  (ch1 <<  0);
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
        return ((b & 0x80) == 0 ? (int) b : (b & (byte) 0x7f) + 0x80);
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

    /**
     * <p>Gets an unsigned int value (8 bytes) from a byte array.</p>
     * 
     * @param data the byte array
     * @param offset a starting offset into the byte array
     * @return the unsigned int (32-bit) value in a long
     */
    public static long getULong(final byte[] data, final int offset)
    {
        int num = (int) getNumber(data, offset, LONG_SIZE);
        long retNum;
        if (num < 0)
            retNum = ((long) Integer.MAX_VALUE + 1) * 2 + num;
        else
            retNum = num;
        return retNum;
    }

}
