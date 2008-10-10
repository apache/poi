
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
 * Title:        Continue Record(0x003C) - Helper class used primarily for SST Records <P>
 * Description:  handles overflow for prior record in the input
 *               stream; content is tailored to that prior record<P>
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
 */
public final class ContinueRecord extends Record {
    public final static short sid = 0x003C;
    private byte[]            _data;

    public ContinueRecord(byte[] data) {
        _data = data;
    }

    /**
     * USE ONLY within "processContinue"
     */
    public byte [] serialize()
    {
        byte[] retval = new byte[ _data.length + 4 ];
        serialize(0, retval);
        return retval;
    }

    public int serialize(int offset, byte[] data) {
        return write(data, offset, null, _data);
    }

    /**
     * get the data for continuation
     * @return byte array containing all of the continued data
     */
    public byte [] getData()
    {
        return _data;
    }

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
    public ContinueRecord(RecordInputStream in)
    {
      _data = in.readRemainder();
    }

    public Object clone() {
      return new ContinueRecord(_data);
    }

    /**
     * Writes the full encoding of a Continue record without making an instance
     */
    public static int write(byte[] destBuf, int destOffset, Byte initialDataByte, byte[] srcData) {
        return write(destBuf, destOffset, initialDataByte, srcData, 0, srcData.length);
    }
    /**
     * @param initialDataByte (optional - often used for unicode flag). 
     * If supplied, this will be written before srcData
     * @return the total number of bytes written
     */
    public static int write(byte[] destBuf, int destOffset, Byte initialDataByte, byte[] srcData, int srcOffset, int len) {
        int totalLen = len + (initialDataByte == null ? 0 : 1);
        LittleEndian.putUShort(destBuf, destOffset, sid);
        LittleEndian.putUShort(destBuf, destOffset + 2, totalLen);
        int pos = destOffset + 4;
        if (initialDataByte != null) {
            LittleEndian.putByte(destBuf, pos, initialDataByte.byteValue());
            pos += 1;
        }
        System.arraycopy(srcData, srcOffset, destBuf, pos, len);
        return 4 + totalLen;
    }
}
