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

package org.apache.poi.hssf.record.aggregates;

import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.FormulaRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.RecordFormatException;
import org.apache.poi.hssf.record.SharedFormulaRecord;
import org.apache.poi.hssf.record.StringRecord;
import org.apache.poi.hssf.record.formula.ExpPtg;
import org.apache.poi.hssf.record.formula.Ptg;
import org.apache.poi.hssf.util.CellReference;

/**
 * The formula record aggregate is used to join together the formula record and it's
 * (optional) string record and (optional) Shared Formula Record (template reads, excel optimization).
 *
 * @author Glen Stampoultzis (glens at apache.org)
 */
public final class FormulaRecordAggregate extends RecordAggregate implements CellValueRecordInterface {

	private final FormulaRecord _formulaRecord;
	private SharedValueManager _sharedValueManager;
	/** caches the calculated result of the formula */
	private StringRecord _stringRecord;
	private SharedFormulaRecord _sharedFormulaRecord;

	/**
	 * @param stringRec may be <code>null</code> if this formula does not have a cached text
	 * value.
	 * @param svm the {@link SharedValueManager} for the current sheet
	 */
	public FormulaRecordAggregate(FormulaRecord formulaRec, StringRecord stringRec, SharedValueManager svm) {
		if (svm == null) {
			throw new IllegalArgumentException("sfm must not be null");
		}
		if (formulaRec.hasCachedResultString()) {
			if (stringRec == null) {
				throw new RecordFormatException("Formula record flag is set but String record was not found");
			}
			_stringRecord = stringRec;
		} else {
			// Usually stringRec is null here (in agreement with what the formula rec says).
			// In the case where an extra StringRecord is erroneously present, Excel (2007)
			// ignores it (see bug 46213).
			_stringRecord = null;
		}

		_formulaRecord = formulaRec;
		_sharedValueManager = svm;
		if (formulaRec.isSharedFormula()) {
			CellReference firstCell = formulaRec.getFormula().getExpReference();
			if (firstCell == null) {
				handleMissingSharedFormulaRecord(formulaRec);
			} else {
				_sharedFormulaRecord = svm.linkSharedFormulaRecord(firstCell, this);
			}
		}
	}
	/**
	 * Sometimes the shared formula flag "seems" to be erroneously set (because the corresponding
	 * {@link SharedFormulaRecord} does not exist). Normally this would leave no way of determining
	 * the {@link Ptg} tokens for the formula.  However as it turns out in these
	 * cases, Excel encodes the unshared {@link Ptg} tokens in the right place (inside the {@link
	 * FormulaRecord}).  So the the only thing that needs to be done is to ignore the erroneous
	 * shared formula flag.<br/>
	 *
	 * This method may also be used for setting breakpoints to help diagnose issues regarding the
	 * abnormally-set 'shared formula' flags.
	 * (see TestValueRecordsAggregate.testSpuriousSharedFormulaFlag()).<p/>
	 */
	private static void handleMissingSharedFormulaRecord(FormulaRecord formula) {
		// make sure 'unshared' formula is actually available
		Ptg firstToken = formula.getParsedExpression()[0];
		if (firstToken instanceof ExpPtg) {
			throw new RecordFormatException(
					"SharedFormulaRecord not found for FormulaRecord with (isSharedFormula=true)");
		}
		// could log an info message here since this is a fairly unusual occurrence.
		formula.setSharedFormula(false); // no point leaving the flag erroneously set
	}

	public FormulaRecord getFormulaRecord() {
		return _formulaRecord;
	}

	/**
	 * debug only
	 * TODO - encapsulate
	 */
	public StringRecord getStringRecord() {
		return _stringRecord;
	}

	public short getXFIndex() {
		return _formulaRecord.getXFIndex();
	}

	public void setXFIndex(short xf) {
		_formulaRecord.setXFIndex(xf);
	}

	public void setColumn(short col) {
		_formulaRecord.setColumn(col);
	}

	public void setRow(int row) {
		_formulaRecord.setRow(row);
	}

	public short getColumn() {
		return _formulaRecord.getColumn();
	}

	public int getRow() {
		return _formulaRecord.getRow();
	}

	public String toString() {
		return _formulaRecord.toString();
	}

	public void visitContainedRecords(RecordVisitor rv) {
		 rv.visitRecord(_formulaRecord);
		 Record sharedFormulaRecord = _sharedValueManager.getRecordForFirstCell(this);
		 if (sharedFormulaRecord != null) {
			 rv.visitRecord(sharedFormulaRecord);
		 }
		 if (_formulaRecord.hasCachedResultString() && _stringRecord != null) {
			 rv.visitRecord(_stringRecord);
		 }
	}

	public String getStringValue() {
		if(_stringRecord==null) {
			return null;
		}
		return _stringRecord.getString();
	}

	public void setCachedStringResult(String value) {

		// Save the string into a String Record, creating one if required
		if(_stringRecord == null) {
			_stringRecord = new StringRecord();
		}
		_stringRecord.setString(value);
		if (value.length() < 1) {
			_formulaRecord.setCachedResultTypeEmptyString();
		} else {
			_formulaRecord.setCachedResultTypeString();
		}
	}
	public void setCachedBooleanResult(boolean value) {
		_stringRecord = null;
		_formulaRecord.setCachedResultBoolean(value);
	}
	public void setCachedErrorResult(int errorCode) {
		_stringRecord = null;
		_formulaRecord.setCachedResultErrorCode(errorCode);
	}
	public void setCachedDoubleResult(double value) {
		_stringRecord = null;
		_formulaRecord.setValue(value);
	}

	public Ptg[] getFormulaTokens() {
		if (_sharedFormulaRecord == null) {
			return _formulaRecord.getParsedExpression();
		}
		return _sharedFormulaRecord.getFormulaTokens(_formulaRecord);
	}

	/**
	 * Also checks for a related shared formula and unlinks it if found
	 */
	public void setParsedExpression(Ptg[] ptgs) {
		notifyFormulaChanging();
		_formulaRecord.setParsedExpression(ptgs);
	}

	public void unlinkSharedFormula() {
		SharedFormulaRecord sfr = _sharedFormulaRecord;
		if (sfr == null) {
			throw new IllegalStateException("Formula not linked to shared formula");
		}
		Ptg[] ptgs = sfr.getFormulaTokens(_formulaRecord);
		_formulaRecord.setParsedExpression(ptgs);
		//Now its not shared!
		_formulaRecord.setSharedFormula(false);
		_sharedFormulaRecord = null;
	}
	/**
	 * Should be called by any code which is either deleting this formula cell, or changing
	 * its type.  This method gives the aggregate a chance to unlink any shared formula
	 * that may be involved with this cell formula.
	 */
	public void notifyFormulaChanging() {
		if (_sharedFormulaRecord != null) {
			_sharedValueManager.unlink(_sharedFormulaRecord);
		}
	}
}
