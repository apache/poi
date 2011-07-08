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
import org.apache.poi.util.BitField;

/**
 * Field Descriptor (FLD).
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format
 * 
 * NOTE: This source is automatically generated please do not modify this file.
 * Either subclass or remove the record in src/records/definitions.
 * 
 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary
 *         File Format Specification [*.doc]
 */
public abstract class FLDAbstractType implements HDFType
{

    protected char field_1_ch;
    protected byte field_2_flt;
    private static BitField fDiffer = new BitField( 0x01 );
    private static BitField fZombieEmbed = new BitField( 0x02 );
    private static BitField fResultDirty = new BitField( 0x04 );
    private static BitField fResultEdited = new BitField( 0x08 );
    private static BitField fLocked = new BitField( 0x10 );
    private static BitField fPrivateResult = new BitField( 0x20 );
    private static BitField fNested = new BitField( 0x40 );
    private static BitField fHasSep = new BitField( 0x40 );

    public FLDAbstractType()
    {

    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_ch = (char) data[0x0 + offset];
        field_2_flt = data[0x1 + offset];

    }

    public void serialize( byte[] data, int offset )
    {
        data[0x0 + offset] = (byte) field_1_ch;
        ;
        data[0x1 + offset] = field_2_flt;
        ;

    }

    public String toString()
    {
        StringBuffer buffer = new StringBuffer();

        buffer.append( "[FLD]\n" );

        buffer.append( "    .ch                   = " );
        buffer.append( " (" ).append( getCh() ).append( " )\n" );

        buffer.append( "    .flt                  = " );
        buffer.append( " (" ).append( getFlt() ).append( " )\n" );
        buffer.append( "         .fDiffer                  = " )
                .append( isFDiffer() ).append( '\n' );
        buffer.append( "         .fZombieEmbed             = " )
                .append( isFZombieEmbed() ).append( '\n' );
        buffer.append( "         .fResultDirty             = " )
                .append( isFResultDirty() ).append( '\n' );
        buffer.append( "         .fResultEdited            = " )
                .append( isFResultEdited() ).append( '\n' );
        buffer.append( "         .fLocked                  = " )
                .append( isFLocked() ).append( '\n' );
        buffer.append( "         .fPrivateResult           = " )
                .append( isFPrivateResult() ).append( '\n' );
        buffer.append( "         .fNested                  = " )
                .append( isFNested() ).append( '\n' );
        buffer.append( "         .fHasSep                  = " )
                .append( isFHasSep() ).append( '\n' );

        buffer.append( "[/FLD]\n" );
        return buffer.toString();
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public static int getSize()
    {
        return 4 + +1 + 1;
    }

    /**
     * Type of field boundary the FLD describes: 19 -- field begin mark, 20 --
     * field separation mark; 21 -- field end mark.
     */
    public char getCh()
    {
        return field_1_ch;
    }

    /**
     * Type of field boundary the FLD describes: 19 -- field begin mark, 20 --
     * field separation mark; 21 -- field end mark.
     */
    public void setCh( char field_1_ch )
    {
        this.field_1_ch = field_1_ch;
    }

    /**
     * Field type when ch == 19 OR field flags when ch == 21 .
     */
    public byte getFlt()
    {
        return field_2_flt;
    }

    /**
     * Field type when ch == 19 OR field flags when ch == 21 .
     */
    public void setFlt( byte field_2_flt )
    {
        this.field_2_flt = field_2_flt;
    }

    /**
     * Sets the fDiffer field value. Ignored for saved file
     */
    public void setFDiffer( boolean value )
    {
        field_2_flt = (byte) fDiffer.setBoolean( field_2_flt, value );

    }

    /**
     * Ignored for saved file
     * 
     * @return the fDiffer field value.
     */
    public boolean isFDiffer()
    {
        return fDiffer.isSet( field_2_flt );

    }

    /**
     * Sets the fZombieEmbed field value. ==1 when result still believes this
     * field is an EMBED or LINK field
     */
    public void setFZombieEmbed( boolean value )
    {
        field_2_flt = (byte) fZombieEmbed.setBoolean( field_2_flt, value );

    }

    /**
     * ==1 when result still believes this field is an EMBED or LINK field
     * 
     * @return the fZombieEmbed field value.
     */
    public boolean isFZombieEmbed()
    {
        return fZombieEmbed.isSet( field_2_flt );

    }

    /**
     * Sets the fResultDirty field value. ==1 when user has edited or formatted
     * the result. == 0 otherwise
     */
    public void setFResultDirty( boolean value )
    {
        field_2_flt = (byte) fResultDirty.setBoolean( field_2_flt, value );

    }

    /**
     * ==1 when user has edited or formatted the result. == 0 otherwise
     * 
     * @return the fResultDirty field value.
     */
    public boolean isFResultDirty()
    {
        return fResultDirty.isSet( field_2_flt );

    }

    /**
     * Sets the fResultEdited field value. ==1 when user has inserted text into
     * or deleted text from the result
     */
    public void setFResultEdited( boolean value )
    {
        field_2_flt = (byte) fResultEdited.setBoolean( field_2_flt, value );

    }

    /**
     * ==1 when user has inserted text into or deleted text from the result
     * 
     * @return the fResultEdited field value.
     */
    public boolean isFResultEdited()
    {
        return fResultEdited.isSet( field_2_flt );

    }

    /**
     * Sets the fLocked field value. ==1 when field is locked from recalculation
     */
    public void setFLocked( boolean value )
    {
        field_2_flt = (byte) fLocked.setBoolean( field_2_flt, value );

    }

    /**
     * ==1 when field is locked from recalculation
     * 
     * @return the fLocked field value.
     */
    public boolean isFLocked()
    {
        return fLocked.isSet( field_2_flt );

    }

    /**
     * Sets the fPrivateResult field value. ==1 whenever the result of the field
     * is never to be shown
     */
    public void setFPrivateResult( boolean value )
    {
        field_2_flt = (byte) fPrivateResult.setBoolean( field_2_flt, value );

    }

    /**
     * ==1 whenever the result of the field is never to be shown
     * 
     * @return the fPrivateResult field value.
     */
    public boolean isFPrivateResult()
    {
        return fPrivateResult.isSet( field_2_flt );

    }

    /**
     * Sets the fNested field value. ==1 when field is nested within another
     * field
     */
    public void setFNested( boolean value )
    {
        field_2_flt = (byte) fNested.setBoolean( field_2_flt, value );

    }

    /**
     * ==1 when field is nested within another field
     * 
     * @return the fNested field value.
     */
    public boolean isFNested()
    {
        return fNested.isSet( field_2_flt );

    }

    /**
     * Sets the fHasSep field value. ==1 when field has a field separator
     */
    public void setFHasSep( boolean value )
    {
        field_2_flt = (byte) fHasSep.setBoolean( field_2_flt, value );

    }

    /**
     * ==1 when field has a field separator
     * 
     * @return the fHasSep field value.
     */
    public boolean isFHasSep()
    {
        return fHasSep.isSet( field_2_flt );

    }

} // END OF CLASS

