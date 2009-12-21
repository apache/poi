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
import org.apache.poi.util.HexDump;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

/**
 * Title: Interface End Record (0x00E2)<P>
 * Description: Shows where the Interface Records end (MMS)
 *  (has no fields)<P>
 * REFERENCE:  PG 324 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @version 2.0-pre
 */
public final class InterfaceEndRecord extends StandardRecord {
    private static POILogger logger = POILogFactory.getLogger(InterfaceEndRecord.class);

    public final static short sid = 0x00E2;

    private byte[] _unknownData;

    public InterfaceEndRecord()
    {
    }

    public InterfaceEndRecord(RecordInputStream in)
    {
        if(in.available() > 0){
            _unknownData = in.readRemainder();
            logger.log(POILogger.WARN, "encountered unexpected " + 
                    _unknownData.length + " bytes in InterfaceEndRecord");
        }
    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[INTERFACEEND]\n");
        buffer.append("  unknownData=").append(HexDump.toHex(_unknownData)).append("\n");
        buffer.append("[/INTERFACEEND]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        if(_unknownData != null) out.write(_unknownData);
    }

    protected int getDataSize() {
        int size = 0;
        if(_unknownData != null) size += _unknownData.length;
        return size;
    }

    public short getSid()
    {
        return sid;
    }
}
