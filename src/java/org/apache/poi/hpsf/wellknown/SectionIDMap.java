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
package org.apache.poi.hpsf.wellknown;

import java.util.*;

/**
 * <p>Maps section format IDs to {@link PropertyIDMap}s. It is
 * initialized with two well-known section format IDs: those of the
 * <tt>\005SummaryInformation</tt> stream and the
 * <tt>\005DocumentSummaryInformation</tt> stream.</p>
 *
 * <p>If you have a section format ID you can use it as a key to query
 * this map.  If you get a {@link PropertyIDMap} returned your section
 * is well-known and you can query the {@link PropertyIDMap} for PID
 * strings. If you get back <code>null</code> you are on your own.</p>
 *
 * <p>This {@link Map} expects the byte arrays of section format IDs
 * as keys. A key maps to a {@link PropertyIDMap} describing the
 * property IDs in sections with the specified section format ID.</p>
 *
 * @author Rainer Klute (klute@rainer-klute.de)
 * @version $Id$
 * @since 2002-02-09
 */
public class SectionIDMap extends HashMap
{

    /**
     * <p>The SummaryInformation's section's format ID.</p>
     */
    public final static byte[] SUMMARY_INFORMATION_ID = new byte[]
    {
	(byte) 0xF2, (byte) 0x9F, (byte) 0x85, (byte) 0xE0,
	(byte) 0x4F, (byte) 0xF9, (byte) 0x10, (byte) 0x68,
	(byte) 0xAB, (byte) 0x91, (byte) 0x08, (byte) 0x00,
	(byte) 0x2B, (byte) 0x27, (byte) 0xB3, (byte) 0xD9
    };

    /**
     * <p>The DocumentSummaryInformation's first section's format
     * ID. The second section has a different format ID which is not
     * well-known.</p>
     */
    public final static byte[] DOCUMENT_SUMMARY_INFORMATION_ID = new byte[]
    {
	(byte) 0xD5, (byte) 0xCD, (byte) 0xD5, (byte) 0x02,
	(byte) 0x2E, (byte) 0x9C, (byte) 0x10, (byte) 0x1B,
	(byte) 0x93, (byte) 0x97, (byte) 0x08, (byte) 0x00,
	(byte) 0x2B, (byte) 0x2C, (byte) 0xF9, (byte) 0xAE
    };

    public final static String UNDEFINED = "[undefined]";

    private static SectionIDMap defaultMap;



    /**
     * <p>Returns the singleton instance of the default {@link
     * SectionIDMap}.</p>
     *
     * @return The instance value
     */
    public static SectionIDMap getInstance()
    {
        if (defaultMap == null)
	{
            final SectionIDMap m = new SectionIDMap();
            m.put(SUMMARY_INFORMATION_ID,
		  PropertyIDMap.getSummaryInformationProperties());
            m.put(DOCUMENT_SUMMARY_INFORMATION_ID,
		  PropertyIDMap.getDocumentSummaryInformationProperties());
            defaultMap = m;
        }
        return defaultMap;
    }



    /**
     * <p>Returns the property ID string that is associated with a
     * given property ID in a section format ID's namespace.</p>
     *
     * @param sectionFormatID Each section format ID has its own name
     * space of property ID strings and thus must be specified.
     * @param  pid The property ID
     * @return The well-known property ID string associated with the
     * property ID <var>pid</var> in the name space spanned by <var>
     * sectionFormatID</var> . If the <var>pid</var>
     * /<var>sectionFormatID </var> combination is not well-known, the
     * string "[undefined]" is returned.
     */
    public static String getPIDString(final byte[] sectionFormatID,
				      final int pid)
    {
        final PropertyIDMap m =
	    (PropertyIDMap) getInstance().get(sectionFormatID);
        if (m == null)
            return UNDEFINED;
        else
	{
            final String s = (String) m.get(pid);
            if (s == null)
                return UNDEFINED;
            return s;
        }
    }



    /**
     * <p>Returns the {@link PropertyIDMap} for a given section format
     * ID.</p>
     */
    public PropertyIDMap get(final byte[] sectionFormatID)
    {
        return (PropertyIDMap) super.get(new String(sectionFormatID));
    }



    /**
     * <p>Returns the {@link PropertyIDMap} for a given section format
     * ID.</p>
     *
     * @param sectionFormatID A section format ID as a <tt>byte[]</tt> .
     * @deprecated Use {@link #get(byte[])} instead!
     */
    public Object get(final Object sectionFormatID)
    {
        return get((byte[]) sectionFormatID);
    }



    /**
     * <p>Associates a section format ID with a {@link
     * PropertyIDMap}.</p>
     */
    public Object put(final byte[] sectionFormatID,
		      final PropertyIDMap propertyIDMap)
    {
        return super.put(new String(sectionFormatID), propertyIDMap);
    }



    /**
     * @deprecated Use {@link #put(byte[], PropertyIDMap)} instead!
     */
    public Object put(final Object key, final Object value)
    {
        return put((byte[]) key, (PropertyIDMap) value);
    }

}
