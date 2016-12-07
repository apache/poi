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
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * A property in a {@link Section} of a {@link PropertySet}.<p>
 *
 * The property's {@code ID} gives the property a meaning
 * in the context of its {@link Section}. Each {@link Section} spans
 * its own name space of property IDs.<p>
 *
 * The property's {@code type} determines how its
 * {@code value} is interpreted. For example, if the type is
 * {@link Variant#VT_LPSTR} (byte string), the value consists of a
 * DWord telling how many bytes the string contains. The bytes follow
 * immediately, including any null bytes that terminate the
 * string. The type {@link Variant#VT_I4} denotes a four-byte integer
 * value, {@link Variant#VT_FILETIME} some date and time (of a file).<p>
 *
 * Please note that not all {@link Variant} types yet. This might change
 * over time but largely depends on your feedback so that the POI team knows
 * which variant types are really needed. So please feel free to submit error
 * reports or patches for the types you need.
 *
 * @see Section
 * @see Variant
 * @see <a href="https://msdn.microsoft.com/en-us/library/dd942421.aspx">
 * [MS-OLEPS]: Object Linking and Embedding (OLE) Property Set Data Structures</a>
 */
public class Property {

    /** The property's ID. */
    private long id;

    /** The property's type. */
    private long type;

    /** The property's value. */
    private Object value;


    /**
     * Creates an empty property. It must be filled using the set method to be usable.
     */
    public Property() {
    }

    /**
     * Creates a {@code Property} as a copy of an existing {@code Property}.
     *
     * @param p The property to copy.
     */
    public Property(Property p) {
        this(p.id, p.type, p.value);
    }
    
    /**
     * Creates a property.
     *
     * @param id the property's ID.
     * @param type the property's type, see {@link Variant}.
     * @param value the property's value. Only certain types are allowed, see
     *        {@link Variant}.
     */
    public Property(final long id, final long type, final Object value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    /**
     * Creates a {@link Property} instance by reading its bytes
     * from the property set stream.
     *
     * @param id The property's ID.
     * @param src The bytes the property set stream consists of.
     * @param offset The property's type/value pair's offset in the
     * section.
     * @param length The property's type/value pair's length in bytes.
     * @param codepage The section's and thus the property's
     * codepage. It is needed only when reading string values.
     * @exception UnsupportedEncodingException if the specified codepage is not
     * supported.
     */
    public Property(final long id, final byte[] src, final long offset, final int length, final int codepage)
    throws UnsupportedEncodingException {
        this.id = id;

        /*
         * ID 0 is a special case since it specifies a dictionary of
         * property IDs and property names.
         */
        if (id == 0) {
            value = readDictionary(src, offset, length, codepage);
            return;
        }

        int o = (int) offset;
        type = LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;

        try {
            value = VariantSupport.read(src, o, length, (int) type, codepage);
        } catch (UnsupportedVariantTypeException ex) {
            VariantSupport.writeUnsupportedTypeMessage(ex);
            value = ex.getValue();
        }
    }



    /**
     * Returns the property's ID.
     *
     * @return The ID value
     */
    public long getID() {
        return id;
    }

    /**
     * Sets the property's ID.
     *
     * @param id the ID
     */
    public void setID(final long id) {
        this.id = id;
    }

    /**
     * Returns the property's type.
     *
     * @return The type value
     */
    public long getType() {
        return type;
    }

    /**
     * Sets the property's type.
     *
     * @param type the property's type
     */
    public void setType(final long type) {
        this.type = type;
    }

    /**
     * Returns the property's value.
     *
     * @return The property's value
     */
    public Object getValue() {
        return value;
    }
    
    /**
     * Sets the property's value.
     *
     * @param value the property's value
     */
    public void setValue(final Object value) {
        this.value = value;
    }



    

    /**
     * Reads a dictionary.
     *
     * @param src The byte array containing the bytes making out the dictionary.
     * @param offset At this offset within {@code src} the dictionary
     *        starts.
     * @param length The dictionary contains at most this many bytes.
     * @param codepage The codepage of the string values.
     * @return The dictonary
     * @throws UnsupportedEncodingException if the dictionary's codepage is not
     *         (yet) supported.
     */
    protected Map<?, ?> readDictionary(final byte[] src, final long offset, final int length, final int codepage)
    throws UnsupportedEncodingException {
        /* Check whether "offset" points into the "src" array". */
        if (offset < 0 || offset > src.length) {
            throw new HPSFRuntimeException
                ("Illegal offset " + offset + " while HPSF stream contains " +
                 length + " bytes.");
        }
        int o = (int) offset;

        /*
         * Read the number of dictionary entries.
         */
        final long nrEntries = LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;

        final Map<Object, Object> m = new LinkedHashMap<Object, Object>((int) nrEntries, (float) 1.0 );

        try {
            for (int i = 0; i < nrEntries; i++) {
                /* The key. */
                final Long id = Long.valueOf(LittleEndian.getUInt(src, o));
                o += LittleEndian.INT_SIZE;

                /* The value (a string). The length is the either the
                 * number of (two-byte) characters if the character set is Unicode
                 * or the number of bytes if the character set is not Unicode.
                 * The length includes terminating 0x00 bytes which we have to strip
                 * off to create a Java string. */
                long sLength = LittleEndian.getUInt(src, o);
                o += LittleEndian.INT_SIZE;

                /* Read the string. */
                final StringBuffer b = new StringBuffer();
                switch (codepage) {
                    case -1:
                        /* Without a codepage the length is equal to the number of
                         * bytes. */
                        b.append(new String(src, o, (int) sLength, Charset.forName("ASCII")));
                        break;
                    case CodePageUtil.CP_UNICODE:
                        /* The length is the number of characters, i.e. the number
                         * of bytes is twice the number of the characters. */
                        final int nrBytes = (int) (sLength * 2);
                        final byte[] h = new byte[nrBytes];
                        for (int i2 = 0; i2 < nrBytes; i2 += 2)
                        {
                            h[i2] = src[o + i2 + 1];
                            h[i2 + 1] = src[o + i2];
                        }
                        b.append(new String(h, 0, nrBytes, CodePageUtil.codepageToEncoding(codepage)));
                        break;
                    default:
                        /* For encodings other than Unicode the length is the number
                         * of bytes. */
                        b.append(new String(src, o, (int) sLength, CodePageUtil.codepageToEncoding(codepage)));
                        break;
                }

                /* Strip 0x00 characters from the end of the string: */
                while (b.length() > 0 && b.charAt(b.length() - 1) == 0x00) {
                    b.setLength(b.length() - 1);
                }
                if (codepage == CodePageUtil.CP_UNICODE) {
                    if (sLength % 2 == 1) {
                        sLength++;
                    }
                    o += (sLength + sLength);
                } else {
                    o += sLength;
                }
                m.put(id, b.toString());
            }
        } catch (RuntimeException ex) {
            final POILogger l = POILogFactory.getLogger(getClass());
            l.log(POILogger.WARN,
                    "The property set's dictionary contains bogus data. "
                    + "All dictionary entries starting with the one with ID "
                    + id + " will be ignored.", ex);
        }
        return m;
    }



    /**
     * Returns the property's size in bytes. This is always a multiple of 4.
     *
     * @return the property's size in bytes
     *
     * @exception WritingNotSupportedException if HPSF does not yet support the
     * property's variant type.
     */
    protected int getSize() throws WritingNotSupportedException
    {
        int length = VariantSupport.getVariantLength(type);
        if (length >= 0) {
            return length; /* Fixed length */
        }
        if (length == -2) {
            /* Unknown length */
            throw new WritingNotSupportedException(type, null);
        }

        /* Variable length: */
        final int PADDING = 4; /* Pad to multiples of 4. */
        switch ((int) type) {
            case Variant.VT_LPSTR: {
                int l = ((String) value).length() + 1;
                int r = l % PADDING;
                if (r > 0)
                    l += PADDING - r;
                length += l;
                break;
            }
            case Variant.VT_EMPTY:
                break;
            default:
                throw new WritingNotSupportedException(type, value);
        }
        return length;
    }



    /**
     * Compares two properties.<p>
     * 
     * Please beware that a property with
     * ID == 0 is a special case: It does not have a type, and its value is the
     * section's dictionary. Another special case are strings: Two properties
     * may have the different types Variant.VT_LPSTR and Variant.VT_LPWSTR;
     *
     * @see Object#equals(java.lang.Object)
     */
    public boolean equals(final Object o) {
        if (!(o instanceof Property)) {
            return false;
        }
        final Property p = (Property) o;
        final Object pValue = p.getValue();
        final long pId = p.getID();
        if (id != pId || (id != 0 && !typesAreEqual(type, p.getType()))) {
            return false;
        }
        if (value == null && pValue == null) {
            return true;
        }
        if (value == null || pValue == null) {
            return false;
        }

        /* It's clear now that both values are non-null. */
        final Class<?> valueClass = value.getClass();
        final Class<?> pValueClass = pValue.getClass();
        if (!(valueClass.isAssignableFrom(pValueClass)) &&
            !(pValueClass.isAssignableFrom(valueClass))) {
            return false;
        }

        if (value instanceof byte[]) {
            return Arrays.equals((byte[]) value, (byte[]) pValue);
        }

        return value.equals(pValue);
    }



    private boolean typesAreEqual(final long t1, final long t2) {
        return (t1 == t2 ||
            (t1 == Variant.VT_LPSTR && t2 == Variant.VT_LPWSTR) ||
            (t2 == Variant.VT_LPSTR && t1 == Variant.VT_LPWSTR));
    }



    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        long hashCode = 0;
        hashCode += id;
        hashCode += type;
        if (value != null) {
            hashCode += value.hashCode();
        }
        return (int) (hashCode & 0x0ffffffffL );

    }



    /**
     * @see Object#toString()
     */
    public String toString() {
        final StringBuffer b = new StringBuffer();
        b.append(getClass().getName());
        b.append('[');
        b.append("id: ");
        b.append(getID());
        b.append(", type: ");
        b.append(getType());
        final Object value = getValue();
        b.append(", value: ");
        if (value instanceof String) {
            b.append(value.toString());
            final String s = (String) value;
            final int l = s.length();
            final byte[] bytes = new byte[l * 2];
            for (int i = 0; i < l; i++) {
                final char c = s.charAt(i);
                final byte high = (byte) ((c & 0x00ff00) >> 8);
                final byte low  = (byte) ((c & 0x0000ff) >> 0);
                bytes[i * 2]     = high;
                bytes[i * 2 + 1] = low;
            }
            b.append(" [");
            if(bytes.length > 0) {
                final String hex = HexDump.dump(bytes, 0L, 0);
                b.append(hex);
            }
            b.append("]");
        } else if (value instanceof byte[]) {
            byte[] bytes = (byte[])value;
            if(bytes.length > 0) {
                String hex = HexDump.dump(bytes, 0L, 0);
                b.append(hex);
            }
        } else {
            b.append(value.toString());
        }
        b.append(']');
        return b.toString();
    }

    /**
     * Writes the property to an output stream.
     *
     * @param out The output stream to write to.
     * @param codepage The codepage to use for writing non-wide strings
     * @return the number of bytes written to the stream
     *
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if a variant type is to be
     * written that is not yet supported
     */
    public int write(final OutputStream out, final int codepage)
    throws IOException, WritingNotSupportedException {
        int length = 0;
        long variantType = getType();

        /* Ensure that wide strings are written if the codepage is Unicode. */
        if (codepage == CodePageUtil.CP_UNICODE && variantType == Variant.VT_LPSTR) {
            variantType = Variant.VT_LPWSTR;
        }

        length += TypeWriter.writeUIntToStream(out, variantType);
        length += VariantSupport.write(out, variantType, getValue(), codepage);
        return length;
    }
    
}
