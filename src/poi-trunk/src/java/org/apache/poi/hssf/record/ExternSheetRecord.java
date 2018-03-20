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

package org.apache.poi.hssf.record;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.util.LittleEndianOutput;

/**
 * EXTERNSHEET (0x0017)<p>
 * A List of Indexes to  EXTERNALBOOK (supplemental book) Records
 */
public class ExternSheetRecord extends StandardRecord {

    public final static short sid = 0x0017;
	private final List<RefSubRecord> _list;
	
	private static final class RefSubRecord {
		public static final int ENCODED_SIZE = 6;

		/** index to External Book Block (which starts with a EXTERNALBOOK record) */
		private final int _extBookIndex;
		private int _firstSheetIndex; // may be -1 (0xFFFF)
		private int _lastSheetIndex;  // may be -1 (0xFFFF)
		
		public void adjustIndex(int offset) {
			_firstSheetIndex += offset;
			_lastSheetIndex += offset;
		}
		
		/** a Constructor for making new sub record
		 */
		public RefSubRecord(int extBookIndex, int firstSheetIndex, int lastSheetIndex) {
			_extBookIndex = extBookIndex;
			_firstSheetIndex = firstSheetIndex;
			_lastSheetIndex = lastSheetIndex;
		}
		
		/**
		 * @param in the RecordInputstream to read the record from
		 */
		public RefSubRecord(RecordInputStream in) {
			this(in.readShort(), in.readShort(), in.readShort());
		}
		public int getExtBookIndex(){
			return _extBookIndex;
		}
		public int getFirstSheetIndex(){
			return _firstSheetIndex;
		}
		public int getLastSheetIndex(){
			return _lastSheetIndex;
		}
		
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("extBook=").append(_extBookIndex);
			buffer.append(" firstSheet=").append(_firstSheetIndex);
			buffer.append(" lastSheet=").append(_lastSheetIndex);
			return buffer.toString();
		}
		
		public void serialize(LittleEndianOutput out) {
			out.writeShort(_extBookIndex);
			out.writeShort(_firstSheetIndex);
			out.writeShort(_lastSheetIndex);
		}
	}	
	
	
	
	public ExternSheetRecord() {
		_list = new ArrayList<>();
	}

	public ExternSheetRecord(RecordInputStream in) {
		_list = new ArrayList<>();
		
		int nItems  = in.readShort();
		
		for (int i = 0 ; i < nItems ; ++i) {
			RefSubRecord rec = new RefSubRecord(in);
			_list.add(rec);
		}
	}
	

	/**  
	 * @return number of REF structures
	 */
	public int getNumOfRefs() {
		return _list.size();
	}
	
	/** 
	 * adds REF struct (ExternSheetSubRecord)
	 * @param rec REF struct
	 */
	public void addREFRecord(RefSubRecord rec) {
		_list.add(rec);
	}
	
	/** returns the number of REF Records, which is in model
	 * @return number of REF records
	 */
	public int getNumOfREFRecords() {
		return _list.size();
	}
	
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int nItems = _list.size();
		sb.append("[EXTERNSHEET]\n");
		sb.append("   numOfRefs     = ").append(nItems).append("\n");
		for (int i=0; i < nItems; i++) {
			sb.append("refrec         #").append(i).append(": ");
			sb.append(getRef(i));
			sb.append('\n');
		}
		sb.append("[/EXTERNSHEET]\n");
		
		
		return sb.toString();
	}
	
	@Override
	protected int getDataSize() {
		return 2 + _list.size() * RefSubRecord.ENCODED_SIZE;
	}
	
	@Override
	public void serialize(LittleEndianOutput out) {
		int nItems = _list.size();

		out.writeShort(nItems);
		
		for (int i = 0; i < nItems; i++) {
			getRef(i).serialize(out);
		}
	}

	private RefSubRecord getRef(int i) {
		return _list.get(i);
	}
	
	public void removeSheet(int sheetIdx) {
        int nItems = _list.size();
        for (int i = 0; i < nItems; i++) {
            RefSubRecord refSubRecord = _list.get(i);
            if(refSubRecord.getFirstSheetIndex() == sheetIdx && 
                    refSubRecord.getLastSheetIndex() == sheetIdx) {
            	// removing the entry would mess up the sheet index in Formula of NameRecord
            	_list.set(i, new RefSubRecord(refSubRecord.getExtBookIndex(), -1, -1));
            } else if (refSubRecord.getFirstSheetIndex() > sheetIdx && 
                    refSubRecord.getLastSheetIndex() > sheetIdx) {
                _list.set(i, new RefSubRecord(refSubRecord.getExtBookIndex(), refSubRecord.getFirstSheetIndex()-1, refSubRecord.getLastSheetIndex()-1));
            }
        }
	}
	
	/**
	 * return the non static version of the id for this record.
	 */
	@Override
	public short getSid() {
		return sid;
	}

    /**
     * @param refIndex specifies the n-th refIndex
     * 
     * @return the index of the SupBookRecord for this index
     */
    public int getExtbookIndexFromRefIndex(int refIndex) {
        RefSubRecord refRec = getRef(refIndex);
        return refRec.getExtBookIndex();
    }

	/**
	 * @param extBookIndex external sheet reference index
	 * 
	 * @return -1 if not found
	 */
	public int findRefIndexFromExtBookIndex(int extBookIndex) {
		int nItems = _list.size();
		for (int i = 0; i < nItems; i++) {
			if (getRef(i).getExtBookIndex() == extBookIndex) {
				return i;
			}
		}
		return -1;
	}

    /**
     * Returns the first sheet that the reference applies to, or
     *  -1 if the referenced sheet can't be found, or -2 if the
     *  reference is workbook scoped.
     *  
     * @param extRefIndex external sheet reference index
     * 
     * @return the first sheet that the reference applies to, or
     *  -1 if the referenced sheet can't be found, or -2 if the
     *  reference is workbook scoped
     */
	public int getFirstSheetIndexFromRefIndex(int extRefIndex) {
		return getRef(extRefIndex).getFirstSheetIndex();
	}

    /**
     * Returns the last sheet that the reference applies to, or
     *  -1 if the referenced sheet can't be found, or -2 if the
     *  reference is workbook scoped.
     * For a single sheet reference, the first and last should be
     *  the same.
     *  
     * @param extRefIndex external sheet reference index
     * 
     * @return the last sheet that the reference applies to, or
     *  -1 if the referenced sheet can't be found, or -2 if the
     *  reference is workbook scoped.
     */
    public int getLastSheetIndexFromRefIndex(int extRefIndex) {
        return getRef(extRefIndex).getLastSheetIndex();
    }

	/**
     * Add a zero-based reference to a {@link org.apache.poi.hssf.record.SupBookRecord}.
     * <p>
     *  If the type of the SupBook record is same-sheet referencing, Add-In referencing,
     *  DDE data source referencing, or OLE data source referencing,
     *  then no scope is specified and this value <em>MUST</em> be -2. Otherwise,
     *  the scope must be set as follows:
     *  <ol>
     *    <li><code>-2</code> Workbook-level reference that applies to the entire workbook.</li>
     *    <li><code>-1</code> Sheet-level reference. </li>
     *    <li><code>&gt;=0</code> Sheet-level reference. This specifies the first sheet in the reference.
     *    <p>If the SupBook type is unused or external workbook referencing,
     *    then this value specifies the zero-based index of an external sheet name,
     *    see {@link org.apache.poi.hssf.record.SupBookRecord#getSheetNames()}.
     *    This referenced string specifies the name of the first sheet within the external workbook that is in scope.
     *    This sheet MUST be a worksheet or macro sheet.</p>
     *    
     *    <p>If the supporting link type is self-referencing, then this value specifies the zero-based index of a
     *    {@link org.apache.poi.hssf.record.BoundSheetRecord} record in the workbook stream that specifies
     *    the first sheet within the scope of this reference. This sheet MUST be a worksheet or a macro sheet.
     *    </p>
     *    </li>
     *  </ol>
     *
     * @param extBookIndex  the external book block index
     * @param firstSheetIndex  the scope, must be -2 for add-in references
     * @param lastSheetIndex   the scope, must be -2 for add-in references
	 * @return index of newly added ref
	 */
	public int addRef(int extBookIndex, int firstSheetIndex, int lastSheetIndex) {
		_list.add(new RefSubRecord(extBookIndex, firstSheetIndex, lastSheetIndex));
		return _list.size() - 1;
	}

	public int getRefIxForSheet(int externalBookIndex, int firstSheetIndex, int lastSheetIndex) {
		int nItems = _list.size();
		for (int i = 0; i < nItems; i++) {
			RefSubRecord ref = getRef(i);
			if (ref.getExtBookIndex() != externalBookIndex) {
				continue;
			}
			if (ref.getFirstSheetIndex() == firstSheetIndex && 
			        ref.getLastSheetIndex() == lastSheetIndex) {
				return i;
			}
		}
		return -1;
	}

	public static ExternSheetRecord combine(ExternSheetRecord[] esrs) {
		ExternSheetRecord result = new ExternSheetRecord();
		for (ExternSheetRecord esr : esrs) {
			int nRefs = esr.getNumOfREFRecords();
			for (int j=0; j<nRefs; j++) {
				result.addREFRecord(esr.getRef(j));
			}
		}
		return result;
	}
}
