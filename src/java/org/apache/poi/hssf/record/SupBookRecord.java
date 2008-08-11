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

import org.apache.poi.hssf.record.UnicodeString.UnicodeRecordStats;
import org.apache.poi.util.LittleEndian;

/**
 * Title:        Sup Book (EXTERNALBOOK) <P>
 * Description:  A External Workbook Description (Supplemental Book)
 *               Its only a dummy record for making new ExternSheet Record <P>
 * REFERENCE:  5.38<P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author Andrew C. Oliver (acoliver@apache.org)
 *
 */
public final class SupBookRecord extends Record {

    public final static short sid = 0x1AE;

    private static final short SMALL_RECORD_SIZE = 4;
    private static final short TAG_INTERNAL_REFERENCES = 0x0401;
    private static final short TAG_ADD_IN_FUNCTIONS = 0x3A01;

    private short             field_1_number_of_sheets;
    private UnicodeString     field_2_encoded_url;
    private UnicodeString[]   field_3_sheet_names;
    private boolean           _isAddInFunctions;

    
    public static SupBookRecord createInternalReferences(short numberOfSheets) {
        return new SupBookRecord(false, numberOfSheets);
    }
    public static SupBookRecord createAddInFunctions() {
        return new SupBookRecord(true, (short)0);
    }
    public static SupBookRecord createExternalReferences(UnicodeString url, UnicodeString[] sheetNames) {
        return new SupBookRecord(url, sheetNames);
    }
    private SupBookRecord(boolean isAddInFuncs, short numberOfSheets) {
        // else not 'External References'
        field_1_number_of_sheets = numberOfSheets;
        field_2_encoded_url = null;
        field_3_sheet_names = null;
        _isAddInFunctions = isAddInFuncs;
    }
    public SupBookRecord(UnicodeString url, UnicodeString[] sheetNames) {
        field_1_number_of_sheets = (short) sheetNames.length;
        field_2_encoded_url = url;
        field_3_sheet_names = sheetNames;
        _isAddInFunctions = false;
    }

    /**
     * Constructs a Extern Sheet record and sets its fields appropriately.
     *
     * @param id     id must be 0x16 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */
    public SupBookRecord(RecordInputStream in) {
        super(in);
    }

    protected void validateSid(short id) {
        if (id != sid) {
            throw new RecordFormatException("NOT An ExternSheet RECORD");
        }
    }

    public boolean isExternalReferences() {
        return field_3_sheet_names != null;
    }
    public boolean isInternalReferences() {
        return field_3_sheet_names == null && !_isAddInFunctions;
    }
    public boolean isAddInFunctions() {
        return field_3_sheet_names == null && _isAddInFunctions;
    }
    /**
     * called by the constructor, should set class level fields.  Should throw
     * runtime exception for bad/incomplete data.
     *
     * @param data raw data
     * @param size size of data
     * @param offset of the record's data (provided a big array of the file)
     */
    protected void fillFields(RecordInputStream in) {
        field_1_number_of_sheets = in.readShort();
        
        if(in.getLength() > SMALL_RECORD_SIZE) {
            // 5.38.1 External References
            _isAddInFunctions = false;

            field_2_encoded_url = in.readUnicodeString();
            UnicodeString[] sheetNames = new UnicodeString[field_1_number_of_sheets];
            for (int i = 0; i < sheetNames.length; i++) {
                sheetNames[i] = in.readUnicodeString();
            }
            field_3_sheet_names = sheetNames;
            return;
        }
        // else not 'External References'
        field_2_encoded_url = null;
        field_3_sheet_names = null;
      
        short nextShort = in.readShort();
        if(nextShort == TAG_INTERNAL_REFERENCES) {
            // 5.38.2 'Internal References'
            _isAddInFunctions = false;
        } else if(nextShort == TAG_ADD_IN_FUNCTIONS) {
            // 5.38.3 'Add-In Functions'
            _isAddInFunctions = true;
            if(field_1_number_of_sheets != 1) {
                throw new RuntimeException("Expected 0x0001 for number of sheets field in 'Add-In Functions' but got ("
                     + field_1_number_of_sheets + ")");
            }
        } else {
            throw new RuntimeException("invalid EXTERNALBOOK code (" 
                     + Integer.toHexString(nextShort) + ")");
        }
     }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getClass().getName()).append(" [SUPBOOK ");
        
        if(isExternalReferences()) {
            sb.append("External References");
            sb.append(" nSheets=").append(field_1_number_of_sheets);
            sb.append(" url=").append(field_2_encoded_url);
        } else if(_isAddInFunctions) {
            sb.append("Add-In Functions");
        } else {
            sb.append("Internal References ");
            sb.append(" nSheets= ").append(field_1_number_of_sheets);
        }
        return sb.toString();
    }
    private int getDataSize() {
        if(!isExternalReferences()) {
            return SMALL_RECORD_SIZE;
        }
        int sum = 2; // u16 number of sheets
        UnicodeRecordStats urs = new UnicodeRecordStats();
        field_2_encoded_url.getRecordSize(urs);
        sum += urs.recordSize;
        
        for(int i=0; i<field_3_sheet_names.length; i++) {
            urs = new UnicodeRecordStats();
            field_3_sheet_names[i].getRecordSize(urs);
            sum += urs.recordSize;
        }
        return sum;
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
        int dataSize = getDataSize();
        LittleEndian.putShort(data, 2 + offset, (short) dataSize);
        LittleEndian.putShort(data, 4 + offset, field_1_number_of_sheets);
               
        if(isExternalReferences()) {
            
            int currentOffset = 6 + offset;
            UnicodeRecordStats urs = new UnicodeRecordStats();
            field_2_encoded_url.serialize(urs, currentOffset, data);
            currentOffset += urs.recordSize;
            
            for(int i=0; i<field_3_sheet_names.length; i++) {
                urs = new UnicodeRecordStats();
                field_3_sheet_names[i].serialize(urs, currentOffset, data);
                currentOffset += urs.recordSize;
            }
        } else {
            short field2val = _isAddInFunctions ? TAG_ADD_IN_FUNCTIONS : TAG_INTERNAL_REFERENCES;
            
            LittleEndian.putShort(data, 6 + offset, field2val);
        }
        return dataSize + 4;
    }

    public void setNumberOfSheets(short number){
        field_1_number_of_sheets = number;
    }

    public short getNumberOfSheets(){
        return field_1_number_of_sheets;
    }

    public int getRecordSize() {
        return getDataSize() + 4;
    }

    public short getSid()
    {
        return sid;
    }
    public UnicodeString getURL() {
        return field_2_encoded_url;
    }
    public UnicodeString[] getSheetNames() {
        return (UnicodeString[]) field_3_sheet_names.clone();
    }
}
