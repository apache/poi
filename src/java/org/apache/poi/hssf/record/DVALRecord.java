
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
 * Title:        DVAL Record<P>
 * Description:  used in data validation ;
 *               This record is the list header of all data validation records in the current sheet.
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 * @version 2.0-pre
 */

public class DVALRecord extends Record
{
    public final static short sid = 0x01B2;

    //unknown field ; it's size should be 10
    private short field_unknown     = 0x0000;

    //Object ID of the drop down arrow object for list boxes ;
    //in our case this will be always FFFF , until
    //MSODrawingGroup and MSODrawing records are implemented
    private int  field_cbo_id      = 0xFFFFFFFF;

    //Number of following DV records
    //Default value is 1
    private int  field_3_dv_no     = 0x00000000;

    public DVALRecord()
    {
    }

    /**
     * Constructs a DVAL record and sets its fields appropriately.
     *
     * @param in the RecordInputstream to read the record from
     */

    public DVALRecord(RecordInputStream in)
    {
        super(in);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A valid DVAL RECORD");
        }
    }

    protected void fillFields(RecordInputStream in)
    {
        for ( int i=0; i<5; i++)
        {
        	this.field_unknown = in.readShort();
        }
        this.field_cbo_id    = in.readInt(); 
        this.field_3_dv_no   = in.readInt();
    }

    /**
     * set the object ID of the drop down arrow object for list boxes
     * @param cboID - Object ID
     */
    public void setObjectID(int cboID)
    {
        this.field_cbo_id = cboID;
    }

    /**
     * Set the number of following DV records
     * @param dvNo - the DV records number
     */
    public void setDVRecNo(int dvNo)
    {
        this.field_3_dv_no = dvNo;
    }

    /**
     * get Object ID of the drop down arrow object for list boxes
     */
    public int getObjectID( )
    {
        return this.field_cbo_id;
    }

    /**
     * Get number of following DV records
     */
    public int getDVRecNo( )
    {
        return this.field_3_dv_no;
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[DVAL]\n");
        buffer.append("    .comboObjectID   = ").append(Integer.toHexString(this.getObjectID())).append("\n");
        buffer.append("    .DVRecordsNumber = ").append(Integer.toHexString(this.getDVRecNo())).append("\n");
        buffer.append("[/DVAL]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, this.sid);
        LittleEndian.putShort(data, 2 + offset, ( short)(this.getRecordSize()-4));
        for ( int i=0; i<5; i++)
        {
          LittleEndian.putShort(data, 4 + i*2 + offset, (short)this.field_unknown);
        }
        LittleEndian.putInt(data, 14 + offset, this.getObjectID());
        LittleEndian.putInt(data, 18 + offset, this.getDVRecNo());
        return getRecordSize();
    }

    //with 4 bytes header
    public int getRecordSize()
    {
        return 22;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone()
    {
      DVALRecord rec = new DVALRecord();
      rec.field_unknown = this.field_unknown;
      rec.field_cbo_id = this.field_cbo_id;
      rec.field_3_dv_no = this.field_3_dv_no;
      return rec;
    }
}