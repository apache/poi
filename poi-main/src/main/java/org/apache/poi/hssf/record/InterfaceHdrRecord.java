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

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Interface Header Record (0x00E1)<P>
 * Description: Defines the beginning of Interface records (MMS)<P>
 * REFERENCE:  PG 324 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)<P>
 * @author Andrew C. Oliver (acoliver at apache dot org)
 */
public final class InterfaceHdrRecord extends StandardRecord {
    public final static short sid = 0x00E1;
    private final int _codepage;

    /**
     * suggested (and probably correct) default
     */
    public final static int CODEPAGE = 0x04B0;

    public InterfaceHdrRecord(int codePage) {
        _codepage = codePage;
    }

    public InterfaceHdrRecord(RecordInputStream in) {
        _codepage = in.readShort();
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[INTERFACEHDR]\n");
        buffer.append("    .codepage = ").append(HexDump.shortToHex(_codepage)).append("\n");
        buffer.append("[/INTERFACEHDR]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_codepage);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid() {
        return sid;
    }
}
