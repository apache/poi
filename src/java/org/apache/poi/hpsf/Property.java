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
 *  <p>
 *
 *  A property in a {@link Section} of a {@link PropertySet}.</p> <p>
 *
 *  The property's <strong>ID</strong> gives the property a meaning in the
 *  context of its {@link Section}. Each {@link Section} spans its own name
 *  space of property IDs.</p> <p>
 *
 *  The property's <strong>type</strong> determines how its <strong>value
 *  </strong> is interpreted. For example, if the type is {@link
 *  Variant#VT_LPSTR} (byte string), the value consists of a {@link DWord}
 *  telling how many bytes the string contains. The bytes follow immediately,
 *  including any null bytes that terminate the string. The type {@link
 *  Variant#VT_I4} denotes a four-byte integer value, {@link
 *  Variant#VT_FILETIME} some date and time (of a file).</p> <p>
 *
 *  <strong>FIXME:</strong> Reading of other types than those mentioned above
 *  and the dictionary property is not yet implemented.</p>
 *
 *@author     Rainer Klute (klute@rainer-klute.de)
 *@author     Drew Varner (Drew.Varner InAndAround sc.edu)
 *@created    May 10, 2002
 *@see        Section
 *@see        Variant
 *@version    $Id$
 *@since      2002-02-09
 */
public class Property {

    private int id;


    /**
     *  <p>
     *
     *  Returns the property's ID.</p>
     *
     *@return    The iD value
     */
    public int getID() {
        return id;
    }



    private long type;


    /**
     *  <p>
     *
     *  Returns the property's type.</p>
     *
     *@return    The type value
     */
    public long getType() {
        return type;
    }



    private Object value;


    /**
     *  <p>
     *
     *  Returns the property value's.</p>
     *
     *@return    The value value
     */
    public Object getValue() {
        return value;
    }



    /**
     *  <p>
     *
     *  Creates a {@link Property} instance by reading its bytes from the
     *  property set stream.</p>
     *
     *@param  id      The property's ID.
     *@param  src     The bytes the property set stream consists of.
     *@param  offset  The property's type/value pair's offset in the section.
     *@param  length  The property's type/value pair's length in bytes. list.
     */
    public Property(final int id, final byte[] src, final long offset,
            int length) {
        this.id = id;

        /*
         *  ID 0 is a special case since it specifies a dictionary of
         *  property IDs and property names.
         */
        if (id == 0) {
            value = readDictionary(src, offset, length);
            return;
        }

        /*
         *  FIXME: Support this!
         */
//        /* ID 1 is another special case: It denotes the code page of
//         * byte strings in this section. */
//        if (id == 1)
//        {
//            value = readCodepage(src, offset);
//            return;
//        }

        int o = (int) offset;
        type = LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;

        /*
         *  FIXME: Support reading more types!
         */
        switch ((int)type) {
            case Variant.VT_I4:
            {
                /*
                 *  Read a word. In Java it is represented as an
                 *  Integer object.
                 */
                value = new Long(LittleEndian.getUInt(src, o));
                break;
            }
            case Variant.VT_FILETIME:
            {
                /*
                 *  Read a FILETIME object. In Java it is represented
                 *  as a Date.
                 */
                final long low = LittleEndian.getUInt(src, o);
                o += LittleEndian.INT_SIZE;
                final long high = LittleEndian.getUInt(src, o);
                value = Util.filetimeToDate((int)high, (int)low);
                break;
            }
            case Variant.VT_LPSTR:
            {
                /*
                 *  Read a byte string. In Java it is represented as a
                 *  String. The null bytes at the end of the byte
                 *  strings must be stripped.
                 */
                final int first = o + LittleEndian.INT_SIZE;
                long last = first + LittleEndian.getUInt(src, o) - 1;
                o += LittleEndian.INT_SIZE;
                while (src[(int)last] == 0 && first <= last) {
                    last--;
                }
                value = new String(src, (int)first, (int)(last - first + 1));
                break;
            }
            case Variant.VT_CF:
            {
                /*
                 *  The first four bytes in src, from rc[offset] to
                 *  src[offset + 3] contain the DWord for VT_CF, so
                 *  skip it, we don't need it.
                 */
                /*
                 *  Truncate the length of the return array by a DWord
                 *  length (4 bytes).
                 */
                length = length - LittleEndian.INT_SIZE;

                final byte[] v = new byte[length];
                for (int i = 0; i < length; i++) {
                    v[i] = src[(int)(o + i)];
                }
                value = v;
                break;
            }
            case Variant.VT_BOOL:
            {
                /*
                 *  The first four bytes in src, from src[offset] to
                 *  src[offset + 3] contain the DWord for VT_BOOL, so
                 *  skip it, we don't need it.
                 */
                final int first = o + LittleEndian.INT_SIZE;
                long bool = LittleEndian.getUInt(src, o);
                if (bool == -1) {
                    value = new Boolean(true);
                } else if (bool == 0) {
                    value = new Boolean(false);
                } else {
                    throw new IllegalPropertySetDataException
                            ("Illegal property set data: A boolean must be " +
                            "either -1 (true) or 0 (false).");
                }
                break;
            }
            default:
            {
                final byte[] v = new byte[length];
                for (int i = 0; i < length; i++) {
                    v[i] = src[(int)(offset + i)];
                }
                value = v;
                break;
            }
        }
    }



    /**
     *  <p>
     *
     *  Reads a dictionary.</p>
     *
     *@param  src     The byte array containing the bytes making out the
     *      dictionary.
     *@param  offset  At this offset within <var>src</var> the dictionary
     *      starts.
     *@param  length  The dictionary contains at most this many bytes.
     *@return         Description of the Return Value
     */
    protected Map readDictionary(final byte[] src, final long offset,
            final int length) {
        /*
         *  FIXME: Check the length!
         */
        int o = (int)offset;

        /*
         *  Read the number of dictionary entries.
         */
        final long nrEntries = LittleEndian.getUInt(src, o);
        o += LittleEndian.INT_SIZE;

        final Map m = new HashMap((int)nrEntries, (float) 1.0);
        for (int i = 0; i < nrEntries; i++) {
            /*
             *  The key
             */
            final Long id = new Long(LittleEndian.getUInt(src, o));
            o += LittleEndian.INT_SIZE;

            /*
             *  The value (a string)
             */
            final long sLength = LittleEndian.getUInt(src, o);
            o += LittleEndian.INT_SIZE;
            /*
             *  Strip trailing 0x00 bytes.
             */
            long l = sLength;
            while (src[(int)(o + l - 1)] == 0x00) {
                l--;
            }
            final String s = new String(src, o, (int)l);
            o += sLength;
            m.put(id, s);
        }
        return m;
    }



    /**
     *  <p>
     *
     *  Reads a code page.</p>
     *
     *@param  src     The byte array containing the bytes making out the code
     *      page.
     *@param  offset  At this offset within <var>src</var> the code page starts.
     *@return         Description of the Return Value
     */
    protected int readCodePage(final byte[] src, final long offset) {
        throw new UnsupportedOperationException("FIXME");
    }

}
