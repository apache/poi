
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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



import org.apache.poi.util.*;

/**
 * The group marker record is used as a position holder for groups.

 * @author Glen Stampoultzis (glens at apache.org)
 */
public class GroupMarkerSubRecord
    extends SubRecord
{
    public final static short      sid                             = 0x06;

    private byte[] reserved = new byte[0];    // would really love to know what goes in here.

    public GroupMarkerSubRecord()
    {

    }

    /**
     * Constructs a group marker record and sets its fields appropriately.
     *
     * @param id    id must be 0x00 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public GroupMarkerSubRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    
    }

    /**
     * Constructs a group marker record and sets its fields appropriately.
     *
     * @param id    id must be 0x00 or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */

    public GroupMarkerSubRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    
    }

    /**
     * Checks the sid matches the expected side for this record
     *
     * @param id   the expected sid.
     */
    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("Not a Group Marker record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
//        int pos = 0;
        reserved = new byte[size];
        System.arraycopy(data, offset, reserved, 0, size);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        String nl = System.getProperty("line.separator");
        buffer.append("[ftGmo]" + nl);
        buffer.append("  reserved = ").append(HexDump.toHex(reserved)).append(nl);
        buffer.append("[/ftGmo]" + nl);
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));
        System.arraycopy(reserved, 0, data, offset + 4, getRecordSize() - 4);

        return getRecordSize();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getRecordSize()
    {
        return 4 + reserved.length;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
        GroupMarkerSubRecord rec = new GroupMarkerSubRecord();
        rec.reserved = new byte[reserved.length];
        for ( int i = 0; i < reserved.length; i++ )
            rec.reserved[i] = reserved[i];
        return rec;
    }



}  // END OF CLASS


