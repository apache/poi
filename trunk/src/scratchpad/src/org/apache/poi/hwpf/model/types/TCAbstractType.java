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

package org.apache.poi.hwpf.model.types;


import org.apache.poi.hwpf.usermodel.*;
import org.apache.poi.util.*;

/**
 * Table Cell Descriptor.
 * <p>
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/types/definitions.
 * <p>
 * This class is internal. It content or properties may change without notice 
 * due to changes in our knowledge of internal Microsoft Word binary structures.

 * @author S. Ryan Ackley. Field descriptions are quoted from Microsoft Office Word 97-2007 Binary
        File Format (.doc) Specification
    
 */
@Internal
public abstract class TCAbstractType
{

    protected short field_1_rgf;
    /**/private static BitField fFirstMerged = new BitField(0x0001);
    /**/private static BitField fMerged = new BitField(0x0002);
    /**/private static BitField fVertical = new BitField(0x0004);
    /**/private static BitField fBackward = new BitField(0x0008);
    /**/private static BitField fRotateFont = new BitField(0x0010);
    /**/private static BitField fVertMerge = new BitField(0x0020);
    /**/private static BitField fVertRestart = new BitField(0x0040);
    /**/private static BitField vertAlign = new BitField(0x0180);
    /**/private static BitField ftsWidth = new BitField(0x0E00);
    /**/private static BitField fFitText = new BitField(0x1000);
    /**/private static BitField fNoWrap = new BitField(0x2000);
    /**/private static BitField fUnused = new BitField(0xC000);
    protected short field_2_wWidth;
    protected ShadingDescriptor field_3_shd;
    protected short field_4_wCellPaddingLeft;
    protected short field_5_wCellPaddingTop;
    protected short field_6_wCellPaddingBottom;
    protected short field_7_wCellPaddingRight;
    protected byte field_8_ftsCellPaddingLeft;
    protected byte field_9_ftsCellPaddingTop;
    protected byte field_10_ftsCellPaddingBottom;
    protected byte field_11_ftsCellPaddingRight;
    protected short field_12_wCellSpacingLeft;
    protected short field_13_wCellSpacingTop;
    protected short field_14_wCellSpacingBottom;
    protected short field_15_wCellSpacingRight;
    protected byte field_16_ftsCellSpacingLeft;
    protected byte field_17_ftsCellSpacingTop;
    protected byte field_18_ftsCellSpacingBottom;
    protected byte field_19_ftsCellSpacingRight;
    protected BorderCode field_20_brcTop;
    protected BorderCode field_21_brcLeft;
    protected BorderCode field_22_brcBottom;
    protected BorderCode field_23_brcRight;

    protected TCAbstractType()
    {
        this.field_3_shd = new ShadingDescriptor();
        this.field_20_brcTop = new BorderCode();
        this.field_21_brcLeft = new BorderCode();
        this.field_22_brcBottom = new BorderCode();
        this.field_23_brcRight = new BorderCode();
    }


    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[TC]\n");
        builder.append("    .rgf                  = ");
        builder.append(" (").append(getRgf()).append(" )\n");
        builder.append("         .fFirstMerged             = ").append(isFFirstMerged()).append('\n');
        builder.append("         .fMerged                  = ").append(isFMerged()).append('\n');
        builder.append("         .fVertical                = ").append(isFVertical()).append('\n');
        builder.append("         .fBackward                = ").append(isFBackward()).append('\n');
        builder.append("         .fRotateFont              = ").append(isFRotateFont()).append('\n');
        builder.append("         .fVertMerge               = ").append(isFVertMerge()).append('\n');
        builder.append("         .fVertRestart             = ").append(isFVertRestart()).append('\n');
        builder.append("         .vertAlign                = ").append(getVertAlign()).append('\n');
        builder.append("         .ftsWidth                 = ").append(getFtsWidth()).append('\n');
        builder.append("         .fFitText                 = ").append(isFFitText()).append('\n');
        builder.append("         .fNoWrap                  = ").append(isFNoWrap()).append('\n');
        builder.append("         .fUnused                  = ").append(getFUnused()).append('\n');
        builder.append("    .wWidth               = ");
        builder.append(" (").append(getWWidth()).append(" )\n");
        builder.append("    .shd                  = ");
        builder.append(" (").append(getShd()).append(" )\n");
        builder.append("    .wCellPaddingLeft     = ");
        builder.append(" (").append(getWCellPaddingLeft()).append(" )\n");
        builder.append("    .wCellPaddingTop      = ");
        builder.append(" (").append(getWCellPaddingTop()).append(" )\n");
        builder.append("    .wCellPaddingBottom   = ");
        builder.append(" (").append(getWCellPaddingBottom()).append(" )\n");
        builder.append("    .wCellPaddingRight    = ");
        builder.append(" (").append(getWCellPaddingRight()).append(" )\n");
        builder.append("    .ftsCellPaddingLeft   = ");
        builder.append(" (").append(getFtsCellPaddingLeft()).append(" )\n");
        builder.append("    .ftsCellPaddingTop    = ");
        builder.append(" (").append(getFtsCellPaddingTop()).append(" )\n");
        builder.append("    .ftsCellPaddingBottom = ");
        builder.append(" (").append(getFtsCellPaddingBottom()).append(" )\n");
        builder.append("    .ftsCellPaddingRight  = ");
        builder.append(" (").append(getFtsCellPaddingRight()).append(" )\n");
        builder.append("    .wCellSpacingLeft     = ");
        builder.append(" (").append(getWCellSpacingLeft()).append(" )\n");
        builder.append("    .wCellSpacingTop      = ");
        builder.append(" (").append(getWCellSpacingTop()).append(" )\n");
        builder.append("    .wCellSpacingBottom   = ");
        builder.append(" (").append(getWCellSpacingBottom()).append(" )\n");
        builder.append("    .wCellSpacingRight    = ");
        builder.append(" (").append(getWCellSpacingRight()).append(" )\n");
        builder.append("    .ftsCellSpacingLeft   = ");
        builder.append(" (").append(getFtsCellSpacingLeft()).append(" )\n");
        builder.append("    .ftsCellSpacingTop    = ");
        builder.append(" (").append(getFtsCellSpacingTop()).append(" )\n");
        builder.append("    .ftsCellSpacingBottom = ");
        builder.append(" (").append(getFtsCellSpacingBottom()).append(" )\n");
        builder.append("    .ftsCellSpacingRight  = ");
        builder.append(" (").append(getFtsCellSpacingRight()).append(" )\n");
        builder.append("    .brcTop               = ");
        builder.append(" (").append(getBrcTop()).append(" )\n");
        builder.append("    .brcLeft              = ");
        builder.append(" (").append(getBrcLeft()).append(" )\n");
        builder.append("    .brcBottom            = ");
        builder.append(" (").append(getBrcBottom()).append(" )\n");
        builder.append("    .brcRight             = ");
        builder.append(" (").append(getBrcRight()).append(" )\n");

        builder.append("[/TC]\n");
        return builder.toString();
    }

    /**
     * Get the rgf field for the TC record.
     */
    @Internal
    public short getRgf()
    {
        return field_1_rgf;
    }

    /**
     * Set the rgf field for the TC record.
     */
    @Internal
    public void setRgf( short field_1_rgf )
    {
        this.field_1_rgf = field_1_rgf;
    }

    /**
     * Preferred cell width.
     */
    @Internal
    public short getWWidth()
    {
        return field_2_wWidth;
    }

    /**
     * Preferred cell width.
     */
    @Internal
    public void setWWidth( short field_2_wWidth )
    {
        this.field_2_wWidth = field_2_wWidth;
    }

    /**
     * Cell shading.
     */
    @Internal
    public ShadingDescriptor getShd()
    {
        return field_3_shd;
    }

    /**
     * Cell shading.
     */
    @Internal
    public void setShd( ShadingDescriptor field_3_shd )
    {
        this.field_3_shd = field_3_shd;
    }

    /**
     * Left cell margin/padding.
     */
    @Internal
    public short getWCellPaddingLeft()
    {
        return field_4_wCellPaddingLeft;
    }

    /**
     * Left cell margin/padding.
     */
    @Internal
    public void setWCellPaddingLeft( short field_4_wCellPaddingLeft )
    {
        this.field_4_wCellPaddingLeft = field_4_wCellPaddingLeft;
    }

    /**
     * Top cell margin/padding.
     */
    @Internal
    public short getWCellPaddingTop()
    {
        return field_5_wCellPaddingTop;
    }

    /**
     * Top cell margin/padding.
     */
    @Internal
    public void setWCellPaddingTop( short field_5_wCellPaddingTop )
    {
        this.field_5_wCellPaddingTop = field_5_wCellPaddingTop;
    }

    /**
     * Bottom cell margin/padding.
     */
    @Internal
    public short getWCellPaddingBottom()
    {
        return field_6_wCellPaddingBottom;
    }

    /**
     * Bottom cell margin/padding.
     */
    @Internal
    public void setWCellPaddingBottom( short field_6_wCellPaddingBottom )
    {
        this.field_6_wCellPaddingBottom = field_6_wCellPaddingBottom;
    }

    /**
     * Right cell margin/padding.
     */
    @Internal
    public short getWCellPaddingRight()
    {
        return field_7_wCellPaddingRight;
    }

    /**
     * Right cell margin/padding.
     */
    @Internal
    public void setWCellPaddingRight( short field_7_wCellPaddingRight )
    {
        this.field_7_wCellPaddingRight = field_7_wCellPaddingRight;
    }

    /**
     * Left cell margin/padding units.
     */
    @Internal
    public byte getFtsCellPaddingLeft()
    {
        return field_8_ftsCellPaddingLeft;
    }

    /**
     * Left cell margin/padding units.
     */
    @Internal
    public void setFtsCellPaddingLeft( byte field_8_ftsCellPaddingLeft )
    {
        this.field_8_ftsCellPaddingLeft = field_8_ftsCellPaddingLeft;
    }

    /**
     * Top cell margin/padding units.
     */
    @Internal
    public byte getFtsCellPaddingTop()
    {
        return field_9_ftsCellPaddingTop;
    }

    /**
     * Top cell margin/padding units.
     */
    @Internal
    public void setFtsCellPaddingTop( byte field_9_ftsCellPaddingTop )
    {
        this.field_9_ftsCellPaddingTop = field_9_ftsCellPaddingTop;
    }

    /**
     * Bottom cell margin/padding units.
     */
    @Internal
    public byte getFtsCellPaddingBottom()
    {
        return field_10_ftsCellPaddingBottom;
    }

    /**
     * Bottom cell margin/padding units.
     */
    @Internal
    public void setFtsCellPaddingBottom( byte field_10_ftsCellPaddingBottom )
    {
        this.field_10_ftsCellPaddingBottom = field_10_ftsCellPaddingBottom;
    }

    /**
     * Right cell margin/padding units.
     */
    @Internal
    public byte getFtsCellPaddingRight()
    {
        return field_11_ftsCellPaddingRight;
    }

    /**
     * Right cell margin/padding units.
     */
    @Internal
    public void setFtsCellPaddingRight( byte field_11_ftsCellPaddingRight )
    {
        this.field_11_ftsCellPaddingRight = field_11_ftsCellPaddingRight;
    }

    /**
     * Left cell spacing.
     */
    @Internal
    public short getWCellSpacingLeft()
    {
        return field_12_wCellSpacingLeft;
    }

    /**
     * Left cell spacing.
     */
    @Internal
    public void setWCellSpacingLeft( short field_12_wCellSpacingLeft )
    {
        this.field_12_wCellSpacingLeft = field_12_wCellSpacingLeft;
    }

    /**
     * Top cell spacing.
     */
    @Internal
    public short getWCellSpacingTop()
    {
        return field_13_wCellSpacingTop;
    }

    /**
     * Top cell spacing.
     */
    @Internal
    public void setWCellSpacingTop( short field_13_wCellSpacingTop )
    {
        this.field_13_wCellSpacingTop = field_13_wCellSpacingTop;
    }

    /**
     * Bottom cell spacing.
     */
    @Internal
    public short getWCellSpacingBottom()
    {
        return field_14_wCellSpacingBottom;
    }

    /**
     * Bottom cell spacing.
     */
    @Internal
    public void setWCellSpacingBottom( short field_14_wCellSpacingBottom )
    {
        this.field_14_wCellSpacingBottom = field_14_wCellSpacingBottom;
    }

    /**
     * Right cell spacing.
     */
    @Internal
    public short getWCellSpacingRight()
    {
        return field_15_wCellSpacingRight;
    }

    /**
     * Right cell spacing.
     */
    @Internal
    public void setWCellSpacingRight( short field_15_wCellSpacingRight )
    {
        this.field_15_wCellSpacingRight = field_15_wCellSpacingRight;
    }

    /**
     * Left cell spacing units.
     */
    @Internal
    public byte getFtsCellSpacingLeft()
    {
        return field_16_ftsCellSpacingLeft;
    }

    /**
     * Left cell spacing units.
     */
    @Internal
    public void setFtsCellSpacingLeft( byte field_16_ftsCellSpacingLeft )
    {
        this.field_16_ftsCellSpacingLeft = field_16_ftsCellSpacingLeft;
    }

    /**
     * Top cell spacing units.
     */
    @Internal
    public byte getFtsCellSpacingTop()
    {
        return field_17_ftsCellSpacingTop;
    }

    /**
     * Top cell spacing units.
     */
    @Internal
    public void setFtsCellSpacingTop( byte field_17_ftsCellSpacingTop )
    {
        this.field_17_ftsCellSpacingTop = field_17_ftsCellSpacingTop;
    }

    /**
     * Bottom cell spacing units.
     */
    @Internal
    public byte getFtsCellSpacingBottom()
    {
        return field_18_ftsCellSpacingBottom;
    }

    /**
     * Bottom cell spacing units.
     */
    @Internal
    public void setFtsCellSpacingBottom( byte field_18_ftsCellSpacingBottom )
    {
        this.field_18_ftsCellSpacingBottom = field_18_ftsCellSpacingBottom;
    }

    /**
     * Right cell spacing units.
     */
    @Internal
    public byte getFtsCellSpacingRight()
    {
        return field_19_ftsCellSpacingRight;
    }

    /**
     * Right cell spacing units.
     */
    @Internal
    public void setFtsCellSpacingRight( byte field_19_ftsCellSpacingRight )
    {
        this.field_19_ftsCellSpacingRight = field_19_ftsCellSpacingRight;
    }

    /**
     * Top border.
     */
    @Internal
    public BorderCode getBrcTop()
    {
        return field_20_brcTop;
    }

    /**
     * Top border.
     */
    @Internal
    public void setBrcTop( BorderCode field_20_brcTop )
    {
        this.field_20_brcTop = field_20_brcTop;
    }

    /**
     * Left border.
     */
    @Internal
    public BorderCode getBrcLeft()
    {
        return field_21_brcLeft;
    }

    /**
     * Left border.
     */
    @Internal
    public void setBrcLeft( BorderCode field_21_brcLeft )
    {
        this.field_21_brcLeft = field_21_brcLeft;
    }

    /**
     * Bottom border.
     */
    @Internal
    public BorderCode getBrcBottom()
    {
        return field_22_brcBottom;
    }

    /**
     * Bottom border.
     */
    @Internal
    public void setBrcBottom( BorderCode field_22_brcBottom )
    {
        this.field_22_brcBottom = field_22_brcBottom;
    }

    /**
     * Right border.
     */
    @Internal
    public BorderCode getBrcRight()
    {
        return field_23_brcRight;
    }

    /**
     * Right border.
     */
    @Internal
    public void setBrcRight( BorderCode field_23_brcRight )
    {
        this.field_23_brcRight = field_23_brcRight;
    }

    /**
     * Sets the fFirstMerged field value.
     * When 1, cell is first cell of a range of cells that have been merged. When a cell is merged, the display areas of the merged cells are consolidated and the text within the cells is interpreted as belonging to one text stream for purposes of calculating line breaks.
     */
    @Internal
    public void setFFirstMerged( boolean value )
    {
        field_1_rgf = (short)fFirstMerged.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, cell is first cell of a range of cells that have been merged. When a cell is merged, the display areas of the merged cells are consolidated and the text within the cells is interpreted as belonging to one text stream for purposes of calculating line breaks.
     * @return  the fFirstMerged field value.
     */
    @Internal
    public boolean isFFirstMerged()
    {
        return fFirstMerged.isSet(field_1_rgf);
    }

    /**
     * Sets the fMerged field value.
     * When 1, cell has been merged with preceding cell
     */
    @Internal
    public void setFMerged( boolean value )
    {
        field_1_rgf = (short)fMerged.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, cell has been merged with preceding cell
     * @return  the fMerged field value.
     */
    @Internal
    public boolean isFMerged()
    {
        return fMerged.isSet(field_1_rgf);
    }

    /**
     * Sets the fVertical field value.
     * When 1, cell has vertical text flow
     */
    @Internal
    public void setFVertical( boolean value )
    {
        field_1_rgf = (short)fVertical.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, cell has vertical text flow
     * @return  the fVertical field value.
     */
    @Internal
    public boolean isFVertical()
    {
        return fVertical.isSet(field_1_rgf);
    }

    /**
     * Sets the fBackward field value.
     * For a vertical table cell, text flow is bottom to top when 1 and is bottom to top when 0
     */
    @Internal
    public void setFBackward( boolean value )
    {
        field_1_rgf = (short)fBackward.setBoolean(field_1_rgf, value);
    }

    /**
     * For a vertical table cell, text flow is bottom to top when 1 and is bottom to top when 0
     * @return  the fBackward field value.
     */
    @Internal
    public boolean isFBackward()
    {
        return fBackward.isSet(field_1_rgf);
    }

    /**
     * Sets the fRotateFont field value.
     * When 1, cell has rotated characters (i.e. uses @font)
     */
    @Internal
    public void setFRotateFont( boolean value )
    {
        field_1_rgf = (short)fRotateFont.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, cell has rotated characters (i.e. uses @font)
     * @return  the fRotateFont field value.
     */
    @Internal
    public boolean isFRotateFont()
    {
        return fRotateFont.isSet(field_1_rgf);
    }

    /**
     * Sets the fVertMerge field value.
     * When 1, cell is vertically merged with the cell(s) above and/or below. When cells are vertically merged, the display area of the merged cells are consolidated. The consolidated area is used to display the contents of the first vertically merged cell (the cell with fVertRestart set to 1), and all other vertically merged cells (those with fVertRestart set to 0) must be empty. Cells can only be merged vertically if their left and right boundaries are (nearly) identical (i.e. if corresponding entries in rgdxaCenter of the table rows differ by at most 3).
     */
    @Internal
    public void setFVertMerge( boolean value )
    {
        field_1_rgf = (short)fVertMerge.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, cell is vertically merged with the cell(s) above and/or below. When cells are vertically merged, the display area of the merged cells are consolidated. The consolidated area is used to display the contents of the first vertically merged cell (the cell with fVertRestart set to 1), and all other vertically merged cells (those with fVertRestart set to 0) must be empty. Cells can only be merged vertically if their left and right boundaries are (nearly) identical (i.e. if corresponding entries in rgdxaCenter of the table rows differ by at most 3).
     * @return  the fVertMerge field value.
     */
    @Internal
    public boolean isFVertMerge()
    {
        return fVertMerge.isSet(field_1_rgf);
    }

    /**
     * Sets the fVertRestart field value.
     * When 1, the cell is the first of a set of vertically merged cells. The contents of a cell with fVertStart set to 1 are displayed in the consolidated area belonging to the entire set of vertically merged cells. Vertically merged cells with fVertRestart set to 0 must be empty.
     */
    @Internal
    public void setFVertRestart( boolean value )
    {
        field_1_rgf = (short)fVertRestart.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, the cell is the first of a set of vertically merged cells. The contents of a cell with fVertStart set to 1 are displayed in the consolidated area belonging to the entire set of vertically merged cells. Vertically merged cells with fVertRestart set to 0 must be empty.
     * @return  the fVertRestart field value.
     */
    @Internal
    public boolean isFVertRestart()
    {
        return fVertRestart.isSet(field_1_rgf);
    }

    /**
     * Sets the vertAlign field value.
     * Specifies the alignment of the cell contents relative to text flow (e.g. in a cell with bottom to top text flow and bottom vertical alignment, the text is shifted horizontally to match the cell's right boundary)
     */
    @Internal
    public void setVertAlign( byte value )
    {
        field_1_rgf = (short)vertAlign.setValue(field_1_rgf, value);
    }

    /**
     * Specifies the alignment of the cell contents relative to text flow (e.g. in a cell with bottom to top text flow and bottom vertical alignment, the text is shifted horizontally to match the cell's right boundary)
     * @return  the vertAlign field value.
     */
    @Internal
    public byte getVertAlign()
    {
        return ( byte )vertAlign.getValue(field_1_rgf);
    }

    /**
     * Sets the ftsWidth field value.
     * Units for wWidth
     */
    @Internal
    public void setFtsWidth( byte value )
    {
        field_1_rgf = (short)ftsWidth.setValue(field_1_rgf, value);
    }

    /**
     * Units for wWidth
     * @return  the ftsWidth field value.
     */
    @Internal
    public byte getFtsWidth()
    {
        return ( byte )ftsWidth.getValue(field_1_rgf);
    }

    /**
     * Sets the fFitText field value.
     * When 1, make the text fit the table cell
     */
    @Internal
    public void setFFitText( boolean value )
    {
        field_1_rgf = (short)fFitText.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, make the text fit the table cell
     * @return  the fFitText field value.
     */
    @Internal
    public boolean isFFitText()
    {
        return fFitText.isSet(field_1_rgf);
    }

    /**
     * Sets the fNoWrap field value.
     * When 1, do not allow text to wrap in the table cell
     */
    @Internal
    public void setFNoWrap( boolean value )
    {
        field_1_rgf = (short)fNoWrap.setBoolean(field_1_rgf, value);
    }

    /**
     * When 1, do not allow text to wrap in the table cell
     * @return  the fNoWrap field value.
     */
    @Internal
    public boolean isFNoWrap()
    {
        return fNoWrap.isSet(field_1_rgf);
    }

    /**
     * Sets the fUnused field value.
     * Not used
     */
    @Internal
    public void setFUnused( byte value )
    {
        field_1_rgf = (short)fUnused.setValue(field_1_rgf, value);
    }

    /**
     * Not used
     * @return  the fUnused field value.
     */
    @Internal
    public byte getFUnused()
    {
        return ( byte )fUnused.getValue(field_1_rgf);
    }

}  // END OF CLASS
