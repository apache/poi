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

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDataValidation;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataValidationErrorStyle;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataValidationOperator;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataValidationType;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataValidationOperator.Enum;

/**
 * @author <a href="rjankiraman@emptoris.com">Radhakrishnan J</a>
 *
 */
public class XSSFDataValidation implements DataValidation {
	private CTDataValidation ctDdataValidation;
	private XSSFDataValidationConstraint validationConstraint;
	private CellRangeAddressList regions;

    static Map<Integer,STDataValidationOperator.Enum> operatorTypeMappings = new HashMap<Integer,STDataValidationOperator.Enum>();
	static Map<STDataValidationOperator.Enum,Integer> operatorTypeReverseMappings = new HashMap<STDataValidationOperator.Enum,Integer>();
	static Map<Integer,STDataValidationType.Enum> validationTypeMappings = new HashMap<Integer,STDataValidationType.Enum>();
	static Map<STDataValidationType.Enum,Integer> validationTypeReverseMappings = new HashMap<STDataValidationType.Enum,Integer>();
    static Map<Integer,STDataValidationErrorStyle.Enum> errorStyleMappings = new HashMap<Integer,STDataValidationErrorStyle.Enum>();
    static {
		errorStyleMappings.put(DataValidation.ErrorStyle.INFO, STDataValidationErrorStyle.INFORMATION);
		errorStyleMappings.put(DataValidation.ErrorStyle.STOP, STDataValidationErrorStyle.STOP);
		errorStyleMappings.put(DataValidation.ErrorStyle.WARNING, STDataValidationErrorStyle.WARNING);
    }
	
    
	static {
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.BETWEEN,STDataValidationOperator.BETWEEN);
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.NOT_BETWEEN,STDataValidationOperator.NOT_BETWEEN);
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.EQUAL,STDataValidationOperator.EQUAL);
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.NOT_EQUAL,STDataValidationOperator.NOT_EQUAL);
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.GREATER_THAN,STDataValidationOperator.GREATER_THAN);    	
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.GREATER_OR_EQUAL,STDataValidationOperator.GREATER_THAN_OR_EQUAL);
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.LESS_THAN,STDataValidationOperator.LESS_THAN);    	
		operatorTypeMappings.put(DataValidationConstraint.OperatorType.LESS_OR_EQUAL,STDataValidationOperator.LESS_THAN_OR_EQUAL);
		
		for( Map.Entry<Integer,STDataValidationOperator.Enum> entry : operatorTypeMappings.entrySet() ) {
			operatorTypeReverseMappings.put(entry.getValue(),entry.getKey());
		}
	}

	static {
		validationTypeMappings.put(DataValidationConstraint.ValidationType.FORMULA,STDataValidationType.CUSTOM);
		validationTypeMappings.put(DataValidationConstraint.ValidationType.DATE,STDataValidationType.DATE);
		validationTypeMappings.put(DataValidationConstraint.ValidationType.DECIMAL,STDataValidationType.DECIMAL);    	
		validationTypeMappings.put(DataValidationConstraint.ValidationType.LIST,STDataValidationType.LIST); 
		validationTypeMappings.put(DataValidationConstraint.ValidationType.ANY,STDataValidationType.NONE);
		validationTypeMappings.put(DataValidationConstraint.ValidationType.TEXT_LENGTH,STDataValidationType.TEXT_LENGTH);
		validationTypeMappings.put(DataValidationConstraint.ValidationType.TIME,STDataValidationType.TIME);  
		validationTypeMappings.put(DataValidationConstraint.ValidationType.INTEGER,STDataValidationType.WHOLE);
		
		for( Map.Entry<Integer,STDataValidationType.Enum> entry : validationTypeMappings.entrySet() ) {
			validationTypeReverseMappings.put(entry.getValue(),entry.getKey());
		}
	}

	
	XSSFDataValidation(CellRangeAddressList regions,CTDataValidation ctDataValidation) {
		super();
		this.validationConstraint = getConstraint(ctDataValidation);
		this.ctDdataValidation = ctDataValidation;
		this.regions = regions;
		this.ctDdataValidation.setErrorStyle(STDataValidationErrorStyle.STOP);
		this.ctDdataValidation.setAllowBlank(true);
	}	

	public XSSFDataValidation(XSSFDataValidationConstraint constraint,CellRangeAddressList regions,CTDataValidation ctDataValidation) {
		super();
		this.validationConstraint = constraint;
		this.ctDdataValidation = ctDataValidation;
		this.regions = regions;
		this.ctDdataValidation.setErrorStyle(STDataValidationErrorStyle.STOP);
		this.ctDdataValidation.setAllowBlank(true);
	}
 
	CTDataValidation getCtDdataValidation() {
		return ctDdataValidation;
	}



	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#createErrorBox(java.lang.String, java.lang.String)
	 */
	public void createErrorBox(String title, String text) {
		ctDdataValidation.setErrorTitle(title);
		ctDdataValidation.setError(text);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#createPromptBox(java.lang.String, java.lang.String)
	 */
	public void createPromptBox(String title, String text) {
		ctDdataValidation.setPromptTitle(title);
		ctDdataValidation.setPrompt(text);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getEmptyCellAllowed()
	 */
	public boolean getEmptyCellAllowed() {
		return ctDdataValidation.getAllowBlank();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getErrorBoxText()
	 */
	public String getErrorBoxText() {
		return ctDdataValidation.getError();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getErrorBoxTitle()
	 */
	public String getErrorBoxTitle() {
		return ctDdataValidation.getErrorTitle();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getErrorStyle()
	 */
	public int getErrorStyle() {
		return ctDdataValidation.getErrorStyle().intValue();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getPromptBoxText()
	 */
	public String getPromptBoxText() {
		return ctDdataValidation.getPrompt();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getPromptBoxTitle()
	 */
	public String getPromptBoxTitle() {
		return ctDdataValidation.getPromptTitle();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getShowErrorBox()
	 */
	public boolean getShowErrorBox() {
		return ctDdataValidation.getShowErrorMessage();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getShowPromptBox()
	 */
	public boolean getShowPromptBox() {
		return ctDdataValidation.getShowInputMessage();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getSuppressDropDownArrow()
	 */
	public boolean getSuppressDropDownArrow() {
		return !ctDdataValidation.getShowDropDown();
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#getValidationConstraint()
	 */
	public DataValidationConstraint getValidationConstraint() {
		return validationConstraint;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#setEmptyCellAllowed(boolean)
	 */
	public void setEmptyCellAllowed(boolean allowed) {
		ctDdataValidation.setAllowBlank(allowed);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#setErrorStyle(int)
	 */
	public void setErrorStyle(int errorStyle) {
		ctDdataValidation.setErrorStyle(errorStyleMappings.get(errorStyle));
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#setShowErrorBox(boolean)
	 */
	public void setShowErrorBox(boolean show) {
		ctDdataValidation.setShowErrorMessage(show);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#setShowPromptBox(boolean)
	 */
	public void setShowPromptBox(boolean show) {
		ctDdataValidation.setShowInputMessage(show);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.ss.usermodel.DataValidation#setSuppressDropDownArrow(boolean)
	 */
	public void setSuppressDropDownArrow(boolean suppress) {
		if (validationConstraint.getValidationType()==ValidationType.LIST) {
			ctDdataValidation.setShowDropDown(!suppress);
		}
	}

	public CellRangeAddressList getRegions() {
		return regions;
	}
	
	public String prettyPrint() {
		StringBuilder builder = new StringBuilder();
		for(CellRangeAddress address : regions.getCellRangeAddresses()) {
			builder.append(address.formatAsString());
		}
		builder.append(" => ");
		builder.append(this.validationConstraint.prettyPrint());	
		return builder.toString();
	}
	
    private XSSFDataValidationConstraint getConstraint(CTDataValidation ctDataValidation) {
    	XSSFDataValidationConstraint constraint = null;
    	String formula1 = ctDataValidation.getFormula1();
    	String formula2 = ctDataValidation.getFormula2();
    	Enum operator = ctDataValidation.getOperator();
    	org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataValidationType.Enum type = ctDataValidation.getType();
		Integer validationType = XSSFDataValidation.validationTypeReverseMappings.get(type);
		Integer operatorType = XSSFDataValidation.operatorTypeReverseMappings.get(operator);
		constraint = new XSSFDataValidationConstraint(validationType,operatorType, formula1,formula2);
    	return constraint;
    }
}
