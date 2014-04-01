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

import org.apache.poi.hwpf.model.types.FibRgLw95AbstractType;
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
class FibRgLw95 extends FibRgLw95AbstractType implements FibRgLw 
{

    public FibRgLw95()
    {
    }

    public FibRgLw95( byte[] std, int offset )
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
            return getCcpMcr();
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
            setCbMac( newLength );
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

}
