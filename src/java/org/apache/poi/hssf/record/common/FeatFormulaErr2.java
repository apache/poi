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
 *  of the Excel binary file format documentation.
 */
public final class FeatFormulaErr2 implements SharedFeature {
	static BitField checkCalculationErrors =
		BitFieldFactory.getInstance(0x01);
	static BitField checkEmptyCellRef =
		BitFieldFactory.getInstance(0x02);
	static BitField checkNumbersAsText =
		BitFieldFactory.getInstance(0x04);
	static BitField checkInconsistentRanges =
		BitFieldFactory.getInstance(0x08);
	static BitField checkInconsistentFormulas =
		BitFieldFactory.getInstance(0x10);
	static BitField checkDateTimeFormats =
		BitFieldFactory.getInstance(0x20);
	static BitField checkUnprotectedFormulas =
		BitFieldFactory.getInstance(0x40);
	static BitField performDataValidation =
		BitFieldFactory.getInstance(0x80);
	
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
		return checkCalculationErrors.isSet(errorCheck);
	}
	public void setCheckCalculationErrors(boolean checkCalculationErrors) {
		FeatFormulaErr2.checkCalculationErrors.setBoolean(
				errorCheck, checkCalculationErrors);
	}

	public boolean getCheckEmptyCellRef() {
		return checkEmptyCellRef.isSet(errorCheck);
	}
	public void setCheckEmptyCellRef(boolean checkEmptyCellRef) {
		FeatFormulaErr2.checkEmptyCellRef.setBoolean(
				errorCheck, checkEmptyCellRef);
	}

	public boolean getCheckNumbersAsText() {
		return checkNumbersAsText.isSet(errorCheck);
	}
	public void setCheckNumbersAsText(boolean checkNumbersAsText) {
		FeatFormulaErr2.checkNumbersAsText.setBoolean(
				errorCheck, checkNumbersAsText);
	}

	public boolean getCheckInconsistentRanges() {
		return checkInconsistentRanges.isSet(errorCheck);
	}
	public void setCheckInconsistentRanges(boolean checkInconsistentRanges) {
		FeatFormulaErr2.checkInconsistentRanges.setBoolean(
				errorCheck, checkInconsistentRanges);
	}

	public boolean getCheckInconsistentFormulas() {
		return checkInconsistentFormulas.isSet(errorCheck);
	}
	public void setCheckInconsistentFormulas(
			boolean checkInconsistentFormulas) {
		FeatFormulaErr2.checkInconsistentFormulas.setBoolean(
				errorCheck, checkInconsistentFormulas);
	}

	public boolean getCheckDateTimeFormats() {
		return checkDateTimeFormats.isSet(errorCheck);
	}
	public void setCheckDateTimeFormats(boolean checkDateTimeFormats) {
		FeatFormulaErr2.checkDateTimeFormats.setBoolean(
				errorCheck, checkDateTimeFormats);
	}

	public boolean getCheckUnprotectedFormulas() {
		return checkUnprotectedFormulas.isSet(errorCheck);
	}
	public void setCheckUnprotectedFormulas(boolean checkUnprotectedFormulas) {
		FeatFormulaErr2.checkUnprotectedFormulas.setBoolean(
				errorCheck, checkUnprotectedFormulas);
	}

	public boolean getPerformDataValidation() {
		return performDataValidation.isSet(errorCheck);
	}
	public void setPerformDataValidation(boolean performDataValidation) {
		FeatFormulaErr2.performDataValidation.setBoolean(
				errorCheck, performDataValidation);
	}
}
