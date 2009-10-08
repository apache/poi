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

package org.apache.poi.hpsf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * <p>Supports reading and writing of variant data.</p>
 *
 * <p><strong>FIXME (3):</strong> Reading and writing should be made more
 * uniform than it is now. The following items should be resolved:
 *
 * <ul>
 *
 * <li><p>Reading requires a length parameter that is 4 byte greater than the
 * actual data, because the variant type field is included. </p></li>
 *
 * <li><p>Reading reads from a byte array while writing writes to an byte array
 * output stream.</p></li>
 *
 * </ul>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 */
public class VariantSupport extends Variant
{

    private static boolean logUnsupportedTypes = false;

    /**
     * <p>Specifies whether warnings about unsupported variant types are to be
     * written to <code>System.err</code> or not.</p>
     *
     * @param logUnsupportedTypes If <code>true</code> warnings will be written,
     * if <code>false</code> they won't.
     */
    public static void setLogUnsupportedTypes(final boolean logUnsupportedTypes)
    {
        VariantSupport.logUnsupportedTypes = logUnsupportedTypes;
    }

    /**
     * <p>Checks whether logging of unsupported variant types warning is turned
     * on or off.</p>
     *
     * @return <code>true</code> if logging is turned on, else
     * <code>false</code>.
     */
    public static boolean isLogUnsupportedTypes()
    {
        return logUnsupportedTypes;
    }



    /**
     * <p>Keeps a list of the variant types an "unsupported" message has already
     * been issued for.</p>
     */
    protected static List unsupportedMessage;

    /**
     * <p>Writes a warning to <code>System.err</code> that a variant type is
     * unsupported by HPSF. Such a warning is written only once for each variant
     * type. Log messages can be turned on or off by </p>
     *
     * @param ex The exception to log
     */
    protected static void writeUnsupportedTypeMessage
        (final UnsupportedVariantTypeException ex)
    {
        if (isLogUnsupportedTypes())
        {
            if (unsupportedMessage == null)
                unsupportedMessage = new LinkedList();
            Long vt = Long.valueOf(ex.getVariantType());
            if (!unsupportedMessage.contains(vt))
            {
                System.err.println(ex.getMessage());
                unsupportedMessage.add(vt);
            }
        }
    }


    /**
     * <p>HPSF is able to read these {@link Variant} types.</p>
     */
    final static public int[] SUPPORTED_TYPES = { Variant.VT_EMPTY,
            Variant.VT_I2, Variant.VT_I4, Variant.VT_I8, Variant.VT_R8,
            Variant.VT_FILETIME, Variant.VT_LPSTR, Variant.VT_LPWSTR,
            Variant.VT_CF, Variant.VT_BOOL };



    /**
     * <p>Checks whether HPSF supports the specified variant type. Unsupported
     * types should be implemented included in the {@link #SUPPORTED_TYPES}
     * array.</p>
     *
     * @see Variant
     * @param variantType the variant type to check
     * @return <code>true</code> if HPFS supports this type, else
     *         <code>false</code>
     */
    public boolean isSupportedType(final int variantType)
    {
        for (int i = 0; i < SUPPORTED_TYPES.length; i++)
            if (variantType == SUPPORTED_TYPES[i])
                return true;
        return false;
    }



    /**
     * <p>Reads a variant type from a byte array.</p>
     *
     * @param src The byte array
     * @param offset The offset in the byte array where the variant starts
     * @param length The length of the variant including the variant type field
     * @param type The variant type to read
     * @param codepage The codepage to use for non-wide strings
     * @return A Java object that corresponds best to the variant field. For
     *         example, a VT_I4 is returned as a {@link Long}, a VT_LPSTR as a
     *         {@link String}.
     * @exception ReadingNotSupportedException if a property is to be written
     *            who's variant type HPSF does not yet support
     * @exception UnsupportedEncodingException if the specified codepage is not
     *            supported.
     * @see Variant
     */
    public static Object read(final byte[] src, final int offset,
                              final int length, final long type,
                              final int codepage)
    throws ReadingNotSupportedException, UnsupportedEncodingException
    {
        Object value;
        int o1 = offset;
        int l1 = length - LittleEndian.INT_SIZE;
        long lType = type;

        /* Instead of trying to read 8-bit characters from a Unicode string,
         * read 16-bit characters. */
        if (codepage == Constants.CP_UNICODE && type == Variant.VT_LPSTR)
            lType = Variant.VT_LPWSTR;

        switch ((int) lType)
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
                value = Integer.valueOf(LittleEndian.getShort(src, o1));
                break;
            }
            case Variant.VT_I4:
            {
                /*
                 * Read a word. In Java it is represented as an
                 * Integer object.
                 */
                value = Integer.valueOf(LittleEndian.getInt(src, o1));
                break;
            }
            case Variant.VT_I8:
            {
                /*
                 * Read a double word. In Java it is represented as a
                 * Long object.
                 */
                value = Long.valueOf(LittleEndian.getLong(src, o1));
                break;
            }
            case Variant.VT_R8:
            {
                /*
                 * Read an eight-byte double value. In Java it is represented as
                 * a Double object.
                 */
                value = new Double(LittleEndian.getDouble(src, o1));
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
                 */
                final int first = o1 + LittleEndian.INT_SIZE;
                long last = first + LittleEndian.getUInt(src, o1) - 1;
                o1 += LittleEndian.INT_SIZE;
                while (src[(int) last] == 0 && first <= last)
                    last--;
                final int l = (int) (last - first + 1);
                value = codepage != -1 ?
                    new String(src, first, l,
                               codepageToEncoding(codepage)) :
                    new String(src, first, l);
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
                    final int low = src[i1] & 0x00ff;
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
                if(l1 < 0) {
                    /**
                     *  YK: reading the ClipboardData packet (VT_CF) is not quite correct.
                     *  The size of the data is determined by the first four bytes of the packet
                     *  while the current implementation calculates it in the Section constructor.
                     *  Test files in Bugzilla 42726 and 45583 clearly show that this approach does not always work.
                     *  The workaround below attempts to gracefully handle such cases instead of throwing exceptions.
                     *
                     *  August 20, 2009
                     */
                    l1 = LittleEndian.getInt(src, o1); o1 += LittleEndian.INT_SIZE;
                }
                final byte[] v = new byte[l1];
                System.arraycopy(src, o1, v, 0, v.length);
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
                    value = Boolean.TRUE;
                else
                    value = Boolean.FALSE;
                break;
            }
            default:
            {
                final byte[] v = new byte[l1];
                for (int i = 0; i < l1; i++)
                    v[i] = src[(o1 + i)];
                throw new ReadingNotSupportedException(type, v);
            }
        }
        return value;
    }



    /**
     * <p>Turns a codepage number into the equivalent character encoding's
     * name.</p>
     *
     * @param codepage The codepage number
     *
     * @return The character encoding's name. If the codepage number is 65001,
     * the encoding name is "UTF-8". All other positive numbers are mapped to
     * "cp" followed by the number, e.g. if the codepage number is 1252 the
     * returned character encoding name will be "cp1252".
     *
     * @exception UnsupportedEncodingException if the specified codepage is
     * less than zero.
     */
    public static String codepageToEncoding(final int codepage)
    throws UnsupportedEncodingException
    {
        if (codepage <= 0)
            throw new UnsupportedEncodingException
                ("Codepage number may not be " + codepage);
        switch (codepage)
        {
            case Constants.CP_UTF16:
                return "UTF-16";
            case Constants.CP_UTF16_BE:
                return "UTF-16BE";
            case Constants.CP_UTF8:
                return "UTF-8";
            case Constants.CP_037:
                return "cp037";
            case Constants.CP_GBK:
                return "GBK";
            case Constants.CP_MS949:
                return "ms949";
            case Constants.CP_WINDOWS_1250:
                return "windows-1250";
            case Constants.CP_WINDOWS_1251:
                return "windows-1251";
            case Constants.CP_WINDOWS_1252:
                return "windows-1252";
            case Constants.CP_WINDOWS_1253:
                return "windows-1253";
            case Constants.CP_WINDOWS_1254:
                return "windows-1254";
            case Constants.CP_WINDOWS_1255:
                return "windows-1255";
            case Constants.CP_WINDOWS_1256:
                return "windows-1256";
            case Constants.CP_WINDOWS_1257:
                return "windows-1257";
            case Constants.CP_WINDOWS_1258:
                return "windows-1258";
            case Constants.CP_JOHAB:
                return "johab";
            case Constants.CP_MAC_ROMAN:
                return "MacRoman";
            case Constants.CP_MAC_JAPAN:
                return "SJIS";
            case Constants.CP_MAC_CHINESE_TRADITIONAL:
                return "Big5";
            case Constants.CP_MAC_KOREAN:
                return "EUC-KR";
            case Constants.CP_MAC_ARABIC:
                return "MacArabic";
            case Constants.CP_MAC_HEBREW:
                return "MacHebrew";
            case Constants.CP_MAC_GREEK:
                return "MacGreek";
            case Constants.CP_MAC_CYRILLIC:
                return "MacCyrillic";
            case Constants.CP_MAC_CHINESE_SIMPLE:
                return "EUC_CN";
            case Constants.CP_MAC_ROMANIA:
                return "MacRomania";
            case Constants.CP_MAC_UKRAINE:
                return "MacUkraine";
            case Constants.CP_MAC_THAI:
                return "MacThai";
            case Constants.CP_MAC_CENTRAL_EUROPE:
                return "MacCentralEurope";
            case Constants.CP_MAC_ICELAND:
                  return "MacIceland";
            case Constants.CP_MAC_TURKISH:
                return "MacTurkish";
            case Constants.CP_MAC_CROATIAN:
                return "MacCroatian";
            case Constants.CP_US_ACSII:
            case Constants.CP_US_ASCII2:
                return "US-ASCII";
            case Constants.CP_KOI8_R:
                return "KOI8-R";
            case Constants.CP_ISO_8859_1:
                return "ISO-8859-1";
            case Constants.CP_ISO_8859_2:
                return "ISO-8859-2";
            case Constants.CP_ISO_8859_3:
                return "ISO-8859-3";
            case Constants.CP_ISO_8859_4:
                return "ISO-8859-4";
            case Constants.CP_ISO_8859_5:
                return "ISO-8859-5";
            case Constants.CP_ISO_8859_6:
                return "ISO-8859-6";
            case Constants.CP_ISO_8859_7:
                return "ISO-8859-7";
            case Constants.CP_ISO_8859_8:
                return "ISO-8859-8";
            case Constants.CP_ISO_8859_9:
                return "ISO-8859-9";
            case Constants.CP_ISO_2022_JP1:
            case Constants.CP_ISO_2022_JP2:
            case Constants.CP_ISO_2022_JP3:
                return "ISO-2022-JP";
            case Constants.CP_ISO_2022_KR:
                return "ISO-2022-KR";
            case Constants.CP_EUC_JP:
                return "EUC-JP";
            case Constants.CP_EUC_KR:
                return "EUC-KR";
            case Constants.CP_GB2312:
                return "GB2312";
            case Constants.CP_GB18030:
                return "GB18030";
            case Constants.CP_SJIS:
                return "SJIS";
            default:
                return "cp" + codepage;
        }
    }


    /**
     * <p>Writes a variant value to an output stream. This method ensures that
     * always a multiple of 4 bytes is written.</p>
     *
     * <p>If the codepage is UTF-16, which is encouraged, strings
     * <strong>must</strong> always be written as {@link Variant#VT_LPWSTR}
     * strings, not as {@link Variant#VT_LPSTR} strings. This method ensure this
     * by converting strings appropriately, if needed.</p>
     *
     * @param out The stream to write the value to.
     * @param type The variant's type.
     * @param value The variant's value.
     * @param codepage The codepage to use to write non-wide strings
     * @return The number of entities that have been written. In many cases an
     * "entity" is a byte but this is not always the case.
     * @exception IOException if an I/O exceptions occurs
     * @exception WritingNotSupportedException if a property is to be written
     * who's variant type HPSF does not yet support
     */
    public static int write(final OutputStream out, final long type,
                            final Object value, final int codepage)
        throws IOException, WritingNotSupportedException
    {
        int length = 0;
        switch ((int) type)
        {
            case Variant.VT_BOOL:
            {
                int trueOrFalse;
                if (((Boolean) value).booleanValue())
                    trueOrFalse = 1;
                else
                    trueOrFalse = 0;
                length = TypeWriter.writeUIntToStream(out, trueOrFalse);
                break;
            }
            case Variant.VT_LPSTR:
            {
                final byte[] bytes =
                    (codepage == -1 ?
                    ((String) value).getBytes() :
                    ((String) value).getBytes(codepageToEncoding(codepage)));
                length = TypeWriter.writeUIntToStream(out, bytes.length + 1);
                final byte[] b = new byte[bytes.length + 1];
                System.arraycopy(bytes, 0, b, 0, bytes.length);
                b[b.length - 1] = 0x00;
                out.write(b);
                length += b.length;
                break;
            }
            case Variant.VT_LPWSTR:
            {
                final int nrOfChars = ((String) value).length() + 1;
                length += TypeWriter.writeUIntToStream(out, nrOfChars);
                char[] s = Util.pad4((String) value);
                for (int i = 0; i < s.length; i++)
                {
                    final int high = ((s[i] & 0x0000ff00) >> 8);
                    final int low = (s[i] & 0x000000ff);
                    final byte highb = (byte) high;
                    final byte lowb = (byte) low;
                    out.write(lowb);
                    out.write(highb);
                    length += 2;
                }
                out.write(0x00);
                out.write(0x00);
                length += 2;
                break;
            }
            case Variant.VT_CF:
            {
                final byte[] b = (byte[]) value;
                out.write(b);
                length = b.length;
                break;
            }
            case Variant.VT_EMPTY:
            {
                TypeWriter.writeUIntToStream(out, Variant.VT_EMPTY);
                length = LittleEndianConsts.INT_SIZE;
                break;
            }
            case Variant.VT_I2:
            {
                TypeWriter.writeToStream(out, ((Integer) value).shortValue());
                length = LittleEndianConsts.SHORT_SIZE;
                break;
            }
            case Variant.VT_I4:
            {
                if (!(value instanceof Integer))
                {
                    throw new ClassCastException("Could not cast an object to "
                            + Integer.class.toString() + ": "
                            + value.getClass().toString() + ", "
                            + value.toString());
                }
                length += TypeWriter.writeToStream(out,
                          ((Integer) value).intValue());
                break;
            }
            case Variant.VT_I8:
            {
                TypeWriter.writeToStream(out, ((Long) value).longValue());
                length = LittleEndianConsts.LONG_SIZE;
                break;
            }
            case Variant.VT_R8:
            {
                length += TypeWriter.writeToStream(out,
                          ((Double) value).doubleValue());
                break;
            }
            case Variant.VT_FILETIME:
            {
                long filetime = Util.dateToFileTime((Date) value);
                int high = (int) ((filetime >> 32) & 0x00000000FFFFFFFFL);
                int low = (int) (filetime & 0x00000000FFFFFFFFL);
                length += TypeWriter.writeUIntToStream
                    (out, 0x0000000FFFFFFFFL & low);
                length += TypeWriter.writeUIntToStream
                    (out, 0x0000000FFFFFFFFL & high);
                break;
            }
            default:
            {
                /* The variant type is not supported yet. However, if the value
                 * is a byte array we can write it nevertheless. */
                if (value instanceof byte[])
                {
                    final byte[] b = (byte[]) value;
                    out.write(b);
                    length = b.length;
                    writeUnsupportedTypeMessage
                        (new WritingNotSupportedException(type, value));
                }
                else
                    throw new WritingNotSupportedException(type, value);
                break;
            }
        }

        return length;
    }
}
