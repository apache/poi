
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
 * Title:        SharedFormulaRecord
 * Description:  Primarily used as an excel optimization so that multiple similar formulas
 * 				  are not written out too many times.  We should recognize this record and
 *               serialize as is since this is used when reading templates.
 * <p>
 * Note: the documentation says that the SID is BC where biffviewer reports 4BC.  The hex dump shows
 * that the two byte sid representation to be 'BC 04' that is consistent with the other high byte
 * record types.
 * @author Danny Mui at apache dot org
 */

public class SharedFormulaRecord
    extends Record
{
	 public final static short   sid = 0x4BC;
    private short  size    = 0;
    private byte[] thedata = null;
    int             offset  = 0;

    public SharedFormulaRecord()
    {
    }

    /**
     * construct the sharedformula record, save all the information
     * @param id    id of the record -not validated, just stored for serialization
     * @param size  size of the data
     * @param data  the data
     */

    public SharedFormulaRecord(short id, short size, byte [] data)
    {
    	  super(id, size, data);
    	  
    	  this.fillFields(data, size, 0);
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


    protected void validateSid(short id)
    {
		if (id != this.sid)
		{
			throw new RecordFormatException("Not a valid SharedFormula");
		}
        
    }

    /**
     * print a sort of string representation ([SHARED FORMULA RECORD] id = x [/SHARED FORMULA RECORD])
     */

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[SHARED FORMULA RECORD:" + Integer.toHexString(sid) + "]\n");
        buffer.append("    .id        = ").append(Integer.toHexString(sid))
            .append("\n");
        buffer.append("[/SHARED FORMULA RECORD]\n");
        return buffer.toString();
    }

    public short getSid()
    {
        return this.sid;
    }

	 /**
	  * Shared formulas are to treated like unknown records, and as a result d
	  */
    protected void fillFields(byte [] data, short size, int offset)
    {
		thedata = new byte[size];
		System.arraycopy(data, 0, thedata, 0, size);		

    }

	/**
	 * Mirroring formula records so it is registered in the ValueRecordsAggregate
	 */
	public boolean isInValueSection()
	{
		 return true;
	}


	 /**
	  * Register it in the ValueRecordsAggregate so it can go into the FormulaRecordAggregate
	  */
	 public boolean isValue() {
	 	return true;
	 }

    public Object clone() {
      SharedFormulaRecord rec = new SharedFormulaRecord();
      rec.offset = offset;      
      rec.size = size;
      rec.thedata = thedata;
      return rec;
    }
}
