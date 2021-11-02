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
 * The LFOLVL structure contains information that is used to override the formatting information of a corresponding LVL.
 */
@Internal
public abstract class LFOLVLBaseAbstractType
{

    protected int field_1_iStartAt;
    protected int field_2_flags;
    /**/private static final BitField iLvl = new BitField(0x0000000F);
    /**/private static final BitField fStartAt = new BitField(0x00000010);
    /**/private static final BitField fFormatting = new BitField(0x00000020);
    /**/private static final BitField grfhic = new BitField(0x00003FC0);
    /**/private static final BitField unused1 = new BitField(0x1FFFC000);
    /**/private static final BitField unused2 = new BitField(0xE0000000);

    protected LFOLVLBaseAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_iStartAt               = LittleEndian.getInt( data, 0x0 + offset );
        field_2_flags                  = LittleEndian.getInt( data, 0x4 + offset );
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_iStartAt );
        LittleEndian.putInt( data, 0x4 + offset, field_2_flags );
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
        return 0 + 4 + 4;
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
        LFOLVLBaseAbstractType other = (LFOLVLBaseAbstractType) obj;
        if ( field_1_iStartAt != other.field_1_iStartAt )
            return false;
        if ( field_2_flags != other.field_2_flags )
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(field_1_iStartAt,field_2_flags);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("[LFOLVLBase]\n");
        builder.append("    .iStartAt             = ");
        builder.append(" (").append(getIStartAt()).append(" )\n");
        builder.append("    .flags                = ");
        builder.append(" (").append(getFlags()).append(" )\n");
        builder.append("         .iLvl                     = ").append(getILvl()).append('\n');
        builder.append("         .fStartAt                 = ").append(isFStartAt()).append('\n');
        builder.append("         .fFormatting              = ").append(isFFormatting()).append('\n');
        builder.append("         .grfhic                   = ").append(getGrfhic()).append('\n');
        builder.append("         .unused1                  = ").append(getUnused1()).append('\n');
        builder.append("         .unused2                  = ").append(getUnused2()).append('\n');

        builder.append("[/LFOLVLBase]\n");
        return builder.toString();
    }

    /**
     * If fStartAt is set to 0x1, this is a signed integer that specifies the start-at value that overrides lvlf.iStartAt of the corresponding LVL. This value MUST be less than or equal to 0x7FFF and MUST be greater than or equal to zero. If both fStartAt and fFormatting are set to 0x1, or if fStartAt is set to 0x0, this value is undefined and MUST be ignored.
     */
    @Internal
    public int getIStartAt()
    {
        return field_1_iStartAt;
    }

    /**
     * If fStartAt is set to 0x1, this is a signed integer that specifies the start-at value that overrides lvlf.iStartAt of the corresponding LVL. This value MUST be less than or equal to 0x7FFF and MUST be greater than or equal to zero. If both fStartAt and fFormatting are set to 0x1, or if fStartAt is set to 0x0, this value is undefined and MUST be ignored.
     */
    @Internal
    public void setIStartAt( int field_1_iStartAt )
    {
        this.field_1_iStartAt = field_1_iStartAt;
    }

    /**
     * Get the flags field for the LFOLVLBase record.
     */
    @Internal
    public int getFlags()
    {
        return field_2_flags;
    }

    /**
     * Set the flags field for the LFOLVLBase record.
     */
    @Internal
    public void setFlags( int field_2_flags )
    {
        this.field_2_flags = field_2_flags;
    }

    /**
     * Sets the iLvl field value.
     * An unsigned integer that specifies the zero-based level of the list that this overrides. This LFOLVL overrides the LVL that specifies the level formatting of this level of the LSTF that is specified by the lsid field of the LFO to which this LFOLVL corresponds. This value MUST be less than or equal to 0x08
     */
    @Internal
    public void setILvl( byte value )
    {
        field_2_flags = iLvl.setValue(field_2_flags, value);
    }

    /**
     * An unsigned integer that specifies the zero-based level of the list that this overrides. This LFOLVL overrides the LVL that specifies the level formatting of this level of the LSTF that is specified by the lsid field of the LFO to which this LFOLVL corresponds. This value MUST be less than or equal to 0x08
     * @return  the iLvl field value.
     */
    @Internal
    public byte getILvl()
    {
        return ( byte )iLvl.getValue(field_2_flags);
    }

    /**
     * Sets the fStartAt field value.
     * A bit that specifies whether this LFOLVL overrides the start-at value of the level.
     */
    @Internal
    public void setFStartAt( boolean value )
    {
        field_2_flags = fStartAt.setBoolean(field_2_flags, value);
    }

    /**
     * A bit that specifies whether this LFOLVL overrides the start-at value of the level.
     * @return  the fStartAt field value.
     */
    @Internal
    public boolean isFStartAt()
    {
        return fStartAt.isSet(field_2_flags);
    }

    /**
     * Sets the fFormatting field value.
     * A bit that specifies whether lvl is an LVL that overrides the corresponding LVL
     */
    @Internal
    public void setFFormatting( boolean value )
    {
        field_2_flags = fFormatting.setBoolean(field_2_flags, value);
    }

    /**
     * A bit that specifies whether lvl is an LVL that overrides the corresponding LVL
     * @return  the fFormatting field value.
     */
    @Internal
    public boolean isFFormatting()
    {
        return fFormatting.isSet(field_2_flags);
    }

    /**
     * Sets the grfhic field value.
     * A grfhic that specifies the HTML incompatibilities of the overriding level formatting
     */
    @Internal
    public void setGrfhic( short value )
    {
        field_2_flags = grfhic.setValue(field_2_flags, value);
    }

    /**
     * A grfhic that specifies the HTML incompatibilities of the overriding level formatting
     * @return  the grfhic field value.
     */
    @Internal
    public short getGrfhic()
    {
        return ( short )grfhic.getValue(field_2_flags);
    }

    /**
     * Sets the unused1 field value.
     * This MUST be ignored
     */
    @Internal
    public void setUnused1( short value )
    {
        field_2_flags = unused1.setValue(field_2_flags, value);
    }

    /**
     * This MUST be ignored
     * @return  the unused1 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public short getUnused1()
    {
        return ( short )unused1.getValue(field_2_flags);
    }

    /**
     * Sets the unused2 field value.
     * This MUST be ignored
     */
    @Internal
    public void setUnused2( byte value )
    {
        field_2_flags = unused2.setValue(field_2_flags, value);
    }

    /**
     * This MUST be ignored
     * @return  the unused2 field value.
     * @deprecated This field should not be used according to specification
     */
    @Internal
    @Deprecated
    public byte getUnused2()
    {
        return ( byte )unused2.getValue(field_2_flags);
    }

}  // END OF CLASS
