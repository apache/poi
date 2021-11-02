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


import java.util.Objects;

import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The FFData structure specifies form field data for a text box, check box, or drop-down list box.
 */
@Internal
public abstract class FFDataBaseAbstractType
{

    protected long field_1_version;
    protected short field_2_bits;
    /**/private static final BitField iType = new BitField(0x0003);
    /**   Specifies that the form field is a textbox. */
    /*  */public static final byte ITYPE_TEXT = 0;
    /**   Specifies that the form field is a checkbox. */
    /*  */public static final byte ITYPE_CHCK = 1;
    /**   Specifies that the form field is a dropdown list box. */
    /*  */public static final byte ITYPE_DROP = 2;
    /**/private static final BitField iRes = new BitField(0x007C);
    /**/private static final BitField fOwnHelp = new BitField(0x0080);
    /**/private static final BitField fOwnStat = new BitField(0x0100);
    /**/private static final BitField fProt = new BitField(0x0200);
    /**/private static final BitField iSize = new BitField(0x0400);
    /**/private static final BitField iTypeTxt = new BitField(0x3800);
    /**   Specifies that the textbox value is regular text. */
    /*  */public static final byte ITYPETXT_REG = 0;
    /**   Specifies that the textbox value is a number. */
    /*  */public static final byte ITYPETXT_NUM = 0;
    /**   Specifies that the textbox value is a date or time. */
    /*  */public static final byte ITYPETXT_DATE = 0;
    /**   Specifies that the textbox value is the current date. */
    /*  */public static final byte ITYPETXT_CURDATE = 0;
    /**   Specifies that the textbox value is the current time. */
    /*  */public static final byte ITYPETXT_CURTIME = 0;
    /**   Specifies that the textbox value is calculated from an expression. The expression is given by FFData.xstzTextDef. */
    /*  */protected static final byte ITYPETXT_CALC = 0;
    /**/private static final BitField fRecalc = new BitField(0x4000);
    /**/private static final BitField fHasListBox = new BitField(0x8000);
    protected int field_3_cch;
    protected int field_4_hps;

    protected FFDataBaseAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_version                = LittleEndian.getUInt( data, 0x0 + offset );
        field_2_bits                   = LittleEndian.getShort( data, 0x4 + offset );
        field_3_cch                    = LittleEndian.getShort( data, 0x6 + offset );
        field_4_hps                    = LittleEndian.getShort( data, 0x8 + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putUInt( data, 0x0 + offset, field_1_version );
        LittleEndian.putShort( data, 0x4 + offset, field_2_bits );
        LittleEndian.putUShort( data, 0x6 + offset, field_3_cch );
        LittleEndian.putUShort( data, 0x8 + offset, field_4_hps );
    }

    public byte[] serialize()
    {
        final byte[] result = new byte[ getSize() ];
        serialize( result, 0 );
        return result;
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 4 + 2 + 2 + 2;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        FFDataBaseAbstractType other = (FFDataBaseAbstractType) obj;
        if ( field_1_version != other.field_1_version )
            return false;
        if ( field_2_bits != other.field_2_bits )
            return false;
        if ( field_3_cch != other.field_3_cch )
            return false;
        if ( field_4_hps != other.field_4_hps )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_version, field_2_bits, field_3_cch, field_4_hps);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("[FFDataBase]\n");
        builder.append( "    .version              = " );
        builder.append(" ( ").append( field_1_version ).append( " )\n" );
        builder.append( "    .bits                 = " );
        builder.append(" ( ").append( field_2_bits ).append( " )\n" );
        builder.append("         .iType                    = ").append(getIType()).append('\n');
        builder.append("         .iRes                     = ").append(getIRes()).append('\n');
        builder.append("         .fOwnHelp                 = ").append(isFOwnHelp()).append('\n');
        builder.append("         .fOwnStat                 = ").append(isFOwnStat()).append('\n');
        builder.append("         .fProt                    = ").append(isFProt()).append('\n');
        builder.append("         .iSize                    = ").append(isISize()).append('\n');
        builder.append("         .iTypeTxt                 = ").append(getITypeTxt()).append('\n');
        builder.append("         .fRecalc                  = ").append(isFRecalc()).append('\n');
        builder.append("         .fHasListBox              = ").append(isFHasListBox()).append('\n');
        builder.append( "    .cch                  = " );
        builder.append(" ( ").append( field_3_cch ).append( " )\n" );
        builder.append( "    .hps                  = " );
        builder.append(" ( ").append( field_4_hps ).append( " )\n" );

        builder.append("[/FFDataBase]");
        return builder.toString();
    }

    /**
     * An unsigned integer that MUST be 0xFFFFFFFF.
     */
    @Internal
    public long getVersion()
    {
        return field_1_version;
    }

    /**
     * An unsigned integer that MUST be 0xFFFFFFFF.
     */
    @Internal
    public void setVersion( long field_1_version )
    {
        this.field_1_version = field_1_version;
    }

    /**
     * An FFDataBits that specifies the type and state of this form field.
     */
    @Internal
    public short getBits()
    {
        return field_2_bits;
    }

    /**
     * An FFDataBits that specifies the type and state of this form field.
     */
    @Internal
    public void setBits( short field_2_bits )
    {
        this.field_2_bits = field_2_bits;
    }

    /**
     * An unsigned integer that specifies the maximum length, in characters, of the value of the textbox. This value MUST NOT exceed 32767. A value of 0 means there is no maximum length of the value of the textbox. If bits.iType is not iTypeText (0), this value MUST be 0..
     */
    @Internal
    public int getCch()
    {
        return field_3_cch;
    }

    /**
     * An unsigned integer that specifies the maximum length, in characters, of the value of the textbox. This value MUST NOT exceed 32767. A value of 0 means there is no maximum length of the value of the textbox. If bits.iType is not iTypeText (0), this value MUST be 0..
     */
    @Internal
    public void setCch( int field_3_cch )
    {
        this.field_3_cch = field_3_cch;
    }

    /**
     * An unsigned integer. If bits.iType is iTypeChck (1), hps specifies the size, in half-points, of the checkbox and MUST be between 2 and 3168, inclusive. If bits.iType is not iTypeChck (1), hps is undefined and MUST be ignored..
     */
    @Internal
    public int getHps()
    {
        return field_4_hps;
    }

    /**
     * An unsigned integer. If bits.iType is iTypeChck (1), hps specifies the size, in half-points, of the checkbox and MUST be between 2 and 3168, inclusive. If bits.iType is not iTypeChck (1), hps is undefined and MUST be ignored..
     */
    @Internal
    public void setHps( int field_4_hps )
    {
        this.field_4_hps = field_4_hps;
    }

    /**
     * Sets the iType field value.
     * An unsigned integer that specifies the type of the form field.
     */
    @Internal
    public void setIType( byte value )
    {
        field_2_bits = (short)iType.setValue(field_2_bits, value);
    }

    /**
     * An unsigned integer that specifies the type of the form field.
     * @return  the iType field value.
     */
    @Internal
    public byte getIType()
    {
        return ( byte )iType.getValue(field_2_bits);
    }

    /**
     * Sets the iRes field value.
     * An unsigned integer. If iType is iTypeText (0), then iRes MUST be 0. If iType is iTypeChck (1), iRes specifies the state of the checkbox and MUST be 0 (unchecked), 1 (checked), or 25 (undefined). Undefined checkboxes are treated as unchecked. If iType is iTypeDrop (2), iRes specifies the current selected list box item. A value of 25 specifies the selection is undefined. Otherwise, iRes is a zero-based index into FFData.hsttbDropList.
     */
    @Internal
    public void setIRes( byte value )
    {
        field_2_bits = (short)iRes.setValue(field_2_bits, value);
    }

    /**
     * An unsigned integer. If iType is iTypeText (0), then iRes MUST be 0. If iType is iTypeChck (1), iRes specifies the state of the checkbox and MUST be 0 (unchecked), 1 (checked), or 25 (undefined). Undefined checkboxes are treated as unchecked. If iType is iTypeDrop (2), iRes specifies the current selected list box item. A value of 25 specifies the selection is undefined. Otherwise, iRes is a zero-based index into FFData.hsttbDropList.
     * @return  the iRes field value.
     */
    @Internal
    public byte getIRes()
    {
        return ( byte )iRes.getValue(field_2_bits);
    }

    /**
     * Sets the fOwnHelp field value.
     * A bit that specifies whether the form field has custom help text in FFData.xstzHelpText. If fOwnHelp is 0, FFData.xstzHelpText contains an empty or auto-generated string.
     */
    @Internal
    public void setFOwnHelp( boolean value )
    {
        field_2_bits = (short)fOwnHelp.setBoolean(field_2_bits, value);
    }

    /**
     * A bit that specifies whether the form field has custom help text in FFData.xstzHelpText. If fOwnHelp is 0, FFData.xstzHelpText contains an empty or auto-generated string.
     * @return  the fOwnHelp field value.
     */
    @Internal
    public boolean isFOwnHelp()
    {
        return fOwnHelp.isSet(field_2_bits);
    }

    /**
     * Sets the fOwnStat field value.
     * A bit that specifies whether the form field has custom status bar text in FFData.xstzStatText. If fOwnStat is 0, FFData.xstzStatText contains an empty or auto-generated string.
     */
    @Internal
    public void setFOwnStat( boolean value )
    {
        field_2_bits = (short)fOwnStat.setBoolean(field_2_bits, value);
    }

    /**
     * A bit that specifies whether the form field has custom status bar text in FFData.xstzStatText. If fOwnStat is 0, FFData.xstzStatText contains an empty or auto-generated string.
     * @return  the fOwnStat field value.
     */
    @Internal
    public boolean isFOwnStat()
    {
        return fOwnStat.isSet(field_2_bits);
    }

    /**
     * Sets the fProt field value.
     * A bit that specifies whether the form field is protected and its value cannot be changed.
     */
    @Internal
    public void setFProt( boolean value )
    {
        field_2_bits = (short)fProt.setBoolean(field_2_bits, value);
    }

    /**
     * A bit that specifies whether the form field is protected and its value cannot be changed.
     * @return  the fProt field value.
     */
    @Internal
    public boolean isFProt()
    {
        return fProt.isSet(field_2_bits);
    }

    /**
     * Sets the iSize field value.
     * A bit that specifies whether the size of a checkbox is automatically determined by the text size where the checkbox is located. This value MUST be 0 if iType is not iTypeChck (1).
     */
    @Internal
    public void setISize( boolean value )
    {
        field_2_bits = (short)iSize.setBoolean(field_2_bits, value);
    }

    /**
     * A bit that specifies whether the size of a checkbox is automatically determined by the text size where the checkbox is located. This value MUST be 0 if iType is not iTypeChck (1).
     * @return  the iSize field value.
     */
    @Internal
    public boolean isISize()
    {
        return iSize.isSet(field_2_bits);
    }

    /**
     * Sets the iTypeTxt field value.
     * An unsigned integer that specifies the type of the textbox. If iType is not iTypeText (0), iTypeTxt MUST be 0 and MUST be ignored.
     */
    @Internal
    public void setITypeTxt( byte value )
    {
        field_2_bits = (short)iTypeTxt.setValue(field_2_bits, value);
    }

    /**
     * An unsigned integer that specifies the type of the textbox. If iType is not iTypeText (0), iTypeTxt MUST be 0 and MUST be ignored.
     * @return  the iTypeTxt field value.
     */
    @Internal
    public byte getITypeTxt()
    {
        return ( byte )iTypeTxt.getValue(field_2_bits);
    }

    /**
     * Sets the fRecalc field value.
     * A bit that specifies whether the value of the field is automatically calculated after the field is modified.
     */
    @Internal
    public void setFRecalc( boolean value )
    {
        field_2_bits = (short)fRecalc.setBoolean(field_2_bits, value);
    }

    /**
     * A bit that specifies whether the value of the field is automatically calculated after the field is modified.
     * @return  the fRecalc field value.
     */
    @Internal
    public boolean isFRecalc()
    {
        return fRecalc.isSet(field_2_bits);
    }

    /**
     * Sets the fHasListBox field value.
     * A bit that specifies that the form field has a list box. This value MUST be 1 if iType is iTypeDrop (2). Otherwise, this value MUST be 0.
     */
    @Internal
    public void setFHasListBox( boolean value )
    {
        field_2_bits = (short)fHasListBox.setBoolean(field_2_bits, value);
    }

    /**
     * A bit that specifies that the form field has a list box. This value MUST be 1 if iType is iTypeDrop (2). Otherwise, this value MUST be 0.
     * @return  the fHasListBox field value.
     */
    @Internal
    public boolean isFHasListBox()
    {
        return fHasListBox.isSet(field_2_bits);
    }

}  // END OF CLASS
