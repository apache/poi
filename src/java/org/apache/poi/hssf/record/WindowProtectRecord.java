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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Title: Window Protect Record (0x0019)<p>
 * Description:  flags whether workbook windows are protected<p>
 * REFERENCE:  PG 424 Microsoft Excel 97 Developer's Kit (ISBN: 1-57231-498-2)
 */
public final class WindowProtectRecord extends StandardRecord {
    public final static short sid = 0x0019;

    private static final BitField settingsProtectedFlag = BitFieldFactory.getInstance(0x0001);

    private int _options;

    public WindowProtectRecord(int options) {
        _options = options;
    }

    public WindowProtectRecord(RecordInputStream in) {
        this(in.readUShort());
    }

    public WindowProtectRecord(boolean protect) {
        this(0);
        setProtect(protect);
    }

    /**
     * set whether this window should be protected or not
     * @param protect or not
     */
    public void setProtect(boolean protect) {
        _options = settingsProtectedFlag.setBoolean(_options, protect);
    }

    /**
     * is this window protected or not
     *
     * @return protected or not
     */
    public boolean getProtect() {
        return settingsProtectedFlag.isSet(_options);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[WINDOWPROTECT]\n");
        buffer.append("    .options = ").append(HexDump.shortToHex(_options)).append("\n");
        buffer.append("[/WINDOWPROTECT]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(_options);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }
    @Override
    public Object clone() {
        return new WindowProtectRecord(_options);
    }
}
