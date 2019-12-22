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

package org.apache.poi.hssf.record.chart;

import org.apache.poi.hssf.record.RecordInputStream;
import org.apache.poi.hssf.record.StandardRecord;
import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianOutput;
import org.apache.poi.util.Removal;

/** The font index record indexes into the font table for the text record. */
public final class FontIndexRecord extends StandardRecord {
    public static final short sid = 0x1026;
    private short field_1_fontIndex;

    public FontIndexRecord() {}

    public FontIndexRecord(FontIndexRecord other) {
        super(other);
        field_1_fontIndex = other.field_1_fontIndex;
    }

    public FontIndexRecord(RecordInputStream in) {
        field_1_fontIndex = in.readShort();
    }

    public String toString()
    {
        StringBuilder buffer = new StringBuilder();

        buffer.append("[FONTX]\n");
        buffer.append("    .fontIndex            = ")
            .append("0x").append(HexDump.toHex(  getFontIndex ()))
            .append(" (").append( getFontIndex() ).append(" )");
        buffer.append(System.getProperty("line.separator"));

        buffer.append("[/FONTX]\n");
        return buffer.toString();
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_1_fontIndex);
    }

    protected int getDataSize() {
        return 2;
    }

    public short getSid()
    {
        return sid;
    }

    @Override
    @SuppressWarnings("squid:S2975")
    @Deprecated
    @Removal(version = "5.0.0")
    public FontIndexRecord clone() {
        return copy();
    }

    @Override
    public FontIndexRecord copy() {
        return new FontIndexRecord(this);
    }




    /**
     * Get the font index field for the FontIndex record.
     */
    public short getFontIndex()
    {
        return field_1_fontIndex;
    }

    /**
     * Set the font index field for the FontIndex record.
     */
    public void setFontIndex(short field_1_fontIndex)
    {
        this.field_1_fontIndex = field_1_fontIndex;
    }
}
