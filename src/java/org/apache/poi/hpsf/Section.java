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

import java.util.*;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.hpsf.wellknown.*;

/**
 * <p>Represents a section in a {@link PropertySet}.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
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

    protected ClassID formatID;


    /**
     * <p>Returns the format ID. The format ID is the "type" of the
     * section. For example, if the format ID of the first {@link
     * Section} contains the bytes specified by
     * <code>org.apache.poi.hpsf.wellknown.SectionIDMap.SUMMARY_INFORMATION_ID</code>
     * the section (and thus the property set) is a
     * SummaryInformation.</p>
     *
     * @return The format ID
     */
    public ClassID getFormatID()
    {
        return formatID;
    }



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
    {}



    /**
     * <p>Creates a {@link Section} instance from a byte array.</p>
     *
     * @param src Contains the complete property set stream.
     * @param offset The position in the stream that points to the
     * section's format ID.
     */
    public Section(final byte[] src, int offset)
    {
        /*
         * Read the format ID.
         */
        formatID = new ClassID(src, offset);
        offset += ClassID.LENGTH;

        /*
         * Read the offset from the stream's start and positions to
         * the section header.
         */
        this.offset = LittleEndian.getUInt(src, offset);
        offset = (int)this.offset;

        /*
         * Read the section length.
         */
        size = (int)LittleEndian.getUInt(src, offset);
        offset += LittleEndian.INT_SIZE;

        /*
         * Read the number of properties.
         */
        propertyCount = (int)LittleEndian.getUInt(src, offset);
        offset += LittleEndian.INT_SIZE;

        /*
         * Read the properties. The offset is positioned at the first
         * entry of the property list. The problem is that we have to
         * read the property with ID 1 before we read other
         * properties, at least before other properties containing
         * strings. The reason is that property 1 specifies the
         * codepage. If it is 1200, all strings are in Unicode. In
         * other words: Before we can read any strings we have to know
         * whether they are in Unicode or not. Unfortunately property
         * 1 is not guaranteed to be the first in a section.
	 *
	 * The algorithm below reads the properties in two passes: The
	 * first one looks for property ID 1 and extracts the codepage
	 * number. The seconds pass reads the other properties.
         */
        properties = new Property[propertyCount];
	Property propertyOne;

 	/* Pass 1: Look for the codepage. */
 	int codepage = -1;
	int pass1Offset = offset;
        for (int i = 0; i < properties.length; i++)
	{
	    /* Read the property ID. */
            final int id = (int) LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            /* Offset from the section's start. */
            final int sOffset = (int) LittleEndian.getUInt(src, pass1Offset);
            pass1Offset += LittleEndian.INT_SIZE;

            /* Calculate the length of the property. */
            int length;
            if (i == properties.length - 1)
                length = (int) (src.length - this.offset - sOffset);
            else
                length = (int)
                    LittleEndian.getUInt(src, pass1Offset +
					 LittleEndian.INT_SIZE) - sOffset;

	    if (id == PropertyIDMap.PID_CODEPAGE)
	    {
		/* Read the codepage if the property ID is 1. */

		/* Read the property's value type. It must be
		 * VT_I2. */
		int o = (int) (this.offset + sOffset);
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

	/* Pass 2: Read all properties, including 1. */
        for (int i = 0; i < properties.length; i++)
	{
	    /* Read the property ID. */
            final int id = (int) LittleEndian.getUInt(src, offset);
            offset += LittleEndian.INT_SIZE;

            /* Offset from the section. */
            final int sOffset = (int) LittleEndian.getUInt(src, offset);
            offset += LittleEndian.INT_SIZE;

            /* Calculate the length of the property. */
            int length;
            if (i == properties.length - 1)
                length = (int) (src.length - this.offset - sOffset);
            else
                length = (int)
                    LittleEndian.getUInt(src, offset + LittleEndian.INT_SIZE) -
                    sOffset;

            /* Create it. */
            properties[i] = new Property(id, src, this.offset + sOffset,
					 length, codepage);
        }

        /*
         * Extract the dictionary (if available).
         */
        dictionary = (Map) getProperty(0);
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
    public Object getProperty(final int id)
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
    protected int getPropertyIntValue(final int id)
    {
        /* FIXME: Find out why the following is a Long instead of an
         * Integer! */
        final Long i = (Long) getProperty(id);
        if (i != null)
            return i.intValue();
        else
            return 0;
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
    public String getPIDString(final int pid)
    {
        String s = null;
        if (dictionary != null)
            s = (String) dictionary.get(new Integer(pid));
        if (s == null)
            s = SectionIDMap.getPIDString(getFormatID().getBytes(), pid);
        if (s == null)
            s = SectionIDMap.UNDEFINED;
        return s;
    }

}
