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

import java.util.*;
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

    /* Codepage 1200 denotes Unicode. */
    private static int CP_UNICODE = 1200;

    private int id;


    /**
     * <p>Returns the property's ID.</p>
     *
     * @return The ID value
     */
    public int getID()
    {
        return id;
    }



    private long type;


    /**
     * <p>Returns the property's type.</p>
     *
     * @return The type value
     */
    public long getType()
    {
        return type;
    }



    private Object value;


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
		    int length, int codepage)
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

}
