
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
 * Title: Function Group Count Record<P>
 * Description:  Number of built in function groups in the current version of the
 *               Spreadsheet (probably only used on Windoze)<P>
 * REFERENCE:  PG 315 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */

public final class FnGroupCountRecord
    extends StandardRecord
{
    public final static short sid   = 0x9c;

    /**
     * suggested default (14 dec)
     */

    public final static short COUNT = 14;
    private short             field_1_count;

    public FnGroupCountRecord()
    {
    }

    public FnGroupCountRecord(RecordInputStream in)
    {
        field_1_count = in.readShort();
    }

    /**
     * set the number of built-in functions
     *
     * @param count - number of functions
     */

    public void setCount(short count)
    {
        field_1_count = count;
    }

    /**
     * get the number of built-in functions
     *
     * @return number of built-in functions
     */

    public short getCount()
    {
        return field_1_count;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[FNGROUPCOUNT]\n");
        buffer.append("    .count            = ").append(getCount())
            .append("\n");
        buffer.append("[/FNGROUPCOUNT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getCount());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }
}
