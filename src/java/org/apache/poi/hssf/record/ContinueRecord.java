
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

import java.util.ArrayList;

import org.apache.poi.util.LittleEndian;

/**
 * Title:        Continue Record - Helper class used primarily for SST Records <P>
 * Description:  handles overflow for prior record in the input
 *               stream; content is tailored to that prior record<P>
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public class ContinueRecord
    extends Record
{
    public final static short sid = 0x003C;
    private byte[]            field_1_data;

    /**
     * default constructor
     */

    public ContinueRecord()
    {
    }

    /**
     * Main constructor -- kinda dummy because we don't validate or fill fields
     *
     * @param id record id
     * @param size record size
     * @param data raw data
     */

    public ContinueRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Main constructor -- kinda dummy because we don't validate or fill fields
     *
     * @param id record id
     * @param size record size
     * @param data raw data
     * @param offset of the record's data
     */

    public ContinueRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    /**
     * USE ONLY within "processContinue"
     */

    public byte [] serialize()
    {
        byte[] retval = new byte[ field_1_data.length + 4 ];

        LittleEndian.putShort(retval, 0, sid);
        LittleEndian.putShort(retval, 2, ( short ) field_1_data.length);
        System.arraycopy(field_1_data, 0, retval, 4, field_1_data.length);
        return retval;
    }

    public int serialize(int offset, byte [] data)
    {
        throw new RecordFormatException(
            "You're not supposed to serialize Continue records like this directly");
    }

    /**
     * set the data for continuation
     * @param data - a byte array containing all of the continued data
     */

    public void setData(byte [] data)
    {
        field_1_data = data;
    }

    /**
     * get the data for continuation
     * @return byte array containing all of the continued data
     */

    public byte [] getData()
    {
        return field_1_data;
    }

    /**
     * Use to serialize records that are too big for their britches (>8228..why 8228 and
     * not 8192 aka 8k?  Those folks in washington don't ususally make sense...
     * or at least to anyone outside fo marketing...
     * @deprecated handle this within the record...this didn't actualyl work out
     */

    public static byte [] processContinue(byte [] data)
    {   // could do this recursively but that seems hard to debug

        // how many continue records do we need
        // System.out.println("In ProcessContinue");
        int       records   =
            (data.length
             / 8214);   // we've a 1 offset but we're also off by one due to rounding...so it balances out
        int       offset    = 8214;

        // System.out.println("we have "+records+" continue records to process");
        ArrayList crs       = new ArrayList(records);
        int       totalsize = 8214;
        byte[]    retval    = null;

        for (int cr = 0; cr < records; cr++)
        {
            ContinueRecord contrec   = new ContinueRecord();
            int            arraysize = Math.min((8214 - 4),
                                                (data.length - offset));
            byte[]         crdata    = new byte[ arraysize ];

            System.arraycopy(data, offset, crdata, 0, arraysize);

            // System.out.println("arraycopy(data,"+offset+",crdata,"+0+","+arraysize+");");
            offset += crdata.length;
            contrec.setData(crdata);
            crs.add(contrec.serialize());
        }
        for (int cr = 0; cr < records; cr++)
        {
            totalsize += (( byte [] ) crs.get(cr)).length;
        }

        // System.out.println("totalsize="+totalsize);
        retval = new byte[ totalsize ];
        offset = 8214;
        System.arraycopy(data, 0, retval, 0, 8214);
        for (int cr = 0; cr < records; cr++)
        {
            byte[] src = ( byte [] ) crs.get(cr);

            System.arraycopy(src, 0, retval, offset, src.length);

            // System.out.println("arraycopy(src,"+0+",retval,"+offset+","+src.length+");");
            offset += src.length;
        }
        return retval;
    }

    /**
     * Fill the fields. Only thing is, this record has no fields --
     *
     * @param ignored_parm1 Ignored
     * @param ignored_parm2 Ignored
     */

    protected void fillFields(byte [] ignored_parm1, short ignored_parm2)
    {

        // throw new RecordFormatException("Are you crazy?  Don't fill a continue record");
        // do nothing
    }

    /**
     * Make sure we have a good id
     *
     * @param id the alleged id
     */

    protected void validateSid(short id)
    {
        if (id != ContinueRecord.sid)
        {
            throw new RecordFormatException("Not a Continue Record");
        }
    }

    /**
     * Debugging toString
     *
     * @return string representation
     */

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CONTINUE RECORD]\n");
        buffer.append("    .id        = ").append(Integer.toHexString(sid))
            .append("\n");
        buffer.append("[/CONTINUE RECORD]\n");
        return buffer.toString();
    }

    public short getSid()
    {
        return this.sid;
    }

    /**
     * Fill the fields. Only thing is, this record has no fields --
     *
     * @param ignored_parm1 Ignored
     * @param ignored_parm2 Ignored
     * @param ignored_parm3 Ignored
     */

    protected void fillFields(byte [] ignored_parm1, short ignored_parm2, int ignored_parm3)
    {
    }
}
