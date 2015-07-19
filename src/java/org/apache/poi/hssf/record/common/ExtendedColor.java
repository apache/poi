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

package org.apache.poi.hssf.record.common;

import org.apache.poi.util.HexDump;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;


/**
 * Title: CTColor (Extended Color) record part
 * <P>
 * The HSSF file format normally stores Color information in the
 *  Palette (see PaletteRecord), but for a few cases (eg Conditional
 *  Formatting, Sheet Extensions), this XSSF-style color record 
 *  can be used.
 */
public final class ExtendedColor {
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_INDEXED = 1;
    public static final int TYPE_RGB = 2;
    public static final int TYPE_THEMED = 3;
    public static final int TYPE_UNSET = 4;
    
    private int type; 
    // TODO Decode
    private byte[] value;
    private double tint;
    
    public ExtendedColor() {
        this.type = TYPE_INDEXED;
        this.value = new byte[4];
        this.tint = 0d;
    }
    public ExtendedColor(LittleEndianInput in) {
        type = in.readInt();
        // TODO Decode color
        value = new byte[4];
        in.readFully(value);
        tint = in.readDouble();
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }
    
    // TODO Return the color details
    
    /**
     * @return Tint and Shade value, between -1 and +1
     */
    public double getTint() {
        return tint;
    }
    /**
     * @param tint Tint and Shade value, between -1 and +1
     */
    public void setTint(double tint) {
        if (tint < -1 || tint > 1) {
            throw new IllegalArgumentException("Tint/Shade must be between -1 and +1");
        }
        this.tint = tint;
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [Extended Color]\n");
        buffer.append("          .type  = ").append(type).append("\n");
        buffer.append("          .tint  = ").append(tint).append("\n");
        buffer.append("          .color = ").append(HexDump.toHex(value)).append("\n");
        buffer.append("    [/Extended Color]\n");
        return buffer.toString();
    }
    
    public Object clone()  {
        ExtendedColor exc = new ExtendedColor();
        exc.type = type;
        exc.tint = tint;
        exc.value = new byte[value.length];
        System.arraycopy(value, 0, exc.value, 0, value.length);
        return exc;
    }
    
    public int getDataLength() {
        return 4+4+8;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(type);
        out.write(value);
        out.writeDouble(tint);
    }
}