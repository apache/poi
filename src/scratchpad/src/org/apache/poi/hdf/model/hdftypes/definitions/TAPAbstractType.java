
/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hdf.model.hdftypes.definitions;



import org.apache.poi.util.BitField;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;
import org.apache.poi.util.HexDump;
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
    private  byte field_4_fCantSplit;
    private  byte field_5_fTableHeader;
    private  int field_6_tlp;
    private  int field_7_itcMac;
    private  short[] field_8_rgdxaCenter;
    private  byte[] field_9_rgtc;
    private  byte[] field_10_rgshd;
    private  byte[] field_11_rgbrcTable;


    public TAPAbstractType()
    {

    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 + 2 + 4 + 4 + 1 + 1 + 4 + 2 + 130 + 0 + 0 + 0;
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
    public byte getFCantSplit()
    {
        return field_4_fCantSplit;
    }

    /**
     * Set the fCantSplit field for the TAP record.
     */
    public void setFCantSplit(byte field_4_fCantSplit)
    {
        this.field_4_fCantSplit = field_4_fCantSplit;
    }

    /**
     * Get the fTableHeader field for the TAP record.
     */
    public byte getFTableHeader()
    {
        return field_5_fTableHeader;
    }

    /**
     * Set the fTableHeader field for the TAP record.
     */
    public void setFTableHeader(byte field_5_fTableHeader)
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
    public int getItcMac()
    {
        return field_7_itcMac;
    }

    /**
     * Set the itcMac field for the TAP record.
     */
    public void setItcMac(int field_7_itcMac)
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
    public byte[] getRgtc()
    {
        return field_9_rgtc;
    }

    /**
     * Set the rgtc field for the TAP record.
     */
    public void setRgtc(byte[] field_9_rgtc)
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
     * Get the rgbrcTable field for the TAP record.
     */
    public byte[] getRgbrcTable()
    {
        return field_11_rgbrcTable;
    }

    /**
     * Set the rgbrcTable field for the TAP record.
     */
    public void setRgbrcTable(byte[] field_11_rgbrcTable)
    {
        this.field_11_rgbrcTable = field_11_rgbrcTable;
    }


}  // END OF CLASS




