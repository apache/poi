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
 * Title:        DATAVALIDATIONS Record<P>
 * Description:  used in data validation ;
 *               This record is the list header of all data validation records (0x01BE) in the current sheet.
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public class DVALRecord extends Record
{
	public final static short sid = 0x01B2;

	/** Options of the DVAL */
	private short field_1_options;
	/** Horizontal position of the dialog */
	private int field_2_horiz_pos;
	/** Vertical position of the dialog */
	private int field_3_vert_pos;

	/** Object ID of the drop down arrow object for list boxes ;
	 * in our case this will be always FFFF , until
	 * MSODrawingGroup and MSODrawing records are implemented */
	private int  field_cbo_id;

	/** Number of following DV Records */
	private int  field_5_dv_no;

    public DVALRecord() {
        field_cbo_id = 0xFFFFFFFF;
        field_5_dv_no = 0x00000000;
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

	protected void fillFields(RecordInputStream in)
	{
		this.field_1_options = in.readShort();
		this.field_2_horiz_pos = in.readInt();
		this.field_3_vert_pos = in.readInt();
        this.field_cbo_id    = in.readInt(); 
        this.field_5_dv_no   = in.readInt();
	}


    /**
	 * @param field_1_options the options of the dialog
	 */
	public void setOptions(short field_1_options) {
		this.field_1_options = field_1_options;
	}

	/**
	 * @param field_2_horiz_pos the Horizontal position of the dialog
	 */
	public void setHorizontalPos(int field_2_horiz_pos) {
		this.field_2_horiz_pos = field_2_horiz_pos;
	}

	/**
	 * @param field_3_vert_pos the Vertical position of the dialog
	 */
	public void setVerticalPos(int field_3_vert_pos) {
		this.field_3_vert_pos = field_3_vert_pos;
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
        this.field_5_dv_no = dvNo;
    }

    
    
    /**
	 * @return the field_1_options
	 */
	public short getOptions() {
		return field_1_options;
	}

	/**
	 * @return the Horizontal position of the dialog
	 */
	public int getHorizontalPos() {
		return field_2_horiz_pos;
	}

	/**
	 * @return the the Vertical position of the dialog
	 */
	public int getVerticalPos() {
		return field_3_vert_pos;
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
        return this.field_5_dv_no;
    }


	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("[DVAL]\n");
		buffer.append("    .options      = ").append(this.getOptions()).append('\n');
		buffer.append("    .horizPos     = ").append(this.getHorizontalPos()).append('\n');
		buffer.append("    .vertPos      = ").append(this.getVerticalPos()).append('\n');
		buffer.append("    .comboObjectID   = ").append(Integer.toHexString(this.getObjectID())).append("\n");
		buffer.append("    .DVRecordsNumber = ").append(Integer.toHexString(this.getDVRecNo())).append("\n");
		buffer.append("[/DVAL]\n");
		return buffer.toString();
	}

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, this.sid);
        LittleEndian.putShort(data, 2 + offset, ( short)(this.getRecordSize()-4));
		
		LittleEndian.putShort(data, 4 + offset, this.getOptions());
		LittleEndian.putInt(data, 6 + offset, this.getHorizontalPos());
		LittleEndian.putInt(data, 10 + offset, this.getVerticalPos());
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
      rec.field_1_options = field_1_options;
      rec.field_2_horiz_pos = field_2_horiz_pos;
      rec.field_3_vert_pos = field_3_vert_pos;
      rec.field_cbo_id = this.field_cbo_id;
      rec.field_5_dv_no = this.field_5_dv_no;
      return rec;
    }
}
