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

public class DrawingRecord extends Record
{
    public static final short sid = 0xEC;

    private byte[] recordData;
    private byte[] contd;

    public DrawingRecord()
    {
    }

    public DrawingRecord( RecordInputStream in )
    {
        super( in );
    }

    protected void fillFields( RecordInputStream in )
    {
      recordData = in.readRemainder();
    }

    public void processContinueRecord( byte[] record )
    {
        //don't merge continue record with the drawing record, it must be serialized separately
        contd = record;
    }

    public int serialize( int offset, byte[] data )
    {
        if (recordData == null)
        {
            recordData = new byte[ 0 ];
        }
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) (recordData.length));
        if (recordData.length > 0)
        {
            System.arraycopy(recordData, 0, data, 4 + offset, recordData.length);
        }
        return getRecordSize();
    }

    public int getRecordSize()
    {
        int retval = 4;

        if (recordData != null)
        {
            retval += recordData.length;
        }
        return retval;
    }

    public short getSid()
    {
        return sid;
    }

    public byte[] getData()
    {
        if(contd != null) {
            byte[] newBuffer = new byte[ recordData.length + contd.length ];
            System.arraycopy( recordData, 0, newBuffer, 0, recordData.length );
            System.arraycopy( contd, 0, newBuffer, recordData.length, contd.length);
            return newBuffer;
        } else {
            return recordData;
        }
    }

    public void setData( byte[] thedata )
    {
        this.recordData = thedata;
    }

    public Object clone() {
    	DrawingRecord rec = new DrawingRecord();
    	
        if (recordData != null) {
        	rec.recordData = new byte[ recordData.length ];
        	System.arraycopy(recordData, 0, rec.recordData, 0, recordData.length);
        }
    	if (contd != null) {
	    	System.arraycopy(contd, 0, rec.contd, 0, contd.length);
	    	rec.contd = new byte[ contd.length ];
    	}
    	
    	return rec;
    }
}