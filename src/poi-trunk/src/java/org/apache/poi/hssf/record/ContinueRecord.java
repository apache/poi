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
 * Title:        Continue Record(0x003C) - Helper class used primarily for SST Records <P>
 * Description:  handles overflow for prior record in the input
 *               stream; content is tailored to that prior record<P>
 * @author Marc Johnson (mjohnson at apache dot org)
 * @author Andrew C. Oliver (acoliver at apache dot org)
 * @author Csaba Nagy (ncsaba at yahoo dot com)
 */
public final class ContinueRecord extends StandardRecord implements Cloneable {
    public final static short sid = 0x003C;
    private byte[] _data;

    public ContinueRecord(byte[] data) {
        _data = data;
    }

    protected int getDataSize() {
        return _data.length;
    }

    public void serialize(LittleEndianOutput out) {
        out.write(_data);
    }

    /**
     * get the data for continuation
     * @return byte array containing all of the continued data
     */
    public byte[] getData() {
        return _data;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[CONTINUE RECORD]\n");
        buffer.append("    .data = ").append(HexDump.toHex(_data)).append("\n");
        buffer.append("[/CONTINUE RECORD]\n");
        return buffer.toString();
    }

    public short getSid() {
        return sid;
    }

    public ContinueRecord(RecordInputStream in) {
        _data = in.readRemainder();
    }

    @Override
    public ContinueRecord clone() {
        return new ContinueRecord(_data);
    }
}
