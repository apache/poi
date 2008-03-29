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
package org.apache.poi.hssf.usermodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.aggregates.CFRecordsAggregate;
import org.apache.poi.hssf.record.cf.CellRange;
import org.apache.poi.hssf.util.Region;

public class HSSFConditionalFormatting
{
	HSSFSheet sheet;
	CFRecordsAggregate cfAggregate;
	
	protected HSSFConditionalFormatting(HSSFSheet sheet)
	{
		this.sheet = sheet;
		this.cfAggregate = new CFRecordsAggregate();
	}
	
	protected HSSFConditionalFormatting(HSSFSheet sheet, CFRecordsAggregate cfAggregate)
	{
		this.sheet = sheet;
		this.cfAggregate = cfAggregate;
	}
	

	public void setFormattingRegions(Region[] regions)
	{
		if( regions != null)
		{
			CFHeaderRecord header = cfAggregate.getHeader();
			header.setCellRanges(mergeCellRanges(toCellRangeList(regions)));
		}
	}

	public Region[] getFormattingRegions()
	{
		CFHeaderRecord cfh = cfAggregate.getHeader();
		
		List cellRanges = cfh.getCellRanges();
		
		if (cellRanges != null)
		{
			return toRegionArray(cellRanges);
		}
		return null;
	}
	
	public void setConditionalFormat(int idx, HSSFConditionalFormattingRule cfRule)
	{
		cfAggregate.getRules().set(idx, cfRule);
	}

	public void addConditionalFormat(HSSFConditionalFormattingRule cfRule)
	{
		cfAggregate.getRules().add(cfRule);
	}
	
	public HSSFConditionalFormattingRule getConditionalFormat(int idx)
	{
		CFRuleRecord ruleRecord = (CFRuleRecord)cfAggregate.getRules().get(idx);
		return new HSSFConditionalFormattingRule(sheet.workbook, ruleRecord);
	}
	
	/**
	 * Do all possible cell merges between cells of the list so that:<br>
	 * 	<li>if a cell range is completely inside of another cell range, it gets removed from the list 
	 * 	<li>if two cells have a shared border, merge them into one bigger cell range
	 * @param cellRangeList
	 * @return updated List of cell ranges
	 */
	private static List mergeCellRanges(List cellRangeList)
	{
		boolean merged = false;
		
		do
		{
			merged = false;
			
			if( cellRangeList.size()>1 )
			{
				for( int i=0; i<cellRangeList.size(); i++)
				{
					CellRange range1 = (CellRange)cellRangeList.get(i);
					for( int j=i+1; j<cellRangeList.size(); j++)
					{
						CellRange range2 = (CellRange)cellRangeList.get(j);
						
						switch(range1.intersect(range2))
						{
							case CellRange.NO_INTERSECTION: 
							{
								if( range1.hasSharedBorder(range2))
								{
									cellRangeList.set(i, range1.createEnclosingCellRange(range2));
									cellRangeList.remove(j--);
									merged = true;
								}
								else
								{
									// No intersection and no shared border: do nothing 
								}
								break;
							}
							case CellRange.OVERLAP:
							{
								// TODO split and re-merge the intersected area
								break;
							}
							case CellRange.INSIDE:
							{
								// Remove range2, since it is completely inside of range1
								cellRangeList.remove(j--);
								merged = true;
								break;
							}
							case CellRange.ENCLOSES:
							{
								// range2 encloses range1, so replace it with the enclosing one
								cellRangeList.set(i, range2);
								cellRangeList.remove(j--);
								merged = true;
								break;
							}
						}
					}
				}
			}
		}
		while( merged );
		
		return cellRangeList;
	}
	
	/**
	 * Convert a List of CellRange objects to an array of regions 
	 *  
	 * @param List of CellRange objects
	 * @return regions
	 */
	private static Region[] toRegionArray(List cellRanges)
	{
		int size = cellRanges.size();
		Region[] regions = new Region[size];
		
		for (int i = 0; i != size; i++)
		{
			CellRange cr = (CellRange) cellRanges.get(i);
			regions[i] = new Region(cr.getFirstRow(), cr.getFirstColumn(), 
					cr.getLastRow(), cr.getLastColumn());
		}
		return regions;
	}
	
	/**
	 * Convert array of regions to a List of CellRange objects
	 *  
	 * @param regions
	 * @return List of CellRange objects
	 */
	private static List toCellRangeList(Region[] regions)
	{
		List cellRangeList = new ArrayList();
		for( int i=0; i<regions.length; i++)
		{
			Region r = regions[i];
			CellRange cr = new CellRange(r.getRowFrom(), r.getRowTo(), r.getColumnFrom(), r
					.getColumnTo());
			cellRangeList.add(cr);
		}
		return cellRangeList;
	}

	public String toString()
	{
		if(cfAggregate!=null)
		{
			return cfAggregate.toString();
		}
		return null;
	}
}
