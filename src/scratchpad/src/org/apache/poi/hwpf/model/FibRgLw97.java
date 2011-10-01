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
package org.apache.poi.hwpf.model;

import org.apache.poi.hwpf.model.types.FibRgLw97AbstractType;
import org.apache.poi.util.Internal;

/**
 * The FibRgLw97 structure is the third section of the FIB. This contains an
 * array of 4-byte values.
 * <p>
 * Class and fields descriptions are quoted from Microsoft Office Word 97-2007
 * Binary File Format and [MS-DOC] - v20110608 Word (.doc) Binary File Format
 * 
 * @author Sergey Vladimirov; according to Microsoft Office Word 97-2007 Binary
 *         File Format Specification [*.doc] and [MS-DOC] - v20110608 Word
 *         (.doc) Binary File Format
 */
@Internal
class FibRgLw97 extends FibRgLw97AbstractType
{

    public FibRgLw97()
    {
    }

    public FibRgLw97( byte[] std, int offset )
    {
        fillFields( std, offset );
    }

    @SuppressWarnings( "deprecation" )
    public int getSubdocumentTextStreamLength( SubdocumentType subdocumentType )
    {
        switch ( subdocumentType )
        {
        case MAIN:
            return getCcpText();
        case FOOTNOTE:
            return getCcpFtn();
        case HEADER:
            return getCcpHdd();
        case MACRO:
            return field_7_reserved3;
        case ANNOTATION:
            return getCcpAtn();
        case ENDNOTE:
            return getCcpEdn();
        case TEXTBOX:
            return getCcpTxbx();
        case HEADER_TEXTBOX:
            return getCcpHdrTxbx();
        }
        throw new UnsupportedOperationException( "Unsupported: "
                + subdocumentType );
    }

    @SuppressWarnings( "deprecation" )
    public void setSubdocumentTextStreamLength(
            SubdocumentType subdocumentType, int newLength )
    {
        switch ( subdocumentType )
        {
        case MAIN:
            setCcpText( newLength );
            return;
        case FOOTNOTE:
            setCcpFtn( newLength );
            return;
        case HEADER:
            setCcpHdd( newLength );
            return;
        case MACRO:
            field_7_reserved3 = newLength;
            return;
        case ANNOTATION:
            setCcpAtn( newLength );
            return;
        case ENDNOTE:
            setCcpEdn( newLength );
            return;
        case TEXTBOX:
            setCcpTxbx( newLength );
            return;
        case HEADER_TEXTBOX:
            setCcpHdrTxbx( newLength );
            return;
        }
        throw new UnsupportedOperationException( "Unsupported: "
                + subdocumentType );
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + field_10_ccpTxbx;
        result = prime * result + field_11_ccpHdrTxbx;
        result = prime * result + field_12_reserved4;
        result = prime * result + field_13_reserved5;
        result = prime * result + field_14_reserved6;
        result = prime * result + field_15_reserved7;
        result = prime * result + field_16_reserved8;
        result = prime * result + field_17_reserved9;
        result = prime * result + field_18_reserved10;
        result = prime * result + field_19_reserved11;
        result = prime * result + field_1_cbMac;
        result = prime * result + field_20_reserved12;
        result = prime * result + field_21_reserved13;
        result = prime * result + field_22_reserved14;
        result = prime * result + field_2_reserved1;
        result = prime * result + field_3_reserved2;
        result = prime * result + field_4_ccpText;
        result = prime * result + field_5_ccpFtn;
        result = prime * result + field_6_ccpHdd;
        result = prime * result + field_7_reserved3;
        result = prime * result + field_8_ccpAtn;
        result = prime * result + field_9_ccpEdn;
        return result;
    }

    @Override
    @SuppressWarnings( "deprecation" )
    public boolean equals( Object obj )
    {
        if ( this == obj )
            return true;
        if ( obj == null )
            return false;
        if ( getClass() != obj.getClass() )
            return false;
        FibRgLw97 other = (FibRgLw97) obj;
        if ( field_10_ccpTxbx != other.field_10_ccpTxbx )
            return false;
        if ( field_11_ccpHdrTxbx != other.field_11_ccpHdrTxbx )
            return false;
        if ( field_12_reserved4 != other.field_12_reserved4 )
            return false;
        if ( field_13_reserved5 != other.field_13_reserved5 )
            return false;
        if ( field_14_reserved6 != other.field_14_reserved6 )
            return false;
        if ( field_15_reserved7 != other.field_15_reserved7 )
            return false;
        if ( field_16_reserved8 != other.field_16_reserved8 )
            return false;
        if ( field_17_reserved9 != other.field_17_reserved9 )
            return false;
        if ( field_18_reserved10 != other.field_18_reserved10 )
            return false;
        if ( field_19_reserved11 != other.field_19_reserved11 )
            return false;
        if ( field_1_cbMac != other.field_1_cbMac )
            return false;
        if ( field_20_reserved12 != other.field_20_reserved12 )
            return false;
        if ( field_21_reserved13 != other.field_21_reserved13 )
            return false;
        if ( field_22_reserved14 != other.field_22_reserved14 )
            return false;
        if ( field_2_reserved1 != other.field_2_reserved1 )
            return false;
        if ( field_3_reserved2 != other.field_3_reserved2 )
            return false;
        if ( field_4_ccpText != other.field_4_ccpText )
            return false;
        if ( field_5_ccpFtn != other.field_5_ccpFtn )
            return false;
        if ( field_6_ccpHdd != other.field_6_ccpHdd )
            return false;
        if ( field_7_reserved3 != other.field_7_reserved3 )
            return false;
        if ( field_8_ccpAtn != other.field_8_ccpAtn )
            return false;
        if ( field_9_ccpEdn != other.field_9_ccpEdn )
            return false;
        return true;
    }

}
