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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.bidimap.TreeBidiMap;
import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.util.CodePageUtil;
import org.apache.poi.util.LittleEndian;

/**
 * Represents a section in a {@link PropertySet}.
 */
public class Section {

    /**
     * Maps property IDs to section-private PID strings. These
     * strings can be found in the property with ID 0.
     */
    private Map<Long,String> dictionary;

    /**
     * The section's format ID, {@link #getFormatID}.
     */
    private ClassID formatID;
    /**
     * If the "dirty" flag is true, the section's size must be
     * (re-)calculated before the section is written.
     */
    private boolean dirty = true;

    /**
     * Contains the bytes making out the section. This byte array is
     * established when the section's size is calculated and can be reused
     * later. It is valid only if the "dirty" flag is false.
     */
    private byte[] sectionBytes;

    /**
     * The offset of the section in the stream.
     */
    private long offset = -1;

    /**
     * The section's size in bytes.
     */
    private int size;

    /**
     * This section's properties.
     */
    private final Map<Long,Property> properties = new TreeMap<Long,Property>();

    /**
     * This member is {@code true} if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else {@code false}.
     */
    private boolean wasNull;

    /**
     * Creates an empty {@link Section}.
     */
    public Section() {
    }

    /**
     * Constructs a {@code Section} by doing a deep copy of an
     * existing {@code Section}. All nested {@code Property}
     * instances, will be their mutable counterparts in the new
     * {@code MutableSection}.
     *
     * @param s The section set to copy
     */
    public Section(final Section s) {
        setFormatID(s.getFormatID());
        for (Property p : s.properties.values()) {
            properties.put(p.getID(), new MutableProperty(p));
        }
        setDictionary(s.getDictionary());
    }



    /**
     * Creates a {@link Section} instance from a byte array.
     *
     * @param src Contains the complete property set stream.
     * @param offset The position in the stream that points to the
     * section's format ID.
     *
     * @exception UnsupportedEncodingException if the section's codepage is not
     * supported.
     */
    @SuppressWarnings("unchecked")
    public Section(final byte[] src, final int offset) throws UnsupportedEncodingException {
        int o1 = offset;

        /*
         * Read the format ID.
         */
        formatID = new ClassID(src, o1);
        o1 += ClassID.LENGTH;

        /*
         * Read the offset from the stream's start and positions to
         * the section header.
         */
        this.offset = LittleEndian.getUInt(src, o1);
        o1 = (int) this.offset;

        /*
         * Read the section length.
         */
        size = (int) LittleEndian.getUInt(src, o1);
        o1 += LittleEndian.INT_SIZE;

        /*
         * Read the number of properties.
         */
        final int propertyCount = (int) LittleEndian.getUInt(src, o1);
        o1 += LittleEndian.INT_SIZE;

        /*
         * Read the properties. The offset is positioned at the first
         * entry of the property list. There are two problems:
         *
         * 1. For each property we have to find out its length. In the
         *    property list we find each property's ID and its offset relative
         *    to the section's beginning. Unfortunately the properties in the
         *    property list need not to be in ascending order, so it is not
         *    possible to calculate the length as
         *    (offset of property(i+1) - offset of property(i)). Before we can
         *    that we first have to sort the property list by ascending offsets.
         *
         * 2. We have to read the property with ID 1 before we read other
         *    properties, at least before other properties containing strings.
         *    The reason is that property 1 specifies the codepage. If it is
         *    1200, all strings are in Unicode. In other words: Before we can
         *    read any strings we have to know whether they are in Unicode or
         *    not. Unfortunately property 1 is not guaranteed to be the first in
         *    a section.
         *
         *    The algorithm below reads the properties in two passes: The first
         *    one looks for property ID 1 and extracts the codepage number. The
         *    seconds pass reads the other properties.
         */
        /* Pass 1: Read the property list. */
        int pass1Offset = o1;
        long cpOffset = -1;
        final TreeBidiMap<Long,Long> offset2Id = new TreeBidiMap<Long,Long>();
        for (int i = 0; i < propertyCount; i++) {
            /* Read the property ID. */
            long id = LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            /* Offset from the section's start. */
            long off = LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            offset2Id.put(off, id);
            
            if (id == PropertyIDMap.PID_CODEPAGE) {
                cpOffset = off;
            }
        }

        /* Look for the codepage. */
        int codepage = -1;
        if (cpOffset != -1) {
            /* Read the property's value type. It must be VT_I2. */
            long o = this.offset + cpOffset;
            final long type = LittleEndian.getUInt(src, (int)o);
            o += LittleEndian.INT_SIZE;

            if (type != Variant.VT_I2) {
                throw new HPSFRuntimeException
                    ("Value type of property ID 1 is not VT_I2 but " +
                     type + ".");
            }

            /* Read the codepage number. */
            codepage = LittleEndian.getUShort(src, (int)o);
        }
        

        /* Pass 2: Read all properties - including the codepage property,
         * if available. */
        for (Map.Entry<Long,Long> me : offset2Id.entrySet()) {
            long off = me.getKey();
            long id = me.getValue();
            Property p;
            if (id == PropertyIDMap.PID_CODEPAGE) {
                p = new Property(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2, codepage);
            } else {
                int pLen = propLen(offset2Id, off, size);
                long o = this.offset + off;
                p = new Property(id, src, o, pLen, codepage);
            }
            properties.put(id, p);
        }

        /*
         * Extract the dictionary (if available).
         */
        dictionary = (Map<Long,String>) getProperty(0);
    }

    /**
     * Retrieves the length of the given property (by key)
     *
     * @param offset2Id the offset to id map
     * @param entryOffset the current entry key
     * @param maxSize the maximum offset/size of the section stream
     * @return the length of the current property
     */
    private static int propLen(
        TreeBidiMap<Long,Long> offset2Id,
        Long entryOffset,
        long maxSize) {
        Long nextKey = offset2Id.nextKey(entryOffset);
        long begin = entryOffset;
        long end = (nextKey != null) ? nextKey : maxSize;
        return (int)(end - begin);
    }


    /**
     * Returns the format ID. The format ID is the "type" of the
     * section. For example, if the format ID of the first {@link
     * Section} contains the bytes specified by
     * {@code org.apache.poi.hpsf.wellknown.SectionIDMap.SUMMARY_INFORMATION_ID}
     * the section (and thus the property set) is a SummaryInformation.
     *
     * @return The format ID
     */
    public ClassID getFormatID() {
        return formatID;
    }

    /**
     * Sets the section's format ID.
     *
     * @param formatID The section's format ID
     */
    public void setFormatID(final ClassID formatID) {
        this.formatID = formatID;
    }

    /**
     * Sets the section's format ID.
     *
     * @param formatID The section's format ID as a byte array. It components
     * are in big-endian format.
     */
    public void setFormatID(final byte[] formatID) {
        ClassID fid = getFormatID();
        if (fid == null) {
            fid = new ClassID();
            setFormatID(fid);
        }
        fid.setBytes(formatID);
    }

    /**
     * Returns the offset of the section in the stream.
     *
     * @return The offset of the section in the stream.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * Returns the number of properties in this section.
     *
     * @return The number of properties in this section.
     */
    public int getPropertyCount() {
        return properties.size();
    }

    /**
     * Returns this section's properties.
     *
     * @return This section's properties.
     */
    public Property[] getProperties() {
        return properties.values().toArray(new Property[properties.size()]);
    }

    /**
     * Sets this section's properties. Any former values are overwritten.
     *
     * @param properties This section's new properties.
     */
    public void setProperties(final Property[] properties) {
        this.properties.clear();
        for (Property p : properties) {
            this.properties.put(p.getID(), p);
        }
        dirty = true;
    }

    /**
     * Returns the value of the property with the specified ID. If
     * the property is not available, {@code null} is returned
     * and a subsequent call to {@link #wasNull} will return
     * {@code true}.
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    public Object getProperty(final long id) {
        wasNull = !properties.containsKey(id);
        return (wasNull) ? null : properties.get(id).getValue();
    }

    /**
     * Sets the string value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value. It will be written as a Unicode
     * string.
     */
    public void setProperty(final int id, final String value) {
        setProperty(id, Variant.VT_LPWSTR, value);
    }

    /**
     * Sets the int value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final int value) {
        setProperty(id, Variant.VT_I4, Integer.valueOf(value));
    }



    /**
     * Sets the long value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final long value) {
        setProperty(id, Variant.VT_I8, Long.valueOf(value));
    }



    /**
     * Sets the boolean value of the property with the specified ID.
     *
     * @param id The property's ID
     * @param value The property's value.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final boolean value) {
        setProperty(id, Variant.VT_BOOL, Boolean.valueOf(value));
    }



    /**
     * Sets the value and the variant type of the property with the
     * specified ID. If a property with this ID is not yet present in
     * the section, it will be added. An already present property with
     * the specified ID will be overwritten. A default mapping will be
     * used to choose the property's type.
     *
     * @param id The property's ID.
     * @param variantType The property's variant type.
     * @param value The property's value.
     *
     * @see #setProperty(int, String)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final int id, final long variantType, final Object value) {
        setProperty(new Property(id, variantType, value));
    }



    /**
     * Sets a property.
     *
     * @param p The property to be set.
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final Property p) {
        Property old = properties.get(p.getID());
        if (old == null || !old.equals(p)) {
            properties.put(p.getID(), p);
            dirty = true;
        }
    }

    /**
     * Sets a property.
     *
     * @param id The property ID.
     * @param value The property's value. The value's class must be one of those
     *        supported by HPSF.
     */
    public void setProperty(final int id, final Object value) {
        if (value instanceof String) {
            setProperty(id, (String) value);
        } else if (value instanceof Long) {
            setProperty(id, ((Long) value).longValue());
        } else if (value instanceof Integer) {
            setProperty(id, ((Integer) value).intValue());
        } else if (value instanceof Short) {
            setProperty(id, ((Short) value).intValue());
        } else if (value instanceof Boolean) {
            setProperty(id, ((Boolean) value).booleanValue());
        } else if (value instanceof Date) {
            setProperty(id, Variant.VT_FILETIME, value);
        } else {
            throw new HPSFRuntimeException(
                    "HPSF does not support properties of type " +
                    value.getClass().getName() + ".");
        }
    }

    /**
     * Returns the value of the numeric property with the specified
     * ID. If the property is not available, 0 is returned. A
     * subsequent call to {@link #wasNull} will return
     * {@code true} to let the caller distinguish that case from
     * a real property value of 0.
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    protected int getPropertyIntValue(final long id) {
        final Number i;
        final Object o = getProperty(id);
        if (o == null) {
            return 0;
        }
        if (!(o instanceof Long || o instanceof Integer)) {
            throw new HPSFRuntimeException
                ("This property is not an integer type, but " +
                 o.getClass().getName() + ".");
        }
        i = (Number) o;
        return i.intValue();
    }

    /**
     * Returns the value of the boolean property with the specified
     * ID. If the property is not available, {@code false} is
     * returned. A subsequent call to {@link #wasNull} will return
     * {@code true} to let the caller distinguish that case from
     * a real property value of {@code false}.
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    protected boolean getPropertyBooleanValue(final int id) {
        final Boolean b = (Boolean) getProperty(id);
        if (b == null) {
            return false;
        }
        return b.booleanValue();
    }

    /**
     * Sets the value of the boolean property with the specified
     * ID.
     *
     * @param id The property's ID
     * @param value The property's value
     *
     * @see #setProperty(int, long, Object)
     * @see #getProperty
     * @see Variant
     */
    protected void setPropertyBooleanValue(final int id, final boolean value) {
        setProperty(id, Variant.VT_BOOL, Boolean.valueOf(value));
    }

    /**
     * @return the section's size in bytes.
     */
    public int getSize() {
        if (dirty) {
            try {
                size = calcSize();
                dirty = false;
            } catch (HPSFRuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new HPSFRuntimeException(ex);
            }
        }
        return size;
    }

    /**
     * Calculates the section's size. It is the sum of the lengths of the
     * section's header (8), the properties list (16 times the number of
     * properties) and the properties themselves.
     *
     * @return the section's length in bytes.
     * @throws WritingNotSupportedException
     * @throws IOException
     */
    private int calcSize() throws WritingNotSupportedException, IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out);
        out.close();
        /* Pad to multiple of 4 bytes so that even the Windows shell (explorer)
         * shows custom properties. */
        sectionBytes = Util.pad4(out.toByteArray());
        return sectionBytes.length;
    }




    /**
     * Checks whether the property which the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access
     * was available or not. This information might be important for
     * callers of {@link #getPropertyIntValue} since the latter
     * returns 0 if the property does not exist. Using {@link
     * #wasNull} the caller can distiguish this case from a property's
     * real value of 0.
     *
     * @return {@code true} if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else {@code false}.
     */
    public boolean wasNull() {
        return wasNull;
    }



    /**
     * Returns the PID string associated with a property ID. The ID
     * is first looked up in the {@link Section}'s private
     * dictionary. If it is not found there, the method calls {@link
     * SectionIDMap#getPIDString}.
     *
     * @param pid The property ID
     *
     * @return The property ID's string value
     */
    public String getPIDString(final long pid) {
        String s = null;
        if (dictionary != null) {
            s = dictionary.get(Long.valueOf(pid));
        }
        if (s == null) {
            s = SectionIDMap.getPIDString(getFormatID().getBytes(), pid);
        }
        return s;
    }

    /**
     * Removes all properties from the section including 0 (dictionary) and
     * 1 (codepage).
     */
    public void clear()
    {
        final Property[] properties = getProperties();
        for (int i = 0; i < properties.length; i++)
        {
            final Property p = properties[i];
            removeProperty(p.getID());
        }
    }

    /**
     * Sets the codepage.
     *
     * @param codepage the codepage
     */
    public void setCodepage(final int codepage)
    {
        setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                Integer.valueOf(codepage));
    }



    /**
     * Checks whether this section is equal to another object. The result is
     * {@code false} if one of the the following conditions holds:
     *
     * <ul>
     *
     * <li>The other object is not a {@link Section}.</li>
     *
     * <li>The format IDs of the two sections are not equal.</li>
     *
     * <li>The sections have a different number of properties. However,
     * properties with ID 1 (codepage) are not counted.</li>
     *
     * <li>The other object is not a {@link Section}.</li>
     *
     * <li>The properties have different values. The order of the properties
     * is irrelevant.</li>
     *
     * </ul>
     *
     * @param o The object to compare this section with
     * @return {@code true} if the objects are equal, {@code false} if
     * not
     */
    public boolean equals(final Object o) {
        if (o == null || !(o instanceof Section)) {
            return false;
        }
        final Section s = (Section) o;
        if (!s.getFormatID().equals(getFormatID())) {
            return false;
        }

        /* Compare all properties except 0 and 1 as they must be handled
         * specially. */
        Property[] pa1 = new Property[getProperties().length];
        Property[] pa2 = new Property[s.getProperties().length];
        System.arraycopy(getProperties(), 0, pa1, 0, pa1.length);
        System.arraycopy(s.getProperties(), 0, pa2, 0, pa2.length);

        /* Extract properties 0 and 1 and remove them from the copy of the
         * arrays. */
        Property p10 = null;
        Property p20 = null;
        for (int i = 0; i < pa1.length; i++) {
            final long id = pa1[i].getID();
            if (id == 0) {
                p10 = pa1[i];
                pa1 = remove(pa1, i);
                i--;
            }
            if (id == 1) {
                pa1 = remove(pa1, i);
                i--;
            }
        }
        for (int i = 0; i < pa2.length; i++) {
            final long id = pa2[i].getID();
            if (id == 0) {
                p20 = pa2[i];
                pa2 = remove(pa2, i);
                i--;
            }
            if (id == 1) {
                pa2 = remove(pa2, i);
                i--;
            }
        }

        /* If the number of properties (not counting property 1) is unequal the
         * sections are unequal. */
        if (pa1.length != pa2.length) {
            return false;
        }

        /* If the dictionaries are unequal the sections are unequal. */
        boolean dictionaryEqual = true;
        if (p10 != null && p20 != null) {
            dictionaryEqual = p10.getValue().equals(p20.getValue());
        } else if (p10 != null || p20 != null) {
            dictionaryEqual = false;
        }
        if (dictionaryEqual) {
            return Util.equals(pa1, pa2);
        }
        return false;
    }

    /**
     * Removes a property.
     *
     * @param id The ID of the property to be removed
     */
    public void removeProperty(final long id) {
        dirty |= (properties.remove(id) != null);
    }

    /**
     * Removes a field from a property array. The resulting array is
     * compactified and returned.
     *
     * @param pa The property array.
     * @param i The index of the field to be removed.
     * @return the compactified array.
     */
    private Property[] remove(final Property[] pa, final int i) {
        final Property[] h = new Property[pa.length - 1];
        if (i > 0) {
            System.arraycopy(pa, 0, h, 0, i);
        }
        System.arraycopy(pa, i + 1, h, i, h.length - i);
        return h;
    }
    /**
     * Writes this section into an output stream.<p>
     *
     * Internally this is done by writing into three byte array output
     * streams: one for the properties, one for the property list and one for
     * the section as such. The two former are appended to the latter when they
     * have received all their data.
     *
     * @param out The stream to write into.
     *
     * @return The number of bytes written, i.e. the section's size.
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    public int write(final OutputStream out) throws WritingNotSupportedException, IOException {
        /* Check whether we have already generated the bytes making out the
         * section. */
        if (!dirty && sectionBytes != null) {
            out.write(sectionBytes);
            return sectionBytes.length;
        }

        /* The properties are written to this stream. */
        final ByteArrayOutputStream propertyStream = new ByteArrayOutputStream();

        /* The property list is established here. After each property that has
         * been written to "propertyStream", a property list entry is written to
         * "propertyListStream". */
        final ByteArrayOutputStream propertyListStream = new ByteArrayOutputStream();

        /* Maintain the current position in the list. */
        int position = 0;

        /* Increase the position variable by the size of the property list so
         * that it points behind the property list and to the beginning of the
         * properties themselves. */
        position += 2 * LittleEndian.INT_SIZE + getPropertyCount() * 2 * LittleEndian.INT_SIZE;

        /* Writing the section's dictionary it tricky. If there is a dictionary
         * (property 0) the codepage property (property 1) must be set, too. */
        int codepage = -1;
        if (getProperty(PropertyIDMap.PID_DICTIONARY) != null) {
            final Object p1 = getProperty(PropertyIDMap.PID_CODEPAGE);
            if (p1 != null) {
                if (!(p1 instanceof Integer)) {
                    throw new IllegalPropertySetDataException
                        ("The codepage property (ID = 1) must be an " +
                         "Integer object.");
                }
            } else {
                /* Warning: The codepage property is not set although a
                 * dictionary is present. In order to cope with this problem we
                 * add the codepage property and set it to Unicode. */
                setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                            Integer.valueOf(CodePageUtil.CP_UNICODE));
            }
            codepage = getCodepage();
        }
        
        /* Write the properties and the property list into their respective
         * streams: */
        for (Property p : properties.values()) {
            final long id = p.getID();

            /* Write the property list entry. */
            TypeWriter.writeUIntToStream(propertyListStream, p.getID());
            TypeWriter.writeUIntToStream(propertyListStream, position);

            /* If the property ID is not equal 0 we write the property and all
             * is fine. However, if it equals 0 we have to write the section's
             * dictionary which has an implicit type only and an explicit
             * value. */
            if (id != 0)
                /* Write the property and update the position to the next
                 * property. */
                position += p.write(propertyStream, getCodepage());
            else
            {
                if (codepage == -1)
                    throw new IllegalPropertySetDataException
                        ("Codepage (property 1) is undefined.");
                position += writeDictionary(propertyStream, dictionary,
                                            codepage);
            }
        }
        propertyStream.close();
        propertyListStream.close();

        /* Write the section: */
        byte[] pb1 = propertyListStream.toByteArray();
        byte[] pb2 = propertyStream.toByteArray();

        /* Write the section's length: */
        TypeWriter.writeToStream(out, LittleEndian.INT_SIZE * 2 +
                                      pb1.length + pb2.length);

        /* Write the section's number of properties: */
        TypeWriter.writeToStream(out, getPropertyCount());

        /* Write the property list: */
        out.write(pb1);

        /* Write the properties: */
        out.write(pb2);

        int streamLength = LittleEndian.INT_SIZE * 2 + pb1.length + pb2.length;
        return streamLength;
    }



    /**
     * Writes the section's dictionary.
     *
     * @param out The output stream to write to.
     * @param dictionary The dictionary.
     * @param codepage The codepage to be used to write the dictionary items.
     * @return The number of bytes written
     * @exception IOException if an I/O exception occurs.
     */
    private static int writeDictionary(final OutputStream out, final Map<Long,String> dictionary, final int codepage)
    throws IOException {
        int length = TypeWriter.writeUIntToStream(out, dictionary.size());
        for (Map.Entry<Long,String> ls : dictionary.entrySet()) {
            final Long key = ls.getKey();
            final String value = ls.getValue();

            if (codepage == CodePageUtil.CP_UNICODE) {
                /* Write the dictionary item in Unicode. */
                int sLength = value.length() + 1;
                if ((sLength & 1) == 1) {
                    sLength++;
                }
                length += TypeWriter.writeUIntToStream(out, key.longValue());
                length += TypeWriter.writeUIntToStream(out, sLength);
                final byte[] ca = CodePageUtil.getBytesInCodePage(value, codepage);
                for (int j = 2; j < ca.length; j += 2) {
                    out.write(ca[j+1]);
                    out.write(ca[j]);
                    length += 2;
                }
                sLength -= value.length();
                while (sLength > 0) {
                    out.write(0x00);
                    out.write(0x00);
                    length += 2;
                    sLength--;
                }
            } else {
                /* Write the dictionary item in another codepage than
                 * Unicode. */
                length += TypeWriter.writeUIntToStream(out, key.longValue());
                length += TypeWriter.writeUIntToStream(out, value.length() + 1L);
                final byte[] ba = CodePageUtil.getBytesInCodePage(value, codepage);
                for (int j = 0; j < ba.length; j++) {
                    out.write(ba[j]);
                    length++;
                }
                out.write(0x00);
                length++;
            }
        }
        return length;
    }

    /**
     * Sets the section's dictionary. All keys in the dictionary must be
     * {@link java.lang.Long} instances, all values must be
     * {@link java.lang.String}s. This method overwrites the properties with IDs
     * 0 and 1 since they are reserved for the dictionary and the dictionary's
     * codepage. Setting these properties explicitly might have surprising
     * effects. An application should never do this but always use this
     * method.
     *
     * @param dictionary The dictionary
     *
     * @exception IllegalPropertySetDataException if the dictionary's key and
     * value types are not correct.
     *
     * @see Section#getDictionary()
     */
    public void setDictionary(final Map<Long,String> dictionary) throws IllegalPropertySetDataException {
        if (dictionary != null) {
            this.dictionary = dictionary;

            /* Set the dictionary property (ID 0). Please note that the second
             * parameter in the method call below is unused because dictionaries
             * don't have a type. */
            setProperty(PropertyIDMap.PID_DICTIONARY, -1, dictionary);

            /* If the codepage property (ID 1) for the strings (keys and
             * values) used in the dictionary is not yet defined, set it to
             * Unicode. */
            final Integer codepage = (Integer) getProperty(PropertyIDMap.PID_CODEPAGE);
            if (codepage == null) {
                setProperty(PropertyIDMap.PID_CODEPAGE, Variant.VT_I2,
                            Integer.valueOf(CodePageUtil.CP_UNICODE));
            }
        } else {
            /* Setting the dictionary to null means to remove property 0.
             * However, it does not mean to remove property 1 (codepage). */
            removeProperty(PropertyIDMap.PID_DICTIONARY);
        }
    }



    /**
     * @see Object#hashCode()
     */
    public int hashCode() {
        long hashCode = 0;
        hashCode += getFormatID().hashCode();
        final Property[] pa = getProperties();
        for (int i = 0; i < pa.length; i++) {
            hashCode += pa[i].hashCode();
        }
        final int returnHashCode = (int) (hashCode & 0x0ffffffffL);
        return returnHashCode;
    }



    /**
     * @see Object#toString()
     */
    public String toString() {
        final StringBuffer b = new StringBuffer();
        final Property[] pa = getProperties();
        b.append(getClass().getName());
        b.append('[');
        b.append("formatID: ");
        b.append(getFormatID());
        b.append(", offset: ");
        b.append(getOffset());
        b.append(", propertyCount: ");
        b.append(getPropertyCount());
        b.append(", size: ");
        b.append(getSize());
        b.append(", properties: [\n");
        for (int i = 0; i < pa.length; i++) {
            b.append(pa[i].toString());
            b.append(",\n");
        }
        b.append(']');
        b.append(']');
        return b.toString();
    }



    /**
     * Gets the section's dictionary. A dictionary allows an application to
     * use human-readable property names instead of numeric property IDs. It
     * contains mappings from property IDs to their associated string
     * values. The dictionary is stored as the property with ID 0. The codepage
     * for the strings in the dictionary is defined by property with ID 1.
     *
     * @return the dictionary or {@code null} if the section does not have
     * a dictionary.
     */
    public Map<Long,String> getDictionary() {
        return dictionary;
    }



    /**
     * Gets the section's codepage, if any.
     *
     * @return The section's codepage if one is defined, else -1.
     */
    public int getCodepage()
    {
        final Integer codepage = (Integer) getProperty(PropertyIDMap.PID_CODEPAGE);
        if (codepage == null) {
            return -1;
        }
        int cp = codepage.intValue();
        return cp;
    }
}
