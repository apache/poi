
/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

/**
 * Title:        Continue Record - Helper class used primarily for SST Records <P>
 * Description:  handles overflow for prior record in the input
 *               stream; content is tailored to that prior record<P>
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
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
     * @param in the RecordInputstream to read the record from
     */

    public ContinueRecord(RecordInputStream in)
    {
        super(in);
    }

    /**
     * USE ONLY within "processContinue"
     */

    public byte [] serialize()
    {
        byte[] retval = new byte[ field_1_data.length + 4 ];
        serialize(0, retval);
        return retval;
    }

    public int serialize(int offset, byte [] data)
    {

        LittleEndian.putShort(data, offset, sid);
        LittleEndian.putShort(data, offset + 2, ( short ) field_1_data.length);
        System.arraycopy(field_1_data, 0, data, offset + 4, field_1_data.length);
        return field_1_data.length + 4;
        // throw new RecordFormatException(
        //    "You're not supposed to serialize Continue records like this directly");
    }

	/*
     * @param data raw data
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
        return sid;
    }

    /**
     * Fill the fields. Only thing is, this record has no fields --
     *
     * @param in the RecordInputstream to read the record from
     */

    protected void fillFields(RecordInputStream in)
    {
      field_1_data = in.readRemainder();
    }

    /**
     * Clone this record.
     */
    public Object clone() {
      ContinueRecord clone = new ContinueRecord();
      clone.setData(field_1_data);
      return clone;
    }

}
