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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * <p>Adds writing capability to the {@link Section} class.</p>
 *
 * <p>Please be aware that this class' functionality will be merged into the
 * {@link Section} class at a later time, so the API will change.</p>
 *
 * @author Rainer Klute <a
 * href="mailto:klute@rainer-klute.de">&lt;klute@rainer-klute.de&gt;</a>
 * @version $Id$
 * @since 2002-02-20
 */
public class MutableSection extends Section
{

    /**
     * <p>If the "dirty" flag is true, the section's size must be
     * (re-)calculated before the section is written.</p>
     */
    private boolean dirty = true;



    /**
     * <p>List to assemble the properties. Unfortunately a wrong
     * decision has been taken when specifying the "properties" field
     * as an Property[]. It should have been a {@link java.util.List}.</p>
     */
    private List preprops;



    /**
     * <p>Creates an empty mutable section.</p>
     */
    public MutableSection()
    {
        dirty = true;
        formatID = null;
        offset = -1;
        preprops = new LinkedList();
    }



    /**
     * <p>Sets the section's format ID.</p>
     *
     * @param formatID The section's format ID
     *
     * @see #setFormatID(byte[])
     * @see #getFormatID
     */
    public void setFormatID(final ClassID formatID)
    {
        this.formatID = formatID;
    }



    /**
     * <p>Sets the section's format ID.</p>
     *
     * @param formatID The section's format ID as a byte array. It components
     * are in big-endian format.
     *
     * @see #setFormatID(ClassID)
     * @see #getFormatID
     */
    public void setFormatID(final byte[] formatID)
    {
        setFormatID(new ClassID(formatID, 0));
    }



    /**
     * <p>Sets this section's properties. Any former values are overwritten.</p>
     *
     * @param properties This section's new properties.
     */
    public void setProperties(final Property[] properties)
    {
        preprops = new LinkedList();
        for (int i = 0; i < properties.length; i++)
            preprops.add(properties[i]);
        dirty = true;
    }



    /**
     * <p>Sets the value of the property with the specified ID. If a
     * property with this ID is not yet present in the section, it
     * will be added. An already present property with the specified
     * ID will be overwritten.</p>
     *
     * @param id The property's ID
     * @param value The property's value. It will be written as a Unicode
     * string.
     *
     * @see #setProperty(int, int, Object)
     * @see #getProperty
     */
    public void setProperty(final int id, final String value)
    {
        setProperty(id, Variant.VT_LPWSTR, value);
        dirty = true;
    }



    /**
     * <p>Sets the value and the variant type of the property with the
     * specified ID. If a property with this ID is not yet present in
     * the section, it will be added. An already present property with
     * the specified ID will be overwritten. A default mapping will be
     * used to choose the property's type.</p>
     *
     * @param id The property's ID.
     * @param variantType The property's variant type.
     * @param value The property's value.
     *
     * @see #setProperty(int, Object)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final int id, final long variantType,
                            final Object value)
    {
        final MutableProperty p = new MutableProperty();
        p.setID(id);
        p.setType(variantType);
        p.setValue(value);
        setProperty(p);
        dirty = true;
    }



    /**
     * <p>Sets a property. If a property with the same ID is not yet present in
     * the section, the property will be added to the section. If there is
     * already a property with the same ID present in the section, it will be
     * overwritten.</p>
     *
     * @param p The property to be added to the section
     *
     * @see #setProperty(int, int, Object)
     * @see #setProperty(int, String)
     * @see #getProperty
     * @see Variant
     */
    public void setProperty(final Property p)
    {
        final long id = p.getID();
        for (final Iterator i = preprops.iterator(); i.hasNext();)
            if (((Property) i.next()).getID() == id)
            {
                i.remove();
                break;
            }
        preprops.add(p);
        dirty = true;
    }



    /**
     * <p>Sets the value of the boolean property with the specified
     * ID.</p>
     *
     * @param id The property's ID
     * @param value The property's value
     *
     * @see #setProperty(int, int, Object)
     * @see #getProperty
     * @see Variant
     */
    protected void setPropertyBooleanValue(final int id, final boolean value)
    {
        setProperty(id, (long) Variant.VT_BOOL, new Boolean(value));
    }



    /**
     * <p>Returns the section's size.</p>
     *
     * @return the section's size.
     */
    public int getSize()
    {
        if (dirty)
        {
            size = calcSize();
            dirty = false;
        }
        return size;
    }



    /**
     * <p>Calculates the section's size. It is the sum of the lengths of the
     * section's header (8), the properties list (16 times the number of
     * properties) and the properties themselves.</p>
     *
     * @return the section's length in bytes.
     */
    private int calcSize()
    {
        int length = 0;

        /* The section header. */
        length += LittleEndianConsts.INT_SIZE * 2;

        /* The length of the property list. */
        Property[] psa = getProperties();
        if (psa == null)
            psa = new MutableProperty[0];
        length += psa.length * LittleEndianConsts.INT_SIZE * 3;

        /* The sum of the lengths of the properties - it is calculated by simply
         * writing the properties to a temporary byte array output stream: */
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        for (int i = 0; i < psa.length; i++)
        {
            final MutableProperty mp = new MutableProperty();
            mp.setID(psa[i].getID());
            mp.setType(psa[i].getType());
            mp.setValue(psa[i].getValue());
            try
            {
                length += mp.write(b);
            }
            catch (WritingNotSupportedException ex)
            {
                /* It was not possible to write the property, not even as a
                 * byte array. We cannot do anything about that. Instead of the
                 * property we insert an empty one into the stream. */
                mp.setType(Variant.VT_EMPTY);
                mp.setValue(null);
                try
                {
                    length += mp.write(b);
                }
                catch (Exception ex2)
                {
                    /* Even writing an empty property went awfully wrong.
                     * Let's give up. */
                    throw new HPSFRuntimeException(ex2);
                }
            }
            catch (IOException ex)
            {
                /* Should never occur. */
                throw new HPSFRuntimeException(ex);
            }
        }

        return length;
    }



    /**
     * <p>Writes this section into an output stream.</p>
     * 
     * <p>Internally this is done by writing into three byte array output
     * streams: one for the properties, one for the property list and one for
     * the section as such. The two former are appended to the latter when they
     * have received all their data.</p>
     *
     * @param out The stream to write into
     * @param offset The offset from the beginning of the property set
     * stream this section begins at
     *
     * @return The offset of the first byte following this section in
     * the property set stream.
     * @exception IOException if an I/O error occurs
     * @exception WritingNotSupportedException if HPSF does not yet support
     * writing a property's variant type.
     */
    public int write(final OutputStream out, final int offset)
        throws WritingNotSupportedException, IOException
    {
        /* The properties are written to this stream. */
        final ByteArrayOutputStream propertyStream =
            new ByteArrayOutputStream();

        /* The property list is established here. After each property that has
         * been written to "propertyStream", a property list entry is written to
         * "propertyListStream". */
        final ByteArrayOutputStream propertyListStream =
            new ByteArrayOutputStream();
 
        /* Maintain the current position in the list. */
        int position = 0;

        /* Increase the position variable by the size of the property list so
         * that it points to the beginning of the properties themselves. */
        position += 2 * LittleEndian.INT_SIZE +
                    getPropertyCount() * 2 * LittleEndian.INT_SIZE;

        /* Write the properties and the property list into their respective
         * streams: */
        for (final Iterator i = preprops.iterator(); i.hasNext();)
        {
            final MutableProperty p = (MutableProperty) i.next();
            
            /* Write the property list entry. */
            TypeWriter.writeUIntToStream(propertyListStream, p.getID());
            TypeWriter.writeUIntToStream(propertyListStream, position);
            
            /* Write the property and update the position to the next
             * property. */
            position += p.write(propertyStream);
        }
        propertyStream.close();
        propertyListStream.close();

        /* Write the section: */
        byte[] pb1 = propertyListStream.toByteArray();
        byte[] pb2 = propertyStream.toByteArray();
        TypeWriter.writeToStream(out, LittleEndian.INT_SIZE * 2 + pb1.length +
                                 pb2.length);
        TypeWriter.writeToStream(out, getPropertyCount());
        out.write(pb1);
        out.write(pb2);

        return offset + position;
    }



    /**
     * <p>Overwrites the super class' method to cope with a redundancy:
     * the property count is maintained in a separate member variable, but
     * shouldn't.</p>
     * 
     * @return The number of properties in this section
     */
    public int getPropertyCount()
    {
        return preprops.size();
    }



    /**
     * <p>Returns this section's properties.</p>
     * 
     * @return this section's properties.
     */
    public Property[] getProperties()
    {
        return (Property[]) preprops.toArray(new Property[0]);
    }

}
