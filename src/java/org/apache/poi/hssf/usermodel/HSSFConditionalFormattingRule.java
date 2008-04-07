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

import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
import org.apache.poi.hssf.record.cf.BorderFormatting;
import org.apache.poi.hssf.record.cf.FontFormatting;
import org.apache.poi.hssf.record.cf.PatternFormatting;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * 
 * High level representation of Conditional Formatting Rule.
 * It allows to specify formula based conditions for the Conditional Formatting
 * and the formatting settings such as font, border and pattern.
 * 
 * @author Dmitriy Kumshayev
 */

public final class HSSFConditionalFormattingRule
{
    private static final byte CELL_COMPARISON = CFRuleRecord.CONDITION_TYPE_CELL_VALUE_IS;

	private final CFRuleRecord cfRuleRecord;
	private final Workbook workbook;

	HSSFConditionalFormattingRule(Workbook pWorkbook, CFRuleRecord pRuleRecord) {
		workbook = pWorkbook;
		cfRuleRecord = pRuleRecord;
	}
	HSSFConditionalFormattingRule(Workbook pWorkbook, CFRuleRecord pRuleRecord, 
			HSSFFontFormatting fontFmt, HSSFBorderFormatting bordFmt, HSSFPatternFormatting patternFmt) {
		this(pWorkbook, pRuleRecord);
		setFontFormatting(fontFmt);
		setBorderFormatting(bordFmt);
		setPatternFormatting(patternFmt);
	}

	CFRuleRecord getCfRuleRecord()
	{
		return cfRuleRecord;
	}
	
	
	/**
	 * @param fontFmt pass <code>null</code> to signify 'font unchanged'
	 */
	public void setFontFormatting(HSSFFontFormatting fontFmt)
	{
		FontFormatting block = fontFmt==null ? null : fontFmt.getFontFormattingBlock();
		cfRuleRecord.setFontFormatting(block);
	}
	
	/**
	 * @return - font formatting object  if defined,  <code>null</code> otherwise
	 */
	public HSSFFontFormatting getFontFormatting()
	{
		FontFormatting ff = cfRuleRecord.getFontFormatting();
		if ( ff == null ) {
			return null;
		}
		return new HSSFFontFormatting(ff);
	}
	
	/**
	 * @param borderFmt pass <code>null</code> to signify 'border unchanged'
	 */
	public void setBorderFormatting(HSSFBorderFormatting borderFmt)
	{
		BorderFormatting block = borderFmt==null ? null : borderFmt.getBorderFormattingBlock();
		cfRuleRecord.setBorderFormatting(block);
	}
	/**
	 * @return - border formatting object  if defined,  <code>null</code> otherwise
	 */
	public HSSFBorderFormatting getBorderFormatting()
	{
		BorderFormatting bf = cfRuleRecord.getBorderFormatting();
		if ( bf == null ) {
			return null;
		}
		return new HSSFBorderFormatting(bf);
	}
	/**
	 * @param patternFmt pass <code>null</code> to signify 'pattern unchanged'
	 */
	public void setPatternFormatting(HSSFPatternFormatting patternFmt)
	{
		PatternFormatting block = patternFmt==null ? null : patternFmt.getPatternFormattingBlock();
		cfRuleRecord.setPatternFormatting(block);
	}
	/**
	 * @return - pattern formatting object  if defined, <code>null</code> otherwise
	 */
	public HSSFPatternFormatting getPatternFormatting()
	{
		PatternFormatting pf = cfRuleRecord.getPatternFormatting();
		if ( pf == null ) {
			return null;
		}
		return new HSSFPatternFormatting(pf);
	}
	
	public String getFormula1()
	{
		return toFormulaString(cfRuleRecord.getParsedExpression1());
	}

	public String getFormula2()
	{
		byte conditionType = cfRuleRecord.getConditionType();
		if (conditionType == CELL_COMPARISON) {
			byte comparisonOperation = cfRuleRecord.getComparisonOperation();
			switch(comparisonOperation)
			{
				case ComparisonOperator.BETWEEN:
				case ComparisonOperator.NOT_BETWEEN:
					return toFormulaString(cfRuleRecord.getParsedExpression2());
			}
		}
		return null;
	}

	private String toFormulaString(Ptg[] parsedExpression)
	{
		if(parsedExpression ==null) {
			return null;
		}
		return FormulaParser.toFormulaString(workbook, parsedExpression);
	}
}
