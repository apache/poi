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

import java.util.HashMap;
import java.util.Map;
import org.apache.poi.util.LittleEndian;

/**
 * <p>A property in a {@link Section} of a {@link PropertySet}.</p>
 *
 * <p>The property's <strong>ID</strong> gives the property a meaning
 * in the context of its {@link Section}. Each {@link Section} spans
 * its own name space of property IDs.</p>
 *
 * <p>The property's <strong>type</strong> determines how its
 * <strong>value </strong> is interpreted. For example, if the type is
 * {@link Variant#VT_LPSTR} (byte string), the value consists of a
 * DWord telling how many bytes the string contains. The bytes follow
 * immediately, including any null bytes that terminate the
 * string. The type {@link Variant#VT_I4} denotes a four-byte integer
 * value, {@link Variant#VT_FILETIME} some date and time (of a
 * file).</p>
 *
 * <p><strong>FIXME:</strong> Reading is not implemented for all
 * {@link Variant} types yet. Feel free to submit error reports or
 * patches for the types you need.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @author Drew Varner (Drew.Varner InAndAround sc.edu)
 * @see Section
 * @see Variant
 * @version $Id$
 * @since 2002-02-09
 */
public class Property
{

    /** <p>Codepage 1200 denotes Unicode.</p> */
    private static final int CP_UNICODE = 1200;

    /** <p>The property's ID.</p> */
    protected int id;


    /**
     * <p>Returns the property's ID.</p>
     *
     * @return The ID value
     */
    public int getID()
    {
        return id;
    }



    /** <p>The property's type.</p> */
    protected long type;


    /**
     * <p>Returns the property's type.</p>
     *
     * @return The type value
     */
    public long getType()
    {
        return type;
    }



    /** <p>The property's value.</p> */
    protected Object value;


    /**
     * <p>Returns the property's value.</p>
     *
     * @return The property's value
     */
    public Object getValue()
    {
        return value;
    }



    /**
     * <p>Creates a {@link Property} instance by reading its bytes
     * from the property set stream.</p>
     *
     * @param id The property's ID.
     * @param src The bytes the property set stream consists of.
     * @param offset The property's type/value pair's offset in the
     * section.
     * @param length The property's type/value pair's length in bytes.
     * @param codepage The section's and thus the property's
     * codepage. It is needed only when reading string values.
     */
    public Property(final int id, final byte[] src, final long offset,
                    final int length, final int codepage)
    {
        this.id = id;

        /*
         * ID 0 is a special case since it specifies a dictionary of
         * property IDs and property names.
         */
        if (id == 0)
        {
            value = readDictionary(src, offset, length, codepage);
            return;
        }

        int o = (int) offset;
        type = LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;

        try
        {
            value = TypeReader.read(src, o, length, (int) type);
        }
        catch (Throwable t)
        {
            t.printStackTrace();
            value = "*** null ***";
        }
    }



    /**
     * <p>Creates an empty property. It must be filled using the set method to
     * be usable.</p>
     */
    protected Property()
    {}



    /**
     * <p>Reads a dictionary.</p>
     *
     * @param src The byte array containing the bytes making out the
     * dictionary.
     * @param offset At this offset within <var>src</var> the
     * dictionary starts.
     * @param length The dictionary contains at most this many bytes.
     * @param codepage The codepage of the string values.
     * @return The dictonary
     */
    protected Map readDictionary(final byte[] src, final long offset,
                                 final int length, final int codepage)
    {
        /* Check whether "offset" points into the "src" array". */
        if (offset < 0 || offset > src.length)
            throw new HPSFRuntimeException
                ("Illegal offset " + offset + " while HPSF stream contains " +
                 length + " bytes.");
        int o = (int) offset;

        /*
         * Read the number of dictionary entries.
         */
        final long nrEntries = LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;

        final Map m = new HashMap((int) nrEntries, (float) 1.0);
        for (int i = 0; i < nrEntries; i++)
        {
            /* The key. */
            final Long id = new Long(LittleEndian.getUInt(src, o));
            o += LittleEndian.INT_SIZE;

            /* The value (a string). The length is the either the
             * number of characters if the character set is Unicode or
             * else the number of bytes. The length includes
             * terminating 0x00 bytes which we have to strip off to
             * create a Java string. */
            long sLength = LittleEndian.getUInt(src, o);
            o += LittleEndian.INT_SIZE;

            /* Read the bytes or characters depending on whether the
             * character set is Unicode or not. */
            StringBuffer b = new StringBuffer((int) sLength);
            for (int j = 0; j < sLength; j++)
                if (codepage == CP_UNICODE)
                {
                    final int i1 = o + (j * 2);
                    final int i2 = i1 + 1;
                    b.append((char) ((src[i2] << 8) + src[i1]));
                }
                else
                    b.append((char) src[o + j]);

            /* Strip 0x00 characters from the end of the string: */
            while (b.charAt(b.length() - 1) == 0x00)
                b.setLength(b.length() - 1);
            if (codepage == CP_UNICODE)
            {
                if (sLength % 2 == 1)
                    sLength++;
                o += (sLength + sLength);
            }
            else
                o += sLength;
            m.put(id, b.toString());
        }
        return m;
    }



    /**
     * <p>Returns the property's size in bytes. This is always a multiple of
     * 4.</p>
     *
     * @return the property's size in bytes
     */
    protected int getSize()
    {
        throw new UnsupportedOperationException("FIXME: Not yet implemented.");
    }



    public boolean equals(Object o)
    {
        throw new UnsupportedOperationException("FIXME: Not yet implemented.");
    }

}
