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

import org.apache.poi.hssf.record.CRNCountRecord;
import org.apache.poi.hssf.record.CRNRecord;
import org.apache.poi.hssf.record.CountryRecord;
import org.apache.poi.hssf.record.ExternSheetRecord;
import org.apache.poi.hssf.record.ExternSheetSubRecord;
import org.apache.poi.hssf.record.ExternalNameRecord;
import org.apache.poi.hssf.record.NameRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SupBookRecord;

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
			return (CRNRecord[]) _crns.clone();
		}
	}

	private static final class ExternalBookBlock {
		private final SupBookRecord _externalBookRecord;
		private final ExternalNameRecord[] _externalNameRecords;
		private final CRNBlock[] _crnBlocks;

		public ExternalBookBlock(RecordStream rs) {
			_externalBookRecord = (SupBookRecord) rs.getNext();
			List temp = new ArrayList();
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

		public ExternalBookBlock(short numberOfSheets) {
			_externalBookRecord = SupBookRecord.createInternalReferences(numberOfSheets);
			_externalNameRecords = new ExternalNameRecord[0];
			_crnBlocks = new CRNBlock[0];
		}

		public SupBookRecord getExternalBookRecord() {
			return _externalBookRecord;
		}

		public String getNameText(int definedNameIndex) {
			return _externalNameRecords[definedNameIndex].getText();
		}
	}

	private final ExternalBookBlock[] _externalBookBlocks;
	private final ExternSheetRecord _externSheetRecord;
	private final List _definedNames;
	private final int _recordCount;
	private final WorkbookRecordList _workbookRecordList; // TODO - would be nice to remove this

	public LinkTable(List inputList, int startIndex, WorkbookRecordList workbookRecordList) {

		_workbookRecordList = workbookRecordList;
		RecordStream rs = new RecordStream(inputList, startIndex);

		List temp = new ArrayList();
		while(rs.peekNextClass() == SupBookRecord.class) {
		   temp.add(new ExternalBookBlock(rs));
		}
		
		_externalBookBlocks = new ExternalBookBlock[temp.size()];
		temp.toArray(_externalBookBlocks);
		temp.clear();
		
		if (_externalBookBlocks.length > 0) {
			// If any ExternalBookBlock present, there is always 1 of ExternSheetRecord
			Record next = rs.getNext();
			_externSheetRecord = (ExternSheetRecord) next;
		} else {
			_externSheetRecord = null;
		}
		
		_definedNames = new ArrayList();
		// collect zero or more DEFINEDNAMEs id=0x18
		while(rs.peekNextClass() == NameRecord.class) {
			NameRecord nr = (NameRecord)rs.getNext();
			_definedNames.add(nr);
		}

		_recordCount = rs.getCountRead();
		_workbookRecordList.getRecords().addAll(inputList.subList(startIndex, startIndex + _recordCount));
	}

	public LinkTable(short numberOfSheets, WorkbookRecordList workbookRecordList) {
		_workbookRecordList = workbookRecordList;
		_definedNames = new ArrayList();
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


	public NameRecord getSpecificBuiltinRecord(byte name, int sheetIndex) {

		Iterator iterator = _definedNames.iterator();
		while (iterator.hasNext()) {
			NameRecord record = ( NameRecord ) iterator.next();

			//print areas are one based
			if (record.getBuiltInName() == name && record.getIndexToSheet() == sheetIndex) {
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
		return (NameRecord) _definedNames.get(index);
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

	public short getIndexToSheet(short num) {
		return _externSheetRecord.getREFRecordAt(num).getIndexToFirstSupBook();
	}

	public int getSheetIndexFromExternSheetIndex(int externSheetNumber) {
		if (externSheetNumber >= _externSheetRecord.getNumOfREFStructures()) {
			return -1;
		}
		return _externSheetRecord.getREFRecordAt(externSheetNumber).getIndexToFirstSupBook();
	}

	public short addSheetIndexToExternSheet(short sheetNumber) {

		ExternSheetSubRecord record = new ExternSheetSubRecord();
		record.setIndexToFirstSupBook(sheetNumber);
		record.setIndexToLastSupBook(sheetNumber);
		_externSheetRecord.addREFRecord(record);
		_externSheetRecord.setNumOfREFStructures((short)(_externSheetRecord.getNumOfREFStructures() + 1));
		return (short)(_externSheetRecord.getNumOfREFStructures() - 1);
	}

	public short checkExternSheet(int sheetNumber) {

		//Trying to find reference to this sheet
		int nESRs = _externSheetRecord.getNumOfREFStructures();
		for(short i=0; i< nESRs; i++) {
			ExternSheetSubRecord esr = _externSheetRecord.getREFRecordAt(i);

			if (esr.getIndexToFirstSupBook() ==  sheetNumber
					&& esr.getIndexToLastSupBook() == sheetNumber){
				return i;
			}
		}

		//We Haven't found reference to this sheet
		return addSheetIndexToExternSheet((short) sheetNumber);
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

	public int getNumberOfREFStructures() {
		return _externSheetRecord.getNumOfREFStructures();
	}

	public String resolveNameXText(int refIndex, int definedNameIndex) {
		short extBookIndex = _externSheetRecord.getREFRecordAt(refIndex).getIndexToSupBook();
		return _externalBookBlocks[extBookIndex].getNameText(definedNameIndex);
	}
}
