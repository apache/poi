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

import org.apache.poi.ss.util.CellRangeAddressList;


public interface DataValidation {
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

	public abstract DataValidationConstraint getValidationConstraint();

	/**
	 * Sets the error style for error box
	 * @see ErrorStyle
	 */
	public abstract void setErrorStyle(int error_style);

	/**o
	 * @return the error style of error box
	 * @see ErrorStyle
	 */
	public abstract int getErrorStyle();

	/**
	 * Sets if this object allows empty as a valid value
	 * 
	 * @param allowed <code>true</code> if this object should treats empty as valid value , <code>false</code>
	 *            otherwise
	 */
	public abstract void setEmptyCellAllowed(boolean allowed);

	/**
	 * Retrieve the settings for empty cells allowed
	 * 
	 * @return True if this object should treats empty as valid value , false
	 *         otherwise
	 */
	public abstract boolean getEmptyCellAllowed();

	/**
	 * Useful for list validation objects .
	 * 
	 * @param suppress
	 *            True if a list should display the values into a drop down list ,
	 *            false otherwise . In other words , if a list should display
	 *            the arrow sign on its right side
	 */
	public abstract void setSuppressDropDownArrow(boolean suppress);

	/**
	 * Useful only list validation objects . This method always returns false if
	 * the object isn't a list validation object
	 * 
	 * @return <code>true</code> if a list should display the values into a drop down list ,
	 *         <code>false</code> otherwise .
	 */
	public abstract boolean getSuppressDropDownArrow();

	/**
	 * Sets the behaviour when a cell which belongs to this object is selected
	 * 
	 * @param show <code>true</code> if an prompt box should be displayed , <code>false</code> otherwise
	 */
	public abstract void setShowPromptBox(boolean show);

	/**
	 * @return <code>true</code> if an prompt box should be displayed , <code>false</code> otherwise
	 */
	public abstract boolean getShowPromptBox();

	/**
	 * Sets the behaviour when an invalid value is entered
	 * 
	 * @param show <code>true</code> if an error box should be displayed , <code>false</code> otherwise
	 */
	public abstract void setShowErrorBox(boolean show);

	/**
	 * @return <code>true</code> if an error box should be displayed , <code>false</code> otherwise
	 */
	public abstract boolean getShowErrorBox();

	/**
	 * Sets the title and text for the prompt box . Prompt box is displayed when
	 * the user selects a cell which belongs to this validation object . In
	 * order for a prompt box to be displayed you should also use method
	 * setShowPromptBox( boolean show )
	 * 
	 * @param title The prompt box's title
	 * @param text The prompt box's text
	 */
	public abstract void createPromptBox(String title, String text);

	/**
	 * @return Prompt box's title or <code>null</code>
	 */
	public abstract String getPromptBoxTitle();

	/**
	 * @return Prompt box's text or <code>null</code>
	 */
	public abstract String getPromptBoxText();

	/**
	 * Sets the title and text for the error box . Error box is displayed when
	 * the user enters an invalid value int o a cell which belongs to this
	 * validation object . In order for an error box to be displayed you should
	 * also use method setShowErrorBox( boolean show )
	 * 
	 * @param title The error box's title
	 * @param text The error box's text
	 */
	public abstract void createErrorBox(String title, String text);

	/**
	 * @return Error box's title or <code>null</code>
	 */
	public abstract String getErrorBoxTitle();

	/**
	 * @return Error box's text or <code>null</code>
	 */
	public abstract String getErrorBoxText();

	public abstract CellRangeAddressList getRegions();

}
