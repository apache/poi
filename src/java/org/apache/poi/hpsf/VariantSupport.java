/*
 *  ====================================================================
 *  The Apache Software License, Version 1.1
 *
 *  Copyright (c) 2000 The Apache Software Foundation.  All rights
 *  reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *  notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in
 *  the documentation and/or other materials provided with the
 *  distribution.
 *
 *  3. The end-user documentation included with the redistribution,
 *  if any, must include the following acknowledgment:
 *  "This product includes software developed by the
 *  Apache Software Foundation (http://www.apache.org/)."
 *  Alternately, this acknowledgment may appear in the software itself,
 *  if and wherever such third-party acknowledgments normally appear.
 *
 *  4. The names "Apache" and "Apache Software Foundation" must
 *  not be used to endorse or promote products derived from this
 *  software without prior written permission. For written
 *  permission, please contact apache@apache.org.
 *
 *  5. Products derived from this software may not be called "Apache",
 *  nor may "Apache" appear in their name, without prior written
 *  permission of the Apache Software Foundation.
 *
 *  THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 *  ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 *  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 *  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 *  USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 *  OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 *  SUCH DAMAGE.
 *  ====================================================================
 *
 *  This software consists of voluntary contributions made by many
 *  individuals on behalf of the Apache Software Foundation.  For more
 *  information on the Apache Software Foundation, please see
 *  <http://www.apache.org/>.
 *
 *  Portions of this software are based upon public domain software
 *  originally written at the National Center for Supercomputing Applications,
 *  University of Illinois, Urbana-Champaign.
 *
 *  Portions of this software are based upon public domain software
 *  originally written at the National Center for Supercomputing Applications,
 *  University of Illinois, Urbana-Champaign.
 */
package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * <p>Supports reading and writing of variant data.</p>
 * 
 * <p><strong>FIXME:</strong> Reading and writing must be made more uniform than
 * it is now. The following items should be resolved:
 * 
 * <ul>
 *
 * <li><p>Reading requires a length parameter that is 4 byte greater than the
 * actual data, because the variant type field is included. </p></li>
 *
 * <li><p>Reading reads from a byte array while writing writes to an byte array
 * output stream.</p></li>
 *
 * <ul>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @since 08.08.2003
 * @version $Id$
 */
public class VariantSupport extends Variant
{

    /**
     * <p>Reads a variant data type from a byte array.</p>
     *
     * @param src The byte array
     * @param offset The offset in the byte array where the variant
     * starts
     * @param length The length of the variant including the variant
     * type field
     * @param type The variant type to read
     * @return A Java object that corresponds best to the variant
     * field. For example, a VT_I4 is returned as a {@link Long}, a
     * VT_LPSTR as a {@link String}.
     * @exception UnsupportedVariantTypeException if HPSF does not (yet)
     * support the variant type which is to be read 
     *
     * @see Variant
     */
    public static Object read(final byte[] src, final int offset,
                              final int length, final long type)
        throws ReadingNotSupportedException
    {
        Object value;
        int o1 = offset;
        int l1 = length - LittleEndian.INT_SIZE;
        switch ((int) type)
        {
            case Variant.VT_EMPTY:
            {
                value = null;
                break;
            }
            case Variant.VT_I2:
            {
                /*
                 * Read a short. In Java it is represented as an
                 * Integer object.
                 */
                value = new Integer(LittleEndian.getUShort(src, o1));
                break;
            }
            case Variant.VT_I4:
            {
                /*
                 * Read a word. In Java it is represented as a
                 * Long object.
                 */
                value = new Long(LittleEndian.getUInt(src, o1));
                break;
            }
            case Variant.VT_FILETIME:
            {
                /*
                 * Read a FILETIME object. In Java it is represented
                 * as a Date object.
                 */
                final long low = LittleEndian.getUInt(src, o1);
                o1 += LittleEndian.INT_SIZE;
                final long high = LittleEndian.getUInt(src, o1);
                value = Util.filetimeToDate((int) high, (int) low);
                break;
            }
            case Variant.VT_LPSTR:
            {
                /*
                 * Read a byte string. In Java it is represented as a
                 * String object. The 0x00 bytes at the end must be
                 * stripped.
                 *
                 * FIXME: Reading an 8-bit string should pay attention
                 * to the codepage. Currently the byte making out the
                 * property's value are interpreted according to the
                 * platform's default character set.
                 */
                final int first = o1 + LittleEndian.INT_SIZE;
                long last = first + LittleEndian.getUInt(src, o1) - 1;
                o1 += LittleEndian.INT_SIZE;
                while (src[(int) last] == 0 && first <= last)
                    last--;
                value = new String(src, (int) first, (int) (last - first + 1));
                break;
            }
            case Variant.VT_LPWSTR:
            {
                /*
                 * Read a Unicode string. In Java it is represented as
                 * a String object. The 0x00 bytes at the end must be
                 * stripped.
                 */
                final int first = o1 + LittleEndian.INT_SIZE;
                long last = first + LittleEndian.getUInt(src, o1) - 1;
                long l = last - first;
                o1 += LittleEndian.INT_SIZE;
                StringBuffer b = new StringBuffer((int) (last - first));
                for (int i = 0; i <= l; i++)
                {
                    final int i1 = o1 + (i * 2);
                    final int i2 = i1 + 1;
                    final int high = src[i2] << 8;
                    final int low = src[i1] & 0xff;
                    final char c = (char) (high | low);
                    b.append(c);
                }
                /* Strip 0x00 characters from the end of the string: */
                while (b.length() > 0 && b.charAt(b.length() - 1) == 0x00)
                    b.setLength(b.length() - 1);
                value = b.toString();
                break;
            }
            case Variant.VT_CF:
            {
                final byte[] v = new byte[l1];
                for (int i = 0; i < l1; i++)
                    v[i] = src[(int) (o1 + i)];
                value = v;
                break;
            }
            case Variant.VT_BOOL:
            {
                /*
                 * The first four bytes in src, from src[offset] to
                 * src[offset + 3] contain the DWord for VT_BOOL, so
                 * skip it, we don't need it.
                 */
                // final int first = offset + LittleEndian.INT_SIZE;
                long bool = LittleEndian.getUInt(src, o1);
                if (bool != 0)
                    value = new Boolean(true);
                else
                    value = new Boolean(false);
                break;
            }
            default:
            {
                final byte[] v = new byte[l1];
                for (int i = 0; i < l1; i++)
                    v[i] = src[(int) (o1 + i)];
                throw new ReadingNotSupportedException(type, v);
            }
        }
        return value;
    }



    /**
     * <p>Writes a variant value to an output stream.</p>
     *
     * @param out The stream to write the value to.
     * @param type The variant's type.
     * @param value The variant's value.
     * @return The number of entities that have been written. In many cases an
     * "entity" is a byte but this is not always the case.
     */
    public static int write(final OutputStream out, final long type,
                               final Object value)
        throws IOException, WritingNotSupportedException
    {
        switch ((int) type)
        {
            case Variant.VT_BOOL:
            {
                int trueOrFalse;
                int length = 0;
                if (((Boolean) value).booleanValue())
                    trueOrFalse = 1;
                else
                    trueOrFalse = 0;
                length += TypeWriter.writeUIntToStream(out, trueOrFalse);
                return length;
            }
            case Variant.VT_LPSTR:
            {
                TypeWriter.writeUIntToStream
                    (out, ((String) value).length() + 1);
                char[] s = toPaddedCharArray((String) value);
                /* FIXME: The following line forces characters to bytes. This
                 * is generally wrong and should only be done according to a
                 * codepage. Alternatively Unicode could be written (see 
                 * Variant.VT_LPWSTR). */
                byte[] b = new byte[s.length];
                for (int i = 0; i < s.length; i++)
                    b[i] = (byte) s[i];
                out.write(b);
                return b.length;
            }
            case Variant.VT_LPWSTR:
            {
                final int nrOfChars = ((String) value).length() + 1; 
                TypeWriter.writeUIntToStream(out, nrOfChars);
                char[] s = toPaddedCharArray((String) value);
                for (int i = 0; i < s.length; i++)
                {
                    final int high = (int) ((s[i] & 0xff00) >> 8);
                    final int low = (int) (s[i] & 0x00ff);
                    final byte highb = (byte) high;
                    final byte lowb = (byte) low;
                    out.write(lowb);
                    out.write(highb);
                }
                return nrOfChars * 2;
            }
            case Variant.VT_CF:
            {
                final byte[] b = (byte[]) value; 
                out.write(b);
                return b.length;
            }
            case Variant.VT_EMPTY:
            {
                TypeWriter.writeUIntToStream(out, Variant.VT_EMPTY);
                return LittleEndianConsts.INT_SIZE;
            }
            case Variant.VT_I2:
            {
                TypeWriter.writeToStream(out, ((Integer) value).shortValue());
                return LittleEndianConsts.SHORT_SIZE;
            }
            case Variant.VT_I4:
            {
                TypeWriter.writeToStream(out, ((Long) value).intValue());
                return LittleEndianConsts.INT_SIZE;
            }
            case Variant.VT_FILETIME:
            {
                int length = 0;
                long filetime = Util.dateToFileTime((Date) value);
                int high = (int) ((filetime >> 32) & 0xFFFFFFFFL);
                int low = (int) (filetime & 0x00000000FFFFFFFFL);
                length += TypeWriter.writeUIntToStream(out, 0x0000000FFFFFFFFL & low);
                length += TypeWriter.writeUIntToStream(out, 0x0000000FFFFFFFFL & high);
                return length;
            }
            default:
            {
                throw new WritingNotSupportedException(type, value);
             }
        }
    }



    /**
     * <p>Converts a string into a 0x00-terminated character sequence padded 
     * with 0x00 bytes to a multiple of 4.</p>
     *
     * @param value The string to convert
     * @return The padded character array
     */
    private static char[] toPaddedCharArray(final String s)
    {
        final int PADDING = 4;
        int dl = s.length() + 1;
        final int r = dl % 4;
        if (r > 0)
            dl += PADDING - r;
        char[] buffer = new char[dl];
        s.getChars(0, s.length(), buffer, 0);
        for (int i = s.length(); i < dl; i++)
            buffer[i] = (char) 0;
        return buffer;
    }



    public static int getLength(final long variantType, final int lengthInBytes)
    {
        switch ((int) variantType)
        {
            case VT_LPWSTR:
                return lengthInBytes / 2;
            default:
                return lengthInBytes;
        }
    }
}