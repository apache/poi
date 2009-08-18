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

package org.apache.poi.hdf.model.hdftypes.definitions;

import org.apache.poi.util.BitField;
import org.apache.poi.util.BitFieldFactory;
import org.apache.poi.hdf.model.hdftypes.HDFType;

/**
 * Table Cell Descriptor.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class TCAbstractType
    implements HDFType
{

    private  short field_1_rgf;
        private static BitField  fFirstMerged = BitFieldFactory.getInstance(0x0001);
        private static BitField  fMerged = BitFieldFactory.getInstance(0x0002);
        private static BitField  fVertical = BitFieldFactory.getInstance(0x0004);
        private static BitField  fBackward = BitFieldFactory.getInstance(0x0008);
        private static BitField  fRotateFont = BitFieldFactory.getInstance(0x0010);
        private static BitField  fVertMerge = BitFieldFactory.getInstance(0x0020);
        private static BitField  fVertRestart = BitFieldFactory.getInstance(0x0040);
        private static BitField  vertAlign = BitFieldFactory.getInstance(0x0180);
    private  short field_2_unused;
    private  short[] field_3_brcTop;
    private  short[] field_4_brcLeft;
    private  short[] field_5_brcBottom;
    private  short[] field_6_brcRight;


    public TCAbstractType()
    {

    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 2 + 2 + 4 + 4 + 4 + 4;
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
     * Get the unused field for the TC record.
     */
    public short getUnused()
    {
        return field_2_unused;
    }

    /**
     * Set the unused field for the TC record.
     */
    public void setUnused(short field_2_unused)
    {
        this.field_2_unused = field_2_unused;
    }

    /**
     * Get the brcTop field for the TC record.
     */
    public short[] getBrcTop()
    {
        return field_3_brcTop;
    }

    /**
     * Set the brcTop field for the TC record.
     */
    public void setBrcTop(short[] field_3_brcTop)
    {
        this.field_3_brcTop = field_3_brcTop;
    }

    /**
     * Get the brcLeft field for the TC record.
     */
    public short[] getBrcLeft()
    {
        return field_4_brcLeft;
    }

    /**
     * Set the brcLeft field for the TC record.
     */
    public void setBrcLeft(short[] field_4_brcLeft)
    {
        this.field_4_brcLeft = field_4_brcLeft;
    }

    /**
     * Get the brcBottom field for the TC record.
     */
    public short[] getBrcBottom()
    {
        return field_5_brcBottom;
    }

    /**
     * Set the brcBottom field for the TC record.
     */
    public void setBrcBottom(short[] field_5_brcBottom)
    {
        this.field_5_brcBottom = field_5_brcBottom;
    }

    /**
     * Get the brcRight field for the TC record.
     */
    public short[] getBrcRight()
    {
        return field_6_brcRight;
    }

    /**
     * Set the brcRight field for the TC record.
     */
    public void setBrcRight(short[] field_6_brcRight)
    {
        this.field_6_brcRight = field_6_brcRight;
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


}  // END OF CLASS




