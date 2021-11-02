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

import java.util.Arrays;

import org.apache.poi.hwpf.model.Grfhic;
import org.apache.poi.util.BitField;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;

/**
 * The LSTF structure contains formatting properties that apply to an entire list.
 */
@Internal
public abstract class LSTFAbstractType
{

    protected int field_1_lsid;
    protected int field_2_tplc;
    protected short[] field_3_rgistdPara;
    protected byte field_4_flags;
    /**/private static final BitField fSimpleList = new BitField(0x01);
    /**/private static final BitField unused1 = new BitField(0x02);
    /**/private static final BitField fAutoNum = new BitField(0x04);
    /**/private static final BitField unused2 = new BitField(0x08);
    /**/private static final BitField fHybrid = new BitField(0x10);
    /**/private static final BitField reserved1 = new BitField(0xE0);
    protected Grfhic field_5_grfhic;

    protected LSTFAbstractType()
    {
        this.field_3_rgistdPara = new short[0];
        this.field_5_grfhic = new Grfhic();
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_lsid                   = LittleEndian.getInt( data, 0x0 + offset );
        field_2_tplc                   = LittleEndian.getInt( data, 0x4 + offset );
        field_3_rgistdPara             = LittleEndian.getShortArray( data, 0x8 + offset, 18 );
        field_4_flags                  = data[ 0x1a + offset ];
        field_5_grfhic                 = new Grfhic( data, 0x1b + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_lsid );
        LittleEndian.putInt( data, 0x4 + offset, field_2_tplc );
        LittleEndian.putShortArray( data, 0x8 + offset, field_3_rgistdPara );
        data[ 0x1a + offset ] = field_4_flags;
        field_5_grfhic.serialize( data, 0x1b + offset );
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
        return 0 + 4 + 4 + 18 + 1 + 1;
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
        LSTFAbstractType other = (LSTFAbstractType) obj;
        if ( field_1_lsid != other.field_1_lsid )
            return false;
        if ( field_2_tplc != other.field_2_tplc )
            return false;
        if ( !Arrays.equals( field_3_rgistdPara, other.field_3_rgistdPara ) )
            return false;
        if ( field_4_flags != other.field_4_flags )
            return false;
        if ( field_5_grfhic == null )
        {
            if ( other.field_5_grfhic != null )
                return false;
        }
        else if ( !field_5_grfhic.equals( other.field_5_grfhic ) )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{field_1_lsid, field_2_tplc, field_3_rgistdPara, field_4_flags, field_5_grfhic});
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[LSTF]\n");
        builder.append("    .lsid                 = ");
        builder.append(" (").append(getLsid()).append(" )\n");
        builder.append("    .tplc                 = ");
        builder.append(" (").append(getTplc()).append(" )\n");
        builder.append("    .rgistdPara           = ");
        builder.append(" (").append(Arrays.toString(getRgistdPara())).append(" )\n");
        builder.append("    .flags                = ");
        builder.append(" (").append(getFlags()).append(" )\n");
        builder.append("         .fSimpleList              = ").append(isFSimpleList()).append('\n');
        builder.append("         .unused1                  = ").append(isUnused1()).append('\n');
        builder.append("         .fAutoNum                 = ").append(isFAutoNum()).append('\n');
        builder.append("         .unused2                  = ").append(isUnused2()).append('\n');
        builder.append("         .fHybrid                  = ").append(isFHybrid()).append('\n');
        builder.append("         .reserved1                = ").append(getReserved1()).append('\n');
        builder.append("    .grfhic               = ");
        builder.append(" (").append(getGrfhic()).append(" )\n");

        builder.append("[/LSTF]\n");
        return builder.toString();
    }

    /**
     * A signed integer that specifies the list identifier. This MUST be unique for each LSTF. This value MUST not be 0xFFFFFFFF.
     */
    @Internal
    public int getLsid()
    {
        return field_1_lsid;
    }

    /**
     * A signed integer that specifies the list identifier. This MUST be unique for each LSTF. This value MUST not be 0xFFFFFFFF.
     */
    @Internal
    public void setLsid( int field_1_lsid )
    {
        this.field_1_lsid = field_1_lsid;
    }

    /**
     * A Tplc that specifies a unique identifier for this LSTF that MAY be used for user interface purposes. If fHybrid is nonzero, this MUST be ignored.
     */
    @Internal
    public int getTplc()
    {
        return field_2_tplc;
    }

    /**
     * A Tplc that specifies a unique identifier for this LSTF that MAY be used for user interface purposes. If fHybrid is nonzero, this MUST be ignored.
     */
    @Internal
    public void setTplc( int field_2_tplc )
    {
        this.field_2_tplc = field_2_tplc;
    }

    /**
     * An array of nine 16-bit signed integers. Each element of rgistdPara specifies the ISTD of the style that is linked to the corresponding level in the list. If no style is linked to a given level, the value of the corresponding element of rgistdPara MUST be 0x0FFF.
     */
    @Internal
    public short[] getRgistdPara()
    {
        return field_3_rgistdPara;
    }

    /**
     * An array of nine 16-bit signed integers. Each element of rgistdPara specifies the ISTD of the style that is linked to the corresponding level in the list. If no style is linked to a given level, the value of the corresponding element of rgistdPara MUST be 0x0FFF.
     */
    @Internal
    public void setRgistdPara( short[] field_3_rgistdPara )
    {
        this.field_3_rgistdPara = field_3_rgistdPara;
    }

    /**
     * Get the flags field for the LSTF record.
     */
    @Internal
    public byte getFlags()
    {
        return field_4_flags;
    }

    /**
     * Set the flags field for the LSTF record.
     */
    @Internal
    public void setFlags( byte field_4_flags )
    {
        this.field_4_flags = field_4_flags;
    }

    /**
     * A grfhic that specifies the HTML incompatibilities of the list..
     */
    @Internal
    public Grfhic getGrfhic()
    {
        return field_5_grfhic;
    }

    /**
     * A grfhic that specifies the HTML incompatibilities of the list..
     */
    @Internal
    public void setGrfhic( Grfhic field_5_grfhic )
    {
        this.field_5_grfhic = field_5_grfhic;
    }

    /**
     * Sets the fSimpleList field value.
     * A bit that, when set to 0x1, specifies that this LSTF represents a simple (one-level) list that has one corresponding LVL (see the fcPlfLst field of FibRgFcLcb97). Otherwise, this LSTF represents a multi-level list that has nine corresponding LVLs
     */
    @Internal
    public void setFSimpleList( boolean value )
    {
        field_4_flags = (byte)fSimpleList.setBoolean(field_4_flags, value);
    }

    /**
     * A bit that, when set to 0x1, specifies that this LSTF represents a simple (one-level) list that has one corresponding LVL (see the fcPlfLst field of FibRgFcLcb97). Otherwise, this LSTF represents a multi-level list that has nine corresponding LVLs
     * @return  the fSimpleList field value.
     */
    @Internal
    public boolean isFSimpleList()
    {
        return fSimpleList.isSet(field_4_flags);
    }

    /**
     * Sets the unused1 field value.
     * This bit MUST be ignored
     */
    @Internal
    public void setUnused1( boolean value )
    {
        field_4_flags = (byte)unused1.setBoolean(field_4_flags, value);
    }

    /**
     * This bit MUST be ignored
     * @return  the unused1 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public boolean isUnused1()
    {
        return unused1.isSet(field_4_flags);
    }

    /**
     * Sets the fAutoNum field value.
     * A bit that specifies whether the list that this LSTF represents is used for the AUTONUMOUT, AUTONUMLGL, and AUTONUM fields (see AUTONUMOUT, AUTONUMLGL, and AUTONUM in flt)
     */
    @Internal
    public void setFAutoNum( boolean value )
    {
        field_4_flags = (byte)fAutoNum.setBoolean(field_4_flags, value);
    }

    /**
     * A bit that specifies whether the list that this LSTF represents is used for the AUTONUMOUT, AUTONUMLGL, and AUTONUM fields (see AUTONUMOUT, AUTONUMLGL, and AUTONUM in flt)
     * @return  the fAutoNum field value.
     */
    @Internal
    public boolean isFAutoNum()
    {
        return fAutoNum.isSet(field_4_flags);
    }

    /**
     * Sets the unused2 field value.
     * This bit MUST be ignored
     */
    @Internal
    public void setUnused2( boolean value )
    {
        field_4_flags = (byte)unused2.setBoolean(field_4_flags, value);
    }

    /**
     * This bit MUST be ignored
     * @return  the unused2 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public boolean isUnused2()
    {
        return unused2.isSet(field_4_flags);
    }

    /**
     * Sets the fHybrid field value.
     * A bit that specifies whether the list this LSTF defines is a hybrid list
     */
    @Internal
    public void setFHybrid( boolean value )
    {
        field_4_flags = (byte)fHybrid.setBoolean(field_4_flags, value);
    }

    /**
     * A bit that specifies whether the list this LSTF defines is a hybrid list
     * @return  the fHybrid field value.
     */
    @Internal
    public boolean isFHybrid()
    {
        return fHybrid.isSet(field_4_flags);
    }

    /**
     * Sets the reserved1 field value.
     * This MUST be zero, and MUST be ignored.
     */
    @Internal
    public void setReserved1( byte value )
    {
        field_4_flags = (byte)reserved1.setValue(field_4_flags, value);
    }

    /**
     * This MUST be zero, and MUST be ignored.
     * @return  the reserved1 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public byte getReserved1()
    {
        return ( byte )reserved1.getValue(field_4_flags);
    }

}  // END OF CLASS
