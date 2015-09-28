
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
 * The AutoFilterInfo record specifies the number of columns that have AutoFilter enabled
 * and indicates the beginning of the collection of AutoFilter records.
 *
 * @author Yegor Kozlov
 */

public final class AutoFilterInfoRecord extends StandardRecord implements Cloneable {
    public final static short sid = 0x9D;
    /**
     * Number of AutoFilter drop-down arrows on the sheet
     */
    private short             _cEntries;   // = 0;

    public AutoFilterInfoRecord()
    {
    }

    public AutoFilterInfoRecord(RecordInputStream in)
    {
        _cEntries = in.readShort();
    }

    /**
     * set the number of AutoFilter drop-down arrows on the sheet
     *
     * @param num  the number of AutoFilter drop-down arrows on the sheet
     */

    public void setNumEntries(short num)
    {
        _cEntries = num;
    }

    /**
     * get the number of AutoFilter drop-down arrows on the sheet
     *
     * @return the number of AutoFilter drop-down arrows on the sheet
     */

    public short getNumEntries()
    {
        return _cEntries;
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[AUTOFILTERINFO]\n");
        buffer.append("    .numEntries          = ")
            .append(_cEntries).append("\n");
        buffer.append("[/AUTOFILTERINFO]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_cEntries);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    public AutoFilterInfoRecord clone()
    {
    	return (AutoFilterInfoRecord)cloneViaReserialise();
    }
    
}