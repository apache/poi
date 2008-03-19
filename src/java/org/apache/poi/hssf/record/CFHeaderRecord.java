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

/*
 * ConditionalFormattingHeaderRecord.java
 *
 * Created on January 17, 2008, 3:05 AM
 */
package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.record.cf.CellRange;
import org.apache.poi.util.LittleEndian;

/**
 * Conditional Formatting Header record (CFHEADER)
 * 
 * @author Dmitriy Kumshayev
 */
public class CFHeaderRecord extends Record
{
	public static final short sid = 0x1B0;

	private int field_1_numcf;
	private int field_2_need_recalculation;
	private CellRange field_3_enclosing_cell_range;
	private List field_4_cell_ranges;

	/** Creates new CFHeaderRecord */
	public CFHeaderRecord()
	{
		field_4_cell_ranges = new ArrayList(5);
	}

	public CFHeaderRecord(RecordInputStream in)
	{
		super(in);
	}

	protected void fillFields(RecordInputStream in)
	{
		field_1_numcf = in.readShort();
		field_2_need_recalculation = in.readShort();
		field_3_enclosing_cell_range = new CellRange(in.readShort(),in.readShort(),in.readShort(),in.readShort());
		int numCellRanges = in.readShort();
		field_4_cell_ranges = new ArrayList(5);
		for( int i=0; i<numCellRanges; i++)
		{
			field_4_cell_ranges.add(new CellRange(in.readShort(),in.readShort(),in.readShort(),in.readShort()));
		}
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
	public void setCellRanges( List cellRanges )
	{
		field_4_cell_ranges.clear();
		if(cellRanges!=null)
		{
			field_3_enclosing_cell_range=null;
			for( int i=0; i<cellRanges.size(); i++)
			{
				field_4_cell_ranges.add(cellRanges.get(i));
				recalculateEnclosingRange((CellRange)cellRanges.get(i));
			}
		}
	}

	private void recalculateEnclosingRange(CellRange cellRange)
	{
		field_3_enclosing_cell_range = cellRange.createEnclosingCellRange(field_3_enclosing_cell_range);
	}
	
	public List getCellRanges()
	{
		return field_4_cell_ranges;
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();

		buffer.append("[CFHEADER]\n");
        buffer.append("    .id        = ").append(Integer.toHexString(sid)).append("\n");
		buffer.append("    .numCF            = ").append(getNumberOfConditionalFormats()).append("\n");
		buffer.append("    .needRecalc       = ").append(getNeedRecalculation()).append("\n");
		buffer.append("    .enclosingCellRange= ").append(getEnclosingCellRange()).append("\n");
		if( field_4_cell_ranges.size()>0)
		{
			buffer.append("    .cfranges=[");
			for( int i=0; i<field_4_cell_ranges.size(); i++)
			{
				buffer.append(i==0?"":",").append(field_4_cell_ranges.get(i));
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
		LittleEndian.putShort(data, 16 + offset, (short) field_4_cell_ranges.size());
		for( int i=0 ; i!=field_4_cell_ranges.size(); i++)
		{
			LittleEndian.putShort(data, 18 + 0 + 8 * i + offset,
					(short) ((CellRange) field_4_cell_ranges.get(i)).getFirstRow());
			LittleEndian.putShort(data, 18 + 2 + 8 * i + offset,
					(short) ((CellRange) field_4_cell_ranges.get(i)).getLastRow());
			LittleEndian.putShort(data, 18 + 4 + 8 * i + offset,
					(short) ((CellRange) field_4_cell_ranges.get(i)).getFirstColumn());
			LittleEndian.putShort(data, 18 + 6 + 8 * i + offset,
					(short) ((CellRange) field_4_cell_ranges.get(i)).getLastColumn());
		}
		return getRecordSize();
	}

	public int getRecordSize()
	{
		return 18+8*field_4_cell_ranges.size();
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
    	CFHeaderRecord rec = new CFHeaderRecord();
    	rec.field_1_numcf = field_1_numcf;
    	rec.field_2_need_recalculation = field_2_need_recalculation;
    	rec.field_3_enclosing_cell_range = field_3_enclosing_cell_range;
        rec.field_4_cell_ranges = new ArrayList(field_4_cell_ranges.size());
        Iterator iterator = field_4_cell_ranges.iterator();
        while (iterator.hasNext()) 
        {
           CellRange oldRange = (CellRange)iterator.next();
           rec.field_4_cell_ranges.add(oldRange.cloneCellRange());
        }
        return rec;
    }

}
