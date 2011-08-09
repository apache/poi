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



import org.apache.poi.hdf.model.hdftypes.HDFType;
import org.apache.poi.hwpf.usermodel.BorderCode;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;

/**
 * Table Cell Descriptor.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
@Internal
public abstract class TCAbstractType
    implements HDFType
{

    protected  short field_1_rgf;
        private static BitField  fFirstMerged = new BitField(0x0001);
        private static BitField  fMerged = new BitField(0x0002);
        private static BitField  fVertical = new BitField(0x0004);
        private static BitField  fBackward = new BitField(0x0008);
        private static BitField  fRotateFont = new BitField(0x0010);
        private static BitField  fVertMerge = new BitField(0x0020);
        private static BitField  fVertRestart = new BitField(0x0040);
        private static BitField  vertAlign = new BitField(0x0180);
        private static BitField  ftsWidth = new BitField(0x0E00);
        private static BitField  fFitText = new BitField(0x1000);
        private static BitField  fNoWrap = new BitField(0x2000);
        private static BitField  fUnused = new BitField(0xC000);
    protected  short field_2_wWidth;
    protected  short field_3_wCellPaddingLeft;
    protected  short field_4_wCellPaddingTop;
    protected  short field_5_wCellPaddingBottom;
    protected  short field_6_wCellPaddingRight;
    protected  byte field_7_ftsCellPaddingLeft;
    protected  byte field_8_ftsCellPaddingTop;
    protected  byte field_9_ftsCellPaddingBottom;
    protected  byte field_10_ftsCellPaddingRight;
    protected  short field_11_wCellSpacingLeft;
    protected  short field_12_wCellSpacingTop;
    protected  short field_13_wCellSpacingBottom;
    protected  short field_14_wCellSpacingRight;
    protected  byte field_15_ftsCellSpacingLeft;
    protected  byte field_16_ftsCellSpacingTop;
    protected  byte field_17_ftsCellSpacingBottom;
    protected  byte field_18_ftsCellSpacingRight;
    protected  BorderCode field_19_brcTop;
    protected  BorderCode field_20_brcLeft;
    protected  BorderCode field_21_brcBottom;
    protected  BorderCode field_22_brcRight;


    public TCAbstractType()
    {

    }


    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append("[TC]\n");

        buffer.append("    .rgf                  = ");
        buffer.append(" (").append(getRgf()).append(" )\n");
        buffer.append("         .fFirstMerged             = ").append(isFFirstMerged()).append('\n');
        buffer.append("         .fMerged                  = ").append(isFMerged()).append('\n');
        buffer.append("         .fVertical                = ").append(isFVertical()).append('\n');
        buffer.append("         .fBackward                = ").append(isFBackward()).append('\n');
        buffer.append("         .fRotateFont              = ").append(isFRotateFont()).append('\n');
        buffer.append("         .fVertMerge               = ").append(isFVertMerge()).append('\n');
        buffer.append("         .fVertRestart             = ").append(isFVertRestart()).append('\n');
        buffer.append("         .vertAlign                = ").append(getVertAlign()).append('\n');
        buffer.append("         .ftsWidth                 = ").append(getFtsWidth()).append('\n');
        buffer.append("         .fFitText                 = ").append(isFFitText()).append('\n');
        buffer.append("         .fNoWrap                  = ").append(isFNoWrap()).append('\n');
        buffer.append("         .fUnused                  = ").append(getFUnused()).append('\n');

        buffer.append("    .wWidth               = ");
        buffer.append(" (").append(getWWidth()).append(" )\n");

        buffer.append("    .wCellPaddingLeft     = ");
        buffer.append(" (").append(getWCellPaddingLeft()).append(" )\n");

        buffer.append("    .wCellPaddingTop      = ");
        buffer.append(" (").append(getWCellPaddingTop()).append(" )\n");

        buffer.append("    .wCellPaddingBottom   = ");
        buffer.append(" (").append(getWCellPaddingBottom()).append(" )\n");

        buffer.append("    .wCellPaddingRight    = ");
        buffer.append(" (").append(getWCellPaddingRight()).append(" )\n");

        buffer.append("    .ftsCellPaddingLeft   = ");
        buffer.append(" (").append(getFtsCellPaddingLeft()).append(" )\n");

        buffer.append("    .ftsCellPaddingTop    = ");
        buffer.append(" (").append(getFtsCellPaddingTop()).append(" )\n");

        buffer.append("    .ftsCellPaddingBottom = ");
        buffer.append(" (").append(getFtsCellPaddingBottom()).append(" )\n");

        buffer.append("    .ftsCellPaddingRight  = ");
        buffer.append(" (").append(getFtsCellPaddingRight()).append(" )\n");

        buffer.append("    .wCellSpacingLeft     = ");
        buffer.append(" (").append(getWCellSpacingLeft()).append(" )\n");

        buffer.append("    .wCellSpacingTop      = ");
        buffer.append(" (").append(getWCellSpacingTop()).append(" )\n");

        buffer.append("    .wCellSpacingBottom   = ");
        buffer.append(" (").append(getWCellSpacingBottom()).append(" )\n");

        buffer.append("    .wCellSpacingRight    = ");
        buffer.append(" (").append(getWCellSpacingRight()).append(" )\n");

        buffer.append("    .ftsCellSpacingLeft   = ");
        buffer.append(" (").append(getFtsCellSpacingLeft()).append(" )\n");

        buffer.append("    .ftsCellSpacingTop    = ");
        buffer.append(" (").append(getFtsCellSpacingTop()).append(" )\n");

        buffer.append("    .ftsCellSpacingBottom = ");
        buffer.append(" (").append(getFtsCellSpacingBottom()).append(" )\n");

        buffer.append("    .ftsCellSpacingRight  = ");
        buffer.append(" (").append(getFtsCellSpacingRight()).append(" )\n");

        buffer.append("    .brcTop               = ");
        buffer.append(" (").append(getBrcTop()).append(" )\n");

        buffer.append("    .brcLeft              = ");
        buffer.append(" (").append(getBrcLeft()).append(" )\n");

        buffer.append("    .brcBottom            = ");
        buffer.append(" (").append(getBrcBottom()).append(" )\n");

        buffer.append("    .brcRight             = ");
        buffer.append(" (").append(getBrcRight()).append(" )\n");

        buffer.append("[/TC]\n");
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 2 + 2 + 2 + 2 + 2 + 2 + 1 + 1 + 1 + 1 + 2 + 2 + 2 + 2 + 1 + 1 + 1 + 1 + 4 + 4 + 4 + 4;
    }



    /**
     * Get the rgf field for the TC record.
     */
    public short getRgf()
    {
        return field_1_rgf;
    }

    /**
     * Set the rgf field for the TC record.
     */
    public void setRgf(short field_1_rgf)
    {
        this.field_1_rgf = field_1_rgf;
    }

    /**
     * Get the wWidth field for the TC record.
     */
    public short getWWidth()
    {
        return field_2_wWidth;
    }

    /**
     * Set the wWidth field for the TC record.
     */
    public void setWWidth(short field_2_wWidth)
    {
        this.field_2_wWidth = field_2_wWidth;
    }

    /**
     * Get the wCellPaddingLeft field for the TC record.
     */
    public short getWCellPaddingLeft()
    {
        return field_3_wCellPaddingLeft;
    }

    /**
     * Set the wCellPaddingLeft field for the TC record.
     */
    public void setWCellPaddingLeft(short field_3_wCellPaddingLeft)
    {
        this.field_3_wCellPaddingLeft = field_3_wCellPaddingLeft;
    }

    /**
     * Get the wCellPaddingTop field for the TC record.
     */
    public short getWCellPaddingTop()
    {
        return field_4_wCellPaddingTop;
    }

    /**
     * Set the wCellPaddingTop field for the TC record.
     */
    public void setWCellPaddingTop(short field_4_wCellPaddingTop)
    {
        this.field_4_wCellPaddingTop = field_4_wCellPaddingTop;
    }

    /**
     * Get the wCellPaddingBottom field for the TC record.
     */
    public short getWCellPaddingBottom()
    {
        return field_5_wCellPaddingBottom;
    }

    /**
     * Set the wCellPaddingBottom field for the TC record.
     */
    public void setWCellPaddingBottom(short field_5_wCellPaddingBottom)
    {
        this.field_5_wCellPaddingBottom = field_5_wCellPaddingBottom;
    }

    /**
     * Get the wCellPaddingRight field for the TC record.
     */
    public short getWCellPaddingRight()
    {
        return field_6_wCellPaddingRight;
    }

    /**
     * Set the wCellPaddingRight field for the TC record.
     */
    public void setWCellPaddingRight(short field_6_wCellPaddingRight)
    {
        this.field_6_wCellPaddingRight = field_6_wCellPaddingRight;
    }

    /**
     * Get the ftsCellPaddingLeft field for the TC record.
     */
    public byte getFtsCellPaddingLeft()
    {
        return field_7_ftsCellPaddingLeft;
    }

    /**
     * Set the ftsCellPaddingLeft field for the TC record.
     */
    public void setFtsCellPaddingLeft(byte field_7_ftsCellPaddingLeft)
    {
        this.field_7_ftsCellPaddingLeft = field_7_ftsCellPaddingLeft;
    }

    /**
     * Get the ftsCellPaddingTop field for the TC record.
     */
    public byte getFtsCellPaddingTop()
    {
        return field_8_ftsCellPaddingTop;
    }

    /**
     * Set the ftsCellPaddingTop field for the TC record.
     */
    public void setFtsCellPaddingTop(byte field_8_ftsCellPaddingTop)
    {
        this.field_8_ftsCellPaddingTop = field_8_ftsCellPaddingTop;
    }

    /**
     * Get the ftsCellPaddingBottom field for the TC record.
     */
    public byte getFtsCellPaddingBottom()
    {
        return field_9_ftsCellPaddingBottom;
    }

    /**
     * Set the ftsCellPaddingBottom field for the TC record.
     */
    public void setFtsCellPaddingBottom(byte field_9_ftsCellPaddingBottom)
    {
        this.field_9_ftsCellPaddingBottom = field_9_ftsCellPaddingBottom;
    }

    /**
     * Get the ftsCellPaddingRight field for the TC record.
     */
    public byte getFtsCellPaddingRight()
    {
        return field_10_ftsCellPaddingRight;
    }

    /**
     * Set the ftsCellPaddingRight field for the TC record.
     */
    public void setFtsCellPaddingRight(byte field_10_ftsCellPaddingRight)
    {
        this.field_10_ftsCellPaddingRight = field_10_ftsCellPaddingRight;
    }

    /**
     * Get the wCellSpacingLeft field for the TC record.
     */
    public short getWCellSpacingLeft()
    {
        return field_11_wCellSpacingLeft;
    }

    /**
     * Set the wCellSpacingLeft field for the TC record.
     */
    public void setWCellSpacingLeft(short field_11_wCellSpacingLeft)
    {
        this.field_11_wCellSpacingLeft = field_11_wCellSpacingLeft;
    }

    /**
     * Get the wCellSpacingTop field for the TC record.
     */
    public short getWCellSpacingTop()
    {
        return field_12_wCellSpacingTop;
    }

    /**
     * Set the wCellSpacingTop field for the TC record.
     */
    public void setWCellSpacingTop(short field_12_wCellSpacingTop)
    {
        this.field_12_wCellSpacingTop = field_12_wCellSpacingTop;
    }

    /**
     * Get the wCellSpacingBottom field for the TC record.
     */
    public short getWCellSpacingBottom()
    {
        return field_13_wCellSpacingBottom;
    }

    /**
     * Set the wCellSpacingBottom field for the TC record.
     */
    public void setWCellSpacingBottom(short field_13_wCellSpacingBottom)
    {
        this.field_13_wCellSpacingBottom = field_13_wCellSpacingBottom;
    }

    /**
     * Get the wCellSpacingRight field for the TC record.
     */
    public short getWCellSpacingRight()
    {
        return field_14_wCellSpacingRight;
    }

    /**
     * Set the wCellSpacingRight field for the TC record.
     */
    public void setWCellSpacingRight(short field_14_wCellSpacingRight)
    {
        this.field_14_wCellSpacingRight = field_14_wCellSpacingRight;
    }

    /**
     * Get the ftsCellSpacingLeft field for the TC record.
     */
    public byte getFtsCellSpacingLeft()
    {
        return field_15_ftsCellSpacingLeft;
    }

    /**
     * Set the ftsCellSpacingLeft field for the TC record.
     */
    public void setFtsCellSpacingLeft(byte field_15_ftsCellSpacingLeft)
    {
        this.field_15_ftsCellSpacingLeft = field_15_ftsCellSpacingLeft;
    }

    /**
     * Get the ftsCellSpacingTop field for the TC record.
     */
    public byte getFtsCellSpacingTop()
    {
        return field_16_ftsCellSpacingTop;
    }

    /**
     * Set the ftsCellSpacingTop field for the TC record.
     */
    public void setFtsCellSpacingTop(byte field_16_ftsCellSpacingTop)
    {
        this.field_16_ftsCellSpacingTop = field_16_ftsCellSpacingTop;
    }

    /**
     * Get the ftsCellSpacingBottom field for the TC record.
     */
    public byte getFtsCellSpacingBottom()
    {
        return field_17_ftsCellSpacingBottom;
    }

    /**
     * Set the ftsCellSpacingBottom field for the TC record.
     */
    public void setFtsCellSpacingBottom(byte field_17_ftsCellSpacingBottom)
    {
        this.field_17_ftsCellSpacingBottom = field_17_ftsCellSpacingBottom;
    }

    /**
     * Get the ftsCellSpacingRight field for the TC record.
     */
    public byte getFtsCellSpacingRight()
    {
        return field_18_ftsCellSpacingRight;
    }

    /**
     * Set the ftsCellSpacingRight field for the TC record.
     */
    public void setFtsCellSpacingRight(byte field_18_ftsCellSpacingRight)
    {
        this.field_18_ftsCellSpacingRight = field_18_ftsCellSpacingRight;
    }

    /**
     * Get the brcTop field for the TC record.
     */
    public BorderCode getBrcTop()
    {
        return field_19_brcTop;
    }

    /**
     * Set the brcTop field for the TC record.
     */
    public void setBrcTop(BorderCode field_19_brcTop)
    {
        this.field_19_brcTop = field_19_brcTop;
    }

    /**
     * Get the brcLeft field for the TC record.
     */
    public BorderCode getBrcLeft()
    {
        return field_20_brcLeft;
    }

    /**
     * Set the brcLeft field for the TC record.
     */
    public void setBrcLeft(BorderCode field_20_brcLeft)
    {
        this.field_20_brcLeft = field_20_brcLeft;
    }

    /**
     * Get the brcBottom field for the TC record.
     */
    public BorderCode getBrcBottom()
    {
        return field_21_brcBottom;
    }

    /**
     * Set the brcBottom field for the TC record.
     */
    public void setBrcBottom(BorderCode field_21_brcBottom)
    {
        this.field_21_brcBottom = field_21_brcBottom;
    }

    /**
     * Get the brcRight field for the TC record.
     */
    public BorderCode getBrcRight()
    {
        return field_22_brcRight;
    }

    /**
     * Set the brcRight field for the TC record.
     */
    public void setBrcRight(BorderCode field_22_brcRight)
    {
        this.field_22_brcRight = field_22_brcRight;
    }

    /**
     * Sets the fFirstMerged field value.
     * 
     */
    public void setFFirstMerged(boolean value)
    {
        field_1_rgf = (short)fFirstMerged.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fFirstMerged field value.
     */
    public boolean isFFirstMerged()
    {
        return fFirstMerged.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fMerged field value.
     * 
     */
    public void setFMerged(boolean value)
    {
        field_1_rgf = (short)fMerged.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fMerged field value.
     */
    public boolean isFMerged()
    {
        return fMerged.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fVertical field value.
     * 
     */
    public void setFVertical(boolean value)
    {
        field_1_rgf = (short)fVertical.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fVertical field value.
     */
    public boolean isFVertical()
    {
        return fVertical.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fBackward field value.
     * 
     */
    public void setFBackward(boolean value)
    {
        field_1_rgf = (short)fBackward.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fBackward field value.
     */
    public boolean isFBackward()
    {
        return fBackward.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fRotateFont field value.
     * 
     */
    public void setFRotateFont(boolean value)
    {
        field_1_rgf = (short)fRotateFont.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fRotateFont field value.
     */
    public boolean isFRotateFont()
    {
        return fRotateFont.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fVertMerge field value.
     * 
     */
    public void setFVertMerge(boolean value)
    {
        field_1_rgf = (short)fVertMerge.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fVertMerge field value.
     */
    public boolean isFVertMerge()
    {
        return fVertMerge.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fVertRestart field value.
     * 
     */
    public void setFVertRestart(boolean value)
    {
        field_1_rgf = (short)fVertRestart.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fVertRestart field value.
     */
    public boolean isFVertRestart()
    {
        return fVertRestart.isSet(field_1_rgf);
        
    }

    /**
     * Sets the vertAlign field value.
     * 
     */
    public void setVertAlign(byte value)
    {
        field_1_rgf = (short)vertAlign.setValue(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the vertAlign field value.
     */
    public byte getVertAlign()
    {
        return ( byte )vertAlign.getValue(field_1_rgf);
        
    }

    /**
     * Sets the ftsWidth field value.
     * 
     */
    public void setFtsWidth(byte value)
    {
        field_1_rgf = (short)ftsWidth.setValue(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the ftsWidth field value.
     */
    public byte getFtsWidth()
    {
        return ( byte )ftsWidth.getValue(field_1_rgf);
        
    }

    /**
     * Sets the fFitText field value.
     * 
     */
    public void setFFitText(boolean value)
    {
        field_1_rgf = (short)fFitText.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fFitText field value.
     */
    public boolean isFFitText()
    {
        return fFitText.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fNoWrap field value.
     * 
     */
    public void setFNoWrap(boolean value)
    {
        field_1_rgf = (short)fNoWrap.setBoolean(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fNoWrap field value.
     */
    public boolean isFNoWrap()
    {
        return fNoWrap.isSet(field_1_rgf);
        
    }

    /**
     * Sets the fUnused field value.
     * 
     */
    public void setFUnused(byte value)
    {
        field_1_rgf = (short)fUnused.setValue(field_1_rgf, value);

        
    }

    /**
     * 
     * @return  the fUnused field value.
     */
    public byte getFUnused()
    {
        return ( byte )fUnused.getValue(field_1_rgf);
        
    }


}  // END OF CLASS




