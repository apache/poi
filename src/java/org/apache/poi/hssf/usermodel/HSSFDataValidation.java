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
import org.apache.poi.ss.util.CellRangeAddressList;

/**
 *Utility class for creating data validation cells
 * 
 * @author Dragos Buleandra (dragos.buleandra@trade2b.ro)
 */
public final class HSSFDataValidation {
	/**
	 * Error style constants for error box
	 */
	public static final class ErrorStyle {
    	/** STOP style */
    	public static final int STOP    = 0x00;
    	/** WARNING style */
    	public static final int WARNING = 0x01;
    	/** INFO style */
    	public static final int INFO    = 0x02;
	}    

	private String _prompt_title;
	private String _prompt_text;
	private String _error_title;
	private String _error_text;

	private int _errorStyle = ErrorStyle.STOP;
	private boolean _emptyCellAllowed = true;
	private boolean _suppress_dropdown_arrow = false;
	private boolean _showPromptBox = true;
	private boolean _showErrorBox = true;
	private final CellRangeAddressList _regions;
	private DVConstraint _constraint;

	/**
	 * Constructor which initializes the cell range on which this object will be
	 * applied
	 * @param constraint 
	 */
	public HSSFDataValidation(CellRangeAddressList regions, DVConstraint constraint) {
		_regions = regions;
		_constraint = constraint;
	}


	public DVConstraint getConstraint() {
		return _constraint;
	}

	/**
	 * Sets the error style for error box
	 * @see ErrorStyle
	 */
	public void setErrorStyle(int error_style) {
		_errorStyle = error_style;
	}

	/**
	 * @return the error style of error box
	 * @see ErrorStyle
	 */
	public int getErrorStyle() {
		return _errorStyle;
	}

	/**
	 * Sets if this object allows empty as a valid value
	 * 
	 * @param allowed <code>true</code> if this object should treats empty as valid value , <code>false</code>
	 *            otherwise
	 */
	public void setEmptyCellAllowed(boolean allowed) {
		_emptyCellAllowed = allowed;
	}

	/**
	 * Retrieve the settings for empty cells allowed
	 * 
	 * @return True if this object should treats empty as valid value , false
	 *         otherwise
	 */
	public boolean getEmptyCellAllowed() {
		return _emptyCellAllowed;
	}

	/**
	 * Useful for list validation objects .
	 * 
	 * @param suppress
	 *            True if a list should display the values into a drop down list ,
	 *            false otherwise . In other words , if a list should display
	 *            the arrow sign on its right side
	 */
	public void setSuppressDropDownArrow(boolean suppress) {
		_suppress_dropdown_arrow = suppress;
	}

	/**
	 * Useful only list validation objects . This method always returns false if
	 * the object isn't a list validation object
	 * 
	 * @return <code>true</code> if a list should display the values into a drop down list ,
	 *         <code>false</code> otherwise .
	 */
	public boolean getSuppressDropDownArrow() {
		if (_constraint.isListValidationType()) {
			return _suppress_dropdown_arrow;
		}
		return false;
	}

	/**
	 * Sets the behaviour when a cell which belongs to this object is selected
	 * 
	 * @param show <code>true</code> if an prompt box should be displayed , <code>false</code> otherwise
	 */
	public void setShowPromptBox(boolean show) {
		_showPromptBox = show;
	}

	/**
	 * @return <code>true</code> if an prompt box should be displayed , <code>false</code> otherwise
	 */
	public boolean getShowPromptBox() {
		return _showPromptBox;
	}

	/**
	 * Sets the behaviour when an invalid value is entered
	 * 
	 * @param show <code>true</code> if an error box should be displayed , <code>false</code> otherwise
	 */
	public void setShowErrorBox(boolean show) {
		_showErrorBox = show;
	}

	/**
	 * @return <code>true</code> if an error box should be displayed , <code>false</code> otherwise
	 */
	public boolean getShowErrorBox() {
		return _showErrorBox;
	}


	/**
	 * Sets the title and text for the prompt box . Prompt box is displayed when
	 * the user selects a cell which belongs to this validation object . In
	 * order for a prompt box to be displayed you should also use method
	 * setShowPromptBox( boolean show )
	 * 
	 * @param title The prompt box's title
	 * @param text The prompt box's text
	 */
	public void createPromptBox(String title, String text) {
		_prompt_title = title;
		_prompt_text = text;
		this.setShowPromptBox(true);
	}

	/**
	 * @return Prompt box's title or <code>null</code>
	 */
	public String getPromptBoxTitle() {
		return _prompt_title;
	}

	/**
	 * @return Prompt box's text or <code>null</code>
	 */
	public String getPromptBoxText() {
		return _prompt_text;
	}

	/**
	 * Sets the title and text for the error box . Error box is displayed when
	 * the user enters an invalid value int o a cell which belongs to this
	 * validation object . In order for an error box to be displayed you should
	 * also use method setShowErrorBox( boolean show )
	 * 
	 * @param title The error box's title
	 * @param text The error box's text
	 */
	public void createErrorBox(String title, String text) {
		_error_title = title;
		_error_text = text;
		this.setShowErrorBox(true);
	}

	/**
	 * @return Error box's title or <code>null</code>
	 */
	public String getErrorBoxTitle() {
		return _error_title;
	}

	/**
	 * @return Error box's text or <code>null</code>
	 */
	public String getErrorBoxText() {
		return _error_text;
	}

	public DVRecord createDVRecord(HSSFSheet sheet) {

		FormulaPair fp = _constraint.createFormulas(sheet);
		
		return new DVRecord(_constraint.getValidationType(),
				_constraint.getOperator(),
				_errorStyle, _emptyCellAllowed, getSuppressDropDownArrow(),
				_constraint.isExplicitList(),
				_showPromptBox, _prompt_title, _prompt_text,
				_showErrorBox, _error_title, _error_text,
				fp.getFormula1(), fp.getFormula2(),
				_regions);
	}
}
