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

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/**
 * The obj record is used to hold various graphic objects and controls.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public class ObjRecord
    extends Record
{
    public final static short      sid                             = 0x5D;
    private List subrecords;

    //00000000 15 00 12 00 01 00 01 00 11 60 00 00 00 00 00 0D .........`......
    //00000010 26 01 00 00 00 00 00 00 00 00                   &.........


    public ObjRecord()
    {
        subrecords = new ArrayList(2);
    }

    /**
     * Constructs a OBJ record and sets its fields appropriately.
     *
     * @param id    id must be 0x5D or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public ObjRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a obj record and sets its fields appropriately.
     *
     * @param id    id must be 0x5D or an exception
     *              will be throw upon validation
     * @param size  size the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */
    public ObjRecord(short id, short size, byte[] data, int offset)
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
            throw new RecordFormatException("Not an OBJ record");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        subrecords = new ArrayList();
        int pos = offset;
        while (pos - offset < size)
        {
            short subRecordSid = LittleEndian.getShort(data, pos);
            short subRecordSize = LittleEndian.getShort(data, pos + 2);
            Record subRecord = SubRecord.createSubRecord(subRecordSid, subRecordSize, data, pos + 4);
            subrecords.add(subRecord);
            pos += 4 + subRecordSize;
        }

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[OBJ]\n");
        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
        {
            Record record = (Record) iterator.next();
            buffer.append("SUBRECORD: " + record.toString());
        }
        buffer.append("[/OBJ]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte[] data)
    {
        int pos = 0;

        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short)(getRecordSize() - 4));

        pos = offset + 4;
        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
        {
            Record record = (Record) iterator.next();
            pos += record.serialize(pos, data);
        }

        return getRecordSize();
    }

    /**
     * Size of record (excluding 4 byte header)
     */
    public int getRecordSize()
    {
        int size = 0;
        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
        {
            Record record = (Record) iterator.next();
            size += record.getRecordSize();
        }
        return 4  + size;
    }

    public short getSid()
    {
        return sid;
    }

    public List getSubRecords()
    {
        return subrecords;
    }

    public void clearSubRecords()
    {
        subrecords.clear();
    }

    public void addSubRecord(int index, Object element)
    {
        subrecords.add( index, element );
    }

    public boolean addSubRecord(Object o)
    {
        return subrecords.add( o );
    }

    public Object clone()
    {
        ObjRecord rec = new ObjRecord();
        rec.subrecords = new ArrayList();

        for ( Iterator iterator = subrecords.iterator(); iterator.hasNext(); )
            subrecords.add(( (Record) iterator.next() ).clone());

        return rec;
    }

}  // END OF CLASS




