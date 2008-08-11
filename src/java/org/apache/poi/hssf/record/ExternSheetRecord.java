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

import org.apache.poi.util.LittleEndian;

/**
 * EXTERNSHEET (0x0017)<br/>
 * A List of Indexes to  EXTERNALBOOK (supplemental book) Records <p/>
 * 
 * @author Libin Roman (Vista Portal LDT. Developer)
 */
public class ExternSheetRecord extends Record {
    public final static short sid = 0x0017;
    private List         _list;
    
    private final class RefSubRecord {
    	public static final int ENCODED_SIZE = 6;

    	/** index to External Book Block (which starts with a EXTERNALBOOK record) */
        private int _extBookIndex;
        private int _firstSheetIndex; // may be -1 (0xFFFF)
        private int _lastSheetIndex;  // may be -1 (0xFFFF)
        
        
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
        
        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append("extBook=").append(_extBookIndex);
            buffer.append(" firstSheet=").append(_firstSheetIndex);
            buffer.append(" lastSheet=").append(_lastSheetIndex);
            return buffer.toString();
        }
        
        /**
         * called by the class that is responsible for writing this sucker.
         * Subclasses should implement this so that their data is passed back in a
         * byte array.
         *
         * @param offset to begin writing at
         * @param data byte array containing instance data
         * @return number of bytes written
         */
        public void serialize(int offset, byte [] data) {
            LittleEndian.putUShort(data, 0 + offset, _extBookIndex);
            LittleEndian.putUShort(data, 2 + offset, _firstSheetIndex);
            LittleEndian.putUShort(data, 4 + offset, _lastSheetIndex);
        }
    }    
    
    
    
    public ExternSheetRecord() {
        _list = new ArrayList();
    }
    
    /**
     * Constructs a Extern Sheet record and sets its fields appropriately.
     * @param in the RecordInputstream to read the record from
     */
    
    public ExternSheetRecord(RecordInputStream in) {
        super(in);
    }
    
    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */
    protected void validateSid(short id) {
        if (id != sid) {
            throw new RecordFormatException("NOT An ExternSheet RECORD");
        }
    }
    
    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/icomplete data.
     *
     * @param in the RecordInputstream to read the record from
     */
    protected void fillFields(RecordInputStream in) {
        _list           = new ArrayList();
        
        int nItems  = in.readShort();
        
        for (int i = 0 ; i < nItems ; ++i) {
            RefSubRecord rec = new RefSubRecord(in);
            
            _list.add( rec);
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
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        int nItems = _list.size();
        sb.append("[EXTERNSHEET]\n");
        sb.append("   numOfRefs     = ").append(nItems).append("\n");
        for (int i=0; i < nItems; i++) {
            sb.append("refrec         #").append(i).append(": ");
            sb.append(getRef(i).toString());
            sb.append('\n');
        }
        sb.append("[/EXTERNSHEET]\n");
        
        
        return sb.toString();
    }
    
    
    private int getDataSize() {
    	return 2 + _list.size() * RefSubRecord.ENCODED_SIZE;
    }
    
    /**
     * called by the class that is responsible for writing this sucker.
     * Subclasses should implement this so that their data is passed back in a
     * byte array.
     *
     * @param offset to begin writing at
     * @param data byte array containing instance data
     * @return number of bytes written
     */
    public int serialize(int offset, byte [] data) {
        int dataSize = getDataSize();
        
        int nItems = _list.size();

        LittleEndian.putShort(data, 0 + offset, sid);
		LittleEndian.putUShort(data, 2 + offset, dataSize);
        LittleEndian.putUShort(data, 4 + offset, nItems);
        
        int pos = 6 ;
        
        for (int i = 0; i < nItems; i++) {
            getRef(i).serialize(offset + pos, data);
            pos +=6;
        }
        return dataSize + 4;
    }

	private RefSubRecord getRef(int i) {
		return (RefSubRecord) _list.get(i);
	}
    
    public int getRecordSize() {
        return 4 + getDataSize();
    }
    
    /**
     * return the non static version of the id for this record.
     */
    public short getSid() {
        return sid;
    }

	public int getExtbookIndexFromRefIndex(int refIndex) {
        return getRef(refIndex).getExtBookIndex();
	}

	/**
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

	public int getFirstSheetIndexFromRefIndex(int extRefIndex) {
		return getRef(extRefIndex).getFirstSheetIndex();
	}

	/**
	 * @return index of newly added ref
	 */
	public int addRef(int extBookIndex, int firstSheetIndex, int lastSheetIndex) {
		_list.add(new RefSubRecord(extBookIndex, firstSheetIndex, lastSheetIndex));
		return _list.size() - 1;
	}

	public int getRefIxForSheet(int sheetIndex) {
        int nItems = _list.size();
        for (int i = 0; i < nItems; i++) {
            RefSubRecord ref = getRef(i);
			if (ref.getFirstSheetIndex() == sheetIndex && ref.getLastSheetIndex() == sheetIndex) {
            	return i;
            }
        }
		return -1;
	}
}
