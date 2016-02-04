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

import java.util.Arrays;

import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.apache.poi.util.StringUtil;

/**
 * The STTB is a string table that is made up of a header that is followed by an
 * array of elements. The cData value specifies the number of elements that are
 * contained in the array.
 * <p>
 * Class and fields descriptions are quoted from [MS-DOC] -- v20121003 Word
 * (.doc) Binary File Format; Copyright (c) 2012 Microsoft Corporation; Release:
 * October 8, 2012
 * <p>
 * This class is internal. It content or properties may change without notice
 * due to changes in our knowledge of internal Microsoft Word binary structures.
 * 
 * @author Sergey Vladimirov; according to [MS-DOC] -- v20121003 Word (.doc)
 *         Binary File Format; Copyright (c) 2012 Microsoft Corporation;
 *         Release: October 8, 2012
 */
public class Sttb
{

    private int _cbExtra;

    private final int _cDataLength;

    private String[] _data;

    private byte[][] _extraData;

    private final boolean _fExtend = true;

    public Sttb( byte[] buffer, int startOffset )
    {
        this( 2, buffer, startOffset );
    }

    public Sttb( int cDataLength, byte[] buffer, int startOffset )
    {
        this._cDataLength = cDataLength;
        fillFields( buffer, startOffset );
    }

    public Sttb( int cDataLength, String[] data )
    {
        this._cDataLength = cDataLength;

        this._data = Arrays.copyOf(data, data.length);

        this._cbExtra = 0;
        this._extraData = null;
    }

    public void fillFields( byte[] buffer, int startOffset )
    {
        short ffff = LittleEndian.getShort( buffer, startOffset );
        int offset = startOffset + LittleEndian.SHORT_SIZE;

        if ( ffff != (short) 0xffff )
        {
            POILogFactory.getLogger(Sttb.class).log(
                    POILogger.WARN,
                    "Non-extended character Pascal strings are not supported right now. "
                            + "Creating empty values in the RevisionMarkAuthorTable for now.  " +
                    "Please, contact POI developers for update.");
            //set data and extraData to empty values to avoid
            //downstream NPE in case someone calls getEntries on RevisionMarkAuthorTable
            _data = new String[0];
            _extraData = new byte[0][];

            return;
        }
        // strings are extended character strings

        int cData = _cDataLength == 2 ? LittleEndian.getUShort( buffer, offset )
                : LittleEndian.getInt( buffer, offset );
        offset += _cDataLength;

        this._cbExtra = LittleEndian.getUShort( buffer, offset );
        offset += 2;

        _data = new String[cData];
        _extraData = new byte[cData][];

        for ( int i = 0; i < cData; i++ )
        {
            int cchData = LittleEndian.getShort( buffer, offset );
            offset += 2;

            if ( cchData < 0 )
                continue;

            _data[i] = StringUtil.getFromUnicodeLE( buffer, offset, cchData );
            offset += cchData * 2;

            _extraData[i] = LittleEndian
                    .getByteArray( buffer, offset, _cbExtra );
            offset += _cbExtra;
        }
    }

    /**
     * The definition of each STTB specifies the meaning of this field. If this
     * STTB uses extended characters, the size of this field is 2*cchData bytes
     * and it is a Unicode string unless otherwise specified by the STTB
     * definition. If this STTB does not use extended characters, then the size
     * of this field is cchData bytes and it is an ANSI string, unless otherwise
     * specified by the STTB definition.
     */
    public String[] getData()
    {
        return _data;
    }

    public int getSize()
    {
        // ffff
        int size = LittleEndian.SHORT_SIZE;

        // cData
        size += _cDataLength;

        // cbExtra
        size += LittleEndian.SHORT_SIZE;

        if ( this._fExtend )
        {
            for ( String data : _data )
            {
                // cchData
                size += LittleEndian.SHORT_SIZE;
                // data
                size += 2 * data.length();
            }
        }
        else
        {
            for ( String data : _data )
            {
                // cchData
                size += LittleEndian.BYTE_SIZE;
                // data
                size += 1 * data.length();
            }
        }

        // extraData
        if ( _extraData != null )
        {
            size += _cbExtra * _data.length;
        }

        return size;
    }

    public byte[] serialize()
    {
        final byte[] buffer = new byte[getSize()];

        LittleEndian.putShort( buffer, 0, (short) 0xffff );

        if ( _data == null || _data.length == 0 )
        {
            if ( _cDataLength == 4 )
            {
                LittleEndian.putInt( buffer, 2, 0 );
                LittleEndian.putUShort( buffer, 6, _cbExtra );
                return buffer;
            }

            LittleEndian.putUShort( buffer, 2, 0 );
            LittleEndian.putUShort( buffer, 4, _cbExtra );
            return buffer;
        }

        int offset;
        if ( _cDataLength == 4 )
        {
            LittleEndian.putInt( buffer, 2, _data.length );
            LittleEndian.putUShort( buffer, 6, _cbExtra );
            offset = 2 + LittleEndian.INT_SIZE + LittleEndian.SHORT_SIZE;
        }
        else
        {
            LittleEndian.putUShort( buffer, 2, _data.length );
            LittleEndian.putUShort( buffer, 4, _cbExtra );
            offset = 2 + LittleEndian.SHORT_SIZE + LittleEndian.SHORT_SIZE;
        }

        for ( int i = 0; i < _data.length; i++ )
        {
            String entry = _data[i];
            if ( entry == null )
            {
                // is it correct?
                buffer[offset] = -1;
                buffer[offset + 1] = 0;
                offset += 2;
                continue;
            }

            if ( _fExtend )
            {
                LittleEndian.putUShort( buffer, offset, entry.length() );
                offset += LittleEndian.SHORT_SIZE;

                StringUtil.putUnicodeLE( entry, buffer, offset );
                offset += 2 * entry.length();
            }
            else
            {
                throw new UnsupportedOperationException(
                        "ANSI STTB is not supported yet" );
            }

            if ( _cbExtra != 0 )
            {
                if ( _extraData[i] != null && _extraData[i].length != 0 )
                {
                    System.arraycopy( _extraData[i], 0, buffer, offset,
                            Math.min( _extraData[i].length, _cbExtra ) );
                }
                offset += _cbExtra;
            }
        }

        return buffer;
    }

    public int serialize( byte[] buffer, int offset )
    {
        byte[] bs = serialize();
        System.arraycopy( bs, 0, buffer, offset, bs.length );
        return bs.length;
    }
}
