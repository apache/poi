
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

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
