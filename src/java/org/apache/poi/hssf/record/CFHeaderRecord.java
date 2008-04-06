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

import org.apache.poi.hssf.record.cf.CellRange;
import org.apache.poi.ss.util.Region;
import org.apache.poi.util.LittleEndian;

/**
 * Conditional Formatting Header record (CFHEADER)
 * 
 * @author Dmitriy Kumshayev
 */
public final class CFHeaderRecord extends Record
{
	public static final short sid = 0x1B0;

	private static final CellRange[] EMPTY_CELL_RANGE_ARRAY = { };

	private int field_1_numcf;
	private int field_2_need_recalculation;
	private CellRange field_3_enclosing_cell_range;
	private CellRange[] field_4_cell_ranges;

	/** Creates new CFHeaderRecord */
	public CFHeaderRecord()
	{
		field_4_cell_ranges = EMPTY_CELL_RANGE_ARRAY;
	}
	public CFHeaderRecord(Region[] regions)
	{
		CellRange[] unmergedRanges = CellRange.convertRegionsToCellRanges(regions);
		CellRange[] mergeCellRanges = CellRange.mergeCellRanges(unmergedRanges);
		setCellRanges(mergeCellRanges);
	}

	public CFHeaderRecord(RecordInputStream in)
	{
		super(in);
	}

	protected void fillFields(RecordInputStream in)
	{
		field_1_numcf = in.readShort();
		field_2_need_recalculation = in.readShort();
		field_3_enclosing_cell_range = new CellRange(in.readUShort(), in.readUShort(), in.readUShort(), in.readUShort());
		int numCellRanges = in.readShort();
		CellRange[] crs = new CellRange[numCellRanges];
		for( int i=0; i<numCellRanges; i++)
		{
			crs[i] = new CellRange(in.readUShort(),in.readUShort(),in.readUShort(),in.readUShort());
		}
		field_4_cell_ranges = crs;
	}
	
	public int getNumberOfConditionalFormats()
	{
		return field_1_numcf;
	}
	public void setNumberOfConditionalFormats(int n)
	{
		field_1_numcf=n;
	}
	
	public boolean getNeedRecalculation()
	{
		return field_2_need_recalculation==1?true:false;
	}

	public void setNeedRecalculation(boolean b)
	{
		field_2_need_recalculation=b?1:0;
	}
	
	public CellRange getEnclosingCellRange()
	{
		return field_3_enclosing_cell_range;
	}

	public void setEnclosingCellRange( CellRange cr)
	{
		field_3_enclosing_cell_range = cr.cloneCellRange();
	}

	/**
	 * Set cell ranges list to a single cell range and 
	 * modify the enclosing cell range accordingly.
	 * @param List cellRanges - list of CellRange objects
	 */
	public void setCellRanges(CellRange[] cellRanges)
	{
		if(cellRanges == null)
		{
			throw new IllegalArgumentException("cellRanges must not be null");
		}
		field_4_cell_ranges = (CellRange[]) cellRanges.clone();
		CellRange enclosingRange = null;
		for (int i = 0; i < cellRanges.length; i++)
		{
			enclosingRange = cellRanges[i].createEnclosingCellRange(enclosingRange);
		}
		field_3_enclosing_cell_range=enclosingRange;
	}
	
	public CellRange[] getCellRanges()
	{
		return (CellRange[]) field_4_cell_ranges.clone();
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("[CFHEADER]\n");
		buffer.append("	.id		= ").append(Integer.toHexString(sid)).append("\n");
		buffer.append("	.numCF			= ").append(getNumberOfConditionalFormats()).append("\n");
		buffer.append("	.needRecalc	   = ").append(getNeedRecalculation()).append("\n");
		buffer.append("	.enclosingCellRange= ").append(getEnclosingCellRange()).append("\n");
		if( field_4_cell_ranges.length>0)
		{
			buffer.append("	.cfranges=[");
			for( int i=0; i<field_4_cell_ranges.length; i++)
			{
				buffer.append(i==0?"":",").append(field_4_cell_ranges[i].toString());
			}
			buffer.append("]\n");
		}
		buffer.append("[/CFHEADER]\n");
		return buffer.toString();
	}

	/**
	 * @return byte array containing instance data
	 */

	public int serialize(int offset, byte[] data)
	{
		int recordsize = getRecordSize();
		
		LittleEndian.putShort(data, 0 + offset, sid);
		LittleEndian.putShort(data, 2 + offset, (short) (recordsize-4));
		LittleEndian.putShort(data, 4 + offset, (short) field_1_numcf);
		LittleEndian.putShort(data, 6 + offset, (short) field_2_need_recalculation);
		LittleEndian.putShort(data, 8 + offset, (short) field_3_enclosing_cell_range.getFirstRow());
		LittleEndian.putShort(data, 10 + offset, (short) field_3_enclosing_cell_range.getLastRow());
		LittleEndian.putShort(data, 12 + offset, (short) field_3_enclosing_cell_range.getFirstColumn());
		LittleEndian.putShort(data, 14 + offset, (short) field_3_enclosing_cell_range.getLastColumn());
		LittleEndian.putShort(data, 16 + offset, (short) field_4_cell_ranges.length);
		for( int i=0 ; i!=field_4_cell_ranges.length; i++)
		{
			CellRange cr = field_4_cell_ranges[i];
			LittleEndian.putShort(data, 18 + 0 + 8 * i + offset, (short) cr.getFirstRow());
			LittleEndian.putShort(data, 18 + 2 + 8 * i + offset, (short) cr.getLastRow());
			LittleEndian.putShort(data, 18 + 4 + 8 * i + offset, (short) cr.getFirstColumn());
			LittleEndian.putShort(data, 18 + 6 + 8 * i + offset, (short) cr.getLastColumn());
		}
		return getRecordSize();
	}

	public int getRecordSize()
	{
		return 18+8*field_4_cell_ranges.length;
	}

	/**
	 * called by constructor, should throw runtime exception in the event of a
	 * record passed with a differing ID.
	 *
	 * @param id alleged id for this record
	 */

	protected void validateSid(short id)
	{
		if (id != sid)
		{
			throw new RecordFormatException(
					"NOT A ConditionalFormattingHeaderRecord RECORD");
		}
	}

	public short getSid()
	{
		return sid;
	}

	public Object clone() 
	{
		CFHeaderRecord result = new CFHeaderRecord();
		result.field_1_numcf = field_1_numcf;
		result.field_2_need_recalculation = field_2_need_recalculation;
		result.field_3_enclosing_cell_range = field_3_enclosing_cell_range;
		CellRange[] crs = new CellRange[field_4_cell_ranges.length];
		for (int i = 0; i < crs.length; i++) {
			crs[i] = field_4_cell_ranges[i].cloneCellRange();
		}
		result.field_4_cell_ranges = crs;
		return result;
	}
}
