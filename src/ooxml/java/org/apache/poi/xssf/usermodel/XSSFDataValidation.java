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

import org.apache.commons.collections4.MapUtils;
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
    private static final int MAX_TEXT_LENGTH = 255;

    private CTDataValidation ctDataValidation;
    private XSSFDataValidationConstraint validationConstraint;
    private CellRangeAddressList regions;

    static Map<Integer, STDataValidationOperator.Enum> operatorTypeMappings = new HashMap<>();
    static Map<STDataValidationOperator.Enum, Integer> operatorTypeReverseMappings = new HashMap<>();
    static Map<Integer, STDataValidationType.Enum> validationTypeMappings = new HashMap<>();
    static Map<STDataValidationType.Enum, Integer> validationTypeReverseMappings = new HashMap<>();
    static Map<Integer, STDataValidationErrorStyle.Enum> errorStyleMappings = new HashMap<>();
    static Map<STDataValidationErrorStyle.Enum, Integer> reverseErrorStyleMappings;

    static {
        errorStyleMappings.put(DataValidation.ErrorStyle.INFO, STDataValidationErrorStyle.INFORMATION);
        errorStyleMappings.put(DataValidation.ErrorStyle.STOP, STDataValidationErrorStyle.STOP);
        errorStyleMappings.put(DataValidation.ErrorStyle.WARNING, STDataValidationErrorStyle.WARNING);

        reverseErrorStyleMappings = MapUtils.invertMap(errorStyleMappings);

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
        this(getConstraint(ctDataValidation), regions, ctDataValidation);
    }

    public XSSFDataValidation(XSSFDataValidationConstraint constraint,CellRangeAddressList regions,CTDataValidation ctDataValidation) {
        super();
        this.validationConstraint = constraint;
        this.ctDataValidation = ctDataValidation;
        this.regions = regions;
    }
 
    CTDataValidation getCtDataValidation() {
        return ctDataValidation;
    }



    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#createErrorBox(java.lang.String, java.lang.String)
     */
    public void createErrorBox(String title, String text) {
        // the spec does not specify a length-limit, however Excel reports files as "corrupt" if they exceed 255 bytes for these texts...
        if(title != null && title.length() > MAX_TEXT_LENGTH) {
            throw new IllegalStateException("Error-title cannot be longer than 32 characters, but had: " + title);
        }
        if(text != null && text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalStateException("Error-text cannot be longer than 255 characters, but had: " + text);
        }
        ctDataValidation.setErrorTitle(encodeUtf(title));
        ctDataValidation.setError(encodeUtf(text));
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#createPromptBox(java.lang.String, java.lang.String)
     */
    public void createPromptBox(String title, String text) {
        // the spec does not specify a length-limit, however Excel reports files as "corrupt" if they exceed 255 bytes for these texts...
        if(title != null && title.length() > MAX_TEXT_LENGTH) {
            throw new IllegalStateException("Error-title cannot be longer than 32 characters, but had: " + title);
        }
        if(text != null && text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalStateException("Error-text cannot be longer than 255 characters, but had: " + text);
        }
        ctDataValidation.setPromptTitle(encodeUtf(title));
        ctDataValidation.setPrompt(encodeUtf(text));
    }

    /**
     * For all characters which cannot be represented in XML as defined by the XML 1.0 specification,
     * the characters are escaped using the Unicode numerical character representation escape character
     * format _xHHHH_, where H represents a hexadecimal character in the character's value.
     * <p>
     * Example: The Unicode character 0D is invalid in an XML 1.0 document,
     * so it shall be escaped as <code>_x000D_</code>.
     * </p>
     * See section 3.18.9 in the OOXML spec.
     *
     * @param   text the string to encode
     * @return  the encoded string
     */
    private String encodeUtf(String text) {
        if(text == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        for(char c : text.toCharArray()) {
            // for now only encode characters below 32, we can add more here if needed
            if(c < 32) {
                builder.append("_x").append(c < 16 ? "000" : "00").append(Integer.toHexString(c)).append("_");
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getEmptyCellAllowed()
     */
    public boolean getEmptyCellAllowed() {
        return ctDataValidation.getAllowBlank();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getErrorBoxText()
     */
    public String getErrorBoxText() {
        return ctDataValidation.getError();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getErrorBoxTitle()
     */
    public String getErrorBoxTitle() {
        return ctDataValidation.getErrorTitle();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getErrorStyle()
     */
    public int getErrorStyle() {
        return reverseErrorStyleMappings.get(ctDataValidation.getErrorStyle());
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getPromptBoxText()
     */
    public String getPromptBoxText() {
        return ctDataValidation.getPrompt();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getPromptBoxTitle()
     */
    public String getPromptBoxTitle() {
        return ctDataValidation.getPromptTitle();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getShowErrorBox()
     */
    public boolean getShowErrorBox() {
        return ctDataValidation.getShowErrorMessage();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getShowPromptBox()
     */
    public boolean getShowPromptBox() {
        return ctDataValidation.getShowInputMessage();
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#getSuppressDropDownArrow()
     */
    public boolean getSuppressDropDownArrow() {
        return !ctDataValidation.getShowDropDown();
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
        ctDataValidation.setAllowBlank(allowed);
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#setErrorStyle(int)
     */
    public void setErrorStyle(int errorStyle) {
        ctDataValidation.setErrorStyle(errorStyleMappings.get(errorStyle));
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#setShowErrorBox(boolean)
     */
    public void setShowErrorBox(boolean show) {
        ctDataValidation.setShowErrorMessage(show);
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#setShowPromptBox(boolean)
     */
    public void setShowPromptBox(boolean show) {
        ctDataValidation.setShowInputMessage(show);
    }

    /* (non-Javadoc)
     * @see org.apache.poi.ss.usermodel.DataValidation#setSuppressDropDownArrow(boolean)
     */
    public void setSuppressDropDownArrow(boolean suppress) {
        if (validationConstraint.getValidationType()==ValidationType.LIST) {
            ctDataValidation.setShowDropDown(!suppress);
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

    private static XSSFDataValidationConstraint getConstraint(CTDataValidation ctDataValidation) {
        String formula1 = ctDataValidation.getFormula1();
        String formula2 = ctDataValidation.getFormula2();
        Enum operator = ctDataValidation.getOperator();
        org.openxmlformats.schemas.spreadsheetml.x2006.main.STDataValidationType.Enum type = ctDataValidation.getType();
        Integer validationType = XSSFDataValidation.validationTypeReverseMappings.get(type);
        Integer operatorType = XSSFDataValidation.operatorTypeReverseMappings.get(operator);
        return new XSSFDataValidationConstraint(validationType,operatorType, formula1,formula2);
    }
}
