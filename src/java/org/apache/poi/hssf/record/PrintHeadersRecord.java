
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
 * Title:        Print Headers Record<P>
 * Description:  Whether or not to print the row/column headers when you
 *               enjoy your spreadsheet in the physical form.<P>
 * REFERENCE:  PG 373 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public class PrintHeadersRecord
    extends Record
{
    public final static short sid = 0x2a;
    private short             field_1_print_headers;

    public PrintHeadersRecord()
    {
    }

    /**
     * Constructs a PrintHeaders record and sets its fields appropriately.
     *
     * @param id     id must be 0x2a or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     */

    public PrintHeadersRecord(short id, short size, byte [] data)
    {
        super(id, size, data);
    }

    /**
     * Constructs a PrintHeaders record and sets its fields appropriately.
     *
     * @param id     id must be 0x2a or an exception will be throw upon validation
     * @param size  the size of the data area of the record
     * @param data  data of the record (should not contain sid/len)
     * @param offset of the data
     */

    public PrintHeadersRecord(short id, short size, byte [] data, int offset)
    {
        super(id, size, data, offset);
    }

    protected void validateSid(short id)
    {
        if (id != sid)
        {
            throw new RecordFormatException("NOT A PrintHeaders RECORD");
        }
    }

    protected void fillFields(byte [] data, short size, int offset)
    {
        field_1_print_headers = LittleEndian.getShort(data, 0 + offset);
    }

    /**
     * set to print the headers - y/n
     * @param p printheaders or not
     */

    public void setPrintHeaders(boolean p)
    {
        if (p == true)
        {
            field_1_print_headers = 1;
        }
        else
        {
            field_1_print_headers = 0;
        }
    }

    /**
     * get whether to print the headers - y/n
     * @return printheaders or not
     */

    public boolean getPrintHeaders()
    {
        return (field_1_print_headers == 1);
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PRINTHEADERS]\n");
        buffer.append("    .printheaders   = ").append(getPrintHeaders())
            .append("\n");
        buffer.append("[/PRINTHEADERS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x2);
        LittleEndian.putShort(data, 4 + offset, field_1_print_headers);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 6;
    }

    public short getSid()
    {
        return this.sid;
    }

    public Object clone() {
      PrintHeadersRecord rec = new PrintHeadersRecord();
      rec.field_1_print_headers = field_1_print_headers;
      return rec;
    }
}
