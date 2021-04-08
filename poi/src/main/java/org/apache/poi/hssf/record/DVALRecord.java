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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianOutput;

/**
 * This record is the list header of all data validation records (0x01BE) in the current sheet.
 */
public final class DVALRecord extends StandardRecord {
	public static final short sid = 0x01B2;

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

	public DVALRecord(DVALRecord other) {
		super(other);
		field_1_options = other.field_1_options;
		field_2_horiz_pos = other.field_2_horiz_pos;
		field_3_vert_pos = other.field_3_vert_pos;
		field_cbo_id = other.field_cbo_id;
		field_5_dv_no = other.field_5_dv_no;
	}

	public DVALRecord(RecordInputStream in) {
		field_1_options = in.readShort();
		field_2_horiz_pos = in.readInt();
		field_3_vert_pos = in.readInt();
        field_cbo_id = in.readInt();
        field_5_dv_no = in.readInt();
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
     * @return the Object ID of the drop down arrow object for list boxes
     */
    public int getObjectID() {
        return field_cbo_id;
    }

    /**
     * @return the number of following DV records
     */
    public int getDVRecNo() {
        return field_5_dv_no;
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

	@Override
    public DVALRecord copy() {
      return new DVALRecord(this);
    }

	@Override
	public HSSFRecordTypes getGenericRecordType() {
		return HSSFRecordTypes.DVAL;
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"options", this::getOptions,
			"horizPos", this::getHorizontalPos,
			"vertPos", this::getVerticalPos,
			"comboObjectID", this::getObjectID,
			"dvRecordsNumber", this::getDVRecNo
		);
	}
}
