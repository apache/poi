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

import java.util.List;
import java.util.Stack;

import org.apache.poi.hssf.model.FormulaParser;
import org.apache.poi.hssf.record.CFRuleRecord;
import org.apache.poi.hssf.record.formula.Ptg;

/**
 * 
 * High level representation of Conditional Format  
 * 
 * @author Dmitriy Kumshayev
 */

public class HSSFConditionalFormattingRule
{
    public static final byte CELL_COMPARISON = CFRuleRecord.CONDITION_TYPE_CELL_VALUE_IS;
    public static final byte FORMULA = CFRuleRecord.CONDITION_TYPE_FORMULA;

    public static final byte COMPARISON_OPERATOR_NO_COMPARISON = CFRuleRecord.COMPARISON_OPERATOR_NO_COMPARISON;
    public static final byte COMPARISON_OPERATOR_BETWEEN 	   = CFRuleRecord.COMPARISON_OPERATOR_BETWEEN;
    public static final byte COMPARISON_OPERATOR_NOT_BETWEEN   = CFRuleRecord.COMPARISON_OPERATOR_NOT_BETWEEN;
    public static final byte COMPARISON_OPERATOR_EQUAL         = CFRuleRecord.COMPARISON_OPERATOR_EQUAL;
    public static final byte COMPARISON_OPERATOR_NOT_EQUAL     = CFRuleRecord.COMPARISON_OPERATOR_NOT_EQUAL;
    public static final byte COMPARISON_OPERATOR_GT            = CFRuleRecord.COMPARISON_OPERATOR_GT;
    public static final byte COMPARISON_OPERATOR_LT            = CFRuleRecord.COMPARISON_OPERATOR_LT;
    public static final byte COMPARISON_OPERATOR_GE            = CFRuleRecord.COMPARISON_OPERATOR_GE;
    public static final byte COMPARISON_OPERATOR_LE            = CFRuleRecord.COMPARISON_OPERATOR_LE;
    
    

	private CFRuleRecord cfRuleRecord;
	private HSSFWorkbook workbook;
	
	protected HSSFConditionalFormattingRule(HSSFWorkbook workbook)
	{
		this.workbook = workbook;
		this.cfRuleRecord = new CFRuleRecord();
	}

	protected HSSFConditionalFormattingRule(HSSFWorkbook workbook, CFRuleRecord cfRuleRecord)
	{
		this.workbook = workbook;
		this.cfRuleRecord = cfRuleRecord;
	}

	/** 
	 *  Keep Font Formatting unchanged for this Conditional Formatting Rule 
	 */
	public void setFontFormattingUnchanged()
	{
		cfRuleRecord.setFontFormattingUnchanged();
	}
	/** 
	 *  Keep Border Formatting unchanged for this Conditional Formatting Rule 
	 */
	public void setBorderFormattingUnchanged()
	{
		cfRuleRecord.setBorderFormattingUnchanged();
	}
	/** 
	 *  Keep Pattern Formatting unchanged for this Conditional Formatting Rule 
	 */
	public void setPatternFormattingUnchanged()
	{
		cfRuleRecord.setPatternFormattingUnchanged();
	}
	
	public void setFontFormatting(HSSFFontFormatting fontFormatting)
	{
		if( fontFormatting!=null )
		{
			cfRuleRecord.setFontFormatting(fontFormatting.getFontFormattingBlock());
		}
		else
		{
			setFontFormattingUnchanged();
		}
	}
	public void setBorderFormatting(HSSFBorderFormatting borderFormatting)
	{
		if( borderFormatting != null )
		{
			cfRuleRecord.setBorderFormatting(borderFormatting.getBorderFormattingBlock());
		}
		else
		{
			setBorderFormattingUnchanged();
		}
	}
	public void setPatternFormatting(HSSFPatternFormatting patternFormatting)
	{
		if( patternFormatting != null)
		{
			cfRuleRecord.setPatternFormatting(patternFormatting.getPatternFormattingBlock());
		}
		else
		{
			setPatternFormattingUnchanged();
		}
	}
	
	public void setCellComparisonCondition(byte comparisonOperation, String formula1, String formula2)
	{
		cfRuleRecord.setConditionType(CELL_COMPARISON);
		cfRuleRecord.setComparisonOperation(comparisonOperation);
		
		// Formula 1
		setFormula1(formula1);
		
		// Formula 2
		setFormula1(formula2);
	}
	
	public void setFormulaCondition(String formula)
	{
		cfRuleRecord.setConditionType(FORMULA);
		// Formula 1
		setFormula1(formula);
	}
	
	public void setFormula1(String formula)
	{
		// Formula 1
		if( formula != null)
		{
		    Stack parsedExpression = parseFormula(formula);
			if( parsedExpression != null )
			{
				cfRuleRecord.setParsedExpression1(parsedExpression);
			}
			else
			{
				cfRuleRecord.setParsedExpression1(null);
			}
		}
		else
		{
			cfRuleRecord.setParsedExpression1(null);
		}
	}
	
	public void setFormula2(String formula)
	{
		// Formula 2
		if( formula != null)
		{
		    Stack parsedExpression = parseFormula(formula);
			if( parsedExpression != null )
			{
				cfRuleRecord.setParsedExpression2(parsedExpression);
			}
			else
			{
				cfRuleRecord.setParsedExpression2(null);
			}
		}
		else
		{
			cfRuleRecord.setParsedExpression2(null);
		}
	}
	
	public String getFormula1()
	{
        return toFormulaString(cfRuleRecord.getParsedExpression1());
	}

	public String getFormula2()
	{
		byte conditionType = cfRuleRecord.getConditionType();
		switch(conditionType)
		{
			case CELL_COMPARISON:
			{
				byte comparisonOperation = cfRuleRecord.getComparisonOperation();
				switch(comparisonOperation)
				{
					case COMPARISON_OPERATOR_BETWEEN:
					case COMPARISON_OPERATOR_NOT_BETWEEN:
						return toFormulaString(cfRuleRecord.getParsedExpression2());
				}
			}
		}
		return null;
	}

	private String toFormulaString(List parsedExpression)
	{
		String formula = null;
		if(parsedExpression!=null)
		{
	        formula = FormulaParser.toFormulaString(workbook.getWorkbook(),parsedExpression);
		}
		return formula;
	}
	

	private Stack parseFormula(String formula2)
	{
		FormulaParser parser = 
			new FormulaParser(formula2, workbook.getWorkbook());
		parser.parse();

		Stack parsedExpression = convertToTokenStack(parser.getRPNPtg());
	    parsedExpression = convertToTokenStack(parser.getRPNPtg());
		return parsedExpression;
	}

	private static Stack convertToTokenStack(Ptg[] ptgs)
	{
		if( ptgs != null)
		{
			Stack parsedExpression = new Stack();
			// fill the Ptg Stack with Ptgs of new formula
			for (int k = 0; k < ptgs.length; k++) 
			{
			    parsedExpression.push(ptgs[ k ]);
			}
			return parsedExpression;
		}
		else
		{
			return null;
		}
	}
	
	
}
