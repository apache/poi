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

import org.apache.poi.hssf.record.DVRecord;
import org.apache.poi.hssf.usermodel.DVConstraint.FormulaPair;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationConstraint.ValidationType;
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * Utility class for creating data validation cells
 */
public final class HSSFDataValidation implements DataValidation {
	private String _prompt_title;
	private String _prompt_text;
	private String _error_title;
	private String _error_text;

	private int _errorStyle = ErrorStyle.STOP;
	private boolean _emptyCellAllowed = true;
	private boolean _suppress_dropdown_arrow;
	private boolean _showPromptBox = true;
	private boolean _showErrorBox = true;
	private CellRangeAddressList _regions;
	private DVConstraint _constraint;

	/**
	 * Constructor which initializes the cell range on which this object will be
	 * applied
	 *
	 * @param regions A list of regions where the constraint is validated.
	 * @param constraint The constraints to apply for this validation.
	 */
	public HSSFDataValidation(CellRangeAddressList regions, DataValidationConstraint constraint) {
		_regions = regions;
		
		//FIXME: This cast can be avoided.
		_constraint = (DVConstraint)constraint;
	}


	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getConstraint()
	 */
	public DataValidationConstraint getValidationConstraint() {
		return _constraint;
	}

	public DVConstraint getConstraint() {
		return _constraint;
	}
	
	public CellRangeAddressList getRegions() {
		return _regions;
	}


	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setErrorStyle(int)
	 */
	public void setErrorStyle(int error_style) {
		_errorStyle = error_style;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorStyle()
	 */
	public int getErrorStyle() {
		return _errorStyle;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setEmptyCellAllowed(boolean)
	 */
	public void setEmptyCellAllowed(boolean allowed) {
		_emptyCellAllowed = allowed;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getEmptyCellAllowed()
	 */
	public boolean getEmptyCellAllowed() {
		return _emptyCellAllowed;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setSuppressDropDownArrow(boolean)
	 */
	public void setSuppressDropDownArrow(boolean suppress) {
		_suppress_dropdown_arrow = suppress;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getSuppressDropDownArrow()
	 */
	public boolean getSuppressDropDownArrow() {
		//noinspection SimplifiableIfStatement
		if (_constraint.getValidationType()==ValidationType.LIST) {
			return _suppress_dropdown_arrow;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setShowPromptBox(boolean)
	 */
	public void setShowPromptBox(boolean show) {
		_showPromptBox = show;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getShowPromptBox()
	 */
	public boolean getShowPromptBox() {
		return _showPromptBox;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#setShowErrorBox(boolean)
	 */
	public void setShowErrorBox(boolean show) {
		_showErrorBox = show;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getShowErrorBox()
	 */
	public boolean getShowErrorBox() {
		return _showErrorBox;
	}


	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#createPromptBox(java.lang.String, java.lang.String)
	 */
	public void createPromptBox(String title, String text) {
		// check length-limits
		if(title != null && title.length() > 32) {
			throw new IllegalStateException("Prompt-title cannot be longer than 32 characters, but had: " + title);
		}
		if(text != null && text.length() > 255) {
			throw new IllegalStateException("Prompt-text cannot be longer than 255 characters, but had: " + text);
		}
		_prompt_title = title;
		_prompt_text = text;
		this.setShowPromptBox(true);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getPromptBoxTitle()
	 */
	public String getPromptBoxTitle() {
		return _prompt_title;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getPromptBoxText()
	 */
	public String getPromptBoxText() {
		return _prompt_text;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#createErrorBox(java.lang.String, java.lang.String)
	 */
	public void createErrorBox(String title, String text) {
		if(title != null && title.length() > 32) {
			throw new IllegalStateException("Error-title cannot be longer than 32 characters, but had: " + title);
		}
		if(text != null && text.length() > 255) {
			throw new IllegalStateException("Error-text cannot be longer than 255 characters, but had: " + text);
		}
		_error_title = title;
		_error_text = text;
		this.setShowErrorBox(true);
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorBoxTitle()
	 */
	public String getErrorBoxTitle() {
		return _error_title;
	}

	/* (non-Javadoc)
	 * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorBoxText()
	 */
	public String getErrorBoxText() {
		return _error_text;
	}

	public DVRecord createDVRecord(HSSFSheet sheet) {

		FormulaPair fp = _constraint.createFormulas(sheet);
		
		return new DVRecord(_constraint.getValidationType(),
				_constraint.getOperator(),
				_errorStyle, _emptyCellAllowed, getSuppressDropDownArrow(),
				_constraint.getValidationType()==ValidationType.LIST && _constraint.getExplicitListValues()!=null,
				_showPromptBox, _prompt_title, _prompt_text,
				_showErrorBox, _error_title, _error_text,
				fp.getFormula1(), fp.getFormula2(),
				_regions);
	}
}
