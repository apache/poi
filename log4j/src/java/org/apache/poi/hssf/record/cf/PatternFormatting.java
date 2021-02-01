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

package org.apache.poi.hssf.record.cf;

import static org.apache.poi.util.GenericRecordUtil.getEnumBitsAsString;

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.common.Duplicatable;
import org.apache.poi.common.usermodel.GenericRecord;
import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.GenericRecordJsonWriter;
import org.apache.poi.util.GenericRecordUtil;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Pattern Formatting Block of the Conditional Formatting Rule Record.
 */
public final class PatternFormatting implements Duplicatable, GenericRecord {
    /**  No background */
    public static final short     NO_FILL             = 0  ;
    /**  Solidly filled */
    public static final short     SOLID_FOREGROUND    = 1  ;
    /**  Small fine dots */
    public static final short     FINE_DOTS           = 2  ;
    /**  Wide dots */
    public static final short     ALT_BARS            = 3  ;
    /**  Sparse dots */
    public static final short     SPARSE_DOTS         = 4  ;
    /**  Thick horizontal bands */
    public static final short     THICK_HORZ_BANDS    = 5  ;
    /**  Thick vertical bands */
    public static final short     THICK_VERT_BANDS    = 6  ;
    /**  Thick backward facing diagonals */
    public static final short     THICK_BACKWARD_DIAG = 7  ;
    /**  Thick forward facing diagonals */
    public static final short     THICK_FORWARD_DIAG  = 8  ;
    /**  Large spots */
    public static final short     BIG_SPOTS           = 9  ;
    /**  Brick-like layout */
    public static final short     BRICKS              = 10 ;
    /**  Thin horizontal bands */
    public static final short     THIN_HORZ_BANDS     = 11 ;
    /**  Thin vertical bands */
    public static final short     THIN_VERT_BANDS     = 12 ;
    /**  Thin backward diagonal */
    public static final short     THIN_BACKWARD_DIAG  = 13 ;
    /**  Thin forward diagonal */
    public static final short     THIN_FORWARD_DIAG   = 14 ;
    /**  Squares */
    public static final short     SQUARES             = 15 ;
    /**  Diamonds */
    public static final short     DIAMONDS            = 16 ;
    /**  Less Dots */
    public static final short     LESS_DOTS           = 17 ;
    /**  Least Dots */
    public static final short     LEAST_DOTS          = 18 ;


    // PATTERN FORMATING BLOCK
    // For Pattern Styles see constants at HSSFCellStyle (from NO_FILL to LEAST_DOTS)
    private static final BitField  fillPatternStyle = BitFieldFactory.getInstance(0xFC00);

    private static final BitField  patternColorIndex = BitFieldFactory.getInstance(0x007F);
    private static final BitField  patternBackgroundColorIndex = BitFieldFactory.getInstance(0x3F80);

    private int field_15_pattern_style;
    private int field_16_pattern_color_indexes;


    public PatternFormatting() {
        field_15_pattern_style = 0;
        field_16_pattern_color_indexes = 0;
    }

    public PatternFormatting(PatternFormatting other) {
        field_15_pattern_style = other.field_15_pattern_style;
        field_16_pattern_color_indexes = other.field_16_pattern_color_indexes;
    }

    public PatternFormatting(LittleEndianInput in) {
        field_15_pattern_style    = in.readUShort();
        field_16_pattern_color_indexes    = in.readUShort();
    }

    public int getDataLength() {
        return 4;
    }

    /**
     * setting fill pattern
     *
     * @see #NO_FILL
     * @see #SOLID_FOREGROUND
     * @see #FINE_DOTS
     * @see #ALT_BARS
     * @see #SPARSE_DOTS
     * @see #THICK_HORZ_BANDS
     * @see #THICK_VERT_BANDS
     * @see #THICK_BACKWARD_DIAG
     * @see #THICK_FORWARD_DIAG
     * @see #BIG_SPOTS
     * @see #BRICKS
     * @see #THIN_HORZ_BANDS
     * @see #THIN_VERT_BANDS
     * @see #THIN_BACKWARD_DIAG
     * @see #THIN_FORWARD_DIAG
     * @see #SQUARES
     * @see #DIAMONDS
     *
     * @param fp  fill pattern
     */
    public void setFillPattern(int fp) {
        field_15_pattern_style = fillPatternStyle.setValue(field_15_pattern_style, fp);
    }

    /**
     * @return fill pattern
     */
    public int getFillPattern() {
        return fillPatternStyle.getValue(field_15_pattern_style);
    }

    /**
     * set the background fill color.
     */
    public void setFillBackgroundColor(int bg) {
        field_16_pattern_color_indexes = patternBackgroundColorIndex.setValue(field_16_pattern_color_indexes,bg);
    }

    /**
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return get the background fill color
     */
    public int getFillBackgroundColor() {
        return patternBackgroundColorIndex.getValue(field_16_pattern_color_indexes);
    }

    /**
     * set the foreground fill color
     */
    public void setFillForegroundColor(int fg) {
        field_16_pattern_color_indexes = patternColorIndex.setValue(field_16_pattern_color_indexes,fg);
    }

    /**
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return get the foreground fill color
     */
    public int getFillForegroundColor() {
        return patternColorIndex.getValue(field_16_pattern_color_indexes);
    }

    @Override
    public Map<String, Supplier<?>> getGenericProperties() {
        return GenericRecordUtil.getGenericProperties(
            "fillPattern", getEnumBitsAsString(this::getFillPattern,
                 new int[]{NO_FILL, SOLID_FOREGROUND, FINE_DOTS, ALT_BARS, SPARSE_DOTS, THICK_HORZ_BANDS, THICK_VERT_BANDS, THICK_BACKWARD_DIAG, THICK_FORWARD_DIAG, BIG_SPOTS, BRICKS, THIN_HORZ_BANDS, THIN_VERT_BANDS, THIN_BACKWARD_DIAG, THIN_FORWARD_DIAG, SQUARES, DIAMONDS, LESS_DOTS, LEAST_DOTS},
                 new String[]{"NO_FILL", "SOLID_FOREGROUND", "FINE_DOTS", "ALT_BARS", "SPARSE_DOTS", "THICK_HORZ_BANDS", "THICK_VERT_BANDS", "THICK_BACKWARD_DIAG", "THICK_FORWARD_DIAG", "BIG_SPOTS", "BRICKS", "THIN_HORZ_BANDS", "THIN_VERT_BANDS", "THIN_BACKWARD_DIAG", "THIN_FORWARD_DIAG", "SQUARES", "DIAMONDS", "LESS_DOTS", "LEAST_DOTS"}),
            "fillForegroundColor", this::getFillForegroundColor,
            "fillBackgroundColor", this::getFillForegroundColor
        );
    }

    public String toString() {
        return GenericRecordJsonWriter.marshal(this);
    }

    public PatternFormatting copy()  {
        return new PatternFormatting(this);
    }

    public void serialize(LittleEndianOutput out) {
        out.writeShort(field_15_pattern_style);
        out.writeShort(field_16_pattern_color_indexes);
    }
}
