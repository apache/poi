
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

/*
 * ExtSSTInfoSubRecord.java
 *
 * Created on September 8, 2001, 8:37 PM
 */
package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Extended SST table info subrecord<P>
 * contains the elements of "info" in the SST's array field<P>
 * WE HAVE VERY LITTLE INFORMATION ON HOW TO IMPLEMENT THIS RECORD! (EXTSSST)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 * @see org.apache.poi.hssf.record.ExtSSTRecord
 */

public class ExtSSTInfoSubRecord
    extends Record
{
    public final static short sid =
        0xFFF;                                             // only here for conformance, doesn't really have an sid
    private int               field_1_stream_pos;          // stream pointer to the SST record
    private short             field_2_bucket_sst_offset;   // don't really understand this yet.
    private short             field_3_zero;                // must be 0;

    /** Creates new ExtSSTInfoSubRecord */

    public ExtSSTInfoSubRecord()
    {
    }

    public ExtSSTInfoSubRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    public ExtSSTInfoSubRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {

        // do nothing
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_stream_pos        = LittleEndian.getInt(data, 0 + offset);
        field_2_bucket_sst_offset = LittleEndian.getShort(data, 4 + offset);
        field_3_zero              = LittleEndian.getShort(data, 6 + offset);
    }

    public void setStreamPos(int pos)
    {
        field_1_stream_pos = pos;
    }

    public void setBucketSSTOffset(short offset)
    {
        field_2_bucket_sst_offset = offset;
    }

    public int getStreamPos()
    {
        return field_1_stream_pos;
    }

    public short getBucketSSTOffset()
    {
        return field_2_bucket_sst_offset;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[EXTSST]\n");
        buffer.append("    .streampos      = ")
            .append(Integer.toHexString(getStreamPos())).append("\n");
        buffer.append("    .bucketsstoffset= ")
            .append(Integer.toHexString(getBucketSSTOffset())).append("\n");
        buffer.append("    .zero           = ")
            .append(Integer.toHexString(field_3_zero)).append("\n");
        buffer.append("[/EXTSST]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putInt(data, 0 + offset, getStreamPos());
        LittleEndian.putShort(data, 4 + offset, getBucketSSTOffset());
        LittleEndian.putShort(data, 6 + offset, ( short ) 0);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 8;
    }

    public short getSid()
    {
        return this.sid;
    }
}
