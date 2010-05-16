/**
 * 
 */
package org.apache.poi.hssf.usermodel;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * @author <a href="rjankiraman@emptoris.com">Radhakrishnan J</a>
 * 
 */
public class HSSFDataValidationHelper implements DataValidationHelper {
	@SuppressWarnings("unused")
	private HSSFSheet sheet;
	
	public HSSFDataValidationHelper(HSSFSheet sheet) {
		super();
		this.sheet = sheet;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createDateConstraint
	 * (int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createDateConstraint(int operatorType, String formula1, String formula2, String dateFormat) {
		return DVConstraint.createDateConstraint(operatorType, formula1, formula2, dateFormat);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createExplicitListConstraint
	 * (java.lang.String[])
	 */
	public DataValidationConstraint createExplicitListConstraint(String[] listOfValues) {
		return DVConstraint.createExplicitListConstraint(listOfValues);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createFormulaListConstraint
	 * (java.lang.String)
	 */
	public DataValidationConstraint createFormulaListConstraint(String listFormula) {
		return DVConstraint.createFormulaListConstraint(listFormula);
	}

	
	
	public DataValidationConstraint createNumericConstraint(int validationType,int operatorType, String formula1, String formula2) {
		return DVConstraint.createNumericConstraint(validationType, operatorType, formula1, formula2);
	}

	public DataValidationConstraint createIntegerConstraint(int operatorType, String formula1, String formula2) {
		return DVConstraint.createNumericConstraint(ValidationType.INTEGER, operatorType, formula1, formula2);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createNumericConstraint
	 * (int, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createDecimalConstraint(int operatorType, String formula1, String formula2) {
		return DVConstraint.createNumericConstraint(ValidationType.DECIMAL, operatorType, formula1, formula2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createTextLengthConstraint
	 * (int, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createTextLengthConstraint(int operatorType, String formula1, String formula2) {
		return DVConstraint.createNumericConstraint(ValidationType.TEXT_LENGTH, operatorType, formula1, formula2);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createTimeConstraint
	 * (int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createTimeConstraint(int operatorType, String formula1, String formula2) {
		return DVConstraint.createTimeConstraint(operatorType, formula1, formula2);
	}

	
	
	public DataValidationConstraint createCustomConstraint(String formula) {
		return DVConstraint.createCustomFormulaConstraint(formula);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.poi.ss.usermodel.DataValidationHelper#createValidation(org
	 * .apache.poi.ss.usermodel.DataValidationConstraint,
	 * org.apache.poi.ss.util.CellRangeAddressList)
	 */
	public DataValidation createValidation(DataValidationConstraint constraint, CellRangeAddressList cellRangeAddressList) {
		return new HSSFDataValidation(cellRangeAddressList, constraint); 
	}
}
