
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
 * Title: Codepage Record<P>
 * Description:  the default characterset. for the workbook<P>
 * REFERENCE:  PG 293 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public final class CodepageRecord
    extends StandardRecord
{
    public final static short sid = 0x42;
    private short             field_1_codepage;   // = 0;

    /**
     * the likely correct value for CODEPAGE (at least for US versions).  We could use
     * some help with international versions (which we do not have access to documentation
     * for)
     */

    public final static short CODEPAGE = ( short ) 0x4b0;

    public CodepageRecord()
    {
    }

    public CodepageRecord(RecordInputStream in)
    {
        field_1_codepage = in.readShort();
    }

    /**
     * set the codepage for this workbook
     *
     * @see #CODEPAGE
     * @param cp the codepage to set
     */

    public void setCodepage(short cp)
    {
        field_1_codepage = cp;
    }

    /**
     * get the codepage for this workbook
     *
     * @see #CODEPAGE
     * @return codepage - the codepage to set
     */

    public short getCodepage()
    {
        return field_1_codepage;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CODEPAGE]\n");
        buffer.append("    .codepage        = ")
            .append(Integer.toHexString(getCodepage())).append("\n");
        buffer.append("[/CODEPAGE]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getCodepage());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }
}
