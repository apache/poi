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
 * List Format Override (LFO).
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format
 * 
 * NOTE: This source is automatically generated please do not modify this file.
 * Either subclass or remove the record in src/types/definitions.
 * 
 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary
 *         File Format Specification [*.doc]
 */
@Internal
public abstract class LFOAbstractType
{

    protected int field_1_lsid;
    protected int field_2_reserved1;
    protected int field_3_reserved2;
    protected byte field_4_clfolvl;
    protected byte field_5_ibstFltAutoNum;
    protected byte field_6_grfhic;
    /**/private static BitField fHtmlChecked = new BitField( 0x01 );
    /**/private static BitField fHtmlUnsupported = new BitField( 0x02 );
    /**/private static BitField fHtmlListTextNotSharpDot = new BitField( 0x04 );
    /**/private static BitField fHtmlNotPeriod = new BitField( 0x08 );
    /**/private static BitField fHtmlFirstLineMismatch = new BitField( 0x10 );
    /**/private static BitField fHtmlTabLeftIndentMismatch = new BitField(
            0x20 );
    /**/private static BitField fHtmlHangingIndentBeneathNumber = new BitField(
            0x40 );
    /**/private static BitField fHtmlBuiltInBullet = new BitField( 0x80 );
    protected byte field_7_reserved3;

    protected LFOAbstractType()
    {
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_lsid = LittleEndian.getInt( data, 0x0 + offset );
        field_2_reserved1 = LittleEndian.getInt( data, 0x4 + offset );
        field_3_reserved2 = LittleEndian.getInt( data, 0x8 + offset );
        field_4_clfolvl = data[0xc + offset];
        field_5_ibstFltAutoNum = data[0xd + offset];
        field_6_grfhic = data[0xe + offset];
        field_7_reserved3 = data[0xf + offset];
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putInt( data, 0x0 + offset, field_1_lsid );
        LittleEndian.putInt( data, 0x4 + offset, field_2_reserved1 );
        LittleEndian.putInt( data, 0x8 + offset, field_3_reserved2 );
        data[0xc + offset] = field_4_clfolvl;
        data[0xd + offset] = field_5_ibstFltAutoNum;
        data[0xe + offset] = field_6_grfhic;
        data[0xf + offset] = field_7_reserved3;
    }

    /**
     * Size of record
     */
    public static int getSize()
    {
        return 0 + 4 + 4 + 4 + 1 + 1 + 1 + 1;
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append( "[LFO]\n" );
        builder.append( "    .lsid                 = " );
        builder.append( " (" ).append( getLsid() ).append( " )\n" );
        builder.append( "    .reserved1            = " );
        builder.append( " (" ).append( getReserved1() ).append( " )\n" );
        builder.append( "    .reserved2            = " );
        builder.append( " (" ).append( getReserved2() ).append( " )\n" );
        builder.append( "    .clfolvl              = " );
        builder.append( " (" ).append( getClfolvl() ).append( " )\n" );
        builder.append( "    .ibstFltAutoNum       = " );
        builder.append( " (" ).append( getIbstFltAutoNum() ).append( " )\n" );
        builder.append( "    .grfhic               = " );
        builder.append( " (" ).append( getGrfhic() ).append( " )\n" );
        builder.append( "         .fHtmlChecked             = " )
                .append( isFHtmlChecked() ).append( '\n' );
        builder.append( "         .fHtmlUnsupported         = " )
                .append( isFHtmlUnsupported() ).append( '\n' );
        builder.append( "         .fHtmlListTextNotSharpDot     = " )
                .append( isFHtmlListTextNotSharpDot() ).append( '\n' );
        builder.append( "         .fHtmlNotPeriod           = " )
                .append( isFHtmlNotPeriod() ).append( '\n' );
        builder.append( "         .fHtmlFirstLineMismatch     = " )
                .append( isFHtmlFirstLineMismatch() ).append( '\n' );
        builder.append( "         .fHtmlTabLeftIndentMismatch     = " )
                .append( isFHtmlTabLeftIndentMismatch() ).append( '\n' );
        builder.append( "         .fHtmlHangingIndentBeneathNumber     = " )
                .append( isFHtmlHangingIndentBeneathNumber() ).append( '\n' );
        builder.append( "         .fHtmlBuiltInBullet       = " )
                .append( isFHtmlBuiltInBullet() ).append( '\n' );
        builder.append( "    .reserved3            = " );
        builder.append( " (" ).append( getReserved3() ).append( " )\n" );

        builder.append( "[/LFO]\n" );
        return builder.toString();
    }

    /**
     * List ID of corresponding LSTF (see LSTF).
     */
    public int getLsid()
    {
        return field_1_lsid;
    }

    /**
     * List ID of corresponding LSTF (see LSTF).
     */
    public void setLsid( int field_1_lsid )
    {
        this.field_1_lsid = field_1_lsid;
    }

    /**
     * Reserved.
     */
    public int getReserved1()
    {
        return field_2_reserved1;
    }

    /**
     * Reserved.
     */
    public void setReserved1( int field_2_reserved1 )
    {
        this.field_2_reserved1 = field_2_reserved1;
    }

    /**
     * Reserved.
     */
    public int getReserved2()
    {
        return field_3_reserved2;
    }

    /**
     * Reserved.
     */
    public void setReserved2( int field_3_reserved2 )
    {
        this.field_3_reserved2 = field_3_reserved2;
    }

    /**
     * Count of levels whose format is overridden (see LFOLVL).
     */
    public byte getClfolvl()
    {
        return field_4_clfolvl;
    }

    /**
     * Count of levels whose format is overridden (see LFOLVL).
     */
    public void setClfolvl( byte field_4_clfolvl )
    {
        this.field_4_clfolvl = field_4_clfolvl;
    }

    /**
     * Used for AUTONUM field emulation.
     */
    public byte getIbstFltAutoNum()
    {
        return field_5_ibstFltAutoNum;
    }

    /**
     * Used for AUTONUM field emulation.
     */
    public void setIbstFltAutoNum( byte field_5_ibstFltAutoNum )
    {
        this.field_5_ibstFltAutoNum = field_5_ibstFltAutoNum;
    }

    /**
     * HTML compatibility flags.
     */
    public byte getGrfhic()
    {
        return field_6_grfhic;
    }

    /**
     * HTML compatibility flags.
     */
    public void setGrfhic( byte field_6_grfhic )
    {
        this.field_6_grfhic = field_6_grfhic;
    }

    /**
     * Reserved.
     */
    public byte getReserved3()
    {
        return field_7_reserved3;
    }

    /**
     * Reserved.
     */
    public void setReserved3( byte field_7_reserved3 )
    {
        this.field_7_reserved3 = field_7_reserved3;
    }

    /**
     * Sets the fHtmlChecked field value. Checked
     */
    public void setFHtmlChecked( boolean value )
    {
        field_6_grfhic = (byte) fHtmlChecked.setBoolean( field_6_grfhic, value );
    }

    /**
     * Checked
     * 
     * @return the fHtmlChecked field value.
     */
    public boolean isFHtmlChecked()
    {
        return fHtmlChecked.isSet( field_6_grfhic );
    }

    /**
     * Sets the fHtmlUnsupported field value. The numbering sequence or format
     * is unsupported (includes tab & size)
     */
    public void setFHtmlUnsupported( boolean value )
    {
        field_6_grfhic = (byte) fHtmlUnsupported.setBoolean( field_6_grfhic,
                value );
    }

    /**
     * The numbering sequence or format is unsupported (includes tab & size)
     * 
     * @return the fHtmlUnsupported field value.
     */
    public boolean isFHtmlUnsupported()
    {
        return fHtmlUnsupported.isSet( field_6_grfhic );
    }

    /**
     * Sets the fHtmlListTextNotSharpDot field value. The list text is not "#."
     */
    public void setFHtmlListTextNotSharpDot( boolean value )
    {
        field_6_grfhic = (byte) fHtmlListTextNotSharpDot.setBoolean(
                field_6_grfhic, value );
    }

    /**
     * The list text is not "#."
     * 
     * @return the fHtmlListTextNotSharpDot field value.
     */
    public boolean isFHtmlListTextNotSharpDot()
    {
        return fHtmlListTextNotSharpDot.isSet( field_6_grfhic );
    }

    /**
     * Sets the fHtmlNotPeriod field value. Something other than a period is
     * used
     */
    public void setFHtmlNotPeriod( boolean value )
    {
        field_6_grfhic = (byte) fHtmlNotPeriod.setBoolean( field_6_grfhic,
                value );
    }

    /**
     * Something other than a period is used
     * 
     * @return the fHtmlNotPeriod field value.
     */
    public boolean isFHtmlNotPeriod()
    {
        return fHtmlNotPeriod.isSet( field_6_grfhic );
    }

    /**
     * Sets the fHtmlFirstLineMismatch field value. First line indent mismatch
     */
    public void setFHtmlFirstLineMismatch( boolean value )
    {
        field_6_grfhic = (byte) fHtmlFirstLineMismatch.setBoolean(
                field_6_grfhic, value );
    }

    /**
     * First line indent mismatch
     * 
     * @return the fHtmlFirstLineMismatch field value.
     */
    public boolean isFHtmlFirstLineMismatch()
    {
        return fHtmlFirstLineMismatch.isSet( field_6_grfhic );
    }

    /**
     * Sets the fHtmlTabLeftIndentMismatch field value. The list tab and the
     * dxaLeft don't match (need table?)
     */
    public void setFHtmlTabLeftIndentMismatch( boolean value )
    {
        field_6_grfhic = (byte) fHtmlTabLeftIndentMismatch.setBoolean(
                field_6_grfhic, value );
    }

    /**
     * The list tab and the dxaLeft don't match (need table?)
     * 
     * @return the fHtmlTabLeftIndentMismatch field value.
     */
    public boolean isFHtmlTabLeftIndentMismatch()
    {
        return fHtmlTabLeftIndentMismatch.isSet( field_6_grfhic );
    }

    /**
     * Sets the fHtmlHangingIndentBeneathNumber field value. The hanging indent
     * falls beneath the number (need plain text)
     */
    public void setFHtmlHangingIndentBeneathNumber( boolean value )
    {
        field_6_grfhic = (byte) fHtmlHangingIndentBeneathNumber.setBoolean(
                field_6_grfhic, value );
    }

    /**
     * The hanging indent falls beneath the number (need plain text)
     * 
     * @return the fHtmlHangingIndentBeneathNumber field value.
     */
    public boolean isFHtmlHangingIndentBeneathNumber()
    {
        return fHtmlHangingIndentBeneathNumber.isSet( field_6_grfhic );
    }

    /**
     * Sets the fHtmlBuiltInBullet field value. A built-in HTML bullet
     */
    public void setFHtmlBuiltInBullet( boolean value )
    {
        field_6_grfhic = (byte) fHtmlBuiltInBullet.setBoolean( field_6_grfhic,
                value );
    }

    /**
     * A built-in HTML bullet
     * 
     * @return the fHtmlBuiltInBullet field value.
     */
    public boolean isFHtmlBuiltInBullet()
    {
        return fHtmlBuiltInBullet.isSet( field_6_grfhic );
    }

} // END OF CLASS
