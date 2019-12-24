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
 * Table Autoformat Look sPecifier (TLP).
 */
@SuppressWarnings("unused")
@Internal
public abstract class TLPAbstractType {
    private static final BitField fBorders = new BitField( 0x0001 );
    private static final BitField fShading = new BitField( 0x0002 );
    private static final BitField fFont = new BitField( 0x0004 );
    private static final BitField fColor = new BitField( 0x0008 );
    private static final BitField fBestFit = new BitField( 0x0010 );
    private static final BitField fHdrRows = new BitField( 0x0020 );
    private static final BitField fLastRow = new BitField( 0x0040 );

    protected short field_1_itl;
    protected byte field_2_tlp_flags;

    public TLPAbstractType() {}

    public TLPAbstractType(TLPAbstractType other) {
        field_1_itl = other.field_1_itl;
        field_2_tlp_flags = other.field_2_tlp_flags;
    }

    protected void fillFields( byte[] data, int offset )
    {
        field_1_itl = LittleEndian.getShort( data, 0x0 + offset );
        field_2_tlp_flags = data[0x2 + offset];
    }

    public void serialize( byte[] data, int offset )
    {
        LittleEndian.putShort( data, 0x0 + offset, field_1_itl );
        data[0x2 + offset] = field_2_tlp_flags;
    }

    public String toString() {
        return
            "[TLP]\n" +
            "    .itl                  = (" + getItl() + " )\n" +
            "    .tlp_flags            = (" + getTlp_flags() + " )\n" +
            "         .fBorders                 = " + isFBorders() + "\n" +
            "         .fShading                 = " + isFShading() + "\n" +
            "         .fFont                    = " + isFFont() + "\n" +
            "         .fColor                   = " + isFColor() + "\n" +
            "         .fBestFit                 = " + isFBestFit() + "\n" +
            "         .fHdrRows                 = " + isFHdrRows() + "\n" +
            "         .fLastRow                 = " + isFLastRow() + "\n" +
            "[/TLP]\n";
    }

    /**
     * Size of record (exluding 4 byte header)
     */
    public int getSize()
    {
        return 4 + +2 + 1;
    }

    /**
     * Get the itl field for the TLP record.
     */
    public short getItl()
    {
        return field_1_itl;
    }

    /**
     * Set the itl field for the TLP record.
     */
    public void setItl( short field_1_itl )
    {
        this.field_1_itl = field_1_itl;
    }

    /**
     * Get the tlp_flags field for the TLP record.
     */
    public byte getTlp_flags()
    {
        return field_2_tlp_flags;
    }

    /**
     * Set the tlp_flags field for the TLP record.
     */
    public void setTlp_flags( byte field_2_tlp_flags )
    {
        this.field_2_tlp_flags = field_2_tlp_flags;
    }

    /**
     * Sets the fBorders field value. When == 1, use the border properties from
     * the selected table look
     */
    public void setFBorders( boolean value )
    {
        field_2_tlp_flags = (byte) fBorders.setBoolean( field_2_tlp_flags,
                value );

    }

    /**
     * When == 1, use the border properties from the selected table look
     *
     * @return the fBorders field value.
     */
    public boolean isFBorders()
    {
        return fBorders.isSet( field_2_tlp_flags );

    }

    /**
     * Sets the fShading field value. When == 1, use the shading properties from
     * the selected table look
     */
    public void setFShading( boolean value )
    {
        field_2_tlp_flags = (byte) fShading.setBoolean( field_2_tlp_flags,
                value );

    }

    /**
     * When == 1, use the shading properties from the selected table look
     *
     * @return the fShading field value.
     */
    public boolean isFShading()
    {
        return fShading.isSet( field_2_tlp_flags );

    }

    /**
     * Sets the fFont field value. When == 1, use the font from the selected
     * table look
     */
    public void setFFont( boolean value )
    {
        field_2_tlp_flags = (byte) fFont.setBoolean( field_2_tlp_flags, value );

    }

    /**
     * When == 1, use the font from the selected table look
     *
     * @return the fFont field value.
     */
    public boolean isFFont()
    {
        return fFont.isSet( field_2_tlp_flags );

    }

    /**
     * Sets the fColor field value. When == 1, use the color from the selected
     * table look
     */
    public void setFColor( boolean value )
    {
        field_2_tlp_flags = (byte) fColor.setBoolean( field_2_tlp_flags, value );

    }

    /**
     * When == 1, use the color from the selected table look
     *
     * @return the fColor field value.
     */
    public boolean isFColor()
    {
        return fColor.isSet( field_2_tlp_flags );

    }

    /**
     * Sets the fBestFit field value. When == 1, do best fit from the selected
     * table look
     */
    public void setFBestFit( boolean value )
    {
        field_2_tlp_flags = (byte) fBestFit.setBoolean( field_2_tlp_flags,
                value );

    }

    /**
     * When == 1, do best fit from the selected table look
     *
     * @return the fBestFit field value.
     */
    public boolean isFBestFit()
    {
        return fBestFit.isSet( field_2_tlp_flags );

    }

    /**
     * Sets the fHdrRows field value. When == 1, apply properties from the
     * selected table look to the header rows in the table
     */
    public void setFHdrRows( boolean value )
    {
        field_2_tlp_flags = (byte) fHdrRows.setBoolean( field_2_tlp_flags,
                value );

    }

    /**
     * When == 1, apply properties from the selected table look to the header
     * rows in the table
     *
     * @return the fHdrRows field value.
     */
    public boolean isFHdrRows()
    {
        return fHdrRows.isSet( field_2_tlp_flags );

    }

    /**
     * Sets the fLastRow field value. When == 1, apply properties from the
     * selected table look to the last row in the table
     */
    public void setFLastRow( boolean value )
    {
        field_2_tlp_flags = (byte) fLastRow.setBoolean( field_2_tlp_flags,
                value );

    }

    /**
     * When == 1, apply properties from the selected table look to the last row
     * in the table
     *
     * @return the fLastRow field value.
     */
    public boolean isFLastRow()
    {
        return fLastRow.isSet( field_2_tlp_flags );

    }

} // END OF CLASS

