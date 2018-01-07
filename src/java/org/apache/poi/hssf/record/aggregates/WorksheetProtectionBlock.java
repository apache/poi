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

import org.apache.poi.hssf.model.RecordStream;
import org.apache.poi.hssf.record.ObjectProtectRecord;
import org.apache.poi.hssf.record.PasswordRecord;
import org.apache.poi.hssf.record.ProtectRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.ScenarioProtectRecord;
import org.apache.poi.poifs.crypt.CryptoFunctions;
import org.apache.poi.util.RecordFormatException;

/**
 * Groups the sheet protection records for a worksheet.
 * <p>
 *
 * See OOO excelfileformat.pdf sec 4.18.2 'Sheet Protection in a Workbook
 * (BIFF5-BIFF8)'
 *
 * @author Josh Micich
 */
public final class WorksheetProtectionBlock extends RecordAggregate {
	// Every one of these component records is optional
	// (The whole WorksheetProtectionBlock may not be present)
	private ProtectRecord _protectRecord;
	private ObjectProtectRecord _objectProtectRecord;
	private ScenarioProtectRecord _scenarioProtectRecord;
	private PasswordRecord _passwordRecord;

	/**
	 * Creates an empty WorksheetProtectionBlock
	 */
	public WorksheetProtectionBlock() {
		// all fields empty
	}

	/**
	 * @return <code>true</code> if the specified Record sid is one belonging to
	 *         the 'Page Settings Block'.
	 */
	public static boolean isComponentRecord(int sid) {
		switch (sid) {
			case ProtectRecord.sid:
			case ObjectProtectRecord.sid:
			case ScenarioProtectRecord.sid:
			case PasswordRecord.sid:
				return true;
		}
		return false;
	}

	private boolean readARecord(RecordStream rs) {
		switch (rs.peekNextSid()) {
			case ProtectRecord.sid:
				checkNotPresent(_protectRecord);
				_protectRecord = (ProtectRecord) rs.getNext();
				break;
			case ObjectProtectRecord.sid:
				checkNotPresent(_objectProtectRecord);
				_objectProtectRecord = (ObjectProtectRecord) rs.getNext();
				break;
			case ScenarioProtectRecord.sid:
				checkNotPresent(_scenarioProtectRecord);
				_scenarioProtectRecord = (ScenarioProtectRecord) rs.getNext();
				break;
			case PasswordRecord.sid:
				checkNotPresent(_passwordRecord);
				_passwordRecord = (PasswordRecord) rs.getNext();
				break;
			default:
				// all other record types are not part of the PageSettingsBlock
				return false;
		}
		return true;
	}

	private void checkNotPresent(Record rec) {
		if (rec != null) {
			throw new RecordFormatException("Duplicate PageSettingsBlock record (sid=0x"
					+ Integer.toHexString(rec.getSid()) + ")");
		}
	}

	public void visitContainedRecords(RecordVisitor rv) {
		// Replicates record order from Excel 2007, though this is not critical

		visitIfPresent(_protectRecord, rv);
		visitIfPresent(_objectProtectRecord, rv);
		visitIfPresent(_scenarioProtectRecord, rv);
		visitIfPresent(_passwordRecord, rv);
	}

	private static void visitIfPresent(Record r, RecordVisitor rv) {
		if (r != null) {
			rv.visitRecord(r);
		}
	}

	public PasswordRecord getPasswordRecord() {
		return _passwordRecord;
	}

	public ScenarioProtectRecord getHCenter() {
		return _scenarioProtectRecord;
	}

	/**
	 * This method reads {@link WorksheetProtectionBlock} records from the supplied RecordStream
	 * until the first non-WorksheetProtectionBlock record is encountered. As each record is read,
	 * it is incorporated into this WorksheetProtectionBlock.
	 * <p>
	 * As per the OOO documentation, the protection block records can be expected to be written
	 * together (with no intervening records), but earlier versions of POI (prior to Jun 2009)
	 * didn't do this.  Workbooks with sheet protection created by those earlier POI versions
	 * seemed to be valid (Excel opens them OK). So PO allows continues to support reading of files
	 * with non continuous worksheet protection blocks.
	 *
	 * <p>
	 * <b>Note</b> - when POI writes out this WorksheetProtectionBlock, the records will always be
	 * written in one consolidated block (in the standard ordering) regardless of how scattered the
	 * records were when they were originally read.
	 */
	public void addRecords(RecordStream rs) {
		while (true) {
			if (!readARecord(rs)) {
				break;
			}
		}
	}

	/**
	 * @return the ProtectRecord. If one is not contained in the sheet, then one
	 *         is created.
	 */
	private ProtectRecord getProtect() {
		if (_protectRecord == null) {
			_protectRecord = new ProtectRecord(false);
		}
		return _protectRecord;
	}

	/**
	 * @return the PasswordRecord. If one is not contained in the sheet, then
	 *         one is created.
	 */
	private PasswordRecord getPassword() {
		if (_passwordRecord == null) {
			_passwordRecord = createPassword();
		}
		return _passwordRecord;
	}

	/**
	 * protect a spreadsheet with a password (not encrypted, just sets protect
	 * flags and the password.
	 *
	 * @param password to set. Pass <code>null</code> to remove all protection
	 * @param shouldProtectObjects are protected
	 * @param shouldProtectScenarios are protected
	 */
	public void protectSheet(String password, boolean shouldProtectObjects,
			boolean shouldProtectScenarios) {
		if (password == null) {
			_passwordRecord = null;
			_protectRecord = null;
			_objectProtectRecord = null;
			_scenarioProtectRecord = null;
			return;
		}

		ProtectRecord prec = getProtect();
		PasswordRecord pass = getPassword();
		prec.setProtect(true);
		pass.setPassword((short)CryptoFunctions.createXorVerifier1(password));
		if (_objectProtectRecord == null && shouldProtectObjects) {
			ObjectProtectRecord rec = createObjectProtect();
			rec.setProtect(true);
			_objectProtectRecord = rec;
		}
		if (_scenarioProtectRecord == null && shouldProtectScenarios) {
			ScenarioProtectRecord srec = createScenarioProtect();
			srec.setProtect(true);
			_scenarioProtectRecord = srec;
		}
	}

	public boolean isSheetProtected() {
		return _protectRecord != null && _protectRecord.getProtect();
	}

	public boolean isObjectProtected() {
		return _objectProtectRecord != null && _objectProtectRecord.getProtect();
	}

	public boolean isScenarioProtected() {
		return _scenarioProtectRecord != null && _scenarioProtectRecord.getProtect();
	}

	/**
	 * creates an ObjectProtect record with protect set to false.
	 */
	private static ObjectProtectRecord createObjectProtect() {
		ObjectProtectRecord retval = new ObjectProtectRecord();
		retval.setProtect(false);
		return retval;
	}

	/**
	 * creates a ScenarioProtect record with protect set to false.
	 */
	private static ScenarioProtectRecord createScenarioProtect() {
		ScenarioProtectRecord retval = new ScenarioProtectRecord();
		retval.setProtect(false);
		return retval;
	}

	/**
	 * creates a Password record with password set to 0x0000.
	 */
	private static PasswordRecord createPassword() {
		return new PasswordRecord(0x0000);
	}

	public int getPasswordHash() {
		if (_passwordRecord == null) {
			return 0;
		}
		return _passwordRecord.getPassword();
	}
}
