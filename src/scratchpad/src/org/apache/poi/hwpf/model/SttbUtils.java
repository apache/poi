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

import java.io.IOException;

import org.apache.poi.hwpf.model.io.HWPFOutputStream;
import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.StringUtil;

/**
 * Utils class for storing and reading "STring TaBle stored in File"
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Internal
class SttbUtils
{

    static class Sttb
    {
        public int cbExtra;

        public int cDataLength;

        public String[] data;

        public byte[][] extraData;
    }

    private static final int CBEXTRA_STTB_SAVED_BY = 0; // bytes

    private static final int CBEXTRA_STTBF_BKMK = 0; // bytes

    private static final int CBEXTRA_STTBF_R_MARK = 0; // bytes

    private static final int CDATA_SIZE_STTB_SAVED_BY = 2; // bytes

    private static final int CDATA_SIZE_STTBF_BKMK = 2; // bytes

    private static final int CDATA_SIZE_STTBF_R_MARK = 2; // bytes

    static Sttb read( int cDataLength, byte[] buffer, int startOffset )
    {
        short ffff = LittleEndian.getShort( buffer, startOffset );
        int offset = startOffset + 2;

        if ( ffff != (short) 0xffff )
        {
            // Non-extended character Pascal strings
            throw new UnsupportedOperationException(
                    "Non-extended character Pascal strings are not supported right now. "
                            + "Please, contact POI developers for update." );
        }
        // strings are extended character strings

        int cData = cDataLength == 2 ? LittleEndian.getUShort( buffer, offset )
                : LittleEndian.getInt( buffer, offset );
        offset += cDataLength;

        Sttb sttb = new Sttb();
        sttb.cDataLength = cDataLength;
        sttb.cbExtra = LittleEndian.getUShort( buffer, offset );
        offset += 2;

        sttb.data = new String[cData];
        sttb.extraData = new byte[cData][];

        for ( int i = 0; i < cData; i++ )
        {
            int cchData = LittleEndian.getShort( buffer, offset );
            offset += 2;

            if ( cchData < 0 )
                continue;

            sttb.data[i] = StringUtil
                    .getFromUnicodeLE( buffer, offset, cchData );
            offset += cchData * 2;

            sttb.extraData[i] = LittleEndian.getByteArray( buffer, offset,
                    sttb.cbExtra );
            offset += sttb.cbExtra;
        }

        return sttb;
    }

    static String[] readSttbfBkmk( byte[] buffer, int startOffset )
    {
        return read( CDATA_SIZE_STTBF_BKMK, buffer, startOffset ).data;
    }

    static String[] readSttbfRMark( byte[] buffer, int startOffset )
    {
        return read( CDATA_SIZE_STTBF_R_MARK, buffer, startOffset ).data;
    }

    static String[] readSttbSavedBy( byte[] buffer, int startOffset )
    {
        return read( CDATA_SIZE_STTB_SAVED_BY, buffer, startOffset ).data;
    }

    static void write( Sttb sttb, HWPFOutputStream tableStream )
            throws IOException
    {
        final int headerSize = sttb.cDataLength == 2 ? 6 : 8;

        byte[] header = new byte[headerSize];
        LittleEndian.putShort( header, 0, (short) 0xffff );

        if ( sttb.data == null || sttb.data.length == 0 )
        {
            if ( sttb.cDataLength == 4 )
            {
                LittleEndian.putInt( header, 2, 0 );
                LittleEndian.putUShort( header, 6, sttb.cbExtra );
                tableStream.write( header );
                return;
            }

            LittleEndian.putUShort( header, 2, 0 );
            LittleEndian.putUShort( header, 4, sttb.cbExtra );
            tableStream.write( header );
            return;
        }

        if ( sttb.cDataLength == 4 )
        {
            LittleEndian.putInt( header, 2, sttb.data.length );
            LittleEndian.putUShort( header, 6, sttb.cbExtra );
            tableStream.write( header );
        }
        else
        {
            LittleEndian.putUShort( header, 2, sttb.data.length );
            LittleEndian.putUShort( header, 4, sttb.cbExtra );
            tableStream.write( header );
        }

        for ( int i = 0; i < sttb.data.length; i++ )
        {
            String entry = sttb.data[i];
            if ( entry == null )
            {
                // is it correct?
                tableStream.write( new byte[] { -1, 0 } );
                continue;
            }

            byte[] buf = new byte[entry.length() * 2 + sttb.cbExtra + 2];

            LittleEndian.putShort( buf, 0, (short) entry.length() );
            StringUtil.putUnicodeLE( entry, buf, 2 );

            if ( sttb.extraData != null && i < sttb.extraData.length
                    && sttb.extraData[i] != null )
                System.arraycopy( sttb.extraData[i], 0, buf,
                        entry.length() * 2,
                        Math.min( sttb.extraData[i].length, sttb.cbExtra ) );

            tableStream.write( buf );
        }
    }

    static void writeSttbfBkmk( String[] data, HWPFOutputStream tableStream )
            throws IOException
    {
        Sttb sttb = new Sttb();
        sttb.cDataLength = CDATA_SIZE_STTBF_BKMK;
        sttb.data = data;
        sttb.cbExtra = CBEXTRA_STTBF_BKMK;
        write( sttb, tableStream );
    }

    static void writeSttbfRMark( String[] data, HWPFOutputStream tableStream )
            throws IOException
    {
        Sttb sttb = new Sttb();
        sttb.cDataLength = CDATA_SIZE_STTBF_R_MARK;
        sttb.data = data;
        sttb.cbExtra = CBEXTRA_STTBF_R_MARK;
        write( sttb, tableStream );
    }

    static void writeSttbSavedBy( String[] data, HWPFOutputStream tableStream )
            throws IOException
    {
        Sttb sttb = new Sttb();
        sttb.cDataLength = CDATA_SIZE_STTB_SAVED_BY;
        sttb.data = data;
        sttb.cbExtra = CBEXTRA_STTB_SAVED_BY;
        write( sttb, tableStream );
    }

}
