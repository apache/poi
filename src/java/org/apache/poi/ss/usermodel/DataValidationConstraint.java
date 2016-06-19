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
package org.apache.poi.ss.usermodel;


public interface DataValidationConstraint {

	/**
	 * @return data validation type of this constraint
	 * @see ValidationType
	 */
	public abstract int getValidationType();

	/**
	 * @return the operator used for this constraint
	 * @see OperatorType
	 */
	public abstract int getOperator();

	/**
	 * Sets the comparison operator for this constraint
	 * @see OperatorType
	 */
	public abstract void setOperator(int operator);

    /**
	 * If validation type is {@link ValidationType#LIST}
	 * and <code>formula1</code> was comma-separated literal values rather than a range or named range,
	 * returns list of literal values.
	 * Otherwise returns <code>null</code>.
	 */
	public abstract String[] getExplicitListValues();

	public abstract void setExplicitListValues(String[] explicitListValues);

	/**
	 * @return the formula for expression 1. May be <code>null</code>
	 */
	public abstract String getFormula1();

	/**
	 * Sets a formula for expression 1.
	 */
	public abstract void setFormula1(String formula1);

	/**
	 * @return the formula for expression 2. May be <code>null</code>
	 */
	public abstract String getFormula2();

	/**
	 * Sets a formula for expression 2.
	 */
	public abstract void setFormula2(String formula2);
	
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
		
		/* package */ public static void validateSecondArg(int comparisonOperator, String paramValue) {
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
}
