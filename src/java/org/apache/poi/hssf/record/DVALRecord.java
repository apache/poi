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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        DATAVALIDATIONS Record (0x01B2)<p/>
 * Description:  used in data validation ;
 *               This record is the list header of all data validation records (0x01BE) in the current sheet.
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public final class DVALRecord extends StandardRecord {
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

	public DVALRecord(RecordInputStream in) {
		field_1_options = in.readShort();
		field_2_horiz_pos = in.readInt();
		field_3_vert_pos = in.readInt();
        field_cbo_id    = in.readInt(); 
        field_5_dv_no   = in.readInt();
	}

    /**
	 * @param options the options of the dialog
	 */
	public void setOptions(short options) {
		field_1_options = options;
	}

	/**
	 * @param horiz_pos the Horizontal position of the dialog
	 */
	public void setHorizontalPos(int horiz_pos) {
		field_2_horiz_pos = horiz_pos;
	}

	/**
	 * @param vert_pos the Vertical position of the dialog
	 */
	public void setVerticalPos(int vert_pos) {
		field_3_vert_pos = vert_pos;
	}

	/**
     * set the object ID of the drop down arrow object for list boxes
     * @param cboID - Object ID
     */
    public void setObjectID(int cboID) {
        field_cbo_id = cboID;
    }

    /**
     * Set the number of following DV records
     * @param dvNo - the DV records number
     */
    public void setDVRecNo(int dvNo) {
        field_5_dv_no = dvNo;
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
    public int getObjectID() {
        return field_cbo_id;
    }

    /**
     * Get number of following DV records
     */
    public int getDVRecNo() {
        return field_5_dv_no;
    }


	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append("[DVAL]\n");
		buffer.append("    .options      = ").append(getOptions()).append('\n');
		buffer.append("    .horizPos     = ").append(getHorizontalPos()).append('\n');
		buffer.append("    .vertPos      = ").append(getVerticalPos()).append('\n');
		buffer.append("    .comboObjectID   = ").append(Integer.toHexString(getObjectID())).append("\n");
		buffer.append("    .DVRecordsNumber = ").append(Integer.toHexString(getDVRecNo())).append("\n");
		buffer.append("[/DVAL]\n");
		return buffer.toString();
	}

    public void serialize(LittleEndianOutput out) {
 		
		out.writeShort(getOptions());
		out.writeInt(getHorizontalPos());
		out.writeInt(getVerticalPos());
		out.writeInt(getObjectID());
		out.writeInt(getDVRecNo());
    }

    protected int getDataSize() {
        return 18;
    }

    public short getSid() {
        return sid;
    }

    public Object clone() {
      DVALRecord rec = new DVALRecord();
      rec.field_1_options = field_1_options;
      rec.field_2_horiz_pos = field_2_horiz_pos;
      rec.field_3_vert_pos = field_3_vert_pos;
      rec.field_cbo_id = field_cbo_id;
      rec.field_5_dv_no = field_5_dv_no;
      return rec;
    }
}
