
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Title:        Unknown Record (for debugging)<P>
 * Description:  Unknown record just tells you the sid so you can figure out
 *               what records you are missing.  Also helps us read/modify sheets we
 *               don't know all the records to.  (HSSF leaves these alone!) <P>
 * Company:      SuperLink Software, Inc.<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class UnknownRecord
    extends Record
{
    private short  sid     = 0;
    private short  size    = 0;
    private byte[] thedata = null;
    int            offset  = 0;

    public UnknownRecord()
    {
    }

    /**
     * construct an unknown record.  No fields are interperated and the record will
     * be serialized in its original form more or less
     * @param id    id of the record -not validated, just stored for serialization
     * @param size  size of the data
     * @param data  the data
     */

    public UnknownRecord(short id, short size, byte [] data)
    {
        sid     = id;
        size    = size;
        thedata = data;
    }

    /**
     * spit the record out AS IS.  no interperatation or identification
     */

    public int serialize(int offset, byte [] data)
    {
        if (thedata == null)
        {
            thedata = new byte[ 0 ];
        }
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) (thedata.length));
        if (thedata.length > 0)
        {
            System.arraycopy(thedata, 0, data, 4 + offset, thedata.length);
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        int retval = 4;

        if (thedata != null)
        {
            retval += thedata.length;
        }
        return retval;
    }

    protected void fillFields(byte [] data, short sid)
    {
        sid     = sid;
        thedata = data;
    }

    /**
     * NO OP!
     */

    protected void validateSid(short id)
    {

        // if we had a valid sid we wouldn't be using the "Unknown Record" record now would we?
    }

    /**
     * print a sort of string representation ([UNKNOWN RECORD] id = x [/UNKNOWN RECORD])
     */

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[UNKNOWN RECORD]\n");
        buffer.append("    .id        = ").append(Integer.toHexString(sid))
            .append("\n");
        buffer.append("[/UNKNWON RECORD]\n");
        return buffer.toString();
    }

    public short getSid()
    {
        return this.sid;
    }

    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param data raw data
     * @param size size of data
     * @param offset of the records data (provided a big array of the file)
     */

    protected void fillFields(byte [] data, short size, int offset)
    {
        throw new RecordFormatException(
            "Unknown record cannot be constructed via offset -- we need a copy of the data");
    }
}
