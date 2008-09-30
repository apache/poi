
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

import org.apache.poi.util.LittleEndian;

/**
 * Title:        File Pass Record<P>
 * Description:  Indicates that the record after this record are encrypted. HSSF does not support encrypted excel workbooks
 * and the presence of this record will cause processing to be aborted.<p>
 * REFERENCE:  PG 420 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 3.0-pre
 */

public class FilePassRecord
    extends Record
{
    public final static short sid = 0x2F;
    private int             field_1_encryptedpassword;

    public FilePassRecord()
    {
    }

    public FilePassRecord(RecordInputStream in)
    {
        field_1_encryptedpassword = in.readInt();
        
        //Whilst i have read in the password, HSSF currently has no plans to support/decrypt the remainder
        //of this workbook
        throw new RecordFormatException("HSSF does not currently support encrypted workbooks");
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FILEPASS]\n");
        buffer.append("    .password        = ").append(field_1_encryptedpassword)
            .append("\n");
        buffer.append("[/FILEPASS]\n");
        return buffer.toString();
    }

    public int serialize(int offset, byte [] data)
    {
        LittleEndian.putShort(data, 0 + offset, sid);
        LittleEndian.putShort(data, 2 + offset, ( short ) 0x4);
        LittleEndian.putInt(data, 4 + offset, ( short ) field_1_encryptedpassword);
        return getRecordSize();
    }

    public int getRecordSize()
    {
        return 8;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      FilePassRecord rec = new FilePassRecord();
      rec.field_1_encryptedpassword = field_1_encryptedpassword;
      return rec;
    }
}
