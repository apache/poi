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

import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.NameCommentRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.util.CellReference;

/**
 * High Level Representation of a 'defined name' which could be a 'built-in' name,
 * 'named range' or name of a user defined function.
 */
public final class HSSFName implements Name {
    
    private HSSFWorkbook _book;
    private NameRecord _definedNameRec;
    private NameCommentRecord _commentRec;

    /** 
     * Creates new HSSFName   - called by HSSFWorkbook to create a name from
     * scratch.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createName()
     * @param name the Name Record
     * @param book workbook object associated with the sheet.
     */
    /* package */ HSSFName(HSSFWorkbook book, NameRecord name) {
      this(book, name, null);
    }
    /** 
     * Creates new HSSFName   - called by HSSFWorkbook to create a name from
     * scratch.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createName()
     * @param name the Name Record
     * @param comment the Name Comment Record, optional.
     * @param book workbook object associated with the sheet.
     */
    /* package */ HSSFName(final HSSFWorkbook book, final NameRecord name, final NameCommentRecord comment) {
        _book = book;
        _definedNameRec = name;
        _commentRec = comment;
    }

    /** Get the sheets name which this named range is referenced to
     * @return sheet name, which this named range referred to
     */
    public String getSheetName() {
        int indexToExternSheet = _definedNameRec.getExternSheetNumber();

        return _book.getWorkbook().findSheetFirstNameFromExternSheet(indexToExternSheet);
    }

    /**
     * @return text name of this defined name
     */
    public String getNameName(){
        return _definedNameRec.getNameText();
    }

    /**
     * Sets the name of the named range
     *
     * <p>The following is a list of syntax rules that you need to be aware of when you create and edit names.</p>
     * <ul>
     *   <li><strong>Valid characters</strong>
     *   The first character of a name must be a letter, an underscore character (_), or a backslash (\).
     *   Remaining characters in the name can be letters, numbers, periods, and underscore characters.
     *   </li>
     *   <li><strong>Cell references disallowed</strong>
     *   Names cannot be the same as a cell reference, such as Z$100 or R1C1.</li>
     *   <li><strong>Spaces are not valid</strong>
     *   Spaces are not allowed as part of a name. Use the underscore character (_) and period (.) as word separators, such as, Sales_Tax or First.Quarter.
     *   </li>
     *   <li><strong>Name length</strong>
     *    A name can contain up to 255 characters.
     *   </li>
     *   <li><strong>Case sensitivity</strong>
     *   Names can contain uppercase and lowercase letters.
     *   </li>
     * </ul>
     *
     * <p>
     * A name must always be unique within its scope. POI prevents you from defining a name that is not unique
     * within its scope. However you can use the same name in different scopes. Example:
     * <pre><blockquote>
     * //by default names are workbook-global
     * HSSFName name;
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
     *
     * @param nameName named range name to set
     * @throws IllegalArgumentException if the name is invalid or the name already exists (case-insensitive)
     */
    public void setNameName(String nameName){
        validateName(nameName);

        InternalWorkbook wb = _book.getWorkbook();
        _definedNameRec.setNameText(nameName);

        int sheetNumber = _definedNameRec.getSheetNumber();

        //Check to ensure no other names have the same case-insensitive name
        final int lastNameIndex = wb.getNumNames()-1;
        for ( int i = lastNameIndex; i >=0; i-- )
        {
            NameRecord rec = wb.getNameRecord(i);
            if (rec != _definedNameRec) {
                if (rec.getNameText().equalsIgnoreCase(nameName) && sheetNumber == rec.getSheetNumber()){
                    String msg = "The "+(sheetNumber == 0 ? "workbook" : "sheet")+" already contains this name: " + nameName;
                    _definedNameRec.setNameText(nameName + "(2)");
                    throw new IllegalArgumentException(msg);
                }
            }
        }
        
        // Update our comment, if there is one
        if(_commentRec != null) {
           _commentRec.setNameText(nameName);
           _book.getWorkbook().updateNameCommentRecordCache(_commentRec);
        }
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
            if (CellReference.cellReferenceIsWithinRange(col, row, SpreadsheetVersion.EXCEL97)) {
                throw new IllegalArgumentException("Invalid name: '"+name+"': cannot be $A$1-style cell reference");
            }
        }
        
        // Is the name a valid R1C1 cell reference?
        if (name.matches("[Rr]\\d+[Cc]\\d+")) {
            throw new IllegalArgumentException("Invalid name: '"+name+"': cannot be R1C1-style cell reference");
        }
    }

    public void setRefersToFormula(String formulaText) {
        Ptg[] ptgs = HSSFFormulaParser.parse(formulaText, _book, FormulaType.NAMEDRANGE, getSheetIndex());
        _definedNameRec.setNameDefinition(ptgs);
    }

    public String getRefersToFormula() {
        if (_definedNameRec.isFunctionName()) {
            throw new IllegalStateException("Only applicable to named ranges");
        }
        Ptg[] ptgs = _definedNameRec.getNameDefinition();
        if (ptgs.length < 1) {
            // 'refersToFormula' has not been set yet
            return null;
        }
        return HSSFFormulaParser.toFormulaString(_book, ptgs);
    }


    /**
     * Sets the NameParsedFormula structure that specifies the formula for the 
     * defined name.
     * 
     * @param ptgs the sequence of {@link Ptg}s for the formula.
     */
    void setNameDefinition(Ptg[] ptgs) {
      _definedNameRec.setNameDefinition(ptgs);
    }


    public boolean isDeleted(){
        Ptg[] ptgs = _definedNameRec.getNameDefinition();
        return Ptg.doesFormulaReferToDeletedCell(ptgs);
    }

    /**
     * Checks if this name is a function name
     *
     * @return true if this name is a function name
     */
    public boolean isFunctionName() {
        return _definedNameRec.isFunctionName();
    }

    public String toString() {
        return getClass().getName() + " [" +
                _definedNameRec.getNameText() +
                "]";
    }

    /**
     * Specifies if the defined name is a local name, and if so, which sheet it is on.
     *
     * @param index if greater than 0, the defined name is a local name and the value MUST be a 0-based index
     * to the collection of sheets as they appear in the workbook.
     * @throws IllegalArgumentException if the sheet index is invalid.
     */
    public void setSheetIndex(int index){
        int lastSheetIx = _book.getNumberOfSheets() - 1;
        if (index < -1 || index > lastSheetIx) {
            throw new IllegalArgumentException("Sheet index (" + index +") is out of range" +
                    (lastSheetIx == -1 ? "" : (" (0.." +    lastSheetIx + ")")));
        }

        _definedNameRec.setSheetNumber(index + 1);
    }

    /**
     * Returns the sheet index this name applies to.
     *
     * @return the sheet index this name applies to, -1 if this name applies to the entire workbook
     */
    public int getSheetIndex(){
        return _definedNameRec.getSheetNumber() - 1;
    }

    /**
     * Returns the comment the user provided when the name was created.
     *
     * @return the user comment for this named range
     */
    public String getComment() {
        if(_commentRec != null) {
           // Prefer the comment record if it has text in it
           if(_commentRec.getCommentText() != null &&
                 _commentRec.getCommentText().length() > 0) {
              return _commentRec.getCommentText();
           }
        }
        return _definedNameRec.getDescriptionText();
    }

    /**
     * Sets the comment the user provided when the name was created.
     *
     * @param comment the user comment for this named range
     */
    public void setComment(String comment){
        // Update the main record
        _definedNameRec.setDescriptionText(comment);
        // If we have a comment record too, update that as well
        if(_commentRec != null) {
           _commentRec.setCommentText(comment);
        }
    }

    /**
     * Indicates that the defined name refers to a user-defined function.
     * This attribute is used when there is an add-in or other code project associated with the file.
     *
     * @param value <code>true</code> indicates the name refers to a function.
     */
    public void setFunction(boolean value) {
        _definedNameRec.setFunction(value);
    }
}
