
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

/**
 * Title:        A sub Record for Extern Sheet <P>
 * Description:  Defines a named range within a workbook. <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @version 1.0-pre
 */

public class ExternSheetSubRecord extends Record {
    public final static short sid = 0xFFF; // only here for conformance, doesn't really have an sid
    private short             field_1_index_to_supbook;
    private short             field_2_index_to_first_supbook_sheet;
    private short             field_3_index_to_last_supbook_sheet;
    
    
    /** a Constractor for making new sub record
     */
    public ExternSheetSubRecord() {
    }
    
    /**
     * Constructs a Extern Sheet Sub Record record and sets its fields appropriately.
     *
     * @param id     id must be 0x18 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */
    public ExternSheetSubRecord(short id, short size, byte[] data) {
        super(id, size, data);
    }
    
    /**
     * Constructs a Extern Sheet Sub Record record and sets its fields appropriately.
     *
     * @param id     id must be 0x18 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */
    public ExternSheetSubRecord(short id, short size, byte[] data, int offset) {
        super(id, size, data, offset);
    }
    
    /** Sets the Index to the sup book
     * @param index sup book index
     */
    public void setIndexToSupBook(short index){
        field_1_index_to_supbook = index;
    }
    
    /** gets the index to sup book
     * @return sup book index
     */
    public short getIndexToSupBook(){
        return field_1_index_to_supbook;
    }
    
    /** sets the index to first sheet in supbook
     * @param index index to first sheet
     */
    public void setIndexToFirstSupBook(short index){
        field_2_index_to_first_supbook_sheet = index;
    }
    
    /** gets the index to first sheet from supbook
     * @return index to first supbook
     */
    public short getIndexToFirstSupBook(){
        return field_2_index_to_first_supbook_sheet;
    }
    
    /** sets the index to last sheet in supbook
     * @param index index to last sheet
     */
    public void setIndexToLastSupBook(short index){
        field_3_index_to_last_supbook_sheet = index;
    }
    
    /** gets the index to last sheet in supbook
     * @return index to last supbook
     */
    public short getIndexToLastSupBook(){
        return field_3_index_to_last_supbook_sheet;
    }
    
    /**
     * called by constructor, should throw runtime exception in the event of a
     * record passed with a differing ID.
     *
     * @param id alleged id for this record
     */
    protected void validateSid(short id) {
        // do nothing
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
        field_1_index_to_supbook             = LittleEndian.getShort(data, 0 + offset);
        field_2_index_to_first_supbook_sheet = LittleEndian.getShort(data, 2 + offset);
        field_3_index_to_last_supbook_sheet  = LittleEndian.getShort(data, 4 + offset);
    }
    
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("   supbookindex =").append(getIndexToSupBook()).append('\n');
        buffer.append("   1stsbindex   =").append(getIndexToFirstSupBook()).append('\n');
        buffer.append("   lastsbindex  =").append(getIndexToLastSupBook()).append('\n');
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
        LittleEndian.putShort(data, 0 + offset, getIndexToSupBook());
        LittleEndian.putShort(data, 2 + offset, getIndexToFirstSupBook());
        LittleEndian.putShort(data, 4 + offset, getIndexToLastSupBook());
        
        return getRecordSize();
    }
    
    
    /** returns the record size
     */
    public int getRecordSize() {
        return 6;
    }
    
    /**
     * return the non static version of the id for this record.
     */
    public short getSid() {
        return this.sid;
    }
}
