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

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.ptg.Ptg;

import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * Represents a defined named range in a SpreadsheetML workbook.
 * <p>
 * Defined names are descriptive text that is used to represents a cell, range of cells, formula, or constant value.
 * Use easy-to-understand names, such as Products, to refer to hard to understand ranges, such as <code>Sales!C20:C30</code>.
 * </p>
 * Example:
 * <pre><blockquote>
 *   XSSFWorkbook wb = new XSSFWorkbook();
 *   XSSFSheet sh = wb.createSheet("Sheet1");
 *
 *   //applies to the entire workbook
 *   XSSFName name1 = wb.createName();
 *   name1.setNameName("FMLA");
 *   name1.setRefersToFormula("Sheet1!$B$3");
 *
 *   //applies to Sheet1
 *   XSSFName name2 = wb.createName();
 *   name2.setNameName("SheetLevelName");
 *   name2.setComment("This name is scoped to Sheet1");
 *   name2.setLocalSheetId(0);
 *   name2.setRefersToFormula("Sheet1!$B$3");
 *
 * </blockquote></pre>
 *
 * @author Nick Burch
 * @author Yegor Kozlov
 */
public final class XSSFName implements Name {
    
    /**
     * A built-in defined name that specifies the workbook's print area
     */
    public static final String BUILTIN_PRINT_AREA = "_xlnm.Print_Area";

    /**
     * A built-in defined name that specifies the row(s) or column(s) to repeat
     * at the top of each printed page.
     */
    public static final String BUILTIN_PRINT_TITLE = "_xlnm.Print_Titles";

    /**
     * A built-in defined name that refers to a range containing the criteria values
     * to be used in applying an advanced filter to a range of data
     */
    public static final String BUILTIN_CRITERIA = "_xlnm.Criteria:";


    /**
     * this defined name refers to the range containing the filtered
     * output values resulting from applying an advanced filter criteria to a source
     * range
     */
    public static final String BUILTIN_EXTRACT = "_xlnm.Extract:";

    /**
     * ?an be one of the following
     * <li> this defined name refers to a range to which an advanced filter has been
     * applied. This represents the source data range, unfiltered.
     * <li> This defined name refers to a range to which an AutoFilter has been
     * applied
     */
    public static final String BUILTIN_FILTER_DB = "_xlnm._FilterDatabase";

    /**
     * A built-in defined name that refers to a consolidation area
     */
    public static final String BUILTIN_CONSOLIDATE_AREA = "_xlnm.Consolidate_Area";

    /**
     * A built-in defined name that specified that the range specified is from a database data source
     */
    public static final String BUILTIN_DATABASE = "_xlnm.Database";

    /**
     * A built-in defined name that refers to a sheet title.
     */
    public static final String BUILTIN_SHEET_TITLE = "_xlnm.Sheet_Title";

    private XSSFWorkbook _workbook;
    private CTDefinedName _ctName;

    /**
     * Creates an XSSFName object - called internally by XSSFWorkbook.
     *
     * @param name - the xml bean that holds data represenring this defined name.
     * @param workbook - the workbook object associated with the name
     * @see org.apache.poi.xssf.usermodel.XSSFWorkbook#createName()
     */
    protected XSSFName(CTDefinedName name, XSSFWorkbook workbook) {
        _workbook = workbook;
        _ctName = name;
    }

    /**
     * Returns the underlying named range object
     */
    protected CTDefinedName getCTName() {
        return _ctName;
    }

    /**
     * Returns the name that will appear in the user interface for the defined name.
     *
     * @return text name of this defined name
     */
    public String getNameName() {
        return _ctName.getName();
    }

    /**
     * Sets the name that will appear in the user interface for the defined name.
     * Names must begin with a letter or underscore, not contain spaces and be unique across the workbook.
     *
     * <p>
     * A name must always be unique within its scope. POI prevents you from defining a name that is not unique
     * within its scope. However you can use the same name in different scopes. Example:
     * <pre><blockquote>
     * //by default names are workbook-global
     * XSSFName name;
     * name = workbook.createName();
     * name.setNameName("sales_08");
     *
     * name = workbook.createName();
     * name.setNameName("sales_08"); //will throw an exception: "The workbook already contains this name (case-insensitive)"
     *
     * //create sheet-level name
     * name = workbook.createName();
     * name.setSheetIndex(0); //the scope of the name is the first sheet
     * name.setNameName("sales_08");  //ok
     *
     * name = workbook.createName();
     * name.setSheetIndex(0);
     * name.setNameName("sales_08");  //will throw an exception: "The sheet already contains this name (case-insensitive)"
     *
     * </blockquote></pre>
    * </p>
     * @param name name of this defined name
     * @throws IllegalArgumentException if the name is invalid or the workbook already contains this name (case-insensitive)
     */
    public void setNameName(String name) {
        validateName(name);

        String oldName = getNameName();
        int sheetIndex = getSheetIndex();
        //Check to ensure no other names have the same case-insensitive name at the same scope
        for (XSSFName foundName : _workbook.getNames(name)) {
            if (foundName.getSheetIndex() == sheetIndex && foundName != this) {
                String msg = "The "+(sheetIndex == -1 ? "workbook" : "sheet")+" already contains this name: " + name;
                throw new IllegalArgumentException(msg);
            }
        }
        _ctName.setName(name);
        //Need to update the name -> named ranges map
        _workbook.updateName(this, oldName);
    }

    public String getRefersToFormula() {
        String result = _ctName.getStringValue();
        if (result == null || result.length() < 1) {
            return null;
        }
        return result;
    }

    public void setRefersToFormula(String formulaText) {
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(_workbook);
        //validate through the FormulaParser
        FormulaParser.parse(formulaText, fpb, FormulaType.NAMEDRANGE, getSheetIndex(), -1);

        _ctName.setStringValue(formulaText);
    }

    public boolean isDeleted(){
        String formulaText = getRefersToFormula();
        if (formulaText == null) {
            return false;
        }
        XSSFEvaluationWorkbook fpb = XSSFEvaluationWorkbook.create(_workbook);
        Ptg[] ptgs = FormulaParser.parse(formulaText, fpb, FormulaType.NAMEDRANGE, getSheetIndex(), -1);
        return Ptg.doesFormulaReferToDeletedCell(ptgs);
    }

    /**
     * Tell Excel that this name applies to the worksheet with the specified index instead of the entire workbook.
     *
     * @param index the sheet index this name applies to, -1 unsets this property making the name workbook-global
     */
    public void setSheetIndex(int index) {
        int lastSheetIx = _workbook.getNumberOfSheets() - 1;
        if (index < -1 || index > lastSheetIx) {
            throw new IllegalArgumentException("Sheet index (" + index +") is out of range" +
                    (lastSheetIx == -1 ? "" : (" (0.." +    lastSheetIx + ")")));
        }

        if(index == -1) {
            if(_ctName.isSetLocalSheetId()) _ctName.unsetLocalSheetId();
        } else {
            _ctName.setLocalSheetId(index);
        }
    }

    /**
     * Returns the sheet index this name applies to.
     *
     * @return the sheet index this name applies to, -1 if this name applies to the entire workbook
     */
    public int getSheetIndex() {
        return _ctName.isSetLocalSheetId() ? (int) _ctName.getLocalSheetId() : -1;
    }

    /**
     * Indicates that the defined name refers to a user-defined function.
     * This attribute is used when there is an add-in or other code project associated with the file.
     *
     * @param value <code>true</code> indicates the name refers to a function.
     */
    public void setFunction(boolean value) {
        _ctName.setFunction(value);
    }

    /**
     * Indicates that the defined name refers to a user-defined function.
     * This attribute is used when there is an add-in or other code project associated with the file.
     *
     * @return <code>true</code> indicates the name refers to a function.
     */
    public boolean getFunction() {
        return _ctName.getFunction();
    }

    /**
     * Specifies the function group index if the defined name refers to a function. The function
     * group defines the general category for the function. This attribute is used when there is
     * an add-in or other code project associated with the file.
     *
     * @param functionGroupId the function group index that defines the general category for the function
     */
    public void setFunctionGroupId(int functionGroupId) {
        _ctName.setFunctionGroupId(functionGroupId);
    }

    /**
     * Returns the function group index if the defined name refers to a function. The function
     * group defines the general category for the function. This attribute is used when there is
     * an add-in or other code project associated with the file.
     *
     * @return the function group index that defines the general category for the function
     */
    public int getFunctionGroupId() {
        return (int) _ctName.getFunctionGroupId();
    }

    /**
     * Get the sheets name which this named range is referenced to
     *
     * @return sheet name, which this named range referred to.
     * Empty string if the referenced sheet name weas not found.
     */
    public String getSheetName() {
        if (_ctName.isSetLocalSheetId()) {
            // Given as explicit sheet id
            int sheetId = (int)_ctName.getLocalSheetId();
            return _workbook.getSheetName(sheetId);
        }
        String ref = getRefersToFormula();
        AreaReference areaRef = new AreaReference(ref, SpreadsheetVersion.EXCEL2007);
        return areaRef.getFirstCell().getSheetName();
    }

    /**
     * Is the name refers to a user-defined function ?
     *
     * @return <code>true</code> if this name refers to a user-defined function
     */
    public boolean isFunctionName() {
        return getFunction();
    }

    /**
     * Returns the comment the user provided when the name was created.
     *
     * @return the user comment for this named range
     */
    public String getComment() {
        return _ctName.getComment();
    }

    /**
     * Specifies the comment the user provided when the name was created.
     *
     * @param comment  the user comment for this named range
     */
    public void setComment(String comment) {
        _ctName.setComment(comment);
    }

    @Override
    public int hashCode() {
        return _ctName.toString().hashCode();
    }

    /**
     * Compares this name to the specified object.
     * The result is <code>true</code> if the argument is XSSFName and the
     * underlying CTDefinedName bean equals to the CTDefinedName representing this name
     *
     * @param   o   the object to compare this <code>XSSFName</code> against.
     * @return  <code>true</code> if the <code>XSSFName </code>are equal;
     *          <code>false</code> otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if(o == this) return true;

        if (!(o instanceof XSSFName)) return false;

        XSSFName cf = (XSSFName) o;
        return _ctName.toString().equals(cf.getCTName().toString());
    }
    
    /**
     * https://support.office.com/en-us/article/Define-and-use-names-in-formulas-4D0F13AC-53B7-422E-AFD2-ABD7FF379C64#bmsyntax_rules_for_names
     * 
     * Valid characters:
     *   First character: { letter | underscore | backslash }
     *   Remaining characters: { letter | number | period | underscore }
     *   
     * Cell shorthand: cannot be { "C" | "c" | "R" | "r" }
     * 
     * Cell references disallowed: cannot be a cell reference $A$1 or R1C1
     * 
     * Spaces are not valid (follows from valid characters above)
     * 
     * Name length: (XSSF-specific?) 255 characters maximum
     * 
     * Case sensitivity: all names are case-insensitive
     * 
     * Uniqueness: must be unique (for names with the same scope)
     *
     * @param name
     */
    private static void validateName(String name) {
        
        if (name.length() == 0) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("Invalid name: '"+name+"': cannot exceed 255 characters in length");
        }
        if (name.equalsIgnoreCase("R") || name.equalsIgnoreCase("C")) {
            throw new IllegalArgumentException("Invalid name: '"+name+"': cannot be special shorthand R or C");
        }
        
        // is first character valid?
        char c = name.charAt(0);
        String allowedSymbols = "_\\";
        boolean characterIsValid = (Character.isLetter(c) || allowedSymbols.indexOf(c) != -1);
        if (!characterIsValid) {
            throw new IllegalArgumentException("Invalid name: '"+name+"': first character must be underscore or a letter");
        }
        
        // are all other characters valid?
        allowedSymbols = "_.\\"; //backslashes needed for unicode escape
        for (final char ch : name.toCharArray()) {
            characterIsValid = (Character.isLetterOrDigit(ch) || allowedSymbols.indexOf(ch) != -1);
            if (!characterIsValid) {
                throw new IllegalArgumentException("Invalid name: '"+name+"': name must be letter, digit, period, or underscore");
            }
        }
        
        // Is the name a valid $A$1 cell reference
        // Because $, :, and ! are disallowed characters, A1-style references become just a letter-number combination
        if (name.matches("[A-Za-z]+\\d+")) {
            String col = name.replaceAll("\\d", "");
            String row = name.replaceAll("[A-Za-z]", "");
            
            try {
                if (CellReference.cellReferenceIsWithinRange(col, row, SpreadsheetVersion.EXCEL2007)) {
                    throw new IllegalArgumentException("Invalid name: '"+name+"': cannot be $A$1-style cell reference");
                }
            } catch (final NumberFormatException e) {
                // row was not parseable as an Integer, such as a BigInt
                // therefore name passes the not-a-cell-reference criteria
            }
        }
        
        // Is the name a valid R1C1 cell reference?
        if (name.matches("[Rr]\\d+[Cc]\\d+")) {
            throw new IllegalArgumentException("Invalid name: '"+name+"': cannot be R1C1-style cell reference");
        }
    }
}
