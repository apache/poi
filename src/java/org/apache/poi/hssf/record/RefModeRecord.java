
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
 * Title:        RefMode Record<P>
 * Description:  Describes which reference mode to use<P>
 * REFERENCE:  PG 376 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Jason Height (jheight at chariot dot net dot au)
 * @version 2.0-pre
 */

public final class RefModeRecord
    extends StandardRecord
{
    public final static short sid           = 0xf;
    public final static short USE_A1_MODE   = 1;
    public final static short USE_R1C1_MODE = 0;
    private short             field_1_mode;

    public RefModeRecord()
    {
    }

    public RefModeRecord(RecordInputStream in)
    {
        field_1_mode = in.readShort();
    }

    /**
     * set the reference mode to use (HSSF uses/assumes A1)
     * @param mode the mode to use
     * @see #USE_A1_MODE
     * @see #USE_R1C1_MODE
     *
     */

    public void setMode(short mode)
    {
        field_1_mode = mode;
    }

    /**
     * get the reference mode to use (HSSF uses/assumes A1)
     * @return mode to use
     * @see #USE_A1_MODE
     * @see #USE_R1C1_MODE
     */

    public short getMode()
    {
        return field_1_mode;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[REFMODE]\n");
        buffer.append("    .mode           = ")
            .append(Integer.toHexString(getMode())).append("\n");
        buffer.append("[/REFMODE]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(getMode());
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    public Object clone() {
      RefModeRecord rec = new RefModeRecord();
      rec.field_1_mode = field_1_mode;
      return rec;
    }
}
