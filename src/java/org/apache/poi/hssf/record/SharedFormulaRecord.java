
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
