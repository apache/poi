
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
 * Title:        Sup Book  <P>
 * Description:  A Extrenal Workbook Deciption (Sup Book)
 *               Its only a dummy record for making new ExternSheet Record <P>
 * REFERENCE:  <P>
 * @author Libin Roman (Vista Portal LDT. Developer)
 * @author Andrew C. Oliver (acoliver@apache.org)
 *
 */
public class SupBookRecord extends Record
{
    public final static short sid = 0x1AE;
    private short             field_1_number_of_sheets;
    private short             field_2_flag;


    public SupBookRecord()
    {
        setFlag((short)0x401);
    }

    /**
     * Constructs a Extern Sheet record and sets its fields appropriately.
     *
     * @param id     id must be 0x16 or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */
    public SupBookRecord(short id, short size, byte[] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a Extern Sheet record and sets its fields appropriately.
     *
     * @param id     id must be 0x1ae or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the record's data
     */
    public SupBookRecord(short id, short size, byte[] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT An Supbook RECORD");
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
    protected void fillFields(byte [] data, short size, int offset)
    {
        //For now We use it only for one case
        //When we need to add an named range when no named ranges was
        //before it
        field_1_number_of_sheets = LittleEndian.getShort(data,offset+0);
        field_2_flag = LittleEndian.getShort(data,offset+2);
    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("[SUPBOOK]\n");
        buffer.append("numberosheets = ").append(getNumberOfSheets()).append('\n');
        buffer.append("flag          = ").append(getFlag()).append('\n');
        buffer.append("[/SUPBOOK]\n");
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
    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, (short) 4);
        LittleEndian.putShort(data, 4 + offset, field_1_number_of_sheets);
        LittleEndian.putShort(data, 6 + offset, field_2_flag);

        return getRecordSize();
    }

    public void setNumberOfSheets(short number){
        field_1_number_of_sheets = number;
    }

    public short getNumberOfSheets(){
        return field_1_number_of_sheets;
    }

    public void setFlag(short flag){
        field_2_flag = flag;
    }

    public short getFlag() {
        return field_2_flag;
    }

    public int getRecordSize()
    {
        return 4 + 4;
    }

    public short getSid()
    {
        return this.sid;
    }
}
