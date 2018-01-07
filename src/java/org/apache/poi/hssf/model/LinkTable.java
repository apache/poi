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
import org.apache.poi.ss.formula.SheetNameFormatter;
import org.apache.poi.ss.formula.ptg.Area3DPtg;
import org.apache.poi.ss.formula.ptg.ErrPtg;
import org.apache.poi.ss.formula.ptg.NameXPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.Ref3DPtg;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Link Table (OOO pdf reference: 4.10.3 ) <p>
 * <p>
 * The main data of all types of references is stored in the Link Table inside the Workbook Globals
 * Substream (4.2.5). The Link Table itself is optional and occurs only if there are any
 * references in the document.
 * <p>
 * <p>
 * In BIFF8 the Link Table consists of
 * <ul>
 * <li>zero or more EXTERNALBOOK Blocks<p>
 * each consisting of
 * <ul>
 * <li>exactly one EXTERNALBOOK (0x01AE) record</li>
 * <li>zero or more EXTERNALNAME (0x0023) records</li>
 * <li>zero or more CRN Blocks<p>
 * each consisting of
 * <ul>
 * <li>exactly one XCT (0x0059)record</li>
 * <li>zero or more CRN (0x005A) records (documentation says one or more)</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>zero or one EXTERNSHEET (0x0017) record</li>
 * <li>zero or more DEFINEDNAME (0x0018) records</li>
 * </ul>
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
        private ExternalNameRecord[] _externalNameRecords;
        private final CRNBlock[] _crnBlocks;

        public ExternalBookBlock(RecordStream rs) {
            _externalBookRecord = (SupBookRecord) rs.getNext();
            List<Object> temp = new ArrayList<>();
            while (rs.peekNextClass() == ExternalNameRecord.class) {
                temp.add(rs.getNext());
            }
            _externalNameRecords = new ExternalNameRecord[temp.size()];
            temp.toArray(_externalNameRecords);

            temp.clear();

            while (rs.peekNextClass() == CRNCountRecord.class) {
                temp.add(new CRNBlock(rs));
            }
            _crnBlocks = new CRNBlock[temp.size()];
            temp.toArray(_crnBlocks);
        }

        /**
         * Create a new block for external references.
         */
        public ExternalBookBlock(String url, String[] sheetNames) {
            _externalBookRecord = SupBookRecord.createExternalReferences(url, sheetNames);
            _crnBlocks = new CRNBlock[0];
        }

        /**
         * Create a new block for internal references. It is called when constructing a new LinkTable.
         *
         * @see org.apache.poi.hssf.model.LinkTable#LinkTable(int, WorkbookRecordList)
         */
        public ExternalBookBlock(int numberOfSheets) {
            _externalBookRecord = SupBookRecord.createInternalReferences((short) numberOfSheets);
            _externalNameRecords = new ExternalNameRecord[0];
            _crnBlocks = new CRNBlock[0];
        }

        /**
         * Create a new block for registering add-in functions
         *
         * @see org.apache.poi.hssf.model.LinkTable#addNameXPtg(String)
         */
        public ExternalBookBlock() {
            _externalBookRecord = SupBookRecord.createAddInFunctions();
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
         *
         * @return -1 if not found
         */
        public int getIndexOfName(String name) {
            for (int i = 0; i < _externalNameRecords.length; i++) {
                if (_externalNameRecords[i].getText().equalsIgnoreCase(name)) {
                    return i;
                }
            }
            return -1;
        }

        public int getNumberOfNames() {
            return _externalNameRecords.length;
        }

        public int addExternalName(ExternalNameRecord rec) {
            ExternalNameRecord[] tmp = new ExternalNameRecord[_externalNameRecords.length + 1];
            System.arraycopy(_externalNameRecords, 0, tmp, 0, _externalNameRecords.length);
            tmp[tmp.length - 1] = rec;
            _externalNameRecords = tmp;
            return _externalNameRecords.length - 1;
        }
    }

    private ExternalBookBlock[] _externalBookBlocks;
    private final ExternSheetRecord _externSheetRecord;
    private final List<NameRecord> _definedNames;
    private final int _recordCount;
    private final WorkbookRecordList _workbookRecordList; // TODO - would be nice to remove this

    public LinkTable(List<Record> inputList, int startIndex, WorkbookRecordList workbookRecordList, Map<String, NameCommentRecord> commentRecords) {

        _workbookRecordList = workbookRecordList;
        RecordStream rs = new RecordStream(inputList, startIndex);

        List<ExternalBookBlock> temp = new ArrayList<>();
        while (rs.peekNextClass() == SupBookRecord.class) {
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

        _definedNames = new ArrayList<>();
        // collect zero or more DEFINEDNAMEs id=0x18,
        //  with their comments if present
        while (true) {
            Class<? extends Record> nextClass = rs.peekNextClass();
            if (nextClass == NameRecord.class) {
                NameRecord nr = (NameRecord) rs.getNext();
                _definedNames.add(nr);
            } else if (nextClass == NameCommentRecord.class) {
                NameCommentRecord ncr = (NameCommentRecord) rs.getNext();
                commentRecords.put(ncr.getNameText(), ncr);
            } else {
                break;
            }
        }

        _recordCount = rs.getCountRead();
        _workbookRecordList.getRecords().addAll(inputList.subList(startIndex, startIndex + _recordCount));
    }

    private static ExternSheetRecord readExtSheetRecord(RecordStream rs) {
        List<ExternSheetRecord> temp = new ArrayList<>(2);
        while (rs.peekNextClass() == ExternSheetRecord.class) {
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
        _definedNames = new ArrayList<>();
        _externalBookBlocks = new ExternalBookBlock[]{
                new ExternalBookBlock(numberOfSheets),
        };
        _externSheetRecord = new ExternSheetRecord();
        _recordCount = 2;

        // tell _workbookRecordList about the 2 new records

        SupBookRecord supbook = _externalBookBlocks[0].getExternalBookRecord();

        int idx = findFirstRecordLocBySid(CountryRecord.sid);
        if (idx < 0) {
            throw new RuntimeException("CountryRecord not found");
        }
        _workbookRecordList.add(idx + 1, _externSheetRecord);
        _workbookRecordList.add(idx + 1, supbook);
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
        Iterator<NameRecord> iterator = _definedNames.iterator();
        while (iterator.hasNext()) {
            NameRecord record = iterator.next();

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
        _workbookRecordList.add(idx + countNames, name);
    }

    public void removeName(int namenum) {
        _definedNames.remove(namenum);
    }

    /**
     * checks if the given name is already included in the linkTable
     */
    public boolean nameAlreadyExists(NameRecord name) {
        // Check to ensure no other names have the same case-insensitive name
        for (int i = getNumNames() - 1; i >= 0; i--) {
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
        int shIx1 = _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex);
        int shIx2 = _externSheetRecord.getLastSheetIndexFromRefIndex(extRefIndex);
        String firstSheetName = null;
        String lastSheetName = null;
        if (shIx1 >= 0) {
            firstSheetName = ebr.getSheetNames()[shIx1];
        }
        if (shIx2 >= 0) {
            lastSheetName = ebr.getSheetNames()[shIx2];
        }
        if (shIx1 == shIx2) {
            return new String[]{
                    ebr.getURL(),
                    firstSheetName
            };
        } else {
            return new String[]{
                    ebr.getURL(),
                    firstSheetName,
                    lastSheetName
            };
        }
    }

    private int getExternalWorkbookIndex(String workbookName) {
        for (int i = 0; i < _externalBookBlocks.length; i++) {
            SupBookRecord ebr = _externalBookBlocks[i].getExternalBookRecord();
            if (!ebr.isExternalReferences()) {
                continue;
            }
            if (workbookName.equals(ebr.getURL())) { // not sure if 'equals()' works when url has a directory
                return i;
            }
        }
        return -1;
    }

    public int linkExternalWorkbook(String name, Workbook externalWorkbook) {
        int extBookIndex = getExternalWorkbookIndex(name);
        if (extBookIndex != -1) {
            // Already linked!
            return extBookIndex;
        }

        // Create a new SupBookRecord
        String[] sheetNames = new String[externalWorkbook.getNumberOfSheets()];
        for (int sn = 0; sn < sheetNames.length; sn++) {
            sheetNames[sn] = externalWorkbook.getSheetName(sn);
        }
        String url = "\000" + name;
        ExternalBookBlock block = new ExternalBookBlock(url, sheetNames);

        // Add it into the list + records
        extBookIndex = extendExternalBookBlocks(block);

        // add the created SupBookRecord before ExternSheetRecord
        int idx = findFirstRecordLocBySid(ExternSheetRecord.sid);
        if (idx == -1) {
            idx = _workbookRecordList.size();
        }
        _workbookRecordList.add(idx, block.getExternalBookRecord());

        // Setup links for the sheets
        for (int sn = 0; sn < sheetNames.length; sn++) {
            _externSheetRecord.addRef(extBookIndex, sn, sn);
        }

        // Report where it went
        return extBookIndex;
    }

    public int getExternalSheetIndex(String workbookName, String firstSheetName, String lastSheetName) {
        int externalBookIndex = getExternalWorkbookIndex(workbookName);
        if (externalBookIndex == -1) {
            throw new RuntimeException("No external workbook with name '" + workbookName + "'");
        }
        SupBookRecord ebrTarget = _externalBookBlocks[externalBookIndex].getExternalBookRecord();

        int firstSheetIndex = getSheetIndex(ebrTarget.getSheetNames(), firstSheetName);
        int lastSheetIndex = getSheetIndex(ebrTarget.getSheetNames(), lastSheetName);

        // Find or add the external sheet record definition for this
        int result = _externSheetRecord.getRefIxForSheet(externalBookIndex, firstSheetIndex, lastSheetIndex);
        if (result < 0) {
            result = _externSheetRecord.addRef(externalBookIndex, firstSheetIndex, lastSheetIndex);
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
    public int getFirstInternalSheetIndexForExtIndex(int extRefIndex) {
        if (extRefIndex >= _externSheetRecord.getNumOfRefs() || extRefIndex < 0) {
            return -1;
        }
        return _externSheetRecord.getFirstSheetIndexFromRefIndex(extRefIndex);
    }

    /**
     * @param extRefIndex as from a {@link Ref3DPtg} or {@link Area3DPtg}
     * @return -1 if the reference is to an external book
     */
    public int getLastInternalSheetIndexForExtIndex(int extRefIndex) {
        if (extRefIndex >= _externSheetRecord.getNumOfRefs() || extRefIndex < 0) {
            return -1;
        }
        return _externSheetRecord.getLastSheetIndexFromRefIndex(extRefIndex);
    }

    public void removeSheet(int sheetIdx) {
        _externSheetRecord.removeSheet(sheetIdx);
    }

    public int checkExternSheet(int sheetIndex) {
        return checkExternSheet(sheetIndex, sheetIndex);
    }

    public int checkExternSheet(int firstSheetIndex, int lastSheetIndex) {
        int thisWbIndex = -1; // this is probably always zero
        for (int i = 0; i < _externalBookBlocks.length; i++) {
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
        int i = _externSheetRecord.getRefIxForSheet(thisWbIndex, firstSheetIndex, lastSheetIndex);
        if (i >= 0) {
            return i;
        }
        //We haven't found reference to this sheet
        return _externSheetRecord.addRef(thisWbIndex, firstSheetIndex, lastSheetIndex);
    }

    /**
     * copied from Workbook
     */
    private int findFirstRecordLocBySid(short sid) {
        int index = 0;
        for (Record record : _workbookRecordList.getRecords()) {
            if (record.getSid() == sid) {
                return index;
            }
            index++;
        }
        return -1;
    }

    public String resolveNameXText(int refIndex, int definedNameIndex, InternalWorkbook workbook) {
        int extBookIndex = _externSheetRecord.getExtbookIndexFromRefIndex(refIndex);
        int firstTabIndex = _externSheetRecord.getFirstSheetIndexFromRefIndex(refIndex);
        if (firstTabIndex == -1) {
            // The referenced sheet could not be found
            throw new RuntimeException("Referenced sheet could not be found");
        }

        // Does it exist via the external book block?
        ExternalBookBlock externalBook = _externalBookBlocks[extBookIndex];
        if (externalBook._externalNameRecords.length > definedNameIndex) {
            return _externalBookBlocks[extBookIndex].getNameText(definedNameIndex);
        } else if (firstTabIndex == -2) {
            // Workbook scoped name, not actually external after all
            NameRecord nr = getNameRecord(definedNameIndex);
            int sheetNumber = nr.getSheetNumber();

            StringBuilder text = new StringBuilder(64);
            if (sheetNumber > 0) {
                String sheetName = workbook.getSheetName(sheetNumber - 1);
                SheetNameFormatter.appendFormat(text, sheetName);
                text.append("!");
            }
            text.append(nr.getNameText());
            return text.toString();
        } else {
            throw new ArrayIndexOutOfBoundsException(
                    "Ext Book Index relative but beyond the supported length, was " +
                            extBookIndex + " but maximum is " + _externalBookBlocks.length
            );
        }
    }

    public int resolveNameXIx(int refIndex, int definedNameIndex) {
        int extBookIndex = _externSheetRecord.getExtbookIndexFromRefIndex(refIndex);
        return _externalBookBlocks[extBookIndex].getNameIx(definedNameIndex);
    }

    /**
     * Finds the external name definition for the given name,
     * optionally restricted by externsheet index, and returns
     * (if found) as a NameXPtg.
     *
     * @param sheetRefIndex The Extern Sheet Index to look for, or -1 if any
     */
    public NameXPtg getNameXPtg(String name, int sheetRefIndex) {
        // first find any external book block that contains the name:
        for (int i = 0; i < _externalBookBlocks.length; i++) {
            int definedNameIndex = _externalBookBlocks[i].getIndexOfName(name);
            if (definedNameIndex < 0) {
                continue;
            }

            // Found one
            int thisSheetRefIndex = findRefIndexFromExtBookIndex(i);
            if (thisSheetRefIndex >= 0) {
                // Check for the sheet index match, if requested
                if (sheetRefIndex == -1 || thisSheetRefIndex == sheetRefIndex) {
                    return new NameXPtg(thisSheetRefIndex, definedNameIndex);
                }
            }
        }
        return null;
    }

    /**
     * Register an external name in this workbook
     *
     * @param name the name to register
     * @return a NameXPtg describing this name
     */
    public NameXPtg addNameXPtg(String name) {
        int extBlockIndex = -1;
        ExternalBookBlock extBlock = null;

        // find ExternalBlock for Add-In functions and remember its index
        for (int i = 0; i < _externalBookBlocks.length; i++) {
            SupBookRecord ebr = _externalBookBlocks[i].getExternalBookRecord();
            if (ebr.isAddInFunctions()) {
                extBlock = _externalBookBlocks[i];
                extBlockIndex = i;
                break;
            }
        }
        // An ExternalBlock for Add-In functions was not found. Create a new one.
        if (extBlock == null) {
            extBlock = new ExternalBookBlock();
            extBlockIndex = extendExternalBookBlocks(extBlock);

            // add the created SupBookRecord before ExternSheetRecord
            int idx = findFirstRecordLocBySid(ExternSheetRecord.sid);
            _workbookRecordList.add(idx, extBlock.getExternalBookRecord());

            // register the SupBookRecord in the ExternSheetRecord
            // -2 means that the scope of this name is Workbook and the reference applies to the entire workbook.
            _externSheetRecord.addRef(_externalBookBlocks.length - 1, -2, -2);
        }

        // create a ExternalNameRecord that will describe this name
        ExternalNameRecord extNameRecord = new ExternalNameRecord();
        extNameRecord.setText(name);
        // The docs don't explain why Excel set the formula to #REF!
        extNameRecord.setParsedExpression(new Ptg[]{ErrPtg.REF_INVALID});

        int nameIndex = extBlock.addExternalName(extNameRecord);
        int supLinkIndex = 0;
        // find the posistion of the Add-In SupBookRecord in the workbook stream,
        // the created ExternalNameRecord will be appended to it
        for (Record record : _workbookRecordList.getRecords()) {
            if (record instanceof SupBookRecord && ((SupBookRecord) record).isAddInFunctions()) {
                break;
            }
            supLinkIndex++;
        }
        int numberOfNames = extBlock.getNumberOfNames();
        // a new name is inserted in the end of the SupBookRecord, after the last name
        _workbookRecordList.add(supLinkIndex + numberOfNames, extNameRecord);
        int fakeSheetIdx = -2; /* the scope is workbook*/
        int ix = _externSheetRecord.getRefIxForSheet(extBlockIndex, fakeSheetIdx, fakeSheetIdx);
        return new NameXPtg(ix, nameIndex);
    }

    private int extendExternalBookBlocks(ExternalBookBlock newBlock) {
        ExternalBookBlock[] tmp = new ExternalBookBlock[_externalBookBlocks.length + 1];
        System.arraycopy(_externalBookBlocks, 0, tmp, 0, _externalBookBlocks.length);
        tmp[tmp.length - 1] = newBlock;
        _externalBookBlocks = tmp;

        return (_externalBookBlocks.length - 1);
    }

    private int findRefIndexFromExtBookIndex(int extBookIndex) {
        return _externSheetRecord.findRefIndexFromExtBookIndex(extBookIndex);
    }

    /**
     * Changes an external referenced file to another file.
     * A formular in Excel which refers a cell in another file is saved in two parts:
     * The referenced file is stored in an reference table. the row/cell information is saved separate.
     * This method invokation will only change the reference in the lookup-table itself.
     *
     * @param oldUrl The old URL to search for and which is to be replaced
     * @param newUrl The URL replacement
     * @return true if the oldUrl was found and replaced with newUrl. Otherwise false
     */
    public boolean changeExternalReference(String oldUrl, String newUrl) {
        for (ExternalBookBlock ex : _externalBookBlocks) {
            SupBookRecord externalRecord = ex.getExternalBookRecord();
            if (externalRecord.isExternalReferences()
                    && externalRecord.getURL().equals(oldUrl)) {

                externalRecord.setURL(newUrl);
                return true;
            }
        }
        return false;
    }

}
