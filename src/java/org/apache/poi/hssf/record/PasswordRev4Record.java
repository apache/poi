
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

import org.apache.poi.util.LittleEndianOutput;

/**
 * Title:        Protection Revision 4 password Record<P>
 * Description:  Stores the (2 byte??!!) encrypted password for a shared
 *               workbook<P>
 * REFERENCE:  PG 374 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public final class PasswordRev4Record
    extends StandardRecord
{
    public final static short sid = 0x1BC;
    private short             field_1_password;

    public PasswordRev4Record()
    {
    }

    public PasswordRev4Record(RecordInputStream in)
    {
        field_1_password = in.readShort();
    }

    /**
     * set the password
     *
     * @param pw  representing the password
     */

    public void setPassword(short pw)
    {
        field_1_password = pw;
    }

    /**
     * get the password
     *
     * @return short  representing the password
     */

    public short getPassword()
    {
        return field_1_password;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[PROT4REVPASSWORD]\n");
        buffer.append("    .password       = ")
            .append(Integer.toHexString(getPassword())).append("\n");
        buffer.append("[/PROT4REVPASSWORD]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getPassword());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }
}
