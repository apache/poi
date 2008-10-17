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

import org.apache.poi.ss.usermodel.Name;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTDefinedName;

/**
 * XSSF Implementation of a Named Range
 */
public class XSSFName implements Name {
    /**
     * The following built-in names are defined in this SpreadsheetML
     * specification:
     * Built-in names reserved by SpreadsheetML begin with "_xlnm.". End users shall not use
     * this string for custom names in the user interface.
     */

    /**
     * this defined name specifies the workbook's print area
     */
    public final static String  BUILTIN_PRINT_AREA            = "_xlnm.Print_Area";

    /**
     * this defined name specifies the row(s) or column(s) to repeat
     *	at the top of each printed page.
     */
    public final static String  BUILTIN_PRINT_TITLE           = "_xlnm.Print_Titles";

    //Filter & Advanced Filter

    /**
     * this defined name refers to a range containing the criteria values
     * to be used in applying an advanced filter to a range of data
     */
    public final static String  BUILTIN_CRITERIA              = "_xlnm.Criteria:";


    /**
     * this defined name refers to the range containing the filtered
     * output values resulting from applying an advanced filter criteria to a source
     * range
     */
    public final static String  BUILTIN_EXTRACT              = "_xlnm.Extract:";

    /**
     * can be one of the following
     * a. this defined name refers to a range to which an advanced filter has been
     * applied. This represents the source data range, unfiltered.
     * b. This defined name refers to a range to which an AutoFilter has been
     * applied
     */
    public final static String  BUILTIN_FILTER_DB             = "_xlnm._FilterDatabase:";


    //Miscellaneous

    /**
     * the defined name refers to a consolidation area
     */
    public final static String  BUILTIN_CONSOLIDATE_AREA      = "_xlnm.Consolidate_Area";

    /**
     * the range specified in the defined name is from a database data source
     */
    public final static String  BUILTIN_DATABASE              = "_xlnm.Database";

    /**
     * the defined name refers to a sheet title.
     */
    public final static String  BUILTIN_SHEET_TITLE           = "_xlnm.Sheet_Title";

    private XSSFWorkbook workbook;
    private CTDefinedName ctName;

    protected XSSFName(XSSFWorkbook workbook) {
	this.workbook = workbook;
	this.ctName = CTDefinedName.Factory.newInstance();
    }
    protected XSSFName(CTDefinedName name, XSSFWorkbook workbook) {
	this.workbook = workbook;
	this.ctName = name;
    }

    public boolean isFunctionName() {
	// TODO Figure out how HSSF does this, and do the same!
	return ctName.getFunction(); // maybe this works - verify
    }
    /**
     * Returns the underlying named range object
     */
    protected CTDefinedName getCTName() {
	return ctName;
    }

    public String getNameName() {
	return ctName.getName();
    }
    public void setNameName(String nameName) {
	ctName.setName(nameName);
    }

    public String getReference() {
	return ctName.getStringValue();
    }
    public void setReference(String ref) {
	ctName.setStringValue(ref);
    }

    public void setLocalSheetId(int sheetId) {
	ctName.setLocalSheetId(sheetId);
    }

    public int getLocalSheetId() {
	return (int)ctName.getLocalSheetId();
    }


    public void setFunction(boolean value) {
	ctName.setFunction(value);
    }

    public boolean getFunction() {
	return ctName.getFunction();
    }

    public void setFunctionGroupId(int functionGroupId) {
	ctName.setFunctionGroupId(functionGroupId);
    }

    public int getFunctionGroupId() {
	return (int)ctName.getFunctionGroupId();
    }

    public String getSheetName() {
	if(ctName.isSetLocalSheetId()) {
	    // Given as explicit sheet id
	    long sheetId = ctName.getLocalSheetId();
	    if(sheetId >= 0) {
		return workbook.getSheetName((int)sheetId);
	    }
	} else {
	    // Is it embeded in the reference itself?
	    int excl = getReference().indexOf('!');
	    if(excl > -1) {
		return getReference().substring(0, excl);
	    }
	}

	// Not given at all
	return null;
    }

    public String getComment() {
	return ctName.getComment();
    }
    public void setComment(String comment) {
	ctName.setComment(comment);
    }


    public int hashCode(){
	return ctName.toString().hashCode();
    }

    public boolean equals(Object o){
	if(!(o instanceof XSSFName)) return false;
	XSSFName cf = (XSSFName)o;
	return ctName.toString().equals(cf.getCTName().toString());
    }


}
