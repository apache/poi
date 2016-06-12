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

package org.apache.poi.hssf.record.common;

import org.apache.poi.hssf.record.FeatRecord;
//import org.apache.poi.hssf.record.Feat11Record;
//import org.apache.poi.hssf.record.Feat12Record;
import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: FeatFormulaErr2 (Formula Evaluation Shared Feature) common record part
 * <P>
 * This record part specifies Formula Evaluation & Error Ignoring data 
 *  for a sheet, stored as part of a Shared Feature. It can be found in 
 *  records such as {@link FeatRecord}.
 * For the full meanings of the flags, see pages 669 and 670
 *  of the Excel binary file format documentation and/or
 *  https://msdn.microsoft.com/en-us/library/dd924991%28v=office.12%29.aspx
 */
public final class FeatFormulaErr2 implements SharedFeature {
	private static final BitField CHECK_CALCULATION_ERRORS =    BitFieldFactory.getInstance(0x01);
	private static final BitField CHECK_EMPTY_CELL_REF =        BitFieldFactory.getInstance(0x02);
	private static final BitField CHECK_NUMBERS_AS_TEXT =       BitFieldFactory.getInstance(0x04);
	private static final BitField CHECK_INCONSISTENT_RANGES =   BitFieldFactory.getInstance(0x08);
	private static final BitField CHECK_INCONSISTENT_FORMULAS = BitFieldFactory.getInstance(0x10);
	private static final BitField CHECK_DATETIME_FORMATS =      BitFieldFactory.getInstance(0x20);
	private static final BitField CHECK_UNPROTECTED_FORMULAS =  BitFieldFactory.getInstance(0x40);
	private static final BitField PERFORM_DATA_VALIDATION =     BitFieldFactory.getInstance(0x80);
	
	/**
	 * What errors we should ignore
	 */
	private int errorCheck;
	
	
	public FeatFormulaErr2() {}

	public FeatFormulaErr2(RecordInputStream in) {
		errorCheck = in.readInt();
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(" [FEATURE FORMULA ERRORS]\n");
		buffer.append("  checkCalculationErrors    = "); 
		buffer.append("  checkEmptyCellRef         = "); 
		buffer.append("  checkNumbersAsText        = "); 
		buffer.append("  checkInconsistentRanges   = "); 
		buffer.append("  checkInconsistentFormulas = "); 
		buffer.append("  checkDateTimeFormats      = "); 
		buffer.append("  checkUnprotectedFormulas  = "); 
		buffer.append("  performDataValidation     = "); 
		buffer.append(" [/FEATURE FORMULA ERRORS]\n");
		return buffer.toString();
	}

	public void serialize(LittleEndianOutput out) {
		out.writeInt(errorCheck);
	}

	public int getDataSize() {
		return 4;
	}
	
	public int _getRawErrorCheckValue() {
		return errorCheck;
	}

	public boolean getCheckCalculationErrors() {
		return CHECK_CALCULATION_ERRORS.isSet(errorCheck);
	}
	public void setCheckCalculationErrors(boolean checkCalculationErrors) {
	    errorCheck = CHECK_CALCULATION_ERRORS.setBoolean(errorCheck, checkCalculationErrors);
	}

	public boolean getCheckEmptyCellRef() {
		return CHECK_EMPTY_CELL_REF.isSet(errorCheck);
	}
	public void setCheckEmptyCellRef(boolean checkEmptyCellRef) {
	    errorCheck = CHECK_EMPTY_CELL_REF.setBoolean(errorCheck, checkEmptyCellRef);
	}

	public boolean getCheckNumbersAsText() {
		return CHECK_NUMBERS_AS_TEXT.isSet(errorCheck);
	}
	public void setCheckNumbersAsText(boolean checkNumbersAsText) {
	    errorCheck = CHECK_NUMBERS_AS_TEXT.setBoolean(errorCheck, checkNumbersAsText);
	}

	public boolean getCheckInconsistentRanges() {
		return CHECK_INCONSISTENT_RANGES.isSet(errorCheck);
	}
	public void setCheckInconsistentRanges(boolean checkInconsistentRanges) {
	    errorCheck = CHECK_INCONSISTENT_RANGES.setBoolean(errorCheck, checkInconsistentRanges);
	}

	public boolean getCheckInconsistentFormulas() {
		return CHECK_INCONSISTENT_FORMULAS.isSet(errorCheck);
	}
	public void setCheckInconsistentFormulas(boolean checkInconsistentFormulas) {
	    errorCheck = CHECK_INCONSISTENT_FORMULAS.setBoolean(errorCheck, checkInconsistentFormulas);
	}

	public boolean getCheckDateTimeFormats() {
		return CHECK_DATETIME_FORMATS.isSet(errorCheck);
	}
	public void setCheckDateTimeFormats(boolean checkDateTimeFormats) {
	    errorCheck = CHECK_DATETIME_FORMATS.setBoolean(errorCheck, checkDateTimeFormats);
	}

	public boolean getCheckUnprotectedFormulas() {
		return CHECK_UNPROTECTED_FORMULAS.isSet(errorCheck);
	}
	public void setCheckUnprotectedFormulas(boolean checkUnprotectedFormulas) {
	    errorCheck = CHECK_UNPROTECTED_FORMULAS.setBoolean(errorCheck, checkUnprotectedFormulas);
	}

	public boolean getPerformDataValidation() {
		return PERFORM_DATA_VALIDATION.isSet(errorCheck);
	}
	public void setPerformDataValidation(boolean performDataValidation) {
	    errorCheck = PERFORM_DATA_VALIDATION.setBoolean(errorCheck, performDataValidation);
	}
}
