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

package org.apache.poi.hssf.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.record.CRNCountRecord;
import org.apache.poi.hssf.record.CRNRecord;
import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.ExternalNameRecord;
import org.apache.poi.hssf.record.NameCommentRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SupBookRecord;
import org.apache.poi.hssf.record.formula.Area3DPtg;
import org.apache.poi.hssf.record.formula.NameXPtg;
import org.apache.poi.hssf.record.formula.Ref3DPtg;

/**
 * Link Table (OOO pdf reference: 4.10.3 ) <p/>
 *
 * The main data of all types of references is stored in the Link Table inside the Workbook Globals
 * Substream (4.2.5). The Link Table itself is optional and occurs only, if  there are any
 * references in the document.
 *  <p/>
 *
 *  In BIFF8 the Link Table consists of
 *  <ul>
 *  <li>zero or more EXTERNALBOOK Blocks<p/>
 *  	each consisting of
 *  	<ul>
 *  	<li>exactly one EXTERNALBOOK (0x01AE) record</li>
 *  	<li>zero or more EXTERNALNAME (0x0023) records</li>
 *  	<li>zero or more CRN Blocks<p/>
 *			each consisting of
 *  		<ul>
 *  		<li>exactly one XCT (0x0059)record</li>
 *  		<li>zero or more CRN (0x005A) records (documentation says one or more)</li>
 *  		</ul>
 *  	</li>
 *  	</ul>
 *  </li>
 *  <li>zero or one EXTERNSHEET (0x0017) record</li>
 *  <li>zero or more DEFINEDNAME (0x0018) records</li>
 *  </ul>
 *
 *
 * @author Josh Micich
 */
final class LinkTable {


	// TODO make this class into a record aggregate

	private static final class CRNBlock {

		private final CRNCountRecord _countRecord;
		private final CRNRecord[] _crns;

		public CRNBlock(RecordStream rs) {
			_countRecord = (CRNCountRecord) rs.getNext();
			int nCRNs = _countRecord.getNumberOfCRNs();
			CRNRecord[] crns = new CRNRecord[nCRNs];
			for (int i = 0; i < crns.length; i++) {
				crns[i] = (CRNRecord) rs.getNext();
			}
			_crns = crns;
		}
		public CRNRecord[] getCrns() {
			return _crns.clone();
		}
	}

	private static final class ExternalBookBlock {
		private final SupBookRecord _externalBookRecord;
		private final ExternalNameRecord[] _externalNameRecords;
		private final CRNBlock[] _crnBlocks;

		public ExternalBookBlock(RecordStream rs) {
			_externalBookRecord = (SupBookRecord) rs.getNext();
			List<Object> temp = new ArrayList<Object>();
			while(rs.peekNextClass() == ExternalNameRecord.class) {
			   temp.add(rs.getNext());
			}
			_externalNameRecords = new ExternalNameRecord[temp.size()];
			temp.toArray(_externalNameRecords);

			temp.clear();

			while(rs.peekNextClass() == CRNCountRecord.class) {
				temp.add(new CRNBlock(rs));
			}
			_crnBlocks = new CRNBlock[temp.size()];
			temp.toArray(_crnBlocks);
		}

		public ExternalBookBlock(int numberOfSheets) {
			_externalBookRecord = SupBookRecord.createInternalReferences((short)numberOfSheets);
			_externalNameRecords = new ExternalNameRecord[0];
			_crnBlocks = new CRNBlock[0];
		}

		public SupBookRecord getExternalBookRecord() {
			return _externalBookRecord;
		}

		public String getNameText(int definedNameIndex) {
			return _externalNameRecords[definedNameIndex].getText();
		}
		
		public int getNameIx(int definedNameIndex) {
		   return _externalNameRecords[definedNameIndex].getIx();
		}

		/**
		 * Performs case-insensitive search
		 * @return -1 if not found
		 */
		public int getIndexOfName(String name) {
			for (int i = 0; i < _externalNameRecords.length; i++) {
				if(_externalNameRecords[i].getText().equalsIgnoreCase(name)) {
					return i;
				}
			}
			return -1;
		}
	}

	private final ExternalBookBlock[] _externalBookBlocks;
	private final ExternSheetRecord _externSheetRecord;
	private final List<NameRecord> _definedNames;
	private final int _recordCount;
	private final WorkbookRecordList _workbookRecordList; // TODO - would be nice to remove this

	public LinkTable(List inputList, int startIndex, WorkbookRecordList workbookRecordList, Map<String, NameCommentRecord> commentRecords) {

		_workbookRecordList = workbookRecordList;
		RecordStream rs = new RecordStream(inputList, startIndex);

		List<ExternalBookBlock> temp = new ArrayList<ExternalBookBlock>();
		while(rs.peekNextClass() == SupBookRecord.class) {
		   temp.add(new ExternalBookBlock(rs));
		}

		_externalBookBlocks = new ExternalBookBlock[temp.size()];
		temp.toArray(_externalBookBlocks);
		temp.clear();

		if (_externalBookBlocks.length > 0) {
			// If any ExternalBookBlock present, there is always 1 of ExternSheetRecord
			if (rs.peekNextClass() != ExternSheetRecord.class) {
				// not quite - if written by google docs
				_externSheetRecord = null;
			} else {
				_externSheetRecord = readExtSheetRecord(rs);
			}
		} else {
			_externSheetRecord = null;
		}

		_definedNames = new ArrayList<NameRecord>();
		// collect zero or more DEFINEDNAMEs id=0x18,
		//  with their comments if present
		while(true) {
		  Class nextClass = rs.peekNextClass();
		  if (nextClass == NameRecord.class) {
		    NameRecord nr = (NameRecord)rs.getNext();
		    _definedNames.add(nr);
		  }
		  else if (nextClass == NameCommentRecord.class) {
		    NameCommentRecord ncr = (NameCommentRecord)rs.getNext();
		    commentRecords.put(ncr.getNameText(), ncr);
		  }
		  else {
		    break;
		  }
		}

		_recordCount = rs.getCountRead();
		_workbookRecordList.getRecords().addAll(inputList.subList(startIndex, startIndex + _recordCount));
	}

	private static ExternSheetRecord readExtSheetRecord(RecordStream rs) {
		List<ExternSheetRecord> temp = new ArrayList<ExternSheetRecord>(2);
		while(rs.peekNextClass() == ExternSheetRecord.class) {
			temp.add((ExternSheetRecord) rs.getNext());
		}

		int nItems = temp.size();
		if (nItems < 1) {
			throw new RuntimeException("Expected an EXTERNSHEET record but got ("
					+ rs.peekNextClass().getName() + ")");
		}
		if (nItems == 1) {
			// this is the normal case. There should be just one ExternSheetRecord
			return temp.get(0);
		}
		// Some apps generate multiple ExternSheetRecords (see bug 45698).
		// It seems like the best thing to do might be to combine these into one
		ExternSheetRecord[] esrs = new ExternSheetRecord[nItems];
		temp.toArray(esrs);
		return ExternSheetRecord.combine(esrs);
	}

	public LinkTable(int numberOfSheets, WorkbookRecordList workbookRecordList) {
		_workbookRecordList = workbookRecordList;
		_definedNames = new ArrayList<NameRecord>();
		_externalBookBlocks = new ExternalBookBlock[] {
				new ExternalBookBlock(numberOfSheets),
		};
		_externSheetRecord = new ExternSheetRecord();
		_recordCount = 2;

		// tell _workbookRecordList about the 2 new records

		SupBookRecord supbook = _externalBookBlocks[0].getExternalBookRecord();

		int idx = findFirstRecordLocBySid(CountryRecord.sid);
		if(idx < 0) {
			throw new RuntimeException("CountryRecord not found");
		}
		_workbookRecordList.add(idx+1, _externSheetRecord);
		_workbookRecordList.add(idx+1, supbook);
	}

	/**
	 * TODO - would not be required if calling code used RecordStream or similar
	 */
	public int getRecordCount() {
		return _recordCount;
	}


	/**
	 * @param builtInCode a BUILTIN_~ constant from {@link NameRecord}
	 * @param sheetNumber 1-based sheet number
	 */
	public NameRecord getSpecificBuiltinRecord(byte builtInCode, int sheetNumber) {

		Iterator iterator = _definedNames.iterator();
		while (iterator.hasNext()) {
			NameRecord record = ( NameRecord ) iterator.next();

			//print areas are one based
			if (record.getBuiltInName() == builtInCode && record.getSheetNumber() == sheetNumber) {
				return record;
			}
		}

		return null;
	}

	public void removeBuiltinRecord(byte name, int sheetIndex) {
		//the name array is smaller so searching through it should be faster than
		//using the findFirstXXXX methods
		NameRecord record = getSpecificBuiltinRecord(name, sheetIndex);
		if (record != null) {
			_definedNames.remove(record);
		}
		// TODO - do we need "Workbook.records.remove(...);" similar to that in Workbook.removeName(int namenum) {}?
	}

	public int getNumNames() {
		return _definedNames.size();
	}

	public NameRecord getNameRecord(int index) {
		return _definedNames.get(index);
	}

	public void addName(NameRecord name) {
		_definedNames.add(name);

		// TODO - this is messy
		// Not the most efficient way but the other way was causing too many bugs
		int idx = findFirstRecordLocBySid(ExternSheetRecord.sid);
		if (idx == -1) idx = findFirstRecordLocBySid(SupBookRecord.sid);
		if (idx == -1) idx = findFirstRecordLocBySid(CountryRecord.sid);
		int countNames = _definedNames.size();
		_workbookRecordList.add(idx+countNames, name);
	}

	public void removeName(int namenum) {
		_definedNames.remove(namenum);
	}

	/**
	 * checks if the given name is already included in the linkTable
	 */
	public boolean nameAlreadyExists(NameRecord name)
	{
		// Check to ensure no other names have the same case-insensitive name
		for ( int i = getNumNames()-1; i >=0; i-- ) {
			NameRecord rec = getNameRecord(i);
			if (rec != name) {
				if (isDuplicatedNames(name, rec))
					return true;
			}
		}
		return false;
	}

	private static boolean isDuplicatedNames(NameRecord firstName, NameRecord lastName) {
		return lastName.getNameText().equalsIgnoreCase(firstName.getNameText())
			&& isSameSheetNames(firstName, lastName);
	}
	private static boolean isSameSheetNames(NameRecord firstName, NameRecord lastName) {
		return lastName.getSheetNumber() == firstName.getSheetNumber();
	}

	public String[] getExternalBookAndSheetName(int extRefIndex) {
		int ebIx = _externSheetRecord.getExtbookIndexFromRefIndex(extRefIndex);
		SupBookRecord ebr = _externalBookBlocks[ebIx].getExternalBookRecord();
		if (!ebr.isExternalReferences()) {
			return null;
		}
		// Sheet name only applies if not a global reference
		int shIx = _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex);
		String usSheetName = null;
		if(shIx >= 0) {
		   usSheetName = ebr.getSheetNames()[shIx];
		}
		return new String[] {
				ebr.getURL(),
				usSheetName,
		};
	}

	public int getExternalSheetIndex(String workbookName, String sheetName) {
		SupBookRecord ebrTarget = null;
		int externalBookIndex = -1;
		for (int i=0; i<_externalBookBlocks.length; i++) {
			SupBookRecord ebr = _externalBookBlocks[i].getExternalBookRecord();
			if (!ebr.isExternalReferences()) {
				continue;
			}
			if (workbookName.equals(ebr.getURL())) { // not sure if 'equals()' works when url has a directory
				ebrTarget = ebr;
				externalBookIndex = i;
				break;
			}
		}
		if (ebrTarget == null) {
			throw new RuntimeException("No external workbook with name '" + workbookName + "'");
		}
		int sheetIndex = getSheetIndex(ebrTarget.getSheetNames(), sheetName);

		int result = _externSheetRecord.getRefIxForSheet(externalBookIndex, sheetIndex);
		if (result < 0) {
			throw new RuntimeException("ExternSheetRecord does not contain combination ("
					+ externalBookIndex + ", " + sheetIndex + ")");
		}
		return result;
	}

	private static int getSheetIndex(String[] sheetNames, String sheetName) {
		for (int i = 0; i < sheetNames.length; i++) {
			if (sheetNames[i].equals(sheetName)) {
				return i;
			}

		}
		throw new RuntimeException("External workbook does not contain sheet '" + sheetName + "'");
	}

	/**
	 * @param extRefIndex as from a {@link Ref3DPtg} or {@link Area3DPtg}
	 * @return -1 if the reference is to an external book
	 */
	public int getIndexToInternalSheet(int extRefIndex) {
		return _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex);
	}

	public int getSheetIndexFromExternSheetIndex(int extRefIndex) {
		if (extRefIndex >= _externSheetRecord.getNumOfRefs()) {
			return -1;
		}
		return _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex);
	}

	public int checkExternSheet(int sheetIndex) {
		int thisWbIndex = -1; // this is probably always zero
		for (int i=0; i<_externalBookBlocks.length; i++) {
			SupBookRecord ebr = _externalBookBlocks[i].getExternalBookRecord();
			if (ebr.isInternalReferences()) {
				thisWbIndex = i;
				break;
			}
		}
		if (thisWbIndex < 0) {
			throw new RuntimeException("Could not find 'internal references' EXTERNALBOOK");
		}

		//Trying to find reference to this sheet
		int i = _externSheetRecord.getRefIxForSheet(thisWbIndex, sheetIndex);
		if (i>=0) {
			return i;
		}
		//We haven't found reference to this sheet
		return _externSheetRecord.addRef(thisWbIndex, sheetIndex, sheetIndex);
	}


	/**
	 * copied from Workbook
	 */
	private int findFirstRecordLocBySid(short sid) {
		int index = 0;
		for (Iterator iterator = _workbookRecordList.iterator(); iterator.hasNext(); ) {
			Record record = ( Record ) iterator.next();

			if (record.getSid() == sid) {
				return index;
			}
			index ++;
		}
		return -1;
	}

	public String resolveNameXText(int refIndex, int definedNameIndex) {
		int extBookIndex = _externSheetRecord.getExtbookIndexFromRefIndex(refIndex);
		return _externalBookBlocks[extBookIndex].getNameText(definedNameIndex);
	}
	public int resolveNameXIx(int refIndex, int definedNameIndex) {
      int extBookIndex = _externSheetRecord.getExtbookIndexFromRefIndex(refIndex);
      return _externalBookBlocks[extBookIndex].getNameIx(definedNameIndex);
	}

	public NameXPtg getNameXPtg(String name) {
		// first find any external book block that contains the name:
		for (int i = 0; i < _externalBookBlocks.length; i++) {
			int definedNameIndex = _externalBookBlocks[i].getIndexOfName(name);
			if (definedNameIndex < 0) {
				continue;
			}
			// found it.
			int sheetRefIndex = findRefIndexFromExtBookIndex(i);
			if (sheetRefIndex >= 0) {
				return new NameXPtg(sheetRefIndex, definedNameIndex);
			}
		}
		return null;
	}

	private int findRefIndexFromExtBookIndex(int extBookIndex) {
		return _externSheetRecord.findRefIndexFromExtBookIndex(extBookIndex);
	}
}
