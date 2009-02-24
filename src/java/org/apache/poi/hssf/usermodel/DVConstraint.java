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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.formula.NumberPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.record.formula.StringPtg;
import org.apache.poi.ss.formula.FormulaType;

/**
 * 
 * @author Josh Micich
 */
public class DVConstraint {
	/**
	 * ValidationType enum
	 */
	public static final class ValidationType {
		private ValidationType() {
			// no instances of this class
		}
		/** 'Any value' type - value not restricted */
		public static final int ANY         = 0x00;
		/** Integer ('Whole number') type */
		public static final int INTEGER     = 0x01;
		/** Decimal type */
		public static final int DECIMAL     = 0x02;
		/** List type ( combo box type ) */
		public static final int LIST        = 0x03;
		/** Date type */
		public static final int DATE        = 0x04;
		/** Time type */
		public static final int TIME        = 0x05;
		/** String length type */
		public static final int TEXT_LENGTH = 0x06;
		/** Formula ( 'Custom' ) type */
		public static final int FORMULA     = 0x07;
	}
	/**
	 * Condition operator enum
	 */
	public static final class OperatorType {
		private OperatorType() {
			// no instances of this class
		}

		public static final int BETWEEN = 0x00;
		public static final int NOT_BETWEEN = 0x01;
		public static final int EQUAL = 0x02;
		public static final int NOT_EQUAL = 0x03;
		public static final int GREATER_THAN = 0x04;
		public static final int LESS_THAN = 0x05;
		public static final int GREATER_OR_EQUAL = 0x06;
		public static final int LESS_OR_EQUAL = 0x07;
		/** default value to supply when the operator type is not used */
		public static final int IGNORED = BETWEEN;
		
		/* package */ static void validateSecondArg(int comparisonOperator, String paramValue) {
			switch (comparisonOperator) {
				case BETWEEN:
				case NOT_BETWEEN:
					if (paramValue == null) {
						throw new IllegalArgumentException("expr2 must be supplied for 'between' comparisons");
					}
				// all other operators don't need second arg
			}
		}
	}
	
	/* package */ static final class FormulaPair {

		private final Ptg[] _formula1;
		private final Ptg[] _formula2;

		public FormulaPair(Ptg[] formula1, Ptg[] formula2) {
			_formula1 = formula1;
			_formula2 = formula2;
		}
		public Ptg[] getFormula1() {
			return _formula1;
		}
		public Ptg[] getFormula2() {
			return _formula2;
		}
		
	}
	
	// convenient access to ValidationType namespace
	private static final ValidationType VT = null;

	
	private final int _validationType;
	private int _operator;
	private String[] _explicitListValues;
	
	private String _formula1;
	private String _formula2;
	private Double _value1;
	private Double _value2;

	
	private DVConstraint(int validationType, int comparisonOperator, String formulaA,
			String formulaB, Double value1, Double value2, String[] excplicitListValues) {
		_validationType = validationType;
		_operator = comparisonOperator;
		_formula1 = formulaA;
		_formula2 = formulaB;
		_value1 = value1;
		_value2 = value2;
		_explicitListValues = excplicitListValues;
	}
	
	
	/**
	 * Creates a list constraint
	 */
	private DVConstraint(String listFormula, String[] excplicitListValues) {
		this(ValidationType.LIST, OperatorType.IGNORED,
			listFormula, null, null, null, excplicitListValues);
	}

	/**
	 * Creates a number based data validation constraint. The text values entered for expr1 and expr2
	 * can be either standard Excel formulas or formatted number values. If the expression starts 
	 * with '=' it is parsed as a formula, otherwise it is parsed as a formatted number. 
	 * 
	 * @param validationType one of {@link ValidationType#ANY}, {@link ValidationType#DECIMAL},
	 * {@link ValidationType#INTEGER}, {@link ValidationType#TEXT_LENGTH}
	 * @param comparisonOperator any constant from {@link OperatorType} enum
	 * @param expr1 date formula (when first char is '=') or formatted number value
	 * @param expr2 date formula (when first char is '=') or formatted number value
	 */
	public static DVConstraint createNumericConstraint(int validationType, int comparisonOperator, 
			String expr1, String expr2) {
		switch (validationType) {
			case ValidationType.ANY:
				if (expr1 != null || expr2 != null) {
					throw new IllegalArgumentException("expr1 and expr2 must be null for validation type 'any'");
				}
				break;
			case ValidationType.DECIMAL:
			case ValidationType.INTEGER:
			case ValidationType.TEXT_LENGTH:
				if (expr1 == null) {
					throw new IllegalArgumentException("expr1 must be supplied");
				}
				OperatorType.validateSecondArg(comparisonOperator, expr2);
				break;
			default:
				throw new IllegalArgumentException("Validation Type ("
						+ validationType + ") not supported with this method");
		}
		// formula1 and value1 are mutually exclusive
		String formula1 = getFormulaFromTextExpression(expr1);
		Double value1 = formula1 == null ? convertNumber(expr1) : null;
		// formula2 and value2 are mutually exclusive
		String formula2 = getFormulaFromTextExpression(expr2);
		Double value2 = formula2 == null ? convertNumber(expr2) : null;
		return new DVConstraint(validationType, comparisonOperator, formula1, formula2, value1, value2, null);
	}

	public static DVConstraint createFormulaListConstraint(String listFormula) {
		return new DVConstraint(listFormula, null);
	}
	public static DVConstraint createExplicitListConstraint(String[] explicitListValues) {
		return new DVConstraint(null, explicitListValues);
	}
	
	
	/**
	 * Creates a time based data validation constraint. The text values entered for expr1 and expr2
	 * can be either standard Excel formulas or formatted time values. If the expression starts 
	 * with '=' it is parsed as a formula, otherwise it is parsed as a formatted time.  To parse 
	 * formatted times, two formats are supported:  "HH:MM" or "HH:MM:SS".  This is contrary to 
	 * Excel which uses the default time format from the OS.
	 * 
	 * @param comparisonOperator constant from {@link OperatorType} enum
	 * @param expr1 date formula (when first char is '=') or formatted time value
	 * @param expr2 date formula (when first char is '=') or formatted time value
	 */
	public static DVConstraint createTimeConstraint(int comparisonOperator, String expr1, String expr2) {
		if (expr1 == null) {
			throw new IllegalArgumentException("expr1 must be supplied");
		}
		OperatorType.validateSecondArg(comparisonOperator, expr1);
		
		// formula1 and value1 are mutually exclusive
		String formula1 = getFormulaFromTextExpression(expr1);
		Double value1 = formula1 == null ? convertTime(expr1) : null;
		// formula2 and value2 are mutually exclusive
		String formula2 = getFormulaFromTextExpression(expr2);
		Double value2 = formula2 == null ? convertTime(expr2) : null;
		return new DVConstraint(VT.TIME, comparisonOperator, formula1, formula2, value1, value2, null);
		
	}
	/**
	 * Creates a date based data validation constraint. The text values entered for expr1 and expr2
	 * can be either standard Excel formulas or formatted date values. If the expression starts 
	 * with '=' it is parsed as a formula, otherwise it is parsed as a formatted date (Excel uses 
	 * the same convention).  To parse formatted dates, a date format needs to be specified.  This
	 * is contrary to Excel which uses the default short date format from the OS.
	 * 
	 * @param comparisonOperator constant from {@link OperatorType} enum
	 * @param expr1 date formula (when first char is '=') or formatted date value
	 * @param expr2 date formula (when first char is '=') or formatted date value
	 * @param dateFormat ignored if both expr1 and expr2 are formulas.  Default value is "YYYY/MM/DD"
	 * otherwise any other valid argument for <tt>SimpleDateFormat</tt> can be used
	 * @see <a href='http://java.sun.com/j2se/1.5.0/docs/api/java/text/DateFormat.html'>SimpleDateFormat</a>
	 */
	public static DVConstraint createDateConstraint(int comparisonOperator, String expr1, String expr2, String dateFormat) {
		if (expr1 == null) {
			throw new IllegalArgumentException("expr1 must be supplied");
		}
		OperatorType.validateSecondArg(comparisonOperator, expr2);
		SimpleDateFormat df = dateFormat == null ? null : new SimpleDateFormat(dateFormat);
		
		// formula1 and value1 are mutually exclusive
		String formula1 = getFormulaFromTextExpression(expr1);
		Double value1 = formula1 == null ? convertDate(expr1, df) : null;
		// formula2 and value2 are mutually exclusive
		String formula2 = getFormulaFromTextExpression(expr2);
		Double value2 = formula2 == null ? convertDate(expr2, df) : null;
		return new DVConstraint(VT.DATE, comparisonOperator, formula1, formula2, value1, value2, null);
	}
	
	/**
	 * Distinguishes formula expressions from simple value expressions.  This logic is only 
	 * required by a few factory methods in this class that create data validation constraints
	 * from more or less the same parameters that would have been entered in the Excel UI.  The
	 * data validation dialog box uses the convention that formulas begin with '='.  Other methods
	 * in this class follow the POI convention (formulas and values are distinct), so the '=' 
	 * convention is not used there.
	 *  
	 * @param textExpr a formula or value expression
	 * @return all text after '=' if textExpr begins with '='. Otherwise <code>null</code> if textExpr does not begin with '='
	 */
	private static String getFormulaFromTextExpression(String textExpr) {
		if (textExpr == null) {
			return null;
		}
		if (textExpr.length() < 1) {
			throw new IllegalArgumentException("Empty string is not a valid formula/value expression");
		}
		if (textExpr.charAt(0) == '=') {
			return textExpr.substring(1);
		}
		return null;
	}


	/**
	 * @return <code>null</code> if numberStr is <code>null</code>
	 */
	private static Double convertNumber(String numberStr) {
		if (numberStr == null) {
			return null;
		}
		try {
			return new Double(numberStr);
		} catch (NumberFormatException e) {
			throw new RuntimeException("The supplied text '" + numberStr 
					+ "' could not be parsed as a number");
		}
	}

	/**
	 * @return <code>null</code> if timeStr is <code>null</code>
	 */
	private static Double convertTime(String timeStr) {
		if (timeStr == null) {
			return null;
		}
		return new Double(HSSFDateUtil.convertTime(timeStr));
	}
	/**
	 * @param dateFormat pass <code>null</code> for default YYYYMMDD
	 * @return <code>null</code> if timeStr is <code>null</code>
	 */
	private static Double convertDate(String dateStr, SimpleDateFormat dateFormat) {
		if (dateStr == null) {
			return null;
		}
		Date dateVal; 
		if (dateFormat == null) {
			dateVal = HSSFDateUtil.parseYYYYMMDDDate(dateStr);
		} else {
			try {
				dateVal = dateFormat.parse(dateStr);
			} catch (ParseException e) {
				throw new RuntimeException("Failed to parse date '" + dateStr 
						+ "' using specified format '" + dateFormat + "'", e);
			}
		}
		return new Double(HSSFDateUtil.getExcelDate(dateVal));
	}

	public static DVConstraint createCustomFormulaConstraint(String formula) {
		if (formula == null) {
			throw new IllegalArgumentException("formula must be supplied");
		}
		return new DVConstraint(VT.FORMULA, OperatorType.IGNORED, formula, null, null, null, null);
	}
	
	/**
	 * @return both parsed formulas (for expression 1 and 2). 
	 */
	/* package */ FormulaPair createFormulas(HSSFSheet sheet) {
		Ptg[] formula1;
		Ptg[] formula2;
		if (isListValidationType()) {
			formula1 = createListFormula(sheet);
			formula2 = Ptg.EMPTY_PTG_ARRAY;
		} else {
			formula1 = convertDoubleFormula(_formula1, _value1, sheet);
			formula2 = convertDoubleFormula(_formula2, _value2, sheet);
		}
		return new FormulaPair(formula1, formula2);
	}

	private Ptg[] createListFormula(HSSFSheet sheet) {

		if (_explicitListValues == null) {
            HSSFWorkbook wb = sheet.getWorkbook();
            // formula is parsed with slightly different RVA rules: (root node type must be 'reference')
			return HSSFFormulaParser.parse(_formula1, wb, FormulaType.DATAVALIDATION_LIST, wb.getSheetIndex(sheet));
			// To do: Excel places restrictions on the available operations within a list formula.
			// Some things like union and intersection are not allowed.
		}
		// explicit list was provided
		StringBuffer sb = new StringBuffer(_explicitListValues.length * 16);
		for (int i = 0; i < _explicitListValues.length; i++) {
			if (i > 0) {
				sb.append('\0'); // list delimiter is the nul char
			}
			sb.append(_explicitListValues[i]);
		
		}
		return new Ptg[] { new StringPtg(sb.toString()), };
	}

	/**
	 * @return The parsed token array representing the formula or value specified. 
	 * Empty array if both formula and value are <code>null</code>
	 */
	private static Ptg[] convertDoubleFormula(String formula, Double value, HSSFSheet sheet) {
		if (formula == null) {
			if (value == null) {
				return Ptg.EMPTY_PTG_ARRAY;
			}
			return new Ptg[] { new NumberPtg(value.doubleValue()), };
		}
		if (value != null) {
			throw new IllegalStateException("Both formula and value cannot be present");
		}
        HSSFWorkbook wb = sheet.getWorkbook();
		return HSSFFormulaParser.parse(formula, wb, FormulaType.CELL, wb.getSheetIndex(sheet));
	}
	
	
	/**
	 * @return data validation type of this constraint
	 * @see ValidationType
	 */
	public int getValidationType() {
		return _validationType;
	}
	/**
	 * Convenience method
	 * @return <code>true</code> if this constraint is a 'list' validation
	 */
	public boolean isListValidationType() {
		return _validationType == VT.LIST;
	}
	/**
	 * Convenience method
	 * @return <code>true</code> if this constraint is a 'list' validation with explicit values
	 */
	public boolean isExplicitList() {
		return _validationType == VT.LIST && _explicitListValues != null;
	}
	/**
	 * @return the operator used for this constraint
	 * @see OperatorType
	 */
	public int getOperator() {
		return _operator;
	}
	/**
	 * Sets the comparison operator for this constraint
	 * @see OperatorType
	 */
	public void setOperator(int operator) {
		_operator = operator;
	}
	
	public String[] getExplicitListValues() {
		return _explicitListValues;
	}
	public void setExplicitListValues(String[] explicitListValues) {
		if (_validationType != VT.LIST) {
			throw new RuntimeException("Cannot setExplicitListValues on non-list constraint");
		}
		_formula1 = null;
		_explicitListValues = explicitListValues;
	}

	/**
	 * @return the formula for expression 1. May be <code>null</code>
	 */
	public String getFormula1() {
		return _formula1;
	}
	/**
	 * Sets a formula for expression 1.
	 */
	public void setFormula1(String formula1) {
		_value1 = null;
		_explicitListValues = null;
		_formula1 = formula1;
	}

	/**
	 * @return the formula for expression 2. May be <code>null</code>
	 */
	public String getFormula2() {
		return _formula2;
	}
	/**
	 * Sets a formula for expression 2.
	 */
	public void setFormula2(String formula2) {
		_value2 = null;
		_formula2 = formula2;
	}

	/**
	 * @return the numeric value for expression 1. May be <code>null</code>
	 */
	public Double getValue1() {
		return _value1;
	}
	/**
	 * Sets a numeric value for expression 1.
	 */
	public void setValue1(double value1) {
		_formula1 = null;
		_value1 = new Double(value1);
	}

	/**
	 * @return the numeric value for expression 2. May be <code>null</code>
	 */
	public Double getValue2() {
		return _value2;
	}
	/**
	 * Sets a numeric value for expression 2.
	 */
	public void setValue2(double value2) {
		_formula2 = null;
		_value2 = new Double(value2);
	}
}
