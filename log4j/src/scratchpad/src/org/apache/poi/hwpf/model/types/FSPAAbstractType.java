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


import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * File Shape Address (FSPA).
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format
 * 
 * <p>
 * NOTE: This source is automatically generated please do not modify this file.
 * Either subclass or remove the record in src/types/definitions.
 * <p>
 * This class is internal. It content or properties may change without notice
 * due to changes in our knowledge of internal Microsoft Word binary structures.
 * 
 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary
 *         File Format Specification [*.doc]
 */
@Internal
public abstract class FSPAAbstractType
{

    protected int field_1_spid;
    protected int field_2_xaLeft;
    protected int field_3_yaTop;
    protected int field_4_xaRight;
    protected int field_5_yaBottom;
    protected short field_6_flags;
    /**/private static BitField fHdr = new BitField(0x0001);
    /**/private static BitField bx = new BitField(0x0006);
    /**/private static BitField by = new BitField(0x0018);
    /**/private static BitField wr = new BitField(0x01E0);
    /**/private static BitField wrk = new BitField(0x1E00);
    /**/private static BitField fRcaSimple = new BitField(0x2000);
    /**/private static BitField fBelowText = new BitField(0x4000);
    /**/private static BitField fAnchorLock = new BitField(0x8000);
    protected int field_7_cTxbx;

    protected FSPAAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_spid                   = LittleEndian.getInt(data, 0x0 + offset);
        field_2_xaLeft                 = LittleEndian.getInt(data, 0x4 + offset);
        field_3_yaTop                  = LittleEndian.getInt(data, 0x8 + offset);
        field_4_xaRight                = LittleEndian.getInt(data, 0xc + offset);
        field_5_yaBottom               = LittleEndian.getInt(data, 0x10 + offset);
        field_6_flags                  = LittleEndian.getShort(data, 0x14 + offset);
        field_7_cTxbx                  = LittleEndian.getInt(data, 0x16 + offset);
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt(data, 0x0 + offset, field_1_spid);
        LittleEndian.putInt(data, 0x4 + offset, field_2_xaLeft);
        LittleEndian.putInt(data, 0x8 + offset, field_3_yaTop);
        LittleEndian.putInt(data, 0xc + offset, field_4_xaRight);
        LittleEndian.putInt(data, 0x10 + offset, field_5_yaBottom);
        LittleEndian.putShort(data, 0x14 + offset, field_6_flags);
        LittleEndian.putInt(data, 0x16 + offset, field_7_cTxbx);
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 4 + 4 + 4 + 4 + 4 + 2 + 4;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[FSPA]\n");
        builder.append("    .spid                 = ");
        builder.append(" (").append(getSpid()).append(" )\n");
        builder.append("    .xaLeft               = ");
        builder.append(" (").append(getXaLeft()).append(" )\n");
        builder.append("    .yaTop                = ");
        builder.append(" (").append(getYaTop()).append(" )\n");
        builder.append("    .xaRight              = ");
        builder.append(" (").append(getXaRight()).append(" )\n");
        builder.append("    .yaBottom             = ");
        builder.append(" (").append(getYaBottom()).append(" )\n");
        builder.append("    .flags                = ");
        builder.append(" (").append(getFlags()).append(" )\n");
        builder.append("         .fHdr                     = ").append(isFHdr()).append('\n');
        builder.append("         .bx                       = ").append(getBx()).append('\n');
        builder.append("         .by                       = ").append(getBy()).append('\n');
        builder.append("         .wr                       = ").append(getWr()).append('\n');
        builder.append("         .wrk                      = ").append(getWrk()).append('\n');
        builder.append("         .fRcaSimple               = ").append(isFRcaSimple()).append('\n');
        builder.append("         .fBelowText               = ").append(isFBelowText()).append('\n');
        builder.append("         .fAnchorLock              = ").append(isFAnchorLock()).append('\n');
        builder.append("    .cTxbx                = ");
        builder.append(" (").append(getCTxbx()).append(" )\n");

        builder.append("[/FSPA]\n");
        return builder.toString();
    }

    /**
     * Shape Identifier. Used in conjunction with the office art data (found via fcDggInfo in the FIB) to find the actual data for this shape.
     */
    @Internal
    public int getSpid()
    {
        return field_1_spid;
    }

    /**
     * Shape Identifier. Used in conjunction with the office art data (found via fcDggInfo in the FIB) to find the actual data for this shape.
     */
    @Internal
    public void setSpid( int field_1_spid )
    {
        this.field_1_spid = field_1_spid;
    }

    /**
     * Left of rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public int getXaLeft()
    {
        return field_2_xaLeft;
    }

    /**
     * Left of rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public void setXaLeft( int field_2_xaLeft )
    {
        this.field_2_xaLeft = field_2_xaLeft;
    }

    /**
     * Top of rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public int getYaTop()
    {
        return field_3_yaTop;
    }

    /**
     * Top of rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public void setYaTop( int field_3_yaTop )
    {
        this.field_3_yaTop = field_3_yaTop;
    }

    /**
     * Right of rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public int getXaRight()
    {
        return field_4_xaRight;
    }

    /**
     * Right of rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public void setXaRight( int field_4_xaRight )
    {
        this.field_4_xaRight = field_4_xaRight;
    }

    /**
     * Bottom of the rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public int getYaBottom()
    {
        return field_5_yaBottom;
    }

    /**
     * Bottom of the rectangle enclosing shape relative to the origin of the shape.
     */
    @Internal
    public void setYaBottom( int field_5_yaBottom )
    {
        this.field_5_yaBottom = field_5_yaBottom;
    }

    /**
     * Get the flags field for the FSPA record.
     */
    @Internal
    public short getFlags()
    {
        return field_6_flags;
    }

    /**
     * Set the flags field for the FSPA record.
     */
    @Internal
    public void setFlags( short field_6_flags )
    {
        this.field_6_flags = field_6_flags;
    }

    /**
     * Count of textboxes in shape (undo doc only).
     */
    @Internal
    public int getCTxbx()
    {
        return field_7_cTxbx;
    }

    /**
     * Count of textboxes in shape (undo doc only).
     */
    @Internal
    public void setCTxbx( int field_7_cTxbx )
    {
        this.field_7_cTxbx = field_7_cTxbx;
    }

    /**
     * Sets the fHdr field value.
     * 1 in the undo doc when shape is from the header doc, 0 otherwise (undefined when not in the undo doc)
     */
    @Internal
    public void setFHdr( boolean value )
    {
        field_6_flags = (short)fHdr.setBoolean(field_6_flags, value);
    }

    /**
     * 1 in the undo doc when shape is from the header doc, 0 otherwise (undefined when not in the undo doc)
     * @return  the fHdr field value.
     */
    @Internal
    public boolean isFHdr()
    {
        return fHdr.isSet(field_6_flags);
    }

    /**
     * Sets the bx field value.
     * X position of shape relative to anchor CP
     */
    @Internal
    public void setBx( byte value )
    {
        field_6_flags = (short)bx.setValue(field_6_flags, value);
    }

    /**
     * X position of shape relative to anchor CP
     * @return  the bx field value.
     */
    @Internal
    public byte getBx()
    {
        return ( byte )bx.getValue(field_6_flags);
    }

    /**
     * Sets the by field value.
     * Y position of shape relative to anchor CP
     */
    @Internal
    public void setBy( byte value )
    {
        field_6_flags = (short)by.setValue(field_6_flags, value);
    }

    /**
     * Y position of shape relative to anchor CP
     * @return  the by field value.
     */
    @Internal
    public byte getBy()
    {
        return ( byte )by.getValue(field_6_flags);
    }

    /**
     * Sets the wr field value.
     * Text wrapping mode
     */
    @Internal
    public void setWr( byte value )
    {
        field_6_flags = (short)wr.setValue(field_6_flags, value);
    }

    /**
     * Text wrapping mode
     * @return  the wr field value.
     */
    @Internal
    public byte getWr()
    {
        return ( byte )wr.getValue(field_6_flags);
    }

    /**
     * Sets the wrk field value.
     * Text wrapping mode type (valid only for wrapping modes 2 and 4
     */
    @Internal
    public void setWrk( byte value )
    {
        field_6_flags = (short)wrk.setValue(field_6_flags, value);
    }

    /**
     * Text wrapping mode type (valid only for wrapping modes 2 and 4
     * @return  the wrk field value.
     */
    @Internal
    public byte getWrk()
    {
        return ( byte )wrk.getValue(field_6_flags);
    }

    /**
     * Sets the fRcaSimple field value.
     * When set, temporarily overrides bx, by, forcing the xaLeft, xaRight, yaTop, and yaBottom fields to all be page relative.
     */
    @Internal
    public void setFRcaSimple( boolean value )
    {
        field_6_flags = (short)fRcaSimple.setBoolean(field_6_flags, value);
    }

    /**
     * When set, temporarily overrides bx, by, forcing the xaLeft, xaRight, yaTop, and yaBottom fields to all be page relative.
     * @return  the fRcaSimple field value.
     */
    @Internal
    public boolean isFRcaSimple()
    {
        return fRcaSimple.isSet(field_6_flags);
    }

    /**
     * Sets the fBelowText field value.
     * 
     */
    @Internal
    public void setFBelowText( boolean value )
    {
        field_6_flags = (short)fBelowText.setBoolean(field_6_flags, value);
    }

    /**
     * 
     * @return  the fBelowText field value.
     */
    @Internal
    public boolean isFBelowText()
    {
        return fBelowText.isSet(field_6_flags);
    }

    /**
     * Sets the fAnchorLock field value.
     * 
     */
    @Internal
    public void setFAnchorLock( boolean value )
    {
        field_6_flags = (short)fAnchorLock.setBoolean(field_6_flags, value);
    }

    /**
     * 
     * @return  the fAnchorLock field value.
     */
    @Internal
    public boolean isFAnchorLock()
    {
        return fAnchorLock.isSet(field_6_flags);
    }

}  // END OF CLASS
