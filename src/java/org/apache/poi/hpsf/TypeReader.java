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

import java.util.*;
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
                final int first = offset + LittleEndian.INT_SIZE;
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
