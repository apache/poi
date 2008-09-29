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
import org.apache.poi.hssf.record.StringRecord;

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

    /**
     * @param stringRec may be <code>null</code> if this formula does not have a cached text
     * value.
     * @param svm the {@link SharedValueManager} for the current sheet
     */
    public FormulaRecordAggregate(FormulaRecord formulaRec, StringRecord stringRec, SharedValueManager svm) {
        if (svm == null) {
            throw new IllegalArgumentException("sfm must not be null");
        }
        boolean hasStringRec = stringRec != null;
        boolean hasCachedStringFlag = formulaRec.hasCachedResultString();
        if (hasStringRec != hasCachedStringFlag) {
            throw new RecordFormatException("String record was "
                    + (hasStringRec ? "": "not ") + " supplied but formula record flag is "
                    + (hasCachedStringFlag ? "" : "not ") + " set");
        }

        if (formulaRec.isSharedFormula()) {
            svm.convertSharedFormulaRecord(formulaRec);
        }
        _formulaRecord = formulaRec;
        _sharedValueManager = svm;
        _stringRecord = stringRec;
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
         Record sharedFormulaRecord = _sharedValueManager.getRecordForFirstCell(_formulaRecord);
         if (sharedFormulaRecord != null) {
             rv.visitRecord(sharedFormulaRecord);
         }
         if (_stringRecord != null) {
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
}
