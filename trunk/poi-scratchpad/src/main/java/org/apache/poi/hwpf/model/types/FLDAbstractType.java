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

/**
 * Field Descriptor (FLD).
 */
@Internal
public abstract class FLDAbstractType
{

    protected byte field_1_chHolder;
    private static final BitField ch = new BitField( 0x1f );
    private static final BitField reserved = new BitField( 0xe0 );
    protected byte field_2_flt;
    private static final BitField fDiffer = new BitField( 0x01 );
    private static final BitField fZombieEmbed = new BitField( 0x02 );
    private static final BitField fResultDirty = new BitField( 0x04 );
    private static final BitField fResultEdited = new BitField( 0x08 );
    private static final BitField fLocked = new BitField( 0x10 );
    private static final BitField fPrivateResult = new BitField( 0x20 );
    private static final BitField fNested = new BitField( 0x40 );
    private static final BitField fHasSep = new BitField( 0x40 );

    public FLDAbstractType()
    {

    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_chHolder = data[0x0 + offset];
        field_2_flt = data[0x1 + offset];
    }

    public void serialize( byte[] data, int offset )
    {
        data[0x0 + offset] = field_1_chHolder;
        data[0x1 + offset] = field_2_flt;
    }

    public String toString() {
        return
            "[FLD]\n" +
            "    .chHolder             =  (" + getChHolder() + " )\n" +
            "         .ch                       = " + getCh() + "\n" +
            "         .reserved                 = " + getReserved() + "\n" +
            "    .flt                  =  (" + getFlt() + " )\n" +
            "         .fDiffer                  = " + isFDiffer() + "\n" +
            "         .fZombieEmbed             = " + isFZombieEmbed() + "\n" +
            "         .fResultDirty             = " + isFResultDirty() + "\n" +
            "         .fResultEdited            = " + isFResultEdited() + "\n" +
            "         .fLocked                  = " + isFLocked() + "\n" +
            "         .fPrivateResult           = " + isFPrivateResult()  + "\n" +
            "         .fNested                  = " + isFNested()  + "\n" +
            "         .fHasSep                  = " + isFHasSep()  + "\n" +
            "[/FLD]\n";
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public static int getSize()
    {
        return 4 + +1 + 1;
    }

    /**
     * ch field holder (along with reserved bits).
     */
    public byte getChHolder()
    {
        return field_1_chHolder;
    }

    /**
     * ch field holder (along with reserved bits).
     */
    public void setChHolder( byte field_1_chHolder )
    {
        this.field_1_chHolder = field_1_chHolder;
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
     * Sets the ch field value. Type of field boundary the FLD describes: 19 --
     * field begin mark, 20 -- field separation mark; 21 -- field end mark
     */
    public void setCh( byte value )
    {
        field_1_chHolder = (byte) ch.setValue( field_1_chHolder, value );

    }

    /**
     * Type of field boundary the FLD describes: 19 -- field begin mark, 20 --
     * field separation mark; 21 -- field end mark
     *
     * @return the ch field value.
     */
    public byte getCh()
    {
        return (byte) ch.getValue( field_1_chHolder );

    }

    /**
     * Sets the reserved field value. Reserved
     */
    public void setReserved( byte value )
    {
        field_1_chHolder = (byte) reserved.setValue( field_1_chHolder, value );

    }

    /**
     * Reserved
     *
     * @return the reserved field value.
     */
    public byte getReserved()
    {
        return (byte) reserved.getValue( field_1_chHolder );

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

