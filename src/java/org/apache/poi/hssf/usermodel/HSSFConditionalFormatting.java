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

import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.CFHeaderRecord;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.aggregates.CFRecordsAggregate;
import org.apache.poi.hssf.record.cf.CellRange;
import org.apache.poi.hssf.util.Region;

/**
 * HSSFConditionalFormatting class encapsulates all settings of Conditional Formatting. 
 * 
 * The class can be used 
 * 
 * <UL>
 * <LI>
 * to make a copy HSSFConditionalFormatting settings.
 * </LI>
 *  
 * 
 * For example:
 * <PRE>
 * HSSFConditionalFormatting cf = sheet.getConditionalFormattingAt(index);
 * newSheet.addConditionalFormatting(cf);
 * </PRE>
 * 
 *  <LI>
 *  or to modify existing Conditional Formatting settings (formatting regions and/or rules).
 *  </LI>
 *  </UL>
 * 
 * Use {@link HSSFSheet#getConditionalFormattingAt(int)} to get access to an instance of this class. 
 * <P>
 * To create a new Conditional Formatting set use the following approach:
 * 
 * <PRE>
 * // Create pattern with red background
 * HSSFPatternFormatting patternFormatting = new HSSFPatternFormatting();
 * patternFormatting.setFillBackgroundColor(HSSFColor.RED.index);
 * 
 * Region [] regions =
 * {
 *     // Define a region containing first column
 *     new Region(1,(short)1,-1,(short)1)
 * };
 *     
 * HSSFConditionalFormattingRule[] rules = 
 * {
 *     // Define a Conditional Formatting rule, which triggers formatting
 *     // when cell's value is greater or equal than 100.0 and
 *     // applies patternFormatting defined above.
 *         
 *     sheet.createConditionalFormattingRule(
 *             HSSFConditionalFormattingRule.COMPARISON_OPERATOR_GE, 
 *             "100.0", // 1st formula 
 *             null,    // 2nd formula is not used for comparison operator GE
 *             null,    // do not override Font Formatting
 *             null,    // do not override Border Formatting
 *             patternFormatting
 *     )
 * };
 *     
 * // Apply Conditional Formatting rules defined above to the regions  
 * sheet.addConditionalFormatting(regions, rules);
 * </PRE>
 * 
 * @author Dmitriy Kumshayev
 */
public final class HSSFConditionalFormatting
{
	private final Workbook workbook;
	private final CFRecordsAggregate cfAggregate;

	HSSFConditionalFormatting(HSSFSheet sheet, CFRecordsAggregate cfAggregate)
	{
		if(sheet == null) {
			throw new IllegalArgumentException("sheet must not be null");
		}
		if(cfAggregate == null) {
			throw new IllegalArgumentException("cfAggregate must not be null");
		}
		workbook = sheet.workbook.getWorkbook();
		this.cfAggregate = cfAggregate;
	}
	CFRecordsAggregate getCFRecordsAggregate() {
		return cfAggregate;
	}

	/**
	 * @return array of <tt>Region</tt>s. never <code>null</code> 
	 */
	public Region[] getFormattingRegions()
	{
		CFHeaderRecord cfh = cfAggregate.getHeader();
		CellRange[] cellRanges = cfh.getCellRanges();
		return CellRange.convertCellRangesToRegions(cellRanges);
	}

	/**
	 * Replaces an existing Conditional Formatting rule at position idx. 
	 * Excel allows to create up to 3 Conditional Formatting rules.
	 * This method can be useful to modify existing  Conditional Formatting rules.
	 * 
	 * @param idx position of the rule. Should be between 0 and 2.
	 * @param cfRule - Conditional Formatting rule
	 */
	public void setRule(int idx, HSSFConditionalFormattingRule cfRule)
	{
		cfAggregate.setRule(idx, cfRule.getCfRuleRecord());
	}

	/**
	 * add a Conditional Formatting rule. 
	 * Excel allows to create up to 3 Conditional Formatting rules.
	 * @param cfRule - Conditional Formatting rule
	 */
	public void addRule(HSSFConditionalFormattingRule cfRule)
	{
		cfAggregate.addRule(cfRule.getCfRuleRecord());
	}

	/**
	 * @return the Conditional Formatting rule at position idx.
	 */
	public HSSFConditionalFormattingRule getRule(int idx)
	{
		CFRuleRecord ruleRecord = cfAggregate.getRule(idx);
		return new HSSFConditionalFormattingRule(workbook, ruleRecord);
	}

	/**
	 * @return number of Conditional Formatting rules.
	 */
	public int getNumberOfRules()
	{
		return cfAggregate.getNumberOfRules();
	}

	public String toString()
	{
		return cfAggregate.toString();
	}
}
