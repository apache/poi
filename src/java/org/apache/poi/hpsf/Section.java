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
 */
package org.apache.poi.hpsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hpsf.wellknown.PropertyIDMap;
import org.apache.poi.hpsf.wellknown.SectionIDMap;
import org.apache.poi.util.LittleEndian;

/**
 * <p>Represents a section in a {@link PropertySet}.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @author Drew Varner (Drew.Varner allUpIn sc.edu)
 * @version $Id$
 * @since 2002-02-09
 */
public class Section
{

    /**
     * <p>Maps property IDs to section-private PID strings. These
     * strings can be found in the property with ID 0.</p>
     */
    protected Map dictionary;

    /**
     * <p>The section's format ID, {@link #getFormatID}.</p>
     */
    protected ClassID formatID;


    /**
     * <p>Returns the format ID. The format ID is the "type" of the
     * section. For example, if the format ID of the first {@link
     * Section} contains the bytes specified by
     * <code>org.apache.poi.hpsf.wellknown.SectionIDMap.SUMMARY_INFORMATION_ID</code>
     * the section (and thus the property set) is a SummaryInformation.</p>
     *
     * @return The format ID
     */
    public ClassID getFormatID()
    {
        return formatID;
    }



    /**
     * @see #getOffset
     */
    protected long offset;


    /**
     * <p>Returns the offset of the section in the stream.</p>
     *
     * @return The offset of the section in the stream.
     */
    public long getOffset()
    {
        return offset;
    }



    /**
     * @see #getSize
     */
    protected int size;


    /**
     * <p>Returns the section's size in bytes.</p>
     *
     * @return The section's size in bytes.
     */
    public int getSize()
    {
        return size;
    }



    /**
     * FIXME (2): Get rid of this! The property count is implicitly available as
     * the length of the "properties" array.
     * 
     * @see #getPropertyCount
     */
    protected int propertyCount;


    /**
     * <p>Returns the number of properties in this section.</p>
     *
     * @return The number of properties in this section.
     */
    public int getPropertyCount()
    {
        return propertyCount;
    }



    /**
     * @see #getProperties
     */
    protected Property[] properties;


    /**
     * <p>Returns this section's properties.</p>
     *
     * @return This section's properties.
     */
    public Property[] getProperties()
    {
        return properties;
    }



    /**
     * <p>Creates an empty and uninitialized {@link Section}.
     */
    protected Section()
    { }



    /**
     * <p>Creates a {@link Section} instance from a byte array.</p>
     *
     * @param src Contains the complete property set stream.
     * @param offset The position in the stream that points to the
     * section's format ID.
     */
    public Section(final byte[] src, final int offset)
    {
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
        propertyCount = (int) LittleEndian.getUInt(src, o1);
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
        properties = new Property[propertyCount];
        
        /* Pass 1: Read the property list. */
        int pass1Offset = o1;
        List propertyList = new ArrayList(propertyCount);
        PropertyListEntry ple;
        for (int i = 0; i < properties.length; i++)
        {
            ple = new PropertyListEntry();

            /* Read the property ID. */
            ple.id = (int) LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            /* Offset from the section's start. */
            ple.offset = (int) LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            /* Add the entry to the property list. */
            propertyList.add(ple);
        }

        /* Sort the property list by ascending offsets: */
        Collections.sort(propertyList);

        /* Calculate the properties' lengths. */
        for (int i = 0; i < propertyCount - 1; i++)
        {
            final PropertyListEntry ple1 =
                (PropertyListEntry) propertyList.get(i);
            final PropertyListEntry ple2 =
                (PropertyListEntry) propertyList.get(i + 1);
            ple1.length = ple2.offset - ple1.offset;
        }
        if (propertyCount > 0)
        {
            ple = (PropertyListEntry) propertyList.get(propertyCount - 1);
            ple.length = size - ple.offset;
        }

        /* Look for the codepage. */
        int codepage = -1;
        for (final Iterator i = propertyList.iterator();
             codepage == -1 && i.hasNext();)
        {
            ple = (PropertyListEntry) i.next();

            /* Read the codepage if the property ID is 1. */
            if (ple.id == PropertyIDMap.PID_CODEPAGE)
            {
                /* Read the property's value type. It must be
                 * VT_I2. */
                int o = (int) (this.offset + ple.offset);
                final long type = LittleEndian.getUInt(src, o);
                o += LittleEndian.INT_SIZE;

                if (type != Variant.VT_I2)
                    throw new HPSFRuntimeException
                        ("Value type of property ID 1 is not VT_I2 but " +
                         type + ".");

                /* Read the codepage number. */
                codepage = LittleEndian.getUShort(src, o);
            }
        }

        /* Pass 2: Read all properties - including the codepage property,
         * if available. */
        int i1 = 0;
        for (final Iterator i = propertyList.iterator(); i.hasNext();)
        {
            ple = (PropertyListEntry) i.next();
            properties[i1++] = new Property(ple.id, src,
                                            this.offset + ple.offset,
                                            ple.length, codepage);
        }

        /*
         * Extract the dictionary (if available).
         */
        dictionary = (Map) getProperty(0);
    }



    /**
     * <p>Represents an entry in the property list and holds a property's ID and
     * its offset from the section's beginning.</p>
     */
    class PropertyListEntry implements Comparable
    {
        int id;
        int offset;
        int length;

        /**
         * <p>Compares this {@link PropertyListEntry} with another one by their
         * offsets. A {@link PropertyListEntry} is "smaller" than another one if
         * its offset from the section's begin is smaller.</p>
         *
         * @see Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(final Object o)
        {
            if (!(o instanceof PropertyListEntry))
                throw new ClassCastException(o.toString());
            final int otherOffset = ((PropertyListEntry) o).offset;
            if (offset < otherOffset)
                return -1;
            else if (offset == otherOffset)
                return 0;
            else
                return 1;
        }
    }



    /**
     * <p>Returns the value of the property with the specified ID. If
     * the property is not available, <code>null</code> is returned
     * and a subsequent call to {@link #wasNull} will return
     * <code>true</code>.</p>
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    public Object getProperty(final long id)
    {
        wasNull = false;
        for (int i = 0; i < properties.length; i++)
            if (id == properties[i].getID())
                return properties[i].getValue();
        wasNull = true;
        return null;
    }



    /**
     * <p>Returns the value of the numeric property with the specified
     * ID. If the property is not available, 0 is returned. A
     * subsequent call to {@link #wasNull} will return
     * <code>true</code> to let the caller distinguish that case from
     * a real property value of 0.</p>
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    protected int getPropertyIntValue(final long id)
    {
        final Long i;
        final Object o = getProperty(id);
        if (o == null)
            return 0;
        if (!(o instanceof Long))
            throw new HPSFRuntimeException
                ("This property is not an integer type, but " +
                 o.getClass().getName() + ".");
        i = (Long) o;
        return i.intValue();
    }



    /**
     * <p>Returns the value of the boolean property with the specified
     * ID. If the property is not available, <code>false</code> is
     * returned. A subsequent call to {@link #wasNull} will return
     * <code>true</code> to let the caller distinguish that case from
     * a real property value of <code>false</code>.</p>
     *
     * @param id The property's ID
     *
     * @return The property's value
     */
    protected boolean getPropertyBooleanValue(final int id)
    {
        final Boolean b = (Boolean) getProperty(id);
        if (b != null)
            return b.booleanValue();
        else
            return false;
        }



    /**
     * <p>This member is <code>true</code> if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else <code>false</code>.</p>
     */
    private boolean wasNull;


    /**
     * <p>Checks whether the property which the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access
     * was available or not. This information might be important for
     * callers of {@link #getPropertyIntValue} since the latter
     * returns 0 if the property does not exist. Using {@link
     * #wasNull} the caller can distiguish this case from a property's
     * real value of 0.</p>
     *
     * @return <code>true</code> if the last call to {@link
     * #getPropertyIntValue} or {@link #getProperty} tried to access a
     * property that was not available, else <code>false</code>.
     */
    public boolean wasNull()
    {
        return wasNull;
    }



    /**
     * <p>Returns the PID string associated with a property ID. The ID
     * is first looked up in the {@link Section}'s private
     * dictionary. If it is not found there, the method calls {@link
     * SectionIDMap#getPIDString}.</p>
     *
     * @param pid The property ID
     *
     * @return The property ID's string value
     */
    public String getPIDString(final long pid)
    {
        String s = null;
        if (dictionary != null)
            s = (String) dictionary.get(new Long(pid));
        if (s == null)
            s = SectionIDMap.getPIDString(getFormatID().getBytes(), pid);
        if (s == null)
            s = SectionIDMap.UNDEFINED;
        return s;
    }



    /**
     * <p>Checks whether this section is equal to another object.</p>
     * 
     * @param o The object to compare this section with
     * @return <code>true</code> if the objects are equal, <code>false</code> if
     * not
     */
    public boolean equals(final Object o)
    {
        if (o == null || !(o instanceof Section))
            return false;
        final Section s = (Section) o;
        if (!s.getFormatID().equals(getFormatID()))
            return false;
        if (s.getPropertyCount() != getPropertyCount())
            return false;
        return Util.equals(s.getProperties(), getProperties());
    }



    /**
     * @see Object#hashCode()
     */
    public int hashCode()
    {
        long hashCode = 0;
        hashCode += getFormatID().hashCode();
        final Property[] pa = getProperties();
        for (int i = 0; i < pa.length; i++)
            hashCode += pa[i].hashCode();
        final int returnHashCode = (int) (hashCode & 0x0ffffffffL);
        return returnHashCode;
    }



    /**
     * @see Object#toString()
     */
    public String toString()
    {
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
        for (int i = 0; i < pa.length; i++)
        {
            b.append(pa[i].toString());
            b.append(",\n");
        }
        b.append(']');
        b.append(']');
        return b.toString();
    }



    /**
     * <p>Gets the section's dictionary. A dictionary allows an application to
     * use human-readable property names instead of numeric property IDs. It
     * contains mappings from property IDs to their associated string
     * values. The dictionary is stored as the property with ID 0. The codepage
     * for the strings in the dictionary is defined by property with ID 1.</p>
     *
     * @return the dictionary or <code>null</code> if the section does not have
     * a dictionary.
     */
    public Map getDictionary()
    {
        return dictionary;
    }

}
