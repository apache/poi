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
 * Utility class for creating data validation cells.
 */
public final class HSSFDataValidation implements DataValidation {

    private String promptTitle;
    private String promptText;
    private String errorTitle;
    private String errorText;

    private int errorStyle = ErrorStyle.STOP;
    private boolean emptyCellAllowed = true;
    private boolean suppressDropdownArrow = false;
    private boolean showPromptBox = true;
    private boolean showErrorBox = true;
    private CellRangeAddressList regions;
    private DVConstraint constraint;

    /**
     * Constructor which initializes the cell range on which this object will be applied.
     */
    public HSSFDataValidation(CellRangeAddressList regions, DataValidationConstraint constraint) {
        this.regions = regions;

        //FIXME: This cast can be avoided.
        this.constraint = (DVConstraint) constraint;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getConstraint()
     */
    public DataValidationConstraint getValidationConstraint() {
        return constraint;
    }

    public DVConstraint getConstraint() {
        return constraint;
    }

    public CellRangeAddressList getRegions() {
        return regions;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#setErrorStyle(int)
     */
    public void setErrorStyle(int errorStyle) {
        this.errorStyle = errorStyle;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorStyle()
     */
    public int getErrorStyle() {
        return errorStyle;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#setEmptyCellAllowed(boolean)
     */
    public void setEmptyCellAllowed(boolean allowed) {
        emptyCellAllowed = allowed;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getEmptyCellAllowed()
     */
    public boolean getEmptyCellAllowed() {
        return emptyCellAllowed;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#setSuppressDropDownArrow(boolean)
     */
    public void setSuppressDropDownArrow(boolean suppress) {
        suppressDropdownArrow = suppress;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getSuppressDropDownArrow()
     */
    public boolean getSuppressDropDownArrow() {
        if (constraint.getValidationType() == ValidationType.LIST) {
            return suppressDropdownArrow;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#setShowPromptBox(boolean)
     */
    public void setShowPromptBox(boolean show) {
        showPromptBox = show;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getShowPromptBox()
     */
    public boolean getShowPromptBox() {
        return showPromptBox;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#setShowErrorBox(boolean)
     */
    public void setShowErrorBox(boolean show) {
        showErrorBox = show;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getShowErrorBox()
     */
    public boolean getShowErrorBox() {
        return showErrorBox;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#createPromptBox(java.lang.String, java.lang.String)
     */
    public void createPromptBox(String title, String text) {
        promptTitle = title;
        promptText = text;
        this.setShowPromptBox(true);
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getPromptBoxTitle()
     */
    public String getPromptBoxTitle() {
        return promptTitle;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getPromptBoxText()
     */
    public String getPromptBoxText() {
        return promptText;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#createErrorBox(java.lang.String, java.lang.String)
     */
    public void createErrorBox(String title, String text) {
        errorTitle = title;
        errorText = text;
        this.setShowErrorBox(true);
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorBoxTitle()
     */
    public String getErrorBoxTitle() {
        return errorTitle;
    }

    /* (non-Javadoc)
     * @see org.apache.poi.hssf.usermodel.DataValidation#getErrorBoxText()
     */
    public String getErrorBoxText() {
        return errorText;
    }

    public DVRecord createDVRecord(HSSFSheet sheet) {
        FormulaPair fp = constraint.createFormulas(sheet);

        return new DVRecord(constraint.getValidationType(),
                constraint.getOperator(),
                errorStyle, emptyCellAllowed, getSuppressDropDownArrow(),
                constraint.getValidationType() == ValidationType.LIST && constraint.getExplicitListValues() != null,
                showPromptBox, promptTitle, promptText,
                showErrorBox, errorTitle, errorText,
                fp.getFormula1(), fp.getFormula2(),
                regions);
    }
}
