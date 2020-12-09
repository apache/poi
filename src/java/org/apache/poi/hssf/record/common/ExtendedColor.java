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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
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
public final class ExtendedColor implements Duplicatable, GenericRecord {
    public static final int TYPE_AUTO = 0;
    public static final int TYPE_INDEXED = 1;
    public static final int TYPE_RGB = 2;
    public static final int TYPE_THEMED = 3;
    public static final int TYPE_UNSET = 4;

    public static final int THEME_DARK_1  = 0;
    public static final int THEME_LIGHT_1 = 1;
    public static final int THEME_DARK_2  = 2;
    public static final int THEME_LIGHT_2 = 3;
    public static final int THEME_ACCENT_1 = 4;
    public static final int THEME_ACCENT_2 = 5;
    public static final int THEME_ACCENT_3 = 6;
    public static final int THEME_ACCENT_4 = 7;
    public static final int THEME_ACCENT_5 = 8;
    public static final int THEME_ACCENT_6 = 9;
    public static final int THEME_HYPERLINK = 10;
    // This one is SheetEx only, not allowed in CFs
    public static final int THEME_FOLLOWED_HYPERLINK = 11;

    private int type;

    // Type = Indexed
    private int colorIndex;
    // Type = RGB
    private byte[] rgba;
    // Type = Theme
    private int themeIndex;

    private double tint;

    public ExtendedColor() {
        type = TYPE_INDEXED;
        colorIndex = 0;
        tint = 0d;
    }

    public ExtendedColor(ExtendedColor other) {
        type = other.type;
        tint = other.tint;
        colorIndex = other.colorIndex;
        rgba = (other.rgba == null) ? null : other.rgba.clone();
        themeIndex = other.themeIndex;
    }


    public ExtendedColor(LittleEndianInput in) {
        type = in.readInt();
        if (type == TYPE_INDEXED) {
            colorIndex = in.readInt();
        } else if (type == TYPE_RGB) {
            rgba = new byte[4];
            in.readFully(rgba);
        } else if (type == TYPE_THEMED) {
            themeIndex = in.readInt();
        } else {
            // Ignored
            in.readInt();
        }
        tint = in.readDouble();
    }

    public int getType() {
        return type;
    }
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return Palette color index, if type is {@link #TYPE_INDEXED}
     */
    public int getColorIndex() {
        return colorIndex;
    }
    public void setColorIndex(int colorIndex) {
        this.colorIndex = colorIndex;
    }

    /**
     * @return Red Green Blue Alpha, if type is {@link #TYPE_RGB}
     */
    public byte[] getRGBA() {
        return rgba;
    }
    public void setRGBA(byte[] rgba) {
        this.rgba = (rgba == null) ? null : rgba.clone();
    }

    /**
     * @return Theme color type index, eg {@link #THEME_DARK_1}, if type is {@link #TYPE_THEMED}
     */
    public int getThemeIndex() {
        return themeIndex;
    }
    public void setThemeIndex(int themeIndex) {
        this.themeIndex = themeIndex;
    }
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

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "type", this::getType,
            "tint", this::getTint,
            "colorIndex", this::getColorIndex,
            "rgba", this::getRGBA,
            "themeIndex", this::getThemeIndex
        );
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    @Override
    public ExtendedColor copy()  {
        return new ExtendedColor(this);
    }

    public int getDataLength() {
        return 4+4+8;
    }

    public void serialize(LittleEndianOutput out) {
        out.writeInt(type);
        if (type == TYPE_INDEXED) {
            out.writeInt(colorIndex);
        } else if (type == TYPE_RGB) {
            out.write(rgba);
        } else if (type == TYPE_THEMED) {
            out.writeInt(themeIndex);
        } else {
            out.writeInt(0);
        }
        out.writeDouble(tint);
    }
}