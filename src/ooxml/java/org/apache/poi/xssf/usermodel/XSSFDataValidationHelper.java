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
package org.apache.poi.xssf.usermodel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataValidation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataValidationType;

/**
 * @author <a href="rjankiraman@emptoris.com">Radhakrishnan J</a>
 *
 */
public class XSSFDataValidationHelper implements DataValidationHelper {
	private XSSFSheet xssfSheet;
	
    
    public XSSFDataValidationHelper(XSSFSheet xssfSheet) {
		super();
		this.xssfSheet = xssfSheet;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createDateConstraint(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createDateConstraint(int operatorType, String formula1, String formula2, String dateFormat) {
		return new XSSFDataValidationConstraint(ValidationType.DATE, operatorType,formula1, formula2);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createDecimalConstraint(int, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createDecimalConstraint(int operatorType, String formula1, String formula2) {
		return new XSSFDataValidationConstraint(ValidationType.DECIMAL, operatorType,formula1, formula2);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createExplicitListConstraint(java.lang.String[])
	 */
	public DataValidationConstraint createExplicitListConstraint(String[] listOfValues) {
		return new XSSFDataValidationConstraint(listOfValues);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createFormulaListConstraint(java.lang.String)
	 */
	public DataValidationConstraint createFormulaListConstraint(String listFormula) {
		return new XSSFDataValidationConstraint(ValidationType.LIST, listFormula);
	}

	
	
	public DataValidationConstraint createNumericConstraint(int validationType, int operatorType, String formula1, String formula2) {
		if( validationType==ValidationType.INTEGER) {
			return createIntegerConstraint(operatorType, formula1, formula2);
		} else if ( validationType==ValidationType.DECIMAL) {
			return createDecimalConstraint(operatorType, formula1, formula2);
		} else if ( validationType==ValidationType.TEXT_LENGTH) {
			return createTextLengthConstraint(operatorType, formula1, formula2);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createIntegerConstraint(int, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createIntegerConstraint(int operatorType, String formula1, String formula2) {
		return new XSSFDataValidationConstraint(ValidationType.INTEGER, operatorType,formula1,formula2);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createTextLengthConstraint(int, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createTextLengthConstraint(int operatorType, String formula1, String formula2) {
		return new XSSFDataValidationConstraint(ValidationType.TEXT_LENGTH, operatorType,formula1,formula2);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createTimeConstraint(int, java.lang.String, java.lang.String, java.lang.String)
	 */
	public DataValidationConstraint createTimeConstraint(int operatorType, String formula1, String formula2) {
		return new XSSFDataValidationConstraint(ValidationType.TIME, operatorType,formula1,formula2);
	}

	public DataValidationConstraint createCustomConstraint(String formula) {
		return new XSSFDataValidationConstraint(ValidationType.FORMULA, formula);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidationHelper#createValidation(org.apache.poi.ss.usermodel.DataValidationConstraint, org.apache.poi.ss.util.CellRangeAddressList)
	 */
	public DataValidation createValidation(DataValidationConstraint constraint, CellRangeAddressList cellRangeAddressList) {
		XSSFDataValidationConstraint dataValidationConstraint = (XSSFDataValidationConstraint)constraint;
		CTDataValidation newDataValidation = CTDataValidation.Factory.newInstance();

		int validationType = constraint.getValidationType();
		switch(validationType) {
			case DataValidationConstraint.ValidationType.LIST:
		    	newDataValidation.setType(STDataValidationType.LIST);
				newDataValidation.setFormula1(constraint.getFormula1());				
		    	break;
			case DataValidationConstraint.ValidationType.ANY:				
				newDataValidation.setType(STDataValidationType.NONE);				
				break;
			case DataValidationConstraint.ValidationType.TEXT_LENGTH:
				newDataValidation.setType(STDataValidationType.TEXT_LENGTH);
				break;				
			case DataValidationConstraint.ValidationType.DATE:
				newDataValidation.setType(STDataValidationType.DATE);
				break;				
			case DataValidationConstraint.ValidationType.INTEGER:
				newDataValidation.setType(STDataValidationType.WHOLE);
				break;				
			case DataValidationConstraint.ValidationType.DECIMAL:
				newDataValidation.setType(STDataValidationType.DECIMAL);
				break;				
			case DataValidationConstraint.ValidationType.TIME:
				newDataValidation.setType(STDataValidationType.TIME);
				break;
			case DataValidationConstraint.ValidationType.FORMULA:
				newDataValidation.setType(STDataValidationType.CUSTOM);
				break;
			default:
				newDataValidation.setType(STDataValidationType.NONE);				
		}
		
		if (validationType!=ValidationType.ANY && validationType!=ValidationType.LIST) {
			newDataValidation.setOperator(XSSFDataValidation.operatorTypeMappings.get(constraint.getOperator()));			
			if (constraint.getFormula1() != null) {
				newDataValidation.setFormula1(constraint.getFormula1());
			}
			if (constraint.getFormula2() != null) {
				newDataValidation.setFormula2(constraint.getFormula2());
			}
		}
		
		CellRangeAddress[] cellRangeAddresses = cellRangeAddressList.getCellRangeAddresses();
		List<String> sqref = new ArrayList<String>();
		for (int i = 0; i < cellRangeAddresses.length; i++) {
			CellRangeAddress cellRangeAddress = cellRangeAddresses[i];
			sqref.add(cellRangeAddress.formatAsString());
		}
		newDataValidation.setSqref(sqref);
		
		return new XSSFDataValidation(dataValidationConstraint,cellRangeAddressList,newDataValidation);
	}
}
