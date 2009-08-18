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

import org.apache.poi.hdf.model.hdftypes.HDFType;

/**
 * Table Properties.
 * NOTE: This source is automatically generated please do not modify this file.  Either subclass or
 *       remove the record in src/records/definitions.

 * @author S. Ryan Ackley
 */
public abstract class TAPAbstractType
    implements HDFType
{

    private  int field_1_jc;
    private  int field_2_dxaGapHalf;
    private  int field_3_dyaRowHeight;
    private  boolean field_4_fCantSplit;
    private  boolean field_5_fTableHeader;
    private  int field_6_tlp;
    private  short field_7_itcMac;
    private  short[] field_8_rgdxaCenter;
    private  TCAbstractType[] field_9_rgtc;
    private  byte[] field_10_rgshd;
    private  short[] field_11_brcBottom;
    private  short[] field_12_brcTop;
    private  short[] field_13_brcLeft;
    private  short[] field_14_brcRight;
    private  short[] field_15_brcVertical;
    private  short[] field_16_brcHorizontal;


    public TAPAbstractType()
    {

    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 +  + 2 + 4 + 4 + 0 + 0 + 4 + 2 + 130 + 0 + 0 + 4 + 4 + 4 + 4 + 4 + 4;
    }



    /**
     * Get the jc field for the TAP record.
     */
    public int getJc()
    {
        return field_1_jc;
    }

    /**
     * Set the jc field for the TAP record.
     */
    public void setJc(int field_1_jc)
    {
        this.field_1_jc = field_1_jc;
    }

    /**
     * Get the dxaGapHalf field for the TAP record.
     */
    public int getDxaGapHalf()
    {
        return field_2_dxaGapHalf;
    }

    /**
     * Set the dxaGapHalf field for the TAP record.
     */
    public void setDxaGapHalf(int field_2_dxaGapHalf)
    {
        this.field_2_dxaGapHalf = field_2_dxaGapHalf;
    }

    /**
     * Get the dyaRowHeight field for the TAP record.
     */
    public int getDyaRowHeight()
    {
        return field_3_dyaRowHeight;
    }

    /**
     * Set the dyaRowHeight field for the TAP record.
     */
    public void setDyaRowHeight(int field_3_dyaRowHeight)
    {
        this.field_3_dyaRowHeight = field_3_dyaRowHeight;
    }

    /**
     * Get the fCantSplit field for the TAP record.
     */
    public boolean getFCantSplit()
    {
        return field_4_fCantSplit;
    }

    /**
     * Set the fCantSplit field for the TAP record.
     */
    public void setFCantSplit(boolean field_4_fCantSplit)
    {
        this.field_4_fCantSplit = field_4_fCantSplit;
    }

    /**
     * Get the fTableHeader field for the TAP record.
     */
    public boolean getFTableHeader()
    {
        return field_5_fTableHeader;
    }

    /**
     * Set the fTableHeader field for the TAP record.
     */
    public void setFTableHeader(boolean field_5_fTableHeader)
    {
        this.field_5_fTableHeader = field_5_fTableHeader;
    }

    /**
     * Get the tlp field for the TAP record.
     */
    public int getTlp()
    {
        return field_6_tlp;
    }

    /**
     * Set the tlp field for the TAP record.
     */
    public void setTlp(int field_6_tlp)
    {
        this.field_6_tlp = field_6_tlp;
    }

    /**
     * Get the itcMac field for the TAP record.
     */
    public short getItcMac()
    {
        return field_7_itcMac;
    }

    /**
     * Set the itcMac field for the TAP record.
     */
    public void setItcMac(short field_7_itcMac)
    {
        this.field_7_itcMac = field_7_itcMac;
    }

    /**
     * Get the rgdxaCenter field for the TAP record.
     */
    public short[] getRgdxaCenter()
    {
        return field_8_rgdxaCenter;
    }

    /**
     * Set the rgdxaCenter field for the TAP record.
     */
    public void setRgdxaCenter(short[] field_8_rgdxaCenter)
    {
        this.field_8_rgdxaCenter = field_8_rgdxaCenter;
    }

    /**
     * Get the rgtc field for the TAP record.
     */
    public TCAbstractType[] getRgtc()
    {
        return field_9_rgtc;
    }

    /**
     * Set the rgtc field for the TAP record.
     */
    public void setRgtc(TCAbstractType[] field_9_rgtc)
    {
        this.field_9_rgtc = field_9_rgtc;
    }

    /**
     * Get the rgshd field for the TAP record.
     */
    public byte[] getRgshd()
    {
        return field_10_rgshd;
    }

    /**
     * Set the rgshd field for the TAP record.
     */
    public void setRgshd(byte[] field_10_rgshd)
    {
        this.field_10_rgshd = field_10_rgshd;
    }

    /**
     * Get the brcBottom field for the TAP record.
     */
    public short[] getBrcBottom()
    {
        return field_11_brcBottom;
    }

    /**
     * Set the brcBottom field for the TAP record.
     */
    public void setBrcBottom(short[] field_11_brcBottom)
    {
        this.field_11_brcBottom = field_11_brcBottom;
    }

    /**
     * Get the brcTop field for the TAP record.
     */
    public short[] getBrcTop()
    {
        return field_12_brcTop;
    }

    /**
     * Set the brcTop field for the TAP record.
     */
    public void setBrcTop(short[] field_12_brcTop)
    {
        this.field_12_brcTop = field_12_brcTop;
    }

    /**
     * Get the brcLeft field for the TAP record.
     */
    public short[] getBrcLeft()
    {
        return field_13_brcLeft;
    }

    /**
     * Set the brcLeft field for the TAP record.
     */
    public void setBrcLeft(short[] field_13_brcLeft)
    {
        this.field_13_brcLeft = field_13_brcLeft;
    }

    /**
     * Get the brcRight field for the TAP record.
     */
    public short[] getBrcRight()
    {
        return field_14_brcRight;
    }

    /**
     * Set the brcRight field for the TAP record.
     */
    public void setBrcRight(short[] field_14_brcRight)
    {
        this.field_14_brcRight = field_14_brcRight;
    }

    /**
     * Get the brcVertical field for the TAP record.
     */
    public short[] getBrcVertical()
    {
        return field_15_brcVertical;
    }

    /**
     * Set the brcVertical field for the TAP record.
     */
    public void setBrcVertical(short[] field_15_brcVertical)
    {
        this.field_15_brcVertical = field_15_brcVertical;
    }

    /**
     * Get the brcHorizontal field for the TAP record.
     */
    public short[] getBrcHorizontal()
    {
        return field_16_brcHorizontal;
    }

    /**
     * Set the brcHorizontal field for the TAP record.
     */
    public void setBrcHorizontal(short[] field_16_brcHorizontal)
    {
        this.field_16_brcHorizontal = field_16_brcHorizontal;
    }


}  // END OF CLASS




