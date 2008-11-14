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
import org.apache.poi.hssf.model.Workbook;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.ss.usermodel.Name;

/**
 * High Level Representation of a 'defined name' which could be a 'built-in' name,
 * 'named range' or name of a user defined function.
 *
 * @author Libin Roman (Vista Portal LDT. Developer)
 */
public final class HSSFName implements Name {
    private HSSFWorkbook _book;
    private NameRecord _definedNameRec;

    /** Creates new HSSFName   - called by HSSFWorkbook to create a sheet from
     * scratch.
     *
     * @see org.apache.poi.hssf.usermodel.HSSFWorkbook#createName()
     * @param name the Name Record
     * @param book workbook object associated with the sheet.
     */
    /* package */ HSSFName(HSSFWorkbook book, NameRecord name) {
        _book = book;
        _definedNameRec = name;
    }

    /** Get the sheets name which this named range is referenced to
     * @return sheet name, which this named range referred to
     */
    public String getSheetName() {
        int indexToExternSheet = _definedNameRec.getExternSheetNumber();

        return _book.getWorkbook().findSheetNameFromExternSheet(indexToExternSheet);
    }

    /**
     * @return text name of this defined name
     */
    public String getNameName(){
        return _definedNameRec.getNameText();
    }

    /**
     * sets the name of the named range
     * @param nameName named range name to set
     */
    public void setNameName(String nameName){
        _definedNameRec.setNameText(nameName);
        Workbook wb = _book.getWorkbook();

        //Check to ensure no other names have the same case-insensitive name
        for ( int i = wb.getNumNames()-1; i >=0; i-- )
        {
            NameRecord rec = wb.getNameRecord(i);
            if (rec != _definedNameRec) {
                if (rec.getNameText().equalsIgnoreCase(getNameName()))
                    throw new IllegalArgumentException("The workbook already contains this name (case-insensitive)");
            }
        }
    }

    /**
     * @deprecated (Nov 2008) Misleading name. Use {@link #getFormula()} instead.
     */
    public String getReference() {
        return getFormula();
    }

    /**
     * @deprecated (Nov 2008) Misleading name. Use {@link #setFormula(String)} instead.
     */
    public void setReference(String ref){
    	setFormula(ref);
    }

    public void setFormula(String formulaText) {
		Ptg[] ptgs = HSSFFormulaParser.parse(formulaText, _book);
    	_definedNameRec.setNameDefinition(ptgs);
	}

    /**
     * Note - this method only applies to named ranges
     * @return the formula text defining this name
     */
    public String getFormula() {
        if (_definedNameRec.isFunctionName()) {
            throw new IllegalStateException("Only applicable to named ranges");
        }
    	return HSSFFormulaParser.toFormulaString(_book, _definedNameRec.getNameDefinition());
    }

    /**
     * Tests if this name points to a cell that no longer exists
     *
     * @return true if the name refers to a deleted cell, false otherwise
     */
    public boolean isDeleted(){
        String formulaText = getReference();
        if (formulaText.startsWith("#REF!")) {
        	// sheet deleted
        	return true;
        }
        if (formulaText.endsWith("#REF!")) {
        	// cell range deleted
        	return true;
        }
        return false;
    }
    public boolean isFunctionName() {
        return _definedNameRec.isFunctionName();
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(64);
        sb.append(getClass().getName()).append(" [");
        sb.append(_definedNameRec.getNameText());
        sb.append("]");
        return sb.toString();
    }
}
