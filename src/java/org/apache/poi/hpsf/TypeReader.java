/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

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

package org.apache.poi.hpsf;

import org.apache.poi.util.LittleEndian;

/**
 * <p>Reader for specific data types.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @see Property
 * @see Variant
 * @version $Id$
 * @since 2002-12-09
 */
public class TypeReader
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
     *
     * @see Variant
     */
    public static Object read(final byte[] src, int offset, int length,
                              final int type)
    {
        /*
         * FIXME: Support reading more types and clean up this code!
         */
        Object value;
        length = length - LittleEndian.INT_SIZE;
        switch (type)
        {
            case Variant.VT_EMPTY:
            {
                /*
                 * FIXME: The value returned by this case relies on the
                 * assumption that the value VT_EMPTY denotes consists of zero 
                 * bytes. I'd be glad if some could confirm or correct this. 
                 */
                value = null;
                break;
            }
            case Variant.VT_I2:
            {
                /*
                 * Read a short. In Java it is represented as an
                 * Integer object.
                 */
                value = new Integer(LittleEndian.getUShort(src, offset));
                break;
            }
            case Variant.VT_I4:
            {
                /*
                 * Read a word. In Java it is represented as a
                 * Long object.
                 */
                value = new Long(LittleEndian.getUInt(src, offset));
                break;
            }
            case Variant.VT_FILETIME:
            {
                /*
                 * Read a FILETIME object. In Java it is represented
                 * as a Date object.
                 */
                final long low = LittleEndian.getUInt(src, offset);
                offset += LittleEndian.INT_SIZE;
                final long high = LittleEndian.getUInt(src, offset);
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
                final int first = offset + LittleEndian.INT_SIZE;
                long last = first + LittleEndian.getUInt(src, offset) - 1;
                offset += LittleEndian.INT_SIZE;
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
                final int first = offset + LittleEndian.INT_SIZE;
                long last = first + LittleEndian.getUInt(src, offset) - 1;
                long l = last - first;
                offset += LittleEndian.INT_SIZE;
                StringBuffer b = new StringBuffer((int) (last - first));
                for (int i = 0; i <= l; i++)
                {
                    final int i1 = offset + (i * 2);
                    final int i2 = i1 + 1;
                    b.append((char) ((src[i2] << 8) + src[i1]));
                }
                /* Strip 0x00 characters from the end of the string: */
                while (b.charAt(b.length() - 1) == 0x00)
                    b.setLength(b.length() - 1);
                value = b.toString();
                break;
            }
            case Variant.VT_CF:
            {
                final byte[] v = new byte[length];
                for (int i = 0; i < length; i++)
                    v[i] = src[(int) (offset + i)];
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
                long bool = LittleEndian.getUInt(src, offset);
                if (bool != 0)
                    value = new Boolean(true);
                else
                    value = new Boolean(false);
                break;
            }
            default:
            {
                final byte[] v = new byte[length];
                for (int i = 0; i < length; i++)
                    v[i] = src[(int) (offset + i)];
                value = v;
                break;
            }
        }
        return value;
    }

}
