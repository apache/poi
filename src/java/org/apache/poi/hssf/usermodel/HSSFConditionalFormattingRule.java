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

import org.apache.poi.hssf.model.HSSFFormulaParser;
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
	private final HSSFWorkbook workbook;

	HSSFConditionalFormattingRule(HSSFWorkbook pWorkbook, CFRuleRecord pRuleRecord) {
		if (pWorkbook == null) {
			throw new IllegalArgumentException("pWorkbook must not be null");
		}
		if (pRuleRecord == null) {
			throw new IllegalArgumentException("pRuleRecord must not be null");
		}
		workbook = pWorkbook;
		cfRuleRecord = pRuleRecord;
	}

	CFRuleRecord getCfRuleRecord()
	{
		return cfRuleRecord;
	}

	private HSSFFontFormatting getFontFormatting(boolean create)
	{
		FontFormatting fontFormatting = cfRuleRecord.getFontFormatting();
		if ( fontFormatting != null)
		{
			cfRuleRecord.setFontFormatting(fontFormatting);
			return new HSSFFontFormatting(cfRuleRecord);
		}
		else if( create )
		{
			fontFormatting = new FontFormatting();
			cfRuleRecord.setFontFormatting(fontFormatting);
			return new HSSFFontFormatting(cfRuleRecord);
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return - font formatting object  if defined,  <code>null</code> otherwise
	 */
	public HSSFFontFormatting getFontFormatting()
	{
		return getFontFormatting(false);
	}
	/**
	 * create a new font formatting structure if it does not exist,
	 * otherwise just return existing object.
	 * @return - font formatting object, never returns <code>null</code>.
	 */
	public HSSFFontFormatting createFontFormatting()
	{
		return getFontFormatting(true);
	}

	private HSSFBorderFormatting getBorderFormatting(boolean create)
	{
		BorderFormatting borderFormatting = cfRuleRecord.getBorderFormatting();
		if ( borderFormatting != null)
		{
			cfRuleRecord.setBorderFormatting(borderFormatting);
			return new HSSFBorderFormatting(cfRuleRecord);
		}
		else if( create )
		{
			borderFormatting = new BorderFormatting();
			cfRuleRecord.setBorderFormatting(borderFormatting);
			return new HSSFBorderFormatting(cfRuleRecord);
		}
		else
		{
			return null;
		}
	}
	/**
	 * @return - border formatting object  if defined,  <code>null</code> otherwise
	 */
	public HSSFBorderFormatting getBorderFormatting()
	{
		return getBorderFormatting(false);
	}
	/**
	 * create a new border formatting structure if it does not exist,
	 * otherwise just return existing object.
	 * @return - border formatting object, never returns <code>null</code>.
	 */
	public HSSFBorderFormatting createBorderFormatting()
	{
		return getBorderFormatting(true);
	}

	private HSSFPatternFormatting getPatternFormatting(boolean create)
	{
		PatternFormatting patternFormatting = cfRuleRecord.getPatternFormatting();
		if ( patternFormatting != null)
		{
			cfRuleRecord.setPatternFormatting(patternFormatting);
			return new HSSFPatternFormatting(cfRuleRecord);
		}
		else if( create )
		{
			patternFormatting = new PatternFormatting();
			cfRuleRecord.setPatternFormatting(patternFormatting);
			return new HSSFPatternFormatting(cfRuleRecord);
		}
		else
		{
			return null;
		}
	}

	/**
	 * @return - pattern formatting object  if defined, <code>null</code> otherwise
	 */
	public HSSFPatternFormatting getPatternFormatting()
	{
		return getPatternFormatting(false);
	}
	/**
	 * create a new pattern formatting structure if it does not exist,
	 * otherwise just return existing object.
	 * @return - pattern formatting object, never returns <code>null</code>.
	 */
	public HSSFPatternFormatting createPatternFormatting()
	{
		return getPatternFormatting(true);
	}

	/**
	 * @return -  the conditiontype for the cfrule
	 */
	public byte getConditionType() {
		return cfRuleRecord.getConditionType();
	}

	/**
	 * @return - the comparisionoperatation for the cfrule
	 */
	public byte getComparisonOperation() {
		return cfRuleRecord.getComparisonOperation();
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
		return HSSFFormulaParser.toFormulaString(workbook, parsedExpression);
	}
}
