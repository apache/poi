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

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianInput;
import org.apache.poi.util.LittleEndianOutput;

/**
 * Border Formatting Block of the Conditional Formatting Rule Record.
 */
public final class BorderFormatting implements Cloneable {
    /** No border */
    public final static short    BORDER_NONE                = 0x0;
    /** Thin border */
    public final static short    BORDER_THIN                = 0x1;
    /** Medium border */
    public final static short    BORDER_MEDIUM              = 0x2;
    /** dash border */
    public final static short    BORDER_DASHED              = 0x3;
    /** dot border */
    public final static short    BORDER_HAIR                = 0x4;
    /** Thick border */
    public final static short    BORDER_THICK               = 0x5;
    /** double-line border */
    public final static short    BORDER_DOUBLE              = 0x6;
    /** hair-line border */
    public final static short    BORDER_DOTTED              = 0x7;
    /** Medium dashed border */
    public final static short    BORDER_MEDIUM_DASHED       = 0x8;
    /** dash-dot border */
    public final static short    BORDER_DASH_DOT            = 0x9;
    /** medium dash-dot border */
    public final static short    BORDER_MEDIUM_DASH_DOT     = 0xA;
    /** dash-dot-dot border */
    public final static short    BORDER_DASH_DOT_DOT        = 0xB;
    /** medium dash-dot-dot border */
    public final static short    BORDER_MEDIUM_DASH_DOT_DOT = 0xC;
    /** slanted dash-dot border */
    public final static short    BORDER_SLANTED_DASH_DOT    = 0xD;

    // BORDER FORMATTING BLOCK
    // For Border Line Style codes see HSSFCellStyle.BORDER_XXXXXX
    private int              field_13_border_styles1;
    private static final BitField  bordLeftLineStyle  = BitFieldFactory.getInstance(0x0000000F);
    private static final BitField  bordRightLineStyle = BitFieldFactory.getInstance(0x000000F0);
    private static final BitField  bordTopLineStyle   = BitFieldFactory.getInstance(0x00000F00);
    private static final BitField  bordBottomLineStyle= BitFieldFactory.getInstance(0x0000F000);
    private static final BitField  bordLeftLineColor  = BitFieldFactory.getInstance(0x007F0000);
    private static final BitField  bordRightLineColor = BitFieldFactory.getInstance(0x3F800000);
    private static final BitField  bordTlBrLineOnOff  = BitFieldFactory.getInstance(0x40000000);
    private static final BitField  bordBlTrtLineOnOff = BitFieldFactory.getInstance(0x80000000);

    private int              field_14_border_styles2;
    private static final BitField  bordTopLineColor   = BitFieldFactory.getInstance(0x0000007F);
    private static final BitField  bordBottomLineColor= BitFieldFactory.getInstance(0x00003f80);
    private static final BitField  bordDiagLineColor  = BitFieldFactory.getInstance(0x001FC000);
    private static final BitField  bordDiagLineStyle  = BitFieldFactory.getInstance(0x01E00000);


    public BorderFormatting() {
        field_13_border_styles1    = 0;
        field_14_border_styles2    = 0;
    }

    /** Creates new FontFormatting */
    public BorderFormatting(LittleEndianInput in) {
        field_13_border_styles1    = in.readInt();
        field_14_border_styles2    = in.readInt();
    }

    public int getDataLength() {
        return 8;
    }

    /**
     * set the type of border to use for the left border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public void setBorderLeft(int border) {
        field_13_border_styles1 = bordLeftLineStyle.setValue(field_13_border_styles1, border);
    }

    /**
     * get the type of border to use for the left border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public int getBorderLeft() {
        return bordLeftLineStyle.getValue(field_13_border_styles1);
    }

    /**
     * set the type of border to use for the right border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public void setBorderRight(int border) {
        field_13_border_styles1 = bordRightLineStyle.setValue(field_13_border_styles1, border);
    }

    /**
     * get the type of border to use for the right border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public int getBorderRight() {
        return bordRightLineStyle.getValue(field_13_border_styles1);
    }

    /**
     * set the type of border to use for the top border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public void setBorderTop(int border) {
        field_13_border_styles1 = bordTopLineStyle.setValue(field_13_border_styles1, border);
    }

    /**
     * get the type of border to use for the top border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public int getBorderTop() {
        return bordTopLineStyle.getValue(field_13_border_styles1);
    }

    /**
     * set the type of border to use for the bottom border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public void setBorderBottom(int border) {
        field_13_border_styles1 = bordBottomLineStyle.setValue(field_13_border_styles1, border);
    }

    /**
     * get the type of border to use for the bottom border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public int getBorderBottom() {
        return bordBottomLineStyle.getValue(field_13_border_styles1);
    }

    /**
     * set the type of border to use for the diagonal border of the cell
     * @param border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public void setBorderDiagonal(int border) {
        field_14_border_styles2 = bordDiagLineStyle.setValue(field_14_border_styles2, border);
    }

    /**
     * get the type of border to use for the diagonal border of the cell
     * @return border type
     * @see #BORDER_NONE
     * @see #BORDER_THIN
     * @see #BORDER_MEDIUM
     * @see #BORDER_DASHED
     * @see #BORDER_DOTTED
     * @see #BORDER_THICK
     * @see #BORDER_DOUBLE
     * @see #BORDER_HAIR
     * @see #BORDER_MEDIUM_DASHED
     * @see #BORDER_DASH_DOT
     * @see #BORDER_MEDIUM_DASH_DOT
     * @see #BORDER_DASH_DOT_DOT
     * @see #BORDER_MEDIUM_DASH_DOT_DOT
     * @see #BORDER_SLANTED_DASH_DOT
     */
    public int getBorderDiagonal() {
        return bordDiagLineStyle.getValue(field_14_border_styles2);
    }

    /**
     * set the color to use for the left border
     * @param color The index of the color definition
     */
    public void setLeftBorderColor(int color) {
        field_13_border_styles1 = bordLeftLineColor.setValue(field_13_border_styles1, color);
    }

    /**
     * get the color to use for the left border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return  The index of the color definition
     */
    public int getLeftBorderColor() {
        return bordLeftLineColor.getValue(field_13_border_styles1);
    }

    /**
     * set the color to use for the right border
     * @param color The index of the color definition
     */
    public void setRightBorderColor(int color) {
        field_13_border_styles1 = bordRightLineColor.setValue(field_13_border_styles1, color);
    }

    /**
     * get the color to use for the right border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public int getRightBorderColor() {
        return bordRightLineColor.getValue(field_13_border_styles1);
    }

    /**
     * set the color to use for the top border
     * @param color The index of the color definition
     */
    public void setTopBorderColor(int color) {
        field_14_border_styles2 = bordTopLineColor.setValue(field_14_border_styles2, color);
    }

    /**
     * get the color to use for the top border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public int getTopBorderColor() {
        return bordTopLineColor.getValue(field_14_border_styles2);
    }

    /**
     * set the color to use for the bottom border
     * @param color The index of the color definition
     */
    public void setBottomBorderColor(int color)
    {
        field_14_border_styles2 = bordBottomLineColor.setValue(field_14_border_styles2, color);
    }

    /**
     * get the color to use for the bottom border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public int getBottomBorderColor() {
        return bordBottomLineColor.getValue(field_14_border_styles2);
    }

    /**
     * set the color to use for the diagonal borders
     * @param color The index of the color definition
     */
    public void setDiagonalBorderColor(int color) {
        field_14_border_styles2 = bordDiagLineColor.setValue(field_14_border_styles2, color);
    }

    /**
     * get the color to use for the diagonal border
     * @see org.apache.poi.hssf.usermodel.HSSFPalette#getColor(short)
     * @return The index of the color definition
     */
    public int getDiagonalBorderColor() {
        return bordDiagLineColor.getValue(field_14_border_styles2);
    }

    /**
     * Of/off bottom left to top right line
     *
     * @param on - if <code>true</code> - on, otherwise off
     */
    public void setForwardDiagonalOn(boolean on) {
        field_13_border_styles1 = bordBlTrtLineOnOff.setBoolean(field_13_border_styles1, on);
    }

    /**
     * Of/off top left to bottom right line
     *
     * @param on - if <code>true</code> - on, otherwise off
     */
    public void setBackwardDiagonalOn(boolean on) {
        field_13_border_styles1 = bordTlBrLineOnOff.setBoolean(field_13_border_styles1, on);
    }

    /**
     * @return <code>true</code> if forward diagonal is on
     */
    public boolean isForwardDiagonalOn() {
        return bordBlTrtLineOnOff.isSet(field_13_border_styles1);
    }

    /**
     * @return <code>true</code> if backward diagonal is on
     */
    public boolean isBackwardDiagonalOn() {
        return bordTlBrLineOnOff.isSet(field_13_border_styles1);
    }


    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("    [Border Formatting]\n");
        buffer.append("          .lftln     = ").append(Integer.toHexString(getBorderLeft())).append("\n");
        buffer.append("          .rgtln     = ").append(Integer.toHexString(getBorderRight())).append("\n");
        buffer.append("          .topln     = ").append(Integer.toHexString(getBorderTop())).append("\n");
        buffer.append("          .btmln     = ").append(Integer.toHexString(getBorderBottom())).append("\n");
        buffer.append("          .leftborder= ").append(Integer.toHexString(getLeftBorderColor())).append("\n");
        buffer.append("          .rghtborder= ").append(Integer.toHexString(getRightBorderColor())).append("\n");
        buffer.append("          .topborder= ").append(Integer.toHexString(getTopBorderColor())).append("\n");
        buffer.append("          .bottomborder= ").append(Integer.toHexString(getBottomBorderColor())).append("\n");
        buffer.append("          .fwdiag= ").append(isForwardDiagonalOn()).append("\n");
        buffer.append("          .bwdiag= ").append(isBackwardDiagonalOn()).append("\n");
        buffer.append("    [/Border Formatting]\n");
        return buffer.toString();
    }

    @Override
    public BorderFormatting clone() {
      BorderFormatting rec = new BorderFormatting();
      rec.field_13_border_styles1 = field_13_border_styles1;
      rec.field_14_border_styles2 = field_14_border_styles2;
      return rec;
    }

    public int serialize(int offset, byte [] data) {
        LittleEndian.putInt(data, offset+0, field_13_border_styles1);
        LittleEndian.putInt(data, offset+4, field_14_border_styles2);
        return 8;
    }
    public void serialize(LittleEndianOutput out) {
        out.writeInt(field_13_border_styles1);
        out.writeInt(field_14_border_styles2);
    }
}
