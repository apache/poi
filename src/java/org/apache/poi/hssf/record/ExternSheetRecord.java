
/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
        

package org.apache.poi.hssf.record;

import org.apache.poi.util.LittleEndian;

import java.util.ArrayList;

/**
 * Title:        Extern Sheet <P>
 * Description:  A List of Inndexes to SupBook <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @version 1.0-pre
 */

public class ExternSheetRecord extends Record {
    public final static short sid = 0x17;
    private short             field_1_number_of_REF_sturcutres;
    private ArrayList         field_2_REF_structures;
    
    public ExternSheetRecord() {
        field_2_REF_structures = new ArrayList();
    }
    
    /**
     * Constructs a Extern Sheet record and sets its fields appropriately.
     *
     * @param id     id must be 0x16 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */
    
    public ExternSheetRecord(short id, short size, byte[] data) {
        super(id, size, data);
    }
    
    /**
     * Constructs a Extern Sheet record and sets its fields appropriately.
     *
     * @param id     id must be 0x16 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */
    public ExternSheetRecord(short id, short size, byte[] data, int offset) {
        super(id, size, data, offset);
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
     * @param data raw data
     * @param size size of data
     * @param offset of the record's data (provided a big array of the file)
     */
    protected void fillFields(byte [] data, short size, int offset) {
        field_2_REF_structures           = new ArrayList();
        
        field_1_number_of_REF_sturcutres = LittleEndian.getShort(data, 0 + offset);
        
        int pos = 2 + offset;
        for (int i = 0 ; i < field_1_number_of_REF_sturcutres ; ++i) {
            ExternSheetSubRecord rec = new ExternSheetSubRecord((short)0, (short)6 , data , pos);
            
            pos += 6;
            
            field_2_REF_structures.add( rec);
        }
    }
    
    /** 
     * sets the number of the REF structors , that is in Excel file
     * @param numStruct number of REF structs
     */
    public void setNumOfREFStructures(short numStruct) {
        field_1_number_of_REF_sturcutres = numStruct;
    }
    
    /**  
     * return the number of the REF structors , that is in Excel file
     * @return number of REF structs
     */
    public short getNumOfREFStructures() {
        return field_1_number_of_REF_sturcutres;
    }
    
    /** 
     * adds REF struct (ExternSheetSubRecord)
     * @param rec REF struct
     */
    public void addREFRecord(ExternSheetSubRecord rec) {
        field_2_REF_structures.add(rec);
    }
    
    /** returns the number of REF Records, which is in model
     * @return number of REF records
     */
    public int getNumOfREFRecords() {
        return field_2_REF_structures.size();
    }
    
    /** returns the REF record (ExternSheetSubRecord)
     * @param elem index to place
     * @return REF record
     */
    public ExternSheetSubRecord getREFRecordAt(int elem) {
        ExternSheetSubRecord result = ( ExternSheetSubRecord ) field_2_REF_structures.get(elem);
        
        return result;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("[EXTERNSHEET]\n");
        buffer.append("   numOfRefs     = ").append(getNumOfREFStructures()).append("\n");
        for (int k=0; k < this.getNumOfREFRecords(); k++) {
            buffer.append("refrec         #").append(k).append('\n');
            buffer.append(getREFRecordAt(k).toString());
            buffer.append("----refrec     #").append(k).append('\n');
        }
        buffer.append("[/EXTERNSHEET]\n");
        
        
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
    public int serialize(int offset, byte [] data) {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset,(short)(2 + (getNumOfREFRecords() *6)));
        
        LittleEndian.putShort(data, 4 + offset, getNumOfREFStructures());
        
        int pos = 6 ;
        
        for (int k = 0; k < getNumOfREFRecords(); k++) {
            ExternSheetSubRecord record = getREFRecordAt(k);
            System.arraycopy(record.serialize(), 0, data, pos + offset, 6);
            
            pos +=6;
        }
        return getRecordSize();
    }
    
    public int getRecordSize() {
        return 4 + 2 + getNumOfREFRecords() * 6;
    }
    
    /**
     * return the non static version of the id for this record.
     */
    public short getSid() {
        return this.sid;
    }
}
