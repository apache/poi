
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

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
 * Title:        Unknown Record (for debugging)<P>
 * Description:  Unknown record just tells you the sid so you can figure out
 *               what records you are missing.  Also helps us read/modify sheets we
 *               don't know all the records to.  (HSSF leaves these alone!) <P>
 * Company:      SuperLink Software, Inc.<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @author Glen Stampoultzis (glens at apache.org)
 */

public class UnknownRecord
    extends Record
{
    private short   sid     = 0;
    private byte[]  thedata = null;

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
        thedata = data;
    }

    public UnknownRecord( short id, short size, byte[] data, int offset )
    {
        sid     = id;
        thedata = new byte[size];
        System.arraycopy(data, offset, thedata, 0, size);
    }

    /**
     * spit the record out AS IS.  no interpretation or identification
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
        this.sid     = sid;
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

        buffer.append("[UNKNOWN RECORD:" + Integer.toHexString(sid) + "]\n");
        buffer.append("    .id        = ").append(Integer.toHexString(sid))
            .append("\n");
        buffer.append("[/UNKNOWN RECORD]\n");
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

    /** Unlike the other Record.clone methods this is a shallow clone*/
    public Object clone() {
      UnknownRecord rec = new UnknownRecord();
      rec.sid = sid;
      rec.thedata = thedata;
      return rec;
    }
}
